package org.telegram.bsui.ActionBar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.telegram.android.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBarMenuItem;

/**
 * Created by E1ektr0 on 10.01.2015.
 */
public class BSActionBarMenu extends LinearLayout {

    public BSActionBar parentActionBar;

    public BSActionBarMenu(Context context, BSActionBar layer) {
        super(context);
        setOrientation(LinearLayout.HORIZONTAL);
        parentActionBar = layer;
    }

    public BSActionBarMenu(Context context) {
        super(context);
    }

    public BSActionBarMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BSActionBarMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public View addItemResource(int id, int resourceId) {
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(resourceId, null);
        view.setTag(id);
        addView(view);
        LayoutParams layoutParams = (LayoutParams)view.getLayoutParams();
        layoutParams.height = FrameLayout.LayoutParams.FILL_PARENT;
        view.setBackgroundResource(parentActionBar.itemsBackgroundResourceId);
        view.setLayoutParams(layoutParams);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick((Integer)view.getTag());
            }
        });
        return view;
    }

    public BSActionBarMenuItem addItem(int id, Drawable drawable) {
        return addItem(id, 0, parentActionBar.itemsBackgroundResourceId, drawable, AndroidUtilities.bsDp(48));
    }

    public BSActionBarMenuItem addItem(int id, int icon) {
        return addItem(id, icon, parentActionBar.itemsBackgroundResourceId);
    }

    public BSActionBarMenuItem addItem(int id, int icon, int backgroundResource) {
        return addItem(id, icon, backgroundResource, null, AndroidUtilities.bsDp(48));
    }

    public BSActionBarMenuItem addItemWithWidth(int id, int icon, int width) {
        return addItem(id, icon, parentActionBar.itemsBackgroundResourceId, null, width);
    }

    public BSActionBarMenuItem addItem(int id, int icon, int backgroundResource, Drawable drawable, int width) {
        BSActionBarMenuItem menuItem = new BSActionBarMenuItem(getContext(), this, backgroundResource);
        menuItem.setTag(id);
//        menuItem.setScaleType(ImageView.ScaleType.CENTER);
        if (drawable != null) {
            menuItem.iconView.setImageDrawable(drawable);
        } else {
            menuItem.iconView.setImageResource(icon);
        }
        addView(menuItem);
        LayoutParams layoutParams = (LayoutParams)menuItem.getLayoutParams();
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.width = width;
        menuItem.setLayoutParams(layoutParams);
        menuItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                BSActionBarMenuItem item = (BSActionBarMenuItem)view;
                if (item.hasSubMenu()) {
                    if (parentActionBar.actionBarMenuOnItemClick.canOpenMenu()) {
                        item.toggleSubMenu();
                    }
                } else if (item.isSearchField()) {
                    parentActionBar.onSearchFieldVisibilityChanged(item.toggleSearch());
                } else {
                    onItemClick((Integer)view.getTag());
                }
            }
        });
        return menuItem;
    }

    public void hideAllPopupMenus() {
        for (int a = 0; a < getChildCount(); a++) {
            View view = getChildAt(a);
            if (view instanceof ActionBarMenuItem) {
                ((ActionBarMenuItem)view).closeSubMenu();
            }
        }
    }

    public void onItemClick(int id) {
        if (parentActionBar.actionBarMenuOnItemClick != null) {
            parentActionBar.actionBarMenuOnItemClick.onItemClick(id);
        }
    }

    public void clearItems() {
        for (int a = 0; a < getChildCount(); a++) {
            View view = getChildAt(a);
            removeView(view);
        }
    }

    public void onMenuButtonPressed() {
        for (int a = 0; a < getChildCount(); a++) {
            View view = getChildAt(a);
            if (view instanceof ActionBarMenuItem) {
                ActionBarMenuItem item = (ActionBarMenuItem)view;
                if (item.hasSubMenu() && item.getVisibility() == VISIBLE) {
                    item.toggleSubMenu();
                    break;
                }
            }
        }
    }

    public void closeSearchField() {
        for (int a = 0; a < getChildCount(); a++) {
            View view = getChildAt(a);
            if (view instanceof BSActionBarMenuItem) {
                BSActionBarMenuItem item = (BSActionBarMenuItem)view;
                if (item.isSearchField()) {
                    parentActionBar.onSearchFieldVisibilityChanged(item.toggleSearch());
                }
            }
        }
    }

    public BSActionBarMenuItem getItem(int id) {
        View v = findViewWithTag(id);
        if (v instanceof BSActionBarMenuItem) {
            return (BSActionBarMenuItem)v;
        }
        return null;
    }
}
