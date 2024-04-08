package org.example.util;

import brigero.Point;
import brigero.PointCloud;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CacheManager {
    final File cacheDir;

    public CacheManager(String filePath) throws IOException {
        this.cacheDir = new File(filePath);
        if (!cacheDir.exists()) {
            cacheDir.createNewFile();
        }
    }

    /**
     * @param line the current line
     * @return index 0 will be the timestamp, index 1 will be the index of the end of the timestamp string
     */
    public long[] getTimeStampLine(String line) {
        int i = 0;
        StringBuilder sb = new StringBuilder();
        while (line.charAt(i) != ' ') {
            sb.append(line.charAt(i));
            i++;
        }

        return new long[]{Long.parseLong(sb.toString()), i + 1};
    }

    public HashMap<Long, List<Point>> getPoints() throws IOException {
        if (!cacheDir.exists()) {
            throw new RuntimeException("Cache directory not found: " + cacheDir.getAbsolutePath());
        }

        final FileReader reader = new FileReader(cacheDir);
        final BufferedReader bufferedReader = new BufferedReader(reader);

        String line;
        HashMap<Long, List<Point>> points = new HashMap<>();
        while ((line = bufferedReader.readLine()) != null) {
            StringBuilder sb = new StringBuilder();
            long[] timestamp = getTimeStampLine(line);

            List<Point> pointList = new ArrayList<>();
            List<Float> tmp = new ArrayList<>();

            for (int i = (int) timestamp[1]; i < line.length(); i++) {
                var curChar = line.charAt(i);
                if (curChar == ' ') {
                    if (tmp.size() != 4 && !sb.isEmpty()) {
                        tmp.add(Float.parseFloat(sb.toString()));
                    } else {
                        pointList.add(new Point(tmp.get(0), tmp.get(1), tmp.get(2), tmp.get(3), 0.0F, 0));
                        tmp.clear();
                    }

                    sb = new StringBuilder();
                    continue;
                }

                sb.append(curChar);
            }

            points.put(timestamp[0], pointList);
        }

        bufferedReader.close();
        reader.close();

        return points;
    }

    public void savePointCloud(PointCloud cloud, Long curTime) throws IOException {
        if (!cacheDir.exists()) {
            throw new RuntimeException("Cache directory not found: " + cacheDir.getAbsolutePath());
        }

        final FileWriter writer = new FileWriter(cacheDir, true);
        var points = cloud.point;

        writer.write(curTime.toString());
        for (var point : points) {
            writer.append(" " + point.x + " " + point.y + " " + point.z + " " + point.intensity);
        }

        writer.write("\n");
    }
}
