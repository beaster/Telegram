package org.telegram.bsui.Components;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.yotadevices.sdk.BSActivity;

import org.telegram.android.AndroidUtilities;
import org.telegram.messenger.R;

/**
 * Created by Ji on 20.01.2015.
 */
public class BSAlertDialog extends FrameLayout {

    public BSAlertDialog(final Context context) {
        super(context);
        this.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.setBackgroundColor(0xffffff00);
        this.getBackground().setAlpha(200);
    }

    public static class Builder {

        private BSAlertDialog mDialog;
        private LinearLayout mMessageBox;
        private TableLayout mContent;
        private Context mContext;
        private Button mCurrentPositive;
        private Button mCurrentNegative;
        private LinearLayout mYesNo;
        private TextView mTitle;
        private static Drawable background;
        private static Drawable dotsVertical;

        public Builder(Context context){
            if(background == null){
                background = context.getResources().getDrawable(R.drawable.bs_dialog_border);
                dotsVertical = context.getResources().getDrawable(R.drawable.dots_vertical);
            }
            mContext = context;
            mDialog = new BSAlertDialog(context);
            mContent = new TableLayout(context);
            mMessageBox = new LinearLayout(context);
            mMessageBox.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mYesNo = new LinearLayout(context);
            mYesNo.setOrientation(LinearLayout.HORIZONTAL);
            mYesNo.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(AndroidUtilities.bsDp(20), 0, AndroidUtilities.bsDp(20), 0);
            params.gravity = Gravity.CENTER;
            mContent.setLayoutParams(params);
            mContent.setBackground(background);
            mContent.addView(mYesNo);
            mDialog.addView(mContent);
        }

        public Builder setView(View view){
            mContent.addView(view);
            return this;
        }

        public Builder setMessage(String message){
            TextView tw = new TextView(mContext);
            tw.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tw.setText(message);
            tw.setTextSize(AndroidUtilities.bsDp(8));
            tw.setTextColor(0xff000000);
            tw.setGravity(Gravity.CENTER);
            tw.setPadding(AndroidUtilities.bsDp(10), AndroidUtilities.bsDp(2) , AndroidUtilities.bsDp(10), AndroidUtilities.bsDp(2));
            if(mTitle != null) {
                mContent.addView(tw, 1);
            } else {
                mContent.addView(tw, 0);
            }
            return this;
        }

        public Builder setTitle(String title){
            mTitle = new TextView(mContext);
            mTitle.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            mTitle.setGravity(Gravity.CENTER);
            mTitle.setText(title);
            mTitle.setPadding(10, 5, 10, 5);
            mTitle.setTextSize(AndroidUtilities.bsDp(10));
            mTitle.setTextColor(0xff000000);
            mTitle.setTypeface(Typeface.DEFAULT_BOLD);
            mContent.addView(mTitle, 0);
            return this;
        }

        public Builder setItems(CharSequence[] items, AdapterView.OnItemClickListener listener){
            ListView listView = new ListView(mContext);
            listView.setBackgroundColor(0xffffff);
            listView.setPadding(5, 5, 5, 5);
            listView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(mContext, R.layout.bs_dialog_item, items);
            listView.setDivider(mContext.getResources().getDrawable(R.drawable.dots));
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(listener);
            mContent.addView(listView);
            return this;
        }

        public Builder setPositiveButton(String ok, OnClickListener listener) {
            if(mCurrentPositive != null) mYesNo.removeView(mCurrentPositive);
            mCurrentPositive = new Button(mContext);
            mCurrentPositive.setText(ok);
            mCurrentPositive.setTextColor(0xff000000);
            mCurrentPositive.setBackgroundColor(0xffffffff);
            mCurrentPositive.setPadding(5, 0, 5, 0);
            mCurrentPositive.setTextSize(10);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.RIGHT;
            lp.weight = 1;
            lp.setMargins(5, 5, 5, 5);
            mCurrentPositive.setLayoutParams(lp);
            mYesNo.addView(mCurrentPositive);
            View separator = new View(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(AndroidUtilities.bsDp(6), AndroidUtilities.bsDp(50));
            params.gravity = Gravity.CENTER_VERTICAL;
            separator.setLayoutParams(params);
            separator.setBackground(dotsVertical);
            mYesNo.addView(separator);
            if(listener == null){
                mCurrentPositive.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        close();
                    }
                });
                return this;
            } else {
                mCurrentPositive.setOnClickListener(listener);
            }
            return this;
        }

        public Builder setNegativeButton(String cancel, OnClickListener listener) {
            if(mCurrentNegative != null) mYesNo.removeView(mCurrentNegative);
            mCurrentNegative = new Button(mContext);
            mCurrentNegative.setText(cancel);
            mCurrentNegative.setTextColor(0xff000000);
            mCurrentNegative.setPadding(5, 0, 5, 0);
            mCurrentNegative.setBackgroundColor(0xffffffff);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.RIGHT;
            lp.weight = 1;
            lp.setMargins(5, 5, 5, 5);
            mCurrentNegative.setLayoutParams(lp);
            mCurrentNegative.setTextSize(10);
            mYesNo.addView(mCurrentNegative);
            if(listener == null){
                mCurrentNegative.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        close();
                    }
                });
                return this;
            } else {
                mCurrentNegative.setOnClickListener(listener);
            }
            return this;
        }

        public void close() {
            ((BSActivity)mContext).getBSDrawer().removeViewFromBS(mDialog);
        }

        public BSAlertDialog show(){
            mDialog.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    close();
                }
            });
            ((BSActivity)mContext).getBSDrawer().addViewToBS(mDialog);
            return mDialog;
        }
    }
}
