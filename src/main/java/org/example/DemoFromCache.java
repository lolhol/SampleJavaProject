package org.example;

import brigero.Point;
import brigero.cartographer4java.Cartographer3D;
import org.example.grid.GridBoardRenderer2;
import org.example.util.CacheManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DemoFromCache {
    private static boolean isOpen = true;

    public static void main(String[] args) throws IOException, InterruptedException {
        Cartographer3D carto = new Cartographer3D();
        final String file = "cached_cloud";
        final CacheManager cm = new CacheManager(file);
        final int pxPerMeter = 20;

        carto.init("src/main/java/org/example/configuration_files_3d", "cartographer_config_main_3d.lua", 8, new String[]{"imu0"}, new String[]{}, new String[]{"range0"});

        Runtime.getRuntime().addShutdownHook(new Thread(carto::stopAndOptimize));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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

        isOpen = false;

        for (var cur : cm.getPoints().entrySet()) {
            carto.addIMUData(cur.getKey() - 10, "imu0", new float[]{0, 0, 0}, new float[]{0, 0, 0});

            List<Point> curPoints = cur.getValue();

            float[] x = new float[curPoints.size()];
            float[] y = new float[curPoints.size()];
            float[] z = new float[curPoints.size()];
            float[] i = new float[curPoints.size()];

            for (int j = 0; j < curPoints.size(); j++) {
                x[j] = curPoints.get(j).x;
                y[j] = curPoints.get(j).y;
                z[j] = curPoints.get(j).z;
                i[j] = curPoints.get(j).intensity;
            }

            carto.addLidarData(cur.getKey(), "range0", x, y, z, i);

            Thread.sleep(100);
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
}
