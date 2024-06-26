package com.jazz.old;

public class Position {
    private final int rank;
    private final int file;

    public Position(int rank, int file) {
        this.rank = rank;
        this.file = file;
    }

    /**
     * Creating and outputting positions as human-readable strings.
     * e.g. (0,0) corresponds to the square "a1".
     */
    public Position(String string) {
        this.file = ((int) string.charAt(0)) - 97;
        this.rank = Integer.parseInt(String.valueOf(string.charAt(1)));
    }
    @Override
    public String toString() {
        return String.valueOf((char) (file + 97)) + (rank + 1);
    }

    public int getRank() {
        return rank;
    }

    public int getFile() {
        return file;
    }

    private int squareOrdinal(){
        return (rank*8)+file;
    }

    @Override
    public int hashCode() {
        return squareOrdinal();
    }
}
