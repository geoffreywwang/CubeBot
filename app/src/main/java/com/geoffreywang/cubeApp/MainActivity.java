package com.geoffreywang.cubeApp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Handler;

import com.cs0x7f.min2phase.Main;
import com.cs0x7f.min2phase.Search;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.geoffreywang.cubeTranslator.Hand;
import com.geoffreywang.cubeTranslator.Solver;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    //Constants for serial connection
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    //For serial connection
    UsbDevice device;
    UsbDeviceConnection connection;
    UsbManager usbManager;
    UsbSerialDevice serialPort;
    PendingIntent pendingIntent;
    boolean isSerialStarted;

    //For camera stuff
    private CameraBridgeViewBase mOpenCvCameraView;
    private TextView textView;
    private Mat mRgba;
    private final Handler handler = new Handler();
    private String[] faces;
    private int scanCount = 0;
    private int leftColor, rightColor;

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
        isSerialStarted = false;

        pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(broadcastReceiver, filter);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textView = (TextView) findViewById(R.id.textView);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        //Start search
        new Thread(new Runnable() {
            public void run() {
                if (!Search.isInited()) {
                    Search.init();
                }
            }
        }).start();


        //USED TO ROTATE SOMETHING 90º
//        Animation rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotation);
//        LayoutAnimationController animController = new LayoutAnimationController(rotateAnim, 0);
//        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
//        layout.setLayoutAnimation(animController);

//        final Button button = (Button) findViewById(R.id.buttonStart);
//        button.setText("Button");
//        button.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                if (!isSerialStarted) {
//                    usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
//
//                    HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
//                    if (!usbDevices.isEmpty()) {
//                        boolean keep = true;
//                        for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
//                            device = entry.getValue();
//                            int deviceVID = device.getVendorId();
//
//                            if (deviceVID == 1027 || deviceVID == 9025) { //Arduino Vendor ID
//                                usbManager.requestPermission(device, pendingIntent);
//                                keep = false;
//                            } else {
//                                connection = null;
//                                device = null;
//                            }
//                            if (!keep)
//                                break;
//                        }
//                    }
//                }
////                if(Search.isInited()) {
////                    String cubeString = "YYYYWYYWWGRGWOGWBWGOORBRYBGBORGYWRGBRBRYRBBOOOOBRGGOWW";
////                    Long tempTime = SystemClock.currentThreadTimeMillis();
////                    Log.i("CubeFace", Main.solveCube(cubeString));
////                    Log.i("CubeFace", SystemClock.currentThreadTimeMillis() - tempTime + "");
////                }else{
////                    Log.i("CubeFace", "Search feature is not ready!");
////                }
////                saveFace();
//            }
//        });

        faces = new String[6];
        autoRefresh();
    }

    //Initializes the serial communication
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) {
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERMISSION NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {

            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                //can add something to close the connection
            }
        }
    };

    //Callback for receiving data
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
//                data.concat("/n");
                if(!data.isEmpty() && !data.equals("")) {
                    if(data.equals("0")){
                        int arrayIndex = saveFace();
                        scanCount ++;
                        if(scanCount == 1){
                            leftColor = Solver.convertColorFromText(faces[arrayIndex].substring(4,5));
                        }else if(scanCount == 3){
                            rightColor = Solver.convertColorFromText(faces[arrayIndex].substring(4,5));
                        }
                    }
                    tvAppend(textView, data);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    //Put text on screen
    private void tvAppend(final TextView tv, final CharSequence text) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                if (text != null) {
//                    tv.setText(text);
                    tv.append(text);
                    if (tv.getText().length() > 10){
                        tv.setText(tv.getText().subSequence(tv.getText().length()-10,tv.getText().length()));
                    }
                }
            }
        });
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
//                    textView.setText("Color: " + boxes.get(0).getColorHsv().val[0] + ',' + boxes.get(0).getColorHsv().val[1] + ',' + boxes.get(0).getColorHsv().val[2]);
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
                Log.i("CubeFace", tempString);
                tempString = "";
            }
        }
    }

    private int saveFace(){
        String tempString = "";
        for (int i = 0; i < boxes.size(); i++) {
            DetectionBox box = boxes.get(i);
            tempString += box.getColor();
        }
        int index = 0;
        if(tempString.substring(4,5).equals("W")){
            index = 0;
        }else if(tempString.substring(4,5).equals("R")){
            index = 1;
        }else if(tempString.substring(4,5).equals("G")){
            index = 2;
        }else if(tempString.substring(4,5).equals("Y")){
            index = 3;
        }else if(tempString.substring(4,5).equals("O")){
            index = 4;
        }else if(tempString.substring(4,5).equals("B")){
            index = 5;
        }

        faces[index] = tempString;

        return index;
    }

    public void onClickConnect(View view) {
        if (!isSerialStarted) {
            usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
            if (!usbDevices.isEmpty()) {
                boolean keep = true;
                for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                    device = entry.getValue();
                    int deviceVID = device.getVendorId();

                    if (deviceVID == 1027 || deviceVID == 9025) { //Arduino Vendor ID
                        usbManager.requestPermission(device, pendingIntent);
                        keep = false;
                    } else {
                        connection = null;
                        device = null;
                    }
                    if (!keep)
                        break;
                }
            }
            isSerialStarted = true;
        }
    }

    public void onClickOpen(View view) {
        if(isSerialStarted) {
            String textInput = "04|";
            serialPort.write(textInput.getBytes());
        }
    }

    public void onClickClose(View view) {
        if(isSerialStarted) {
            String textInput = "15|";
            serialPort.write(textInput.getBytes());
        }
    }

    public void onClickScan(View view) {
        if(isSerialStarted) {
            scanCount = 0;
            String textInput = "0|6|8|7|7|8|1|4|62|8|3|3|8|5|0|62|8|7|7|8|1|";
            serialPort.write(textInput.getBytes());
        }
    }

    public void onClickSolve(View view) {
        String tempString = "";
        for (int i = 0; i < faces.length; i++) {
            tempString += faces[i];
        }
        Log.i("CubeFace", tempString);
        if(isSerialStarted && tempString.length() == 54) {
            String solution = Main.solveCube(tempString);
            if(solution.contains("Error")){
                textView.setText(solution);
            }else{
                Solver solver = new Solver(leftColor, rightColor, solution);
                String moveCode = solver.generateSolution();
                serialPort.write(moveCode.getBytes());
            }
        }
    }
}