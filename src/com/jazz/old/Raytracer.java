package com.jazz.old;

import com.jazz.engine.ChessEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs ray-traces on board objects.
 * "Rays" are cast in specific directions, being halted by pieces in the way or the board boundaries.
 * e.g. for a queen, 8 rays are cast (left, right, up, down and the 4 diagonals), which may be blocked by pieces
 *
 * Rays striking friendly pieces don't return the friendly square as a valid move, but rays striking enemy ones do (captures)
 *
 * This method is a bit computationally expensive since most of the time, the rays won't change much but still have to be re-computed,
 * which will involve a lot of branches, loops and array accesses causing cache misses.
 */
public class Raytracer {
    private boolean opposingColours(byte pieceA, byte pieceB){
        return getColour(pieceA) != getColour(pieceB);
    }

    /**
     * Considers a move, adding it to the list of viable moves if valid.
     * true = continue trace
     * false = halt trace
     */
    private boolean considerMove(List<Position> moveAccumulator,
                                 byte[][] board,
                                 int moverColor,
                                 Position position,
                                 boolean allowCaptures){


        int rank = position.getRank();
        int file = position.getFile();
        
        byte boardValue = board[rank][file];

        if (boardValue == 0) {
            // Empty square, free to move here.
            moveAccumulator.add(position);
            return true;
        } else {
            if(moverColor != getColour(boardValue) && allowCaptures){
                // Opposing color, halt the trace and mark as a viable move (capture).
                moveAccumulator.add(position);
            }
            // Otherwise this is a blockage, halt the trace but don't record it as a viable move.
            return false;
        }
    }

    public List<Position> traceLeft(byte[][] board, Position start, int moverColor, int limit){
        List<Position> viableMoves = new ArrayList<>();

        int file = start.getFile();
        int rank = start.getRank();

        byte startPiece = board[rank][file];

        int finish = Math.max(file-limit, 0); // Limit the leftmost file.

        // Start a file to the left.
        file--;

        for (; file >=finish ; file--) {
            Position position = new Position(rank, file); // Decrease the file but keep rank the same.
            boolean traceOutcome = considerMove(viableMoves, board, moverColor, position,true);
            if (!traceOutcome) {
                // If the trace hits something, terminate.
                return viableMoves;
            }
        }
        return viableMoves;
    }
    public List<Position> traceRight(byte[][] board, Position start, int moverColor, int limit){
        List<Position> viableMoves = new ArrayList<>();

        int file = start.getFile();
        int rank = start.getRank();

        byte startPiece = board[rank][file];

        int finish = Math.min(file+limit, 7); // Limit the rightmost file.

        // Start a file to the right.
        file++;

        for (; file <=finish ; file++) {
            Position position = new Position(rank, file); // Increase the file but keep rank the same.
            boolean traceOutcome = considerMove(viableMoves, board, moverColor, position, true);
            if (!traceOutcome) {
                return viableMoves;
            }
        }

        return viableMoves;
    }
    public List<Position> traceUp(byte[][] board, Position start, int moverColor, int limit, boolean allowCaptures){
        List<Position> viableMoves = new ArrayList<>();

        int file = start.getFile();
        int rank = start.getRank();

        byte startPiece = board[rank][file];

        int finish = Math.min(rank+limit, 7); // Limit the highest rank.

        // Start a square up.
        rank++;

        for (; rank <=finish ; rank++) {
            Position position = new Position(rank, file); // Increase the rank but keep the file the same.
            boolean traceOutcome = considerMove(viableMoves, board, moverColor, position, allowCaptures);
            if (!traceOutcome) {
                return viableMoves;
            }
        }

        return viableMoves;
    }
    public List<Position> traceDown(byte[][] board, Position start, int moverColor, int limit, boolean allowCaptures){
        List<Position> viableMoves = new ArrayList<>();

        int file = start.getFile();
        int rank = start.getRank();

        byte startPiece = board[rank][file];

        int finish = Math.max(rank-limit, 0); // Limit the lowest rank.

        // Begin a rank down.
        rank--;

        for (; rank >=finish ; rank--) {
            Position position = new Position(rank, file); // Decrease the rank but keep the file the same.
            boolean traceOutcome = considerMove(viableMoves, board, moverColor, position, allowCaptures);

            if (!traceOutcome) {
                return viableMoves;
            }
        }

        return viableMoves;
    }

