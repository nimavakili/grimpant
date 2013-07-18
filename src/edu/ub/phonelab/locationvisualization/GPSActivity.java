package edu.ub.phonelab.locationvisualization;

import java.io.BufferedInputStream;
import android.util.AttributeSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.io.IOException;
//import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
//import com.nineoldandroids.view.ViewHelper;
//import org.xmlpull.v1.XmlPullParser;
//import org.xmlpull.v1.XmlPullParserException;
//import org.xmlpull.v1.XmlPullParserFactory;
//import com.uraroji.garage.android.mp3recvoice.MainActivity;
//import com.uraroji.garage.android.mp3recvoice.RecMicToMp3;
//import com.uraroji.garage.android.mp3recvoice.RecMicToMp3;
//import com.varma.samples.audiorecorder.AppLog;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
//import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
//import android.content.res.TypedArray;
import android.graphics.Color;
//import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
//import android.graphics.drawable.Drawable.Callback;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.media.MediaRecorder;
//import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
//import android.os.Looper;
//import android.os.Handler;
//import android.os.Message;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
//import android.util.AttributeSet;
import android.util.Log;
//import android.util.Xml;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.Menu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
//import android.view.animation.Animation;
//import android.view.animation.Animation.AnimationListener;
//import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ToggleButton;
import edu.ub.phonelab.locationvisualization.R;
import com.bugsense.trace.BugSenseHandler;

public class GPSActivity extends Activity {

	private Context mContext;
	//private AudioRecorder recorder = new AudioRecorder();
	private MP3Recorder recorder = new MP3Recorder(44100, 64);
	
