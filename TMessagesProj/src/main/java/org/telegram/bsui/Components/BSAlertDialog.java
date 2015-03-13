package org.telegram.bsui.Components;

import android.content.Context;
import android.graphics.Color;
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
        this.setBackgroundColor(android.R.color.transparent);
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

        public Builder(Context context){
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
            mContent.setBackground(mContext.getResources().getDrawable(R.drawable.bs_dialog_border));
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
            tw.setTextSize(AndroidUtilities.bsDp(6));
            tw.setTextColor(Color.parseColor("#000000"));
            tw.setGravity(Gravity.CENTER);
            tw.setBackground(mContext.getResources().getDrawable(R.drawable.bs_dialog_border));
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
            mTitle.setBackground(mContext.getResources().getDrawable(R.drawable.bs_dialog_border));
            mTitle.setTextColor(Color.parseColor("#000000"));
            mContent.addView(mTitle, 0);
            return this;
        }

        public Builder setItems(CharSequence[] items, AdapterView.OnItemClickListener listener){
            ListView listView = new ListView(mContext);
            listView.setBackgroundColor(0xffffff);
            listView.setPadding(5, 5, 5, 5);
            listView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(mContext, android.R.layout.simple_list_item_1, items);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(listener);
            mContent.addView(listView);
            return this;
        }

        public Builder setPositiveButton(String ok, OnClickListener listener) {
            if(mCurrentPositive != null) mYesNo.removeView(mCurrentPositive);
            mCurrentPositive = new Button(mContext);
            mCurrentPositive.setText(ok);
            mCurrentPositive.setTextColor(Color.parseColor("#000000"));
            mCurrentPositive.setPadding(5, 0, 5, 0);
            mCurrentPositive.setTextSize(10);
            mCurrentPositive.setBackground(mContext.getResources().getDrawable(R.drawable.bs_dialog_border));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.RIGHT;
            lp.weight = 1;
            mCurrentPositive.setLayoutParams(lp);
            mYesNo.addView(mCurrentPositive);
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
            mCurrentNegative.setTextColor(Color.parseColor("#000000"));
            mCurrentNegative.setPadding(5, 0, 5, 0);
            mCurrentNegative.setBackground(mContext.getResources().getDrawable(R.drawable.bs_dialog_border));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.RIGHT;
            lp.weight = 1;
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
