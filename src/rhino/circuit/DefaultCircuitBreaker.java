package src.rhino.circuit;

import src.rhino.circuit.degrade.DegradeStrategy;
import src.rhino.circuit.forceopen.ForceOpenDegradeStrategy;
import src.rhino.circuit.listener.CircuitBreakerListenerTrigger;
import src.rhino.circuit.recover.RecoverStrategy;
import src.rhino.circuit.semaphore.TryableSemaphore;
import src.rhino.circuit.timeout.RhinoTimer;
import src.rhino.circuit.timeout.TimerListener;
import src.rhino.circuit.trigger.TriggerStrategy;
import src.rhino.dispatcher.RhinoEvent;
import src.rhino.dispatcher.RhinoEventDispatcher;
import src.rhino.exception.RhinoTimeoutException;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.metric.DefaultRhinoMetric;
import src.rhino.metric.HealthCountSummary;

import java.lang.ref.Reference;
import java.util.Arrays;
import java.util.Set;

/**
 * @author by zhanjun on 2017/4/21.
 */
public class DefaultCircuitBreaker implements CircuitBreaker {

    private static Logger logger = LoggerFactory.getLogger(DefaultCircuitBreaker.class);

    private String key;

    /**
     * 熔断器配置参数
     */
    private CircuitBreakerProperties circuitBreakerProperties;

    /**
     * 熔断器状态机
     */
    private CircuitBreakerRuntime circuitBreakerRuntime;

    /**
     * 数据收集器
     */
    private DefaultRhinoMetric metrics;

    /**
     * 熔断触发策略
     */
    private TriggerStrategy triggerStrategy;

    /**
     * 请求恢复策略
     */
    private RecoverStrategy recoverStrategy;

    /**
     * 请求降级策略
     */
    private DegradeStrategy degradeStrategy;

    /**
     * 熔断器强制开启，降级策略
     */
    private ForceOpenDegradeStrategy forceOpenDegradeStrategy;

    /**
     * 信号量
     */
    private TryableSemaphore tryableSemaphore;

    /**
     * 熔断回调函数
     */
    private CircuitBreakerListenerTrigger circuitBreakerListenerTrigger;

    /**
     * 熔断统计
     */
    private CircuitBreakerStatistic circuitBreakerStatistic;

    /**
     * 强制熔断监听
     */
    private CircuitBreakerForceOpenListener forceOpenListener;

    /**
     * 熔断事件分发器
     */
    private RhinoEventDispatcher eventDispatcher;

    /**
     * 熔断器请求上下文数据
     */
    private ThreadLocal<CircuitBreakerContext> circuitBreakerContextTl = new ThreadLocal<>();

    //压测熔断器标记为true，正常熔断器标记为false
    private boolean isTest = false;

    public DefaultCircuitBreaker(String key, CircuitBreakerProperties circuitBreakerProperties) {
        this.key = key;
        this.circuitBreakerProperties = circuitBreakerProperties;
        this.circuitBreakerRuntime = new CircuitBreakerRuntime();
        this.circuitBreakerStatistic = new CircuitBreakerStatistic();
        this.metrics = new DefaultRhinoMetric(key, circuitBreakerProperties);
        this.triggerStrategy = TriggerStrategy.Factory.create(circuitBreakerProperties);
        this.recoverStrategy = RecoverStrategy.Factory.create(circuitBreakerProperties);
        this.degradeStrategy = DegradeStrategy.Factory.create(circuitBreakerProperties);
        this.tryableSemaphore = TryableSemaphore.Factory.create(circuitBreakerProperties);
        this.forceOpenDegradeStrategy = ForceOpenDegradeStrategy.Factory.create(circuitBreakerProperties);
        this.eventDispatcher = new CircuitBreakerEventDispatcher(key);
        this.circuitBreakerListenerTrigger = new CircuitBreakerListenerTrigger(key, eventDispatcher);
        this.forceOpenListener = new CircuitBreakerForceOpenListener(key, circuitBreakerProperties,
                circuitBreakerStatistic, circuitBreakerListenerTrigger);

        // 如果启动的时候，强制熔断开关已经开启，则需要标记启动时间点，不然无法统计熔断时长
        if (circuitBreakerProperties.getIsForceOpen()) {
            circuitBreakerStatistic.markOpened(true);
        }
    }

