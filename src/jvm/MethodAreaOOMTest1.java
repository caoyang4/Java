package src.jvm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

/**
 * 方法区OOM
 * -XX:MetaspaceSize=16m
 * -XX:MaxMetaspaceSize=16m
 */
@Slf4j(topic = "MethodAreaOOMTest")
public class MethodAreaOOMTest1 {
    public static void main(String[] args) {
        // 借助 cglib 代理创建对象
        int times = 0;
        try{
            for(;;){
                Enhancer enhancer = new Enhancer();
                enhancer.setSuperclass(OOMObject.class);
                // 禁用缓存
                enhancer.setUseCache(false);
                enhancer.setCallback((MethodInterceptor) (o, method, objects, methodProxy) -> methodProxy.invoke(o, objects));
                enhancer.create();
                times++;
            }
        } catch (OutOfMemoryError e){
            throw e;
        } finally {
            System.out.println("创建了"+times+"个代理对象之后，方法区发生OOM");
        }
    }
    static class OOMObject{

    }
}
