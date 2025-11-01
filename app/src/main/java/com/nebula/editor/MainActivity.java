package com.nebula.editor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.nebula.editor.utils.FileUtils;
import com.nebula.editor.utils.VideoUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements VideoProcessor.ProgressListener {

    private static final int PICK_VIDEO_REQUEST = 1;
    
    private Button btnSelectVideo, btnProcess, btnContact, btnReleases;
    private TextView tvSelectedFile;
    private RadioGroup qualityGroup;
    private RecyclerView formatsRecycler;
    private AdView adView;
    private ProgressDialog progressDialog;
    
    private Uri selectedVideoUri;
    private String selectedFormat = "1x1";
    private String selectedQuality = "lossless";
    private QualitySettings qualitySettings;
    
    private List<FormatItem> formatItems;
    private VideoProcessingTask currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        qualitySettings = new QualitySettings(this);
        initializeApp();
    }

    private void initializeApp() {
        initializeAds();
        initializeViews();
        setupFormats();
        setupListeners();
        loadSettings();
        
        if (qualitySettings.isFirstRun()) {
            showWelcomeDialog();
        }
    }

    private void initializeAds() {
        MobileAds.initialize(this, initializationStatus -> {});
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void initializeViews() {
        btnSelectVideo = findViewById(R.id.btn_select_video);
        btnProcess = findViewById(R.id.btn_process);
        btnContact = findViewById(R.id.btn_contact);
        btnReleases = findViewById(R.id.btn_releases);
        tvSelectedFile = findViewById(R.id.tv_selected_file);
        qualityGroup = findViewById(R.id.quality_group);
        formatsRecycler = findViewById(R.id.formats_recycler);
        
        formatsRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        
        // Настройка ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void setupFormats() {
        formatItems = new ArrayList<>();
        formatItems.add(new FormatItem("1x1", "1:1", "Квадрат", R.drawable.ic_format_1x1));
        formatItems.add(new FormatItem("9x16", "9:16", "Вертикальный", R.drawable.ic_format_9x16));
        formatItems.add(new FormatItem("16x9", "16:9", "Горизонтальный", R.drawable.ic_format_16x9));
        formatItems.add(new FormatItem("4x5", "4:5", "Инстаграм", R.drawable.ic_format_4x5));
        formatItems.add(new FormatItem("1x2", "1:2", "Ультра Vertical", R.drawable.ic_format_1x2));
        formatItems.add(new FormatItem("2x1", "2:1", "Ультра Horizontal", R.drawable.ic_format_2x1));
        
        FormatAdapter adapter = new FormatAdapter(formatItems, this::onFormatSelected);
        formatsRecycler.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSelectVideo.setOnClickListener(v -> openVideoPicker());
        btnProcess.setOnClickListener(v -> processVideo());
        btnContact.setOnClickListener(v -> openTelegramChannel());
        btnReleases.setOnClickListener(v -> openReleasesChannel());
        
        qualityGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.quality_lossless) {
                selectedQuality = "lossless";
            } else if (checkedId == R.id.quality_compressed) {
                selectedQuality = "compressed";
            } else if (checkedId == R.id.quality_ai) {
                selectedQuality = "ai_enhanced";
            }
            qualitySettings.setQualityMode(selectedQuality);
        });
        
        // Длинное нажатие для информации
        btnProcess.setOnLongClickListener(v -> {
            showVideoInfo();
            return true;
        });
    }

    private void loadSettings() {
        selectedQuality = qualitySettings.getQualityMode();
        selectedFormat = qualitySettings.getDefaultFormat();
        
        switch (selectedQuality) {
            case "lossless":
                qualityGroup.check(R.id.quality_lossless);
                break;
            case "compressed":
                qualityGroup.check(R.id.quality_compressed);
                break;
            case "ai_enhanced":
                qualityGroup.check(R.id.quality_ai);
                break;
        }
        
        // Выбираем формат по умолчанию
        onFormatSelected(new FormatItem(selectedFormat, "", "", 0));
    }

    private void showWelcomeDialog() {
        new AlertDialog.Builder(this)
            .setTitle("🌌 Добро пожаловать в Nebula Editor!")
            .setMessage("Космический видеоредактор с ИИ-улучшением\n\n" +
                       "• Выберите видео\n• Настройте качество\n• Выберите формат\n• Обработайте!")
            .setPositiveButton("Начать", null)
            .show();
    }

    private void onFormatSelected(FormatItem format) {
        selectedFormat = format.getId();
        qualitySettings.setDefaultFormat(selectedFormat);
        
        // Update UI to show selected format
        for (FormatItem item : formatItems) {
            item.setSelected(item.getId().equals(selectedFormat));
        }
        formatsRecycler.getAdapter().notifyDataSetChanged();
    }

    private void openVideoPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Выберите видео"), PICK_VIDEO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK) {
            selectedVideoUri = data.getData();
            String fileName = FileUtils.getFileName(this, selectedVideoUri);
            tvSelectedFile.setText("✅ " + fileName);
            btnProcess.setEnabled(true);
            btnProcess.setAlpha(1.0f);
        }
    }

    private void processVideo() {
        if (selectedVideoUri == null) {
            Toast.makeText(this, "🎬 Сначала выберите видео", Toast.LENGTH_SHORT).show();
            return;
        }

        // Показываем диалог подтверждения
        new AlertDialog.Builder(this)
            .setTitle("🚀 Запуск обработки")
            .setMessage("Формат: " + getFormatName(selectedFormat) + 
                       "\nКачество: " + getQualityName(selectedQuality) +
                       "\n\nПродолжить?")
            .setPositiveButton("Вперед!", (dialog, which) -> startVideoProcessing())
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void startVideoProcessing() {
        currentTask = new VideoProcessingTask();
        currentTask.execute();
    }

    private class VideoProcessingTask extends AsyncTask<Void, Integer, Boolean> {
        private File outputFile;
        private String errorMessage;

        @Override
        protected void onPreExecute() {
            showProgressDialog("🚀 Подготовка к обработке...");
            VideoProcessor.setProgressListener(MainActivity.this);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                File inputFile = FileUtils.getFileFromUri(MainActivity.this, selectedVideoUri);
                outputFile = new File(getExternalFilesDir(null), 
                    "nebula_" + selectedFormat + "_" + System.currentTimeMillis() + ".mp4");

                publishProgress(20);
                
                return VideoProcessor.processVideo(
                    inputFile.getAbsolutePath(),
                    outputFile.getAbsolutePath(),
                    selectedFormat,
                    selectedQuality
                );

            } catch (Exception e) {
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            updateProgressDialog(values[0], "Обработка...");
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressDialog.dismiss();
            if (success && outputFile != null && outputFile.exists()) {
                showSuccessDialog(outputFile);
            } else {
                Toast.makeText(MainActivity.this, 
                    "❌ Ошибка: " + (errorMessage != null ? errorMessage : "Неизвестная ошибка"), 
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showProgressDialog(String message) {
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void updateProgressDialog(int progress, String message) {
        if (progressDialog.isShowing()) {
            progressDialog.setMessage(message + " (" + progress + "%)");
        }
    }

    // VideoProcessor.ProgressListener implementation
    @Override
    public void onProgress(int progress, String message) {
        runOnUiThread(() -> updateProgressDialog(progress, message));
    }

    @Override
    public void onComplete(boolean success, String outputPath) {
        runOnUiThread(() -> {
            progressDialog.dismiss();
            if (success) {
                File outputFile = new File(outputPath);
                showSuccessDialog(outputFile);
            }
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            progressDialog.dismiss();
            Toast.makeText(this, "❌ " + error, Toast.LENGTH_LONG).show();
        });
    }

    private void showSuccessDialog(File outputFile) {
        long fileSize = outputFile.length() / (1024 * 1024); // MB
        
        new AlertDialog.Builder(this)
            .setTitle("🎉 Успешно!")
            .setMessage("Видео обработано!\n\n" +
                       "Формат: " + getFormatName(selectedFormat) + "\n" +
                       "Качество: " + getQualityName(selectedQuality) + "\n" +
                       "Размер: " + fileSize + " МБ")
            .setPositiveButton("Поделиться", (dialog, which) -> shareVideo(outputFile))
            .setNegativeButton("Закрыть", null)
            .setNeutralButton("Еще раз", (dialog, which) -> resetSelection())
            .show();
    }

    private void shareVideo(File videoFile) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("video/mp4");
        Uri videoUri = Uri.fromFile(videoFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        try {
            startActivity(Intent.createChooser(shareIntent, "Поделиться видео"));
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка при отправке", Toast.LENGTH_SHORT).show();
        }
    }

    private void showVideoInfo() {
        if (selectedVideoUri == null) {
            Toast.makeText(this, "Сначала выберите видео", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File inputFile = FileUtils.getFileFromUri(this, selectedVideoUri);
            VideoUtils.VideoInfo info = VideoUtils.getVideoInfo(inputFile.getAbsolutePath());
            
            new AlertDialog.Builder(this)
                .setTitle("📊 Информация о видео")
                .setMessage("Разрешение: " + info.getResolution() + "\n" +
                           "Длительность: " + info.getDurationFormatted() + "\n" +
                           "Поворот: " + info.rotation + "°")
                .setPositiveButton("OK", null)
                .show();
                
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка получения информации", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetSelection() {
        selectedVideoUri = null;
        tvSelectedFile.setText("Файл не выбран");
        btnProcess.setEnabled(false);
        btnProcess.setAlpha(0.5f);
    }

    private String getFormatName(String format) {
        switch (format) {
            case "1x1": return "1:1 Квадрат";
            case "9x16": return "9:16 Вертикальный";
            case "16x9": return "16:9 Горизонтальный";
            case "4x5": return "4:5 Инстаграм";
            case "1x2": return "1:2 Ультра Vertical";
            case "2x1": return "2:1 Ультра Horizontal";
            default: return format;
        }
    }

    private String getQualityName(String quality) {
        switch (quality) {
            case "lossless": return "💎 Без сжатия";
            case "compressed": return "📦 Со сжатием";
            case "ai_enhanced": return "🤖 ИИ Улучшение";
            default: return quality;
        }
    }

    private void openTelegramChannel() {
        openUrl("https://t.me/glitch_qzq");
    }

    private void openReleasesChannel() {
        openUrl("https://t.me/Script_Releases");
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка открытия ссылки", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (currentTask != null && !currentTask.isCancelled()) {
            currentTask.cancel(true);
        }
        VideoProcessor.cancelProcessing();
        super.onDestroy();
    }
          }
