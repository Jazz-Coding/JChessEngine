package com.jazz.old;

import com.jazz.engine.ChessEngine;
import com.jazz.engine.utils.bits.OldPieces;

import java.util.List;

/**
 * Leverages the rayrtracer in reverse to find attackers that have the king in their sights
 * i.e. We pretend we are the attacker in the king's position and look to see if we can capture ourselves anywhere.
 */
public class CheckChecker {

    private static boolean traceContains(byte[][] board, List<Position> trace, byte piece){
        // We need only check the final element since the rays halt when they reach the piece in question.
        for (Position position : trace) {
            if(board[position.getRank()][position.getFile()] == piece){
                return true;
            }
        }
        return false;
    }
    public static boolean isSquareCheckedByWhite(Raytracer rt, Board boardObj, Position kingPosition){
        byte[][] board = boardObj.getBoardRepresentation();

        // Does inverse ray-traces for each piece, and if a corresponding piece of the opposing colour is found, the king is in check.

        // e.g. we pretend we are a black pawn and see if we run into a white pawn in our attack pattern.
        // This tell us if there is a white pawn attacking us (the black king)
        if(kingPosition.getRank() > 1) {
            // impossible to be checked by a white pawn if the black king is on ranks 1 or 2
            List<Position> pawnTraces = rt.traceBlackPawn(boardObj, kingPosition);
            if (traceContains(board, pawnTraces, OldPieces.pawn_white)) {
                return true;
            }
        }

        List<Position> knightTraces = rt.traceKnight(board, kingPosition, ChessEngine.BLACK);
        if(traceContains(board, knightTraces, OldPieces.knight_white)){
            return true;
        }

        List<Position> bishopTraces = rt.traceBishop(board, kingPosition, ChessEngine.BLACK, 1337);
        if(traceContains(board, bishopTraces, OldPieces.bishop_white)){
            return true;
        }
        if(traceContains(board, bishopTraces, OldPieces.queen_white)){
            return true;
        }

        List<Position> rookTraces = rt.traceRook(board, kingPosition, ChessEngine.BLACK, 1337);
        if(traceContains(board, rookTraces, OldPieces.rook_white)){
            return true;
        }
        if(traceContains(board, rookTraces, OldPieces.queen_white)){
            return true;
        }

        // Check for "attacking" kings.
        List<Position> kingTraces = rt.traceQueen(board, kingPosition, ChessEngine.BLACK, 1);
        if(traceContains(board, kingTraces, OldPieces.king_white)){
            return true;
        }

        return false;
    }

    public static boolean isSquareCheckedByBlack(Raytracer rt, Board boardObj, Position kingPosition){
        byte[][] board = boardObj.getBoardRepresentation();

        // Pretend we are white and see if we run into any black pieces.

        if(kingPosition.getRank() < 6) {
            List<Position> pawnTraces = rt.traceWhitePawn(boardObj, kingPosition);
            if (traceContains(board, pawnTraces, OldPieces.pawn_black)) {
                return true;
            }
        }

        List<Position> knightTraces = rt.traceKnight(board, kingPosition, ChessEngine.WHITE);
        if(traceContains(board, knightTraces, OldPieces.knight_black)){
            return true;
        }

        List<Position> bishopTraces = rt.traceBishop(board, kingPosition, ChessEngine.WHITE, 1337);
        if(traceContains(board, bishopTraces, OldPieces.bishop_black)){
            return true;
        }
        if(traceContains(board, bishopTraces, OldPieces.queen_black)){
            return true;
        }

        List<Position> rookTraces = rt.traceRook(board, kingPosition, ChessEngine.WHITE, 1337);
        if(traceContains(board, rookTraces, OldPieces.rook_black)){
            return true;
        }
        if(traceContains(board, rookTraces, OldPieces.queen_black)){
            return true;
        }

        // Check for "attacking" kings.
        List<Position> kingTraces = rt.traceQueen(board, kingPosition, ChessEngine.WHITE, 1);
        if(traceContains(board, kingTraces, OldPieces.king_black)){
            return true;
        }

        return false;
    }
}
