package com.useless.intervalometer;

import android.content.Context;
import android.support.v7.appcompat.*;
import android.support.v7.appcompat.R;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;

class EditNumber extends EditText {

    public EditNumber(Context context) {
        super(context);
        this.setText("00");
        this.setBackgroundColor(0x80808080);
        this.setTextSize(48);
        this.setInputType(InputType.TYPE_CLASS_NUMBER);

    }
}
public class IntervalEntry extends LinearLayout {

    private EditNumber minute;
    private EditNumber second;
    static int instanceNumber = 0;
    private int bgColor;
    public IntervalEntry(Context context) {
        super(context);

        this.setBackgroundColor(0x40000000);
        Log.i("IntervalEntry", "Creating EditTexts...");
        this.minute = new EditNumber(context);

        this.second = new EditNumber(context);
        Log.i("IntervalEntry", "Adding views...");
        this.setGravity(Gravity.CENTER_HORIZONTAL);

        this.addView(this.minute);
        this.addView(this.second);
        this.bgColor = this.getDrawingCacheBackgroundColor();
        Log.i("IntervalEntry", "IntervalEntry Created!!!");
    }

    public long getInterval() {
        int min, sec;
        String tmp;

        tmp = this.minute.getText().toString();
        min = Integer.parseInt(tmp);
        tmp = this.second.getText().toString();
        sec = Integer.parseInt(tmp);
        return (min * 60 + sec) * 1000;
    }

    public void setInterval(long msec) {
        long sec = msec / 1000;
        this.minute.setText(String.format("%02d", (sec / 60)));
        this.second.setText(String.format("%02d", (sec % 60)));
    }

    public void mark() {
        this.setBackgroundColor(0x80ffffff);
    }

    public void unmark()
    {
        this.setBackgroundColor(this.bgColor);
    }
}
