package com.jazz.mvc;

import com.jazz.engine.Node;
import com.jazz.engine.audio.AudioController;
import com.jazz.engine.data.Move;
import com.jazz.engine.gui.Statistics;
import com.jazz.engine.player.EnginePlayer;
import com.jazz.engine.player.HumanPlayer;
import com.jazz.engine.player.Player;
import com.jazz.mvc.io.impl.ViewData;
import com.jazz.mvc.io.impl.ModelData;
import com.jazz.mvc.io.impl.EngineSettings;
import com.jazz.mvc.io.impl.StartupPackage;
import com.jazz.mvc.utils.enums.MoveResult;
import com.jazz.old.Board;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.jazz.engine.ChessEngine.WHITE;
import static com.jazz.mvc.utils.enums.MoveResult.*;
import static com.jazz.mvc.utils.logging.Logger.*;
import static com.jazz.mvc.utils.sync.SyncUtils.waitForLock;

public class ChessController {
    private final ChessModel model;
    private ExecutorService modelThread;

    private final ChessView view;
    private ExecutorService viewThread;

    // The controller communicates:
    // input from View -> Model
    // output from Model -> View

    public ChessController(ChessModel model, ChessView view) {
        this.model = model;
        this.view = view;
    }

    public static ChessController build(
            ChessModel model,
            ChessView view,
            ViewData viewData, ModelData modelData){
        /*
                            -- Pre-init --
                 - Instantiation of MVC components -
         */
        status("Pre-init","Starting");

        ChessController controller = new ChessController(model,view);

        // Provide backward references to MV.
        model.link(controller);
        model.setModelData(modelData);
        model.updateFromViewData(viewData); // First time set.

        view.link(controller);
        view.setViewData(viewData);
        view.updateFromModelData(modelData); // First time set.
        view.create();

        status("Pre-init","Finished");

        /*
                            -- Init --
              - Settings are requested from the view. -
         */

        status("Init","Starting");

        view.askForStartup(); // Request mandatory information from the view (user) before the main loop can begin.

        status("Init","Finished");

        /*
                            -- Post-init --
                    - Settings can be considered set -
         */
        status("Post-init","Starting");

        controller.initBoardState();

        status("Post-init","Finished");

        return controller;
    }

    private void initBoardState(){
        ModelData modelData = model.getModelData();
        Board board = modelData.getBoard();

        Node root = Node.from(board);
        model.initRoot(root);

        modelData.setLastAvailableMoves(root.childMoves()); // First-time set
        setTurn(board.getTurn()); // Read the turn off the board object.
    }

    /**
     * Controller lock interfaces.
     * The controller provides the model and view with a "callback",
     * It then requests things from their threads and waits on a
     * specialized lock, this lock is released when the threads call
     * the "reply()" methods via the callback, enabling the game
     * loop to continue.
     */
    private Player[] players;
    private final Object startupLock = new Object();
    public boolean replyStartup(StartupPackage startupPackage){
        // Convert player types -> real players, with assistance from the model.

        String[] names = new String[2];
        Player[] players = new Player[2];
        boolean[] arePlayersHuman = startupPackage.getPlayerTypes();

        for (int i = 0; i < 2; i++) {
            players[i] = arePlayersHuman[i] ?
                    new HumanPlayer() :
                    EnginePlayer.standard(view.getDataFromView().getEngineSettings().TTSizeLimit);
            names[i] = players[i].getName();
        }
        setPlayers(players);

        model.initPGN(names);
        model.getModelData().setBoard(startupPackage.getBoardFromFEN());

        synchronized (startupLock){
            startupLock.notify(); // Notify the main controller thread that the game loop may now proceed.
        }

        AudioController.getInstance().playNewGame();
        return true;
    }

    private int lastMove;
    private final Object moveLock = new Object();
    public boolean replyMove(int move){
        // Consult the model to validate this move.
        int result = model.extractMoveIfValid(move);
        if(result==-1){
            // Move was invalid, notify the GUI.
            return false;
        } else {
            // Move was valid and we can proceed.
            this.lastMove = result;
            synchronized (moveLock){
                moveLock.notify();
            }
            return true;
        }
    }

    private final Object postGameLock = new Object();
    /**
     * The resign button was pressed, interrupt the main loop (puts it in a waiting state).
     */
    public void resignInterrupt(){
        this.lastMove = -2; // Resign code so the loop can tell immediately what happened.
        synchronized (moveLock){
            moveLock.notify();
        }
        synchronized (modelLock){
            modelLock.notify();
        }
    }

    /**
     * After the game ended, the "restart" button was pushed, breaking the waiting state and resetting everything.
     */
    public void restartInterrupt(){
        synchronized (postGameLock){
            postGameLock.notify();
        }
    }

    private MoveResult modelResult;
    private final Object modelLock = new Object();
    public void replyModel(int move, MoveResult moveResult){
        lastMove = move;
        modelResult = moveResult;

        synchronized (modelLock){
            debug("Notifying model lock...");
            modelLock.notify();
        }
    }

    // Main loop
    private int turn = 0;

    public void setPlayers(Player[] players) {
        this.players = players;
    }

