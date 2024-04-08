package org.example;

import brigero.*;

import java.util.ArrayList;
import java.util.List;

public class Main {
  static UnitreeLidar4Java unitreeLidar4Java = new UnitreeLidar4Java();
  public static void main(String[] args) throws InterruptedException {
    System.out.println("START");
    unitreeLidar4Java.init("/dev/ttyUSB0");
    unitreeLidar4Java.setState(UnitreeLidar4JavaStates.STANDBY);
    Thread.sleep(1000);
    unitreeLidar4Java.setState(UnitreeLidar4JavaStates.NORMAL);
    System.out.println("End");
    Thread.sleep(1000);

    List<IMUUnitree> imuData = new ArrayList<>();
    List<PointCloud> points = new ArrayList<>();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      unitreeLidar4Java.setState(UnitreeLidar4JavaStates.STANDBY);
    }));

    unitreeLidar4Java.setLidarLEDMode(LEDDisplayMode.SIXSTAGE_BREATHING);

    MessageType prev = null;
    while (true) {
      Thread.sleep(0, 500);

      var state = unitreeLidar4Java.getCurMessageEnum();
      switch (state) {
        case IMU:
          var imu = unitreeLidar4Java.getIMUData();
          if (imu.angular_velocity[0] != 0) {
            imuData.add(imu);
          }
        case POINTCLOUD:
          var pointCloud = unitreeLidar4Java.getPointCloudObject();
          if (pointCloud.point != null && pointCloud.point.length != 0 && pointCloud.point[0] != null) {
            points.add(pointCloud);
          }
      }
    }
  }
}