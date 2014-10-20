package org.opencv.samples.tutorial2;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

public class SquareFinder extends Activity implements CvCameraViewListener2 {
    private static final String    TAG = "OCVSample::Activity";

    private static final int       VIEW_MODE_RGBA     = 0;
    private static final int       VIEW_MODE_GRAY     = 1;
    private static final int       VIEW_MODE_CANNY    = 2;
    private static final int       VIEW_MODE_FEATURES = 5;

    private int                    mViewMode;
    private Mat                    mRgba;
    private Mat                    mIntermediateMat;
    private Mat                    mGray;
    private Mat					   mLines;

    private MenuItem               mItemPreviewRGBA;
    private MenuItem               mItemPreviewGray;
    private MenuItem               mItemPreviewCanny;
    private MenuItem               mItemPreviewFeatures;

    private CameraBridgeViewBase   mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("mixed_sample");

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public SquareFinder() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.square_finder_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.square_finder_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        //mItemPreviewRGBA = menu.add("Preview RGBA");
        //mItemPreviewGray = menu.add("Preview GRAY");
        //mItemPreviewCanny = menu.add("Canny");
        //mItemPreviewFeatures = menu.add("Find features");
        return true;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
        mLines.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        //final int viewMode = mViewMode;
        //switch (viewMode) {
        //case VIEW_MODE_GRAY:
        //    // input frame has gray scale format
        //    Imgproc.cvtColor(inputFrame.gray(), mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
        //    break;
        //case VIEW_MODE_RGBA:
        //    // input frame has RBGA format
        //    mRgba = inputFrame.rgba();
        //    break;
        //case VIEW_MODE_CANNY:
        //    // input frame has gray scale format
    	
            mRgba = inputFrame.rgba();
            Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 80, 100);
            Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
            Imgproc.HoughLinesP(mIntermediateMat, mLines, 1, Math.PI/180, 50, 20, 20);
            
            for (int x = 0; x < mLines.cols(); x++)
            {
            	double[] vec = mLines.get(0, x);
            	double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
            	Point start = new Point(x1, y1);
            	Point end = new Point(x2, y2);
            	
            	Core.line(mIntermediateMat, start, end, new Scalar(255, 0, 0), 3);
            }
            
            Bitmap mBmp = Bitmap.createBitmap(getWallpaperDesiredMinimumWidth(), getWallpaperDesiredMinimumHeight(), Bitmap.Config.ARGB_8888);
            
            if(Utils.matToBitmap(mIntermediateMat, mBmp)) return mBmp;
            //    break;
        //case VIEW_MODE_FEATURES:
        //    // input frame has RGBA format
        //    mRgba = inputFrame.rgba();
        //    mGray = inputFrame.gray();
        //    FindFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
        //    break;
        //}
        
        //return mRgba;
        //return mIntermediateMat;
    }

	public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        //if (item == mItemPreviewRGBA) {
        //    mViewMode = VIEW_MODE_RGBA;
        //} else if (item == mItemPreviewGray) {
        //    mViewMode = VIEW_MODE_GRAY;
        //} else if (item == mItemPreviewCanny) {
        //    mViewMode = VIEW_MODE_CANNY;
        //} else if (item == mItemPreviewFeatures) {
        //    mViewMode = VIEW_MODE_FEATURES;
        //}

        return true;
    }

    public native void FindFeatures(long matAddrGr, long matAddrRgba);
}
