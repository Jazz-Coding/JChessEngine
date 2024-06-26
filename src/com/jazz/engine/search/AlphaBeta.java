package com.jazz.engine.search;

import com.jazz.engine.data.KillerTable;
import com.jazz.engine.data.Move;
import com.jazz.engine.data.tt.TTEntry;
import com.jazz.engine.data.tt.TranspositionTable;
import com.jazz.engine.Node;
import com.jazz.mvc.ChessController;
import com.jazz.old.Checkmate;

import java.util.*;

/**
 * Alpha-Beta pruning search implemented in a negamax framework.
 *
 * A significant improvement over plain minimax.
 * Conceptually, since we explore the tree depth first, we can make statements about the leaf node positions, i.e. "black is up 2 pawns here".
 *
 * We can use these statements to make deductions about other positions, for instance if we know black is up 2 pawns if they play some other move,
 * and we are considering a different move available to black, and we find an outcome that leaves black down 2 pawns instead, we need not search any other outcomes
 * for this move. It's our turn after black plays this move (we have initiative), so we have already "proven" that this alternative move by black we are considering is bad,
 * since we have a "refutation".
 *
 * We only need one refutation to stop searching, there's no need to find even better refutations since black
 * won't go down this evidently inferior route in real play (assuming an optimal player).
 *
 * In practice, this means we maintain a record of black's "worst case, but guaranteed" option, a value we call "beta".
 * If we explore a move and find its score is > beta, we've found a case even worse for black (who is the minimizing player). And can halt the search of this sub-tree.
 * This is known as a "beta-cutoff", and allows for huge amounts of the game tree to be avoided. In fact, in the best case, the depth we can reach is effectively doubled, with the branching
 * factor reduced from 35 moves per ply to just ~6.
 *
 * Recursively, black can use the same logic to reason about white's moves.
 * In negamax, we utilize the mathematical fact: min(A,B) = -max(-B,-A)
 * In other words our opponent's loss is our gain (zero-sum game). So by negating the values like this we can just recursively call the exact same method.
 */
public class AlphaBeta extends Search{
    //private boolean useKillerHeuristic = true;
    private KillerTable killers;

    /*
            Order the moves (with a selection-sort approach where we select the best move and remove it as an option right after)

            Alpha-beta pruning is most optimal when we find refutations, i.e. we need to start with a "good option" for the player, so we can quickly rule out the far more numerous bad options.
            For instance positive exchange evaluation captures (aggressor value < victim value) are tried first - capturing the queen with a pawn is probably a good move.
            Also the best move from a shallower search is tried first, it's probably quite good here too
         */
    private int nextMove(List<Integer> moves,
                         int currentDepth,
                         int hashMove){
        int highestScore = 0;

        int currentBest = moves.getFirst();
        int indexOfBest = 0;

        for (int i = 0; i < moves.size(); i++) {
            int candidate = moves.get(i);

            int score = Move.getScore(candidate, killers.contains(candidate,currentDepth), hashMove, getSettings().pieceValues);
            if(score > highestScore){
                highestScore = score;
                currentBest = candidate;
                indexOfBest = i;

                if(highestScore==16) break; // There's only 1 hash move, we can stop early.
            }
        }

        moves.remove(indexOfBest);
        return currentBest;
    }

