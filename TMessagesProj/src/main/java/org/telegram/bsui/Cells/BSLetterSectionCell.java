package org.telegram.bsui.Cells;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.android.AndroidUtilities;

/**
 * Created by E1ektr0 on 09.01.2015.
 */
public class BSLetterSectionCell extends FrameLayout {

    private TextView textView;

    public BSLetterSectionCell(Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(AndroidUtilities.bsDp(54), AndroidUtilities.bsDp(64)));

        textView = new TextView(getContext());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);//22
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setTextColor(0xff808080);
        textView.setGravity(Gravity.CENTER);
        addView(textView);
        LayoutParams layoutParams = (LayoutParams)textView.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.MATCH_PARENT;
        textView.setLayoutParams(layoutParams);
    }

    public void setLetter(String letter) {
        textView.setText(letter.toUpperCase());
    }

    public void setCellHeight(int height) {
        setLayoutParams(new ViewGroup.LayoutParams(AndroidUtilities.bsDp(54), height));
    }
}
