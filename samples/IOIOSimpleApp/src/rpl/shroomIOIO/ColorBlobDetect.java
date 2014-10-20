package rpl.shroomIOIO;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;

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

import rpl.shroomIOIO.MenuAct.Looper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

public class ColorBlobDetect extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG              = "OCVSample::Activity";
    
    String SHROOM_PREF = IOIOSimpleApp.SHROOM_PREF;
	SharedPreferences shroomPrefs = IOIOSimpleApp.shroomPrefs;
    
    private Mat                  mRgba;
    private Scalar				 mPixelCounter;
    private int				 	 opt;
    
    private TextView 			 basePixel_;
    private TextView			 firstPixel_;
    private TextView			 secondPixel_;
    private TextView			 thirdPixel_;
    private TextView			 fourthPixel_;
    
    public static int				mPixelBase;
    public static int				mPixelFirst;
    public static int				mPixelSecond;
    public static int				mPixelThird;
    public static int				mPixelFourth;
    
    public static int				sumPixelBase;
    public static int				sumPixelFirst;
    public static int				sumPixelSecond;
    public static int				sumPixelThird;
    public static int				sumPixelFourth;
    
    private static boolean			AC_ON;
    private static boolean			TV_ON;
    private static boolean			lamp_ON;
    private static boolean			curt_OPEN;
    
    public Integer ACStatus_;
	public Integer TVStatus_;
	public Integer CurtStatus_;
	public Integer lampStatus_;
	public Integer globalStatus_;
	
	private boolean base;
	private boolean first;
	private boolean second;
	private boolean third;
	private boolean fourth;
    
    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ColorBlobDetect.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ColorBlobDetect() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detect);
        
        shroomPrefs = getSharedPreferences("SHROOM_PREF",0);
		ACStatus_  = shroomPrefs.getInt("AC_Status", 0);
		TVStatus_  = shroomPrefs.getInt("TV_Status", 0);
		CurtStatus_  = shroomPrefs.getInt("curt_Status", 0);
		lampStatus_  = shroomPrefs.getInt("lamp_Status", 0);
		globalStatus_  = shroomPrefs.getInt("global_Status", 0);
		
		if(ACStatus_ == 1) AC_ON = true;
		else AC_ON = false;
		if(TVStatus_ == 1) TV_ON = true;
		else TV_ON = false;
		if(lampStatus_ == 1) lamp_ON = true;
		else lamp_ON = false;
		if(CurtStatus_ == 1) curt_OPEN = true;
		else curt_OPEN = false;
		
        basePixel_ = (TextView) findViewById(R.id.basePixel);
        firstPixel_ = (TextView) findViewById(R.id.firstPixel);
        secondPixel_ = (TextView) findViewById(R.id.secondPixel);
        thirdPixel_ = (TextView) findViewById(R.id.thirdPixel);
        fourthPixel_ = (TextView) findViewById(R.id.fourthPixel);
        
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detect);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
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

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        
        shroomPrefs = getSharedPreferences(SHROOM_PREF, 0);
        ColorBlobSetup.mIsColorBase = shroomPrefs.getBoolean("condBase", false);
        ColorBlobSetup.mIsColorFirst = shroomPrefs.getBoolean("condFirst", false);
        ColorBlobSetup.mIsColorSecond = shroomPrefs.getBoolean("condSecond", false);
        ColorBlobSetup.mIsColorThird = shroomPrefs.getBoolean("condThird", false);
        ColorBlobSetup.mIsColorFourth = shroomPrefs.getBoolean("condFourth", false);
        
        ColorBlobSetup.mDetectorBase = new ColorBlobDetector();
        ColorBlobSetup.mSpectrumBase = new Mat();
        ColorBlobSetup.SPECTRUM_SIZE_BASE = new Size(200, 64);
        ColorBlobSetup.CONTOUR_COLOR_BASE = new Scalar(255,0,0,255);
        
        ColorBlobSetup.mDetectorFirst = new ColorBlobDetector();
        ColorBlobSetup.mSpectrumFirst = new Mat();
        ColorBlobSetup.SPECTRUM_SIZE_FIRST = new Size(200, 64);
        ColorBlobSetup.CONTOUR_COLOR_FIRST = new Scalar(255,0,0,255);
        
        ColorBlobSetup.mDetectorSecond = new ColorBlobDetector();
        ColorBlobSetup.mSpectrumSecond = new Mat();
        ColorBlobSetup.SPECTRUM_SIZE_SECOND = new Size(200, 64);
        ColorBlobSetup.CONTOUR_COLOR_SECOND = new Scalar(255,0,0,255);
        
        ColorBlobSetup.mDetectorThird = new ColorBlobDetector();
        ColorBlobSetup.mSpectrumThird = new Mat();
        ColorBlobSetup.SPECTRUM_SIZE_THIRD = new Size(200, 64);
        ColorBlobSetup.CONTOUR_COLOR_THIRD = new Scalar(255,0,0,255);
        
        ColorBlobSetup.mDetectorFourth = new ColorBlobDetector();
        ColorBlobSetup.mSpectrumFourth = new Mat();
        ColorBlobSetup.SPECTRUM_SIZE_FOURTH = new Size(200, 64);
        ColorBlobSetup.CONTOUR_COLOR_FOURTH = new Scalar(255,0,0,255);
        
        ColorBlobSetup.mDetectorBase.mUpperBound.val[0] = (double) shroomPrefs.getFloat("BaseU0", 0);
        ColorBlobSetup.mDetectorBase.mUpperBound.val[1] = (double) shroomPrefs.getFloat("BaseU1", 0);
        ColorBlobSetup.mDetectorBase.mUpperBound.val[2] = (double) shroomPrefs.getFloat("BaseU2", 0);
        ColorBlobSetup.mDetectorBase.mUpperBound.val[3] = (double) shroomPrefs.getFloat("BaseU3", 0);
        ColorBlobSetup.mDetectorBase.mLowerBound.val[0] = (double) shroomPrefs.getFloat("BaseL0", 0);
        ColorBlobSetup.mDetectorBase.mLowerBound.val[1] = (double) shroomPrefs.getFloat("BaseL1", 0);
        ColorBlobSetup.mDetectorBase.mLowerBound.val[2] = (double) shroomPrefs.getFloat("BaseL2", 0);
        ColorBlobSetup.mDetectorBase.mLowerBound.val[3] = (double) shroomPrefs.getFloat("BaseL3", 0);
        
        ColorBlobSetup.mDetectorFirst.mUpperBound.val[0] = (double) shroomPrefs.getFloat("FirstU0", 0);
        ColorBlobSetup.mDetectorFirst.mUpperBound.val[1] = (double) shroomPrefs.getFloat("FirstU1", 0);
        ColorBlobSetup.mDetectorFirst.mUpperBound.val[2] = (double) shroomPrefs.getFloat("FirstU2", 0);
        ColorBlobSetup.mDetectorFirst.mUpperBound.val[3] = (double) shroomPrefs.getFloat("FirstU3", 0);
        ColorBlobSetup.mDetectorFirst.mLowerBound.val[0] = (double) shroomPrefs.getFloat("FirstL0", 0);
        ColorBlobSetup.mDetectorFirst.mLowerBound.val[1] = (double) shroomPrefs.getFloat("FirstL1", 0);
        ColorBlobSetup.mDetectorFirst.mLowerBound.val[2] = (double) shroomPrefs.getFloat("FirstL2", 0);
        ColorBlobSetup.mDetectorFirst.mLowerBound.val[3] = (double) shroomPrefs.getFloat("FirstL3", 0);
     
        ColorBlobSetup.mDetectorSecond.mUpperBound.val[0] = (double) shroomPrefs.getFloat("SecondU0", 0);
        ColorBlobSetup.mDetectorSecond.mUpperBound.val[1] = (double) shroomPrefs.getFloat("SecondU1", 0);
        ColorBlobSetup.mDetectorSecond.mUpperBound.val[2] = (double) shroomPrefs.getFloat("SecondU2", 0);
        ColorBlobSetup.mDetectorSecond.mUpperBound.val[3] = (double) shroomPrefs.getFloat("SecondU3", 0);
        ColorBlobSetup.mDetectorSecond.mLowerBound.val[0] = (double) shroomPrefs.getFloat("SecondL0", 0);
        ColorBlobSetup.mDetectorSecond.mLowerBound.val[1] = (double) shroomPrefs.getFloat("SecondL1", 0);
        ColorBlobSetup.mDetectorSecond.mLowerBound.val[2] = (double) shroomPrefs.getFloat("SecondL2", 0);
        ColorBlobSetup.mDetectorSecond.mLowerBound.val[3] = (double) shroomPrefs.getFloat("SecondL3", 0);
        
        ColorBlobSetup.mDetectorThird.mUpperBound.val[0] = (double) shroomPrefs.getFloat("ThirdU0", 0);
        ColorBlobSetup.mDetectorThird.mUpperBound.val[1] = (double) shroomPrefs.getFloat("ThirdU1", 0);
        ColorBlobSetup.mDetectorThird.mUpperBound.val[2] = (double) shroomPrefs.getFloat("ThirdU2", 0);
        ColorBlobSetup.mDetectorThird.mUpperBound.val[3] = (double) shroomPrefs.getFloat("ThirdU3", 0);
        ColorBlobSetup.mDetectorThird.mLowerBound.val[0] = (double) shroomPrefs.getFloat("ThirdL0", 0);
        ColorBlobSetup.mDetectorThird.mLowerBound.val[1] = (double) shroomPrefs.getFloat("ThirdL1", 0);
        ColorBlobSetup.mDetectorThird.mLowerBound.val[2] = (double) shroomPrefs.getFloat("ThirdL2", 0);
        ColorBlobSetup.mDetectorThird.mLowerBound.val[3] = (double) shroomPrefs.getFloat("ThirdL3", 0);
        
        ColorBlobSetup.mDetectorFourth.mUpperBound.val[0] = (double) shroomPrefs.getFloat("FourthU0", 0);
        ColorBlobSetup.mDetectorFourth.mUpperBound.val[1] = (double) shroomPrefs.getFloat("FourthU1", 0);
        ColorBlobSetup.mDetectorFourth.mUpperBound.val[2] = (double) shroomPrefs.getFloat("FourthU2", 0);
        ColorBlobSetup.mDetectorFourth.mUpperBound.val[3] = (double) shroomPrefs.getFloat("FourthU3", 0);
        ColorBlobSetup.mDetectorFourth.mLowerBound.val[0] = (double) shroomPrefs.getFloat("FourthL0", 0);
        ColorBlobSetup.mDetectorFourth.mLowerBound.val[1] = (double) shroomPrefs.getFloat("FourthL1", 0);
        ColorBlobSetup.mDetectorFourth.mLowerBound.val[2] = (double) shroomPrefs.getFloat("FourthL2", 0);
        ColorBlobSetup.mDetectorFourth.mLowerBound.val[3] = (double) shroomPrefs.getFloat("FourthL3", 0);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        return false; // don't need subsequent touch events
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        
        if (ColorBlobSetup.mIsColorBase) {
        	mPixelBase = 0;
        	
        	ColorBlobSetup.mDetectorBase.process(mRgba);
            List<MatOfPoint> contours = ColorBlobSetup.mDetectorBase.getContours();
            Log.e(TAG, "Contours count: " + contours.size());
            Imgproc.drawContours(mRgba, contours, -1, ColorBlobSetup.CONTOUR_COLOR_BASE);
            
            mPixelCounter = Core.sumElems(ColorBlobDetector.mMask);
            for (int i = 0; i < mPixelCounter.val.length; i++) {
            	if(mPixelCounter.val[i] > 500000) mPixelBase += 1; //maximum 4.895.935
            }
            
            sumPixelBase = mPixelBase;
            if(sumPixelBase == 1) base = true;
            else base = false;
            
            baseText(Integer.toString(sumPixelBase));
        }
        if (ColorBlobSetup.mIsColorFirst) {
        	mPixelFirst = 0;
        	
            ColorBlobSetup.mDetectorFirst.process(mRgba);
            List<MatOfPoint> contours = ColorBlobSetup.mDetectorFirst.getContours();
            Log.e(TAG, "Contours count: " + contours.size());
            Imgproc.drawContours(mRgba, contours, -1, ColorBlobSetup.CONTOUR_COLOR_FIRST);
            
            mPixelCounter = Core.sumElems(ColorBlobDetector.mMask);
            for (int i = 0; i < mPixelCounter.val.length; i++) {
            	if(mPixelCounter.val[i] > 100000) mPixelFirst += 1; //maximum 4895935
            }
            
            sumPixelFirst = mPixelFirst;
            if(sumPixelFirst == 1) first = true;
            else first = false;
            
            firstText(Integer.toString(sumPixelFirst));
        }
        if (ColorBlobSetup.mIsColorSecond) {
        	mPixelSecond = 0;
        	
            ColorBlobSetup.mDetectorSecond.process(mRgba);
            List<MatOfPoint> contours = ColorBlobSetup.mDetectorSecond.getContours();
            Log.e(TAG, "Contours count: " + contours.size());
            Imgproc.drawContours(mRgba, contours, -1, ColorBlobSetup.CONTOUR_COLOR_SECOND);
            
            mPixelCounter = Core.sumElems(ColorBlobDetector.mMask);
            for (int i = 0; i < mPixelCounter.val.length; i++) {
            	if(mPixelCounter.val[i] > 100000) mPixelSecond += 1; //maximum 4895935
            }
            
            sumPixelSecond = mPixelSecond;
            if(sumPixelSecond == 1) second = true;
            else second = false;
            
            secondText(Integer.toString(sumPixelSecond));
        }
        if (ColorBlobSetup.mIsColorThird) {
        	mPixelThird = 0;
        	
            ColorBlobSetup.mDetectorThird.process(mRgba);
            List<MatOfPoint> contours = ColorBlobSetup.mDetectorThird.getContours();
            Log.e(TAG, "Contours count: " + contours.size());
            Imgproc.drawContours(mRgba, contours, -1, ColorBlobSetup.CONTOUR_COLOR_THIRD);
            
            mPixelCounter = Core.sumElems(ColorBlobDetector.mMask);
            for (int i = 0; i < mPixelCounter.val.length; i++) {
            	if(mPixelCounter.val[i] > 100000) mPixelThird += 1; //maximum 4895935
            }
            
            sumPixelThird = mPixelThird;
            if(sumPixelThird == 1) third = true;
            else third = false;
            
            thirdText(Integer.toString(sumPixelThird));
        }
        if (ColorBlobSetup.mIsColorFourth) {
        	mPixelFourth = 0;
        	
            ColorBlobSetup.mDetectorFourth.process(mRgba);
            List<MatOfPoint> contours = ColorBlobSetup.mDetectorFourth.getContours();
            Log.e(TAG, "Contours count: " + contours.size());
            Imgproc.drawContours(mRgba, contours, -1, ColorBlobSetup.CONTOUR_COLOR_FOURTH);
            
            mPixelCounter = Core.sumElems(ColorBlobDetector.mMask);
            for (int i = 0; i < mPixelCounter.val.length; i++) {
            	if(mPixelCounter.val[i] > 100000) mPixelFourth += 1; //maximum 4895935
            }
            
            sumPixelFourth = mPixelFourth;
            if(sumPixelFourth == 1) fourth = true;
            fourth = false;
            
            fourthText(Integer.toString(sumPixelFourth));
        }
        
        if(base)	{
        	if(first){
        		if(second) {
        			if(lamp_ON) lamp_ON = false;
        			else lamp_ON = true;
        			
        			Intent negativeActivity = new Intent(getApplicationContext(),IOIOSimpleApp.class);
		            startActivity(negativeActivity);
		            }
        		/*
        		  AlertDialog.Builder alertDialogBuilderAC = new AlertDialog.Builder(this);
  			      alertDialogBuilderAC.setMessage(R.string.AC_Detected);
  			      alertDialogBuilderAC.setPositiveButton(R.string.ON_answer, 
  			      new DialogInterface.OnClickListener() {
  					
  			         @Override
  			         public void onClick(DialogInterface arg0, int arg1) {
  			        	AC_ON = true; 
  			         }
  			      });
  			      alertDialogBuilderAC.setNegativeButton(R.string.OFF_answer, 
  			      new DialogInterface.OnClickListener() {
  						
  			         @Override
  			         public void onClick(DialogInterface dialog, int which) {
  			            AC_ON = false;
  					 }
  			      });
  				    
  			      AlertDialog alertDialogAC = alertDialogBuilderAC.create();
  			      alertDialogAC.show();
        		}
        		
        		*/
        		else if(third) {
        			if(curt_OPEN) curt_OPEN = false;
        			else curt_OPEN = true;
        			
        			Intent negativeActivity = new Intent(getApplicationContext(),IOIOSimpleApp.class);
		            startActivity(negativeActivity);
		            }
        			/*
        			AlertDialog.Builder alertDialogBuilderTV = new AlertDialog.Builder(this);
  			      alertDialogBuilderTV.setMessage(R.string.AC_Detected);
  			      alertDialogBuilderTV.setPositiveButton(R.string.ON_answer, 
  			      new DialogInterface.OnClickListener() {
  					
  			         @Override
  			         public void onClick(DialogInterface arg0, int arg1) {
  			        	TV_ON = true; 
  			         }
  			      });
  			      alertDialogBuilderTV.setNegativeButton(R.string.OFF_answer, 
  			      new DialogInterface.OnClickListener() {
  						
  			         @Override
  			         public void onClick(DialogInterface dialog, int which) {
  			            TV_ON = false;
  					 }
  			      });
  				    
  			      AlertDialog alertDialogTV = alertDialogBuilderTV.create();
  			      alertDialogTV.show();
  			      */
        		}
        	}
        	else if (second) {
        		if(third) {
        			/*
        		  AlertDialog.Builder alertDialogBuilderLamp = new AlertDialog.Builder(this);
  			      alertDialogBuilderLamp.setMessage(R.string.AC_Detected);
  			      alertDialogBuilderLamp.setPositiveButton(R.string.ON_answer, 
  			      new DialogInterface.OnClickListener() {
  					
  			         @Override
  			         public void onClick(DialogInterface arg0, int arg1) {
  			        	lamp_ON = true; 
  			         }
  			      });
  			      alertDialogBuilderLamp.setNegativeButton(R.string.OFF_answer, 
  			      new DialogInterface.OnClickListener() {
  						
  			         @Override
  			         public void onClick(DialogInterface dialog, int which) {
  			            lamp_ON = false;
  					 }
  			      });
  				    
  			      AlertDialog alertDialogLamp = alertDialogBuilderLamp.create();
  			      alertDialogLamp.show();
  			      */
        		}
        		else if(fourth) {
        		  /*
        			AlertDialog.Builder alertDialogBuilderCurt = new AlertDialog.Builder(this);
  			      alertDialogBuilderCurt.setMessage(R.string.AC_Detected);
  			      alertDialogBuilderCurt.setPositiveButton(R.string.ON_answer, 
  			      new DialogInterface.OnClickListener() {
  					
  			         @Override
  			         public void onClick(DialogInterface arg0, int arg1) {
  			        	curt_OPEN = true; 
  			         }
  			      });
  			      alertDialogBuilderCurt.setNegativeButton(R.string.OFF_answer, 
  			      new DialogInterface.OnClickListener() {
  						
  			         @Override
  			         public void onClick(DialogInterface dialog, int which) {
  			            curt_OPEN = false;
  					 }
  			      });
  				    
  			      AlertDialog alertDialogCurt = alertDialogBuilderCurt.create();
  			      alertDialogCurt.show();
  			      */
        		}
        	}
        
        return mRgba;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        return super.onOptionsItemSelected(item);
    }
    
    private void baseText(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				basePixel_.setText(str);
			}
		});
	}
    
    private void firstText(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				firstPixel_.setText(str);
			}
		});
	}
    
    private void secondText(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				secondPixel_.setText(str);
			}
		});
	}
    
    private void thirdText(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				thirdPixel_.setText(str);
			}
		});
	}
    
    private void fourthText(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				fourthPixel_.setText(str);
			}
		});
	}

    class Looper extends BaseIOIOLooper {
		private DigitalOutput AC_;
		private DigitalOutput Lamp_;
		private DigitalOutput TV_;
		private DigitalOutput CurtOpen_;
		private DigitalOutput CurtClose_;

		@Override
		public void setup() throws ConnectionLostException {
			Lamp_ = ioio_.openDigitalOutput(10, true);
			AC_ = ioio_.openDigitalOutput(9, true);
			TV_ = ioio_.openDigitalOutput(8,true);
			CurtOpen_ = ioio_.openDigitalOutput(7, true);
			CurtClose_ = ioio_.openDigitalOutput(6, true);
			
			if(lampStatus_ == 1) Lamp_.write(true);
			else Lamp_.write(false);
		}

		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			if(AC_ON) {
				if(ACStatus_ == 1)	{
					
				}
				else	{
					AC_.write(true);
					Thread.sleep(1000);
					AC_.write(false);
					ACStatus_ = 1;
					saveAC();
				}
			}	else {
				if(ACStatus_ == 1)	{
					AC_.write(true);
					Thread.sleep(1000);
					AC_.write(false);
					ACStatus_ = 0;
					saveAC();
				}
				else	{
					
				}
			}
			
			if(lamp_ON)	{
				if(lampStatus_ == 1)	{
					
				}	else {
					Lamp_.write(true);
					lampStatus_ = 1;
					saveLamp();
					}
				Thread.sleep(500);
			} else {
				if(lampStatus_ == 1)	{
					Lamp_.write(false);
					lampStatus_ = 0;
					saveLamp();
				}	else {
					
					}
				Thread.sleep(500);
			}
			
			if(TV_ON) {
				if(TVStatus_ == 1)	{
					TV_.write(true);
					Thread.sleep(1000);
					TV_.write(false);
					TVStatus_ = 0;
					saveTV();
				}
				else	{
					TV_.write(true);
					Thread.sleep(1000);
					TV_.write(false);
					TVStatus_ = 1;
					saveTV();
				}
			}
			
			if(curt_OPEN) {
				if(CurtStatus_ == 1)	{
					CurtOpen_.write(true);
					Thread.sleep(3000);
					CurtOpen_.write(false);
					CurtStatus_ = 0;
					saveCurt();
				}
				else	{
					
				}
			} else {
				if(CurtStatus_ == 1)	{
					
				}
				else	{
					CurtClose_.write(true);
					Thread.sleep(3000);
					CurtClose_.write(false);
					CurtStatus_ = 1;
					saveCurt();
				}
			}
			
			Thread.sleep(10);
		}

		@Override
		public void disconnected() {
			//enableUi(false);
		}
	}
    
    protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}
    
    public void saveAC() {
		shroomPrefs = getSharedPreferences(SHROOM_PREF, 0);
		Editor editor = shroomPrefs.edit();
		editor.putInt("AC_Status", ACStatus_);
		editor.commit();		
	}
	
	public void saveTV() {
		shroomPrefs = getSharedPreferences(SHROOM_PREF, 0);
		Editor editor = shroomPrefs.edit();
		editor.putInt("TV_Status", TVStatus_);
		editor.commit();		
	}
	
	public void saveCurt() {
		shroomPrefs = getSharedPreferences(SHROOM_PREF, 0);
		Editor editor = shroomPrefs.edit();
		editor.putInt("curt_Status", CurtStatus_);
		editor.commit();		
	}
	
	public void saveLamp() {
		shroomPrefs = getSharedPreferences(SHROOM_PREF, 0);
		Editor editor = shroomPrefs.edit();
		editor.putInt("lamp_Status", lampStatus_);
		editor.commit();			
	}
	
	public void saveGlobal() {
		shroomPrefs = getSharedPreferences(SHROOM_PREF, 0);
		Editor editor = shroomPrefs.edit();
		editor.putInt("global_Status", globalStatus_);
		editor.commit();			
	}



}
