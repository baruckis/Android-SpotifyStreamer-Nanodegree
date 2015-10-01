package com.baruckis.nanodegree.spotifystreamer.fragments;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.baruckis.nanodegree.spotifystreamer.App;
import com.baruckis.nanodegree.spotifystreamer.PlayerService;
import com.baruckis.nanodegree.spotifystreamer.R;
import com.baruckis.nanodegree.spotifystreamer.Utils;
import com.baruckis.nanodegree.spotifystreamer.models.CustomTrack;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Andrius-Baruckis on 2015.
 * http://www.baruckis.com/
 */

public class PlayerDialogFragment extends DialogFragment {

    public static final String ARG_TRACKS_LIST = "tracks_list";
    public static final String ARG_SELECTED_TRACK_INDEX = "selected_track_index";

    public static final String ARG_CONFIG_CHANGE = "config_change";

    private ArrayList<CustomTrack> mTracksList;
    private Integer mSelectedTrackIndex;
    private boolean mIsConfigChanged = false;

    private TextView mArtistName;
    private TextView mAlbumName;
    private ImageView mAlbumImage;
    private TextView mTrackName;
    private TextView mCurrentTime;
    private TextView mTotalTime;

    private ImageButton mPlayPauseButton;
    private SeekBar mScrubBar;

    private PlayerService mPlayerService;

    private boolean playbackPaused = false;

    private Handler mDurationHandler = new Handler();

    private boolean mIsSeekBarUpdating = false;
    private boolean mIsLargeLayout;


    /*
     * Constructors
     * */
    public static PlayerDialogFragment newInstance(ArrayList<CustomTrack> tracksList, Integer index) {
        PlayerDialogFragment playerDialogFragment = new PlayerDialogFragment();
        if (tracksList != null && index != null) {
            Bundle args = new Bundle();
            args.putParcelableArrayList(ARG_TRACKS_LIST, tracksList);
            args.putInt(ARG_SELECTED_TRACK_INDEX, index);
            playerDialogFragment.setArguments(args);
        }
        return playerDialogFragment;
    }

