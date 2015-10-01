package com.baruckis.nanodegree.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by Andrius-Baruckis on 2015.
 * http://www.baruckis.com/
 */

/*
* This is Utility class for few common operations
* */
public class Utils {

    public static void fillAdapter(ArrayAdapter mListAdapter, ArrayList mList) {
        if (Build.VERSION.SDK_INT >= 11) {
            mListAdapter.addAll(mList);
        } else {
            for (Object object : mList) {
                mListAdapter.add(object);
            }
        }
    }

    public static int getPreferredImageSmallIndex(List mImagesList) {
        int imagesCount = mImagesList.size();
        int preferredImageIndex = imagesCount - 1;
        if (imagesCount > 2) {
            preferredImageIndex--;
        }
        return preferredImageIndex;
    }

    public static int getPreferredImageBigIndex() {
        int preferredImageIndex = 0;
        return preferredImageIndex;
    }

    public static String getImageSmallUrl(List<Image> imageList) {
        if (!imageList.isEmpty()) {
            return imageList.get(getPreferredImageSmallIndex(imageList)).url;
        }
        return null;
    }

    public static void setActionBar(Context context, String title, String subtitle) {
        ActionBar actionBar = null;
        if (context instanceof AppCompatActivity) {
            actionBar = ((AppCompatActivity) context).getSupportActionBar();
        }
        if (actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setSubtitle(subtitle);
        }
    }

    public static String getSecondsFromMilliseconds(int milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    public static String getAppVersionNumber(Context context){
        String versionNumber;
        try {
            versionNumber = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionNumber = "Version number not found!";
        }
        return versionNumber;
    }

    /**
     * Hides the soft keyboard
     */
    public static void hideSoftKeyboard(Activity activity) {
        View focusedView = activity.getCurrentFocus();
        hideSoftKeyboard(focusedView);
    }

    public static void hideSoftKeyboard(View focusedView) {
        if(focusedView != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) focusedView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public static void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }
}
