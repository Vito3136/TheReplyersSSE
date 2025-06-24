package com.example.vulnapp.model;

public class Upload {
    private long id;
    private String filename;
    private String content;
    private long userId;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
}
