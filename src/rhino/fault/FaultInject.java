
package src.rhino.fault;

import java.util.concurrent.ConcurrentHashMap;

import src.rhino.RhinoType;
import src.rhino.RhinoUseMode;
import src.rhino.service.RhinoEntity;
import src.rhino.service.RhinoManager;
import src.rhino.util.AppUtils;

/**
 * @author zhanjun on 2017/4/25.
 */
public interface FaultInject {

    /**
     * check fault inject is active
     *
     * @param context
     * @return
     */
    boolean isActive(FaultInjectContext context);

    /**
     * inject fault
     *
     * @throws Exception
     */
    void inject() throws Exception;

    /**
     * inject fault with context
     *
     * @param context
     * @throws Exception
     */
    void inject(FaultInjectContext context) throws Exception;

    /**
     * 可以返回mock数据
     *
     * @param returnType
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T inject(Class<T> returnType) throws Exception;

    /**
     * return context
     *
     * @return
     */
    FaultInjectContext getContext();

    FaultInjectProperties getFaultInjectProperties();

    /*********************** Factory ***********************/

    class Factory {
        private static final FaultInject EMPTY = new NoOpFaultInject();

        private static final ConcurrentHashMap<String, FaultInject> faultInjects = new ConcurrentHashMap<>();

        public static FaultInject getInstance(String rhinoKey, FaultInjectProperties requestInjectProperties) {
            return getInstance(rhinoKey, requestInjectProperties, RhinoUseMode.API.getValue());
        }

        public static FaultInject getInstance(String rhinoKey, FaultInjectProperties requestInjectProperties, int useMode) {
            if (rhinoKey == null) {
                return EMPTY;
            }
            FaultInject faultInject = faultInjects.get(rhinoKey);
            if (faultInject != null) {
                return faultInject;
            }
            if (requestInjectProperties == null) {
                requestInjectProperties = new DefaultFaultInjectProperties(rhinoKey);
            }
            FaultInject faultInject1 = faultInjects.putIfAbsent(rhinoKey, new DefaultFaultInject(rhinoKey, requestInjectProperties));
            if (faultInject1 != null) {
                return faultInject1;
            }
            if (requestInjectProperties instanceof DefaultFaultInjectProperties) {
                RhinoManager.report(new RhinoEntity(rhinoKey, RhinoType.FaultInject, useMode, AppUtils.DEFAULT_CELL, null));
            }
            requestInjectProperties.addConfigChangedListener(null);
            return faultInjects.get(rhinoKey);
        }

        public static FaultInject getEMPTY() {
            return EMPTY;
        }

        public static FaultInject getFaultInject(String rhinoKey) {
            return faultInjects.get(rhinoKey);
        }
    }
}