    /**
     * Combines left, right, up and down.
     */
    public List<Position> traceRook(byte[][] board, Position start, int moverColor, int limit){
        List<Position> allTraces = new ArrayList<>();
        List<Position> traceResultL = traceLeft(board, start, moverColor, limit);
        List<Position> traceResultR = traceRight(board, start, moverColor, limit);
        List<Position> traceResultU = traceUp(board, start, moverColor, limit,true);
        List<Position> traceResultD = traceDown(board, start, moverColor, limit,true);

        allTraces.addAll(traceResultL);
        allTraces.addAll(traceResultR);
        allTraces.addAll(traceResultU);
        allTraces.addAll(traceResultD);

        return allTraces;
    }

    public List<Position> traceLDDiagonal(byte[][] board, Position start, int moverColor, int limit){
        List<Position> viableMoves = new ArrayList<>();

        int file = start.getFile();
        int rank = start.getRank(); // Begin 1 square below.

        byte startPiece = board[rank][file];

        int finish = Math.max(file-limit, 0); // Limit at least one of the dimensions.

        // Begin 1 square below and to the left.
        file--;
        rank--;

        for (; file >= finish && rank >= 0; file--, rank--) {
            Position position = new Position(rank, file); // Vary both the rank and file
            boolean traceOutcome = considerMove(viableMoves, board, moverColor, position,true);
            if (!traceOutcome) {
                return viableMoves;
            }
        }

        return viableMoves;
    }
    public List<Position> traceLUDiagonal(byte[][] board, Position start, int moverColor, int limit){
        List<Position> viableMoves = new ArrayList<>();

        int file = start.getFile();
        int rank = start.getRank();

        byte startPiece = board[rank][file];

        int finish = Math.max(file-limit, 0); // Limit at least one of the dimensions.

        // Begin 1 square above and to the left.
        file--;
        rank++;

        for (; file >= finish && rank <= 7; file--, rank++) {
            Position position = new Position(rank, file);
            boolean traceOutcome = considerMove(viableMoves, board, moverColor, position,true);
            if (!traceOutcome) {
                return viableMoves;
            }
        }

        return viableMoves;
    }
    public List<Position> traceRDDiagonal(byte[][] board, Position start, int moverColor, int limit){
        List<Position> viableMoves = new ArrayList<>();

        int file = start.getFile();
        int rank = start.getRank();

        byte startPiece = board[rank][file];

        int finish = Math.min(file+limit, 7); // Limit at least one of the dimensions.

        // Begin 1 square below and to the right.
        file++;
        rank--;

        for (; file <= finish && rank >= 0; file++, rank--) {
            Position position = new Position(rank, file);
            boolean traceOutcome = considerMove(viableMoves, board, moverColor, position,true);
            if (!traceOutcome) {
                return viableMoves;
            }
        }

        return viableMoves;
    }
    public List<Position> traceRUDiagonal(byte[][] board, Position start, int moverColor, int limit){
        List<Position> viableMoves = new ArrayList<>();

        int file = start.getFile();
        int rank = start.getRank();

        byte startPiece = board[rank][file];

        int finish = Math.min(file+limit, 7); // Limit at least one of the dimensions.

        // Begin 1 square above and to the right.
        file++;
        rank++;

        for (; file <= finish && rank <= 7; file++, rank++) {
            Position position = new Position(rank, file);
            boolean traceOutcome = considerMove(viableMoves, board, moverColor, position,true);
            if (!traceOutcome) {
                return viableMoves;
            }
        }

        return viableMoves;
    }

