package src.rhino.server;

import java.util.HashMap;
import java.util.Map;

import com.mysql.cj.util.StringUtils;

/**
 * Created by zmz on 2020/10/29.
 */
public class RhinoHttpRequestHanlderDispatcher {

    protected static final Map<String, RhinoHttpRequestHandler> handlers = new HashMap<>();

    static {
        handlers.put("common", new CommonRequestHandler());
        handlers.put("threadpool", new ThreadPoolRequestHandler());
        handlers.put("circuit", new CircuitBreakerRequestHandler());
        handlers.put("limiter", new OneLimiterRequestHandler());
    }

    public static Object handleRequest(String uri) {
        if(StringUtils.isNullOrEmpty(uri)){
            return null;
        }

        String[] items = uri.split("/");
        if(items.length < 3){
            throw new IllegalArgumentException("Unsupported url: " + uri);
        }

        String handlerName = items[1];
        String command = items[2];
        String rhinoKey = items.length >= 4 ? items[3] : null;

        String[] params = null;
        if(items.length > 4){
            int paramsNum = items.length - 4;
            params = new String[paramsNum];
            for(int i=0; i<paramsNum; i++){
                params[i] = items[i+4];
            }
        }

        RhinoHttpRequestHandler handler = handlers.get(handlerName);
        if(handler == null){
            throw new IllegalArgumentException("Unsupported handler: " + handlerName);
        }
        return handler.handleCommand(command, rhinoKey, params);
    }

}
