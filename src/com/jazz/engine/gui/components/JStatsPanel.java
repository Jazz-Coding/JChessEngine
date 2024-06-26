package com.jazz.engine.gui.components;

import com.jazz.engine.gui.Fonts;
import com.jazz.engine.gui.Statistics;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import static com.jazz.engine.ChessEngine.BLACK;
import static com.jazz.engine.ChessEngine.WHITE;

public class JStatsPanel extends JPanel {
    private int width=0;
    private int height=0;

    private int marginX = 5;
    private int marginY = 5;

    private boolean init = true;
    private Statistics statistics = Statistics.initial();

    private void init(){
        width=getWidth()-1;
        height=getHeight()-1;
    }

    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    public void receiveDirect(float currentEval, int currentDepth, int ttOccupation) {
        this.statistics.lastEval = currentEval;
        this.statistics.currentDepth = currentDepth;
        this.statistics.currentTTSize = ttOccupation;
    }

    public void receiveDirectPerformance(int nps, float ebf) {
        this.statistics.NPS = nps;
        this.statistics.branchingFactor = ebf;
    }

    private void drawString(Graphics2D g2D, String text, int x, int y, int fontSize){
        Fonts.drawCustomString(g2D,text,x+marginX,y+marginY,fontSize);
    }

    @Override
    public void paintComponent (Graphics g) {
        super.paintComponent(g);

        Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if(init){
            init();
            init=false;
        }

        g2D.setColor(Color.BLACK);
        g2D.drawRect(0,0, width, height);

        //drawString(g2D,"Piece Positional Values",0,0,12);

        int col1X = 0;
        int col2X = getWidth()/2;
        drawString(g2D,"White",col1X,0,16);
        drawString(g2D,"Black",col2X,0,16);

        int colY = 16;
        for (int i = 0; i < 6; i++) {
            float whitePiecePositionalScore = statistics.positionalScores[WHITE][i];
            float blackPiecePositionalScore = statistics.positionalScores[BLACK][i];

            drawString(g2D, String.valueOf(whitePiecePositionalScore),col1X,colY,16);
            drawString(g2D, String.valueOf(blackPiecePositionalScore),col2X,colY,16);

            colY+=16;
        }
    }
}
