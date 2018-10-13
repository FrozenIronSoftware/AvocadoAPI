package com.frozenironsoftware.avocado.data.model;

import com.google.gson.annotations.SerializedName;

public class Podcast {
    private long id;
    private String title;
    private String image;
    private String description;
    private long plays;
    @SerializedName("unplayed_episodes")
    private int unplayedEpisodes;

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
}
