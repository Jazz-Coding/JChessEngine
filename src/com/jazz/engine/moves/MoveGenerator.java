package com.jazz.engine.moves;

import com.jazz.engine.data.BBs;
import com.jazz.engine.data.Move;
import com.jazz.engine.utils.bits.CastlingMasks;
import com.jazz.engine.utils.bits.LongParser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.jazz.engine.ChessEngine.BLACK;
import static com.jazz.engine.ChessEngine.KINGSIDE;
import static com.jazz.engine.ChessEngine.QUEENSIDE;
import static com.jazz.engine.ChessEngine.WHITE;
import static com.jazz.engine.data.Pieces.*;
import static com.jazz.engine.data.PrecomputedBitboards.*;
import static com.jazz.engine.utils.bits.BBUtils.sqrToOrdinal;
import static com.jazz.engine.utils.bits.RankMasks.*;

/**
 * Move generation using bitboard techniques.
 *
 * Pseudo-legal: Illegal moves are removed after generation.
 */
public class MoveGenerator {
    public static List<Integer> allWhiteMoves(BBs bbs) throws ArrayIndexOutOfBoundsException{
        List<Integer> whiteMoves = new LinkedList<>();
        addWhitePawnMoves(bbs, whiteMoves);

        addKnightMoves(bbs,whiteMoves,WHITE);
        addBishopMoves(bbs,whiteMoves,WHITE);
        addRookMoves(bbs,whiteMoves,WHITE);
        addQueenMoves(bbs,whiteMoves,WHITE);
        addKingMoves(bbs,whiteMoves,WHITE);

        whiteMoves.replaceAll(move -> Move.setPrevEP(move, bbs.enPassantable[BLACK])); // Ensure illegalMove() is able to restore en-passants.
        whiteMoves.removeIf(move -> illegalMove(bbs,move,WHITE));

        return whiteMoves;
    }
    public static List<Integer> allBlackMoves(BBs bbs){
        List<Integer> blackMoves = new ArrayList<>();
        addBlackPawnMoves(bbs, blackMoves);

        addKnightMoves(bbs,blackMoves,BLACK);
        addBishopMoves(bbs,blackMoves,BLACK);
        addRookMoves(bbs,blackMoves,BLACK);
        addQueenMoves(bbs,blackMoves,BLACK);
        addKingMoves(bbs,blackMoves,BLACK);

        blackMoves.replaceAll(move -> Move.setPrevEP(move, bbs.enPassantable[WHITE])); // Ensure illegalMove() is able to restore en-passants.
        blackMoves.removeIf(move -> illegalMove(bbs, move,BLACK));

        return blackMoves;
    }
    public static List<Integer> allMoves(BBs bbs) throws ArrayIndexOutOfBoundsException{
        List<Integer> moves;
        int color = bbs.color;
        //int opposingColor = bbs.color ^ 1;

        if(color==WHITE) moves = allWhiteMoves(bbs);
        else moves =  allBlackMoves(bbs);

        //moves.replaceAll(move -> Move.setPrevEP(move, bbs.enPassantable[opposingColor]));

        return moves;
    }

    public static boolean illegalMove(
            BBs bbs, int move, int moverColor){
        long[] records = new long[4];
        long prevEP = Move.getPrevEP(move);

        records[0] = bbs.enPassantable[moverColor^1];
        MoveUpdater.makeMove(bbs, move);
        records[1] = bbs.enPassantable[moverColor^1];
        boolean leavesKingChecked = isKingInCheck(bbs,moverColor);
        records[2] = bbs.enPassantable[moverColor^1];
        MoveUpdater.unmakeMove(bbs, move);
        records[3] = bbs.enPassantable[moverColor^1];

        if(records[0] != records[3]){
            System.out.println("EP permanence violation detected! Printing log: ");
            System.out.println("---Begin illegal move checking---> ["+(moverColor==WHITE?"WHITE":"BLACK"));
            for (long record : records) {
                System.out.println(LongParser.printable(record));
            }
            System.out.println("<---end illegal move checking---");
            System.out.println("Move info (so you can check prevEP): ");
            System.out.println(LongParser.printable(prevEP));
        }
        return leavesKingChecked;
    }

