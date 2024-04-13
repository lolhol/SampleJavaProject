package org.example.util;

import brigero.IMUUnitree;
import brigero.Point;
import brigero.PointCloud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CacheManager {
    final File cacheDir;

    public CacheManager(String filePath, boolean flushAtStart) throws IOException {
        this.cacheDir = new File(filePath);

        if (!cacheDir.exists()) {
            cacheDir.createNewFile();
        }

        if (flushAtStart) {
            cacheDir.delete();
            cacheDir.createNewFile();
        }
    }

    public List<CachedEntree> loadData() throws IOException {
        try (FileInputStream reader = new FileInputStream(cacheDir)) {
            List<CachedEntree> cachedEntrees = new ArrayList<>();

            while (reader.available() > 0) {
                byte identifier = reader.readNBytes(1)[0];
                long timestamp = MemUtils.bytesToLong(reader.readNBytes(8));

                if (identifier == CacheType.IMU.identifier) {
                    cachedEntrees.add(new CachedEntree(null, readIMUData(reader), timestamp));
                } else {
                    cachedEntrees.add(new CachedEntree(readPoints(reader), null, timestamp));
                }
            }

            reader.close();

            return cachedEntrees;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private IMUUnitree readIMUData(FileInputStream reader) throws IOException {
        float[] linear_acceleration = new float[3];
        for (int i = 0; i < 3; i++) {
            linear_acceleration[i] = MemUtils.bytesToFloat(reader.readNBytes(4));
        }

        float[] angular_velocity = new float[3];
        for (int i = 0; i < 3; i++) {
            angular_velocity[i] = MemUtils.bytesToFloat(reader.readNBytes(4));
        }

        return new IMUUnitree(0, 0, new float[]{0, 0, 0, 0}, angular_velocity, linear_acceleration);
    }

    private PointsCombined readPoints(FileInputStream reader) throws IOException {
        int numPoints = MemUtils.bytesToInt(reader.readNBytes(4));

        System.out.println("Reading " + numPoints + " points...");

        float[] xs = new float[numPoints];
        float[] ys = new float[numPoints];
        float[] zs = new float[numPoints];
        float[] is = new float[numPoints];

        for (int i = 0; i < numPoints; i++) {
            xs[i] = MemUtils.bytesToFloat(reader.readNBytes(4));
            ys[i] = MemUtils.bytesToFloat(reader.readNBytes(4));
            zs[i] = MemUtils.bytesToFloat(reader.readNBytes(4));
            is[i] = MemUtils.bytesToFloat(reader.readNBytes(4));
        }
        return new PointsCombined(xs, ys, zs, is);
    }

    /**
     * @param data      the data to be added to the file
     * @param curTimeMS the current time in milliseconds
     * @throws IOException if an I/O error occurs
     * @apiNote will write: identifier (byte 1), timestamp (long 8 bytes), (x (float 4 bytes), y (float 4 bytes), z (float 4 bytes)), (x (float 4 bytes), y (float 4 bytes), z (float 4 bytes))
     */
    public void saveIMUData(IMUUnitree data, long curTimeMS) throws IOException {
        FileOutputStream writer = new FileOutputStream(cacheDir, true);
        writer.write(CacheType.IMU.identifier);

        writer.write(MemUtils.longToBytes(curTimeMS)); // write timestamp

        write3Floats(writer, data.linear_acceleration);
        write3Floats(writer, data.angular_velocity);
    }

    private void write3Floats(FileOutputStream writer, float[] data) throws IOException {
        for (int i = 0; i < 3; i++) {
            writer.write(MemUtils.floatToBytes(data[i]));
        }
    }

    /**
     * @param cloud     the cloud to add to the file
     * @param curTimeMS the current time in milliseconds
     * @throws IOException if an I/O error occurs
     * @apiNote will write in the format: identifier (byte 1), timestamp (long 8 bytes), number of points (int 4 bytes), (x (float 4 bytes),
     * y (float 4 bytes), z (float 4 bytes), i (float 4 bytes))
     */
    public void savePointCloud(PointCloud cloud, long curTimeMS) throws IOException {
        FileOutputStream writer = new FileOutputStream(cacheDir, true);

        writer.write(CacheType.CLOUD.identifier);

        writer.write(MemUtils.longToBytes(curTimeMS)); // write timestamp
        writer.write(MemUtils.intToBytes(cloud.point.length)); // write number of points
        System.out.println("Writing " + cloud.point.length + " points...");

        // write points
        for (Point p : cloud.point) {
            writer.write(MemUtils.floatToBytes(p.x));
            writer.write(MemUtils.floatToBytes(p.y));
            writer.write(MemUtils.floatToBytes(p.z));
            writer.write(MemUtils.floatToBytes(p.intensity));
        }
    }

    public class CachedEntree {
        public final PointsCombined points;
        public final IMUUnitree imu;
        public final long timestamp;

        public CachedEntree(PointsCombined points, IMUUnitree imu, long timestamp) {
            this.points = points;
            this.imu = imu;
            this.timestamp = timestamp;
        }
    }

    public class PointsCombined {
        public final float[] xs;
        public final float[] ys;
        public final float[] zs;
        public final float[] is;

        public PointsCombined(float[] xs, float[] ys, float[] zs, float[] is) {
            this.xs = xs;
            this.ys = ys;
            this.zs = zs;
            this.is = is;
        }
    }
}
