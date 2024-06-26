package com.jazz.engine.data;

import java.util.Map;

/**
 * Bitboards computed ahead of time.
 *
 * As chess has a rather simple move-set, much of the information we need to explore the game tree can be computed ahead of time using slower methods (the com.jazz.old package).
 * Then at run-time we can reference this in a much faster method.
 *
 * Adds a small initial memory overheard.
 */
public class PrecomputedBitboards {
    public static long[][] precomputedPawnAttacks = new long[2][64];
    // Knights are colour invariant. Only one map required.
    public static long[] precomputedKnightAttacks = new long[64];

    // Same for kings.
    public static long[] precomputedKingAttacks = new long[64];

    // Sliding pieces are more complex, requiring individual maps for every possible square.
    // Despite how inefficient this sounds, the engine strength is dominated by temporal complexity, not spatial.
    // Anything we can do now BEFORE searching pays off dramatically at higher depths.
    public static Map<Long, Long>[] precomputedBishopAttacks = new Map[64];
    public static Map<Long, Long>[] precomputedRookAttacks = new Map[64];

    // An array of all the sliding piece "rays", e.g.:
    // x..
    // .x.
    // ..B
    // a bishop's diagonal.
    public static Long[] precomputedRookRayMasks = new Long[64];
    public static Long[] precomputedBishopRayMasks = new Long[64];
}
