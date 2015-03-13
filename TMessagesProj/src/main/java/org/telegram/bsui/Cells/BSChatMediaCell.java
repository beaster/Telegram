package org.telegram.bsui.Cells;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import org.telegram.android.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.Cells.ChatMediaCell;

/**
 * Created by E1ektr0 on 04.01.2015.
 */
public class BSChatMediaCell extends ChatMediaCell {

    private static Drawable backgroundDrawableOutSelected;
    private static Drawable backgroundDrawableIn;
    private static Drawable backgroundDrawableInSelected;
    private static Drawable backgroundDrawableOut;
    private static TextPaint namePaint;
    private static Paint docBackPaint;

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

    private static TextPaint timePaintIn;
    private static TextPaint timePaintOut;
    private static Drawable placeholderDocInDrawable;
    private static Drawable placeholderDocOutDrawable;
    private static Drawable videoIconDrawable;
    private static Drawable docMenuInDrawable;
    private static Drawable docMenuOutDrawable;
    private static Drawable[] buttonStatesDrawables = new Drawable[8];
    private static Drawable[][] buttonStatesDrawablesDoc = new Drawable[3][2];
    private static TextPaint infoPaint;
    private static Paint deleteProgressPaint;


    public BSChatMediaCell(Context context) {
        super(context);
        initMedia();
    }

    @Override
    protected void initMedia() {
        if(placeholderDocInDrawable == null) {
            placeholderDocInDrawable = getResources().getDrawable(R.drawable.doc_blue);
            placeholderDocOutDrawable = getResources().getDrawable(R.drawable.doc_green);
            buttonStatesDrawables[0] = getResources().getDrawable(R.drawable.photoload);
            buttonStatesDrawables[1] = getResources().getDrawable(R.drawable.photocancel);
            buttonStatesDrawables[2] = getResources().getDrawable(R.drawable.photogif);
            buttonStatesDrawables[3] = getResources().getDrawable(R.drawable.playvideo);
            buttonStatesDrawables[4] = getResources().getDrawable(R.drawable.photopause);
            buttonStatesDrawables[5] = getResources().getDrawable(R.drawable.burn);
            buttonStatesDrawables[6] = getResources().getDrawable(R.drawable.circle);
            buttonStatesDrawables[7] = getResources().getDrawable(R.drawable.photocheck);
            buttonStatesDrawablesDoc[0][0] = getResources().getDrawable(R.drawable.docload_b);
            buttonStatesDrawablesDoc[1][0] = getResources().getDrawable(R.drawable.doccancel_b);
            buttonStatesDrawablesDoc[2][0] = getResources().getDrawable(R.drawable.docpause_b);
            buttonStatesDrawablesDoc[0][1] = getResources().getDrawable(R.drawable.docload_g);
            buttonStatesDrawablesDoc[1][1] = getResources().getDrawable(R.drawable.doccancel_g);
            buttonStatesDrawablesDoc[2][1] = getResources().getDrawable(R.drawable.docpause_g);
            videoIconDrawable = getResources().getDrawable(R.drawable.ic_video);

            backgroundDrawableIn = getResources().getDrawable(R.drawable.msg_in_bs);
            backgroundDrawableOut = getResources().getDrawable(R.drawable.msg_out_bs);
            backgroundDrawableOutSelected = getResources().getDrawable(R.drawable.msg_out_selected_bs);
            backgroundDrawableInSelected = getResources().getDrawable(R.drawable.msg_in_selected_bs);
            docMenuInDrawable = getResources().getDrawable(android.R.color.white);
            docMenuOutDrawable = getResources().getDrawable(android.R.color.white);

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

            infoPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);


            namePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            namePaint.setColor(0xff212121);


            docBackPaint = new Paint();

            timePaintIn = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);

            timePaintIn.setColor(0xffa1aab3);

            timePaintOut = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);

            timePaintOut.setColor(0xff70b15c);

            deleteProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            deleteProgressPaint.setColor(0xffe4e2e0);
        }
        infoPaint.setTextSize(dp(12));

        namePaint.setTextSize(dp(16));

        timePaintIn.setTextSize(dp(12));

        timePaintOut.setTextSize(dp(12));
    }

    @Override
    public Paint getDeleteProgressPaint() {
        return deleteProgressPaint;
    }

    @Override
    protected Drawable getPlaceholderDocInDrawable() {
        return placeholderDocInDrawable;
    }

    @Override
    protected Drawable getPlaceholderDocOutDrawable() {
        return placeholderDocOutDrawable;
    }

    @Override
    protected Drawable getVideoIconDrawable() {
        return videoIconDrawable;
    }

    @Override
    protected Drawable[] getButtonStatesDrawables() {
        return buttonStatesDrawables;
    }

    @Override
    protected Drawable[][] getButtonStatesDrawablesDoc() {
        return buttonStatesDrawablesDoc;
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
    protected Drawable getDocMenuOutDrawable() {
        return docMenuOutDrawable;
    }

    @Override
    protected Drawable getDocMenuInDrawable() {
        return docMenuInDrawable;
    }

    @Override
    protected Drawable getBackgroundDrawableIn() {
        return backgroundDrawableIn;
    }

    @Override
    protected Drawable getBackgroundDrawableInSelected() {
        return backgroundDrawableInSelected;
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
    protected TextPaint getInfoPaint() {
        return infoPaint;
    }

    @Override
    protected TextPaint getNamePaint() {
        return namePaint;
    }

    @Override
    protected Paint getDocBackPaint() {
        return docBackPaint;
    }

    @Override
    protected int dp(float value) {
        return AndroidUtilities.bsDp(value);
    }
    @Override
    protected int getDisplayY() {
        return getDisplaySize().y;
    }

    @Override
    protected int getDisplayX() {
        return getDisplaySize().x;
    }

    private Point getDisplaySize() {
        return AndroidUtilities.bsDisplaySize;
    }

    @Override
    protected float getDensity() {
        return AndroidUtilities.bsDensity;
    }


}
