package sun.misc;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import sun.security.action.GetBooleanAction;

public class ProxyGenerator {

    private static final int CLASSFILE_MAJOR_VERSION = 49;
    private static final int CLASSFILE_MINOR_VERSION = 0;

    /* constant pool tags */
    private static final int CONSTANT_UTF8              = 1;
    private static final int CONSTANT_UNICODE           = 2;
    private static final int CONSTANT_INTEGER           = 3;
    private static final int CONSTANT_FLOAT             = 4;
    private static final int CONSTANT_LONG              = 5;
    private static final int CONSTANT_DOUBLE            = 6;
    private static final int CONSTANT_CLASS             = 7;
    private static final int CONSTANT_STRING            = 8;
    private static final int CONSTANT_FIELD             = 9;
    private static final int CONSTANT_METHOD            = 10;
    private static final int CONSTANT_INTERFACEMETHOD   = 11;
    private static final int CONSTANT_NAMEANDTYPE       = 12;

    /* access and modifier flags */
    private static final int ACC_PUBLIC                 = 0x00000001;
    private static final int ACC_PRIVATE                = 0x00000002;
    private static final int ACC_STATIC                 = 0x00000008;
    private static final int ACC_FINAL                  = 0x00000010;
    private static final int ACC_SUPER                  = 0x00000020;

    /* opcodes */
    private static final int opc_aconst_null            = 1;
    private static final int opc_iconst_0               = 3;
    private static final int opc_bipush                 = 16;
    private static final int opc_sipush                 = 17;
    private static final int opc_ldc                    = 18;
    private static final int opc_ldc_w                  = 19;
    private static final int opc_iload                  = 21;
    private static final int opc_lload                  = 22;
    private static final int opc_fload                  = 23;
    private static final int opc_dload                  = 24;
    private static final int opc_aload                  = 25;
    private static final int opc_iload_0                = 26;
    private static final int opc_lload_0                = 30;
    private static final int opc_fload_0                = 34;
    private static final int opc_dload_0                = 38;
    private static final int opc_aload_0                = 42;
    private static final int opc_astore                 = 58;
    private static final int opc_astore_0               = 75;
    private static final int opc_aastore                = 83;
    private static final int opc_pop                    = 87;
    private static final int opc_dup                    = 89;
    private static final int opc_ireturn                = 172;
    private static final int opc_lreturn                = 173;
    private static final int opc_freturn                = 174;
    private static final int opc_dreturn                = 175;
    private static final int opc_areturn                = 176;
    private static final int opc_return                 = 177;
    private static final int opc_getstatic              = 178;
    private static final int opc_putstatic              = 179;
    private static final int opc_getfield               = 180;
    private static final int opc_invokevirtual          = 182;
    private static final int opc_invokespecial          = 183;
    private static final int opc_invokestatic           = 184;
    private static final int opc_invokeinterface        = 185;
    private static final int opc_new                    = 187;
    private static final int opc_anewarray              = 189;
    private static final int opc_athrow                 = 191;
    private static final int opc_checkcast              = 192;

    private static final int opc_wide                   = 196;



    private final static String superclassName = "java/lang/reflect/Proxy";

    private final static String handlerFieldName = "h";

    private final static boolean saveGeneratedFiles = java.security.AccessController.doPrivileged(new GetBooleanAction("sun.misc.ProxyGenerator.saveGeneratedFiles")).booleanValue();

    public static byte[] generateProxyClass(final String name, Class<?>[] interfaces) {
        return generateProxyClass(name, interfaces, (ACC_PUBLIC | ACC_FINAL | ACC_SUPER));
    }

    public static byte[] generateProxyClass(final String name, Class<?>[] interfaces, int accessFlags) {
        ProxyGenerator gen = new ProxyGenerator(name, interfaces, accessFlags);
        // 生成字节码
        final byte[] classFile = gen.generateClassFile();

        if (saveGeneratedFiles) {
            java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Void>() {
                public Void run() {
                    try {
                        int i = name.lastIndexOf('.');
                        Path path;
                        if (i > 0) {
                            Path dir = Paths.get(name.substring(0, i).replace('.', File.separatorChar));
                            Files.createDirectories(dir);
                            path = dir.resolve(name.substring(i+1, name.length()) + ".class");
                        } else {
                            path = Paths.get(name + ".class");
                        }
                        Files.write(path, classFile);
                        return null;
                    } catch (IOException e) {
                        throw new InternalError("I/O exception saving generated file: " + e);
                    }
                }
            });
        }

