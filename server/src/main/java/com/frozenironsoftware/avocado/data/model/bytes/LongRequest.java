package com.frozenironsoftware.avocado.data.model.bytes;

import java.nio.ByteBuffer;

public class LongRequest implements ByteSerializable {
    public static final int BYTES = Long.BYTES;
    private long data;

    public LongRequest(long data) {
        this.data = data;
    }

    public LongRequest(byte[] data) {
        fromBytes(data);
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(BYTES);
        byteBuffer.putLong(data);
        return byteBuffer.array();
    }

    @Override
    public void fromBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        data = byteBuffer.getLong();
    }

    public long getData() {
        return data;
    }

    public void setData(long data) {
        this.data = data;
    }
}
