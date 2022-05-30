package src.rhino.onelimiter;

/**
 * Created by zhanjun on 2018/4/14.
 */
public class ExecuteResult {

    public static final ExecuteResult CONTINUE = new ExecuteResult(ExecuteStatus.CONTINUE);
    public static final ExecuteResult DIRECT_PASS = new ExecuteResult(ExecuteStatus.DIRECT_PASS);
    public static final ExecuteResult CONTINUE_AND_WARN = new ExecuteResult(ExecuteStatus.CONTINUE_AND_WARN);

    private int code;
    private String msg;
    private ExecuteStatus status;

    public ExecuteResult() {
    }

    public ExecuteResult(ExecuteStatus status) {
        this.status = status;
    }

    public static ExecuteResult createRejectResult(int code, String msg) {
        ExecuteResult result = new ExecuteResult();
        result.code = code;
        result.msg = msg;
        result.status = ExecuteStatus.DIRECT_REJECT;
        return result;
    }

    public ExecuteStatus getStatus() {
        return status;
    }

    public void setStatus(ExecuteStatus executeStatus) {
        this.status = executeStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "{\"status\": \"" + status + "\", \"code\": " + code + ", \"msg\": \"" + msg + "\"}";
    }
}
