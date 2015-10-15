package com.kircherelectronics.gyroscopeexplorer.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kircherelectronics.gyroscopeexplorer.activity.filter.GyroscopeOrientation;
import com.kircherelectronics.gyroscopeexplorer.activity.filter.ImuOCfOrientation;
import com.kircherelectronics.gyroscopeexplorer.activity.filter.ImuOCfQuaternion;
import com.kircherelectronics.gyroscopeexplorer.activity.filter.ImuOCfRotationMatrix;
import com.kircherelectronics.gyroscopeexplorer.activity.filter.ImuOKfQuaternion;
import com.kircherelectronics.gyroscopeexplorer.activity.filter.Orientation;
import com.kircherelectronics.gyroscopeexplorer.activity.gauge.GaugeBearing;
import com.kircherelectronics.gyroscopeexplorer.activity.gauge.GaugeRotation;

import info.korzeniowski.rcontroller.R;

/*
 * Gyroscope Explorer
 * Copyright (C) 2013-2015, Kaleb Kircher - Kircher Engineering, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * The main activity displays the orientation estimated by the sensor(s) and
 * provides an interface for the user to modify settings, reset or view help.
 * 
 * @author Kaleb
 *
 */
public class GyroscopeActivity extends Activity implements Runnable
{
	private static final String tag = GyroscopeActivity.class.getSimpleName();

	// Indicate if the output should be logged to a .csv file
	private boolean logData = false;
	private boolean dataReady = false;

	private boolean imuOCfOrienationEnabled;
	private boolean imuOCfRotationMatrixEnabled;
	private boolean imuOCfQuaternionEnabled;
	private boolean imuOKfQuaternionEnabled;
	private boolean isCalibrated;
	private boolean gyroscopeAvailable;

	private float[] vOrientation = new float[3];

	// The generation of the log output
	private int generation = 0;

	// Log output time stamp
	private long logTime = 0;

	// The gauge views. Note that these are views and UI hogs since they run in
	// the UI thread, not ideal, but easy to use.
	private GaugeBearing gaugeBearingCalibrated;
	private GaugeRotation gaugeTiltCalibrated;

	// Handler for the UI plots so everything plots smoothly
	protected Handler handler;

	private Orientation orientation;

	protected Runnable runable;

	// Acceleration plot titles
	private String plotAccelXAxisTitle = "Azimuth";
	private String plotAccelYAxisTitle = "Pitch";
	private String plotAccelZAxisTitle = "Roll";

	// Output log
	private String log;

	private TextView tvXAxis;
	private TextView tvYAxis;
	private TextView tvZAxis;
	private TextView tvStatus;

	private Thread thread;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

//		setContentView(R.layout.activity_gyroscope);

		initUI();