    /**
     * Combines all diagonals.
     */
    public List<Position> traceBishop(byte[][] board, Position start, int moverColor, int limit){
        List<Position> viableMoves = new ArrayList<>();
        viableMoves.addAll(traceLDDiagonal(board,start,moverColor,limit));
        viableMoves.addAll(traceLUDiagonal(board,start,moverColor,limit));
        viableMoves.addAll(traceRDDiagonal(board,start,moverColor,limit));
        viableMoves.addAll(traceRUDiagonal(board,start,moverColor,limit));

        return viableMoves;
    }

    /**
     * Combines all directions.
     */
    public List<Position> traceQueen(byte[][] board, Position start, int moverColor, int limit){
        List<Position> rookMoves = traceRook(board, start, moverColor, limit);
        List<Position> bishopMoves = traceBishop(board, start, moverColor, limit);
        List<Position> allMoves = new ArrayList<>();
        allMoves.addAll(rookMoves);
        allMoves.addAll(bishopMoves);

        return allMoves;
    }


    /**
     * Special pawn-trace functions.
     * Mark pawn attacks on empty squares as valid in case of en-passant,
     * the non-en-passant moves must be filtered out later.
     */
    public List<Position> tracePawn(Board boardObj, Position start, int color){
        if(color == ChessEngine.WHITE){
            return traceWhitePawn(boardObj, start);
        } else {
            return traceBlackPawn(boardObj, start);
        }
    }
    public List<Position> tracePawnAttacksWhite(byte[][] board, Position start){
        List<Position> attacks = new ArrayList<>();

        int rank = start.getRank();
        int file = start.getFile();

        if(file > 0) {
            // Consider up-left attack.
            // Reuse considerMove function to check if the attack is legal (or could be an en-passant, which will be confirmed later).
            considerMove(attacks,board,ChessEngine.WHITE, new Position(rank+1,file-1),true);
        }
        if(file < 7) {
            // Consider up-right attack.
            considerMove(attacks,board,ChessEngine.WHITE, new Position(rank+1,file+1),true);
        }

        /*if(!beingUsedForAKingCheck) {
            if (rank == 4) {
                // Consider en-passant on viable targets.
                // Left attack.
                if (file > 0) {
                    if (board[rank][file - 1] == Pieces.pawn_black) {
                        // Pawn on the same en-passantable rank. Evaluate.
                        if (isEnPassantable(boardObj, start, new Position(rank, file - 1), 0)) {
                            attacks.add(new Position(rank + 1, file - 1, true, true, false));
                        }
                    }
                }
                // Right attack.
                if (file < 7) {
                    if (board[rank][file + 1] == Pieces.pawn_black) {
                        // Pawn on the same en-passantable rank. Evaluate.
                        if (isEnPassantable(boardObj, start, new Position(rank, file + 1), 0)) {
                            attacks.add(new Position(rank + 1, file + 1, true, true,  false));
                        }
                    }
                }
            }
        }*/

        return attacks;
    }
    public List<Position> tracePawnAttacksBlack(byte[][] board, Position start){
        List<Position> attacks = new ArrayList<>();

        int rank = start.getRank();
        int file = start.getFile();

        if(file > 0) {
            // Consider down-left attack.
            // Reuse considerMove function to check if the attack is legal (or could be an en-passant, which will be confirmed later).
            considerMove(attacks,board,ChessEngine.BLACK, new Position(rank-1,file-1),true);
        }
        if(file < 7) {
            // Consider down-right attack.
            considerMove(attacks,board,ChessEngine.BLACK, new Position(rank-1,file+1),true);
        }

        /*if(!beingUsedForAKingCheck) {
            if (rank == 3) {
                // Consider en-passant on viable targets.
                // Left attack.
                if (file > 0) {
                    if (board[rank][file - 1] == Pieces.pawn_white) {
                        // Pawn on the same en-passantable rank. Evaluate.
                        if (isEnPassantable(boardObj, start, new Position(rank, file - 1), 1)) {
                            attacks.add(new Position(rank - 1, file - 1,true,true,false));
                        }
                    }
                }
                // Right attack.
                if (file < 7) {
                    if (board[rank][file + 1] == Pieces.pawn_white) {
                        // Pawn on the same en-passantable rank. Evaluate.
                        if (isEnPassantable(boardObj, start, new Position(rank, file + 1), 1)) {
                            attacks.add(new Position(rank - 1, file + 1,true,true,false));
                        }
                    }
                }
            }
        }*/
        return attacks;
    }

