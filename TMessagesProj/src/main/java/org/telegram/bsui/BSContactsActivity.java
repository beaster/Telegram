package org.telegram.bsui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yotadevices.sdk.Constants;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.ContactsController;
import org.telegram.android.LocaleController;
import org.telegram.android.MessagesController;
import org.telegram.android.MessagesStorage;
import org.telegram.android.NotificationCenter;
import org.telegram.android.SecretChatHelper;
import org.telegram.bsui.ActionBar.BSActionBar;
import org.telegram.bsui.ActionBar.BSActionBarMenu;
import org.telegram.bsui.ActionBar.BSActionBarMenuItem;
import org.telegram.bsui.Adapters.BSContactsAdapter;
import org.telegram.bsui.Adapters.BSContactsSearchAdapter;
import org.telegram.bsui.Components.BSSectionsListView;
import org.telegram.bsui.widget.BSBaseActivity;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Adapters.BaseSectionsAdapter;
import org.telegram.ui.Cells.UserCell;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ji on 29.12.2014.
 */
public class BSContactsActivity extends BSBaseActivity implements NotificationCenter.NotificationCenterDelegate {

    private BaseSectionsAdapter listViewAdapter;
    private TextView emptyTextView;
    private BSSectionsListView listView;
    private BSContactsSearchAdapter searchListViewAdapter;

    private boolean searchWas;
    private boolean searching;
    private boolean onlyUsers;
    private boolean needPhonebook;
    private boolean destroyAfterSelect;
    private boolean returnAsResult;
    private boolean createSecretChat;
    private boolean creatingChat = false;
    private String selectAlertString = null;
    private HashMap<Integer, TLRPC.User> ignoreUsers;
    private boolean allowUsernameSearch = true;
    private ContactsActivityDelegate delegate;
    public  Bundle arguments;
    protected View fragmentView;
    public Bundle getArguments() {
        return arguments;
    }

    public static interface ContactsActivityDelegate {
        public abstract void didSelectContact(TLRPC.User user, String param);
    }

    @Override
    protected void onBSCreate() {
        setFeature(Constants.Feature.FEATURE_OVERRIDE_BACK_PRESS);
        super.onBSCreate();
        this.arguments = getIntent().getExtras();
        Initialize();
    }

    @Override
    protected boolean onBackPressed() {
        return super.onBackPressed();
    }

    @Override
    protected void onBSResume() {
        super.onBSResume();
    }

    @Override
    protected void onBSPause() {
        super.onBSPause();
        if (actionBar != null) {
            actionBar.closeSearchField();
        }
    }

