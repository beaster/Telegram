package org.telegram.bsui;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yotadevices.sdk.Drawer;
import com.yotadevices.sdk.NotificationBSActivity;
import com.yotadevices.sdk.SdkUtils;
import com.yotadevices.sdk.utils.EinkUtils;
import com.yotadevices.sdk.utils.RotationAlgorithm;

import org.telegram.messenger.R;
import org.telegram.ui.LaunchActivity;

import java.util.HashMap;

/**
 * Created by Ji on 08.01.2015.
 */
public class OtherFlipBSActivity extends NotificationBSActivity {

    public static final String SETTINGS_FLAG = "com.yotadevices.yotaphone2.telegram.bsui.OtherFlipBSActivity.SETTINGS_FLAG";
    public static final String VIEW_PROFILE = "com.yotadevices.yotaphone2.telegram.bsui.OtherFlipBSActivity.VIEW_PROFILE";
    public static final String VIEW_VIDEO = "com.yotadevices.yotaphone2.telegram.bsui.OtherFlipBSActivity.VIEW_VIDEO";
    public static final String ATTACH = "com.yotadevices.yotaphone2.telegram.bsui.OtherFlipBSActivity.ATTACH";
    public static final String VIEW_FILE = "com.yotadevices.yotaphone2.telegram.bsui.OtherFlipBSActivity.VIEW_FILE";
    public static final String SHARE = "com.yotadevices.yotaphone2.telegram.bsui.OtherFlipBSActivity.SHARE";
    public static final String GEO = "com.yotadevices.yotaphone2.telegram.bsui.OtherFlipBSActivity.GEO";
    public static final String USER_PROFILE = "com.yotadevices.yotaphone2.telegram.bsui.OtherFlipBSActivity.USER_PROFILE";
    private static final String EXTRA_PENDING = "pending";
    private static final String WHITE_THEME = "white_theme";

    private static boolean profile = false;
    private static boolean settings = false;
    private static boolean video = false;
    private static boolean attach = false;
    private static boolean share = false;
    private static boolean file = false;
    private static boolean geo = false;
    private static boolean userProfile = false;

