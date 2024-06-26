package com.jazz.mvc;

import com.jazz.engine.audio.AudioController;
import com.jazz.engine.data.Pieces;
import com.jazz.engine.gui.Icons;
import com.jazz.engine.gui.Statistics;
import com.jazz.engine.gui.components.JBoardPanel;
import com.jazz.engine.gui.components.JFrameBuilder;
import com.jazz.engine.gui.components.JStatsPanel;
import com.jazz.engine.serialization.PGNSequence;
import com.jazz.mvc.io.impl.*;
import com.jazz.mvc.utils.enums.MoveResult;

import javax.swing.*;

import static com.jazz.mvc.utils.logging.Logger.debug;

public class ChessView {
    private JPanel mainPanel;
    private JCheckBox quiescenceSearchCheckBox;
    private JCheckBox iterativeDeepeningCheckBox;
    private JCheckBox transpositionTableCheckBox;
    private JFormattedTextField ttSizeTextField;
    private JButton maxButton;
    private JCheckBox killerHeuristicCheckBox;
    private JFormattedTextField killerSizeTextField;
    private JCheckBox alphaBetaPruningCheckBox;
    private JFormattedTextField pawnValueTextField;
    private JFormattedTextField knightValueTextField;
    private JButton resetButton;
    private JSlider depthSlider;
    private JCheckBox valueCentrePawnsCheckBox;
    private JProgressBar ttSizeBar;
    private JLabel depthLabel;
    private JFormattedTextField bishopValueTextField;
    private JFormattedTextField rookValueTextField;
    private JFormattedTextField queenValueTextField;
    private JFormattedTextField kingValueTextField;
    private JLabel ttSizeCounter;
    private JButton resignButton;
    private JLabel positionScoreLabel;
    private JPanel statsPanel;
    private JCheckBox showMoveHighlightsCheckBox;
    private JButton unmakeLastMoveButton;
    private JCheckBox showSquareLabelsCheckBox;
    private JLabel bfLabel;
    private JLabel npsLabel;
    private JButton beginButton;
    private JComboBox blackComboBox;
    private JComboBox whiteComboBox;
    private JLabel setupLabel;
    private JLabel whiteCBLabel;
    private JLabel blackCBLabel;
    private JTextArea PGN_Pane;
    private JTextField fenField;
    private JLabel fenLabel;
    private JScrollPane scrollPane;

    private ModelData output;
    private EngineSettings settings;
    private ViewSettings viewSettings = ViewSettings.defaults();

    private ChessController controller;
    private ViewData i;
    private boolean resignPressed = false;

    public void link(ChessController controller){
        this.controller = controller;
    }
    public void setViewData(ViewData i) {
        this.i = i;
    }
    public ViewData getDataFromView() {
        return i;
    }
    public ChessController getControllerCallback() { // For event callbacks.
        return controller;
    }

    private JBoardPanel boardPanel(){
        return (JBoardPanel) mainPanel;
    }

    private JStatsPanel statsPanel(){
        return (JStatsPanel) statsPanel;
    }
    
