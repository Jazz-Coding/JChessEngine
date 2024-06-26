package com.jazz.mvc.io.impl;

import com.jazz.old.Board;

public class StartupPackage {
    private boolean[] playerTypes;
    private Board board;

    public StartupPackage(boolean[] playerTypes, String FEN) {
        this.playerTypes = playerTypes;
        if(FEN.isBlank() || FEN.isEmpty()){
            board = Board.base();
        } else {
            board = Board.fromFEN(FEN);
        }
    }

    public boolean[] getPlayerTypes() {
        return playerTypes;
    }

    public void setPlayerTypes(boolean[] playerTypes) {
        this.playerTypes = playerTypes;
    }

    public Board getBoardFromFEN() {
        return board;
    }
}
