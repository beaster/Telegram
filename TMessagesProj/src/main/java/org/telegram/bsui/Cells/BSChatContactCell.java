package org.telegram.bsui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.android.AndroidUtilities;
import org.telegram.android.ContactsController;
import org.telegram.android.LocaleController;
import org.telegram.android.MessageObject;
import org.telegram.android.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Components.AvatarDrawable;

/**
 * Created by E1ektr0 on 04.01.2015.
 */
public class BSChatContactCell extends BSChatBaseCell {

    public static interface ChatContactCellDelegate {
        public abstract void didClickAddButton(BSChatContactCell cell, TLRPC.User user);
        public abstract void didClickPhone(BSChatContactCell cell);
    }

    private static TextPaint namePaint;
    private static TextPaint phonePaint;
    private static Drawable addContactDrawableIn;
    private static Drawable addContactDrawableOut;

    private StaticLayout nameLayout;
    private StaticLayout phoneLayout;

    private TLRPC.User contactUser;

    private boolean namePressed = false;
    private boolean buttonPressed = false;
    private boolean drawAddButton = false;
    private int namesWidth = 0;

    private ChatContactCellDelegate contactDelegate = null;

    public BSChatContactCell(Context context) {
        super(context);
        initNamePaint();
    }

    protected void initNamePaint() {
        if (namePaint == null) {
            namePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            namePaint.setTextSize(dp(15));
            namePaint.setColor(0xff000000);
            namePaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

            phonePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            phonePaint.setTextSize(dp(15));
            phonePaint.setColor(0xff000000);

            addContactDrawableIn = getResources().getDrawable(R.drawable.addcontact_bs);
            addContactDrawableOut = getResources().getDrawable(R.drawable.addcontact_bs);
        }
    }

    public void setContactDelegate(ChatContactCellDelegate delegate) {
        this.contactDelegate = delegate;
    }

    @Override
    protected boolean isUserDataChanged() {
        if (currentMessageObject == null) {
            return false;
        }

        int uid = currentMessageObject.messageOwner.media.user_id;
        boolean newDrawAdd = contactUser != null && uid != UserConfig.getClientUserId() && ContactsController.getInstance().contactsDict.get(uid) == null;
        if (newDrawAdd != drawAddButton) {
            return true;
        }

        contactUser = MessagesController.getInstance().getUser(currentMessageObject.messageOwner.media.user_id);

        return super.isUserDataChanged();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        boolean result = false;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (x >= getLeft() && x <= getLeft() + namesWidth + dp(42) && y >= getTop() && y <= getTop() + getBackground().getIntrinsicHeight()) {
                namePressed = true;
                result = true;
            } else if (x >= /*getLeft() */+ namesWidth + dp(52) && y >= dp(13) && x <= /*getLeft()*/ + namesWidth + dp(92) && y <= dp(52)) {
                buttonPressed = true;
                result = true;
            }
            if (result) {
                startCheckLongPress();
            }
        } else {
            if (event.getAction() != MotionEvent.ACTION_MOVE) {
                cancelCheckLongPress();
            }
            if (namePressed) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    namePressed = false;
                    playSoundEffect(SoundEffectConstants.CLICK);
                    if (contactUser != null) {
                        if (delegate != null) {
                            delegate.didPressedUserAvatar(this, contactUser);
                        }
                    } else {
                        if (contactDelegate != null) {
                            contactDelegate.didClickPhone(this);
                        }
                    }
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    namePressed = false;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!(x >= getLeft() && x <= getLeft() + namesWidth + dp(42) && y >= getTop() && y <= getTop() + getBackground().getIntrinsicHeight())) {
                        namePressed = false;
                    }
                }
            } else if (buttonPressed) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    buttonPressed = false;
                    playSoundEffect(SoundEffectConstants.CLICK);
                    if (contactUser != null && contactDelegate != null) {
                        contactDelegate.didClickAddButton(this, contactUser);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonPressed = false;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!(x >= getLeft() + namesWidth + dp(52) && y >= dp(13) && x <= getLeft() + namesWidth + dp(92) && y <= dp(52))) {
                        buttonPressed = false;
                    }
                }
            }
        }
        if (!result) {
            result = super.onTouchEvent(event);
        }

        return result;
    }

    @Override
    public void setMessageObject(MessageObject messageObject) {
        if (currentMessageObject != messageObject || isUserDataChanged()) {

            int uid = messageObject.messageOwner.media.user_id;
            contactUser = MessagesController.getInstance().getUser(uid);

            drawAddButton = contactUser != null && uid != UserConfig.getClientUserId() && ContactsController.getInstance().contactsDict.get(uid) == null;

            int maxWidth;
            if (AndroidUtilities.isTablet()) {
                maxWidth = (int) (AndroidUtilities.getMinTabletSide() * 0.7f);
            } else {
                maxWidth = (int) (Math.min(getDisplayX(), getDisplayY()) * 0.7f);
            }
            maxWidth -= dp(58 + (drawAddButton ? 42 : 0));

            String currentNameString = ContactsController.formatName(messageObject.messageOwner.media.first_name, messageObject.messageOwner.media.last_name);
            int nameWidth = Math.min((int) Math.ceil(namePaint.measureText(currentNameString)), maxWidth);

            CharSequence stringFinal = TextUtils.ellipsize(currentNameString.replace("\n", " "), namePaint, nameWidth, TextUtils.TruncateAt.END);
            nameLayout = new StaticLayout(stringFinal, namePaint, nameWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (nameLayout.getLineCount() > 0) {
                nameWidth = (int)Math.ceil(nameLayout.getLineWidth(0));
            } else {
                nameWidth = 0;
            }

            String phone = messageObject.messageOwner.media.phone_number;
            if (phone != null && phone.length() != 0) {
                if (!phone.startsWith("+")) {
                    phone = "+" + phone;
                }
                phone = PhoneFormat.getInstance().format(phone);
            } else {
                phone = LocaleController.getString("NumberUnknown", R.string.NumberUnknown);
            }
            int phoneWidth = Math.min((int) Math.ceil(phonePaint.measureText(phone)), maxWidth);
            stringFinal = TextUtils.ellipsize(phone.replace("\n", " "), phonePaint, phoneWidth, TextUtils.TruncateAt.END);
            phoneLayout = new StaticLayout(stringFinal, phonePaint, phoneWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (phoneLayout.getLineCount() > 0) {
                phoneWidth = (int)Math.ceil(phoneLayout.getLineWidth(0));
            } else {
                phoneWidth = 0;
            }

            namesWidth = Math.max(nameWidth, phoneWidth);
            backgroundWidth = dp(77 + (drawAddButton ? 42 : 0)) + namesWidth;

            super.setMessageObject(messageObject);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), dp(91));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (currentMessageObject == null) {
            return;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentMessageObject == null) {
            return;
        }


        if (nameLayout != null) {
            canvas.save();
            canvas.translate(getLeft() + dp(19), dp(10));
            nameLayout.draw(canvas);
            canvas.restore();
        }
        if (phoneLayout != null) {
            canvas.save();
            canvas.translate(getLeft() + dp(19), dp(31));
            phoneLayout.draw(canvas);
            canvas.restore();
        }

        if (drawAddButton) {
            Drawable addContactDrawable;
            if (currentMessageObject.isOut()) {
                addContactDrawable = addContactDrawableOut;
            } else {
                addContactDrawable = addContactDrawableIn;
            }
            setDrawableBounds(addContactDrawable, getLeft() + namesWidth + dp(78), dp(22));
            addContactDrawable.draw(canvas);
        }
    }
}
