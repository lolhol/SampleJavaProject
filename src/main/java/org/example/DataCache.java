package org.example;

import brigero.UnitreeLidar4Java;
import brigero.UnitreeLidar4JavaStates;
import org.example.util.CacheManager;

import java.io.IOException;

public class DataCache {
    public static void main(String[] args) throws IOException {
        final String file = "cached_cloud";
        final CacheManager manager = new CacheManager(file, true);

        final String USBPort = "/dev/ttyUSB0";
        final UnitreeLidar4Java unitree = new UnitreeLidar4Java();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            unitree.setState(UnitreeLidar4JavaStates.STANDBY);
        }));
        unitree.init(USBPort);

        try {
            unitree.setState(UnitreeLidar4JavaStates.STANDBY);
            Thread.sleep(1000);

            unitree.setState(UnitreeLidar4JavaStates.NORMAL);
            Thread.sleep(1000);

            long startTime = System.currentTimeMillis();
            int cachedAmount = 0;
            while (cachedAmount < 101 && System.currentTimeMillis() - startTime < 60000) {
                switch (unitree.getCurMessageEnum()) {
                    case POINTCLOUD:
                        var obj = unitree.getPointCloudObject();
                        manager.savePointCloud(obj, (long) obj.stamp);
                        cachedAmount++;
                    case IMU:
                        var o = unitree.getIMUData();
                        manager.saveIMUData(o, (long) o.stamp);
                }
                Thread.sleep(0, 500);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
