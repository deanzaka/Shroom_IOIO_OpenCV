package rpl.shroomIOIO;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
//import ioio.lib.api.DigitalOutput;
//import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
//import android.widget.ToggleButton;
import android.widget.ToggleButton;

public class IOIOSimpleApp extends IOIOActivity {
	public static final String SHROOM_PREF = "ShroomPrefs";
	public static SharedPreferences		shroomPrefs; 
	
	private TextView luxValue_;
	private TextView tempValue_;
	private NumberPicker pickerLux_;
	private NumberPicker pickerTemp_;
	private ToggleButton toggleAuto_;
	
	public Integer ACStatus_;
	SharedPreferences prefAC_;
	public Integer lampStatus_;
	SharedPreferences prefLamp_;
	//private ToggleButton toggleIR_;
	//private ToggleButton toggleRelay_;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		shroomPrefs = getSharedPreferences(SHROOM_PREF, 0);
		ACStatus_  = shroomPrefs.getInt("AC_Status", 0);
		lampStatus_  = shroomPrefs.getInt("lamp_Status", 0);
		
		
		luxValue_ = (TextView) findViewById(R.id.LuxValue);
		tempValue_ = (TextView) findViewById(R.id.TempValue);
		toggleAuto_ = (ToggleButton) findViewById(R.id.ToggleAuto);
		//toggleIR_ = (ToggleButton) findViewById(R.id.ToggleIR);
		//toggleRelay_ = (ToggleButton) findViewById(R.id.ToggleRelay);
		String[] nums = new String[41];
		for(int i=0; i<nums.length; i++)
		nums[i] = Integer.toString(i*500);
		pickerLux_ = (NumberPicker) findViewById(R.id.userLux);
		pickerLux_.setMaxValue(nums.length - 1);
        pickerLux_.setMinValue(0);
        pickerLux_.setDisplayedValues(nums);
        for( int i=0; i<nums.length ; i++ )
            if(nums[i].equals("9000"))
                 pickerLux_.setValue(i);
        pickerLux_.setWrapSelectorWheel(false);
        pickerLux_.setOnLongPressUpdateInterval(50);
        pickerTemp_ = (NumberPicker) findViewById(R.id.userTemp);
        pickerTemp_.setMaxValue(100);
        pickerTemp_.setMinValue(0);
        pickerTemp_.setValue(30);
        pickerTemp_.setWrapSelectorWheel(false);
        pickerTemp_.setOnLongPressUpdateInterval(50);
		
        //enableUi(false);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		//Handle presses on the action bar item
		switch (item.getItemId()) {
			case R.id.action_settings:
				//Intent setup = new Intent(this, ColorBlobSetup.class);
				//startActivity(setup);
				
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			      alertDialogBuilder.setMessage(R.string.setup_alert);
			      alertDialogBuilder.setPositiveButton(R.string.yes_answer, 
			      new DialogInterface.OnClickListener() {
					
			         @Override
			         public void onClick(DialogInterface arg0, int arg1) {
			            Intent positveActivity = new Intent(getApplicationContext(),ColorBlobSetup.class);
			            startActivity(positveActivity);
						
			         }
			      });
			      alertDialogBuilder.setNegativeButton(R.string.no_answer, 
			      new DialogInterface.OnClickListener() {
						
			         @Override
			         public void onClick(DialogInterface dialog, int which) {
			            Intent negativeActivity = new Intent(getApplicationContext(),IOIOSimpleApp.class);
			            startActivity(negativeActivity);
					 }
			      });
				    
			      AlertDialog alertDialog = alertDialogBuilder.create();
			      alertDialog.show();
				
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	class Looper extends BaseIOIOLooper {
		private AnalogInput lux_;
		private AnalogInput temp_;
		//private DigitalOutput led_;
		private DigitalOutput AC_;
		public DigitalOutput lamp_;
		private DigitalOutput Global_;

		@Override
		public void setup() throws ConnectionLostException {
			//led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
			lamp_ = ioio_.openDigitalOutput(10, true);
			AC_ = ioio_.openDigitalOutput(9, true);
			lux_ = ioio_.openAnalogInput(40);
			temp_ = ioio_.openAnalogInput(39);
			Global_ = ioio_.openDigitalOutput(11, true);
			//enableUi(true);
		}

		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			Global_.write(true);
			float luxRead = lux_.read();
			luxRead = ((2500/luxRead) - 500);
			int intLuxRead = Math.round(luxRead);
			luxText(Integer.toString(intLuxRead));
			int userLux = pickerLux_.getValue();
			//tempText(Integer.toString(userLux)); debug light
			float tempRead = temp_.read();
			tempRead = tempRead * 350;
			int intTempRead = (int) tempRead;
			tempText(Integer.toString(intTempRead));
			int userTemp = pickerTemp_.getValue();
			if(toggleAuto_.isChecked())	{
				//Automatic control for Lamp
				if(intLuxRead < (userLux*500)) {		
					lamp_.write(true);
					lampStatus_ = 1;
					saveLamp();
				}
				else {
					lamp_.write(false);
					lampStatus_ = 0;
					saveLamp();
				}
				
				//Automatic control for AC
				if(intTempRead < userTemp) {
					if(ACStatus_ == 1)	{
					AC_.write(true);
					Thread.sleep(1000);
					AC_.write(false);
					ACStatus_ = 0;
					saveAC();
					}
					else ACStatus_ = 0;
				}
				else {
					if(ACStatus_ == 0)	{
					AC_.write(true);
					Thread.sleep(1000);
					AC_.write(false);
					ACStatus_ = 1;
					saveAC();
					}
					else ACStatus_ = 1;
				}
			}	
			//IR_.write(toggleIR_.isChecked());
			Thread.sleep(10);
		}
			
		@Override
		public void disconnected() {
			//enableUi(false);
		}
		

	}
	
	public void saveAC() {
		shroomPrefs = getSharedPreferences(SHROOM_PREF, 0);
		Editor editor = shroomPrefs.edit();
		editor.putInt("AC_Status", ACStatus_);
		editor.commit();
				
	}
	
	public void saveLamp() {
		shroomPrefs = getSharedPreferences(SHROOM_PREF, 0);
		Editor editor = shroomPrefs.edit();
		editor.putInt("lamp_Status", lampStatus_);
		editor.commit();
				
	}
	
	public void menuAct(final View view) {
		Intent menu = new Intent(this, MenuAct.class);
		startActivity(menu);
	}
	
	public void detectAct(final View view) {
		Intent detect = new Intent(this, ColorBlobDetect.class);
		startActivity(detect);
	}
	
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}
	/*
	private void enableUi(final boolean enable) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//toggleLED_.setEnabled(enable);
				//toggleIR_.setEnabled(enable);
				toggleAuto_.setEnabled(enable);
				pickerLux_.setEnabled(enable);
				pickerTemp_.setEnabled(enable);
				tempValue_.setEnabled(enable);
				luxValue_.setEnabled(enable);
			}
		});
	}*/

	private void luxText(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				luxValue_.setText(str);
			}
		});
	}
	
	private void tempText(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tempValue_.setText(str);
			}
		});
	
		
	
	}
}