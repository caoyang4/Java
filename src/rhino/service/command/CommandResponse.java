package src.rhino.service.command;

/**
 * Created by zhen on 2018/11/29.
 */
public class CommandResponse {

    private static final int SUCCESS_CODE = 200;
    private static final int ERROR_CODE = -1;


    private int code;
    private String message;
    private Object data;

    public void setSuccess(Object data) {
        this.code = SUCCESS_CODE;
        this.data = data;
    }

    public void setError(String msg) {
        this.code = ERROR_CODE;
        this.message = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
