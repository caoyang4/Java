package java.lang.reflect;

import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import sun.misc.ProxyGenerator;
import sun.misc.VM;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.reflect.misc.ReflectUtil;
import sun.security.util.SecurityConstants;

/**
 * java中的代理模式：
 * 定义：给目标对象提供一个代理对象，并且由代理对象控制对目标对象的引用
 * 目的：
 *   ①：通过代理对象的方式间接的访问目标对象，防止直接访问目标对象给系统带来不必要的复杂性
 *   ②：通过代理业务对原有业务进行增强
 * java当中有三种方式来创建代理对象：静态代理，jdk(基于接口)动态代理， cglib(基于父类)动态代理。
 *   静态代理违反了开闭原则
 *
 * 静态代理：代理对象和实际对象都继承了同一个接口，在代理对象中指向的是实际对象的实例，
 *   优点：可以很好的保护实际对象的业务逻辑对外暴露，从而提高安全性。
 *   缺点：不同的接口要有不同的代理类实现，会很冗余
 * JDK 动态代理：
 *   为了解决静态代理中，生成大量的代理类造成的冗余；
 *   只需要实现 InvocationHandler 接口，重写 invoke 方法便可以完成代理的实现，
 *   利用反射生成代理类 Proxyxx.class 代理类字节码，并生成对象
 *   jdk动态代理之所以只能代理接口是因为代理类本身已经extends了Proxy，而java是不允许多重继承的，但是允许实现多个接口
 *   优点：解决了静态代理中冗余的代理实现类问题。
 *   缺点：JDK 动态代理是基于接口设计实现的，如果没有接口，会抛异常。
 *
 * CGLIB 代理：
 *   由于 JDK 动态代理限制了只能基于接口设计，而对于没有接口的情况，JDK方式解决不了；
 *   CGLib 采用了非常底层的字节码技术，其原理是通过字节码技术为一个类创建子类，并在子类中采用方法拦截的技术拦截所有父类方法的调用，顺势织入横切逻辑，来完成动态代理的实现。
 *   实现方式实现 MethodInterceptor 接口，重写 intercept 方法，通过 Enhancer 类的回调方法来实现。
 *   但是CGLib在创建代理对象时所花费的时间却比JDK多得多，所以对于单例的对象，因为无需频繁创建对象，用CGLib合适，反之，使用JDK方式要更为合适一些。
 *   同时，由于CGLib由于是采用动态创建子类的方法，对于final方法，无法进行代理。
 *   优点：没有接口也能实现动态代理，而且采用字节码增强技术，性能也不错。
 *   缺点：技术实现较高
 */
public class Proxy implements java.io.Serializable {

    private static final long serialVersionUID = -2222568056686623797L;

    private static final Class<?>[] constructorParams = { InvocationHandler.class };
    // Proxy类通过一个WeakCache类的实例来缓存代理类
    private static final WeakCache<ClassLoader, Class<?>[], Class<?>> proxyClassCache = new WeakCache<>(new KeyFactory(), new ProxyClassFactory());
    // 事件处理器
    protected InvocationHandler h;

    private Proxy() {
    }

    protected Proxy(InvocationHandler h) {
        Objects.requireNonNull(h);
        this.h = h;
    }

    @CallerSensitive
    public static Class<?> getProxyClass(ClassLoader loader, Class<?>... interfaces) throws IllegalArgumentException {
        final Class<?>[] intfs = interfaces.clone();
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkProxyAccess(Reflection.getCallerClass(), loader, intfs);
        }

