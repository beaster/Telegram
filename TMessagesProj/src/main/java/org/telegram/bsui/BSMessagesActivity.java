package org.telegram.bsui;


import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.yotadevices.sdk.Constants;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.ContactsController;
import org.telegram.android.LocaleController;
import org.telegram.android.MessageObject;
import org.telegram.android.MessagesController;
import org.telegram.android.MessagesStorage;
import org.telegram.android.NotificationCenter;
import org.telegram.bsui.ActionBar.BSActionBar;
import org.telegram.bsui.ActionBar.BSActionBarMenu;
import org.telegram.bsui.ActionBar.BSActionBarMenuItem;
import org.telegram.bsui.ActionBar.BSMenuDrawable;
import org.telegram.bsui.Adapters.BSDialogsAdapter;
import org.telegram.bsui.Adapters.BSDialogsSearchAdapter;
import org.telegram.bsui.Cells.BSDialogCell;
import org.telegram.bsui.Cells.BSUserCell;
import org.telegram.bsui.Components.BSAlertDialog;
import org.telegram.bsui.widget.BSBaseActivity;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.ui.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.ui.AnimationCompat.ViewProxy;
import org.telegram.ui.Cells.UserCell;

import java.util.ArrayList;

/**
 * Created by E1ektr0 on 27.12.2014.
 */
public class BSMessagesActivity extends BSBaseActivity implements NotificationCenter.NotificationCenterDelegate {

    private ListView messagesListView;
    private BSDialogsAdapter dialogsAdapter;
    private View emptyView;
    private boolean onlySelect;
    private String selectAlertString;
    private String selectAlertStringGroup;
    private Bundle arguments;
    private ImageView floatingButton;
    private BSDialogsSearchAdapter dialogsSearchAdapter;
    private boolean floatingHidden;
    private View searchEmptyView;
    private long selectedDialog;

    private boolean searching = false;
    private boolean searchWas = false;
    private boolean serverOnly = false;
    private static boolean dialogsLoaded = false;

    private static BSMessagesActivityDelegate delegate;

    private long openedDialogId = 0;

    public static interface BSMessagesActivityDelegate {
        public abstract void didSelectDialog(BSMessagesActivity fragment, long dialog_id, boolean param);
    }
    private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();

    public static void setDelegate(BSMessagesActivityDelegate d) {
        delegate = d;
    }

    public static BSMessagesActivityDelegate getDelegate() {
        return delegate;
    }

    @Override
    public void finishFragment() {
        removeObservers();
        super.finishFragment();
        if(backPressed && counter > 10)
        {
            System.exit(0);
        }
    }

    Boolean backPressed = false;
    @Override
    protected boolean onBackPressed() {
        if(!onlySelect) {
            backPressed = true;
        }
        return super.onBackPressed();
    }

