package com.nebula.editor.utils;

import android.media.MediaMetadataRetriever;
import android.util.Log;
import java.io.File;

public class VideoUtils {
    private static final String TAG = "VideoUtils";
    
    public static VideoInfo getVideoInfo(String filePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            
            String widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            String rotationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            
            int width = Integer.parseInt(widthStr != null ? widthStr : "0");
            int height = Integer.parseInt(heightStr != null ? heightStr : "0");
            long duration = Long.parseLong(durationStr != null ? durationStr : "0");
            int rotation = Integer.parseInt(rotationStr != null ? rotationStr : "0");
            
            // Корректируем ширину и высоту если видео повернуто
            if (rotation == 90 || rotation == 270) {
                int temp = width;
                width = height;
                height = temp;
            }
            
            return new VideoInfo(width, height, duration, rotation);
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка получения информации о видео", e);
            return new VideoInfo(0, 0, 0, 0);
        } finally {
            try {
                retriever.release();
            } catch (Exception e) {
                Log.e(TAG, "Ошибка освобождения retriever", e);
            }
        }
    }
    
    public static class VideoInfo {
        public final int width;
        public final int height;
        public final long durationMs;
        public final int rotation;
        
        public VideoInfo(int width, int height, long durationMs, int rotation) {
            this.width = width;
            this.height = height;
            this.durationMs = durationMs;
            this.rotation = rotation;
        }
        
        public String getResolution() {
            return width + "x" + height;
        }
        
        public String getDurationFormatted() {
            long seconds = durationMs / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
