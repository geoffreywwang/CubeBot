package com.geoffreywang.cubebot;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase mOpenCvCameraView;
    private TextView textView;
    private Mat mRgba;
    private final Handler handler = new Handler();

    private ArrayList<DetectionBox> boxes;
    /**
     * Box Location Diagram:
     *
     *      |       3
     *      |   2       6
     *      |1      5       9
     *      |   4       8
     *      |       7
     */
    private Point[] boxLocations = {
            new Point(-1,0),new Point(-0.5,-0.5),new Point(0,-1),
            new Point(-0.5,0.5),new Point(0,0),new Point(0.5,-0.5),
            new Point(0,1),new Point(0.5,0.5),new Point(1,0)};
    private int boxLayoutDistance = 600;
    private int boxSize = 110;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textView = (TextView) findViewById(R.id.textView);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                printFace();
            }
        });

        autoRefresh();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onPause(){
        super.onPause();
        if (mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0,this,mLoaderCallback);
        }else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int i, int i1) {
        mRgba = new Mat();

    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame cvCameraViewFrame) {
        mRgba = cvCameraViewFrame.rgba();
        if(boxes == null){
            Point tempCenter = new Point(mRgba.width()/2,mRgba.height()/2);
            boxes = new ArrayList<>();
            for(Point boxLocation: boxLocations){
                boxes.add(new DetectionBox(new Point(boxLocation.x*boxLayoutDistance+tempCenter.x,boxLocation.y*boxLayoutDistance+tempCenter.y),boxSize));
            }
        }
        processColor();
        drawOnFrame();
        return mRgba;
    }

    private void processColor(){
        for(DetectionBox box : boxes) {
            Mat regionRgba = mRgba.submat(box.getRect());
            Mat regionHsv = new Mat();
            Imgproc.cvtColor(regionRgba, regionHsv, Imgproc.COLOR_RGB2HSV_FULL);
            Scalar tempHsv = Core.sumElems(regionHsv);
            int pointCount = box.getRect().width * box.getRect().height;
            for (int i = 0; i < tempHsv.val.length; i++) {
                tempHsv.val[i] /= pointCount;
            }
            box.setColorHsv(tempHsv);
        }
    }

    private void drawOnFrame(){
        for (int i = 0; i < boxes.size(); i++) {
            DetectionBox box = boxes.get(i);

            Point textDrawPoint = new Point(box.getCenter().x-box.getSize(),box.getCenter().y+box.getSize());
            Imgproc.rectangle(mRgba, box.getTopLeftPoint(), box.getBottomRightPoint(), new Scalar(255,0,0,255), 4);
            Imgproc.putText(mRgba,(i+1)+":"+box.getColor(),textDrawPoint,1,6,new Scalar(0,0,255,255),10);
        }
    }

    private void autoRefresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(boxes != null) {
                    textView.setText("Color: " + boxes.get(0).getColorHsv().val[0] + ',' + boxes.get(0).getColorHsv().val[1] + ',' + boxes.get(0).getColorHsv().val[2]);
                    textView.setTextColor(Color.BLUE);
                }
                autoRefresh();
            }
        }, 100);
    }

    private void printFace() {
        String tempString = "";
        for (int i = 0; i < boxes.size(); i++) {
            DetectionBox box = boxes.get(i);
            tempString += box.getColor() + " ";
            if(i%3==2){
                Log.d("CubeFace", tempString);
                tempString = "";
            }
        }
    }
}