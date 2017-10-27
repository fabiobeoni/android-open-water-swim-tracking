package com.beoni.openwaterswimtracking;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

//TODO: manage FS availability for reading/writing and QUOTA
/**
 * DAO class to read/write files on app files directory.
 */
public class PrivateFileStorage
{
    private static PrivateFileStorage instance;

    /**
     * Factory method to initialize the PrivateFileStorage singleton class.
     * @return Instance of the PrivateFileStorage
     */
    public static PrivateFileStorage get(){
        if(instance==null)
            instance = new PrivateFileStorage();

        return instance;
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

        if(new File(ctx.getFilesDir(),fileName).exists())
        {
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
                } finally
                {
                    fin.close();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return content;
    }

    /**
     * Writes a string content to a file in the app.
     * files directory.
     * @param fileName Name of the file to save
     * @param content Content as text string of the file to save
     */
    public boolean writeTextFile(Context ctx, String fileName, String content){

        FileOutputStream outputStream = null;
        boolean result = false;
        try
        {
            outputStream = ctx.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());
            result = true;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (outputStream!=null)
                try
                {
                    outputStream.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
        }

        return result;
    }

}
