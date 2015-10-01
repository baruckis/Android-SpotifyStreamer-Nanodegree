package com.baruckis.nanodegree.spotifystreamer;

import android.app.Application;

/**
 * Created by Andrius-Baruckis on 2015.
 * http://www.baruckis.com/
 */

public class App extends Application {

    private boolean mIsNowPlaying = false;

    public void setIsNowPlaying(boolean isNowPlaying) {
        this.mIsNowPlaying = isNowPlaying;
    }

    public boolean isIsNowPlaying() {
        return mIsNowPlaying;
    }
}
