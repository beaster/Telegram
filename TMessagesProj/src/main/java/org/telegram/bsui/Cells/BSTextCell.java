package org.telegram.bsui.Cells;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;

/**
 * Created by E1ektr0 on 09.01.2015.
 */
public class BSTextCell extends FrameLayout {

    private TextView textView;
    private TextView valueTextView;
    private ImageView imageView;
    private ImageView valueImageView;

    public BSTextCell(Context context) {
        super(context);

        textView = new TextView(context);
        textView.setTextColor(0xff212121);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);//16
        textView.setLines(1);
        textView.setMaxLines(1);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        addView(textView);
        LayoutParams layoutParams = (LayoutParams) textView.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.MATCH_PARENT;
        layoutParams.leftMargin = AndroidUtilities.bsDp(LocaleController.isRTL ? 16 : 71);
        layoutParams.rightMargin = AndroidUtilities.bsDp(LocaleController.isRTL ? 71 : 16);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        textView.setLayoutParams(layoutParams);

        valueTextView = new TextView(context);
        valueTextView.setTextColor(0xff2f8cc9);
        valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);//16
        valueTextView.setLines(1);
        valueTextView.setMaxLines(1);
        valueTextView.setSingleLine(true);
        valueTextView.setGravity((LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL);
        addView(valueTextView);
        layoutParams = (LayoutParams) valueTextView.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.MATCH_PARENT;
        layoutParams.leftMargin = AndroidUtilities.bsDp(LocaleController.isRTL ? 24 : 0);
        layoutParams.rightMargin = AndroidUtilities.bsDp(LocaleController.isRTL ? 0 : 24);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT;
        valueTextView.setLayoutParams(layoutParams);

        imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        addView(imageView);
        layoutParams = (LayoutParams) imageView.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.leftMargin = AndroidUtilities.bsDp(LocaleController.isRTL ? 0 : 16);
        layoutParams.rightMargin = AndroidUtilities.bsDp(LocaleController.isRTL ? 16 : 0);
        layoutParams.gravity = (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL;
        imageView.setLayoutParams(layoutParams);

        valueImageView = new ImageView(context);
        valueImageView.setScaleType(ImageView.ScaleType.CENTER);
        addView(valueImageView);
        layoutParams = (LayoutParams) valueImageView.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.leftMargin = AndroidUtilities.bsDp(LocaleController.isRTL ? 24 : 0);
        layoutParams.rightMargin = AndroidUtilities.bsDp(LocaleController.isRTL ? 0 : 24);
        layoutParams.gravity = (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL;
        valueImageView.setLayoutParams(layoutParams);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.bsDp(48), MeasureSpec.EXACTLY));
    }

    public void setTextColor(int color) {
        textView.setTextColor(color);
    }

    public void setText(String text) {
        textView.setText(text);
        imageView.setVisibility(GONE);
        valueTextView.setVisibility(GONE);
        valueImageView.setVisibility(GONE);
    }

    public void setTextAndIcon(String text, int resId) {
        textView.setText(text);
        imageView.setImageResource(resId);
        imageView.setVisibility(VISIBLE);
        valueTextView.setVisibility(GONE);
        valueImageView.setVisibility(GONE);
    }

    public void setTextAndValue(String text, String value) {
        textView.setText(text);
        valueTextView.setText(value);
        valueTextView.setVisibility(VISIBLE);
        imageView.setVisibility(GONE);
        valueImageView.setVisibility(GONE);
    }

    public void setTextAndValueDrawable(String text, Drawable drawable) {
        textView.setText(text);
        valueImageView.setVisibility(VISIBLE);
        valueImageView.setImageDrawable(drawable);
        valueTextView.setVisibility(GONE);
        imageView.setVisibility(GONE);
    }
}
