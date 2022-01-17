package src.designPattern.chainOfResponsibity.requestAndResponse;

/**
 * @author caoyang
 */
public class UrlFilter implements Filter{
    @Override
    public boolean doFilter(Request request, Response response, FilterChain chain) {
        request.setRequest(request.getRequest().replace("http", "https"));
        System.out.println("UrlFilterRequest");
        System.out.println(request.getRequest());
        chain.doFilter(request, response, chain);
        response.setResponse(response.getResponse() + "->UrlFilter ");
        System.out.println("UrlFilterResponse");
        System.out.println(response.getResponse());
        return false;
    }
}
