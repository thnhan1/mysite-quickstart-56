package com.mysite.core.models.dto;

import java.util.Calendar;

public class ArticleItem {

    private final String title;
    private final String summary;
    private final String imagePath;
    private final String url;
    private final String author;
    private final Calendar publishedDate;
    private final String categoryTitle;

    public ArticleItem(String title, String summary, String imagePath,
                       String url, String author, Calendar publishedDate,
                       String categoryTitle) {
        this.title = title;
        this.summary = summary;
        this.imagePath = imagePath;
        this.url = url;
        this.author = author;
        this.publishedDate = publishedDate;
        this.categoryTitle = categoryTitle;
    }

    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getImagePath() { return imagePath; }
    public String getUrl() { return url; }
    public String getAuthor() { return author; }
    public Calendar getPublishedDate() { return publishedDate; }
    public String getCategoryTitle() { return categoryTitle; }

    public String getFormattedDate() {
        if (publishedDate == null) {
            return "";
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(publishedDate.getTime());
    }
}
