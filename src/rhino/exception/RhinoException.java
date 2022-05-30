package src.rhino.exception;

/**
 * Created by zhanjun on 2017/5/9.
 */
public class RhinoException extends RuntimeException {

    public RhinoException(String msg) {
        super(msg);
    }

    public RhinoException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RhinoException(Throwable cause) {
        super(cause);
    }
}