		gyroscopeAvailable = gyroscopeAvailable();
	};

	public void onResume()
	{
		super.onResume();

		readPrefs();
		reset();

		orientation.onResume();

		handler.post(runable);
	}

	public void onPause()
	{
		super.onPause();

		orientation.onPause();

		handler.removeCallbacks(runable);
	}

	/**
	 * Output and logs are run on their own thread to keep the UI from hanging
	 * and the output smooth.
	 */
	@Override
	public void run()
	{
		while (logData && !Thread.currentThread().isInterrupted())
		{
			logData();
		}

		Thread.currentThread().interrupt();
	}

	private boolean getPrefCalibratedGyroscopeEnabled()
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		return prefs.getBoolean(
				ConfigActivity.CALIBRATED_GYROSCOPE_ENABLED_KEY, true);
	}

	private boolean getPrefImuOCfOrientationEnabled()
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		return prefs.getBoolean(ConfigActivity.IMUOCF_ORIENTATION_ENABLED_KEY,
				false);
	}

	private boolean getPrefImuOCfRotationMatrixEnabled()
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		return prefs.getBoolean(
				ConfigActivity.IMUOCF_ROTATION_MATRIX_ENABLED_KEY, false);
	}

	private boolean getPrefImuOCfQuaternionEnabled()
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		return prefs.getBoolean(ConfigActivity.IMUOCF_QUATERNION_ENABLED_KEY,
				false);
	}

	private boolean getPrefImuOKfQuaternionEnabled()
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		return prefs.getBoolean(ConfigActivity.IMUOKF_QUATERNION_ENABLED_KEY,
				false);
	}

	private float getPrefImuOCfOrienationCoeff()
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		return Float.valueOf(prefs.getString(
				ConfigActivity.IMUOCF_ORIENTATION_COEFF_KEY, "0.5"));
	}

	private float getPrefImuOCfRotationMatrixCoeff()
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		return Float.valueOf(prefs.getString(
				ConfigActivity.IMUOCF_ROTATION_MATRIX_COEFF_KEY, "0.5"));
	}

	private float getPrefImuOCfQuaternionCoeff()
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		return Float.valueOf(prefs.getString(
				ConfigActivity.IMUOCF_QUATERNION_COEFF_KEY, "0.5"));
	}

	private boolean gyroscopeAvailable()
	{
		return getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_SENSOR_GYROSCOPE);
	}

	private void initStartButton()
	{
		final Button button = (Button) findViewById(R.id.search_button);

		button.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (!logData)
				{
					button.setText("Stop Log");

					thread = new Thread(GyroscopeActivity.this);

					thread.start();
				}
				else
				{
					button.setText("Start Log");

					stopDataLog();
				}
			}
		});
	}

	/**
	 * Initialize the UI.
	 */
	private void initUI()
	{
		// Initialize the calibrated text views
		tvXAxis = (TextView) this.findViewById(R.id.text);
		tvYAxis = (TextView) this.findViewById(R.id.text);
		tvZAxis = (TextView) this.findViewById(R.id.text);
		tvStatus = (TextView) this.findViewById(R.id.text);

		// Initialize the calibrated gauges views
		gaugeBearingCalibrated = (GaugeBearing) findViewById(R.id.text);
		gaugeTiltCalibrated = (GaugeRotation) findViewById(R.id.text);

		initStartButton();
	}

	/**
	 * Log output data to an external .csv file.
	 */
	private void logData()
	{
		if (logData && dataReady)
		{
			if (generation == 0)
			{
				logTime = System.currentTimeMillis();
			}

			log += generation++ + ",";

			log += String.format("%.2f",
					(System.currentTimeMillis() - logTime) / 1000.0f) + ",";

			log += vOrientation[0] + ",";
			log += vOrientation[1] + ",";
			log += vOrientation[2] + ",";

			log += System.getProperty("line.separator");

			dataReady = false;
		}
	}

	private void reset()
	{
		isCalibrated = getPrefCalibratedGyroscopeEnabled();

		orientation = new GyroscopeOrientation(this);

		if (isCalibrated)
		{
			tvStatus.setText("Sensor Calibrated");
		}
		else
		{
			tvStatus.setText("Sensor Uncalibrated");
		}

		if (imuOCfOrienationEnabled)
		{
			orientation = new ImuOCfOrientation(this);
			orientation.setFilterCoefficient(getPrefImuOCfOrienationCoeff());

			if (isCalibrated)
			{
				tvStatus.setText("ImuOCfOrientation Calibrated");
			}
			else
			{
				tvStatus.setText("ImuOCfOrientation Uncalibrated");
			}

		}
		if (imuOCfRotationMatrixEnabled)
		{
			orientation = new ImuOCfRotationMatrix(this);
			orientation
					.setFilterCoefficient(getPrefImuOCfRotationMatrixCoeff());

			if (isCalibrated)
			{
				tvStatus.setText("ImuOCfRm Calibrated");
			}
			else
			{
				tvStatus.setText("ImuOCfRm Uncalibrated");
			}
		}
		if (imuOCfQuaternionEnabled)
		{
			orientation = new ImuOCfQuaternion(this);
			orientation.setFilterCoefficient(getPrefImuOCfQuaternionCoeff());

			if (isCalibrated)
			{
				tvStatus.setText("ImuOCfQuaternion Calibrated");
			}
			else
			{
				tvStatus.setText("ImuOCfQuaternion Uncalibrated");
			}
		}
		if (imuOKfQuaternionEnabled)
		{
			orientation = new ImuOKfQuaternion(this);

			if (isCalibrated)
			{
				tvStatus.setText("ImuOKfQuaternion Calibrated");
			}
			else
			{
				tvStatus.setText("ImuOKfQuaternion Uncalibrated");
			}
		}

		handler = new Handler();

		runable = new Runnable()
		{
			@Override
			public void run()
			{
				handler.postDelayed(this, 100);

				vOrientation = orientation.getOrientation();

				dataReady = true;

				updateText();
				updateGauges();
			}
		};
	}

	private void readPrefs()
	{
		imuOCfOrienationEnabled = getPrefImuOCfOrientationEnabled();
		imuOCfRotationMatrixEnabled = getPrefImuOCfRotationMatrixEnabled();
		imuOCfQuaternionEnabled = getPrefImuOCfQuaternionEnabled();
		imuOKfQuaternionEnabled = getPrefImuOKfQuaternionEnabled();
	}

	private void stopDataLog()
	{
		if (logData)
		{
			writeLogToFile();
		}

		if (logData && thread != null)
		{
			logData = false;

			thread.interrupt();

			thread = null;
		}
	}

	private void updateText()
	{
		tvXAxis.setText(String.format("%.2f", Math.toDegrees(vOrientation[0])));
		tvYAxis.setText(String.format("%.2f", Math.toDegrees(vOrientation[1])));
		tvZAxis.setText(String.format("%.2f", Math.toDegrees(vOrientation[2])));
	}

	private void updateGauges()
	{
		gaugeBearingCalibrated.updateBearing(vOrientation[0]);
		gaugeTiltCalibrated.updateRotation(vOrientation);
	}

	/**
	 * Write the logged data out to a persisted file.
	 */
	private void writeLogToFile()
	{
		Calendar c = Calendar.getInstance();
		String filename = "GyroscopeExplorer-" + c.get(Calendar.YEAR) + "-"
				+ (c.get(Calendar.MONTH) + 1) + "-"
				+ c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.HOUR)
				+ "-" + c.get(Calendar.MINUTE) + "-" + c.get(Calendar.SECOND)
				+ ".csv";

		File dir = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "GyroscopeExplorer" + File.separator
				+ "Logs");
		if (!dir.exists())
		{
			dir.mkdirs();
		}

		File file = new File(dir, filename);

		FileOutputStream fos;
		byte[] data = log.getBytes();
		try
		{
			fos = new FileOutputStream(file);
			fos.write(data);
			fos.flush();
			fos.close();

			CharSequence text = "Log Saved";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(this, text, duration);
			toast.show();
		}
		catch (FileNotFoundException e)
		{
			CharSequence text = e.toString();
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(this, text, duration);
			toast.show();
		}
		catch (IOException e)
		{
			// handle exception
		}
		finally
		{
			// Update the MediaStore so we can view the file without rebooting.
			// Note that it appears that the ACTION_MEDIA_MOUNTED approach is
			// now blocked for non-system apps on Android 4.4.
			MediaScannerConnection.scanFile(this, new String[]
			{ file.getPath() }, null,
					new MediaScannerConnection.OnScanCompletedListener()
					{
						@Override
						public void onScanCompleted(final String path,
								final Uri uri)
						{

						}
					});
		}
	}

}
