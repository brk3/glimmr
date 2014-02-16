package com.bourke.glimmr.common;

import android.content.Context;
import android.os.Bundle;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class GsonHelper {

    private final Context mContext;

    public GsonHelper(Context context) {
        mContext = context;
    }

    public boolean marshallObject(Object o, String file) {
        Gson gson = new Gson();
        String json = gson.toJson(o);
        try {
            FileOutputStream fos = mContext.openFileOutput(
                    file, Context.MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean marshallObject(Object o, Bundle bundle, String key) {
        Gson gson = new Gson();
        String json = gson.toJson(o);
        bundle.putString(key, json);
        return true;
    }

    public String loadJson(String file) {
        StringBuilder json = new StringBuilder();
        try {
            FileInputStream in = mContext.openFileInput(file);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader =
                new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                json.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
