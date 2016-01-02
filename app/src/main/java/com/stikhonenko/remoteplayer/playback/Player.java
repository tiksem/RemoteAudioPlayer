package com.stikhonenko.remoteplayer.playback;

import android.media.MediaPlayer;

import java.io.IOException;
import java.util.List;

/**
 * Created by stykhonenko on 23.10.15.
 */

abstract class Player<PlayingEntity> {
    private MediaPlayer mediaPlayer;
    private State state = State.IDLE;
    private int position = -1;
    private PlayBackListener playBackListener;

    protected abstract int getPlayListSize();
    public abstract String getCurrentUrl();
    public abstract List<PlayingEntity> getPlayList();
    protected abstract void setPlayList(List<PlayingEntity> newPlayList);

    public Player(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public final void play(int position) {
        if (this.position == position) {
            return;
        }
        setPosition(position);
        reset();
        tryPlayCurrentUrl();
    }

    protected abstract void getCurrentUrl(OnUrlReady onUrlReady);

    protected void tryPlayCurrentUrl() {
        setState(State.PREPARING);
        getCurrentUrl(new OnUrlReady() {
            @Override
            public void onUrlReady(String url) {
                tryPlayUrl(url);
            }
        });
    }

    private void tryPlayUrl(final String url) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    onPlayingUrlError(url);
                    return true;
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playNext();
                }
            });

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    setState(State.PLAYING);
                    mp.start();
                }
            });
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            onPlayingUrlError(url);
        }
    }

    private void setState(State state) {
        State lastState = this.state;
        this.state = state;

        if (playBackListener != null) {
            playBackListener.onStatusChanged(state, lastState);
        }
    }

    protected void onPlayingUrlError(String url) {
        onError(url);

        if (canGoToNextUrl()) {
            goToNextUrl();
        } else {
            goNext();
        }
        tryPlayCurrentUrl();
    }

    protected void onError(String url) {
        if (playBackListener != null) {
            playBackListener.onError(url);
        }
    }

    public void playNext() {
        goNext();
        reset();
        tryPlayCurrentUrl();
    }

    public void playPrev() {
        goPrev();
        reset();
        tryPlayCurrentUrl();
    }

    protected void goToNextUrl() {
        throw new UnsupportedOperationException("Override this method if supportsSeveralUrlsForAudio returns true");
    }

    protected boolean canGoToNextUrl() {
        return false;
    }

    protected void onPositionChanged() {
        if(playBackListener != null) {
            playBackListener.onPositionChanged();
        }
    }

    protected final int getPosition() {
        return position;
    }

    protected final void setPosition(int newPosition) {
        if (position == newPosition) {
            return;
        }

        position = newPosition;

        onPositionChanged();
    }

    private void goNext() {
        int size = getPlayListSize();
        if (size >= 2) {
            int position = getPosition();
            if (position < size - 1) {
                position++;
            } else {
                position = 0;
            }

            setPosition(position);
        }
    }

    private void goPrev() {
        int size = getPlayListSize();
        if (size >= 2) {
            int position = getPosition();
            if (position == 0) {
                position = size - 1;
            } else {
                position--;
            }

            setPosition(position);
        }
    }

    public void pause() {
        if (state != State.PLAYING) {
            throw new IllegalStateException("pause can be called only in PLAYING state");
        }

        mediaPlayer.pause();
        setState(State.PAUSED);
    }

    public void resume() {
        if (state != State.PAUSED) {
            throw new IllegalStateException("resume can be called only in PAUSED state");
        }

        mediaPlayer.start();
        setState(State.PLAYING);
    }

    public void togglePauseState() {
        if (state == State.PLAYING) {
            pause();
        } else if(state == State.PAUSED) {
            resume();
        } else {
            throw new IllegalStateException("togglePauseState can be called only in PAUSED or PLAYING state");
        }
    }

    public void changePlayList(List<PlayingEntity> newPlayList) {
        int position = getPosition();
        List<PlayingEntity> playList = getPlayList();
        if (playList == null) {
            throw new IllegalStateException("Unable to change playlist, no playlist set. Call play before");
        }

        PlayingEntity currentPlayingEntity = playList.get(position);
        int newPosition = newPlayList.indexOf(currentPlayingEntity);
        if (newPosition < 0) {
            throw new IllegalArgumentException("new playList should contain current playing url");
        }

        setPosition(newPosition);
        setPlayList(newPlayList);
    }

    public void reset() {
        setState(State.IDLE);
        mediaPlayer.reset();
    }

    public State getState() {
        return state;
    }

    public PlayBackListener getPlayBackListener() {
        return playBackListener;
    }

    public void setPlayBackListener(PlayBackListener playBackListener) {
        this.playBackListener = playBackListener;
    }

    public interface PlayBackListener {
        void onPositionChanged();
        void onStatusChanged(State state, State lastState);
        void onError(String url);
    }
    protected interface OnUrlReady {
        void onUrlReady(String url);
    }
}
