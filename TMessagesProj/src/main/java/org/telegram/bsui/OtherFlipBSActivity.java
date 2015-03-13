package org.telegram.bsui;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.yotadevices.sdk.NotificationBSActivity;
import com.yotadevices.sdk.utils.RotationAlgorithm;

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
    public final static String EXTRA_PENDING = "pending";

    private static boolean profile = false;
    private static boolean settings = false;
    private static boolean video = false;
    private static boolean attach = false;
    private static boolean share = false;
    private static boolean file = false;
    private static boolean geo = false;
    private static boolean userProfile = false;

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
            PendingIntent pending = i.getParcelableExtra(EXTRA_PENDING);
            setBSContentView(com.yotadevices.sdk.R.layout.flip_layout);
            getResources().getString(com.yotadevices.sdk.R.string.flip_popup_default_title);

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
}
