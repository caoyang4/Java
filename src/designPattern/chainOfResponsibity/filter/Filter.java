package src.designPattern.chainOfResponsibity.filter;

/**
 * @author caoyang
 */
public interface Filter<T> {
    /**
     * 过滤
     */
    T doFilter(T t);
}
