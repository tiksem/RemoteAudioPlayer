package com.stikhonenko.remoteplayer.database;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by stikhonenko on 1/2/16.
 */
public class AudioDataBase {
    private static final String AUDIO_QUERY_WHERE = MediaStore.Audio.Media.IS_MUSIC + "=1"
            + " AND " + MediaStore.Audio.Media.DATA + "<> ''";
    private static final String[] AUDIO_QUERY_FIELDS = {
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
    };

    private List<Audio> songs;

    public AudioDataBase(ContentResolver contentResolver) {
        Cursor cursor = queryAudios(contentResolver);
        try {
            songs = getAudiosFromCursor(cursor);
        } finally {
            cursor.close();
        }
    }

    private Cursor queryAudios(ContentResolver contentResolver) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        return contentResolver.query(uri, AUDIO_QUERY_FIELDS, AUDIO_QUERY_WHERE, null, null);
    }
    
    private List<Audio> getAudiosFromCursor(Cursor cursor) {
        int artistNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
        int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
        int urlColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

        List<Audio> result = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            String artistName = cursor.getString(artistNameColumn);
            String title = cursor.getString(titleColumn);
            String url = cursor.getString(urlColumn);

            Audio audio = new Audio.Builder()
                    .setArtistsName(artistName)
                    .setTitle(title)
                    .setUrl(url)
                    .build();

            result.add(audio);
        }

        return Collections.unmodifiableList(result);
    }

    public List<Audio> getSongs() {
        return songs;
    }
}
