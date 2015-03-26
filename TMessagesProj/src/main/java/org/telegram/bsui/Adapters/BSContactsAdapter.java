package org.telegram.bsui.Adapters;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.ContactsController;
import org.telegram.android.LocaleController;
import org.telegram.android.MessagesController;
import org.telegram.bsui.Cells.BSDividerCell;
import org.telegram.bsui.Cells.BSGreySectionCell;
import org.telegram.bsui.Cells.BSLetterSectionCell;
import org.telegram.bsui.Cells.BSTextCell;
import org.telegram.bsui.Cells.BSUserCell;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.ui.Adapters.BaseSectionsAdapter;
import org.telegram.ui.AnimationCompat.ViewProxy;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by E1ektr0 on 09.01.2015.
 */
public class BSContactsAdapter extends BaseSectionsAdapter {

    private Context mContext;
    private boolean onlyUsers;
    private boolean needPhonebook;
    private HashMap<Integer, TLRPC.User> ignoreUsers;
    private HashMap<Integer, ?> checkedMap;
    private boolean scrolling;

    public BSContactsAdapter(Context context, boolean arg1, boolean arg2, HashMap<Integer, TLRPC.User> arg3) {
        mContext = context;
        onlyUsers = arg1;
        needPhonebook = arg2;
        ignoreUsers = arg3;
    }

    public void setCheckedMap(HashMap<Integer, ?> map) {
        checkedMap = map;
    }

    public void setIsScrolling(boolean value) {
        scrolling = value;
    }

