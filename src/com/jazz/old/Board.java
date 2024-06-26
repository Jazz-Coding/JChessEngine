package com.jazz.old;

import com.jazz.engine.gui.components.Piece;
import com.jazz.engine.utils.HashUtils;
import com.jazz.engine.utils.bits.OldPieces;

import java.util.*;

import static com.jazz.engine.ChessEngine.*;
import static com.jazz.engine.data.Pieces.*;

public class Board {
    public static boolean CONSOLE_DARK_MODE = true;

    Board parentBoard;
    private List<Board> children = new ArrayList<>();

    private float evaluation;

    private int turn;

    byte[][] boardRepresentation;
    private boolean[] castlingRights = new boolean[4];
    private long enPassantable = 0L;

    private int halfMoveClock = 0;
    private int fullMoveClock = 0;

    public Board(byte[][] boardRepresentation, int turn) {
        this.boardRepresentation = boardRepresentation;
        this.turn = turn;
    }

    private static void arrayCopy(byte[][] src, byte[][] dst) {
        for (int i = 0; i < src.length; i++) {
            System.arraycopy(src[i], 0, dst[i], 0, src[i].length);
        }
    }

    public static byte[][] boardCopy(Board b) {
        byte[][] src = b.getBoardRepresentation();
        byte[][] newB = new byte[src.length][src[0].length];
        arrayCopy(src, newB);

        return newB;
    }


    public static Board empty() {
        return new Board(new byte[8][8],0);
    }

    public static byte oldToNew(byte piece){
        switch (piece){
            case OldPieces.pawn_white, OldPieces.pawn_black -> {return pawn;}
            case OldPieces.knight_white, OldPieces.knight_black -> {return knight;}
            case OldPieces.bishop_white , OldPieces.bishop_black -> {return bishop;}
            case OldPieces.rook_white, OldPieces.rook_black -> {return rook;}
            case OldPieces.queen_white, OldPieces.queen_black -> {return queen;}
            case OldPieces.king_white, OldPieces.king_black -> {return king;}
            default -> {return empty;}
        }
    }

    public static int oldColor(byte piece){
        if((piece>>3==0)){
            return WHITE;
        } else {
            return BLACK;
        }
    }

