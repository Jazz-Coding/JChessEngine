package com.jazz.engine.data.tt;

import com.jazz.engine.Node;
import com.jazz.engine.utils.datastructures.CappedHashMap;


/**
 * Basic transposition table implementation built for single-thread operation.
 */
public class SerialTranspositionTable implements TranspositionTable{
    private CappedHashMap<Long, Long> table;

    public SerialTranspositionTable(int maximumCapacity) {
        table = new CappedHashMap<>(maximumCapacity);
    }

    @Override
    public boolean has(Node node) {
        long positionHash = node.hash();
        return table.containsKey(positionHash);
    }

    @Override
    public boolean isValid(Node node, int plannedSearchDepth) {
        if(has(node)){
            long entry = get(node);
            int depthEvaluated = TTEntry.getDepthEvaluated(entry);

            if(depthEvaluated >= plannedSearchDepth){ // Ensure the search was deep enough or else we may end up using outdated information.
                return true;
            }
        }
        return false;
    }

    @Override
    public long get(Node node) {
        long positionHash = node.hash();
        return table.get(positionHash);
    }

    @Override
    public void addDirect(Node node, long entry) {
        this.table.put(node.hash(),entry);
    }

    @Override
    public void add(Node node,
                    int depthSearched,
                    float alpha, float beta, float value) {
        long positionHash = node.hash();

        long ttEntry = TTEntry.build(depthSearched,value,alpha,beta);
        this.table.put(positionHash,ttEntry);
    }

    @Override
    public void add(Node node,
                    int depthSearched,
                    float alpha, float beta, float value,
                    int bestMove) {
        long positionHash = node.hash();

        long ttEntry = TTEntry.build(depthSearched,value,alpha,beta,bestMove);
        this.table.put(positionHash,ttEntry);
    }

    @Override
    public int size() {
        return table.size();
    }

    @Override
    public void setSize(int newSize) {
        if(newSize != size()) {
            this.table.setMaxSize(Math.max(1,newSize)); // We need at least 1 to retrieve the best move.
        }
    }
}
