package src.rhino.exception;

/**
 * @author zhanjun
 * 只在内部使用，用于故障模拟忽略异常
 */
public class RhinoIgnoreException extends Exception {

    public RhinoIgnoreException() {
        super();
    }

    public RhinoIgnoreException(String message) {
        super(message);
    }

    public RhinoIgnoreException(Throwable cause) {
        super(cause);
    }

    public RhinoIgnoreException(String message, Throwable cause) {
        super(message, cause);
    }
}