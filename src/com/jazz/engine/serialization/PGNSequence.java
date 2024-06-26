package com.jazz.engine.serialization;

import java.util.ArrayList;
import java.util.List;

/**
 * Move -> Portable Game Notation
 * More human readable and allows exporting to other platforms.
 * e.g.
 * // TODO: Remove redundant from-to information when we can.
 *  1. Ng1f3 c7c5
 *  2. Nb1c3 f7f5
 */
public class PGNSequence {
    private List<PGNMove> moveSequence;
    private String whitePlayer;
    private String blackPlayer;

    public PGNSequence(List<PGNMove> moveSequence, String whitePlayer, String blackPlayer) {
        this.moveSequence = moveSequence;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
    }

    public PGNSequence(String whitePlayer, String blackPlayer) {
        this.moveSequence = new ArrayList<>();
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
    }

    public void append(PGNMove move){
        this.moveSequence.add(move);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[White \"").append(whitePlayer).append("\"]\n");
        sb.append("[Black \"").append(blackPlayer).append("\"]\n");
        for (int i = 0; i < moveSequence.size(); i+=2) {
            int moveIndex = (i/2)+1;
            sb.append(moveIndex).append(". ");
            sb.append(moveSequence.get(i)).append(" ");
            if(moveSequence.size() > i+1) {
                sb.append(moveSequence.get(i+1)).append(" ");
            }
        }
        return sb.toString();
    }
}
