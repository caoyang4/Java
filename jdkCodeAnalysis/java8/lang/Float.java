package java.lang;

import sun.misc.FloatingDecimal;
import sun.misc.FloatConsts;
import sun.misc.DoubleConsts;

public final class Float extends Number implements Comparable<Float> {
    public static final float POSITIVE_INFINITY = 1.0f / 0.0f;

    public static final float NEGATIVE_INFINITY = -1.0f / 0.0f;

    public static final float NaN = 0.0f / 0.0f;

    public static final float MAX_VALUE = 0x1.fffffeP+127f; // 3.4028235e+38f

    public static final float MIN_NORMAL = 0x1.0p-126f; // 1.17549435E-38f

    public static final float MIN_VALUE = 0x0.000002P-126f; // 1.4e-45f

    public static final int MAX_EXPONENT = 127;

    public static final int MIN_EXPONENT = -126;

    public static final int SIZE = 32;

    public static final int BYTES = SIZE / Byte.SIZE;

    @SuppressWarnings("unchecked")
    public static final Class<Float> TYPE = (Class<Float>) Class.getPrimitiveClass("float");

    public static String toString(float f) {
        return FloatingDecimal.toJavaFormatString(f);
    }

    public static String toHexString(float f) {
        if (Math.abs(f) < FloatConsts.MIN_NORMAL
            &&  f != 0.0f ) {// float subnormal
            // Adjust exponent to create subnormal double, then
            // replace subnormal double exponent with subnormal float
            // exponent
            String s = Double.toHexString(Math.scalb((double)f,
                                                     /* -1022+126 */
                                                     DoubleConsts.MIN_EXPONENT-
                                                     FloatConsts.MIN_EXPONENT));
            return s.replaceFirst("p-1022$", "p-126");
        }
        else // double string will be the same as float string
            return Double.toHexString(f);
    }

    public static Float valueOf(String s) throws NumberFormatException {
        return new Float(parseFloat(s));
    }

    public static Float valueOf(float f) {
        return new Float(f);
    }

    public static float parseFloat(String s) throws NumberFormatException {
        return FloatingDecimal.parseFloat(s);
    }

    public static boolean isNaN(float v) {
        return (v != v);
    }

    public static boolean isInfinite(float v) {
        return (v == POSITIVE_INFINITY) || (v == NEGATIVE_INFINITY);
    }


     public static boolean isFinite(float f) {
        return Math.abs(f) <= FloatConsts.MAX_VALUE;
    }

    private final float value;

    public Float(float value) {
        this.value = value;
    }

    public Float(double value) {
        this.value = (float)value;
    }

    public Float(String s) throws NumberFormatException {
        value = parseFloat(s);
    }

    public boolean isNaN() {
        return isNaN(value);
    }

    public boolean isInfinite() {
        return isInfinite(value);
    }

    public String toString() {
        return Float.toString(value);
    }

    public byte byteValue() {
        return (byte)value;
    }

    public short shortValue() {
        return (short)value;
    }

    public int intValue() {
        return (int)value;
    }

    public long longValue() {
        return (long)value;
    }

    public float floatValue() {
        return value;
    }

    public double doubleValue() {
        return (double)value;
    }

    @Override
    public int hashCode() {
        return Float.hashCode(value);
    }

    public static int hashCode(float value) {
        return floatToIntBits(value);
    }

    public boolean equals(Object obj) {
        return (obj instanceof Float)
               && (floatToIntBits(((Float)obj).value) == floatToIntBits(value));
    }

    public static int floatToIntBits(float value) {
        int result = floatToRawIntBits(value);
        // Check for NaN based on values of bit fields, maximum
        // exponent and nonzero significand.
        if ( ((result & FloatConsts.EXP_BIT_MASK) ==
              FloatConsts.EXP_BIT_MASK) &&
             (result & FloatConsts.SIGNIF_BIT_MASK) != 0)
            result = 0x7fc00000;
        return result;
    }

    public static native int floatToRawIntBits(float value);

    public static native float intBitsToFloat(int bits);

    public int compareTo(Float anotherFloat) {
        return Float.compare(value, anotherFloat.value);
    }

    public static int compare(float f1, float f2) {
        if (f1 < f2)
            return -1;           // Neither val is NaN, thisVal is smaller
        if (f1 > f2)
            return 1;            // Neither val is NaN, thisVal is larger

        // Cannot use floatToRawIntBits because of possibility of NaNs.
        int thisBits    = Float.floatToIntBits(f1);
        int anotherBits = Float.floatToIntBits(f2);

        return (thisBits == anotherBits ?  0 : // Values are equal
                (thisBits < anotherBits ? -1 : // (-0.0, 0.0) or (!NaN, NaN)
                 1));                          // (0.0, -0.0) or (NaN, !NaN)
    }

    public static float sum(float a, float b) {
        return a + b;
    }

    public static float max(float a, float b) {
        return Math.max(a, b);
    }

    public static float min(float a, float b) {
        return Math.min(a, b);
    }

    private static final long serialVersionUID = -2671257302660747028L;
}
