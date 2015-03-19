package org.telegram.bsui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import org.telegram.android.ImageReceiver;
import org.telegram.android.LocaleController;
import org.telegram.android.MessageObject;
import org.telegram.android.MessagesController;
import org.telegram.bsui.BSAvatarDrawable;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;

import java.util.Vector;

/**
 * Created by W on 3/4/2015.
 */
public class BSTelegramWidgetMessages {
    private static final String LOG_TAG = "BSTGWidgetMessages";

    public static final int PERSONAL_CHAT = 1;
    public static final int GROUP_CHAT = 2;
    public static final int SECRET_CHAT = 3;

    private static BSTelegramWidgetMessages mInstance;
    private Context mContext;

    private MessageObject[] mCurrentMessages;
    private int[] mCount;
    private String[] mUserName;
    private TLRPC.User[] mUser;

    private Vector<MessageObject> mAllUnreadMessages;
    private Vector<Integer> mUnreadMessagesCount;
    private Vector<TLRPC.TL_dialog> mDialog;

    private boolean mIsDialogsLoaded;

    public static BSTelegramWidgetMessages getInstance(Context context) {
        if (BSTelegramWidgetMessages.mInstance == null) {
            BSTelegramWidgetMessages.mInstance = new BSTelegramWidgetMessages(context);
        }

        return BSTelegramWidgetMessages.mInstance;
    }

    private BSTelegramWidgetMessages(Context context) {
        Log.d(LOG_TAG, ".ctor");
        this.mContext = context;
        this.mAllUnreadMessages = new Vector<>();
        this.mUnreadMessagesCount = new Vector<>();
        this.mDialog = new Vector<>();
        this.mIsDialogsLoaded = false;
    }

    public long getTime(int messageIndex) {
        return (long) this.mCurrentMessages[messageIndex].messageOwner.date;
    }

    public String getText(int messageIndex) {
        return LocaleController.formatStringSimple("<b>%s:</b> %s", this.mUserName[messageIndex], this.getMessageText(messageIndex));
    }

    public String getMessageText(int messageIndex) {
        return this.mCurrentMessages[messageIndex].messageText.toString();
    }

    public int getCount(int messageIndex) {
        return this.mCount[messageIndex];
    }

    public String getUserName(int messageIndex) {
        return this.mUserName[messageIndex];
    }

    public boolean isDialogsLoaded() {
        return this.mIsDialogsLoaded;
    }

    public void setDialogsLoaded(boolean isDialogsLoaded) {
        this.mIsDialogsLoaded = isDialogsLoaded;
    }

    public int getChatType(int messageIndex) {
        return this.getChatType(this.mDialog.get(messageIndex).id);
    }

    public Long getDialogId(int messageIndex) {
        return this.mDialog.get(messageIndex).id;
    }

    public Bitmap getAvatar(int messageIndex) {
        TLRPC.User user = this.mUser[messageIndex];
        TLRPC.TL_dialog dialog = this.mDialog.get(messageIndex);
        BSAvatarDrawable bsAvatarDrawable = new BSAvatarDrawable();

        TLRPC.FileLocation photo;
        if (this.getChatType(messageIndex) == BSTelegramWidgetMessages.GROUP_CHAT) {
            MessagesController messagesController = MessagesController.getInstance();

            int chatId = 0;
            int lower_part = (int) dialog.id;
            int high_id = (int) (dialog.id >> 32);
            if (lower_part != 0) {
                if (high_id == 1) {
                    chatId = lower_part;
                } else {
                    if (lower_part < 0) {
                        chatId = -lower_part;
                    }
                }
            }
            TLRPC.Chat chat = messagesController.getChat(chatId);

            photo = chat.photo.photo_big;

            if (photo == null)
                bsAvatarDrawable.setInfo(chat);
        } else {
            photo = user.photo.photo_big;
            if (photo == null)
                bsAvatarDrawable.setInfo(user);
        }

        ImageReceiver imageReceiver = new ImageReceiver();
        imageReceiver.setForcePreview(photo == null);
        imageReceiver.setImageCoords(0, 0, 100, 100);
        imageReceiver.setRoundRadius(50);
        imageReceiver.setImage(photo, "50_50", bsAvatarDrawable, false);
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        imageReceiver.draw(canvas);

        return bitmap;
    }