    public static void addWhitePawnMoves(BBs bbs,
                                         List<Integer> movesList){
        long whitePawns = bbs.pieceBBs[WHITE][pawn];
        while(whitePawns != 0){
            long whitePawn = whitePawns & -whitePawns;

            int from = sqrToOrdinal(whitePawn);

            // Consider pawn pushes.
            long onePush = (whitePawn << 8) & bbs.emptySquares;
            long twoPush = ((onePush << 8) & rank4Mask) & bbs.emptySquares;

            long squaresAttacked = precomputedPawnAttacks[WHITE][from];

            long regularCaptures = bbs.occupiedByColor[BLACK] & squaresAttacked;
            long EPs = bbs.enPassantable[BLACK] & squaresAttacked;

            long quietMoves = onePush | twoPush;

            while (quietMoves != 0){
                long m = quietMoves & -quietMoves;

                int to = sqrToOrdinal(m);

                if((m & rank8Mask) != 0) {
                    // Factor in promotion options.
                    movesList.add(Move.buildPawnPromotion(knight,empty,from,to, WHITE));
                    movesList.add(Move.buildPawnPromotion(bishop,empty,from,to, WHITE));
                    movesList.add(Move.buildPawnPromotion(rook,empty,from,to, WHITE));
                    movesList.add(Move.buildPawnPromotion(queen,empty,from,to, WHITE));
                } else {
                    movesList.add(Move.buildPawnMove(empty, from, to, false, WHITE));
                }
                quietMoves &= quietMoves-1;
            }

            while (regularCaptures != 0){
                long capture = regularCaptures & -regularCaptures;

                int to = sqrToOrdinal(capture);
                int dpt = getCaptureType(bbs,capture);

                if((capture & rank8Mask) != 0) {
                    // Factor in promotion options.
                    movesList.add(Move.buildPawnPromotion(knight,dpt,from,to, WHITE));
                    movesList.add(Move.buildPawnPromotion(bishop,dpt,from,to, WHITE));
                    movesList.add(Move.buildPawnPromotion(rook,dpt,from,to, WHITE));
                    movesList.add(Move.buildPawnPromotion(queen,dpt,from,to, WHITE));
                } else {
                    movesList.add(Move.buildPawnMove(dpt, from, to, false, WHITE));
                }

                regularCaptures &= regularCaptures-1;
            }

            while (EPs != 0){
                long ep = EPs & -EPs;

                int to = sqrToOrdinal(ep);
                movesList.add(Move.buildPawnMove(pawn, from, to, true, WHITE));

                EPs &= EPs-1;
            }

            whitePawns &= whitePawns-1;
        }
    }

    public static void addBlackPawnMoves(BBs bbs, List<Integer> movesList){
        long blackPawns = bbs.pieceBBs[BLACK][pawn];
        while(blackPawns != 0){
            long blackPawn = blackPawns & -blackPawns;

            int from = sqrToOrdinal(blackPawn);

            // Consider pawn pushes.
            long onePush = (blackPawn >>> 8) & bbs.emptySquares;
            long twoPush = ((onePush >>> 8) & rank5Mask) & bbs.emptySquares;

            long squaresAttacked = precomputedPawnAttacks[BLACK][from];

            long regularCaptures = bbs.occupiedByColor[WHITE] & squaresAttacked;
            long EPs = bbs.enPassantable[WHITE] & squaresAttacked;

            long quietMoves = onePush | twoPush;

            while (quietMoves != 0){
                long m = quietMoves & -quietMoves;

                int to = sqrToOrdinal(m);

                if((m & rank1Mask) != 0) {
                    // Factor in promotion options.
                    movesList.add(Move.buildPawnPromotion(knight,empty,from,to, BLACK));
                    movesList.add(Move.buildPawnPromotion(bishop,empty,from,to, BLACK));
                    movesList.add(Move.buildPawnPromotion(rook,empty,from,to, BLACK));
                    movesList.add(Move.buildPawnPromotion(queen,empty,from,to, BLACK));
                } else {
                    movesList.add(Move.buildPawnMove(empty, from, to, false, BLACK));
                }
                quietMoves &= quietMoves-1;
            }
            while (regularCaptures != 0){
                long capture = regularCaptures & -regularCaptures;

                int to = sqrToOrdinal(capture);
                int dpt = getCaptureType(bbs,capture);

                if((capture & rank1Mask) != 0) {
                    // Factor in promotion options.
                    movesList.add(Move.buildPawnPromotion(knight,dpt,from,to, BLACK));
                    movesList.add(Move.buildPawnPromotion(bishop,dpt,from,to, BLACK));
                    movesList.add(Move.buildPawnPromotion(rook,dpt,from,to, BLACK));
                    movesList.add(Move.buildPawnPromotion(queen,dpt,from,to, BLACK));
                } else {
                    movesList.add(Move.buildPawnMove(dpt, from, to, false, BLACK));
                }

                regularCaptures &= regularCaptures-1;
            }
            while (EPs != 0){
                long ep = EPs & -EPs;

                int to = sqrToOrdinal(ep);
                movesList.add(Move.buildPawnMove(pawn, from, to, true, BLACK));

                EPs &= EPs-1;
            }

            blackPawns &= blackPawns-1;
        }
    }

