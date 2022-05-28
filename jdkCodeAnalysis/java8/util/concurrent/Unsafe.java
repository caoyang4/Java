
package sun.misc;

/**
 * Java中的Unsafe类为我们提供了管理内存的能力
 * Unsafe类是final的，不允许继承，而且构造函数是私有的，无法通过new的方式创建对象，但可以通过反射创建
 * 主要功能：
 *  内存管理：普通读写、volatile读写、有序写入、直接操作内存等分配内存与释放内存的功能
 *  非常规对象实例化：Unsafe类提供的 allocateInstance 方法，可以直接生成对象实例，且无需调用构造方法和其他初始化方法
 *  类加载：
 *  偏移量相关: 通过对象指针进行偏移，不仅可以直接修改指针指向的数据（即使是私有的），甚至可以找到JVM已经认定为垃圾、可以进行回收的对象
 *  数组操作: Java的数组最大值为Integer.MAX_VALUE，使用Unsafe类的内存分配方法可以实现超大数组
 *  线程调度: 整个并发框架中对线程的挂起操作被封装在LockSupport类中，LockSupport类中有各种版本pack方法，但最终都调用了Unsafe.park()方法
 *  CAS操作: 为Java的锁机制提供了一种新的解决办法，比如AtomicInteger等类都是通过该方法来实现的。compareAndSwap方法是原子的，可以避免繁重的锁机制，提高代码效率
 *  内存屏障: JDK8新引入了用于定义内存屏障、避免代码重排的方法
 *
 *
 */

import java.security.*;
import java.lang.reflect.*;

import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;


public final class Unsafe {

    private static native void registerNatives();
    static {
        registerNatives();
        sun.reflect.Reflection.registerMethodsToFilter(Unsafe.class, "getUnsafe");
    }

    private Unsafe() {}

    /**
     * 饿汉式单例模式
     */
    private static final Unsafe theUnsafe = new Unsafe();

    @CallerSensitive
    public static Unsafe getUnsafe() {
        Class<?> caller = Reflection.getCallerClass();
        if (!VM.isSystemDomainLoader(caller.getClassLoader()))
            throw new SecurityException("Unsafe");
        return theUnsafe;
    }

    /**
     * 普通读写，无法保证可见性和有序性
     * 可以通过Unsafe去读写一个类的属性，不管这个类是否私有（直接从内存读写）
     */

    // 从对象的指定偏移地址处读取一个int类型的数据，需要指定要读取的对象和偏移量
    public native int getInt(Object o, long offset);
    //在对象的指定地址出写入一个int类型的数据，需要执行要读取的对象、偏移量和新写入的值。其他类型的数据也有相同的方法，如Object和Boolean
    public native void putInt(Object o, long offset, int x);
    public native Object getObject(Object o, long offset);
    public native void putObject(Object o, long offset, Object x);
    public native boolean getBoolean(Object o, long offset);
    public native void    putBoolean(Object o, long offset, boolean x);
    public native byte    getByte(Object o, long offset);
    public native void    putByte(Object o, long offset, byte x);
    public native short   getShort(Object o, long offset);
    public native void    putShort(Object o, long offset, short x);
    public native char    getChar(Object o, long offset);
    public native void    putChar(Object o, long offset, char x);
    public native long    getLong(Object o, long offset);
    public native void    putLong(Object o, long offset, long x);
    public native float   getFloat(Object o, long offset);
    public native void    putFloat(Object o, long offset, float x);
    public native double  getDouble(Object o, long offset);
    public native void    putDouble(Object o, long offset, double x);

    @Deprecated
    public int getInt(Object o, int offset) {return getInt(o, (long)offset);}
    @Deprecated
    public void putInt(Object o, int offset, int x) {putInt(o, (long)offset, x);}
    @Deprecated
    public Object getObject(Object o, int offset) {return getObject(o, (long)offset);}
    @Deprecated
    public void putObject(Object o, int offset, Object x) {putObject(o, (long)offset, x);}
    @Deprecated
    public boolean getBoolean(Object o, int offset) {return getBoolean(o, (long)offset);}
    @Deprecated
    public void putBoolean(Object o, int offset, boolean x) {putBoolean(o, (long)offset, x);}
    @Deprecated
    public byte getByte(Object o, int offset) {return getByte(o, (long)offset);}
    @Deprecated
    public void putByte(Object o, int offset, byte x) {putByte(o, (long)offset, x);}
    @Deprecated
    public short getShort(Object o, int offset) {return getShort(o, (long)offset);}
    @Deprecated
    public void putShort(Object o, int offset, short x) {putShort(o, (long)offset, x);}
    @Deprecated
    public char getChar(Object o, int offset) {return getChar(o, (long)offset);}
    @Deprecated
    public void putChar(Object o, int offset, char x) {putChar(o, (long)offset, x);}
    @Deprecated
    public long getLong(Object o, int offset) {return getLong(o, (long)offset);}
    @Deprecated
    public void putLong(Object o, int offset, long x) {putLong(o, (long)offset, x);}
    @Deprecated
    public float getFloat(Object o, int offset) {return getFloat(o, (long)offset);}
    @Deprecated
    public void putFloat(Object o, int offset, float x) {putFloat(o, (long)offset, x);}
    @Deprecated
    public double getDouble(Object o, int offset) {return getDouble(o, (long)offset);}
    @Deprecated
    public void putDouble(Object o, int offset, double x) {putDouble(o, (long)offset, x);}

