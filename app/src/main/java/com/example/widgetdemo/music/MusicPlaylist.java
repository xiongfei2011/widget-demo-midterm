package com.example.widgetdemo.music;

import android.net.Uri;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;

import com.example.widgetdemo.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class MusicPlaylist {
    private final Map<String, Integer> songCovers = new HashMap<>();
    private final List<MediaItem> mediaItems;

    MusicPlaylist(String packageName) {
        mediaItems = Arrays.asList(
                createSong(packageName, R.raw.hoan_hao_song, "Hoàn Hảo", "Bray",
                        R.drawable.hoan_hao_cover),
                createSong(packageName, R.raw.anh_voi_quen, "Em Vội Quên",
                        "Nguyễn Thạc Bảo Ngọc", R.drawable.em_voi_quen_cover),
                createSong(packageName, R.raw.thuong_nhau_den_the, "Thương Nhau Đến Thế",
                        "Gia Huy", R.drawable.thuong_nhau_den_the_cover),
                createSong(packageName, R.raw.tra_cho_anh, "Trả Cho Anh",
                        "Nguyễn Thạc Bảo Ngọc", R.drawable.tra_cho_anh_cover)
        );
    }

    List<MediaItem> getMediaItems() {
        return mediaItems;
    }

    @DrawableRes
    int getCoverResource(@Nullable MediaItem song) {
        if (song == null) {
            return 0;
        }

        Integer cover = songCovers.get(song.mediaId);
        return cover != null ? cover : 0;
    }

    private MediaItem createSong(String packageName, @RawRes int audioResource,
                                String title, String artist, @DrawableRes int coverResource) {
        String mediaId = String.valueOf(audioResource);
        songCovers.put(mediaId, coverResource);

        Uri uri = Uri.parse("android.resource://" + packageName + "/" + audioResource);
        MediaMetadata metadata = new MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .build();

        return new MediaItem.Builder()
                .setMediaId(mediaId)
                .setUri(uri)
                .setMediaMetadata(metadata)
                .build();
    }
}
