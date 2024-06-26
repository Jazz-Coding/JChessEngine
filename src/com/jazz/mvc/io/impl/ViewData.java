package com.jazz.mvc.io.impl;

/**
 * Input to the model.
 * - The current engine settings.
 */
public class ViewData {
    private EngineSettings settings;
    private int lastMove;

    public ViewData(EngineSettings settings) {
        this.settings = settings;
    }

    public void setSettings(EngineSettings settings) {
        this.settings = settings;
    }

    public EngineSettings getEngineSettings() {
        return settings;
    }

    public int getLastMove() {
        return lastMove;
    }

    public void setLastMove(int lastMove) {
        this.lastMove = lastMove;
    }
}
