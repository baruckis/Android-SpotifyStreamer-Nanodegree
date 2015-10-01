package com.baruckis.nanodegree.spotifystreamer.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.baruckis.nanodegree.spotifystreamer.InfoView;
import com.baruckis.nanodegree.spotifystreamer.R;
import com.baruckis.nanodegree.spotifystreamer.Utils;
import com.baruckis.nanodegree.spotifystreamer.activities.ArtistTracksListActivity;
import com.baruckis.nanodegree.spotifystreamer.activities.MainArtistsListActivity;
import com.baruckis.nanodegree.spotifystreamer.adapters.TrackArrayAdapter;
import com.baruckis.nanodegree.spotifystreamer.models.CustomImage;
import com.baruckis.nanodegree.spotifystreamer.models.CustomTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Andrius-Baruckis on 2015.
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

    private static final String ARG_COUNTRY_CODE = "country_code";

    private String mArtistId = null;
    private String mArtistName = null;

    private SpotifyService mSpotifyService;

    private TrackArrayAdapter mListAdapter;

    private InfoView mInfoView;

    private ArrayList<CustomTrack> mTracksList;

    private String mCountryCode = "";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(ArrayList<CustomTrack> tracksList, Integer index);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(ArrayList<CustomTrack> tracksList, Integer index) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArtistTracksListFragment() {
    }


    /*
     * Events
     * */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Context containing this fragment must implement its callbacks.
        if (!(context instanceof Callbacks)) {
            throw new IllegalStateException("Context must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SpotifyApi spotifyApi = new SpotifyApi();
        mSpotifyService = spotifyApi.getService();

        mListAdapter = new TrackArrayAdapter(getActivity(), new ArrayList<CustomTrack>());
        setListAdapter(mListAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Get currently selected country code.
        SharedPreferences countryListPreference= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String countryCode = countryListPreference.getString(getString(R.string.list_preference_country_code_key), Locale.getDefault().getCountry());

        // When country code is changed from settings screen, show tracks list based on new country.
        if (!mCountryCode.equals(countryCode)) {
            mCountryCode = countryCode;
            showSelectedArtistTopTracks(mArtistId, mArtistName);
        }
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

        if (savedInstanceState.containsKey(ARG_COUNTRY_CODE)) {
            mCountryCode = savedInstanceState.getString(ARG_COUNTRY_CODE);
        }

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
            restoreActionBar();
        }

        // restore artist id
        if (savedInstanceState.containsKey(ARG_ACTIVATED_ARTIST_ID)) {
            mArtistId = savedInstanceState.getString(ARG_ACTIVATED_ARTIST_ID);
        }
    }

    public void restoreActionBar() {
        if (mInfoView.getIsError()) {
            // if it is error screen don't need to count tracks
            Utils.setActionBar(getActivity(), " ", mArtistName);
        } else {
            setActionBar(mListAdapter.getCount(), mArtistName);
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

        if (mCountryCode != null) {
            outState.putString(ARG_COUNTRY_CODE, mCountryCode);
        }

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

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        mCallbacks.onItemSelected(mTracksList, position);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }


    /*
     * Methods
     * */
    public void showSelectedArtistTopTracks(String artistId, String artistName) {

        if (artistId==null || artistName==null) return;

        mArtistId = artistId;
        mArtistName = artistName;
        mTracksList = null;

        // just to look more nice, while waiting for tracks total number
        Utils.setActionBar(getActivity(), " ", mArtistName);

        mListAdapter.clear();
        mInfoView.hide();

        SharedPreferences myPreference= PreferenceManager.getDefaultSharedPreferences(getActivity());
        mCountryCode = myPreference.getString(getString(R.string.list_preference_country_code_key), Locale.getDefault().getCountry());

        Map<String, Object> map = new HashMap<>();
        map.put("country", mCountryCode);

        mSpotifyService.getArtistTopTrack(artistId, map, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {

                setActionBar(tracks.tracks.size(), mArtistName);

                mTracksList = new ArrayList<CustomTrack>();

                for (Track track : tracks.tracks) {

                    CustomTrack customTrack = new CustomTrack();
                    customTrack.setId(track.id);

                    /* Prepare list of all artists that own the track.
                    *  First show artist name that was selected by the user,
                    *  than add other artist if any.
                    * */
                    String artistNames = mArtistName;
                    for (ArtistSimple artist : track.artists) {
                        if (!artist.id.equals(mArtistId)) {
                            artistNames = artistNames + " & " + artist.name;
                        }
                    }
                    customTrack.setArtistsNamesList(artistNames);
                    customTrack.setTrackName(track.name);
                    customTrack.setAlbumName(track.album.name);
                    customTrack.setAlbumImageSmallUrl(Utils.getImageSmallUrl(track.album.images));

                    Image image = track.album.images.get(Utils.getPreferredImageBigIndex());
                    CustomImage customImage = new CustomImage();
                    customImage.setHeight(image.height);
                    customImage.setWidth(image.width);
                    customImage.setUrl(image.url);

                    customTrack.setCustomImageBig(customImage);

                    customTrack.setPreviewStreamUrl(track.preview_url);
                    customTrack.setExternalUrl(track.external_urls.get("spotify"));
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
