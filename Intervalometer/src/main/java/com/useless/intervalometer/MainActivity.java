package com.useless.intervalometer;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

import android.media.MediaPlayer;

public class MainActivity extends ActionBarActivity {

    protected boolean isStarted;

    private TextView timerTextView;
    private Button b;
    private List<IntervalEntry> intervals;

    intervalService iService;
    boolean iBound;
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i("intervalometer", "service Connected!");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            intervalService.LocalBinder binder = (intervalService.LocalBinder) service;
            iService = binder.getService();
            iBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            iBound = false;
            Log.i("intervalometer", "service Disconnected!");
        }
    };

    int lastCurrent;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            long millis = iService.getMillis();
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            int hundredths = (int) (millis / 10) % 100;
            seconds = seconds % 60;

            timerTextView.setText(String.format("%d:%02d:%02d", minutes, seconds, hundredths));
            int current = iService.getCurrent();
            if (current != lastCurrent) {

                int last = current - 1;
                if (last < 0)
                    last = intervals.size() - 1;
                intervals.get(current).mark();
                intervals.get(last).unmark();
            }
            timerHandler.postDelayed(this, 250);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LinearLayout l;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.isStarted = false;
        this.intervals = new ArrayList<IntervalEntry>();

        this.timerTextView = (TextView) findViewById(R.id.status);
        this.b =  (Button) findViewById(R.id.startBtn);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        l = (LinearLayout) findViewById(R.id.MainLayout);
        Intent i = new Intent(getBaseContext(), intervalService.class);
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void start(View v)
    {
        if (this.isStarted) {
            iService.stop();
            this.isStarted = false;
            b.setText("Start");
            timerHandler.removeCallbacks(timerRunnable);
            // Unmark any marked field
            for (int i = 0; i < intervals.size(); i++)
                intervals.get(i).unmark();
        }
        else {
            List<Long> intervalTimeList = new ArrayList<Long>(intervals.size());
            for (int i = 0; i < intervals.size(); i++)
                intervalTimeList.add(i,intervals.get(i).getInterval());
            if (iService.start(intervalTimeList))
            {
                intervals.get(0).mark();
                lastCurrent = 0;
                this.isStarted = true;

                b.setText("Stop");
                timerHandler.postDelayed(timerRunnable, 0);
            }
        }

    }

    public void addInterval(View v)
    {
        LinearLayout l;
        IntervalEntry i;

        l = (LinearLayout) findViewById(R.id.MainLayout);

        i = new IntervalEntry(l.getContext());
        intervals.add(i);
        l.addView(i);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i("intervalometer", "saving Instance State");
        outState.putBoolean("started", isStarted);
        timerHandler.removeCallbacks(timerRunnable);

        long[] times = new long[intervals.size()];
        for (int i = 0; i < intervals.size(); i++)
            times[i] = (this.intervals.get(i).getInterval());
        outState.putLongArray("intervals", times);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        long[] times = inState.getLongArray("intervals");
        LinearLayout l = (LinearLayout) findViewById(R.id.MainLayout);
        super.onRestoreInstanceState(inState);
        for (int i = 0; i < times.length; i++)
        {
            IntervalEntry ie = new IntervalEntry(l.getContext());
            ie.setInterval(times[i]);
            l.addView(ie);
            intervals.add(ie);
        }

        isStarted = inState.getBoolean("started");
        Log.i("intervalometer", "restoring Instance State");

        if (isStarted) {
            timerHandler.postDelayed(timerRunnable, 0);
            b.setText("Stop");
        }


    }
        @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}