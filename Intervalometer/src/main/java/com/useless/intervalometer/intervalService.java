package com.useless.intervalometer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


/*
private final iServiceInterface.Stub mBinder = new IServiceInterface.Stub() {
public int getPid(){
        return Process.myPid();
        }
public void basicTypes(int anInt, long aLong, boolean aBoolean,
        float aFloat, double aDouble, String aString) {
        // Does nothing
        }
        };
*/
public class intervalService extends Service {
    PowerManager pm;
    PowerManager.WakeLock wl;
    private List<Long> intervals;
    MediaPlayer mp;
    //Interval timing
    private long nextInterval = 0;
    private int current = 0;
    long millis;
    private long startTime = 0;

    public class LocalBinder extends Binder {
        intervalService getService() {
            // Return this instance of LocalService so clients can call public methods
            return intervalService.this;
        }
    }
    private final IBinder mBinder = new LocalBinder();

            @Override
    public void onCreate() {
        super.onCreate();
        // setup interval entries
        Log.i("intervalService", "onCreate()");

        pm = (PowerManager)getSystemService(POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"CollectData");
        mp =  MediaPlayer.create(this, R.raw.tardisesque);
        mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        wl.acquire();


    }
    @Override
    public int onStartCommand(Intent intent, int startId, int other) {
        //handleCommand(Intent);
        Log.i("intervalService", "onStartCommand()");
        return START_STICKY;
    }
    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
    }
    @Override
    public void onDestroy() {
        Log.i("intervalService", "onDestroy()");
        // TODO Auto-generated method stub
        super.onDestroy();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            millis = System.currentTimeMillis() - startTime;

            if (millis > nextInterval)
            {
                mp.start();
                current++;
                if (current >= intervals.size())
                    current = 0;
                Log.i("Intervalometer", "Interval Reached!");
                nextInterval += intervals.get(current);

            }

            timerHandler.postDelayed(this, 250);
        }
    };

   long getMillis() {
        return this.millis;
    }

   boolean start(List<Long> intervalTimes) {
        intervals = intervalTimes;
        startTime = System.currentTimeMillis();
        this.current = 0;
        if (intervals.size() > 0)
        {
            this.nextInterval = intervals.get(this.current);
            timerHandler.postDelayed(timerRunnable, 0);
            Log.i("intervalService", "started!");
            return true;
        }
       else
            Log.i("intervalService", "No intervals in list!");
       return false;
    }
    void stop() {
        Log.i("intervalService", "stopped!");
        timerHandler.removeCallbacks(timerRunnable);
    }

    int getCurrent() {
        return current;
    }
    List<Long> getIntervals() {
        if (intervals != null)
            return intervals;
        else
            return new ArrayList<Long>();
    }
}
