package com.steam.view.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class FlappyBirdPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private int birdY = 250;
    private double velocity = 0;
    private double gravity = 0.5;
    private int pipeX = 500;
    private int pipeGap = 130;
    private int pipeHeight = 150;
    private boolean gameOver = false;
    private int score = 0;
    private Timer timer;

    public FlappyBirdPanel() {
        setFocusable(true);
        setBackground(new Color(113, 197, 207)); // Classic sky blue
        addKeyListener(new TAdapter());

        timer = new Timer(20, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        velocity += gravity;
        birdY += velocity;

        // Move obstacles
        pipeX -= 4;
        if (pipeX < -60) {
            pipeX = getWidth() > 0 ? getWidth() : 500;
            pipeHeight = (int) (Math.random() * 200) + 80;
            score++;
        }

        // Ceiling or floor collision
        if (birdY > getHeight() - 40 || birdY < 0) {
            gameOver = true;
            com.steam.controller.SteamController.getInstance().recordScore(5, score);
        }

        // Obstacles collision
        Rectangle birdRect = new Rectangle(100, birdY, 34, 24);
        Rectangle pipeTop = new Rectangle(pipeX, 0, 60, pipeHeight);
        Rectangle pipeBottom = new Rectangle(pipeX, pipeHeight + pipeGap, 60, getHeight() - (pipeHeight + pipeGap));

        if (birdRect.intersects(pipeTop) || birdRect.intersects(pipeBottom)) {
            gameOver = true;
            com.steam.controller.SteamController.getInstance().recordScore(5, score);
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw bird
        g.setColor(Color.YELLOW);
        g.fillOval(100, birdY, 34, 24);
        g.setColor(Color.WHITE);
        g.fillOval(120, birdY + 3, 8, 8);
        g.setColor(Color.RED);
        g.fillOval(125, birdY + 10, 10, 5);

        // Draw Pipes
        g.setColor(new Color(115, 191, 46));
        g.fillRect(pipeX, 0, 60, pipeHeight);
        g.fillRect(pipeX, pipeHeight + pipeGap, 60, getHeight());

        // Draw ground
        g.setColor(new Color(222, 216, 149));
        g.fillRect(0, getHeight() - 40, getWidth(), 40);

        // Draw Score
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + score, 20, 30);

        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("GAME OVER!", getWidth()/2 - 70, getHeight()/2);
        }
    }

    class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                if (gameOver) {
                    birdY = 250;
                    velocity = 0;
                    pipeX = getWidth() > 0 ? getWidth() : 500;
                    score = 0;
                    gameOver = false;
                } else {
                    velocity = -8.5; // Jump
                }
            }
        }
    }
}
