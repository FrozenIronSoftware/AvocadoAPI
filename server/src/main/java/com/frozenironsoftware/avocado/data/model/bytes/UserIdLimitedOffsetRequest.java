package com.frozenironsoftware.avocado.data.model.bytes;

import java.nio.ByteBuffer;

public class UserIdLimitedOffsetRequest implements ByteSerializable {
    private long userId;
    private int limit;
    private long offset;
    public static int BYTES = Long.BYTES + LimitedOffsetRequest.BYTES;

    public UserIdLimitedOffsetRequest(long userId, int limit, long offset) {
        this.userId = userId;
        this.limit = limit;
        this.offset = offset;
    }

    public UserIdLimitedOffsetRequest(byte[] bytes) {
        fromBytes(bytes);
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(BYTES);
        byteBuffer.putLong(userId);
        byteBuffer.put(new LimitedOffsetRequest(limit, offset).toBytes());
        return byteBuffer.array();
    }

    @Override
    public void fromBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        userId = byteBuffer.getLong();
        byteBuffer.compact();
        LimitedOffsetRequest limitedOffsetRequest = new LimitedOffsetRequest(byteBuffer.array());
        limit = limitedOffsetRequest.getLimit();
        offset = limitedOffsetRequest.getOffset();
    }

    public long getUserId() {
        return userId;
    }

    public int getLimit() {
        return limit;
    }

    public long getOffset() {
        return offset;
    }
}
