package com.example.widgetdemo;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.net.Uri;

import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

public class MainActivity extends AppCompatActivity {

    private ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.music_test_play).setOnClickListener(v -> {
            if (player == null) {
                return;
            }

            // Nếu bài đã kết thúc, phát lại từ đầu
            if (player.getPlaybackState() == Player.STATE_ENDED) {
                player.seekTo(0);
            }

            player.play();
        });

        findViewById(R.id.music_test_pause).setOnClickListener(v -> {
            if (player != null) {
                player.pause();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        player = new ExoPlayer.Builder(this).build();

        AudioAttributes audioAttributes =
                new AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build();

        // Cho player quản lý audio focus
        player.setAudioAttributes(audioAttributes, true);

        Uri songUri = Uri.parse(
                "android.resource://" + getPackageName()
                        + "/" + R.raw.hoan_hao_song
        );

        MediaItem mediaItem = MediaItem.fromUri(songUri);
        player.setMediaItem(mediaItem);
        player.prepare();

    }

    @Override
    protected void onStop() {
        if (player != null) {
            player.release();
            player = null;
        }

        super.onStop();
    }
}