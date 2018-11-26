package com.frozenironsoftware.avocado.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class Podcast {
    private static final Map<String, String> COLUMN_MAPPINGS;
    private long id;
    private String title;
    private String image;
    private String description;
    private long plays;
    @SerializedName("unplayed_episodes")
    private int unplayedEpisodes;
    private int episodes;
    private String author;
    @SerializedName("is_placeholder")
    private boolean isPlaceholder = false;
    @SerializedName("itunes_id")
    private long itunesId;
    @SerializedName("feed_url")
    private String feedUrl;
    private String genre;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("itunes_id", "itunesId");
        map.put("feed_url", "feedUrl");
        COLUMN_MAPPINGS = map;
    }

    /**
     * Get SQL column mappings
     * @return column mappings
     */
    public static Map<String, String> getColumnMapings() {
        return COLUMN_MAPPINGS;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getPlays() {
        return plays;
    }

    public void setPlays(long plays) {
        this.plays = plays;
    }

    public void setUnplayedEpisodes(int value) {
        this.unplayedEpisodes = value;
    }

    public int getUnplayedEpisodes() {
        return unplayedEpisodes;
    }

    public void setEpisodes(int episodes) {
        this.episodes = episodes;
    }

    public int getEpisodes() {
        return episodes;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isPlaceholder() {
        return isPlaceholder;
    }

    public void setPlaceholder(boolean placeholder) {
        isPlaceholder = placeholder;
    }

    public long getItunesId() {
        return itunesId;
    }

    public void setItunesId(long itunesId) {
        this.itunesId = itunesId;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