        return classFile;
    }

    private static Method hashCodeMethod;
    private static Method equalsMethod;
    private static Method toStringMethod;
    static {
        try {
            hashCodeMethod = Object.class.getMethod("hashCode");
            equalsMethod = Object.class.getMethod("equals", new Class<?>[] { Object.class });
            toStringMethod = Object.class.getMethod("toString");
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    private String className;

    private Class<?>[] interfaces;

    private int accessFlags;

    private ConstantPool cp = new ConstantPool();

    private List<FieldInfo> fields = new ArrayList<>();

    private List<MethodInfo> methods = new ArrayList<>();

    private Map<String, List<ProxyMethod>> proxyMethods = new HashMap<>();

    private int proxyMethodCount = 0;

    private ProxyGenerator(String className, Class<?>[] interfaces, int accessFlags) {
        this.className = className;
        this.interfaces = interfaces;
        this.accessFlags = accessFlags;
    }

    // 生成字节码
    private byte[] generateClassFile() {
        // 只代理Object的hashCode、equals和toString
        addProxyMethod(hashCodeMethod, Object.class);
        addProxyMethod(equalsMethod, Object.class);
        addProxyMethod(toStringMethod, Object.class);
        // 代理接口的每个方法
        for (Class<?> intf : interfaces) {
            for (Method m : intf.getMethods()) {
                addProxyMethod(m, intf);
            }
        }


        for (List<ProxyMethod> sigmethods : proxyMethods.values()) {
            checkReturnTypes(sigmethods);
        }


        try {
            // 添加带有 InvocationHandler 参数的构造器
            methods.add(generateConstructor());

            for (List<ProxyMethod> sigmethods : proxyMethods.values()) {
                for (ProxyMethod pm : sigmethods) {
                    // add static field for method's Method object
                    fields.add(new FieldInfo(pm.methodFieldName, "Ljava/lang/reflect/Method;", ACC_PRIVATE | ACC_STATIC));
                    // generate code for proxy method and add it
                    methods.add(pm.generateMethod());
                }
            }
            // 类初始化，cinit 方法
            methods.add(generateStaticInitializer());

        } catch (IOException e) {
            throw new InternalError("unexpected I/O Exception", e);
        }
        if (methods.size() > 65535) {
            throw new IllegalArgumentException("method limit exceeded");
        }
        if (fields.size() > 65535) {
            throw new IllegalArgumentException("field limit exceeded");
        }

        cp.getClass(dotToSlash(className));
        cp.getClass(superclassName);
        for (Class<?> intf: interfaces) {
            cp.getClass(dotToSlash(intf.getName()));
        }

        // 常量池设为只读
        cp.setReadOnly();

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);

        try {
            // u4 magic 魔数
            dout.writeInt(0xCAFEBABE);
            // u2 minor_version; 副版本
            dout.writeShort(CLASSFILE_MINOR_VERSION);
            // u2 major_version; 主版本
            dout.writeShort(CLASSFILE_MAJOR_VERSION);
            // write constant pool 写入常量池
            cp.write(dout);
            // u2 access_flags; 访问标识
            dout.writeShort(accessFlags);
            // u2 this_class; 类名
            dout.writeShort(cp.getClass(dotToSlash(className)));
            // u2 super_class; 父类
            dout.writeShort(cp.getClass(superclassName));

            // u2 interfaces_count; 接口集合
            dout.writeShort(interfaces.length);
            // u2 interfaces[interfaces_count];
            for (Class<?> intf : interfaces) {
                dout.writeShort(cp.getClass(dotToSlash(intf.getName())));
            }

            // u2 fields_count; 字段表
            dout.writeShort(fields.size());
            // field_info fields[fields_count];
            for (FieldInfo f : fields) {
                f.write(dout);
            }

           // u2 methods_count; 方法表
            dout.writeShort(methods.size());
            // method_info methods[methods_count];
            for (MethodInfo m : methods) {
                m.write(dout);
            }
            // u2 attributes_count; 属性表
            dout.writeShort(0); // (no ClassFile attributes for proxy classes)

        } catch (IOException e) {
            throw new InternalError("unexpected I/O Exception", e);
        }
        // 输出字节流
        return bout.toByteArray();
    }

    private void addProxyMethod(Method m, Class<?> fromClass) {
        String name = m.getName();
        Class<?>[] parameterTypes = m.getParameterTypes();
        Class<?> returnType = m.getReturnType();
        Class<?>[] exceptionTypes = m.getExceptionTypes();

        String sig = name + getParameterDescriptors(parameterTypes);
        List<ProxyMethod> sigmethods = proxyMethods.get(sig);
        if (sigmethods != null) {
            for (ProxyMethod pm : sigmethods) {
                if (returnType == pm.returnType) {
                    List<Class<?>> legalExceptions = new ArrayList<>();
                    collectCompatibleTypes(exceptionTypes, pm.exceptionTypes, legalExceptions);
                    collectCompatibleTypes(pm.exceptionTypes, exceptionTypes, legalExceptions);
                    pm.exceptionTypes = new Class<?>[legalExceptions.size()];
                    pm.exceptionTypes = legalExceptions.toArray(pm.exceptionTypes);
                    return;
                }
            }
        } else {
            sigmethods = new ArrayList<>(3);
            proxyMethods.put(sig, sigmethods);
        }
        sigmethods.add(new ProxyMethod(name, parameterTypes, returnType, exceptionTypes, fromClass));
    }

    private static void checkReturnTypes(List<ProxyMethod> methods) {
        /*
         * If there is only one method with a given signature, there
         * cannot be a conflict.  This is the only case in which a
         * primitive (or void) return type is allowed.
         */
        if (methods.size() < 2) {
            return;
        }

        /*
         * List of return types that are not yet known to be
         * assignable from ("covered" by) any of the others.
         */
        LinkedList<Class<?>> uncoveredReturnTypes = new LinkedList<>();

    nextNewReturnType:
        for (ProxyMethod pm : methods) {
            Class<?> newReturnType = pm.returnType;
            if (newReturnType.isPrimitive()) {
                throw new IllegalArgumentException("methods with same signature " + getFriendlyMethodSignature(pm.methodName, pm.parameterTypes) + " but incompatible return types: " + newReturnType.getName() + " and others");
            }
            boolean added = false;

            /*
             * Compare the new return type to the existing uncovered
             * return types.
             */
            ListIterator<Class<?>> liter = uncoveredReturnTypes.listIterator();
            while (liter.hasNext()) {
                Class<?> uncoveredReturnType = liter.next();

                /*
                 * If an existing uncovered return type is assignable
                 * to this new one, then we can forget the new one.
                 */
                if (newReturnType.isAssignableFrom(uncoveredReturnType)) {
                    assert !added;
                    continue nextNewReturnType;
                }

                /*
                 * If the new return type is assignable to an existing
                 * uncovered one, then should replace the existing one
                 * with the new one (or just forget the existing one,
                 * if the new one has already be put in the list).
                 */
                if (uncoveredReturnType.isAssignableFrom(newReturnType)) {
                    // (we can assume that each return type is unique)
                    if (!added) {
                        liter.set(newReturnType);
                        added = true;
                    } else {
                        liter.remove();
                    }
                }
            }

            /*
             * If we got through the list of existing uncovered return
             * types without an assignability relationship, then add
             * the new return type to the list of uncovered ones.
             */
            if (!added) {
                uncoveredReturnTypes.add(newReturnType);
            }
        }

        /*
         * We shouldn't end up with more than one return type that is
         * not assignable from any of the others.
         */
        if (uncoveredReturnTypes.size() > 1) {
            ProxyMethod pm = methods.get(0);
            throw new IllegalArgumentException(
                "methods with same signature " +
                getFriendlyMethodSignature(pm.methodName, pm.parameterTypes) +
                " but incompatible return types: " + uncoveredReturnTypes);
        }
    }

    private class FieldInfo {
        public int accessFlags;
        public String name;
        public String descriptor;

        public FieldInfo(String name, String descriptor, int accessFlags) {
            this.name = name;
            this.descriptor = descriptor;
            this.accessFlags = accessFlags;

            /*
             * Make sure that constant pool indexes are reserved for the
             * following items before starting to write the final class file.
             */
            cp.getUtf8(name);
            cp.getUtf8(descriptor);
        }

        public void write(DataOutputStream out) throws IOException {
            /*
             * Write all the items of the "field_info" structure.
             * See JVMS section 4.5.
             */
                                        // u2 access_flags;
            out.writeShort(accessFlags);
                                        // u2 name_index;
            out.writeShort(cp.getUtf8(name));
                                        // u2 descriptor_index;
            out.writeShort(cp.getUtf8(descriptor));
                                        // u2 attributes_count;
            out.writeShort(0);  // (no field_info attributes for proxy classes)
        }
    }

    private static class ExceptionTableEntry {
        public short startPc;
        public short endPc;
        public short handlerPc;
        public short catchType;

        public ExceptionTableEntry(short startPc, short endPc, short handlerPc, short catchType)
        {
            this.startPc = startPc;
            this.endPc = endPc;
            this.handlerPc = handlerPc;
            this.catchType = catchType;
        }
    };

    private class MethodInfo {
        public int accessFlags;
        public String name;
        public String descriptor;
        public short maxStack;
        public short maxLocals;
        public ByteArrayOutputStream code = new ByteArrayOutputStream();
        public List<ExceptionTableEntry> exceptionTable = new ArrayList<ExceptionTableEntry>();
        public short[] declaredExceptions;

        public MethodInfo(String name, String descriptor, int accessFlags) {
            this.name = name;
            this.descriptor = descriptor;
            this.accessFlags = accessFlags;

            /*
             * Make sure that constant pool indexes are reserved for the
             * following items before starting to write the final class file.
             */
            cp.getUtf8(name);
            cp.getUtf8(descriptor);
            cp.getUtf8("Code");
            cp.getUtf8("Exceptions");
        }

        public void write(DataOutputStream out) throws IOException {
            /*
             * Write all the items of the "method_info" structure.
             * See JVMS section 4.6.
             */
                                        // u2 access_flags;
            out.writeShort(accessFlags);
                                        // u2 name_index;
            out.writeShort(cp.getUtf8(name));
                                        // u2 descriptor_index;
            out.writeShort(cp.getUtf8(descriptor));
                                        // u2 attributes_count;
            out.writeShort(2);  // (two method_info attributes:)

            // Write "Code" attribute. See JVMS section 4.7.3.

                                        // u2 attribute_name_index;
            out.writeShort(cp.getUtf8("Code"));
                                        // u4 attribute_length;
            out.writeInt(12 + code.size() + 8 * exceptionTable.size());
                                        // u2 max_stack;
            out.writeShort(maxStack);
                                        // u2 max_locals;
            out.writeShort(maxLocals);
                                        // u2 code_length;
            out.writeInt(code.size());
                                        // u1 code[code_length];
            code.writeTo(out);
                                        // u2 exception_table_length;
            out.writeShort(exceptionTable.size());
            for (ExceptionTableEntry e : exceptionTable) {
                                        // u2 start_pc;
                out.writeShort(e.startPc);
                                        // u2 end_pc;
                out.writeShort(e.endPc);
                                        // u2 handler_pc;
                out.writeShort(e.handlerPc);
                                        // u2 catch_type;
                out.writeShort(e.catchType);
            }
                                        // u2 attributes_count;
            out.writeShort(0);

            // write "Exceptions" attribute.  See JVMS section 4.7.4.

                                        // u2 attribute_name_index;
            out.writeShort(cp.getUtf8("Exceptions"));
                                        // u4 attributes_length;
            out.writeInt(2 + 2 * declaredExceptions.length);
                                        // u2 number_of_exceptions;
            out.writeShort(declaredExceptions.length);
                        // u2 exception_index_table[number_of_exceptions];
            for (short value : declaredExceptions) {
                out.writeShort(value);
            }
        }

    }

    private class ProxyMethod {

        public String methodName;
        public Class<?>[] parameterTypes;
        public Class<?> returnType;
        public Class<?>[] exceptionTypes;
        public Class<?> fromClass;
        public String methodFieldName;

        private ProxyMethod(String methodName, Class<?>[] parameterTypes,
                            Class<?> returnType, Class<?>[] exceptionTypes,
                            Class<?> fromClass)
        {
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
            this.returnType = returnType;
            this.exceptionTypes = exceptionTypes;
            this.fromClass = fromClass;
            this.methodFieldName = "m" + proxyMethodCount++;
        }

        private MethodInfo generateMethod() throws IOException {
            String desc = getMethodDescriptor(parameterTypes, returnType);
            MethodInfo minfo = new MethodInfo(methodName, desc, ACC_PUBLIC | ACC_FINAL);

            int[] parameterSlot = new int[parameterTypes.length];
            int nextSlot = 1;
            for (int i = 0; i < parameterSlot.length; i++) {
                parameterSlot[i] = nextSlot;
                nextSlot += getWordsPerType(parameterTypes[i]);
            }
            int localSlot0 = nextSlot;
            short pc, tryBegin = 0, tryEnd;

            DataOutputStream out = new DataOutputStream(minfo.code);

            code_aload(0, out);

            out.writeByte(opc_getfield);
            out.writeShort(cp.getFieldRef(
                superclassName,
                handlerFieldName, "Ljava/lang/reflect/InvocationHandler;"));

            code_aload(0, out);

            out.writeByte(opc_getstatic);
            out.writeShort(cp.getFieldRef(
                dotToSlash(className),
                methodFieldName, "Ljava/lang/reflect/Method;"));

            if (parameterTypes.length > 0) {

                code_ipush(parameterTypes.length, out);

                out.writeByte(opc_anewarray);
                out.writeShort(cp.getClass("java/lang/Object"));

                for (int i = 0; i < parameterTypes.length; i++) {

                    out.writeByte(opc_dup);

                    code_ipush(i, out);

                    codeWrapArgument(parameterTypes[i], parameterSlot[i], out);

                    out.writeByte(opc_aastore);
                }
            } else {

                out.writeByte(opc_aconst_null);
            }

            out.writeByte(opc_invokeinterface);
            out.writeShort(cp.getInterfaceMethodRef(
                "java/lang/reflect/InvocationHandler",
                "invoke",
                "(Ljava/lang/Object;Ljava/lang/reflect/Method;" + "[Ljava/lang/Object;)Ljava/lang/Object;"));
            out.writeByte(4);
            out.writeByte(0);

            if (returnType == void.class) {

                out.writeByte(opc_pop);

                out.writeByte(opc_return);

            } else {

                codeUnwrapReturnValue(returnType, out);
            }

            tryEnd = pc = (short) minfo.code.size();

            List<Class<?>> catchList = computeUniqueCatchList(exceptionTypes);
            if (catchList.size() > 0) {

                for (Class<?> ex : catchList) {
                    minfo.exceptionTable.add(new ExceptionTableEntry(
                        tryBegin, tryEnd, pc,
                        cp.getClass(dotToSlash(ex.getName()))));
                }

                out.writeByte(opc_athrow);

                pc = (short) minfo.code.size();

                minfo.exceptionTable.add(new ExceptionTableEntry(
                    tryBegin, tryEnd, pc, cp.getClass("java/lang/Throwable")));

                code_astore(localSlot0, out);

                out.writeByte(opc_new);
                out.writeShort(cp.getClass(
                    "java/lang/reflect/UndeclaredThrowableException"));

                out.writeByte(opc_dup);

                code_aload(localSlot0, out);

                out.writeByte(opc_invokespecial);

                out.writeShort(cp.getMethodRef(
                    "java/lang/reflect/UndeclaredThrowableException",
                    "<init>", "(Ljava/lang/Throwable;)V"));

                out.writeByte(opc_athrow);
            }

            if (minfo.code.size() > 65535) {
                throw new IllegalArgumentException("code size limit exceeded");
            }

            minfo.maxStack = 10;
            minfo.maxLocals = (short) (localSlot0 + 1);
            minfo.declaredExceptions = new short[exceptionTypes.length];
            for (int i = 0; i < exceptionTypes.length; i++) {
                minfo.declaredExceptions[i] = cp.getClass(
                    dotToSlash(exceptionTypes[i].getName()));
            }

            return minfo;
        }

        private void codeWrapArgument(Class<?> type, int slot, DataOutputStream out) throws IOException {
            if (type.isPrimitive()) {
                PrimitiveTypeInfo prim = PrimitiveTypeInfo.get(type);

                if (type == int.class ||
                    type == boolean.class ||
                    type == byte.class ||
                    type == char.class ||
                    type == short.class)
                {
                    code_iload(slot, out);
                } else if (type == long.class) {
                    code_lload(slot, out);
                } else if (type == float.class) {
                    code_fload(slot, out);
                } else if (type == double.class) {
                    code_dload(slot, out);
                } else {
                    throw new AssertionError();
                }

                out.writeByte(opc_invokestatic);
                out.writeShort(cp.getMethodRef(
                    prim.wrapperClassName,
                    "valueOf", prim.wrapperValueOfDesc));

            } else {

                code_aload(slot, out);
            }
        }

        private void codeUnwrapReturnValue(Class<?> type, DataOutputStream out) throws IOException {
            if (type.isPrimitive()) {
                PrimitiveTypeInfo prim = PrimitiveTypeInfo.get(type);

                out.writeByte(opc_checkcast);
                out.writeShort(cp.getClass(prim.wrapperClassName));

                out.writeByte(opc_invokevirtual);
                out.writeShort(cp.getMethodRef(
                    prim.wrapperClassName,
                    prim.unwrapMethodName, prim.unwrapMethodDesc));

                if (type == int.class ||
                    type == boolean.class ||
                    type == byte.class ||
                    type == char.class ||
                    type == short.class)
                {
                    out.writeByte(opc_ireturn);
                } else if (type == long.class) {
                    out.writeByte(opc_lreturn);
                } else if (type == float.class) {
                    out.writeByte(opc_freturn);
                } else if (type == double.class) {
                    out.writeByte(opc_dreturn);
                } else {
                    throw new AssertionError();
                }

            } else {

                out.writeByte(opc_checkcast);
                out.writeShort(cp.getClass(dotToSlash(type.getName())));

                out.writeByte(opc_areturn);
            }
        }

        private void codeFieldInitialization(DataOutputStream out)
            throws IOException
        {
            codeClassForName(fromClass, out);

            code_ldc(cp.getString(methodName), out);

            code_ipush(parameterTypes.length, out);

            out.writeByte(opc_anewarray);
            out.writeShort(cp.getClass("java/lang/Class"));

            for (int i = 0; i < parameterTypes.length; i++) {

                out.writeByte(opc_dup);

                code_ipush(i, out);

                if (parameterTypes[i].isPrimitive()) {
                    PrimitiveTypeInfo prim =
                        PrimitiveTypeInfo.get(parameterTypes[i]);

                    out.writeByte(opc_getstatic);
                    out.writeShort(cp.getFieldRef(
                        prim.wrapperClassName, "TYPE", "Ljava/lang/Class;"));

                } else {
                    codeClassForName(parameterTypes[i], out);
                }

                out.writeByte(opc_aastore);
            }

            out.writeByte(opc_invokevirtual);
            out.writeShort(cp.getMethodRef(
                "java/lang/Class",
                "getMethod",
                "(Ljava/lang/String;[Ljava/lang/Class;)" +
                "Ljava/lang/reflect/Method;"));

            out.writeByte(opc_putstatic);
            out.writeShort(cp.getFieldRef(
                dotToSlash(className),
                methodFieldName, "Ljava/lang/reflect/Method;"));
        }
    }

    private MethodInfo generateConstructor() throws IOException {
        MethodInfo minfo = new MethodInfo(
            "<init>", "(Ljava/lang/reflect/InvocationHandler;)V",
            ACC_PUBLIC);

        DataOutputStream out = new DataOutputStream(minfo.code);

        code_aload(0, out);

        code_aload(1, out);

        out.writeByte(opc_invokespecial);
        out.writeShort(cp.getMethodRef(
            superclassName,
            "<init>", "(Ljava/lang/reflect/InvocationHandler;)V"));

        out.writeByte(opc_return);

        minfo.maxStack = 10;
        minfo.maxLocals = 2;
        minfo.declaredExceptions = new short[0];

        return minfo;
    }

    private MethodInfo generateStaticInitializer() throws IOException {
        MethodInfo minfo = new MethodInfo("<clinit>", "()V", ACC_STATIC);

        int localSlot0 = 1;
        short pc, tryBegin = 0, tryEnd;

        DataOutputStream out = new DataOutputStream(minfo.code);

        for (List<ProxyMethod> sigmethods : proxyMethods.values()) {
            for (ProxyMethod pm : sigmethods) {
                pm.codeFieldInitialization(out);
            }
        }

        out.writeByte(opc_return);

        tryEnd = pc = (short) minfo.code.size();

        minfo.exceptionTable.add(new ExceptionTableEntry(
            tryBegin, tryEnd, pc,
            cp.getClass("java/lang/NoSuchMethodException")));

        code_astore(localSlot0, out);

        out.writeByte(opc_new);
        out.writeShort(cp.getClass("java/lang/NoSuchMethodError"));

        out.writeByte(opc_dup);

        code_aload(localSlot0, out);

        out.writeByte(opc_invokevirtual);
        out.writeShort(cp.getMethodRef("java/lang/Throwable", "getMessage", "()Ljava/lang/String;"));

        out.writeByte(opc_invokespecial);
        out.writeShort(cp.getMethodRef("java/lang/NoSuchMethodError", "<init>", "(Ljava/lang/String;)V"));

        out.writeByte(opc_athrow);

        pc = (short) minfo.code.size();

        minfo.exceptionTable.add(new ExceptionTableEntry(
            tryBegin, tryEnd, pc,
            cp.getClass("java/lang/ClassNotFoundException")));

        code_astore(localSlot0, out);

        out.writeByte(opc_new);
        out.writeShort(cp.getClass("java/lang/NoClassDefFoundError"));

        out.writeByte(opc_dup);

        code_aload(localSlot0, out);

        out.writeByte(opc_invokevirtual);
        out.writeShort(cp.getMethodRef(
            "java/lang/Throwable", "getMessage", "()Ljava/lang/String;"));

        out.writeByte(opc_invokespecial);
        out.writeShort(cp.getMethodRef(
            "java/lang/NoClassDefFoundError",
            "<init>", "(Ljava/lang/String;)V"));

        out.writeByte(opc_athrow);

        if (minfo.code.size() > 65535) {
            throw new IllegalArgumentException("code size limit exceeded");
        }

        minfo.maxStack = 10;
        minfo.maxLocals = (short) (localSlot0 + 1);
        minfo.declaredExceptions = new short[0];

        return minfo;
    }


    /*
     * =============== Code Generation Utility Methods ===============
     */

    /*
     * The following methods generate code for the load or store operation
     * indicated by their name for the given local variable.  The code is
     * written to the supplied stream.
     */

    private void code_iload(int lvar, DataOutputStream out) throws IOException {
        codeLocalLoadStore(lvar, opc_iload, opc_iload_0, out);
    }

    private void code_lload(int lvar, DataOutputStream out) throws IOException {
        codeLocalLoadStore(lvar, opc_lload, opc_lload_0, out);
    }

    private void code_fload(int lvar, DataOutputStream out) throws IOException {
        codeLocalLoadStore(lvar, opc_fload, opc_fload_0, out);
    }

    private void code_dload(int lvar, DataOutputStream out) throws IOException {
        codeLocalLoadStore(lvar, opc_dload, opc_dload_0, out);
    }

    private void code_aload(int lvar, DataOutputStream out) throws IOException {
        codeLocalLoadStore(lvar, opc_aload, opc_aload_0, out);
    }

    private void code_astore(int lvar, DataOutputStream out) throws IOException {
        codeLocalLoadStore(lvar, opc_astore, opc_astore_0, out);
    }

    private void codeLocalLoadStore(int lvar, int opcode, int opcode_0, DataOutputStream out) throws IOException {
        assert lvar >= 0 && lvar <= 0xFFFF;
        if (lvar <= 3) {
            out.writeByte(opcode_0 + lvar);
        } else if (lvar <= 0xFF) {
            out.writeByte(opcode);
            out.writeByte(lvar & 0xFF);
        } else {
            /*
             * Use the "wide" instruction modifier for local variable
             * indexes that do not fit into an unsigned byte.
             */
            out.writeByte(opc_wide);
            out.writeByte(opcode);
            out.writeShort(lvar & 0xFFFF);
        }
    }

    private void code_ldc(int index, DataOutputStream out) throws IOException {
        assert index >= 0 && index <= 0xFFFF;
        if (index <= 0xFF) {
            out.writeByte(opc_ldc);
            out.writeByte(index & 0xFF);
        } else {
            out.writeByte(opc_ldc_w);
            out.writeShort(index & 0xFFFF);
        }
    }

    private void code_ipush(int value, DataOutputStream out) throws IOException {
        if (value >= -1 && value <= 5) {
            out.writeByte(opc_iconst_0 + value);
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            out.writeByte(opc_bipush);
            out.writeByte(value & 0xFF);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            out.writeByte(opc_sipush);
            out.writeShort(value & 0xFFFF);
        } else {
            throw new AssertionError();
        }
    }

    private void codeClassForName(Class<?> cl, DataOutputStream out) throws IOException {
        code_ldc(cp.getString(cl.getName()), out);

        out.writeByte(opc_invokestatic);
        out.writeShort(cp.getMethodRef(
            "java/lang/Class",
            "forName", "(Ljava/lang/String;)Ljava/lang/Class;"));
    }


    /*
     * ==================== General Utility Methods ====================
     */

    private static String dotToSlash(String name) {
        return name.replace('.', '/');
    }

    private static String getMethodDescriptor(Class<?>[] parameterTypes, Class<?> returnType) {
        return getParameterDescriptors(parameterTypes) +
            ((returnType == void.class) ? "V" : getFieldType(returnType));
    }

    private static String getParameterDescriptors(Class<?>[] parameterTypes) {
        StringBuilder desc = new StringBuilder("(");
        for (int i = 0; i < parameterTypes.length; i++) {
            desc.append(getFieldType(parameterTypes[i]));
        }
        desc.append(')');
        return desc.toString();
    }

    private static String getFieldType(Class<?> type) {
        if (type.isPrimitive()) {
            return PrimitiveTypeInfo.get(type).baseTypeString;
        } else if (type.isArray()) {
            return type.getName().replace('.', '/');
        } else {
            return "L" + dotToSlash(type.getName()) + ";";
        }
    }

    private static String getFriendlyMethodSignature(String name, Class<?>[] parameterTypes) {
        StringBuilder sig = new StringBuilder(name);
        sig.append('(');
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                sig.append(',');
            }
            Class<?> parameterType = parameterTypes[i];
            int dimensions = 0;
            while (parameterType.isArray()) {
                parameterType = parameterType.getComponentType();
                dimensions++;
            }
            sig.append(parameterType.getName());
            while (dimensions-- > 0) {
                sig.append("[]");
            }
        }
        sig.append(')');
        return sig.toString();
    }

    private static int getWordsPerType(Class<?> type) {
        if (type == long.class || type == double.class) {
            return 2;
        } else {
            return 1;
        }
    }

    private static void collectCompatibleTypes(Class<?>[] from, Class<?>[] with, List<Class<?>> list) {
        for (Class<?> fc: from) {
            if (!list.contains(fc)) {
                for (Class<?> wc: with) {
                    if (wc.isAssignableFrom(fc)) {
                        list.add(fc);
                        break;
                    }
                }
            }
        }
    }

    private static List<Class<?>> computeUniqueCatchList(Class<?>[] exceptions) {
        List<Class<?>> uniqueList = new ArrayList<>();
                                                // unique exceptions to catch

        uniqueList.add(Error.class);            // always catch/rethrow these
        uniqueList.add(RuntimeException.class);

    nextException:
        for (Class<?> ex: exceptions) {
            if (ex.isAssignableFrom(Throwable.class)) {
                /*
                 * If Throwable is declared to be thrown by the proxy method,
                 * then no catch blocks are necessary, because the invoke
                 * can, at most, throw Throwable anyway.
                 */
                uniqueList.clear();
                break;
            } else if (!Throwable.class.isAssignableFrom(ex)) {
                /*
                 * Ignore types that cannot be thrown by the invoke method.
                 */
                continue;
            }
            /*
             * Compare this exception against the current list of
             * exceptions that need to be caught:
             */
            for (int j = 0; j < uniqueList.size();) {
                Class<?> ex2 = uniqueList.get(j);
                if (ex2.isAssignableFrom(ex)) {
                    /*
                     * if a superclass of this exception is already on
                     * the list to catch, then ignore this one and continue;
                     */
                    continue nextException;
                } else if (ex.isAssignableFrom(ex2)) {
                    /*
                     * if a subclass of this exception is on the list
                     * to catch, then remove it;
                     */
                    uniqueList.remove(j);
                } else {
                    j++;        // else continue comparing.
                }
            }
            // This exception is unique (so far): add it to the list to catch.
            uniqueList.add(ex);
        }
        return uniqueList;
    }

    private static class PrimitiveTypeInfo {

        public String baseTypeString;

        public String wrapperClassName;

        public String wrapperValueOfDesc;

        public String unwrapMethodName;

        public String unwrapMethodDesc;

        private static Map<Class<?>,PrimitiveTypeInfo> table = new HashMap<>();
        static {
            add(byte.class, Byte.class);
            add(char.class, Character.class);
            add(double.class, Double.class);
            add(float.class, Float.class);
            add(int.class, Integer.class);
            add(long.class, Long.class);
            add(short.class, Short.class);
            add(boolean.class, Boolean.class);
        }

        private static void add(Class<?> primitiveClass, Class<?> wrapperClass) {
            table.put(primitiveClass,
                      new PrimitiveTypeInfo(primitiveClass, wrapperClass));
        }

        private PrimitiveTypeInfo(Class<?> primitiveClass, Class<?> wrapperClass) {
            assert primitiveClass.isPrimitive();

            baseTypeString =
                Array.newInstance(primitiveClass, 0)
                .getClass().getName().substring(1);
            wrapperClassName = dotToSlash(wrapperClass.getName());
            wrapperValueOfDesc =
                "(" + baseTypeString + ")L" + wrapperClassName + ";";
            unwrapMethodName = primitiveClass.getName() + "Value";
            unwrapMethodDesc = "()" + baseTypeString;
        }

        public static PrimitiveTypeInfo get(Class<?> cl) {
            return table.get(cl);
        }
    }


    private static class ConstantPool {

        private List<Entry> pool = new ArrayList<>(32);

        private Map<Object,Short> map = new HashMap<>(16);

        private boolean readOnly = false;

        public short getUtf8(String s) {
            if (s == null) {
                throw new NullPointerException();
            }
            return getValue(s);
        }

        public short getInteger(int i) {
            return getValue(new Integer(i));
        }

        public short getFloat(float f) {
            return getValue(new Float(f));
        }

        public short getClass(String name) {
            short utf8Index = getUtf8(name);
            return getIndirect(new IndirectEntry(
                CONSTANT_CLASS, utf8Index));
        }

        public short getString(String s) {
            short utf8Index = getUtf8(s);
            return getIndirect(new IndirectEntry(
                CONSTANT_STRING, utf8Index));
        }

        public short getFieldRef(String className, String name, String descriptor) {
            short classIndex = getClass(className);
            short nameAndTypeIndex = getNameAndType(name, descriptor);
            return getIndirect(new IndirectEntry(
                CONSTANT_FIELD, classIndex, nameAndTypeIndex));
        }

        public short getMethodRef(String className, String name, String descriptor) {
            short classIndex = getClass(className);
            short nameAndTypeIndex = getNameAndType(name, descriptor);
            return getIndirect(new IndirectEntry(CONSTANT_METHOD, classIndex, nameAndTypeIndex));
        }

        public short getInterfaceMethodRef(String className, String name, String descriptor) {
            short classIndex = getClass(className);
            short nameAndTypeIndex = getNameAndType(name, descriptor);
            return getIndirect(new IndirectEntry(
                CONSTANT_INTERFACEMETHOD, classIndex, nameAndTypeIndex));
        }

        public short getNameAndType(String name, String descriptor) {
            short nameIndex = getUtf8(name);
            short descriptorIndex = getUtf8(descriptor);
            return getIndirect(new IndirectEntry(
                CONSTANT_NAMEANDTYPE, nameIndex, descriptorIndex));
        }

        public void setReadOnly() {
            readOnly = true;
        }

        public void write(OutputStream out) throws IOException {
            DataOutputStream dataOut = new DataOutputStream(out);

            // constant_pool_count: number of entries plus one
            dataOut.writeShort(pool.size() + 1);

            for (Entry e : pool) {
                e.write(dataOut);
            }
        }

        private short addEntry(Entry entry) {
            pool.add(entry);
            /*
             * Note that this way of determining the index of the
             * added entry is wrong if this pool supports
             * CONSTANT_Long or CONSTANT_Double entries.
             */
            if (pool.size() >= 65535) {
                throw new IllegalArgumentException(
                    "constant pool size limit exceeded");
            }
            return (short) pool.size();
        }

        private short getValue(Object key) {
            Short index = map.get(key);
            if (index != null) {
                return index.shortValue();
            } else {
                if (readOnly) {
                    throw new InternalError(
                        "late constant pool addition: " + key);
                }
                short i = addEntry(new ValueEntry(key));
                map.put(key, new Short(i));
                return i;
            }
        }

        private short getIndirect(IndirectEntry e) {
            Short index = map.get(e);
            if (index != null) {
                return index.shortValue();
            } else {
                if (readOnly) {
                    throw new InternalError("late constant pool addition");
                }
                short i = addEntry(e);
                map.put(e, new Short(i));
                return i;
            }
        }

        private static abstract class Entry {
            public abstract void write(DataOutputStream out)
                throws IOException;
        }

        private static class ValueEntry extends Entry {
            private Object value;

            public ValueEntry(Object value) {
                this.value = value;
            }

            public void write(DataOutputStream out) throws IOException {
                if (value instanceof String) {
                    out.writeByte(CONSTANT_UTF8);
                    out.writeUTF((String) value);
                } else if (value instanceof Integer) {
                    out.writeByte(CONSTANT_INTEGER);
                    out.writeInt(((Integer) value).intValue());
                } else if (value instanceof Float) {
                    out.writeByte(CONSTANT_FLOAT);
                    out.writeFloat(((Float) value).floatValue());
                } else if (value instanceof Long) {
                    out.writeByte(CONSTANT_LONG);
                    out.writeLong(((Long) value).longValue());
                } else if (value instanceof Double) {
                    out.writeDouble(CONSTANT_DOUBLE);
                    out.writeDouble(((Double) value).doubleValue());
                } else {
                    throw new InternalError("bogus value entry: " + value);
                }
            }
        }

        private static class IndirectEntry extends Entry {
            private int tag;
            private short index0;
            private short index1;

            public IndirectEntry(int tag, short index) {
                this.tag = tag;
                this.index0 = index;
                this.index1 = 0;
            }

            public IndirectEntry(int tag, short index0, short index1) {
                this.tag = tag;
                this.index0 = index0;
                this.index1 = index1;
            }

            public void write(DataOutputStream out) throws IOException {
                out.writeByte(tag);
                out.writeShort(index0);
                /*
                 * If this entry type contains two indexes, write
                 * out the second, too.
                 */
                if (tag == CONSTANT_FIELD ||
                    tag == CONSTANT_METHOD ||
                    tag == CONSTANT_INTERFACEMETHOD ||
                    tag == CONSTANT_NAMEANDTYPE)
                {
                    out.writeShort(index1);
                }
            }

            public int hashCode() {
                return tag + index0 + index1;
            }

            public boolean equals(Object obj) {
                if (obj instanceof IndirectEntry) {
                    IndirectEntry other = (IndirectEntry) obj;
                    if (tag == other.tag &&
                        index0 == other.index0 && index1 == other.index1)
                    {
                        return true;
                    }
                }
                return false;
            }
        }
    }
}