	private static String TAG = "GPSActiviy";
	private GPSService gpsService;
	private ServiceConnection gpsServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			gpsService = ((GPSService.LocationServiceBinder) service).getService();
			Log.v(TAG, "GPS service is tracking: " + gpsService.isTracking());
			if (gpsService.isTracking()) {
				tb.setChecked(true);
			}
			trackFile = new File(Environment.getExternalStorageDirectory().getPath() + "/Grimpant", gpsService.lastSession() + ".csv");
			//Log.v("Upload", trackFile.getName());
			if (gb.getVisibility() == View.INVISIBLE) {
				getUnsent(true);
				//addAllSeeds();
				handler.post(GPSDialog);
				if (!isUploading && !recordingMode)
					handler.post(unsentDialog);
				//handler.postDelayed(GPSDialog, 500);
			}
			//else if (gpsService.isTracking()) {
				//onGo(null);
			//}
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			gpsService = null;
		}
	};
	float scale;
	int width;
	public static Button gb, ub, bb, sb, stb;//, pb;
	public static ToggleButton tb, rb, pb;
	public static TextView tt, rt, gt, searching;
	public static VideoView video;
	public static ImageView center, mapOver, pngAnim1, blank;
	public static LinearLayout mapAll;
	public static RelativeLayout parent, mapPins, about, help;
	public static LinearLayout[] mapH = new LinearLayout[20];
	public static ImageView[][] map = new ImageView[20][20];
	public MediaPlayer mPlayer;
	public static boolean recordingMode = false;
	public static boolean listeningMode = false;
	//private AnimationDrawable encircleAnim1, encircleAnim2;
	private int curFrameI;
	private long curFrameT1, curFrameT2;
	private Timer timer;
	private boolean cancelAnim = false;
	private boolean firstFocus = true;
	private Drawable frame;
	private Runnable setFrame, animCompletion, GPSDialog, unsentDialog, saveDialog, okDialog, deleteDialog;
	private boolean frameLoaded = false;
	//private boolean animStopped = false;
	//loadVine pieceHandler;
	loadNextFrame load;
	setNextFrame set;
	//loadNextPiece loadNext;
	//playNextPiece playNext;
	public ArrayList<File> unsentFiles = new ArrayList<File>();
	public ArrayList<String> allAudioPaths = new ArrayList<String>();
	public int unsentTracks = 0;
	public int unsentAudios = 0;
	private Handler handler;
	public NotificationManager mNotificationManager;
	private NotificationCompat.Builder mBuilder;
	public static AlertDialog GPSAlert, unsentAlert, saveAlert, okAlert, deleteAlert;
	private boolean saveRecording = false;
	private File trackFile;
	private Intent gpsIntent;
	private boolean isUploading = false;
	//private AnimationDrawable budAnim;
	
    /*private int iFrameCount = 0;
    //public ImageView myIV;
    private String myString;
    long startTime;
    long endTime;
    private int[] budRes = new int[40];
    Runnable r1 = new Runnable() 
	{
	    public void run() 
	    {
	        while (iFrameCount < 40) 
	        {
	        	runOnUiThread(new Runnable() 
	        {
	        	public void run() 
	            {
	        		if (iFrameCount < 40) {
	        		//String image = "";
	        		//if (iFrameCount < 10)
	        		//	image = "bud_0000" + iFrameCount;
	        		//else
	        		//	image = "bud_000" + iFrameCount;
	                //int resID = getResources().getIdentifier(image, "drawable", getPackageName()); 
	                int resID = budRes[iFrameCount];
	        		if (resID != 0)    //if there is no image/change for this frame skip it
	                {
	            	    myString = "iFrameCount: " + iFrameCount;
	            	    Log.v("Anim", myString);

	                    //speed the same between setImageResource & setBackgroundResource
	                    //myIV.setImageResource(resID);
	                    //myIV.postInvalidate();

	            	    startTime = System.currentTimeMillis();
	                    pngAnim.setBackgroundResource(resID);
	                    endTime = System.currentTimeMillis();
	                    System.out.println("setBackground took: " + (endTime - startTime) + " milliseconds");
	                }
	                else
	                {  //we can skip frames 1-119, 209-251, 272-322, 416-472 & 554-745 (as no change from previous frame)
	            	    myString="File skipped: " + iFrameCount;
	            	    Log.v("Anim", myString);
	                }
	                iFrameCount++;
	        		}
	           }
	     });

	     try 
	     {
	         Thread.sleep(200);
	     }
	     catch (InterruptedException iex) {}
	             }
	          Log.v("Anim", "Finished playing all frames");     
	         }
	   };*/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
		    @Override
		    public void uncaughtException(Thread thread, Throwable ex)
		    {
		        new Thread() {
		            @Override
		            public void run() {
		        		Log.v("ErrorTest", "toast");
		                Looper.prepare();
		                Toast.makeText(getApplicationContext(), "Unfotunately Grimpant crashed!", Toast.LENGTH_LONG).show();
		                Looper.loop();
		                
		            }
		        }.start();
		        ex.printStackTrace();
		        try
		        {
		            Thread.sleep(3500); // Let the Toast display before app will get shutdown
		        }
		        catch (InterruptedException e)
		        {
		        	e.printStackTrace();
		        }
		        System.exit(1);
		    }
		});*/
		BugSenseHandler.initAndStartSession(this, "c662635a");
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_gps);
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Toast.makeText(getApplicationContext(), getStr(R.string.no_sd), Toast.LENGTH_LONG).show();
			//moveTaskToBack(true);
			finish();
			return;
		}
		File grimpant = new File(Environment.getExternalStorageDirectory().getPath() + "/Grimpant");
		if (!grimpant.exists()) {
			grimpant.mkdir();
		}

		/*String image = "";
		for (int i = 0; i < 40; i++) {
    		if (i < 10)
    			image = "bud_0000" + i;
    		else
    			image = "bud_000" + i;
            budRes[i] = getResources().getIdentifier(image, "drawable", getPackageName());
		}*/

		scale = getResources().getDisplayMetrics().widthPixels/320.0f;
		width = getResources().getDisplayMetrics().widthPixels;
		gb = (Button) findViewById(R.id.goButton);
		bb = (Button) findViewById(R.id.backButton);
		sb = (Button) findViewById(R.id.saveButton);
		stb = (Button) findViewById(R.id.stopButton);
		pb = (ToggleButton) findViewById(R.id.playButton);
		//ub = (Button) findViewById(R.id.uploadButton);
		tb = (ToggleButton) findViewById(R.id.trackingButton);
		rb = (ToggleButton) findViewById(R.id.recordingButton);
		//rb.setEnabled(false);
		tt = (TextView) findViewById(R.id.trackingText);
		rt = (TextView) findViewById(R.id.recordingText);
		gt = (TextView) findViewById(R.id.goText);
		center = (ImageView) findViewById(R.id.center);
		mapOver = (ImageView) findViewById(R.id.mapOver);
		searching = (TextView) findViewById(R.id.searching);
		parent = (RelativeLayout) findViewById(R.id.parent);
		mapPins = (RelativeLayout) findViewById(R.id.mapPins);
		about = (RelativeLayout) findViewById(R.id.aboutFrame);
		help = (RelativeLayout) findViewById(R.id.helpFrame);
		pngAnim1 = (ImageView) findViewById(R.id.pngAnim);
		//pngAnim2 = (ImageView) findViewById(R.id.pngAnim2);
		//encircleAnim1 = new AnimationDrawable();
		//encircleAnim2 = new AnimationDrawable();
		//ub.setVisibility(View.INVISIBLE);
		gb.setEnabled(false);
		tb.setVisibility(View.INVISIBLE);
		rb.setVisibility(View.INVISIBLE);
		//tt.setVisibility(View.INVISIBLE);
		//rt.setVisibility(View.INVISIBLE);
		center.setVisibility(View.INVISIBLE);
		mapOver.setVisibility(View.INVISIBLE);
		searching.setVisibility(View.INVISIBLE);
		blank = (ImageView) findViewById(R.id.blank);
		//mapFrame = (LinearLayout) findViewById(R.id.map);
		
		//final float scale = getResources().getDisplayMetrics().density;
		mapAll = (LinearLayout) findViewById(R.id.map);
		//Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.empty);
		float widthDP = 409.6f;
		float heightDP = 409.6f;
		for (int j = 0; j < 20; j++) {
			mapH[j] = new LinearLayout(this);
			mapAll.addView(mapH[j]);
			for (int i = 0; i < 20; i++) {
				map[i][j] = new ImageView(this);
				//map[i][j].setImageBitmap(null);
				map[i][j].setScaleType(ImageView.ScaleType.FIT_XY);
				mapH[j].addView(map[i][j], (int)(widthDP*scale), (int)(heightDP*scale));
				//map[i][j] = (ImageView) findViewById(R.id.map1);
			}
		}

		gpsIntent = new Intent(getApplicationContext(), GPSService.class);
		//startService(gpsIntent);
		
		mContext = this;
		
		video = (VideoView) findViewById(R.id.videoView);
		//video.setVisibility(VideoView.INVISIBLE);
		Uri uri = Uri.parse("android.resource://edu.ub.phonelab.locationvisualization/raw/vine_grow_up");
		video.setVideoURI(uri);
		//video.setBackgroundResource(R.drawable.empty);
		//video.setBackgroundColor(Color.TRANSPARENT);
		video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp)
            {                  
                //player = mp;
            	mp.setLooping(true);
        		//video.setVisibility(VideoView.VISIBLE);
            	//video.start();
            }
        });
		/*video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
            	Log.v("video", "completed");
    			sb.setVisibility(View.VISIBLE);
    			bb.setVisibility(View.VISIBLE);
    			//rb.setEnabled(false);
    			pb.setVisibility(View.VISIBLE);
    			stb.setVisibility(View.VISIBLE);
    			stb.setEnabled(false);
    			rb.setChecked(false);
    			recorder.stop();
    			//video.pause();
    			//Log.v("test", "file://" + Environment.getExternalStorageDirectory().getPath() + "/" + Global.lastSession + "/" + Global.lastLocation + ".mp3");
    			Uri uri = Uri.parse("file://" + Global.lastMP3);
    			//File audioFile = new File(MP3Recorder.getFileName());
    			//Log.v("test", "Exists: " + audioFile.exists());
    			//Uri uri = Uri.parse("file://" + Environment.getExternalStorageDirectory().getPath() + "/" + Global.lastSession + "/" + Global.lastLocation + ".mp3");
    			mPlayer = MediaPlayer.create(Global.mainContext, uri);
    			mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
    	            public void onCompletion(MediaPlayer mp) {
    	        		video.pause();
    	        		pb.setEnabled(true);
    	        		stb.setEnabled(false);
    	            }
    	        });
            }
        });*/
		video.start();
		handler = new Handler();
		setFrame = new Runnable() {
			@Override
			public void run() {
				//Log.v("Test", "Delay2: " + (System.currentTimeMillis() - curFrameT2));
				if (System.currentTimeMillis() - curFrameT2 <= 50) {
					pngAnim1.setImageDrawable(frame);
				}
			}
		};
		animCompletion = new Runnable() {
			@Override
			public void run() {
				stopAnim();
				rb.setChecked(false);
			}
		};
		GPSDialog = new Runnable() {
			@Override
			public void run() {
				if (gpsService != null) {
					if (gpsService.GPSDisabled()) {
						searching.setVisibility(View.INVISIBLE);
		            	if (GPSAlert != null) {
		            		if (GPSAlert.isShowing()) {
		            			return;
		            		}
		            	}
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			            builder.setMessage(getStr(R.string.gps_disabled));
			            builder.setCancelable(false);
			            builder.setPositiveButton(getStr(R.string.enable_gps), new DialogInterface.OnClickListener() {
			                 public void onClick(DialogInterface dialog, int id) {
			                      Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			                      gpsOptionsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			                      startActivity(gpsOptionsIntent);
			                  }
			             });
			             builder.setNegativeButton(getStr(R.string.do_nothing), new DialogInterface.OnClickListener() {
			                  public void onClick(DialogInterface dialog, int id) {
			                	  dialog.cancel();
			                  }
			             });
			             GPSAlert = builder.create();
			             GPSAlert.show();
		            }
					else {
						if (!gpsService.gotLock())
							searching.setVisibility(View.VISIBLE);
					}
				}
			}
		};
		unsentDialog = new Runnable() {
			@Override
			public void run() {
				if ((tb.isChecked() && unsentTracks > 1) || (tb.isChecked() && unsentAudios > 0) || (!tb.isChecked() && unsentFiles.size() > 0)) {
	            	if (unsentAlert != null) {
	            		if (unsentAlert.isShowing()) {
	            			return;
	            		}
	            	}
	            	AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	            	if (unsentTracks > 0 && unsentAudios > 0)
		            	//builder.setMessage("Grimpant has " + unsentTracks + " unsent track(s) and " + unsentAudios + " unsent recording(s). Upload now?");
	            		builder.setMessage(String.format(getStr(R.string.unsent_both), unsentTracks, unsentAudios));
	            	else if (unsentTracks > 0)
		            	//builder.setMessage("Grimpant has " + unsentTracks + " unsent track(s). Upload now?");
	            		builder.setMessage(String.format(getStr(R.string.unsent_track), unsentTracks));
	            	else if (unsentAudios > 0)
		            	//builder.setMessage("Grimpant has " + unsentAudios + " unsent recording(s). Upload now?");
	            		builder.setMessage(String.format(getStr(R.string.unsent_audio), unsentAudios));
		            builder.setCancelable(false);
		            builder.setPositiveButton(getStr(R.string.yes), new DialogInterface.OnClickListener() {
		            	public void onClick(DialogInterface dialog, int id) {
		            		UploadUnsent uu = new UploadUnsent();
		            		uu.execute();
		            	}
		            });
		            builder.setNegativeButton(getStr(R.string.no), new DialogInterface.OnClickListener() {
		            	public void onClick(DialogInterface dialog, int id) {
		            		dialog.cancel();
		                }
		            });
		            unsentAlert = builder.create();
		            unsentAlert.show();
	            }
			}
		};
		saveDialog = new Runnable() {
			@Override
			public void run() {
            	if (saveAlert != null) {
            		if (saveAlert.isShowing()) {
            			return;
            		}
            	}
            	AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            	if (saveRecording)
            		builder.setMessage(getStr(R.string.upload_recording));
            	else
            		builder.setMessage(getStr(R.string.upload_track));
	            builder.setCancelable(false);
	            builder.setPositiveButton(getStr(R.string.yes), new DialogInterface.OnClickListener() {
	            	public void onClick(DialogInterface dialog, int id) {
	            		if (saveRecording) {
	            			onSave(null);
	            		}
	            		else {
	            			UploadFile ut = new UploadFile(trackFile);
	        				ut.execute();
	            		}
	            	}
	            });
	            builder.setNegativeButton(getStr(R.string.no), new DialogInterface.OnClickListener() {
	            	public void onClick(DialogInterface dialog, int id) {
	            		if (saveRecording)
	            			onBack(null);
	            		else {
	            			trackFile.delete();
	            		}
	            		dialog.cancel();
	                }
	            });
	            saveAlert = builder.create();
	            saveAlert.show();
			}
		};
		okDialog = new Runnable() {
			@Override
			public void run() {
            	if (okAlert != null) {
            		if (okAlert.isShowing()) {
            			return;
            		}
            	}
            	AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	            builder.setCancelable(false);
            	/*if (!tb.isChecked()) {
            		builder.setMessage("Recording requires GPS lock. Do you want to start tracking?");
    	            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	            	public void onClick(DialogInterface dialog, int id) {
   	            			tb.setChecked(true);
    	            		onTrackingToggled(null);
    	            	}
    	            });
    	            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
    	            	public void onClick(DialogInterface dialog, int id) {
    	            		dialog.cancel();
    	                }
    	            });
            	}*/
            	if (gpsService.GPSDisabled()) {
		            builder.setMessage(getStr(R.string.need_lock) + " " + getStr(R.string.gps_disabled));
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
            	}
            	else {
            		builder.setMessage(getStr(R.string.need_lock) + " " + getStr(R.string.wait_lock));
    	            builder.setNeutralButton(getStr(R.string.ok), new DialogInterface.OnClickListener() {
    	            	public void onClick(DialogInterface dialog, int id) {
    	            		dialog.cancel();
    	                }
    	            });
            	}
	            saveAlert = builder.create();
	            saveAlert.show();
			}
		};
		deleteDialog = new Runnable() {
			@Override
			public void run() {
            	if (deleteAlert != null) {
            		if (deleteAlert.isShowing()) {
            			return;
            		}
            	}
            	AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            	if (unsentTracks > 0 || unsentAudios > 0)
            		builder.setMessage(getStr(R.string.unsent_notif) + " " + getStr(R.string.delete_notif));
            	else
            		builder.setMessage(getStr(R.string.delete_notif));
	            builder.setCancelable(false);
	            builder.setPositiveButton(getStr(R.string.yes), new DialogInterface.OnClickListener() {
	            	public void onClick(DialogInterface dialog, int id) {
	            		File grimpant = new File(Environment.getExternalStorageDirectory().getPath() + "/Grimpant");
	            		File[] files = grimpant.listFiles();
	            		for (int i = 0; i < files.length; i++) {
	            			files[i].delete();
	            		}
	            	}
	            });
	            builder.setNegativeButton(getStr(R.string.no), new DialogInterface.OnClickListener() {
	            	public void onClick(DialogInterface dialog, int id) {
	            		dialog.cancel();
	            	}
	            });
	            deleteAlert = builder.create();
	            deleteAlert.show();
			}
		};

		Intent intent = new Intent(this, GPSActivity.class);
	    PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, Notification.FLAG_AUTO_CANCEL);

	    mBuilder = new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_notif)
		        .setContentTitle(getStr(R.string.unsent_notif))
		        .setContentText(getStr(R.string.detail_notif))
	    		.setContentIntent(pIntent)
	    		.setAutoCancel(true)
	    		.setTicker(getStr(R.string.unsent_notif));

	    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (firstFocus) {
			//pngAnim1.setBackgroundResource(R.drawable.encircle_forward_1);
			//encircleAnim1 = (AnimationDrawable) pngAnim1.getBackground();
			
			LayoutParams params1 = pngAnim1.getLayoutParams();
			params1.height = (int)(290*scale);
			params1.width = (int)(290*scale);
			pngAnim1.setLayoutParams(params1);
			//LayoutParams params2 = pngAnim2.getLayoutParams();
			//params2.height = (int)(290*scale);
			//params2.width = (int)(290*scale);
			//pngAnim2.setLayoutParams(params2);
			LayoutParams params3 = video.getLayoutParams();
			params3.height = width;
			params3.width = width;
			video.setLayoutParams(params3);
			
			//budAnim = (AnimationDrawable) pngAnim2.getBackground();

			//parent.removeView(findViewById(R.id.spinner));
			//findViewById(R.id.spinner).setVisibility(View.INVISIBLE);
			blank.setVisibility(View.INVISIBLE);
			blank.setBackgroundColor(Color.argb(127, 255, 255, 255));
			gb.setEnabled(true);
			//parent.removeView(blank);
			firstFocus = false;
			if (tb.isChecked()) {
				onGo(null);
				//onTrackingToggled(null);
			}
		}
		Runtime.getRuntime().gc();
		//Log.v("test", "Display Width: " + getResources().getDisplayMetrics().widthPixels);
		//Log.v("test", "Scale: " + scale);
		//Log.v("test", "Display Density: " + getResources().getDisplayMetrics().density);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_gps, menu);

	    getLayoutInflater().setFactory( new Factory() {
	        @Override
	        public View onCreateView ( String name, Context context, AttributeSet attrs ) {
                //Log.v("Menu", "View name: " + name);
	            //if ( name.equalsIgnoreCase( "com.android.internal.view.menu.ListMenuItemView" ) ) {
	            if ( name.equalsIgnoreCase( "TextView" ) ) {
	                try { // Ask our inflater to create the view
	                	//TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.MenuView, 0, 0);
	                	//boolean forceIcon = true;
                        //Log.v("Menu", "Menu : " + forceIcon);
	                    LayoutInflater li = LayoutInflater.from(context);
                        //Log.v("Menu", "Menu AttCount: " + attrs.getAttributeName(0));
	                    final View view = li.createView( name, null, attrs );
	                    // Kind of apply our own background
	                    handler.post( new Runnable() {
	                        public void run () {
	                            //Log.v("Menu", "Menu Width: " + view.getWidth());
	                        	//((ListMenuItemView) view).setForceShowIcon(true);
	                        	if (((TextView) view).getText().equals(getStr(R.string.menu_delete))) {
		                        	((TextView) view).setTextSize(16);
	                        	}
	                            //((RelativeLayout) view).setMinimumWidth(getResources().getDisplayMetrics().widthPixels);
	                            //view.setMinimumWidth(getResources().getDisplayMetrics().widthPixels);
	                            //view.setBackgroundColor(Color.BLACK);
	                        }
	                    });
	                    return view;
	                }
	                catch ( InflateException e ) {
	                    e.printStackTrace();
	                }
	                catch ( ClassNotFoundException e ) {
	                    e.printStackTrace();
	                }
	            }
	            return null;
	        }
	    });
	    
	    return true;
	}
	
