package com.frozenironsoftware.avocado.data.model.bytes;

import com.google.common.base.Charsets;

import java.nio.ByteBuffer;

public class QueryLimitedOffsetRequest implements ByteSerializable {
    private String query;
    private int limit;
    private long offset;

    public QueryLimitedOffsetRequest(String query, int limit, long offset) {
        this.query = query;
        this.limit = limit;
        this.offset = offset;
    }

    public QueryLimitedOffsetRequest(byte[] bytes) {
        fromBytes(bytes);
    }

    @Override
    public byte[] toBytes() {
        byte[] stringBytes = query.getBytes(Charsets.UTF_8);
        ByteBuffer byteBuffer = ByteBuffer.allocate(LimitedOffsetRequest.BYTES + Integer.BYTES + stringBytes.length);
        LimitedOffsetRequest limitedOffsetRequest = new LimitedOffsetRequest(limit, offset);
        byteBuffer.put(limitedOffsetRequest.toBytes());
        byteBuffer.putInt(stringBytes.length);
        byteBuffer.put(stringBytes);
        return byteBuffer.array();
    }

    @Override
    public void fromBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byte[] limitOffsetBytes = new byte[LimitedOffsetRequest.BYTES];
        byteBuffer.get(limitOffsetBytes);
        LimitedOffsetRequest limitedOffsetRequest = new LimitedOffsetRequest(bytes);
        limit = limitedOffsetRequest.getLimit();
        offset = limitedOffsetRequest.getOffset();
        int stringLength = byteBuffer.getInt();
        byte[] stringBytes = new byte[stringLength];
        byteBuffer.get(stringBytes);
        query = new String(stringBytes, Charsets.UTF_8);
    }

    public String getQuery() {
        return query;
    }

    public int getLimit() {
        return limit;
    }

    public long getOffset() {
        return offset;
    }
}
