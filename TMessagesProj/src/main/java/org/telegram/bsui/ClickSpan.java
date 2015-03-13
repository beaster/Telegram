package org.telegram.bsui;

import android.text.style.ClickableSpan;
import android.view.View;

/**
 * Created by Ji on 30.01.2015.
 */
public class ClickSpan extends ClickableSpan {

    private OnClickListener mListener;

    public ClickSpan(OnClickListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View widget) {
        if (mListener != null) mListener.onClick();
    }

    public interface OnClickListener {
        void onClick();
    }
}