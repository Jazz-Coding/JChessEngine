package com.jazz.engine.moves;

import com.jazz.engine.data.Move;
import com.jazz.engine.data.BBs;
import com.jazz.engine.utils.HashUtils;
import com.jazz.engine.utils.bits.CastlingMasks;

import static com.jazz.engine.ChessEngine.*;


import static com.jazz.engine.data.Pieces.*;
import static com.jazz.engine.data.Pieces.empty;
import static com.jazz.engine.utils.bits.BBUtils.ordinalToSqr;
import static com.jazz.engine.utils.bits.BBUtils.sqrToOrdinal;

/**
 * Move updater - responsible for modifying the current game state to reflect moves made.
 */
public class MoveUpdater {
    /*
        Node update functions.
     */
    private static void updateMove(BBs bbs, int move, boolean unmake){
        int color = Move.getColor(move);
        int opposingColor = color^1;

        int cr = Move.getEffectOnCastlingRights(move);
        bbs.castlingRights[color]^=cr;

        int from = Move.getFrom(move);
        int to = Move.getTo(move);

        // Long forms for bitboard manipulation.
        long l_from = ordinalToSqr(from);
        long l_to = ordinalToSqr(to);

        byte opt = (byte) Move.getOriginPieceType(move);
        byte dpt = (byte) Move.getDestinationPieceType(move);

        long l_fromTo = l_from | l_to;

        boolean enPassant = Move.isEnpassant(move);
        boolean castle = Move.isCastles(move);

        if(unmake){
            bbs.byteBoard[from] = opt;
            bbs.byteBoard[to] = enPassant ? empty : dpt;
        } else {
            bbs.byteBoard[from] = empty;
            bbs.byteBoard[to] = opt;
        }

        if(Move.isCapture(move)){
            updateCapture(bbs, opt,dpt, l_from, l_to, l_fromTo, enPassant,color, unmake);
        } else {
            // Non-captures.
            int castleType = Move.castleType(move);
            updateQuiet(bbs, opt, l_from, l_to, l_fromTo, color, castle, castleType, Move.isDblPawnPush(move), unmake);
        }

        if(Move.isPromotion(move)){
            byte promotionType = (byte) Move.getPromotionType(move);
            updatePromotion(bbs, promotionType, l_to, unmake,color);
        }

        bbs.capturableByColor[color] = ~bbs.occupiedByColor[color];
        bbs.capturableByColor[opposingColor] = ~bbs.occupiedByColor[opposingColor];
    }

    private static void flipHashTurn(BBs bbs){
        bbs.hash ^= HashUtils.zobristBTMString;
        bbs.color^=1;
    }

    private static void updateHashCapture(BBs bbs,
                                  int opt, int dpt, long from, long to, int color){
        int ordFrom = sqrToOrdinal(from);
        int ordTo = sqrToOrdinal(to);

        // Xor the piece out.
        bbs.hash ^= HashUtils.zobristTable[ordFrom][color][opt];

        // Xor the new square.
        bbs.hash ^= HashUtils.zobristTable[ordTo][color][opt];

        // Xor the captured piece out.
        bbs.hash ^= HashUtils.zobristTable[ordTo][color][dpt];
    }
    private static void updateHashPromotion(BBs bbs,
                                    int promotesTo, long to, int color){
        int ordProm = sqrToOrdinal(to);

        // Xor in the new promoted piece.
        bbs.hash ^= HashUtils.zobristTable[ordProm][color][promotesTo];
    }
    private static void updateHashQuiet(BBs bbs,
                                int opt, long from, long to, int color){
        int ordFrom = sqrToOrdinal(from);
        int ordTo = sqrToOrdinal(to);

        // Xor the piece out.
        bbs.hash ^= HashUtils.zobristTable[ordFrom][color][opt];

        if(to != 0) {
            // Xor the new square.
            bbs.hash ^= HashUtils.zobristTable[ordTo][color][opt];
        }
    }

    private static void updateCastle(BBs bbs,
                             int castleType, int color, boolean unmake){
        // Also move the rook.
        int ordRookStart = CastlingMasks.rookStart[color][castleType];
        int ordRookEnd = CastlingMasks.rookEnd[color][castleType];

        long rookStart = ordinalToSqr(ordRookStart);
        long rookEnd = ordinalToSqr(ordRookEnd);
        long rookFromTo = rookStart | rookEnd;

        if(unmake){
            bbs.byteBoard[ordRookEnd] = empty;
            bbs.byteBoard[ordRookStart] = rook;
        } else {
            bbs.byteBoard[ordRookStart] = empty;
            bbs.byteBoard[ordRookEnd] = rook;
        }

        bbs.pieceBBs[color][rook] ^= rookFromTo;
        bbs.occupiedByColor[color] ^= rookFromTo;
        bbs.squaresOccupied ^= rookFromTo;
        bbs.emptySquares ^= rookFromTo;

        updateHashQuiet(bbs, rook, rookStart, rookEnd,color);
    }

