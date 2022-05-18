package java.lang;
import java.util.Random;
import sun.misc.DoubleConsts;

/** Math精度版本工具类
 * strictfp关键字修饰
 */
public final class StrictMath {

    private StrictMath() {}

    public static final double E = 2.7182818284590452354;

    public static final double PI = 3.14159265358979323846;

    public static native double sin(double a);

    public static native double cos(double a);

    public static native double tan(double a);

    public static native double asin(double a);

    public static native double acos(double a);

    public static native double atan(double a);

    public static strictfp double toRadians(double angdeg) {
        // Do not delegate to Math.toRadians(angdeg) because
        // this method has the strictfp modifier.
        return angdeg / 180.0 * PI;
    }

    public static strictfp double toDegrees(double angrad) {
        // Do not delegate to Math.toDegrees(angrad) because
        // this method has the strictfp modifier.
        return angrad * 180.0 / PI;
    }

    public static native double exp(double a);

    public static native double log(double a);

    public static native double log10(double a);

    public static native double sqrt(double a);

    public static native double cbrt(double a);

    public static native double IEEEremainder(double f1, double f2);

    public static double ceil(double a) {
        return floorOrCeil(a, -0.0, 1.0, 1.0);
    }

    public static double floor(double a) {
        return floorOrCeil(a, -1.0, 0.0, -1.0);
    }

    private static double floorOrCeil(double a, double negativeBoundary, double positiveBoundary, double sign) {
        int exponent = Math.getExponent(a);

        if (exponent < 0) {
            /*
             * Absolute value of argument is less than 1.
             * floorOrceil(-0.0) => -0.0
             * floorOrceil(+0.0) => +0.0
             */
            return ((a == 0.0) ? a :
                    ( (a < 0.0) ?  negativeBoundary : positiveBoundary) );
        } else if (exponent >= 52) {
            /*
             * Infinity, NaN, or a value so large it must be integral.
             */
            return a;
        }
        // Else the argument is either an integral value already XOR it
        // has to be rounded to one.
        assert exponent >= 0 && exponent <= 51;

        long doppel = Double.doubleToRawLongBits(a);
        long mask   = DoubleConsts.SIGNIF_BIT_MASK >> exponent;

        if ( (mask & doppel) == 0L )
            return a; // integral value
        else {
            double result = Double.longBitsToDouble(doppel & (~mask));
            if (sign*a > 0.0)
                result = result + sign;
            return result;
        }
    }

    public static double rint(double a) {
        double twoToThe52 = (double)(1L << 52); // 2^52
        double sign = Math.copySign(1.0, a); // preserve sign info
        a = Math.abs(a);

        if (a < twoToThe52) { // E_min <= ilogb(a) <= 51
            a = ((twoToThe52 + a ) - twoToThe52);
        }

        return sign * a; // restore original sign
    }

    public static native double atan2(double y, double x);


    public static native double pow(double a, double b);

    public static int round(float a) {
        return Math.round(a);
    }

    public static long round(double a) {
        return Math.round(a);
    }

    private static final class RandomNumberGeneratorHolder {
        static final Random randomNumberGenerator = new Random();
    }

    public static double random() {
        return RandomNumberGeneratorHolder.randomNumberGenerator.nextDouble();
    }

    public static int addExact(int x, int y) {
        return Math.addExact(x, y);
    }

    public static long addExact(long x, long y) {
        return Math.addExact(x, y);
    }

    public static int subtractExact(int x, int y) {
        return Math.subtractExact(x, y);
    }

    public static long subtractExact(long x, long y) {
        return Math.subtractExact(x, y);
    }

    public static int multiplyExact(int x, int y) {
        return Math.multiplyExact(x, y);
    }

    public static long multiplyExact(long x, long y) {
        return Math.multiplyExact(x, y);
    }

    public static int toIntExact(long value) {
        return Math.toIntExact(value);
    }

    public static int floorDiv(int x, int y) {
        return Math.floorDiv(x, y);
    }

    public static long floorDiv(long x, long y) {
        return Math.floorDiv(x, y);
    }

    public static int floorMod(int x, int y) {
        return Math.floorMod(x , y);
    }

    public static long floorMod(long x, long y) {
        return Math.floorMod(x, y);
    }

    public static int abs(int a) {
        return Math.abs(a);
    }

    public static long abs(long a) {
        return Math.abs(a);
    }

    public static float abs(float a) {
        return Math.abs(a);
    }

    public static double abs(double a) {
        return Math.abs(a);
    }

    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    public static long max(long a, long b) {
        return Math.max(a, b);
    }

    public static float max(float a, float b) {
        return Math.max(a, b);
    }

    public static double max(double a, double b) {
        return Math.max(a, b);
    }

    public static int min(int a, int b) {
        return Math.min(a, b);
    }

    public static long min(long a, long b) {
        return Math.min(a, b);
    }

    public static float min(float a, float b) {
        return Math.min(a, b);
    }

    public static double min(double a, double b) {
        return Math.min(a, b);
    }

    public static double ulp(double d) {
        return Math.ulp(d);
    }

    public static float ulp(float f) {
        return Math.ulp(f);
    }

    public static double signum(double d) {
        return Math.signum(d);
    }

    public static float signum(float f) {
        return Math.signum(f);
    }

    public static native double sinh(double x);

    public static native double cosh(double x);

    public static native double tanh(double x);

    public static native double hypot(double x, double y);

    public static native double expm1(double x);

    public static native double log1p(double x);

    public static double copySign(double magnitude, double sign) {
        return Math.copySign(magnitude, (Double.isNaN(sign)?1.0d:sign));
    }

    public static float copySign(float magnitude, float sign) {
        return Math.copySign(magnitude, (Float.isNaN(sign)?1.0f:sign));
    }
    public static int getExponent(float f) {
        return Math.getExponent(f);
    }

    public static int getExponent(double d) {
        return Math.getExponent(d);
    }

    public static double nextAfter(double start, double direction) {
        return Math.nextAfter(start, direction);
    }

    public static float nextAfter(float start, double direction) {
        return Math.nextAfter(start, direction);
    }

    public static double nextUp(double d) {
        return Math.nextUp(d);
    }

    public static float nextUp(float f) {
        return Math.nextUp(f);
    }

    public static double nextDown(double d) {
        return Math.nextDown(d);
    }

    public static float nextDown(float f) {
        return Math.nextDown(f);
    }

    public static double scalb(double d, int scaleFactor) {
        return Math.scalb(d, scaleFactor);
    }

    public static float scalb(float f, int scaleFactor) {
        return Math.scalb(f, scaleFactor);
    }
}
