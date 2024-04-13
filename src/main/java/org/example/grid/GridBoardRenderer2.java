package org.example.grid;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GridBoardRenderer2 extends JPanel {
    private final List<double[]> additionalInfo = new ArrayList<>();
    private final JFrame frame;
    private final CartoToPXFunctions essentialFunctions;
    public int widthPX, heightPX;
    private byte[] data;
    private double[] curPosGlobal = null;

    public GridBoardRenderer2(int width, int height, byte[] initDat, CartoToPXFunctions cartoToPXFunctions) {
        this.essentialFunctions = cartoToPXFunctions;
        this.widthPX = width;
        this.heightPX = height;
        this.data = initDat;

        frame = new JFrame("Grid Map");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(this);
        frame.pack();
        frame.setSize(width, height);
        frame.setVisible(true);

        this.reDraw();
    }

    public void setSize(int width, int height) {
        this.widthPX = width;
        this.heightPX = height;
        frame.setSize(width, height);
    }

    public void setSizeExtendDat(int width, int height) {
        this.widthPX = width;
        this.heightPX = height;
        frame.setSize(width, height);
    }

    public void clearData() {
        this.data = new byte[widthPX * heightPX];
    }

    public void setCurPosition(double[] pos) {
        curPosGlobal = pos;
    }

    public void setCurPosition(float[] pos) {
        curPosGlobal = new double[]{pos[0], pos[1], pos[2]};
    }

    public void clearAdditionalData() {
        additionalInfo.clear();
    }

    public void putData(byte[] newData, int newW, int newH) {
        this.data = newData.clone();
        this.setSize(newW, newH);
        this.reDraw();
    }

    public void addAdditionalInfo(int[] positionMap) {
        additionalInfo.add(essentialFunctions.fromMapToGlobal(positionMap));
    }

    public void addAdditionalInfo(double[] positionMap) {
        additionalInfo.add(positionMap);
    }

    public void addAdditionalInfo(float[] positionMap) {
        if (positionMap.length <= 2) {
            additionalInfo.add(new double[]{positionMap[0], positionMap[1]});
        } else {
            additionalInfo.add(new double[]{positionMap[0], positionMap[1], positionMap[2]});
        }
    }

    public void updateResolution(double newRes) {
        GlobalData.resolution = newRes;
    }

    public void updateGlobalOrigen(double[] newOrigen) {
        GlobalData.originX = newOrigen[0];
        GlobalData.originY = newOrigen[1];
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (data == null || data.length == 0)
            return;

        for (int y = 0; y < heightPX; y++) {
            for (int x = 0; x < widthPX; x++) {
                double value = (data[y * widthPX + x] & 0xff) / 100.;

                int color = 255;
                if (value == 2.55) {
                    color = 0;
                }

                /*if (color > 255) {
                    color = 255;
                } else if (color < 0) {
                    color = 0;
                }*/

                g.setColor(new Color(color, color, color));
                g.fillRect(x, y, 20, 20);
            }
        }

        for (double[] cur : additionalInfo) {
            int[] i = essentialFunctions.fromGlobalToMap(cur);
            g.setColor(Color.RED);
            g.fillRect(i[0], i[1], 2, 2);

            if (cur.length > 2) {
                g.setColor(Color.BLUE);
                int[] point2 = getPointInDirection(i, cur[2], 20);
                g.drawLine(i[0], i[1], point2[0], point2[1]);
            }
        }

        if (curPosGlobal != null) {
            int[] curPosMap = essentialFunctions.fromGlobalToMap(curPosGlobal);
            g.setColor(Color.BLUE);
            g.fillRect(curPosMap[0], curPosMap[1], 2, 2);

            g.setColor(Color.CYAN);
            int[] point2 = getPointInDirection(curPosMap, curPosGlobal[2], 20);
            g.drawLine(curPosMap[0], curPosMap[1], point2[0], point2[1]);
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
        return new int[]{newX, newY};
    }

    public void reDraw() {
        this.repaint();
    }
}
