package src.designPattern.decorator;

/**
 * @author caoyang
 * @create 2022-05-26 17:42
 */
public class SsoInterceptor implements HandlerInterceptor{
    @Override
    public boolean preHandle(String request, String response, Object handler) {
        // 模拟校验
        return request.contains("success");
    }
}
