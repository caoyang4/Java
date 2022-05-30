package src.rhino.exception;

/**
 * @author zhanjun on 2017/6/8.
 */
public class RhinoTimeoutException extends RuntimeException {

    public RhinoTimeoutException() {
    }

    public RhinoTimeoutException(String message) {
        super(message);
    }

    public RhinoTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
