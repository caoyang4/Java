package src.designPattern.chainOfResponsibity.filter;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 责任链模式
 * input -> | -> | -> | 链式传递
 * @author caoyang
 */
public class FilterChain implements Filter<Comment> {

    private List<Filter> filters = new ArrayList<>();

    public FilterChain add(Filter filter){
        filters.add(filter);
        return this;
    }

    @Override
    public Comment doFilter(@NotNull Comment comment) {
        for (Filter filter : filters) {
            filter.doFilter(comment);
        }
        return comment;
    }

    public static void main(String[] args) {
        FilterChain filter = new FilterChain();
        FilterChain filter2 = new FilterChain();
        filter2.add(new CityFilter());
        filter.add(new NameFilter()).add(new SensitiveFilter()).add(filter2);
        Comment comment = new Comment();
        comment.setComment("Shanghai caoyang says: 996 is perfect!");
        System.out.println(comment);
        System.out.println("after doFilter...");
        filter.doFilter(comment);
        System.out.println(comment);
    }
}

