package com.jazz.engine.gui.components;

import javax.swing.*;

/**
 * Utility class for assembling a one-window JFrame.
 */
public class JFrameBuilder {
    private JFrame frame;

    public JFrameBuilder() {
        this.frame = new JFrame();
    }

    public JFrameBuilder title(String title){
        frame.setTitle(title);
        return this;
    }

    public JFrameBuilder size(int width, int height){
        frame.setSize(width,height);
        return this;
    }

    public JFrameBuilder contentPane(JPanel panel){
        frame.setContentPane(panel);
        return this;
    }

    public JFrameBuilder resizable(){
        frame.setResizable(true);
        return this;
    }

    public JFrameBuilder nonResizable(){
        frame.setResizable(false);
        return this;
    }

    public JFrame build(){
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return frame;
    }
}
