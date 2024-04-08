package org.example.grid;

import javax.swing.*;
import java.awt.Graphics;
import java.awt.Color;
import java.util.*;

public class GridBoardRenderer extends JPanel {
    private byte[] data;
    private List<int[]> additionalInfo = new ArrayList<>();
    private final JFrame frame;

    private double theta = 0;

    private int height, width;
    private int actualWidth, actualHeight;
    private boolean isUseActualWH = false;

    public GridBoardRenderer(int width, int height, byte[] initDat) {
        data = initDat;
        this.height = height;
        this.width = width;
        this.actualWidth = height;
        this.actualHeight = width;
        frame = new JFrame("Grid Map");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(this);
        frame.pack();
        frame.setSize(width, height);
        frame.setVisible(true);
    }

    public void addAdditionalInfo(int[] pos) {
        additionalInfo.add(pos);
    }

    public void removeAInfo() {
        additionalInfo.clear();
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        frame.setSize(width, height);
    }

    public void setRetSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setActualSize(int width, int height) {
        this.actualWidth = width;
        this.actualHeight = height;
        frame.setSize(width, height);
    }

    public void putData(byte[] newData, int newW, int newH) {
        this.data = newData.clone();
        this.setSize(newW, newH);
        this.reDraw();
    }

    public void putDataRet(byte[] newData, int retDataW, int retDataH) {
        this.width = retDataW;
        this.height = retDataH;
        this.data = newData;
        this.reDraw();
    }

    public int[] getMapData() {
        return new int[] { actualWidth, actualHeight };
    }

    public void setAngle(double theta) {
        this.theta = theta;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (data.length <= 0)
            return;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double value = (data[y * width + x] & 0xff) / 100.;

                int color;
                if (value == 2.55) {
                    color = 127;
                } else {
                    if (value < 0.5) {
                        color = (data[y * width + x] & 0xff) + 127;
                    } else {
                        color = (data[y * width + x] & 0xff) - 127;
                    }
                }

                if (color > 255) {
                    color = 255;
                } else if (color < 0) {
                    color = 0;
                }

                g.setColor(new Color(color, color, color));
                g.fillRect(x, y, 1, 1);

                /*
                 * if (color < 180) {
                 * // Blocked
                 * 
                 * g.setColor(Color.RED);
                 * g.fillRect(x, y, boxWidth, boxHeight);
                 * } else if (color > 180) {
                 * // Unobstructed
                 * 
                 * g.setColor(Color.WHITE);
                 * g.fillRect(x, y, boxWidth, boxHeight);
                 * } else if (color == 3) {
                 * // Unknown
                 * 
                 * g.setColor(Color.BLACK);
                 * g.drawRect(x, y, boxWidth, boxHeight);
                 * }
                 */
            }
        }

        for (int[] i : additionalInfo) {
            g.setColor(Color.RED);
            g.fillRect(i[0], i[1], 2, 2);

            g.setColor(Color.BLUE);
            int[] point2 = getPointInDirection(i, theta, 20);
            // g.drawLine(i[0], i[1], point2[0], point2[1]);
        }

        // g.setColor(Color.BLUE);
        // System.out.println(this.playerPosInfo[0] * this.playerPosInfo[3] + " | " +
        // this.playerPosInfo[1] * this.playerPosInfo[4]);
        // g.fillRect(getAbsPlayerPos()[0], getAbsPlayerPos()[1], 5, 5);
        // g.drawLine(getAbsPlayerPos()[0], getAbsPlayerPos()[1], (int)
        // (getAbsPlayerPos()[0] + 30 * Math.cos(Math.toRadians(playerPosInfo[2]))),
        // (int) (getAbsPlayerPos()[1] + 30 *
        // Math.sin(Math.toRadians(playerPosInfo[2]))));
    }

    private int[] getPointInDirection(int[] coordinates, double angle, double dist) {
        int x = coordinates[0];
        int y = coordinates[1];
        int newX = x + (int) (dist * Math.cos(angle));
        int newY = y + (int) (dist * Math.sin(angle));
        return new int[] { newX, newY };
    }

    public void reDraw() {
        this.repaint();
    }
}
