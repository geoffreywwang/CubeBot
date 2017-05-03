package com.geoffreywang.cubebot;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.WindowManager;
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
    private Point[] boxLocations = {new Point(0.5,0.5),new Point(0.5,0.25),new Point(0.5,0.75),new Point(0.25,0.5),new Point(0.75,0.5)};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textView = (TextView) findViewById(R.id.textView);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

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
            boxes = new ArrayList<>();
            for(Point boxLocation: boxLocations){
                boxes.add(new DetectionBox(new Point(boxLocation.x*mRgba.width(),boxLocation.y*mRgba.height()),100));
            }
        }
        processColor();
        drawOnFrame();
        return mRgba;
    }

    private Scalar convertScalarHsv2Rgba(Scalar hsvColor){
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1,1, CvType.CV_8UC3,hsvColor);
        Imgproc.cvtColor(pointMatHsv,pointMatRgba,Imgproc.COLOR_HSV2RGB_FULL,4);
        return  new Scalar(pointMatRgba.get(0,0));
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
        for(DetectionBox box : boxes) {
            Scalar tempRgba = convertScalarHsv2Rgba(box.getColorHsv());
            double hue = box.getColorHsv().val[0];
            double sat = box.getColorHsv().val[1];
            double val = box.getColorHsv().val[2];
            double red = tempRgba.val[0];
            double blue = tempRgba.val[1];
            double green = tempRgba.val[2];

            String tempString = "";
            if(sat < 20 && val > 200){
                tempString = "W";
            }else if(hue > 28 && hue < 40){
                tempString = "Y";
            }else if(hue > 170 && hue < 240){
                tempString = "B";
            }else if(hue > 80 && hue < 140){
                tempString = "G";
            }else if(hue > 355 || hue < 10){
                tempString = "R";
            }else if(hue > 20 && hue < 40){
                tempString = "O";
            }else{
                tempString = "N/A";
            }

            Imgproc.rectangle(mRgba, box.getTopLeftPoint(), box.getBottomRightPoint(), tempRgba, 3);
            Imgproc.putText(mRgba,tempString,box.getCenter(),1,10,new Scalar(0,0,255,255),10);
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
}