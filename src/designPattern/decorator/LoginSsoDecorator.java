package src.designPattern.decorator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author caoyang
 * @create 2022-05-26 17:44
 */
public class LoginSsoDecorator extends SsoDecorator{
    private static Map<String, String> authMap = new ConcurrentHashMap<String, String>();
    static {
        authMap.put("james", "queryUserInfo");
    }
    public LoginSsoDecorator(HandlerInterceptor handlerInterceptor) {
        super(handlerInterceptor);
    }
    @Override
    public boolean preHandle(String request, String response, Object handler) {
        boolean success = super.preHandle(request, response, handler);
        if (!success) return false;
        String userId = request.substring(0, 5);
        System.out.println(userId);
        String method = authMap.get(userId);
        return "queryUserInfo".equals(method);
    }
}
