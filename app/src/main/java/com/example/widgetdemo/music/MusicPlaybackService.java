package com.example.widgetdemo.music;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import com.example.widgetdemo.R;

public class MusicPlaybackService extends MediaSessionService {
    public static final String ACTION_TOGGLE = "music.widget.action.TOGGLE";

    private ExoPlayer player;
    private MediaSession mediaSession;

    @Override
    public void onCreate(){
        super.onCreate();

        player = new ExoPlayer.Builder(this).build();

        player.addListener(new Player.Listener() {
            @Override
            public void onEvents(@NonNull Player player, @NonNull Player.Events events){
                updateWidgetState();
            }
        });

        AudioAttributes audioAttributes =
                new AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build();

        player.setAudioAttributes(audioAttributes,true);

        //Tạm dừng khi ngắt tai nghe
        player.setHandleAudioBecomingNoisy(true);

        mediaSession = new MediaSession.Builder(this, player).build();
        addSession(mediaSession);

        Uri songUri = Uri.parse(
                "android.resource://" + getPackageName() + "/" + R.raw.hoan_hao_song
        );


        MediaMetadata metadata = new MediaMetadata.Builder()
                .setTitle("Hoàn Hảo")
                .setArtist("Bray")
                .build();

        MediaItem song = new MediaItem.Builder()
                .setUri(songUri)
                .setMediaMetadata(metadata)
                .build();

        player.setMediaItem(song);
        player.prepare();
        updateWidgetState();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if(intent != null && ACTION_TOGGLE.equals(intent.getAction())){
            if (player.getPlayWhenReady()){
                player.pause();
                Log.d("MusicService", "Đã yêu cầu tạm dừng");
            } else {
                if (player.getPlaybackState() == Player.STATE_ENDED){
                    player.seekTo(0);
                    Log.d("MusicService", "Kết thúc bài hát");
                }

                if (player.getPlaybackState() == Player.STATE_IDLE){
                    player.prepare();
                    Log.d("MusicService", "Đang chuẩn bị bài hát");
                }
                player.play();
                Log.d("MusicService", "Bắt đầu bài hát");
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }
    @Nullable
    @Override
    public MediaSession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo){
        return mediaSession;
    }

    private void updateWidgetState(){
        CharSequence title = player.getMediaMetadata().title;

        String songTitle = title != null ? title.toString() : "Chưa chọn bài";

        int state = player.getPlaybackState();

        boolean shouldShowPause = player.getPlayWhenReady()
                && (state == Player.STATE_READY || state == Player.STATE_BUFFERING);

        MusicWidgetProvider.updateAllWidget(this, songTitle, shouldShowPause);
    }

    @Override
    public void onDestroy(){
        player.release();
        mediaSession.release();

        MusicWidgetProvider.updateAllWidget(
                this,
                "Chưa phát nhạc",
                false
        );
        super.onDestroy();
        Log.d("MusicService", "Tắt ứng dụng");
    }

}