    private void Initialize() {
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.contactsDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.encryptedChatCreated);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.appDidLogout);
        if (arguments != null) {
            onlyUsers = getArguments().getBoolean("onlyUsers", false);
            destroyAfterSelect = arguments.getBoolean("destroyAfterSelect", false);
            returnAsResult = arguments.getBoolean("returnAsResult", false);
            createSecretChat = arguments.getBoolean("createSecretChat", false);
            selectAlertString = arguments.getString("selectAlertString");
            allowUsernameSearch = arguments.getBoolean("allowUsernameSearch", true);
        } else {
            needPhonebook = true;
        }
        ContactsController.getInstance().checkInviteText();
        IniActionBar();
        View view = createView();
        setBSContentView(createActionBar(view));
    }

    @Override
    public void finishFragment() {
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.contactsDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.encryptedChatCreated);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.appDidLogout);
        delegate = null;
        super.finishFragment();
    }

    public View createView() {
        if (fragmentView == null) {
            searching = false;
            searchWas = false;

            actionBar.setBackButtonImage(R.drawable.arrow_white);
//            actionBar.setAllowOverlayTitle(true);
            if (destroyAfterSelect) {
                if (returnAsResult) {
                    actionBar.setTitle(LocaleController.getString("SelectContact", R.string.SelectContact));
                } else {
                    actionBar.setTitle(LocaleController.getString("NewMessageTitle", R.string.NewMessageTitle));
                }
            } else {
                actionBar.setTitle(LocaleController.getString("Contacts", R.string.Contacts));
            }

            actionBar.setActionBarMenuOnItemClick(new BSActionBar.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        finishFragment();
                    }
                }
            });

            BSActionBarMenu menu = actionBar.createMenu();
            menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new BSActionBarMenuItem.ActionBarMenuItemSearchListener() {
                @Override
                public void onSearchExpand() {
                    searching = true;
                }

                @Override
                public void onSearchCollapse() {
                    searchListViewAdapter.searchDialogs(null);
                    searching = false;
                    searchWas = false;
                    ViewGroup group = (ViewGroup) listView.getParent();
                    listView.setAdapter(listViewAdapter);
                    listViewAdapter.notifyDataSetChanged();
/*                    if (Build.VERSION.SDK_INT >= 11) {
                        listView.setFastScrollAlwaysVisible(true);
                    }
                    listView.setFastScrollEnabled(true);*/
                    listView.setVerticalScrollBarEnabled(false);
                    emptyTextView.setText(LocaleController.getString("NoContacts", R.string.NoContacts));
                }

                @Override
                public void onTextChanged(EditText editText) {
                    if (searchListViewAdapter == null) {
                        return;
                    }
                    String text = editText.getText().toString();
                    if (text.length() != 0) {
                        searchWas = true;
                        if (listView != null) {
                            listView.setAdapter(searchListViewAdapter);
                            searchListViewAdapter.notifyDataSetChanged();
/*                            if(Build.VERSION.SDK_INT >= 11) {
                                listView.setFastScrollAlwaysVisible(false);
                            }
                            listView.setFastScrollEnabled(false);*/
                            listView.setVerticalScrollBarEnabled(true);
                        }
                        if (emptyTextView != null) {
                            emptyTextView.setText(LocaleController.getString("NoResult", R.string.NoResult));
                        }
                    }
                    searchListViewAdapter.searchDialogs(text);
                }
            });

            searchListViewAdapter = new BSContactsSearchAdapter(getParentActivity(), ignoreUsers, allowUsernameSearch);
            listViewAdapter = new BSContactsAdapter(getParentActivity(), onlyUsers, needPhonebook, ignoreUsers);
            fragmentView = new FrameLayout(getParentActivity());
            fragmentView.setBackgroundColor(0xffffffff);
            FrameLayout emptyTextLayout = new FrameLayout(getParentActivity());
            emptyTextLayout.setVisibility(View.INVISIBLE);
            ((FrameLayout) fragmentView).addView(emptyTextLayout);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) emptyTextLayout.getLayoutParams();
            layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
            layoutParams.gravity = Gravity.CENTER;
            emptyTextLayout.setLayoutParams(layoutParams);
            emptyTextLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            emptyTextView = new TextView(getParentActivity());
            emptyTextView.setTextColor(0xff000000);
            emptyTextView.setTextSize(20);
            emptyTextView.setGravity(Gravity.CENTER);
            emptyTextView.setText(LocaleController.getString("NoContacts", R.string.NoContacts));
            emptyTextLayout.addView(emptyTextView);
            FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) emptyTextView.getLayoutParams();
            layoutParams1.width = FrameLayout.LayoutParams.WRAP_CONTENT;
            layoutParams1.height = FrameLayout.LayoutParams.WRAP_CONTENT;
            layoutParams1.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
            emptyTextView.setLayoutParams(layoutParams1);

            FrameLayout frameLayout = new FrameLayout(getParentActivity());
            emptyTextLayout.addView(frameLayout);
            layoutParams1 = (FrameLayout.LayoutParams) frameLayout.getLayoutParams();
            layoutParams1.width = FrameLayout.LayoutParams.MATCH_PARENT;
            layoutParams1.height = FrameLayout.LayoutParams.MATCH_PARENT;
            frameLayout.setLayoutParams(layoutParams1);

            listView = new BSSectionsListView(getParentActivity());
            listView.setEmptyView(emptyTextLayout);
            listView.setVerticalScrollBarEnabled(false);
            listView.setDivider(null);
            listView.setDividerHeight(0);
