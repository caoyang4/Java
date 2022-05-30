package src.rhino.exception;

/**
 * Created by zhen on 2019/2/22.
 */
public class RhinoRetryInterruptedException extends RhinoException {

    public RhinoRetryInterruptedException(String msg) {
        super(msg);
    }

    public RhinoRetryInterruptedException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RhinoRetryInterruptedException(Throwable cause) {
        super(cause);
    }
}
