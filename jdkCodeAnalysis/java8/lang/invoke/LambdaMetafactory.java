package java.lang.invoke;

import java.io.Serializable;
import java.util.Arrays;

public class LambdaMetafactory {

    public static final int FLAG_SERIALIZABLE = 1 << 0;

    public static final int FLAG_MARKERS = 1 << 1;

    public static final int FLAG_BRIDGES = 1 << 2;

    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
    private static final MethodType[] EMPTY_MT_ARRAY = new MethodType[0];

    public static CallSite metafactory(MethodHandles.Lookup caller,
                                       String invokedName,
                                       MethodType invokedType,
                                       MethodType samMethodType,
                                       MethodHandle implMethod,
                                       MethodType instantiatedMethodType)
            throws LambdaConversionException {
        AbstractValidatingLambdaMetafactory mf;
        mf = new InnerClassLambdaMetafactory(caller, invokedType,
                                             invokedName, samMethodType,
                                             implMethod, instantiatedMethodType,
                                             false, EMPTY_CLASS_ARRAY, EMPTY_MT_ARRAY);
        mf.validateMetafactoryArgs();
        return mf.buildCallSite();
    }

    public static CallSite altMetafactory(MethodHandles.Lookup caller,
                                          String invokedName,
                                          MethodType invokedType,
                                          Object... args)
            throws LambdaConversionException {
        MethodType samMethodType = (MethodType)args[0];
        MethodHandle implMethod = (MethodHandle)args[1];
        MethodType instantiatedMethodType = (MethodType)args[2];
        int flags = (Integer) args[3];
        Class<?>[] markerInterfaces;
        MethodType[] bridges;
        int argIndex = 4;
        if ((flags & FLAG_MARKERS) != 0) {
            int markerCount = (Integer) args[argIndex++];
            markerInterfaces = new Class<?>[markerCount];
            System.arraycopy(args, argIndex, markerInterfaces, 0, markerCount);
            argIndex += markerCount;
        }
        else
            markerInterfaces = EMPTY_CLASS_ARRAY;
        if ((flags & FLAG_BRIDGES) != 0) {
            int bridgeCount = (Integer) args[argIndex++];
            bridges = new MethodType[bridgeCount];
            System.arraycopy(args, argIndex, bridges, 0, bridgeCount);
            argIndex += bridgeCount;
        }
        else
            bridges = EMPTY_MT_ARRAY;

        boolean isSerializable = ((flags & FLAG_SERIALIZABLE) != 0);
        if (isSerializable) {
            boolean foundSerializableSupertype = Serializable.class.isAssignableFrom(invokedType.returnType());
            for (Class<?> c : markerInterfaces)
                foundSerializableSupertype |= Serializable.class.isAssignableFrom(c);
            if (!foundSerializableSupertype) {
                markerInterfaces = Arrays.copyOf(markerInterfaces, markerInterfaces.length + 1);
                markerInterfaces[markerInterfaces.length-1] = Serializable.class;
            }
        }

        AbstractValidatingLambdaMetafactory mf
                = new InnerClassLambdaMetafactory(caller, invokedType,
                                                  invokedName, samMethodType,
                                                  implMethod,
                                                  instantiatedMethodType,
                                                  isSerializable,
                                                  markerInterfaces, bridges);
        mf.validateMetafactoryArgs();
        return mf.buildCallSite();
    }
}
