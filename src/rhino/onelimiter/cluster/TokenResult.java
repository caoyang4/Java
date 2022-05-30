package src.rhino.onelimiter.cluster;

import java.util.Map;

/**
 * @author Feng
 * @date 2020-06-17
 */
public class TokenResult {

	//status，TokenResultStatus
    private int status;
    
    //remain tokens,unused
    private int remaining;
    
    //wait time, millseconds
    private int waitInMs;
    
    // block message,unused
    private String msg;

    // attachments,unused
    private Map<String, String> attachments;

    public TokenResult() {}
    
    public TokenResult(int status) {
        this.status = status;
    }
    
    public TokenResult(int status,int waitInMs) {
        this.status = status;
        this.waitInMs = waitInMs;
    }

    public Integer getStatus() {
        return status;
    }

    public TokenResult setStatus(int status) {
        this.status = status;
        return this;
    }

    public int getRemaining() {
        return remaining;
    }

    public TokenResult setRemaining(int remaining) {
        this.remaining = remaining;
        return this;
    }

    public int getWaitInMs() {
        return waitInMs;
    }

    public TokenResult setWaitInMs(int waitInMs) {
        this.waitInMs = waitInMs;
        return this;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public TokenResult setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
        return this;
    }
    
    public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
    public String toString() {
        return "TokenResult{" +
            "status=" + status +
            ", remaining=" + remaining +
            ", waitInMs=" + waitInMs +
            ", attachments=" + attachments +
            ", msg=" + msg +
            '}';
    }
}
