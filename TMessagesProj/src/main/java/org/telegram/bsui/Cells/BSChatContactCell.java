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

    public BSChatContactCell(Context context) {
        super(context);
    }

    @Override
    protected void initResources() {

    }

    @Override
    protected void initNamePaint() {
        if (namePaint == null) {
            namePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            namePaint.setTextSize(dp(15));

            phonePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            phonePaint.setTextSize(dp(15));
            phonePaint.setColor(0xff000000);

            backgroundDrawableIn = getResources().getDrawable(R.drawable.msg_in_bs);
            backgroundDrawableOut = getResources().getDrawable(R.drawable.msg_out_bs);
            backgroundDrawableInSelected = getResources().getDrawable(R.drawable.msg_in_selected_bs);
            backgroundDrawableOutSelected = getResources().getDrawable(R.drawable.msg_out_selected_bs);

            addContactDrawableIn = getResources().getDrawable(R.drawable.addcontact_black);
            addContactDrawableOut = getResources().getDrawable(R.drawable.addcontact_black);
        }
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

    @Override
    protected Drawable getBackgroundDrawableIn() {
        return backgroundDrawableIn;
    }

    @Override
    protected Drawable getBackgroundDrawableOut() {
        return backgroundDrawableOut;
    }

    @Override
    protected Drawable getBackgroundDrawableOutSelected() {
        return backgroundDrawableOutSelected;
    }

    @Override
    protected Drawable getBackgroundDrawableInSelected() {
        return backgroundDrawableInSelected;
    }
}
