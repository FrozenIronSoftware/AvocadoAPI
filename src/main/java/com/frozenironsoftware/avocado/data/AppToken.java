package com.frozenironsoftware.avocado.data;

import com.google.gson.annotations.SerializedName;

public class AppToken {
    public long id;
    @SerializedName("user_id")
    public long userId;
    public String token;
    public String name;

    // Not in database. This is only populated at creation
    public String tokenPlain;

    public AppToken(long id, long userId, String token, String name) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.name = name;
    }
}
