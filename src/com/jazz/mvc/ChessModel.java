package com.jazz.mvc;

import com.jazz.engine.Node;
import com.jazz.engine.data.Move;
import com.jazz.engine.data.Precomputer;
import com.jazz.engine.gui.Statistics;
import com.jazz.engine.player.EnginePlayer;
import com.jazz.engine.player.Player;
import com.jazz.engine.serialization.PGNMove;
import com.jazz.engine.serialization.PGNSequence;
import com.jazz.engine.utils.bits.CastlingMasks;

import com.jazz.mvc.io.impl.ModelData;
import com.jazz.mvc.io.impl.ViewData;

import com.jazz.mvc.utils.enums.MoveResult;
import com.jazz.engine.utils.HashUtils;

import static com.jazz.engine.data.Pieces.queen;
import static com.jazz.mvc.utils.logging.Logger.debug;

/**
 * The pilot class.
 * Sets up the initial position and launches the GUI and engine loop.
 */
public class ChessModel{
    private ChessController controller;
    private ModelData modelData;

    public void link(ChessController controller){
        this.controller = controller;
    }
    public void setModelData(ModelData o) {
        this.modelData = o;
    }
    public ChessController getControllerCallback() { // For event callbacks.
        return controller;
    }
    public ModelData getModelData() {
        return modelData;
    }

    public boolean isSomeoneInCheck(){
        return root.kingInCheck();
    }

    public MoveResult result(){
        return root.getResult();
    }

    public void askForMove(Player player){
        ((EnginePlayer) player).loadEngineSettings(input.getEngineSettings()); // Load in new settings.

        debug("Engine is calculating move...");
        int move = player.getMove(root);
        getModelData().setEval(((EnginePlayer) player).getLastEval(root.color()));

        MoveResult result = root.getResult();

        debug("Done, signalling controller callback...");
        getControllerCallback().replyModel(move, result);
    }

    /**
     * Initializes all the engine-specific things (e.g. precomputed attacks).
     */
    private void initEnginePrep(){
        HashUtils.init();
        CastlingMasks.fill();
        new Precomputer().precomputeAttacks();
    }

    public ChessModel() {
        initEnginePrep();
    }

    private Node root;
    private ViewData input;

    public void updateFromViewData(ViewData i) {
        input = i;
    }

    /**
     * Packages any additional data into the output for use in the view.
     */
    private void packageViewData(int movePlayed){
        ModelData md = getModelData();
        Statistics stats = md.getStatistics();

        md.setLastAvailableMoves(root.childMoves()); // For move highlights and verification.
        md.setBoard(root.toBoard());

        // Append to the PGN.
        md.getPGN().append(new PGNMove(movePlayed));

        // Compute positional evaluation.
        root.computePositionalScores(stats);
    }

    public void doModel() {
        int move = input.getLastMove(); // The controller handles the logic to obtains this, it may call back to the model.

        root.makeMove(move); // Play the move, this modifies the "root" node.
        root.checkIsLateGame(input.getEngineSettings().pieceValues);

        packageViewData(move);
    }

    private static final int TAG_MASK = 0b0000_00_000_0_0_0_0_000_000_111111_111111_0; // Erase all but the from-to squares (all that's needed to distinguish which move we are referring to).
    public int extractMoveIfValid(int candidate){
        for (int move : getModelData().getLastAvailableMoves()) {
            // Compare, sans the first 4 most significant bits. This is because the GUI has no move continuity and must reuse the move generator's
            // en-passant data (which is stored in the first 4 bits).
            int moveTag = move&TAG_MASK;
            int candidateTag = candidate&TAG_MASK;

            if(moveTag == candidateTag){
                if(Move.isPromotion(move)){
                    // TODO: Dispatch to a promotion-selection GUI.
                    // In the meantime, just prefer the queen promotion.
                    return Move.setPromotionType(move,queen);
                } else return move;
            }
        }
        return -1;
    }

    public void initPGN(String[] names) {
        getModelData().setPGN(new PGNSequence(names[0],names[1]));
    }

    public void initRoot(Node root) {
        this.root = root;
    }
}
