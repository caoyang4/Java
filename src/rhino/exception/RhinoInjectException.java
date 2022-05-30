package src.rhino.exception;

/**
 * Created by zhanjun on 2017/6/2.
 */
public class RhinoInjectException extends RuntimeException {

    public RhinoInjectException() {
    }

    public RhinoInjectException(String message) {
        super(message);
    }

    public RhinoInjectException(Throwable cause) {
        super(cause);
    }
}
