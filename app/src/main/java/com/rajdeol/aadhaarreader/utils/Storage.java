package com.rajdeol.aadhaarreader.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Class to save data into internal storage
 * This data gets deleted when the app is uninstalled
 * Created by RajinderPal on 6/16/2016.
 */
public class Storage {
    protected Context mContext;
    protected static final String STORAGE_FILE_NAME = "data_storage.txt";

    /**
     * constructor
     * @param activity
     */
    public Storage(Context activity){
        mContext = activity;
    }

    /**
     * Write to storage file
     * @param data
     */
    public void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(mContext.openFileOutput(STORAGE_FILE_NAME, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    /**
     * Read from storage file
     * @return String
     */
    public String readFromFile() {
        // ensure file is created
        checkFilePresent(STORAGE_FILE_NAME);

        String ret = "";

        try {
            InputStream inputStream = mContext.openFileInput(STORAGE_FILE_NAME);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("Storage activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("Storage activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private void checkFilePresent(String fileName) {
        String path = mContext.getFilesDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
