package com.jazz.engine;

import com.jazz.engine.data.tt.TTEntry;
import com.jazz.engine.data.tt.TranspositionTable;
import com.jazz.engine.search.Search;
import com.jazz.engine.search.evaluation.PieceValueTables;
import com.jazz.mvc.ChessController;
import com.jazz.mvc.io.impl.EngineSettings;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * Container for the chess engine algorithms.
 * Explores a given root game node using the selected algorithm.
 * Implements callable so as to be multi-threaded.
 */
public class ChessEngine implements Callable<Float> {
    public static final int WHITE = 0;
    public static final int BLACK = 1;

    public static final int KINGSIDE = 0;
    public static final int QUEENSIDE = 1;

    private Node root;

    private ChessController callback;
    private TranspositionTable TT;
    //private int depthLimit;

    private CountDownLatch latch;

    private EngineSettings settings;
    private Search searchAlgorithm;

    public ChessEngine(Search searchAlgorithm, Node root, TranspositionTable TT, CountDownLatch latch) {
        this.root = root;
        this.TT = TT;
        this.latch = latch;

        this.searchAlgorithm = searchAlgorithm;
        PieceValueTables.init();
    }
    public ChessEngine(Search searchAlgorithm, TranspositionTable TT, CountDownLatch latch) {
        this.TT = TT;
        this.latch = latch;

        this.searchAlgorithm = searchAlgorithm;
        PieceValueTables.init();
    }
    public ChessEngine(Search searchAlgorithm, Node root, TranspositionTable TT, int nThreads) {
        this(searchAlgorithm,root,TT,new CountDownLatch(nThreads));
    }
    public ChessEngine(Search searchAlgorithm, Node root, TranspositionTable TT) {
        this(searchAlgorithm,root,TT,new CountDownLatch(1));
    }

    public ChessEngine(Search searchAlgorithm, TranspositionTable TT) {
        this(searchAlgorithm,TT,new CountDownLatch(1));
    }

    public void setCallback(ChessController controller){
        this.callback = controller;
    }

    public void setRootNode(Node root){
        this.root=root;
    }

    /**
     * Returns the engine evaluation of the root position.
     */
    public float evaluate(){
        TT.setSize(settings.TTSizeLimit); // Adjust if required.
        float evaluation = searchAlgorithm.evaluate(root, settings.depthLimit, TT, callback);
        latch.countDown(); // This thread's work is now done, notify the latch.
        return evaluation;
    }

    /**
     * After evaluation, reads the best move off the transposition table.
     */
    public int retrieveBestMove(){
        long rootEntry= TT.get(root);
        return TTEntry.getHashMove(rootEntry);
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public void setEngineSettings(EngineSettings settings) {
        this.settings = settings;
        searchAlgorithm.setEngineSettings(settings);
    }

    @Override
    public Float call() {
        return evaluate();
    }
}
