package com.jazz.engine.search;

import com.jazz.engine.Node;
import com.jazz.engine.data.tt.TranspositionTable;
import com.jazz.mvc.ChessController;

import java.util.List;

public class PureMinimax extends Search{
    private long nodes = 0;
    private float evaluateMinimax(Node node, int currentDepth, int targetDepth){
        nodes++;
        if(currentDepth==targetDepth) return node.heuristicEvaluation(getSettings().pieceValues, getSettings().valuePosition);
        float value = 0f;//Float.NEGATIVE_INFINITY;

        List<Integer> moves = node.childMoves();
        //moves.sort(Collections.reverseOrder());

        for (Integer move : moves) {
            node.makeMove(move);
            float subValue = -evaluateMinimax(node,currentDepth+1,targetDepth);
            node.unmakeMove(move);
        }

        return value;
    }

    @Override
    public float evaluate(Node node, int targetDepth, TranspositionTable TT, ChessController callback) {
        long start = System.nanoTime();
        float fullEval = evaluateMinimax(node, 0, targetDepth);
        long end = System.nanoTime();
        long dur = end-start;
        float s = dur/1e9F;

        System.out.println("Perft results: ");
        int NPS = Math.round(nodes/s);
        System.out.printf("NPS: %,d%n",NPS);

        return fullEval;
    }
}
