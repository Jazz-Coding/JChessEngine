package com.jazz.engine.data;

import com.jazz.engine.ChessEngine;
import com.jazz.old.Board;
import com.jazz.old.Position;
import com.jazz.old.Raytracer;
import com.jazz.engine.utils.bits.OldPieces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.jazz.engine.ChessEngine.*;
import static com.jazz.engine.utils.bits.BBUtils.sqrToOrdinal;


/**
 * Interface between the older techniques and the newer precomputed tables.
 */
public class Precomputer {
    private Raytracer rt;

    public Precomputer() {
        this.rt = new Raytracer();
    }

    private void putWPawnAttack(long location, long attack){
        int pord = sqrToOrdinal(location);
        PrecomputedBitboards.precomputedPawnAttacks[WHITE][pord] = attack;
    }

    private void putBPawnAttack(long location, long attack){
        int pord = sqrToOrdinal(location);
        PrecomputedBitboards.precomputedPawnAttacks[BLACK][pord] = attack;
    }

    private void putKnightAttack(long location, long attack){
        int pord = sqrToOrdinal(location);
        PrecomputedBitboards.precomputedKnightAttacks[pord] = attack;
    }

    private void putKingAttack(long location, long attack){
        int pord = sqrToOrdinal(location);
        PrecomputedBitboards.precomputedKingAttacks[pord] = attack;
    }