    public static long knightAttacks(BBs bbs, int fromOrd, int color){
        return (precomputedKnightAttacks[fromOrd] & bbs.capturableByColor[color]);
    }
    public static void addKnightMoves(BBs bbs, List<Integer> movesList, int color){
        long knights = bbs.pieceBBs[color][knight];
        while (knights != 0){
            long knightSqr = knights & -knights;
            int from = sqrToOrdinal(knightSqr);
            long knightAttacks = knightAttacks(bbs, from, color);

            while (knightAttacks != 0){
                long destination = knightAttacks & -knightAttacks;
                int to = sqrToOrdinal(destination);
                int dpt = getCaptureType(bbs, destination);

                movesList.add(Move.buildBNQMove(knight,dpt,from,to, color));

                knightAttacks &= knightAttacks-1;
            }

            knights &= knights-1;
        }
    }

    public static long bishopAttacks(BBs bbs, int fromOrd, int color){
        // Sliding piece attacks are mapped by the configuration of blockers.
        // In the piece line of sight.
        long relevantBlockers =
                bbs.squaresOccupied
                & precomputedBishopRayMasks[fromOrd]; // Precomputed LOS masks.
        return (precomputedBishopAttacks[fromOrd].get(relevantBlockers) & bbs.capturableByColor[color]);
    }
    public static void addBishopMoves(BBs bbs, List<Integer> movesList, int color){
        long bishops = bbs.pieceBBs[color][bishop];
        while (bishops != 0){
            long bishopSqr = bishops & -bishops;
            int from = sqrToOrdinal(bishopSqr);
            long bishopAttacks = bishopAttacks(bbs, from, color);

            while (bishopAttacks != 0){
                long destination = bishopAttacks & -bishopAttacks;
                int to = sqrToOrdinal(destination);
                int dpt = getCaptureType(bbs, destination);

                movesList.add(Move.buildBNQMove(bishop,dpt,from,to, color));

                bishopAttacks &= bishopAttacks-1;
            }

            bishops &= bishops-1;
        }
    }

    public static long rookAttacks(BBs bbs, int fromOrd, int color){
        // Sliding piece attacks are mapped by the configuration of blockers.
        // In the piece line of sight.
        long relevantBlockers = bbs.squaresOccupied & precomputedRookRayMasks[fromOrd]; // Precomputed LOS masks.
        return (precomputedRookAttacks[fromOrd].get(relevantBlockers) & bbs.capturableByColor[color]);
    }

    public static void addRookMoves(BBs bbs, List<Integer> movesList, int color){
        long rooks = bbs.pieceBBs[color][rook];
        while (rooks != 0){
            long rookSqr = rooks & -rooks;
            int from = sqrToOrdinal(rookSqr);
            long rookAttacks = rookAttacks(bbs, from, color);

            while (rookAttacks != 0){
                long destination = rookAttacks & -rookAttacks;
                int to = sqrToOrdinal(destination);
                int dpt = getCaptureType(bbs, destination);

                movesList.add(Move.buildRMove(dpt,from,to,color,bbs.castlingRights));

                rookAttacks &= rookAttacks-1;
            }

            rooks &= rooks-1;
        }
    }

