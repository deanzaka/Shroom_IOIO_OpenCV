package org.opencv.samples.colorblobdetect;

import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;

public class ColorBlobDetectionActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG              = "OCVSample::Activity";

    private boolean              mIsColorBase = false;
    private boolean              mIsColorFirst = false;
    private boolean              mIsColorSecond = false;
    private boolean              mIsColorThird = false;
    private boolean              mIsColorFourth = false;
    
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    
    private ColorBlobDetector    mDetectorBase;
    private Mat                  mSpectrumBase;
    private Size                 SPECTRUM_SIZE_BASE;
    private Scalar               CONTOUR_COLOR_BASE;
    
    private ColorBlobDetector    mDetectorFirst;
    private Mat                  mSpectrumFirst;
    private Size                 SPECTRUM_SIZE_FIRST;
    private Scalar               CONTOUR_COLOR_FIRST;
    
    private ColorBlobDetector    mDetectorSecond;
    private Mat                  mSpectrumSecond;
    private Size                 SPECTRUM_SIZE_SECOND;
    private Scalar               CONTOUR_COLOR_SECOND;
    
    private ColorBlobDetector    mDetectorThird;
    private Mat                  mSpectrumThird;
    private Size                 SPECTRUM_SIZE_THIRD;
    private Scalar               CONTOUR_COLOR_THIRD;
    
    private ColorBlobDetector    mDetectorFourth;
    private Mat                  mSpectrumFourth;
    private Size                 SPECTRUM_SIZE_FOURTH;
    private Scalar               CONTOUR_COLOR_FOURTH;
    
    private static final int       VIEW_BASE    = 0;
    private static final int       VIEW_FIRST   = 1;
    private static final int       VIEW_SECOND  = 2;
    private static final int       VIEW_THIRD   = 3;
    private static final int	   VIEW_FOURTH  = 4;

    private int                    mViewMode;
    
    private MenuItem			 mBaseSelector;
    private MenuItem			 mFirstSelector;
    private MenuItem			 mSecondSelector;
    private MenuItem			 mThirdSelector;
    private MenuItem			 mFourthSelector;
    
    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ColorBlobDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detection_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mBaseSelector = menu.add("Base");
        mFirstSelector = menu.add("1");
        mSecondSelector = menu.add("2");
        mThirdSelector = menu.add("3");
        mFourthSelector = menu.add("4");
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
        
        mDetectorBase = new ColorBlobDetector();
        mSpectrumBase = new Mat();
        SPECTRUM_SIZE_BASE = new Size(200, 64);
        CONTOUR_COLOR_BASE = new Scalar(255,0,0,255);
        
        mDetectorFirst = new ColorBlobDetector();
        mSpectrumFirst = new Mat();
        SPECTRUM_SIZE_FIRST = new Size(200, 64);
        CONTOUR_COLOR_FIRST = new Scalar(255,0,0,255);
        
        mDetectorSecond = new ColorBlobDetector();
        mSpectrumSecond = new Mat();
        SPECTRUM_SIZE_SECOND = new Size(200, 64);
        CONTOUR_COLOR_SECOND = new Scalar(255,0,0,255);
        
        mDetectorThird = new ColorBlobDetector();
        mSpectrumThird = new Mat();
        SPECTRUM_SIZE_THIRD = new Size(200, 64);
        CONTOUR_COLOR_THIRD = new Scalar(255,0,0,255);
        
        mDetectorFourth = new ColorBlobDetector();
        mSpectrumFourth = new Mat();
        SPECTRUM_SIZE_FOURTH = new Size(200, 64);
        CONTOUR_COLOR_FOURTH = new Scalar(255,0,0,255);
        
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");
        
        final int viewMode = mViewMode;
        switch (viewMode) {
        	case VIEW_BASE:
        		mDetectorBase.setHsvColor(mBlobColorHsv);
                Imgproc.resize(mDetectorBase.getSpectrum(), mSpectrumBase, SPECTRUM_SIZE_BASE);
                mIsColorBase = true;
                break;
        	case VIEW_FIRST:
        		mDetectorFirst.setHsvColor(mBlobColorHsv);
                Imgproc.resize(mDetectorFirst.getSpectrum(), mSpectrumFirst, SPECTRUM_SIZE_FIRST);
                mIsColorFirst = true;
                break;    
        	case VIEW_SECOND:
        		mDetectorSecond.setHsvColor(mBlobColorHsv);
                Imgproc.resize(mDetectorSecond.getSpectrum(), mSpectrumSecond, SPECTRUM_SIZE_SECOND);
                mIsColorSecond = true;
                break;
        	case VIEW_THIRD:
        		mDetectorThird.setHsvColor(mBlobColorHsv);
                Imgproc.resize(mDetectorThird.getSpectrum(), mSpectrumThird, SPECTRUM_SIZE_THIRD);
                mIsColorThird = true;
                break;
        	case VIEW_FOURTH:
        		mDetectorFourth.setHsvColor(mBlobColorHsv);
                Imgproc.resize(mDetectorFourth.getSpectrum(), mSpectrumFourth, SPECTRUM_SIZE_FOURTH);
                mIsColorFourth = true;
                break;
        }
        
        

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        final int viewMode = mViewMode;
        switch (viewMode) {
	        case VIEW_BASE:
		        if (mIsColorBase) {
		            mDetectorBase.process(mRgba);
		            List<MatOfPoint> contours = mDetectorBase.getContours();
		            Log.e(TAG, "Contours count: " + contours.size());
		            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR_BASE);
		
		            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
		            colorLabel.setTo(mBlobColorRgba);
		
		            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrumBase.rows(), 70, 70 + mSpectrumBase.cols());
		            mSpectrumBase.copyTo(spectrumLabel);
		            
		        }
		        break;
	        case VIEW_FIRST:
	        	if (mIsColorFirst) {
		            mDetectorFirst.process(mRgba);
		            List<MatOfPoint> contours = mDetectorFirst.getContours();
		            Log.e(TAG, "Contours count: " + contours.size());
		            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR_FIRST);
		
		            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
		            colorLabel.setTo(mBlobColorRgba);
		
		            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrumFirst.rows(), 70, 70 + mSpectrumFirst.cols());
		            mSpectrumFirst.copyTo(spectrumLabel);
		        }
		        break;
	        case VIEW_SECOND:
	        	if (mIsColorSecond) {
		            mDetectorSecond.process(mRgba);
		            List<MatOfPoint> contours = mDetectorSecond.getContours();
		            Log.e(TAG, "Contours count: " + contours.size());
		            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR_SECOND);
		
		            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
		            colorLabel.setTo(mBlobColorRgba);
		
		            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrumSecond.rows(), 70, 70 + mSpectrumSecond.cols());
		            mSpectrumSecond.copyTo(spectrumLabel);
		        }
		        break;
	        case VIEW_THIRD:
	        	if (mIsColorThird) {
		            mDetectorThird.process(mRgba);
		            List<MatOfPoint> contours = mDetectorThird.getContours();
		            Log.e(TAG, "Contours count: " + contours.size());
		            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR_THIRD);
		
		            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
		            colorLabel.setTo(mBlobColorRgba);
		
		            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrumThird.rows(), 70, 70 + mSpectrumThird.cols());
		            mSpectrumThird.copyTo(spectrumLabel);
		        }
		        break;
	        case VIEW_FOURTH:
	        	if (mIsColorFourth) {
		            mDetectorFourth.process(mRgba);
		            List<MatOfPoint> contours = mDetectorFourth.getContours();
		            Log.e(TAG, "Contours count: " + contours.size());
		            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR_FOURTH);
		
		            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
		            colorLabel.setTo(mBlobColorRgba);
		
		            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrumFourth.rows(), 70, 70 + mSpectrumFourth.cols());
		            mSpectrumFourth.copyTo(spectrumLabel);
		        }
		        break;
        }

        return mRgba;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mBaseSelector) {
            mViewMode = VIEW_BASE;
        } else if (item == mFirstSelector) {
            mViewMode = VIEW_FIRST;
        } else if (item == mSecondSelector) {
            mViewMode = VIEW_SECOND;
        } else if (item == mThirdSelector) {
            mViewMode = VIEW_THIRD;
        } else if (item == mFourthSelector) {
        	mViewMode = VIEW_FOURTH;
        }

        return true;
    }
}
