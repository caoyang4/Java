package src.designPattern.chainOfResponsibity.requestAndResponse;

/**
 * @author caoyang
 */
public interface Filter {
    /**
     * 过滤
     */
    boolean doFilter(Request request, Response response, FilterChain chain);
}
