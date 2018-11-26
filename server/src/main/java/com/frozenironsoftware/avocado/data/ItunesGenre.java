package com.frozenironsoftware.avocado.data;

public enum ItunesGenre {
    ARTS("1301", "Arts"),
    COMEDY("1303", "Comedy"),
    EDUCATION("1304", "Education"),
    KIDS_FAMILY("1305", "Kids and Family"),
    HEALTH("1307", "Health"),
    TV_FILM("1309", "TV and Film"),
    MUSIC("1310", "Music"),
    NEWS_POLITICS("1311", "News and Politics"),
    REGLIGION_SPIRITUALITY("1314", "Religion and Spirituality"),
    SCIENCE_MEDICINE("1315", "Science and Medicine"),
    SPORTS_RECREATION("1316", "Sports and Recreation"),
    TECHNOLOGY("1318", "Technology"),
    BUSINESS("1321", "Business"),
    GAMES_HOBBIES("1323", "Games and Hobbies"),
    SOCIETY_CULTURE("1324", "Society and Culture"),
    GOVERNMENT_ORGANIZATIONS("1325", "Government and Organizations");

    private final String id;
    private final String letterCode;

    ItunesGenre(String id, String letterCode) {
        this.id = id;
        this.letterCode = letterCode;
    }

    public String getId() {
        return id;
    }

    public String getLetterCode() {
        return letterCode;
    }
}
