package com.jazz.engine.gui;

/**
 * Manages all the settings that may be updated/read by the model and view.
 */
public class Settings{
    public int depthLimit;

    public boolean useQuiescence;
    public boolean useAlphaBetaPruning;

    public boolean useKillerHeuristic;
    public int n_killers;

    public boolean useTT;
    public int TTSizeLimit;

    public boolean useIterativeDeepening;

    public float[] pieceValues;
    public float[] defaultPieceValues = new float[]{1,3,3.6F,5.1F,8.75F,3F};

    public boolean valueCentrePawns;

    public boolean showMoveHighlights;
    public boolean labelSquares = false;

    public boolean whiteHuman = true;
    public boolean blackHuman = true;

    public void resetToDefaults(){
        depthLimit=6;

        useQuiescence=true;
        useAlphaBetaPruning=true;

        useKillerHeuristic=true;
        n_killers=2;

        useTT=true;
        TTSizeLimit=100_000_000;
        useIterativeDeepening=true;

        pieceValues = defaultPieceValues;
        valueCentrePawns = true;
        showMoveHighlights = false;
    }
}
