package src.rhino.server;

/**
 * Created by zmz on 2020/10/29.
 */
public interface RhinoHttpRequestHandler {
    Object handleCommand(String command, String rhinoKey, String... params);
}
