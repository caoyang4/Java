![jdkProxy.png](jdkProxy.png)
代理动态生产字节码
内部通过反射
Proxy.newProxyInstance -> Class.getConstructor
ProxyClassFactory
proxyClassCache or ProxyGenerator.generateProxyClass
