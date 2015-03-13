package org.telegram.bsui.Cells;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.Log;

import org.telegram.android.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.Cells.ChatAudioCell;

/**
 * Created by E1ektr0 on 04.01.2015.
 */
public class BSChatAudioCell extends ChatAudioCell {

    private static Drawable backgroundDrawableIn;
    private static Drawable backgroundDrawableOut;
    private static Drawable backgroundDrawableInSelected;
    private static Drawable backgroundDrawableOutSelected;
    private static TextPaint timePaint;
    private static TextPaint timePaintIn;
    private static TextPaint timePaintOut;

    private static Drawable checkDrawable;
    private static Drawable halfCheckDrawable;
    private static Drawable clockDrawable;
    private static Drawable broadcastDrawable;
    private static Drawable checkMediaDrawable;
    private static Drawable halfCheckMediaDrawable;
    private static Drawable clockMediaDrawable;
    private static Drawable broadcastMediaDrawable;
    private static Drawable errorDrawable;
    protected static Drawable mediaBackgroundDrawable;
    private static Drawable[][] statesDrawable = new Drawable[8][2];

    public BSChatAudioCell(Context context) {
        super(context);
        initAudio();
    }

    @Override
    protected void initAudio() {
        Log.d("AudioCell", "init");
        if (timePaint == null || statesDrawable[0][0] == null) {
            statesDrawable[0][0] = getResources().getDrawable(R.drawable.play1);
            statesDrawable[0][1] = getResources().getDrawable(R.drawable.play1_pressed);
            statesDrawable[1][0] = getResources().getDrawable(R.drawable.pause1);
            statesDrawable[1][1] = getResources().getDrawable(R.drawable.pause1_pressed);
            statesDrawable[2][0] = getResources().getDrawable(R.drawable.audioload1);
            statesDrawable[2][1] = getResources().getDrawable(R.drawable.audioload1_pressed);
            statesDrawable[3][0] = getResources().getDrawable(R.drawable.audiocancel1);
            statesDrawable[3][1] = getResources().getDrawable(R.drawable.audiocancel1_pressed);

            statesDrawable[4][0] = getResources().getDrawable(R.drawable.play2);
            statesDrawable[4][1] = getResources().getDrawable(R.drawable.play2_pressed);
            statesDrawable[5][0] = getResources().getDrawable(R.drawable.pause2);
            statesDrawable[5][1] = getResources().getDrawable(R.drawable.pause2_pressed);
            statesDrawable[6][0] = getResources().getDrawable(R.drawable.audioload2);
            statesDrawable[6][1] = getResources().getDrawable(R.drawable.audioload2_pressed);
            statesDrawable[7][0] = getResources().getDrawable(R.drawable.audiocancel2);
            statesDrawable[7][1] = getResources().getDrawable(R.drawable.audiocancel2_pressed);

            timePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaint.setTextSize(AndroidUtilities.bsDp(12));
        }
    }

    @Override
    protected void initResources() {
        if(timePaint == null) {
            backgroundDrawableIn = getResources().getDrawable(R.drawable.msg_in_bs);
            backgroundDrawableOut = getResources().getDrawable(R.drawable.msg_out_bs);
            backgroundDrawableOutSelected = getResources().getDrawable(R.drawable.msg_out_selected_bs);
            backgroundDrawableInSelected = getResources().getDrawable(R.drawable.msg_in_selected_bs);

            checkDrawable = getResources().getDrawable(R.drawable.msg_check);
            halfCheckDrawable = getResources().getDrawable(R.drawable.msg_halfcheck);
            clockDrawable = getResources().getDrawable(R.drawable.msg_clock);
            checkMediaDrawable = getResources().getDrawable(R.drawable.msg_check_w);
            halfCheckMediaDrawable = getResources().getDrawable(R.drawable.msg_halfcheck_w);
            clockMediaDrawable = getResources().getDrawable(R.drawable.msg_clock_photo);
            errorDrawable = getResources().getDrawable(R.drawable.msg_warning);
            mediaBackgroundDrawable = getResources().getDrawable(R.drawable.phototime);
            broadcastDrawable = getResources().getDrawable(R.drawable.broadcast3);
            broadcastMediaDrawable = getResources().getDrawable(R.drawable.broadcast4);

            timePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);

            timePaintIn = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaintIn.setColor(0xffa1aab3);

            timePaintOut = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaintOut.setColor(0xff70b15c);

            timePaint.setTextSize(dp(12));
            timePaintIn.setTextSize(dp(12));
            timePaintOut.setTextSize(dp(12));
        }
    }

    @Override
    protected Drawable[][] getStatesDrawable() {
        return statesDrawable;
    }

    @Override
    public TextPaint getTimePaintIn() {
        return timePaintIn;
    }

    @Override
    public TextPaint getTimePaintOut() {
        return timePaintOut;
    }

    @Override
    public Drawable getCheckDrawable() {
        return checkDrawable;
    }

    @Override
    public Drawable getHalfCheckDrawable() {
        return halfCheckDrawable;
    }

    @Override
    public Drawable getClockDrawable() {
        return clockDrawable;
    }

    @Override
    public Drawable getBroadcastDrawable() {
        return broadcastDrawable;
    }

    @Override
    public Drawable getErrorDrawable() {
        return errorDrawable;
    }

    @Override
    public Drawable getBroadcastMediaDrawable() {
        return broadcastMediaDrawable;
    }

    @Override
    public Drawable getClockMediaDrawable() {
        return clockMediaDrawable;
    }

    @Override
    public Drawable getCheckMediaDrawable() {
        return checkMediaDrawable;
    }

    @Override
    public Drawable getHalfCheckMediaDrawable() {
        return halfCheckMediaDrawable;
    }

    @Override
    public TextPaint getTimePaint() {
        return timePaint;
    }

    @Override
    public Drawable getBackgroundDrawableIn() {
        return backgroundDrawableIn;
    }

    @Override
    public Drawable getBackgroundDrawableInSelected() {
        return backgroundDrawableInSelected;
    }

    @Override
    public Drawable getBackgroundDrawableOutSelected() {
        return backgroundDrawableOutSelected;
    }

    @Override
    public Drawable getBackgroundDrawableOut() {
        return backgroundDrawableOut;
    }

    @Override
    protected int dp(float value) {
        return AndroidUtilities.bsDp(value);
    }
}
