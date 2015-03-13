/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.ContactsController;
import org.telegram.android.LocaleController;
import org.telegram.messenger.TLRPC;
import org.telegram.android.MessagesController;
import org.telegram.messenger.R;
import org.telegram.android.MessageObject;
import org.telegram.android.ImageReceiver;
import org.telegram.ui.Components.AvatarDrawable;

public class ChatBaseCell extends BaseCell {

    public static interface ChatBaseCellDelegate {
        public abstract void didPressedUserAvatar(ChatBaseCell cell, TLRPC.User user);
        public abstract void didPressedCancelSendButton(ChatBaseCell cell);
        public abstract void didLongPressed(ChatBaseCell cell);
        public abstract boolean canPerformActions();
    }

    public boolean isChat = false;
    protected boolean isPressed = false;
    protected boolean forwardName = false;
    protected boolean media = false;
    protected boolean isCheckPressed = true;
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
    private static Drawable checkDrawable;
    private static Drawable halfCheckDrawable;
    private static Drawable clockDrawable;
    private static Drawable broadcastDrawable;
    private static Drawable checkMediaDrawable;
    private static Drawable halfCheckMediaDrawable;
    private static Drawable clockMediaDrawable;
    private static Drawable broadcastMediaDrawable;
    private static Drawable errorDrawable;
    protected static Drawable mediaBackgroundDrawable;
    private static TextPaint timePaintIn;
    private static TextPaint timePaintOut;
    private static TextPaint timeMediaPaint;
    private static TextPaint namePaint;
    private static TextPaint forwardNamePaint;

    protected TextPaint getTimePaintIn() {
        return timePaintIn;
    }

    protected TextPaint getTimePaintOut() {
        return timePaintOut;
    }

    protected TextPaint getTimeMediaPaint() {
        return timeMediaPaint;
    }

    protected TextPaint getNamePaint() {
        return namePaint;
    }

    protected TextPaint getForwardNamePaint() {
        return forwardNamePaint;
    }

    protected Drawable getCheckDrawable() {
        return checkDrawable;
    }

    protected Drawable getHalfCheckDrawable() {
        return halfCheckDrawable;
    }

    protected Drawable getClockDrawable() {
        return clockDrawable;
    }

    protected Drawable getBroadcastDrawable() {
        return broadcastDrawable;
    }

    protected Drawable getErrorDrawable() {
        return errorDrawable;
    }

    protected Drawable getBroadcastMediaDrawable() {
        return broadcastMediaDrawable;
    }

    protected Drawable getClockMediaDrawable() {
        return clockMediaDrawable;
    }

    protected Drawable getCheckMediaDrawable() {
        return checkMediaDrawable;
    }

    protected Drawable getHalfCheckMediaDrawable() {
        return halfCheckMediaDrawable;
    }

    protected Drawable getBackgroundDrawableOutSelected() {
        return backgroundDrawableOutSelected;
    }

    protected Drawable getBackgroundDrawableOut() {
        return backgroundDrawableOut;
    }

    protected Drawable getBackgroundDrawableInSelected() {
        return backgroundDrawableInSelected;
    }

    protected Drawable getBackgroundDrawableIn() {
        return backgroundDrawableIn;
    }

    protected int backgroundWidth = 100;

    protected int layoutWidth;
    protected int layoutHeight;

    private ImageReceiver avatarImage;
    private AvatarDrawable avatarDrawable;
    private boolean avatarPressed = false;
    private boolean forwardNamePressed = false;

    private StaticLayout nameLayout;
    protected int nameWidth;
    private float nameOffsetX = 0;
    protected boolean drawName = false;

    private StaticLayout forwardedNameLayout;
    protected int forwardedNameWidth;
    protected boolean drawForwardedName = false;
    private int forwardNameX;
    private int forwardNameY;
    private float forwardNameOffsetX = 0;

