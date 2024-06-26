package com.jazz.engine.data;

import com.jazz.engine.utils.HashUtils;
import com.jazz.engine.utils.bits.LongParser;
import com.jazz.old.Board;
import com.jazz.engine.utils.bits.OldPieces;

import java.util.Arrays;

import static com.jazz.engine.ChessEngine.*;
import static com.jazz.engine.utils.bits.BBUtils.ordinalToSqr;

/**
 * Bitboard implementation.
 *
 * A chess board has 64 squares. Conveniently, a single "long" has 64 bits. Thus a single long can answer questions about the entire chess board.
 * e.g. where are all the white pawns?
 *
 * pieceBBs[WHITE][pawn] =
 *  00000000
 *  00000000
 *  00000000
 *  00000000
 *  00000000
 *  00000000
 *  11111111
 *  00000000
 *
 *  We can then update all of this information in a single bitwise operation.
 *  (e.g. using XOR with a "destination" long will erase the original pawn location and create a new one)
 *
 *  These bitwise operations dramatically improve performance compared to alternative solutions, at the cost of some memory overhead.
 *  To get the best of both worlds, we constantly update the state and revert our changes afterwards using the following recursive paradigm:
 *
 *         -->
 *  makeMove()
 *  // look at this new position recursively
 *  unmakeMove()
 *  <--
 *
 *  This results in a memory consumption proportional to the depth of the game tree, we only ever keep one game state in memory, plus the recursive call stack.
 *  i.e. a spatial complexity of O(log_2(n)) where n is the total size of the game tree, or O(n) where n is the depth.
 *
 *  A linear increase with depth easily makes the computation time-bound instead of memory bound. So the only cost
 *  of this method is less intuitive code.
 */
public class BBs {
    public long hash = 0; // The Zobrist hash of the position. See HashUtils.java for a detailed explannation.
    public int color; // The player to move.

    public long[][] pieceBBs; // Piece-presence boards. i.e. the precise location of ALL white/black pawns/knights/bishops/rooks/queens/king

    public long squaresOccupied; // squares with pieces
    public long emptySquares; // squares without pieces
    public long[] occupiedByColor; // squares with white/black pieces
    public long[] capturableByColor; // valid white/black attacks

    public int[][] movesSinceDblPwnPush = new int[2][8];
    public long[] enPassantable;

    public byte[] byteBoard; // A more bulky board representation that allows for type-checking. i.e. what is the type of piece I'm capturing with hxe5?
    public int[] castlingRights = new int[]{3,3}; // white/black castling rights, 0 = no castles, 1 = just kingside, 2 = just queenside, 3 = all castles available

    public static BBs from(Board board){
        BBs bbs = new BBs();
        bbs.pieceBBs = new long[2][6];

        // Frequently reused bitboards.
        bbs.squaresOccupied = 0L;
        bbs.emptySquares = 0L;

        bbs.occupiedByColor = new long[2];
        bbs.capturableByColor = new long[2];

        bbs.enPassantable = new long[2];

        bbs.castlingRights = new int[]{3,3}; // TODO: get from FEN

        int turn = board.getTurn();
        bbs.color = turn;

        bbs.enPassantable[turn^1] = board.getEnPassantable();

        // Initialize byte board to -1s to distinguish empty squares from white pawns (0s).
        bbs.byteBoard = new byte[64];
        Arrays.fill(bbs.byteBoard, (byte) Pieces.empty);

        byte[][] bytes = board.getBoardRepresentation();
        for (int rank = 0; rank < bytes.length; rank++) {
            for (int file = 0; file < bytes[0].length; file++) {
                int ordinal = rank*8+file;
                byte piece = bytes[rank][file];
                switch (piece){
                    case OldPieces.pawn_white -> {
                        bbs.pieceBBs[WHITE][Pieces.pawn] |= 1L << ordinal;
                        bbs.byteBoard[ordinal] = Pieces.pawn;}
                    case OldPieces.pawn_black -> {
                        bbs.pieceBBs[BLACK][Pieces.pawn] |= 1L << ordinal;
                        bbs.byteBoard[ordinal] = Pieces.pawn;}
                    case OldPieces.knight_white -> {
                        bbs.pieceBBs[WHITE][Pieces.knight] |= 1L << ordinal;
                        bbs.byteBoard[ordinal] = Pieces.knight;}
                    case OldPieces.knight_black -> {
                        bbs.pieceBBs[BLACK][Pieces.knight] |= 1L << ordinal;
                        bbs.byteBoard[ordinal] = Pieces.knight;}
                    case OldPieces.bishop_white -> {
                        bbs.pieceBBs[WHITE][Pieces.bishop] |= 1L << ordinal;
                        bbs.byteBoard[ordinal] = Pieces.bishop;}
                    case OldPieces.bishop_black -> {
                        bbs.pieceBBs[BLACK][Pieces.bishop] |= 1L << ordinal;
                        bbs.byteBoard[ordinal] = Pieces.bishop;}
                    case OldPieces.rook_white -> {
                        bbs.pieceBBs[WHITE][Pieces.rook] |= 1L << ordinal;
                        bbs.byteBoard[ordinal] = Pieces.rook;}
                    case OldPieces.rook_black -> {
                        bbs.pieceBBs[BLACK][Pieces.rook] |= 1L << ordinal;
                        bbs.byteBoard[ordinal] = Pieces.rook;}
                    case OldPieces.queen_white -> {
                        bbs.pieceBBs[WHITE][Pieces.queen] |= 1L << ordinal;
                        bbs.byteBoard[ordinal] = Pieces.queen;}
                    case OldPieces.queen_black -> {
                        bbs.pieceBBs[BLACK][Pieces.queen] |= 1L << ordinal;
                        bbs.byteBoard[ordinal] = Pieces.queen;}
                    case OldPieces.king_white -> {
                        bbs.pieceBBs[WHITE][Pieces.king] |= 1L << ordinal;
                        bbs.byteBoard[ordinal] = Pieces.king;}
                    case OldPieces.king_black -> {
                        bbs.pieceBBs[BLACK][Pieces.king] |= 1L << ordinal;
                        bbs.byteBoard[ordinal] = Pieces.king;
                    }
                }
            }
        }

        bbs.occupiedByColor[WHITE] |= bbs.pieceBBs[WHITE][Pieces.pawn];
        bbs.occupiedByColor[WHITE] |= bbs.pieceBBs[WHITE][Pieces.knight];
        bbs.occupiedByColor[WHITE] |= bbs.pieceBBs[WHITE][Pieces.bishop];
        bbs.occupiedByColor[WHITE] |= bbs.pieceBBs[WHITE][Pieces.rook];
        bbs.occupiedByColor[WHITE] |= bbs.pieceBBs[WHITE][Pieces.queen];
        bbs.occupiedByColor[WHITE] |= bbs.pieceBBs[WHITE][Pieces.king];

        bbs.occupiedByColor[BLACK] |= bbs.pieceBBs[BLACK][Pieces.pawn];
        bbs.occupiedByColor[BLACK] |= bbs.pieceBBs[BLACK][Pieces.knight];
        bbs.occupiedByColor[BLACK] |= bbs.pieceBBs[BLACK][Pieces.bishop];
        bbs.occupiedByColor[BLACK] |= bbs.pieceBBs[BLACK][Pieces.rook];
        bbs.occupiedByColor[BLACK] |= bbs.pieceBBs[BLACK][Pieces.queen];
        bbs.occupiedByColor[BLACK] |= bbs.pieceBBs[BLACK][Pieces.king];

        bbs.squaresOccupied = (bbs.occupiedByColor[WHITE] | bbs.occupiedByColor[BLACK]);
        bbs.emptySquares = ~bbs.squaresOccupied;

        bbs.capturableByColor[BLACK] = ~bbs.occupiedByColor[BLACK];
        bbs.capturableByColor[WHITE] = ~bbs.occupiedByColor[WHITE];

        // Update the hash.
        bbs.hash = HashUtils.hash(bbs.pieceBBs, turn);

        return bbs;
    }


