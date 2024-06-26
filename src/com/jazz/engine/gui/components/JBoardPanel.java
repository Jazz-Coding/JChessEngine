package com.jazz.engine.gui.components;

import com.jazz.engine.data.Move;
import com.jazz.engine.gui.Colors;
import com.jazz.mvc.ChessView;
import com.jazz.mvc.io.impl.ModelData;
import com.jazz.mvc.io.impl.ViewSettings;
import com.jazz.old.Board;

import javax.swing.JPanel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.GlyphVector;

import java.util.Map;

import static com.jazz.engine.data.Pieces.empty;
import static com.jazz.engine.gui.Fonts.*;

import static com.jazz.engine.utils.bits.BBUtils.ordinalToSqr;
import static com.jazz.mvc.utils.logging.Logger.debug;
import static com.jazz.mvc.utils.ui.UIUtils.mapRange;

/**
 * Chess board rendering JPanel.
 * Currently, pieces are rendered with unicode text strings, board state is updated via setBoard(newBoard).
 */
public class JBoardPanel extends JPanel{
    private ChessView callback;

    private int boardStartX = 0;
    private int boardEndX = 0;
    private int squareSize=75;

    private int boardStartY = 0;
    private int boardEndY = 0;

    private int heldPieceX = -1;
    private int heldPieceY = -1;

    private boolean inMenu = true;

    /**
     * Checks if coordinate (x,y) is within the square {(x1,y1) to (x2,y2)}
     */
    private boolean inBounds(int x, int y,
                             int x1, int x2, int y1, int y2){
        if(x >=x1 && x <=x2){
            if(y >=y1 && y <=y2){
                return true;
            }
        }
        return false;
    }

    /**
     * Translates a mouse from-to coordinate into the underlying move representation.
     */
    private int buildMove(int fromX, int fromY, int toX, int toY){
        byte[][] bytes = o.getBoard().getBoardRepresentation();

        int i1 = 7-fromY;
        int j1 = fromX;

        int i2 = 7-toY;
        int j2 = toX;

        int from = (i1*8)+j1;
        int to = (i2*8)+j2;

        long l_from = ordinalToSqr(from);
        long l_to = ordinalToSqr(to);

        byte originPiece = bytes[i1][j1];
        byte destinationPiece = bytes[i2][j2];


        int opt = Board.oldToNew(originPiece);
        int dpt = Board.oldToNew(destinationPiece);

        boolean isCapture = dpt!= empty;

        int color = Board.oldColor(originPiece);

        int candidateMove = Move.buildMove(0, 0, 0, isCapture, false, false, 0,
                opt, dpt,
                from, to,
                color);
        debug("Built candidate move.");

        return candidateMove;

    }

