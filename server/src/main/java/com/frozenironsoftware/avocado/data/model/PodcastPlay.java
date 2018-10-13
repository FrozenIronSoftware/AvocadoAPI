package com.frozenironsoftware.avocado.data.model;

import com.google.gson.annotations.SerializedName;

public class PodcastPlay {
    private long id;
    @SerializedName("user_id")
    private long userId;
    @SerializedName("podacst_id")
    private long podcastId;
    @SerializedName("episode_id")
    private long episodeId;
    private int position;

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

    public long getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(long episodeId) {
        this.episodeId = episodeId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
