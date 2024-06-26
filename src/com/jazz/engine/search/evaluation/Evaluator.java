package com.jazz.engine.search.evaluation;

import com.jazz.engine.data.Pieces;
import com.jazz.engine.gui.Statistics;
import com.jazz.engine.utils.bits.LongParser;

import static com.jazz.engine.data.Pieces.king;
import static com.jazz.engine.data.Pieces.pawn;
import static com.jazz.engine.ChessEngine.*;
import static com.jazz.engine.utils.bits.BBUtils.sqrToOrdinal;

/**
 * Heuristic evaluation of a position.
 *
 * Who has more material?
 * Who has better control of the centre?
 *
 * This can be expanded with hard-coded chess principles humans use.
 * A more complex heuristic value can have both positive and negative impacts.
 * Too slow and the huge number of leaf nodes requiring evaluation begin to add up, slowing down the search.
 * But more advanced and we can potentially find refutations more easily (weaknesses are more obvious).
 *
 * Many modern superhuman chess engines now replace this with a neural network that is able to capture the nuances of positional advantage.
 */
public class Evaluator {
    private static final long CENTRE = LongParser.parse(

               "00000000" +
                    "00000000" +
                    "00000000" +

                    "00011000" +
                    "00011000" +

                    "00000000" +
                    "00000000" +
                    "00000000");

    /**
     * |--Heuristic evaluation function--|
     * How good does a position "look"?
     * Who has more material?
     * Who's pieces are better placed?
     * Who can still castle?
     */
    private static float getPiecePositionScore(long[][] pieceBBs, int color, int pieceType, boolean isLateGame){

        int totalValue = 0;
        long pieces = pieceBBs[color][pieceType];
        while (pieces != 0){
            long p = pieces & -pieces;
            int sqr = sqrToOrdinal(p);

            int row = sqr/8;
            int col = sqr%8;

            if(pieceType == king && isLateGame){
                pieceType = 6;
            }
            int value = PieceValueTables.allPieces[color][pieceType][row][col];

            totalValue +=value;

            pieces &= pieces-1;
        }

        return totalValue/100F;
    }

    public static float material(long[][] pieceBBs, float[] pieceValues,
                                 int color){
        float white = 0f;
        float black = 0f;


        // Pawns
        white += Long.bitCount(pieceBBs[WHITE][pawn]);
        black += Long.bitCount(pieceBBs[BLACK][pawn]);

        // Knights/bishops
        white += Long.bitCount(pieceBBs[WHITE][Pieces.knight]) * pieceValues[1];
        black += Long.bitCount(pieceBBs[BLACK][Pieces.knight]) * pieceValues[1];

        white += Long.bitCount(pieceBBs[WHITE][Pieces.bishop]) * pieceValues[2];
        black += Long.bitCount(pieceBBs[BLACK][Pieces.bishop]) * pieceValues[2];

        // Rooks
        white += Long.bitCount(pieceBBs[WHITE][Pieces.rook]) * pieceValues[3];
        black += Long.bitCount(pieceBBs[BLACK][Pieces.rook]) * pieceValues[3];

        // Queens
        white += Long.bitCount(pieceBBs[WHITE][Pieces.queen]) * pieceValues[4];
        black += Long.bitCount(pieceBBs[BLACK][Pieces.queen]) * pieceValues[4];

        //return black-white;
        if(color==WHITE) return white;
        else return black;
    }
    public static float heuristicEvaluation(
            long[][] pieceBBs,
            int color,
            boolean doPositionalEvaluation,
            boolean isLateGame, float[] pieceValues){
        float white = 0f;
        float black = 0f;


        // Pawns
        white += Long.bitCount(pieceBBs[WHITE][pawn]);
        black += Long.bitCount(pieceBBs[BLACK][pawn]);

        // Knights/bishops
        white += Long.bitCount(pieceBBs[WHITE][Pieces.knight]) * pieceValues[1];
        black += Long.bitCount(pieceBBs[BLACK][Pieces.knight]) * pieceValues[1];

        white += Long.bitCount(pieceBBs[WHITE][Pieces.bishop]) * pieceValues[2];
        black += Long.bitCount(pieceBBs[BLACK][Pieces.bishop]) * pieceValues[2];

        // Rooks
        white += Long.bitCount(pieceBBs[WHITE][Pieces.rook]) * pieceValues[3];
        black += Long.bitCount(pieceBBs[BLACK][Pieces.rook]) * pieceValues[3];

        // Queens
        white += Long.bitCount(pieceBBs[WHITE][Pieces.queen]) * pieceValues[4];
        black += Long.bitCount(pieceBBs[BLACK][Pieces.queen]) * pieceValues[4];

        if(doPositionalEvaluation) {
            // Add score from piece-value tables.
            for (int i = 0; i < 6; i++) {
                white += getPiecePositionScore(pieceBBs, WHITE, i, isLateGame);
                black += getPiecePositionScore(pieceBBs, BLACK, i, isLateGame);
            }
        }

        //return black-white;
        if(color==WHITE) return white-black;
        else return black-white;
    }

    public static void updatePositionalScoresUI(long[][] pieceBBs, Statistics statistics, boolean isLateGame){
        for (int i = 0; i < 6; i++) {
            statistics.positionalScores[WHITE][i] = getPiecePositionScore(pieceBBs, WHITE, i, isLateGame);
            statistics.positionalScores[BLACK][i] = getPiecePositionScore(pieceBBs, BLACK, i, isLateGame);
        }
    }
}
