package com.frozenironsoftware.avocado.data.model.itunes;

import java.util.List;

public class ItunesSearch {
    private long resultCount;
    private List<ItunesPodcast> results;

    public long getResultCount() {
        return resultCount;
    }

    public void setResultCount(long resultCount) {
        this.resultCount = resultCount;
    }

    public List<ItunesPodcast> getResults() {
        return results;
    }

    public void setResults(List<ItunesPodcast> results) {
        this.results = results;
    }
}
