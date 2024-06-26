package com.jazz.engine.gui.components;

/**
 * GUI's representation of a piece.
 * TODO: Enable the use of image icons instead of unicode.
 */
public class Piece {
    private String name;
    private String colour;
    private byte internalRepresentation;
    private String asciiRepresentation;

    private boolean selected;

    public Piece(String name, String colour, byte internalRepresentation, String asciiRepresentation) {
        this.name = name;
        this.colour = colour;
        this.internalRepresentation = internalRepresentation;
        this.asciiRepresentation = asciiRepresentation;

        this.selected = false;
    }

    public String getName() {
        return name;
    }

    public String getColour() {
        return colour;
    }
    public boolean isWhite(){
        return colour.equals("White");
    }

    public byte getInternalRepresentation() {
        return internalRepresentation;
    }

    public String getAsciiRepresentation() {
        return asciiRepresentation;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
