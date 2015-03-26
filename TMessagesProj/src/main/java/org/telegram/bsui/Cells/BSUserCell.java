package org.telegram.bsui.Cells;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.ContactsController;
import org.telegram.android.LocaleController;
import org.telegram.android.MessagesController;
import org.telegram.bsui.BSAvatarDrawable;
import org.telegram.messenger.ConnectionsManager;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CheckBox;

/**
 * Created by E1ektr0 on 09.01.2015.
 */
public class BSUserCell extends FrameLayout {

    private BackupImageView avatarImageView;
    private TextView nameTextView;
    private TextView statusTextView;
    private ImageView imageView;
    private CheckBox checkBox;

    private BSAvatarDrawable avatarDrawable;
    private TLRPC.User currentUser = null;

    private CharSequence currentName;
    private CharSequence currrntStatus;
    private int currentDrawable;

    private String lastName = null;
    private int lastStatus = 0;
    private TLRPC.FileLocation lastAvatar = null;

    private int statusColor = 0xffa8a8a8;
    private int statusOnlineColor = 0xff3b84c0;

    public BSUserCell(Context context, int padding) {
        super(context);

        avatarImageView = new BackupImageView(context);
        avatarImageView.imageReceiver.setRoundRadius(AndroidUtilities.bsDp(24));
        addView(avatarImageView);
        LayoutParams layoutParams = (LayoutParams) avatarImageView.getLayoutParams();
        layoutParams.width = AndroidUtilities.bsDp(48);
        layoutParams.height = AndroidUtilities.bsDp(48);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        layoutParams.leftMargin = LocaleController.isRTL ? 0 : AndroidUtilities.bsDp(15 + padding);
        layoutParams.rightMargin = LocaleController.isRTL ? AndroidUtilities.bsDp(15 + padding) : 0;
        layoutParams.topMargin = AndroidUtilities.bsDp(8);
        avatarImageView.setLayoutParams(layoutParams);
        avatarDrawable = new BSAvatarDrawable();

        nameTextView = new TextView(context);
        nameTextView.setTextColor(0xff212121);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);//17;
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        addView(nameTextView);
        layoutParams = (LayoutParams) nameTextView.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.leftMargin = AndroidUtilities.bsDp(LocaleController.isRTL ? 16 : (68 + padding));
        layoutParams.rightMargin = AndroidUtilities.bsDp(LocaleController.isRTL ? (68 + padding) : 16);
        layoutParams.topMargin = AndroidUtilities.bsDp(10.5f);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        nameTextView.setLayoutParams(layoutParams);

        statusTextView = new TextView(context);
        statusTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8);//14
        statusTextView.setLines(1);
        statusTextView.setMaxLines(1);
        statusTextView.setSingleLine(true);
        statusTextView.setEllipsize(TextUtils.TruncateAt.END);
        statusTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        addView(statusTextView);
        layoutParams = (LayoutParams) statusTextView.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.leftMargin = AndroidUtilities.bsDp(LocaleController.isRTL ? 16 : (68 + padding));
        layoutParams.rightMargin = AndroidUtilities.bsDp(LocaleController.isRTL ? (68 + padding) : 16);
        layoutParams.topMargin = AndroidUtilities.bsDp(33.5f);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        statusTextView.setLayoutParams(layoutParams);

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

        checkBox = new CheckBox(context, R.drawable.round_check2);
        checkBox.setVisibility(GONE);
        addView(checkBox);
        layoutParams = (LayoutParams) checkBox.getLayoutParams();
        layoutParams.width = AndroidUtilities.bsDp(22);
        layoutParams.height = AndroidUtilities.bsDp(22);
        layoutParams.topMargin = AndroidUtilities.bsDp(38);
        layoutParams.leftMargin = LocaleController.isRTL ? 0 : AndroidUtilities.bsDp(37 + padding);
        layoutParams.rightMargin = LocaleController.isRTL ? AndroidUtilities.bsDp(37 + padding) : 0;
        layoutParams.gravity = (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        checkBox.setLayoutParams(layoutParams);
    }

    public void setData(TLRPC.User user, CharSequence name, CharSequence status, int resId) {
        if (user == null) {
            currrntStatus = null;
            currentName = null;
            currentUser = null;
            nameTextView.setText("");
            statusTextView.setText("");
            avatarImageView.setImageDrawable(null);
            return;
        }
        currrntStatus = status;
        currentName = name;
        currentUser = user;
        currentDrawable = resId;
        update(0);
    }

    public void setChecked(boolean checked, boolean animated) {
        if (checkBox.getVisibility() != VISIBLE) {
            checkBox.setVisibility(VISIBLE);
        }
        checkBox.setChecked(checked, animated);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        lastAvatar = null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.bsDp(64), MeasureSpec.EXACTLY));
    }

    public void setStatusColors(int color, int onlineColor) {
        statusColor = color;
        statusOnlineColor = onlineColor;
    }

    public void update(int mask) {
        if (currentUser == null) {
            return;
        }
        TLRPC.FileLocation photo = null;
        if (currentUser.photo != null) {
            photo = currentUser.photo.photo_small;
        }

        if (mask != 0) {
            boolean continueUpdate = false;
            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0) {
                if (lastAvatar != null && photo == null || lastAvatar == null && photo != null && lastAvatar != null && photo != null && (lastAvatar.volume_id != photo.volume_id || lastAvatar.local_id != photo.local_id)) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate && (mask & MessagesController.UPDATE_MASK_STATUS) != 0) {
                int newStatus = 0;
                if (currentUser.status != null) {
                    newStatus = currentUser.status.expires;
                }
                if (newStatus != lastStatus) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate && (mask & MessagesController.UPDATE_MASK_NAME) != 0) {
                String newName = currentUser.first_name + currentUser.last_name;
                if (newName == null || !newName.equals(lastName)) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate) {
                return;
            }
        }

        avatarDrawable.setInfo(currentUser);
        if (currentUser.status != null) {
            lastStatus = currentUser.status.expires;
        } else {
            lastStatus = 0;
        }
        lastName = currentUser.first_name + currentUser.last_name;
        lastAvatar = photo;

        if (currentName != null) {
            nameTextView.setText(currentName);
        } else {
            nameTextView.setText(ContactsController.formatName(currentUser.first_name, currentUser.last_name));
        }
        if (currrntStatus != null) {
            statusTextView.setText(currrntStatus);
            statusTextView.setTextColor(statusColor);
        } else {
            if (currentUser.id == UserConfig.getClientUserId() || currentUser.status != null && currentUser.status.expires > ConnectionsManager.getInstance().getCurrentTime()) {
                statusTextView.setText(LocaleController.getString("Online", R.string.Online));
                statusTextView.setTextColor(statusOnlineColor);
            } else {
                statusTextView.setText(LocaleController.formatUserStatus(currentUser));
                statusTextView.setTextColor(statusColor);
            }
        }

        imageView.setVisibility(currentDrawable == 0 ? GONE : VISIBLE);
        imageView.setImageResource(currentDrawable);
        avatarImageView.setImage(photo, "50_50", avatarDrawable);
    }
}