package org.example;

import brigero.IMUUnitree;
import brigero.PointCloud;
import brigero.UnitreeLidar4Java;
import brigero.UnitreeLidar4JavaStates;
import brigero.cartographer4java.Cartographer3D;
import org.example.grid.GridBoardRenderer2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Demo2 {
    public static boolean isOpen = true;

    public static void main(String[] args) throws InterruptedException {
        Cartographer3D carto = new Cartographer3D();
        final int pxPerMeter = 20;
        carto.init("src/main/java/org/example/configuration_files_3d", "cartographer_config_main_3d.lua", 8, new String[]{"imu0"}, new String[]{}, new String[]{"range0"});
        final String USBPort = "/dev/ttyUSB0";
        final UnitreeLidar4Java unitree = new UnitreeLidar4Java();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> unitree.setState(UnitreeLidar4JavaStates.STANDBY)));

        new Thread(() -> {
            GridBoardRenderer2 b2 = new GridBoardRenderer2(100, 100, null, null);
            try {
                while (isOpen) {
                    Thread.sleep(500);
                    var out = carto.paintMap();
                    List<float[]> filteredList = filterOutOfRange(out);

                    float[] minMaxXY = getMinMaxXY(filteredList);
                    System.out.println(Arrays.toString(minMaxXY) + " | " + filteredList.size());

                    if (minMaxXY[0] == Float.MAX_VALUE || minMaxXY[1] == Float.MAX_VALUE || minMaxXY[2] == Float.MIN_VALUE || minMaxXY[3] == Float.MIN_VALUE)
                        continue;

                    int width = (int) (pxPerMeter * (Math.abs(minMaxXY[2] - minMaxXY[0]))) + pxPerMeter;
                    int height = (int) (pxPerMeter * (Math.abs(minMaxXY[3] - minMaxXY[1]))) + pxPerMeter;
                    System.out.println(width + " " + height);

                    byte[] newData = new byte[width * height];
                    for (float[] dat : filteredList) {

                        if (dat[1] > 0.2 || dat[1] < -0.2) continue;

                        int x = (int) Math.abs(dat[0] * pxPerMeter);
                        int z = (int) Math.abs(dat[2] * pxPerMeter);

                        //

                        newData[z * width + x] = (byte) 255;
                    }

                    b2.putData(newData, width, height);

                    //System.out.println(Arrays.toString(minMaxXY));
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
                    handlePointCloud(unitree.getPointCloudObject(), carto);
                    break;
                case IMU:
                    IMUUnitree imuUnitree = unitree.getIMUData();
                    carto.addIMUData(System.currentTimeMillis(), "imu0", imuUnitree.linear_acceleration, imuUnitree.angular_velocity);
                    break;
            }
            Thread.sleep(0, 500);
        }
    }

    private static List<float[]> filterOutOfRange(float[] out) {
        List<float[]> list = new ArrayList<>();
        for (int i = 0; i < out.length; i += 4) {
            float x = out[i];
            float y = out[i + 1];
            float z = out[i + 2];
            if (x > 100 || y > 100 || z > 100 || x < -100 || y < -100 || z < -100) continue;
            list.add(new float[]{x, y, z, out[i + 3]});
        }
        return list;
    }

    private static void handlePointCloud(PointCloud pointCloud, Cartographer3D carto) {
        float[] x = new float[pointCloud.point.length];
        float[] y = new float[pointCloud.point.length];
        float[] z = new float[pointCloud.point.length];
        float[] intensity = new float[pointCloud.point.length];
        for (int i = 0; i < pointCloud.point.length; i++) {
            x[i] = pointCloud.point[i].x;
            y[i] = pointCloud.point[i].y;
            z[i] = pointCloud.point[i].z;
            intensity[i] = pointCloud.point[i].intensity;
        }
        System.out.println(x[0] + " " + y[0] + " " + z[0] + " " + intensity[0]);
        carto.addLidarData(System.currentTimeMillis(), "range0", x, y, z, intensity);
    }

    public static float[] getMinMaxXY(List<float[]> points) {
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;

        for (float[] f : points) {
            if (f[0] > maxX) maxX = f[0];
            if (f[2] > maxY) maxY = f[2];
            if (f[0] < minX) minX = f[0];
            if (f[2] < minY) minY = f[2];
        }
        return new float[]{minX, minY, maxX, maxY};
    }

    public static float[] getMinMaxXY(float[] points) {
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;

        for (int i = 0; i < points.length; i += 4) {
            if (points[i] > maxX) maxX = points[i];
            if (points[i + 2] > maxY) maxY = points[i + 1];
            if (points[i] < minX) minX = points[i];
            if (points[i + 2] < minY) minY = points[i + 1];
        }
        return new float[]{minX, minY, maxX, maxY};
    }
}