    private static void updateQuiet(BBs bbs,
                            byte opt, long from, long to, long fromTo, int color, boolean castle, int castleType, boolean dblPawnPush, boolean unmake){
        // Non-captures.
        bbs.pieceBBs[color][opt] ^= fromTo;
        bbs.occupiedByColor[color] ^= fromTo;
        bbs.squaresOccupied ^= fromTo;
        bbs.emptySquares ^= fromTo;

        updateHashQuiet(bbs, opt, from, to,color);

        if(castle){
            updateCastle(bbs, castleType, color, unmake);
        } else if(dblPawnPush){
            if (color == WHITE) {
                bbs.enPassantable[color] ^= to >>> 8;
            } else {
                bbs.enPassantable[color] ^= to << 8;
            }
        }
    }

    private static void updateEnPassant(BBs bbs,
                                long from, long to, long fromTo,
                                int color, boolean unmake){
        // If a move was en-passant we have to update the bitboards differently.
        int opposingColor = color^1;
        long pieceCapturedViaEnPassant;
        if(color == WHITE){
            pieceCapturedViaEnPassant = to >>> 8; // The black pawn was below (right shift) where we are now.
        } else {
            pieceCapturedViaEnPassant = to << 8; // The white pawn was above (left shift) where we are now.
        }

        updateHashQuiet(bbs, pawn, pieceCapturedViaEnPassant, 0,opposingColor); // A "to" of 0 makes the piece disappear.

        bbs.pieceBBs[opposingColor][pawn] ^= pieceCapturedViaEnPassant;
        bbs.occupiedByColor[opposingColor] ^= pieceCapturedViaEnPassant;

        int enpsntOrdinal = sqrToOrdinal(pieceCapturedViaEnPassant);

        if(unmake){
            bbs.byteBoard[enpsntOrdinal] = pawn;
        } else {
            bbs.byteBoard[enpsntOrdinal] = empty;
        }

        long allInvolved = fromTo | pieceCapturedViaEnPassant;

        bbs.squaresOccupied ^= allInvolved;
        bbs.emptySquares ^= allInvolved;
    }
    private static void updateCapture(BBs bbs,
                              byte opt, byte dpt,
                              long from, long to, long fromTo, boolean enPassant,
                              int color, boolean unmake){
        int opposingColor = color^1;

        bbs.pieceBBs[color][opt] ^= fromTo;
        bbs.occupiedByColor[color] ^= fromTo;

        if(enPassant){
            updateEnPassant(bbs, from, to, fromTo, color, unmake);
            updateHashQuiet(bbs, opt, from, to,color);
        } else {
            bbs.pieceBBs[opposingColor][dpt] ^= to;
            updateHashCapture(bbs, opt, dpt, from, to,color);

            bbs.occupiedByColor[opposingColor] ^= to;
            bbs.squaresOccupied ^= from;
            bbs.emptySquares ^= from;
        }
    }
    private static void updatePromotion(BBs bbs,
                                byte promotesTo, long to, boolean unmake, int color){
        // Replace the pawn with the new piece.
        bbs.pieceBBs[color][promotesTo] ^= to;
        bbs.pieceBBs[color][pawn] ^= to;

        if(!unmake){
            bbs.byteBoard[sqrToOrdinal(to)] = promotesTo;
        }
        updateHashPromotion(bbs, promotesTo,to,color);
    }

    // Actual game sequence logic.
    public static void makeMove(BBs bbs, int move){
        int mover = Move.getColor(move);

        bbs.enPassantable[mover^1] = 0L; // Invalidate en-passants.
        //bbs.enPassantable[mover] = 0L;

        int cr = Move.getEffectOnCastlingRights(move);
        bbs.castlingRights[mover] ^= cr;

        if(Move.isNull(move)){return;}
        updateMove(bbs, move, false);
        flipHashTurn(bbs);
    }

    public static void unmakeMove(BBs bbs, int move){
        long prevEP = Move.getPrevEP(move);
        int mover = Move.getColor(move);

        if(Move.isNull(move)){return;}
        updateMove(bbs, move, true);
        flipHashTurn(bbs);

        bbs.enPassantable[mover^1] ^= prevEP; // Restore EP.

        int cr = Move.getEffectOnCastlingRights(move);
        bbs.castlingRights[mover] ^= cr; // Restore castling rights.
    }
}
