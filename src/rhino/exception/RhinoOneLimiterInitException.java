package src.rhino.exception;

/**
 * Created by zhanjun on 2018/6/26.
 */
public class RhinoOneLimiterInitException extends RuntimeException {

    public RhinoOneLimiterInitException(String message) {
        super(message);
    }

    public RhinoOneLimiterInitException(Throwable cause) {
        super(cause);
    }
}