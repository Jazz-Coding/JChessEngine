package com.jazz.engine.data;

public class KillerTable {
    private int[][] killers; // Indexed by depth
    private int[] killerIndices; // Indexed by depth
    private int n_killers;

    public KillerTable(int n_killers) {
        this.n_killers = n_killers;
        this.killers = new int[1000][n_killers];
        this.killerIndices = new int[1000];
    }

    public boolean contains(int move, int currentDepth){
        for (int i = 0; i < n_killers; i++) {
            if(killers[currentDepth][i]==move) return true;
        }
        return false;
    }

    public void add(int move, int currentDepth){
        killers[currentDepth][killerIndices[currentDepth]^=1] = move; // Keep a record of constantly flipping indices to overwrite in a sliding fashion.
    }
}
