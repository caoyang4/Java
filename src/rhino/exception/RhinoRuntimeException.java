package src.rhino.exception;

/**
 * Created by zhanjun on 2017/4/26.
 */
public class RhinoRuntimeException extends RuntimeException {

    public RhinoRuntimeException() {
    }

    public RhinoRuntimeException(String message) {
        super(message);
    }

    public RhinoRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public RhinoRuntimeException(Throwable cause) {
        super(cause);
    }
}