    private StaticLayout timeLayout;
    protected int timeWidth;
    private int timeX;
    private TextPaint currentTimePaint;
    private String currentTimeString;
    protected boolean drawTime = true;

    private TLRPC.User currentUser;
    private TLRPC.FileLocation currentPhoto;
    private String currentNameString;

    private TLRPC.User currentForwardUser;
    private String currentForwardNameString;

    protected ChatBaseCellDelegate delegate;

    protected int namesOffset = 0;

    private int last_send_state = 0;
    private int last_delete_date = 0;

    public ChatBaseCell(Context context) {
        super(context);
        initResources();
        avatarImage = new ImageReceiver(this);
        avatarImage.setRoundRadius(dp(21));
        avatarDrawable = new AvatarDrawable();
    }

    protected void initResources() {
        if (backgroundDrawableIn == null) {
            backgroundDrawableIn = getResources().getDrawable(R.drawable.msg_in);
            backgroundDrawableInSelected = getResources().getDrawable(R.drawable.msg_in_selected);
            backgroundDrawableOut = getResources().getDrawable(R.drawable.msg_out);
            backgroundDrawableOutSelected = getResources().getDrawable(R.drawable.msg_out_selected);
            backgroundMediaDrawableIn = getResources().getDrawable(R.drawable.msg_in_photo);
            backgroundMediaDrawableInSelected = getResources().getDrawable(R.drawable.msg_in_photo_selected);
            backgroundMediaDrawableOut = getResources().getDrawable(R.drawable.msg_out_photo);
            backgroundMediaDrawableOutSelected = getResources().getDrawable(R.drawable.msg_out_photo_selected);
            checkDrawable = getResources().getDrawable(R.drawable.msg_check);
            halfCheckDrawable = getResources().getDrawable(R.drawable.msg_halfcheck);
            clockDrawable = getResources().getDrawable(R.drawable.msg_clock);
            checkMediaDrawable = getResources().getDrawable(R.drawable.msg_check_w);
            halfCheckMediaDrawable = getResources().getDrawable(R.drawable.msg_halfcheck_w);
            clockMediaDrawable = getResources().getDrawable(R.drawable.msg_clock_photo);
            errorDrawable = getResources().getDrawable(R.drawable.msg_warning);
            mediaBackgroundDrawable = getResources().getDrawable(R.drawable.phototime);
            broadcastDrawable = getResources().getDrawable(R.drawable.broadcast3);
            broadcastMediaDrawable = getResources().getDrawable(R.drawable.broadcast4);

            timePaintIn = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaintIn.setTextSize(dp(12));
            timePaintIn.setColor(0xffa1aab3);

            timePaintOut = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaintOut.setTextSize(dp(12));
            timePaintOut.setColor(0xff70b15c);

            timeMediaPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timeMediaPaint.setTextSize(dp(12));
            timeMediaPaint.setColor(0xffffffff);

            namePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            namePaint.setTextSize(dp(15));

            forwardNamePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            forwardNamePaint.setTextSize(dp(14));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        avatarImage.clearImage();
        currentPhoto = null;
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        invalidate();
    }

    public void setDelegate(ChatBaseCellDelegate delegate) {
        this.delegate = delegate;
    }

    public void setCheckPressed(boolean value, boolean pressed) {
        isCheckPressed = value;
        isPressed = pressed;
        invalidate();
    }

    protected boolean isUserDataChanged() {
        if (currentMessageObject == null || currentUser == null) {
            return false;
        }
        if (last_send_state != currentMessageObject.messageOwner.send_state) {
            return true;
        }
        if (last_delete_date != currentMessageObject.messageOwner.destroyTime) {
            return true;
        }

        TLRPC.User newUser = MessagesController.getInstance().getUser(currentMessageObject.messageOwner.from_id);
        TLRPC.FileLocation newPhoto = null;

        if (isAvatarVisible && newUser != null && newUser.photo != null) {
            newPhoto = newUser.photo.photo_small;
        }

        if (currentPhoto == null && newPhoto != null || currentPhoto != null && newPhoto == null || currentPhoto != null && newPhoto != null && (currentPhoto.local_id != newPhoto.local_id || currentPhoto.volume_id != newPhoto.volume_id)) {
            return true;
        }

        String newNameString = null;
        if (drawName && isChat && newUser != null && !currentMessageObject.isOut()) {
            newNameString = ContactsController.formatName(newUser.first_name, newUser.last_name);
        }

        if (currentNameString == null && newNameString != null || currentNameString != null && newNameString == null || currentNameString != null && newNameString != null && !currentNameString.equals(newNameString)) {
            return true;
        }

        newUser = MessagesController.getInstance().getUser(currentMessageObject.messageOwner.fwd_from_id);
        newNameString = null;
        if (newUser != null && drawForwardedName && currentMessageObject.messageOwner instanceof TLRPC.TL_messageForwarded) {
            newNameString = ContactsController.formatName(newUser.first_name, newUser.last_name);
        }
        return currentForwardNameString == null && newNameString != null || currentForwardNameString != null && newNameString == null || currentForwardNameString != null && newNameString != null && !currentForwardNameString.equals(newNameString);
    }

    public void setMessageObject(MessageObject messageObject) {
        currentMessageObject = messageObject;
        last_send_state = messageObject.messageOwner.send_state;
        last_delete_date = messageObject.messageOwner.destroyTime;
        isPressed = false;
        isCheckPressed = true;
        isAvatarVisible = false;
        wasLayout = false;

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

        if (!media) {
            if (currentMessageObject.isOut()) {
                currentTimePaint = getTimePaintOut();
            } else {
                currentTimePaint = getTimePaintIn();
            }
        } else {
            currentTimePaint = getTimePaintOut();
        }

        currentTimeString = LocaleController.formatterDay.format((long) (currentMessageObject.messageOwner.date) * 1000);
        timeWidth = (int)Math.ceil(currentTimePaint.measureText(currentTimeString));

        namesOffset = 0;

        if (drawName && isChat && currentUser != null && !currentMessageObject.isOut()) {
            currentNameString = ContactsController.formatName(currentUser.first_name, currentUser.last_name);
            nameWidth = getMaxNameWidth();

            CharSequence nameStringFinal = TextUtils.ellipsize(currentNameString.replace("\n", " "), getNamePaint(), nameWidth - dp(12), TextUtils.TruncateAt.END);
            nameLayout = new StaticLayout(nameStringFinal, getNamePaint(), nameWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (nameLayout.getLineCount() > 0) {
                nameWidth = (int)Math.ceil(nameLayout.getLineWidth(0));
                namesOffset += dp(18);
                nameOffsetX = nameLayout.getLineLeft(0);
            } else {
                nameWidth = 0;
            }
        } else {
            currentNameString = null;
            nameLayout = null;
            nameWidth = 0;
        }

        if (drawForwardedName && messageObject.messageOwner instanceof TLRPC.TL_messageForwarded) {
            currentForwardUser = MessagesController.getInstance().getUser(messageObject.messageOwner.fwd_from_id);
            if (currentForwardUser != null) {
                currentForwardNameString = ContactsController.formatName(currentForwardUser.first_name, currentForwardUser.last_name);

                forwardedNameWidth = getMaxNameWidth();

                CharSequence str = TextUtils.ellipsize(currentForwardNameString.replace("\n", " "), getForwardNamePaint(), forwardedNameWidth - dp(40), TextUtils.TruncateAt.END);
                str = Html.fromHtml(String.format("%s<br>%s <b>%s</b>", LocaleController.getString("ForwardedMessage", R.string.ForwardedMessage), LocaleController.getString("From", R.string.From), str));
                forwardedNameLayout = new StaticLayout(str, getForwardNamePaint(), forwardedNameWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                if (forwardedNameLayout.getLineCount() > 1) {
                    forwardedNameWidth = Math.max((int) Math.ceil(forwardedNameLayout.getLineWidth(0)), (int) Math.ceil(forwardedNameLayout.getLineWidth(1)));
                    namesOffset += dp(36);
                    forwardNameOffsetX = Math.min(forwardedNameLayout.getLineLeft(0), forwardedNameLayout.getLineLeft(1));
                } else {
                    forwardedNameWidth = 0;
                }
            } else {
                currentForwardNameString = null;
                forwardedNameLayout = null;
                forwardedNameWidth = 0;
            }
        } else {
            currentForwardNameString = null;
            forwardedNameLayout = null;
            forwardedNameWidth = 0;
        }

        requestLayout();
    }

    public final MessageObject getMessageObject() {
        return currentMessageObject;
    }

    protected int getMaxNameWidth() {
        return backgroundWidth - dp(8);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;
        float x = event.getX();
        float y = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (delegate == null || delegate.canPerformActions()) {
                if (isAvatarVisible && avatarImage.isInsideImage(x, y)) {
                    avatarPressed = true;
                    result = true;
                } else if (drawForwardedName && forwardedNameLayout != null) {
                    if (x >= forwardNameX && x <= forwardNameX + forwardedNameWidth && y >= forwardNameY && y <= forwardNameY + dp(32)) {
                        forwardNamePressed = true;
                        result = true;
                    }
                }
                if (result) {
                    startCheckLongPress();
                }
            }
        } else {
            if (event.getAction() != MotionEvent.ACTION_MOVE) {
                cancelCheckLongPress();
            }
            if (avatarPressed) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    avatarPressed = false;
                    playSoundEffect(SoundEffectConstants.CLICK);
                    if (delegate != null) {
                        delegate.didPressedUserAvatar(this, currentUser);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    avatarPressed = false;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (isAvatarVisible && !avatarImage.isInsideImage(x, y)) {
                        avatarPressed = false;
                    }
                }
            } else if (forwardNamePressed) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    forwardNamePressed = false;
                    playSoundEffect(SoundEffectConstants.CLICK);
                    if (delegate != null) {
                        delegate.didPressedUserAvatar(this, currentForwardUser);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    forwardNamePressed = false;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!(x >= forwardNameX && x <= forwardNameX + forwardedNameWidth && y >= forwardNameY && y <= forwardNameY + dp(32))) {
                        forwardNamePressed = false;
                    }
                }
            }
        }
        return result;
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

