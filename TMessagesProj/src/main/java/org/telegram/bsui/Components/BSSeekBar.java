package org.telegram.bsui.Components;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import org.telegram.android.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.Components.SeekBar;

/**
 * Created by fanticqq on 16.03.15.
 */
public class BSSeekBar extends SeekBar {

    private static Drawable thumbDrawable1;
    private static Drawable thumbDrawablePressed1;
    private static Drawable thumbDrawable2;
    private static Drawable thumbDrawablePressed2;
    private static Paint innerPaint1 = new Paint();
    private static Paint outerPaint1 = new Paint();
    private static Paint innerPaint2 = new Paint();
    private static Paint outerPaint2 = new Paint();
    private static int thumbWidth;
    private static int thumbHeight;

    public BSSeekBar(Context context) {
        super(context);
    }

    @Override
    protected void initThumbs(Context context) {
        if (thumbDrawable1 == null) {
            thumbDrawable1 = context.getResources().getDrawable(R.drawable.player1);
            thumbDrawablePressed1 = context.getResources().getDrawable(R.drawable.player1_pressed);
            thumbDrawable2 = context.getResources().getDrawable(R.drawable.player2);
            thumbDrawablePressed2 = context.getResources().getDrawable(R.drawable.player2_pressed);
            innerPaint1.setColor(0xffb4e396);
            outerPaint1.setColor(0xff6ac453);
            innerPaint2.setColor(0xffd9e2eb);
            outerPaint2.setColor(0xff86c5f8);
            thumbWidth = getThumbDrawable1().getIntrinsicWidth();
            thumbHeight = getThumbDrawable1().getIntrinsicHeight();
        }
    }

    @Override
    protected int getThumbWidth() {
        return thumbWidth;
    }

    @Override
    protected int getThumbHeight() {
        return thumbHeight;
    }

    @Override
    protected Drawable getThumbDrawable1() {
        return thumbDrawable1;
    }

    @Override
    protected Drawable getThumbDrawablePressed1() {
        return thumbDrawablePressed1;
    }

    @Override
    protected Drawable getThumbDrawable2() {
        return thumbDrawable2;
    }

    @Override
    protected Drawable getThumbDrawablePressed2() {
        return thumbDrawablePressed2;
    }

    @Override
    protected Paint getInnerPaint1() {
        return innerPaint1;
    }

    @Override
    protected Paint getOuterPaint1() {
        return outerPaint1;
    }

    @Override
    protected Paint getInnerPaint2() {
        return innerPaint2;
    }

    @Override
    protected Paint getOuterPaint2() {
        return outerPaint2;
    }
}
