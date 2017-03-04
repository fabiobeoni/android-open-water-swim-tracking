package com.beoni.openwaterswimtracking.utils;

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
}