    public JBoardPanel(ChessView callback) {
        squareSize=75;

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                if(!listeningForMoves) return;

                int mouseX = e.getX();
                int mouseY = e.getY();

                if(inBounds(mouseX,mouseY,boardStartX,boardEndX,boardStartY,boardEndY)){
                    // Associated mouseX and mouseY with a board square.
                    int sqrX = mouseX/squareSize;
                    int sqrY = mouseY/squareSize;

                    if(heldPieceX !=-1 && heldPieceY != -1) {
                        if (!(heldPieceX == sqrX && heldPieceY == sqrY)) {
                            // This defines a new move.
                            int mov = buildMove(heldPieceX, heldPieceY, sqrX, sqrY);
                            if(mov != -1) {
                                debug("Move is not -1, signalling callback for verification.");
                                boolean result = callback.getControllerCallback().replyMove(mov);
                                if(result) {
                                    debug("Move was valid, we're done here");
                                    listeningForMoves = false; // Move was valid, we can stop listening.
                                } else {
                                    debug("Move was invalid! Try again!");
                                }

                                //UIController.getInstance().receiveMove(mov);
                            }
                            heldPieceX = -1;
                            heldPieceY = -1;
                            return;
                        }
                    }

                    heldPieceX=sqrX;
                    heldPieceY=sqrY;

                    if(Board.oldToNew(o.getBoard().getBoardRepresentation()[7-heldPieceY][heldPieceX]) == empty){
                        heldPieceX=-1;
                        heldPieceY=-1;
                    }
                } else {
                    heldPieceX=-1;
                    heldPieceY=-1;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseClicked(e);
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        this.callback = callback;
    }

    private Coordinate pieceOrdToGUISquare(int ord){
        // Convert to i,j indices.
        int i = 7-(ord/8); // Row (rank)
        int j = ord%8; // Column (file)

        // Convert to x,y coordinates based on GUI square size.
        int x = j*squareSize;
        int y = i*squareSize;

        // Move to the centre of the square.
        x+= squareSize/2;
        y+=squareSize/2;

        return new Coordinate(x,y);
    }

    private ViewSettings settings;
    private ModelData o;

    private static final String[] files = new String[]{"a","b","c","d","e","f","g","h"};
    private static final String[] ranks = new String[]{"1","2","3","4","5","6","7","8"};

    public void setModelData(ModelData o){
        this.o=o;
    }

    public void setViewSettings(ViewSettings settings) {
        this.settings = settings;
    }

    @Override
    public void paintComponent (Graphics g) {
        super.paintComponent(g);

        Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Square dimensions.
        int w = squareSize;
        int h = squareSize;

        boardEndX = 8 * w;
        boardEndY = 8 * h;

        int boardWidth = boardEndX-boardStartX;
        int boardMidX = boardWidth/2;

        int boardHeight = boardEndY-boardStartY;
        int boardMidY = boardHeight/2;

        if(inMenu){
            // Display idle animation.
            long t = System.currentTimeMillis()/300;

            boolean color = t %2==1;

            int pos = (int) (t %64);

            int newSize = squareSize;///2;
            int w2 = newSize;
            int h2 = newSize;

            int xOffset = 0;//squareSize*4;
            int yOffset = 0;//squareSize;

            int x = pos%8;
            int y = pos/8;

            g2D.setColor(color ? Color.WHITE : Color.BLACK);
            g2D.fillRect(xOffset+x*w2, yOffset+y *h2,newSize,newSize);
        } else {
            byte[][] bytes = o.getBoard().getBoardRepresentation();

            Map<Byte, Piece> bytePieceMap = Board.bytePieceMap();
            boolean col = false;

            int sx = 0;
            int sy = 0;

            // Board squares.
            for (int j = 0; j < 8; j++) {
                int y = j * h + sy;
                for (int i = 0; i < 8; i++) {
                    int x = i * w + sx;

                    if (col) {
                        g2D.setColor(Colors.SQUARE_DARK);
                    } else {
                        g2D.setColor(Colors.SQUARE_LIGHT);
                    }
                    g2D.fillRect(x, y, w, h);

                    col = !col;
                }
                col = !col;
            }

            // Pieces.
            for (int j = 0; j < 8; j++) {
                int y = j * h + sy;
                for (int i = 0; i < 8; i++) {
                    int x = i * w + sx;

                    Piece piece = bytePieceMap.get(bytes[7 - j][i]);
                    boolean white = piece.isWhite();

                    if (white) {
                        g2D.setColor(Color.WHITE);
                    } else {
                        g2D.setColor(Color.BLACK);
                    }

                    g2D.setFont(large);

                    int xCentre = x + (w / 2);
                    int yCentre = y + (h / 2);

                    if (i == heldPieceX && j == heldPieceY) {
                        xCentre = getMousePosition().x;
                        yCentre = getMousePosition().y;
                    }

                    // TODO: Use a proper approach to rendering pieces.
                    String ascii = piece.getAsciiRepresentation();
                    int tw = g2D.getFontMetrics().stringWidth(ascii);
                    int th = (int) Math.round(g2D.getFontMetrics().getStringBounds(ascii, g2D).getHeight());
                    int xTextCentre = xCentre - tw / 2;
                    int yTextCentre = yCentre - th / 2;

                    g2D.drawString(ascii, xTextCentre, yTextCentre + h);

                    GlyphVector glyphVector = large.createGlyphVector(g2D.getFontMetrics().getFontRenderContext(), ascii);
                    Shape textShape = glyphVector.getOutline();

                    if (white) {
                        g2D.setColor(Color.BLACK);
                    } else {
                        g2D.setColor(Color.WHITE);
                    }
                    g2D.setStroke(new BasicStroke(0.5F));
                    g2D.translate(xTextCentre, yTextCentre + h);
                    g2D.draw(textShape);
                    g2D.translate(-xTextCentre, -yTextCentre - h);


                    if (settings.labelSquares) {
                        g2D.setColor(Color.DARK_GRAY);
                        g2D.setFont(small);
                        String squareLabel = files[i] + ranks[7 - j];
                        g2D.drawString(squareLabel, x, y + h);
                    }

                }
            }

            if (settings.showMoveHighlights) {
                // Draw move indicators (as lines).
                Stroke originalStroke = g2D.getStroke();
                g2D.setStroke(new BasicStroke(4));
                g2D.setColor(Color.LIGHT_GRAY);
                for (int move : o.getLastAvailableMoves()) {
                    int from = Move.getFrom(move);
                    int to = Move.getTo(move);

                    Coordinate fromCoordinate = pieceOrdToGUISquare(from);
                    Coordinate toCoordinate = pieceOrdToGUISquare(to);

                    g2D.drawLine(
                            fromCoordinate.x(), fromCoordinate.y(),
                            toCoordinate.x(), toCoordinate.y());
                }
                g2D.setStroke(originalStroke);
            }

            int barHeight = h * 8;
            int barWidth = w / 4;
            drawEvalBar(g2D, barHeight, barWidth);

            if(gameEnded){
                if(endString.contains("Black")){
                    g2D.setColor(Color.BLACK);
                } else if (endString.contains("White")){
                    g2D.setColor(Color.WHITE);
                } else {
                    g2D.setColor(Color.LIGHT_GRAY);
                }
                drawFullyLargeCentredString(g2D, endString,4*w,4*h);
            }
        }
    }

    private void drawEvalBar(Graphics2D g2D, int barHeight, int barWidth){
        float engineEvaluation = o.getEval();

        float frac = mapRange(engineEvaluation,-10,10,0,1);
        int fracBlack = (int) ((1-frac)*barHeight);

        g2D.setColor(Color.BLACK);

        int barX = 600;
        int barY = 0;

        int barEndY = barY + fracBlack;

        g2D.fillRect(barX,barY,barWidth,fracBlack);

        g2D.setColor(Color.WHITE);
        g2D.fillRect(barX,barEndY,barWidth,barY+barHeight-barEndY);

        g2D.setColor(Color.ORANGE);
        g2D.fillRect(barX,barY+(barHeight/2),barWidth,3);
    }

    public boolean inMenu() {
        return inMenu;
    }

    public void setInMenu(boolean value){
        this.inMenu=value;
    }

    private boolean listeningForMoves;
    public void listenForMoves() {
        listeningForMoves = true;
    }

    private boolean gameEnded = false;
    private String endString = "";

    public void setGameEnded(boolean gameEnded) {
        this.gameEnded = gameEnded;
    }

    public void drawGameResult(String display) {
        endString = display;
    }
}