    // These work on values in the C heap.
    public native byte    getByte(long address);
    public native void    putByte(long address, byte x);
    public native short   getShort(long address);
    public native void    putShort(long address, short x);
    public native char    getChar(long address);
    public native void    putChar(long address, char x);
    public native int     getInt(long address);
    public native void    putInt(long address, int x);
    public native long    getLong(long address);
    public native void    putLong(long address, long x);
    public native float   getFloat(long address);
    public native void    putFloat(long address, float x);
    public native double  getDouble(long address);
    public native void    putDouble(long address, double x);
    public native long getAddress(long address);
    public native void putAddress(long address, long x);



    /// wrappers for malloc, realloc, free:

    /**
     * 直接内存操作
     */
    // 分配内存
    public native long allocateMemory(long bytes);
    // 重新设置内存
    public native long reallocateMemory(long address, long bytes);
    // 设置内存
    public native void setMemory(Object o, long offset, long bytes, byte value);
    public void setMemory(long address, long bytes, byte value) {
        setMemory(null, address, bytes, value);
    }
    // 复制
    public native void copyMemory(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes);
    public void copyMemory(long srcAddress, long destAddress, long bytes) {
        copyMemory(null, srcAddress, null, destAddress, bytes);
    }
    // 释放内存
    public native void freeMemory(long address);

    /// random queries

    public static final int INVALID_FIELD_OFFSET   = -1;

    @Deprecated
    public int fieldOffset(Field f) {
        if (Modifier.isStatic(f.getModifiers()))
            return (int) staticFieldOffset(f);
        else
            return (int) objectFieldOffset(f);
    }

