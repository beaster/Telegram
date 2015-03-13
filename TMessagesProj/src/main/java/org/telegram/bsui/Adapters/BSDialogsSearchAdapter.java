package org.telegram.bsui.Adapters;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;

import org.telegram.android.LocaleController;
import org.telegram.android.MessageObject;
import org.telegram.android.MessagesController;
import org.telegram.bsui.Cells.BSDialogCell;
import org.telegram.bsui.Cells.BSGreySectionCell;
import org.telegram.bsui.Cells.BSLoadingCell;
import org.telegram.bsui.Cells.BSProfileSearchCell;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.ui.Adapters.DialogsSearchAdapter;

/**
 * Created by Ji on 19.01.2015.
 */
public class BSDialogsSearchAdapter extends DialogsSearchAdapter {
    public BSDialogsSearchAdapter(Context context, boolean messagesSearch) {
        super(context, messagesSearch);
    }

    private BSMessagesActivitySearchAdapterDelegate delegate;
    public static interface BSMessagesActivitySearchAdapterDelegate {
        public abstract void searchStateChanged(boolean search);
    }

    public void setDelegate(BSMessagesActivitySearchAdapterDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        int type = getItemViewType(i);

        if (type == 1) {
            if (view == null) {
                view = new BSGreySectionCell(mContext);
            }
            if (!globalSearch.isEmpty() && i == searchResult.size()) {
                ((BSGreySectionCell) view).setText(LocaleController.getString("GlobalSearch", R.string.GlobalSearch));
            } else {
                ((BSGreySectionCell) view).setText(LocaleController.getString("SearchMessages", R.string.SearchMessages));
            }
        } else if (type == 0) {
            if (view == null) {
                view = new BSProfileSearchCell(mContext);
            }

            TLRPC.User user = null;
            TLRPC.Chat chat = null;
            TLRPC.EncryptedChat encryptedChat = null;

            int localCount = searchResult.size();
            int globalCount = globalSearch.isEmpty() ? 0 : globalSearch.size() + 1;

            ((BSProfileSearchCell) view).useSeparator = (i != getCount() - 1 && i != localCount - 1 && i != localCount + globalCount - 1);
            Object obj = getItem(i);
            if (obj instanceof TLRPC.User) {
                user = MessagesController.getInstance().getUser(((TLRPC.User) obj).id);
                if (user == null) {
                    user = (TLRPC.User) obj;
                }
            } else if (obj instanceof TLRPC.Chat) {
                chat = MessagesController.getInstance().getChat(((TLRPC.Chat) obj).id);
            } else if (obj instanceof TLRPC.EncryptedChat) {
                encryptedChat = MessagesController.getInstance().getEncryptedChat(((TLRPC.EncryptedChat) obj).id);
                user = MessagesController.getInstance().getUser(encryptedChat.user_id);
            }

            CharSequence username = null;
            CharSequence name = null;
            if (i < searchResult.size()) {
                name = searchResultNames.get(i);
                if (name != null && user != null && user.username != null && user.username.length() > 0) {
                    if (name.toString().startsWith("@" + user.username)) {
                        username = name;
                        name = null;
                    }
                }
            } else if (i > searchResult.size() && user != null && user.username != null) {
                try {
                    username = Html.fromHtml(String.format("<font color=\"#4d83b3\">@%s</font>%s", user.username.substring(0, lastFoundUsername.length()), user.username.substring(lastFoundUsername.length())));
                } catch (Exception e) {
                    username = user.username;
                    FileLog.e("tmessages", e);
                }
            }

            ((BSProfileSearchCell) view).setData(user, chat, encryptedChat, name, username);
        } else if (type == 2) {
            if (view == null) {
                view = new BSDialogCell(mContext);
            }
            ((BSDialogCell) view).useSeparator = (i != getCount() - 1);
            MessageObject messageObject = (MessageObject)getItem(i);
            ((BSDialogCell) view).setDialog(messageObject.getDialogId(), messageObject, false, messageObject.messageOwner.date, 0);
        } else if (type == 3) {
            if (view == null) {
                view = new BSLoadingCell(mContext);
            }
        }

        return view;
    }
}
