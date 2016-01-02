package com.stikhonenko.remoteplayer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.nestapi.lib.API.AccessToken;
import com.nestapi.lib.API.Listener;
import com.nestapi.lib.API.NestAPI;
import com.nestapi.lib.API.Structure;
import com.nestapi.lib.AuthManager;
import com.nestapi.lib.ClientMetadata;
import com.stikhonenko.remoteplayer.adapters.SongsAdapter;
import com.stikhonenko.remoteplayer.database.Audio;
import com.stikhonenko.remoteplayer.database.AudioDataBase;
import com.stikhonenko.remoteplayer.playback.AudioPlayerService;
import com.stikhonenko.remoteplayer.playback.PositionChangedListener;
import com.stikhonenko.remoteplayer.utils.Generator;
import com.stikhonenko.remoteplayer.utils.Lists;
import com.stikhonenko.remoteplayer.utils.PermissionUtils;
import com.stikhonenko.remoteplayer.utils.Services;
import com.stikhonenko.remoteplayer.utils.Toasts;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NestAPI.AuthenticationListener {
    private static final int AUTH_TOKEN_REQUEST_CODE = 101;

    private AudioPlayerService.Binder playBackBinder;
    private Services.UnBinder playBackUnBinder;
    private TextView onlineStatusTextView;
    private Structure.AwayState awayState;
    private List<String> urls;
    private int playOnHomeStateAudioPosition = -1;
    private ListView listView;
    private Listener nestUpdateListener;
    private TextView listViewDescriptionView;
    private AsyncTask<Void, Void, AudioDataBase> audioDatabaseLoadingTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onlineStatusTextView = (TextView) findViewById(R.id.online);
        onlineStatusTextView.setText(R.string.loading_state);

        listViewDescriptionView = (TextView) findViewById(R.id.list_view_state_description);

        AudioPlayerService.bindAndStart(this, new Services.OnBind<AudioPlayerService.Binder>() {
            @Override
            public void onBind(Services.Connection<AudioPlayerService.Binder> connection) {
                playBackBinder = connection.getBinder();
                playBackUnBinder = connection;

                onPlayBackServiceBound();
            }
        });
    }

    private void onPlayBackServiceBound() {
        authorizeNest();
        requestReadAudioDatabasePermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != AUTH_TOKEN_REQUEST_CODE) {
            return;
        }

        if (AuthManager.hasAccessToken(data)) {
            AccessToken token = AuthManager.getAccessToken(data);
            NestTokenUtils.saveAuthToken(this, token);
            authenticate(token);
        } else {
            finish();
        }
    }

    @Override
    public void onAuthenticationSuccess() {
        nestUpdateListener = new Listener.Builder()
                .setStructureListener(new Listener.StructureListener() {
                    @Override
                    public void onStructureUpdated(@NonNull Structure structure) {
                        MainActivity.this.onStructureUpdated(structure);
                    }
                })
                .build();
        NestAPI.getInstance().addUpdateListener(nestUpdateListener);
    }

    @Override
    public void onAuthenticationFailure(int errorCode) {
        Toasts.message(this, R.string.auth_failed);
        onlineStatusTextView.setText(R.string.status_failed);
    }

    private void onAudioDatabaseLoaded(AudioDataBase audioDataBase) {
        List<Audio> songs = audioDataBase.getSongs();
        if (songs.isEmpty()) {
            listViewDescriptionView.setText(R.string.empty_songs_list);
            return;
        }

        listView = (ListView) findViewById(R.id.songs_list);
        listView.setVisibility(View.VISIBLE);
        listViewDescriptionView.setVisibility(View.GONE);
        final SongsAdapter adapter = new SongsAdapter(this);
        adapter.setElements(songs);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onAudioSelected(position);
            }
        });

        urls = Lists.map(songs, new Generator<Audio, String>() {
            @Override
            public String get(Audio audio) {
                return audio.getUrl();
            }
        });

        playBackBinder.addPositionChangedListener(new PositionChangedListener() {
            @Override
            public void onPositionChanged() {
                listView.setItemChecked(playBackBinder.getPosition(), true);
            }
        });
    }

    private void requestReadAudioDatabasePermissions() {
        if (PermissionUtils.shouldRequestReadStoragePermission(this)) {
            PermissionUtils.requestReadStoragePermission(this);
        } else {
            readAudioDatabase();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.READ_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                listViewDescriptionView.setOnClickListener(null);
                readAudioDatabase();
            } else {
                listViewDescriptionView.setText(R.string.read_permission_disabled);
                listViewDescriptionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestReadAudioDatabasePermissions();
                    }
                });
            }
        }
    }

    private void readAudioDatabase() {
        audioDatabaseLoadingTask = new AsyncTask<Void, Void, AudioDataBase>() {
            @Override
            protected AudioDataBase doInBackground(Void... params) {
                return new AudioDataBase(getContentResolver());
            }

            @Override
            protected void onPostExecute(AudioDataBase audioDataBase) {
                onAudioDatabaseLoaded(audioDataBase);
                audioDatabaseLoadingTask = null;
            }
        };
        audioDatabaseLoadingTask.execute();
    }

    private void authorizeNest() {
        AccessToken accessToken = NestTokenUtils.loadAuthToken(this);
        if (accessToken != null) {
            authenticate(accessToken);
        } else {
            obtainAccessToken();
        }
    }

    private void obtainAccessToken() {
        final ClientMetadata metadata = new ClientMetadata.Builder()
                .setClientID(Constants.CLIENT_ID)
                .setClientSecret(Constants.CLIENT_SECRET)
                .setRedirectURL(Constants.REDIRECT_URL)
                .build();
        AuthManager.launchAuthFlow(this, AUTH_TOKEN_REQUEST_CODE, metadata);
    }

    private void authenticate(AccessToken token) {
        NestAPI.getInstance().authenticate(token, this);
    }

    private void onAudioSelected(int position) {
        if (awayState == Structure.AwayState.HOME) {
            playBackBinder.play(urls, position);
        } else {
            playOnHomeStateAudioPosition = position;

            int message = awayState == null ? R.string.audio_selected_on_loading_state :
                    R.string.audio_selected_on_away_state;
            Toasts.message(this, message);
        }
    }

    private void onStructureUpdated(@NonNull Structure structure) {
        Structure.AwayState awayState = structure.getAwayState();
        if (awayState != MainActivity.this.awayState) {
            MainActivity.this.awayState = awayState;
            onAwayStateChanged();
        }
    }

    private void onAwayStateChanged() {
        onlineStatusTextView.setText(getAwayStateKey());

        if (awayState == Structure.AwayState.HOME) {
            if(playOnHomeStateAudioPosition >= 0) {
                playBackBinder.play(urls, playOnHomeStateAudioPosition);
                playOnHomeStateAudioPosition = -1;
            } else if (playBackBinder.isPaused()) {
                playBackBinder.resume();
            }
        } else {
            if (playBackBinder.isPlaying()) {
                playBackBinder.pause();
            }
        }
    }

    private int getAwayStateKey() {
        if (awayState == Structure.AwayState.HOME) {
            return R.string.home;
        }

        return R.string.away;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (playBackUnBinder != null) {
            playBackUnBinder.unbind();
        }
        if (nestUpdateListener != null) {
            NestAPI.getInstance().removeUpdateListener(nestUpdateListener);
        }
        if (audioDatabaseLoadingTask != null) {
            audioDatabaseLoadingTask.cancel(false);
        }
    }
}