    public ChessView(ViewData input) {
        settings = input.getEngineSettings();
        boardPanel().setViewSettings(viewSettings);

        Icons.load();
        AudioController.create();

        /*
            Add listeners for adjustable engine properties.
         */
        depthSlider.addChangeListener(evt -> settings.depthLimit = depthSlider.getValue());
        quiescenceSearchCheckBox.addActionListener(evt -> settings.useQuiescence = quiescenceSearchCheckBox.isSelected());
        alphaBetaPruningCheckBox.addActionListener(evt -> settings.useAlphaBetaPruning = alphaBetaPruningCheckBox.isSelected());

        killerHeuristicCheckBox.addActionListener(evt -> {
            boolean selected = killerHeuristicCheckBox.isSelected();
            settings.useKillerHeuristic = selected;
            killerSizeTextField.setEditable(selected);
        });

        transpositionTableCheckBox.addActionListener(evt -> {
            boolean selected = transpositionTableCheckBox.isSelected();
            settings.useTT = selected;
            ttSizeTextField.setEditable(selected);
            maxButton.setEnabled(selected);
        });
        maxButton.addActionListener(evt -> {
            long availableMemory = Runtime.getRuntime().maxMemory() / 2;
            long memoryUsePerEntry = 8;

            int maxSizeCalc = (int) (availableMemory / memoryUsePerEntry);

            settings.TTSizeLimit = maxSizeCalc;
            ttSizeTextField.setText(String.format("%,d",maxSizeCalc));
        });
        iterativeDeepeningCheckBox.addActionListener(evt -> {
            settings.useIterativeDeepening = iterativeDeepeningCheckBox.isSelected();
        });

        /*
            Text field property change listeners.
         */
        pawnValueTextField.addPropertyChangeListener(evt ->
                settings.pieceValues[Pieces.pawn] = (float) pawnValueTextField.getValue());
        knightValueTextField.addPropertyChangeListener(evt ->
                settings.pieceValues[Pieces.knight] = (float) knightValueTextField.getValue());
        bishopValueTextField.addPropertyChangeListener(evt ->
                settings.pieceValues[Pieces.bishop] = (float) bishopValueTextField.getValue());
        rookValueTextField.addPropertyChangeListener(evt ->
                settings.pieceValues[Pieces.rook] = (float) rookValueTextField.getValue());
        queenValueTextField.addPropertyChangeListener(evt ->
                settings.pieceValues[Pieces.queen] = (float) queenValueTextField.getValue());
        kingValueTextField.addPropertyChangeListener(evt ->
                settings.pieceValues[Pieces.king] = (float) kingValueTextField.getValue());

        ttSizeTextField.addPropertyChangeListener(evt -> {
                    settings.TTSizeLimit = (int) ttSizeTextField.getValue();
                }
        );

        killerSizeTextField.addPropertyChangeListener(evt -> {
            settings.n_killers = (int) killerSizeTextField.getValue();
        });

        resetButton.addActionListener(evt -> {
            settings.pieceValues = settings.defaultPieceValues;
            pawnValueTextField.setValue(settings.pieceValues[Pieces.pawn]);
            knightValueTextField.setValue(settings.pieceValues[Pieces.knight]);
            bishopValueTextField.setValue(settings.pieceValues[Pieces.bishop]);
            rookValueTextField.setValue(settings.pieceValues[Pieces.rook]);
            queenValueTextField.setValue(settings.pieceValues[Pieces.queen]);
            kingValueTextField.setValue(settings.pieceValues[Pieces.king]);
        });

        resignButton.addActionListener(evt -> {
            if(resignPressed){ // Function as a restart.
                debug("Restart signalled.");
                resignPressed = false;
                resignButton.setText("Resign");

                getControllerCallback().restartInterrupt();
            } else {
                debug("Resign signalled.");

                /*resignPressed = true;
                resignButton.setText("Restart Game");*/

                getControllerCallback().resignInterrupt();
            }
        });

        showMoveHighlightsCheckBox.addActionListener(evt -> viewSettings.showMoveHighlights = showMoveHighlightsCheckBox.isSelected());
        showSquareLabelsCheckBox.addActionListener(evt -> viewSettings.labelSquares = showSquareLabelsCheckBox.isSelected());

        valueCentrePawnsCheckBox.addActionListener(evt -> {
            positionScoreLabel.setVisible(!positionScoreLabel.isVisible());
            statsPanel.setVisible(!statsPanel.isVisible());
            settings.valuePosition = !settings.valuePosition;
        });

        beginButton.addActionListener(evt -> {
            synchronized(beginButtonLock){
                    beginButtonLock.notify();
            }
        });
    }

    private final Object beginButtonLock = new Object();

    private StartupPackage loadStartupPackage(){
        boolean whiteHuman = ((String) whiteComboBox.getSelectedItem()).equals("Human");
        boolean blackHuman = ((String) blackComboBox.getSelectedItem()).equals("Human");

        boolean[] playerTypes = new boolean[]{whiteHuman,blackHuman};

        return new StartupPackage(playerTypes, fenField.getText());
    }

    private int candidateMove;
    private void listenForMoves(){
        boardPanel().listenForMoves();
        //return candidateMove;
    }

    private void toggleMenu(){
        debug("Toggling menu...");
        boolean newValue = !beginButton.isVisible();

        if(!newValue) {
            PGN_Pane.setText("");
        }

        beginButton.setVisible(newValue);

        whiteComboBox.setVisible(newValue);
        whiteCBLabel.setVisible(newValue);

        blackComboBox.setVisible(newValue);
        blackCBLabel.setVisible(newValue);

        fenField.setVisible(newValue);
        fenLabel.setVisible(newValue);

        setupLabel.setVisible(newValue);
        boardPanel().setInMenu(newValue);
    }

