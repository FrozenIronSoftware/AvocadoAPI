package com.frozenironsoftware.avocado.data.model;

import com.google.gson.annotations.SerializedName;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class UserRecent {
    private long id;
    @SerializedName("podcast_id")
    private long podcastId;
    @SerializedName("user_id")
    private long userId;
    private Timestamp added;

    private static final Map<String, String> COLUMN_MAPPINGS;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("podcast_id", "podcastId");
        map.put("user_id", "userId");
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

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Timestamp getAdded() {
        return added;
    }

    public void setAdded(Timestamp added) {
        this.added = added;
    }
}
