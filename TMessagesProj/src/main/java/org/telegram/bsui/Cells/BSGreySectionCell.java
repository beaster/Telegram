package org.telegram.bsui.Cells;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;

/**
 * Created by E1ektr0 on 09.01.2015.
 */
public class BSGreySectionCell extends LinearLayout {
    private TextView textView;

    private void init() {
        setOrientation(HORIZONTAL);
        View line = new View(getContext());
        line.setBackgroundColor(0xff000000);
        addView(line);
        LinearLayout.LayoutParams layoutParams = (LayoutParams) line.getLayoutParams();
        layoutParams.height =  AndroidUtilities.bsDp(2);
        layoutParams.weight = 2;
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        line.setLayoutParams(layoutParams);
        textView = new TextView(getContext());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setTextColor(0xff000000);
        addView(textView);
        layoutParams = (LayoutParams)textView.getLayoutParams();
        layoutParams.height = LayoutParams.MATCH_PARENT;
        layoutParams.weight = 1;
        layoutParams.gravity = Gravity.CENTER;
        textView.setLayoutParams(layoutParams);
        View line1 = new View(getContext());
        line1.setBackgroundColor(0xff000000);
        addView(line1);
        layoutParams = (LayoutParams) line1.getLayoutParams();
        layoutParams.height =  AndroidUtilities.bsDp(2);
        layoutParams.weight = 2;
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        line1.setLayoutParams(layoutParams);
    }

    public BSGreySectionCell(Context context) {
        super(context);
        init();
    }

    public BSGreySectionCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.bsDp(36), MeasureSpec.EXACTLY));
    }

    public void setText(String text) {
        textView.setText(text);
    }
}