    public static Board base() {
        byte[][] boardRepresentation = new byte[8][8];

        // Add pawns on rows 2 and 7.
        for (int i = 0; i < 8; i++) {
            boardRepresentation[1][i] = OldPieces.pawn_white;
            boardRepresentation[6][i] = OldPieces.pawn_black;
        }

        // Add other pieces to rows 1 and 8.
        boardRepresentation[0][0] = OldPieces.rook_white;
        boardRepresentation[0][1] = OldPieces.knight_white;
        boardRepresentation[0][2] = OldPieces.bishop_white;
        boardRepresentation[0][3] = OldPieces.queen_white;
        boardRepresentation[0][4] = OldPieces.king_white;
        boardRepresentation[0][5] = OldPieces.bishop_white;
        boardRepresentation[0][6] = OldPieces.knight_white;
        boardRepresentation[0][7] = OldPieces.rook_white;

        boardRepresentation[7][0] = OldPieces.rook_black;
        boardRepresentation[7][1] = OldPieces.knight_black;
        boardRepresentation[7][2] = OldPieces.bishop_black;
        boardRepresentation[7][3] = OldPieces.queen_black;
        boardRepresentation[7][4] = OldPieces.king_black;
        boardRepresentation[7][5] = OldPieces.bishop_black;
        boardRepresentation[7][6] = OldPieces.knight_black;
        boardRepresentation[7][7] = OldPieces.rook_black;

        Board b = new Board(boardRepresentation, 0);
        b.setCastlingRights(new boolean[]{true, true, true, true});

        return b;
    }
    public static Board fromFEN(String FEN) {
        byte[][] board = new byte[8][8];

        String[] components = FEN.split(" ");

        String boardLayout = components[0];
        String[] rows = boardLayout.split("/");
        for (int i = 0; i < rows.length; i++) {
            String row = rows[rows.length-1-i];

            int rowIndex = 0;
            for (int j = 0; rowIndex < row.length(); j++, rowIndex++) {
                char c = row.charAt(rowIndex);
                if (Character.isAlphabetic(c)) {
                    switch (c) {
                        case 'r' -> board[i][j] = OldPieces.rook_black;
                        case 'n' -> board[i][j] = OldPieces.knight_black;
                        case 'b' -> board[i][j] = OldPieces.bishop_black;
                        case 'k' -> board[i][j] = OldPieces.king_black;
                        case 'q' -> board[i][j] = OldPieces.queen_black;
                        case 'p' -> board[i][j] = OldPieces.pawn_black;

                        case 'R' -> board[i][j] = OldPieces.rook_white;
                        case 'N' -> board[i][j] = OldPieces.knight_white;
                        case 'B' -> board[i][j] = OldPieces.bishop_white;
                        case 'K' -> board[i][j] = OldPieces.king_white;
                        case 'Q' -> board[i][j] = OldPieces.queen_white;
                        case 'P' -> board[i][j] = OldPieces.pawn_white;
                    }
                } else {
                    String s = String.valueOf(c);
                    int digit = Integer.parseInt(s);
                    j += digit-1;
                }
            }
        }

        int turn = components[1].equals("w") ? 0 : 1;

        boolean whiteKSCastle = components[2].contains("K");
        boolean whiteQSCastle = components[2].contains("Q");
        boolean blackKSCastle = components[2].contains("k");
        boolean blackQSCastle = components[2].contains("q");
        long enPassantable = 0L;

        if(!components[3].equals("-")){
            // En-passant square specified.
            int file = ((int) components[3].charAt(0)) - 97;
            int rank = Integer.parseInt(String.valueOf(components[3].charAt(1)))-1;

            int ordinal = rank*8 + file;
            enPassantable = 1L << ordinal;
        }

        int halfMoveClock = 0;
        if(components.length > 4 && !components[4].equals("-")) {
            halfMoveClock = Integer.parseInt(components[4]);
        }

        int fullMoveClock = 0;
        if(components.length > 5 && !components[5].equals("-")) {
            fullMoveClock = Integer.parseInt(components[5]);
        }

        Board b = new Board(board, turn);
        b.setCastlingRights(new boolean[]{whiteKSCastle, whiteQSCastle, blackKSCastle, blackQSCastle});
        b.setEnPassantable(enPassantable);
        b.setHalfMoveClock(halfMoveClock);
        b.setFullMoveClock(fullMoveClock);

        return b;
    }

    public boolean[] getCastlingRights() {
        return castlingRights;
    }

    public long getEnPassantable() {
        return enPassantable;
    }

    public int getHalfMoveClock() {
        return halfMoveClock;
    }

    public int getFullMoveClock() {
        return fullMoveClock;
    }

    public void setCastlingRights(boolean[] castlingRights) {
        this.castlingRights = castlingRights;
    }

    public void setEnPassantable(long enPassantable) {
        this.enPassantable = enPassantable;
    }

    public void setHalfMoveClock(int halfMoveClock) {
        this.halfMoveClock = halfMoveClock;
    }

    public void setFullMoveClock(int fullMoveClock) {
        this.fullMoveClock = fullMoveClock;
    }

    public byte[][] getBoardRepresentation() {
        return boardRepresentation;
    }

    public Board placePiece(byte piece, Position position) {
        int rank = position.getRank();
        int file = position.getFile();

        byte[][] boardRepresentation = boardCopy(this);
        boardRepresentation[rank][file] = piece;

        return new Board(boardRepresentation, turn);
    }

    public Board getParentBoard() {
        return parentBoard;
    }

    public void setParentBoard(Board parentBoard) {
        this.parentBoard = parentBoard;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Go backwards during string formation since the rows go from 8-1 instead of 1-8.
        for (int rowIndex = boardRepresentation.length - 1; rowIndex >= 0; rowIndex--) {
            byte[] row = boardRepresentation[rowIndex];
            //String line = String.format("%1s %1s %1s %1s %1s %1s %1s %1s", pieceString(row[0]), pieceString(row[1]), pieceString(row[2]), pieceString(row[3]), pieceString(row[4]), pieceString(row[5]), pieceString(row[6]), pieceString(row[7]));
            StringBuilder rowSB = new StringBuilder();
            for (byte pieceBitString : row) {
                byte modified = pieceBitString;
                if (CONSOLE_DARK_MODE && (modified != 0b1111)) {
                    modified ^= 1 << 3;
                }
                String s = pieceString(modified);
                //rowSB.append(s);
                if (s.equals(".") || s.equals("#") || s.equals("x")) {
                    rowSB.append(" ☐ ");
                } else {
                    rowSB.append(" " + s + " ");
                }
            }

            sb.append(rowSB).append("\n");
        }
        return sb.toString();
    }

