package src.designPattern.chainOfResponsibity.requestAndResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * req1 -> req2 -> req3
 *                  |
 * res1 <- res2 <- res3
 * @author caoyang
 */
public class FilterChain implements Filter{
    private List<Filter> filters = new ArrayList<>();
    volatile int index = 0;

    public FilterChain add(Filter filter){
        filters.add(filter);
        return this;
    }

    @Override
    public boolean doFilter(Request request, Response response, FilterChain chain) {
        if(index >= filters.size()){
            return false;
        }
        Filter filter = filters.get(index++);
        return filter.doFilter(request, response, chain);
    }

    public static void main(String[] args) {
        FilterChain chain = new FilterChain();
        chain.add(new UrlFilter()).add(new SensitiveFilter());
        chain.doFilter(new Request("[你好，长者] http://www.young.com"), new Response("闷声发大财"), chain);

    }
}