    public DefaultCircuitBreaker(String key, CircuitBreakerProperties circuitBreakerProperties, boolean isTest) {
        this(key, circuitBreakerProperties);
        this.isTest = isTest;
    }

    @Override
    public boolean isEnable() {
        return circuitBreakerProperties.getIsActive();
    }

    @Override
    public Status getStatus() {
        if (circuitBreakerProperties.getIsForceOpen()) {
            return Status.OPEN;
        }

        return circuitBreakerRuntime.getCircuitStatus();
    }

    @Override
    public boolean allowRequest() {
        return allowRequest(null);
    }

    @Override
    public boolean allowRequest(CircuitBreakerContext circuitBreakerContext) {
        return !isEnable() || doAllowRequest(circuitBreakerContext);
    }

    /**
     * 只有状态为normal、singleTest返回true
     *
     * @return
     */
    private boolean doAllowRequest(CircuitBreakerContext circuitBreakerContext) {
        if (circuitBreakerContext == null) {
            circuitBreakerContext = new CircuitBreakerContext(isTest);
        }
        circuitBreakerContextTl.set(circuitBreakerContext);
        circuitBreakerContext.start(circuitBreakerProperties);
        boolean isForceOpen = circuitBreakerProperties.getIsForceOpen();
        RequestStatus requestStatus = isForceOpen ? (forceOpenDegradeStrategy == null ? RequestStatus.DEGRADE : forceOpenDegradeStrategy.getRequestStatus()) : getRequestStatus();
        circuitBreakerContext.setRequestStatus(requestStatus);
        // 提前埋点降级请求
        if (requestStatus.isDegrade()) {
            circuitBreakerStatistic.addDegradeCount(isForceOpen);
            eventDispatcher.dispatchEvent(new RhinoEvent(CircuitBreakerEventType.REQUEST_DEGRADE));
            circuitBreakerContextTl.remove();
            return false;
        }
        // 信号量检查
        if (tryableSemaphore.tryAcquire()) {
            addTimeoutChecker(circuitBreakerContext);
            return true;
        }
        // 请求被信号量拒绝
        eventDispatcher.dispatchEvent(new RhinoEvent(CircuitBreakerEventType.REQUEST_REJECT_SEMAPHORE));
        return false;
    }

    /**
     * 如果是正常请求或试探请求，且参数timeout大于0，则需要进行超时检查
     *
     * @param circuitBreakerContext
     */
    private void addTimeoutChecker(final CircuitBreakerContext circuitBreakerContext) {
        final long timeoutInMilliseconds = circuitBreakerProperties.getTimeoutInMilliseconds();
        if (timeoutInMilliseconds > 0) {
            TimerListener timerListener = new TimerListener() {
                // 如果执行了tick()方法
                // 且setRequestTimeout()返回true，说明主线程已经超时
                @Override
                public void tick() {
                    if (circuitBreakerContext.setRequestTimeout()) {
                        // 发生超时，主动记录，并判断是否触发熔断
                        setFailed(new RhinoTimeoutException(), circuitBreakerContext);
                        //中断主线程，抛出timeoutException
                        circuitBreakerContext.interrupt();
                        //清空定时器任务
                        circuitBreakerContext.clearTimeoutListener();
                        eventDispatcher.dispatchEvent(new RhinoEvent(CircuitBreakerEventType.REQUEST_TIME_OUT));
                    }
                }

                @Override
                public long getIntervalTimeInMilliseconds() {
                    return timeoutInMilliseconds;
                }
            };
            Reference<TimerListener> timerListenerRf = RhinoTimer.getInstance().addTimerListener(timerListener);
            circuitBreakerContext.setTimerListenerRf(timerListenerRf);
        }
    }

    /**
     * 请求状态判断，分流
     *
     * @return
     */
    private RequestStatus getRequestStatus() {
        Status status = circuitBreakerRuntime.getCircuitStatus();

        // 如果是未开启，则请求状态应该是正常的
        if (status == Status.CLOSE) {
            return RequestStatus.NORMAL;
        }

        // 如果是半开启，即试探成功，则开始恢复正常请求
        if (status == Status.HALF_OPEN) {
            return recoverRequest();
        }

        // 如果是开启状态，先判断是否要进行试探
        long sleepWindowInMilliseconds = circuitBreakerProperties.getSleepWindowInMilliseconds();
        if (circuitBreakerRuntime.allowHeartbeat(sleepWindowInMilliseconds)) {
            return RequestStatus.SINGLE_TEST;
        }
        return RequestStatus.DEGRADE;
    }