    public void linkEngineSettingsToEngines(EngineSettings settings){
        for (Player player : this.players) {
            if(player instanceof EnginePlayer ep){
                ep.loadEngineSettings(settings);
                ep.setCallback(this);
            }
        }
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    private void flipTurn(){
        turn^=1;
    }

    /**
     * A special direct pipelines the chess engine can use to communicate sub-move updates like current depth straight to the view.
     */
    public void sendDirect(
            float currentEval, int currentDepth, int ttOccupation){
        view.receiveDirect(currentEval, currentDepth, ttOccupation);
    }
    public void sendDirectPerformance(int nps, float ebf){
        view.receiveDirectPerformance(nps, ebf);
    }


    private void restartGameUI(){
        debug("Returning to menu");
        view.returnToMenu(); // UI changes.

        debug("Asking for startup...");
        viewThread.submit(view::askForStartup);

        debug("Waiting on startup lock...");
        waitForLock(startupLock);

        debug("Done, re-initializing board state.");
        initBoardState();
    }

    private void gameEndResign(){ // A UI change is required, and we know the game result already.
        AudioController.getInstance().playCheckMate();

        debug("Game resigned. Awaiting further action...");
        int resignColor=0;
        for (int i = 0; i < players.length; i++) {
            if(players[i] instanceof HumanPlayer) resignColor = i;
        }

        if(resignColor == WHITE) {
            view.displayGameResult(WHITE_RESIGNS);
        } else {
            view.displayGameResult(BLACK_RESIGNS);
        }

        // Resign interrupt. Wait for further action (e.g. "restart game" pushed).
        waitForLock(postGameLock);

        // If it was pushed, trigger a global restart.
        debug("Restart game triggered.");

        restartGameUI(); // UI changes.
    }
    private void gameEndNormal(MoveResult gameResult){ // A UI change is required, but no interrupts are needed and we must extract the game result from the model.
        debug("Game ended conventionally.");
        view.displayGameResult(gameResult);

        // Wait for further action (e.g. "restart game" pushed).
        waitForLock(postGameLock);

        // If it was pushed, trigger a global restart.
        debug("Restart game triggered.");
        restartGameUI();
    }

    /**
     * Primary control loop.
     * Follows the form:
     * -> Get any changed settings from the view (GUI).
     * -> Load these into the model (chess engines and board updating component)
     * -> Get a move from the next player to move:
     *      -> If this is a human, blocks the loop until the view returns a move from the user.
     *      -> If this is an engine, blocks the loop until the model returns a move from the engine.
     * -> Use the model to play this move.
     * -> Get any changed outputs from the model (e.g. the current board).
     * -> Load these into the view.
     */
    public void loop(){
        status("Main loop","Starting");

        modelThread = Executors.newSingleThreadExecutor();
        viewThread = Executors.newSingleThreadExecutor();

        while (true){
            debug("Getting view input");

            ViewData dataFromView = view.getDataFromView();
            linkEngineSettingsToEngines(dataFromView.getEngineSettings());

            Player mover = players[turn];

            // Update "lastMove" depending on the player type.
            if(mover instanceof HumanPlayer){
                debug("Asking human for move.");
                viewThread.submit(view::askForMove);
                waitForLock(moveLock); // Wait for reply from the GUI.
                debug("Done, continuing.");
            } else {
                debug("Asking engine for move.");
                modelThread.submit(() -> model.askForMove(mover));
                waitForLock(modelLock); // Wait for reply from the model.
                debug("Done, continuing.");
            }

            if(lastMove==-2){
                gameEndResign();
                // Redo the loop from the beginning.
                continue;
            }

            dataFromView.setLastMove(lastMove);

            debug("Updating chess model from input.");
            // View -> Model
            model.updateFromViewData(dataFromView); // Load the previous move and any changed settings.
            model.doModel(); // Actually play the move, modifying the game state.
            flipTurn();

            debug("Updating view from model (i.e. rendering)");

            // Model -> View
            view.updateFromModelData(model.getModelData()); // Forward the new game state to the view.
            view.doView(); // Render the view

            // Play relevant audio.
            MoveResult gameResult = model.result();
            viewThread.submit(() -> playAudio(lastMove, gameResult, model.isSomeoneInCheck()));

            if(isGameEnding(gameResult)){
                gameEndNormal(gameResult);
                debug("Continuing from the beginning...");
                // Redo the loop from the beginning.
                continue;
            }
        }
    }

    private boolean isGameEnding(MoveResult moveResult){
        return moveResult == CHECKMATE_WHITE_WINS || moveResult == CHECKMATE_BLACK_WINS ||
                moveResult == STALEMATE_NORMAL || moveResult == STALEMATE_TFR || moveResult == STALEMATE_FMR;
    }

    private void playAudio(int move, MoveResult moveResult, boolean wasCheck){
        if(wasCheck){
            AudioController.getInstance().playCheck();
        } else {
            if (Move.isCapture(move)) {
                AudioController.getInstance().playCapture();
            } else {
                AudioController.getInstance().playMove();
            }
        }

        if(isGameEnding(moveResult)){
            AudioController.getInstance().playCheckMate();
        }
    }

    public static void main(String[] args) {
        debug("Setting up: Model, EngineSettings and ChessInput");
        ChessModel M = new ChessModel();

        EngineSettings defaultSettings = EngineSettings.defaults();
        ViewData i = new ViewData(defaultSettings);

        debug("Setting up: View, defaultBoard, Statistics and ChessOutput");
        ChessView V = new ChessView(i);

        Board defaultBoard = Board.base();
        Statistics initialStatistics = Statistics.initial();

        ModelData o = new ModelData(defaultBoard,initialStatistics);

        debug("Instantiating controller...");
        ChessController C = build(M, V, i, o);

        debug("Launching main loop...");
        C.loop();
    }
}
