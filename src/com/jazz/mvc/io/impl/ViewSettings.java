package com.jazz.mvc.io.impl;

/**
 * View settings (i.e. purely visual settings).
 */
public class ViewSettings {
    public boolean showMoveHighlights;
    public boolean labelSquares;

    public ViewSettings(boolean showMoveHighlights, boolean labelSquares) {
        this.showMoveHighlights = showMoveHighlights;
        this.labelSquares = labelSquares;
    }

    public static ViewSettings defaults(){
        return new ViewSettings(false,false);
    }
}
