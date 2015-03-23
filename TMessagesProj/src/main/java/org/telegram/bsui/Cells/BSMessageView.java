package org.telegram.bsui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.ImageReceiver;
import org.telegram.android.MessageObject;
import org.telegram.android.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.ui.Components.AvatarDrawable;

/**
 * Created by fanticqq on 18.03.15.
 */
public class BSMessageView extends BSBaseCell {

    public boolean isChat = false;
    protected boolean media = false;
    private boolean wasLayout = false;
    protected boolean isAvatarVisible = false;
    protected boolean drawBackground = true;
    protected MessageObject currentMessageObject;

    private static Drawable backgroundDrawableIn;
    private static Drawable backgroundDrawableInSelected;
    private static Drawable backgroundDrawableOut;
    private static Drawable backgroundDrawableOutSelected;
    private static Drawable backgroundMediaDrawableIn;
    private static Drawable backgroundMediaDrawableInSelected;
    private static Drawable backgroundMediaDrawableOut;
    private static Drawable backgroundMediaDrawableOutSelected;

    protected int backgroundWidth = 100;

    protected int layoutWidth;
    protected int layoutHeight;

    private ImageReceiver avatarImage;
    private AvatarDrawable avatarDrawable;
    private boolean avatarPressed = false;
    private boolean forwardNamePressed = false;
    private TLRPC.User currentUser;
    private TLRPC.FileLocation currentPhoto;
    private boolean isCheckPressed;
    private boolean isPressed;
    private Drawable currentBackgroundDrawable;

    public Drawable getCurrentBackgroundDrawable() {
        return currentBackgroundDrawable;
    }

    public BSMessageView(Context context) {
        super(context);
        if (backgroundDrawableIn == null) {
            backgroundDrawableIn = getResources().getDrawable(R.drawable.msg_in);
            backgroundDrawableOut = getResources().getDrawable(R.drawable.msg_out);
            backgroundDrawableInSelected = getResources().getDrawable(R.drawable.msg_in_selected);
            backgroundDrawableOutSelected = getResources().getDrawable(R.drawable.msg_out_selected);
            backgroundMediaDrawableIn = getResources().getDrawable(R.drawable.msg_in_photo);
            backgroundMediaDrawableInSelected = getResources().getDrawable(R.drawable.msg_in_photo_selected);
            backgroundMediaDrawableOut = getResources().getDrawable(R.drawable.msg_out_photo);
            backgroundMediaDrawableOutSelected = getResources().getDrawable(R.drawable.msg_out_photo_selected);
        }
        avatarImage = new ImageReceiver(this);
        avatarImage.setRoundRadius(dp(21));
        avatarDrawable = new AvatarDrawable();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        avatarImage.clearImage();
    }

    public void setCheckPressed(boolean value, boolean pressed) {
        isCheckPressed = value;
        isPressed = pressed;
        invalidate();
    }

    public void setMessageObject(MessageObject messageObject) {
        currentMessageObject = messageObject;
        isAvatarVisible = false;
        wasLayout = false;
        isPressed = false;
        isCheckPressed = true;

        currentUser = MessagesController.getInstance().getUser(messageObject.messageOwner.from_id);
        if (isChat && !messageObject.isOut()) {
            isAvatarVisible = true;
            if (currentUser != null) {
                if (currentUser.photo != null) {
                    currentPhoto = currentUser.photo.photo_small;
                } else {
                    currentPhoto = null;
                }
                avatarDrawable.setInfo(currentUser);
            } else {
                currentPhoto = null;
                avatarDrawable.setInfo(messageObject.messageOwner.from_id, null, null, false);
            }
            avatarImage.setImage(currentPhoto, "50_50", avatarDrawable, false);
        }

        if (currentMessageObject.isOut()) {
            if (isPressed() && isCheckPressed || !isCheckPressed && isPressed) {
                if (!media) {
                    currentBackgroundDrawable = backgroundDrawableOutSelected;
                } else {
                    currentBackgroundDrawable = backgroundMediaDrawableOutSelected;
                }
            } else {
                if (!media) {
                    currentBackgroundDrawable = backgroundDrawableOut;
                } else {
                    currentBackgroundDrawable = backgroundMediaDrawableOut;
                }
            }
            setDrawableBounds(currentBackgroundDrawable, layoutWidth - backgroundWidth - (!media ? 0 : dp(9)), dp(1), backgroundWidth, layoutHeight - dp(2));
        } else {
            if (isPressed() && isCheckPressed || !isCheckPressed && isPressed) {
                if (!media) {
                    currentBackgroundDrawable = backgroundDrawableInSelected;
                } else {
                    currentBackgroundDrawable = backgroundMediaDrawableInSelected;
                }
            } else {
                if (!media) {
                    currentBackgroundDrawable = backgroundDrawableIn;
                } else {
                    currentBackgroundDrawable = backgroundMediaDrawableIn;
                }
            }
            if (isChat) {
                setDrawableBounds(currentBackgroundDrawable, dp(52 + (!media ? 0 : 9)), dp(1), backgroundWidth, layoutHeight - dp(2));
            } else {
                setDrawableBounds(currentBackgroundDrawable, (!media ? 0 : dp(9)), dp(1), backgroundWidth, layoutHeight - dp(2));
            }
        }

        requestLayout();
    }

    public final MessageObject getMessageObject() {
        return currentMessageObject;
    }

    protected int getMaxNameWidth() {
        return backgroundWidth - dp(8);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (currentMessageObject == null) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }

        if (changed || !wasLayout) {
            layoutWidth = getMeasuredWidth();
            layoutHeight = getMeasuredHeight();

            if (isAvatarVisible) {
                avatarImage.setImageCoords(dp(6), layoutHeight - dp(45), dp(42), dp(42));
            }

            wasLayout = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (currentMessageObject == null) {
            return;
        }

        if (!wasLayout) {
            requestLayout();
            return;
        }

        if (isAvatarVisible) {
            avatarImage.draw(canvas);
        }

        if (drawBackground) {
            currentBackgroundDrawable.draw(canvas);
        }
    }

    protected int dp(float value) {
        return AndroidUtilities.bsDp(value);
    }


    protected int getDisplayY() {
        return AndroidUtilities.bsDisplaySize.y;
    }

    protected int getDisplayX() {
        return AndroidUtilities.bsDisplaySize.x;
    }

    protected float getDensity() {
        return AndroidUtilities.bsDensity;
    }
}
