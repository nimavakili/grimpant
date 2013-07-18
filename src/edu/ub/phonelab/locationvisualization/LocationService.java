package edu.ub.phonelab.locationvisualization;

//import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
//import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
//import java.net.InetAddress;

//import org.apache.commons.net.ftp.FTP;
//import org.apache.commons.net.ftp.FTPClient;

//import edu.ub.phonelab.locationvisualization.GPSActivity.UploadTask;

//import org.json.JSONObject;

//import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.Notification;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
//import android.app.TaskStackBuilder;
//import android.app.ProgressDialog;
//import android.app.AlarmManager;
//import android.app.PendingIntent;
import android.app.Service;
//import android.content.BroadcastReceiver;
import android.content.Context;
//import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
//import android.os.AsyncTask;
//import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
//import android.os.Handler;
import android.os.IBinder;
//import android.os.Message;
//import android.telephony.TelephonyManager;
//import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
//import android.widget.LinearLayout;
//import android.view.animation.TranslateAnimation;
//import android.widget.LinearLayout.LayoutParams;
import com.nineoldandroids.animation.ObjectAnimator;
//import android.widget.ToggleButton;
import com.nineoldandroids.view.ViewHelper;

public class LocationService extends Service implements LocationListener {

	private final IBinder mBinder = new LocationServiceBinder();
	private LocationManager locationManager;
	private boolean gps_started = false;
	public boolean gpsOn = false;
	private BufferedWriter out;
	private static String TAG = "LocationService";
	//private TranslateAnimation animation = new TranslateAnimation(0, 0, 0, 0);
	private int curMapI = -1;
	private int curMapJ = -1;
	public NotificationManager mNotificationManager;
	private NotificationCompat.Builder mBuilder;
	private SimpleDateFormat date = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
	private float scale;
	private String filename;

