package org.telegram.bsui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Point;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.MessageObject;
import org.telegram.android.MessagesController;
import org.telegram.messenger.TLRPC;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by E1ektr0 on 04.01.2015.
 */
public class BSMessageObject extends MessageObject {

    private Context context;
    private static TextPaint textPaint;

    public BSMessageObject(TLRPC.Message message, AbstractMap<Integer, TLRPC.User> users) {
        super(message, users, true);
    }

    @Override
    protected void initTextPaint() {
        if (textPaint == null) {
            textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(0xff000000);
            textPaint.linkColor = 0xff316f9f;
        }
        textPaint.setTextSize(dp(MessagesController.getInstance().fontSize));
    }

    @Override
    protected TextPaint getTextPaint() {
        return textPaint;
    }

    @Override
    protected int getDisplayY() {
        return getDisplaySize().y;
    }

    @Override
    protected int getDisplayX() {
        return getDisplaySize().x;
    }

    private Point getDisplaySize() {
        return AndroidUtilities.bsDisplaySize;
    }

    @Override
    protected int dp(float value) {
        return AndroidUtilities.bsDp(value);
    }


    private ArrayList<String> pullLinks(String text) {
        ArrayList links = new ArrayList();

        String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        while(m.find()) {
            String urlStr = m.group();
            if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
                urlStr = urlStr.substring(1, urlStr.length() - 1);
            }
            links.add(urlStr);
        }
        return links;
    }
    @Override
    protected void clickify(CharSequence messageText, int i) {

        String string = messageText.toString();
        ArrayList<String> list = pullLinks(string);
        if(list.isEmpty())
            return;
        for(String link : list)
        {
            spanned((Spannable) messageText, string, link);
        }


    }

    private void spanned(Spannable messageText, String string, String link) {
        final String clickableText = link;
        int start = string.indexOf(clickableText);
        int end = start + clickableText.length();
        if (start == -1)
            return;
        ClickSpan span = new ClickSpan(new ClickSpan.OnClickListener() {
            @Override
            public void onClick() {
                Intent i = new Intent();
                i.setComponent(new ComponentName("com.yotadevices.yotaphone.yd_browser", "com.yotadevices.yotaphone.yd_browser.BSBrowser"));
                i.putExtra("URL_TO_OPEN", clickableText);
                context.startService(i);
            }
        });
        ((Spannable)messageText).setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
}
