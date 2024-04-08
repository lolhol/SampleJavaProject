package org.example.util;

public class PointPosFloat {
    public float x;
    public float y;

    public PointPosFloat(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PointPosFloat other) {
            return x == other.x && y == other.y;
        }

        return false;
    }
}
