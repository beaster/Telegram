package org.telegram.bsui.Components;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

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

    public BSSeekBar(Context context) {
        super(context);
    }


}
