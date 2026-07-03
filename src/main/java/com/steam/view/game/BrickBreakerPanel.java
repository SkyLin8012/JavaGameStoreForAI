package com.steam.view.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class BrickBreakerPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private Timer timer;
    private int ballX = 150, ballY = 400;
    private int ballDX = -3, ballDY = -4;
    private int paddleX = 120;
    private final int PADDLE_WIDTH = 80;
    private boolean gameOver = false;
    private int score = 0;

    // Bricks grid 5x8
    private boolean[][] bricks = new boolean[5][8];

    public BrickBreakerPanel() {
        setFocusable(true);
        setBackground(new Color(25, 25, 35));
        addKeyListener(new TAdapter());

        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 8; j++) {
                bricks[i][j] = true;
            }
        }

        timer = new Timer(15, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        ballX += ballDX;
        ballY += ballDY;

        // Bounce left/right
        if (ballX <= 0 || ballX >= getWidth() - 15) {
            ballDX = -ballDX;
        }
        // Bounce top
        if (ballY <= 0) {
            ballDY = -ballDY;
        }

        // Paddle Collision
        if (ballY >= getHeight() - 50 && ballY <= getHeight() - 40) {
            if (ballX + 15 >= paddleX && ballX <= paddleX + PADDLE_WIDTH) {
                ballDY = -ballDY;
                // Add some direct dynamic impact based on where it hit
                int offset = (ballX + 7) - (paddleX + PADDLE_WIDTH/2);
                ballDX = offset / 8;
            }
        }

        // Out of bounds
        if (ballY > getHeight()) {
            gameOver = true;
            com.steam.controller.SteamController.getInstance().recordScore(4, score);
        }

        // Bricks impact
        int brickWidth = 60;
        int brickHeight = 20;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 8; j++) {
                if (bricks[i][j]) {
                    int bX = 30 + j * 65;
                    int bY = 50 + i * 25;
                    Rectangle rect = new Rectangle(bX, bY, brickWidth, brickHeight);
                    Rectangle ballRect = new Rectangle(ballX, ballY, 15, 15);
                    if (rect.intersects(ballRect)) {
                        bricks[i][j] = false;
                        score += 20;
                        ballDY = -ballDY;
                        break;
                    }
                }
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw Paddle
        g.setColor(new Color(46, 204, 113));
        g.fillRoundRect(paddleX, getHeight() - 50, PADDLE_WIDTH, 15, 10, 10);

        // Draw Ball
        g.setColor(Color.WHITE);
        g.fillOval(ballX, ballY, 15, 15);

        // Draw Bricks
        int brickWidth = 60;
        int brickHeight = 20;
        Color[] rowColors = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN};
        for (int i = 0; i < 5; i++) {
            g.setColor(rowColors[i]);
            for (int j = 0; j < 8; j++) {
                if (bricks[i][j]) {
                    g.fillRect(30 + j * 65, 50 + i * 25, brickWidth, brickHeight);
                }
            }
        }

        // Scores
        g.setColor(Color.WHITE);
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
        g.drawString("分數: " + score, 20, 30);

        if (gameOver) {
            g.drawString("GAME OVER! 分數: " + score, getWidth()/2 - 100, getHeight()/2);
        }
    }

    class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT) {
                if (paddleX > 10) paddleX -= 15;
            }
            if (key == KeyEvent.VK_RIGHT) {
                if (paddleX < getWidth() - PADDLE_WIDTH - 10) paddleX += 15;
            }
        }
    }
}
