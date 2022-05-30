package src.rhino.exception;

/**
 * Created by zhanjun on 2017/5/9.
 */
public class RhinoLimitRejectException extends RuntimeException {

    public RhinoLimitRejectException() {
    }

    public RhinoLimitRejectException(String msg) {
        super(msg);
    }

    public RhinoLimitRejectException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RhinoLimitRejectException(Throwable cause) {
        super(cause);
    }
}
