package org.telegram.bsui.Cells;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.MessagesController;
import org.telegram.messenger.R;
import org.telegram.ui.Cells.ChatActionCell;

/**
 * Created by E1ektr0 on 04.01.2015.
 */
public class BSChatActionCell extends ChatActionCell {

    private static Drawable backgroundBlack;
    private static Drawable backgroundBlue;
    private static TextPaint textPaint;

    public BSChatActionCell(Context context) {
        super(context);
    }

    @Override
    protected void initBackground() {
        if (backgroundBlack == null) {
            backgroundBlack = getResources().getDrawable(R.drawable.system_blue_bs);
            backgroundBlue = getResources().getDrawable(R.drawable.system_blue_bs);

            textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(0xffffffff);
            textPaint.linkColor = 0xffffffff;
            textPaint.setTextSize(dp(MessagesController.getInstance().fontSize));
        }
    }

    @Override
    public Drawable getBackgroundBlack() {
        return backgroundBlack;
    }

    @Override
    public Drawable getBackgroundBlue() {
        return backgroundBlue;
    }

    @Override
    public TextPaint getTextPaint() {
        return textPaint;
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
