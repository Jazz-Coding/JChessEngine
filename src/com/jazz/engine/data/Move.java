package com.jazz.engine.data;


import static com.jazz.engine.ChessEngine.*;
import static com.jazz.engine.data.Pieces.*;
import static com.jazz.engine.utils.bits.BBUtils.sqrToOrdinal;
import static com.jazz.engine.utils.bits.CastlingMasks.*;
import static java.lang.StringTemplate.STR;

public class Move {
    /**
     * A move is 32 bits, (4 bytes), AKA (int)
     * move = 0 0 0 0 0 0 0 0 0 0 0 0 0  0 0 0  0 0 0  0 0 0 0 0 0  0 0 0 0 0 0  0
     *       |________________________| |____| |____| |__________| |__________| |_|
     *                 flags             opt    dpt      from          to       color
     *
     *
     * flag subdivisions:
     *           0 0 0 0  0 0  0 0 0      0           0           0           0
     *          |______| |__| |____|      |           |           |           |
     *            score    cr   pt     captures   en-passant   castles     castle type
     */
    public static int buildMove(
                                int ep_file, int effectOnCastlingRights, int promotesTo, boolean captures, boolean enPassant, boolean castles, int castleType,
                                int opt,
                                int dpt,
                                int from,
                                int to,
                                int color){
        //if(captures){prospectiveScore+=SEE(opt,dpt);}
        return
                ep_file << 28
                | effectOnCastlingRights << 26
                | promotesTo << 23
                | (captures ? 1:0) << 22
                | (enPassant ? 1:0) << 21
                | (castles ? 1:0) << 20
                | castleType << 19
                | opt << 16
                | dpt << 13
                | from << 7
                | to << 1
                | color
                ;
    }

    public static int buildPawnPromotion(int promotesTo, int dpt, int from, int to, int color){
        return buildMove(
                0,
                0,promotesTo, dpt!=empty,false,false,0,
                pawn,
                dpt,
                from,
                to,
                color);
    }

    public static int buildPawnMove(int dpt, int from, int to, boolean enPassant, int color){
        return buildMove(0,
                0,0,
                dpt!=empty,enPassant,false,0,
                pawn,
                dpt,
                from,
                to,
                color);
    }

    public static int buildBNQMove(byte piece, int dpt, int from, int to, int color){
        return buildMove(0,
                0,0,dpt!=empty,false,false,0,
                piece,
                dpt,
                from,
                to,
                color);
    }

    public static int buildKMove(int dpt, int from, int to, int color, int[] cr_current){
        int cr = 0;

        if(color==WHITE){ // Any king moves invalidate all castling rights, IF any existed to begin with.
            if(from==kingStart[1][1]) cr=3&cr_current[1];
        } else {
            if(from==kingStart[0][1]) cr=3&cr_current[0];
        }

        return buildMove(0,
                cr,0,dpt!=empty,false,false,0,
                king,
                dpt,
                from,
                to,
                color);
    }

    public static int buildRMove(int dpt, int from, int to, int color, int[] cr_current){ // Also include castling right impact, if any.
        int cr = 0;
        // 0 = no impact, 1 = kingside invalidated, 2 = queenside invalidated, 3 = everything invalidated

        /*
         Check if this rook move involves previously unmoved rooks, in which
         case castling on that side is now illegal.
        */
        if(from==rookStart[color][KINGSIDE]) {
            cr = 2 & cr_current[color];
        } else if(from==rookStart[color][QUEENSIDE]) {
            cr = 1 & cr_current[color];
        }

        return buildMove(0,
                cr,0,dpt!=empty,false,false,0,
                rook,
                dpt,
                from,
                to,
                color);
    }

   /* private static int SEE(int opt, int dpt){
        return (int) (pieceValues[dpt]-pieceValues[opt]);
    }*/

    public static int moveSEE(int move, float[] pieceValues){
        float moverValue = pieceValues[getOriginPieceType(move)];
        if(isCapture(move)){
            float victimValue = pieceValues[getDestinationPieceType(move)];
            return (int) (victimValue-moverValue);
        } else {
            return 0;
        }
    }

    public static int bumpKiller(int move){ // bump this killer move to nearly maximum score
        return move|0b1111_00_000_0_0_0_0_000_000_000000_000000_0;
        //return ((move << 4)>>>4) | 0b1110_00_000_0_0_0_0_000_000_000000_000000_0;
    }

    public static int bumpLastBest(int move){ // bump the best move from the last search to maximum score
        return ((move << 4)>>>4) | 0b1111_00_000_0_0_0_0_000_000_000000_000000_0;
    }

    public static int buildKSCastle(int color){
        int from = kingStart[color][KINGSIDE];
        int to = kingEnd[color][KINGSIDE];

        return buildMove(0,
                3,0,false,false,true,KINGSIDE,
                king,
                empty,
                from,
                to,
                color);
    }

    public static int buildQSCastle(int color){
        int from = kingStart[color][QUEENSIDE];
        int to = kingEnd[color][QUEENSIDE];

        return buildMove(0,
                3,0,false,false,true,QUEENSIDE,
                king,
                empty,
                from,
                to,
                color);
    }

    public static int buildNull(int color){
        return buildMove(0,0,0,false,false,false,0,
                empty,empty,0,0,color);
    }

    public static boolean isNull(int move){
        return getOriginPieceType(move)==empty;
    }

