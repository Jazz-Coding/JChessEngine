package com.jazz.engine.gui;

/**
 * Model -> View communication pipeline
 */
public class Statistics {
    public int NPS;
    public float branchingFactor;

    public float lastEval;
    public int currentDepth;
    public int currentTTSize;
    public int leafNodesEvaluated;
    public int quiescenceNodesEvaluated;
    public boolean gameOver;

    public float[][] positionalScores = new float[2][6];

    public Statistics(int NPS, float branchingFactor, float lastEval, int currentDepth, int currentTTSize, int leafNodesEvaluated, int quiescenceNodesEvaluated, boolean gameOver, float[][] positionalScores) {
        this.NPS = NPS;
        this.branchingFactor = branchingFactor;
        this.lastEval = lastEval;
        this.currentDepth = currentDepth;
        this.currentTTSize = currentTTSize;
        this.leafNodesEvaluated = leafNodesEvaluated;
        this.quiescenceNodesEvaluated = quiescenceNodesEvaluated;
        this.gameOver = gameOver;
        this.positionalScores = positionalScores;
    }

    public static Statistics initial(){
        return new Statistics(
                0,0,0,0,0,0,0,false, new float[2][6]
        );
    }
}
