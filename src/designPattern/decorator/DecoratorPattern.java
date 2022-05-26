package src.designPattern.decorator;

/**
 * new BufferedReader(new FileReader("")); 采用了装饰器模式
 * @author caoyang
 * @create 2022-05-26 17:35
 */
public class DecoratorPattern {
    public static void main(String[] args) {
        LoginSsoDecorator ssoDecorator = new LoginSsoDecorator(new SsoInterceptor());
        String request = "james success";
        boolean success = ssoDecorator.preHandle(request, "闷声发大财", "naive");
        System.out.println("登录校验:" + request + (success ? " 放行" : "拦截"));
    }
}
