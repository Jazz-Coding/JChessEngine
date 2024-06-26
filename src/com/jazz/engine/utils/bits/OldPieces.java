package com.jazz.engine.utils.bits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Interoperability patch with the old techniques (now used for precomputation).
 */
public class OldPieces {
    public static final byte pawn_white = 0b0001;
    public static final byte knight_white = 0b0010;
    public static final byte bishop_white = 0b0011;
    public static final byte rook_white = 0b0100;
    public static final byte queen_white = 0b0101;
    public static final byte king_white = 0b0110;

    public static final byte pawn_black = 0b1001;
    public static final byte knight_black = 0b1010;
    public static final byte bishop_black = 0b1011;
    public static final byte rook_black = 0b1100;
    public static final byte queen_black = 0b1101;
    public static final byte king_black = 0b1110;

    public static final byte nothing = 69;

    public static List<Byte> ordered =
            new ArrayList<>(Arrays.asList(pawn_white,
                    knight_white,
                    bishop_white,
                    rook_white,
                    queen_white,
                    king_white,
                    pawn_black,
                    knight_black,
                    bishop_black,
                    rook_black,
                    queen_black,
                    king_black
                    ));

    public static int indexOf(byte piece){
        return ordered.indexOf(piece);
    }
}