//            listView.setFastScrollEnabled(true);
            listView.setAdapter(listViewAdapter);
/*            if (Build.VERSION.SDK_INT >= 11) {
                listView.setFastScrollAlwaysVisible(true);
                listView.setVerticalScrollbarPosition(LocaleController.isRTL ? 0 : 0);
            }*/
            ((FrameLayout) fragmentView).addView(listView);
            layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
            layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
            listView.setLayoutParams(layoutParams);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (searching && searchWas) {
                        TLRPC.User user = searchListViewAdapter.getItem(i);
                        if (user == null || user.id == UserConfig.getClientUserId()) {
                            return;
                        }
                        if (searchListViewAdapter.isGlobalSearch(i)) {
                            ArrayList<TLRPC.User> users = new ArrayList<>();
                            users.add(user);
                            MessagesController.getInstance().putUsers(users, false);
                            MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                        }
                        if (returnAsResult) {
                            if (ignoreUsers != null && ignoreUsers.containsKey(user.id)) {
                                return;
                            }
                            didSelectResult(user, true, null);
                        } else {
                            if (createSecretChat) {
                                creatingChat = true;
                                SecretChatHelper.getInstance().startSecretChat(getParentActivity(), user);
                            } else {
                                Bundle args = new Bundle();
                                args.putInt("user_id", user.id);
                                presentFragment(BSChatActivity.class, args, true);
                            }
                        }
                    } else {
                        int section = listViewAdapter.getSectionForPosition(i);
                        int row = listViewAdapter.getPositionInSectionForPosition(i);
                        if (row < 0 || section < 0) {
                            return;
                        }
                        if (!onlyUsers && section == 0) {
                            if (needPhonebook) {
                                if (row == 0) {
                                    try {
                                        Intent intent = new Intent(Intent.ACTION_SEND);
                                        intent.setType("text/plain");
                                        intent.putExtra(Intent.EXTRA_TEXT, ContactsController.getInstance().getInviteText());
                                        startBSActivity(intent);
                                    } catch (Exception e) {
                                        FileLog.e("tmessages", e);
                                    }
                                }
                            } else {
                                if (row == 0) {
                                    presentFragment(BSGroupCreateActivity.class, true);
                                } else if (row == 1) {
                                    Bundle args = new Bundle();
                                    args.putBoolean("onlyUsers", true);
                                    args.putBoolean("destroyAfterSelect", true);
                                    args.putBoolean("createSecretChat", true);
                                    presentFragment(BSSecretChatContactsActivity.class, args, true);
                                } else if (row == 2) {
                                    Bundle args = new Bundle();
                                    args.putBoolean("broadcast", true);
                                    presentFragment(BSGroupCreateActivity.class,args, true);
                                }
                            }
                        } else {
                            Object item = listViewAdapter.getItem(section, row);

                            if (item instanceof TLRPC.User) {
                                TLRPC.User user = (TLRPC.User) item;
                                if (user.id == UserConfig.getClientUserId()) {
                                    return;
                                }
                                if (returnAsResult) {
                                    if (ignoreUsers != null && ignoreUsers.containsKey(user.id)) {
                                        return;
                                    }
                                    didSelectResult(user, true, null);
                                } else {
                                    if (createSecretChat) {
                                        creatingChat = true;
                                        SecretChatHelper.getInstance().startSecretChat(getParentActivity(), user);
                                    } else {
                                        Bundle args = new Bundle();
                                        args.putInt("user_id", user.id);
                                        presentFragment(BSChatActivity.class,args, true);
                                    }
                                }
                            } else if (item instanceof ContactsController.Contact) {
                                ContactsController.Contact contact = (ContactsController.Contact) item;
                                String usePhone = null;
                                if (!contact.phones.isEmpty()) {
                                    usePhone = contact.phones.get(0);
                                }
                                if (usePhone == null || getParentActivity() == null) {
                                    return;
                                }
                                /*final BSAlertDialog.Builder builder = new BSAlertDialog.Builder(getParentActivity());
                                builder.setMessage(LocaleController.getString("InviteUser", R.string.InviteUser));
                                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                final String arg1 = usePhone;
                                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        try {
                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", arg1, null));
                                            intent.putExtra("sms_body", LocaleController.getString("InviteText", R.string.InviteText));
                                            startBSActivity(intent);
                                            builder.close();
                                        } catch (Exception e) {
                                            FileLog.e("tmessages", e);
                                        }
                                    }
                                });
                                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);*/
                            }
                        }
                    }
                }
            });

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {
                    if (i == SCROLL_STATE_TOUCH_SCROLL && searching && searchWas) {
                        AndroidUtilities.hideKeyboard(fragmentView);
                    }
                }

                @Override
                public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (absListView.isFastScrollEnabled()) {
                        AndroidUtilities.clearDrawableAnimation(absListView);
                    }
                }
            });
        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        return fragmentView;
    }



    private void didSelectResult(final TLRPC.User user, boolean useAlert, String param) {
        if (useAlert && selectAlertString != null) {
            if (getParentActivity() == null) {
                return;
            }
/*            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            builder.setMessage(LocaleController.formatStringSimple(selectAlertString, ContactsController.formatName(user.first_name, user.last_name)));
            final EditText editText = new EditText(getParentActivity());
            if (Build.VERSION.SDK_INT < 11) {
                editText.setBackgroundResource(android.R.drawable.editbox_background_normal);
            }
            editText.setTextSize(18);
            editText.setText("50");
            editText.setGravity(Gravity.CENTER);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            builder.setView(editText);
            builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    didSelectResult(user, false, editText.getText().toString());
                }
            });
            builder.setNegativeButton(R.string.Cancel, null);
            showAlertDialog(builder);*/
/*            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)editText.getLayoutParams();
            if (layoutParams != null) {
                if (layoutParams instanceof FrameLayout.LayoutParams) {
                    ((FrameLayout.LayoutParams)layoutParams).gravity = Gravity.CENTER_HORIZONTAL;
                }
                layoutParams.rightMargin = layoutParams.leftMargin = AndroidUtilities.bsDp(10);
                editText.setLayoutParams(layoutParams);
            }
            editText.setSelection(editText.getText().length());*/
        } else {
            if (delegate != null) {
                delegate.didSelectContact(user, param);
                delegate = null;
            }
            finishFragment();
        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.contactsDidLoaded) {
            if (listViewAdapter != null) {
                listViewAdapter.notifyDataSetChanged();
            }
        } else if (id == NotificationCenter.updateInterfaces) {
            int mask = (Integer)args[0];
            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0 || (mask & MessagesController.UPDATE_MASK_STATUS) != 0) {
                updateVisibleRows(mask);
            }
        } else if (id == NotificationCenter.encryptedChatCreated) {
            if (createSecretChat && creatingChat) {
                TLRPC.EncryptedChat encryptedChat = (TLRPC.EncryptedChat)args[0];
                Bundle args2 = new Bundle();
                args2.putInt("enc_id", encryptedChat.id);
                presentFragment(BSChatActivity.class,args2, true);
            }
        } else if (id == NotificationCenter.appDidLogout){
            finishFragment();
        }
    }
    private void updateVisibleRows(int mask) {
        if (listView != null) {
            int count = listView.getChildCount();
            for (int a = 0; a < count; a++) {
                View child = listView.getChildAt(a);
                if (child instanceof UserCell) {
                    ((UserCell) child).update(mask);
                }
            }
        }
    }

}
