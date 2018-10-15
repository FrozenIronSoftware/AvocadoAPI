package com.frozenironsoftware.avocado.data.model.bytes;

import java.nio.ByteBuffer;

public class LimitedOffsetRequest implements ByteSerializable {
    private int limit;
    private long offset;
    public static int BYTES = Integer.BYTES + Long.BYTES;

    public LimitedOffsetRequest(int limit, long offset) {
        this.limit = limit;
        this.offset = offset;
    }

    public LimitedOffsetRequest(byte[] bytes) {
        fromBytes(bytes);
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(BYTES);
        byteBuffer.putInt(limit);
        byteBuffer.putLong(offset);
        return byteBuffer.array();
    }

    @Override
    public void fromBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        limit = byteBuffer.getInt();
        offset = byteBuffer.getLong();
    }

    public int getLimit() {
        return limit;
    }

    public long getOffset() {
        return offset;
    }
}
