package com.jazz.engine.data.tt;

import com.jazz.engine.Node;

/**
 * Table of positions and useful information about those positions.
 * In games like chess, positions tend to re-occur quite often as moves are repeated or
 * players "transpose" into the same position from different starting points.
 *
 * Thus we can "memoize" the search, falling back to stored values when we can.
 * This significantly accelerates the search.
 *
 * For alpha-beta searching, evaluations may be incomplete as they were terminated early. In these cases we can still
 * use some of the information as an upper/lower bound, but must check before how reliable the stored information is.
 *
 *  Additionally, in an iterative-deepening framework, we can perform shallower searches to give us an idea of which
 *  moves are in the right direction, and try these moves first. This often yields more rapid cutoffs, and in fact is much faster
 *  than just doing one search of the desired depth.
 *  In essence, we are combining the benefits of breadth-first search with depth-first search,
 *  and using memoization to remove most if not all of the overheard associated with this.
 *
 *  Finally, we can also easily get the best move (or series of moves) according to the engine - simply look for the root node in the transposition table
 *  and read off the "best move" value.
 */
public interface TranspositionTable {
    boolean has(Node node);
    boolean isValid(Node node, int plannedSearchDepth);
    long get(Node node);

    void addDirect(Node node, long entry);
    void add(Node node, int depthSearched, float alpha, float beta, float value);
    void add(Node node, int depthSearched, float alpha, float beta, float value, int bestMove);

    int size();
    void setSize(int newSize);
}
