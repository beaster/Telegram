package org.telegram.bsui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;

import org.telegram.android.ImageLoader;
import org.telegram.android.MediaController;
import org.telegram.android.MessageObject;
import org.telegram.android.MessagesController;
import org.telegram.bsui.Components.BSSeekBar;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.ui.Components.ProgressView;

import java.io.File;

/**
 * Created by E1ektr0 on 04.01.2015.
 */
public class BSChatAudioCell extends BSChatBaseCell implements BSSeekBar.SeekBarDelegate, MediaController.FileDownloadProgressListener {

    private static Drawable[][] statesDrawable = new Drawable[8][2];
    private static TextPaint timePaint;

    protected BSSeekBar seekBar;
    private ProgressView progressView;
    private int seekBarX;
    private int seekBarY;

    private int buttonState = 0;
    private int buttonX;
    private int buttonY;
    private boolean buttonPressed = false;

    private StaticLayout timeLayout;
    private int timeX;
    private String lastTimeString = null;

    private int TAG;

    public TLRPC.User audioUser;

    public BSChatAudioCell(Context context) {
        super(context);
        TAG = MediaController.getInstance().generateObserverTag();

        progressView = new ProgressView();
        initAudio(context);
    }

    protected void initAudio(Context context) {
        seekBar = new BSSeekBar(context);
        seekBar.delegate = this;

        if (timePaint == null) {
            statesDrawable[0][0] = getResources().getDrawable(R.drawable.play_bs);
            statesDrawable[0][1] = getResources().getDrawable(R.drawable.play_bs);
            statesDrawable[1][0] = getResources().getDrawable(R.drawable.pause_bs);
            statesDrawable[1][1] = getResources().getDrawable(R.drawable.pause_bs);
            statesDrawable[2][0] = getResources().getDrawable(R.drawable.doc_download_bs);
            statesDrawable[2][1] = getResources().getDrawable(R.drawable.doc_download_bs);
            statesDrawable[3][0] = getResources().getDrawable(R.drawable.audiocancel_bs);
            statesDrawable[3][1] = getResources().getDrawable(R.drawable.audiocancel_bs);

            statesDrawable[4][0] = getResources().getDrawable(R.drawable.play_bs);
            statesDrawable[4][1] = getResources().getDrawable(R.drawable.play_bs);
            statesDrawable[5][0] = getResources().getDrawable(R.drawable.pause_bs);
            statesDrawable[5][1] = getResources().getDrawable(R.drawable.pause_bs);
            statesDrawable[6][0] = getResources().getDrawable(R.drawable.doc_download_bs);
            statesDrawable[6][1] = getResources().getDrawable(R.drawable.doc_download_bs);
            statesDrawable[7][0] = getResources().getDrawable(R.drawable.audiocancel_bs);
            statesDrawable[7][1] = getResources().getDrawable(R.drawable.audiocancel_bs);

            timePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaint.setTextSize(dp(12));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MediaController.getInstance().removeLoadingFileObserver(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        boolean result = seekBar.onTouch(event.getAction(), event.getX() - seekBarX, event.getY() - seekBarY);
        if (result) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            invalidate();
        } else {
            int side = dp(36);
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (x >= buttonX && x <= buttonX + side && y >= buttonY && y <= buttonY + side) {
                    buttonPressed = true;
                    invalidate();
                    result = true;
                }
            } else if (buttonPressed) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    buttonPressed = false;
                    playSoundEffect(SoundEffectConstants.CLICK);
                    didPressedButton();
                    invalidate();
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonPressed = false;
                    invalidate();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!(x >= buttonX && x <= buttonX + side && y >= buttonY && y <= buttonY + side)) {
                        buttonPressed = false;
                        invalidate();
                    }
                }
            }
            if (!result) {
                result = super.onTouchEvent(event);
            }
        }

        return result;
    }

    private void didPressedButton() {
        if (buttonState == 0) {
            boolean result = MediaController.getInstance().playAudio(currentMessageObject);
            if (result) {
                buttonState = 1;
                invalidate();
            }
        } else if (buttonState == 1) {
            boolean result = MediaController.getInstance().pauseAudio(currentMessageObject);
            if (result) {
                buttonState = 0;
                invalidate();
            }
        } else if (buttonState == 2) {
            FileLoader.getInstance().loadFile(currentMessageObject.messageOwner.media.audio, true);
            buttonState = 3;
            invalidate();
        } else if (buttonState == 3) {
            FileLoader.getInstance().cancelLoadFile(currentMessageObject.messageOwner.media.audio);
            buttonState = 2;
            invalidate();
        }
    }

    public void updateProgress() {
        if (currentMessageObject == null) {
            return;
        }

        if (!seekBar.isDragging()) {
            seekBar.setProgress(currentMessageObject.audioProgress);
        }

        int duration = 0;
        if (!MediaController.getInstance().isPlayingAudio(currentMessageObject)) {
            duration = currentMessageObject.messageOwner.media.audio.duration;
        } else {
            duration = currentMessageObject.audioProgressSec;
        }
        String timeString = String.format("%02d:%02d", duration / 60, duration % 60);
        if (lastTimeString == null || lastTimeString != null && !lastTimeString.equals(timeString)) {
            int timeWidth = (int)Math.ceil(timePaint.measureText(timeString));
            timeLayout = new StaticLayout(timeString, timePaint, timeWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        }
        invalidate();
    }

    public void downloadAudioIfNeed() {
        if (buttonState == 2) {
            FileLoader.getInstance().loadFile(currentMessageObject.messageOwner.media.audio, true);
            buttonState = 3;
            invalidate();
        }
    }

    public void updateButtonState() {
        String fileName = currentMessageObject.getFileName();
        File cacheFile = FileLoader.getPathToMessage(currentMessageObject.messageOwner);
        if (cacheFile.exists()) {
            MediaController.getInstance().removeLoadingFileObserver(this);
            boolean playing = MediaController.getInstance().isPlayingAudio(currentMessageObject);
            if (!playing || playing && MediaController.getInstance().isAudioPaused()) {
                buttonState = 0;
            } else {
                buttonState = 1;
            }
            progressView.setProgress(0);
        } else {
            MediaController.getInstance().addLoadingFileObserver(fileName, this);
            if (!FileLoader.getInstance().isLoadingFile(fileName)) {
                buttonState = 2;
                progressView.setProgress(0);
            } else {
                buttonState = 3;
                Float progress = ImageLoader.getInstance().getFileProgress(fileName);
                if (progress != null) {
                    progressView.setProgress(progress);
                } else {
                    progressView.setProgress(0);
                }
            }
        }
        updateProgress();
    }

    @Override
    public void onFailedDownload(String fileName) {
        updateButtonState();
    }

    @Override
    public void onSuccessDownload(String fileName) {
        updateButtonState();
    }

    @Override
    public void onProgressDownload(String fileName, float progress) {
        progressView.setProgress(progress);
        if (buttonState != 3) {
            updateButtonState();
        }
        invalidate();
    }

    @Override
    public void onProgressUpload(String fileName, float progress, boolean isEncrypted) {

    }

    @Override
    public int getObserverTag() {
        return TAG;
    }

    @Override
    public void onSeekBarDrag(float progress) {
        if (currentMessageObject == null) {
            return;
        }
        currentMessageObject.audioProgress = progress;
        MediaController.getInstance().seekToProgress(currentMessageObject, progress);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, dp(75));
        if (isChat) {
            backgroundWidth = Math.min(width - dp(102), dp(300));
        } else {
            backgroundWidth = Math.min(width - dp(50), dp(300));
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int x;

        if (currentMessageObject.isOut()) {
            x = layoutWidth - backgroundWidth + dp(8);
            seekBarX = layoutWidth - backgroundWidth + dp(135);
            buttonX = layoutWidth - backgroundWidth + dp(65);
            timeX = layoutWidth - backgroundWidth + dp(101);
        } else {
            /*if (isChat) {
                x = dp(69);
                seekBarX = dp(158);
                buttonX = dp(128);
                timeX = dp(132);
            } else {*/
                x = dp(16);
                seekBarX = dp(145);
                buttonX = dp(74);
                timeX = dp(111);
//            }
        }
        int diff = dp(56);
        seekBarX -= diff;
        buttonX -= diff;
        timeX -= diff;

        seekBar.width = backgroundWidth - dp(112);
        seekBar.height = dp(30);
        progressView.width = backgroundWidth - dp(136);
        progressView.height = dp(30);
        seekBarY = dp(10);
        buttonY = dp(6);

        updateProgress();
    }

    @Override
    public void setMessageObject(MessageObject messageObject) {
        if (currentMessageObject != messageObject || isUserDataChanged()) {
            int uid = messageObject.messageOwner.media.audio.user_id;
            if (uid == 0) {
                uid = messageObject.messageOwner.from_id;
            }
            audioUser = MessagesController.getInstance().getUser(uid);

            if (messageObject.isOut()) {
                seekBar.type = 0;
                progressView.setProgressColors(0xff000000, 0xff000000);
            } else {
                seekBar.type = 1;
                progressView.setProgressColors(0xff000000, 0xff000000);
            }

            super.setMessageObject(messageObject);
        }
        updateButtonState();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentMessageObject == null) {
            return;
        }

        canvas.save();
        if (buttonState == 0 || buttonState == 1) {
            canvas.translate(seekBarX, seekBarY);
            seekBar.draw(canvas);
        } else {
            canvas.translate(seekBarX + dp(12), seekBarY);
            progressView.draw(canvas);
        }
        canvas.restore();

        int state = buttonState;
        if (!currentMessageObject.isOut()) {
            state += 4;
            timePaint.setColor(0xff000000);
        } else {
            timePaint.setColor(0xff000000);
        }
        Drawable buttonDrawable = statesDrawable[state][buttonPressed ? 1 : 0];
        int side = dp(36);
        int x = (side - buttonDrawable.getIntrinsicWidth()) / 2;
        int y = (side - buttonDrawable.getIntrinsicHeight()) / 2;
        setDrawableBounds(buttonDrawable, x + buttonX, y + buttonY);
        buttonDrawable.draw(canvas);

        canvas.save();
        canvas.translate(timeX, seekBarY + dp(6.5f));
        timeLayout.draw(canvas);
        canvas.restore();
    }
}
