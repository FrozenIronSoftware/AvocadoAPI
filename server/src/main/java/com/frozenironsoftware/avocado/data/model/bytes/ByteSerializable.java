package com.frozenironsoftware.avocado.data.model.bytes;

public interface ByteSerializable {
    /**
     * Serialize the class to a byte array that can be deserialized with @see ByteSerializable#fromBytes(byte[])
     * @return byte array representing this class
     */
    public byte[] toBytes();

    /**
     * Deserialize a byte array into the class instance
     * @param bytes byte array made from @see ByteSerializable#toBytes()
     */
    public void fromBytes(byte[] bytes);

    public static int BYTES = 0;
}