    public static String consoleString(byte piece){
        byte modified = piece;
        if (CONSOLE_DARK_MODE && (modified != 0b1111)) {
            modified ^= 1 << 3;
        }
        String s = pieceString(modified);
        return s;
    }

    public static Map<Byte, Piece> bytePieceMap(){
        Map<Byte,Piece> bytePieceMap = new HashMap<>();
        bytePieceMap.put((byte) 0b0001,new Piece("Pawn","White", (byte) 0b0001,"♟"));
        bytePieceMap.put((byte) 0b1001,new Piece("Pawn","Black", (byte) 0b1001,"♟"));

        bytePieceMap.put((byte) 0b0010,new Piece("Knight","White", (byte) 0b0010,"♞"));
        bytePieceMap.put((byte) 0b1010,new Piece("Knight","Black", (byte) 0b1010,"♞"));

        bytePieceMap.put((byte) 0b0011,new Piece("Bishop","White", (byte) 0b0011,"♝"));
        bytePieceMap.put((byte) 0b1011,new Piece("Bishop","Black", (byte) 0b1011,"♝"));

        bytePieceMap.put((byte) 0b0100,new Piece("Rook","White", (byte) 0b0100,"♜"));
        bytePieceMap.put((byte) 0b1100,new Piece("Rook","Black", (byte) 0b1100,"♜"));

        bytePieceMap.put((byte) 0b0101,new Piece("Queen","White", (byte) 0b0101,"♛"));
        bytePieceMap.put((byte) 0b1101,new Piece("Queen","Black", (byte) 0b1101,"♛"));

        bytePieceMap.put((byte) 0b0110,new Piece("King","White", (byte) 0b0110,"♚"));
        bytePieceMap.put((byte) 0b1110,new Piece("King","Black", (byte) 0b1110,"♚"));

        bytePieceMap.put((byte) 0b1111,new Piece("Empty","", (byte) 0b1111,""));
        bytePieceMap.put((byte) 0b0000,new Piece("Empty","", (byte) 0b0000,""));
        //bytePieceMap.put((byte) 0b1110,new Piece("King","Black", (byte) 0b1110,"♚"));

        return bytePieceMap;
    }
    public static String pieceString(byte piece) {
        switch (piece) {
            case 0b0001 -> {
                return "♙";
            }
            case 0b1001 -> {
                return "♟";
            }
            case 0b0010 -> {
                return "♘";
            }
            case 0b1010 -> {
                return "♞";
            }
            case 0b0011 -> {
                return "♗";
            }
            case 0b1011 -> {
                return "♝";
            }
            case 0b0100 -> {
                return "♖";
            }
            case 0b1100 -> {
                return "♜";
            }
            case 0b0101 -> {
                return "♕";
            }
            case 0b1101 -> {
                return "♛";
            }
            case 0b0110 -> {
                return "♔";
            }
            case 0b1110 -> {
                return "♚";
            }
            case 0b1111 -> {
                return "x";
            }
            default -> {
                return ".";
            }
        }
    }

    public void switchTurn(){
        this.turn^=1;
    }

    public int getTurn() {
        return turn;
    }

    public Board visualizeRays(List<Position> rayMoves) {
        byte[][] boardRepresentation = boardCopy(this);

        for (Position move : rayMoves) {
            int rank = move.getRank();
            int file = move.getFile();


            boardRepresentation[rank][file] = 0b1111; // Special visualization character.
        }

        return new Board(boardRepresentation, turn);
    }

    @Override
    public boolean equals(Object obj) {
        Board otherBoard = (Board) obj;
        return otherBoard.hash() == this.hash();
    }

    public long hash() {
        return HashUtils.hash(boardRepresentation, turn);
    }

    public int cardinality(){
        int sum = 0;
        for (byte[] bytes : boardRepresentation) {
            for (byte piece : bytes) {
                if(piece != 0){
                    sum ++;
                }
            }
        }
        return sum;
    }
}
