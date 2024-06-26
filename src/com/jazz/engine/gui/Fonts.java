package com.jazz.engine.gui;

import java.awt.*;

public class Fonts {
    public static Font small = new Font("",Font.PLAIN,14);
    public static Font small_italic = new Font("",Font.ITALIC,14);
    public static Font small_bold = new Font("",Font.BOLD,14);

    public static Font medium = new Font("",Font.PLAIN,25);
    public static Font medium_italic = new Font("",Font.ITALIC,25);
    public static Font medium_bold = new Font("",Font.BOLD,25);

    public static Font mediumBigger = new Font("",Font.ITALIC,50);

    public static Font large = new Font("",Font.PLAIN,75);
    public static Font large_italic = new Font("",Font.ITALIC,75);
    public static Font large_bold = new Font("",Font.BOLD,75);

    private static void drawString(Graphics2D g2D, String text, int x, int y, Font font){
        g2D.setFont(font);
        int stringHeight = g2D.getFontMetrics().getHeight();
        int adjustment = stringHeight/2;

        g2D.drawString(text,x,y+adjustment);
        g2D.setFont(medium);
    }

    public static void drawFullyLargeCentredString(Graphics2D g2D, String text, int x, int y){
        g2D.setFont(mediumBigger);
        FontMetrics metrics = g2D.getFontMetrics();
        int stringWidth = metrics.stringWidth(text);
        int stringHeight = metrics.getHeight();

        int adjustmentX = stringWidth/2;
        int adjustmentY = stringHeight/2;

        g2D.drawString(text,x-adjustmentX,y+adjustmentY);
        g2D.setFont(medium);
    }

    public static void drawSmallString(Graphics2D g2D, String text, int x, int y){
        drawString(g2D,text,x,y,small);
    }

    public static void drawMediumString(Graphics2D g2D, String text, int x, int y){
        drawString(g2D,text,x,y,medium);
    }

    public static void drawLargeString(Graphics2D g2D, String text, int x, int y){
        drawString(g2D,text,x,y,large);
    }

    public static void drawCustomString(Graphics2D g2D, String text, int x, int y, int fontSize){
        drawString(g2D,text,x,y,new Font("",Font.PLAIN,fontSize));
    }
}
