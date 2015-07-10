package com.baruckis.nanodegree.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Andrius-Baruckis on 2015-07-10.
 * http://www.baruckis.com/
 */

/*
* Make track object parcelable as it allows to save and retain the list
* when user rotates the screen.
* */
public class CustomTrack implements Parcelable {

    private String id;
    private String trackName;
    private String albumName;
    private String albumImageUrl;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumImageUrl(String albumImageUrl) {
        this.albumImageUrl = albumImageUrl;
    }

    public String getAlbumImageUrl() {
        return albumImageUrl;
    }

    public static final Creator<CustomTrack> CREATOR = new Creator<CustomTrack>() {
        public CustomTrack createFromParcel(Parcel in) {
            CustomTrack mCustomArtist = new CustomTrack();
            mCustomArtist.id = in.readString();
            mCustomArtist.trackName = in.readString();
            mCustomArtist.albumName = in.readString();
            mCustomArtist.albumImageUrl = in.readString();
            return mCustomArtist;
        }

        public CustomTrack[] newArray(int size) {
            return new CustomTrack[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(trackName);
        out.writeString(albumName);
        out.writeString(albumImageUrl);
    }
}
