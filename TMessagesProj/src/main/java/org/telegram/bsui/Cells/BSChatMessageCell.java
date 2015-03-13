package org.telegram.bsui.Cells;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.ClickableSpan;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.MessageObject;
import org.telegram.bsui.BSMessageObject;
import org.telegram.bsui.ClickSpan;
import org.telegram.messenger.R;
import org.telegram.ui.Cells.ChatMessageCell;

/**
 * Created by E1ektr0 on 04.01.2015.
 */
public class BSChatMessageCell extends ChatMessageCell {

    private final Context context;

    private static Drawable backgroundDrawableIn;
    private static Drawable backgroundDrawableOut;
    private static Drawable backgroundDrawableInSelected;
    private static TextPaint timePaintIn;
    private static TextPaint timePaintOut;
    private static TextPaint timeMediaPaint;
    private static TextPaint namePaint;
    private static TextPaint forwardNamePaint;
    private static Drawable backgroundDrawableOutSelected;
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

    public BSChatMessageCell(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void initResources() {
        if(backgroundDrawableIn == null) {
            backgroundDrawableIn = getResources().getDrawable(R.drawable.msg_in_bs);
            backgroundDrawableOut = getResources().getDrawable(R.drawable.msg_out_bs);
            backgroundDrawableInSelected = getResources().getDrawable(R.drawable.msg_in_selected_bs);
            backgroundDrawableOutSelected = getResources().getDrawable(R.drawable.msg_out_selected_bs);

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

            timePaintIn = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaintIn.setTextSize(dp(12));
            timePaintIn.setColor(0xff000000);

            timePaintOut = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaintOut.setTextSize(dp(12));
            timePaintOut.setColor(0xff000000);

            timeMediaPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timeMediaPaint.setTextSize(dp(12));
            timeMediaPaint.setColor(0xffffffff);

            namePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            namePaint.setTextSize(dp(15));

            forwardNamePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            forwardNamePaint.setTextSize(dp(14));
        }
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
    public TextPaint getTimePaintIn() {
        return timePaintIn;
    }

    @Override
    public TextPaint getTimePaintOut() {
        return timePaintOut;
    }

    @Override
    public TextPaint getTimeMediaPaint() {
        return timeMediaPaint;
    }

    @Override
    public TextPaint getNamePaint() {
        return namePaint;
    }

    @Override
    public TextPaint getForwardNamePaint() {
        return forwardNamePaint;
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
    protected int getDisplayY() {
        return getDisplaySize().y;
    }

    @Override
    protected ClickableSpan[] getSpans(int off, Spannable buffer) {
        return buffer.getSpans(off, off, ClickSpan.class);
    }

    @Override
    public void setMessageObject(MessageObject messageObject) {
        super.setMessageObject(messageObject);
        if(messageObject instanceof BSMessageObject)
        {
            ((BSMessageObject)messageObject).setContext(context);
        }
    }

    @Override
    protected int getDisplayX() {
        return getDisplaySize().x;
    }

    private Point getDisplaySize() {
        return AndroidUtilities.bsDisplaySize;
    }

    @Override
    protected int dp(float value) {
        return AndroidUtilities.bsDp(value);
    }
}
