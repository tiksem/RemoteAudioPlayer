package com.stikhonenko.remoteplayer.adapters;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.stikhonenko.remoteplayer.R;
import com.stikhonenko.remoteplayer.database.Audio;

/**
 * Created by stikhonenko on 1/5/16.
 */
public class SongsAdapter extends ViewArrayAdapter<Audio, SongHolder> {
    public SongsAdapter(Context context) {
        super(context);
    }

    @Override
    protected int getRootLayoutId(int viewType) {
        return R.layout.song_item;
    }

    @Override
    protected SongHolder createViewHolder(View view) {
        SongHolder holder = new SongHolder();
        holder.title = (TextView) view.findViewById(R.id.song_name);
        holder.artistName = (TextView) view.findViewById(R.id.artist_name);
        return holder;
    }

    @Override
    protected void reuseView(Audio audio, SongHolder holder, int position, View view) {
        holder.title.setText(audio.getTitle());
        holder.artistName.setText(audio.getArtistsName());
    }
}
