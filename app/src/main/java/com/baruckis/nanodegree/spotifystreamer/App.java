/*
 * Copyright 2017 Andrius Baruckis www.baruckis.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baruckis.nanodegree.spotifystreamer;

import android.app.Application;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;


public class App extends Application {

    private SpotifyApi mSpotifyApi = new SpotifyApi();

    private boolean mIsAuthorized = false;
    private boolean mIsNowPlaying = false;


    public void setSpotifyApiAccessToken(String SpotifyAccessToken) { mSpotifyApi.setAccessToken(SpotifyAccessToken); }

    public SpotifyService getSpotifyService() { return mSpotifyApi.getService(); }

    public void setIsAuthorized(boolean isAuthorized) { this.mIsAuthorized = isAuthorized; }

    public boolean getIsAuthorized() { return mIsAuthorized; }

    public void setIsNowPlaying(boolean isNowPlaying) {
        this.mIsNowPlaying = isNowPlaying;
    }

    public boolean getIsNowPlaying() {
        return mIsNowPlaying;
    }
}