    @Deprecated
    public Object staticFieldBase(Class<?> c) {
        Field[] fields = c.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (Modifier.isStatic(fields[i].getModifiers())) {
                return staticFieldBase(fields[i]);
            }
        }
        return null;
    }

    /**
     * 获取偏移量
     * 可以通过偏移量和对象得到对象中属性在内存中所处的位置
     */
    // 获取静态属性Field在对象中的偏移量
    public native long staticFieldOffset(Field f);
    // 获取非静态属性在对象中的偏移量
    public native long objectFieldOffset(Field f);
    // 返回Field所在对象
    public native Object staticFieldBase(Field f);
    // 返回数组中第一个元素的偏移量
    public native int arrayBaseOffset(Class<?> arrayClass);
    // 返回数组中第一个元素所占用的内存空间
    public native int arrayIndexScale(Class<?> arrayClass);

    public static final int ARRAY_BOOLEAN_BASE_OFFSET = theUnsafe.arrayBaseOffset(boolean[].class);
    public static final int ARRAY_BYTE_BASE_OFFSET = theUnsafe.arrayBaseOffset(byte[].class);
    public static final int ARRAY_SHORT_BASE_OFFSET = theUnsafe.arrayBaseOffset(short[].class);
    public static final int ARRAY_CHAR_BASE_OFFSET = theUnsafe.arrayBaseOffset(char[].class);
    public static final int ARRAY_INT_BASE_OFFSET = theUnsafe.arrayBaseOffset(int[].class);
    public static final int ARRAY_LONG_BASE_OFFSET = theUnsafe.arrayBaseOffset(long[].class);
    public static final int ARRAY_FLOAT_BASE_OFFSET = theUnsafe.arrayBaseOffset(float[].class);
    public static final int ARRAY_DOUBLE_BASE_OFFSET = theUnsafe.arrayBaseOffset(double[].class);
    public static final int ARRAY_OBJECT_BASE_OFFSET = theUnsafe.arrayBaseOffset(Object[].class);
    public static final int ARRAY_BOOLEAN_INDEX_SCALE = theUnsafe.arrayIndexScale(boolean[].class);
    public static final int ARRAY_BYTE_INDEX_SCALE = theUnsafe.arrayIndexScale(byte[].class);
    public static final int ARRAY_SHORT_INDEX_SCALE = theUnsafe.arrayIndexScale(short[].class);
    public static final int ARRAY_CHAR_INDEX_SCALE = theUnsafe.arrayIndexScale(char[].class);
    public static final int ARRAY_INT_INDEX_SCALE = theUnsafe.arrayIndexScale(int[].class);
    public static final int ARRAY_LONG_INDEX_SCALE = theUnsafe.arrayIndexScale(long[].class);
    public static final int ARRAY_FLOAT_INDEX_SCALE = theUnsafe.arrayIndexScale(float[].class);
    public static final int ARRAY_DOUBLE_INDEX_SCALE = theUnsafe.arrayIndexScale(double[].class);
    public static final int ARRAY_OBJECT_INDEX_SCALE = theUnsafe.arrayIndexScale(Object[].class);




    public native int addressSize();

    public static final int ADDRESS_SIZE = theUnsafe.addressSize();

    public native int pageSize();


    /// random trusted operations from JNI:

    /**
     * 类加载
     */
    // 判断是否需要初始化一个类
    public native boolean shouldBeInitialized(Class<?> c);
    // 保证一个类已经被初始化
    public native void ensureClassInitialized(Class<?> c);
    // 定义一个类，用于动态创建Class对象
    public native Class<?> defineClass(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protectionDomain);
    // 用于动态创建一个匿名内部类
    public native Class<?> defineAnonymousClass(Class<?> hostClass, byte[] data, Object[] cpPatches);
    // 用于创建一个类的实例，但是不会调用这个实例的构造方法，如果这个类还未被初始化，则初始化这个类
    public native Object allocateInstance(Class<?> cls) throws InstantiationException;

    @Deprecated
    public native void monitorEnter(Object o);

    @Deprecated
    public native void monitorExit(Object o);

    @Deprecated
    public native boolean tryMonitorEnter(Object o);

    public native void throwException(Throwable ee);


    /**
     * CAS操作
     * Unsafe中提供了三种类型Object、int、long的CAS操作
     */
    public final native boolean compareAndSwapObject(Object o, long offset, Object expected, Object x);
    public final native boolean compareAndSwapInt(Object o, long offset, int expected, int x);
    public final native boolean compareAndSwapLong(Object o, long offset, long expected, long x);


    /**
     * Volatile读写
     * 可保证有序性和可见性
     */
    public native Object getObjectVolatile(Object o, long offset);
    public native void    putObjectVolatile(Object o, long offset, Object x);
    public native int     getIntVolatile(Object o, long offset);
    public native void    putIntVolatile(Object o, long offset, int x);
    public native boolean getBooleanVolatile(Object o, long offset);
    public native void    putBooleanVolatile(Object o, long offset, boolean x);
    public native byte    getByteVolatile(Object o, long offset);
    public native void    putByteVolatile(Object o, long offset, byte x);
    public native short   getShortVolatile(Object o, long offset);
    public native void    putShortVolatile(Object o, long offset, short x);
    public native char    getCharVolatile(Object o, long offset);
    public native void    putCharVolatile(Object o, long offset, char x);
    public native long    getLongVolatile(Object o, long offset);
    public native void    putLongVolatile(Object o, long offset, long x);
    public native float   getFloatVolatile(Object o, long offset);
    public native void    putFloatVolatile(Object o, long offset, float x);
    public native double  getDoubleVolatile(Object o, long offset);
    public native void    putDoubleVolatile(Object o, long offset, double x);

    /**
     * 有序写入
     * 有序写入只保证写入的有序性，并不保证可见性，也就是说，该线程的写入不保证其他线程能立马看到
     */
    public native void    putOrderedObject(Object o, long offset, Object x);
    public native void    putOrderedInt(Object o, long offset, int x);
    public native void    putOrderedLong(Object o, long offset, long x);


    /**
     * 线程调度
     * LockSupport中的park()和unpark()方法就是依靠Unsafe中的这两个方法实现的
     * 线程唤醒和阻塞
     */
    public native void unpark(Object thread);
    public native void park(boolean isAbsolute, long time);



    public native int getLoadAverage(double[] loadavg, int nelems);

    // The following contain CAS-based Java implementations used on
    // platforms not supporting native instructions

    public final int getAndAddInt(Object o, long offset, int delta) {
        int v;
        do {
            v = getIntVolatile(o, offset);
        } while (!compareAndSwapInt(o, offset, v, v + delta));
        return v;
    }

    public final long getAndAddLong(Object o, long offset, long delta) {
        long v;
        do {
            v = getLongVolatile(o, offset);
        } while (!compareAndSwapLong(o, offset, v, v + delta));
        return v;
    }

    public final int getAndSetInt(Object o, long offset, int newValue) {
        int v;
        do {
            v = getIntVolatile(o, offset);
        } while (!compareAndSwapInt(o, offset, v, newValue));
        return v;
    }

    public final long getAndSetLong(Object o, long offset, long newValue) {
        long v;
        do {
            v = getLongVolatile(o, offset);
        } while (!compareAndSwapLong(o, offset, v, newValue));
        return v;
    }

    public final Object getAndSetObject(Object o, long offset, Object newValue) {
        Object v;
        do {
            v = getObjectVolatile(o, offset);
        } while (!compareAndSwapObject(o, offset, v, newValue));
        return v;
    }


    /**
     * 内存屏障
     */
    // 保证这个屏障之前所有读操作已经完成
    public native void loadFence();
    // 保证这个屏障之前所有写操作已经完成
    public native void storeFence();
    // 保证这个屏障之前所有读写操作都完成
    public native void fullFence();

    private static void throwIllegalAccessError() {
       throw new IllegalAccessError();
    }

}