    public void askForStartup() {
        // The controller requested the startup settings.
        // Once complete, we'll have the start-up settings we need.

        // Wait for the "begin" button to be pressed.
        debug("Waiting on begin button lock...");
        synchronized (beginButtonLock){
            try{
                beginButtonLock.wait();
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }

        debug("Done, toggling menu...");
        // Toggle graphically to the main game UI.
        toggleMenu();

        debug("Done, calling back...");
        // Reply through a callback that can notify the lock.
        getControllerCallback().replyStartup(loadStartupPackage());
    }

    public void askForMove() { // The controller requested the user's move.
        listenForMoves(); // Signal the BoardPanel to start listening for user moves.
    }

    public void updateFromModelData(ModelData o) { // Contains the former "Statistics" information.
        setPGN(o.getPGN());

        this.output = o;

        statsPanel().setStatistics(o.getStatistics());
        boardPanel().setModelData(o);
    }

    public void doView() {
        boardPanel().setViewSettings(viewSettings);
        statsPanel().setStatistics(this.output.getStatistics()); // Forward stats object to sub-component.

        //debug("Calling repaint...");
        //repaint();
    }

    public void setPGN(PGNSequence sequence){
        if(sequence==null)return;
        PGN_Pane.setText(sequence.toString());
    }

    public void repaint(){
        Statistics statistics = output.getStatistics();

        float pct = 100 * (float) statistics.currentTTSize / settings.TTSizeLimit;
        ttSizeBar.setValue(Math.round(pct));
        ttSizeCounter.setText(String.format("%,d/%,d",statistics.currentTTSize, settings.TTSizeLimit));

        String depthString = String.format("Depth: %d/%d", statistics.currentDepth, settings.depthLimit);
        String NPS_String = String.format("%,d N/S",statistics.NPS);
        String branchingFactorString = String.format("~%.1f",statistics.branchingFactor);

        depthLabel.setText(depthString);

        bfLabel.setIcon(Icons.bfIcon);
        npsLabel.setIcon(Icons.npsIcon);

        npsLabel.setText(NPS_String);
        bfLabel.setText(branchingFactorString);

        mainPanel.repaint();
    }

    public void create(){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        JFrame frame = new JFrameBuilder()
                .title("Chess Engine")
                .contentPane(mainPanel)
                .size(768+240+60+240, 768)
                .nonResizable()
                .build();

        startRedrawing();
    }

    private static final int REFRESH_RATE = 175;
    private static final int REFRESH_TIME_MS = Math.round(1000F/REFRESH_RATE);

    private Timer renderingTimer;
    public void startRedrawing(){
        renderingTimer = new Timer(REFRESH_TIME_MS, tickEvent -> repaint());
        renderingTimer.start();
    }

    /**
     * Custom UI component creation for formatted text fields and the main board panel.
     */
    private void createUIComponents() {
        statsPanel = new JStatsPanel();
        mainPanel = new JBoardPanel(this);

        settings = EngineSettings.defaults();

        ttSizeTextField = new JFormattedTextField(settings.TTSizeLimit);
        killerSizeTextField = new JFormattedTextField(settings.n_killers);

        pawnValueTextField = new JFormattedTextField(settings.pieceValues[Pieces.pawn]);
        knightValueTextField = new JFormattedTextField(settings.pieceValues[Pieces.knight]);
        bishopValueTextField = new JFormattedTextField(settings.pieceValues[Pieces.bishop]);
        rookValueTextField = new JFormattedTextField(settings.pieceValues[Pieces.rook]);
        queenValueTextField = new JFormattedTextField(settings.pieceValues[Pieces.queen]);
        kingValueTextField = new JFormattedTextField(settings.pieceValues[Pieces.king]);

        whiteComboBox = new JComboBox();
        whiteComboBox.addItem("Human");
        whiteComboBox.addItem("Engine");

        blackComboBox = new JComboBox();
        blackComboBox.addItem("Human");
        blackComboBox.addItem("Engine");
    }

    public void receiveDirect(float currentEval, int currentDepth, int ttOccupation) {
        statsPanel().receiveDirect(currentEval, currentDepth,ttOccupation);
    }
    public void receiveDirectPerformance(int nps, float ebf) {
        statsPanel().receiveDirectPerformance(nps, ebf);
    }

    public void returnToMenu(){ // Called if "restart" is pressed when the game has ended.
        boardPanel().setGameEnded(false); // A different variable hides the board if we're in the menu, this is only used for the game end string.
        toggleMenu();
    }

    public void displayGameResult(MoveResult gameResult) { // Called when the game ends.
        String display = "";
        switch (gameResult){
            case CHECKMATE_WHITE_WINS -> display = "Checkmate. White wins.";
            case CHECKMATE_BLACK_WINS -> display = "Checkmate. Black wins.";
            case WHITE_RESIGNS -> display = "Black wins by resignation.";
            case BLACK_RESIGNS -> display = "White wins by resignation.";
            case STALEMATE_NORMAL -> display = "Stalemate.";
            case STALEMATE_TFR -> display = "Draw by threefold repetition.";
            case STALEMATE_FMR -> display = "Draw by fifty-move rule.";
        }

        resignPressed = true;
        resignButton.setText("Restart Game");

        boardPanel().setGameEnded(true);
        boardPanel().drawGameResult(display);
    }
}
