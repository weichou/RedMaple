package redmaple.util;

/**
 * Created with IntelliJ IDEA.
 * User: wolf
 * Date: 22.3.2013
 * Time: 21:14
 * To change this template use File | Settings | File Templates.
 */
public class PrimitiveCompressor {
    public static short compressNormalizedFloatToShort(float f) {
        return (short) (f * (float) Short.MAX_VALUE);
    }

    public static float compressShortToNormalizedFloat(short s) {
        return ((float) s) / Short.MAX_VALUE;
    }

    public static byte compressNormalizedFloatToByte(float f) {
        return (byte) (f * (float) Byte.MAX_VALUE);
    }

    public static float compressByteToNormalizedFloat(byte s) {
        return ((float) s) / Byte.MAX_VALUE;
    }

    public static void main(String[] args) {
        float f5 = 0.12345f;
        float f4 = 0.1234f;
        float f3 = 0.123f;
        float f2 = 0.12f;
        float f1 = 0.1f;

        short s5 = compressNormalizedFloatToShort(f5);

        System.out.println(f5 + " --> " + s5);
        System.out.println(" --> " + compressShortToNormalizedFloat(s5));

        byte b5 = compressNormalizedFloatToByte(f5);

        System.out.println(f5 + " --> " + b5);
        System.out.println(" --> " + compressByteToNormalizedFloat(b5));


    }
}
