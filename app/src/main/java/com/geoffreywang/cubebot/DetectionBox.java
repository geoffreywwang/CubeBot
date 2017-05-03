package com.geoffreywang.cubebot;

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

    public DetectionBox(Point center, int size){
        colorHsv = new Scalar(255);
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
}