    /**
     * 试探请求成功之后，开始恢复正常请求
     *
     * @return
     */
    private RequestStatus recoverRequest() {
        long percentage = recoverStrategy.getPercent();
        if (percentage >= 100 && circuitBreakerRuntime.tryCloseCircuit()) {
            circuitBreakerStatistic.markClosed(false);
            CircuitBreakerEventData eventData = new CircuitBreakerEventData(circuitBreakerStatistic.getCircuitDurationInSecond(false),
                    circuitBreakerStatistic.getDegradeCount(false));
            circuitBreakerListenerTrigger.circuitBreakerClosed(CircuitBreakerEventType.CIRCUIT_BREAKER_AUTO_CLOSE, eventData);
            logger.info("[circuitBreaker] " + key + " auto close!!! " + eventData.toJson());
        }
        return recoverStrategy.doRecover(percentage);
    }

    /**
     * 跨线程上下文对象传递
     *
     * @return
     */
    @Override
    public CircuitBreakerContext getCircuitBreakerContext() {
        CircuitBreakerContext circuitBreakerContext = circuitBreakerContextTl.get();
        circuitBreakerContextTl.remove();
        return circuitBreakerContext;
    }

    @Override
    public void setSuccess() {
        doSuccess(circuitBreakerContextTl.get());
    }

    /**
     * 跨线程调用时使用
     *
     * @param circuitBreakerContext
     */
    @Override
    public void setSuccess(CircuitBreakerContext circuitBreakerContext) {
        doSuccess(circuitBreakerContext);
        doComplete(circuitBreakerContext);
    }

    /**
     * @param circuitBreakerContext
     */
    private void doSuccess(CircuitBreakerContext circuitBreakerContext) {
        if (circuitBreakerContext != null && isEnable()) {
            RequestStatus requestStatus = circuitBreakerContext.getRequestStatus();
            if (requestStatus.isDegrade()) {
                return;
            }
            // 如果有超时检查
            // 设置的interrupt标识可能被内部吃掉，并返回null
            // 需要重新判断
            if (circuitBreakerContext.setRequestComplete() && circuitBreakerContext.markSuccess()) {
                //避免与tick发生并发删除的NPE问题
                circuitBreakerContext.clearTimeoutListener();
                // 试探成功，重置统计数据
                if (requestStatus.isSingleTest()) {
                    metrics.reset();
                    recoverStrategy.reset();
                    circuitBreakerRuntime.setTestSuccess();
                    eventDispatcher.dispatchEvent(new RhinoEvent(CircuitBreakerEventType.HEARTBEAT_SUCCESS));
                    logger.info("[circuitBreaker] " + key + " heartbeat success!!!");
                } else {
                    metrics.markSuccess();
                    eventDispatcher.dispatchEvent(new RhinoEvent(CircuitBreakerEventType.REQUEST_NORMAL_SUCCESS));
                }
            }
        }
    }

    /**
     * 排除试探请求的统计(如果前几次试探失败之后再成功, 这时的失败率比较高，导致会再次设置重新试探)
     * 排除降级请求的统计
     *
     * @param throwable
     */
    @Override
    public boolean setFailed(Throwable throwable) {
        if (isIgnoredException(throwable)) {
            //如果是可忽略异常，就判定为成功请求
            setSuccess();
            return false;
        }
        doFailed(throwable, circuitBreakerContextTl.get());
        return true;
    }

    /**
     * 跨线程调用时使用
     *
     * @param throwable
     * @param circuitBreakerContext
     */
    @Override
    public boolean setFailed(Throwable throwable, CircuitBreakerContext circuitBreakerContext) {
        if (isIgnoredException(throwable)) {
            //如果是可忽略异常，就判定为成功请求
            setSuccess(circuitBreakerContext);
            return false;
        }
        doFailed(throwable, circuitBreakerContext);
        doComplete(circuitBreakerContext);
        return true;
    }

