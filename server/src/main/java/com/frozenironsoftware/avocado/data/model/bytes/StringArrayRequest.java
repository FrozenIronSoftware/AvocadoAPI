package com.frozenironsoftware.avocado.data.model.bytes;

import com.google.common.base.Charsets;
import com.google.common.primitives.Ints;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class StringArrayRequest implements ByteSerializable {
    private List<String> strings;

    public StringArrayRequest(List<String> strings) {
        this.strings = strings;
    }

    public StringArrayRequest(byte[] bytes) {
        fromBytes(bytes);
    }

    @Override
    public byte[] toBytes() {
        List<Byte> bytes = new ArrayList<>();
        for (String string : strings) {
            byte[] stringBytes = string.getBytes(Charsets.UTF_8);
            byte[] stringBytesSize = Ints.toByteArray(stringBytes.length);
            for (byte stringByteSizeByte : stringBytesSize) {
                bytes.add(stringByteSizeByte);
            }
            for (byte stringByte : stringBytes) {
                bytes.add(stringByte);
            }
        }
        byte[] bytesPrimitive = new byte[bytes.size()];
        for (int byteIndex = 0; byteIndex < bytes.size(); byteIndex++) {
            bytesPrimitive[byteIndex] = bytes.get(byteIndex);
        }
        return bytesPrimitive;
    }

    @Override
    public void fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        List<String> strings = new ArrayList<>();
        while (buffer.remaining() >= Integer.BYTES) {
            int stringSize = buffer.getInt();
            byte[] stringBytes = new byte[stringSize];
            buffer.get(stringBytes);
            String string = new String(stringBytes, Charsets.UTF_8);
            strings.add(string);
        }
        this.strings = strings;
    }

    public List<String> getStrings() {
        return strings;
    }

    public void setStrings(List<String> strings) {
        this.strings = strings;
    }
}
