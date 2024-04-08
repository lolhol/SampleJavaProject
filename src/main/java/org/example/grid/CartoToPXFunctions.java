package org.example.grid;

public interface CartoToPXFunctions {
    int fromGlobalXToMapX(double global);

    double fromMapXToGlobalX(int map);

    int fromGlobalYToMapY(double global);

    double fromMapYToGlobalY(int map);

    default double[] fromMapToGlobal(int[] mapPos) {
        return new double[] { fromMapXToGlobalX(mapPos[0]), fromMapYToGlobalY(mapPos[1]) };
    }

    default int[] fromGlobalToMap(double[] mapPos) {
        return new int[] { fromGlobalXToMapX(mapPos[0]), fromGlobalYToMapY(mapPos[1]) };
    }

    default int[] fromGlobalToMap(float[] mapPos) {
        return new int[] { fromGlobalXToMapX(mapPos[0]), fromGlobalYToMapY(mapPos[1]) };
    }
}
