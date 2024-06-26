package com.jazz.engine.utils.bits;

/**
 * Conversion from a bitboard with single set bits to the index of that bit, and vice versa.
 */
public class BBUtils {
    public static int sqrToOrdinal(long from){
        return Long.numberOfTrailingZeros(from);
    }
    public static long ordinalToSqr(int intSqr){
        return 1L << intSqr;
    }

    public static byte toOldPrintableFormat(boolean isWhite, byte newType){
        if(newType==0) return 0;

        byte initial = (byte) (newType+1);
        if(!isWhite){
            initial |= 1<<3; // add an extra 1 at the start
        }
        return initial;
    }
}
