package com.jazz.engine.search.decorator;

import com.jazz.engine.Node;
import com.jazz.engine.data.tt.TranspositionTable;
import com.jazz.engine.search.Search;
import com.jazz.mvc.ChessController;
import com.jazz.mvc.io.impl.EngineSettings;

public abstract class SearchDecorator extends Search {
    private Search search;

    public SearchDecorator(Search search) {
        this.search = search;
    }

    @Override
    public float evaluate(Node node, int targetDepth, TranspositionTable TT, ChessController callback) {
        return search.evaluate(node,targetDepth, TT, callback);
    }

    @Override
    public void setEngineSettings(EngineSettings settings){
        search.setEngineSettings(settings);
    }

    @Override
    public EngineSettings getSettings() {
        return search.getSettings();
    }
}
