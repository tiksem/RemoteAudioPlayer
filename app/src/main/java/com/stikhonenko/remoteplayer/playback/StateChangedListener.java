package com.stikhonenko.remoteplayer.playback;

/**
 * Created by stykhonenko on 20.10.15.
 */
public interface StateChangedListener {
    void onStateChanged(State state, State lastState);
}
