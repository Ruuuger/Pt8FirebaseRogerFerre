package com.example.pt8firebase_rogerferr;

public class Note {
    private String id;
    private String title;
    private String content;
    private boolean important;
    private long createdAt;

    public Note() {
    }

    public Note(String id, String title, String content, boolean important, long createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.important = important;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content == null ? "" : content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}