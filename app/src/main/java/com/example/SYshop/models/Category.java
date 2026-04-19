package com.example.SYshop.models;

public class Category {
    private String name;
    private boolean selected;
    private int imageRes;

    public Category(String name, boolean selected) {
        this(name, selected, 0);
    }

    public Category(String name, boolean selected, int imageRes) {
        this.name = name;
        this.selected = selected;
        this.imageRes = imageRes;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getImageRes() {
        return imageRes;
    }
}
