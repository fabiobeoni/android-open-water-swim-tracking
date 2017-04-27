package com.beoni.openwaterswimtracking.data;

import android.content.Context;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * DAO class to read/write files on app files directory.
 */
public class LocalFileStorage
{
    private static LocalFileStorage instance;

    /**
     * Factory method to initialize the LocalFileStorage singleton class.
     * @return Instance of the LocalFileStorage
     */
    public static synchronized LocalFileStorage get(){
        if(LocalFileStorage.instance==null){
            LocalFileStorage.instance = new LocalFileStorage();
        }

        return LocalFileStorage.instance;
    }

    /**
     * Reads a text file from app files directory. If file is not found returns empty string.
     * @param fileName Name of the file to read
     * @return content of the file as text string. Empty string if the file doesn't exist or if an exception occurs.
     */
    public String readTextFile(Context ctx, String fileName)
    {
        int size;
        String content = "";
        FileInputStream fin = null;

        try
        {
            fin = ctx.openFileInput(fileName);

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
     * @param fileName Name of the file to save
     * @param content Content as text string of the file to save
     */
    public void writeTextFile(Context ctx, String fileName, String content){
        FileOutputStream outputStream;
        try {
            outputStream = ctx.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
