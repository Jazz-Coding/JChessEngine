package com.jazz.old.moves;

import com.jazz.engine.ChessEngine;
import com.jazz.old.Board;
import com.jazz.old.Position;
import com.jazz.engine.utils.bits.OldPieces;

public class MoveCastle extends Move {
    private Position rookPosition;

    public MoveCastle(int pieceColour, byte piece, Position origin, Position destination, Position rookPosition) {
        super(pieceColour, piece, origin, destination);
        this.rookPosition = rookPosition;
    }

    @Override
    public Board executeMove(Board start) {
        byte[][] newBoard = copyMove(start, origin.getRank(), origin.getFile(),
                destination.getRank(), destination.getFile());

        // Castles also move the rook that was involved.

        boolean whiteCastled = pieceColour == ChessEngine.WHITE;
        byte rookPiece = whiteCastled ? OldPieces.rook_white : OldPieces.rook_black;

        // Move the rook to the right square.
        if(destination.getFile() == 6){
            // Kingside castling.
            // Move the kingside rook to the left square.
            newBoard[rookPosition.getRank()][7] = 0;
            newBoard[rookPosition.getRank()][5] = rookPiece;
        } else {
            // Queenside castling.
            // Move the queenside rook to the right square.
            newBoard[rookPosition.getRank()][0] = 0;
            newBoard[rookPosition.getRank()][3] = rookPiece;
        }

        Board board = new Board(newBoard, start.getTurn());
        board.setParentBoard(start);
        board.switchTurn();

        return board;
    }

    @Override
    public String toString() {
        if(rookPosition.getFile() == 0){
            return "O - O - O";
        } else {
            return "O - O";
        }
    }
}
