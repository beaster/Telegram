package org.telegram.bsui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import org.telegram.android.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;

/**
 * Created by E1ektr0 on 15.01.2015.
 */
public class BSTimerDrawable extends Drawable {

    private static Drawable emptyTimerDrawable;
    private static Drawable timerDrawable;
    private static TextPaint timePaint;
    private StaticLayout timeLayout;
    private float timeWidth = 0;
    private int timeHeight = 0;
    private int time = 0;

    public BSTimerDrawable(Context context) {
        if (emptyTimerDrawable == null) {
            emptyTimerDrawable = context.getResources().getDrawable(R.drawable.header_timer);
            timerDrawable = context.getResources().getDrawable(R.drawable.header_timer2);
            timePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            timePaint.setTextSize(AndroidUtilities.bsDp(11));
            timePaint.setColor(0xffffffff);
            timePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        }
    }

    public void setTime(int value) {
        time = value;

        String timeString = null;
        if (time >= 1 && time < 60) {
            timeString = "" + value;
            if (timeString.length() < 2) {
                timeString += "s";
            }
        } else if (time >= 60 && time < 60 * 60) {
            timeString = "" + value / 60;
            if (timeString.length() < 2) {
                timeString += "m";
            }
        } else if (time >= 60 * 60 && time < 60 * 60 * 24) {
            timeString = "" + value / 60 / 60;
            if (timeString.length() < 2) {
                timeString += "h";
            }
        } else if (time >= 60 * 60 * 24 && time < 60 * 60 * 24 * 7) {
            timeString = "" + value / 60 / 60 / 24;
            if (timeString.length() < 2) {
                timeString += "d";
            }
        } else {
            timeString = "" + value / 60 / 60 / 24 / 7;
            if (timeString.length() < 2) {
                timeString += "w";
            } else if (timeString.length() > 2) {
                timeString = "c";
            }
        }

        timeWidth = timePaint.measureText(timeString);
        try {
            timeLayout = new StaticLayout(timeString, timePaint, (int)Math.ceil(timeWidth), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            timeHeight = timeLayout.getHeight();
        } catch (Exception e) {
            timeLayout = null;
            FileLog.e("tmessages", e);
        }

        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        int width =35;// timerDrawable.getIntrinsicWidth();
        int height =35;// timerDrawable.getIntrinsicHeight();



        Drawable drawable = null;
        if (time == 0) {
            drawable = timerDrawable;
        } else {
            drawable = emptyTimerDrawable;
        }

        int x = (width - 0) / 2;
        int y = (height - 0) / 2;
        drawable.setBounds(x, y, x + 35, y + 35);
        drawable.draw(canvas);

        if (time != 0 && timeLayout != null) {
            int xOffxet = 0;
            if (AndroidUtilities.bsDensity == 3) {
                xOffxet = -1;
            }
            canvas.translate((int)(width / 2 - Math.ceil(timeWidth / 2)) + xOffxet, (height - timeHeight) / 2);
            timeLayout.draw(canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    public int getIntrinsicWidth() {
        return timerDrawable.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return timerDrawable.getIntrinsicHeight();
    }
}