    public List<Position> traceWhitePawn(Board boardObj, Position position){
        int rank = position.getRank();

        byte[][] board = boardObj.getBoardRepresentation();
        int verticalCap = (rank==1) ? 2 : 1; // Pawns on their first move (on rank 1) are allowed to move an additional square forward.

        // White pawns go upwards.
        List<Position> pawnMoves = new ArrayList<>(traceUp(board, position, ChessEngine.WHITE, verticalCap,false));
        pawnMoves.addAll(tracePawnAttacksWhite(board,position));

        return pawnMoves;
    }
    public List<Position> traceBlackPawn(Board boardObj, Position position){
        int rank = position.getRank();

        byte[][] board = boardObj.getBoardRepresentation();
        int verticalCap = (rank==6) ? 2 : 1; // Pawns on their first move (on rank 6) are allowed to move an additional square forward.

        // Black pawns go down.
        List<Position> pawnMoves = new ArrayList<>(traceDown(board, position, ChessEngine.BLACK, verticalCap,false));
        pawnMoves.addAll(tracePawnAttacksBlack(board,position));
        return pawnMoves;
    }

    /**
     * Knights move in an L shape.
     * Total of 8 squares to check.
     *
     * Moves not in bounds or to positions with friendly pieces are removed.
     */
    public List<Position> traceKnight(byte[][] board, Position start, int knightColor){
        List<Position> viableMoves = new ArrayList<>();

        int xPos = start.getRank();
        int yPos = start.getFile();

        int x_a1 = xPos + 2;
        int y_a1 = yPos + 1;
        viableMoves.add(new Position(x_a1, y_a1));

        int x_a2 = xPos + 2;
        int y_a2 = yPos - 1;
        viableMoves.add(new Position(x_a2, y_a2));

        int x_b1 = xPos - 2;
        int y_b1 = yPos + 1;
        viableMoves.add(new Position(x_b1, y_b1));

        int x_b2 = xPos - 2;
        int y_b2 = yPos - 1;
        viableMoves.add(new Position(x_b2, y_b2));


        int x_c1 = xPos + 1;
        int y_c1 = yPos + 2;
        viableMoves.add(new Position(x_c1, y_c1));

        int x_c2 = xPos + 1;
        int y_c2 = yPos - 2;
        viableMoves.add(new Position(x_c2, y_c2));

        int x_d1 = xPos - 1;
        int y_d1 = yPos + 2;
        viableMoves.add(new Position(x_d1, y_d1));

        int x_d2 = xPos - 1;
        int y_d2 = yPos - 2;
        viableMoves.add(new Position(x_d2, y_d2));

        List<Position> actuallyViableMoves = new ArrayList<>();
        // Remove invalid knight moves.
        for (Position viableMove : viableMoves) {
            if(isInBounds(viableMove)) {
                byte dest = board[viableMove.getRank()][viableMove.getFile()];
                if(dest == 0){
                    actuallyViableMoves.add(viableMove);
                } else {
                    if(getColour(dest) != knightColor){
                        actuallyViableMoves.add(viableMove);
                    }
                }
            }
        }

        return actuallyViableMoves;
    }

    private boolean isInBounds(Position position){
        int x = position.getFile();
        int y = position.getRank();

        return (x <= 7 && x >=0) && (y <=7 && y >=0);
    }

