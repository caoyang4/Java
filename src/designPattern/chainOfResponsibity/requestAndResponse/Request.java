package src.designPattern.chainOfResponsibity.requestAndResponse;

/**
 * @author caoyang
 */
public class Request {
    private String request;

    public Request() {
    }

    public Request(String request) {
        this.request = request;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}
