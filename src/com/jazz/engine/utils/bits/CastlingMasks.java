package com.jazz.engine.utils.bits;

import static com.jazz.engine.ChessEngine.*;

public class CastlingMasks {
    // 2 rows for each colour
    // 2 columns for each type of castling (kingside/queenside)
    // 6 aisles for each relevant bit of information
    //  0 -> squares that must be empty for a legal castle
    //  1 -> squares that must not be under attack (checked) for a legal castle.
    //  2 -> king start position
    //  3 -> king end position
    //  4 -> rook start position
    //  5 -> rook end position

    // They are stored in this fashion to enable quick castling operations by XORing with the king and rook bitboards.
    //      e.g     rooks[white] ^= (castlingMasks[0][0][4] | castlingMasks[0][0][5])
    //      Moves the rook in a white kingside castle.
    public static long[][][] castlingMasks = new long[2][2][6];

    public static int[][] rookStart = new int[2][2];
    public static int[][] rookEnd = new int[2][2];

    public static int[][] kingStart = new int[2][2];
    public static int[][] kingEnd = new int[2][2];

    /**
     * Populates the castling masks.
     */
    public static void fill(){
        rookStart[WHITE][KINGSIDE] = 7; // WKS
        rookStart[WHITE][QUEENSIDE] = 0; // WQS

        rookStart[BLACK][KINGSIDE] = 63; // BKS
        rookStart[BLACK][QUEENSIDE] = 56; // BQS

        rookEnd[WHITE][KINGSIDE] = 5; // WKS
        rookEnd[WHITE][QUEENSIDE] = 3; // WQS

        rookEnd[BLACK][KINGSIDE] = 61; // BKS
        rookEnd[BLACK][QUEENSIDE] = 59; // BQS


        kingStart[WHITE][KINGSIDE] = 4; // WKS
        kingStart[WHITE][QUEENSIDE] = 4; // WQS

        kingStart[BLACK][KINGSIDE] = 60; // BKS
        kingStart[BLACK][QUEENSIDE] = 60; // BQS

        kingEnd[WHITE][KINGSIDE] = 6; // WKS
        kingEnd[WHITE][QUEENSIDE] = 2; // WQS

        kingEnd[BLACK][KINGSIDE] = 62; // BKS
        kingEnd[BLACK][QUEENSIDE] = 58; // BQS


        // White kingside.
        castlingMasks[0][0][0] = LongParser.parse( // Must be empty
                "00000000" +
                "00000000" +
                "00000000" +
                "00000000" +
                "00000000" +
                "00000000" +
                "00000000" +
                "00000110");
        castlingMasks[0][0][1] = LongParser.parse( // Can't be under attack
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00001110");
        castlingMasks[0][0][2] = LongParser.parse( // King start
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00001000");
        castlingMasks[0][0][3] = LongParser.parse( // King end
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000010");
        castlingMasks[0][0][4] = LongParser.parse( // Rook start
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000001");
        castlingMasks[0][0][5] = LongParser.parse( // Rook end
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000100");


        // White queenside.
        castlingMasks[0][1][0] = LongParser.parse( // Must be empty
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "01110000");
        castlingMasks[0][1][1] = LongParser.parse( // Can't be under attack
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00111000");
        castlingMasks[0][1][2] = LongParser.parse( // King start
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00001000");
        castlingMasks[0][1][3] = LongParser.parse( // King end
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00100000");
        castlingMasks[0][1][4] = LongParser.parse( // Rook start
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "10000000");
        castlingMasks[0][1][5] = LongParser.parse( // Rook end
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00010000");


        // Black kingside.
        castlingMasks[1][0][0] = LongParser.parse( // Must be empty
                        "00000110" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000");
        castlingMasks[1][0][1] = LongParser.parse( // Can't be under attack
                        "00001110" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000");
        castlingMasks[1][0][2] = LongParser.parse( // King start
                        "00001000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000");
        castlingMasks[1][0][3] = LongParser.parse( // King end
                        "00000010" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000");
        castlingMasks[1][0][4] = LongParser.parse( // Rook start
                        "00000001" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000");
        castlingMasks[1][0][5] = LongParser.parse( // Rook end
                        "00000100" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000");


        // Black queenside.
        castlingMasks[1][1][0] = LongParser.parse( // Must be empty
                        "01110000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000");
        castlingMasks[1][1][1] = LongParser.parse( // Can't be under attack
                        "00111000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000");
        castlingMasks[1][1][2] = LongParser.parse( // King start
                        "00001000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000");
        castlingMasks[1][1][3] = LongParser.parse( // King end
                        "00100000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000");
        castlingMasks[1][1][4] = LongParser.parse( // Rook start
                        "10000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000");
        castlingMasks[1][1][5] = LongParser.parse( // Rook end
                        "00010000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000" +
                        "00000000");
    }
}
