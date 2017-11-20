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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.baruckis.nanodegree.spotifystreamer.activities.BaseActivity;
import com.baruckis.nanodegree.spotifystreamer.activities.MainArtistsListActivity;
import com.baruckis.nanodegree.spotifystreamer.models.CustomTrack;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;


public class PlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    // Service will respond to this action String
    public static final String RECEIVE_BROADCAST_INTENT_PAUSE = "com.baruckis.nanodegree.spotifystreamer.receive_broadcast_intent_pause";
    public static final String RECEIVE_BROADCAST_INTENT_NOTIFICATION = "com.baruckis.nanodegree.spotifystreamer.receive_broadcast_intent_notification";

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";

    private final IBinder mPlayerBind = new PlayerBinder();

    private BoundServiceListener mListener;

    private boolean isPlaybackPausedByUser = false;

    private boolean mIsError = false;
    private boolean mIsPrepared = false;

    private MediaPlayer mMediaPlayer;

    // tracks list
    private ArrayList<CustomTrack> tracks;

    // currently playing track position in the tracks list
    private Integer trackPosition;


    private MediaSessionCompat mSession;
    private MediaControllerCompat mController;

    private Target mTarget;
    private Bitmap mLargeIconBitmap;

    private boolean mShowNotification;
    private Notification mNotification;


    /*
    *  Interface that you can pass to the binder object that your service can use to send updates to.
    * */
    public interface BoundServiceListener {

        void onPlayTrack();

        void onPauseTrack();

        void onPreviousTrack();

        void onNextTrack();

        void onTrackPrepared();

        void onError();

        void onTrackComplete();

        void onSetTrack(CustomTrack track);
    }

    // Binder
    public class PlayerBinder extends Binder {

        public PlayerService getService() {
            return PlayerService.this;
        }

        public void setListener(BoundServiceListener listener) {
            mListener = listener;
        }
    }

    /*
     * Objects
     * */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // pause playback if required
            if(intent.getAction().equals(RECEIVE_BROADCAST_INTENT_PAUSE)) {
                mController.getTransportControls().pause();
            }
            if(intent.getAction().equals(RECEIVE_BROADCAST_INTENT_NOTIFICATION)) {
                mShowNotification = intent.getBooleanExtra(RECEIVE_BROADCAST_INTENT_NOTIFICATION, getResources().getBoolean(R.bool.default_show_notification));
                showCancelNotification(mShowNotification);
            }
        }
    };


    /*
     * Events
     * */
    @Override
    public void onCreate() {
        //create the service
        super.onCreate();
        //create mMediaPlayer
        mMediaPlayer = new MediaPlayer();

        //initialize
        trackPosition = 0;
        initMediaPlayer();
        initMediaSession();

        //Notification visibility (show controls on the lock screen or not)
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mShowNotification = prefs.getBoolean(getString(R.string.switch_preference_notification_key), getResources().getBoolean(R.bool.default_show_notification));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVE_BROADCAST_INTENT_PAUSE);
        intentFilter.addAction(RECEIVE_BROADCAST_INTENT_NOTIFICATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        setTrack();
        return mPlayerBind;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        setTrack();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onDestroy() {
        // clean notification if available
        showCancelNotification(false);

        // release resources when destroy
        mSession.release();
        mMediaPlayer.stop();
        mMediaPlayer.release();

        stopForeground(true);
    }

    /*
    * If track is ready to be played.
    * */
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mIsError = false;
        mIsPrepared = true;
        // if user has not pressed pause button before new track was prepared to play...
        if (!isPlaybackPausedByUser) {
            // ...then start playback
            mediaPlayer.start();
        }

        // Inform UI that track is prepared to be played.
        if (mListener != null) {
            mListener.onTrackPrepared();
        }
    }

    /*
    * If error occurred while preparing track to be played or something else.
    * */
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        if (mIsError) return false;

        mIsError = true;

        Toast.makeText(this, getString(R.string.toast_preparing_track_msg_error), Toast.LENGTH_LONG).show();

        mediaPlayer.reset();
        mController.getTransportControls().pause();

        return true;
    }

    /*
    * If completed playing track.
    * */
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        // Inform UI that track completed playing.
        if (!mIsError && mListener != null)
            mListener.onTrackComplete();

        // Check if playback has reached the end of a track...
        if (!mIsError && mediaPlayer.getCurrentPosition() > 0) {
            mediaPlayer.reset();
            // ...if it reached than jump to the next one.
            playNext();
        }
    }


    /*
     * Methods
     * */

    /*
    * Initialize Media Player for the first time.
    * */
    private void initMediaPlayer() {
        // Set mMediaPlayer properties
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // Set listeners
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    private void initMediaSession() {

        mSession = new MediaSessionCompat(getApplicationContext(), "media_player_session", null, null);

        try {
            mController = new MediaControllerCompat(getApplicationContext(), mSession.getSessionToken());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        mSession.setCallback(new MediaSessionCompat.Callback() {
                                 @Override
                                 public void onPlay() {
                                     if (mListener != null) {
                                         mListener.onPlayTrack();
                                     }
                                 }

                                 @Override
                                 public void onPause() {
                                     if (!mIsError && mListener != null) {
                                         mListener.onPauseTrack();
                                     }
                                     if (mIsError) {
                                         isPlaybackPausedByUser = true;
                                         buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY), false);
                                         if (mListener != null) {
                                             mListener.onError();
                                         }
                                     }
                                 }

                                 @Override
                                 public void onSkipToNext() {
                                     if (mListener != null) {
                                         mListener.onNextTrack();
                                     }
                                 }

                                 @Override
                                 public void onSkipToPrevious() {
                                     if (mListener != null) {
                                         mListener.onPreviousTrack();
                                     }
                                 }
                             }
        );
    }

    public boolean isError() {
        return mIsError;
    }

    public boolean isPrepared() {
        return mIsPrepared;
    }

    /*
    * Communicates with activity to pass full track url of currently playing track for sharing.
    * */
    private void updateShareTrackURL() {

        CustomTrack track = getCurrentTrack();
        if (track == null) return;

        Intent shareIntent = new Intent(BaseActivity.RECEIVE_BROADCAST_INTENT_SHARE_UPDATE);
        shareIntent.putExtra(Intent.EXTRA_TEXT, track.getExternalUrl());

        LocalBroadcastManager.getInstance(this).sendBroadcast(shareIntent);
    }

    public boolean updateData(ArrayList<CustomTrack> theSongs, Integer songIndex) {

        if (theSongs == null || songIndex == null) {
            return false;
        }

        boolean update = false;

        if (tracks == null) {
            update = true;
        } else {
            update = !(tracks.equals(theSongs) && trackPosition == songIndex);
        }

        if (update) {
            tracks = theSongs;
            trackPosition = songIndex;
        }

        return update;
    }

    /*
    * Set currently playing track for controlling parent
    * */
    private void setTrack() {

        //get currently playing track
        CustomTrack currentTrack = getCurrentTrack();

        // Update UI with currently playing track.
        if (mListener != null && currentTrack!=null) {
            mListener.onSetTrack(currentTrack);
        }
    }

    // Play new track
    public void playNewTrack() {
        mIsError = false;
        mIsPrepared = false;
        isPlaybackPausedByUser = false;
        mMediaPlayer.reset();
        CustomTrack currentTrack = getCurrentTrack();

        // Set the data source
        try {
            mMediaPlayer.setDataSource(currentTrack.getPreviewStreamUrl());

        } catch (Exception e) {
            if (currentTrack!=null && currentTrack.getPreviewStreamUrl() == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_spotify_no_preview_msg_error), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_data_source_msg_error), Toast.LENGTH_SHORT).show();
                Log.e(PlayerService.class.getSimpleName(), getString(R.string.log_data_source_msg_error), e);
            }
            mIsError = true;
            buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY), true);
            mListener.onError();
            return;
        } finally {
            setTrack();
            updateShareTrackURL();
        }
        buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE), true);
        mMediaPlayer.prepareAsync();
    }

    public void pausePlayer() {
        mMediaPlayer.pause();
        isPlaybackPausedByUser = true;
        buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY), false);
    }

    public void resumePlayer() {
        mMediaPlayer.start();
        isPlaybackPausedByUser = false;
        buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE), false);
    }

    public int getPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public void seek(int posn) {
        mMediaPlayer.seekTo(posn);
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    // Skip to previous track
    public void playPrevious() {
        trackPosition--;
        if (trackPosition < 0) trackPosition = tracks.size() - 1;
        playNewTrack();
    }

    // Skip to next track
    public void playNext() {
        trackPosition++;
        if (trackPosition >= tracks.size()) trackPosition = 0;
        playNewTrack();
    }

    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null)
            return;

        String action = intent.getAction();

        if (action.equalsIgnoreCase(ACTION_PLAY)) {
            mController.getTransportControls().play();
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            mController.getTransportControls().pause();
        } else if (action.equalsIgnoreCase(ACTION_PREVIOUS)) {
            mController.getTransportControls().skipToPrevious();
        } else if (action.equalsIgnoreCase(ACTION_NEXT)) {
            mController.getTransportControls().skipToNext();
        }
    }

    private CustomTrack getCurrentTrack() {
        if (tracks == null || trackPosition == null) return null;
        return tracks.get(trackPosition);
    }

    private void buildNotification(NotificationCompat.Action action, boolean updateLargeIcon) {

        CustomTrack playSong = getCurrentTrack();

        if (playSong == null) return;

        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();
        style.setShowActionsInCompactView(0, 1, 2);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // open last activity when tapping notification
        Intent openAppIntent = new Intent(this, MainArtistsListActivity.class);
        openAppIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        openAppIntent.setAction(Intent.ACTION_MAIN);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        builder.setSmallIcon(android.R.drawable.ic_media_play)
                .setLargeIcon(mLargeIconBitmap)
                .setContentTitle(playSong.getTrackName())
                .setContentText(playSong.getArtistsNamesList())
                .setStyle(style)
                .setShowWhen(false) // hide time in notification
                .addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS))
                .addAction(action)
                .addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));

        if (updateLargeIcon) {

            Picasso.Builder picassoBuilder = new Picasso.Builder(this);
            Picasso picasso = picassoBuilder.build();
            // to avoid too many requests when user jumps on different tracks too fast, cancel latest one.
            picasso.cancelRequest(mTarget);

            mTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    mLargeIconBitmap = bitmap;
                    builder.setLargeIcon(mLargeIconBitmap);

                    mNotification = builder.build();

                    if(mShowNotification) {
                        showCancelNotification(true);
                    }
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };

            String url = playSong.getCustomImageBig().getUrl();

            if (url!=null) {
                picasso.load(url).into(mTarget);
            }
        }

        mNotification = builder.build();
        if (mShowNotification) {
            showCancelNotification(true);
        }
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), PlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private void showCancelNotification(boolean visible) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotification == null) return;
        if (visible) {
            notificationManager.notify(1, mNotification);
        } else {
            notificationManager.cancel(1);
        }
    }
}
