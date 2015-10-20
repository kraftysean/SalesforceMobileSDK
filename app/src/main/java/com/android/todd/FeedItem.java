package com.android.todd;

public class FeedItem {
    private String name;
    private String message;
    private String image;
    private String createdDate;

    public FeedItem(String name, String message, String image, String createdDate) {
        super();
        this.name = name;
        this.message = message;
        this.image = image;
        this.createdDate = createdDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

}