    public int getColour(byte boardValue){
        return boardValue >> 3;
    }

    /**
     * Calculate king moves.
     * Base formula is a traceAll with a cap of 1 (0), but also takes into account castling rights.
     */
    /*public List<Position> traceKing(Board boardObj, Position kingLocation, int kingColor){
        byte[][] boardRepresentation = boardObj.getBoardRepresentation();

        List<Position> baseMoves = traceQueen(boardRepresentation, kingLocation, 1-1, false);

        // Check if castling moves are available.
        if(kingColor == 0){
            boolean canCastleKingside = canCastleKingsideWhite(boardObj, kingLocation);
            boolean canCastleQueenside = canCastleQueensideWhite(boardObj, kingLocation);

            if(canCastleKingside){
                // Define a special move
                baseMoves.add(new Position(0,6,false,false,true));
            }
            if(canCastleQueenside){
                baseMoves.add(new Position(0,2,false,false,true));
            }
        } else {
            boolean canCastleKingside = canCastleKingsideBlack(boardObj, kingLocation);
            boolean canCastleQueenside = canCastleQueensideBlack(boardObj, kingLocation);

            if(canCastleKingside){
                // Define a special move
                baseMoves.add(new Position(7,6,false,false,true));
            }
            if(canCastleQueenside){
                baseMoves.add(new Position(7,2,false,false,true));
            }
        }
        return baseMoves;
    }*/

