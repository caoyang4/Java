package src.rhino;

import src.rhino.config.ConfigChangedListener;
import src.rhino.config.PropertyChangedListener;

/**
 * @author zhanjun on 2017/6/30.
 */
public interface RhinoProperties {

    String configKeySuffix = "props";

    /**
     * return appKey specified for this properties
     *
     * @return
     */
    String getAppKey();

    /**
     * return rhinoType specified for this properties
     *
     * @return
     */
    RhinoType getRhinoType();

    /**
     * return rhinoKey specified for this properties
     *
     * @return
     */
    String getRhinoKey();

    /**
     * add config changed listener
     * @param listener
     */
    void addConfigChangedListener(ConfigChangedListener listener);

    /**
     * add property changed listener
     * @param listener
     */
    void addPropertyChangedListener(PropertyChangedListener listener);

    /**
     * properties to json
     * @return
     */
    String toJson();
}
