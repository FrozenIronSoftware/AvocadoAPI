package com.frozenironsoftware.avocado.data.model;

import com.google.gson.annotations.SerializedName;

public class Episode {
    private long id;
    @SerializedName("podcast_id")
    private long podcastId;
    @SerializedName("episode_id")
    private long episodeId;
    private String title;
    private String description;
    private long plays;

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
}
