package com.baruckis.nanodegree.spotifystreamer.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baruckis.nanodegree.spotifystreamer.InfoView;
import com.baruckis.nanodegree.spotifystreamer.R;
import com.baruckis.nanodegree.spotifystreamer.Utils;
import com.baruckis.nanodegree.spotifystreamer.activities.ArtistTracksListActivity;
import com.baruckis.nanodegree.spotifystreamer.activities.MainArtistsListActivity;
import com.baruckis.nanodegree.spotifystreamer.adapters.TrackArrayAdapter;
import com.baruckis.nanodegree.spotifystreamer.models.CustomTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Andrius-Baruckis on 2015-07-10.
 * http://www.baruckis.com/
 */

/**
 * A fragment representing a single Item detail screen (artist top tracks).
 * This fragment is either contained in a {@link MainArtistsListActivity}
 * in two-pane mode (on tablets) or a {@link ArtistTracksListActivity}
 * on handsets.
 */
public class ArtistTracksListFragment extends ListFragment {
    /**
     * The fragment argument representing the artist ID that this fragment
     * represents.
     */
    public static final String ARG_ACTIVATED_ARTIST_ID = "activated_artist_id";
    public static final String ARG_ACTIVATED_ARTIST_NAME = "activated_artist_name";
    private static final String STATE_TRACKS_LIST = "tracks_list";

    private String mArtistId = null;
    private String mArtistName = null;

    private SpotifyService mSpotifyService;

    private TrackArrayAdapter mListAdapter;

    private InfoView mInfoView;

    private ArrayList<CustomTrack> mTracksList;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArtistTracksListFragment() {
    }

    /*
    *   Events
    * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SpotifyApi spotifyApi = new SpotifyApi();
        mSpotifyService = spotifyApi.getService();

        mListAdapter = new TrackArrayAdapter(getActivity(), new ArrayList<CustomTrack>());
        setListAdapter(mListAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_artist_tracks_list, container, false);

        mInfoView = (InfoView) rootView.findViewById(R.id.info_view);
        ViewGroup contentLayout = (ViewGroup) rootView.findViewById(R.id.content_layout);

        contentLayout.addView(view);
        // after add info view goes behind and becomes unclickable, so that's why it is moved to front.
        mInfoView.bringToFront();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // if view is restored...
        if (savedInstanceState == null) return;

        // check if error message should be shown
        if (savedInstanceState.containsKey(InfoView.STATE_IS_ERROR) && savedInstanceState.getBoolean(InfoView.STATE_IS_ERROR)){
            showError();
        } else if (savedInstanceState.containsKey(STATE_TRACKS_LIST)) {
            // than restore tracks list without call to the web
            mTracksList = savedInstanceState.getParcelableArrayList(STATE_TRACKS_LIST);
            showTracks();
        }

        // restore action bar to show correct information
        if (savedInstanceState.containsKey(ARG_ACTIVATED_ARTIST_NAME)) {
            mArtistName = savedInstanceState.getString(ARG_ACTIVATED_ARTIST_NAME);
            if (mInfoView.getIsError()) {
                // if it is error screen don't need to count tracks
                Utils.setActionBar(getActivity(), " ", mArtistName);
            } else {
                setActionBar(mListAdapter.getCount(), mArtistName);
            }
        }

        // restore artist id
        if (savedInstanceState.containsKey(ARG_ACTIVATED_ARTIST_ID)) {
            mArtistId = savedInstanceState.getString(ARG_ACTIVATED_ARTIST_ID);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        /*
        * When fragment is destroyed it is needed to store tracks list for easy
        * restoration without call to the web. Also it is needed to store artist id and
        * artist name, because when error is shown and restoration is done if user would
        * decide to press "Try Again" button, than a new call to the web would require
        * these values.
        * */
        // store artist id required for the call
        if (mArtistId != null) {
            outState.putString(ARG_ACTIVATED_ARTIST_ID, mArtistId);
        }
        // store artist name which will be recreated for the subtitle
        if (mArtistName != null) {
            outState.putString(ARG_ACTIVATED_ARTIST_NAME, mArtistName);
        }
        // check if error message is shown and save state if it is
        if (mInfoView!=null && mInfoView.getIsError()) {
            outState.putBoolean(InfoView.STATE_IS_ERROR, mInfoView.getIsError());

        } else if (mTracksList != null) {
            // if everything is ok (no error screen) than store tracks list
            outState.putParcelableArrayList(STATE_TRACKS_LIST, mTracksList);
        }

        super.onSaveInstanceState(outState);
    }

    /*
    *   Methods
    * */
    public void showSelectedArtistTopTracks(String artistId, String artistName) {

        mArtistId = artistId;
        mArtistName = artistName;
        mTracksList = null;

        // just to look more nice, while waiting for tracks total number
        Utils.setActionBar(getActivity(), " ", mArtistName);

        mListAdapter.clear();
        mInfoView.hide();

        Map<String, Object> map = new HashMap<>();
        map.put("country", Locale.getDefault().getCountry());

        mSpotifyService.getArtistTopTrack(artistId, map, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {

                setActionBar(tracks.tracks.size(), mArtistName);

                mTracksList = new ArrayList<CustomTrack>();

                for (Track track : tracks.tracks) {
                    CustomTrack customTrack = new CustomTrack();
                    customTrack.setId(track.id);
                    customTrack.setTrackName(track.name);
                    customTrack.setAlbumName(track.album.name);
                    customTrack.setAlbumImageUrl(Utils.getImageUrl(track.album.images));
                    mTracksList.add(customTrack);
                }

                showTracks();
            }

            @Override
            public void failure(RetrofitError error) {
                showError();
            }
        });
    }

    public void onSearchTextChanged() {
        // if text inside search field was changed, than clean everything
        mListAdapter.clear();
        mInfoView.hide();
        mInfoView.setIsError(false);
        mArtistId = null;
        mArtistName = null;
        mTracksList = null;
    }

    private void showTracks() {
        mInfoView.setIsError(false);
        if (mTracksList.isEmpty()) {
            mInfoView.showEmpty(getString(R.string.info_msg_empty_artist_top_tracks_list));
            return;
        }
        Utils.fillAdapter(mListAdapter, mTracksList);
    }

    private void setActionBar(int tracksCount, String artistName) {
        Utils.setActionBar(getActivity(), getResources().getQuantityString(R.plurals.title_selected_artist_top_tracks, tracksCount, tracksCount), artistName);
    }

    private void showError() {
        // Show error message with ability to for user to restart the call by pressing a button
        mInfoView.showError(getString(R.string.info_msg_error), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectedArtistTopTracks(mArtistId, mArtistName);
            }
        });
    }
}
