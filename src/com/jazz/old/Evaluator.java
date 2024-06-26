package com.jazz.old;

import com.jazz.engine.utils.bits.OldPieces;

/**
 * Evaluates a position.
 */
public class Evaluator {
    public static float getMaterialWorth(byte piece){
        byte whiteWashed = (byte) (piece & 0b0111);
        switch (whiteWashed) {
            case OldPieces.pawn_white -> {
                return 1f;
            }
            case OldPieces.knight_white, OldPieces.bishop_white -> {
                return 3f;
            }
            case OldPieces.rook_white -> {
                return 5f;
            }
            case OldPieces.queen_white -> {
                return 9f;
            }
            default -> {
                return 0f;
            }
        }
    }

    public static float evaluate(Board position){
        byte[][] boardRepresentation = position.getBoardRepresentation();

        float whiteMaterial = 0;
        float blackMaterial = 0;
        for (int i = 0; i < boardRepresentation.length; i++) {
            for (int j = 0; j < boardRepresentation[0].length; j++) {
                byte piece = boardRepresentation[i][j];
                if(piece != 0) {
                    float materialWorth = getMaterialWorth(piece);
                    if(piece >> 3 == 0) {
                        whiteMaterial+=materialWorth;
                    } else {
                        blackMaterial +=materialWorth;
                    }
                }
            }
        }

        float raw = whiteMaterial - blackMaterial;
        return raw;
    }
}
