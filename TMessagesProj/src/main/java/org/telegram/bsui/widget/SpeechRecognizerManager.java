package org.telegram.bsui.widget;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;

/**
 * Created by W on 3/5/2015.
 */
public
class SpeechRecognizerManager
{
    private static final String LOG_TAG = "SpeechRecognizerManager";

    public static final String EXTRA_SPEECH_RECOGNIZER_RESULT       = "speech_recognizer_result";
    public static final String ACTION_SPEAK                         = "com.yotadevices.yotaphone2.telegram.bsui.widget.action.SPEAK";
    public static final String ACTION_CLOSE_RECOGNIZER_ERROR        = "com.yotadevices.yotaphone2.telegram.bsui.widget.action.CLOSE_RECOGNIZER_ERROR";
    public static final String ACTION_LISTENING_TIMEOUT_END         = "com.yotadevices.yotaphone2.telegram.bsui.widget.action.LISTENING_TIMEOUT_END";
    public static final int    SPEECH_RECOGNIZER_ERROR_DISPLAY_TIME = 3 * 1000;
    public static final int    LISTENING_TIMEOUT                    = 7 * 1000;

    private static SpeechRecognizerManager mInstance;

    private SpeechRecognizer mSpeechRecognizer;
    private Intent           mIntent;
    private boolean          mIsListening;
    private boolean          mHasError;
    private int              mErrorVisibility;
    private int              mError;
    private String           mResult;
    private int              mMessageIndex;

    public static
    SpeechRecognizerManager getInstance(Context context)
    {
        if (SpeechRecognizerManager.mInstance == null)
        {
            SpeechRecognizerManager.mInstance = new SpeechRecognizerManager(context);
        }
        return SpeechRecognizerManager.mInstance;
    }

    private static
    SpeechRecognizer createSpeechRecognizer(Context context)
    {
        Log.d(LOG_TAG, "CreateSpeechRecognizer");
        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context.getApplicationContext());
        return speechRecognizer;
    }

    private static
    Intent createRecognizerIntent(Context context)
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //            BSTelegramWidgetMedium.mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
        //                                             Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        //            BSTelegramWidgetMedium.mIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
        //                                                                    5 * 1000);
        //            BSTelegramWidgetMedium.mIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
        //                                                                    4 * 1000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                        2 * 1000);

        return intent;
    }

    public static
    void destroy()
    {
        if (SpeechRecognizerManager.mInstance != null)
        {
            Log.d(LOG_TAG, "Destroy");
            SpeechRecognizerManager.mInstance.mSpeechRecognizer.destroy();
            SpeechRecognizerManager.mInstance = null;
        }
    }

    private
    SpeechRecognizerManager(Context context)
    {
        this.mSpeechRecognizer = SpeechRecognizerManager.createSpeechRecognizer(context);
        this.mIntent = SpeechRecognizerManager.createRecognizerIntent(context);
        this.mIsListening = false;
        this.mHasError = false;
        this.mErrorVisibility = View.GONE;
        this.mError = -1;
        this.mResult = null;
    }

    public
    String getResult()
    {
        return this.mResult;
    }

    public
    void setResult(String result)
    {
        this.mResult = result;
    }

    public
    boolean isListening()
    {
        return this.mIsListening;
    }

    public
    void setListening(boolean isListening)
    {
        this.mIsListening = isListening;
    }

    public
    SpeechRecognizer getSpeechRecognizer()
    {
        return this.mSpeechRecognizer;
    }

    public
    Intent getIntent()
    {
        return this.mIntent;
    }

    public
    int getErrorVisibility()
    {
        return this.mErrorVisibility;
    }

    public
    void setErrorVisibility(int errorVisibility)
    {
        this.mErrorVisibility = errorVisibility;
    }

    public
    boolean isHasError()
    {
        return this.mHasError;
    }

    public
    void setHasError(boolean hasError)
    {
        this.mHasError = hasError;
    }

    public
    int getError()
    {
        return this.mError;
    }

    public
    void setError(int error)
    {
        this.mError = error;
    }

    public
    int getMessageIndex()
    {
        return this.mMessageIndex;
    }

    public
    void setMessageIndex(int messageIndex)
    {
        this.mMessageIndex = messageIndex;
    }
}