    /**
     * 判断异常是否在忽略集合中
     *
     * @param throwable
     * @return
     */
    private boolean isIgnoredException(Throwable throwable) {
        Set<Class<? extends Exception>> ignoredExceptions = circuitBreakerProperties.getIgnoredExceptions();
        if (ignoredExceptions == null || ignoredExceptions.isEmpty()) {
            return false;
        }
        for (Class<? extends Throwable> ignoreException : ignoredExceptions) {
            if (ignoreException.isAssignableFrom(throwable.getClass())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param throwable
     * @param circuitBreakerContext
     */
    private void doFailed(Throwable throwable, CircuitBreakerContext circuitBreakerContext) {
        if (circuitBreakerContext != null && isEnable() && circuitBreakerContext.markFailed()) {
            RequestStatus requestStatus = circuitBreakerContext.getRequestStatus();
            // 忽略降级请求
            if (requestStatus.isDegrade()) {
                return;
            }
            circuitBreakerContext.clearTimeoutListener();
            // 忽略试探请求
            if (requestStatus.isSingleTest()) {
                eventDispatcher.dispatchEvent(new RhinoEvent(CircuitBreakerEventType.HEARTBEAT_FAILED));
                logger.info("[circuitBreaker] " + key + " heartbeat failed!!!");
                return;
            }
            metrics.markFailed(throwable);
            HealthCountSummary health = metrics.getHealthCount();

            // 接口失败率过高，触发熔断，分成两种情况：1、正常 2、恢复过程中
            if (triggerStrategy.trigger(health)) {

                // 1、当前是CLOSE状态，则开启熔断
                if (circuitBreakerRuntime.tryOpenCircuit()) {
                    circuitBreakerStatistic.markOpened(false);
                    CircuitBreakerEventData eventData = new CircuitBreakerEventData(health, metrics.getExceptionSummary());
                    circuitBreakerListenerTrigger.circuitBreakerOpened(CircuitBreakerEventType.CIRCUIT_BREAKER_AUTO_OPEN, eventData);
                    logger.info("[circuitBreaker] " + key + " auto open!!! health data -> " + eventData.toJson());
                    // 记录触发熔断的最后一个异常详情，方便定位问题
                    logger.error("[circuitBreaker] " + key + " auto open!!!", throwable);
                }

                // 当前是HALF_OPEN状态，则重新开启熔断，重新试探
                circuitBreakerRuntime.setTestFailed();
            }

            // 如果配置了发生异常就降级，则需要重新设置requestStatus
            if (isFallbackOnException()) {
                eventDispatcher.dispatchEvent(new RhinoEvent(CircuitBreakerEventType.REQUEST_FAILED_DEGREE));
            } else {
                eventDispatcher.dispatchEvent(new RhinoEvent(CircuitBreakerEventType.REQUEST_NORMAL_FAILED));
            }
        }
    }

    @Override
    public void complete() {
        doComplete(circuitBreakerContextTl.get());
        circuitBreakerContextTl.remove();
    }

    /**
     * @param circuitBreakerContext
     */
    private void doComplete(CircuitBreakerContext circuitBreakerContext) {
        if (circuitBreakerContext != null && isEnable()) {
            if (circuitBreakerContext.markComplete() && !circuitBreakerContext.getRequestStatus().isDegrade()) {
                tryableSemaphore.release();
            }
            circuitBreakerContext.clearTimeoutListener();
            circuitBreakerContext.clearInterrupted();
        }
    }

    @Override
    public void setCircuitBreakerListener(CircuitBreakerListener... circuitBreakerListeners) {
        if (circuitBreakerListeners != null && circuitBreakerListeners.length > 0) {
            circuitBreakerListenerTrigger.setCircuitBreakerListenerList(Arrays.asList(circuitBreakerListeners));
        }
    }

    @Override
    public boolean isFallbackOnException() {
        return circuitBreakerProperties.getIsActive() && circuitBreakerProperties.getIsDegradeOnException();
    }

    /**
     * 标记流量被拒绝
     */
    @Override
    public void markReject() {
        eventDispatcher.dispatchEvent(new RhinoEvent(CircuitBreakerEventType.REQUEST_REJECT));
    }

    /**
     * 判断当前降级策略是否为默认策略
     *
     * @return
     */
    @Override
    public boolean isDefaultDegrade() {
        return degradeStrategy.isDefault();
    }

    /**
     * 执行降级逻辑
     *
     * @return
     */
    @Override
    public Object handleDegrade() throws Exception {
        return degradeStrategy.degrade();
    }

    @Override
    public CircuitBreakerProperties getCircuitBreakerProperties() {
        return circuitBreakerProperties;
    }
}
