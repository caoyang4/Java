package src.rhino.fault;

/**
 * Created by zhanjun on 2017/6/2.
 */
public enum RpcException {

    rhinoInjectException("0", "src.rhino.exception.RhinoInjectException"),
    requestAbortedException("1", "src.pigeon.remoting.provider.exception.RequestAbortedException"),
    serializationException("2", "src.pigeon.remoting.common.exception.SerializationException"),
    badRequestException("3", "src.pigeon.remoting.common.exception.BadRequestException"),
    badResponseException("4", "src.pigeon.remoting.common.exception.BadResponseException"),
    networkException("5", "src.pigeon.remoting.common.exception.NetworkException"),
    requestTimeoutException("6", "src.pigeon.remoting.invoker.exception.RequestTimeoutException"),
    rejectedException("7", "src.pigeon.remoting.common.exception.RejectedException"),
    serviceUnavailableException("8", "src.pigeon.remoting.invoker.exception.ServiceUnavailableException");

    RpcException(String code, String clazz) {
        this.code = code;
        this.clazz = clazz;
    }

    private String code;
    private String clazz;

    public boolean isDefault() {
        return this == rhinoInjectException;
    }

    public static RpcException getDefault() {
        return rhinoInjectException;
    }
    public String getCode() {
        return code;
    }

    public String getClazz() {
        return clazz;
    }
}