    @Override
    protected void onBSCreate() {
        super.onBSCreate();
        setFeature(Constants.Feature.FEATURE_OVERRIDE_BACK_PRESS);
        this.arguments = getIntent().getExtras();
        if(arguments!=null) {
            onlySelect = arguments.getBoolean("onlySelect", false);
            serverOnly = arguments.getBoolean("serverOnly", false);
            selectAlertString = arguments.getString("selectAlertString");
            selectAlertStringGroup = arguments.getString("selectAlertStringGroup");
        }
        addObservers();
        if (!dialogsLoaded) {
            MessagesController.getInstance().loadDialogs(0, 0, 100, true);
            dialogsLoaded = true;
        }
        LayoutInflater bsLayoutInflater = getBSDrawer().getBSLayoutInflater();
        View view =bsLayoutInflater.inflate(R.layout.bs_messages_list, null);
        IniActionBar();
        setBSContentView(createActionBar(view));
        floatingButton = (ImageView)findViewById(R.id.btn_new_sms);
        floatingButton.setVisibility(onlySelect ? View.GONE : View.VISIBLE);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)floatingButton.getLayoutParams();
        layoutParams.leftMargin = LocaleController.isRTL ? AndroidUtilities.bsDp(14) : 0;
        layoutParams.rightMargin = LocaleController.isRTL ? 0 : AndroidUtilities.bsDp(14);
        layoutParams.addRule((LocaleController.isRTL ? RelativeLayout.ALIGN_PARENT_LEFT : RelativeLayout.ALIGN_PARENT_RIGHT) | RelativeLayout.ALIGN_BOTTOM);
        floatingButton.setLayoutParams(layoutParams);
        BSActionBarMenu menu = actionBar.createMenu();
        menu.setGravity(Gravity.TOP);
        menu.setPadding(0, AndroidUtilities.bsDp(5), AndroidUtilities.bsDp(5), 0);
        menu.addItem(0, R.drawable.zoom)
                .setIsSearchField(true)
                .setActionBarMenuItemSearchListener(new BSActionBarMenuItem.ActionBarMenuItemSearchListener() {
                    @Override
                    public void onSearchExpand() {
                        searching = true;
                        if (messagesListView != null) {
                            messagesListView.setEmptyView(searchEmptyView);
                            emptyView.setVisibility(View.GONE);
                            if (!onlySelect) {
                                floatingButton.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onSearchCollapse() {
                        searching = false;
                        searchWas = false;
                        if (messagesListView != null) {
                            if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
                                searchEmptyView.setVisibility(View.GONE);
                                emptyView.setVisibility(View.GONE);
                            } else {
                                messagesListView.setEmptyView(emptyView);
                                searchEmptyView.setVisibility(View.GONE);
                            }
                            if (!onlySelect) {
                                floatingButton.setVisibility(View.VISIBLE);
                                floatingHidden = true;
                                ViewProxy.setTranslationY(floatingButton, AndroidUtilities.bsDp(100));
                                hideFloatingButton(false);
                            }
                            if (messagesListView.getAdapter() != dialogsAdapter) {
                                messagesListView.setAdapter(dialogsAdapter);
                                dialogsAdapter.notifyDataSetChanged();
                            }
                        }
                        if (dialogsSearchAdapter != null) {
                            dialogsSearchAdapter.searchDialogs(null, false);
                        }
                    }

                    @Override
                    public void onTextChanged(EditText editText) {
                        String text = editText.getText().toString();
                        if (text.length() != 0) {
                            searchWas = true;
                            if (dialogsSearchAdapter != null) {
                                messagesListView.setAdapter(dialogsSearchAdapter);
                                dialogsSearchAdapter.notifyDataSetChanged();
                            }
                            if (searchEmptyView != null && messagesListView.getEmptyView() == emptyView) {
                                messagesListView.setEmptyView(searchEmptyView);
                                emptyView.setVisibility(View.GONE);
                            }
                        }
                        if (dialogsSearchAdapter != null) {
                            dialogsSearchAdapter.searchDialogs(text, serverOnly);
                        }
                    }
                });
        if (onlySelect){
            actionBar.setBackButtonImage(R.drawable.arrow_white);
            actionBar.setTitle(LocaleController.getString("SelectChat", R.string.SelectChat));
        } else {
            actionBar.setBackButtonDrawable(new BSMenuDrawable());
            actionBar.setTitle(LocaleController.getString("AppName", R.string.AppName));
        }
//        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new BSActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (onlySelect) {
                        finishFragment();
                    } else{
                        Intent i = new Intent(getContext(), OtherFlipBSActivity.class);
                        OtherFlipBSActivity.setSettingsFlag();
                        getContext().startService(i);
                    }
                }
            }
        });

        searching = false;
        searchWas = false;

        dialogsSearchAdapter = new BSDialogsSearchAdapter(getParentActivity(), !onlySelect);
        dialogsSearchAdapter.setDelegate(new BSDialogsSearchAdapter.BSMessagesActivitySearchAdapterDelegate(){
            @Override
            public void searchStateChanged(boolean search) {
                if (searching && searchWas && messagesListView != null) {
                    searchEmptyView.setVisibility(search ? View.GONE : View.VISIBLE);
                }
            }
        });
        messagesListView = (ListView) findViewById(R.id.sms_listview);
        dialogsAdapter = new BSDialogsAdapter(getBSDrawer().getBSContext(), serverOnly);
        emptyView = findViewById(R.id.nomessages_layout);
        messagesListView.setAdapter(dialogsAdapter);
        messagesListView.setEmptyView(emptyView);
        searchEmptyView = findViewById(R.id.bs_search_empty_view);
        searchEmptyView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        messagesListView.setScrollingCacheEnabled(false);

        if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
            searchEmptyView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        } else {
            messagesListView.setEmptyView(emptyView);
            searchEmptyView.setVisibility(View.GONE);
        }

        messagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (messagesListView == null || messagesListView.getAdapter() == null) {
                    return;
                }
                long dialog_id = 0;
                int message_id = 0;
                BaseAdapter adapter = (BaseAdapter)messagesListView.getAdapter();

                if (adapter == dialogsAdapter) {
                    TLRPC.TL_dialog dialog = dialogsAdapter.getItem(i);
                    if (dialog == null) {
                        return;
                    }
                    dialog_id = dialog.id;
                } else if (adapter == dialogsSearchAdapter) {
                    Object obj = dialogsSearchAdapter.getItem(i);
                    if (obj instanceof TLRPC.User) {
                        dialog_id = ((TLRPC.User) obj).id;
                        if (dialogsSearchAdapter.isGlobalSearch(i)) {
                            ArrayList<TLRPC.User> users = new ArrayList<TLRPC.User>();
                            users.add((TLRPC.User)obj);
                            MessagesController.getInstance().putUsers(users, false);
                            MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                        }
                    } else if (obj instanceof TLRPC.Chat) {
                        if (((TLRPC.Chat) obj).id > 0) {
                            dialog_id = -((TLRPC.Chat) obj).id;
                        } else {
                            dialog_id = AndroidUtilities.makeBroadcastId(((TLRPC.Chat) obj).id);
                        }
                    } else if (obj instanceof TLRPC.EncryptedChat) {
                        dialog_id = ((long)((TLRPC.EncryptedChat) obj).id) << 32;
                    } else if (obj instanceof MessageObject) {
                        MessageObject messageObject = (MessageObject)obj;
                        dialog_id = messageObject.getDialogId();
                        message_id = messageObject.messageOwner.id;
                    }
                }
                if (dialog_id == 0) {
                    return;
                }

                if (onlySelect) {
                    didSelectResult(dialog_id, true, false);
                } else {
                    Bundle args = new Bundle();
                    int lower_part = (int) dialog_id;
                    int high_id = (int) (dialog_id >> 32);
                    if (lower_part != 0) {
                        if (high_id == 1) {
                            args.putInt("chat_id", lower_part);
                        } else {
                            if (lower_part > 0) {
                                args.putInt("user_id", lower_part);
                            } else if (lower_part < 0) {
                                args.putInt("chat_id", -lower_part);
                            }
                        }
                    } else {
                        args.putInt("enc_id", high_id);
                    }
                    if (message_id != 0) {
                        args.putInt("message_id", message_id);
                    }
                    presentFragment(BSChatActivity.class, args, false);
                }
            }});

        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putBoolean("destroyAfterSelect", true);
                presentFragment(BSContactsActivity.class, args, false);
            }
        });
    }


    @Override
    protected void onBSDestroy() {
        super.onBSDestroy();
    }

    @Override
    protected void onBSPause() {
        super.onBSPause();
    }

    private void hideFloatingButton(boolean hide) {
        if (floatingHidden == hide) {
            return;
        }
        floatingHidden = hide;
        ObjectAnimatorProxy animator = ObjectAnimatorProxy.ofFloatProxy(floatingButton, "translationY", floatingHidden ? AndroidUtilities.bsDp(100) : 0).setDuration(300);
        animator.setInterpolator(floatingInterpolator);
        floatingButton.setClickable(!hide);
        animator.start();
    }

    private void addObservers() {
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.dialogsNeedReload);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.encryptedChatUpdated);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.contactsDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.appDidLogout);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.openedChatChanged);
    }

    @Override
    protected void onBSResume() {
        super.onBSResume();
        if (dialogsAdapter != null) {
            dialogsAdapter.notifyDataSetChanged();
        }
        if (dialogsSearchAdapter != null) {
            dialogsSearchAdapter.notifyDataSetChanged();
        }
    }

    private void removeObservers(){
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.dialogsNeedReload);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.encryptedChatUpdated);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.contactsDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.appDidLogout);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.openedChatChanged);
        delegate = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!onlySelect && floatingButton != null) {
            floatingButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewProxy.setTranslationY(floatingButton, floatingHidden ? AndroidUtilities.bsDp(100) : 0);
                    floatingButton.setClickable(!floatingHidden);
                    if (floatingButton != null) {
                        if (Build.VERSION.SDK_INT < 16) {
                            floatingButton.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            floatingButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                }
            });
        }
    }


    private void didSelectResult(final long dialog_id, boolean useAlert, final boolean param) {
        if (useAlert && selectAlertString != null && selectAlertStringGroup != null) {
            if (getParentActivity() == null) {
                return;
            }
            final BSAlertDialog.Builder builder = new BSAlertDialog.Builder(BSMessagesActivity.this);
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            int lower_part = (int)dialog_id;
            int high_id = (int)(dialog_id >> 32);
            if (lower_part != 0) {
                if (high_id == 1) {
                    TLRPC.Chat chat = MessagesController.getInstance().getChat(lower_part);
                    if (chat == null) {
                        return;
                    }
                    builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
                } else {
                    if (lower_part > 0) {
                        TLRPC.User user = MessagesController.getInstance().getUser(lower_part);
                        if (user == null) {
                            return;
                        }
                        builder.setMessage(LocaleController.formatStringSimple(selectAlertString, ContactsController.formatName(user.first_name, user.last_name)));
                    } else if (lower_part < 0) {
                        TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_part);
                        if (chat == null) {
                            return;
                        }
                        builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
                    }
                }
            } else {
                TLRPC.EncryptedChat chat = MessagesController.getInstance().getEncryptedChat(high_id);
                TLRPC.User user = MessagesController.getInstance().getUser(chat.user_id);
                if (user == null) {
                    return;
                }
                builder.setMessage(LocaleController.formatStringSimple(selectAlertString, ContactsController.formatName(user.first_name, user.last_name)));
            }
            final CheckBox checkBox = null;
            /*if (delegate instanceof BSChatActivity) {
                checkBox = new CheckBox(getParentActivity());
                checkBox.setText(LocaleController.getString("ForwardFromMyName", R.string.ForwardFromMyName));
                checkBox.setChecked(false);
                bsAlertDialog.setView(checkBox);
            }*/
            final CheckBox checkBoxFinal = checkBox;
            builder.setPositiveButton(LocaleController.getString("Ok", R.string.OK), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    didSelectResult(dialog_id, false, checkBoxFinal != null && checkBox.isChecked());
                    builder.close();
                }
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            builder.show();
            if (checkBox != null) {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)checkBox.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.rightMargin = layoutParams.leftMargin = AndroidUtilities.bsDp(10);
                    checkBox.setLayoutParams(layoutParams);
                }
            }
        } else {
            if (delegate != null) {
                delegate.didSelectDialog(BSMessagesActivity.this, dialog_id, param);
                Bundle args = new Bundle();
                int lower_part = (int) dialog_id;
                int high_id = (int) (dialog_id >> 32);
                if (lower_part != 0) {
                    if (high_id == 1) {
                        args.putInt("chat_id", lower_part);
                    } else {
                        if (lower_part > 0) {
                            args.putInt("user_id", lower_part);
                        } else if (lower_part < 0) {
                            args.putInt("chat_id", -lower_part);
                        }
                    }
                } else {
                    args.putInt("enc_id", high_id);
                }
                presentFragment(BSChatActivity.class, args, true);
                delegate = null;
            } else {
                finishFragment();
            }
        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.dialogsNeedReload) {
            if (dialogsAdapter != null) {
                dialogsAdapter.notifyDataSetChanged();
            }
            if (messagesListView != null) {
                try {
                    if (messagesListView.getAdapter() != null && messagesListView.getAdapter() instanceof BaseAdapter) {
                        ((BaseAdapter) messagesListView.getAdapter()).notifyDataSetChanged();
                    }
                    if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
                        emptyView.setVisibility(View.GONE);
                    } else {
                        messagesListView.setEmptyView(emptyView);
                    }
                } catch (Exception e) {
                    FileLog.e("bsmessages", e);
                }
            } else if (id == NotificationCenter.emojiDidLoaded) {
                if (messagesListView != null) {
                    updateVisibleRows(0);
                }
            } else if (id == NotificationCenter.updateInterfaces) {
                updateVisibleRows((Integer) args[0]);
            } else if (id == NotificationCenter.appDidLogout) {
                dialogsLoaded = false;
                finishFragment();
            } else if (id == NotificationCenter.encryptedChatUpdated) {
                updateVisibleRows(0);
            } else if (id == NotificationCenter.contactsDidLoaded) {
                updateVisibleRows(0);
            } else if (id == NotificationCenter.openedChatChanged) {
                if (!serverOnly && AndroidUtilities.isTablet()) {
                    boolean close = (Boolean) args[1];
                    long dialog_id = (Long) args[0];
                    if (close) {
                        if (dialog_id == openedDialogId) {
                            openedDialogId = 0;
                        }
                    } else {
                        openedDialogId = dialog_id;
                    }
                    if (dialogsAdapter != null) {
                        dialogsAdapter.setOpenedDialogId(openedDialogId);
                    }
                    updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                }
            }
        } else if (id == NotificationCenter.appDidLogout) {
            finishFragment();
        }
    }

    private void updateVisibleRows(int mask) {
        if (messagesListView == null) {
            return;
        }
        int count = messagesListView.getChildCount();
        for (int a = 0; a < count; a++) {
            View child = messagesListView.getChildAt(a);
            if (child instanceof BSDialogCell) {
                BSDialogCell cell = (BSDialogCell) child;
                if ((mask & MessagesController.UPDATE_MASK_SELECT_DIALOG) != 0) {
                    cell.update(mask);
                }
            } else if (child instanceof BSUserCell) {
                ((BSUserCell) child).update(mask);
            }
        }
    }
}