    public static long queenAttacks(BBs bbs, int fromOrd, int color){
        // Queen moves combine rook and bishop moves.
        return rookAttacks(bbs, fromOrd,color) | bishopAttacks(bbs, fromOrd,color);
    }

    public static void addQueenMoves(BBs bbs, List<Integer> movesList, int color){
        long queens = bbs.pieceBBs[color][queen];
        while (queens != 0){
            long queenSqr = queens & -queens;
            int from = sqrToOrdinal(queenSqr);
            long queenAttacks = queenAttacks(bbs, from, color);

            while (queenAttacks != 0){
                long destination = queenAttacks & -queenAttacks;
                int to = sqrToOrdinal(destination);
                int dpt = getCaptureType(bbs, destination);

                movesList.add(Move.buildBNQMove(queen,dpt,from,to,color));

                queenAttacks &= queenAttacks-1;
            }

            queens &= queens-1;
        }
    }

    public static long kingAttacks(BBs bbs, int fromOrd, int color) throws ArrayIndexOutOfBoundsException{
        return precomputedKingAttacks[fromOrd] & bbs.capturableByColor[color];
    }

    public static void addKingMoves(BBs bbs, List<Integer> movesList, int color) throws ArrayIndexOutOfBoundsException{
        long kingSqr = bbs.pieceBBs[color][king];

        addCastles(bbs, movesList,kingSqr,color);

        int from = sqrToOrdinal(kingSqr);
        long kingAttacks = kingAttacks(bbs, from, color);

        while (kingAttacks != 0){
            long destination = kingAttacks & -kingAttacks;
            int to = sqrToOrdinal(destination);
            int dpt = getCaptureType(bbs, destination);

            movesList.add(Move.buildKMove(dpt,from,to,color,bbs.castlingRights));

            kingAttacks &= kingAttacks-1;
        }
    }

    public static void addCastles(BBs bbs, List<Integer> movesList, long kingPos, int color){
        int opposingColor = color^1;

        if(bbs.castlingRights[color]==2 || bbs.castlingRights[color]==3) {
            if ((CastlingMasks.castlingMasks[color][KINGSIDE][2] & kingPos) != 0) {
                long kingsideRookSqr = bbs.pieceBBs[color][rook];
                if ((CastlingMasks.castlingMasks[color][KINGSIDE][4] & kingsideRookSqr) != 0) {
                    // Both pieces are in the right spots, but are the square in between empty?
                    if ((CastlingMasks.castlingMasks[color][KINGSIDE][0] & bbs.squaresOccupied) == 0) {
                        // Is anything in check?
                        if (!squaresAttackedBy(bbs, CastlingMasks.castlingMasks[color][KINGSIDE][1],opposingColor)) {
                            // If not then castling is a legal move.
                            movesList.add(Move.buildKSCastle(color));
                        }
                    }
                }
            }
        }

        if(bbs.castlingRights[color]==1 || bbs.castlingRights[color]==3) {
            if ((CastlingMasks.castlingMasks[color][QUEENSIDE][2] & kingPos) != 0) {
                long queensideRook = bbs.pieceBBs[color][rook];
                if ((CastlingMasks.castlingMasks[color][QUEENSIDE][4] & queensideRook) != 0) {
                    // Both pieces are in the right spots, but are the square in between empty?
                    if ((CastlingMasks.castlingMasks[color][QUEENSIDE][0] & bbs.squaresOccupied) == 0) {
                        // Is anything in check?
                        if (!squaresAttackedBy(bbs, CastlingMasks.castlingMasks[color][QUEENSIDE][1],opposingColor)) {
                            // If not then castling is a legal move.
                            movesList.add(Move.buildQSCastle(color));
                        }
                    }
                }
            }
        }
    }


    /*
    Utils
     */
    public static byte getCaptureType(BBs bbs, long attack){
        return bbs.byteBoard[sqrToOrdinal(attack)];
    }


