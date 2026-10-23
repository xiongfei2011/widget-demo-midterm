package com.example.widgetdemo.music;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import com.example.widgetdemo.R;


public class MusicPlaybackService extends MediaSessionService {
    public static final String ACTION_TOGGLE = "music.widget.action.TOGGLE";
    public static final String ACTION_NEXT = "music.widget.action.NEXT";
    public static final String ACTION_PREVIOUS = "music.widget.action.PREVIOUS";

    private MusicPlaylist playlist;
    private ExoPlayer player;
    private MediaSession mediaSession;

    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onEvents(@NonNull Player player, @NonNull Player.Events events) {
            updateWidgetState();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        playlist = new MusicPlaylist(getPackageName());
        initializePlayer();
        updateWidgetState();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_NEXT.equals(action)) {
                changeSong(1);
            } else if (ACTION_PREVIOUS.equals(action)) {
                changeSong(-1);
            } else if (ACTION_TOGGLE.equals(action)) {
                togglePlayback();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public MediaSession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.removeListener(playerListener);
            player.release();
            player = null;
        }
        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }

        MusicWidgetProvider.resetWidgets(this);
        super.onDestroy();
    }

    private void initializePlayer() {
        player = new ExoPlayer.Builder(this).build();
        player.addListener(playerListener);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build();
        player.setAudioAttributes(audioAttributes, true);

        // Tạm dừng khi ngắt tai nghe.
        player.setHandleAudioBecomingNoisy(true);

        mediaSession = new MediaSession.Builder(this, player).build();
        addSession(mediaSession);

        player.setMediaItems(playlist.getMediaItems());
        player.prepare();
    }

    private void togglePlayback() {
        int state = player.getPlaybackState();
        if (state == Player.STATE_ENDED) {
            changeSong(1);
        } else if (state == Player.STATE_IDLE) {
            player.prepare();
            player.play();
        } else if (player.getPlayWhenReady()) {
            player.pause();
        } else {
            player.play();
        }
    }

    private void changeSong(int direction) {
        int count = player.getMediaItemCount();
        if (count == 0) {
            return;
        }

        // Quay vòng khi chuyển qua đầu hoặc cuối danh sách.
        int currentIndex = player.getCurrentMediaItemIndex();
        int newIndex = (currentIndex + direction + count) % count;
        player.seekTo(newIndex, 0);

        if (player.getPlaybackState() == Player.STATE_IDLE) {
            player.prepare();
        }
        player.play();
    }

    private void updateWidgetState() {
        if (player == null) {
            return;
        }

        MediaMetadata metadata = player.getMediaMetadata();

        String title = metadata.title != null
                ? metadata.title.toString()
                : getString(R.string.music_no_song);
        String artist = metadata.artist != null
                ? metadata.artist.toString()
                : getString(R.string.music_name_artist);

        int state = player.getPlaybackState();
        boolean shouldShowPause = player.getPlayWhenReady()
                && (state == Player.STATE_READY || state == Player.STATE_BUFFERING);

        MusicWidgetProvider.updateAllWidgets(
                this, title, artist, shouldShowPause,
                playlist.getCoverResource(player.getCurrentMediaItem())
        );
    }
}
