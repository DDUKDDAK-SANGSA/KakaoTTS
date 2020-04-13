package com.ddukddak.sangsa;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class BadwordFilter extends Service {
    @Nullable
    String TAG = "BadwordFilter";
    List<String> list = new ArrayList<String>();

    @Override
    public void onStart(Intent intent, int id) {
        super.onStart(intent, id);
        Log.d(TAG, "onStart");
        readData();
        Log.d(TAG, "intent ; " + intent.getStringExtra("TextToFilter"));
        filterBadWords(intent.getStringExtra("TextToFilter"));
    }

    private void readData() {
        InputStream is = getResources().openRawResource(R.raw.word_filter);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String line = "";
        try {
            while((line = reader.readLine()) != null) {
                String[] token = line.split(",");

                list.add(token[0].trim());

                // Log.d(TAG, "CREATED : " + token[0]);
            }
        } catch (IOException e) {
            //Log.wtf(TAG, "error reading datafile on line" + line, e);
            e.printStackTrace();
        }
    }

    private void filterBadWords(String str) {
        int i = 0, j = 1;
        StringTokenizer st = new StringTokenizer(str);
        String s = st.nextToken();
        while(st.hasMoreTokens()) {
            s = st.nextToken();
            j++;
        }

        String[] strSplit = new String[j];
        st = new StringTokenizer(str);
        j = 0;
        while(j != strSplit.length) {
            strSplit[j] = st.nextToken();
            //Log.d("print strSplit", strSplit[j]);
            j++;
        }
        while(i != list.size()) {
            j = 0;
            while(j != strSplit.length) {
                if(strSplit[j].equals(list.get(i))) {
                    Log.d("check strSplit", strSplit[j]);
                    Log.d("check list", list.get(i));
                    strSplit[j] = "";
                }
                // Log.d("check j", String.valueOf(j));
                j++;
            }
            // Log.d("check i", String.valueOf(i));
            i++;
        }

        String text = "";
        j = 0;
        while(j != strSplit.length) {
            Log.d("print strFiltered", strSplit[j]);
            text.concat(strSplit[j]);
            j++;
        }
        //      Log.d("filtered list", list.get(i));
        //    Log.d("filtered word", strSplit[j]);
        //if(!word.equals(list.get(i)))

        Intent TTSIntent = new Intent(this, TextToSpeech.class);
        TTSIntent.putExtra("TextForSpeech", text);
        startService(TTSIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
