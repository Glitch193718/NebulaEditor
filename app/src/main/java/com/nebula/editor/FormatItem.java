package com.nebula.editor;

public class FormatItem {
    private String id;
    private String aspectRatio;
    private String name;
    private int iconRes;
    private boolean selected;

    public FormatItem(String id, String aspectRatio, String name, int iconRes) {
        this.id = id;
        this.aspectRatio = aspectRatio;
        this.name = name;
        this.iconRes = iconRes;
        this.selected = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getAspectRatio() { return aspectRatio; }
    public String getName() { return name; }
    public int getIconRes() { return iconRes; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}
