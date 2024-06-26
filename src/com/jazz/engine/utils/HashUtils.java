package com.jazz.engine.utils;

import com.jazz.engine.utils.bits.OldPieces;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Zobrist hashing implementation.
 * The transposition table maps boards -> evaluations (and other information).
 * Zobrist hashing is a means of using randomly initialized bitboards, one for each combination of board position and piece type.
 * The bitstrings are then all XOR'd together to uniquely specify a board state.
 *
 * Furthermore, only a single XOR operation is necessary to make a change to the key we use to access the transposition table,
 * as opposed to a basic approach that would require recalculating the entire hash.
 */

public class HashUtils {
    public static long[][][] zobristTable = new long[64][2][6];
    public static long zobristBTMString;

    public static void init(){
        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 6; k++) {
                    zobristTable[i][j][k] = ThreadLocalRandom.current().nextLong();
                }
            }
        }
        zobristBTMString = ThreadLocalRandom.current().nextLong();
    }

    public static long hash(byte[][] board, int turn){
        long hash = 0;
        if(turn == 1){
            hash ^= zobristBTMString;
        }

        int k = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                byte piece = board[i][j];
                int col = piece%2;
                int col_less = piece/2;

                if(col_less != 0){
                    int ordinal = OldPieces.indexOf(piece);
                    hash ^= zobristTable[k][col][col_less];
                }
                k++;
            }
        }
        return hash;
    }

    public static long hash(long[][] pieceBBs, int turn){
        long hash = 0;
        if(turn == 1){
            hash ^= zobristBTMString;
        }

        for (int j = 0; j < 64; j++) {
            for (int i = 0; i < 2; i++) {
                for (int k = 0; k < 6; k++) {
                    if((pieceBBs[i][k] & (1L << j)) != 0) {
                        hash ^= zobristTable[j][i][k];
                    }
                }
            }
        }

        return hash;
    }
}
