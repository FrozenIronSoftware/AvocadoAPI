package com.frozenironsoftware.avocado.data.model.itunes;

import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.List;

public class ItunesPodcast {
    @Nullable private String wrapperType;
    @Nullable private String kind;
    private long artistId;
    private long collectionId;
    private long trackId;
    @Nullable private String artistName;
    @Nullable private String collectionName;
    @Nullable private String trackName;
    @Nullable private String collectionCensoredName;
    @Nullable private String trackCensoredName;
    @Nullable private String artistViewUrl;
    @Nullable private String collectionViewUrl;
    @Nullable private String feedUrl;
    @Nullable private String trackViewUrl;
    @Nullable private String artworkUrl30;
    @Nullable private String artworkUrl60;
    @Nullable private String artworkUrl100;
    @Nullable private String artworkUrl600;
    private int collectionPrice;
    private int trackPrice;
    private int trackRentalPrice;
    private int collectionHdPrice;
    private int trackHdPrice;
    private int trackHdRentalPrice;
    @Nullable private Date releaseDate;
    @Nullable private String collectionExplicitness;
    @Nullable private String trackExplicitness;
    private int trackCount;
    @Nullable private String country;
    @Nullable private String currency;
    @Nullable private String primaryGenreName;
    @Nullable private String contentAdvisoryRating;
    @Nullable private List<String> genreIds;
    @Nullable private List<String> genres;

    public String getWrapperType() {
        return wrapperType;
    }

    public void setWrapperType(String wrapperType) {
        this.wrapperType = wrapperType;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(long collectionId) {
        this.collectionId = collectionId;
    }

    public long getTrackId() {
        return trackId;
    }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getCollectionCensoredName() {
        return collectionCensoredName;
    }

    public void setCollectionCensoredName(String collectionCensoredName) {
        this.collectionCensoredName = collectionCensoredName;
    }

    public String getTrackCensoredName() {
        return trackCensoredName;
    }

    public void setTrackCensoredName(String trackCensoredName) {
        this.trackCensoredName = trackCensoredName;
    }

    public String getArtistViewUrl() {
        return artistViewUrl;
    }

    public void setArtistViewUrl(String artistViewUrl) {
        this.artistViewUrl = artistViewUrl;
    }

    public String getCollectionViewUrl() {
        return collectionViewUrl;
    }

    public void setCollectionViewUrl(String collectionViewUrl) {
        this.collectionViewUrl = collectionViewUrl;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public String getTrackViewUrl() {
        return trackViewUrl;
    }

    public void setTrackViewUrl(String trackViewUrl) {
        this.trackViewUrl = trackViewUrl;
    }

    public String getArtworkUrl30() {
        return artworkUrl30;
    }

    public void setArtworkUrl30(String artworkUrl30) {
        this.artworkUrl30 = artworkUrl30;
    }

    public String getArtworkUrl60() {
        return artworkUrl60;
    }

    public void setArtworkUrl60(String artworkUrl60) {
        this.artworkUrl60 = artworkUrl60;
    }

    public String getArtworkUrl100() {
        return artworkUrl100;
    }

    public void setArtworkUrl100(String artworkUrl100) {
        this.artworkUrl100 = artworkUrl100;
    }

    public String getArtworkUrl600() {
        return artworkUrl600;
    }

    public void setArtworkUrl600(String artworkUrl600) {
        this.artworkUrl600 = artworkUrl600;
    }

    public int getCollectionPrice() {
        return collectionPrice;
    }

    public void setCollectionPrice(int collectionPrice) {
        this.collectionPrice = collectionPrice;
    }

    public int getTrackPrice() {
        return trackPrice;
    }

    public void setTrackPrice(int trackPrice) {
        this.trackPrice = trackPrice;
    }

    public int getTrackRentalPrice() {
        return trackRentalPrice;
    }

    public void setTrackRentalPrice(int trackRentalPrice) {
        this.trackRentalPrice = trackRentalPrice;
    }

    public int getCollectionHdPrice() {
        return collectionHdPrice;
    }

    public void setCollectionHdPrice(int collectionHdPrice) {
        this.collectionHdPrice = collectionHdPrice;
    }

    public int getTrackHdPrice() {
        return trackHdPrice;
    }

    public void setTrackHdPrice(int trackHdPrice) {
        this.trackHdPrice = trackHdPrice;
    }

    public int getTrackHdRentalPrice() {
        return trackHdRentalPrice;
    }

    public void setTrackHdRentalPrice(int trackHdRentalPrice) {
        this.trackHdRentalPrice = trackHdRentalPrice;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getCollectionExplicitness() {
        return collectionExplicitness;
    }

    public void setCollectionExplicitness(String collectionExplicitness) {
        this.collectionExplicitness = collectionExplicitness;
    }

    public String getTrackExplicitness() {
        return trackExplicitness;
    }

    public void setTrackExplicitness(String trackExplicitness) {
        this.trackExplicitness = trackExplicitness;
    }

    public int getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(int trackCount) {
        this.trackCount = trackCount;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPrimaryGenreName() {
        return primaryGenreName;
    }

    public void setPrimaryGenreName(String primaryGenreName) {
        this.primaryGenreName = primaryGenreName;
    }

    public String getContentAdvisoryRating() {
        return contentAdvisoryRating;
    }

    public void setContentAdvisoryRating(String contentAdvisoryRating) {
        this.contentAdvisoryRating = contentAdvisoryRating;
    }

    public List<String> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(List<String> genreIds) {
        this.genreIds = genreIds;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }
}
