/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.bsui.ActionBar;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.animation.DecelerateInterpolator;

import org.telegram.android.AndroidUtilities;

public class BSMenuDrawable extends Drawable {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean reverseAngle = false;
    private long lastFrameTime;
    private boolean animationInProgress;
    private float finalRotation;
    private float currentRotation;
    private int currentAnimationTime;
    private DecelerateInterpolator interpolator = new DecelerateInterpolator();

    public BSMenuDrawable() {
        super();
        paint.setColor(0xffffffff);
        paint.setStrokeWidth(AndroidUtilities.bsDp(2));
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    public void setRotation(float rotation) {
        lastFrameTime = 0;
        if (currentRotation == 1) {
            reverseAngle = true;
        } else if (currentRotation == 0) {
            reverseAngle = false;
        }
        lastFrameTime = 0;
        finalRotation = currentRotation = rotation;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if (currentRotation != finalRotation) {
            if (lastFrameTime != 0) {
                long dt = System.currentTimeMillis() - lastFrameTime;

                currentAnimationTime += dt;
                if (currentAnimationTime >= 300) {
                    currentRotation = finalRotation;
                } else {
                    if (currentRotation < finalRotation) {
                        currentRotation = interpolator.getInterpolation(currentAnimationTime / 300.0f) * finalRotation;
                    } else {
                        currentRotation = 1.0f - interpolator.getInterpolation(currentAnimationTime / 300.0f);
                    }
                }
            }
            lastFrameTime = System.currentTimeMillis();
            invalidateSelf();
        }

        canvas.save();
        canvas.translate(getIntrinsicWidth() / 2, getIntrinsicHeight() / 2);
        canvas.rotate(currentRotation * (reverseAngle ? -180 : 180));
        canvas.drawLine(-AndroidUtilities.bsDp(9), 0, AndroidUtilities.bsDp(9) - AndroidUtilities.bsDp(1) * currentRotation, 0, paint);
        float endYDiff = AndroidUtilities.bsDp(5) * (1 - Math.abs(currentRotation)) - AndroidUtilities.bsDp(0.5f) * Math.abs(currentRotation);
        float endXDiff = AndroidUtilities.bsDp(9) - AndroidUtilities.bsDp(0.5f) *  Math.abs(currentRotation);
        float startYDiff = AndroidUtilities.bsDp(5) + AndroidUtilities.bsDp(3.5f) * Math.abs(currentRotation);
        float startXDiff = -AndroidUtilities.bsDp(9) + AndroidUtilities.bsDp(8.5f) * Math.abs(currentRotation);
        canvas.drawLine(startXDiff, -startYDiff, endXDiff, -endYDiff, paint);
        canvas.drawLine(startXDiff, startYDiff, endXDiff, endYDiff, paint);
        canvas.restore();
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
        return AndroidUtilities.bsDp(24);
    }

    @Override
    public int getIntrinsicHeight() {
        return AndroidUtilities.bsDp(24);
    }
}
