package src.rhino;

import java.util.concurrent.Future;

import src.rhino.circuit.DefaultCircuitBreakerProperties;
import src.rhino.threadpool.DefaultThreadPoolProperties;

/**
 * Created by zhanjun on 2018/4/26.
 */
public abstract class RhinoCommand<R> extends RhinoAbstractCommand<R> {

    public RhinoCommand(String rhinoKey) {
        this(rhinoKey, rhinoKey);
    }

    public RhinoCommand(String rhinoKey, String threadPoolKey) {
        this(Setter.withRhinoKey(rhinoKey).addCircuitBreakerProperties(DefaultCircuitBreakerProperties.Setter())
                .addThreadPoolKey(threadPoolKey).addThreadPoolProperties(DefaultThreadPoolProperties.Setter()));
    }

    public RhinoCommand(Setter setter) {
        super(setter.rhinoKey, setter.threadPoolKey, setter.circuitBreakerProperties, setter.threadPoolProperties);
    }

    /**
     * 同步执行
     *
     * @return
     * @throws Exception
     */
    @Override
    public R execute() throws Exception {
        return super.execute();
    }

    /**
     * 异步执行
     *
     * @return
     */
    public Future<R> queue() throws Exception {
        return executeAsync();
    }

    public final static class Setter {
        protected String rhinoKey;
        protected String threadPoolKey;
        protected DefaultCircuitBreakerProperties.Setter circuitBreakerProperties;
        protected DefaultThreadPoolProperties.Setter threadPoolProperties;

        private Setter(String rhinoKey) {
            this.rhinoKey = rhinoKey;
        }

        public static Setter withRhinoKey(String rhinoKey) {
            return new Setter(rhinoKey);
        }

        public Setter addThreadPoolKey(String threadPoolKey) {
            this.threadPoolKey = threadPoolKey;
            return this;
        }

        public Setter addCircuitBreakerProperties(DefaultCircuitBreakerProperties.Setter circuitBreakerProperties) {
            this.circuitBreakerProperties = circuitBreakerProperties;
            return this;
        }

        public Setter addThreadPoolProperties(DefaultThreadPoolProperties.Setter threadPoolProperties) {
            this.threadPoolProperties = threadPoolProperties;
            return this;
        }
    }
}
