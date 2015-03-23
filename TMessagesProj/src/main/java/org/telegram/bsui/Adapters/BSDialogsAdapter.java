package org.telegram.bsui.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.telegram.android.MessageObject;
import org.telegram.android.MessagesController;
import org.telegram.bsui.Cells.BSDialogCell;
import org.telegram.messenger.TLRPC;

/**
 * Created by Ji on 29.12.2014.
 */
public class BSDialogsAdapter extends BaseAdapter {

    private boolean serverOnly;
    private Context mContext;
    private long openedDialogId;

    public BSDialogsAdapter(Context context, boolean onlyFromServer){
        mContext = context;
        serverOnly = onlyFromServer;
    }

    public void setOpenedDialogId(long id) {
        openedDialogId = id;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public int getCount() {
        int count;
        if (serverOnly) {
            count = MessagesController.getInstance().dialogsServerOnly.size();
        } else {
            count = MessagesController.getInstance().dialogs.size();
        }
        if (count == 0 && MessagesController.getInstance().loadingDialogs) {
            return 0;
        }
        if (!MessagesController.getInstance().dialogsEndReached) {
            count++;
        }
        return count;
    }

    @Override
    public TLRPC.TL_dialog getItem(int i) {
        if (serverOnly) {
            if (i < 0 || i >= MessagesController.getInstance().dialogsServerOnly.size()) {
                return null;
            }
            return MessagesController.getInstance().dialogsServerOnly.get(i);
        } else {
            if (i < 0 || i >= MessagesController.getInstance().dialogs.size()) {
                return null;
            }
            return MessagesController.getInstance().dialogs.get(i);
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        int type = getItemViewType(i);

        if (type == 1) {
            view = new View(mContext);
        }

        if (type == 0) {
            if (view == null) {
                view = new BSDialogCell(mContext);
            }
            ((BSDialogCell) view).useSeparator = (i != 0);
            TLRPC.TL_dialog dialog = null;
            if (serverOnly) {
                dialog = MessagesController.getInstance().dialogsServerOnly.get(i);
            } else {
                dialog = MessagesController.getInstance().dialogs.get(i);
            }
            MessageObject message = MessagesController.getInstance().dialogMessage.get(dialog.top_message);
            ((BSDialogCell) view).setDialog(dialog.id, message, true, dialog.last_message_date, dialog.unread_count);
        }
        return view;
    }

    @Override
    public int getItemViewType(int i) {
        if (serverOnly && i == MessagesController.getInstance().dialogsServerOnly.size() || !serverOnly && i == MessagesController.getInstance().dialogs.size()) {
            return 1;
        }
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
            return true;
        }
        int count;
        if (serverOnly) {
            count = MessagesController.getInstance().dialogsServerOnly.size();
        } else {
            count = MessagesController.getInstance().dialogs.size();
        }
        if (count == 0 && MessagesController.getInstance().loadingDialogs) {
            return true;
        }
        if (!MessagesController.getInstance().dialogsEndReached) {
            count++;
        }
        return count == 0;
    }
}
