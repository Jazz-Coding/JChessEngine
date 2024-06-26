package com.jazz.engine.search.decorator;

import com.jazz.engine.Node;
import com.jazz.engine.data.tt.TranspositionTable;
import com.jazz.engine.search.Search;
import com.jazz.mvc.ChessController;

public class IterativeDeepeningDecorator extends SearchDecorator{
    public IterativeDeepeningDecorator(Search search) {
        super(search);
    }

    @Override
    public float evaluate(Node node, int targetDepth, TranspositionTable TT, ChessController callback) {
        float eval = Float.NEGATIVE_INFINITY;
        for (int depth = 3; depth <= targetDepth; depth++) {
            eval = super.evaluate(node, depth, TT,callback);

            // Use the callback here to set current depth and evaluation.
            callback.sendDirect(eval,depth,TT.size());
        }
        return eval;
    }
}
