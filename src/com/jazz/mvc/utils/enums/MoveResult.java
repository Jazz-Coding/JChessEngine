package com.jazz.mvc.utils.enums;

public enum MoveResult {
    SUCCESS,

    CHECKMATE_WHITE_WINS,
    CHECKMATE_BLACK_WINS,

    WHITE_RESIGNS,
    BLACK_RESIGNS,

    STALEMATE_NORMAL,
    STALEMATE_FMR,
    STALEMATE_TFR,

    INTERRUPTED
}
