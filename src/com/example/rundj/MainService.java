package com.example.rundj;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.os.IBinder;
import android.util.Log;
import com.example.rundj.MusicService.ServiceBinder;

public class MainService extends Service implements SensorEventListener {
	Sensor mAccelerometer = null;
	SensorManager sm = null;
	public double accelLast = 0;
	public double derivLast = 0;
	int time = 50;
	int playTime = 100;
	int changes = 0;
    MediaPlayer mPlayer;
    private int length = 0;
    int[] songs = {R.raw.slow, R.raw.medium, R.raw.fast};
    int song_id = 0;
	
	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
	    //TODO do something useful
		sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.i("RunDJ", "rundj service started");
		sm.registerListener(this, mAccelerometer, sm.SENSOR_DELAY_FASTEST);
		
		mPlayer.start();
		

	    return Service.START_NOT_STICKY;
	  }

	public void startSong(int speed){
		if(speed != song_id){
		onDestroy();
		song_id = speed;
			onCreate();
			mPlayer.start();
		}
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		
		double accel = Math.abs(getAccel(event));
		double delta = accel-accelLast;
		if(accel>20){
			changes++;
		}
		if(time<=0){
			changes = 0;
			time = 100;
		}
		if(playTime<=0){
			if(changes>25){
				this.startSong(2);
			}
			else if(changes >=10){
				this.startSong(1);
			}
			else{
				this.startSong(0);
			}
			playTime = 500;
		}
		playTime--;
		time--;
		derivLast = delta;
		accelLast = accel;
		Log.i("RunDJ", "changes:"+changes);		

	}
	public double getAccel(SensorEvent event){
		return Math.sqrt(Math.pow(event.values[0],2)+Math.pow(event.values[1],2)+Math.pow(event.values[2],2));
		
	}
	
	public void onCreate (){
		  super.onCreate();

	       mPlayer = MediaPlayer.create(this, songs[song_id]);
	       if(mPlayer!= null)
	        {
	        	mPlayer.setLooping(true);
	        	mPlayer.setVolume(100,100);
	        }


	        mPlayer.setOnErrorListener(new OnErrorListener() {

		  public boolean onError(MediaPlayer mp, int what, int
	          extra){

				onError(mPlayer, what, extra);
				return true;
			}
	    	  });
		}

		public void pauseMusic()
		{
			if(mPlayer.isPlaying())
			{
				mPlayer.pause();
				length=mPlayer.getCurrentPosition();

			}
		}

		public void resumeMusic()
		{
			if(mPlayer.isPlaying()==false)
			{
				mPlayer.seekTo(length);
				mPlayer.start();
			}
		}

		public void stopMusic()
		{
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}

		@Override
		public void onDestroy ()
		{
			super.onDestroy();
			if(mPlayer != null)
			{
			try{
			 mPlayer.stop();
			 mPlayer.release();
				}finally {
					mPlayer = null;
				}
			}
		}
} 