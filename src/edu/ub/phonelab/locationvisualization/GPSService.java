package edu.ub.phonelab.locationvisualization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class GPSService extends Service implements LocationListener {

	private String TAG = "GPSService";
	private final IBinder mBinder = new LocationServiceBinder();
	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder mBuilder;
	private LocationManager locationManager;
	private boolean GPSDisabled = false;
	private boolean isTracking = false;
	private boolean gotLock = false;
	//private Location curLocation = null;
	private boolean bound = false;
	private BufferedWriter out;
	private SimpleDateFormat date = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
	private String filename;
	//private TimerTask gpsLockCheck;
	private long lastLocationMillis;
	private float scale;
	private int curMapI = -1;
	private int curMapJ = -1;
	private String lastSession = "NoSession";
	private String lastLocation = "NoLocation";
	private Handler handler;
	private Runnable updateMapOC;
	public static final double leftLon = 3.759888d;
	public static final double widthLon = 0.21992d;
	public static final double topLat = 43.659722d;
	public static final double heightLat = 0.159391d;
	private int linesCt = 0;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "OnCreate");

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		Timer timer = new Timer();
		LockCheck lockCheck = new LockCheck();
		lastLocationMillis = System.currentTimeMillis();
		timer.schedule(lockCheck, 5000, 5000);

		Intent intent = new Intent(this, GPSActivity.class);
	    PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, Notification.FLAG_ONGOING_EVENT);
	    mBuilder = new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_notif)
		        .setContentTitle(getResources().getString(R.string.is_tracking))
		        .setContentText(getResources().getString(R.string.no_lock))
	    		.setContentIntent(pIntent)
	    		.setOngoing(true)
	    		.setTicker(getResources().getString(R.string.is_tracking));
	    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		scale = getResources().getDisplayMetrics().widthPixels/320.0f;
		
		handler = new Handler();

		updateMapOC = new Runnable() {
			@Override
			public void run() {
				try {
					GPSActivity.mapOver.setVisibility(View.VISIBLE);
					GPSActivity.center.setVisibility(View.INVISIBLE);
					if (!GPSDisabled) {
						GPSActivity.searching.setVisibility(View.VISIBLE);
					}
					else {
						GPSActivity.searching.setVisibility(View.INVISIBLE);
					}
					if (!GPSActivity.GPSAlert.isShowing()) {
						//Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_lock), Toast.LENGTH_LONG).show();
						Toast lockToast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_lock), Toast.LENGTH_LONG);
						lockToast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
						lockToast.show();
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		/*gpsLockCheck = new Runnable() {
			@Override
			public void run() {
				if (System.currentTimeMillis() - lastLocationMillis > 15000) {
					if (gotLock) {
						Log.v(TAG, "OnStatusChanged NoLock");
						gotLock = false;
						if (isTracking) {
							mBuilder.setContentText("No GPS lock");
							mNotificationManager.notify(1366, mBuilder.build());
							//startForeground(0, mBuilder.build());
						}
					}
				}
			}
		};*/
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "OnStartCommand");
		//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, this);
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.v(TAG, "OnBind");
		onRebind(null);
		return mBinder;
	}

	public void onRebind(Intent intent) {
		Log.v(TAG, "OnRebind");
		bound = true;
		//if (!gotLock && !GPSDisabled)
			//GPSActivity.searching.setVisibility(View.VISIBLE);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.v(TAG, "OnUnbind");
		bound = false;
		if (!isTracking) {
			Log.v(TAG, "OnUnbind StopSelf");
			locationManager.removeUpdates(this);
			stopSelf();
		}
		return true;
	}

	public class LocationServiceBinder extends Binder {
		GPSService getService() {
			return GPSService.this;
		}
	}
	
	public void onProviderDisabled(String provider) {
		GPSDisabled = true;
		//GPSActivity.searching.setVisibility(View.INVISIBLE);
		noLock();
	}

	public void onProviderEnabled(String provider) {
		GPSDisabled = false;
		//if (!gotLock && GPSActivity.gb.getVisibility() != View.VISIBLE && !GPSActivity.recordingMode && !GPSActivity.listeningMode)
		//	GPSActivity.searching.setVisibility(View.VISIBLE);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
	public void startTracking() {
		Log.v(TAG, "startTracking");
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return;
		}
		startForeground(1366, mBuilder.build());
		isTracking = true;
		linesCt = 0;

		filename = date.format(new Date());
		File outputFile = new File(Environment.getExternalStorageDirectory().getPath() + "/Grimpant", filename + ".csv");
		lastSession = filename;
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
		
		if (!gotLock && !GPSDisabled) {
			//Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_lock), Toast.LENGTH_LONG).show();
			Toast lockToast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_lock), Toast.LENGTH_LONG);
			lockToast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
			lockToast.show();
		}
		
		//updateMap(43.610873d, 3.876306d);
		//updateMap(43.659722d, 3.759888d);
		//updateMap(43.659722d - 0.159391d, 3.759888d + 0.21992d);
		//updateMap(43.611962825625106d,3.876388898489914d);
		//GPSActivity.mapOver.setVisibility(View.INVISIBLE);
	}

	public void onLocationChanged(Location location) {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return;
		}
		//curLocation = location;
		lastLocationMillis = System.currentTimeMillis();
		if (!gotLock) {
			Log.v(TAG, "OnLockChanged Lock");
			gotLock = true;
			Toast lockToast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.lock), Toast.LENGTH_LONG);
			lockToast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
			lockToast.show();
			mBuilder.setContentText(getResources().getString(R.string.lock));
			if (isTracking) {
				mNotificationManager.notify(1366, mBuilder.build());
			}
		}
		
		String stamp = date.format(new Date());
		String comm = ",";
		String line = location.getLatitude() + comm	+ location.getLongitude() + comm + stamp;
		lastLocation = line;
		Log.v("Location", line);

		if (isTracking) {
			String path = lastLocation;
			String name = path.split("/")[path.split("/").length - 1];
			double lat = Double.parseDouble(name.split(",")[0]);
			double lon = Double.parseDouble(name.split(",")[1]);
			if ((lat < topLat - heightLat || lat > topLat || lon < leftLon || lon > leftLon + widthLon) && linesCt < 10) {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.outide_map), Toast.LENGTH_LONG).show();
				//Log.v(TAG, "Track Size: " + linesCt);
				File trackFile = new File(Environment.getExternalStorageDirectory().getPath() + "/Grimpant", lastSession + ".csv");
				GPSActivity.tb.setChecked(false);
				stopTracking();
				trackFile.delete();
			}
			try {
				out.write(line + "\n");
				linesCt++;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (bound) {
			updateMap(location.getLatitude(), location.getLongitude());
		}
	}
	
	private class LockCheck extends TimerTask {
		public void run() {
			if (System.currentTimeMillis() - lastLocationMillis > 30000) {
				if (gotLock) {
					noLock();
				}
			}
		}
	}
	public void stopTracking() {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return;
		}
		Log.v(TAG, "stopTracking");
		stopForeground(true);
		isTracking = false;

		//locationManager.removeUpdates(this);

		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isTracking() {
		return isTracking;
	}

	public boolean gotLock() {
		return gotLock;
	}

	public boolean GPSDisabled() {
		return GPSDisabled;
	}

	/*public Location curLocation() {
		return curLocation;
	}*/

	public String lastSession() {
		return lastSession;
	}
	
	public String lastLocation() {
		return lastLocation;
	}
	
	public void updateMap(double lat, double lon) {
		if (GPSActivity.gb.getVisibility() != View.VISIBLE) {// && !GPSActivity.listeningMode) {
			GPSActivity.mapOver.setVisibility(View.INVISIBLE);
			GPSActivity.searching.setVisibility(View.INVISIBLE);
			GPSActivity.center.setVisibility(View.VISIBLE);
		}
		
		//float leftLon = -79.05f; //buffalo
		//float widthLon = 0.4f;
		//float leftLon = -74.0f; //new york
		//float widthLon = 0.4f;
		//double leftLon = 3.759888d; //montpellier
		//double widthLon = 0.21992d;
		//float topLat = 43.1f; //buffalo
		//float heightLat = 0.3f;
		//float topLat = 40.7f; //new york
		//float heightLat = 0.3f;
		//double topLat = 43.659722d; // montpellier
		//double heightLat = 0.159391d;
		int widthDP = ((int)(409.6*scale))*20;
		int heightDP = ((int)(409.6*scale))*20;
		//double x = ViewHelper.getTranslationX(GPSActivity.mapAll) - ((int)409.6*scale)/2.0;
		//double y = ViewHelper.getTranslationY(GPSActivity.mapAll) - ((int)409.6*scale)/2.0;
		//if (location != null) {
		double x = (widthDP*(leftLon - lon)/widthLon) + 160*scale;
		double y = (heightDP*(lat - topLat)/heightLat) + 160*scale;
		//}
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
			curMapI = i;
			curMapJ = j;
		}		
	}
	
	private void noLock() {
		if (GPSActivity.gb.getVisibility() != View.VISIBLE) {
			//GPSActivity.mapOver.setVisibility(View.VISIBLE);
			//GPSActivity.center.setVisibility(View.INVISIBLE);
			handler.postDelayed(updateMapOC, 100);
		}
		Log.v(TAG, "OnLockChanged NoLock");
		gotLock = false;
		mBuilder.setContentText(getResources().getString(R.string.no_lock));
		if (isTracking) {
			mNotificationManager.notify(1366, mBuilder.build());
		}
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "OnDestroy");
		//stopForeground(true);
		//locationManager.removeUpdates(this);
	}
}