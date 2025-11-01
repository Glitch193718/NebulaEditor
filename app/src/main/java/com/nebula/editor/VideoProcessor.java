package com.nebula.editor;

import android.util.Log;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.FFmpegExecution;
import java.io.File;

public class VideoProcessor {
    
    private static final String TAG = "VideoProcessor";
    private static ProgressListener progressListener;
    
    public interface ProgressListener {
        void onProgress(int progress, String message);
        void onComplete(boolean success, String outputPath);
        void onError(String error);
    }
    
    public static void setProgressListener(ProgressListener listener) {
        progressListener = listener;
    }
    
    public static boolean processVideo(String inputPath, String outputPath, 
                                     String format, String quality) {
        try {
            Log.d(TAG, "Начало обработки видео");
            Log.d(TAG, "Вход: " + inputPath);
            Log.d(TAG, "Выход: " + outputPath);
            Log.d(TAG, "Формат: " + format);
            Log.d(TAG, "Качество: " + quality);
            
            if (progressListener != null) {
                progressListener.onProgress(10, "🚀 Анализ видео...");
            }
            
            // Проверяем существование входного файла
            File inputFile = new File(inputPath);
            if (!inputFile.exists()) {
                throw new Exception("Входной файл не найден");
            }
            
            // Строим команду FFmpeg
            String[] cmd = buildFFmpegCommand(inputPath, outputPath, format, quality);
            
            if (progressListener != null) {
                progressListener.onProgress(30, "🎬 Запуск обработки...");
            }
            
            // Выполняем команду
            int exitCode = FFmpeg.execute(cmd);
            
            boolean success = exitCode == 0;
            Log.d(TAG, "Код завершения FFmpeg: " + exitCode);
            
            if (progressListener != null) {
                if (success) {
                    progressListener.onProgress(100, "✅ Обработка завершена!");
                    progressListener.onComplete(true, outputPath);
                } else {
                    progressListener.onError("Ошибка обработки видео");
                }
            }
            
            return success;
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка обработки видео", e);
            if (progressListener != null) {
                progressListener.onError("Ошибка: " + e.getMessage());
            }
            return false;
        }
    }
    
    private static String[] buildFFmpegCommand(String inputPath, String outputPath, 
                                             String format, String quality) {
        String scaleFilter = getScaleFilter(format);
        String[] qualityParams = getQualityParams(quality);
        
        // Базовая команда
        String[] baseCmd = {
            "-i", inputPath,
            "-vf", scaleFilter,
            "-c:v", "libx264",
            "-pix_fmt", "yuv420p",
            "-movflags", "+faststart",
            "-y"
        };
        
        // Объединяем массивы
        String[] fullCmd = new String[baseCmd.length + qualityParams.length + 1];
        System.arraycopy(baseCmd, 0, fullCmd, 0, baseCmd.length);
        System.arraycopy(qualityParams, 0, fullCmd, baseCmd.length, qualityParams.length);
        fullCmd[fullCmd.length - 1] = outputPath;
        
        // Логируем команду
        StringBuilder cmdLog = new StringBuilder();
        for (String arg : fullCmd) {
            cmdLog.append(arg).append(" ");
        }
        Log.d(TAG, "FFmpeg команда: " + cmdLog.toString());
        
        return fullCmd;
    }
    
    private static String getScaleFilter(String format) {
        switch (format) {
            case "1x1":
                return "scale=1080:1080:flags=lanczos,setsar=1";
            case "9x16":
                return "scale=1080:1920:flags=lanczos,setsar=1";
            case "16x9":
                return "scale=1920:1080:flags=lanczos,setsar=1";
            case "4x5":
                return "scale=1080:1350:flags=lanczos,setsar=1";
            case "1x2":
                return "scale=1080:2160:flags=lanczos,setsar=1";
            case "2x1":
                return "scale=2160:1080:flags=lanczos,setsar=1";
            default:
                return "scale=1080:1080:flags=lanczos,setsar=1";
        }
    }
    
    private static String[] getQualityParams(String quality) {
        switch (quality) {
            case "lossless":
                return new String[]{"-crf", "0", "-preset", "medium", "-c:a", "copy"};
            case "compressed":
                return new String[]{"-crf", "23", "-preset", "medium", "-c:a", "aac", "-b:a", "192k"};
            case "ai_enhanced":
                return new String[]{"-crf", "18", "-preset", "slow", "-c:a", "copy"};
            default:
                return new String[]{"-crf", "23", "-preset", "medium", "-c:a", "aac", "-b:a", "192k"};
        }
    }
    
    public static void cancelProcessing() {
        FFmpeg.cancel();
    }
}