	public LocationService() {
		super();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "inside OnCreate()");
		Global.serviceStarted = true;
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Intent intent = new Intent(this, GPSActivity.class);
	    PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, Notification.FLAG_ONGOING_EVENT);

	    mBuilder = new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_notif)
		        .setContentTitle("Grimpant is tracking")
		        .setContentText("No GPS lock")
	    		.setContentIntent(pIntent)
	    		.setOngoing(true)
	    		.setTicker("Grimpant is tracking");

	    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		//TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		//stackBuilder.addParentStack(GPSActivity.class);
		//stackBuilder.addNextIntent(intent);
		//PendingIntent pIntent =
		//        stackBuilder.getPendingIntent(
		//            0,
		//            PendingIntent.FLAG_UPDATE_CURRENT
		//        );
		//mBuilder.setContentIntent(pIntent);
		//NotificationManager mNotificationManager =
		//    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		scale = getResources().getDisplayMetrics().widthPixels/320.0f;
		//startForeground();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	public boolean isGps_started() {
		return gps_started;
	}

	//public void checkGps() {
		//gpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	//}
	
	//public boolean isGpsOn() {
		//gpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		//return gpsOn;
		//return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	//}

	public void setGps_started(boolean gps_started) {
		this.gps_started = gps_started;
	}

	public void startGPS() {
		Log.v(TAG, "Start Tracking");
		gps_started = true;
		//String filename = android.text.format.DateFormat.format(
		//		"yyyyMMddHHmmss", new java.util.Date()).toString();
		filename = date.format(new Date());
		//Log.v("date", filename + ".csv");
		Global.lastSession = filename;
		//File outputFile = new File(getApplicationContext().getExternalFilesDir(null), filename);
		File outputFile = new File(Environment.getExternalStorageDirectory().getPath() + "/Grimpant", filename + ".csv");
		FileWriter gpxwriter = null;
		if (!outputFile.exists()) {
			try {
				//outputFile.mkdirs();
				outputFile.createNewFile();
				gpxwriter = new FileWriter(outputFile, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (gpxwriter != null) {
				out = new BufferedWriter(gpxwriter);
			}
		}

		if (gps_started) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, this);

			mNotificationManager.notify(0, mBuilder.build());
		}

		/*float leftLon = -79.05f;
		float widthLon = 0.4f;
		float widthDP = 2000f;
		float topLat = 43.1f;
		float heightLat = 0.3f;
		float heightDP = 2050f;*/
		//final float scale = getResources().getDisplayMetrics().density;
		double x = ViewHelper.getTranslationX(GPSActivity.mapAll) - ((int)409.6*scale)/2.0;
		double y = ViewHelper.getTranslationY(GPSActivity.mapAll) - ((int)409.6*scale)/2.0;
	    
		if (Math.abs((int) x - ViewHelper.getTranslationX(GPSActivity.mapAll)) > 0 || Math.abs((int) y - ViewHelper.getTranslationY(GPSActivity.mapAll)) > 0) {
			ObjectAnimator animationX = ObjectAnimator.ofFloat(GPSActivity.mapAll, "translationX", ViewHelper.getTranslationX(GPSActivity.mapAll), (int) x);
			animationX.setDuration(750);
			animationX.start();
			ObjectAnimator animationY = ObjectAnimator.ofFloat(GPSActivity.mapAll, "translationY", ViewHelper.getTranslationY(GPSActivity.mapAll), (int) y);
			animationY.setDuration(750);
			animationY.start();

			ObjectAnimator animationX2 = ObjectAnimator.ofFloat(GPSActivity.mapPins, "translationX", ViewHelper.getTranslationX(GPSActivity.mapPins), (int) x);
			animationX2.setDuration(750);
			animationX2.start();
			ObjectAnimator animationY2 = ObjectAnimator.ofFloat(GPSActivity.mapPins, "translationY", ViewHelper.getTranslationY(GPSActivity.mapPins), (int) y);
			animationY2.setDuration(750);
			animationY2.start();
	    	Log.v("Location", ViewHelper.getTranslationX(GPSActivity.mapAll) + ", " + ViewHelper.getTranslationY(GPSActivity.mapAll) + "; " + x + ", " + y);
    	}

		
		int i = (int)(-(x-160*scale)/(int)(409.6*scale));
		int j = (int)(-(y-160*scale)/(int)(409.6*scale));

		if (curMapI != i || curMapJ != j) {
			for (int k = 0; k < 20; k++) {
				for (int l = 0; l < 20; l++) {
					GPSActivity.map[k][l].setImageBitmap(null);
				}
			}
			//int id = i + 1 + j*10;
			//int resID = getResources().getIdentifier("map_" + id, "drawable", getPackageName());
			//Log.v("Position", i + "," + j + "," + "map_master_" + id);
			for (int k = -1; k < 2; k++) {
				if(i + k >= 0 && i + k < 20) {
					for (int l = -1; l < 2; l++) {
						if(j + l >= 0 && j + l < 20) {
							int id = (i + k) + 1 + (j + l)*20;
							int resID = getResources().getIdentifier("map_" + id, "drawable", getPackageName());
							Bitmap bMap = BitmapFactory.decodeResource(getResources(), resID);
							GPSActivity.map[i + k][j + l].setImageBitmap(bMap);
							bMap = null;
							Log.v("Position", (i + k) + "," + (j + l) + "," + "map_" + id);
						}
					}
				}
			}
			//Bitmap bMap = BitmapFactory.decodeResource(getResources(), resID);
			//GPSActivity.map[i][j].setImageBitmap(bMap);
			//bMap = null;
			curMapI = i;
			curMapJ = j;
		}
		
		/*if (x-160*scale < -1000*scale) {
			if (y-160*scale < -1025*scale) {
				Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.empty);
				GPSActivity.map[0][0].setImageBitmap(bMap);
				//GPSActivity.map2.setImageBitmap(bMap);
				//GPSActivity.map3.setImageBitmap(bMap);

				bMap = BitmapFactory.decodeResource(getResources(), R.drawable.map_master_04);
				GPSActivity.map[1][1].setImageBitmap(bMap);
				bMap = null;
				Log.v("Position1", x + "," + y);
			}
			else {
				Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.empty);
				GPSActivity.map[0][0].setImageBitmap(bMap);
				//GPSActivity.map3.setImageBitmap(bMap);
				//GPSActivity.map4.setImageBitmap(bMap);

				//bMap = BitmapFactory.decodeResource(getResources(), R.drawable.map_master_02);
				//GPSActivity.map2.setImageBitmap(bMap);
				bMap = null;
			}
		}
		else {
			if (y-160*scale < -1025*scale) {
				Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.empty);
				GPSActivity.map[0][0].setImageBitmap(bMap);
				//GPSActivity.map2.setImageBitmap(bMap);
				//GPSActivity.map4.setImageBitmap(bMap);

				//bMap = BitmapFactory.decodeResource(getResources(), R.drawable.map_master_03);
				//GPSActivity.map3.setImageBitmap(bMap);
				bMap = null;
			}
			else {
				//Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.empty);
				//GPSActivity.map2.setImageBitmap(bMap);
				//GPSActivity.map3.setImageBitmap(bMap);
				//GPSActivity.map4.setImageBitmap(bMap);

				Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.map_master_01);
				GPSActivity.map[0][0].setImageBitmap(bMap);
				bMap = null;
				Log.v("Position4", (x-160*scale) + "," + (y-160*scale) + "," + GPSActivity.map[0][0].getHeight());
			}
		}*/
	}

	public void stopGPS() {
		Log.v(TAG, "Stop Tracking");
		gps_started = false;
		mBuilder.setContentText("No GPS lock");
		locationManager.removeUpdates(this);
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		mNotificationManager.cancel(0);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void onLocationChanged(Location location) {
		Global.curLocation = location;
		// JSONObject jsonObj = new JSONObject();
		// try {
		// jsonObj.put("Action", "onLocationChanged");
		// jsonObj.put("Time", location.getTime());
		// jsonObj.put("Latitude", location.getLatitude());
		// jsonObj.put("Longitude", location.getLongitude());
		// jsonObj.put("Accuracy", location.getAccuracy());
		// jsonObj.put("Altitude", location.getAltitude());
		// jsonObj.put("Bearing", location.getBearing());
		// jsonObj.put("Speed", location.getSpeed());
		// out.write(jsonObj.toString());
		// out.write("\n");
		//
		// } catch (Exception e) {
		// Log.e(TAG, "JSONException");
		// return;
		// }
		//SimpleDateFormat date = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
		String stamp = date.format(new Date());
		String comm = ",";
		String line = location.getLatitude()
				+ comm
				+ location.getLongitude()
				+ comm
				+ stamp;
		try {
			out.write(line + "\n");
			Global.lastSession = filename;
			Global.lastLocation = line;
			mBuilder.setContentText("GPS lock");
			mNotificationManager.notify(0,mBuilder.build());
			Global.gotLock = true;
			if (GPSActivity.pngAnim1.getVisibility() == View.INVISIBLE) {
				//GPSActivity.rb.setEnabled(true);
				GPSActivity.mapOver.setVisibility(View.INVISIBLE);
				GPSActivity.center.setVisibility(View.VISIBLE);
			}
			Log.v("Location", line);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//final float scale = getResources().getDisplayMetrics().density;
		float leftLon = -79.05f;
		float widthLon = 0.4f;
		//float leftLon = 3.759888f;
		//float widthLon = 0.21992f;
		int widthDP = (int)(409.6*20*scale);
		float topLat = 43.1f;
		float heightLat = 0.3f;
		//float topLat = 43.659722f;
		//float heightLat = 0.159391f;
		int heightDP = (int)(409.6*20*scale);
		double x = (widthDP*(leftLon - location.getLongitude())/widthLon + 160)*scale;
		double y = (heightDP*(location.getLatitude() - topLat)/heightLat + 160)*scale;
		//LayoutParams layoutParams = new LayoutParams((int) (widthDP*scale), (int) (heightDP*scale));
	    //layoutParams.setMargins((int) x, (int) y, 0, 0);
		if (Math.abs((int) x - ViewHelper.getTranslationX(GPSActivity.mapAll)) > 0 || Math.abs((int) y - ViewHelper.getTranslationY(GPSActivity.mapAll)) > 0) {
			ObjectAnimator animationX = ObjectAnimator.ofFloat(GPSActivity.mapAll, "translationX", ViewHelper.getTranslationX(GPSActivity.mapAll), (int) x);
			animationX.setDuration(750);
			animationX.start();
			ObjectAnimator animationY = ObjectAnimator.ofFloat(GPSActivity.mapAll, "translationY", ViewHelper.getTranslationY(GPSActivity.mapAll), (int) y);
			animationY.setDuration(750);
			animationY.start();

			ObjectAnimator animationX2 = ObjectAnimator.ofFloat(GPSActivity.mapPins, "translationX", ViewHelper.getTranslationX(GPSActivity.mapPins), (int) x);
			animationX2.setDuration(750);
			animationX2.start();
			ObjectAnimator animationY2 = ObjectAnimator.ofFloat(GPSActivity.mapPins, "translationY", ViewHelper.getTranslationY(GPSActivity.mapPins), (int) y);
			animationY2.setDuration(750);
			animationY2.start();
	    	//Log.v("Location", ViewHelper.getTranslationX(GPSActivity.mapPins) + ", " + ViewHelper.getTranslationY(GPSActivity.mapPins) + "; " + x + ", " + y);
	    	Log.v("Test", x + ", " + y);
    	}
		
		int i = (int)(-x/(int)(409.6*scale));
		int j = (int)(-y/(int)(409.6*scale));

		if (curMapI != i || curMapJ != j) {
			for (int k = 0; k < 20; k++) {
				for (int l = 0; l < 20; l++) {
					GPSActivity.map[k][l].setImageBitmap(null);
				}
			}
			//int id = i + 1 + j*10;
			//int resID = getResources().getIdentifier("map_" + id, "drawable", getPackageName());
			//Log.v("Position", i + "," + j + "," + "map_master_" + id);
			for (int k = -1; k < 2; k++) {
				if(i + k >= 0 && i + k < 20) {
					for (int l = -1; l < 2; l++) {
						if(j + l >= 0 && j + l < 20) {
							int id = (i + k) + 1 + (j + l)*20;
							int resID = getResources().getIdentifier("map_" + id, "drawable", getPackageName());
							Bitmap bMap = BitmapFactory.decodeResource(getResources(), resID);
							GPSActivity.map[i + k][j + l].setImageBitmap(bMap);
							bMap = null;
							Log.v("Position", (i + k) + "," + (j + l) + "," + "map_" + id);
						}
					}
				}
			}
			//Bitmap bMap = BitmapFactory.decodeResource(getResources(), resID);
			//GPSActivity.map[i][j].setImageBitmap(bMap);
			//bMap = null;
			curMapI = i;
			curMapJ = j;
		}

		/*if (x < -1000*scale) {
			if (y < -1025*scale) {
				//Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.empty);
				GPSActivity.map[0][0].setImageBitmap(null);
				//GPSActivity.map2.setImageBitmap(bMap);
				//GPSActivity.map3.setImageBitmap(bMap);

				//Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.map_master_04);
				//GPSActivity.map4.setImageBitmap(bMap);
				//bMap = null;
			}
			else {
				//Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.empty);
				GPSActivity.map[0][0].setImageBitmap(null);
				//GPSActivity.map3.setImageBitmap(bMap);
				//GPSActivity.map4.setImageBitmap(bMap);

				//bMap = BitmapFactory.decodeResource(getResources(), R.drawable.map_master_02);
				//GPSActivity.map2.setImageBitmap(bMap);
				//bMap = null;
			}
		}
		else {
			if (y < -1025*scale) {
				//Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.empty);
				GPSActivity.map[0][0].setImageBitmap(null);
				//GPSActivity.map2.setImageBitmap(bMap);
				//GPSActivity.map4.setImageBitmap(bMap);

				//bMap = BitmapFactory.decodeResource(getResources(), R.drawable.map_master_03);
				//GPSActivity.map3.setImageBitmap(bMap);
				//bMap = null;
			}
			else {
				//Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.empty);
				//GPSActivity.map2.setImageBitmap(bMap);
				//GPSActivity.map3.setImageBitmap(bMap);
				//GPSActivity.map4.setImageBitmap(bMap);

				//Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.map_master_00);
				//GPSActivity.map[0][0].setImageBitmap(bMap);
				//bMap = null;
			}
		}*/
		
		//GPSActivity.map.setLayoutParams(layoutParams);
	}

	public void onProviderDisabled(String provider) {
		Log.v("GPS", "Sensor Disabled");
		Global.GPSDisabled = true;
		/*Log.v("Test", Global.firstToggle + "," + Global.firstTask);
		if (Global.firstToggle) {
			GPSDisabled task = new GPSDisabled();
			try {
				task.execute();
			}
			catch (Exception e) {
				Log.v("Error", "Cannot execute task");
			}
			Global.firstToggle= false; 
		}*/
	}

	public void onProviderEnabled(String provider) {
		Log.v("GPS", "Sensor Enabled");
		Global.GPSDisabled = false;
		//Global.firstToggle= false; 
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
	/*private class GPSDisabled extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
            AlertDialog.Builder builder = new AlertDialog.Builder(Global.mainContext);
            builder.setMessage("Your GPS is disabled! Would you like to enable it?");
            builder.setCancelable(false);
            builder.setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                      Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                      gpsOptionsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                      startActivity(gpsOptionsIntent);
                  }
             });
             builder.setNegativeButton("Do Nothing", new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                	  dialog.cancel();
                  }
             });
             AlertDialog alert = builder.create();
             alert.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			return null;
		}
	}*/

	
	public class LocationServiceBinder extends Binder {
		LocationService getService() {
			return LocationService.this;
		}
	}

	@Override
	public void onDestroy() {
		mNotificationManager.cancel(0);
		Global.serviceStarted = false;
	}
}