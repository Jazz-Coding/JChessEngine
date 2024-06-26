package com.jazz.old;

/**
 * Static values for checkmates.
 */
public class Checkmate {
    public static final int MATE_COST = -16384;
    public static final int TFR_COST = -1;

    public static float in(int moves, int color){
        return MATE_COST + moves;
    }
}