    @Override
    public Object getItem(int section, int position) {
        if (onlyUsers) {
            if (section < ContactsController.getInstance().sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section));
                if (position < arr.size()) {
                    return MessagesController.getInstance().getUser(arr.get(position).user_id);
                }
            }
            return null;
        } else {
            if (section == 0) {
                return null;
            } else {
                if (section - 1 < ContactsController.getInstance().sortedUsersSectionsArray.size()) {
                    ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section - 1));
                    if (position < arr.size()) {
                        return MessagesController.getInstance().getUser(arr.get(position).user_id);
                    }
                    return null;
                }
            }
        }
        if (needPhonebook) {
            return ContactsController.getInstance().phoneBookContacts.get(position);
        }
        return null;
    }

    @Override
    public boolean isRowEnabled(int section, int row) {
        if (onlyUsers) {
            ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section));
            return row < arr.size();
        } else {
            if (section == 0) {
                if (needPhonebook) {
                    if (row == 1) {
                        return false;
                    }
                } else {
                    if (row == 3) {
                        return false;
                    }
                }
                return true;
            } else if (section - 1 < ContactsController.getInstance().sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section - 1));
                return row < arr.size();
            }
        }
        return true;
    }

    @Override
    public int getSectionCount() {
        int count = ContactsController.getInstance().sortedUsersSectionsArray.size();
        if (!onlyUsers) {
            count++;
        }
        if (needPhonebook) {
            count++;
        }
        return count;
    }

    @Override
    public int getCountForSection(int section) {
        if (onlyUsers) {
            if (section < ContactsController.getInstance().sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section));
                int count = arr.size();
                if (section != (ContactsController.getInstance().sortedUsersSectionsArray.size() - 1) || needPhonebook) {
                    count++;
                }
                return count;
            }
        } else {
            if (section == 0) {
                if (needPhonebook) {
                    return 2;
                } else {
                    return 4;
                }
            } else if (section - 1 < ContactsController.getInstance().sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section - 1));
                int count = arr.size();
                if (section - 1 != (ContactsController.getInstance().sortedUsersSectionsArray.size() - 1) || needPhonebook) {
                    count++;
                }
                return count;
            }
        }
        if (needPhonebook) {
            return ContactsController.getInstance().phoneBookContacts.size();
        }
        return 0;
    }

    @Override
    public View getSectionHeaderView(int section, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new BSLetterSectionCell(mContext);
        }
        if (onlyUsers) {
            if (section < ContactsController.getInstance().sortedUsersSectionsArray.size()) {
                ((BSLetterSectionCell) convertView).setLetter(ContactsController.getInstance().sortedUsersSectionsArray.get(section));
            } else {
                ((BSLetterSectionCell) convertView).setLetter("");
            }
        } else {
            if (section == 0) {
                ((BSLetterSectionCell) convertView).setLetter("");
            } else if (section - 1 < ContactsController.getInstance().sortedUsersSectionsArray.size()) {
                ((BSLetterSectionCell) convertView).setLetter(ContactsController.getInstance().sortedUsersSectionsArray.get(section - 1));
            } else {
                ((BSLetterSectionCell) convertView).setLetter("");
            }
        }
        return convertView;
    }

    @Override
    public View getItemView(int section, int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(section, position);
        if (type == 4) {
            if (convertView == null) {
                convertView = new BSDividerCell(mContext);
                convertView.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 28 : 72), 0, AndroidUtilities.dp(LocaleController.isRTL ? 72 : 28), 0);
            }
        } else if (type == 3) {
            if (convertView == null) {
                convertView = new BSGreySectionCell(mContext);
                ((BSGreySectionCell) convertView).setText(LocaleController.getString("Contacts", R.string.Contacts).toUpperCase());
            }
        } else if (type == 2) {
            if (convertView == null) {
                convertView = new BSTextCell(mContext);
            }
            BSTextCell actionCell = (BSTextCell) convertView;
            if (needPhonebook) {
                actionCell.setTextAndIcon(LocaleController.getString("InviteFriends", R.string.InviteFriends), R.drawable.menu_invite);
            } else {
                if (position == 0) {
                    actionCell.setTextAndIcon(LocaleController.getString("NewGroup", R.string.NewGroup), R.drawable.new_group_bs);
                } else if (position == 1) {
                    actionCell.setTextAndIcon(LocaleController.getString("NewSecretChat", R.string.NewSecretChat), R.drawable.new_secret_chat_bs);
                } else if (position == 2) {
                    actionCell.setTextAndIcon(LocaleController.getString("NewBroadcastList", R.string.NewBroadcastList), R.drawable.new_broadcast_bs);
                }
            }
        } else if (type == 1) {
            if (convertView == null) {
                convertView = new BSTextCell(mContext);
            }
            ContactsController.Contact contact = ContactsController.getInstance().phoneBookContacts.get(position);
            if (contact.first_name != null && contact.last_name != null) {
                ((BSTextCell) convertView).setText(contact.first_name + " " + contact.last_name);
            } else if (contact.first_name != null && contact.last_name == null) {
                ((BSTextCell) convertView).setText(contact.first_name);
            } else {
                ((BSTextCell) convertView).setText(contact.last_name);
            }
        } else if (type == 0) {
            if (convertView == null) {
                convertView = new BSUserCell(mContext, 58);
                ((BSUserCell) convertView).setStatusColors(0xff000000, 0xff000000);
            }

            ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section - (onlyUsers ? 0 : 1)));
            TLRPC.User user = MessagesController.getInstance().getUser(arr.get(position).user_id);
            ((BSUserCell)convertView).setData(user, null, null, 0);
            if (checkedMap != null) {
                ((BSUserCell) convertView).setChecked(checkedMap.containsKey(user.id), !scrolling  && Build.VERSION.SDK_INT > 10);
            }
            if (ignoreUsers != null) {
                if (ignoreUsers.containsKey(user.id)) {
                    ViewProxy.setAlpha(convertView, 0.5f);
                } else {
                    ViewProxy.setAlpha(convertView, 1.0f);
                }
            }
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int section, int position) {
        if (onlyUsers) {
            ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section));
            return position < arr.size() ? 0 : 4;
        } else {
            if (section == 0) {
                if (needPhonebook) {
                    if (position == 1) {
                        return 3;
                    }
                } else {
                    if (position == 3) {
                        return 3;
                    }
                }
                return 2;
            } else if (section - 1 < ContactsController.getInstance().sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = ContactsController.getInstance().usersSectionsDict.get(ContactsController.getInstance().sortedUsersSectionsArray.get(section - 1));
                return position < arr.size() ? 0 : 4;
            }
        }
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }
}
