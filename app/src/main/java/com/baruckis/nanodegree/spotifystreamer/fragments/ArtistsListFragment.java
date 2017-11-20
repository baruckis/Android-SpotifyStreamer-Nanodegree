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

package com.baruckis.nanodegree.spotifystreamer.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.baruckis.nanodegree.spotifystreamer.App;
import com.baruckis.nanodegree.spotifystreamer.InfoView;
import com.baruckis.nanodegree.spotifystreamer.R;
import com.baruckis.nanodegree.spotifystreamer.Utils;
import com.baruckis.nanodegree.spotifystreamer.adapters.ArtistArrayAdapter;
import com.baruckis.nanodegree.spotifystreamer.models.CustomArtist;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A list fragment representing a list of Items (Artists). This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link ArtistTracksListFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ArtistsListFragment extends ListFragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * found artists list.
     */
    private static final String STATE_ARTISTS_LIST = "artists_list";

    private EditText mSearchEditText;
    private InfoView mInfoView;

    private SpotifyService mSpotifyService;

    private ArtistArrayAdapter mListAdapter;
    private ArrayList<CustomArtist> mArtistsList;

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
        void onItemSelected(String searchText, String artistId, String artistName);

        /**
         * Callback for when an text has been changed.
         */
        void onSearchTextChanged();
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String searchText, String artistId, String artistName) {
        }

        public void onSearchTextChanged() {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArtistsListFragment() {

    }

    /*
    *   Events
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

        mListAdapter = new ArtistArrayAdapter(getActivity(), new ArrayList<CustomArtist>());
        setListAdapter(mListAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_artists_list, container, false);

        mSearchEditText = (EditText) rootView.findViewById(R.id.search_edit_text);
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

        if (savedInstanceState != null) {

            // Check if error message should be shown, and if yes than don't continue on other checks and return;
            if (savedInstanceState.containsKey(InfoView.STATE_IS_ERROR) && savedInstanceState.getBoolean(InfoView.STATE_IS_ERROR)){
                showError();
                return;
            }

            // If the was no error than get artists list, which can be empty or filled with data.
            // If it is empty than there was no such artists based on search and message for the user will be showed.
            if (savedInstanceState.containsKey(STATE_ARTISTS_LIST)){
                // restore the previously serialized artists list.
                mArtistsList = savedInstanceState.getParcelableArrayList(STATE_ARTISTS_LIST);
                showArtists();
                return;
            }
        }

        // if there was no error or no artist list saved (is null), that means that initial message should be shown
        mInfoView.showEmpty(getString(R.string.info_msg_empty_artists_list_init));
        resetActionBar();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSpotifyService = ((App) getActivity().getApplication()).getSpotifyService();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSearchEditText.addTextChangedListener(mTextWatcher);
    }

    @Override
    public void onPause() {
        mSearchEditText.removeTextChangedListener(mTextWatcher);
        super.onPause();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        CustomArtist clickedArtist = mListAdapter.getItem(position);
        mCallbacks.onItemSelected(mSearchEditText.getText().toString(), clickedArtist.getId(), clickedArtist.getName());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        // Check if error message is shown and save state if it is.
        if (mInfoView!=null && mInfoView.getIsError()) {
            outState.putBoolean(InfoView.STATE_IS_ERROR, mInfoView.getIsError());

        // If artists list is empty or filled with data than save it.
        // If it is null, means initial search message for user is shown.
        } else if (mArtistsList != null) {
            // Serialize and persist found artists list.
            outState.putParcelableArrayList(STATE_ARTISTS_LIST, mArtistsList);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }


    /*
     * Objects
     * */
    private TextWatcher mTextWatcher = new TextWatcher() {
        Boolean block = false;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            block = count == after;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (block || mSpotifyService == null) return;

            getListView().clearChoices();
            mListAdapter.clear();
            mInfoView.hide();
            resetActionBar();

            mCallbacks.onSearchTextChanged();

            if (s.toString().length() == 0) {
                mArtistsList = null;
                mInfoView.showEmpty(getString(R.string.info_msg_empty_artists_list_init));
            } else {

                mSpotifyService.searchArtists(s.toString(), searchArtistsCallback);
            }
        }
    };

    private Callback<ArtistsPager> searchArtistsCallback = new Callback<ArtistsPager>() {

        @Override
        public void success(ArtistsPager artistsPager, Response response) {

            /*  Block filling list with new artists if text entered inside search box is different
            *   compared to query text for which data response came back.
            *   This can happen if user change search text inside search box too fast.
            * */
            String searchString = mSearchEditText.getText().toString();
            Uri uri = Uri.parse(response.getUrl());
            String queryString = uri.getQueryParameter("q");

            if (!searchString.equals(queryString)) return;

            mArtistsList = new ArrayList<CustomArtist>();

            // if OK than fill adapter with new data
            for (Artist artist : artistsPager.artists.items) {
                CustomArtist customArtist = new CustomArtist();
                customArtist.setId(artist.id);
                customArtist.setName(artist.name);
                customArtist.setImageUrl(Utils.getImageSmallUrl(artist.images));
                mArtistsList.add(customArtist);
            }

            showArtists();
        }

        @Override
        public void failure(RetrofitError error) {
            Toast.makeText(getContext(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            Log.e(ArtistsListFragment.class.getSimpleName(), error.getMessage());
            showError();
        }
    };


    /*
     * Methods
     * */

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    public void restoreSearchEditText(String text) {
        if (text == null) return;

        mSearchEditText.addTextChangedListener(mTextWatcher);
        mSearchEditText.setText(text);

        // set the EditText cursor to the end of its text
        mSearchEditText.setSelection(mSearchEditText.getText().length());
    }

    private void resetActionBar() {
        Utils.setActionBar(getActivity(), getString(R.string.app_name), null);
    }

    private void showArtists() {
        mInfoView.setIsError(false);
        if (mArtistsList.isEmpty()) {
            mInfoView.showEmpty(getString(R.string.info_msg_empty_artists_list));
            return;
        }
        Utils.fillAdapter(mListAdapter, mArtistsList);
    }

    private void showError() {
        // Show error message with ability to for user to restart the call by pressing a button
        mInfoView.showError(getString(R.string.info_msg_error), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInfoView.hide();
                mSpotifyService.searchArtists(mSearchEditText.getText().toString(), searchArtistsCallback);
            }
        });
    }
}
