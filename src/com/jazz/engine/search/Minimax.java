package com.jazz.engine.search;

import com.jazz.engine.data.tt.TTEntry;
import com.jazz.engine.data.tt.TTEntryType;
import com.jazz.engine.Node;
import com.jazz.engine.data.tt.TranspositionTable;
import com.jazz.mvc.ChessController;

import java.util.Collections;
import java.util.List;

/**
 * Minimax algorithm implemented in negamax framework.
 * The maximizer (white), selects the move that maximizes the evaluation.
 * The minimizer (black), selects the response move that minimizes the evaluation.
 *
 * The cycle is followed until "targetDepth" is reached, at which point a heuristic evaluation is performed
 * based on material count or other game specific knowledge. The entire tree is then considered depth-first,
 * and the move that maximizes the score is selected.
 *
 * VERY slow compared to AlphaBeta.
 */
public class Minimax extends Search{
    private float evaluateMinimax(Node node, int currentDepth, int targetDepth, TranspositionTable TT){
        if(currentDepth==targetDepth) return node.heuristicEvaluation(getSettings().pieceValues, getSettings().valuePosition);

        int depthPlanned = targetDepth - currentDepth;

        if(TT.isValid(node, depthPlanned)){ // We can still use the transposition table, but only exact type entries are considered.
            long entry = TT.get(node);
            float storedValue = TTEntry.getValue(entry);

            if (TTEntry.getType(entry) == TTEntryType.EXACT) {
                return storedValue;
            }
        }

        float value = Float.NEGATIVE_INFINITY;

        int remainingDepth = targetDepth-currentDepth;

        List<Integer> moves = node.childMoves();
        moves.sort(Collections.reverseOrder());

        int bestMove = -1;
        for (Integer move : moves) {
            node.makeMove(move);
            float subValue = -evaluateMinimax(node,currentDepth+1,targetDepth, TT);
            node.unmakeMove(move);

            if(subValue >= value){
                value = subValue;
                bestMove = move;
            }
        }

        // Store position in transposition table.
        TT.add(node, remainingDepth, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, value, bestMove);

        return value;
    }
    @Override
    public float evaluate(Node node, int targetDepth, TranspositionTable TT, ChessController callback) {
        return evaluateMinimax(node,0,targetDepth, TT);
    }
}
