package org.example;

import brigero.Point;
import brigero.PointCloud;
import brigero.UnitreeLidar4Java;
import brigero.UnitreeLidar4JavaStates;
import org.example.grid.CartoToPXFunctions;
import org.example.grid.GlobalData;
import org.example.grid.GridBoardRenderer2;
import org.example.testing_robot.extern.carto.CartographerOut;
import org.example.testing_robot.extern.carto.GoogleCartographer;

import java.util.ArrayList;
import java.util.List;

public class Demo1 {
    public static boolean isOpen = true;

    public static void main(String[] args) throws InterruptedException {
        GoogleCartographer carto = new GoogleCartographer();
        final String USBPort = "/dev/ttyUSB0";
        final UnitreeLidar4Java unitree = new UnitreeLidar4Java();

        carto.initiate("src/main/java/org/example/configuration_files", "cartographer_config_main.lua", false, false,
                10);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            carto.deleteCartoAndOptimize();
            unitree.setState(UnitreeLidar4JavaStates.STANDBY);
        }));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            GridBoardRenderer2 b2 = null;
            CartographerOut out = null;
            CartoToPXFunctions functions = GlobalData.getInterface();

            try {
                while (isOpen) {
                    out = carto.getCartographerMapData();

                    if (out == null)
                        continue;

                    if (b2 == null) {
                        GlobalData.originX = out.originX;
                        GlobalData.originY = out.originY;
                        GlobalData.resolution = out.resolution;

                        b2 = new GridBoardRenderer2((int) out.mapSizeX, (int) out.mapSizeY, out.map,
                                functions);
                        continue;
                    } else {
                        b2.updateResolution(out.resolution);
                        b2.updateGlobalOrigen(new double[]{out.originX, out.originY});
                        b2.putData(out.map, (int) out.mapSizeX, (int) out.mapSizeY);

                        b2.setCurPosition(out.functions.GetGlobalData());
                    }

                    Thread.sleep(250);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        unitree.init(USBPort);
        unitree.setState(UnitreeLidar4JavaStates.STANDBY);
        Thread.sleep(1000);
        unitree.setState(UnitreeLidar4JavaStates.NORMAL);
        Thread.sleep(1000);

        while (isOpen) {
            switch (unitree.getCurMessageEnum()) {
                case POINTCLOUD:
                    PointCloud pointCloud = unitree.getPointCloudObject();

                    List<Float> tmpX = new ArrayList<>();
                    List<Float> tmpY = new ArrayList<>();
                    List<Float> tmpIntensity = new ArrayList<>();

                    for (int i = 0; i < pointCloud.point.length; i++) {
                        var cur = pointCloud.point[i];
                        if (cur.y < -0.2 || cur.y > 0.2) {
                            continue;
                        }

                        tmpX.add(cur.x);
                        tmpY.add(cur.z);
                        tmpIntensity.add(cur.intensity);
                    }

                    float[] x = new float[tmpX.size()];
                    float[] y = new float[tmpX.size()];
                    float[] intensity = new float[tmpX.size()];
                    for (int i = 0; i < tmpX.size(); i++) {
                        x[i] = tmpX.get(i);
                        y[i] = tmpY.get(i);
                        intensity[i] = tmpIntensity.get(i);
                    }

                    carto.updateLidarData(System.currentTimeMillis(), x, y, intensity);
            }

            Thread.sleep(0, 500);
        }
    }

    private static boolean isToRemove(float[] x, float[] y, float[] intensity, Point ceilPoint, Point cur, int curPos) {
        double[] slopes = calculateSlopes(cur.x, cur.y, cur.z, ceilPoint.x, ceilPoint.y, ceilPoint.z);
        return slopes[0] >= -0.5 && slopes[0] <= 0.5 || slopes[1] >= -0.5 && slopes[1] <= 0.5;
    }

    private static Point getBestCeilPoint(Point[] pointCloud) {
        float[] range = new float[]{-0.5F, 0.5F, -0.5F, 0.5F};
        Point bestPoint = null;
        float bestPointHeight = Float.MAX_VALUE;
        int pointsFound = 0;
        for (Point cur : pointCloud) {
            if (pointsFound >= 16) {
                return bestPoint;
            }

            if (cur.x >= range[0] && cur.x <= range[1] && cur.z >= range[2] && cur.z <= range[3]) {
                pointsFound++;
                if (bestPoint == null || cur.y < bestPointHeight) {
                    bestPoint = cur;
                    bestPointHeight = cur.y;
                }
            }
        }

        System.out.println(pointsFound);

        return bestPoint;
    }

    public static double getDistanceXZ(Point point1, Point point2) {
        return Math.sqrt(point1.x * point2.x + point1.z * point2.z);
    }

    public static double getDistanceXZ(int[] pos1, Point point2) {
        return Math.sqrt(pos1[0] * point2.x + pos1[1] * point2.z);
    }

    public static double[] calculateSlopes(double x, double y, double z, double x1, double y1, double z1) {
        return new double[]{getSlope(x, y, x1, y1), getSlope(z, y, z1, y1)};
    }

    public static double getSlope(double x, double y, double x1, double y1) {
        return (x - x1) / (y - y1);
    }
}
