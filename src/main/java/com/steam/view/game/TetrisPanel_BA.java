package com.steam.view.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class TetrisPanel_BA extends JPanel {
    private static final long serialVersionUID = 1L;
    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 20;
    private final int CELL_SIZE = 25;
    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private int numLinesRemoved = 0;
    private int curX = 0;
    private int curY = 0;
    private JLabel statusbar;
    private Shape curPiece;
    private ShapeType[] board;

    public TetrisPanel_BA() {
        setFocusable(true);
        statusbar = new JLabel(" 得分: 0 ");
        statusbar.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        statusbar.setForeground(Color.WHITE);
        
        setLayout(new BorderLayout());
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(Color.DARK_GRAY);
        statusPanel.add(statusbar);
        add(statusPanel, BorderLayout.NORTH);

        curPiece = new Shape();
        timer = new Timer(400, new TimerListener());
        timer.start();

        board = new ShapeType[BOARD_WIDTH * BOARD_HEIGHT];
        clearBoard();
        addKeyListener(new TAdapter());
        isStarted = true;
        newPiece();
    }

    private int squareWidth() { return CELL_SIZE; }
    private int squareHeight() { return CELL_SIZE; }
    private ShapeType shapeAt(int x, int y) { return board[(y * BOARD_WIDTH) + x]; }

    private void clearBoard() {
        for (int i = 0; i < BOARD_WIDTH * BOARD_HEIGHT; ++i) board[i] = ShapeType.NoShape;
    }

    private void dropDown() {
        int newY = curY;
        while (newY > 0) {
            if (!tryMove(curPiece, curX, newY - 1)) break;
            --newY;
        }
        pieceDropped();
    }

    private void oneLineDown() {
        if (!tryMove(curPiece, curX, curY - 1)) pieceDropped();
    }

    private void pieceDropped() {
        for (int i = 0; i < 4; ++i) {
            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[(y * BOARD_WIDTH) + x] = curPiece.getShapeType();
        }
        removeFullLines();
        if (!isFallingFinished) newPiece();
    }

    private void newPiece() {
        curPiece.setRandomShape();
        curX = BOARD_WIDTH / 2 + 1;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();

        if (!tryMove(curPiece, curX, curY)) {
            curPiece.setShape(ShapeType.NoShape);
            timer.stop();
            isStarted = false;
            statusbar.setText(" 遊戲結束! 總得分: " + numLinesRemoved);
            com.steam.controller.SteamController.getInstance().recordScore(1, numLinesRemoved);
        }
    }

    private boolean tryMove(Shape newPiece, int newX, int newY) {
        for (int i = 0; i < 4; ++i) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);
            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) return false;
            if (shapeAt(x, y) != ShapeType.NoShape) return false;
        }
        curPiece = newPiece;
        curX = newX;
        curY = newY;
        repaint();
        return true;
    }

    private void removeFullLines() {
        int numFullLines = 0;
        for (int i = BOARD_HEIGHT - 1; i >= 0; --i) {
            boolean lineIsFull = true;
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                if (shapeAt(j, i) == ShapeType.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }
            if (lineIsFull) {
                ++numFullLines;
                for (int k = i; k < BOARD_HEIGHT - 1; ++k) {
                    for (int j = 0; j < BOARD_WIDTH; ++j) board[(k * BOARD_WIDTH) + j] = shapeAt(j, k + 1);
                }
            }
        }

        if (numFullLines > 0) {
            numLinesRemoved += numFullLines * 100;
            statusbar.setText(" 得分: " + numLinesRemoved);
            isFallingFinished = true;
            curPiece.setShape(ShapeType.NoShape);
            repaint();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(20, 20, 30));
        g.fillRect(0, 0, getWidth(), getHeight());

        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight() - 50;

        // Draw grid
        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i <= BOARD_WIDTH; i++) {
            g.drawLine(i * squareWidth() + 20, boardTop, i * squareWidth() + 20, boardTop + BOARD_HEIGHT * squareHeight());
        }
        for (int i = 0; i <= BOARD_HEIGHT; i++) {
            g.drawLine(20, boardTop + i * squareHeight(), 20 + BOARD_WIDTH * squareWidth(), boardTop + i * squareHeight());
        }

        for (int i = 0; i < BOARD_HEIGHT; ++i) {
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                ShapeType shape = shapeAt(j, BOARD_HEIGHT - i - 1);
                if (shape != ShapeType.NoShape) drawSquare(g, 20 + j * squareWidth(), boardTop + i * squareHeight(), shape);
            }
        }

        if (curPiece.getShapeType() != ShapeType.NoShape) {
            for (int i = 0; i < 4; ++i) {
                int x = curX + curPiece.x(i);
                int y = curY - curPiece.y(i);
                drawSquare(g, 20 + x * squareWidth(), boardTop + (BOARD_HEIGHT - y - 1) * squareHeight(), curPiece.getShapeType());
            }
        }
    }

    private void drawSquare(Graphics g, int x, int y, ShapeType shape) {
        Color colors[] = { 
            new Color(0, 0, 0), new Color(204, 102, 102), new Color(102, 204, 102), 
            new Color(102, 102, 204), new Color(204, 204, 102), new Color(204, 102, 204), 
            new Color(102, 204, 204), new Color(218, 170, 0) 
        };
        Color color = colors[shape.ordinal()];
        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);
        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
    }

    class TimerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (isFallingFinished) {
                isFallingFinished = false;
                newPiece();
            } else {
                oneLineDown();
            }
        }
    }

    enum ShapeType { NoShape, ZShape, SShape, LineShape, TShape, SquareShape, LShape, MirroredLShape }

    static class Shape {
        private ShapeType pieceShape;
        private int coords[][];
        private int[][][] coordsTable;

        public Shape() {
            coords = new int[4][2];
            setShape(ShapeType.NoShape);
        }

        public void setShape(ShapeType shape) {
            coordsTable = new int[][][] {
                { { 0, 0 },   { 0, 0 },   { 0, 0 },   { 0, 0 } },
                { { 0, -1 },  { 0, 0 },   { -1, 0 },  { -1, 1 } },
                { { 0, -1 },  { 0, 0 },   { 1, 0 },   { 1, 1 } },
                { { 0, -1 },  { 0, 0 },   { 0, 1 },   { 0, 2 } },
                { { -1, 0 },  { 0, 0 },   { 1, 0 },   { 0, 1 } },
                { { 0, 0 },   { 1, 0 },   { 0, 1 },   { 1, 1 } },
                { { -1, -1 }, { 0, -1 },  { 0, 0 },   { 0, 1 } },
                { { 1, -1 },  { 0, -1 },  { 0, 0 },   { 0, 1 } }
            };

            for (int i = 0; i < 4 ; i++) {
                for (int j = 0; j < 2; ++j) coords[i][j] = coordsTable[shape.ordinal()][i][j];
            }
            pieceShape = shape;
        }

        private void setX(int index, int x) { coords[index][0] = x; }
        private void setY(int index, int y) { coords[index][1] = y; }
        public int x(int index) { return coords[index][0]; }
        public int y(int index) { return coords[index][1]; }
        public ShapeType getShapeType()  { return pieceShape; }

        public void setRandomShape() {
            Random r = new Random();
            int x = Math.abs(r.nextInt()) % 7 + 1;
            ShapeType[] values = ShapeType.values();
            setShape(values[x]);
        }

        public int minX() {
            int m = coords[0][0];
            for (int i=0; i < 4; i++) m = Math.min(m, coords[i][0]);
            return m;
        }

        public int minY() {
            int m = coords[0][1];
            for (int i=0; i < 4; i++) m = Math.min(m, coords[i][1]);
            return m;
        }

        public Shape rotateLeft() {
            if (pieceShape == ShapeType.SquareShape) return this;
            Shape result = new Shape();
            result.pieceShape = pieceShape;
            for (int i = 0; i < 4; ++i) {
                result.setX(i, y(i));
                result.setY(i, -x(i));
            }
            return result;
        }
    }

    class TAdapter extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if (!isStarted || curPiece.getShapeType() == ShapeType.NoShape) return;
            int keycode = e.getKeyCode();
            if (keycode == KeyEvent.VK_LEFT) tryMove(curPiece, curX - 1, curY);
            if (keycode == KeyEvent.VK_RIGHT) tryMove(curPiece, curX + 1, curY);
            if (keycode == KeyEvent.VK_UP) tryMove(curPiece.rotateLeft(), curX, curY);
            if (keycode == KeyEvent.VK_DOWN) oneLineDown();
            if (keycode == KeyEvent.VK_SPACE) dropDown();
        }
    }
}
