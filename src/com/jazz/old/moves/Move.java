package com.jazz.old.moves;

import com.jazz.old.Board;
import com.jazz.old.Position;

public class Move {
    protected int pieceColour;
    protected byte piece;
    protected Position origin;
    protected Position destination;

    private static void arrayCopy(byte[][] src, byte[][] dst) {
        for (int i = 0; i < src.length; i++) {
            System.arraycopy(src[i], 0, dst[i], 0, src[i].length);
        }
    }

    public static byte[][] boardCopy(Board b){
        byte[][] src = b.getBoardRepresentation();
        byte[][] newB = new byte[src.length][src[0].length];
        arrayCopy(src,newB);

        return newB;
    }

    public Move(int pieceColour, byte piece, Position origin, Position destination) {
        this.pieceColour = pieceColour;
        this.piece = piece;
        this.origin = origin;
        this.destination = destination;
    }

    public Position getOrigin(){
        return origin;
    }
    public Position getDestination(){
        return destination;
    }

    public byte getPiece(){
        return piece;
    }
    public int getPieceColour(){
        return pieceColour;
    }

    public byte[][] copyMove(Board inputBoard,
                             int originRank, int originFile,
                             int destinationRank, int destinationFile){
        byte[][] currentBoard = boardCopy(inputBoard);

        currentBoard[destinationRank][destinationFile] = currentBoard[originRank][originFile];
        currentBoard[originRank][originFile] = 0;

        return currentBoard;
    }

    // Copy-make move strategy.
    public Board executeMove(Board start){
        byte[][] newBoard = copyMove(start, origin.getRank(), origin.getFile(),
                destination.getRank(), destination.getFile());

        Board board = new Board(newBoard, start.getTurn());
        board.setParentBoard(start);
        board.switchTurn();

        return board;
    }

    public float getValueEstimate(){
        return 0.0F;
    }

    @Override
    public String toString() {
        return Board.consoleString(piece) + destination;
    }

    @Override
    public int hashCode() {
        return Byte.hashCode(piece) ^ origin.hashCode() ^ destination.hashCode();
    }
}
