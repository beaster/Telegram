package org.telegram.bsui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.FrameLayout;


import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.IntroActivity;
import org.telegram.ui.LaunchActivity;

import java.util.Map;

/**
 * Created by fanticqq on 04.02.15.
 */
public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationLoader.postInitApplication();
        if (!UserConfig.isClientActivated()) {
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("logininfo", MODE_PRIVATE);
            Map<String, ?> state = preferences.getAll();
            if (state.isEmpty()) {
                Intent intent = new Intent(getApplicationContext(), IntroActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            Intent intent = new Intent(getApplicationContext(), LaunchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(OtherFlipBSActivity.SETTINGS_FLAG);
            startActivity(intent);
            finish();
        }
    }
}
