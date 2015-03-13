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
import com.yotadevices.sdk.template.ModernWidgetFooterTemplate;

import org.telegram.android.AndroidUtilities;
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
        //        long dialog_id = MessagesController.getInstance().dialogs.get(0).id;

        BSTelegramWidgetMessages messages = BSTelegramWidgetMessages.getInstance(context);

        long dialogId = messages.getCurrentMessage(messageIndex).getDialogId();
        Bundle args = this.getChatActivityArgs(dialogId);

        SpeechRecognizerManager speechRecognizerManager = SpeechRecognizerManager.getInstance(context);
        if (speechRecognizerManager.getResult() != null) {
            args.putString(SpeechRecognizerManager.EXTRA_SPEECH_RECOGNIZER_RESULT, speechRecognizerManager.getResult());

            speechRecognizerManager.setResult(null);
        }

        Intent intent = new Intent(context, BSChatActivity.class);
        intent.putExtras(args);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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

    private PendingIntent getPendingSelfIntent(Context context, String action, int data) {
        Intent intent = new Intent(context, BSTelegramWidgetMedium.class);
        intent.setAction(action);
        intent.putExtra("data", data);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }


    private void acquireWakeLock(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);
        wakeLock.acquire();
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock.acquire(1000);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive: " + intent.getAction());

        SpeechRecognizerManager speechRecognizerManager = SpeechRecognizerManager.getInstance(context);
        switch (intent.getAction()) {
            case SpeechRecognizerManager.ACTION_SPEAK: {
                if (!speechRecognizerManager.isListening()) {
                    SpeechRecognizer speechRecognizer = speechRecognizerManager.getSpeechRecognizer();
                    speechRecognizerManager.setMessageIndex(intent.getIntExtra("data", 0));
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
        drawWidgets(context);
    }

    @Override
    public void onEnabled(Context context) {
        Log.d(LOG_TAG, "onEnabled");
        super.onEnabled(context);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(LOG_TAG, "onUpdate");
        drawWidgets(context);
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(LOG_TAG, "onDisabled");
        onFinish();
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
        releaseWakeLock();
    }

    private void drawWidget(Context context, int widgetId) {
        Log.d(LOG_TAG, "drawWidget");
        Bundle appWidgetOptions = AppWidgetManager.getInstance(context).getAppWidgetOptions(widgetId);
        int size = appWidgetOptions.getInt(BackscreenLauncherConstants.OPTION_WIDGET_SIZE, -1);
        boolean demo = appWidgetOptions.getBoolean(BackscreenLauncherConstants.OPTION_WIDGET_DEMO_MODE, false);

        RemoteViews remoteViews = new WidgetView(context, size, demo).invoke();

        AppWidgetManager.getInstance(context).updateAppWidget(widgetId, remoteViews);
    }

    private int getCountContentLine(String text) {
        TextPaint tp = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        tp.setTextSize(AndroidUtilities.dp(20));
        StaticLayout sl = new StaticLayout(text, tp, 448, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        return sl.getLineCount();
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
            mContext = context;
            initialize = true;
        }
    }


    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.appDidLogout) {
            onFinish();
        } else if (id == NotificationCenter.dialogsNeedReload || id == NotificationCenter.updateInterfaces) {
            BSTelegramWidgetMessages messages = BSTelegramWidgetMessages.getInstance(this.mContext);
            messages.reloadDialogs();
        }
        drawWidgets(mContext);
    }

    private void onFinish() {
        if (finished) {
            return;
        }
        finished = true;
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
        private final int FLAG_MEDIUM_WIDGET = 0;
        private final int FLAG_LARGE_WIDGET_SINGLE = 1;
        private final int FLAG_LARGE_WIDGET = 2;

        private Context mContext;
        private RemoteViews mRemoteViews;
        private int mWidgetSize;
        private boolean mIsDemo;

        private int mDisplayMessagesCount;
        private BSTelegramWidgetMessages mMessages;
        private SpeechRecognizerManager mSpeechRecognizerManager;

        public WidgetView(Context context, int widgetSize, boolean demo) {
            Log.d(LOG_TAG, "WidgetView.ctor: size = " + widgetSize);

            this.mContext = context;
            this.mWidgetSize = widgetSize;
            this.mIsDemo = demo;

            this.mDisplayMessagesCount = 0;
            this.mMessages = BSTelegramWidgetMessages.getInstance(this.mContext);
            this.mSpeechRecognizerManager = SpeechRecognizerManager.getInstance(mContext);

            switch (this.mWidgetSize) {
                case BackscreenLauncherConstants.WIDGET_SIZE_LARGE: {
                    this.mDisplayMessagesCount = this.mMessages.updateMessages(2);

                    if (this.mSpeechRecognizerManager.isListening()) {
                        this.mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.bs_telegram_large_widget_voice);
                    } else {
                        if (this.mDisplayMessagesCount <= 1) {
                            this.mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.bs_telegram_image_large_widget);
                        } else {
                            this.mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.bs_telegram_large_widget);
                        }
                    }


                    break;
                }
                case BackscreenLauncherConstants.WIDGET_SIZE_MEDIUM: {
                    this.mDisplayMessagesCount = this.mMessages.updateMessages(1);
                    if (this.mSpeechRecognizerManager.isListening()) {
                        this.mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.bs_telegram_medium_widget_voice);
                    } else {
                        this.mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.bs_telegram_medium_widget);
                    }


                    break;
                }
            }

            //            this.mRemoteViews.removeAllViews(R.id.widget_messages_container);
        }

        public RemoteViews invoke() {
            RemoteViews remoteViews = null;
            if (!this.mIsDemo) {
                BSTelegramWidgetMedium.this.onInit(this.mContext);
                if (UserConfig.isClientActivated()) {

                    this.mMessages.loadDialogs();
                    switch (this.mWidgetSize) {
                        case BackscreenLauncherConstants.WIDGET_SIZE_LARGE: {
                            Log.d(LOG_TAG, "Large: count: " + this.mDisplayMessagesCount);
                            remoteViews = this.buildMessageView();
                            break;
                        }
                        case BackscreenLauncherConstants.WIDGET_SIZE_MEDIUM: {
                            Log.d(LOG_TAG, "Medium: count: " + this.mDisplayMessagesCount);
                            remoteViews = this.buildMessageView();
                            break;
                        }
                    }
                } else {
                    remoteViews = this.buildOfflineMessageView();
                }
            } else {
                remoteViews = this.buildDemoMessageView();
            }

            return remoteViews;
        }

        private String getTimeText(int messageIndex) {
            String time = this.getFormattedTimeAgoString(this.mContext, this.mMessages.getMessageTime(messageIndex), false);
            String chatType = this.getChatType(this.mMessages.getCurrentMessage(messageIndex).getDialogId());
            return LocaleController.formatStringSimple("%s, %s", time, chatType);
        }


        private RemoteViews buildMessageView() {
            if (this.mDisplayMessagesCount == 0) {
                return this.buildEmptyMessageView();
            } else {
                int buttonSpeakFlag = this.FLAG_MEDIUM_WIDGET;

                this.buildRightButton();

                if (this.mSpeechRecognizerManager.isListening()) {
                    this.mRemoteViews.setTextViewText(R.id.speak_to, this.mMessages.getMessagesUser(this.mSpeechRecognizerManager.getMessageIndex()));

                } else {
                    this.mRemoteViews.setTextViewText(R.id.sms_missed, Html.fromHtml(this.mMessages.getMessageText(0)));

                    this.mRemoteViews.setOnClickPendingIntent(R.id.right_button, getMessagesActivity(this.mContext));
                    this.mRemoteViews.setOnClickPendingIntent(R.id.sms_missed_container, BSTelegramWidgetMedium.this.getChatActivity(this.mContext, 0));

                    if (this.mWidgetSize == BackscreenLauncherConstants.WIDGET_SIZE_MEDIUM) {
                        this.mRemoteViews.setViewVisibility(R.id.time_image, View.VISIBLE);

                        this.mRemoteViews.setViewVisibility(R.id.time, View.VISIBLE);
                        this.mRemoteViews.setTextViewText(R.id.time, this.getTimeText(0));
                    } else {
                        if (this.mDisplayMessagesCount == 1) {
                            buttonSpeakFlag = this.FLAG_LARGE_WIDGET_SINGLE;

                            this.mRemoteViews.setViewVisibility(R.id.time, View.GONE);
                            this.mRemoteViews.setViewVisibility(R.id.time_image, View.GONE);

                            this.mRemoteViews.setViewVisibility(R.id.extra_time_image, View.VISIBLE);

                            this.mRemoteViews.setViewVisibility(R.id.extra_time, View.VISIBLE);
                            this.mRemoteViews.setTextViewText(R.id.extra_time, this.getTimeText(0));
                        } else {
                            buttonSpeakFlag = this.FLAG_LARGE_WIDGET;

                            this.mRemoteViews.setViewVisibility(R.id.extra_time_image, View.GONE);

                            this.mRemoteViews.setViewVisibility(R.id.time_image, View.VISIBLE);

                            this.mRemoteViews.setViewVisibility(R.id.extra_time, View.VISIBLE);
                            this.mRemoteViews.setTextViewText(R.id.extra_time, this.mContext.getApplicationContext().getString(R.string.LastFromYotagram));

                            this.mRemoteViews.setViewVisibility(R.id.time, View.VISIBLE);
                            this.mRemoteViews.setTextViewText(R.id.time, this.getTimeText(0));

                            this.mRemoteViews.setViewVisibility(R.id.sms_missed_2_container, View.VISIBLE);
                            this.mRemoteViews.setTextViewText(R.id.sms_missed_2, Html.fromHtml(this.mMessages.getMessageText(1)));
                            this.mRemoteViews.setOnClickPendingIntent(R.id.sms_missed_2, BSTelegramWidgetMedium.this.getChatActivity(this.mContext, 1));
                            this.mRemoteViews.setTextViewText(R.id.time_2, this.getTimeText(1));
                        }
                    }
                    this.buildSpeakButton(buttonSpeakFlag);
                }
                return this.mRemoteViews;
            }
        }

        private void buildSpeakButton(int buttonSpeakFlag) {
            if (SpeechRecognizer.isRecognitionAvailable(this.mContext)) {
                SpeechRecognizerManager speechRecognizerManager = SpeechRecognizerManager.getInstance(mContext);

                //TODO: delete block
                if (speechRecognizerManager.isListening()) {
                    if (buttonSpeakFlag == this.FLAG_MEDIUM_WIDGET) {
                        this.mRemoteViews.setViewVisibility(R.id.button_speak, View.INVISIBLE);
                    } else if (buttonSpeakFlag == this.FLAG_LARGE_WIDGET_SINGLE) {
                        this.mRemoteViews.setViewVisibility(R.id.button_speak_hide, View.INVISIBLE);
                        this.mRemoteViews.setViewVisibility(R.id.button_speak, View.GONE);
                    } else if (buttonSpeakFlag == this.FLAG_LARGE_WIDGET) {
                        this.mRemoteViews.setViewVisibility(R.id.button_speak_hide, View.GONE);
                        this.mRemoteViews.setViewVisibility(R.id.button_speak, View.INVISIBLE);
                        this.mRemoteViews.setViewVisibility(R.id.button_speak_2, View.INVISIBLE);
                    }
                } else {
                    if (buttonSpeakFlag == this.FLAG_MEDIUM_WIDGET) {
                        this.mRemoteViews.setViewVisibility(R.id.button_speak, View.VISIBLE);
                        this.mRemoteViews.setOnClickPendingIntent(R.id.button_speak, BSTelegramWidgetMedium.this.getPendingSelfIntent(this.mContext, SpeechRecognizerManager.ACTION_SPEAK, 0));
                    } else if (buttonSpeakFlag == this.FLAG_LARGE_WIDGET_SINGLE) {
                        this.mRemoteViews.setViewVisibility(R.id.button_speak, View.GONE);
                        this.mRemoteViews.setViewVisibility(R.id.button_speak_hide, View.VISIBLE);
                        this.mRemoteViews.setOnClickPendingIntent(R.id.button_speak_hide, BSTelegramWidgetMedium.this.getPendingSelfIntent(this.mContext, SpeechRecognizerManager.ACTION_SPEAK, 0));
                    } else if (buttonSpeakFlag == this.FLAG_LARGE_WIDGET) {
                        this.mRemoteViews.setViewVisibility(R.id.button_speak_hide, View.GONE);
                        this.mRemoteViews.setViewVisibility(R.id.button_speak, View.VISIBLE);
                        this.mRemoteViews.setOnClickPendingIntent(R.id.button_speak, BSTelegramWidgetMedium.this.getPendingSelfIntent(this.mContext, SpeechRecognizerManager.ACTION_SPEAK, 0));
                        this.mRemoteViews.setViewVisibility(R.id.button_speak_2, View.VISIBLE);
                        this.mRemoteViews.setOnClickPendingIntent(R.id.button_speak_2, BSTelegramWidgetMedium.this.getPendingSelfIntent(this.mContext, SpeechRecognizerManager.ACTION_SPEAK, 1));
                    }

                    if (speechRecognizerManager.isHasError()) {
                        speechRecognizerManager.setErrorVisibility(View.VISIBLE);
                        speechRecognizerManager.setHasError(false);

                        AlarmManager alarmManager = (AlarmManager) this.mContext.getSystemService(Context.ALARM_SERVICE);
                        final PendingIntent pendingIntent = BSTelegramWidgetMedium.this.getPendingSelfIntent(this.mContext, SpeechRecognizerManager.ACTION_CLOSE_RECOGNIZER_ERROR);
                        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + SpeechRecognizerManager.SPEECH_RECOGNIZER_ERROR_DISPLAY_TIME, pendingIntent);
                    }
                }
                this.mRemoteViews.setTextViewText(R.id.recognizer_error, this.getErrorText(speechRecognizerManager.getError()));
                this.mRemoteViews.setViewVisibility(R.id.recognizer_error, speechRecognizerManager.getErrorVisibility());
            }
        }

        private void buildRightButton() {
            int count = this.mMessages.sumUnreadMessagesCount() + 1 - this.mDisplayMessagesCount;
            if (count > 1) {
                this.mRemoteViews.setViewVisibility(R.id.right_button_paperplan_image, View.VISIBLE);
                this.mRemoteViews.setViewVisibility(R.id.right_button_list_image, View.GONE);
                this.mRemoteViews.setViewVisibility(R.id.right_button_count, View.VISIBLE);
                this.mRemoteViews.setTextViewText(R.id.right_button_count, String.valueOf(count - 1));
            } else {
                this.mRemoteViews.setViewVisibility(R.id.right_button_paperplan_image, View.VISIBLE);
                this.mRemoteViews.setViewVisibility(R.id.right_button_list_image, View.VISIBLE);
            }
        }

        private RemoteViews buildEmptyMessageView() {
            this.mRemoteViews.setViewVisibility(R.id.right_button_paperplan_image, View.VISIBLE);
            this.mRemoteViews.setViewVisibility(R.id.right_button_list_image, View.VISIBLE);

            this.mRemoteViews.setTextViewText(R.id.sms_missed, this.mContext.getString(R.string.NoItems));

            this.mRemoteViews.setOnClickPendingIntent(R.id.right_button, BSTelegramWidgetMedium.this.getMessagesActivity(this.mContext));
            this.mRemoteViews.setOnClickPendingIntent(R.id.sms_missed_container, BSTelegramWidgetMedium.this.getMessagesActivity(this.mContext));

            if (this.mWidgetSize == BackscreenLauncherConstants.WIDGET_SIZE_MEDIUM) {
                this.mRemoteViews.setViewVisibility(R.id.time, View.GONE);
                this.mRemoteViews.setViewVisibility(R.id.time_image, View.GONE);
            } else {
                this.mRemoteViews.setViewVisibility(R.id.time, View.GONE);
                this.mRemoteViews.setViewVisibility(R.id.time_image, View.GONE);
                this.mRemoteViews.setViewVisibility(R.id.extra_time_image, View.GONE);
                this.mRemoteViews.setViewVisibility(R.id.extra_time, View.GONE);
            }

            return this.mRemoteViews;
        }

        private RemoteViews buildOfflineMessageView() {
            ModernWidgetFooterTemplate builder = new ModernWidgetFooterTemplate();

            this.mRemoteViews.setTextViewText(R.id.sms_missed, this.mContext.getString(R.string.StartMessaging));
            builder.showTime(false);
            builder.setText(this.mContext.getString(R.string.StartMessagingWidget));
            builder.setTime(0);
            builder.showRightButton(R.drawable.counter_paperplan, R.drawable.counter_list, getStartActivity(this.mContext));
            builder.setMaxViewActivity(getStartActivity(this.mContext));


            builder.setContentView(this.mRemoteViews);
            return builder.apply(this.mContext);
        }

        private RemoteViews buildDemoMessageView() {
            ModernWidgetFooterTemplate builder = new ModernWidgetFooterTemplate();

            builder.showTime(true);
            builder.showRightButton(R.drawable.counter_paperplan, R.drawable.counter_list, getMessagesActivity(this.mContext));
            builder.setMaxViewActivity(getMessagesActivity(this.mContext));
            builder.setTime(0);
            this.mRemoteViews.setTextViewText(R.id.sms_missed, this.mContext.getString(R.string.demo_message_from) + ": " + mContext.getString(R.string.demo_message_text));

            builder.setContentView(this.mRemoteViews);
            return builder.apply(this.mContext);
        }

        public String getFormattedTimeAgoString(Context context, long eventTime, boolean addToday) {
            long difference = Math.abs((System.currentTimeMillis() - eventTime));
            Context appContext = this.mContext.getApplicationContext();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(calendar.get(Calendar.YEAR), Calendar.JANUARY, 1, 0, 0, 0);
            long timeTillEndOfYear = System.currentTimeMillis() - calendar.getTimeInMillis();
            if (difference < 24f * 60f * 60f * 1000f) {
                SimpleDateFormat simpleDateFormat;
                if (DateFormat.is24HourFormat(context)) {
                    simpleDateFormat = (SimpleDateFormat) DateFormat.getTimeFormat(context);
                } else {
                    simpleDateFormat = (SimpleDateFormat) java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT, Locale.US);
                }
                return addToday ?
                        appContext.getString(R.string.WidgetToday, simpleDateFormat.format(new Date(eventTime))) :
                        simpleDateFormat.format(new Date(eventTime));
            } else if (difference < 30f * 24f * 60f * 60f * 1000f) {
                //TODO:
                int day = Math.round(difference / 1000f / 60f / 60f / 24f);
                switch (day) {
                    case 2:
                        return appContext.getString(R.string.WidgetYesterday);
                    default:
                        return appContext.getString(R.string.WidgetDayAgo, String.valueOf(day));
                }
            } else if (difference < timeTillEndOfYear) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM");
                return simpleDateFormat.format(new Date(eventTime));
            } else {
                return DateFormat.getDateFormat(context).format(new Date(eventTime));
            }
        }

        private String getChatType(long dialogId) {
            //TODO
            Context appContext = this.mContext.getApplicationContext();
            String chatType = appContext.getString(R.string.ChatTypePersonal);

            int chatId = 0;
            int userId = 0;
            int encId = 0;

            int lower_part = (int) dialogId;
            int high_id = (int) (dialogId >> 32);
            if (lower_part != 0) {
                if (high_id == 1) {
                    chatId = lower_part;
                } else {
                    if (lower_part > 0) {
                        userId = lower_part;
                    } else if (lower_part < 0) {
                        chatId = -lower_part;
                    }
                }
            } else {
                encId = high_id;
            }
            if (chatId != 0) {
                if (chatId > 0) {
                    chatType = appContext.getString(R.string.ChatTypeGroup);
                }
            } else if (userId != 0) {

            } else if (encId != 0) {
                chatType = appContext.getString(R.string.ChatTypeSecret);
            }
            return chatType;
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
            Log.d(SpeechRecognizerListener.LOG_TAG, "onBufferReceived: " + buffer);
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
