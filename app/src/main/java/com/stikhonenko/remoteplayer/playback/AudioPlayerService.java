package com.stikhonenko.remoteplayer.playback;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.stikhonenko.remoteplayer.utils.Services;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * User: Tikhonenko.S
 * Date: 17.07.14
 * Time: 22:19
 */

public class AudioPlayerService extends Service implements Player.PlayBackListener {
    private MediaPlayer mediaPlayer;
    private Set<PositionChangedListener> positionChangedListeners = new LinkedHashSet<>();
    private Set<PlaybackErrorListener> errorListeners = new LinkedHashSet<>();
    private Set<StateChangedListener> stateListeners = new LinkedHashSet<>();
    private UrlListPlayer urlListPlayer;

    public static void bindAndStart(Context context,
                                    Services.OnBind<Binder> onBind) {
        Services.start(context, AudioPlayerService.class);
        Services.bind(context, AudioPlayerService.class, onBind);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();

        stateListeners.add(new StateChangedListener() {
            @Override
            public void onStateChanged(State state, State lastState) {
                if (state != State.IDLE) {
                    startForeground();
                } else {
                    stopForeground(true);
                }
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    private void setupPlayer(Player player) {
        player.setPlayBackListener(this);
    }

    void play(List<String> urls, int position) {
        urlListPlayer = new UrlListPlayer(mediaPlayer, urls);
        setupPlayer(urlListPlayer);
        urlListPlayer.play(position);
    }

    @Override
    public void onPositionChanged() {
        for (PositionChangedListener listener : positionChangedListeners) {
            listener.onPositionChanged();
        }
    }

    @Override
    public void onError(String url) {
        for (PlaybackErrorListener listener : errorListeners) {
            listener.onError(url);
        }
    }

    @Override
    public void onStatusChanged(State state, State lastState) {
        for (StateChangedListener listener : stateListeners) {
            listener.onStateChanged(state, lastState);
        }
    }

    void changePlayList(List<String> newPlayListUrls) {
        if (urlListPlayer == null) {
            throw new IllegalStateException("Unable to call changePlayList, when playList is not set");
        }

        urlListPlayer.changePlayList(newPlayListUrls);
    }

    void seekTo(int progress) {
        mediaPlayer.seekTo(progress);
    }

    public void reset() {
        Player player = getPlayer();
        if (player != null) {
            urlListPlayer = null;
            player.reset();
        }
    }

    protected void startForeground() {

    }

    Player getPlayer() {
        return urlListPlayer;
    }

    State getStatus() {
        Player player = getPlayer();
        if (player == null) {
            return State.IDLE;
        }

        return player.getState();
    }

    protected int getPosition() {
        Player player = getPlayer();
        if (player == null) {
            return -1;
        }

        return player.getPosition();
    }

    int getDuration() {
        return mediaPlayer.getDuration();
    }

    public class Binder extends android.os.Binder implements Services.OnUnbind {
        private Set<PositionChangedListener> currentPositionChangedListeners = new HashSet<>();
        private Set<PlaybackErrorListener> currentErrorListeners = new HashSet<>();
        private Set<StateChangedListener> currentStateListeners = new HashSet<>();

        public void play(List<String> urls, int position) {
            AudioPlayerService.this.play(urls, position);
        }

        public void play(int position) {
            getPlayer().play(position);
        }

        public void play(List<String> urls) {
            play(urls, 0);
        }

        public void changePlayList(List<String> newPlayListUrls) {
            AudioPlayerService.this.changePlayList(newPlayListUrls);
        }

        public void pause() {
            Player player = getPlayer();
            if (player != null) {
                player.pause();
            } else {
                throw new IllegalStateException("pause is called on IDLE state");
            }
        }

        public void resume() {
            Player player = getPlayer();
            if (player != null) {
                player.resume();
            } else {
                throw new IllegalStateException("resume is called on IDLE state");
            }
        }

        public void togglePauseState() {
            Player player = getPlayer();
            if (player != null) {
                player.togglePauseState();
            } else {
                throw new IllegalStateException("togglePauseState is called on IDLE state");
            }
        }

        public void playNext() {
            Player player = getPlayer();
            if (player == null) {
                throw new IllegalStateException("set play list first");
            }

            player.playNext();
        }

        public void playPrev() {
            Player player = getPlayer();
            if (player == null) {
                throw new IllegalStateException("set play list first");
            }

            player.playPrev();
        }

        public void addPositionChangedListener(PositionChangedListener listener) {
            currentPositionChangedListeners.add(listener);
            positionChangedListeners.add(listener);
        }

        public void removePositionChangedListener(PositionChangedListener listener) {
            currentPositionChangedListeners.remove(listener);
            positionChangedListeners.remove(listener);
        }

        public void addPlayBackErrorListener(PlaybackErrorListener listener) {
            currentErrorListeners.add(listener);
            errorListeners.add(listener);
        }

        public void removePlayBackErrorListener(PlaybackErrorListener listener) {
            currentErrorListeners.remove(listener);
            errorListeners.remove(listener);
        }

        public void addStateChangedListener(StateChangedListener listener) {
            currentStateListeners.add(listener);
            stateListeners.add(listener);
        }

        public void removeStateChangedListener(StateChangedListener listener) {
            currentStateListeners.remove(listener);
            stateListeners.remove(listener);
        }

        public void onUnbind() {
            positionChangedListeners.removeAll(currentPositionChangedListeners);
            errorListeners.removeAll(currentErrorListeners);
            stateListeners.removeAll(currentStateListeners);
        }

        public void seekTo(int msec) {
            AudioPlayerService.this.seekTo(msec);
        }

        public int getDuration() {
            return AudioPlayerService.this.getDuration();
        }

        public int getPosition() {
            return AudioPlayerService.this.getPosition();
        }

        public List<String> getPlayList() {
            if (urlListPlayer == null) {
                return null;
            }

            return urlListPlayer.getPlayList();
        }

        public String getCurrentUrl() {
            Player player = getPlayer();
            if (player == null) {
                throw new IllegalStateException("getCurrentUrl can only be called " +
                        "when player is selected");
            }

            return player.getCurrentUrl();
        }

        public void reset() {
            AudioPlayerService.this.reset();
        }

        public boolean isPaused() {
            return getStatus() == State.PAUSED;
        }

        public boolean isPlaying() {
            return getStatus() == State.PLAYING;
        }

        public State getStatus() {
            return AudioPlayerService.this.getStatus();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }
}
