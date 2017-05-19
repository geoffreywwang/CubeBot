package com.geoffreywang.cubeTranslator;

/**
 * Created by geoffrey_wang on 5/8/17.
 */
public class Hand {
    //CONSTANTS
    public static final int WHITE = 0, BLUE = 1, RED = 2, GREEN = 3, ORANGE = 4, YELLOW = 5;
    public static final int MC_OPEN = 0, MC_CLOSE = 1, MC_CLOCKWISE = 2, MC_COUNTER = 3;
    public static final Integer[]
            colorsBG = {WHITE, RED, YELLOW, ORANGE},
            colorsRO = {WHITE, GREEN, YELLOW, BLUE},
            colorsWY = {BLUE, ORANGE, GREEN, RED};

    //Instance Fields
    private boolean isClosed, isInverted;
    private int offset;
    private int color;

    //Everything constructor
    public Hand(boolean isClosed, boolean isInverted, int offset, int color) {
        this.isClosed = isClosed;
        this.offset = offset;
        this.isInverted = isInverted;
        this.color = color;
    }

    //Default constructor
    public Hand(int color, int offset){
        this(true, false, offset, color);
    }

    @Override
    public String toString() {
        if(color == WHITE){
            return "WHITE";
        }else if(color == YELLOW){
            return "YELLOW";
        }else if(color == RED){
            return "RED";
        }else if(color == ORANGE){
            return "ORANGE";
        }else if(color == BLUE){
            return "BLUE";
        }else{ //GREEN
            return "GREEN";
        }
    }

    //GETTERS
    public boolean isInverted() {
        return isInverted;
    }

    public int getColor() {
        return color;
    }

    public Integer[] getColorArray(){
        if(color == Hand.BLUE || color == Hand.GREEN){
            return Hand.colorsBG;
        }else if(color == Hand.RED || color == Hand.ORANGE){
            return Hand.colorsRO;
        }else { //(color == Converter.Hand.WHITE || color == Converter.Hand.YELLOW)
            return Hand.colorsWY;
        }
    }

    //SETTERS
    public void setColor(int color) {
        this.color = color;
    }


    // ===== Hand movement methods =====
    public String open(){
        isClosed = false;
        return MC_OPEN + offset + "";
    }

    public String close(){
        isClosed = true;
        return  MC_CLOSE + offset + "";
    }

    public String turnClockwise(){
        isInverted = !isInverted;
        return MC_CLOCKWISE + offset + "";
    }

    public String turnCounter(){
        isInverted = !isInverted;
        return MC_COUNTER + offset + "";
    }

    public String turnDouble(){
        return turnClockwise() + "|" + turnClockwise();
    }
}
