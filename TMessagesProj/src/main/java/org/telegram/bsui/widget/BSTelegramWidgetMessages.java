package org.telegram.bsui.widget;

import android.content.Context;
import android.util.Log;

import org.telegram.android.LocaleController;
import org.telegram.android.MessageObject;
import org.telegram.android.MessagesController;
import org.telegram.messenger.TLRPC;

import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by W on 3/4/2015.
 */
public class BSTelegramWidgetMessages {
    private static final String LOG_TAG = "BSTGWidgetMessages";

    private static BSTelegramWidgetMessages mInstance;
    private Context mContext;

    private MessageObject[] mCurrentMessages;
    private int[] mMessagesCount;
    private String[] mMessagesText;
    private String[] mMessagesUser;
    private long[] mMessagesTime;

    private Vector<MessageObject> mAllMessages;
    private HashMap<Long, Integer> mUnreadMessagesCount;

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
        this.mAllMessages = new Vector<>();
        this.mUnreadMessagesCount = new HashMap<>();
        this.mIsDialogsLoaded = false;
    }

    public long getMessageTime(int messageIndex) {
        return this.mMessagesTime[messageIndex];
    }

    public String getMessageText(int messageIndex) {
        return this.mMessagesText[messageIndex];
    }

    public int getMessageCount(int messageIndex) {
        return this.mMessagesCount[messageIndex];
    }

    public MessageObject getCurrentMessage(int messageIndex) {
        return this.mCurrentMessages[messageIndex];
    }

    public String getMessagesUser(int messageIndex) {
        return this.mMessagesUser[messageIndex];
    }

    public boolean isDialogsLoaded() {
        return this.mIsDialogsLoaded;
    }

    public void setDialogsLoaded(boolean isDialogsLoaded) {
        this.mIsDialogsLoaded = isDialogsLoaded;
    }

    public Integer getUnreadMessagesCount(Long dialogId) {
        return this.mUnreadMessagesCount.get(dialogId);
    }

    public void clear() {
        this.mUnreadMessagesCount.clear();
        this.mAllMessages.clear();
    }

    public Vector<MessageObject> getAllMessages() {
        return mAllMessages;
    }

    public int updateMessages(int maxCount) {
        int count = Math.min(this.mAllMessages.size(), maxCount);

        this.mCurrentMessages = new MessageObject[count];
        this.mMessagesCount = new int[count];
        this.mMessagesText = new String[count];
        this.mMessagesTime = new long[count];
        this.mMessagesUser = new String[count];
        String temp;
        String userName;
        MessagesController messagesController = MessagesController.getInstance();
        TLRPC.User user;

        for (int i = 0; i < count; i++) {
            this.mCurrentMessages[i] = this.mAllMessages.get(this.mAllMessages.size() - 1 - i);
            this.mMessagesCount[i] = count == 1 ?
                    this.sumUnreadMessagesCount() :
                    this.mUnreadMessagesCount.get(this.mCurrentMessages[i].getDialogId());
            user = messagesController.getUser(this.mCurrentMessages[i].messageOwner.from_id);

            userName = "";
            if (user.first_name != null) {
                userName += user.first_name + " " + user.last_name;
            } else {
                userName += user.phone;
            }
            this.mMessagesUser[i] = userName;
            temp = LocaleController.formatStringSimple("<b>%s:</b> %s", userName, this.mCurrentMessages[i].messageText);
            this.mMessagesText[i] = temp;
            this.mMessagesTime[i] = (long) this.mCurrentMessages[i].messageOwner.date * 1000;
        }

        Log.d(LOG_TAG, "all " + this.mAllMessages.size());
        Log.d(LOG_TAG, "current " + this.mCurrentMessages.length);

        return count;
    }

    public int sumUnreadMessagesCount() {
        int sum = 0;
        for (int value : this.mUnreadMessagesCount.values()) {
            sum += value;
        }
        return sum;
    }

    public void reloadDialogs() {
        this.mAllMessages = new Vector<>();
        this.mUnreadMessagesCount = new HashMap<>();
        //mCount = 0;
        for (TLRPC.TL_dialog dialog : MessagesController.getInstance().dialogs) {
            if (dialog.unread_count == 0) {
                continue;
            }
            MessageObject message = MessagesController.getInstance().dialogMessage.get(dialog.top_message);
            if (!message.isFromMe()) {
                this.mAllMessages.add(message);
                this.mUnreadMessagesCount.put(dialog.id, dialog.unread_count);
                Collections.reverse(this.mAllMessages);
            }
        }
    }

    public void loadDialogs() {
        if (!this.mIsDialogsLoaded) {
            MessagesController.getInstance().loadDialogs(0, 0, 100, true);
            this.mIsDialogsLoaded = true;
        }
    }
}
