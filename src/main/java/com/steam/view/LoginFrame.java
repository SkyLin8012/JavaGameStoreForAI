package com.steam.view;

import com.steam.controller.SteamController;
import com.steam.exception.SteamException;
import com.steam.model.Member;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginFrame() {
        setTitle("Steam 遊戲平台 - 登入");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        contentPane = new JPanel();
        contentPane.setBackground(new Color(23, 26, 33)); // Steam dark slate blue
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 20));

        // Title Header
        JLabel lblTitle = new JLabel("STEAM GAME PLATFORM", JLabel.CENTER);
        lblTitle.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
        lblTitle.setForeground(new Color(102, 192, 244));
        contentPane.add(lblTitle, BorderLayout.NORTH);

        // Center Panel for Form
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new GridLayout(2, 2, 10, 15));

        JLabel lblUsername = new JLabel("使用者名稱:");
        lblUsername.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        lblUsername.setForeground(Color.WHITE);
        formPanel.add(lblUsername);

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        txtUsername.setBackground(new Color(42, 71, 94));
        txtUsername.setForeground(Color.WHITE);
        txtUsername.setCaretColor(Color.WHITE);
        txtUsername.setBorder(BorderFactory.createLineBorder(new Color(103, 112, 123)));
        formPanel.add(txtUsername);

        JLabel lblPassword = new JLabel("密碼:");
        lblPassword.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        lblPassword.setForeground(Color.WHITE);
        formPanel.add(lblPassword);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        txtPassword.setBackground(new Color(42, 71, 94));
        txtPassword.setForeground(Color.WHITE);
        txtPassword.setCaretColor(Color.WHITE);
        txtPassword.setBorder(BorderFactory.createLineBorder(new Color(103, 112, 123)));
        formPanel.add(txtPassword);

        contentPane.add(formPanel, BorderLayout.CENTER);

        // South Panel for Action Buttons
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 5));

        JButton btnLogin = new JButton("登入");
        btnLogin.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        btnLogin.setBackground(new Color(102, 192, 244));
        btnLogin.setForeground(Color.BLACK);
        btnLogin.setFocusPainted(false);
        btnLogin.addActionListener(this::handleLogin);
        btnPanel.add(btnLogin);

        JButton btnRegister = new JButton("註冊帳號");
        btnRegister.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        btnRegister.setBackground(new Color(42, 71, 94));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });
        btnPanel.add(btnRegister);

        contentPane.add(btnPanel, BorderLayout.SOUTH);
    }

    private void handleLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "請填寫所有欄位！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            SteamController controller = SteamController.getInstance();
            Member member = controller.login(username, password);
            JOptionPane.showMessageDialog(this, "歡迎回來, " + member.getNickname() + "!", "登入成功", JOptionPane.INFORMATION_MESSAGE);
            
            // Open platform main frame
            new MainFrame().setVisible(true);
            dispose();
        } catch (SteamException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "登入失敗", JOptionPane.ERROR_MESSAGE);
        }
    }
}
