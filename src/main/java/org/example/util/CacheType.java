package org.example.util;

public enum CacheType {
    IMU((byte) 0),
    CLOUD((byte) 1);

    public final byte identifier;

    CacheType(byte identifier) {
        this.identifier = identifier;
    }
}