    /*
     * Events
     * */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);
        setHasOptionsMenu(true);
    }

    /**
     * The system calls this to get the DialogFragment's layout, regardless
     * of whether it's being displayed as a dialog or an embedded fragment.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ARG_CONFIG_CHANGE)) {
                mIsConfigChanged = savedInstanceState.getBoolean(ARG_CONFIG_CHANGE);
            }
        } else {
            Bundle bundle = getArguments();
            if (bundle != null) {
                if (bundle.containsKey(ARG_TRACKS_LIST)) {
                    mTracksList = bundle.getParcelableArrayList(ARG_TRACKS_LIST);
                }
                if (bundle.containsKey(ARG_SELECTED_TRACK_INDEX)) {
                    mSelectedTrackIndex = bundle.getInt(ARG_SELECTED_TRACK_INDEX);
                }
                if (bundle.containsKey(ARG_CONFIG_CHANGE)) {
                    mIsConfigChanged = bundle.getBoolean(ARG_CONFIG_CHANGE);
                }
            }
        }

        mArtistName = (TextView) view.findViewById(R.id.artist_name);
        mAlbumName = (TextView) view.findViewById(R.id.album_name);
        mAlbumImage = (ImageView) view.findViewById(R.id.album_image);
        mTrackName = (TextView) view.findViewById(R.id.track_name);
        mCurrentTime = (TextView) view.findViewById(R.id.current_time);
        mTotalTime = (TextView) view.findViewById(R.id.total_time);

        mScrubBar = (SeekBar) view.findViewById(R.id.scrub_bar);
        ImageButton previousButton = (ImageButton) view.findViewById(R.id.previous_button);
        mPlayPauseButton = (ImageButton) view.findViewById(R.id.play_pause_button);
        ImageButton nextButton = (ImageButton) view.findViewById(R.id.next_button);

        mScrubBar.setClickable(false);

        onInitService();

        // Setup player controls.
        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playbackPaused) {
                    generateAction(PlayerService.ACTION_PLAY);
                } else {
                    generateAction(PlayerService.ACTION_PAUSE);
                }
            }
        });

        mScrubBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mScrubBar.isEnabled()) {
                    return false;
                }
                return true;
            }
        });

        mScrubBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mCurrentTime.setText(Utils.getSecondsFromMilliseconds(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                updateSeekBar(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPlayerService.seek(seekBar.getProgress());
                updateSeekBar(true);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateAction(PlayerService.ACTION_NEXT);
            }
        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateAction(PlayerService.ACTION_PREVIOUS);
            }
        });

        Utils.hideSoftKeyboard(view);
    }

    @Override
    public void onPause() {
        updateSeekBar(false);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlayerService != null && mPlayerService.isPlaying()) {
            updateSeekBar(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (getActivity().isChangingConfigurations()) {
            outState.putBoolean(ARG_CONFIG_CHANGE, true);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem nowPlayingActionButton = menu.findItem(R.id.now_playing);
        if (!mIsLargeLayout) {
            nowPlayingActionButton.setVisible(false);
        }
    }

    @Override
    public void onDestroy() {
        if (mPlayerConnection != null) {
            getActivity().unbindService(mPlayerConnection);
            mPlayerConnection = null;
        }
        super.onDestroy();
    }


    /*
     * Objects
     * */
    // Connect to the service.
    private ServiceConnection mPlayerConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;

            //get service
            mPlayerService = binder.getService();

            binder.setListener(new PlayerService.BoundServiceListener() {

                @Override
                public void onPlayTrack() {

                    if (mPlayerService.isError()) {
                        onPlayNewTrack();
                    } else {
                        resumePlayback();
                    }
                }

                @Override
                public void onPauseTrack() {
                    pausePlayback();
                }

                @Override
                public void onPreviousTrack() {
                    playPrevious();
                }

                @Override
                public void onNextTrack() {
                    playNext();
                }

                // Iform when streaming is ready to be played...
                @Override
                public void onTrackPrepared() {
                    initSeekBar();
                    updateSeekBar(true);
//                    mIsPrepared = true;
                }

                @Override
                public void onError() {
                    updateSeekBar(false);
                    playbackPaused = true;
                    mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
                }

                // When track stream is completed, than stop updating seek bar
                @Override
                public void onTrackComplete() {
                    updateSeekBar(false);
                    resetSeekBar();
                }

                // When new track is set, update UI information for that track
                @Override
                public void onSetTrack(CustomTrack track) {

                    mArtistName.setText(track.getArtistsNamesList());
                    mAlbumName.setText(track.getAlbumName());

                    Integer width = track.getCustomImageBig().getWidth();
                    Integer height = track.getCustomImageBig().getHeight();
                    String url = track.getCustomImageBig().getUrl();

                    if (width!=null) {
                        mAlbumImage.setMinimumWidth(width);
                    }
                    if (height!=null) {
                        mAlbumImage.setMinimumHeight(height);
                    }
                    if (url!=null) {
                        Picasso.with(getActivity()).load(url).into(mAlbumImage);
                    }

                    mTrackName.setText(track.getTrackName());

                    if (!mIsLargeLayout) {
                        Utils.setActionBar(getActivity(), track.getTrackName(), track.getArtistsNamesList());
                    }
                }
            });

            // if service is connected after configuration was changed (device screen rotation)...
            if (mIsConfigChanged) {
                // different actions if track currently is playing or paused...
                if (mPlayerService.isPlaying()) {
                    onContinuePlayingTrack();

                } else onPauseTrack();
            }
            // if service is connected after fragment was newly created or destroyed and now recreated...
            else {
                // check if user selected different track, or different artist's track or perhaps came back to the same track, which is now currently playing...
                boolean dataUpdated = mPlayerService.updateData(mTracksList, mSelectedTrackIndex);
                // so if it different than what is playing right now...
                if (dataUpdated) {
                    onPlayNewTrack();
                    // and if it is the same track...
                    // if this track is currently playing...
                } else if (mPlayerService.isPlaying()) {
                    onContinuePlayingTrack();
                    // or if it paused...
                } else onPauseTrack();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    //handler to change seekBarTime
    private Runnable updateSeekBarTime = new Runnable() {

        public void run() {
            //set seekbar progress
            int currentMilliseconds = mPlayerService.getPosition();
            mScrubBar.setProgress(currentMilliseconds);
            mCurrentTime.setText(Utils.getSecondsFromMilliseconds(currentMilliseconds));

            //repeat yourself that again in 100 miliseconds
            mDurationHandler.postDelayed(this, 100);

        }
    };


    /*
     * Methods
     * */
    private void resetSeekBar() {
        mScrubBar.setMax(1);
        mScrubBar.setProgress(0);
        mScrubBar.setEnabled(false);

        mCurrentTime.setText(Utils.getSecondsFromMilliseconds(0));
        mTotalTime.setText("");
    }

    private void initSeekBar() {
        if (mPlayerService == null) return;

        int currentMilliseconds = mPlayerService.getPosition();
        int totalMilliseconds = mPlayerService.getDuration();

        mScrubBar.setMax(totalMilliseconds);
        mScrubBar.setProgress(currentMilliseconds);
        mScrubBar.setEnabled(true);

        mCurrentTime.setText(Utils.getSecondsFromMilliseconds(currentMilliseconds));
        mTotalTime.setText(Utils.getSecondsFromMilliseconds(totalMilliseconds));
    }

    private void updateSeekBar(boolean flag) {
        if (flag) {
            if (mIsSeekBarUpdating) return;
            mIsSeekBarUpdating = true;
            mDurationHandler.post(updateSeekBarTime);
        } else {
            mIsSeekBarUpdating = false;
            mDurationHandler.removeCallbacks(updateSeekBarTime);
        }
    }

    private void generateAction(String intentAction) {
        Intent intent = new Intent(getActivity(), PlayerService.class);
        intent.setAction(intentAction);
        getActivity().startService(intent);
    }

    private void onInitService() {

        Intent playIntent = new Intent(getActivity(), PlayerService.class);

        getActivity().startService(playIntent);
        getActivity().bindService(playIntent, mPlayerConnection, Context.BIND_AUTO_CREATE);

        App app = ((App) getActivity().getApplication());
        app.setIsNowPlaying(true);
    }

    private void onPlayNewTrack() {
        playbackPaused = false;

        mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);

        mPlayerService.playNewTrack();

        updateSeekBar(true);
    }

    private void onContinuePlayingTrack() {
        playbackPaused = false;
        mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);

        if (mPlayerService != null && mPlayerService.isPrepared()) {
            initSeekBar();
            updateSeekBar(true);
        } else {
            resetSeekBar();
        }
    }

    private void onPauseTrack() {
        if (mPlayerService != null && mPlayerService.isPrepared()) {
            playbackPaused = true;
            mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);

            initSeekBar();
            updateSeekBar(false);

        } else {
            if (mPlayerService != null && mPlayerService.isError()) {
                playbackPaused = true;
                mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
            } else {
                playbackPaused = false;
                mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
            }
            resetSeekBar();
        }
    }

    private void resumePlayback() {
        playbackPaused = false;
        mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        mPlayerService.resumePlayer();

        updateSeekBar(true);
    }

    private void pausePlayback() {
        playbackPaused = true;
        mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
        mPlayerService.pausePlayer();

        updateSeekBar(false);
    }

    private void playNext() {
        resetSeekBar();
        mPlayerService.playNext();
        playbackPaused = false;
        mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
    }

    private void playPrevious() {
        resetSeekBar();
        mPlayerService.playPrevious();
        playbackPaused = false;
        mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
    }

    public void destroy() {
        if (mPlayerConnection != null) {
            getActivity().unbindService(mPlayerConnection);
            mPlayerConnection = null;
        }
        dismiss();
    }
}