    /*private boolean opposingPieceInRay(byte[][] board, List<Position> trace, byte piece, int originalColor){
        int opposingColor = originalColor^1;

        for (Position position : trace) {
            byte pieceInRay = board[position.getRank()][position.getFile()];

            if(((pieceInRay&0b0111) == piece) && getColour(pieceInRay) == opposingColor){
                return true;
            }
        }

        return false;
    }

    public String determineMoveMade(Board past, Board present){
        byte[][] pastRep = past.getBoardRepresentation();
        byte[][] presentRep = present.getBoardRepresentation();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pastRep.length; i++) {
            for (int j = 0; j < pastRep[0].length; j++) {
                if(pastRep[i][j] != presentRep[i][j]){
                    String rank = String.valueOf(i+1);
                    String file  = String.valueOf(((char) (j+97)));
                    sb.append(rank).append(file).append("-");
                }
            }
        }
        return sb.reverse().deleteCharAt(0).toString();
    }

    public boolean isSquareChecked(Board boardObj, Position square, int color){
        byte[][] board = boardObj.getBoardRepresentation();

        // Does inverse ray-traces for each piece, and if a corresponding piece of the opposing colour is found, the king is in check.
        List<Position> pawnTraces = tracePawn(boardObj, square, color, true);
        if(opposingPieceInRay(board, pawnTraces,Pieces.pawn_white,color)){
            return true;
        }
        List<Position> knightTraces = traceKnight(board, square, color);
        if(opposingPieceInRay(board, knightTraces,Pieces.knight_white,color)){
            return true;
        }

        List<Position> bishopTraces = traceBishop(board, square, 1337, true);
        if(opposingPieceInRay(board, bishopTraces,Pieces.bishop_white,color)){
            return true;
        }
        if(opposingPieceInRay(board, bishopTraces,Pieces.queen_white,color)){
            return true;
        }

        List<Position> rookTraces = traceRook(board, square, 1337, true);
        if(opposingPieceInRay(board, rookTraces,Pieces.rook_white,color)){
            return true;
        }
        if(opposingPieceInRay(board, rookTraces,Pieces.queen_white, color)){
            return true;
        }

        // Check for "attacking" kings.
        List<Position> kingTraces = traceQueen(board, square, 0, true);
        if(opposingPieceInRay(board, kingTraces, Pieces.king_white, color)){
            return true;
        }

        return false;
    }

    *//**
     * Go back through time to see if the piece was always here.
     *//*
    public boolean wasPieceAlwaysThere(Board boardObj, byte piece, Position position){
        Board parent = boardObj.getParentBoard();
        while (parent != null){
            byte[][] boardRepresentation = parent.getBoardRepresentation();
            if(boardRepresentation[position.getRank()][position.getFile()] != piece){
                return false;
            }
            parent = parent.getParentBoard();
        }
        return true;
    }

    public boolean canCastleKingsideWhite(Board boardObj, Position kingLocation){
        int kingRank = kingLocation.getRank();
        int kingFile = kingLocation.getFile();
        byte[][] board = boardObj.getBoardRepresentation();

        int backRank = 0; // 0 if white, 7 if black
        if(kingFile == 5-1){
            //System.out.println("Fulfilled check 1.");
            if(kingRank == backRank){
                //System.out.println("Fulfilled check 2.");
                // Check there is a rook in the right square...
                if(board[kingRank][7] == Pieces.rook_white){
                    //System.out.println("Fulfilled check 3.");
                    // ...and there are no pieces in the way...
                    if(board[kingRank][kingFile+1] == 0 && board[kingRank][kingFile+2] == 0){
                        //System.out.println("Fulfilled check 4.");
                        Position kingPosition = new Position(kingRank, kingFile);

                        Position kingMovesThrough1 = new Position(kingRank, kingFile+1);
                        Position kingMovesThrough2 = new Position(kingRank, kingFile+2);

                        Position rookPosition = new Position(kingRank, 7);
                        //...and the empty squares the king moves through isn't being checked...
                        if(!isSquareChecked(boardObj,kingPosition,0)
                                && !isSquareChecked(boardObj, kingMovesThrough1, 0)
                                && !isSquareChecked(boardObj, kingMovesThrough2, 0)){
                            //System.out.println("Fulfilled check 5.");
                            //...and neither the king or rook have moved in the past...
                            if(wasPieceAlwaysThere(boardObj,Pieces.king_white, kingPosition) &&
                                    wasPieceAlwaysThere(boardObj, Pieces.rook_white, rookPosition)){
                                //System.out.println("Fulfilled check 6 (final check).");
                                // Then yes, this is a legal castle.
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean canCastleKingsideBlack(Board boardObj, Position kingLocation){
        int kingRank = kingLocation.getRank();
        int kingFile = kingLocation.getFile();
        byte[][] board = boardObj.getBoardRepresentation();

        int backRank = 7; // 0 if white, 7 if black
        if(kingFile == 5-1){
            if(kingRank == backRank){
                // Check there is a rook in the right square...
                if(board[kingRank][7] == Pieces.rook_black){
                    // ...and there are no pieces in the way...
                    if(board[kingRank][kingFile+1] == 0 && board[kingRank][kingFile+2] == 0){
                        Position kingPosition = new Position(kingRank, kingFile);

                        Position kingMovesThrough1 = new Position(kingRank, kingFile+1);
                        Position kingMovesThrough2 = new Position(kingRank, kingFile+2);

                        Position rookPosition = new Position(kingRank, 7);
                        //...and the empty squares the king moves through isn't being checked...
                        if(!isSquareChecked(boardObj,kingPosition,1)
                                && !isSquareChecked(boardObj, kingMovesThrough1, 1)
                                && !isSquareChecked(boardObj, kingMovesThrough2, 1)){
                            //...and neither the king or rook have moved in the past...
                            if(wasPieceAlwaysThere(boardObj,Pieces.king_black, kingPosition) &&
                                    wasPieceAlwaysThere(boardObj, Pieces.rook_black, rookPosition)){
                                // Then yes, this is a legal castle.
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean canCastleQueensideWhite(Board boardObj, Position kingLocation){
        int kingRank = kingLocation.getRank();
        int kingFile = kingLocation.getFile();
        byte[][] board = boardObj.getBoardRepresentation();

        int backRank = 0; // 0 if white, 7 if black
        if(kingFile == 5-1){
            if(kingRank == backRank){
                // Check there is a rook in the right square...
                if(board[kingRank][0] == Pieces.rook_white){
                    // ...and there are no pieces in the way...
                    if(board[kingRank][kingFile-1] == 0
                            && board[kingRank][kingFile-2] == 0
                            && board[kingRank][kingFile-3] == 0){
                        Position kingPosition = new Position(kingRank, kingFile);
                        Position rookPosition = new Position(kingRank, 0);

                        Position kingMovesThrough1 = new Position(kingRank, kingFile-1);
                        Position kingMovesThrough2 = new Position(kingRank, kingFile-2);
                        //...and the empty squares the king moves through isn't being checked...
                        if(!isSquareChecked(boardObj,kingPosition,0)
                                && !isSquareChecked(boardObj,kingMovesThrough1,0)
                                && !isSquareChecked(boardObj,kingMovesThrough2,0)){
                            //...and neither the king or rook have moved in the past...
                            if(wasPieceAlwaysThere(boardObj,Pieces.king_white, kingPosition) &&
                                    wasPieceAlwaysThere(boardObj, Pieces.rook_white, rookPosition)){
                                // Then yes, this is a legal castle.
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean canCastleQueensideBlack(Board boardObj, Position kingLocation){
        int kingRank = kingLocation.getRank();
        int kingFile = kingLocation.getFile();
        byte[][] board = boardObj.getBoardRepresentation();

        int backRank = 7; // 0 if white, 7 if black
        if(kingFile == 5-1){
            if(kingRank == backRank){
                // Check there is a rook in the right square...
                if(board[kingRank][0] == Pieces.rook_black){
                    // ...and there are no pieces in the way...
                    if(board[kingRank][kingFile-1] == 0
                            && board[kingRank][kingFile-2] == 0
                            && board[kingRank][kingFile-3] == 0){
                        Position kingPosition = new Position(kingRank, kingFile);
                        Position rookPosition = new Position(kingRank, 0);

                        Position kingMovesThrough1 = new Position(kingRank, kingFile-1);
                        Position kingMovesThrough2 = new Position(kingRank, kingFile-2);
                        //...and the empty squares the king moves through isn't being checked...
                        if(!isSquareChecked(boardObj,kingPosition,1)
                                && !isSquareChecked(boardObj,kingMovesThrough1,1)
                                && !isSquareChecked(boardObj,kingMovesThrough2,1)){
                            //...and neither the king or rook have moved in the past...
                            if(wasPieceAlwaysThere(boardObj,Pieces.king_black, kingPosition) &&
                                    wasPieceAlwaysThere(boardObj, Pieces.rook_black, rookPosition)){
                                // Then yes, this is a legal castle.
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    *//**
     * Determines if an en-passant is possible by comparing the board the move prior to the current one to determine if this happened on the last move.
     *//*
    private boolean isEnPassantable(Board boardObj,
                                    Position attackingPawnPosition, Position victimPawnPosition,
                                    int attackerColor){
        int attackerRank = attackingPawnPosition.getRank();
        int attackerFile = attackingPawnPosition.getFile();

        int victimRank = victimPawnPosition.getRank();
        int victimFile = victimPawnPosition.getFile();

        try {
            if (attackerColor == 0) {
                // Check the victim pawn (black) was on the 7th rank last move.
                // Since only one black pawn can ever occupy the 7th rank, we just check if there was a black pawn there last move.
                return boardObj.getParentBoard().getBoardRepresentation()[7 - 1][victimFile] == Pieces.pawn_black;
            } else {
                // Check the victim pawn (white) was on the 2nd rank last move.
                return boardObj.getParentBoard().getBoardRepresentation()[2 - 1][victimFile] == Pieces.pawn_white;
            }
        } catch (NullPointerException n){
            return false;
        }
    }*/

}
