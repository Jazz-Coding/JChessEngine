package com.jazz.old.moves;

import com.jazz.engine.ChessEngine;
import com.jazz.old.Board;
import com.jazz.old.Position;
import com.jazz.old.Evaluator;

public class MoveCapture extends Move{
    private byte victim; // Keep track of the victim for potentially smarter ways of ordering moves.
    private boolean wasEnPassant;

    public MoveCapture(int pieceColour, byte piece, Position origin, Position destination, byte victim, boolean wasEnPassant) {
        super(pieceColour, piece, origin, destination);

        this.victim = victim;
        this.wasEnPassant = wasEnPassant;
    }

    @Override
    public Board executeMove(Board start) {
        // Regular captures already overwrite pieces.
        // En-passants also need to delete the pawn behind/in front of the destination.
        if(wasEnPassant){
            byte[][] newBoard = copyMove(start, origin.getRank(), origin.getFile(),
                    destination.getRank(), destination.getFile());
            if(pieceColour == ChessEngine.WHITE){
                // Erase the pawn behind.
                newBoard[destination.getRank()-1][destination.getFile()] = 0;
            } else {
                // Erase the pawn ahead.
                newBoard[destination.getRank()+1][destination.getFile()] = 0;
            }
            Board board = new Board(newBoard, start.getTurn());
            board.setParentBoard(start);
            board.switchTurn();

            return board;
        }
        return super.executeMove(start);
    }

    public boolean wasEnPassant() {
        return wasEnPassant;
    }

    @Override
    public float getValueEstimate() {
        float baseLine = 1F; // Bias towards looking at captures first.

        // Calculate the static exchange evaluation (SEE) of the capture.
        // This is the most we stand to lose by doing it.
        // i.e. pawn x queen has a very high SEE, which is good for us
        float see = Evaluator.getMaterialWorth(victim) - Evaluator.getMaterialWorth(piece);
        return baseLine + see;
    }

    @Override
    public String toString() {
        return Board.consoleString(piece) + "x" + destination;
    }

    @Override
    public int hashCode() {
        return Byte.hashCode(piece) ^ origin.hashCode() ^ destination.hashCode() ^ Byte.hashCode(victim);
    }
}
