package com.frozenironsoftware.avocado.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class PodcastPlay {
    private long id;
    @SerializedName("user_id")
    private long userId;
    @SerializedName("podacst_id")
    private long podcastId;
    @SerializedName("episode_guid")
    private String episodeGuid;
    private int position;
    private int progress;

    private static final Map<String, String> COLUMN_MAPPINGS;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("user_id", "userId");
        map.put("podcast_id", "podcastId");
        map.put("episode_guid", "episodeGuid");
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

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getPodcastId() {
        return podcastId;
    }

    public void setPodcastId(long podcastId) {
        this.podcastId = podcastId;
    }

    public String getEpisodeGuid() {
        return episodeGuid;
    }

    public void setEpisodeGuid(String episodeGuid) {
        this.episodeGuid = episodeGuid;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
