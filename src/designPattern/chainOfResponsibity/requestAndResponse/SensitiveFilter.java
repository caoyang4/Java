package src.designPattern.chainOfResponsibity.requestAndResponse;

/**
 * @author caoyang
 */
public class SensitiveFilter implements Filter{
    @Override
    public boolean doFilter(Request request, Response response, FilterChain chain) {
        request.setRequest(request.getRequest().replace("长者", "elder"));
        System.out.println("SensitiveFilterRequest");
        System.out.println(request.getRequest());
        chain.doFilter(request, response, chain);
        response.setResponse(response.getResponse() + "->SensitiveFilter ");
        System.out.println("SensitiveFilterResponse");
        System.out.println(response.getResponse());
        return false;
    }
}
