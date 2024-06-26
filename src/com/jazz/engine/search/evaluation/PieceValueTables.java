package com.jazz.engine.search.evaluation;

import static com.jazz.engine.ChessEngine.BLACK;
import static com.jazz.engine.ChessEngine.WHITE;

public class PieceValueTables {
    private static String pawnsBase = """
            0,  0,  0,  0,  0,  0,  0,  0,
            50, 50, 50, 50, 50, 50, 50, 50,
            10, 10, 20, 30, 30, 20, 10, 10,
            5,  5, 10, 25, 25, 10,  5,  5,
            0,  0,  0, 20, 20,  0,  0,  0,
            5, -5,-10,  0,  0,-10, -5,  5,
            5, 10, 10,-20,-20, 10, 10,  5,
            0,  0,  0,  0,  0,  0,  0,  0""";

    private static String knightsBase = """
            -50,-40,-30,-30,-30,-30,-40,-50,
            -40,-20,  0,  0,  0,  0,-20,-40,
            -30,  0, 10, 15, 15, 10,  0,-30,
            -30,  5, 15, 20, 20, 15,  5,-30,
            -30,  0, 15, 20, 20, 15,  0,-30,
            -30,  5, 10, 15, 15, 10,  5,-30,
            -40,-20,  0,  5,  5,  0,-20,-40,
            -50,-40,-30,-30,-30,-30,-40,-50""";

    private static String bishopsBase = """
            -20,-10,-10,-10,-10,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5, 10, 10,  5,  0,-10,
            -10,  5,  5, 10, 10,  5,  5,-10,
            -10,  0, 10, 10, 10, 10,  0,-10,
            -10, 10, 10, 10, 10, 10, 10,-10,
            -10,  5,  0,  0,  0,  0,  5,-10,
            -20,-10,-10,-10,-10,-10,-10,-20""";

    private static String rooksBase = """
              0,  0,  0,  0,  0,  0,  0,  0,
              5, 10, 10, 10, 10, 10, 10,  5,
             -5,  0,  0,  0,  0,  0,  0, -5,
             -5,  0,  0,  0,  0,  0,  0, -5,
             -5,  0,  0,  0,  0,  0,  0, -5,
             -5,  0,  0,  0,  0,  0,  0, -5,
             -5,  0,  0,  0,  0,  0,  0, -5,
              0,  0,  0,  5,  5,  0,  0,  0""";

    private static String queenBase = """
            -20,-10,-10, -5, -5,-10,-10,-20,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -10,  0,  5,  5,  5,  5,  0,-10,
             -5,  0,  5,  5,  5,  5,  0, -5,
              0,  0,  5,  5,  5,  5,  0, -5,
            -10,  5,  5,  5,  5,  5,  0,-10,
            -10,  0,  5,  0,  0,  0,  0,-10,
            -20,-10,-10, -5, -5,-10,-10,-20""";

    private static String king_midgameBase = """
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -20,-30,-30,-40,-40,-30,-30,-20,
            -10,-20,-20,-20,-20,-20,-20,-10,
             20, 20,  0,  0,  0,  0, 20, 20,
             20, 30, 10,  0,  0, 10, 30, 20""";

    private static String king_endgameBase = """
            -50,-40,-30,-20,-20,-30,-40,-50,
            -30,-20,-10,  0,  0,-10,-20,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-30,  0,  0,  0,  0,-30,-30,
            -50,-30,-30,-30,-30,-30,-30,-50""";

    private static String[] allBases = new String[]{pawnsBase,knightsBase,bishopsBase,rooksBase,queenBase,king_midgameBase,king_endgameBase};
    public static int[][][][] allPieces = new int[2][7][8][8];

    private static int[][] parseBaseString(String base){
        int[][] result = new int[8][8];

        String[] rows = base.split("\n");
        for (int i = 0; i < rows.length; i++) {
            String row = rows[i];

            String[] elements = row.split(",");
            for (int j = 0; j < 8; j++) {
                String element = elements[j].replaceAll(" ","");
                result[i][j] = Integer.parseInt(element);
            }
        }

        return result;
    }

    public static void init(){
        for (int p = 0; p < allBases.length; p++) {
            String base = allBases[p];
            int[][] valuesWhitePOV = parseBaseString(base);

            for (int i = 0; i <= 7; i++) {
                int wi = 7-i;
                for (int j = 0; j <= 7; j++) {
                    int wj = j;
                    allPieces[WHITE][p][wi][wj] = valuesWhitePOV[i][j];
                }
            }

            // Black's tables are mirrored.
            for (int i = 0; i <= 7; i++) {
                int bi = 7-i;
                for (int j = 0; j <= 7; j++) {
                    int bj = j;
                    allPieces[BLACK][p][bi][bj] = valuesWhitePOV[7-i][7-j];
                }
            }
        }
    }
}
