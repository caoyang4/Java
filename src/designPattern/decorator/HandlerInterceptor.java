package src.designPattern.decorator;

/**
 * @author caoyang
 * @create 2022-05-26 17:41
 */
public interface HandlerInterceptor {
    boolean preHandle(String request, String response, Object handler);
}
