package org.example.grid;

import java.util.List;

public class Board {
    GridBoardRenderer renderer = null;

    public Board(int width, int height, byte[] initList) {
        renderer = new GridBoardRenderer(width, height, initList);
        renderer.reDraw();
    }

    public GridBoardRenderer getRenderer() {
        return renderer;
    }
}
