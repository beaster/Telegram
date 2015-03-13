package org.telegram.bsui.Cells;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;

/**
 * Created by E1ektr0 on 09.01.2015.
 */
public class BSGreySectionCell extends FrameLayout {
    private TextView textView;

    private void init() {
        setBackgroundColor(0xfff2f2f2);

        textView = new TextView(getContext());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8);//14
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setTextColor(0xff8a8a8a);
        textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        addView(textView);
        LayoutParams layoutParams = (LayoutParams)textView.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.MATCH_PARENT;
        layoutParams.leftMargin = AndroidUtilities.bsDp(16);
        layoutParams.rightMargin = AndroidUtilities.bsDp(16);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        textView.setLayoutParams(layoutParams);
    }

    public BSGreySectionCell(Context context) {
        super(context);
        init();
    }

    public BSGreySectionCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BSGreySectionCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BSGreySectionCell(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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