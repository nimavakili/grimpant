package edu.ub.phonelab.locationvisualization;

import java.io.File;
import java.io.IOException;

//import com.varma.samples.audiorecorder.AppLog;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

public class AudioRecorder extends Activity {
	
	private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
	private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
	//private static final String AUDIO_RECORDER_FOLDER = Global.lastSession;
	
	private MediaRecorder recorder = null;
	private int currentFormat = 0;
	private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP };
	private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP }; 

    AudioRecorder() {}
	
	private String getFilename(){
		String filepath = Environment.getExternalStorageDirectory().getPath();
		Log.v("AudioRecorder", "Session: " + Global.lastSession);
		File file = new File(filepath, Global.lastSession);
		
		if(!file.exists()){
			file.mkdirs();
		}
		
		return (file.getAbsolutePath() + "/" + Global.lastLocation + file_exts[currentFormat]);
	}

	public void startRecording(){
		recorder = new MediaRecorder();
		
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(output_formats[currentFormat]);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setOutputFile(getFilename());
		
		recorder.setOnErrorListener(errorListener);
		recorder.setOnInfoListener(infoListener);
		
		try {
			recorder.prepare();
			recorder.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stopRecording(){
		if(null != recorder){
			recorder.stop();
			recorder.reset();
			recorder.release();
			recorder = null;
		}
	}

	private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
		@Override
		public void onError(MediaRecorder mr, int what, int extra) {
			Log.i("AudioRecorder", "Error: " + what + ", " + extra);
		}
	};
	
	private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
		@Override
		public void onInfo(MediaRecorder mr, int what, int extra) {
			Log.i("AudioRecorder", "Warning: " + what + ", " + extra);
		}
	};
}