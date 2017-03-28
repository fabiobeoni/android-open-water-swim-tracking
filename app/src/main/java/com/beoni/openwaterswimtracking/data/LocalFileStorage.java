package com.beoni.openwaterswimtracking.data;

import android.content.Context;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * DAO class to read/write files on app files directory.
 * Async invoke from UI thread is required.
 */
public class LocalFileStorage
{
    private static LocalFileStorage instance;

    private Context mContext;

    /**
     * Factory method to initialize the LocalFileStorage singleton class.
     * @return Instance of the LocalFileStorage
     */
    public static synchronized LocalFileStorage get(Context ctx){
        if(LocalFileStorage.instance==null){
            LocalFileStorage.instance = new LocalFileStorage();
            LocalFileStorage.instance.mContext = ctx;
        }

        return LocalFileStorage.instance;
    }

    /**
     * Reads a text file from app files directory. If file is not found returns empty string.
     * @param fileName
     */
    public String readTextFile(String fileName)
    {
        int size;
        String content = "";
        FileInputStream fin = null;

        try
        {
            fin = mContext.openFileInput(fileName);

            try
            {
                while ((size = fin.read()) != -1)
                    content += Character.toString((char) size);

            } catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                fin.close();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return content;
    }

    /**
     * Writes a string content to a file in the app.
     * files directory.
     * @param fileName
     * @param content
     */
    public void writeTextFile(String fileName, String content){
        FileOutputStream outputStream;
        try {
            outputStream = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
