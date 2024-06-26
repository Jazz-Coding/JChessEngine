package com.jazz.old;

import com.jazz.engine.utils.bits.OldPieces;

public class CastleChecker {
    private Raytracer rt;

    public CastleChecker(Raytracer rt) {
        this.rt = rt;
    }

    private boolean wasPieceAlwaysThere(Board board, byte piece, Position location){
        Board parent = board.getParentBoard();
        while (parent != null){
            byte[][] boardRepresentation = parent.getBoardRepresentation();
            if(boardRepresentation[location.getRank()][location.getFile()] != piece){
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
            if(kingRank == backRank){
                // Check there is a rook in the right square...
                if(board[kingRank][7] == OldPieces.rook_white){
                    // ...and there are no pieces in the way...
                    if(board[kingRank][kingFile+1] == 0 && board[kingRank][kingFile+2] == 0){
                        Position kingPosition = new Position(kingRank, kingFile);

                        Position kingMovesThrough1 = new Position(kingRank, kingFile+1);
                        Position kingMovesThrough2 = new Position(kingRank, kingFile+2);

                        Position rookPosition = new Position(kingRank, 7);
                        //...and the empty squares the king moves through isn't being checked...
                        if(!CheckChecker.isSquareCheckedByBlack(rt, boardObj,kingPosition)
                                && !CheckChecker.isSquareCheckedByBlack(rt, boardObj, kingMovesThrough1)
                                && !CheckChecker.isSquareCheckedByBlack(rt, boardObj, kingMovesThrough2)){
                            //...and neither the king or rook have moved in the past...
                            if(wasPieceAlwaysThere(boardObj, OldPieces.king_white, kingPosition) &&
                                    wasPieceAlwaysThere(boardObj, OldPieces.rook_white, rookPosition)){
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
                if(board[kingRank][7] == OldPieces.rook_black){
                    // ...and there are no pieces in the way...
                    if(board[kingRank][kingFile+1] == 0 && board[kingRank][kingFile+2] == 0){
                        Position kingPosition = new Position(kingRank, kingFile);

                        Position kingMovesThrough1 = new Position(kingRank, kingFile+1);
                        Position kingMovesThrough2 = new Position(kingRank, kingFile+2);

                        Position rookPosition = new Position(kingRank, 7);
                        //...and the empty squares the king moves through isn't being checked...
                        if(!CheckChecker.isSquareCheckedByWhite(rt, boardObj,kingPosition)
                                && !CheckChecker.isSquareCheckedByWhite(rt, boardObj, kingMovesThrough1)
                                && !CheckChecker.isSquareCheckedByWhite(rt, boardObj, kingMovesThrough2)){
                            //...and neither the king or rook have moved in the past...
                            if(wasPieceAlwaysThere(boardObj, OldPieces.king_black, kingPosition) &&
                                    wasPieceAlwaysThere(boardObj, OldPieces.rook_black, rookPosition)){
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
                if(board[kingRank][0] == OldPieces.rook_white){
                    // ...and there are no pieces in the way...
                    if(board[kingRank][kingFile-1] == 0
                            && board[kingRank][kingFile-2] == 0
                            && board[kingRank][kingFile-3] == 0){
                        Position kingPosition = new Position(kingRank, kingFile);
                        Position rookPosition = new Position(kingRank, 0);

                        Position kingMovesThrough1 = new Position(kingRank, kingFile-1);
                        Position kingMovesThrough2 = new Position(kingRank, kingFile-2);
                        //...and the empty squares the king moves through isn't being checked...
                        if(!CheckChecker.isSquareCheckedByBlack(rt, boardObj,kingPosition)
                                && !CheckChecker.isSquareCheckedByBlack(rt, boardObj,kingMovesThrough1)
                                && !CheckChecker.isSquareCheckedByBlack(rt, boardObj,kingMovesThrough2)){
                            //...and neither the king or rook have moved in the past...
                            if(wasPieceAlwaysThere(boardObj, OldPieces.king_white, kingPosition) &&
                                    wasPieceAlwaysThere(boardObj, OldPieces.rook_white, rookPosition)){
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
                if(board[kingRank][0] == OldPieces.rook_black){
                    // ...and there are no pieces in the way...
                    if(board[kingRank][kingFile-1] == 0
                            && board[kingRank][kingFile-2] == 0
                            && board[kingRank][kingFile-3] == 0){
                        Position kingPosition = new Position(kingRank, kingFile);
                        Position rookPosition = new Position(kingRank, 0);

                        Position kingMovesThrough1 = new Position(kingRank, kingFile-1);
                        Position kingMovesThrough2 = new Position(kingRank, kingFile-2);
                        //...and the empty squares the king moves through isn't being checked...
                        if(!CheckChecker.isSquareCheckedByWhite(rt, boardObj,kingPosition)
                                && !CheckChecker.isSquareCheckedByWhite(rt, boardObj,kingMovesThrough1)
                                && !CheckChecker.isSquareCheckedByWhite(rt, boardObj,kingMovesThrough2)){
                            //...and neither the king or rook have moved in the past...
                            if(wasPieceAlwaysThere(boardObj, OldPieces.king_black, kingPosition) &&
                                    wasPieceAlwaysThere(boardObj, OldPieces.rook_black, rookPosition)){
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
}
