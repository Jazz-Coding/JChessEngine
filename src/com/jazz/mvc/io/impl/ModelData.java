package com.jazz.mvc.io.impl;

import com.jazz.engine.gui.Statistics;
import com.jazz.engine.serialization.PGNSequence;
import com.jazz.old.Board;

import java.util.List;

/**
 * Output from the model.
 * - The current board, packaged into the render-optimized "Board" object.
 */
public class ModelData {
    private Board board;

    private Statistics statistics;
    private PGNSequence PGN;

    private float eval;
    private List<Integer> lastAvailableMoves;

    public ModelData(Board board, Statistics statistics, PGNSequence PGN) {
        this.board = board;
        this.statistics = statistics;
    }

    public ModelData(Board board, Statistics statistics) {
        this.board = board;
        this.statistics = statistics;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    public PGNSequence getPGN() {
        return PGN;
    }

    public void setPGN(PGNSequence PGN) {
        this.PGN = PGN;
    }

    public float getEval() {
        return eval;
    }

    public void setEval(float eval) {
        this.eval = eval;
    }

    public List<Integer> getLastAvailableMoves() {
        return lastAvailableMoves;
    }

    public void setLastAvailableMoves(List<Integer> lastAvailableMoves) {
        this.lastAvailableMoves = lastAvailableMoves;
    }
}
