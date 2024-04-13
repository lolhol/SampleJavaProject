package org.example;

import brigero.cartographer4java.Cartographer3D;
import org.example.grid.GridBoardRenderer2;
import org.example.util.CacheManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DemoFromCache {
    private static final boolean isOpen = true;

    public static void main(String[] args) throws IOException, InterruptedException {
        Cartographer3D carto = new Cartographer3D();
        final String file = "cached_cloud";
        final CacheManager cm = new CacheManager(file, false);
        final int pxPerMeter = 100;

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

                    if (out == null) {
                        continue;
                    }

                    List<float[]> filteredList = filterOutOfRange(out);

                    float[] minMaxXY = getMinMaxXY(filteredList);
                    System.out.println(Arrays.toString(minMaxXY) + " | " + filteredList.size());

                    if (minMaxXY[0] == Float.MAX_VALUE || minMaxXY[1] == Float.MAX_VALUE || minMaxXY[2] == Float.MIN_VALUE || minMaxXY[3] == Float.MIN_VALUE)
                        continue;

                    int width = (int) (pxPerMeter * (Math.abs(minMaxXY[2] - minMaxXY[0]))) + pxPerMeter;
                    int height = (int) (pxPerMeter * (Math.abs(minMaxXY[3] - minMaxXY[1]))) + pxPerMeter;

                    byte[] newData = new byte[width * height];
                    for (float[] dat : filteredList) {

                        //if (dat[2] < -0.5 || dat[2] > 0.5) continue;

                        int x = (int) Math.abs(dat[0] * pxPerMeter);
                        int z = (int) Math.abs(dat[1] * pxPerMeter);

                        //

                        newData[z * width + x] = (byte) 255;
                    }

                    System.out.println(width + " " + height);
                    b2.putData(newData, width, height);

                    //System.out.println(Arrays.toString(minMaxXY));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        boolean isPointsSeen = false;
        for (var cur : cm.loadData()) {
            if (cur.points == null) {
                carto.addIMUData(System.currentTimeMillis(), "imu0", cur.imu.linear_acceleration, cur.imu.angular_velocity);
                //System.out.println(cur.timestamp + " imu");
            }

            if (cur.points != null) {
                isPointsSeen = true;
                //System.out.println(cur.timestamp + " pointcloud");
                carto.addLidarData(System.currentTimeMillis(), "range0", cur.points.xs, cur.points.ys, cur.points.zs, cur.points.is);
            }

            Thread.sleep(isPointsSeen ? 50 : 0);
        }

        System.out.println("DONE");
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
            if (f[1] > maxY) maxY = f[1];
            if (f[0] < minX) minX = f[0];
            if (f[1] < minY) minY = f[1];
        }
        return new float[]{minX, minY, maxX, maxY};
    }
}