    public static boolean isKingInCheck(BBs bbs, int kingColor){
        long kingSqr = bbs.pieceBBs[kingColor][king];
        int kOrd = sqrToOrdinal(kingSqr);

        int attackerColor = kingColor^1;

        long opposingPawns = bbs.pieceBBs[attackerColor][pawn];
        long opposingPawnAttacks = precomputedPawnAttacks[kingColor][kOrd];
        if((opposingPawnAttacks & opposingPawns) != 0) return true;

        long opposingKnights =
                bbs.pieceBBs[attackerColor][knight];
        long opposingKnightAttacks =
                precomputedKnightAttacks[kOrd];
        if((opposingKnightAttacks & opposingKnights) != 0) return true;

        long opposingKing = bbs.pieceBBs[attackerColor][king];
        long opposingKingAttacks = precomputedKingAttacks[kOrd];
        if((opposingKingAttacks & opposingKing) != 0) return true;

        long opposingBishops = bbs.pieceBBs[attackerColor][bishop];
        long opposingRooks = bbs.pieceBBs[attackerColor][rook];
        long opposingQueens = bbs.pieceBBs[attackerColor][queen];

        long opposingBQ = opposingBishops | opposingQueens;
        long relevantBlockersBQ = bbs.squaresOccupied & precomputedBishopRayMasks[kOrd];
        long opposingBQAttacks = precomputedBishopAttacks[kOrd].get(relevantBlockersBQ);
        if((opposingBQAttacks & opposingBQ) != 0) return true;

        long opposingRQ = opposingRooks | opposingQueens;
        long relevantBlockersRQ = bbs.squaresOccupied & precomputedRookRayMasks[kOrd];
        long opposingRQAttacks = precomputedRookAttacks[kOrd].get(relevantBlockersRQ);
        if((opposingRQAttacks & opposingRQ) != 0) return true;

        return false;
       /* long result =
                (opposingPawnAttacks & opposingPawns)
                |   (opposingKnightAttacks & opposingKnights)
                |   (opposingBQAttacks & opposingBQ)
                |   (opposingRQAttacks & opposingRQ)
                |   (opposingKingAttacks & opposingKing);
        return result != 0;*/
    }
    /**
     * Square attack detection for single squares.
     * Used for check checking.
     */
    public static boolean squareAttackedBy(BBs bbs, long sqr, int attackerColor){
        int defenderColor = attackerColor^1;
        int ord = sqrToOrdinal(sqr);

        long opposingPawns = bbs.pieceBBs[attackerColor][pawn];
        if ((precomputedPawnAttacks[defenderColor][ord] & opposingPawns) != 0) {
            return true;
        }

        long opposingKnights = bbs.pieceBBs[attackerColor][knight];
        if ((precomputedKnightAttacks[ord] & opposingKnights) != 0) {
            return true;
        }

        long opposingBishops = bbs.pieceBBs[attackerColor][bishop];
        long opposingRooks = bbs.pieceBBs[attackerColor][rook];
        long opposingQueens = bbs.pieceBBs[attackerColor][queen];

        long opposingKing = bbs.pieceBBs[attackerColor][king];
        if((precomputedKingAttacks[ord] & opposingKing) != 0){
            return true;
        }

        long opposingBQ = opposingBishops | opposingQueens;
        long relevantBlockers = bbs.squaresOccupied & precomputedBishopRayMasks[ord];
        if((precomputedBishopAttacks[ord].get(relevantBlockers) & opposingBQ) != 0){
            return true;
        }

        long opposingRQ = opposingRooks | opposingQueens;
        relevantBlockers = bbs.squaresOccupied & precomputedRookRayMasks[ord];
        if((precomputedRookAttacks[ord].get(relevantBlockers) & opposingRQ) != 0){
            return true;
        }

        return false;
    }

    /**
     * Square attack detection for multiple squares.
     * Used for castling rights checking.
     */
    public static boolean squaresAttackedBy(BBs bbs, long sqrs, int attackerColor){
        while (sqrs != 0){
            long sqr = sqrs & -sqrs;
            if(squareAttackedBy(bbs, sqr,attackerColor)){
                return true;
            }
            sqrs &= sqrs-1;
        }
        return false;
    }


    public static boolean inCheck(BBs bbs, int color){
        return isKingInCheck(bbs,color);
        //int opposingColor = color^1;
        //return squareAttackedBy(bbs, bbs.pieceBBs[color][king],opposingColor);
    }
    public static boolean someoneInCheck(BBs bbs){
        return inCheck(bbs,0) || inCheck(bbs,1);
    }
}