        return getProxyClass0(loader, intfs);
    }

    private static void checkProxyAccess(Class<?> caller, ClassLoader loader, Class<?>... interfaces) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            ClassLoader ccl = caller.getClassLoader();
            if (VM.isSystemDomainLoader(loader) && !VM.isSystemDomainLoader(ccl)) {
                sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);
            }
            ReflectUtil.checkProxyPackageAccess(ccl, interfaces);
        }
    }

    private static Class<?> getProxyClass0(ClassLoader loader, Class<?>... interfaces) {
        if (interfaces.length > 65535) {
            throw new IllegalArgumentException("interface limit exceeded");
        }

       return proxyClassCache.get(loader, interfaces);
    }

    private static final Object key0 = new Object();

    private static final class Key1 extends WeakReference<Class<?>> {
        private final int hash;

        Key1(Class<?> intf) {
            super(intf);
            this.hash = intf.hashCode();
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            Class<?> intf;
            return this == obj ||
                   obj != null &&
                   obj.getClass() == Key1.class &&
                   (intf = get()) != null &&
                   intf == ((Key1) obj).get();
        }
    }


    private static final class Key2 extends WeakReference<Class<?>> {
        private final int hash;
        private final WeakReference<Class<?>> ref2;

        Key2(Class<?> intf1, Class<?> intf2) {
            super(intf1);
            hash = 31 * intf1.hashCode() + intf2.hashCode();
            ref2 = new WeakReference<Class<?>>(intf2);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            Class<?> intf1, intf2;
            return this == obj ||
                   obj != null &&
                   obj.getClass() == Key2.class &&
                   (intf1 = get()) != null &&
                   intf1 == ((Key2) obj).get() &&
                   (intf2 = ref2.get()) != null &&
                   intf2 == ((Key2) obj).ref2.get();
        }
    }


    private static final class KeyX {
        private final int hash;
        private final WeakReference<Class<?>>[] refs;

        @SuppressWarnings("unchecked")
        KeyX(Class<?>[] interfaces) {
            hash = Arrays.hashCode(interfaces);
            refs = (WeakReference<Class<?>>[])new WeakReference<?>[interfaces.length];
            for (int i = 0; i < interfaces.length; i++) {
                refs[i] = new WeakReference<>(interfaces[i]);
            }
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj ||
                   obj != null &&
                   obj.getClass() == KeyX.class &&
                   equals(refs, ((KeyX) obj).refs);
        }

        private static boolean equals(WeakReference<Class<?>>[] refs1, WeakReference<Class<?>>[] refs2) {
            if (refs1.length != refs2.length) {
                return false;
            }
            for (int i = 0; i < refs1.length; i++) {
                Class<?> intf = refs1[i].get();
                if (intf == null || intf != refs2[i].get()) {
                    return false;
                }
            }
            return true;
        }
    }

    private static final class KeyFactory implements BiFunction<ClassLoader, Class<?>[], Object> {
        @Override
        public Object apply(ClassLoader classLoader, Class<?>[] interfaces) {
            switch (interfaces.length) {
                case 1: return new Key1(interfaces[0]); // the most frequent
                case 2: return new Key2(interfaces[0], interfaces[1]);
                case 0: return key0;
                default: return new KeyX(interfaces);
            }
        }
    }

    private static final class ProxyClassFactory implements BiFunction<ClassLoader, Class<?>[], Class<?>> {
        // 生成的class名称前缀
        private static final String proxyClassNamePrefix = "$Proxy";

        // 名字的自增标识
        private static final AtomicLong nextUniqueNumber = new AtomicLong();

        /**
         * @param loader  类加载器
         * @param interfaces 接口
         */
        @Override
        public Class<?> apply(ClassLoader loader, Class<?>[] interfaces) {
            // IdentityHashMap以对象地址为 key
            Map<Class<?>, Boolean> interfaceSet = new IdentityHashMap<>(interfaces.length);
            for (Class<?> intf : interfaces) {
                // Verify that the class loader resolves the name of this interface to the same Class object.
                Class<?> interfaceClass = null;
                try {
                    // 加载传入的接口
                    interfaceClass = Class.forName(intf.getName(), false, loader);
                } catch (ClassNotFoundException e) {
                }
                if (interfaceClass != intf) {
                    throw new IllegalArgumentException(intf + " is not visible from class loader");
                }
                // 不是接口抛出异常，JDK动态代理只能代理接口
                if (!interfaceClass.isInterface()) {
                    throw new IllegalArgumentException(interfaceClass.getName() + " is not an interface");
                }
                // 接口不能重复
                if (interfaceSet.put(interfaceClass, Boolean.TRUE) != null) {
                    throw new IllegalArgumentException("repeated interface: " + interfaceClass.getName());
                }
            }
            // 代理类的包名
            String proxyPkg = null;
            // 代理类访问标志，默认是public final
            int accessFlags = Modifier.PUBLIC | Modifier.FINAL;

            // 如果接口有一个是非public的，就设置代理类包名与接口包名相同
            for (Class<?> intf : interfaces) {
                int flags = intf.getModifiers();
                if (!Modifier.isPublic(flags)) {
                    // 更改代理类访问标志为final
                    accessFlags = Modifier.FINAL;
                    // 获取接口的全限定名
                    String name = intf.getName();
                    int n = name.lastIndexOf('.');
                    String pkg = ((n == -1) ? "" : name.substring(0, n + 1));
                    if (proxyPkg == null) {
                        // 首次遇到非public的接口，将代理类包名设置与接口包名相同
                        proxyPkg = pkg;
                    } else if (!pkg.equals(proxyPkg)) {
                        throw new IllegalArgumentException("non-public interfaces from different packages");
                    }
                }
            }

            if (proxyPkg == null) {
                // 如果接口全是public的，代理类放在默认包下：com.sun.proxy
                proxyPkg = ReflectUtil.PROXY_PACKAGE + ".";
            }

            // num自增
            long num = nextUniqueNumber.getAndIncrement();
            // 生成的代理类名称 包名.$Proxy0
            String proxyName = proxyPkg + proxyClassNamePrefix + num;

            // sun.misc.ProxyGenerator工具生成代理类的字节流
            byte[] proxyClassFile = ProxyGenerator.generateProxyClass(proxyName, interfaces, accessFlags);
            try {
                // 根据字节流生成Class对象即代理类
                return defineClass0(loader, proxyName, proxyClassFile, 0, proxyClassFile.length);
            } catch (ClassFormatError e) {
                throw new IllegalArgumentException(e.toString());
            }
        }
    }

    /**
     * 创建代理类的静态方法
     * @param loader     类加载器
     * @param interfaces 接口
     * @param h          事件处理,执行target对象的方法时，会触发事件处理器的方法，会把当前执行target对象的方法作为参数传入
     *                   InvocationHandler就像一个中介，代理对象中关联了InvocationHandler对象，InvocationHandler对象中关联了被代理对象。
     *                   代理对象调用方法时直接调用InvocationHandler对象的invoke()方法，在InvocationHandler的invoke()方法中再调用被代理对象的方法，
     *                   同时在invoke()中可以做一些额外的工作
     */
    @CallerSensitive
    public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h) throws IllegalArgumentException {
        // 传入的代理类非空
        Objects.requireNonNull(h);
        // 使用克隆的方式拿到接口
        final Class<?>[] intfs = interfaces.clone();
        // 系统安全性检查
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkProxyAccess(Reflection.getCallerClass(), loader, intfs);
        }

        // 查找或者生成一个代理类
        Class<?> cl = getProxyClass0(loader, intfs);

        // 使用构造方法创建代理对象
        try {
            if (sm != null) {
                checkNewProxyPermission(Reflection.getCallerClass(), cl);
            }

            final Constructor<?> cons = cl.getConstructor(constructorParams);
            final InvocationHandler ih = h;
            // 如果不是 public，则赋权
            if (!Modifier.isPublic(cl.getModifiers())) {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        cons.setAccessible(true);
                        return null;
                    }
                });
            }
            // 使用构造器，返回new的对象
            return cons.newInstance(new Object[]{h});
        } catch (IllegalAccessException|InstantiationException e) {
            throw new InternalError(e.toString(), e);
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new InternalError(t.toString(), t);
            }
        } catch (NoSuchMethodException e) {
            throw new InternalError(e.toString(), e);
        }
    }

    private static void checkNewProxyPermission(Class<?> caller, Class<?> proxyClass) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (ReflectUtil.isNonPublicProxyClass(proxyClass)) {
                ClassLoader ccl = caller.getClassLoader();
                ClassLoader pcl = proxyClass.getClassLoader();

                // do permission check if the caller is in a different runtime package
                // of the proxy class
                int n = proxyClass.getName().lastIndexOf('.');
                String pkg = (n == -1) ? "" : proxyClass.getName().substring(0, n);

                n = caller.getName().lastIndexOf('.');
                String callerPkg = (n == -1) ? "" : caller.getName().substring(0, n);

                if (pcl != ccl || !pkg.equals(callerPkg)) {
                    sm.checkPermission(new ReflectPermission("newProxyInPackage." + pkg));
                }
            }
        }
    }
    // 是否是代理对象
    public static boolean isProxyClass(Class<?> cl) {
        return Proxy.class.isAssignableFrom(cl) && proxyClassCache.containsValue(cl);
    }

    @CallerSensitive
    public static InvocationHandler getInvocationHandler(Object proxy) throws IllegalArgumentException {
        /*
         * Verify that the object is actually a proxy instance.
         */
        if (!isProxyClass(proxy.getClass())) {
            throw new IllegalArgumentException("not a proxy instance");
        }

        final Proxy p = (Proxy) proxy;
        final InvocationHandler ih = p.h;
        if (System.getSecurityManager() != null) {
            Class<?> ihClass = ih.getClass();
            Class<?> caller = Reflection.getCallerClass();
            if (ReflectUtil.needsPackageAccessCheck(caller.getClassLoader(), ihClass.getClassLoader())) {
                ReflectUtil.checkPackageAccess(ihClass);
            }
        }

        return ih;
    }

    private static native Class<?> defineClass0(ClassLoader loader, String name, byte[] b, int off, int len);
}
