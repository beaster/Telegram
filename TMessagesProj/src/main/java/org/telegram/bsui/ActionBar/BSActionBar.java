/*
 * This is the source code of Telegram for Android v. 1.4.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.bsui.ActionBar;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.telegram.android.AndroidUtilities;
import org.telegram.messenger.R;

public class BSActionBar extends FrameLayout {

    public static class ActionBarMenuOnItemClick {
        public void onItemClick(int id) {

        }

        public boolean canOpenMenu() {
            return true;
        }
    }

    private FrameLayout titleFrameLayout;
    private ImageView backButtonImageView;
    private TextView titleTextView;
    private TextView subTitleTextView;
    private View actionModeTop;
    private BSActionBarMenu menu;
    private BSActionBarMenu actionMode;
    private boolean occupyStatusBar = Build.VERSION.SDK_INT >= 21;

    private boolean allowOverlayTitle;
    private CharSequence lastTitle;
    private boolean showingOverlayTitle;
    private boolean castShadows = true;

    protected boolean isSearchFieldVisible;
    protected int itemsBackgroundResourceId;
    private boolean isBackOverlayVisible;
    public ActionBarMenuOnItemClick actionBarMenuOnItemClick;
    private int extraHeight;

    public BSActionBar(Context context) {
        super(context);
        Ini(context);
    }

    public BSActionBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        Ini(context);
    }

    public BSActionBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs,defStyle);
        Ini(context);
    }

    private void Ini(Context context) {
        titleFrameLayout = new FrameLayout(context);
        addView(titleFrameLayout);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)titleFrameLayout.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.FILL_PARENT;
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        titleFrameLayout.setLayoutParams(layoutParams);
        titleFrameLayout.setPadding(0, 0, AndroidUtilities.bsDp(4), 0);
        titleFrameLayout.setEnabled(false);
    }

    private void positionBackImage(int height) {
        if (backButtonImageView != null) {
            LayoutParams layoutParams = (LayoutParams)backButtonImageView.getLayoutParams();
            layoutParams.width = AndroidUtilities.bsDp(54);
            layoutParams.height = height;
            layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            backButtonImageView.setLayoutParams(layoutParams);
        }
    }

    private void positionTitle(int width, int height) {
        int offset = AndroidUtilities.bsDp(2);
        if (!AndroidUtilities.isTablet() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            offset = AndroidUtilities.bsDp(1);
        }
        int maxTextWidth = 0;

        LayoutParams layoutParams = null;

        if (titleTextView != null && titleTextView.getVisibility() == VISIBLE) {
            if (!AndroidUtilities.isTablet() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            } else {
                titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            }

            layoutParams = (LayoutParams) titleTextView.getLayoutParams();
            layoutParams.width = LayoutParams.WRAP_CONTENT;
            layoutParams.height = LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            titleTextView.setLayoutParams(layoutParams);
            titleTextView.measure(width, height);
            maxTextWidth = titleTextView.getMeasuredWidth();
        }
        if (subTitleTextView != null && subTitleTextView.getVisibility() == VISIBLE) {
            if (!AndroidUtilities.isTablet() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                subTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            } else {
                subTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            }

            layoutParams = (LayoutParams) subTitleTextView.getLayoutParams();
            layoutParams.width = LayoutParams.WRAP_CONTENT;
            layoutParams.height = LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            subTitleTextView.setLayoutParams(layoutParams);
            subTitleTextView.measure(width, height);
            maxTextWidth = Math.max(maxTextWidth, subTitleTextView.getMeasuredWidth());
        }

        int x = 0;
        if (backButtonImageView != null) {
            if (AndroidUtilities.isTablet()) {
                x = AndroidUtilities.bsDp(80);
            } else {
                x = AndroidUtilities.bsDp(72);
            }
        } else {
            if (AndroidUtilities.isTablet()) {
                x = AndroidUtilities.bsDp(26);
            } else {
                x = AndroidUtilities.bsDp(18);
            }
        }

        if (menu != null) {
            maxTextWidth = Math.min(maxTextWidth, width - menu.getMeasuredWidth() - AndroidUtilities.bsDp(16) - x);
        }

        if (titleTextView != null && titleTextView.getVisibility() == VISIBLE) {
            layoutParams = (LayoutParams) titleTextView.getLayoutParams();
            layoutParams.width = LayoutParams.MATCH_PARENT;
            layoutParams.height = titleTextView.getMeasuredHeight();
            int y;
            if (subTitleTextView != null && subTitleTextView.getVisibility() == VISIBLE) {
                y = (height / 2 - titleTextView.getMeasuredHeight()) / 2 + offset;
            } else {
                y = (height - titleTextView.getMeasuredHeight()) / 2 - AndroidUtilities.bsDp(1);
            }
            layoutParams.setMargins(x, y, 0, 0);
            titleTextView.setLayoutParams(layoutParams);
        }
        if (subTitleTextView != null && subTitleTextView.getVisibility() == VISIBLE) {
            layoutParams = (LayoutParams) subTitleTextView.getLayoutParams();
            layoutParams.width = LayoutParams.MATCH_PARENT;
            layoutParams.height = subTitleTextView.getMeasuredHeight();
            layoutParams.setMargins(x, height / 2 + (height / 2 - subTitleTextView.getMeasuredHeight()) / 2 - offset, 0, 0);
            subTitleTextView.setLayoutParams(layoutParams);
        }

        MarginLayoutParams layoutParams1 = (MarginLayoutParams) titleFrameLayout.getLayoutParams();
        layoutParams1.width = x + maxTextWidth + (isSearchFieldVisible ? 0 : AndroidUtilities.bsDp(6));
        titleFrameLayout.setLayoutParams(layoutParams1);
    }

    public void positionMenu(int width, int height) {
        if (menu == null) {
            return;
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)menu.getLayoutParams();
        layoutParams.width = isSearchFieldVisible ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT;
        layoutParams.height = height;
        layoutParams.leftMargin = isSearchFieldVisible ? AndroidUtilities.bsDp(AndroidUtilities.isTablet() ? 74 : 66) : 0;
        menu.setLayoutParams(layoutParams);
        menu.measure(width, height);
    }

    private void createBackButtonImage() {
        if (backButtonImageView != null) {
            return;
        }
        backButtonImageView = new ImageView(getContext());
        titleFrameLayout.addView(backButtonImageView);
        backButtonImageView.setScaleType(ImageView.ScaleType.CENTER);
        backButtonImageView.setBackgroundResource(itemsBackgroundResourceId);
        backButtonImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSearchFieldVisible) {
                    closeSearchField();
                    return;
                }
                if (actionBarMenuOnItemClick != null) {
                    actionBarMenuOnItemClick.onItemClick(-1);
                }
            }
        });
    }

    public void setBackButtonDrawable(Drawable drawable) {
        if (backButtonImageView == null) {
            createBackButtonImage();
        }
        backButtonImageView.setImageDrawable(drawable);
    }

    public void setBackButtonImage(int resource) {
        if (backButtonImageView == null) {
            createBackButtonImage();
        }
        backButtonImageView.setImageResource(resource);
    }

    private void createSubtitleTextView() {
        if (subTitleTextView != null) {
            return;
        }
        subTitleTextView = new TextView(getContext());
        titleFrameLayout.addView(subTitleTextView);
        subTitleTextView.setGravity(Gravity.LEFT);
        subTitleTextView.setTextColor(0xffd7e8f7);
        subTitleTextView.setSingleLine(true);
        subTitleTextView.setLines(1);
        subTitleTextView.setMaxLines(1);
        subTitleTextView.setEllipsize(TextUtils.TruncateAt.END);
    }

    public void setSubtitle(CharSequence value) {
        if (value != null && subTitleTextView == null) {
            createSubtitleTextView();
        }
        if (subTitleTextView != null) {
            subTitleTextView.setVisibility(value != null && !isSearchFieldVisible ? VISIBLE : GONE);
            subTitleTextView.setText(value);
            positionTitle(getMeasuredWidth(), getMeasuredHeight());
        }
    }

    public void setSubTitleIcon(int resourceId, Drawable drawable, int padding) {
        if ((resourceId != 0 || drawable != null) && subTitleTextView == null) {
            createSubtitleTextView();
            positionTitle(getMeasuredWidth(), getMeasuredHeight());
        }
        if (subTitleTextView != null) {
            if (drawable != null) {
                subTitleTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            } else {
                subTitleTextView.setCompoundDrawablesWithIntrinsicBounds(resourceId, 0, 0, 0);
            }
            subTitleTextView.setCompoundDrawablePadding(padding);
        }
    }

    private void createTitleTextView() {
        if (titleTextView != null) {
            return;
        }
        titleTextView = new TextView(getContext());
        titleTextView.setGravity(Gravity.LEFT);
        titleTextView.setGravity(Gravity.TOP);
        titleTextView.setSingleLine(true);
        titleTextView.setLines(1);
        titleTextView.setMaxLines(1);
        titleTextView.setEllipsize(TextUtils.TruncateAt.END);
        titleFrameLayout.addView(titleTextView);
        titleTextView.setTextColor(0xffffffff);
        titleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
    }

    public void setTitle(CharSequence value) {
        if (value != null && titleTextView == null) {
            createTitleTextView();
        }
        if (titleTextView != null) {
            lastTitle = value;
            titleTextView.setVisibility(value != null && !isSearchFieldVisible ? VISIBLE : GONE);
            titleTextView.setText(value);
            positionTitle(getMeasuredWidth(), getMeasuredHeight());
        }
    }

    public void setTitleIcon(int resourceId, int padding) {
        if (resourceId != 0 && titleTextView == null) {
            createTitleTextView();
            positionTitle(getMeasuredWidth(), getMeasuredHeight());
        }
        titleTextView.setCompoundDrawablesWithIntrinsicBounds(resourceId, 0, 0, 0);
        titleTextView.setCompoundDrawablePadding(padding);
    }

    public Drawable getSubTitleIcon() {
        return subTitleTextView.getCompoundDrawables()[0];
    }

    public CharSequence getTitle() {
        if (titleTextView == null) {
            return null;
        }
        return titleTextView.getText();
    }

    public BSActionBarMenu createMenu() {
        if (menu != null) {
            return menu;
        }
        menu = new BSActionBarMenu(getContext(), this);
        addView(menu);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)menu.getLayoutParams();
        layoutParams.height = LayoutParams.FILL_PARENT;
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.RIGHT;
        menu.setLayoutParams(layoutParams);
        return menu;
    }

    public void setActionBarMenuOnItemClick(ActionBarMenuOnItemClick listener) {
        actionBarMenuOnItemClick = listener;
    }

    public void setCustomView(int resourceId) {
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(resourceId, null);
        addView(view);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)view.getLayoutParams();
        layoutParams.width = LayoutParams.FILL_PARENT;
        layoutParams.height = LayoutParams.FILL_PARENT;
        view.setLayoutParams(layoutParams);
    }

    public BSActionBarMenu createActionMode() {
        if (actionMode != null) {
            return actionMode;
        }
        actionMode = new BSActionBarMenu(getContext(), this);
        actionMode.setBackgroundResource(R.drawable.editheader_bs);
        addView(actionMode);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)actionMode.getLayoutParams();
        layoutParams.height = LayoutParams.FILL_PARENT;
        layoutParams.width = LayoutParams.FILL_PARENT;
        layoutParams.gravity = Gravity.RIGHT;
        actionMode.setLayoutParams(layoutParams);
        actionMode.setVisibility(GONE);

        return actionMode;
    }

    public void showActionMode() {
        if (actionMode == null) {
            return;
        }
        actionMode.setVisibility(VISIBLE);
        if (occupyStatusBar && actionModeTop != null) {
            actionModeTop.setVisibility(VISIBLE);
        }
        if (titleFrameLayout != null) {
            titleFrameLayout.setVisibility(INVISIBLE);
        }
        if (menu != null) {
            menu.setVisibility(INVISIBLE);
        }
    }

    public void hideActionMode() {
        if (actionMode == null) {
            return;
        }
        actionMode.setVisibility(GONE);
        if (occupyStatusBar && actionModeTop != null) {
            actionModeTop.setVisibility(GONE);
        }
        if (titleFrameLayout != null) {
            titleFrameLayout.setVisibility(VISIBLE);
        }
        if (menu != null) {
            menu.setVisibility(VISIBLE);
        }
    }

    public boolean isActionModeShowed() {
        return actionMode != null && actionMode.getVisibility() == VISIBLE;
    }

    protected void onSearchFieldVisibilityChanged(boolean visible) {
        isSearchFieldVisible = visible;
        if (titleTextView != null) {
            titleTextView.setVisibility(visible ? GONE : VISIBLE);
        }
        if (subTitleTextView != null) {
            subTitleTextView.setVisibility(visible ? GONE : VISIBLE);
        }
        Drawable drawable = backButtonImageView.getDrawable();
        if (drawable != null && drawable instanceof BSMenuDrawable) {
            ((BSMenuDrawable)drawable).setRotation(visible ? 1 : 0);
        }
    }

    public void closeSearchField() {
        if (!isSearchFieldVisible || menu == null) {
            return;
        }
        menu.closeSearchField();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int actionBarHeight = AndroidUtilities.getBSCurrentActionBarHeight();
        positionBackImage(actionBarHeight);
        positionMenu(MeasureSpec.getSize(widthMeasureSpec), actionBarHeight);
        positionTitle(MeasureSpec.getSize(widthMeasureSpec), actionBarHeight);
        super.onMeasure(widthMeasureSpec, actionBarHeight);
    }

    public void onMenuButtonPressed() {
        if (menu != null) {
            menu.onMenuButtonPressed();
        }
    }

    protected void onPause() {
        if (menu != null) {
            menu.hideAllPopupMenus();
        }
    }

    public void setAllowOverlayTitle(boolean value) {
        allowOverlayTitle = value;
    }

    public void setTitleOverlayText(String text) {
        if (showingOverlayTitle == (text != null) || !allowOverlayTitle) {
            return;
        }
        showingOverlayTitle = text != null;
        CharSequence textToSet = text != null ? text : lastTitle;
        if (textToSet != null && titleTextView == null) {
            createTitleTextView();
        }
        if (titleTextView != null) {
            titleTextView.setVisibility(textToSet != null && !isSearchFieldVisible ? VISIBLE : GONE);
            titleTextView.setText(textToSet);
            positionTitle(getMeasuredWidth(), getMeasuredHeight());
        }
    }

    public void setExtraHeight(int value, boolean layout) {
        extraHeight = value;
        if (layout) {
            requestLayout();
        }
    }

    public int getExtraHeight() {
        return extraHeight;
    }

    public boolean getOccupyStatusBar() {
        return occupyStatusBar;
    }

    public void setItemsBackground(int resourceId) {
        itemsBackgroundResourceId = resourceId;
        if (backButtonImageView != null) {
            backButtonImageView.setBackgroundResource(itemsBackgroundResourceId);
        }
    }

    public void setCastShadows(boolean value) {
        castShadows = value;
    }

    public boolean getCastShadows() {
        return castShadows;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return true;
    }
}