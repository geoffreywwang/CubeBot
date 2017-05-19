package com.geoffreywang.cubeApp;

import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

/**
 * Created by geoffreywang on 5/2/17.
 */

public class DetectionBox {
    private Scalar colorHsv;
    private Rect rect;
    private Point center;
    private int size;

    public DetectionBox(Point center, int size){
        colorHsv = new Scalar(255);
        this.size = size;

        this.center = center;

        rect = new Rect();
        rect.x = (int)(center.x - size/2);
        rect.y = (int)(center.y - size/2);
        rect.width = size;
        rect.height = size;
    }

    public Scalar getColorHsv() {
        return colorHsv;
    }

    public void setColorHsv(Scalar colorHsv) {
        this.colorHsv = colorHsv;
    }

    public Rect getRect() {
        return rect;
    }

    public Point getTopLeftPoint(){
        return new Point(rect.x,rect.y);
    }

    public Point getBottomRightPoint(){
        return new Point(rect.x+rect.width,rect.y+rect.height);
    }

    public Point getCenter() {
        return center;
    }

    public int getSize() {
        return size;
    }

    public String getColor(){
        Scalar tempRgba = Utils.convertScalarHsv2Rgba(this.getColorHsv());
        double hue = this.getColorHsv().val[0];
        double sat = this.getColorHsv().val[1];
        double val = this.getColorHsv().val[2];
        double red = tempRgba.val[0];
        double blue = tempRgba.val[1];
        double green = tempRgba.val[2];

        String tempString = "";
        if (sat < 100){
            tempString = "W";
        }else if(hue > 28 && hue < 40){
            tempString = "Y";
        }else if(hue > 145 && hue < 160){
            tempString = "B";
        }else if(hue > 100 && hue < 120){
            tempString = "G";
        }else if(hue > 355 || hue < 10){
            tempString = "R";
        }else if(hue > 10 && hue < 25){
            tempString = "O";
        }

        return tempString;
    }
}
