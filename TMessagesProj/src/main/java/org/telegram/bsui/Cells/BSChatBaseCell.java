package org.telegram.bsui.Cells;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;

import org.telegram.android.ContactsController;
import org.telegram.android.LocaleController;
import org.telegram.android.MessageObject;
import org.telegram.android.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;

/**
 * Created by fanticqq on 20.03.15.
 */
public class BSChatBaseCell extends BSBaseCell {

    private boolean forwardNamePressed = false;

    public static interface ChatBaseCellDelegate {
        public abstract void didPressedUserAvatar(BSChatBaseCell cell, TLRPC.User user);
        public abstract void didPressedCancelSendButton(BSChatBaseCell cell);
        public abstract void didLongPressed(BSChatBaseCell cell);
        public abstract boolean canPerformActions();
    }

    public boolean isChat = false;
    protected boolean isPressed = false;
    protected boolean isCheckPressed = true;
    protected boolean forwardName = false;
    protected boolean media = false;
    protected boolean drawBackground = true;
    protected MessageObject currentMessageObject;
    private boolean wasLayout = false;

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
    private static Drawable clockMediaDrawable;
    private static Drawable broadcastMediaDrawable;
    private static Drawable errorDrawable;
    private static TextPaint timePaintIn;
    private static TextPaint timePaintOut;
    private static TextPaint timeMediaPaint;
    private static TextPaint namePaint;
    private static TextPaint forwardNamePaint;

    protected int backgroundWidth = 100;

    protected int layoutWidth;
    protected int layoutHeight;

    private StaticLayout nameLayout;
    protected int nameWidth;
    private float nameOffsetX = 0;
    protected boolean drawName = false;

    private StaticLayout forwardedNameLayout;
    private StaticLayout forwardedTitleLayout;
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
    private TLRPC.User currentForwardUser;
    private String currentForwardNameString;
    private String currentNameString;

    protected ChatBaseCellDelegate delegate;

    protected int namesOffset = 0;
    protected int timeOffset = 0;
    protected int forwardedOffset = 0;

    private int last_send_state = 0;
    private int last_delete_date = 0;

