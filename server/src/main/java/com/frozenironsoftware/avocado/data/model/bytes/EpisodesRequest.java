package com.frozenironsoftware.avocado.data.model.bytes;

import com.frozenironsoftware.avocado.data.SortOrder;

import java.nio.ByteBuffer;

public class EpisodesRequest implements ByteSerializable {
    public static final int BYTES = Long.BYTES * 4 + Integer.BYTES * 2;
    private long episodeId;
    private long userId;
    private long offset;
    private long podcastId;
    private SortOrder sortOrder;
    private int limit;

    public EpisodesRequest(long userId, int limit, long offset, long podcastId, SortOrder sortOrder, long episodeId) {
        this.userId = userId;
        this.limit = limit;
        this.offset = offset;
        this.podcastId = podcastId;
        this.sortOrder = sortOrder;
        this.episodeId = episodeId;
    }

    public EpisodesRequest(byte[] bytes) {
        fromBytes(bytes);
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(BYTES);
        byteBuffer.putLong(userId);
        byteBuffer.putInt(limit);
        byteBuffer.putLong(offset);
        byteBuffer.putLong(podcastId);
        byteBuffer.putInt(sortOrder.ordinal());
        byteBuffer.putLong(episodeId);
        return byteBuffer.array();
    }

    @Override
    public void fromBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        userId = byteBuffer.getLong();
        limit = byteBuffer.getInt();
        offset = byteBuffer.getLong();
        podcastId = byteBuffer.getLong();
        sortOrder = SortOrder.values()[byteBuffer.getInt()];
        episodeId = byteBuffer.getLong();
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getPodcastId() {
        return podcastId;
    }

    public void setPodcastId(long podcastId) {
        this.podcastId = podcastId;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(long episodeId) {
        this.episodeId = episodeId;
    }
}
