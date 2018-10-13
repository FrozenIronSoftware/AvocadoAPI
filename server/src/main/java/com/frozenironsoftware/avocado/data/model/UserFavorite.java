package com.frozenironsoftware.avocado.data.model;

import com.google.gson.annotations.SerializedName;

import java.sql.Timestamp;

public class UserFavorite {
    private long id;
    @SerializedName("podacst_id")
    private long podcastId;
    @SerializedName("user_id")
    private long userId;
    @SerializedName("date_favorited")
    private Timestamp dateFavorited;

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

    public Timestamp getDateFavorited() {
        return dateFavorited;
    }

    public void setDateFavorited(Timestamp dateFavorited) {
        this.dateFavorited = dateFavorited;
    }
}
