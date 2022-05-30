package src.rhino.server;

/**
 * 测试Server端口是否正常
 * path: /common
 * Created by zmz on 2020/10/29.
 */
public class CommonRequestHandler implements RhinoHttpRequestHandler {

    @Override
    public Object handleCommand(String command, String rhinoKey, String... params) {
        switch (command){
            case "heartbeat":
                return "HelloWorld";
            default:
                throw new IllegalArgumentException("Unsurpport command: " + command);
        }
    }

}
