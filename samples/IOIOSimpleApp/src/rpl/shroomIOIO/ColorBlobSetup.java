package rpl.shroomIOIO;

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
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class ColorBlobSetup extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG              = "OCVSample::Activity";
    
    String SHROOM_PREF = IOIOSimpleApp.SHROOM_PREF;
	SharedPreferences shroomPrefs = IOIOSimpleApp.shroomPrefs;
	
    public static boolean              mIsColorBase;
    public static boolean              mIsColorFirst;
    public static boolean              mIsColorSecond;
    public static boolean              mIsColorThird;
    public static boolean              mIsColorFourth;
    
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private int					 pointCount;
    
    public static ColorBlobDetector    mDetectorBase;
    public static Mat                  mSpectrumBase;
    public static Size                 SPECTRUM_SIZE_BASE;
    public static Scalar               CONTOUR_COLOR_BASE;
    
    public static ColorBlobDetector    mDetectorFirst;
    public static Mat                  mSpectrumFirst;
    public static Size                 SPECTRUM_SIZE_FIRST;
    public static Scalar               CONTOUR_COLOR_FIRST;
    
    public static ColorBlobDetector    mDetectorSecond;
    public static Mat                  mSpectrumSecond;
    public static Size                 SPECTRUM_SIZE_SECOND;
    public static Scalar               CONTOUR_COLOR_SECOND;
    
    public static ColorBlobDetector    mDetectorThird;
    public static Mat                  mSpectrumThird;
    public static Size                 SPECTRUM_SIZE_THIRD;
    public static Scalar               CONTOUR_COLOR_THIRD;
    
    public static ColorBlobDetector    mDetectorFourth;
    public static Mat                  mSpectrumFourth;
    public static Size                 SPECTRUM_SIZE_FOURTH;
    public static Scalar               CONTOUR_COLOR_FOURTH;
    
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
    
    //private TextView basePixel_;
    
    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ColorBlobSetup.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ColorBlobSetup() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_setup);
        
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_setup);
        mOpenCvCameraView.setCvCameraViewListener(this);
        
        //mCamera.set(Highgui.CV_CAP_PROP_ANDROID_FLASH_MODE, Highgui.CV_CAP_ANDROID_FLASH_MODE_TORCH);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mBaseSelector = menu.add("Base");
        mFirstSelector = menu.add("1");
        mSecondSelector = menu.add("2");
        mThirdSelector = menu.add("3");
        mFourthSelector = menu.add("4");
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.i(TAG, "called onPrepareOptionsMenu");
        return super.onPrepareOptionsMenu(menu);
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
    
    public void onStop() {
    	super.onStop();
    }
    
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        
        shroomPrefs = getSharedPreferences(SHROOM_PREF, 0);
        mIsColorBase = shroomPrefs.getBoolean("condBase", false);
        mIsColorFirst = shroomPrefs.getBoolean("condFirst", false);
        mIsColorSecond = shroomPrefs.getBoolean("condSecond", false);
        mIsColorThird = shroomPrefs.getBoolean("condThird", false);
        mIsColorFourth = shroomPrefs.getBoolean("condFourth", false);
        
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
        
        mDetectorBase.mUpperBound.val[0] = (double) shroomPrefs.getFloat("BaseU0", 0);
        mDetectorBase.mUpperBound.val[1] = (double) shroomPrefs.getFloat("BaseU1", 0);
        mDetectorBase.mUpperBound.val[2] = (double) shroomPrefs.getFloat("BaseU2", 0);
        mDetectorBase.mUpperBound.val[3] = (double) shroomPrefs.getFloat("BaseU3", 0);
        mDetectorBase.mLowerBound.val[0] = (double) shroomPrefs.getFloat("BaseL0", 0);
        mDetectorBase.mLowerBound.val[1] = (double) shroomPrefs.getFloat("BaseL1", 0);
        mDetectorBase.mLowerBound.val[2] = (double) shroomPrefs.getFloat("BaseL2", 0);
        mDetectorBase.mLowerBound.val[3] = (double) shroomPrefs.getFloat("BaseL3", 0);
        
        mDetectorFirst.mUpperBound.val[0] = (double) shroomPrefs.getFloat("FirstU0", 0);
        mDetectorFirst.mUpperBound.val[1] = (double) shroomPrefs.getFloat("FirstU1", 0);
        mDetectorFirst.mUpperBound.val[2] = (double) shroomPrefs.getFloat("FirstU2", 0);
        mDetectorFirst.mUpperBound.val[3] = (double) shroomPrefs.getFloat("FirstU3", 0);
        mDetectorFirst.mLowerBound.val[0] = (double) shroomPrefs.getFloat("FirstL0", 0);
        mDetectorFirst.mLowerBound.val[1] = (double) shroomPrefs.getFloat("FirstL1", 0);
        mDetectorFirst.mLowerBound.val[2] = (double) shroomPrefs.getFloat("FirstL2", 0);
        mDetectorFirst.mLowerBound.val[3] = (double) shroomPrefs.getFloat("FirstL3", 0);
     
        mDetectorSecond.mUpperBound.val[0] = (double) shroomPrefs.getFloat("SecondU0", 0);
        mDetectorSecond.mUpperBound.val[1] = (double) shroomPrefs.getFloat("SecondU1", 0);
        mDetectorSecond.mUpperBound.val[2] = (double) shroomPrefs.getFloat("SecondU2", 0);
        mDetectorSecond.mUpperBound.val[3] = (double) shroomPrefs.getFloat("SecondU3", 0);
        mDetectorSecond.mLowerBound.val[0] = (double) shroomPrefs.getFloat("SecondL0", 0);
        mDetectorSecond.mLowerBound.val[1] = (double) shroomPrefs.getFloat("SecondL1", 0);
        mDetectorSecond.mLowerBound.val[2] = (double) shroomPrefs.getFloat("SecondL2", 0);
        mDetectorSecond.mLowerBound.val[3] = (double) shroomPrefs.getFloat("SecondL3", 0);
        
        mDetectorThird.mUpperBound.val[0] = (double) shroomPrefs.getFloat("ThirdU0", 0);
        mDetectorThird.mUpperBound.val[1] = (double) shroomPrefs.getFloat("ThirdU1", 0);
        mDetectorThird.mUpperBound.val[2] = (double) shroomPrefs.getFloat("ThirdU2", 0);
        mDetectorThird.mUpperBound.val[3] = (double) shroomPrefs.getFloat("ThirdU3", 0);
        mDetectorThird.mLowerBound.val[0] = (double) shroomPrefs.getFloat("ThirdL0", 0);
        mDetectorThird.mLowerBound.val[1] = (double) shroomPrefs.getFloat("ThirdL1", 0);
        mDetectorThird.mLowerBound.val[2] = (double) shroomPrefs.getFloat("ThirdL2", 0);
        mDetectorThird.mLowerBound.val[3] = (double) shroomPrefs.getFloat("ThirdL3", 0);
        
        mDetectorFourth.mUpperBound.val[0] = (double) shroomPrefs.getFloat("FourthU0", 0);
        mDetectorFourth.mUpperBound.val[1] = (double) shroomPrefs.getFloat("FourthU1", 0);
        mDetectorFourth.mUpperBound.val[2] = (double) shroomPrefs.getFloat("FourthU2", 0);
        mDetectorFourth.mUpperBound.val[3] = (double) shroomPrefs.getFloat("FourthU3", 0);
        mDetectorFourth.mLowerBound.val[0] = (double) shroomPrefs.getFloat("FourthL0", 0);
        mDetectorFourth.mLowerBound.val[1] = (double) shroomPrefs.getFloat("FourthL1", 0);
        mDetectorFourth.mLowerBound.val[2] = (double) shroomPrefs.getFloat("FourthL2", 0);
        mDetectorFourth.mLowerBound.val[3] = (double) shroomPrefs.getFloat("FourthL3", 0);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        shroomPrefs = getSharedPreferences(SHROOM_PREF, 0);
    	SharedPreferences.Editor editor = shroomPrefs.edit();
    	editor.putBoolean("condBase", mIsColorBase);
    	editor.putBoolean("condFirst", mIsColorFirst);
    	editor.putBoolean("condSecond", mIsColorSecond);
    	editor.putBoolean("condThird", mIsColorThird);
    	editor.putBoolean("condFourth", mIsColorFourth);
    	editor.commit();
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
        pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");
        
        final int viewMode = mViewMode;
        switch (viewMode) {
        	case VIEW_BASE:
        		mDetectorBase.setHsvColor(mBlobColorHsv);
        		shroomPrefs = getSharedPreferences(SHROOM_PREF, 0);
        		Editor editor = shroomPrefs.edit();
        		editor.putFloat("BaseU0", (float) mDetectorBase.mUpperBound.val[0]);
        		editor.putFloat("BaseU1", (float) mDetectorBase.mUpperBound.val[1]);
        		editor.putFloat("BaseU2", (float) mDetectorBase.mUpperBound.val[2]);
        		editor.putFloat("BaseU3", (float) mDetectorBase.mUpperBound.val[3]);
        		editor.putFloat("BaseL0", (float) mDetectorBase.mLowerBound.val[0]);
        		editor.putFloat("BaseL1", (float) mDetectorBase.mLowerBound.val[1]);
        		editor.putFloat("BaseL2", (float) mDetectorBase.mLowerBound.val[2]);
        		editor.putFloat("BaseL3", (float) mDetectorBase.mLowerBound.val[3]);
        		editor.commit();
        		
                Imgproc.resize(mDetectorBase.getSpectrum(), mSpectrumBase, SPECTRUM_SIZE_BASE);
                mIsColorBase = true;
                break;
        	case VIEW_FIRST:
        		mDetectorFirst.setHsvColor(mBlobColorHsv);
        		shroomPrefs = getSharedPreferences(SHROOM_PREF, 0);
        		Editor editor1 = shroomPrefs.edit();
        		editor1.putFloat("FirstU0", (float) mDetectorFirst.mUpperBound.val[0]);
        		editor1.putFloat("FirstU1", (float) mDetectorFirst.mUpperBound.val[1]);
        		editor1.putFloat("FirstU2", (float) mDetectorFirst.mUpperBound.val[2]);
        		editor1.putFloat("FirstU3", (float) mDetectorFirst.mUpperBound.val[3]);
        		editor1.putFloat("FirstL0", (float) mDetectorFirst.mLowerBound.val[0]);
        		editor1.putFloat("FirstL1", (float) mDetectorFirst.mLowerBound.val[1]);
        		editor1.putFloat("FirstL2", (float) mDetectorFirst.mLowerBound.val[2]);
        		editor1.putFloat("FirstL3", (float) mDetectorFirst.mLowerBound.val[3]);
        		editor1.commit();
                Imgproc.resize(mDetectorFirst.getSpectrum(), mSpectrumFirst, SPECTRUM_SIZE_FIRST);
                mIsColorFirst = true;
                break;    
        	case VIEW_SECOND:
        		mDetectorSecond.setHsvColor(mBlobColorHsv);
        		shroomPrefs = getSharedPreferences(SHROOM_PREF, 0);
        		Editor editor2 = shroomPrefs.edit();
        		editor2.putFloat("SecondU0", (float) mDetectorSecond.mUpperBound.val[0]);
        		editor2.putFloat("SecondU1", (float) mDetectorSecond.mUpperBound.val[1]);
        		editor2.putFloat("SecondU2", (float) mDetectorSecond.mUpperBound.val[2]);
        		editor2.putFloat("SecondU3", (float) mDetectorSecond.mUpperBound.val[3]);
        		editor2.putFloat("SecondL0", (float) mDetectorSecond.mLowerBound.val[0]);
        		editor2.putFloat("SecondL1", (float) mDetectorSecond.mLowerBound.val[1]);
        		editor2.putFloat("SecondL2", (float) mDetectorSecond.mLowerBound.val[2]);
        		editor2.putFloat("SecondL3", (float) mDetectorSecond.mLowerBound.val[3]);
        		editor2.commit();
                Imgproc.resize(mDetectorSecond.getSpectrum(), mSpectrumSecond, SPECTRUM_SIZE_SECOND);
                mIsColorSecond = true;
                break;
        	case VIEW_THIRD:
        		mDetectorThird.setHsvColor(mBlobColorHsv);
        		shroomPrefs = getSharedPreferences(SHROOM_PREF, 0);
        		Editor editor3 = shroomPrefs.edit();
        		editor3.putFloat("ThirdU0", (float) mDetectorThird.mUpperBound.val[0]);
        		editor3.putFloat("ThirdU1", (float) mDetectorThird.mUpperBound.val[1]);
        		editor3.putFloat("ThirdU2", (float) mDetectorThird.mUpperBound.val[2]);
        		editor3.putFloat("ThirdU3", (float) mDetectorThird.mUpperBound.val[3]);
        		editor3.putFloat("ThirdL0", (float) mDetectorThird.mLowerBound.val[0]);
        		editor3.putFloat("ThirdL1", (float) mDetectorThird.mLowerBound.val[1]);
        		editor3.putFloat("ThirdL2", (float) mDetectorThird.mLowerBound.val[2]);
        		editor3.putFloat("ThirdL3", (float) mDetectorThird.mLowerBound.val[3]);
        		editor3.commit();
                Imgproc.resize(mDetectorThird.getSpectrum(), mSpectrumThird, SPECTRUM_SIZE_THIRD);
                mIsColorThird = true;
                break;
        	case VIEW_FOURTH:
        		mDetectorFourth.setHsvColor(mBlobColorHsv);
        		shroomPrefs = getSharedPreferences(SHROOM_PREF, 0);
        		Editor editor4 = shroomPrefs.edit();
        		editor4.putFloat("FourthU0", (float) mDetectorFourth.mUpperBound.val[0]);
        		editor4.putFloat("FourthU1", (float) mDetectorFourth.mUpperBound.val[1]);
        		editor4.putFloat("FourthU2", (float) mDetectorFourth.mUpperBound.val[2]);
        		editor4.putFloat("FourthU3", (float) mDetectorFourth.mUpperBound.val[3]);
        		editor4.putFloat("FourthL0", (float) mDetectorFourth.mLowerBound.val[0]);
        		editor4.putFloat("FourthL1", (float) mDetectorFourth.mLowerBound.val[1]);
        		editor4.putFloat("FourthL2", (float) mDetectorFourth.mLowerBound.val[2]);
        		editor4.putFloat("FourthL3", (float) mDetectorFourth.mLowerBound.val[3]);
        		editor4.commit();
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

        return super.onOptionsItemSelected(item);
    }
}
