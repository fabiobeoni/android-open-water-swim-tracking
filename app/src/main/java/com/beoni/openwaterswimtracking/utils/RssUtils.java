package com.beoni.openwaterswimtracking.utils;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RssUtils
{
    public static ArrayList<String> getImagesURL(String rssHTMLContent){
        Pattern p = Pattern.compile("src=\"(.*?)\"");
        Matcher m = p.matcher(rssHTMLContent);
        ArrayList<String> urls = new ArrayList<>();
        while(m.find())
            //Displaying the url
            urls.add(m.group(1));

        return urls;
    }

    public static Spanned removeImageSpanObjects(String inStr) {
        SpannableStringBuilder spannedStr = (SpannableStringBuilder) Html
                .fromHtml(inStr.trim());
        Object[] spannedObjects = spannedStr.getSpans(0, spannedStr.length(),
                Object.class);
        for (int i = 0; i < spannedObjects.length; i++) {
            if (spannedObjects[i] instanceof ImageSpan) {
                ImageSpan imageSpan = (ImageSpan) spannedObjects[i];
                spannedStr.replace(spannedStr.getSpanStart(imageSpan),
                        spannedStr.getSpanEnd(imageSpan), "");
            }
        }
        return spannedStr;
    }
}
