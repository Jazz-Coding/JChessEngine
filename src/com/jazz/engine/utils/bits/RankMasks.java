package com.jazz.engine.utils.bits;

/**
 * Hard coded bitmasks.
 */
public class RankMasks {
    public static long rank4Mask = Long.parseUnsignedLong(
            "00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000" +
                    "11111111" +
                    "00000000" +
                    "00000000" +
                    "00000000",2);

    public static long rank5Mask = Long.parseUnsignedLong(
            "00000000" +
                    "00000000" +
                    "00000000" +
                    "11111111" +
                    "00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000",2);
    public static long rank6Mask = Long.parseUnsignedLong(
            "00000000" +
                    "00000000" +
                    "11111111" +
                    "00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000",2);
    public static long rank3Mask = Long.parseUnsignedLong(
            "00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000" +
                    "11111111" +
                    "00000000" +
                    "00000000",2);

    public static long rank8Mask = Long.parseUnsignedLong(
            "11111111" +
                    "00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000",2);
    public static long rank1Mask = Long.parseUnsignedLong(
            "00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000" +
                    "00000000" +
                    "11111111",2);
}