    public BSChatBaseCell(Context context) {
        super(context);
        if (backgroundDrawableIn == null) {
            backgroundDrawableIn = getResources().getDrawable(R.drawable.msg_in_bs);
            backgroundDrawableInSelected = getResources().getDrawable(R.drawable.msg_in_selected_bs);
            backgroundDrawableOut = getResources().getDrawable(R.drawable.msg_out_bs);
            backgroundDrawableOutSelected = getResources().getDrawable(R.drawable.msg_out_selected_bs);
            backgroundMediaDrawableIn = getResources().getDrawable(android.R.color.white);
            backgroundMediaDrawableInSelected = getResources().getDrawable(android.R.color.darker_gray);
            backgroundMediaDrawableOut = getResources().getDrawable(android.R.color.white);
            backgroundMediaDrawableOutSelected = getResources().getDrawable(android.R.color.darker_gray);

            clockDrawable = getResources().getDrawable(R.drawable.msg_clock_bs);
            clockMediaDrawable = getResources().getDrawable(R.drawable.msg_clock_bs);
            errorDrawable = getResources().getDrawable(R.drawable.alert_bs);
            broadcastDrawable = getResources().getDrawable(R.drawable.broadcast_bs);
            broadcastMediaDrawable = getResources().getDrawable(R.drawable.broadcast_bs);

            timePaintIn = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaintIn.setTextSize(dp(14));
            timePaintIn.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            timePaintIn.setColor(0xff000000);

            timePaintOut = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaintOut.setTextSize(dp(14));
            timePaintOut.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            timePaintOut.setColor(0xff000000);

            timeMediaPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timeMediaPaint.setTextSize(dp(14));
            timeMediaPaint.setColor(0xffffffff);

            namePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            namePaint.setTextSize(dp(18));
            namePaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            namePaint.setColor(0xff000000);

            forwardNamePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            forwardNamePaint.setTextSize(dp(18));

            checkDrawable = getResources().getDrawable(R.drawable.msg_check_bs);
            halfCheckDrawable = getResources().getDrawable(R.drawable.msg_check_bs);
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
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
        wasLayout = false;

        currentUser = MessagesController.getInstance().getUser(messageObject.messageOwner.from_id);

        if (!media) {
            if (currentMessageObject.isOut()) {
                currentTimePaint = timePaintOut;
            } else {
                currentTimePaint = timePaintIn;
            }
        } else {
            currentTimePaint = timePaintOut;
        }

        currentTimeString = LocaleController.formatterDay.format((long) (currentMessageObject.messageOwner.date) * 1000);
        currentTimeString = LocaleController.formatStringSimple(getResources().getString(R.string.SentMessageDate), currentTimeString);
        timeWidth = (int)Math.ceil(currentTimePaint.measureText(currentTimeString));

        namesOffset = 0;
        if(drawTime) {
            timeOffset = dp(25);
        }

        if (drawName && isChat && currentUser != null && !currentMessageObject.isOut()) {
            currentNameString = ContactsController.formatName(currentUser.first_name, currentUser.last_name);
            nameWidth = getMaxNameWidth();

            CharSequence nameStringFinal = TextUtils.ellipsize(currentNameString.replace("\n", " "), namePaint, nameWidth - dp(12), TextUtils.TruncateAt.END);
            nameLayout = new StaticLayout(nameStringFinal, namePaint, nameWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (nameLayout.getLineCount() > 0) {
                nameWidth = (int)Math.ceil(nameLayout.getLineWidth(0));
                namesOffset += dp(25);
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

                CharSequence str = TextUtils.ellipsize(currentForwardNameString.replace("\n", " "), forwardNamePaint, forwardedNameWidth - dp(40), TextUtils.TruncateAt.END);
                str = Html.fromHtml(String.format("%s<br>%s <b>%s</b>", LocaleController.getString("ForwardedMessage", R.string.ForwardedMessage), LocaleController.getString("From", R.string.From), str));
                forwardedNameLayout = new StaticLayout(str, forwardNamePaint, forwardedNameWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                if (forwardedNameLayout.getLineCount() > 1) {
                    forwardedNameWidth = Math.max((int) Math.ceil(forwardedNameLayout.getLineWidth(0)), (int) Math.ceil(forwardedNameLayout.getLineWidth(1)));
                    namesOffset += dp(46);
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

    protected int getMaxNameWidth() {
        return backgroundWidth - dp(8);
    }

    public final MessageObject getMessageObject() {
        return currentMessageObject;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;
        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (delegate == null || delegate.canPerformActions()) {
                if (drawForwardedName && forwardedNameLayout != null) {
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
            if (forwardNamePressed) {
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
                    timeX = dp(20);
                } else {
                    timeX = layoutWidth - timeWidth - dp(38.5f);
                }
            } else {
                if (!currentMessageObject.isOut()) {
                    timeX = dp(20);
                } else {
                    timeX = layoutWidth - timeWidth - dp(42.0f);
                }
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

        Drawable currentBackgroundDrawable = null;
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
            setDrawableBounds(currentBackgroundDrawable, layoutWidth - backgroundWidth - (!media ? 0 : dp(9)), dp(1), backgroundWidth, layoutHeight - dp(2) - timeOffset);
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
            setDrawableBounds(currentBackgroundDrawable, (!media ? 0 : dp(9)), dp(1), backgroundWidth, layoutHeight - dp(2) - timeOffset);
        }
        if (drawBackground) {
            currentBackgroundDrawable.draw(canvas);
        }

        onAfterBackgroundDraw(canvas);

        if (drawName && nameLayout != null) {
            canvas.save();
            canvas.translate(currentBackgroundDrawable.getBounds().left + dp(19) - nameOffsetX, + dp(10));
            namePaint.setColor(0xff000000);
            nameLayout.draw(canvas);
            canvas.restore();
        }

        if (drawForwardedName && forwardedNameLayout != null) {
            canvas.save();
            if (currentMessageObject.isOut()) {
                forwardNamePaint.setColor(0xff000000);
                forwardNameX = currentBackgroundDrawable.getBounds().left + dp(10);
                forwardNameY = dp(10 + (drawName ? 18 : 0));
            } else {
                forwardNamePaint.setColor(0xff000000);
                forwardNameX = currentBackgroundDrawable.getBounds().left + dp(19);
                forwardNameY = dp(10 + (drawName ? 18 : 0));
            }
            canvas.translate(forwardNameX - forwardNameOffsetX, forwardNameY);
            forwardedNameLayout.draw(canvas);
            canvas.restore();
        }

        if (drawTime) {
                canvas.save();
                canvas.translate(timeX, layoutHeight - (timeLayout.getHeight() + dp(6.5f)));
                timeLayout.draw(canvas);
                canvas.restore();

            if (currentMessageObject.isOut()) {
                boolean drawCheck1 = false;
                boolean drawCheck2 = false;
                boolean drawClock = false;
                boolean drawError = false;
                boolean isBroadcast = (int) (currentMessageObject.getDialogId() >> 32) == 1;

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
                        setDrawableBounds(clockDrawable, layoutWidth - dp(18.5f) - clockDrawable.getIntrinsicWidth(), layoutHeight - dp(8.5f) - clockDrawable.getIntrinsicHeight());
                        clockDrawable.draw(canvas);
                    } else {
                        setDrawableBounds(clockDrawable, layoutWidth - dp(22.0f) - clockDrawable.getIntrinsicWidth(), layoutHeight - dp(13.0f) - clockDrawable.getIntrinsicHeight());
                        clockDrawable.draw(canvas);
                    }
                }
                if (isBroadcast) {
                    if (drawCheck1 || drawCheck2) {
                        if (!media) {
                            setDrawableBounds(broadcastDrawable, layoutWidth - dp(20.5f) - broadcastDrawable.getIntrinsicWidth(), layoutHeight - dp(8.0f) - broadcastDrawable.getIntrinsicHeight());
                            broadcastDrawable.draw(canvas);
                        } else {
                            setDrawableBounds(broadcastDrawable, layoutWidth - dp(24.0f) - broadcastDrawable.getIntrinsicWidth(), layoutHeight - dp(13.0f) - broadcastDrawable.getIntrinsicHeight());
                            broadcastDrawable.draw(canvas);
                        }
                    }
                } else {
                    if (drawCheck2) {
                        if (drawCheck1) {
                            final int x = layoutWidth - dp(22.5f) - checkDrawable.getIntrinsicWidth();
                            final int y = layoutHeight - dp(8.5f) - checkDrawable.getIntrinsicHeight();
                            setDrawableBounds(checkDrawable, x, y);
                        } else {
                            setDrawableBounds(checkDrawable, layoutWidth - dp(18.5f) - checkDrawable.getIntrinsicWidth(), layoutHeight - dp(8.5f) - checkDrawable.getIntrinsicHeight());
                        }
                        checkDrawable.draw(canvas);
                    }
                    if (drawCheck1) {
                        setDrawableBounds(halfCheckDrawable, layoutWidth - dp(18) - checkDrawable.getIntrinsicWidth(), layoutHeight - dp(8.5f) - checkDrawable.getIntrinsicHeight());
                        halfCheckDrawable.draw(canvas);
                    }
                }
                if (drawError) {
                    if (!media) {
                        setDrawableBounds(errorDrawable, layoutWidth - dp(18) - errorDrawable.getIntrinsicWidth(), layoutHeight - dp(6.5f) - errorDrawable.getIntrinsicHeight());
                        errorDrawable.draw(canvas);
                    } else {
                        setDrawableBounds(errorDrawable, layoutWidth - dp(20.5f) - errorDrawable.getIntrinsicWidth(), layoutHeight - dp(12.5f) - errorDrawable.getIntrinsicHeight());
                        errorDrawable.draw(canvas);
                    }
                }
            }
        }
    }
}
