package org.telegram.bsui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import org.telegram.android.AndroidUtilities;
import org.telegram.ui.Cells.BaseCell;

/**
 * Created by E1ektr0 on 09.01.2015.
 */
public class BSDividerCell extends BSBaseCell {

    private static Paint paint;

    public BSDividerCell(Context context) {
        super(context);
        if (paint == null) {
            paint = new Paint();
            paint.setColor(0xffd9d9d9);
            paint.setStrokeWidth(1);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.bsDp(16) + 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(getPaddingLeft(), AndroidUtilities.bsDp(8), getWidth() - getPaddingRight(), AndroidUtilities.bsDp(8), paint);
    }
}
