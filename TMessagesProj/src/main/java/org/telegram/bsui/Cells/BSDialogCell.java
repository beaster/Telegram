package org.telegram.bsui.Cells;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.android.AndroidUtilities;
import org.telegram.android.ContactsController;
import org.telegram.android.Emoji;
import org.telegram.android.ImageReceiver;
import org.telegram.android.LocaleController;
import org.telegram.android.MessageObject;
import org.telegram.android.MessagesController;
import org.telegram.bsui.BSAvatarDrawable;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Cells.BaseCell;

/**
 * Created by Ji on 29.12.2014.
 */
public class BSDialogCell extends BSBaseCell {

    private static TextPaint namePaint;
    private static TextPaint nameEncryptedPaint;
    private static TextPaint nameUnknownPaint;
    private static TextPaint messagePaint;
    private static TextPaint messagePrintingPaint;
    private static TextPaint timePaint;
    private static TextPaint countPaint;

    private static Drawable checkDrawable;
    private static Drawable halfCheckDrawable;
    private static Drawable clockDrawable;
    private static Drawable errorDrawable;
    private static Drawable lockDrawable;
    private static Drawable countDrawable;
    private static Drawable groupDrawable;
    private static Drawable broadcastDrawable;

    private static Bitmap dottedBitmap;

    private long currentDialogId;
    private boolean allowPrintStrings;
    private int lastMessageDate;
    private int unreadCount;
    private boolean lastUnreadState;
    private MessageObject message;

    private ImageReceiver avatarImage;
    private BSAvatarDrawable avatarDrawable;

    private TLRPC.User user = null;
    private TLRPC.Chat chat = null;
    private TLRPC.EncryptedChat encryptedChat = null;
    private CharSequence lastPrintString = null;

    public boolean useSeparator = false;


    private int nameLeft;
    private StaticLayout nameLayout;
    private boolean drawNameLock;
    private boolean drawNameGroup;
    private boolean drawNameBroadcast;
    private int nameLockLeft;
    private int nameLockTop;

    private int timeLeft;
    private int timeTop = AndroidUtilities.bsDp(25);
    private StaticLayout timeLayout;

    private boolean drawCheck1;
    private boolean drawCheck2;
    private boolean drawClock;
    private int checkDrawLeft;
    private int checkDrawTop = AndroidUtilities.bsDp(27);
    private int halfCheckDrawLeft;

    private int messageTop = AndroidUtilities.bsDp(53);
    private int messageLeft;
    private StaticLayout messageLayout;

    private boolean drawError;
    private int errorTop = AndroidUtilities.bsDp(48);
    private int errorLeft;

    private boolean drawCount;
    private int countTop = AndroidUtilities.bsDp(48);
    private int countLeft;
    private int countWidth;
    private StaticLayout countLayout;

    private int avatarTop = AndroidUtilities.bsDp(20);

