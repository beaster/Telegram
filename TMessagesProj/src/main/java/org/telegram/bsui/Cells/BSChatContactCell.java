package org.telegram.bsui.Cells;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import org.telegram.android.AndroidUtilities;
import org.telegram.bsui.BSAvatarDrawable;
import org.telegram.messenger.R;
import org.telegram.ui.Cells.ChatContactCell;
import org.telegram.ui.Components.AvatarDrawable;

/**
 * Created by E1ektr0 on 04.01.2015.
 */
public class BSChatContactCell extends ChatContactCell {

    private static TextPaint namePaint;
    private static TextPaint phonePaint;
    private static Drawable addContactDrawableIn;
    private static Drawable addContactDrawableOut;

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

    public BSChatContactCell(Context context) {
        super(context);
        initNamePaint();
    }

    @Override
    protected TextPaint getNamePaint() {
        return namePaint;
    }

    @Override
    protected TextPaint getPhonePaint() {
        return phonePaint;
    }

    @Override
    protected Drawable getAddContactDrawableIn() {
        return addContactDrawableIn;
    }

    @Override
    protected Drawable getAddContactDrawableOut() {
        return addContactDrawableOut;
    }

    @Override
    protected TextPaint getTimePaintIn() {
        return timePaintIn;
    }

    @Override
    protected TextPaint getTimePaintOut() {
        return timePaintOut;
    }

    @Override
    protected Drawable getCheckDrawable() {
        return checkDrawable;
    }

    @Override
    protected Drawable getHalfCheckDrawable() {
        return halfCheckDrawable;
    }

    @Override
    protected Drawable getClockDrawable() {
        return clockDrawable;
    }

    @Override
    protected Drawable getBroadcastDrawable() {
        return broadcastDrawable;
    }

    @Override
    protected Drawable getErrorDrawable() {
        return errorDrawable;
    }

    @Override
    protected Drawable getBroadcastMediaDrawable() {
        return broadcastMediaDrawable;
    }

    @Override
    protected Drawable getClockMediaDrawable() {
        return clockMediaDrawable;
    }

    @Override
    protected Drawable getCheckMediaDrawable() {
        return checkMediaDrawable;
    }

    @Override
    protected Drawable getHalfCheckMediaDrawable() {
        return halfCheckMediaDrawable;
    }

    @Override
    protected Drawable getBackgroundDrawableOutSelected() {
        return backgroundDrawableOutSelected;
    }

    @Override
    protected Drawable getBackgroundDrawableOut() {
        return backgroundDrawableOut;
    }

    @Override
    protected Drawable getBackgroundDrawableInSelected() {
        return backgroundDrawableInSelected;
    }

    @Override
    protected Drawable getBackgroundDrawableIn() {
        return backgroundDrawableIn;
    }



    @Override
    protected void initNamePaint() {
        if (namePaint == null) {
            namePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            namePaint.setTextSize(dp(15));

            phonePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            phonePaint.setTextSize(dp(15));
            phonePaint.setColor(0xff212121);

            addContactDrawableIn = getResources().getDrawable(R.drawable.addcontact_black);
            addContactDrawableOut = getResources().getDrawable(R.drawable.addcontact_black);
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
    protected int dp(float value) {
        return AndroidUtilities.bsDp(value);
    }

    @Override
    protected float getDensity() {
        return AndroidUtilities.bsDensity;
    }

    @Override
    protected int getDisplayY() {
        return AndroidUtilities.bsDisplaySize.y;
    }

    @Override
    protected int getDisplayX() {
        return AndroidUtilities.bsDisplaySize.x;
    }
}