            timeLayout = new StaticLayout(currentTimeString, currentTimePaint, timeWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (!media) {
                if (!currentMessageObject.isOut()) {
                    timeX = backgroundWidth - dp(9) - timeWidth + (isChat ? dp(52) : 0);
                } else {
                    timeX = layoutWidth - timeWidth - dp(38.5f);
                }
            } else {
                if (!currentMessageObject.isOut()) {
                    timeX = backgroundWidth - dp(4) - timeWidth + (isChat ? dp(52) : 0);
                } else {
                    timeX = layoutWidth - timeWidth - dp(42.0f);
                }
            }

            if (isAvatarVisible) {
                avatarImage.setImageCoords(dp(6), layoutHeight - dp(45), dp(42), dp(42));
            }

            wasLayout = true;
        }
    }

    protected void onAfterBackgroundDraw(Canvas canvas) {

    }

    @Override
    protected void onLongPress() {
        if (delegate != null) {
            delegate.didLongPressed(this);
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

        Drawable currentBackgroundDrawable = null;
        if (currentMessageObject.isOut()) {
            if (isPressed() && isCheckPressed || !isCheckPressed && isPressed) {
                if (!media) {
                    currentBackgroundDrawable = getBackgroundDrawableOutSelected();
                } else {
                    currentBackgroundDrawable = backgroundMediaDrawableOutSelected;
                }
            } else {
                if (!media) {
                    currentBackgroundDrawable = getBackgroundDrawableOut();
                } else {
                    currentBackgroundDrawable = backgroundMediaDrawableOut;
                }
            }
            setDrawableBounds(currentBackgroundDrawable, layoutWidth - backgroundWidth - (!media ? 0 : dp(9)), dp(1), backgroundWidth, layoutHeight - dp(2));
        } else {
            if (isPressed() && isCheckPressed || !isCheckPressed && isPressed) {
                if (!media) {
                    currentBackgroundDrawable = getBackgroundDrawableInSelected();
                } else {
                    currentBackgroundDrawable = backgroundMediaDrawableInSelected;
                }
            } else {
                if (!media) {
                    currentBackgroundDrawable = getBackgroundDrawableIn();
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
        if (drawBackground) {
            currentBackgroundDrawable.draw(canvas);
        }

        onAfterBackgroundDraw(canvas);

        if (drawName && nameLayout != null) {
            canvas.save();
            canvas.translate(currentBackgroundDrawable.getBounds().left + dp(19) - nameOffsetX, dp(10));
            getNamePaint().setColor(AvatarDrawable.getNameColorForId(currentUser.id));
            nameLayout.draw(canvas);
            canvas.restore();
        }

        if (drawForwardedName && forwardedNameLayout != null) {
            canvas.save();
            if (currentMessageObject.isOut()) {
                getForwardNamePaint().setColor(0xff4a923c);
                forwardNameX = currentBackgroundDrawable.getBounds().left + dp(10);
                forwardNameY = dp(10 + (drawName ? 18 : 0));
            } else {
                getForwardNamePaint().setColor(0xff006fc8);
                forwardNameX = currentBackgroundDrawable.getBounds().left + dp(19);
                forwardNameY = dp(10 + (drawName ? 18 : 0));
            }
            canvas.translate(forwardNameX - forwardNameOffsetX, forwardNameY);
            forwardedNameLayout.draw(canvas);
            canvas.restore();
        }

        if (drawTime) {
            if (media) {
                setDrawableBounds(mediaBackgroundDrawable, timeX - dp(3), layoutHeight - dp(27.5f), timeWidth + dp(6 + (currentMessageObject.isOut() ? 20 : 0)), dp(16.5f));
                mediaBackgroundDrawable.draw(canvas);

                canvas.save();
                canvas.translate(timeX, layoutHeight - dp(12.0f) - timeLayout.getHeight());
                timeLayout.draw(canvas);
                canvas.restore();
            } else {
                canvas.save();
                canvas.translate(timeX, layoutHeight - dp(6.5f) - timeLayout.getHeight());
                timeLayout.draw(canvas);
                canvas.restore();
            }

            if (currentMessageObject.isOut()) {
                boolean drawCheck1 = false;
                boolean drawCheck2 = false;
                boolean drawClock = false;
                boolean drawError = false;
                boolean isBroadcast = (int)(currentMessageObject.getDialogId() >> 32) == 1;

                if (currentMessageObject.isSending()) {
                    drawCheck1 = false;
                    drawCheck2 = false;
                    drawClock = true;
                    drawError = false;
                } else if (currentMessageObject.isSendError()) {
                    drawCheck1 = false;
                    drawCheck2 = false;
                    drawClock = false;
                    drawError = true;
                } else if (currentMessageObject.isSent()) {
                    if (!currentMessageObject.isUnread()) {
                        drawCheck1 = true;
                        drawCheck2 = true;
                    } else {
                        drawCheck1 = false;
                        drawCheck2 = true;
                    }
                    drawClock = false;
                    drawError = false;
                }

                if (drawClock) {
                    if (!media) {
                        setDrawableBounds(getClockDrawable(), layoutWidth - dp(18.5f) - getClockDrawable().getIntrinsicWidth(), layoutHeight - dp(8.5f) - getClockDrawable().getIntrinsicHeight());
                        getClockDrawable().draw(canvas);
                    } else {
                        setDrawableBounds(getClockMediaDrawable(), layoutWidth - dp(22.0f) - getClockMediaDrawable().getIntrinsicWidth(), layoutHeight - dp(13.0f) - getClockMediaDrawable().getIntrinsicHeight());
                        getClockMediaDrawable().draw(canvas);
                    }
                }
                if (isBroadcast) {
                    if (drawCheck1 || drawCheck2) {
                        if (!media) {
                            setDrawableBounds(getBroadcastDrawable(), layoutWidth - dp(20.5f) - getBroadcastDrawable().getIntrinsicWidth(), layoutHeight - dp(8.0f) - getBroadcastDrawable().getIntrinsicHeight());
                            getBroadcastDrawable().draw(canvas);
                        } else {
                            setDrawableBounds(getBroadcastMediaDrawable(), layoutWidth - dp(24.0f) - getBroadcastMediaDrawable().getIntrinsicWidth(), layoutHeight - dp(13.0f) - getBroadcastMediaDrawable().getIntrinsicHeight());
                            getBroadcastMediaDrawable().draw(canvas);
                        }
                    }
                } else {
                    if (drawCheck2) {
                        if (!media) {
                            if (drawCheck1) {
                                setDrawableBounds(getCheckDrawable(), layoutWidth - dp(22.5f) - getCheckDrawable().getIntrinsicWidth(), layoutHeight - dp(8.5f) - getCheckDrawable().getIntrinsicHeight());
                            } else {
                                setDrawableBounds(getCheckDrawable(), layoutWidth - dp(18.5f) - getCheckDrawable().getIntrinsicWidth(), layoutHeight - dp(8.5f) - getCheckDrawable().getIntrinsicHeight());
                            }
                            getCheckDrawable().draw(canvas);
                        } else {
                            if (drawCheck1) {
                                setDrawableBounds(getCheckMediaDrawable(), layoutWidth - dp(26.0f) - getCheckMediaDrawable().getIntrinsicWidth(), layoutHeight - dp(13.0f) - getCheckMediaDrawable().getIntrinsicHeight());
                            } else {
                                setDrawableBounds(getCheckMediaDrawable(), layoutWidth - dp(22.0f) - getCheckMediaDrawable().getIntrinsicWidth(), layoutHeight - dp(13.0f) - getCheckMediaDrawable().getIntrinsicHeight());
                            }
                            getCheckMediaDrawable().draw(canvas);
                        }
                    }
                    if (drawCheck1) {
                        if (!media) {
                            setDrawableBounds(getHalfCheckDrawable(), layoutWidth - dp(18) - getHalfCheckDrawable().getIntrinsicWidth(), layoutHeight - dp(8.5f) - getHalfCheckDrawable().getIntrinsicHeight());
                            getHalfCheckDrawable().draw(canvas);
                        } else {
                            setDrawableBounds(getHalfCheckMediaDrawable(), layoutWidth - dp(20.5f) - getHalfCheckMediaDrawable().getIntrinsicWidth(), layoutHeight - dp(13.0f) - getHalfCheckMediaDrawable().getIntrinsicHeight());
                            getHalfCheckMediaDrawable().draw(canvas);
                        }
                    }
                }
                if (drawError) {
                    if (!media) {
                        setDrawableBounds(getErrorDrawable(), layoutWidth - dp(18) - getErrorDrawable().getIntrinsicWidth(), layoutHeight - dp(6.5f) - getErrorDrawable().getIntrinsicHeight());
                        getErrorDrawable().draw(canvas);
                    } else {
                        setDrawableBounds(getErrorDrawable(), layoutWidth - dp(20.5f) - getErrorDrawable().getIntrinsicWidth(), layoutHeight - dp(12.5f) - getErrorDrawable().getIntrinsicHeight());
                        getErrorDrawable().draw(canvas);
                    }
                }
            }
        }
    }
}
