package src.rhino.circuit;

/**
 * Created by zhanjun on 2018/4/25.
 */
public abstract class CircuitBreakerCommand<R> {

    /**
     * 正常执行逻辑
     *
     * @return
     * @throws Exception
     */
    protected abstract R run() throws Exception;

    /**
     * 降级执行逻辑
     *
     * @return
     * @throws Exception
     */
    protected R getFallback() {
        throw new UnsupportedOperationException("No fallback available.");
    }
}