    public void printBreakDown(){
        System.out.println("Occupied by white:");
        System.out.println(LongParser.printable(occupiedByColor[WHITE]));
        System.out.println("Occupied by black:");
        System.out.println(LongParser.printable(occupiedByColor[BLACK]));
        System.out.println("Occupied in general: ");
        System.out.println(LongParser.printable(squaresOccupied));
        System.out.println("Empty in general: ");
        System.out.println(LongParser.printable(emptySquares));
    }
    public Board toBoard(){
        byte[][] pretty = new byte[8][8];
        /*for (int pieceColor = 0; pieceColor < 2; pieceColor++) {
            for (byte pieceByte = 0; pieceByte < 6; pieceByte++) {
                long piece = pieceBBs[pieceColor][pieceByte];

                while (piece != 0 ){ // Bitwise iteration.
                    long bit = piece & -piece; // Index of a single piece.

                    int ord = sqrToOrdinal(bit); // Ordinal of a single piece.
                    int rank = ord/8;
                    int file = ord%8;

                    pretty[rank][file] = toLongByteFormat(pieceByte,pieceColor);


                    piece &= piece-1;
                }
            }
        }*/

        for (int ord = 0; ord < 64; ord++) {
            byte pieceByte = byteBoard[ord];
            int rank = ord/8;
            int file = ord%8;

            boolean isWhite = (ordinalToSqr(ord) & occupiedByColor[WHITE]) != 0;
            int color = isWhite ? WHITE : BLACK;

            pretty[rank][file] = toLongByteFormat(pieceByte,color);
        }

        return new Board(pretty, 0);
    }

    private static byte toLongByteFormat(byte shortByteFormat, int color){
        if(shortByteFormat==6) return 0; // empty = 0 in the old format
        if(color==WHITE){
            return (byte) (shortByteFormat+1);
        } else {
            return (byte) (shortByteFormat+1+8);
        }
    }
    public static BBs duplicate(BBs existing){
        BBs copy = new BBs();
        copy.hash=existing.hash;
        copy.color=existing.color;

        copy.pieceBBs = new long[2][6];
        for (int i = 0; i < 2; i++) {
            System.arraycopy(existing.pieceBBs[i], 0, copy.pieceBBs[i], 0, 6);
        }

        copy.squaresOccupied = existing.squaresOccupied;
        copy.emptySquares = existing.emptySquares;

        copy.occupiedByColor = new long[2];
        System.arraycopy(existing.occupiedByColor,0,copy.occupiedByColor,0,2);

        copy.capturableByColor = new long[2];
        System.arraycopy(existing.capturableByColor,0,copy.capturableByColor,0,2);

        copy.enPassantable = new long[2];
        System.arraycopy(existing.enPassantable,0,copy.enPassantable,0,2);

        copy.byteBoard = new byte[64];
        System.arraycopy(existing.byteBoard,0,copy.byteBoard,0,64);

        copy.castlingRights = new int[2];
        System.arraycopy(existing.castlingRights,0,copy.castlingRights,0,2);

        return copy;
    }
}
