package src.rhino;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.cglib.proxy.Enhancer;

import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.util.StringHelper;

/**
 * @author zhanjun on 2017/4/21.
 */
public class RhinoServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(RhinoServiceFactory.class);
    private static Map<String, RhinoService> rhinoServices = new ConcurrentHashMap<>();

    /**
     * @param clazz
     * @param origin
     * @return
     * @throws Exception
     */
    public static Object create(Class clazz, Object origin) throws Exception {
        String fullName = StringHelper.generateKey(clazz);
        RhinoService service = rhinoServices.get(fullName);
        if (service == null) {
            synchronized (clazz) {
                if (!rhinoServices.containsKey(fullName)) {
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        RhinoMethodFactory.init(clazz, method, origin);
                    }
                    rhinoServices.put(fullName, new RhinoService());
                }
            }
        }
        return createProxyService(clazz, origin);
    }

    /**
     * @param clazz
     * @param origin
     * @return
     */
    private static Object createProxyService(Class clazz, Object origin) {
        Object proxy;
        if (clazz.getInterfaces().length > 0) {
            proxy = Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), new RhinoInvocationHandler(origin, clazz));
        } else {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(clazz);
            enhancer.setCallback(new RhinoInvocationHandler(origin, clazz));
            proxy = enhancer.create();
        }
        return proxy;
    }
}