    private float evaluateAB(Node node,
                             int currentDepth, int targetDepth,
                             float alpha, float beta,
                             TranspositionTable TT) throws Exception{
        nodesEvaluated++;
        if(node.isTRF()){
            return Checkmate.TFR_COST;
        }

        int plannedSearchDepth = Math.max(targetDepth - currentDepth,0); // If we continue, how long can we expect to search this node?
        // For quiescence, we consider all nodes to be searched to the same depth (0), since they get fully explored regardless.

        int hashMove = -1;
        float alpha_initial = alpha;

        /*
            Consult the transposition table.
            We may have encountered this position before and can reuse some already found values.
            This effectively memoizes the search.
         */
        if(TT.has(node)){
            long entry = TT.get(node);

            int depthEvaluated = TTEntry.getDepthEvaluated(entry);
            hashMove = TTEntry.getHashMove(entry); // We can use this for move ordering regardless.

            if(depthEvaluated >= plannedSearchDepth){
                float recordedValue = TTEntry.getValue(entry);

                switch (TTEntry.getType(entry)) {
                    case EXACT -> {
                        return recordedValue;
                    }
                    case LOWER_BOUND -> {
                        // A beta-cutoff took place here, i.e. we didn't fully explore all the outcomes of this node.
                        // This node is "at least" this good for us, but could be even better.
                        // Thus our minimum assured score is this node's value (but we might find a better one later).
                        alpha = Math.max(alpha,recordedValue);
                    }
                    case UPPER_BOUND -> {
                        // No moves raised alpha, i.e. an alpha-cutoff occurred one level
                        // below, resulting in the search being terminated early.
                        // We know these moves are at least this bad for us, but could be even
                        // worse.
                        beta = Math.min(beta,recordedValue);
                    }
                }

                // Check for beta cutoff to cause them here too.
                if(alpha >= beta) return recordedValue;
            }
        }

        /*
            Get all available moves for us in this position.
         */
        List<Integer> moves = node.childMoves();
        if(moves.isEmpty()){
            if(node.kingInCheck()){
                // Checkmate, worst possible outcome for us.
                return Checkmate.in(currentDepth, node.color());
            } else {
                return 0F; // Stalemate.
            }
        }

         /*
            When we reach the target depth (the "horizon"), defer to the heuristic evaluator.
            Technically, this is an approximation of the node's value, but this is more than outweighed by how deep we can search.
         */
        if(currentDepth==targetDepth && !getSettings().useQuiescence) return node.heuristicEvaluation(getSettings().pieceValues, getSettings().valuePosition);

        // ----------QUIESCENCE SEARCH-----------
        /*
        We've reached the horizon, but what if we captured a piece but our opponent could "simply"
        recapture it on the next move? Can we do a bit more searching just to make sure that doesn't happen?

        As it turns out, we can. By limiting the search past this point to only tactical (non-quiet) moves, we can fully explore
        sequences of captures (e.g. ..xe5, ..xe5,.. xe5...) or checks. To put it another way, we explore the position and only stop when it is "quiet".

        Since there are normally far fewer tactical moves in a position, and we use alpha-beta pruning and the transposition table, we can afford to do this at only a moderate
        amount of performance slowdown; in exchange the engine becomes virtually immune to "tactics", dramatically improving its strength.
         */
        float value = Float.NEGATIVE_INFINITY;

        if(currentDepth >= targetDepth && getSettings().useQuiescence) { // Horizon reached.
            float staticEvaluation = node.heuristicEvaluation(getSettings().pieceValues, getSettings().valuePosition);

            if (staticEvaluation >= beta) {
                // Beta-cutoff, the minimizer is assured something better already so won't go down this path.
                return staticEvaluation;
            }

            if (alpha < staticEvaluation) {
                // We can assume this is a lower bound, since doing nothing gives us
                // this score, doing something will surely be even higher.
                alpha = staticEvaluation;
            }

            value = staticEvaluation;

            moves.removeIf(m ->
                            !node.isMoveLoud(m) ||
                            (Move.moveSEE(m,getSettings().pieceValues) < 0)); // Also remove unlikely captures.
            // Now progress to the search with this limited scope.
        }
        //---------------------------------------

        int bestMoveFound = -1;
        while (!moves.isEmpty()){
            int move = nextMove(moves, currentDepth, hashMove); // Automatically performs a selection sort.

            node.makeMove(move);
            float subValue = -evaluateAB(node, currentDepth + 1, targetDepth, -beta, -alpha, TT); // Negamax evaluation.
            node.unmakeMove(move);

            if(value < subValue){
                value = subValue; // New best candidate.
                bestMoveFound = move;
            }

            if(alpha < value){
                // Improvement on alpha.
                // This might not be the case, for instance maybe the previous move was better.
                // Update alpha, this will help quickly find refutations of our other available moves.
                alpha = value;
            }

            if(value >= beta){
                // Beta-cutoff.
                // We've discovered something worse for the opponent than what they are already assured, so they won't go down this path, so we can halt.
                // Effectively we have "proved" the parent node was bad, i.e. refuted it.

                // Record as killer move.
                if(!Move.isCapture(move)) {
                    // Don't record captures, since we'll try these first anyway and normally they are far outnumbered by quiet moves.
                    // If there are any good quiet moves, it may take us some time to reveal them, and we should definitely remember them.
                    if(!killers.contains(move,currentDepth)) {
                        killers.add(move, currentDepth);
                    }
                }

                break;
            }
        }

        TT.add(node, plannedSearchDepth, alpha_initial, beta, value, bestMoveFound);

        return value;
    }

    @Override
    public float evaluate(Node node, int targetDepth, TranspositionTable TT, ChessController callback) {
        long start = System.nanoTime();

        //useKillerHeuristic = getSettings().useKillerHeuristic;
        killers = new KillerTable(getSettings().n_killers);

        float eval;
        try {
            eval = evaluateAB(node,
                    0, targetDepth,
                    Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY,
                    TT);
        } catch (Exception e){
          return Float.NaN;
        }

        long end = System.nanoTime();
        long dur = end-start;
        float s = dur/1e9F;

        float ebf = (float) Math.pow(nodesEvaluated, 1D/targetDepth);
        int nodesPerSecond = Math.round(nodesEvaluated/s);
        callback.sendDirectPerformance(nodesPerSecond,ebf);

        nodesEvaluated=0;

        return eval;
    }

    private long nodesEvaluated = 0;
}
