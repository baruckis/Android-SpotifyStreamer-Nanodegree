package com.baruckis.nanodegree.spotifystreamer;

import android.content.Context;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by Andrius-Baruckis on 2015-07-10.
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

    public static int getPreferredImageIndex(List mImagesList) {
        int imagesCount = mImagesList.size();
        int preferredImageIndex = imagesCount - 1;
        if (imagesCount > 2) {
            preferredImageIndex--;
        }
        return preferredImageIndex;
    }

    public static String getImageUrl(List<Image> imageList) {
        if (!imageList.isEmpty()) {
            return imageList.get(getPreferredImageIndex(imageList)).url;
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

}
