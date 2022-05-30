
package src.rhino.fault;

import java.io.IOException;

import src.rhino.RhinoProperties;
import src.rhino.config.Configuration;
import src.rhino.fault.type.FaultType;

/**
 * @author zhanjun on 2017/4/28.
 */
public interface FaultInjectProperties extends RhinoProperties {

    boolean default_isActive = false;

    int default_faultType = FaultType.EXCEPTION.getValue();

    float default_sampleRate = 0.0f;

    boolean default_isRandomDelay = false;

    int default_maxDelay = 500;

    /**
     * return component is active or not
     *
     * @return
     */
    boolean getIsActive();

    /**
     * return component is active or not
     *
     * @return
     */
    boolean getIsActive(FaultInjectContext context);

    /**
     * return the type of simulate fail
     *
     * @return
     */
    int getType();

    /**
     * return the sample rate of request
     *
     * @return
     */
    float getSampleRate();

    /**
     * return the max delay of latency simulate
     *
     * @return
     */
    int getMaxDelay();

    /**
     * return is random latency or not
     *
     * @return
     */
    boolean getIsRandomDelay();

    /**
     * return the type of exception simulate
     *
     * @return
     */
    String getExceptionType();

    /**
     * 返回mock对象
     * @param returnType
     * @param <T>
     * @return
     * @throws IOException
     */
    <T> T getMockValue(Class<T> returnType) throws IOException;

    class Factory {

        /**
         * create DefaultRequestInjectProperties with specified rhino key
         *
         * @param rhinoKey
         * @return
         */
        public static FaultInjectProperties create(String rhinoKey) {
            return new DefaultFaultInjectProperties(rhinoKey);
        }

        /**
         * create DefaultRequestInjectProperties with specified app key, rhino key, configuration
         *
         * @param appKey
         * @param rhinoKey
         * @param configuration
         * @return
         */
        public static FaultInjectProperties create(String appKey, String rhinoKey,
                Configuration configuration) {
            return new DefaultFaultInjectProperties(appKey, rhinoKey, configuration);
        }
    }
}
