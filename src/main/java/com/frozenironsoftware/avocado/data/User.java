package com.frozenironsoftware.avocado.data;

import com.google.gson.annotations.SerializedName;

public class User {
    public long id;
    @SerializedName("account_level")
    public AccountLevel accountLevel;
    public String email;
    public String password;

    public User(long id, AccountLevel accountLevel, String email) {
        this.id = id;
        this.accountLevel = accountLevel;
        this.email = email;
    }

    public enum AccountLevel {
        ANONYMOUS, FREE, PREMIUM
    }
}
