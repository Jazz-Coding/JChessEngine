package com.jazz.mvc.io.impl;

/**
 * Engine settings (i.e. non-visual).
 */
public class EngineSettings {
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

    public boolean valuePosition;

    public EngineSettings(int depthLimit, boolean useQuiescence, boolean useAlphaBetaPruning, boolean useKillerHeuristic, int n_killers, boolean useTT, int TTSizeLimit, boolean useIterativeDeepening, float[] pieceValues, boolean valuePosition) {
        this.depthLimit = depthLimit;
        this.useQuiescence = useQuiescence;
        this.useAlphaBetaPruning = useAlphaBetaPruning;
        this.useKillerHeuristic = useKillerHeuristic;
        this.n_killers = n_killers;
        this.useTT = useTT;
        this.TTSizeLimit = TTSizeLimit;
        this.useIterativeDeepening = useIterativeDeepening;
        this.pieceValues = pieceValues;
        this.defaultPieceValues = pieceValues.clone();
        this.valuePosition = valuePosition;
    }

    public static EngineSettings defaults(){
        return new EngineSettings(
                6,
                true,
                true,
                true, 2,
                true, 100_000_000,
                true,
                new float[]{1,3,3.6F,5.1F,8.75F,3F},true
        );
    }
}
