package com.jazz.old.moves;

import com.jazz.old.Board;
import com.jazz.old.Position;
import com.jazz.old.Evaluator;
import com.jazz.engine.utils.bits.OldPieces;

public class MovePromotion extends Move{
    private byte victim;
    private byte promotesTo;

    public MovePromotion(int pieceColour, byte piece, Position origin, Position destination, byte promotesTo, byte victim) {
        super(pieceColour, piece, origin, destination);
        this.promotesTo = promotesTo;
        this.victim = victim;
    }

    @Override
    public Board executeMove(Board start) {
        byte[][] newBoard = copyMove(start, origin.getRank(), origin.getFile(),
                destination.getRank(), destination.getFile());

        // Promotions replace the piece at the destination with another.
        newBoard[destination.getRank()][destination.getFile()] = promotesTo;

        Board board = new Board(newBoard, start.getTurn());
        board.setParentBoard(start);
        board.switchTurn();

        return board;
    }

    public byte getVictim() {
        return victim;
    }

    @Override
    public float getValueEstimate() {
        float see = 0;
        if(victim != OldPieces.nothing){
            see = Evaluator.getMaterialWorth(victim) - Evaluator.getMaterialWorth(piece);
        }
        // Higher value promotions are probably worth exploring first.
        return Evaluator.getMaterialWorth(promotesTo) + see;
    }

    @Override
    public String toString() {
        return origin + "=" + Board.consoleString(promotesTo);
    }
}
