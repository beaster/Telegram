package org.telegram.bsui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
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
import org.telegram.android.ImageReceiver;
import org.telegram.android.LocaleController;
import org.telegram.android.MessageObject;
import org.telegram.android.MessagesController;
import org.telegram.bsui.BSAvatarDrawable;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Cells.ChatContactCell;
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

    private ImageReceiver avatarImage;
    private BSAvatarDrawable avatarDrawable;

    private StaticLayout nameLayout;
    private StaticLayout phoneLayout;

    private TLRPC.User contactUser;
    private TLRPC.FileLocation currentPhoto;

    private boolean avatarPressed = false;
    private boolean buttonPressed = false;
    private boolean drawAddButton = false;
    private int namesWidth = 0;

    private ChatContactCellDelegate contactDelegate = null;

    protected TextPaint getNamePaint() {
        return namePaint;
    }
    protected TextPaint getPhonePaint() {
        return phonePaint;
    }
    protected Drawable getAddContactDrawableIn() {
        return addContactDrawableIn;
    }
    protected Drawable getAddContactDrawableOut() {
        return addContactDrawableOut;
    }

    public BSChatContactCell(Context context) {
        super(context);
        initNamePaint();
        avatarImage = new ImageReceiver(this);
        avatarImage.setRoundRadius(dp(21));
        avatarDrawable = new BSAvatarDrawable();
    }

    protected void initNamePaint() {
        if (namePaint == null) {
            namePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            namePaint.setTextSize(dp(15));

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

        TLRPC.FileLocation newPhoto = null;
        if (contactUser != null && contactUser.photo != null) {
            newPhoto = contactUser.photo.photo_small;
        }

        return currentPhoto == null && newPhoto != null || currentPhoto != null && newPhoto == null || currentPhoto != null && newPhoto != null && (currentPhoto.local_id != newPhoto.local_id || currentPhoto.volume_id != newPhoto.volume_id) || super.isUserDataChanged();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        boolean result = false;
        int side = dp(36);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (x >= avatarImage.getImageX() && x <= avatarImage.getImageX() + namesWidth + dp(42) && y >= avatarImage.getImageY() && y <= avatarImage.getImageY() + avatarImage.getImageHeight()) {
                avatarPressed = true;
                result = true;
            } else if (x >= avatarImage.getImageX() + namesWidth + dp(52) && y >= dp(13) && x <= avatarImage.getImageX() + namesWidth + dp(92) && y <= dp(52)) {
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
            if (avatarPressed) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    avatarPressed = false;
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
                    avatarPressed = false;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!(x >= avatarImage.getImageX() && x <= avatarImage.getImageX() + namesWidth + dp(42) && y >= avatarImage.getImageY() && y <= avatarImage.getImageY() + avatarImage.getImageHeight())) {
                        avatarPressed = false;
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
                    if (!(x >= avatarImage.getImageX() + namesWidth + dp(52) && y >= dp(13) && x <= avatarImage.getImageX() + namesWidth + dp(92) && y <= dp(52))) {
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

            if (contactUser != null) {
                if (contactUser.photo != null) {
                    currentPhoto = contactUser.photo.photo_small;
                } else {
                    currentPhoto = null;
                }
                avatarDrawable.setInfo(contactUser);
            } else {
                currentPhoto = null;
                avatarDrawable.setInfo(uid, null, null, false);
            }
            avatarImage.setImage(currentPhoto, "50_50", avatarDrawable, false);

            String currentNameString = ContactsController.formatName(messageObject.messageOwner.media.first_name, messageObject.messageOwner.media.last_name);
            int nameWidth = Math.min((int) Math.ceil(getNamePaint().measureText(currentNameString)), maxWidth);

            CharSequence stringFinal = TextUtils.ellipsize(currentNameString.replace("\n", " "), getNamePaint(), nameWidth, TextUtils.TruncateAt.END);
            nameLayout = new StaticLayout(stringFinal, getNamePaint(), nameWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
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
            int phoneWidth = Math.min((int) Math.ceil(getPhonePaint().measureText(phone)), maxWidth);
            stringFinal = TextUtils.ellipsize(phone.replace("\n", " "), getPhonePaint(), phoneWidth, TextUtils.TruncateAt.END);
            phoneLayout = new StaticLayout(stringFinal, getPhonePaint(), phoneWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
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
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), dp(71));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (currentMessageObject == null) {
            return;
        }

        int x;

        if (currentMessageObject.isOut()) {
            x = layoutWidth - backgroundWidth + dp(8);
        } else {
            if (isChat) {
                x = dp(69);
            } else {
                x = dp(16);
            }
        }
        avatarImage.setImageCoords(x, dp(9), dp(42), dp(42));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentMessageObject == null) {
            return;
        }

        avatarImage.draw(canvas);

        if (nameLayout != null) {
            canvas.save();
            canvas.translate(avatarImage.getImageX() + avatarImage.getImageWidth() + dp(9), dp(10));
            getNamePaint().setColor(AvatarDrawable.getColorForId(currentMessageObject.messageOwner.media.user_id));
            nameLayout.draw(canvas);
            canvas.restore();
        }
        if (phoneLayout != null) {
            canvas.save();
            canvas.translate(avatarImage.getImageX() + avatarImage.getImageWidth() + dp(9), dp(31));
            phoneLayout.draw(canvas);
            canvas.restore();
        }

        if (drawAddButton) {
            Drawable addContactDrawable;
            if (currentMessageObject.isOut()) {
                addContactDrawable = getAddContactDrawableOut();
            } else {
                addContactDrawable = getAddContactDrawableIn();
            }
            setDrawableBounds(addContactDrawable, avatarImage.getImageX() + namesWidth + dp(78), dp(13));
            addContactDrawable.draw(canvas);
        }
    }
}
