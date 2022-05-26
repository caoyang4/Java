package java.lang;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Field;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.AnnotatedType;
import java.lang.ref.SoftReference;
import java.io.InputStream;
import java.io.ObjectStreamField;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import sun.misc.Unsafe;
import sun.reflect.CallerSensitive;
import sun.reflect.ConstantPool;
import sun.reflect.Reflection;
import sun.reflect.ReflectionFactory;
import sun.reflect.generics.factory.CoreReflectionFactory;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.repository.ClassRepository;
import sun.reflect.generics.repository.MethodRepository;
import sun.reflect.generics.repository.ConstructorRepository;
import sun.reflect.generics.scope.ClassScope;
import sun.security.util.SecurityConstants;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import sun.reflect.annotation.*;
import sun.reflect.misc.ReflectUtil;

/**
 * Class类是Java反射机制的基础，是一个特殊类，它用于表示JVM运行时类或接口的信息。
 * Java反射（Reflection）允许应用程序在运行时借助于反射API，来获取所有类或接口的内部信息，并且能直接操作任意对象的内部属性及方法。
 * 反射机制的核心类为java.lang.Class，类加载完后，会在堆内存的方法区中产生一个Class类型的对象。
 * Class类没有 public 构造函数，是由类加载器的defineClass方法构造而成。所以Class对象不是“new”出来的，而是通过方法来获取的。
 * 这个Class对象具有类的完整结构信息，并且一个类只有一个Class对象。
 * Class类提供很多方法用于获取类的各种信息，比如获取类名、判断该类是否是一个接口还是普通类等。
 *
 * 在Java中枚举类是一种类，而注解是一个接口，数组也是一个类；
 * Java原始类型(boolean, byte, char, short, int, long, float, and double)和关键字void也被表示为Class的对象。
 * Class类是java中的一个类和其他非继承类一样，默认的父类也是Object
 *
 * 获取Class对象有以下四种方式：
 *   通过类对象获取；x.getClass()
 *   通过类直接调用class获取；X.class
 *   通过Class.forName获取；
 *   通过类加载器获取
 *
 * 反射虽是强大的，但不可随意使用。如果可以在不使用反射的情况下执行操作，则应避免使用它
 *   性能开销: 反射包括了一些动态类型，所以JVM无法对这些代码进行优化。因此，反射操作的效率要比那些非反射操作低得多
 *   安全限制: 使用反射技术要求程序必须在一个没有安全限制的环境中运行
 *   内部暴露: 由于反射允许代码执行一些在正常情况下不被允许的操作，比如访问私有的属性和方法。所以使用反射可能会导致意料之外的副作用：
 *            代码有功能上的错误，降低可移植性。反射代码破坏了抽象性，因此当平台发生改变的时候，代码的行为就有可能也随着变化
 */
public final class Class<T> implements java.io.Serializable, GenericDeclaration, Type, AnnotatedElement {
    private static final int ANNOTATION= 0x00002000;
    private static final int ENUM      = 0x00004000;
    private static final int SYNTHETIC = 0x00001000;

    private static native void registerNatives();
    static {
        registerNatives();
    }
    // 私有化构造方法，只有JVM才能创建Class对象，传入类加载器
    private Class(ClassLoader loader) {
        classLoader = loader;
    }
    // 根据类型生成字符串 "接口/类 名称"
    public String toString() {
        return (isInterface() ? "interface " : (isPrimitive() ? "" : "class ")) + getName();
    }
    // 给出完整名称（包括类型，修饰符，参数等）
    public String toGenericString() {
        if (isPrimitive()) {
            return toString();
        } else {
            StringBuilder sb = new StringBuilder();

            // Class modifiers are a superset of interface modifiers
            int modifiers = getModifiers() & Modifier.classModifiers();
            if (modifiers != 0) {
                sb.append(Modifier.toString(modifiers));
                sb.append(' ');
            }
            // 注解类型
            if (isAnnotation()) {
                sb.append('@');
            }
            // 接口类型
            if (isInterface()) {
                sb.append("interface");
            } else {
                // 枚举
                if (isEnum())
                    sb.append("enum");
                // 普通类
                else
                    sb.append("class");
            }
            sb.append(' ');
            sb.append(getName());

            TypeVariable<?>[] typeparms = getTypeParameters();
            if (typeparms.length > 0) {
                boolean first = true;
                sb.append('<');
                for(TypeVariable<?> typeparm: typeparms) {
                    if (!first)
                        sb.append(',');
                    sb.append(typeparm.getTypeName());
                    first = false;
                }
                sb.append('>');
            }
            return sb.toString();
        }
    }
    // 通过类的全限定名获取Class对象
    @CallerSensitive
    public static Class<?> forName(String className) throws ClassNotFoundException {
        Class<?> caller = Reflection.getCallerClass();
        // 默认类初始化
        // 是类加载过程中收集静态代码块和静态变量形成的方法的初始化，
        // 不是指类构造函数的执行(构造函数的执行是在类生成实例的时候执行的，是对实例变量的初始化)
        return forName0(className, true, ClassLoader.getClassLoader(caller), caller);
    }


