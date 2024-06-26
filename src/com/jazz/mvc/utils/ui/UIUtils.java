package com.jazz.mvc.utils.ui;

public class UIUtils {
    /**
     * Checks if coordinate (x,y) is within the square {(x1,y1) to (x2,y2)}
     */
    public static boolean inBounds(int x, int y,
                                   int x1, int x2, int y1, int y2){
        if(x >=x1 && x <=x2){
            if(y >=y1 && y <=y2){
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a variable ranging from (fromMin, fromMax) to (toMin, toMax).
     * e.g. we know a value ranges from 0-100, but would like to scale it to between 0-1 (normalization).
     */
    public static float mapRange(float value,
                                 float fromMin, float fromMax,
                                 float toMin, float toMax){
        float boundedValue = Math.min(Math.max(value, fromMin), fromMax); // Hard caps to fromMin -> fromMax

        float fromRange = (fromMax-fromMin);
        float toRange = (toMax-toMin);

        float scaling = toRange/fromRange;
        float adjusting = toMin-fromMin;

        return (boundedValue+adjusting)*scaling;
    }
}
