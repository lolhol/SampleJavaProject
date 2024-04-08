package org.example.grid;

public class GlobalData {
    public static double resolution = 0.5;
    public static double originX;
    public static double originY;

    public static CartoToPXFunctions getInterface() {
        return new CartoToPXFunctions() {

            @Override
            public int fromGlobalXToMapX(double global) {
                return (int) ((global - GlobalData.originX) / GlobalData.resolution);
            }

            @Override
            public double fromMapXToGlobalX(int map) {
                return (map * GlobalData.resolution + GlobalData.originX);
            }

            @Override
            public int fromGlobalYToMapY(double global) {
                return (int) ((global - GlobalData.originY) / GlobalData.resolution);
            }

            @Override
            public double fromMapYToGlobalY(int map) {
                return (map * GlobalData.resolution + GlobalData.originY);
            }

        };
    }
}
