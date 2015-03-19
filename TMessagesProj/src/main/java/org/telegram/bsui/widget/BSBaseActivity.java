package org.telegram.bsui.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.yotadevices.sdk.BSActivity;
import com.yotadevices.sdk.Constants;

import org.telegram.android.AndroidUtilities;
import org.telegram.bsui.ActionBar.BSActionBar;
import org.telegram.messenger.R;

import java.util.Stack;

/**
 * Created by E1ektr0 on 09.01.2015.
 */
public class BSBaseActivity extends BSActivity{
    private View rootView;
    protected Bundle arguments;
    protected BSActionBar actionBar;
    private static Stack<BSActivity> stack = new Stack<>();

    @Override
    protected void onBSCreate() {
        stack.add(this);
        super.onBSCreate();
    }

    @Override
    protected void onBSDestroy() {
        super.onBSDestroy();
        finishFragment();
    }

    public void finishFragment() {
        stack.remove(this);

        finish();
    }

    protected <T extends BSBaseActivity> void presentFragment(Class<T> bsActivityClass, boolean removeFromHistory) {
        Intent intent = new Intent(getContext(), bsActivityClass);
        present(removeFromHistory, intent);
    }

    protected <T extends BSBaseActivity> void presentFragment(Class<T> bsActivityClass, Bundle args, boolean removeFromHistory) {
        stack.add(this);
        Intent intent = new Intent(getContext(), bsActivityClass);
        intent.putExtras(args);
        present(removeFromHistory, intent);
    }

    private void present(boolean removeFromHistory, Intent intent) {
        if(removeFromHistory)
            intent.putExtra(Constants.YOTAPHONE_EXTRA_FLAGS, Constants.YotaIntent.FLAG_BSACTIVITY_NO_HISTORY);
        startBSActivity(intent);
    }

    protected void IniActionBar()
    {
        LayoutInflater bsLayoutInflater = getBSDrawer().getBSLayoutInflater();
        rootView = bsLayoutInflater.inflate(R.layout.bs_actoin_bar, null);
        actionBar = (BSActionBar)rootView.findViewById(R.id.action_bar);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) actionBar.getLayoutParams();
        params.setMargins(0, AndroidUtilities.bsDp(-40), 0, 0);
        actionBar.setLayoutParams(params);
    }

    protected View createActionBar(View childView)
    {
        ((FrameLayout)rootView.findViewById(R.id.view_container)).addView(childView);
        return rootView;
    }

    protected void removeSelfFromStack(){

    }

    protected Context getParentActivity() {
        return getContext();
    }

    protected void setTitle(String str)
    {
        actionBar.setTitle(str);
    }
    protected void setSubtitle(String str)
    {
        actionBar.setSubtitle(str);
    }
}