    public static int getTo(int move){
        return (move & 0b0000_00_000_0_0_0_0_000_000_000000_111111_0) >>> 1;
    }
    public static int getFrom(int move){
        return (move & 0b0000_00_000_0_0_0_0_000_000_111111_000000_0) >>> 7;
    }
    public static int getDestinationPieceType(int move){
        return (move & 0b0000_00_000_0_0_0_0_000_111_000000_000000_0) >>> 13;
    }
    public static int getOriginPieceType(int move){
        return (move & 0b0000_00_000_0_0_0_0_111_000_000000_000000_0) >>> 16;
    }


    public static int getColor(int move){
        return move & 0b0000_00_000_0_0_0_0_000_000_000000_000000_1;
    }

    public static boolean isCapture(int move){
        return (move & 0b0000_00_000_1_0_0_0_000_000_000000_000000_0) != 0;
    }
    public static boolean isEnpassant(int move){
        return (move & 0b0000_00_000_0_1_0_0_000_000_000000_000000_0) != 0;
    }

    public static boolean isCastles(int move){ // Was the move a castle?
        return (move & 0b0000_00_000_0_0_1_0_000_000_000000_000000_0) != 0;
    }
    public static int castleType(int move){ // The castle type, i.e. Kingside/Queenside
        return (move & 0b0000_00_000_0_0_0_1_000_000_000000_000000_0) >> 19;
    }

    public static boolean isPromotion(int move){ // Was the move a pawn promotion?
        return (move & 0b0000_00_111_0_0_0_0_000_000_000000_000000_0) != 0;
    }

    public static boolean isDblPawnPush(int move){
        int opt = getOriginPieceType(move);
        if(opt != pawn) return false;

        int color = getColor(move);

        int toOrd = getTo(move);
        int fromOrd = getFrom(move);

        if(color==WHITE) {
            return toOrd - fromOrd == 16;
        } else {
            return fromOrd - toOrd == 16;
        }
    }

    public static int getPromotionType(int move){ // The promotion, i.e. Knight/Bishop/Rook/Queen
        return (move & 0b0000_00_111_0_0_0_0_000_000_000000_000000_0) >>> 23;
    }
    public static int setPromotionType(int move, int type){ // The promotion, i.e. Knight/Bishop/Rook/Queen
        return (move & (type << 23));
    }

    public static boolean changedCastlingRights(int move){ // Did the move invalidate castling rights? e.g. moving the kingside rook
        return getEffectOnCastlingRights(move) != 0;
    }

    public static int getEffectOnCastlingRights(int move){ // If so, in what way? e.g. moving the kingside rook invalidates kingside castling
        return (move & 0b0000_11_000_0_0_0_0_000_000_000000_000000_0) >>> 26;
    }

    public static int getScore(int move, boolean isKiller, int hashMove, float[] pieceValues){
        if(move == hashMove) return 16; // The hash move

        boolean capture = isCapture(move);
        if(!capture){
            if(isKiller) return 14; // Killers
            return 4; // Quiet moves.
        } else {
            int SEE = moveSEE(move, pieceValues);
            if(SEE > 0) return 15; // Good captures
            return 5; // Bad captures.
        }
    }

    public static int convert(byte old){
        old &= 0b00000111;
        return (old+2)/2;
    }

    public static int getEPFile(int move){
        return (move & 0b1111_00_000_0_0_0_0_000_000_000000_000000_0) >>> 28;
    }

    public static int setPrevEP(int move,
                                long ep){
        if(ep==0) return move;

        int ord = sqrToOrdinal(ep);
        int file = ord % 8;

        file++; // Shift up by 1 so 0 = no ep.

        return (move & 0b0000_11_111_1_1_1_1_111_111_111111_111111_1) | (file << 28);
    }

    public static long getPrevEP(int move) {
        int epFile = getEPFile(move);
        if(epFile==0) return 0L;
        epFile--; // Shift down 1.

        long epFileL = 1L << epFile;

        int color = getColor(move);

        // Translate into EP long square.
        if(color== WHITE){
            // This move invalidated some of black's EP squares.
            return epFileL << (8*5);
        } else {
            // This move invalidated some of white's EP squares.
            return epFileL << (8*2); // third rank
        }
    }

    public static String ordToPGN(int ord){
        int rank = ord/8;
        int file = ord%8;

        String[] files = new String[]{"a","b","c","d","e","f","g","h"};
        return files[file] + (rank+1);
    }

    private static String[] pieceNames = new String[]{"Pawn","Knight","Bishop","Rook","Queen","King",""};
    public static String ptToString(int pieceType){
        return pieceNames[pieceType];
    }

    public static void printBreakdown(int move){
        System.out.println(STR."""
                Binary: \{String.format("%32s",Integer.toBinaryString(move)).replace(" ","0")}
                Color: \{Move.getColor(move)==WHITE ? "White" : "Black"}
                From: \{ordToPGN(Move.getFrom(move))}
                To: \{ordToPGN(Move.getTo(move))}
                opt: \{ptToString(Move.getOriginPieceType(move))}
                dpt: \{ptToString(Move.getDestinationPieceType(move))}
                captures?: \{Move.isCapture(move)}
                en-passant?: \{Move.isEnpassant(move)}
                dblPawnPush?: \{Move.isDblPawnPush(move)}
                """);
    }
}