    public Bitmap getDemoAvatar(String firstName, String lastName) {
        BSAvatarDrawable bsAvatarDrawable = new BSAvatarDrawable();
        bsAvatarDrawable.setInfo(11, firstName, lastName, false);
        ImageReceiver imageReceiver = new ImageReceiver();
        imageReceiver.setForcePreview(true);
        imageReceiver.setImageCoords(0, 0, 100, 100);
        imageReceiver.setRoundRadius(50);
        imageReceiver.setImage(null, "50_50", bsAvatarDrawable, false);
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        imageReceiver.draw(canvas);

        return bitmap;
    }

    public void clear() {
        this.mUnreadMessagesCount.clear();
        this.mAllUnreadMessages.clear();
        this.mDialog.clear();
    }

    public int updateMessages(int maxCount) {
        int count = Math.min(this.mAllUnreadMessages.size(), maxCount);

        MessageObject[] tempCurrentMessages = new MessageObject[count];
        int[] tempCount = new int[count];
        String[] tempUserName = new String[count];
        TLRPC.User[] tempUser = new TLRPC.User[count];

        String temp;
        MessagesController messagesController = MessagesController.getInstance();

        for (int i = 0; i < count; i++) {
            tempCurrentMessages[i] = this.mAllUnreadMessages.get(i);

            tempCount[i] = count == 1 ?
                    this.sumUnreadMessagesCount() :
                    this.mUnreadMessagesCount.get(i);

            tempUser[i] = messagesController.getUser(tempCurrentMessages[i].messageOwner.from_id);

            temp = "";
            if (tempUser[i].first_name != null) {
                temp += tempUser[i].first_name + " " + tempUser[i].last_name;
            } else {
                temp += tempUser[i].phone;
            }
            tempUserName[i] = temp;
        }

        this.mCurrentMessages = tempCurrentMessages;
        this.mCount = tempCount;
        this.mUserName = tempUserName;
        this.mUser = tempUser;

        return count;
    }

    public int sumUnreadMessagesCount() {
        int sum = 0;
        for (int value : this.mUnreadMessagesCount) {
            sum += value;
        }
        return sum;
    }

    public void reloadDialogs() {
        Log.d(LOG_TAG, "reloadDialogs");
        Vector<MessageObject> allUnreadMessages = new Vector<>();
        Vector<Integer> unreadMessagesCount = new Vector<>();
        Vector<TLRPC.TL_dialog> dialogs = new Vector<>();

        MessagesController messagesController = MessagesController.getInstance();

        for (TLRPC.TL_dialog dialog : messagesController.dialogs) {
            if (dialog.unread_count != 0) {
                MessageObject message = messagesController.dialogMessage.get(dialog.top_message);
                if (!message.isFromMe()) {
                    allUnreadMessages.add(message);
                    unreadMessagesCount.add(dialog.unread_count);
                    dialogs.add(dialog);
//                    Collections.reverse(allUnreadMessages);
                }
            }
        }

        this.mAllUnreadMessages = allUnreadMessages;
        this.mUnreadMessagesCount = unreadMessagesCount;
        this.mDialog = dialogs;
    }

    private int getChatType(long dialogId) {
        int chatType = BSTelegramWidgetMessages.PERSONAL_CHAT;

        int chatId = 0;
        int encId = 0;

        int lower_part = (int) dialogId;
        int high_id = (int) (dialogId >> 32);
        if (lower_part != 0) {
            if (high_id == 1) {
                chatId = lower_part;
            } else {
                if (lower_part < 0) {
                    chatId = -lower_part;
                }
            }
        } else {
            encId = high_id;
        }
        if (chatId != 0) {
            if (chatId > 0) {
                chatType = BSTelegramWidgetMessages.GROUP_CHAT;
            }
        } else if (encId != 0) {
            chatType = BSTelegramWidgetMessages.SECRET_CHAT;
        }
        return chatType;
    }

    public String getChatTypeName(int chatType) {

        switch (chatType) {
            case BSTelegramWidgetMessages.PERSONAL_CHAT:
                return this.mContext.getString(R.string.ChatTypePersonal);
            case BSTelegramWidgetMessages.GROUP_CHAT:
                return this.mContext.getString(R.string.ChatTypeGroup);
            case BSTelegramWidgetMessages.SECRET_CHAT:
                return this.mContext.getString(R.string.ChatTypeSecret);
            default:
                return null;
        }
    }

    public void loadDialogs() {
        Log.d(LOG_TAG, "loadDialogs");
        if (!this.mIsDialogsLoaded) {
            MessagesController.getInstance().loadDialogs(0, 0, 100, true);
            this.mIsDialogsLoaded = true;
        }
    }
}
