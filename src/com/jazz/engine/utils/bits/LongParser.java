package com.jazz.engine.utils.bits;

/**
 * Converts a long into an 8x8 grid of 0s and 1s.
 * Ideal for debugging bitboard functions.
 */
public class LongParser {
    /**
     * Automatically reverses columns.
     */
    public static long parse(String input){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int begin = i*8;
            int end = begin+8;

            String row = input.substring(begin, end);
            sb.append(new StringBuilder(row).reverse());
        }

        return Long.parseUnsignedLong(sb.toString(), 2);
    }

    public static String printable(long input){
        StringBuilder sb = new StringBuilder();
        String padded = String.format("%64s", Long.toBinaryString(input)).replace(" ", "0");
        for (int row = 0; row < 8; row++) {
            StringBuilder subSB = new StringBuilder();
            int begin = row*8;
            subSB.append(padded, begin, begin+8);
            sb.append(subSB.reverse());
            sb.append("\n");
        }
        return sb.toString();
    }
}
