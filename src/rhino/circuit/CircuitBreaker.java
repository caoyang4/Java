package src.rhino.circuit;

import java.util.concurrent.ConcurrentHashMap;


import com.mysql.cj.util.StringUtils;
import src.rhino.RhinoType;
import src.rhino.RhinoUseMode;
import src.rhino.circuit.component.RpcCircuitBreakerManager;
import src.rhino.service.RhinoEntity;
import src.rhino.service.RhinoManager;
import src.rhino.util.AppUtils;
import src.rhino.util.CommonUtils;

/**
 * @author zhanjun on 2017/4/20.
 */
public interface CircuitBreaker {

    /**
     * CircuitBreaker is enable
     *
     * @return
     */
    boolean isEnable();

    /**
     * 获取熔断器状态
     *
     * @return
     */
    Status getStatus();

    /**
     * check the request is allow to get through
     *
     * @return
     */
    boolean allowRequest();

    /**
     * @param circuitBreakerContext
     * @return
     */
    boolean allowRequest(CircuitBreakerContext circuitBreakerContext);

    /**
     * mark the successful request
     */
    void setSuccess();

    /**
     * mark the successful request with context
     *
     * @param circuitBreakerContext
     */
    void setSuccess(CircuitBreakerContext circuitBreakerContext);

    /**
     * mark the failed request
     *
     * @param throwable
     */
    boolean setFailed(Throwable throwable);

    /**
     * mark the failed request with context
     *
     * @param throwable
     * @param circuitBreakerContext
     */
    boolean setFailed(Throwable throwable, CircuitBreakerContext circuitBreakerContext);

    /**
     * which executed in finally block
     */
    void complete();

    /**
     * add circuit breaker listener with 2 methods
     *
     * @param circuitBreakerListeners
     */
    void setCircuitBreakerListener(CircuitBreakerListener... circuitBreakerListeners);

    /**
     * is return degrade result when exception occur
     *
     * @return
     */
    boolean isFallbackOnException();

    /**
     * current context
     *
     * @return
     */
    CircuitBreakerContext getCircuitBreakerContext();

    /**
     * 标记请求被拒绝，主要在cat埋点
     */
    void markReject();

    /**
     * 是否使用默认降级策略
     *
     * @return
     */
    boolean isDefaultDegrade();

    /**
     * 执行降级逻辑
     *
     * @return
     */
    Object handleDegrade() throws Exception;

    /**
     * 获取熔断器属性（监控）
     *
     * @return CircuitBreakerProperties
     */
    CircuitBreakerProperties getCircuitBreakerProperties();

    enum Status {
        OPEN,
        HALF_OPEN,
        CLOSE
    }

    class Factory {

        private static final CircuitBreaker EMPTY = NoOpCircuitBreaker.INSTANCE;
        private static final ConcurrentHashMap<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

        /**
         * @param rhinoKey
         * @param active
         * @return
         */
        public static CircuitBreaker getInstance(String rhinoKey, boolean active) {
            return getInstance(rhinoKey, DefaultCircuitBreakerProperties.Setter().withActive(active));
        }

        /**
         * @param rhinoKey
         * @param setter
         * @return
         */
        public static CircuitBreaker getInstance(String rhinoKey, DefaultCircuitBreakerProperties.Setter setter) {
            return getInstance(rhinoKey, setter, RhinoUseMode.API);
        }

        /**
         * get or create CircuitBreaker with rhino key and circuitBreakerProperties
         *
         * @param rhinoKey
         * @param setter
         * @param useMode
         * @return
         */
        public static CircuitBreaker getInstance(String rhinoKey, DefaultCircuitBreakerProperties.Setter setter, RhinoUseMode useMode) {
            if (StringUtils.isNullOrEmpty(rhinoKey)) {
                return EMPTY;
            }
            CircuitBreaker circuitBreaker = circuitBreakers.get(rhinoKey);
            if (circuitBreaker != null) {
                return circuitBreaker;
            }
            if (setter == null) {
                setter = DefaultCircuitBreakerProperties.Setter();
            }
            DefaultCircuitBreakerProperties circuitBreakerProperties = new DefaultCircuitBreakerProperties(rhinoKey, setter);

            CircuitBreaker newOne = new CircuitBreakerProxy(rhinoKey, circuitBreakerProperties);

            CircuitBreaker oldOne = circuitBreakers.putIfAbsent(rhinoKey, newOne);

            // 保证并发下都返回同一个对象，并只有一个线程能上报
            if (oldOne == null) {
                if(useMode.isReport()){
                    RhinoManager.report(new RhinoEntity(rhinoKey, RhinoType.CircuitBreaker, useMode.getValue(), AppUtils.getSet(), CommonUtils.parseProperties(circuitBreakerProperties)));
                }
                circuitBreakerProperties.addConfigChangedListener(null);
                oldOne = newOne;
            }
            return oldOne;
        }

        /**
         * return no operation circuitBreaker
         *
         * @return
         */
        public static CircuitBreaker getEmpty() {
            return EMPTY;
        }

        public static CircuitBreaker getCircuitBreaker(String rhinoKey) {
            return circuitBreakers.get(rhinoKey);
        }

        /**
         * 根据服务名和方法名返回熔断器
         * @param serviceName
         * @param methodName
         * @return
         */
        public static CircuitBreaker getCircuitBreaker(String remoteAppkey, String serviceName, String methodName){
            //如果没有配置appkey，返回空熔断器
            String appkey = AppUtils.getAppName();
            if(StringUtils.isNullOrEmpty(appkey)){
                return EMPTY;
            }

            //如果参数不完整，返回空熔断器
            if(StringUtils.isNullOrEmpty(remoteAppkey) || StringUtils.isNullOrEmpty(serviceName) || StringUtils.isNullOrEmpty(methodName)){
                return EMPTY;
            }

            RpcCircuitBreakerManager clientConfig = RpcCircuitBreakerManager.getInstance();
            String rhinoKey = clientConfig.getRhinoKey(remoteAppkey, serviceName, methodName);

            if(StringUtils.isNullOrEmpty(rhinoKey)){
                //如果没有接入，返回空熔断器
                return EMPTY;
            }

            CircuitBreaker circuitBreaker = circuitBreakers.get(rhinoKey);
            if(circuitBreaker == null){
                //如果没有初始化完成，返回空熔断器
                return EMPTY;
            }else{
                return circuitBreaker;
            }
        }

    }
}
