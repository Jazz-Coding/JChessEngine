package com.jazz.engine.player;

import com.jazz.engine.Node;

public interface Player {
    int getMove(Node currentGameNode);
    String getName();
}
