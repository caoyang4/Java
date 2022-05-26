package src.designPattern.decorator;

/**
 * @author caoyang
 * @create 2022-05-26 17:44
 */
public class SsoDecorator implements HandlerInterceptor{

    private HandlerInterceptor handlerInterceptor;
    public SsoDecorator(HandlerInterceptor handlerInterceptor) {
        this.handlerInterceptor = handlerInterceptor;
    }
    @Override
    public boolean preHandle(String request, String response, Object handler) {
        return handlerInterceptor.preHandle(request, response, handler);
    }

}
