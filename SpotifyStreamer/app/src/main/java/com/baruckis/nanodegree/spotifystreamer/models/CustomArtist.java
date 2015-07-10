package com.baruckis.nanodegree.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Andrius-Baruckis on 2015-07-10.
 * http://www.baruckis.com/
 */

/*
* Make artist object parcelable as it allows to save and retain the list
* when user rotates the screen.
* */
public class CustomArtist implements Parcelable {

    private String id;
    private String name;
    private String imageUrl;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public static final Parcelable.Creator<CustomArtist> CREATOR = new Parcelable.Creator<CustomArtist>() {
        public CustomArtist createFromParcel(Parcel in) {
            CustomArtist mCustomArtist = new CustomArtist();
            mCustomArtist.id = in.readString();
            mCustomArtist.name = in.readString();
            mCustomArtist.imageUrl = in.readString();
            return mCustomArtist;
        }

        public CustomArtist[] newArray(int size) {
            return new CustomArtist[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(name);
        out.writeString(imageUrl);
    }
}
