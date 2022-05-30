package src.rhino.circuit.component;

import java.util.Map;
import java.util.Set;

import src.rhino.RhinoType;
import src.rhino.RhinoUseMode;
import src.rhino.circuit.CircuitBreaker;
import src.rhino.util.AppUtils;

/**
 * rpc客户端熔断器配置
 * Created by zmz on 2020/2/11.
 */
public class RpcCircuitBreakerManager extends ComponentCircuitBreakerManager {

    private static volatile RpcCircuitBreakerManager instance = null;

    public static RpcCircuitBreakerManager getInstance(){
        if(instance == null){
            synchronized (RpcCircuitBreakerManager.class){
                if(instance == null){
                    String appkey = AppUtils.getAppName();
                    instance = new RpcCircuitBreakerManager(appkey);
                }
            }
        }
        return instance;
    }

    /**
     * 触动触发instance初始化，避免在请求到达时初始化造成延迟
     */
    public static void init(){
        getInstance();
    }

    private RpcCircuitBreakerManager(String appkey){
        super(appkey);
    }

    @Override
    protected String getComponentConfigKey() {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(appkey).append(".");
        keyBuilder.append(RhinoType.RPCCircuitBreaker.getTag()).append(".");
        keyBuilder.append("rpc.client.config");
        return keyBuilder.toString();
    }

    /**
     * 客户端配置初始化后，初始化熔断器
     */
    @Override
    protected void postConfigInit() {
        initOrUpdateCircuitBreakers(configInfo, "initRpcCircuitBreakers");
    }

    /**
     * 客户端配置变更中，创建新添加的熔断器
     * 这里可以只处理新增的熔断器，对于删除的熔断器是获取不到rhinokey的，但是会留在内存中
     * @param oldValue
     * @param newValue
     */
    @Override
    protected void postConfigChange(String oldValue, String newValue) {
        initOrUpdateCircuitBreakers(configInfo, "updateRpcCircuitBreakers");
    }

    /**
     * 初始化或者更新熔断器
     * @param config
     * @param type
     */
    private void initOrUpdateCircuitBreakers(Map<String, Set<String>> config, String type){
        if(config == null){
            return;
        }

        for(String servcie : config.keySet()){
            Set<String> methods = config.get(servcie);

            if(methods == null || methods.isEmpty()){
                //初始化service熔断器
                String rhinoKey = getServiceCircuitRhinoKey(servcie);
                CircuitBreaker.Factory.getInstance(rhinoKey, null, RhinoUseMode.RPC_CLIENT);
            }else{
                //初始化method熔断器
                for(String method : methods){
                    String rhinoKey = getMethodCircuitRhinoKey(servcie, method);
                    CircuitBreaker.Factory.getInstance(rhinoKey, null, RhinoUseMode.RPC_CLIENT);
                }
            }
        }
    }

    @Override
    protected String getCatTransactionName() {
        return "RpcConfigChange";
    }

    /**
     * 根据客户端配置获取rhinokey
     * 如果只配置了service（methods为空），表示接入service熔断器
     * 如果配置了service及其methods列表，则service熔断器失效，接入对应的methdos熔断器
     * 否则返回空
     * @param remoteAppkey 服务端appkey
     * @param service
     * @param method
     * @return
     */
    public String getRhinoKey(String remoteAppkey, String service, String method){
        String serviceKey = remoteAppkey + "-" + service;

        if(isServiceCircuitBreaker(serviceKey)){
            return getServiceCircuitRhinoKey(serviceKey);
        }else if(isMethodCircuitBreaker(serviceKey, method)){
            return getMethodCircuitRhinoKey(serviceKey, method);
        }else{
            return null;
        }
    }

    /**
     * 是否打开了service熔断器
     * config包含service，并且没有method熔断器
     * @param service
     * @return
     */
    private boolean isServiceCircuitBreaker(String serviceKey){
        if(configInfo.containsKey(serviceKey)){
            Set<String> methods = configInfo.get(serviceKey);
            return methods == null || methods.isEmpty();
        }else{
            return false;
        }
    }

    /**
     * 是否打开了method熔断器
     * config包含对应的service以及method
     * @param serviceKey remoteAppkey-service
     * @param method
     * @return
     */
    private boolean isMethodCircuitBreaker(String serviceKey, String method){
        Set<String> methods = configInfo.get(serviceKey);
        return methods != null && methods.contains(method);
    }

    /**
     * service熔断器的rhinokey
     * @param servcieKey remoteAppkey=service(需要是包全名！）
     * @return
     */
    private String getServiceCircuitRhinoKey(String servcieKey){
        return servcieKey;
    }

    /**
     * method熔断器的rhinokey
     * @param service remoteAppkey=service(需要是包全名！）
     * @param method 方法名
     * @return
     */
    private String getMethodCircuitRhinoKey(String serviceKey, String method){
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(serviceKey).append(".");
        keyBuilder.append(method);
        return keyBuilder.toString();
    }
}
