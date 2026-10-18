package com.example.widgetdemo.music;

import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Metadata;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import com.example.widgetdemo.R;

public class MusicPlaybackService extends MediaSessionService {
    public static final String ACTION_TOGGLE = "music.widget.action.TOGGLE";
    public static final String ACTION_NEXT = "music.widget.action.NEXT";
    public static final String ACTION_PREVIOUS = "music.widget.action.PREVIOUS";

    private final Map<String, Integer> songCovers = new HashMap<>();
    private ExoPlayer player;
    private MediaSession mediaSession;

    private MediaItem createSong(int audioResource, String title, String artist, int coverResource){

        String mediaId = String.valueOf(audioResource);
        songCovers.put(mediaId, coverResource);

        Uri song = Uri.parse("android.resource://"
                            + getPackageName()
                            + "/" + audioResource);

        MediaMetadata metadata = new MediaMetadata.Builder()
                                .setTitle(title)
                                .setArtist(artist)
                                .build();

        return new MediaItem.Builder()
                .setMediaId(String.valueOf(audioResource))
                .setUri(song)
                .setMediaMetadata(metadata)
                .build();

    }

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

        player.setMediaItems(Arrays.asList(
                createSong(R.raw.hoan_hao_song, "Hoàn Hảo", "Bray", R.drawable.hoan_hao_cover),
                createSong(R.raw.anh_voi_quen,"Em Vội Quên", "Nguyễn Thạc Bảo Ngọc", R.drawable.em_voi_quen_cover),
                createSong(R.raw.thuong_nhau_den_the,"Thương Nhau Đến Thế", "Gia Huy", R.drawable.thuong_nhau_den_the_cover),
                createSong(R.raw.tra_cho_anh, "Trả Cho Anh", "Nguyễn Thạc Bảo Ngọc", R.drawable.tra_cho_anh_cover)
                ));

        player.prepare();
        updateWidgetState();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        if (intent != null){
            String action = intent.getAction();
            if (ACTION_NEXT.equals(action)){
                changeSong(1);
            } else if (ACTION_PREVIOUS.equals(action)){
                changeSong(-1);
            } else if (ACTION_TOGGLE.equals(action)){
                int state = player.getPlaybackState();
                if(state == Player.STATE_ENDED){
                    changeSong(1);
                    player.play();
                } else if (state == Player.STATE_IDLE) {
                    player.prepare();
                    player.play();
                } else if (player.getPlayWhenReady()) {
                    player.pause();
                } else {
                    player.play();
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }
    @Nullable
    @Override
    public MediaSession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo){
        return mediaSession;
    }

    private void changeSong(int direction){
        int count = player.getMediaItemCount();

        if (count == 0){
            return;
        }

        int currIndex = player.getCurrentMediaItemIndex();
        int newIndex = (currIndex + direction + count) % count;

        player.seekTo(newIndex,0);
        if (player.getPlaybackState() == Player.STATE_IDLE){
            player.prepare();
        }

        player.play();
    }

    private void updateWidgetState(){
        MediaMetadata metadata = player.getMediaMetadata();

        String title = metadata.title != null
                ? metadata.title.toString()
                : "Chưa chọn bài";

        String artist = metadata.artist != null
                ? metadata.artist.toString()
                : "Ca sĩ";

        int coverResource = 0;
        MediaItem currSong = player.getCurrentMediaItem();

        if (currSong != null){
            Integer cover = songCovers.get(currSong.mediaId);

            if (cover != null){
                coverResource = cover;
            }
        }
        int state = player.getPlaybackState();

        boolean shouldShowPause = player.getPlayWhenReady()
                && (state == Player.STATE_READY || state == Player.STATE_BUFFERING);

        MusicWidgetProvider.updateAllWidget(this, artist, title, shouldShowPause, coverResource);
    }

    @Override
    public void onDestroy(){
        player.release();
        mediaSession.release();

        MusicWidgetProvider.updateAllWidget(
                this,
                "Ca sĩ",
                "Chưa phát nhạc",
                false,
                0
        );
        super.onDestroy();
        Log.d("MusicService", "Tắt ứng dụng");
    }

}
