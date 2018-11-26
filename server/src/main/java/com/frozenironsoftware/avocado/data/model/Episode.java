package com.frozenironsoftware.avocado.data.model;

import com.google.gson.annotations.SerializedName;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Episode {
    private static final Map<String, String> COLUMN_MAPPINGS;
    private long id;
    @SerializedName("podcast_id")
    private long podcastId;
    @SerializedName("episode_id")
    private long episodeId;
    private String title;
    private String description;
    private long plays;
    private int progress;
    private int position;
    private String type;
    private String url;
    private String guid;
    @SerializedName("date_released")
    private Timestamp dateReleased;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("podcast_id", "podcastId");
        map.put("episode_id", "episodeId");
        map.put("date_released", "dateReleased");
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

    public long getPodcastId() {
        return podcastId;
    }

    public void setPodcastId(long podcastId) {
        this.podcastId = podcastId;
    }

    public long getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(long episodeId) {
        this.episodeId = episodeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Timestamp getDateReleased() {
        return dateReleased;
    }

    public void setDateReleased(Timestamp dateReleased) {
        this.dateReleased = dateReleased;
    }
}
