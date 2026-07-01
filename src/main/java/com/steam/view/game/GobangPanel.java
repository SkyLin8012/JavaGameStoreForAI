package com.steam.view.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GobangPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final int BOARD_SIZE = 15;
    private final int CELL_SIZE = 35;
    private final int MARGIN = 30;
    // 0: empty, 1: Black (Player), 2: White (AI)
    private int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
    private boolean isPlayerTurn = true;
    private boolean gameOver = false;
    private String winnerText = "五子棋 - 您的回合 (點擊落子)";

    public GobangPanel() {
        setBackground(new Color(243, 197, 126));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameOver || !isPlayerTurn) return;
                int x = Math.round((float)(e.getX() - MARGIN) / CELL_SIZE);
                int y = Math.round((float)(e.getY() - MARGIN) / CELL_SIZE);

                if (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE) {
                    if (board[x][y] == 0) {
                        board[x][y] = 1; // Black
                        repaint();
                        if (checkWin(x, y, 1)) {
                            winnerText = "恭喜你贏了！ 點擊右鍵重新開始";
                            gameOver = true;
                            com.steam.controller.SteamController.getInstance().recordScore(3, 100);
                        } else {
                            isPlayerTurn = false;
                            winnerText = "電腦思考中...";
                            Timer aiTimer = new Timer(600, ev -> runAI());
                            aiTimer.setRepeats(false);
                            aiTimer.start();
                        }
                    }
                }
            }
        });

        // Right click to reset
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    resetGame();
                }
            }
        });
    }

    private void resetGame() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        gameOver = false;
        isPlayerTurn = true;
        winnerText = "五子棋 - 您的回合 (點擊落子)";
        repaint();
    }

    private void runAI() {
        if (gameOver) return;
        // Simple defensive AI or random placement
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 0) {
                    board[i][j] = 2; // AI falls
                    if (checkWin(i, j, 2)) {
                        winnerText = "電腦贏了！ 點擊右鍵重新開始";
                        gameOver = true;
                    } else {
                        isPlayerTurn = true;
                        winnerText = "您的回合 (點擊落子)";
                    }
                    repaint();
                    return;
                }
            }
        }
    }

    private boolean checkWin(int x, int y, int player) {
        int[][] directions = {{1,0}, {0,1}, {1,1}, {1,-1}};
        for (int[] d : directions) {
            int count = 1;
            // Forward
            int nx = x + d[0], ny = y + d[1];
            while (nx >= 0 && nx < BOARD_SIZE && ny >= 0 && ny < BOARD_SIZE && board[nx][ny] == player) {
                count++;
                nx += d[0]; ny += d[1];
            }
            // Backward
            nx = x - d[0]; ny = y - d[1];
            while (nx >= 0 && nx < BOARD_SIZE && ny >= 0 && ny < BOARD_SIZE && board[nx][ny] == player) {
                count++;
                nx -= d[0]; ny -= d[1];
            }
            if (count >= 5) return true;
        }
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Grid lines
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1));
        for (int i = 0; i < BOARD_SIZE; i++) {
            g2.drawLine(MARGIN, MARGIN + i * CELL_SIZE, MARGIN + (BOARD_SIZE - 1) * CELL_SIZE, MARGIN + i * CELL_SIZE);
            g2.drawLine(MARGIN + i * CELL_SIZE, MARGIN, MARGIN + i * CELL_SIZE, MARGIN + (BOARD_SIZE - 1) * CELL_SIZE);
        }

        // Draw stones
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 1) { // Black
                    g2.setColor(Color.BLACK);
                    g2.fillOval(MARGIN + i * CELL_SIZE - CELL_SIZE/2, MARGIN + j * CELL_SIZE - CELL_SIZE/2, CELL_SIZE, CELL_SIZE);
                } else if (board[i][j] == 2) { // White
                    g2.setColor(Color.WHITE);
                    g2.fillOval(MARGIN + i * CELL_SIZE - CELL_SIZE/2, MARGIN + j * CELL_SIZE - CELL_SIZE/2, CELL_SIZE, CELL_SIZE);
                    g2.setColor(Color.BLACK);
                    g2.drawOval(MARGIN + i * CELL_SIZE - CELL_SIZE/2, MARGIN + j * CELL_SIZE - CELL_SIZE/2, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        // Subtitle Text
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        g2.drawString(winnerText, MARGIN, MARGIN + BOARD_SIZE * CELL_SIZE + 20);
        g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        g2.drawString("提示：按滑鼠右鍵可重置遊戲", MARGIN, MARGIN + BOARD_SIZE * CELL_SIZE + 40);
    }
}
