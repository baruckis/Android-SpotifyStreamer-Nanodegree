package com.baruckis.nanodegree.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by Andrius-Baruckis on 2015.
 * http://www.baruckis.com/
 */

public class CustomImage extends Image implements Parcelable {

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getWidth() {
        return width;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getHeight() {
        return height;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public static final Creator<CustomImage> CREATOR = new Creator<CustomImage>() {
        public CustomImage createFromParcel(Parcel in) {
            CustomImage customImage = new CustomImage();
            customImage.width = in.readInt();
            customImage.height = in.readInt();
            customImage.url = in.readString();
            return customImage;
        }

        public CustomImage[] newArray(int size) {
            return new CustomImage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(width);
        out.writeInt(height);
        out.writeString(url);
    }
}
