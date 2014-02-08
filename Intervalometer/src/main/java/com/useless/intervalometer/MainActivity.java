package com.useless.intervalometer;

import android.os.Bundle;
import android.os.Handler;
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


    MediaPlayer mp;
    //Interval timing
    private long nextInterval = 0;
    private int current = 0;
    private long startTime = 0;
    long millis;
    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            millis = System.currentTimeMillis() - startTime;

            if (millis > nextInterval)
            {
                int last = current - 1;
                if (last < 0)
                    last = intervals.size() - 1;
                Log.i("Intervalometer", "unmarking " + last);
                intervals.get(last).unmark();
                Log.i("Intervalometer", "marking " + current);
                intervals.get(current).mark();

                mp.start();
                Log.i("Intervalometer", "Interval Reached!");
                nextInterval += intervals.get(current++).getInterval();
                if (current >= intervals.size())
                    current = 0;

            }
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            int hundredths = (int) (millis / 10) % 100;
            seconds = seconds % 60;

            timerTextView.setText(String.format("%d:%02d:%02d", minutes, seconds, hundredths));

            timerHandler.postDelayed(this, 250);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.isStarted = false;
        this.timerTextView = (TextView) findViewById(R.id.status);
        this.b =  (Button) findViewById(R.id.startBtn);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        // setup interval entries
        this.intervals = new ArrayList<IntervalEntry>();

        this.mp =  MediaPlayer.create(this, R.raw.tardisesque);
    }

    public void start(View v)
    {

        if (this.isStarted) {
            this.isStarted = false;
            timerHandler.removeCallbacks(timerRunnable);
            b.setText("Start");
        }
        else {
            this.isStarted = true;
            startTime = System.currentTimeMillis();
            this.current = 0;
            intervals.get(this.current).mark();
            this.nextInterval = intervals.get(this.current++).getInterval();
            timerHandler.postDelayed(timerRunnable, 0);
            b.setText("Stop");
        }
    }

    public void addInterval(View v)
    {
        LinearLayout l;
        IntervalEntry i;

        l = (LinearLayout) findViewById(R.id.MainLayout);

        i = new IntervalEntry(l.getContext());
        this.intervals.add(i);
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
        int i;
        super.onSaveInstanceState(outState);

        long[] times = new long[intervals.size()];
        for (i = 0; i < intervals.size(); i++)
            times[i] = (this.intervals.get(i).getInterval());
        outState.putLongArray("intervals", times);

        outState.putBoolean("started", isStarted);
        outState.putLong("timer", millis);
        outState.putLong("timerStart", startTime);
        outState.putLong("timerNext", nextInterval);
        outState.putInt("currentInterval", current);
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        LinearLayout l = (LinearLayout) findViewById(R.id.MainLayout);
        int i;
        long[] times = inState.getLongArray("intervals");
        super.onRestoreInstanceState(inState);
        isStarted = inState.getBoolean("started");
        startTime = inState.getLong("timerStart");
        millis = inState.getLong("timer");
        nextInterval = inState.getLong("timerNext");
        current = inState.getInt("currentInterval");
        if (current >= intervals.size())
            current = 0;
        for (i = 0; i < times.length; i++)
        {
            IntervalEntry ie = new IntervalEntry(l.getContext());
            ie.setInterval(times[i]);
            l.addView(ie);
            intervals.add(ie);
        }
        if (isStarted) {
            timerHandler.postDelayed(timerRunnable, 0);
            b.setText("Stop");
            intervals.get(current).mark();
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