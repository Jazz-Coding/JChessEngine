package com.jazz.engine.player;

import com.jazz.engine.ChessEngine;
import com.jazz.engine.Node;
import com.jazz.engine.data.tt.SerialTranspositionTable;
import com.jazz.engine.search.AlphaBeta;
import com.jazz.engine.search.decorator.IterativeDeepeningDecorator;
import com.jazz.mvc.ChessController;
import com.jazz.mvc.io.impl.EngineSettings;

import static com.jazz.engine.ChessEngine.WHITE;

/**
 * Engine players use their algorithm to explore the position and return a best move.
 */
public class EnginePlayer implements Player{
    private ChessEngine engine;
    private ChessController callback;

    public EnginePlayer(ChessEngine engine) {
        this.engine = engine;
    }

    public void setCallback(ChessController controller) {
        this.callback = controller;
        this.engine.setCallback(callback); // Propagate down to the engine.
    }

    public static EnginePlayer standard(int TT_size){
        return new EnginePlayer(
                new ChessEngine(
                        new IterativeDeepeningDecorator(new AlphaBeta()),
                        new SerialTranspositionTable(TT_size))
        );
    }

    public void loadEngineSettings(EngineSettings settings){
        engine.setEngineSettings(settings);
    }

    private float lastEval = 0;

    public float getLastEval(int color) {
        return color == WHITE ? lastEval : -lastEval;
    }

    @Override
    public int getMove(Node currentGameNode) {
        // Consult the engine.
        engine.setRoot(currentGameNode);

        float eval = engine.evaluate();
        lastEval = eval;

        return engine.retrieveBestMove();
    }

    @Override
    public String getName() {
        return "Engine";
    }
}