    private void init() {
        if (namePaint == null) {
            namePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            namePaint.setTextSize(AndroidUtilities.bsDp(17));

            namePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

            nameEncryptedPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            nameEncryptedPaint.setTextSize(AndroidUtilities.bsDp(17));

            nameEncryptedPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

            nameUnknownPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            nameUnknownPaint.setTextSize(AndroidUtilities.bsDp(17));

            nameUnknownPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

            messagePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            messagePaint.setTextSize(AndroidUtilities.bsDp(16));
            messagePaint.setColor(0xFF000000);


            dottedBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dots);

            messagePrintingPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            messagePrintingPaint.setTextSize(AndroidUtilities.bsDp(16));
            messagePrintingPaint.setColor(0xFF000000);


            timePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaint.setTextSize(AndroidUtilities.bsDp(13));


            countPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            countPaint.setTextSize(AndroidUtilities.bsDp(13));

            countPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

            lockDrawable = getResources().getDrawable(R.drawable.secret_chat_bs);
            checkDrawable = getResources().getDrawable(R.drawable.msg_check_bs);
            halfCheckDrawable = getResources().getDrawable(R.drawable.msg_halfchecked_bs);
            clockDrawable = getResources().getDrawable(R.drawable.msg_clock_bs);
            errorDrawable = getResources().getDrawable(R.drawable.alert_bs);
            countDrawable = getResources().getDrawable(R.drawable.msg_counter_bs);
            groupDrawable = getResources().getDrawable(R.drawable.list_group);
            broadcastDrawable = getResources().getDrawable(R.drawable.broadcast_bs);
        }
    }

    Context context;

    public BSDialogCell(Context context) {
        super(context);
        init();
        avatarImage = new ImageReceiver(this);
        avatarImage.setRoundRadius(AndroidUtilities.bsDp(26));
        avatarDrawable = new BSAvatarDrawable();
        this.context = context;
    }

    public void setDialog(long dialog_id, MessageObject messageObject, boolean usePrintStrings, int date, int unread) {
        currentDialogId = dialog_id;
        message = messageObject;
        allowPrintStrings = usePrintStrings;
        lastMessageDate = date;
        unreadCount = unread;
        lastUnreadState = messageObject != null && messageObject.isUnread();
        update(0);
    }

    public long getDialogId() {
        return currentDialogId;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (avatarImage != null) {
            avatarImage.clearImage();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.bsDp(100));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (currentDialogId == 0) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }
        if (changed) {
            buildLayout();
        }
    }

    public void buildLayout() {
        String nameString = "";
        String timeString = "";
        String countString = null;
        CharSequence messageString = "";
        CharSequence printingString = null;
        if (allowPrintStrings) {
            printingString = MessagesController.getInstance().printingStrings.get(currentDialogId);
        }
        TextPaint currentNamePaint = namePaint;
        TextPaint currentMessagePaint = messagePaint;
        boolean checkMessage = true;

        drawNameGroup = false;
        drawNameBroadcast = false;
        drawNameLock = false;

        if (encryptedChat != null) {
            drawNameLock = true;
            nameLockTop = AndroidUtilities.bsDp(25.5f);
            if (!LocaleController.isRTL) {
                nameLockLeft = AndroidUtilities.bsDp(84);
                nameLeft = AndroidUtilities.bsDp(94) + lockDrawable.getIntrinsicWidth();
            } else {
                nameLockLeft = getMeasuredWidth() - AndroidUtilities.bsDp(84) - lockDrawable.getIntrinsicWidth();
                nameLeft = AndroidUtilities.bsDp(22);
            }
        } else {
            if (chat != null) {
                if (chat.id < 0) {
                    drawNameBroadcast = true;
                    nameLockTop = AndroidUtilities.bsDp(25.5f);
                } else {
                    drawNameGroup = true;
                    nameLockTop = AndroidUtilities.bsDp(26.5f);
                }

                if (!LocaleController.isRTL) {
                    nameLockLeft = AndroidUtilities.bsDp(84);
                    nameLeft = AndroidUtilities.bsDp(94) + (drawNameGroup ? groupDrawable.getIntrinsicWidth() : broadcastDrawable.getIntrinsicWidth());
                } else {
                    nameLockLeft = getMeasuredWidth() - AndroidUtilities.bsDp(84) - (drawNameGroup ? groupDrawable.getIntrinsicWidth() : broadcastDrawable.getIntrinsicWidth());
                    nameLeft = AndroidUtilities.bsDp(22);
                }
            } else {
                if (!LocaleController.isRTL) {
                    nameLeft = AndroidUtilities.bsDp(84);
                } else {
                    nameLeft = AndroidUtilities.bsDp(22);
                }
            }
        }

        if (message == null) {
            if (printingString != null) {
                lastPrintString = messageString = printingString;
                currentMessagePaint = messagePrintingPaint;
            } else {
                lastPrintString = null;
                if (encryptedChat != null) {
                    currentMessagePaint = messagePrintingPaint;
                    if (encryptedChat instanceof TLRPC.TL_encryptedChatRequested) {
                        messageString = LocaleController.getString("EncryptionProcessing", R.string.EncryptionProcessing);
                    } else if (encryptedChat instanceof TLRPC.TL_encryptedChatWaiting) {
                        if (user != null && user.first_name != null) {
                            messageString = LocaleController.formatString("AwaitingEncryption", R.string.AwaitingEncryption, user.first_name);
                        } else {
                            messageString = LocaleController.formatString("AwaitingEncryption", R.string.AwaitingEncryption, "");
                        }
                    } else if (encryptedChat instanceof TLRPC.TL_encryptedChatDiscarded) {
                        messageString = LocaleController.getString("EncryptionRejected", R.string.EncryptionRejected);
                    } else if (encryptedChat instanceof TLRPC.TL_encryptedChat) {
                        if (encryptedChat.admin_id == UserConfig.getClientUserId()) {
                            if (user != null && user.first_name != null) {
                                messageString = LocaleController.formatString("EncryptedChatStartedOutgoing", R.string.EncryptedChatStartedOutgoing, user.first_name);
                            } else {
                                messageString = LocaleController.formatString("EncryptedChatStartedOutgoing", R.string.EncryptedChatStartedOutgoing, "");
                            }
                        } else {
                            messageString = LocaleController.getString("EncryptedChatStartedIncoming", R.string.EncryptedChatStartedIncoming);
                        }
                    }
                }
            }
            if (lastMessageDate != 0) {
                timeString = LocaleController.stringForMessageListDate(lastMessageDate);
            }
            drawCheck1 = false;
            drawCheck2 = false;
            drawClock = false;
            drawCount = false;
            drawError = false;
        } else {
            TLRPC.User fromUser = MessagesController.getInstance().getUser(message.messageOwner.from_id);

            if (lastMessageDate != 0) {
                timeString = LocaleController.stringForMessageListDate(lastMessageDate);
            } else {
                timeString = LocaleController.stringForMessageListDate(message.messageOwner.date);
            }
            if (printingString != null) {
                lastPrintString = messageString = printingString;
                currentMessagePaint = messagePrintingPaint;
            } else {
                lastPrintString = null;
                if (message.messageOwner instanceof TLRPC.TL_messageService) {
                    messageString = message.messageText;
                    currentMessagePaint = messagePrintingPaint;
                } else {
                    if (chat != null && chat.id > 0) {
                        String name = "";
                        if (message.isFromMe()) {
                            name = LocaleController.getString("FromYou", R.string.FromYou);
                        } else {
                            if (fromUser != null) {
                                if (fromUser.first_name.length() > 0) {
                                    name = fromUser.first_name;
                                } else {
                                    name = fromUser.last_name;
                                }
                            }
                        }
                        checkMessage = false;
                        if (message.messageOwner.media != null && !(message.messageOwner.media instanceof TLRPC.TL_messageMediaEmpty)) {
                            currentMessagePaint = messagePrintingPaint;
                            messageString = Emoji.replaceEmoji(Html.fromHtml(String.format("<font color=#4d83b3>%s:</font> <font color=#4d83b3>%s</font>", name, message.messageText)), messagePaint.getFontMetricsInt(), AndroidUtilities.bsDp(20));
                        } else {
                            if (message.messageOwner.message != null) {
                                String mess = message.messageOwner.message;
                                if (mess.length() > 150) {
                                    mess = mess.substring(0, 150);
                                }
                                mess = mess.replace("\n", " ");
                                messageString = Emoji.replaceEmoji(Html.fromHtml(String.format("<font color=#4d83b3>%s:</font> <font color=#808080>%s</font>", name, mess.replace("<", "&lt;").replace(">", "&gt;"))), messagePaint.getFontMetricsInt(), AndroidUtilities.bsDp(20));
                            }
                        }
                    } else {
                        messageString = message.messageText;
                        if (message.messageOwner.media != null && !(message.messageOwner.media instanceof TLRPC.TL_messageMediaEmpty)) {
                            currentMessagePaint = messagePrintingPaint;
                        }
                    }
                }
            }

            if (unreadCount != 0) {
                drawCount = true;
                countString = String.format("%d", unreadCount);
            } else {
                drawCount = false;
            }

            if (message.isFromMe() && message.isOut()) {
                if (message.isSending()) {
                    drawCheck1 = false;
                    drawCheck2 = false;
                    drawClock = true;
                    drawError = false;
                } else if (message.isSendError()) {
                    drawCheck1 = false;
                    drawCheck2 = false;
                    drawClock = false;
                    drawError = true;
                    drawCount = false;
                } else if (message.isSent()) {
                    if (!message.isUnread()) {
                        drawCheck1 = true;
                        drawCheck2 = true;
                    } else {
                        drawCheck1 = false;
                        drawCheck2 = true;
                    }
                    drawClock = false;
                    drawError = false;
                }
            } else {
                drawCheck1 = false;
                drawCheck2 = false;
                drawClock = false;
                drawError = false;
            }
        }

        int timeWidth = (int) Math.ceil(timePaint.measureText(timeString));
        timeLayout = new StaticLayout(timeString, timePaint, timeWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        if (!LocaleController.isRTL) {
            timeLeft = getMeasuredWidth() - AndroidUtilities.bsDp(20) - timeWidth;
        } else {
            timeLeft = AndroidUtilities.bsDp(20);
        }

        if (chat != null) {
            nameString = chat.title;
        } else if (user != null) {
            if (user.id / 1000 != 777 && user.id / 1000 != 333 && ContactsController.getInstance().contactsDict.get(user.id) == null) {
                if (ContactsController.getInstance().contactsDict.size() == 0 && (!ContactsController.getInstance().contactsLoaded || ContactsController.getInstance().isLoadingContacts())) {
                    nameString = ContactsController.formatName(user.first_name, user.last_name);
                } else {
                    if (user.phone != null && user.phone.length() != 0) {
                        nameString = PhoneFormat.getInstance().format("+" + user.phone);
                    } else {
                        currentNamePaint = nameUnknownPaint;
                        nameString = ContactsController.formatName(user.first_name, user.last_name);
                    }
                }
            } else {
                nameString = ContactsController.formatName(user.first_name, user.last_name);
            }
            if (encryptedChat != null) {
                currentNamePaint = nameEncryptedPaint;
            }
        }
        if (nameString.length() == 0) {
            nameString = LocaleController.getString("HiddenName", R.string.HiddenName);
        }

        int nameWidth;

        if (!LocaleController.isRTL) {
            nameWidth = getMeasuredWidth() - nameLeft - AndroidUtilities.bsDp(14) - timeWidth;
        } else {
            nameWidth = getMeasuredWidth() - nameLeft - AndroidUtilities.bsDp(84) - timeWidth;
            nameLeft += timeWidth;
        }
        if (drawNameLock) {
            nameWidth -= AndroidUtilities.bsDp(4) + lockDrawable.getIntrinsicWidth();
        } else if (drawNameGroup) {
            nameWidth -= AndroidUtilities.bsDp(4) + groupDrawable.getIntrinsicWidth();
        } else if (drawNameBroadcast) {
            nameWidth -= AndroidUtilities.bsDp(4) + broadcastDrawable.getIntrinsicWidth();
        }
        if (drawClock) {
            int w = clockDrawable.getIntrinsicWidth() + AndroidUtilities.bsDp(5);
            nameWidth -= w;
            if (!LocaleController.isRTL) {
                checkDrawLeft = timeLeft - w;
            } else {
                checkDrawLeft = timeLeft + timeWidth + AndroidUtilities.bsDp(5);
                nameLeft += w;
            }
        } else if (drawCheck2) {
            int w = checkDrawable.getIntrinsicWidth() + AndroidUtilities.bsDp(5);
            nameWidth -= w;
            if (drawCheck1) {
                nameWidth -= halfCheckDrawable.getIntrinsicWidth() - AndroidUtilities.bsDp(8);
                if (!LocaleController.isRTL) {
                    halfCheckDrawLeft = timeLeft - w;
                    checkDrawLeft = halfCheckDrawLeft - AndroidUtilities.bsDp(6.5f);
                } else {
                    checkDrawLeft = timeLeft + timeWidth + AndroidUtilities.bsDp(5);
                    halfCheckDrawLeft = checkDrawLeft + AndroidUtilities.bsDp(6.5f);
                    nameLeft += w + halfCheckDrawable.getIntrinsicWidth() - AndroidUtilities.bsDp(8);
                }
            } else {
                if (!LocaleController.isRTL) {
                    checkDrawLeft = timeLeft - w;
                } else {
                    checkDrawLeft = timeLeft + timeWidth + AndroidUtilities.bsDp(6.5f);
                    nameLeft += w;
                }
            }
        }

        nameWidth = Math.max(AndroidUtilities.bsDp(12), nameWidth);
        CharSequence nameStringFinal = TextUtils.ellipsize(nameString.replace("\n", " "), currentNamePaint, nameWidth - AndroidUtilities.bsDp(12), TextUtils.TruncateAt.END);
        try {
            nameLayout = new StaticLayout(nameStringFinal, currentNamePaint, nameWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }

        int messageWidth = getMeasuredWidth() - AndroidUtilities.bsDp(88);
        int avatarLeft;
        if (!LocaleController.isRTL) {
            messageLeft = AndroidUtilities.bsDp(84);
            avatarLeft = AndroidUtilities.bsDp(18);
        } else {
            messageLeft = AndroidUtilities.bsDp(22);
            avatarLeft = getMeasuredWidth() - AndroidUtilities.bsDp(84);
        }
        avatarImage.setImageCoords(avatarLeft, avatarTop, AndroidUtilities.bsDp(52), AndroidUtilities.bsDp(52));
        if (drawError) {
            int w = errorDrawable.getIntrinsicWidth() + AndroidUtilities.bsDp(8);
            messageWidth -= w;
            if (!LocaleController.isRTL) {
                errorLeft = getMeasuredWidth() - errorDrawable.getIntrinsicWidth() - AndroidUtilities.bsDp(11);
            } else {
                errorLeft = AndroidUtilities.bsDp(11);
                messageLeft += w;
            }
        } else if (countString != null) {
            countWidth = Math.max(AndroidUtilities.bsDp(12), (int)Math.ceil(countPaint.measureText(countString)));
            countLayout = new StaticLayout(countString, countPaint, countWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
            int w = countWidth + AndroidUtilities.bsDp(18);
            messageWidth -= w;
            if (!LocaleController.isRTL) {
                countLeft = getMeasuredWidth() - countWidth - AndroidUtilities.bsDp(24);
            } else {
                countLeft = AndroidUtilities.bsDp(24);
                messageLeft += w;
            }
            drawCount = true;
        } else {
            drawCount = false;
        }

        if (checkMessage) {
            if (messageString == null) {
                messageString = "";
            }
            String mess = messageString.toString();
            if (mess.length() > 150) {
                mess = mess.substring(0, 150);
            }
            mess = mess.replace("\n", " ");
            messageString = Emoji.replaceEmoji(mess, messagePaint.getFontMetricsInt(), AndroidUtilities.bsDp(17));
        }
        messageWidth = Math.max(AndroidUtilities.bsDp(12), messageWidth);
        CharSequence messageStringFinal = TextUtils.ellipsize(messageString, currentMessagePaint, messageWidth - AndroidUtilities.bsDp(12), TextUtils.TruncateAt.END);
        try {
            messageLayout = new StaticLayout(messageStringFinal, currentMessagePaint, messageWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }

        double widthpx = 0;
        float left = 0;
        if (LocaleController.isRTL) {
            if (nameLayout != null && nameLayout.getLineCount() > 0) {
                left = nameLayout.getLineLeft(0);
                if (left == 0) {
                    widthpx = Math.ceil(nameLayout.getLineWidth(0));
                    if (widthpx < nameWidth) {
                        nameLeft += (nameWidth - widthpx);
                    }
                }
            }
            if (messageLayout != null && messageLayout.getLineCount() > 0) {
                left = messageLayout.getLineLeft(0);
                if (left == 0) {
                    widthpx = Math.ceil(messageLayout.getLineWidth(0));
                    if (widthpx < messageWidth) {
                        messageLeft += (messageWidth - widthpx);
                    }
                }
            }
        } else {
            if (nameLayout != null && nameLayout.getLineCount() > 0) {
                left = nameLayout.getLineRight(0);
                if (left == nameWidth) {
                    widthpx = Math.ceil(nameLayout.getLineWidth(0));
                    if (widthpx < nameWidth) {
                        nameLeft -= (nameWidth - widthpx);
                    }
                }
            }
            if (messageLayout != null && messageLayout.getLineCount() > 0) {
                left = messageLayout.getLineRight(0);
                if (left == messageWidth) {
                    widthpx = Math.ceil(messageLayout.getLineWidth(0));
                    if (widthpx < messageWidth) {
                        messageLeft -= (messageWidth - widthpx);
                    }
                }
            }
        }
    }

    public void update(int mask) {
        if (mask != 0) {
            boolean continueUpdate = false;
            if (allowPrintStrings && (mask & MessagesController.UPDATE_MASK_USER_PRINT) != 0) {
                CharSequence printString = MessagesController.getInstance().printingStrings.get(currentDialogId);
                if (lastPrintString != null && printString == null || lastPrintString == null && printString != null || lastPrintString != null && printString != null && !lastPrintString.equals(printString)) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate && (mask & MessagesController.UPDATE_MASK_AVATAR) != 0) {
                if (chat == null) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate && (mask & MessagesController.UPDATE_MASK_NAME) != 0) {
                if (chat == null) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate && (mask & MessagesController.UPDATE_MASK_CHAT_AVATAR) != 0) {
                if (user == null) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate && (mask & MessagesController.UPDATE_MASK_CHAT_NAME) != 0) {
                if (user == null) {
                    continueUpdate = true;
                }
            }
            if (!continueUpdate && (mask & MessagesController.UPDATE_MASK_READ_DIALOG_MESSAGE) != 0) {
                if (message != null && lastUnreadState != message.isUnread()) {
                    continueUpdate = true;
                } else if (allowPrintStrings) {
                    TLRPC.TL_dialog dialog = MessagesController.getInstance().dialogs_dict.get(currentDialogId);
                    if (dialog != null && unreadCount != dialog.unread_count) {
                        unreadCount = dialog.unread_count;
                        continueUpdate = true;
                    }
                }
            }

            if (!continueUpdate) {
                return;
            }
        }
        user = null;
        chat = null;
        encryptedChat = null;

        int lower_id = (int)currentDialogId;
        int high_id = (int)(currentDialogId >> 32);
        if (lower_id != 0) {
            if (high_id == 1) {
                chat = MessagesController.getInstance().getChat(lower_id);
            } else {
                if (lower_id < 0) {
                    chat = MessagesController.getInstance().getChat(-lower_id);
                } else {
                    user = MessagesController.getInstance().getUser(lower_id);
                }
            }
        } else {
            encryptedChat = MessagesController.getInstance().getEncryptedChat(high_id);
            if (encryptedChat != null) {
                user = MessagesController.getInstance().getUser(encryptedChat.user_id);
            }
        }

        TLRPC.FileLocation photo = null;
        if (user != null) {
            if (user.photo != null) {
                photo = user.photo.photo_small;
            }
            avatarDrawable.setInfo(user);
        } else if (chat != null) {
            if (chat.photo != null) {
                photo = chat.photo.photo_small;
            }
            avatarDrawable.setInfo(chat);
        }
        avatarImage.setImage(photo, "50_50", avatarDrawable, false);

        if (getMeasuredWidth() != 0 || getMeasuredHeight() != 0) {
            buildLayout();
        } else {
            requestLayout();
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (currentDialogId == 0) {
            return;
        }

        if (drawNameLock) {
            setDrawableBounds(lockDrawable, nameLockLeft, nameLockTop);
            lockDrawable.draw(canvas);
        } else if (drawNameGroup) {
            setDrawableBounds(groupDrawable, nameLockLeft, nameLockTop);
            groupDrawable.draw(canvas);
        } else if (drawNameBroadcast) {
            setDrawableBounds(broadcastDrawable, nameLockLeft, nameLockTop);
            broadcastDrawable.draw(canvas);
        }

        if (nameLayout != null) {
            canvas.save();
            canvas.translate(nameLeft, AndroidUtilities.bsDp(22));
            nameLayout.draw(canvas);
            canvas.restore();
        }

        canvas.save();
        canvas.translate(timeLeft, timeTop);
        timeLayout.draw(canvas);
        canvas.restore();

        if (messageLayout != null) {
            canvas.save();
            canvas.translate(messageLeft, messageTop);
            messageLayout.draw(canvas);
            canvas.restore();
        }

        if (drawClock) {
            setDrawableBounds(clockDrawable, checkDrawLeft, checkDrawTop);
            clockDrawable.draw(canvas);
        } else if (drawCheck2) {
            if (drawCheck1) {
                setDrawableBounds(halfCheckDrawable, halfCheckDrawLeft, checkDrawTop);
                halfCheckDrawable.draw(canvas);
                setDrawableBounds(checkDrawable, checkDrawLeft, checkDrawTop);
                checkDrawable.draw(canvas);
            } else {
                setDrawableBounds(checkDrawable, checkDrawLeft, checkDrawTop);
                checkDrawable.draw(canvas);
            }
        }

        if (drawError) {
            setDrawableBounds(errorDrawable, errorLeft, errorTop);
            errorDrawable.draw(canvas);
        } else if (drawCount) {
            setDrawableBounds(countDrawable, countLeft - AndroidUtilities.bsDp(5.5f), countTop, countWidth + AndroidUtilities.bsDp(11), countDrawable.getIntrinsicHeight());
            countDrawable.draw(canvas);
            canvas.save();
            canvas.translate(countLeft, countTop + AndroidUtilities.bsDp(4));
            countLayout.draw(canvas);
            canvas.restore();
        }

        if (useSeparator) {
            canvas.drawBitmap(dottedBitmap, AndroidUtilities.bsDp(22), 0, null);
        }

        avatarImage.draw(canvas);
    }

    protected void setDrawableBounds(Drawable drawable, int x, int y) {
        setDrawableBounds(drawable, x, y, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    }

    protected void setDrawableBounds(Drawable drawable, int x, int y, int w, int h) {
        drawable.setBounds(x, y, x + w, y + h);
    }
}