//	@Override
//	public boolean onOptionsItemSelected (MenuItem item) {
//		switch (item.getItemId()) {
//			case R.id.menu_about:
//			//
//			return true;
//		}
//		return false;		
//	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Toast.makeText(getApplicationContext(), getStr(R.string.no_sd), Toast.LENGTH_LONG).show();
			moveTaskToBack(true);
			return;
		}
		//Global.focused = true;
		startService(gpsIntent);
		bindService(gpsIntent, gpsServiceConnection, Context.BIND_AUTO_CREATE);
		//if (gb.getVisibility() == View.INVISIBLE) {
			//getUnsent();
			//addAllSeeds();
			//if (!isUploading && !recordingMode)
				//handler.post(unsentDialog);
			//handler.postDelayed(GPSDialog, 500);
		//}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return;
		}
		if (gb.getVisibility() == View.VISIBLE) {
			onGo(null);
		}
		else {
			getUnsent(false);
		}
		unbindService(gpsServiceConnection);
		//Global.focused = false;
		//uploadData(null);
	}

	public void onGo(View view) {
		//bindService(gpsIntent, gpsServiceConnection, Context.BIND_AUTO_CREATE);

		getUnsent(true);
		//addAllSeeds();
		if (view != null) {
			if (!isUploading && !recordingMode)
				handler.post(unsentDialog);
			handler.postDelayed(GPSDialog, 100);
		}
		
		gb.setVisibility(View.INVISIBLE);
		gt.setVisibility(View.INVISIBLE);
		//ub.setVisibility(View.VISIBLE);
		tb.setVisibility(View.VISIBLE);
		rb.setVisibility(View.VISIBLE);
		//tt.setVisibility(View.VISIBLE);
		//rt.setVisibility(View.VISIBLE);
		//center.setVisibility(View.VISIBLE);
		mapOver.setVisibility(View.VISIBLE);
		//searching.setVisibility(View.VISIBLE);
		//try {
			//player.setLooping(false);
			//player.stop();
		video.pause();
			//video.seekTo(0);
		video.setVisibility(View.INVISIBLE);
			/*video.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
	        {
	
	            public void onPrepared(MediaPlayer mp)
	            {                  
	                player = mp;
	            	player.setLooping(false);
	        		//video.setVisibility(VideoView.VISIBLE);
	            	//video.start();
	            }
	        });*/
		video.suspend();
		//}
		//catch (NullPointerException e) {
		//	e.printStackTrace();
		//}
		/*Uri uri = Uri.parse("android.resource://edu.ub.phonelab.locationvisualization/raw/bud");
		video.setVideoURI(uri);
		video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp)
            {                  
                //player = mp;
            	mp.setLooping(false);
        		//video.setVisibility(VideoView.VISIBLE);
            	//video.start();
            }
        });*/
		parent.removeView(video);
		//locationService.startGPS();
		//pngAnim2.setVisibility(View.VISIBLE);
		//mapOver.setVisibility(View.INVISIBLE);
    	//seedPlayAnim();
		//onSeedListen(null);
		Runtime.getRuntime().gc();
	}
	
	public void onTrackingToggled(View view) {
		boolean on = tb.isChecked();

		if (on) {
			// Start listening to GPS
			//Global.firstToggle = true;
			final Handler handler = new Handler();
			handler.post(GPSDialog);
			
			gpsService.startTracking();
			trackFile = new File(Environment.getExternalStorageDirectory().getPath() + "/Grimpant", gpsService.lastSession() + ".csv");

			//mapOver.setVisibility(View.INVISIBLE);

		    //Thread thr1 = new Thread(r1);
		    //thr1.start();
		}
		else {
			// Disable GPS
			gpsService.stopTracking();
			//Global.firstToggle = false;
			//rb.setEnabled(false);
			//mapOver.setVisibility(View.VISIBLE);
			//center.setVisibility(View.INVISIBLE);
			//audioOnly = false;
			//uploadData(null);
			if (trackFile.length() < 1) {
				trackFile.delete();
			}
			else {
				saveRecording = false;
				handler.post(saveDialog);
			}
			//UploadTrack ut = new UploadTrack(trackFile);
			//ut.execute();
			//GPSService.gotLock = false;
			//Global.lastSession = "NoSession";
			//Global.lastLocation = "NoLocation";
		}
	}

	public void onRecordingToggled(View view) {
		boolean start = rb.isChecked();
		if (start) {
			if (!gpsService.gotLock()) {
				handler.post(okDialog);
				rb.setChecked(false);
				return;
			}
			//double leftLon = 3.759888d;
			//double widthLon = 0.21992d;
			//double topLat = 43.659722d;
			//double heightLat = 0.159391d;
			String path = gpsService.lastLocation();
			String name = path.split("/")[path.split("/").length - 1];
			double lat = Double.parseDouble(name.split(",")[0]);
			double lon = Double.parseDouble(name.split(",")[1]);
			if (lat < GPSService.topLat - GPSService.heightLat || lat > GPSService.topLat || lon < GPSService.leftLon || lon > GPSService.leftLon + GPSService.widthLon) {
				Toast.makeText(getApplicationContext(), getStr(R.string.outide_map), Toast.LENGTH_LONG).show();
				rb.setChecked(false);
				return;
			}
			if (pb.getVisibility() == View.VISIBLE) {
				onBack(null);
			}
			sb.setVisibility(View.INVISIBLE);
			//bb.setVisibility(View.INVISIBLE);
			tb.setEnabled(false);
			blank.setVisibility(View.VISIBLE);
			//center.setVisibility(View.INVISIBLE);
			playAnim();
			recorder.start(path);
			recordingMode = true;
			//addSeed(recorder.lastMP3());
		}
		else {
			stopAnim();
		}
	}

	public void onListen(View view) {
		boolean play = pb.isChecked();
		if (play) {
			onPlay(view);
		}
		else {
			if (listeningMode) {
				seedStopAnim();
				return;
			}
			onStop(view);
		}
		
	}
	
	public void onPlay(View view) {
		//video.seekTo(0);
		//video.start();
		//pb.setEnabled(false);
		stb.setEnabled(true);
		rb.setEnabled(false);
		bb.setEnabled(false);
		sb.setEnabled(false);

		playAnim();
		//mPlayer.seekTo(0);
		try {
			mPlayer.start();
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	public void onStop(View view) {
		//video.pause();
		pb.setEnabled(true);
		stb.setEnabled(false);
		//rb.setEnabled(true);
		bb.setEnabled(true);
		sb.setEnabled(true);

		try {
			mPlayer.pause();
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}
		//mPlayer.stop();
		stopAnim();
		saveRecording = true;
		handler.post(saveDialog);
	}
	
	public void onSave(View view) {
		//mPlayer.stop();
		try {
			mPlayer.release();
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}
		pb.setVisibility(View.INVISIBLE);
		stb.setVisibility(View.INVISIBLE);
		sb.setVisibility(View.INVISIBLE);
		//bb.setVisibility(View.INVISIBLE);
		tb.setEnabled(true);
		rb.setEnabled(true);
		pngAnim1.setVisibility(View.INVISIBLE);
		blank.setVisibility(View.INVISIBLE);
		//pngAnim2.setVisibility(View.INVISIBLE);
		//audioOnly = true;
		//uploadData(null);
		//rb.setEnabled(true);
		if (gpsService.gotLock())
			center.setVisibility(View.VISIBLE);
		//video.setVisibility(View.INVISIBLE);
		//View seed = addSeed(recorder.lastMP3());
		addSeed(recorder.lastMP3());
		File audioFile = new File(recorder.lastMP3());
		Log.v("Upload", "Audio file exists: " + audioFile.exists());
		UploadFile ua = new UploadFile(audioFile);
		ua.execute();
		recordingMode = false;
		//seed.performClick();
	}

	public void onBack(View view) {
		//mPlayer.stop();
		mPlayer.release();
		pb.setVisibility(View.INVISIBLE);
		stb.setVisibility(View.INVISIBLE);
		sb.setVisibility(View.INVISIBLE);
		//bb.setVisibility(View.INVISIBLE);
		tb.setEnabled(true);
		rb.setEnabled(true);
		pngAnim1.setVisibility(View.INVISIBLE);
		blank.setVisibility(View.INVISIBLE);
		//pngAnim2.setVisibility(View.INVISIBLE);

		//if (view != null) {
		try {
			File audioFile = new File(recorder.lastMP3());
			audioFile.delete();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		//audioOnly = true;
		//uploadData(null);
		//rb.setEnabled(true);
		if (gpsService.gotLock())
			center.setVisibility(View.VISIBLE);
		//video.setVisibility(View.INVISIBLE);
		recordingMode = false;
	}
	
	public void onAbout(MenuItem item) {
		//Log.v("Menu", "About clicked");
		onReturn(null);
		about.setVisibility(View.VISIBLE);
		bb.setVisibility(View.VISIBLE);
	}

	public void onHelp(MenuItem item) {
		//Log.v("Menu", "Help clicked");
		onReturn(null);
		help.setVisibility(View.VISIBLE);
		bb.setVisibility(View.VISIBLE);
	}

	public void onReturn(View view) {
		//Log.v("Menu", "Help clicked");
		about.setVisibility(View.INVISIBLE);
		help.setVisibility(View.INVISIBLE);
		bb.setVisibility(View.INVISIBLE);
	}
	
	public void onDelete(MenuItem item) {
		//Log.v("Menu", "Delete clicked");
		handler.post(deleteDialog);
	}

	/*public void uploadData(View view) {
		// launch dialog to upload files
		Log.v(TAG, "Upload btn pressed");
		UploadTask uTask = new UploadTask();
		uTask.execute();
	}

	public class UploadTask extends AsyncTask<Void, Void, Void> {

		private final ProgressDialog dialog = new ProgressDialog(
				GPSActivity.this);
		private File[] filestoupload;
		private File[] audiopath;
		private File[][] audiotoupload;

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			dialog.setTitle("Uploading Files");
			File root = getApplicationContext().getExternalFilesDir(null);
			filestoupload = root.listFiles();

			if (filestoupload != null) {
				String filepath = Environment.getExternalStorageDirectory().getPath();
				audiopath = new File[filestoupload.length];
				audiotoupload = new File[filestoupload.length][];
				for (int i = 0; i < filestoupload.length; i++) {
					audiopath[i] = new File(filepath, filestoupload[i].getName());
					if(audiopath[i].exists()){
						audiotoupload[i] = audiopath[i].listFiles();
						Log.v("UploadTask", "Uploading " + audiotoupload[i].length + " audio files...");
					}
				}
			}
			
			dialog.setMessage("Uploading " + filestoupload.length + " tracking files...");
			dialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {

			if (filestoupload != null) {
				FTPClient ftpClient = new FTPClient();
				try {
					TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
					String devid = tm.getDeviceId();
					ftpClient.connect(InetAddress
							.getByName("ftp.drivehq.com"));
					ftpClient.login("nimavakili", "miNimVcom1");
					if(!ftpClient.changeWorkingDirectory(devid)){
						ftpClient.makeDirectory(devid);
						ftpClient.changeWorkingDirectory(devid);
					}
					
					for (int i = 0; i < filestoupload.length; i++) {
						File toupload = filestoupload[i];
						ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
						BufferedInputStream buffIn = null;
						buffIn = new BufferedInputStream(new FileInputStream(toupload));
						if (!audioOnly) {
							ftpClient.enterLocalPassiveMode();
							ftpClient.storeFile(filestoupload[i].getName() + ".csv", buffIn);
						}
						buffIn.close();
						
						if (audiotoupload[i] != null) {
							ftpClient.makeDirectory(filestoupload[i].getName() + "-A");
							Log.v("UploadTask", "Create directory successful " + ftpClient.changeWorkingDirectory(filestoupload[i].getName() + "-A"));

							for (int j = 0; j < audiotoupload[i].length; j++) {
								toupload = audiotoupload[i][j];
								ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
								buffIn = null;
								buffIn = new BufferedInputStream(new FileInputStream(toupload));
								ftpClient.enterLocalPassiveMode();
							
								ftpClient.storeFile(audiotoupload[i][j].getName(), buffIn);
								buffIn.close();
								audiotoupload[i][j].delete();
							}
							audiopath[i].delete();
							ftpClient.changeToParentDirectory();
						}

						if (!audioOnly) {
							filestoupload[i].delete();
						}
					}
					
					ftpClient.logout();
					ftpClient.disconnect();
					runOnUiThread(successMessage);
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

				} catch (Exception e) {
					e.printStackTrace();
					runOnUiThread(changeMessage);
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
			return null;
		}

		private Runnable changeMessage = new Runnable() {
			@Override
			public void run() {
				dialog.setMessage("Error uploading the files, please try again later.");
			}
		};

		private Runnable successMessage = new Runnable() {
			@Override
			public void run() {
				dialog.setMessage("Finished uploading all files.");
			}
		};

		@Override
		protected void onPostExecute(Void result) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
		}
	}*/

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

	/*private class loadVine extends AsyncTask<Void, Void, Void> {
		//@Override
		//protected void onPreExecute() {
			//pngAnim2.setBackgroundResource(R.drawable.encircle_forward_3);
		//}
		@Override
		protected Void doInBackground(Void... params) {
			/*try {
				XmlPullParser parser = getResources().getXml(R.drawable.encircle_forward_2);
				AttributeSet attrs = Xml.asAttributeSet(parser);
				encircleAnim2.inflate(getResources(), parser, attrs);
			}
			catch (XmlPullParserException e1) {
				e1.printStackTrace();
			}
			catch (IOException e2) {
				e2.printStackTrace();
			}
			catch (NullPointerException e3) {
				e3.printStackTrace();
			}
			
			//for (int i = 90; i < 180; i += 3) {
			if ((curFrame/90)%2 == 1) {
				/*for (int i = 0; i < encircleAnim2.getNumberOfFrames(); i++){
				    Drawable frame = encircleAnim2.getFrame(i);
				    if (frame instanceof BitmapDrawable) {
				        ((BitmapDrawable)frame).getBitmap().recycle();
				    }
				    frame.setCallback(null);
				}
				encircleAnim2.setCallback(null);
				encircleAnim2 = new AnimationDrawable();
				encircleAnim2.setOneShot(true);
			}
			else {
				/*for (int i = 0; i < encircleAnim1.getNumberOfFrames(); i++){
				    Drawable frame = encircleAnim1.getFrame(i);
				    if (frame instanceof BitmapDrawable) {
				        ((BitmapDrawable)frame).getBitmap().recycle();
				    }
				    frame.setCallback(null);
				}
				encircleAnim1.setCallback(null);
				encircleAnim1 = new AnimationDrawable();
				encircleAnim1.setOneShot(true);
			}
    		String image = "";
			for (int i = curFrame; i < curFrame + 90; i += 3) {
				//if (!animStopped) {
				//Log.v("test", "CurFrame: " + i);
	    		if (i < 10)
	    			image = "encircle_0000" + i;
	    		else if (i < 100)
	    			image = "encircle_000" + i;
	    		else
	    			image = "encircle_00" + i;
	            int resID = getResources().getIdentifier(image, "drawable", getPackageName());
				if ((curFrame/90)%2 == 1) {
					//encircleAnim2.mutate();
					encircleAnim2.addFrame(getResources().getDrawable(resID), 200);
				}
				else {
					//encircleAnim1.mutate();
					encircleAnim1.addFrame(getResources().getDrawable(resID), 200);
				}
				try {
					Thread.sleep(10);
				}
				catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				//}
			}
			
			/*try {
				Thread.sleep(6000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}*/
			/*for (int i = 0; i < encircleAnim1.getNumberOfFrames(); i++){
			    Drawable frame = encircleAnim1.getFrame(i);
			    if (frame instanceof BitmapDrawable) {
			        ((BitmapDrawable)frame).getBitmap().recycle();
			    }
			    frame.setCallback(null);
			}
			encircleAnim1.setCallback(null);
			//encircleAnim1.stop();
			return null;
		}
		/*@Override
		protected void onPostExecute(Void result) {
			//pngAnim2.setBackgroundResource(R.drawable.encircle_forward_2);
			pngAnim2.setBackgroundDrawable(encircleAnim2);
			//encircleAnim2 = (AnimationDrawable) pngAnim2.getBackground();
			pngAnim1.setVisibility(View.GONE);
			pngAnim2.setVisibility(View.VISIBLE);
			encircleAnim2.start();
		}
	}*/

	/*private class loadVine extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			pngAnim1.setBackgroundResource(R.drawable.encircle_forward_3);
		}
		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(11000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			/*for (int i = 0; i < encircleAnim2.getNumberOfFrames(); i++){
			    Drawable frame = encircleAnim2.getFrame(i);
			    if (frame instanceof BitmapDrawable) {
			        ((BitmapDrawable)frame).getBitmap().recycle();
			    }
			    frame.setCallback(null);
			}
			encircleAnim2.setCallback(null);
			encircleAnim2.stop();
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			encircleAnim1 = (AnimationDrawable) pngAnim1.getBackground();
			pngAnim2.setVisibility(View.GONE);
			pngAnim1.setVisibility(View.VISIBLE);
			encircleAnim1.start();
		}
		
	}*/

	private class loadNextFrame extends TimerTask {
		public void run() {
			if (curFrameI <= 900 && !cancelAnim) {
				//Log.v("Test", "Delay0: " + (System.currentTimeMillis() - curFrameT1));
				if (System.currentTimeMillis() - curFrameT1 <= 250) {
	        	    curFrameT1 = System.currentTimeMillis();
		    		String image = "";
		    		if (curFrameI < 10)
		    			image = "encircle_0000" + curFrameI;
		    		else if (curFrameI < 100)
		    			image = "encircle_000" + curFrameI;
		    		else
		    			image = "encircle_00" + curFrameI;
		            int resID = getResources().getIdentifier(image, "drawable", getPackageName());
		            frame = getResources().getDrawable(resID);
		            frameLoaded = true;
					//Log.v("Test", "Delay1: " + (System.currentTimeMillis() - curFrameT));
					//runOnUiThread(setFrame);
		            
					//curFrameI += 3;
				}
				else {
					curFrameT1 = System.currentTimeMillis();
				}
			}
		}
	}

	private class setNextFrame extends TimerTask {
		public void run() {
			if (!cancelAnim) {
				if (curFrameI <= 900) {
	        	    //curFrameT = System.currentTimeMillis();
		    		//String image = "";
		    		//if (curFrameI < 10)
		    		//	image = "encircle_0000" + curFrameI;
		    		//else if (curFrameI < 100)
		    		//	image = "encircle_000" + curFrameI;
		    		//else
		    		//	image = "encircle_00" + curFrameI;
		            //int resID = getResources().getIdentifier(image, "drawable", getPackageName());
		            //frame = getResources().getDrawable(resID);
					curFrameT2 = System.currentTimeMillis();
					//Log.v("Test", "Delay1: " + (curFrameT2 - curFrameT1));
					//if (curFrameT2 - curFrameT1 <= 215 && curFrameT2 - curFrameT1 >= 185)
					if (frameLoaded) {
						runOnUiThread(setFrame);
						frameLoaded = false;
					}
		            
					curFrameI += 3;
				}
				else {
					runOnUiThread(animCompletion);
					cancelAnim = true;
				}
			}
		}
	}

	/*private class loadNextPiece extends TimerTask {
		public void run() {
			if (curFrame > 855) {
				curFrame = 0;
				cancelAnim = true;
			}
			loadVine pieceHandler = new loadVine();
			pieceHandler.execute();
		}
	}*/

	/*private class playNextPiece extends TimerTask {
		public void run() {
			if (curFrame < 900 && !cancelAnim) {
				runOnUiThread(new Runnable() {
					@SuppressWarnings("deprecation")
					public void run() {
						//encircleAnim1.stop();
						//encircleAnim2.stop();
						try {
							//if (curFrame >= curFrameI) {
							if ((curFrame/90)%2 == 1) {
								Log.v("Test", "pngAnim2: " + curFrame);
								pngAnim2.setBackgroundDrawable(encircleAnim2);
								pngAnim2.setVisibility(View.VISIBLE);
								pngAnim1.setVisibility(View.INVISIBLE);
								//Log.v("test", "First: " + (curFrame/90));
								//encircleAnim2.setVisible(true,true);
								encircleAnim2.start();
							}
							else {
								Log.v("Test", "pngAnim1: " + curFrame);
								pngAnim1.setBackgroundDrawable(encircleAnim1);
								pngAnim1.setVisibility(View.VISIBLE);
								pngAnim2.setVisibility(View.INVISIBLE);
								//Log.v("test", "Second: " + (curFrame/90));
								//encircleAnim2.setVisible(true,true);
								encircleAnim1.start();
							}
							//}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						curFrame += 90;
						//curFrameI = 0;
					}
				});
			}
			if (cancelAnim) {
				timer.cancel();
				//cancelAnim = false;
				runOnUiThread(new Runnable() {
					public void run() {
						stopAnim();
						rb.setChecked(false);
					}
				});
			}
		}
	}*/

	//@SuppressWarnings("deprecation")
	private void playAnim() {
		pngAnim1.setImageResource(R.drawable.encircle_00000);
		//frameSet = true;
		//pngAnim1.setImageBitmap(null);
		//pngAnim2.setVisibility(View.INVISIBLE);
		//pngAnim1.setBackgroundDrawable(encircleAnim1);
		//curFrame = 45;
		curFrameI = 3;
		cancelAnim = false;
		load = new loadNextFrame();
		set = new setNextFrame();
		//loadNext = new loadNextPiece();
		//playNext = new playNextPiece();
        timer = new Timer();
        //timer.schedule(loadNext, 100, 3000);        
        //timer.schedule(playNext, 3000, 3000);
	    curFrameT1 = System.currentTimeMillis() - 200;
	    //curFrameT2 = curFrameT1 + 200;
        timer.schedule(load, 0, 200);        
        timer.schedule(set, 0, 200);        
        //Log.v("test", "After pngAnim");
        pngAnim1.setVisibility(View.VISIBLE);
		//encircleAnim1.start();
		
		//video.setVisibility(View.VISIBLE);
		//player.stop();
		//video.seekTo(0);
		//video.start();
		/*if (player.isPlaying())
			Log.v("video", "playing");
		if (player.isLooping())
			Log.v("video", "looping");*/
		//player.start();
		//player.setLooping(false);
		//video.setBackgroundColor(Color.TRANSPARENT);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	//@SuppressWarnings("deprecation")
	private void stopAnim() {
		sb.setVisibility(View.VISIBLE);
		//bb.setVisibility(View.VISIBLE);
		rb.setEnabled(false);
		pb.setVisibility(View.VISIBLE);
		//stb.setVisibility(View.VISIBLE);
		stb.setEnabled(false);
		
		recorder.stop();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		/*String image = "";
		if (curFrameI < 10)
			image = "encircle_0000" + curFrameI;
		else if (curFrameI < 100)
			image = "encircle_000" + curFrameI;
		else
			image = "encircle_00" + curFrameI;
        int resID = getResources().getIdentifier(image, "drawable", getPackageName());
	    if (pngAnim1.getVisibility() == View.VISIBLE) {
	    	//Drawable frame = encircleAnim1.getFrame(curFrameI);
	    	//if (frame instanceof BitmapDrawable) {
	    		//Bitmap bm = ((BitmapDrawable) frame).getBitmap().copy(Bitmap.Config.ARGB_8888, false);
	    		//pngAnim1.setBackgroundDrawable(getResources().getDrawable(resID));
	    		pngAnim1.setImageDrawable(getResources().getDrawable(resID));
	    		//pngAnim1.setImageBitmap(bm);
	    		//pngAnim1.setImageDrawable(frame);
	    	//}
	    }
	    else {
	    	//Drawable frame = encircleAnim2.getFrame(curFrameI);
	    	//if (frame instanceof BitmapDrawable) {
	    		pngAnim2.setBackgroundDrawable(getResources().getDrawable(resID));
	    		//pngAnim2.setBackgroundDrawable(frame);
	    		//pngAnim2.setImageDrawable(frame);
	    	//}
	    }*/
		cancelAnim = true;
		timer.cancel();
		//timer.purge();
		//loadNext.cancel();
		//playNext.cancel();
		//encircleAnim1.stop();
		//encircleAnim2.stop();
		//animStopped = true;
		//video.pause();
		//curFrame = 0;
		//pieceHandler.cancel(true);
		//loadNext = new loadNextPiece();
        //timer = new Timer();
        //timer.schedule(loadNext, 0);
		
		//Log.v("test", "file://" + Environment.getExternalStorageDirectory().getPath() + "/" + Global.lastSession + "/" + Global.lastLocation + ".mp3");
		Uri uri = Uri.parse("file://" + recorder.lastMP3());
		//File audioFile = new File(MP3Recorder.getFileName());
		//Log.v("test", "Exists: " + audioFile.exists());
		//Uri uri = Uri.parse("file://" + Environment.getExternalStorageDirectory().getPath() + "/" + Global.lastSession + "/" + Global.lastLocation + ".mp3");
		try {
			mPlayer = MediaPlayer.create(this, uri);
			/*try {
				mPlayer.prepare();
			}
			catch (IOException e) {
				e.printStackTrace();
			}*/
			mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
	            public void onCompletion(MediaPlayer mp) {
	        		//video.pause();
	        		pb.setEnabled(true);
	        		stb.setEnabled(false);
	        		//rb.setEnabled(true);
	        		bb.setEnabled(true);
	        		sb.setEnabled(true);
	            	stopAnim();
	        		saveRecording = true;
	        		handler.post(saveDialog);
	            }
	        });
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	/*public class UploadAudio extends AsyncTask<Void, Void, Void> {
		private File audioToUpload;
		private String audioSession;
		
		public UploadAudio(File toUpload, String session) {
			super();
			audioToUpload = toUpload;
			audioSession = session + "-A";
		}

		@Override
		protected Void doInBackground(Void... params) {
			Log.v("Upload", "Uploading audio file...");
			if (audioToUpload.exists()) {
				isUploading = true; 
				mBuilder.setContentTitle("Grimpant upload");
				mBuilder.setContentText("Uploading recording...");
				mBuilder.setTicker("Uploading recording...");
				mNotificationManager.notify(1, mBuilder.build());
				FTPClient ftpClient = new FTPClient();
				try {
					TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
					String devid = tm.getDeviceId();
					ftpClient.connect(InetAddress
							.getByName("ftp.drivehq.com"));
					ftpClient.login("nimavakili", "miNimVcom1");

					if(!ftpClient.changeWorkingDirectory("Grimpant")){
						ftpClient.makeDirectory("Grimpant");
						ftpClient.changeWorkingDirectory("Grimpant");
					}
					if(!ftpClient.changeWorkingDirectory(devid)){
						ftpClient.makeDirectory(devid);
						ftpClient.changeWorkingDirectory(devid);
					}
					if(!ftpClient.changeWorkingDirectory(audioSession)){
						ftpClient.makeDirectory(audioSession);
						ftpClient.changeWorkingDirectory(audioSession);
					}

					ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
					BufferedInputStream buffIn = null;
					buffIn = new BufferedInputStream(new FileInputStream(audioToUpload));
					ftpClient.enterLocalPassiveMode();
					if (ftpClient.storeFile(audioToUpload.getName(), buffIn)) {
						mBuilder.setContentText("Finished uploading recording.");
						mBuilder.setTicker("Finished uploading recording.");
						mNotificationManager.notify(1, mBuilder.build());
						//audioToUpload.setLastModified(60000);
						//Runtime.getRuntime().exec("attrib +H " + audioToUpload.getAbsolutePath()); 
						File rename = new File(audioToUpload.getParent(), "." + audioToUpload.getName());
						audioToUpload.renameTo(rename);
						Log.v("Upload", "Upload Audio Successful");
					}
					else {
						mBuilder.setContentText("Error uploading recording!");
						mBuilder.setTicker("Error uploading recording!");
						mNotificationManager.notify(1, mBuilder.build());
					}
					buffIn.close();
					
					ftpClient.logout();
					ftpClient.disconnect();
				}
				catch (Exception e) {
					e.printStackTrace();
					mBuilder.setContentText("Error uploading recording!");
					mBuilder.setTicker("Error uploading recording!");
					mNotificationManager.notify(1, mBuilder.build());
				}
			}
			isUploading = false; 
			return null;
		}
	}*/

	public class UploadFile extends AsyncTask<Void, Void, Void> {
		private File fileToUpload;
		private boolean error = false;
		private String type = getStr(R.string.track);
		
		public UploadFile(File toUpload) {
			super();
			fileToUpload = toUpload;
		}

		@Override
		protected Void doInBackground(Void... params) {
			Log.v("Upload", "Uploading file...");
			if (fileToUpload.exists()) {
				isUploading = true;
				//String type;
				if (fileToUpload.getName().endsWith(".csv"))
					type = getStr(R.string.track);
				else
					type = getStr(R.string.audio);
				mBuilder.setContentTitle(getStr(R.string.upload_notif));
				mBuilder.setContentText(getStr(R.string.uploading) + " " + type + "...");
				mBuilder.setTicker(getStr(R.string.uploading) + " " + type + "...");
				mNotificationManager.notify(1, mBuilder.build());
				FTPClient ftpClient = new FTPClient();
				try {
					TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
					String devid = tm.getDeviceId();
					ftpClient.connect(InetAddress.getByName("ftp.minim-v.com"));
							//.getByName("ftp.drivehq.com"));
					ftpClient.login("grimpant", "miNimVcom1!");

					/*if(!ftpClient.changeWorkingDirectory("Grimpant")){
						ftpClient.makeDirectory("Grimpant");
						ftpClient.changeWorkingDirectory("Grimpant");
					}*/
					if(!ftpClient.changeWorkingDirectory(devid)){
						ftpClient.makeDirectory(devid);
						ftpClient.changeWorkingDirectory(devid);
					}

					ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
					BufferedInputStream buffIn = null;
					buffIn = new BufferedInputStream(new FileInputStream(fileToUpload));
					ftpClient.enterLocalPassiveMode();
					if (ftpClient.storeFile(fileToUpload.getName(), buffIn)) {
						//mBuilder.setContentText(getStr(R.string.uploading) + " " + type + " " + getStr(R.string.uploading_finished) + ".");
						//mBuilder.setTicker(getStr(R.string.uploading) + " " + type + " " + getStr(R.string.uploading_finished) + ".");
						//mNotificationManager.notify(1, mBuilder.build());
						//Toast.makeText(getApplicationContext(), getStr(R.string.uploading) + " " + type + " " + getStr(R.string.uploading_finished) + ".", Toast.LENGTH_LONG).show();
						//trackToUpload.setLastModified(60000);
						File rename = new File(fileToUpload.getParent(), "." + fileToUpload.getName());
						fileToUpload.renameTo(rename);
						//Runtime.getRuntime().exec("attrib +H " + trackToUpload.getAbsolutePath()); 
						Log.v("Upload", "Upload file successful");
					}
					else {
						//mBuilder.setContentText(getStr(R.string.uploading_error) + " " + type + " !");
						//mBuilder.setTicker(getStr(R.string.uploading_error) + " " + type + " !");
						//mNotificationManager.notify(1, mBuilder.build());
						//Toast.makeText(getApplicationContext(), getStr(R.string.uploading_error) + " " + type + " !", Toast.LENGTH_LONG).show();
						Log.v("Upload", "storeFile error");
						BugSenseHandler.addCrashExtraData("Upload " + type, "storeFile error");
						error = true;
					}
					buffIn.close();
					
					ftpClient.logout();
					ftpClient.disconnect();
				}
				catch (Exception e) {
					//mBuilder.setContentText(getStr(R.string.uploading_error) + " " + type + " !");
					//mBuilder.setTicker(getStr(R.string.uploading_error) + " " + type + " !");
					//mNotificationManager.notify(1, mBuilder.build());
					//Toast.makeText(getApplicationContext(), getStr(R.string.uploading_error) + " " + type + " !", Toast.LENGTH_LONG).show();
					e.printStackTrace();
					BugSenseHandler.sendException(e);
					error = true;
				}
			}
			else {
				Log.v("Upload", "File doesn't exist");
				BugSenseHandler.addCrashExtraData("Upload " + type, "File doesn't exist");
				error = true;
			}
			/*try {
				Thread.sleep(10000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			mNotificationManager.cancel(1);
			isUploading = false;
			getUnsent();*/
			return null;
		}
		protected void onPostExecute(Void result) {
			String msg;
			if (!error) {
				msg = getStr(R.string.uploading) + " " + type + " " + getStr(R.string.uploading_finished) + ".";
				//mBuilder.setContentText(msg);
				//mBuilder.setTicker(msg);
				mNotificationManager.cancel(1);
			}
			else {
				msg = getStr(R.string.uploading_error) + " " + type + " !";
				mBuilder.setContentText(msg);
				mBuilder.setTicker(msg);
				mNotificationManager.notify(1, mBuilder.build());
			}
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			isUploading = false; 
			getUnsent(true);
		}
	}

	public class UploadUnsent extends AsyncTask<Void, Void, Void> {
		//private File filesToUpload;
		private boolean error = false;
		
		@Override
		protected Void doInBackground(Void... params) {
			Log.v("Upload", "Uploading unsent files...");
			if (unsentFiles.size() > 0) {
				isUploading = true;
				mBuilder.setContentTitle(getStr(R.string.upload_notif));
				mBuilder.setContentText(getStr(R.string.uploading) + " " + getStr(R.string.uploading_unsent) + "...");
				mBuilder.setTicker(getStr(R.string.uploading) + " " + getStr(R.string.uploading_unsent) + "...");
				mNotificationManager.notify(1, mBuilder.build());
				FTPClient ftpClient = new FTPClient();
				try {
					TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
					String devid = tm.getDeviceId();
					ftpClient.connect(InetAddress.getByName("ftp.minim-v.com"));
					ftpClient.login("grimpant", "miNimVcom1!");
					/*if(!ftpClient.changeWorkingDirectory("Grimpant")) {
						ftpClient.makeDirectory("Grimpant");
						ftpClient.changeWorkingDirectory("Grimpant");
					}*/
					if(!ftpClient.changeWorkingDirectory(devid)) {
						ftpClient.makeDirectory(devid);
						ftpClient.changeWorkingDirectory(devid);
					}
					ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
					ftpClient.enterLocalPassiveMode();
					for (int i = 0; i < unsentFiles.size(); i++) {
						File fileToUpload = (File) unsentFiles.get(i);
						if (fileToUpload.exists()) {
						//try {
							/*TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
							String devid = tm.getDeviceId();
							ftpClient.connect(InetAddress.getByName("ftp.drivehq.com"));
							ftpClient.login("nimavakili", "miNimVcom1");
		
							if(!ftpClient.changeWorkingDirectory("Grimpant")) {
								ftpClient.makeDirectory("Grimpant");
								ftpClient.changeWorkingDirectory("Grimpant");
							}
							if(!ftpClient.changeWorkingDirectory(devid)) {
								ftpClient.makeDirectory(devid);
								ftpClient.changeWorkingDirectory(devid);
							}*/
							/*if(fileToUpload.getName().endsWith(".mp3")) {
								String audioSession = fileToUpload.getParentFile().getName(); 
								if(!ftpClient.changeWorkingDirectory(audioSession)) {
									ftpClient.makeDirectory(audioSession);
									ftpClient.changeWorkingDirectory(audioSession);
								}
							}*/
		
							//ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
							BufferedInputStream buffIn = null;
							buffIn = new BufferedInputStream(new FileInputStream(fileToUpload));
							//ftpClient.enterLocalPassiveMode();
														
							if (ftpClient.storeFile(fileToUpload.getName(), buffIn)) {
								File rename = new File(fileToUpload.getParent(), "." + fileToUpload.getName());
								fileToUpload.renameTo(rename);
								Log.v("Upload", "Upload File Successful");
							}
							else {
								Log.v("Upload", "storeFile error");
								BugSenseHandler.addCrashExtraData("Upload unsent", "storeFile error");
								error = true;
							}
							
							buffIn.close();
							//ftpClient.logout();
							//ftpClient.disconnect();
						/*}
						catch (Exception e) {
							e.printStackTrace();
							error = true;
						}*/
						}
						else {
							Log.v("Upload", "File doesn't exist");
							BugSenseHandler.addCrashExtraData("Upload unsent", "File doesn't exist");
							error = true;
						}
					}
					ftpClient.logout();
					ftpClient.disconnect();
				}
				catch (Exception e) {
					e.printStackTrace();
					BugSenseHandler.sendException(e);
					error = true;
				}
				/*String msg;
				if (!error) {
					msg = getStr(R.string.uploading) + " " + getStr(R.string.uploading_unsent) + " " + getStr(R.string.uploading_finished) + ".";
					mBuilder.setContentText(msg);
					mBuilder.setTicker(msg);
				}
				else {
					msg = getStr(R.string.uploading_error) + " " + getStr(R.string.uploading_error) + " !";
					mBuilder.setContentText(msg);
					mBuilder.setTicker(msg);
				}
				mNotificationManager.notify(1, mBuilder.build());
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
				try {
					Thread.sleep(10000);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				mNotificationManager.cancel(1);
				isUploading = false; 
				getUnsent();*/
			}
			return null;
		}
		protected void onPostExecute(Void result) {
			String msg;
			if (!error) {
				msg = getStr(R.string.uploading) + " " + getStr(R.string.uploading_unsent) + " " + getStr(R.string.uploading_finished) + ".";
				//mBuilder.setContentText(msg);
				//mBuilder.setTicker(msg);
				mNotificationManager.cancel(1);
			}
			else {
				msg = getStr(R.string.uploading_error) + " " + getStr(R.string.uploading_unsent) + " !";
				mBuilder.setContentText(msg);
				mBuilder.setTicker(msg);
				mNotificationManager.notify(1, mBuilder.build());
			}
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			isUploading = false; 
			getUnsent(true);
		}
	}

	private void getUnsent(boolean onResume) {
		if (!isUploading && !recordingMode) {
			File grimpant = new File(Environment.getExternalStorageDirectory().getPath() + "/Grimpant");
			File[] files = grimpant.listFiles();
			unsentFiles.clear();
			allAudioPaths.clear();
			unsentTracks = 0;
			unsentAudios = 0;
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					//Log.v("Upload", "Track isHidden: " + files[i].isHidden());
					if (files[i].getName().endsWith(".mp3") && !files[i].isHidden()) {
						String path = files[i].getAbsolutePath();
						//path = path.replace("/.", "/");
						//if (path.startsWith("."))
							//path = path.substring(1);
						allAudioPaths.add(path);
					}
					if (!files[i].isHidden()) {
						Log.v("Test", "this file is not hidden: " + files[i].getName());
						if (!(tb.isChecked() && trackFile.equals(files[i])))
							unsentFiles.add(files[i]);
						if (files[i].getName().endsWith(".mp3"))
							unsentAudios++;
						else {
							if (!(tb.isChecked() && trackFile.equals(files[i])))
								unsentTracks++;
						}
						//Log.v("Upload", "Track: " + files[i].getName());
					}
					//else
						//Log.v("Upload", "Track HiddenName: " + files[i].getName());
				}
				/*else {
					File[] audioFiles = files[i].listFiles();
					for (int j = 0; j < audioFiles.length; j++) {
						//Log.v("Upload", "Audio isHidden: " + audioFiles[j].isHidden());
						if (!audioFiles[j].isHidden()) {
							unsentFiles.add(audioFiles[j]);
							//Log.v("Upload", "Audio: " + audioFiles[j].getName());
						}
					}
				}*/
			}
			//Log.v("Test", "tbChecked: " + tb.isChecked());
			//if ((tb.isChecked() && unsentTracks > 1) || (tb.isChecked() && unsentAudios > 0) || (!tb.isChecked() && unsentFiles.size() > 0)) {
			if (unsentFiles.size() > 0) {
				mBuilder.setContentTitle(getStr(R.string.unsent_notif));
		        mBuilder.setContentText(getStr(R.string.detail_notif));
	    		mBuilder.setTicker(getStr(R.string.unsent_notif));
				mNotificationManager.notify(1, mBuilder.build());
			}
			Log.v("Upload", "Tracks: " + unsentTracks);
			Log.v("Upload", "Audios: " + unsentAudios);
			if (onResume) {
				DownloadAudioPaths getServerAudioPaths = new DownloadAudioPaths();
				getServerAudioPaths.execute();
			}
		}
	}
	
	public class DownloadAudioPaths extends AsyncTask<Void, Void, Void> {
		//boolean foundlocal = false;
		@Override
		protected Void doInBackground(Void... params) {
			try {
			    // Create a URL for the desired page
			    URL url = new URL("http://minim-v.com/paths.php?file_dir=grimpant");

			    // Read all the text returned by the server
			    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			    String path;
				//TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			    while ((path = in.readLine()) != null) {
			        // str is one line of text; readLine() strips the newline character(s)
			    	/*String devid = path.split("/")[path.split("/").length - 2];
			    	//Log.v("Test", devid);
			    	if (devid.equals(tm.getDeviceId())) {
				    	String name = path.split("/")[path.split("/").length - 1];
				    	String localpath = Environment.getExternalStorageDirectory().getPath() + "/Grimpant/" + name;
			    		if (allAudioPaths.contains(localpath)) {
					    	Log.v("Test", "found the recording on local storage");
					    	//foundlocal = true;
					    	continue;
			    		}
			    	}*/
			    	allAudioPaths.add(path);
			    	//addSeed()
			    }
			    in.close();
			}
			catch (MalformedURLException e) {
			}
			catch (IOException e) {
			}
			//finally {
			//}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			/*if (foundlocal) {
		    	mapPins.removeAllViews();
			}*/
			addAllSeeds();
		}
	}
	
	//private void getServerAudioPaths() {
	//}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			mNotificationManager.cancel(1);
			BugSenseHandler.closeSession(this);
			video.suspend();
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}
		//locationService.mNotificationManager.cancel(0);
		// Investigate this!
		//Intent gpsintent = new Intent(getApplicationContext(), LocationService.class);
		//stopService(gpsintent);
	}
	
	private View addSeed(String path) {
		try {
			//float leftLon = -79.05f;
			//float widthLon = 0.4f;
			//float leftLon = -74.0f; //new york
			//float widthLon = 0.4f;
			//double leftLon = 3.759888d;
			//double widthLon = 0.21992d;
			//float topLat = 43.1f;
			//float heightLat = 0.3f;
			//double topLat = 43.659722d;
			//double heightLat = 0.159391d;
			//float topLat = 40.7f; //new york
			//float heightLat = 0.3f;
			int widthDP = ((int)(409.6*scale))*20;
			int heightDP = ((int)(409.6*scale))*20;
			float seedWidth = 32.0f;
			float seedHeight = 32.0f;
			//Log.v("Test", path.split("/")[path.split("/").length - 1]);
			String name = path.split("/")[path.split("/").length - 1];
			//Log.v("Test", name);
			double lat = Double.parseDouble(name.split(",")[0]);
			double lon = Double.parseDouble(name.split(",")[1]);
			if (lat < GPSService.topLat - GPSService.heightLat || lat > GPSService.topLat || lon < GPSService.leftLon || lon > GPSService.leftLon + GPSService.widthLon) {
				//Toast.makeText(getApplicationContext(), "Your seed went into space!", Toast.LENGTH_LONG).show();
				return null;
			}
			double x = -(widthDP*(GPSService.leftLon - lon)/GPSService.widthLon + seedWidth*scale/2.0f);
			double y = -(heightDP*(lat - GPSService.topLat)/GPSService.heightLat + seedHeight*scale/2.0f);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)(seedWidth*scale), (int)(seedHeight*scale));
	    	Log.v("Test", "addSeed: " + x + ", " + y);
			params.leftMargin = (int) x;
			params.topMargin = (int) y;
			ImageView pin = new ImageView(this);
			pin.setImageResource(R.drawable.seed);
			pin.setScaleType(ImageView.ScaleType.FIT_CENTER);
			pin.setContentDescription(path);
			pin.setOnClickListener(new View.OnClickListener() {
			    @Override
			    public void onClick(View v) {
			        onSeedListen(v);
			    }
			});
			mapPins.addView(pin, params);
			Log.v("Test", "addSeed: " + path + " seed added");
			return pin;
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void addAllSeeds() {
		Log.v("Test", "addAllSeeds: childCount=" + mapPins.getChildCount());
		ArrayList<String> children = new ArrayList<String>();
		String path;
		for (int i = 0; i < mapPins.getChildCount(); i++) {
			path = mapPins.getChildAt(i).getContentDescription().toString();
			Log.v("Test", "addAllSeeds: i=" + i + ", childPath=" + path);
			//if (path.startsWith("."))
				//path = path.substring(1);
			//if starts with storage then check if the file exists else delete the view
			if (path.startsWith("/storage")) {
				File file = new File(path);
				//if (!file.exists())
					//file =  new File(path.replace("Grimpant/", "Grimpant/."));
				if (!file.exists()) {
					mapPins.removeViewAt(i);
					Log.v("Test", "addAllSeeds: deleted a seed");
				}
				else
					children.add(path);
			}
			else
			children.add(path);
		}
		for (int i = 0; i < allAudioPaths.size(); i++) {
			path = allAudioPaths.get(i);
			if (!children.contains(path)) {
				//Log.v("Test", "addAllSeeds: " + path + " seed added");
				addSeed(path);
			}
		}
	}

	public void onSeedListen(View view) {
		if (recordingMode || listeningMode)
			return;
		if (isNetworkAvailable()) {
			listeningMode = true;
			findViewById(R.id.spinner).setVisibility(View.VISIBLE);
			String path = view.getContentDescription().toString();
		    Log.v("Test", path);

			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		    String devid = path.split("/")[path.split("/").length - 2];
		    Log.v("Test", devid);
		    if (devid.equals(tm.getDeviceId())) {
			   	String name = path.split("/")[path.split("/").length - 1];
			   	String localpath = Environment.getExternalStorageDirectory().getPath() + "/Grimpant/." + name;
			    Log.v("Test", localpath);
			   	File localfile = new File(localpath);
			   	if (localfile.exists()) {
			   		path = localpath;
			   	}
		    }
			
			if (path.startsWith("/storage")) {
				File file = new File(path);
				if (!file.exists()) {
					file =  new File(path.replace("Grimpant/", "Grimpant/."));
				}
				if (file.exists()) {
				    Log.v("Test", "playing from storage");
					path = "file://" + path;
				}
				else
					return;
			}
			Uri uri = Uri.parse(path);
			mPlayer = new MediaPlayer();
			try {
				mPlayer.setDataSource(this, uri);
				//mPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
				mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
		            public void onPrepared(MediaPlayer mp) {
		            	findViewById(R.id.spinner).setVisibility(View.INVISIBLE);
		            	mPlayer.start();
		            	seedPlayAnim();
		            }
		        });
				mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
		            public void onCompletion(MediaPlayer mp) {
		            	seedStopAnim();
		            }
		        });
				mPlayer.prepareAsync();
				//center.setVisibility(View.INVISIBLE);
				pb.setVisibility(View.VISIBLE);
				pb.setChecked(true);
				rb.setEnabled(false);
				blank.setVisibility(View.VISIBLE);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			Toast.makeText(getApplicationContext(), getStr(R.string.no_internet), Toast.LENGTH_SHORT).show();
		}
	}
	
	private void seedPlayAnim() {
		//video.seekTo(0);
		//video.setVisibility(View.VISIBLE);
		//video.start();
		//pngAnim2.setVisibility(View.VISIBLE);
		//budAnim.start();
		playAnim();
	}
	
	private void seedStopAnim() {
		try{
			mPlayer.stop();
			mPlayer.release();
		}
		catch (IllegalStateException e) {
			e.printStackTrace();
		}
		//budAnim.stop();
		//video.pause();
		//video.setVisibility(View.INVISIBLE);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		cancelAnim = true;
		timer.cancel();
        pngAnim1.setVisibility(View.INVISIBLE);
    	findViewById(R.id.spinner).setVisibility(View.INVISIBLE);
		//pngAnim2.setVisibility(View.INVISIBLE);
		//if (gpsService.gotLock())
			//center.setVisibility(View.VISIBLE);
		pb.setVisibility(View.INVISIBLE);
		pb.setChecked(false);
		rb.setEnabled(true);
		blank.setVisibility(View.INVISIBLE);
		listeningMode = false;
	}
	
	private String getStr(int id) {
		return getResources().getString(id);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) { //Back key pressed
        	if (bb.getVisibility() == View.VISIBLE) {
		    	onReturn(null);
	        	Log.v("Test", "onBackDown");
		    	return true;
        	}
        	if (recordingMode)
        		return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}