package com.jazz.engine.data.tt;

import static com.jazz.engine.data.tt.TTEntryType.*;

/**
 * 64 bit (long) datastructure
 *  00000000    00000000 00000000 00000000 00000000   00000000  00000000 00000000
 * |________|  |___________________________________| |_______| |________________|
 *   flags                bestMove (int)               depth    eval (short/100)
 *
 *       flags breakdown:
 *        000000  00 = ,
 *       |______| 01 = LOWER BOUND
 *       ignored  10 = UPPER BOUND
 *                11 = EXACT
 */
public class TTEntry {
    private static final int FLAGS_SHIFT = 56; // to get 8 bits to span positions 56->64
    private static final int MOVE_SHIFT = 24; // to get 32 bits to span positions 24->56
    private static final int DEPTH_SHIFT = 16; // to get 8 bits to span positions 16->24
    private static final int EVAL_SHIFT = 0; // to get 16 bits to span positions 0->16

    private static final long FLAGS_MASK =  0b11111111L << FLAGS_SHIFT;
    private static final long MOVE_MASK  =  0b11111111_11111111_11111111_11111111L << MOVE_SHIFT;
    private static final long DEPTH_MASK =  0b11111111L << DEPTH_SHIFT;
    private static final long EVAL_MASK  =  0b11111111_11111111L << EVAL_SHIFT;
    private static final long EVAL_SIGN_BIT_MASK = 0b10000000_00000000L << EVAL_SHIFT;

    public static long build(int depthEvaluated,
                             float value,
                             float alpha,
                             float beta){
        byte type = 0b00;
        if(value >= alpha && value <= beta){
            type = 0b11; // EXACT
        } else if(value >= beta){
            // cutoff occurred here - LOWER BOUND
            // our opponent has something better and will play that instead
            // the maximizer can use this as their worst assured score
            type = 0b01;
        } else {
            // value < alpha - UPPER BOUND
            // we have something better elsewhere and will play that instead
            // that said, the minimizer can use this as their worst assured score (beta)
            type = 0b10;
        }

        short shortValue = (short) (Math.max(Math.min(value,Short.MAX_VALUE),Short.MIN_VALUE)*100);

        return  Byte.toUnsignedLong(type) << FLAGS_SHIFT
                | Integer.toUnsignedLong(depthEvaluated) << DEPTH_SHIFT
                | Short.toUnsignedLong(shortValue) << EVAL_SHIFT;
    }

    public static long build(int depthEvaluated, // build with best move included
                             float value,
                             float alpha,
                             float beta,
                             int bestMove){
        long curr = build(depthEvaluated, value, alpha, beta);
        long bm = Integer.toUnsignedLong(bestMove) << MOVE_SHIFT;
        return curr | bm;
    }

    public static long build(TTEntryType type,
                             int depthEvaluated,
                             float value){
        byte btype = 0b00;
        switch (type){
            case EXACT -> {
                btype = 0b11;
            }
            case LOWER_BOUND -> {
                btype=0b01;
            }
            case UPPER_BOUND -> {
                btype=0b10;
            }
        }

        short shortValue = (short) (Math.max(Math.min(value,Short.MAX_VALUE),Short.MIN_VALUE)*100);

        return  Byte.toUnsignedLong(btype) << FLAGS_SHIFT
                | Integer.toUnsignedLong(depthEvaluated) << DEPTH_SHIFT
                | Short.toUnsignedLong(shortValue) << EVAL_SHIFT;
    }

    private static final TTEntryType[] types = new TTEntryType[]{LOWER_BOUND,UPPER_BOUND,EXACT};

    public static TTEntryType getType(long entry){
        long flags = (entry & FLAGS_MASK) >>> FLAGS_SHIFT;
        return types[(int) flags-1];
    }

    public static int getHashMove(long entry){
        return (int) ((entry&MOVE_MASK) >>> MOVE_SHIFT);
    }

    public static float getValue(long entry){
        long eval = (entry & EVAL_MASK);
        short evalShort = (short) (eval >>> EVAL_SHIFT);
        if((eval & EVAL_SIGN_BIT_MASK) != 0) {// i.e. the sign bit is set
            return -evalShort / 100F;
        } else {
            return  evalShort/100F;
        }
    }

    public static int getDepthEvaluated(long entry){
        return (int) ((entry&DEPTH_MASK) >>> DEPTH_SHIFT);
    }
}
