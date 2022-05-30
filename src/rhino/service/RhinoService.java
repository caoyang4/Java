package src.rhino.service;

import src.rhino.exception.RhinoRuntimeException;

/**
 * @author zhanjun on 2017/7/6.
 */
public abstract class RhinoService extends HttpClient {

    private static String serviceApiKey = "rhino.service.api";
    private static String serviceApi;

    /*static {
        serviceApi = Lion.getStringValue(serviceApiKey, "");
        if (serviceApi.isEmpty()) {
            throw new RhinoRuntimeException("rhino service api is empty");
        }
    }*/

    public RhinoService(String method) {
        super(serviceApi + method);
    }
}
