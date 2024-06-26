package com.jazz.engine.search;

import com.jazz.engine.Node;
import com.jazz.engine.data.tt.TranspositionTable;
import com.jazz.mvc.ChessController;
import com.jazz.mvc.io.impl.EngineSettings;

public abstract class Search {
    protected EngineSettings settings;

    public abstract float evaluate(Node node, int targetDepth, TranspositionTable TT, ChessController callback);

    public void setEngineSettings(EngineSettings settings){
        this.settings = settings;
    }

    public EngineSettings getSettings() {
        return settings;
    }
}
