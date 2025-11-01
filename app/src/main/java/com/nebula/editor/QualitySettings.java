package com.nebula.editor;

import android.content.Context;
import android.content.SharedPreferences;

public class QualitySettings {
    private static final String PREFS_NAME = "NebulaEditorPrefs";
    private static final String KEY_QUALITY = "quality_mode";
    private static final String KEY_FORMAT = "default_format";
    private static final String KEY_FIRST_RUN = "first_run";
    
    private final SharedPreferences prefs;
    
    public QualitySettings(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public void setQualityMode(String quality) {
        prefs.edit().putString(KEY_QUALITY, quality).apply();
    }
    
    public String getQualityMode() {
        return prefs.getString(KEY_QUALITY, "lossless");
    }
    
    public void setDefaultFormat(String format) {
        prefs.edit().putString(KEY_FORMAT, format).apply();
    }
    
    public String getDefaultFormat() {
        return prefs.getString(KEY_FORMAT, "1x1");
    }
    
    public boolean isFirstRun() {
        boolean firstRun = prefs.getBoolean(KEY_FIRST_RUN, true);
        if (firstRun) {
            prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply();
        }
        return firstRun;
    }
    
    public void resetFirstRun() {
        prefs.edit().putBoolean(KEY_FIRST_RUN, true).apply();
    }
}