    private TextView mTitle;
    private TextView mDescription;
    private LinearLayout mMainLayout;

    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            handleFinish();
        }
    };

    @Override
    protected void onBSDestroy() {
        super.onBSDestroy();
    }

    public static HashMap<String,Object> Params = new HashMap<>();

    public static void setProfileFlag(){
        setDisableFlags();
        Params.clear();
        profile = true;
    }
    public static void setSettingsFlag(){
        setDisableFlags();
        Params.clear();
        settings = true;
    }
    public static void setGeoFlag(){
        setDisableFlags();
        Params.clear();
        geo = true;
    }
    public static void setDisableFlags(){
        profile = settings = video = attach = share = file = geo = userProfile = false;
    }
    public static void setViewVideoFlag(){
        setDisableFlags();
        Params.clear();
        video = true;
    }
    public static void setAttachFlag(){
        setDisableFlags();
        Params.clear();
        attach = true;
    }
    public static void setShareFlag(){
        setDisableFlags();
        Params.clear();
        share = true;
    }
    public static void setFileFlag(){
        setDisableFlags();
        Params.clear();
        file = true;
    }
    public static void setUserProfileFlag() {
        setDisableFlags();
        Params.clear();
        userProfile = true;
    }

    @Override
    protected void onBSCreate() {
        super.onBSCreate();
        Intent i = new Intent(getContext(), LaunchActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(settings) {
            setDisableFlags();
            i.setAction(SETTINGS_FLAG);
        } else if(profile) {
            setDisableFlags();
            i.setAction(VIEW_PROFILE);
        } else if(video){
            setDisableFlags();
            i.setAction(VIEW_VIDEO);
        } else if(attach){
            setDisableFlags();
            i.setAction(ATTACH);
        } else if(share){
            setDisableFlags();
            i.setAction(SHARE);
        } else  if(file){
            setDisableFlags();
            i.setAction(VIEW_FILE);
        } else if(geo){
            setDisableFlags();
            i.setAction(GEO);
        } else if(userProfile){
            setDisableFlags();
            i.setAction(USER_PROFILE);
        }
        startActivity(i);
        handleIntent();
    }

    @Override
    protected void onBSResume() {
        super.onBSResume();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        getContext().registerReceiver(mScreenReceiver, filter);
    }

    @Override
    protected void onBSPause() {
        super.onBSPause();
        if (mScreenReceiver != null) {
            try {
                getContext().unregisterReceiver(mScreenReceiver);
            } catch (Exception unused) {
            }
            mScreenReceiver = null;
        }
    }

    private synchronized void handleFinish() {
        if (!isFinishing()) {
            finish();
        }
    }

    private void handleIntent() {
        Intent i = getIntent();
        if (i != null) {
            initLayout();

            PendingIntent pending = i.getParcelableExtra(EXTRA_PENDING);

            if (pending != null) {
                try {
                    pending.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        }

        RotationAlgorithm.getInstance(getApplicationContext()).turnScreenOffIfRotated(
                RotationAlgorithm.OPTION_START_WITH_BS | RotationAlgorithm.OPTION_NO_UNLOCK | RotationAlgorithm.OPTION_EXPECT_FIRST_ROTATION_FOR_60SEC,
                new RotationAlgorithm.OnPhoneRotatedListener() {
                    @Override
                    public void onRotataionCancelled() {

                    }

                    @Override
                    public void onPhoneRotatedToFS() {
                        handleFinish();
                    }

                    @Override
                    public void onPhoneRotatedToBS() {
                    }
                });
    }

    private void initLayout() {
        int titleId, descriptionId;
        int whiteThemeResId, blackThemeResId;
        int whiteOkBtn, blackOkBtn;

        setInitialDithering(Drawer.Dithering.DITHER_ATKINSON);

        if (SdkUtils.getSdkApiLevel() <= SdkUtils.SDK_API_LEVEL_2) {
            setBSContentView(com.yotadevices.sdk.R.layout.flip_layout);
            titleId = com.yotadevices.sdk.R.id.flip_popup_title;
            descriptionId = com.yotadevices.sdk.R.id.flip_popup_description;
            whiteThemeResId = R.drawable.bs_notification_bg_white;
            blackThemeResId = R.drawable.bs_notification_bg_black;
            whiteOkBtn = com.yotadevices.sdk.R.drawable.bs_ok_button_white;
            blackOkBtn = com.yotadevices.sdk.R.drawable.bs_ok_button;

            View parentView = getBSDrawer().getParentView();
            applyEinkFix(parentView);
        } else {
            setBSContentView(R.layout.temp_flip_other_layout);
            titleId = R.id.flip_popup_title;
            descriptionId = R.id.flip_popup_description;
            whiteThemeResId = R.drawable.temp_bs_flip_bg_white;
            blackThemeResId = R.drawable.temp_bs_flip_bg_black;
            whiteOkBtn = com.yotadevices.sdk.R.drawable.bs_ok_button;
            blackOkBtn = com.yotadevices.sdk.R.drawable.bs_ok_button;

            mMainLayout = (LinearLayout) findViewById(R.id.main_layout);
            applyEinkFix(mMainLayout);
        }


        mTitle = (TextView) findViewById(titleId);
        mDescription = (TextView) findViewById(descriptionId);
        mDescription.setMaxLines(3);
        mTitle.setText(com.yotadevices.sdk.R.string.flip_popup_default_title);


        // Themes for notifications support only SDK API Level 3
        if (SdkUtils.getSdkApiLevel() > SdkUtils.SDK_API_LEVEL_2)
            initTheme(whiteThemeResId, blackThemeResId, whiteOkBtn, blackOkBtn);
    }

    private void applyEinkFix(View view) {
        if (view == null)
            return;

        EinkUtils.setViewWaveform(view.getRootView(), Drawer.Waveform.WAVEFORM_GC_PARTIAL);
        EinkUtils.setViewDithering(view.getRootView(), Drawer.Dithering.DITHER_ATKINSON);
    }

    private void initTheme(int whiteThemeResId, int blackThemeResId, int whiteOkBtnRes, int blackOkBtnRes) {
        LinearLayout.LayoutParams curLayParams = (LinearLayout.LayoutParams) mDescription.getLayoutParams();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(12, curLayParams.topMargin, 12, curLayParams.bottomMargin);

        mDescription.setLayoutParams(lp);
        getBackgroundImageView().setScaleType(ImageView.ScaleType.FIT_XY);

        if (isWhiteThemeEnabled()) {
            mTitle.setTextColor(0xff000000);
            mDescription.setTextColor(0xff000000);
            setOKImageResource(whiteOkBtnRes);
            getBackgroundImageView().setImageResource(whiteThemeResId);
        } else {
            mTitle.setTextColor(0xffffffff);
            mDescription.setTextColor(0xffffffff);
            setOKImageResource(blackOkBtnRes);
            getBackgroundImageView().setImageResource(blackThemeResId);
        }
    }

    private boolean isWhiteThemeEnabled() {
        return Settings.Global.getInt(this.getContentResolver(), WHITE_THEME, 0) == 1;
    }
}
