package org.telegram.bsui.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.text.Html;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.yotadevices.sdk.BackscreenLauncherConstants;
import com.yotadevices.sdk.Constants;

import org.telegram.android.LocaleController;
import org.telegram.android.NotificationCenter;
import org.telegram.bsui.BSChatActivity;
import org.telegram.bsui.BSMessagesActivity;
import org.telegram.bsui.OtherFlipBSActivity;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Ji on 02.01.2015.
 */
public class BSTelegramWidgetMedium extends AppWidgetProvider implements NotificationCenter.NotificationCenterDelegate {
    private static final String LOG_TAG = "BSTelegramWidgetMedium";
    public static final String WAKELOCK_TAG = "com.yotadevices.yotaphone2.telegram.bsui.widget.WAKELOCK";
    private static boolean initialize = false;
    private boolean finished = false;
    private Context mContext;
    private PowerManager.WakeLock wakeLock;

    private Bundle getChatActivityArgs(long dialogId) {
        Bundle args = new Bundle();
        int lower_part = (int) dialogId;
        int high_id = (int) (dialogId >> 32);
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

        return args;
    }

    private PendingIntent getChatActivity(Context context, int messageIndex) {
        BSTelegramWidgetMessages messages = BSTelegramWidgetMessages.getInstance(context);

        long dialogId = messages.getDialogId(messageIndex);// messages.getCurrentMessage(messageIndex).getDialogId();
        Bundle args = this.getChatActivityArgs(dialogId);

        SpeechRecognizerManager speechRecognizerManager = SpeechRecognizerManager.getInstance(context);
        if (speechRecognizerManager.getResult() != null) {
            args.putString(SpeechRecognizerManager.EXTRA_SPEECH_RECOGNIZER_RESULT, speechRecognizerManager.getResult());

            speechRecognizerManager.setResult(null);
        }

        Intent intent = new Intent(context, BSChatActivity.class);
        intent.putExtras(args);
        return PendingIntent.getService(context, messageIndex, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getStartActivity(Context context) {
        Intent intent = new Intent(context, OtherFlipBSActivity.class);
        intent.putExtra(Constants.YOTAPHONE_EXTRA_FLAGS, Constants.YotaIntent.FLAG_BSACTIVITY_NO_HISTORY);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getMessagesActivity(Context context) {
        Intent intent = new Intent(context, BSMessagesActivity.class);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, BSTelegramWidgetMedium.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private PendingIntent getPendingSelfIntent(Context context, String action, int messageIndex) {
        Log.d(LOG_TAG, "getPendingSelfIntent messageIndex = " + messageIndex);
        Intent intent = new Intent(context, BSTelegramWidgetMedium.class);
        intent.setAction(action);

        intent.putExtra("messageIndex", messageIndex);
        return PendingIntent.getBroadcast(context, messageIndex, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void acquireWakeLock(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        this.wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);
        this.wakeLock.acquire();
    }

    private void releaseWakeLock() {
        if (this.wakeLock != null && this.wakeLock.isHeld()) {
            this.wakeLock.release();
            this.wakeLock.acquire(1000);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive: " + intent.getAction() + " (" + intent.getIntExtra("messageIndex", -1) + ")");
        SpeechRecognizerManager speechRecognizerManager = SpeechRecognizerManager.getInstance(context);
        switch (intent.getAction()) {
            case SpeechRecognizerManager.ACTION_SPEAK: {
                if (!speechRecognizerManager.isListening()) {
                    SpeechRecognizer speechRecognizer = speechRecognizerManager.getSpeechRecognizer();
                    speechRecognizerManager.setMessageIndex(intent.getIntExtra("messageIndex", 0));
                    Intent recognizerIntent = speechRecognizerManager.getIntent();
                    speechRecognizer.setRecognitionListener(new SpeechRecognizerListener(context));
                    speechRecognizer.startListening(recognizerIntent);
                }
                speechRecognizerManager.setListening(true);
                break;
            }
            case SpeechRecognizerManager.ACTION_LISTENING_TIMEOUT_END: {
                if (speechRecognizerManager.isListening()) {
                    speechRecognizerManager.getSpeechRecognizer().stopListening();
                }
                break;
            }
            case SpeechRecognizerManager.ACTION_CLOSE_RECOGNIZER_ERROR: {
                speechRecognizerManager.setErrorVisibility(View.GONE);
                break;
            }
            case BackscreenLauncherConstants.ACTION_APPWIDGET_VISIBILITY_CHANGED: {
                SpeechRecognizerManager.destroy();
                break;
            }
        }
        super.onReceive(context, intent);
        this.drawWidgets(context);
    }

    @Override
    public void onEnabled(Context context) {
        Log.d(LOG_TAG, "onEnabled");
        super.onEnabled(context);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(LOG_TAG, "onUpdate");
        this.drawWidgets(context);
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(LOG_TAG, "onDisabled");
        this.onFinish();
        super.onDisabled(context);
    }

    public void drawWidgets(Context context) {
        Log.d(LOG_TAG, "drawWidgets");
        this.acquireWakeLock(context);
        int[] allWidgetIds;
        ComponentName thisWidget = new ComponentName(context, BSTelegramWidgetMedium.class);
        allWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            this.drawWidget(context, widgetId);
        }
        this.releaseWakeLock();
    }

    private void drawWidget(Context context, int widgetId) {
        Log.d(LOG_TAG, "drawWidget, id: " + widgetId);
        Bundle appWidgetOptions = AppWidgetManager.getInstance(context).getAppWidgetOptions(widgetId);
        int size = appWidgetOptions.getInt(BackscreenLauncherConstants.OPTION_WIDGET_SIZE, -1);
        boolean demo = appWidgetOptions.getBoolean(BackscreenLauncherConstants.OPTION_WIDGET_DEMO_MODE, false);

        if (size != -1) {
            RemoteViews remoteViews = new WidgetView(context, size, demo).invoke();

            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, remoteViews);
        }
    }

    private int getCountContentLine(float textSize, String... text) {
        int widgetWidth = (int) (476f / 1.5f);//AndroidUtilities.dp(476f);/

        TextPaint textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);

        textPaint.setTextSize(textSize);
        StaticLayout staticLayout;
        int count = 0;
        for (String t : text) {
            staticLayout = new StaticLayout(t, textPaint, widgetWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            count += staticLayout.getLineCount();
        }

        return count;
    }

    private void onInit(Context context) {
        if (!initialize) {
            ApplicationLoader.postInitApplication();
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.appDidLogin);
            this.mContext = context;
            initialize = true;
        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.appDidLogout) {
            this.onFinish();
        } else if (id == NotificationCenter.dialogsNeedReload || id == NotificationCenter.updateInterfaces) {
            BSTelegramWidgetMessages messages = BSTelegramWidgetMessages.getInstance(this.mContext);
            messages.reloadDialogs();
        }
        this.drawWidgets(mContext);
    }

    private void onFinish() {
        if (this.finished) {
            return;
        }
        this.finished = true;
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.dialogsNeedReload);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageSendError);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.appDidLogout);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.appDidLogin);
        initialize = false;
        BSTelegramWidgetMessages messages = BSTelegramWidgetMessages.getInstance(this.mContext);
        messages.setDialogsLoaded(false);
        messages.clear();
    }

    private class WidgetView {
        private final int MEDIUM_WIDGET_MESSAGE_LIMIT = 1;//1
        private final int LARGE_WIDGET_MESSAGE_LIMIT = 3;//3
        private final int EXTRA_LARGE_WIDGET_MESSAGE_LIMIT = 4;//4
        private final int MESSAGE_UPDATE_LIMIT = 4;

        private final int EXTRA_LARGE_WIDGET_2_MESSAGE_LINE_LIMIT = 8;
        private final int EXTRA_LARGE_WIDGET_3_MESSAGE_LINE_LIMIT = 6;

        private Context mContext;
        private RemoteViews mRemoteViews;
        private int mWidgetSize;
        private boolean mIsDemo;
        private boolean mIsOnline;
        private boolean mShowAvatar;
        private int mDisplayMessagesCount;
        private BSTelegramWidgetMessages mMessages;
        private SpeechRecognizerManager mSpeechRecognizerManager;

        public WidgetView(Context context, int widgetSize, boolean demo) {
            Log.d(LOG_TAG, "WidgetView.ctor: size = " + widgetSize + " isDemo: " + demo);

            this.mContext = context;
            this.mWidgetSize = widgetSize;
            this.mIsDemo = demo;
            this.mShowAvatar = false;
            this.mIsOnline = UserConfig.isClientActivated();
            this.mDisplayMessagesCount = 0;
            this.mMessages = BSTelegramWidgetMessages.getInstance(this.mContext);
            this.mSpeechRecognizerManager = SpeechRecognizerManager.getInstance(mContext);

            int messageCount = this.mMessages.updateMessages(MESSAGE_UPDATE_LIMIT);

            switch (this.mWidgetSize) {
                case BackscreenLauncherConstants.WIDGET_SIZE_EXTRA_LARGE: {
                    this.mDisplayMessagesCount = Math.min(messageCount, EXTRA_LARGE_WIDGET_MESSAGE_LIMIT);
                    this.buildExtraLargeWidget();
                    break;
                }
                case BackscreenLauncherConstants.WIDGET_SIZE_LARGE: {
                    this.mDisplayMessagesCount = Math.min(messageCount, LARGE_WIDGET_MESSAGE_LIMIT);
                    this.buildLargeWidget();
                    break;
                }
                case BackscreenLauncherConstants.WIDGET_SIZE_MEDIUM: {
                    this.mDisplayMessagesCount = Math.min(messageCount, MEDIUM_WIDGET_MESSAGE_LIMIT);
                    this.buildMediumWidget();
                    break;
                }
            }
        }

        private void buildExtraLargeWidget() {
            if (this.mIsDemo) {
                this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_extra_large_widget_1_messge);
                this.mShowAvatar = true;
            } else {
                if (this.mSpeechRecognizerManager.isListening()) {
                    this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_extra_large_widget_voice);
                } else {
                    if (!this.mIsOnline || this.mDisplayMessagesCount == 0) {
                        this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_extra_large_widget_no_message);
                    } else if (this.mDisplayMessagesCount == 1) {
                        this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_extra_large_widget_1_messge);
                        this.mShowAvatar = true;
                    } else if (this.mDisplayMessagesCount == 2) {
                        this.mShowAvatar = true;

                        int linesCount32_0 = BSTelegramWidgetMedium.this.getCountContentLine(32,
                                this.mMessages.getText(0));
                        int linesCount32_1 = BSTelegramWidgetMedium.this.getCountContentLine(32,
                                this.mMessages.getText(1));

                        int linesCount32 = linesCount32_0 + linesCount32_1;
                        if (linesCount32 <= 2) {
                            Log.d(LOG_TAG, "2 msg, short single (32sp): " + linesCount32 + " lines");
                            this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_extra_large_widget_2_message_short_single_line);
                        } else {
                            int linesCount26 = BSTelegramWidgetMedium.this.getCountContentLine(26.66f,
                                    this.mMessages.getText(0),
                                    this.mMessages.getText(1));
                            Log.d(LOG_TAG, "2 msg, short double (26.66sp): " + linesCount26 + " lines");
                            if (linesCount26 <= 4) {
                                this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_extra_large_widget_2_message_short_double_line);
                            } else {
                                Log.d(LOG_TAG, "2 msg, long double (26.66sp): " + linesCount26 + " lines");

                                this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_extra_large_widget_2_message_long);

                                int message2lines = Math.min(linesCount32_1, EXTRA_LARGE_WIDGET_2_MESSAGE_LINE_LIMIT - 1);
                                int message1lines = EXTRA_LARGE_WIDGET_2_MESSAGE_LINE_LIMIT - message2lines;

                                this.mRemoteViews.setInt(R.id.sms_missed, "setMaxLines", message1lines);
                                this.mRemoteViews.setInt(R.id.sms_missed_2, "setMaxLines", message2lines);
                            }
                        }
                    } else if (this.mDisplayMessagesCount == 3) {
                        this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_extra_large_widget_3_message);

                        int linesCount32_1 = BSTelegramWidgetMedium.this.getCountContentLine(32,
                                this.mMessages.getText(1));
                        int linesCount32_2 = BSTelegramWidgetMedium.this.getCountContentLine(32,
                                this.mMessages.getText(2));

                        int message3lines = Math.min(linesCount32_2, EXTRA_LARGE_WIDGET_3_MESSAGE_LINE_LIMIT - 2);
                        int message2lines = Math.min(linesCount32_1, EXTRA_LARGE_WIDGET_3_MESSAGE_LINE_LIMIT - message3lines - 1);
                        int message1lines = EXTRA_LARGE_WIDGET_3_MESSAGE_LINE_LIMIT - message2lines - message3lines;

                        Log.e(LOG_TAG, "Lines: " + message3lines + " " + message2lines + " " + message1lines);

                        this.mRemoteViews.setInt(R.id.sms_missed, "setMaxLines", message1lines);
                        this.mRemoteViews.setInt(R.id.sms_missed_2, "setMaxLines", message2lines);
                        this.mRemoteViews.setInt(R.id.sms_missed_3, "setMaxLines", message3lines);
                    } else if (this.mDisplayMessagesCount == 4) {
                        this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_extra_large_widget_4_message);
                    }
                }
            }
        }

        private void buildLargeWidget() {
            if (this.mIsDemo) {
                this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_large_widget_1_message);
                this.mShowAvatar = true;
            } else if (!this.mIsOnline) {
                this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_large_widget_no_message);
            } else {
                if (this.mSpeechRecognizerManager.isListening()) {
                    this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_large_widget_voice);
                } else {
                    if (this.mDisplayMessagesCount == 0) {
                        this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_large_widget_no_message);
                    } else if (this.mDisplayMessagesCount == 1) {
                        this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_large_widget_1_message);
                        this.mShowAvatar = true;
                    } else if (this.mDisplayMessagesCount == 2) {
                        int linesCount = BSTelegramWidgetMedium.this.getCountContentLine(32,
                                this.mMessages.getText(0),
                                this.mMessages.getText(1));

                        if (linesCount <= 2) {
                            Log.d(LOG_TAG, "2 msg, short: " + linesCount + " lines");
                            this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_large_widget_2_message_short);
                        } else {
                            Log.d(LOG_TAG, "2 msg, long: " + linesCount + " lines");
                            this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_large_widget_2_message_long);
                        }
                    } else if (this.mDisplayMessagesCount == 3) {
                        this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_large_widget_3_message);
                    }
                }
            }
        }

        private void buildMediumWidget() {
            if (this.mIsDemo) {
                this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_medium_widget);
            } else if (!this.mIsOnline) {
                this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_medium_widget_no_message);
            } else {
                if (this.mSpeechRecognizerManager.isListening()) {
                    this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_medium_widget_voice);
                } else {
                    if (this.mDisplayMessagesCount == 0) {
                        this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_medium_widget_no_message);
                    } else if (this.mDisplayMessagesCount == 1) {
                        this.mRemoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.bs_telegram_medium_widget);
                    }
                }
            }
        }

        public RemoteViews invoke() {
            if (!this.mIsDemo) {
                BSTelegramWidgetMedium.this.onInit(this.mContext);
                if (this.mIsOnline) {
                    this.mMessages.loadDialogs();
                    if (this.mDisplayMessagesCount == 0) {
                        this.buildEmptyMessageView();
                    } else {
                        this.buildMessageView();
                    }
                } else {
                    this.buildOfflineMessageView();
                }
            } else {
                this.buildDemoMessageView();
            }

            return this.mRemoteViews;
        }

        private String getTimeText(int messageIndex) {
            String time = this.formatDate(this.mMessages.getTime(messageIndex));
            return LocaleController.formatStringSimple("%s, %s", time, this.mMessages.getChatTypeName(this.mMessages.getChatType(messageIndex)));
        }

        private String formatDate(long date) {
            Calendar rightNow = Calendar.getInstance(Locale.getDefault());
            int day = rightNow.get(Calendar.DAY_OF_YEAR);
            int year = rightNow.get(Calendar.YEAR);
            rightNow.setTimeInMillis(date * 1000);
            int dateDay = rightNow.get(Calendar.DAY_OF_YEAR);
            int dateYear = rightNow.get(Calendar.YEAR);
            SimpleDateFormat format;
            String result;
            Date d = new Date(date * 1000);

            if (dateDay == day && year == dateYear) {
                if (DateFormat.is24HourFormat(ApplicationLoader.applicationContext)) {
                    format = new SimpleDateFormat("HH:mm", Locale.getDefault());
                } else {
                    format = new SimpleDateFormat("h:mm a", Locale.US);
                }
                result = format.format(d);
            } else if (dateDay + 1 == day && year == dateYear) {
                result = this.mContext.getString(R.string.WidgetYesterday);
            } else if (year == dateYear) {
                if (DateFormat.is24HourFormat(ApplicationLoader.applicationContext)) {
                    format = new SimpleDateFormat("d MMM HH:mm", Locale.getDefault());
                } else {
                    format = new SimpleDateFormat("d MMM hh:mm a", Locale.US);
                }
                result = format.format(d);
            } else {
                result = this.mContext.getString(R.string.WidgetLastYear);
            }

            return result;
        }

        private void buildMessageView() {
            if (this.mShowAvatar) {
                this.buildAvatar();
            }

            this.buildRightButton();

            if (this.mSpeechRecognizerManager.isListening()) {
                this.mRemoteViews.setTextViewText(R.id.speak_to, this.mMessages.getUserName(this.mSpeechRecognizerManager.getMessageIndex()));
            } else {
                if (this.mDisplayMessagesCount == 1) {
                    this.setOnClickMessage(R.id.widget_root);
                    this.buildMessage(R.id.sms_missed);
                    this.buildSpeakButton(R.id.button_speak);
                    this.buildTime(R.id.time);
                } else if (this.mDisplayMessagesCount == 2) {
                    this.setOnClickMessage(R.id.sms_missed_container_2, R.id.sms_missed_container);
                    this.buildMessage(R.id.sms_missed_2, R.id.sms_missed);
                    this.buildTime(R.id.time_2, R.id.time);
                    this.buildSpeakButton(R.id.button_speak_2, R.id.button_speak);
                } else if (this.mDisplayMessagesCount == 3) {
                    this.setOnClickMessage(R.id.sms_missed_container_3, R.id.sms_missed_container_2, R.id.sms_missed_container);
                    this.buildMessage(R.id.sms_missed_3, R.id.sms_missed_2, R.id.sms_missed);
                    this.buildTime(R.id.time_3, R.id.time_2, R.id.time);
                    this.buildSpeakButton(R.id.button_speak_3, R.id.button_speak_2, R.id.button_speak);
                } else if (this.mDisplayMessagesCount == 4) {
                    this.setOnClickMessage(R.id.sms_missed_container_4, R.id.sms_missed_container_3, R.id.sms_missed_container_2, R.id.sms_missed_container);
                    this.buildMessage(R.id.sms_missed_4, R.id.sms_missed_3, R.id.sms_missed_2, R.id.sms_missed);
                    this.buildTime(R.id.time_4, R.id.time_3, R.id.time_2, R.id.time);
                    this.buildSpeakButton(R.id.button_speak_4, R.id.button_speak_3, R.id.button_speak_2, R.id.button_speak);
                }

                this.buildError();
            }
        }

        private void buildAvatar() {
            this.mRemoteViews.setImageViewBitmap(R.id.user_avatar, this.mMessages.getAvatar(0));
        }

        private void buildMessage(int... resourceIds) {
            for (int i = 0; i < resourceIds.length; i++) {
                this.mRemoteViews.setTextViewText(resourceIds[i], Html.fromHtml(this.mMessages.getText(i)));
            }
        }

        private void setOnClickMessage(int... resourceIds) {
            for (int i = 0; i < resourceIds.length; i++) {
                this.mRemoteViews.setOnClickPendingIntent(resourceIds[i], BSTelegramWidgetMedium.this.getChatActivity(this.mContext, i));
            }
        }

        private void buildTime(int... resourceIds) {
            for (int i = 0; i < resourceIds.length; i++) {
                this.mRemoteViews.setTextViewText(resourceIds[i], this.getTimeText(i));
            }
        }

        private void buildError() {
            if (!this.mSpeechRecognizerManager.isListening() && this.mSpeechRecognizerManager.isHasError()) {
                this.mSpeechRecognizerManager.setErrorVisibility(View.VISIBLE);
                this.mSpeechRecognizerManager.setHasError(false);

                AlarmManager alarmManager = (AlarmManager) this.mContext.getSystemService(Context.ALARM_SERVICE);
                final PendingIntent pendingIntent = BSTelegramWidgetMedium.this.getPendingSelfIntent(this.mContext, SpeechRecognizerManager.ACTION_CLOSE_RECOGNIZER_ERROR);
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + SpeechRecognizerManager.SPEECH_RECOGNIZER_ERROR_DISPLAY_TIME, pendingIntent);
            }
            this.mRemoteViews.setTextViewText(R.id.recognizer_error, this.getErrorText(this.mSpeechRecognizerManager.getError()));
            this.mRemoteViews.setViewVisibility(R.id.recognizer_error, this.mSpeechRecognizerManager.getErrorVisibility());
        }

        private void buildSpeakButton(int... resourceIds) {
            if (SpeechRecognizer.isRecognitionAvailable(this.mContext)) {
                if (this.mSpeechRecognizerManager.isListening()) {
                    for (int i = 0; i < resourceIds.length; i++) {
                        this.mRemoteViews.setViewVisibility(resourceIds[i], View.INVISIBLE);
                    }
                } else {
                    for (int i = 0; i < resourceIds.length; i++) {
                        this.mRemoteViews.setViewVisibility(resourceIds[i], View.VISIBLE);
                        this.mRemoteViews.setOnClickPendingIntent(resourceIds[i], BSTelegramWidgetMedium.this.getPendingSelfIntent(this.mContext, SpeechRecognizerManager.ACTION_SPEAK, i));
                    }
                }
            }
        }

        private void buildRightButton() {
            int count = this.mMessages.sumUnreadMessagesCount() + 1 - this.mDisplayMessagesCount;
            if (count > 1) {
                this.buildRightButtonCount(count);
            } else {
                this.buildRightButtonList();
            }
            this.mRemoteViews.setOnClickPendingIntent(R.id.right_button, BSTelegramWidgetMedium.this.getMessagesActivity(this.mContext));
        }

        private void buildRightButtonCount(int count) {
            this.mRemoteViews.setViewVisibility(R.id.right_button_list_image, View.GONE);
            this.mRemoteViews.setViewVisibility(R.id.right_button_count, View.VISIBLE);
            this.mRemoteViews.setTextViewText(R.id.right_button_count, String.valueOf(count - 1));
        }

        private void buildRightButtonList() {
            this.mRemoteViews.setViewVisibility(R.id.right_button_list_image, View.VISIBLE);
            this.mRemoteViews.setViewVisibility(R.id.right_button_count, View.GONE);
        }

        private void buildEmptyMessageView() {
            this.buildRightButtonList();

            this.mRemoteViews.setTextViewText(R.id.widget_message, this.mContext.getString(R.string.NoItems));
            this.mRemoteViews.setOnClickPendingIntent(R.id.widget_root, BSTelegramWidgetMedium.this.getMessagesActivity(this.mContext));
            this.mRemoteViews.setTextViewText(R.id.yotagram_text, this.mContext.getString(R.string.Yotagram));
        }

        private void buildOfflineMessageView() {
            this.buildRightButtonList();

            this.mRemoteViews.setTextViewText(R.id.widget_message, this.mContext.getString(R.string.StartMessagingWidget));
            this.mRemoteViews.setOnClickPendingIntent(R.id.widget_root, BSTelegramWidgetMedium.this.getStartActivity(this.mContext));
            this.mRemoteViews.setTextViewText(R.id.yotagram_text, this.mContext.getString(R.string.Authorize));
        }

        private void buildDemoMessageView() {
            this.buildRightButtonCount(this.mContext.getResources().getInteger(R.integer.demo_messages_count));

            if (this.mShowAvatar) {
                this.mRemoteViews.setImageViewBitmap(R.id.user_avatar,
                        this.mMessages.getDemoAvatar(
                                this.mContext.getString(R.string.demo_message_from_first_name),
                                this.mContext.getString(R.string.demo_message_from_last_name)));
            }

            String messageText = LocaleController.formatStringSimple("<b>%s %s:</b> %s",
                    this.mContext.getString(R.string.demo_message_from_first_name),
                    this.mContext.getString(R.string.demo_message_from_last_name),
                    this.mContext.getString(R.string.demo_message_text));
            this.mRemoteViews.setTextViewText(R.id.sms_missed, Html.fromHtml(messageText));
            this.mRemoteViews.setTextViewText(R.id.time, this.formatDate(System.currentTimeMillis() / 1000 - 60 * 60 * 23));
        }

        private String getErrorText(int errorCode) {
            String message;
            Context appContext = this.mContext.getApplicationContext();
            switch (errorCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = appContext.getString(R.string.SpeechRecognizerErrorAudio);
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = appContext.getString(R.string.SpeechRecognizerErrorClient);
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = appContext.getString(R.string.SpeechRecognizerErrorInsufficientPermissions);
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = appContext.getString(R.string.SpeechRecognizerErrorNetwork);
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = appContext.getString(R.string.SpeechRecognizerErrorNetworkTimeout);
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = appContext.getString(R.string.SpeechRecognizerErrorNoMatch);
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = appContext.getString(R.string.SpeechRecognizerErrorRecognizerBusy);
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = appContext.getString(R.string.SpeechRecognizerErrorServer);
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = appContext.getString(R.string.SpeechRecognizerErrorSpeechTimeout);
                    break;
                default:
                    message = appContext.getString(R.string.SpeechRecognizerError);
                    break;
            }
            return message;
        }
    }

    private class SpeechRecognizerListener implements RecognitionListener {
        public static final String LOG_TAG = "RecognizerListener";

        private PendingIntent mStopListeningAction;
        private Context mContext;
        private AlarmManager mAlarmManager;

        public SpeechRecognizerListener(Context context) {
            this.mContext = context;
            this.mStopListeningAction = BSTelegramWidgetMedium.this.getPendingSelfIntent(this.mContext, SpeechRecognizerManager.ACTION_LISTENING_TIMEOUT_END);
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService(Context.ALARM_SERVICE);
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d(SpeechRecognizerListener.LOG_TAG, "onBeginningOfSpeech");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.d(SpeechRecognizerListener.LOG_TAG, "onBufferReceived");
        }

        @Override
        public void onEndOfSpeech() {
            Log.d(SpeechRecognizerListener.LOG_TAG, "onEndOfSpeech");
            SpeechRecognizerManager speechRecognizerManager = SpeechRecognizerManager.getInstance(this.mContext);
            speechRecognizerManager.setListening(false);
            BSTelegramWidgetMedium.this.drawWidgets(this.mContext);
        }

        @Override
        public void onError(int errorCode) {
            Log.d(SpeechRecognizerListener.LOG_TAG, "onError: " + errorCode);

            this.mAlarmManager.cancel(this.mStopListeningAction);

            SpeechRecognizerManager speechRecognizerManager = SpeechRecognizerManager.getInstance(this.mContext);
            speechRecognizerManager.setError(errorCode);
            speechRecognizerManager.setHasError(true);
            speechRecognizerManager.setListening(false);

            BSTelegramWidgetMedium.this.drawWidgets(this.mContext);
        }

        @Override
        public void onEvent(int arg0, Bundle arg1) {
            Log.d(SpeechRecognizerListener.LOG_TAG, "onEvent");
        }

        @Override
        public void onPartialResults(Bundle arg0) {
            Log.d(SpeechRecognizerListener.LOG_TAG, "onPartialResults");
        }

        @Override
        public void onReadyForSpeech(Bundle arg0) {
            Log.d(SpeechRecognizerListener.LOG_TAG, "onReadyForSpeech");

            this.mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + SpeechRecognizerManager.LISTENING_TIMEOUT, this.mStopListeningAction);
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            Log.d(SpeechRecognizerListener.LOG_TAG, "onResults: matches:" + matches);

            this.mAlarmManager.cancel(this.mStopListeningAction);

            SpeechRecognizerManager speechRecognizerManager = SpeechRecognizerManager.getInstance(this.mContext);
            speechRecognizerManager.setResult(matches.get(0));
            speechRecognizerManager.setListening(false);

            PendingIntent pendingIntent = BSTelegramWidgetMedium.this.getChatActivity(this.mContext, speechRecognizerManager.getMessageIndex());

            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            Log.d(SpeechRecognizerListener.LOG_TAG, "onRmsChanged: " + rmsdB);
        }
    }
}