    public void precomputeAttacks(){
        // White pawns.
        for (int rank = 0; rank <= 7; rank++) {
            int ordinal = rank*8 + 0;
            long location = 1L << ordinal;
            long attack = location << 9;

            putWPawnAttack(location, attack);

            for (int file = 1; file <= 6; file++) {
                ordinal = rank*8 + file;

                location = 1L << ordinal;
                long attacks = (location << 7) | (location << 9);
                putWPawnAttack(location, attacks);
            }

            ordinal = rank*8 + 7;
            location = 1L << ordinal;
            attack = location << 7;
            putWPawnAttack(location, attack);
        }

        // Black pawns.
        for (int rank = 0; rank <= 7; rank++) {
            int ordinal = rank*8 + 0;
            long location = 1L << ordinal;
            long attack = location >> 7;
            putBPawnAttack(location, attack);

            for (int file = 1; file <= 6; file++) {
                ordinal = rank*8 + file;

                location = 1L << ordinal;
                long attacks = location >> 9 | location >> 7;
                putBPawnAttack(location, attacks);
            }

            ordinal = rank*8 + 7;
            location = 1L << ordinal;
            attack = location >> 9;
            putBPawnAttack(location, attack);
        }


        // Knights.
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                int baseOrdinal = rank*8 + file;
                long baseLocation = 1L << baseOrdinal;

                long base = 0;

                int x_a1 = rank + 2;
                int y_a1 = file + 1;
                if(x_a1 < 8 && y_a1 < 8){
                    int ordinal = x_a1*8 + y_a1;
                    base |= 1L << ordinal;
                }

                int x_a2 = rank + 2;
                int y_a2 = file - 1;
                if(x_a2 < 8 && y_a2 >=0){
                    int ordinal = x_a2*8 + y_a2;
                    base |= 1L << ordinal;
                }

                int x_b1 = rank - 2;
                int y_b1 = file + 1;
                if(x_b1 >= 0 && y_b1 < 8){
                    int ordinal = x_b1*8 + y_b1;
                    base |= 1L << ordinal;
                }

                int x_b2 = rank - 2;
                int y_b2 = file - 1;
                if(x_b2 >= 0 && y_b2 >= 0){
                    int ordinal = x_b2*8 + y_b2;
                    base |= 1L << ordinal;
                }

                int x_c1 = rank + 1;
                int y_c1 = file + 2;
                if(x_c1 < 8 && y_c1 < 8){
                    int ordinal = x_c1*8 + y_c1;
                    base |= 1L << ordinal;
                }

                int x_c2 = rank + 1;
                int y_c2 = file - 2;
                if(x_c2 < 8 && y_c2 >= 0){
                    int ordinal = x_c2*8 + y_c2;
                    base |= 1L << ordinal;
                }

                int x_d1 = rank - 1;
                int y_d1 = file + 2;
                if(x_d1 >= 0 && y_d1 < 8){
                    int ordinal = x_d1*8 + y_d1;
                    base |= 1L << ordinal;
                }

                int x_d2 = rank - 1;
                int y_d2 = file - 2;
                if(x_d2 >= 0 && y_d2 >= 0){
                    int ordinal = x_d2*8 + y_d2;
                    base |= 1L << ordinal;
                }

                //precomputedKnightAttacks.put(baseLocation, base);
                putKnightAttack(baseLocation,base);
            }
        }

        // Kings.
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Board empty = Board.empty();
                Board board = empty.placePiece(OldPieces.king_white,new Position(rank, file));

                int baseOrdinal = rank * 8 + file;
                long baseLocation = 1L << baseOrdinal;

                // Use the slower raytracer.
                List<Position> positions = rt.traceQueen(board.getBoardRepresentation(), new Position(rank, file), ChessEngine.WHITE, 1);

                long base = 0;
                for (Position position : positions) {
                    int newOrdinal = position.getRank()*8 + position.getFile();
                    base |= 1L << newOrdinal;
                }
                putKingAttack(baseLocation,base);
            }
        }

        preCalculateRookAttacks();
        preCalculateBishopAttacks();
    }
    public void preCalculateBishopAttacks(){
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int sqrOrdinal = i*8+j;
                long bishopOnBoard = 1L << sqrOrdinal;

                int pord = sqrToOrdinal(bishopOnBoard);

                PrecomputedBitboards.precomputedBishopAttacks[pord] = new HashMap<>();

                        Board board = Board.empty();
                Position rookPosition = new Position(i, j);

                board.placePiece(OldPieces.bishop_white, rookPosition);

                List<Position> bishopTrace =
                        rt.traceBishop(board.getBoardRepresentation(), rookPosition, ChessEngine.WHITE, 1337);

                long mask = 0L;

                // Convert to mask.
                for (Position bishopPotentialPos : bishopTrace) {
                    int rank = bishopPotentialPos.getRank();
                    int file = bishopPotentialPos.getFile();
                    int ordinal = rank * 8 + file;

                    mask |= 1L << ordinal;
                }

                PrecomputedBitboards.precomputedBishopRayMasks[pord] = mask;

                List<Long> allBlockerConfigs = getAllBlockerConfigs(mask);

                for (Long blockerConfig : allBlockerConfigs) {
                    byte[][] copy = Board.boardCopy(board);
                    // Use the raytracer to determine the rook's actual moves.
                    // Replace each "1" with a black pawn just for testing purposes.
                    for (int k = 0; k < 8; k++) {
                        for (int l = 0; l < 8; l++) {
                            int ordinal = k*8+l;
                            if((blockerConfig >> ordinal & 1L) == 1){
                                copy[k][l] = OldPieces.pawn_black;
                            }
                        }
                    }

                    List<Position> legalRookMoves =
                            rt.traceBishop(copy, rookPosition, ChessEngine.WHITE, 1337);

                    long legalMask = 0;

                    // Convert to mask.
                    for (Position rookPotentialPos : legalRookMoves) {
                        int rank = rookPotentialPos.getRank();
                        int file = rookPotentialPos.getFile();
                        int ordinal = rank * 8 + file;

                        legalMask |= 1L << ordinal;
                    }

                    // Once we have the legal mask, map this specific blocker config to this legal mask.
                    PrecomputedBitboards.precomputedBishopAttacks[pord].put(blockerConfig,legalMask);
                }
            }
        }
    }
    public void preCalculateRookAttacks(){
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int sqrOrdinal = i*8+j;
                long rookOnBoard = 1L << sqrOrdinal;

                int pord = sqrToOrdinal(rookOnBoard);

                PrecomputedBitboards.precomputedRookAttacks[pord] = new HashMap<>();

                Board board = Board.empty();
                Position rookPosition = new Position(i, j);

                board.placePiece(OldPieces.rook_white, rookPosition);

                List<Position> rookTrace = rt.traceRook(board.getBoardRepresentation(), rookPosition, ChessEngine.WHITE, 1337);

                long mask = 0L;

                // Convert to mask.
                for (Position rookPotentialPos : rookTrace) {
                    int rank = rookPotentialPos.getRank();
                    int file = rookPotentialPos.getFile();
                    int ordinal = rank * 8 + file;

                    mask |= 1L << ordinal;
                }

                PrecomputedBitboards.precomputedRookRayMasks[pord] = mask;

                List<Long> allBlockerConfigs = getAllBlockerConfigs(mask);

                for (Long blockerConfig : allBlockerConfigs) {
                    byte[][] copy = Board.boardCopy(board);
                    // Use the raytracer to determine the rook's actual moves.
                    // Replace each "1" with a black pawn just for testing purposes.
                    for (int k = 0; k < 8; k++) {
                        for (int l = 0; l < 8; l++) {
                            int ordinal = k*8+l;
                            if((blockerConfig >> ordinal & 1L) == 1){
                                copy[k][l] = OldPieces.pawn_black;
                            }
                        }
                    }

                    List<Position> legalRookMoves = rt.traceRook(copy, rookPosition, ChessEngine.WHITE, 1337);

                    long legalMask = 0;
                    // Convert to mask.
                    for (Position rookPotentialPos : legalRookMoves) {
                        int rank = rookPotentialPos.getRank();
                        int file = rookPotentialPos.getFile();
                        int ordinal = rank * 8 + file;

                        legalMask |= 1L << ordinal;
                    }

                    // Once we have the legal mask, map this specific blocker config to this legal mask.
                    PrecomputedBitboards.precomputedRookAttacks[pord].put(blockerConfig,legalMask);
                }
            }
        }
    }

    public long getBlockerConfig(long original, int permutationBitString){
        long duplicate = original;
        int oneIndex = 0;
        for (int i = 0; i < 64; i++) {
            long l = original >> i;
            if((l & 1L) == 1){
                int correspondingBitStringNumber = permutationBitString >> oneIndex;
                if((correspondingBitStringNumber & 1L) == 0){
                    // If the corresponding number is a 0, zero it.
                    duplicate ^= (1L << i);
                }
                oneIndex++;
            }
        }

        return duplicate;
    }
    public List<Long> getAllBlockerConfigs(long original){
        int ones = 0;
        for (int i = 0; i < 64; i++) {
            long l = original >> i;
            if((l & 1L) == 1){
                ones++;
            }
        }

        List<Long> configurations = new ArrayList<>();

        for (int i = 0; i < (1 << ones); i++) {
            configurations.add(getBlockerConfig(original, i));
        }

        return configurations;
    }
}
