package com.steam.view.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PacmanPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final int BLOCK_SIZE = 24;
    private final int N_BLOCKS = 15;
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;
    private Timer timer;
    private int score = 0;
    private int pacmanX = BLOCK_SIZE * 7, pacmanY = BLOCK_SIZE * 11;
    private int reqDX, reqDY, pacmanDX, pacmanDY;
    private int ghostX = BLOCK_SIZE * 7, ghostY = BLOCK_SIZE * 3;
    private int ghostDX = BLOCK_SIZE/8, ghostDY = 0;
    private boolean gameOver = false;

    // 1: wall, 2: dot, 0: empty
    private final short levelData[] = {
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1,
        1, 2, 1, 1, 2, 1, 2, 1, 2, 1, 2, 1, 1, 2, 1,
        1, 2, 1, 1, 2, 1, 2, 1, 2, 1, 2, 1, 1, 2, 1,
        1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1,
        1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1,
        1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1,
        1, 1, 1, 1, 2, 1, 0, 0, 0, 1, 2, 1, 1, 1, 1,
        0, 0, 0, 1, 2, 1, 0, 0, 0, 1, 2, 1, 0, 0, 0,
        1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1,
        1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1,
        1, 2, 1, 1, 2, 1, 2, 1, 2, 1, 2, 1, 1, 2, 1,
        1, 2, 2, 1, 2, 2, 2, 0, 2, 2, 2, 1, 2, 2, 1,
        1, 1, 2, 1, 2, 1, 1, 1, 1, 1, 2, 1, 2, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
    };

    private short[] screenData;

    public PacmanPanel() {
        setFocusable(true);
        setBackground(Color.BLACK);
        screenData = new short[N_BLOCKS * N_BLOCKS];
        System.arraycopy(levelData, 0, screenData, 0, levelData.length);
        addKeyListener(new TAdapter());

        timer = new Timer(40, this);
        timer.start();
    }

    private void movePacman() {
        if (reqDX != 0 || reqDY != 0) {
            int nextX = pacmanX + reqDX;
            int nextY = pacmanY + reqDY;
            if (isValidPosition(nextX, nextY)) {
                pacmanDX = reqDX;
                pacmanDY = reqDY;
            }
        }

        int newX = pacmanX + pacmanDX;
        int newY = pacmanY + pacmanDY;

        if (isValidPosition(newX, newY)) {
            pacmanX = newX;
            pacmanY = newY;

            // Check dot collision
            int cellX = pacmanX / BLOCK_SIZE;
            int cellY = pacmanY / BLOCK_SIZE;
            int idx = cellY * N_BLOCKS + cellX;
            if (screenData[idx] == 2) {
                screenData[idx] = 0;
                score += 10;
            }
        }
    }

    private void moveGhost() {
        ghostX += ghostDX;
        ghostY += ghostDY;
        if (!isValidPosition(ghostX, ghostY) || Math.random() < 0.05) {
            ghostX -= ghostDX;
            ghostY -= ghostDY;
            // Pick random direction
            int r = (int)(Math.random() * 4);
            if (r == 0) { ghostDX = 4; ghostDY = 0; }
            else if (r == 1) { ghostDX = -4; ghostDY = 0; }
            else if (r == 2) { ghostDX = 0; ghostDY = 4; }
            else { ghostDX = 0; ghostDY = -4; }
        }
    }

    private boolean isValidPosition(int x, int y) {
        if (x < 0 || x >= SCREEN_SIZE || y < 0 || y >= SCREEN_SIZE) return false;
        int cellX = x / BLOCK_SIZE;
        int cellY = y / BLOCK_SIZE;
        int idx = cellY * N_BLOCKS + cellX;
        return screenData[idx] != 1;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;
        movePacman();
        moveGhost();
        if (Math.abs(pacmanX - ghostX) < BLOCK_SIZE - 4 && Math.abs(pacmanY - ghostY) < BLOCK_SIZE - 4) {
            gameOver = true;
            timer.stop();
            com.steam.controller.SteamController.getInstance().recordScore(2, score);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw maze
        for (int y = 0; y < N_BLOCKS; y++) {
            for (int x = 0; x < N_BLOCKS; x++) {
                int type = screenData[y * N_BLOCKS + x];
                if (type == 1) {
                    g.setColor(new Color(5, 5, 200));
                    g.fillRect(x * BLOCK_SIZE, y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                } else if (type == 2) {
                    g.setColor(Color.WHITE);
                    g.fillOval(x * BLOCK_SIZE + BLOCK_SIZE/2 - 2, y * BLOCK_SIZE + BLOCK_SIZE/2 - 2, 4, 4);
                }
            }
        }

        // Draw Pacman
        g.setColor(Color.YELLOW);
        g.fillArc(pacmanX, pacmanY, BLOCK_SIZE, BLOCK_SIZE, 30, 300);

        // Draw Ghost
        g.setColor(Color.RED);
        g.fillOval(ghostX, ghostY, BLOCK_SIZE, BLOCK_SIZE);
        g.setColor(Color.WHITE);
        g.fillOval(ghostX + 5, ghostY + 5, 5, 5);
        g.fillOval(ghostX + 13, ghostY + 5, 5, 5);

        // Draw Score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        g.drawString("Score: " + score, 10, SCREEN_SIZE + 20);

        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, SCREEN_SIZE, SCREEN_SIZE + 30);
            g.setColor(Color.RED);
            g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));
            g.drawString("GAME OVER", SCREEN_SIZE / 2 - 70, SCREEN_SIZE / 2);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
            g.drawString("按 ESC 鍵可重新開始", SCREEN_SIZE / 2 - 65, SCREEN_SIZE / 2 + 30);
        }
    }

    class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (gameOver && key == KeyEvent.VK_ESCAPE) {
                gameOver = false;
                score = 0;
                pacmanX = BLOCK_SIZE * 7;
                pacmanY = BLOCK_SIZE * 11;
                ghostX = BLOCK_SIZE * 7;
                ghostY = BLOCK_SIZE * 3;
                System.arraycopy(levelData, 0, screenData, 0, levelData.length);
                timer.start();
                repaint();
                return;
            }
            if (key == KeyEvent.VK_LEFT)  { reqDX = -4; reqDY = 0; }
            if (key == KeyEvent.VK_RIGHT) { reqDX = 4;  reqDY = 0; }
            if (key == KeyEvent.VK_UP)    { reqDX = 0;  reqDY = -4; }
            if (key == KeyEvent.VK_DOWN)  { reqDX = 0;  reqDY = 4; }
        }
    }
}