    @CallerSensitive
    public static Class<?> forName(String name, boolean initialize, ClassLoader loader) throws ClassNotFoundException {
        Class<?> caller = null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            caller = Reflection.getCallerClass();
            if (sun.misc.VM.isSystemDomainLoader(loader)) {
                ClassLoader ccl = ClassLoader.getClassLoader(caller);
                if (!sun.misc.VM.isSystemDomainLoader(ccl)) {
                    sm.checkPermission(
                        SecurityConstants.GET_CLASSLOADER_PERMISSION);
                }
            }
        }
        return forName0(name, initialize, loader, caller);
    }

    private static native Class<?> forName0(String name, boolean initialize, ClassLoader loader, Class<?> caller) throws ClassNotFoundException;

    // 调用类的无参构造方法创建对象
    // 在初始化一个类，生成一个实例的时候，newInstance()方法和new关键字除了一个是方法，一个是关键字外，最主要有什么区别？
    //      1.它们的区别在于创建对象的方式不一样，前者是使用类加载机制，后者是通过 new 指令创建一个新类。
    //      2.那么为什么会有两种创建对象方式？
    //        这主要考虑到软件的可伸缩、可扩展和可重用等软件设计思想。
    //        Java中工厂模式经常使用newInstance()方法来创建对象
    //      3.从JVM的角度看，我们使用关键字new创建一个类的时候，这个类可以没有被加载。
    //        但是使用newInstance()方法的时候，需要保证该类已被加载和连接，这两步是forName()完成
    //      4.newInstance只能调用无参构造，new能调用任何public构造
    @CallerSensitive
    public T newInstance() throws InstantiationException, IllegalAccessException {
        if (System.getSecurityManager() != null) {
            checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), false);
        }

        // Constructor lookup
        if (cachedConstructor == null) {
            if (this == Class.class) {
                throw new IllegalAccessException("Can not call newInstance() on the Class for java.lang.Class");
            }
            try {
                Class<?>[] empty = {};
                // 获取无参构造器，如果没有就抛出异常，说明这个方法只适用于有无参构造函数的类
                final Constructor<T> c = getConstructor0(empty, Member.DECLARED);
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<Void>() {
                        public Void run() {
                                c.setAccessible(true);
                                return null;
                            }
                        });
                // 缓存起来
                cachedConstructor = c;
            } catch (NoSuchMethodException e) {
                throw (InstantiationException)
                    new InstantiationException(getName()).initCause(e);
            }
        }
        Constructor<T> tmpConstructor = cachedConstructor;
        // 安全检查
        int modifiers = tmpConstructor.getModifiers();
        if (!Reflection.quickCheckMemberAccess(this, modifiers)) {
            Class<?> caller = Reflection.getCallerClass();
            if (newInstanceCallerCache != caller) {
                Reflection.ensureMemberAccess(caller, this, null, modifiers);
                newInstanceCallerCache = caller;
            }
        }
        // 执行无参构造函数创建实例对象
        try {
            return tmpConstructor.newInstance((Object[])null);
        } catch (InvocationTargetException e) {
            Unsafe.getUnsafe().throwException(e.getTargetException());
            // Not reached
            return null;
        }
    }

    private volatile transient Constructor<T> cachedConstructor;
    private volatile transient Class<?>       newInstanceCallerCache;

    // 判断obj是否动态等效(dynamic equivalent)某个类的实例化对象
    public native boolean isInstance(Object obj);
    // cls的参数和某个类是否一致，或是否为超类或超接口
    public native boolean isAssignableFrom(Class<?> cls);
    // 是否为接口
    public native boolean isInterface();
    // 是否为数组
    public native boolean isArray();
    // 是否为基础数据类型
    public native boolean isPrimitive();
    // 是否为注解
    public boolean isAnnotation() {
        return (getModifiers() & ANNOTATION) != 0;
    }
    // 是否为Java语言规范所定义的综合类
    public boolean isSynthetic() {
        return (getModifiers() & SYNTHETIC) != 0;
    }

    // 获取类的名称
    public String getName() {
        String name = this.name;
        if (name == null)
            this.name = name = getName0();
        return name;
    }

    // cache the name to reduce the number of calls into the VM
    private transient String name;
    private native String getName0();

    // 获取类加载器，对于基本数据类型会返回 null，基本数据类型，基本数据类型的数组都不需要类加载器加载
    @CallerSensitive
    public ClassLoader getClassLoader() {
        ClassLoader cl = getClassLoader0();
        if (cl == null)
            return null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            ClassLoader.checkClassLoaderPermission(cl, Reflection.getCallerClass());
        }
        return cl;
    }

    // Package-private to allow ClassLoader access
    ClassLoader getClassLoader0() { return classLoader; }

    // 类加载器
    private final ClassLoader classLoader;

    // 获得类的类型参数
    @SuppressWarnings("unchecked")
    public TypeVariable<Class<T>>[] getTypeParameters() {
        ClassRepository info = getGenericInfo();
        if (info != null)
            return (TypeVariable<Class<T>>[])info.getTypeParameters();
        else
            return (TypeVariable<Class<T>>[])new TypeVariable<?>[0];
    }
    // 获得该类的父类
    public native Class<? super T> getSuperclass();
    // 获得该类的父类（包含泛型参数）
    public Type getGenericSuperclass() {
        ClassRepository info = getGenericInfo();
        if (info == null) {
            return getSuperclass();
        }
        if (isInterface()) {
            return null;
        }

        return info.getSuperclass();
    }
    // 获得包
    public Package getPackage() {
        return Package.getPackage(this);
    }
    // 获得实现的接口数组
    public Class<?>[] getInterfaces() {
        ReflectionData<T> rd = reflectionData();
        if (rd == null) {
            // no cloning required
            return getInterfaces0();
        } else {
            Class<?>[] interfaces = rd.interfaces;
            if (interfaces == null) {
                interfaces = getInterfaces0();
                rd.interfaces = interfaces;
            }
            // defensively copy before handing over to user code
            return interfaces.clone();
        }
    }

    private native Class<?>[] getInterfaces0();
    // 获得实现的接口数组（带泛型）
    public Type[] getGenericInterfaces() {
        ClassRepository info = getGenericInfo();
        return (info == null) ?  getInterfaces() : info.getSuperInterfaces();
    }


    public native Class<?> getComponentType();

    // 获得类的修饰符
    public native int getModifiers();

    public native Object[] getSigners();
    // 设置类的签名
    native void setSigners(Object[] signers);

    @CallerSensitive
    public Method getEnclosingMethod() throws SecurityException {
        EnclosingMethodInfo enclosingInfo = getEnclosingMethodInfo();

        if (enclosingInfo == null)
            return null;
        else {
            if (!enclosingInfo.isMethod())
                return null;

            MethodRepository typeInfo = MethodRepository.make(enclosingInfo.getDescriptor(),
                                                              getFactory());
            Class<?>   returnType       = toClass(typeInfo.getReturnType());
            Type []    parameterTypes   = typeInfo.getParameterTypes();
            Class<?>[] parameterClasses = new Class<?>[parameterTypes.length];

            // Convert Types to Classes; returned types *should*
            // be class objects since the methodDescriptor's used
            // don't have generics information
            for(int i = 0; i < parameterClasses.length; i++)
                parameterClasses[i] = toClass(parameterTypes[i]);

            // Perform access check
            Class<?> enclosingCandidate = enclosingInfo.getEnclosingClass();
            enclosingCandidate.checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
            /*
             * Loop over all declared methods; match method name,
             * number of and type of parameters, *and* return
             * type.  Matching return type is also necessary
             * because of covariant returns, etc.
             */
            for(Method m: enclosingCandidate.getDeclaredMethods()) {
                if (m.getName().equals(enclosingInfo.getName()) ) {
                    Class<?>[] candidateParamClasses = m.getParameterTypes();
                    if (candidateParamClasses.length == parameterClasses.length) {
                        boolean matches = true;
                        for(int i = 0; i < candidateParamClasses.length; i++) {
                            if (!candidateParamClasses[i].equals(parameterClasses[i])) {
                                matches = false;
                                break;
                            }
                        }

                        if (matches) { // finally, check return type
                            if (m.getReturnType().equals(returnType) )
                                return m;
                        }
                    }
                }
            }

            throw new InternalError("Enclosing method not found");
        }
    }

    private native Object[] getEnclosingMethod0();

    private EnclosingMethodInfo getEnclosingMethodInfo() {
        Object[] enclosingInfo = getEnclosingMethod0();
        if (enclosingInfo == null)
            return null;
        else {
            return new EnclosingMethodInfo(enclosingInfo);
        }
    }

    private final static class EnclosingMethodInfo {
        private Class<?> enclosingClass;
        private String name;
        private String descriptor;

        private EnclosingMethodInfo(Object[] enclosingInfo) {
            if (enclosingInfo.length != 3)
                throw new InternalError("Malformed enclosing method information");
            try {
                // The array is expected to have three elements:

                // the immediately enclosing class
                enclosingClass = (Class<?>) enclosingInfo[0];
                assert(enclosingClass != null);

                // the immediately enclosing method or constructor's
                // name (can be null).
                name            = (String)   enclosingInfo[1];

                // the immediately enclosing method or constructor's
                // descriptor (null iff name is).
                descriptor      = (String)   enclosingInfo[2];
                assert((name != null && descriptor != null) || name == descriptor);
            } catch (ClassCastException cce) {
                throw new InternalError("Invalid type in enclosing method information", cce);
            }
        }

        boolean isPartial() {
            return enclosingClass == null || name == null || descriptor == null;
        }

        boolean isConstructor() { return !isPartial() && "<init>".equals(name); }

        boolean isMethod() { return !isPartial() && !isConstructor() && !"<clinit>".equals(name); }

        Class<?> getEnclosingClass() { return enclosingClass; }

        String getName() { return name; }

        String getDescriptor() { return descriptor; }

    }

    private static Class<?> toClass(Type o) {
        if (o instanceof GenericArrayType)
            return Array.newInstance(toClass(((GenericArrayType)o).getGenericComponentType()), 0)
                .getClass();
        return (Class<?>)o;
     }
    // 若该类是本地类或匿名类，则获取此类的方法
    @CallerSensitive
    public Constructor<?> getEnclosingConstructor() throws SecurityException {
        EnclosingMethodInfo enclosingInfo = getEnclosingMethodInfo();

        if (enclosingInfo == null)
            return null;
        else {
            if (!enclosingInfo.isConstructor())
                return null;

            ConstructorRepository typeInfo = ConstructorRepository.make(enclosingInfo.getDescriptor(),
                                                                        getFactory());
            Type []    parameterTypes   = typeInfo.getParameterTypes();
            Class<?>[] parameterClasses = new Class<?>[parameterTypes.length];

            // Convert Types to Classes; returned types *should*
            // be class objects since the methodDescriptor's used
            // don't have generics information
            for(int i = 0; i < parameterClasses.length; i++)
                parameterClasses[i] = toClass(parameterTypes[i]);

            // Perform access check
            Class<?> enclosingCandidate = enclosingInfo.getEnclosingClass();
            enclosingCandidate.checkMemberAccess(Member.DECLARED,
                                                 Reflection.getCallerClass(), true);
            /*
             * Loop over all declared constructors; match number
             * of and type of parameters.
             */
            for(Constructor<?> c: enclosingCandidate.getDeclaredConstructors()) {
                Class<?>[] candidateParamClasses = c.getParameterTypes();
                if (candidateParamClasses.length == parameterClasses.length) {
                    boolean matches = true;
                    for(int i = 0; i < candidateParamClasses.length; i++) {
                        if (!candidateParamClasses[i].equals(parameterClasses[i])) {
                            matches = false;
                            break;
                        }
                    }

                    if (matches)
                        return c;
                }
            }

            throw new InternalError("Enclosing constructor not found");
        }
    }

    // 获取方法或属性的归属类，或者获取当前Class对象继承于哪个类
    @CallerSensitive
    public Class<?> getDeclaringClass() throws SecurityException {
        final Class<?> candidate = getDeclaringClass0();

        if (candidate != null)
            candidate.checkPackageAccess(ClassLoader.getClassLoader(Reflection.getCallerClass()), true);
        return candidate;
    }

    private native Class<?> getDeclaringClass0();


    @CallerSensitive
    public Class<?> getEnclosingClass() throws SecurityException {
        // There are five kinds of classes (or interfaces):
        // a) Top level classes
        // b) Nested classes (static member classes)
        // c) Inner classes (non-static member classes)
        // d) Local classes (named classes declared within a method)
        // e) Anonymous classes

        EnclosingMethodInfo enclosingInfo = getEnclosingMethodInfo();
        Class<?> enclosingCandidate;

        if (enclosingInfo == null) {
            // This is a top level or a nested class or an inner class (a, b, or c)
            enclosingCandidate = getDeclaringClass();
        } else {
            Class<?> enclosingClass = enclosingInfo.getEnclosingClass();
            // This is a local class or an anonymous class (d or e)
            if (enclosingClass == this || enclosingClass == null)
                throw new InternalError("Malformed enclosing method information");
            else
                enclosingCandidate = enclosingClass;
        }

        if (enclosingCandidate != null)
            enclosingCandidate.checkPackageAccess(
                    ClassLoader.getClassLoader(Reflection.getCallerClass()), true);
        return enclosingCandidate;
    }
    // 获得一个简单类名
    public String getSimpleName() {
        if (isArray())
            return getComponentType().getSimpleName()+"[]";

        String simpleName = getSimpleBinaryName();
        if (simpleName == null) { // top level class
            simpleName = getName();
            return simpleName.substring(simpleName.lastIndexOf(".")+1); // strip the package name
        }
        // Remove leading "\$[0-9]*" from the name
        int length = simpleName.length();
        if (length < 1 || simpleName.charAt(0) != '$')
            throw new InternalError("Malformed class name");
        int index = 1;
        while (index < length && isAsciiDigit(simpleName.charAt(index)))
            index++;
        // Eventually, this is the empty string iff this is an anonymous class
        return simpleName.substring(index);
    }
    // 获得该类类型的名称
    public String getTypeName() {
        if (isArray()) {
            try {
                Class<?> cl = this;
                int dimensions = 0;
                while (cl.isArray()) {
                    dimensions++;
                    cl = cl.getComponentType();
                }
                StringBuilder sb = new StringBuilder();
                sb.append(cl.getName());
                for (int i = 0; i < dimensions; i++) {
                    sb.append("[]");
                }
                return sb.toString();
            } catch (Throwable e) { /*FALLTHRU*/ }
        }
        return getName();
    }

    private static boolean isAsciiDigit(char c) {
        return '0' <= c && c <= '9';
    }

    public String getCanonicalName() {
        if (isArray()) {
            String canonicalName = getComponentType().getCanonicalName();
            if (canonicalName != null)
                return canonicalName + "[]";
            else
                return null;
        }
        if (isLocalOrAnonymousClass())
            return null;
        Class<?> enclosingClass = getEnclosingClass();
        if (enclosingClass == null) { // top level class
            return getName();
        } else {
            String enclosingName = enclosingClass.getCanonicalName();
            if (enclosingName == null)
                return null;
            return enclosingName + "." + getSimpleName();
        }
    }

    public boolean isAnonymousClass() {
        return "".equals(getSimpleName());
    }
    // 判断是否为局部类
    public boolean isLocalClass() {
        return isLocalOrAnonymousClass() && !isAnonymousClass();
    }

    public boolean isMemberClass() {
        return getSimpleBinaryName() != null && !isLocalOrAnonymousClass();
    }
    // 获得类的全限定名称
    private String getSimpleBinaryName() {
        Class<?> enclosingClass = getEnclosingClass();
        if (enclosingClass == null) // top level class
            return null;
        // Otherwise, strip the enclosing class' name
        try {
            return getName().substring(enclosingClass.getName().length());
        } catch (IndexOutOfBoundsException ex) {
            throw new InternalError("Malformed class name", ex);
        }
    }

    private boolean isLocalOrAnonymousClass() {
        return getEnclosingMethodInfo() != null;
    }
    // 获得类以及父类的所有的public的内部类
    @CallerSensitive
    public Class<?>[] getClasses() {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), false);
        return java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Class<?>[]>() {
                public Class<?>[] run() {
                    List<Class<?>> list = new ArrayList<>();
                    Class<?> currentClass = Class.this;
                    while (currentClass != null) {
                        Class<?>[] members = currentClass.getDeclaredClasses();
                        for (int i = 0; i < members.length; i++) {
                            if (Modifier.isPublic(members[i].getModifiers())) {
                                list.add(members[i]);
                            }
                        }
                        currentClass = currentClass.getSuperclass();
                    }
                    return list.toArray(new Class<?>[0]);
                }
            });
    }

    // 获取public修饰的属性域
    @CallerSensitive
    public Field[] getFields() throws SecurityException {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), true);
        return copyFields(privateGetPublicFields(null));
    }

    // 获取public修饰的方法
    @CallerSensitive
    public Method[] getMethods() throws SecurityException {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), true);
        return copyMethods(privateGetPublicMethods());
    }

    // 获取public修饰的构造器
    @CallerSensitive
    public Constructor<?>[] getConstructors() throws SecurityException {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), true);
        return copyConstructors(privateGetDeclaredConstructors(true));
    }

    // 根据输入的属性名查找对应的属性域
    @CallerSensitive
    public Field getField(String name) throws NoSuchFieldException, SecurityException {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), true);
        Field field = getField0(name);
        if (field == null) {
            throw new NoSuchFieldException(name);
        }
        return field;
    }

    // 根据输入的方法名和入参类型，查找对应的方法
    @CallerSensitive
    public Method getMethod(String name, Class<?>... parameterTypes)
        throws NoSuchMethodException, SecurityException {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), true);
        Method method = getMethod0(name, parameterTypes, true);
        if (method == null) {
            throw new NoSuchMethodException(getName() + "." + name + argumentTypesToString(parameterTypes));
        }
        return method;
    }

    // 根据输入的方法名和入参类型，查找对应的构造器
    @CallerSensitive
    public Constructor<T> getConstructor(Class<?>... parameterTypes)
        throws NoSuchMethodException, SecurityException {
        checkMemberAccess(Member.PUBLIC, Reflection.getCallerClass(), true);
        return getConstructor0(parameterTypes, Member.PUBLIC);
    }

    // 获取Class对象中的内部类，继承成员是不包含在内的
    @CallerSensitive
    public Class<?>[] getDeclaredClasses() throws SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), false);
        return getDeclaredClasses0();
    }

    // 获取Class对象中的属性域
    @CallerSensitive
    public Field[] getDeclaredFields() throws SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
        return copyFields(privateGetDeclaredFields(false));
    }

    // 获取Class对象中的方法
    @CallerSensitive
    public Method[] getDeclaredMethods() throws SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
        return copyMethods(privateGetDeclaredMethods(false));
    }

    // 获得该类的所有构造方法
    @CallerSensitive
    public Constructor<?>[] getDeclaredConstructors() throws SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
        return copyConstructors(privateGetDeclaredConstructors(false));
    }


    @CallerSensitive
    public Field getDeclaredField(String name)
        throws NoSuchFieldException, SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
        Field field = searchFields(privateGetDeclaredFields(false), name);
        if (field == null) {
            throw new NoSuchFieldException(name);
        }
        return field;
    }


    @CallerSensitive
    public Method getDeclaredMethod(String name, Class<?>... parameterTypes)
        throws NoSuchMethodException, SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
        Method method = searchMethods(privateGetDeclaredMethods(false), name, parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException(getName() + "." + name + argumentTypesToString(parameterTypes));
        }
        return method;
    }


    @CallerSensitive
    public Constructor<T> getDeclaredConstructor(Class<?>... parameterTypes)
        throws NoSuchMethodException, SecurityException {
        checkMemberAccess(Member.DECLARED, Reflection.getCallerClass(), true);
        return getConstructor0(parameterTypes, Member.DECLARED);
    }

     public InputStream getResourceAsStream(String name) {
        name = resolveName(name);
        ClassLoader cl = getClassLoader0();
        if (cl==null) {
            // A system class.
            return ClassLoader.getSystemResourceAsStream(name);
        }
        return cl.getResourceAsStream(name);
    }

    public java.net.URL getResource(String name) {
        name = resolveName(name);
        ClassLoader cl = getClassLoader0();
        if (cl==null) {
            // A system class.
            return ClassLoader.getSystemResource(name);
        }
        return cl.getResource(name);
    }



    private static java.security.ProtectionDomain allPermDomain;


    public java.security.ProtectionDomain getProtectionDomain() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_PD_PERMISSION);
        }
        java.security.ProtectionDomain pd = getProtectionDomain0();
        if (pd == null) {
            if (allPermDomain == null) {
                java.security.Permissions perms =
                    new java.security.Permissions();
                perms.add(SecurityConstants.ALL_PERMISSION);
                allPermDomain =
                    new java.security.ProtectionDomain(null, perms);
            }
            pd = allPermDomain;
        }
        return pd;
    }


    private native java.security.ProtectionDomain getProtectionDomain0();

    static native Class<?> getPrimitiveClass(String name);

    private void checkMemberAccess(int which, Class<?> caller, boolean checkProxyInterfaces) {
        final SecurityManager s = System.getSecurityManager();
        if (s != null) {

            final ClassLoader ccl = ClassLoader.getClassLoader(caller);
            final ClassLoader cl = getClassLoader0();
            if (which != Member.PUBLIC) {
                if (ccl != cl) {
                    s.checkPermission(SecurityConstants.CHECK_MEMBER_ACCESS_PERMISSION);
                }
            }
            this.checkPackageAccess(ccl, checkProxyInterfaces);
        }
    }


    private void checkPackageAccess(final ClassLoader ccl, boolean checkProxyInterfaces) {
        final SecurityManager s = System.getSecurityManager();
        if (s != null) {
            final ClassLoader cl = getClassLoader0();

            if (ReflectUtil.needsPackageAccessCheck(ccl, cl)) {
                String name = this.getName();
                int i = name.lastIndexOf('.');
                if (i != -1) {
                    // skip the package access check on a proxy class in default proxy package
                    String pkg = name.substring(0, i);
                    if (!Proxy.isProxyClass(this) || ReflectUtil.isNonPublicProxyClass(this)) {
                        s.checkPackageAccess(pkg);
                    }
                }
            }
            // check package access on the proxy interfaces
            if (checkProxyInterfaces && Proxy.isProxyClass(this)) {
                ReflectUtil.checkProxyPackageAccess(ccl, this.getInterfaces());
            }
        }
    }

    private String resolveName(String name) {
        if (name == null) {
            return name;
        }
        if (!name.startsWith("/")) {
            Class<?> c = this;
            while (c.isArray()) {
                c = c.getComponentType();
            }
            String baseName = c.getName();
            int index = baseName.lastIndexOf('.');
            if (index != -1) {
                name = baseName.substring(0, index).replace('.', '/')
                    +"/"+name;
            }
        } else {
            name = name.substring(1);
        }
        return name;
    }

    private static class Atomic {
        // initialize Unsafe machinery here, since we need to call Class.class instance method
        // and have to avoid calling it in the static initializer of the Class class...
        private static final Unsafe unsafe = Unsafe.getUnsafe();
        // offset of Class.reflectionData instance field
        private static final long reflectionDataOffset;
        // offset of Class.annotationType instance field
        private static final long annotationTypeOffset;
        // offset of Class.annotationData instance field
        private static final long annotationDataOffset;

        static {
            Field[] fields = Class.class.getDeclaredFields0(false); // bypass caches
            reflectionDataOffset = objectFieldOffset(fields, "reflectionData");
            annotationTypeOffset = objectFieldOffset(fields, "annotationType");
            annotationDataOffset = objectFieldOffset(fields, "annotationData");
        }

        private static long objectFieldOffset(Field[] fields, String fieldName) {
            Field field = searchFields(fields, fieldName);
            if (field == null) {
                throw new Error("No " + fieldName + " field found in java.lang.Class");
            }
            return unsafe.objectFieldOffset(field);
        }

        static <T> boolean casReflectionData(Class<?> clazz,
                                             SoftReference<ReflectionData<T>> oldData,
                                             SoftReference<ReflectionData<T>> newData) {
            return unsafe.compareAndSwapObject(clazz, reflectionDataOffset, oldData, newData);
        }

        static <T> boolean casAnnotationType(Class<?> clazz,
                                             AnnotationType oldType,
                                             AnnotationType newType) {
            return unsafe.compareAndSwapObject(clazz, annotationTypeOffset, oldType, newType);
        }

        static <T> boolean casAnnotationData(Class<?> clazz,
                                             AnnotationData oldData,
                                             AnnotationData newData) {
            return unsafe.compareAndSwapObject(clazz, annotationDataOffset, oldData, newData);
        }
    }


    // Caches for certain reflective results
    private static boolean useCaches = true;

    // 反射数据
    private static class ReflectionData<T> {
        volatile Field[] declaredFields;
        volatile Field[] publicFields;
        volatile Method[] declaredMethods;
        volatile Method[] publicMethods;
        volatile Constructor<T>[] declaredConstructors;
        volatile Constructor<T>[] publicConstructors;
        // Intermediate results for getFields and getMethods
        volatile Field[] declaredPublicFields;
        volatile Method[] declaredPublicMethods;
        volatile Class<?>[] interfaces;

        // Value of classRedefinedCount when we created this ReflectionData instance
        final int redefinedCount;

        ReflectionData(int redefinedCount) {
            this.redefinedCount = redefinedCount;
        }
    }
    // 软引用，内存不足，才会gc回收
    private volatile transient SoftReference<ReflectionData<T>> reflectionData;

    // Incremented by the VM on each call to JVM TI RedefineClasses()
    // that redefines this class or a superclass.
    private volatile transient int classRedefinedCount = 0;

    // Lazily create and cache ReflectionData
    private ReflectionData<T> reflectionData() {
        SoftReference<ReflectionData<T>> reflectionData = this.reflectionData;
        int classRedefinedCount = this.classRedefinedCount;
        ReflectionData<T> rd;
        if (useCaches &&
            reflectionData != null &&
            (rd = reflectionData.get()) != null &&
            rd.redefinedCount == classRedefinedCount) {
            return rd;
        }
        // else no SoftReference or cleared SoftReference or stale ReflectionData
        // -> create and replace new instance
        return newReflectionData(reflectionData, classRedefinedCount);
    }

    private ReflectionData<T> newReflectionData(SoftReference<ReflectionData<T>> oldReflectionData, int classRedefinedCount) {
        if (!useCaches) return null;

        while (true) {
            ReflectionData<T> rd = new ReflectionData<>(classRedefinedCount);
            // try to CAS it...
            if (Atomic.casReflectionData(this, oldReflectionData, new SoftReference<>(rd))) {
                return rd;
            }
            // else retry
            oldReflectionData = this.reflectionData;
            classRedefinedCount = this.classRedefinedCount;
            if (oldReflectionData != null &&
                (rd = oldReflectionData.get()) != null &&
                rd.redefinedCount == classRedefinedCount) {
                return rd;
            }
        }
    }

    // Generic signature handling
    private native String getGenericSignature0();

    // Generic info repository; lazily initialized
    private volatile transient ClassRepository genericInfo;

    // accessor for factory
    private GenericsFactory getFactory() {
        // create scope and factory
        return CoreReflectionFactory.make(this, ClassScope.make(this));
    }

    // accessor for generic info repository;
    // generic info is lazily initialized
    private ClassRepository getGenericInfo() {
        ClassRepository genericInfo = this.genericInfo;
        if (genericInfo == null) {
            String signature = getGenericSignature0();
            if (signature == null) {
                genericInfo = ClassRepository.NONE;
            } else {
                genericInfo = ClassRepository.make(signature, getFactory());
            }
            this.genericInfo = genericInfo;
        }
        return (genericInfo != ClassRepository.NONE) ? genericInfo : null;
    }

    // Annotations handling
    native byte[] getRawAnnotations();
    // Since 1.8
    native byte[] getRawTypeAnnotations();
    static byte[] getExecutableTypeAnnotationBytes(Executable ex) {
        return getReflectionFactory().getExecutableTypeAnnotationBytes(ex);
    }

    native ConstantPool getConstantPool();

    //
    //
    // java.lang.reflect.Field handling
    //
    //

    // Returns an array of "root" fields. These Field objects must NOT
    // be propagated to the outside world, but must instead be copied
    // via ReflectionFactory.copyField.
    private Field[] privateGetDeclaredFields(boolean publicOnly) {
        checkInitted();
        Field[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = publicOnly ? rd.declaredPublicFields : rd.declaredFields;
            if (res != null) return res;
        }
        // No cached value available; request value from VM
        res = Reflection.filterFields(this, getDeclaredFields0(publicOnly));
        if (rd != null) {
            if (publicOnly) {
                rd.declaredPublicFields = res;
            } else {
                rd.declaredFields = res;
            }
        }
        return res;
    }

    // Returns an array of "root" fields. These Field objects must NOT
    // be propagated to the outside world, but must instead be copied
    // via ReflectionFactory.copyField.
    private Field[] privateGetPublicFields(Set<Class<?>> traversedInterfaces) {
        checkInitted();
        Field[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = rd.publicFields;
            if (res != null) return res;
        }

        // No cached value available; compute value recursively.
        // Traverse in correct order for getField().
        List<Field> fields = new ArrayList<>();
        if (traversedInterfaces == null) {
            traversedInterfaces = new HashSet<>();
        }

        // Local fields
        Field[] tmp = privateGetDeclaredFields(true);
        addAll(fields, tmp);

        // Direct superinterfaces, recursively
        for (Class<?> c : getInterfaces()) {
            if (!traversedInterfaces.contains(c)) {
                traversedInterfaces.add(c);
                addAll(fields, c.privateGetPublicFields(traversedInterfaces));
            }
        }

        // Direct superclass, recursively
        if (!isInterface()) {
            Class<?> c = getSuperclass();
            if (c != null) {
                addAll(fields, c.privateGetPublicFields(traversedInterfaces));
            }
        }

        res = new Field[fields.size()];
        fields.toArray(res);
        if (rd != null) {
            rd.publicFields = res;
        }
        return res;
    }

    private static void addAll(Collection<Field> c, Field[] o) {
        for (int i = 0; i < o.length; i++) {
            c.add(o[i]);
        }
    }


    // java.lang.reflect.Constructor handling

    // Returns an array of "root" constructors. These Constructor
    // objects must NOT be propagated to the outside world, but must
    // instead be copied via ReflectionFactory.copyConstructor.
    private Constructor<T>[] privateGetDeclaredConstructors(boolean publicOnly) {
        checkInitted();
        Constructor<T>[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = publicOnly ? rd.publicConstructors : rd.declaredConstructors;
            if (res != null) return res;
        }
        // No cached value available; request value from VM
        if (isInterface()) {
            @SuppressWarnings("unchecked")
            Constructor<T>[] temporaryRes = (Constructor<T>[]) new Constructor<?>[0];
            res = temporaryRes;
        } else {
            res = getDeclaredConstructors0(publicOnly);
        }
        if (rd != null) {
            if (publicOnly) {
                rd.publicConstructors = res;
            } else {
                rd.declaredConstructors = res;
            }
        }
        return res;
    }

    // java.lang.reflect.Method handling

    private Method[] privateGetDeclaredMethods(boolean publicOnly) {
        checkInitted();
        Method[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = publicOnly ? rd.declaredPublicMethods : rd.declaredMethods;
            if (res != null) return res;
        }
        // No cached value available; request value from VM
        res = Reflection.filterMethods(this, getDeclaredMethods0(publicOnly));
        if (rd != null) {
            if (publicOnly) {
                rd.declaredPublicMethods = res;
            } else {
                rd.declaredMethods = res;
            }
        }
        return res;
    }

    static class MethodArray {
        // Don't add or remove methods except by add() or remove() calls.
        private Method[] methods;
        private int length;
        private int defaults;

        MethodArray() {
            this(20);
        }

        MethodArray(int initialSize) {
            if (initialSize < 2)
                throw new IllegalArgumentException("Size should be 2 or more");

            methods = new Method[initialSize];
            length = 0;
            defaults = 0;
        }

        boolean hasDefaults() {
            return defaults != 0;
        }

        void add(Method m) {
            if (length == methods.length) {
                methods = Arrays.copyOf(methods, 2 * methods.length);
            }
            methods[length++] = m;

            if (m != null && m.isDefault())
                defaults++;
        }

        void addAll(Method[] ma) {
            for (int i = 0; i < ma.length; i++) {
                add(ma[i]);
            }
        }

        void addAll(MethodArray ma) {
            for (int i = 0; i < ma.length(); i++) {
                add(ma.get(i));
            }
        }

        void addIfNotPresent(Method newMethod) {
            for (int i = 0; i < length; i++) {
                Method m = methods[i];
                if (m == newMethod || (m != null && m.equals(newMethod))) {
                    return;
                }
            }
            add(newMethod);
        }

        void addAllIfNotPresent(MethodArray newMethods) {
            for (int i = 0; i < newMethods.length(); i++) {
                Method m = newMethods.get(i);
                if (m != null) {
                    addIfNotPresent(m);
                }
            }
        }

        /* Add Methods declared in an interface to this MethodArray.
         * Static methods declared in interfaces are not inherited.
         */
        void addInterfaceMethods(Method[] methods) {
            for (Method candidate : methods) {
                if (!Modifier.isStatic(candidate.getModifiers())) {
                    add(candidate);
                }
            }
        }

        int length() {
            return length;
        }

        Method get(int i) {
            return methods[i];
        }

        Method getFirst() {
            for (Method m : methods)
                if (m != null)
                    return m;
            return null;
        }

        void removeByNameAndDescriptor(Method toRemove) {
            for (int i = 0; i < length; i++) {
                Method m = methods[i];
                if (m != null && matchesNameAndDescriptor(m, toRemove)) {
                    remove(i);
                }
            }
        }

        private void remove(int i) {
            if (methods[i] != null && methods[i].isDefault())
                defaults--;
            methods[i] = null;
        }

        private boolean matchesNameAndDescriptor(Method m1, Method m2) {
            return m1.getReturnType() == m2.getReturnType() &&
                   m1.getName() == m2.getName() && // name is guaranteed to be interned
                   arrayContentsEq(m1.getParameterTypes(),
                           m2.getParameterTypes());
        }

        void compactAndTrim() {
            int newPos = 0;
            // Get rid of null slots
            for (int pos = 0; pos < length; pos++) {
                Method m = methods[pos];
                if (m != null) {
                    if (pos != newPos) {
                        methods[newPos] = m;
                    }
                    newPos++;
                }
            }
            if (newPos != methods.length) {
                methods = Arrays.copyOf(methods, newPos);
            }
        }

        void removeLessSpecifics() {
            if (!hasDefaults())
                return;

            for (int i = 0; i < length; i++) {
                Method m = get(i);
                if  (m == null || !m.isDefault())
                    continue;

                for (int j  = 0; j < length; j++) {
                    if (i == j)
                        continue;

                    Method candidate = get(j);
                    if (candidate == null)
                        continue;

                    if (!matchesNameAndDescriptor(m, candidate))
                        continue;

                    if (hasMoreSpecificClass(m, candidate))
                        remove(j);
                }
            }
        }

        Method[] getArray() {
            return methods;
        }

        // Returns true if m1 is more specific than m2
        static boolean hasMoreSpecificClass(Method m1, Method m2) {
            Class<?> m1Class = m1.getDeclaringClass();
            Class<?> m2Class = m2.getDeclaringClass();
            return m1Class != m2Class && m2Class.isAssignableFrom(m1Class);
        }
    }


    // Returns an array of "root" methods. These Method objects must NOT
    // be propagated to the outside world, but must instead be copied
    // via ReflectionFactory.copyMethod.
    private Method[] privateGetPublicMethods() {
        checkInitted();
        Method[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = rd.publicMethods;
            if (res != null) return res;
        }

        // No cached value available; compute value recursively.
        // Start by fetching public declared methods
        MethodArray methods = new MethodArray();
        {
            Method[] tmp = privateGetDeclaredMethods(true);
            methods.addAll(tmp);
        }
        // Now recur over superclass and direct superinterfaces.
        // Go over superinterfaces first so we can more easily filter
        // out concrete implementations inherited from superclasses at
        // the end.
        MethodArray inheritedMethods = new MethodArray();
        for (Class<?> i : getInterfaces()) {
            inheritedMethods.addInterfaceMethods(i.privateGetPublicMethods());
        }
        if (!isInterface()) {
            Class<?> c = getSuperclass();
            if (c != null) {
                MethodArray supers = new MethodArray();
                supers.addAll(c.privateGetPublicMethods());
                // Filter out concrete implementations of any
                // interface methods
                for (int i = 0; i < supers.length(); i++) {
                    Method m = supers.get(i);
                    if (m != null &&
                            !Modifier.isAbstract(m.getModifiers()) &&
                            !m.isDefault()) {
                        inheritedMethods.removeByNameAndDescriptor(m);
                    }
                }
                // Insert superclass's inherited methods before
                // superinterfaces' to satisfy getMethod's search
                // order
                supers.addAll(inheritedMethods);
                inheritedMethods = supers;
            }
        }
        // Filter out all local methods from inherited ones
        for (int i = 0; i < methods.length(); i++) {
            Method m = methods.get(i);
            inheritedMethods.removeByNameAndDescriptor(m);
        }
        methods.addAllIfNotPresent(inheritedMethods);
        methods.removeLessSpecifics();
        methods.compactAndTrim();
        res = methods.getArray();
        if (rd != null) {
            rd.publicMethods = res;
        }
        return res;
    }


    //
    // Helpers for fetchers of one field, method, or constructor
    //

    private static Field searchFields(Field[] fields, String name) {
        String internedName = name.intern();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName() == internedName) {
                return getReflectionFactory().copyField(fields[i]);
            }
        }
        return null;
    }

    private Field getField0(String name) throws NoSuchFieldException {
        // Note: the intent is that the search algorithm this routine
        // uses be equivalent to the ordering imposed by
        // privateGetPublicFields(). It fetches only the declared
        // public fields for each class, however, to reduce the number
        // of Field objects which have to be created for the common
        // case where the field being requested is declared in the
        // class which is being queried.
        Field res;
        // Search declared public fields
        if ((res = searchFields(privateGetDeclaredFields(true), name)) != null) {
            return res;
        }
        // Direct superinterfaces, recursively
        Class<?>[] interfaces = getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class<?> c = interfaces[i];
            if ((res = c.getField0(name)) != null) {
                return res;
            }
        }
        // Direct superclass, recursively
        if (!isInterface()) {
            Class<?> c = getSuperclass();
            if (c != null) {
                if ((res = c.getField0(name)) != null) {
                    return res;
                }
            }
        }
        return null;
    }

    private static Method searchMethods(Method[] methods,
                                        String name,
                                        Class<?>[] parameterTypes)
    {
        Method res = null;
        String internedName = name.intern();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (m.getName() == internedName
                && arrayContentsEq(parameterTypes, m.getParameterTypes())
                && (res == null
                    || res.getReturnType().isAssignableFrom(m.getReturnType())))
                res = m;
        }

        return (res == null ? res : getReflectionFactory().copyMethod(res));
    }

    private Method getMethod0(String name, Class<?>[] parameterTypes, boolean includeStaticMethods) {
        MethodArray interfaceCandidates = new MethodArray(2);
        Method res =  privateGetMethodRecursive(name, parameterTypes, includeStaticMethods, interfaceCandidates);
        if (res != null)
            return res;

        // Not found on class or superclass directly
        interfaceCandidates.removeLessSpecifics();
        return interfaceCandidates.getFirst(); // may be null
    }

    private Method privateGetMethodRecursive(String name,
            Class<?>[] parameterTypes,
            boolean includeStaticMethods,
            MethodArray allInterfaceCandidates) {
        // Note: the intent is that the search algorithm this routine
        // uses be equivalent to the ordering imposed by
        // privateGetPublicMethods(). It fetches only the declared
        // public methods for each class, however, to reduce the
        // number of Method objects which have to be created for the
        // common case where the method being requested is declared in
        // the class which is being queried.
        //
        // Due to default methods, unless a method is found on a superclass,
        // methods declared in any superinterface needs to be considered.
        // Collect all candidates declared in superinterfaces in {@code
        // allInterfaceCandidates} and select the most specific if no match on
        // a superclass is found.

        // Must _not_ return root methods
        Method res;
        // Search declared public methods
        if ((res = searchMethods(privateGetDeclaredMethods(true),
                                 name,
                                 parameterTypes)) != null) {
            if (includeStaticMethods || !Modifier.isStatic(res.getModifiers()))
                return res;
        }
        // Search superclass's methods
        if (!isInterface()) {
            Class<? super T> c = getSuperclass();
            if (c != null) {
                if ((res = c.getMethod0(name, parameterTypes, true)) != null) {
                    return res;
                }
            }
        }
        // Search superinterfaces' methods
        Class<?>[] interfaces = getInterfaces();
        for (Class<?> c : interfaces)
            if ((res = c.getMethod0(name, parameterTypes, false)) != null)
                allInterfaceCandidates.add(res);
        // Not found
        return null;
    }

    private Constructor<T> getConstructor0(Class<?>[] parameterTypes,
                                        int which) throws NoSuchMethodException
    {
        Constructor<T>[] constructors = privateGetDeclaredConstructors((which == Member.PUBLIC));
        for (Constructor<T> constructor : constructors) {
            if (arrayContentsEq(parameterTypes,
                                constructor.getParameterTypes())) {
                return getReflectionFactory().copyConstructor(constructor);
            }
        }
        throw new NoSuchMethodException(getName() + ".<init>" + argumentTypesToString(parameterTypes));
    }

    //
    // Other helpers and base implementation
    //

    private static boolean arrayContentsEq(Object[] a1, Object[] a2) {
        if (a1 == null) {
            return a2 == null || a2.length == 0;
        }

        if (a2 == null) {
            return a1.length == 0;
        }

        if (a1.length != a2.length) {
            return false;
        }

        for (int i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i]) {
                return false;
            }
        }

        return true;
    }

    private static Field[] copyFields(Field[] arg) {
        Field[] out = new Field[arg.length];
        ReflectionFactory fact = getReflectionFactory();
        for (int i = 0; i < arg.length; i++) {
            out[i] = fact.copyField(arg[i]);
        }
        return out;
    }

    private static Method[] copyMethods(Method[] arg) {
        Method[] out = new Method[arg.length];
        ReflectionFactory fact = getReflectionFactory();
        for (int i = 0; i < arg.length; i++) {
            out[i] = fact.copyMethod(arg[i]);
        }
        return out;
    }

    private static <U> Constructor<U>[] copyConstructors(Constructor<U>[] arg) {
        Constructor<U>[] out = arg.clone();
        ReflectionFactory fact = getReflectionFactory();
        for (int i = 0; i < out.length; i++) {
            out[i] = fact.copyConstructor(out[i]);
        }
        return out;
    }

    private native Field[]       getDeclaredFields0(boolean publicOnly);
    private native Method[]      getDeclaredMethods0(boolean publicOnly);
    private native Constructor<T>[] getDeclaredConstructors0(boolean publicOnly);
    private native Class<?>[]   getDeclaredClasses0();

    private static String        argumentTypesToString(Class<?>[] argTypes) {
        StringBuilder buf = new StringBuilder();
        buf.append("(");
        if (argTypes != null) {
            for (int i = 0; i < argTypes.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                Class<?> c = argTypes[i];
                buf.append((c == null) ? "null" : c.getName());
            }
        }
        buf.append(")");
        return buf.toString();
    }

    private static final long serialVersionUID = 3206093459760846163L;


    private static final ObjectStreamField[] serialPersistentFields =
        new ObjectStreamField[0];

    public boolean desiredAssertionStatus() {
        ClassLoader loader = getClassLoader();
        // If the loader is null this is a system class, so ask the VM
        if (loader == null)
            return desiredAssertionStatus0(this);

        // If the classloader has been initialized with the assertion
        // directives, ask it. Otherwise, ask the VM.
        synchronized(loader.assertionLock) {
            if (loader.classAssertionStatus != null) {
                return loader.desiredAssertionStatus(getName());
            }
        }
        return desiredAssertionStatus0(this);
    }

    // Retrieves the desired assertion status of this class from the VM
    private static native boolean desiredAssertionStatus0(Class<?> clazz);

    public boolean isEnum() {
        // An enum must both directly extend java.lang.Enum and have
        // the ENUM bit set; classes for specialized enum constants
        // don't do the former.
        return (this.getModifiers() & ENUM) != 0 &&
        this.getSuperclass() == java.lang.Enum.class;
    }

    // Fetches the factory for reflective objects
    private static ReflectionFactory getReflectionFactory() {
        if (reflectionFactory == null) {
            reflectionFactory =
                java.security.AccessController.doPrivileged
                    (new sun.reflect.ReflectionFactory.GetReflectionFactoryAction());
        }
        return reflectionFactory;
    }
    private static ReflectionFactory reflectionFactory;

    // To be able to query system properties as soon as they're available
    private static boolean initted = false;
    private static void checkInitted() {
        if (initted) return;
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    // Tests to ensure the system properties table is fully
                    // initialized. This is needed because reflection code is
                    // called very early in the initialization process (before
                    // command-line arguments have been parsed and therefore
                    // these user-settable properties installed.) We assume that
                    // if System.out is non-null then the System class has been
                    // fully initialized and that the bulk of the startup code
                    // has been run.

                    if (System.out == null) {
                        // java.lang.System not yet fully initialized
                        return null;
                    }

                    // Doesn't use Boolean.getBoolean to avoid class init.
                    String val =
                        System.getProperty("sun.reflect.noCaches");
                    if (val != null && val.equals("true")) {
                        useCaches = false;
                    }

                    initted = true;
                    return null;
                }
            });
    }

    public T[] getEnumConstants() {
        T[] values = getEnumConstantsShared();
        return (values != null) ? values.clone() : null;
    }

    T[] getEnumConstantsShared() {
        if (enumConstants == null) {
            if (!isEnum()) return null;
            try {
                final Method values = getMethod("values");
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<Void>() {
                        public Void run() {
                                values.setAccessible(true);
                                return null;
                            }
                        });
                @SuppressWarnings("unchecked")
                T[] temporaryConstants = (T[])values.invoke(null);
                enumConstants = temporaryConstants;
            }
            // These can happen when users concoct enum-like classes
            // that don't comply with the enum spec.
            catch (InvocationTargetException | NoSuchMethodException |
                   IllegalAccessException ex) { return null; }
        }
        return enumConstants;
    }
    private volatile transient T[] enumConstants = null;

    Map<String, T> enumConstantDirectory() {
        if (enumConstantDirectory == null) {
            T[] universe = getEnumConstantsShared();
            if (universe == null)
                throw new IllegalArgumentException(
                    getName() + " is not an enum type");
            Map<String, T> m = new HashMap<>(2 * universe.length);
            for (T constant : universe)
                m.put(((Enum<?>)constant).name(), constant);
            enumConstantDirectory = m;
        }
        return enumConstantDirectory;
    }
    private volatile transient Map<String, T> enumConstantDirectory = null;

    @SuppressWarnings("unchecked")
    public T cast(Object obj) {
        if (obj != null && !isInstance(obj))
            throw new ClassCastException(cannotCastMsg(obj));
        return (T) obj;
    }

    private String cannotCastMsg(Object obj) {
        return "Cannot cast " + obj.getClass().getName() + " to " + getName();
    }

    @SuppressWarnings("unchecked")
    public <U> Class<? extends U> asSubclass(Class<U> clazz) {
        if (clazz.isAssignableFrom(this))
            return (Class<? extends U>) this;
        else
            throw new ClassCastException(this.toString());
    }

    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        Objects.requireNonNull(annotationClass);

        return (A) annotationData().annotations.get(annotationClass);
    }

    /**
     * {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return GenericDeclaration.super.isAnnotationPresent(annotationClass);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
        Objects.requireNonNull(annotationClass);

        AnnotationData annotationData = annotationData();
        return AnnotationSupport.getAssociatedAnnotations(annotationData.declaredAnnotations,
                                                          this,
                                                          annotationClass);
    }

    public Annotation[] getAnnotations() {
        return AnnotationParser.toArray(annotationData().annotations);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getDeclaredAnnotation(Class<A> annotationClass) {
        Objects.requireNonNull(annotationClass);

        return (A) annotationData().declaredAnnotations.get(annotationClass);
    }

    @Override
    public <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<A> annotationClass) {
        Objects.requireNonNull(annotationClass);

        return AnnotationSupport.getDirectlyAndIndirectlyPresent(annotationData().declaredAnnotations,
                                                                 annotationClass);
    }

    public Annotation[] getDeclaredAnnotations()  {
        return AnnotationParser.toArray(annotationData().declaredAnnotations);
    }

    // annotation data that might get invalidated when JVM TI RedefineClasses() is called
    private static class AnnotationData {
        final Map<Class<? extends Annotation>, Annotation> annotations;
        final Map<Class<? extends Annotation>, Annotation> declaredAnnotations;

        // Value of classRedefinedCount when we created this AnnotationData instance
        final int redefinedCount;

        AnnotationData(Map<Class<? extends Annotation>, Annotation> annotations,
                       Map<Class<? extends Annotation>, Annotation> declaredAnnotations,
                       int redefinedCount) {
            this.annotations = annotations;
            this.declaredAnnotations = declaredAnnotations;
            this.redefinedCount = redefinedCount;
        }
    }

    // Annotations cache
    @SuppressWarnings("UnusedDeclaration")
    private volatile transient AnnotationData annotationData;

    private AnnotationData annotationData() {
        while (true) { // retry loop
            AnnotationData annotationData = this.annotationData;
            int classRedefinedCount = this.classRedefinedCount;
            if (annotationData != null &&
                annotationData.redefinedCount == classRedefinedCount) {
                return annotationData;
            }
            // null or stale annotationData -> optimistically create new instance
            AnnotationData newAnnotationData = createAnnotationData(classRedefinedCount);
            // try to install it
            if (Atomic.casAnnotationData(this, annotationData, newAnnotationData)) {
                // successfully installed new AnnotationData
                return newAnnotationData;
            }
        }
    }

    private AnnotationData createAnnotationData(int classRedefinedCount) {
        Map<Class<? extends Annotation>, Annotation> declaredAnnotations =
            AnnotationParser.parseAnnotations(getRawAnnotations(), getConstantPool(), this);
        Class<?> superClass = getSuperclass();
        Map<Class<? extends Annotation>, Annotation> annotations = null;
        if (superClass != null) {
            Map<Class<? extends Annotation>, Annotation> superAnnotations =
                superClass.annotationData().annotations;
            for (Map.Entry<Class<? extends Annotation>, Annotation> e : superAnnotations.entrySet()) {
                Class<? extends Annotation> annotationClass = e.getKey();
                if (AnnotationType.getInstance(annotationClass).isInherited()) {
                    if (annotations == null) { // lazy construction
                        annotations = new LinkedHashMap<>((Math.max(
                                declaredAnnotations.size(),
                                Math.min(12, declaredAnnotations.size() + superAnnotations.size())
                            ) * 4 + 2) / 3
                        );
                    }
                    annotations.put(annotationClass, e.getValue());
                }
            }
        }
        if (annotations == null) {
            // no inherited annotations -> share the Map with declaredAnnotations
            annotations = declaredAnnotations;
        } else {
            // at least one inherited annotation -> declared may override inherited
            annotations.putAll(declaredAnnotations);
        }
        return new AnnotationData(annotations, declaredAnnotations, classRedefinedCount);
    }

    // Annotation types cache their internal (AnnotationType) form

    @SuppressWarnings("UnusedDeclaration")
    private volatile transient AnnotationType annotationType;

    boolean casAnnotationType(AnnotationType oldType, AnnotationType newType) {
        return Atomic.casAnnotationType(this, oldType, newType);
    }

    AnnotationType getAnnotationType() {
        return annotationType;
    }

    Map<Class<? extends Annotation>, Annotation> getDeclaredAnnotationMap() {
        return annotationData().declaredAnnotations;
    }

    /* Backing store of user-defined values pertaining to this class.
     * Maintained by the ClassValue class.
     */
    transient ClassValue.ClassValueMap classValueMap;

    public AnnotatedType getAnnotatedSuperclass() {
        if (this == Object.class ||
                isInterface() ||
                isArray() ||
                isPrimitive() ||
                this == Void.TYPE) {
            return null;
        }

        return TypeAnnotationParser.buildAnnotatedSuperclass(getRawTypeAnnotations(), getConstantPool(), this);
    }

    public AnnotatedType[] getAnnotatedInterfaces() {
         return TypeAnnotationParser.buildAnnotatedInterfaces(getRawTypeAnnotations(), getConstantPool(), this);
    }
}
