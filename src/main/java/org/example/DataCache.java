package org.example;

import brigero.UnitreeLidar4Java;
import brigero.UnitreeLidar4JavaStates;
import org.example.util.CacheManager;

import java.io.IOException;

public class DataCache {
    public static void main(String[] args) {
        try {
            final String file = "cached_cloud";
            final CacheManager manager = new CacheManager(file);

            final String USBPort = "/dev/ttyUSB0";
            final UnitreeLidar4Java unitree = new UnitreeLidar4Java();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> unitree.setState(UnitreeLidar4JavaStates.STANDBY)));
            unitree.init(USBPort);

            unitree.setState(UnitreeLidar4JavaStates.NORMAL);
            Thread.sleep(1000);


            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 10000) {
                switch (unitree.getCurMessageEnum()) {
                    case POINTCLOUD:
                        manager.savePointCloud(unitree.getPointCloudObject(), System.currentTimeMillis());
                    case IMU:
                        break;
                }
                Thread.sleep(0, 500);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
