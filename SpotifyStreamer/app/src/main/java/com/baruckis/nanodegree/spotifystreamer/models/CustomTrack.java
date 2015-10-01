package com.baruckis.nanodegree.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Andrius-Baruckis on 2015.
 * http://www.baruckis.com/
 */

/*
* Make track object parcelable as it allows to save and retain the list
* when user rotates the screen.
* */
public class CustomTrack implements Parcelable {

    private String id;
    private String artistsNamesList;
    private String trackName;
    private String albumName;
    private String albumImageSmallUrl;
    private String albumImageBigUrl;
    private CustomImage customImageBig;
    private String previewStreamUrl;
    private String externalUrl;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setArtistsNamesList(String artistsNamesList) {
        this.artistsNamesList = artistsNamesList;
    }

    public String getArtistsNamesList() {
        return artistsNamesList;
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

    public void setAlbumImageSmallUrl(String albumImageSmallUrl) {
        this.albumImageSmallUrl = albumImageSmallUrl;
    }

    public String getAlbumImageSmallUrl() {
        return albumImageSmallUrl;
    }

    public void setCustomImageBig(CustomImage customImageBig) {
        this.customImageBig = customImageBig;
    }

    public CustomImage getCustomImageBig() {
        return customImageBig;
    }

    public void setPreviewStreamUrl(String previewStreamUrl) {
        this.previewStreamUrl = previewStreamUrl;
    }

    public String getPreviewStreamUrl() {
        return previewStreamUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public static final Creator<CustomTrack> CREATOR = new Creator<CustomTrack>() {
        public CustomTrack createFromParcel(Parcel in) {
            CustomTrack customArtist = new CustomTrack();
            customArtist.id = in.readString();
            customArtist.artistsNamesList = in.readString();
            customArtist.trackName = in.readString();
            customArtist.albumName = in.readString();
            customArtist.albumImageSmallUrl = in.readString();
            customArtist.customImageBig = in.readParcelable(CustomImage.class.getClassLoader());
            customArtist.previewStreamUrl = in.readString();
            customArtist.externalUrl = in.readString();
            return customArtist;
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
        out.writeString(artistsNamesList);
        out.writeString(trackName);
        out.writeString(albumName);
        out.writeString(albumImageSmallUrl);
        out.writeParcelable(customImageBig, flags);
        out.writeString(previewStreamUrl);
        out.writeString(externalUrl);
    }
}