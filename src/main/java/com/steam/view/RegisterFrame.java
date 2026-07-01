package com.steam.view;

import com.steam.controller.SteamController;
import com.steam.exception.SteamException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtEmail;
    private JTextField txtNickname;

    public RegisterFrame() {
        setTitle("Steam 遊戲平台 - 註冊");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 380);
        setLocationRelativeTo(null);
        setResizable(false);

        contentPane = new JPanel();
        contentPane.setBackground(new Color(23, 26, 33));
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 20));

        JLabel lblTitle = new JLabel("創立全新 STEAM 帳號", JLabel.CENTER);
        lblTitle.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
        lblTitle.setForeground(new Color(102, 192, 244));
        contentPane.add(lblTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new GridLayout(4, 2, 10, 15));

        JLabel lblUsername = new JLabel("使用者名稱:");
        lblUsername.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        lblUsername.setForeground(Color.WHITE);
        formPanel.add(lblUsername);

        txtUsername = new JTextField();
        txtUsername.setBackground(new Color(42, 71, 94));
        txtUsername.setForeground(Color.WHITE);
        txtUsername.setBorder(BorderFactory.createLineBorder(new Color(103, 112, 123)));
        formPanel.add(txtUsername);

        JLabel lblPassword = new JLabel("密碼:");
        lblPassword.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        lblPassword.setForeground(Color.WHITE);
        formPanel.add(lblPassword);

        txtPassword = new JPasswordField();
        txtPassword.setBackground(new Color(42, 71, 94));
        txtPassword.setForeground(Color.WHITE);
        txtPassword.setBorder(BorderFactory.createLineBorder(new Color(103, 112, 123)));
        formPanel.add(txtPassword);

        JLabel lblEmail = new JLabel("電子郵件:");
        lblEmail.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        lblEmail.setForeground(Color.WHITE);
        formPanel.add(lblEmail);

        txtEmail = new JTextField();
        txtEmail.setBackground(new Color(42, 71, 94));
        txtEmail.setForeground(Color.WHITE);
        txtEmail.setBorder(BorderFactory.createLineBorder(new Color(103, 112, 123)));
        formPanel.add(txtEmail);

        JLabel lblNickname = new JLabel("暱稱:");
        lblNickname.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
        lblNickname.setForeground(Color.WHITE);
        formPanel.add(lblNickname);

        txtNickname = new JTextField();
        txtNickname.setBackground(new Color(42, 71, 94));
        txtNickname.setForeground(Color.WHITE);
        txtNickname.setBorder(BorderFactory.createLineBorder(new Color(103, 112, 123)));
        formPanel.add(txtNickname);

        contentPane.add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 5));

        JButton btnSubmit = new JButton("註冊");
        btnSubmit.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        btnSubmit.setBackground(new Color(102, 192, 244));
        btnSubmit.setForeground(Color.BLACK);
        btnSubmit.setFocusPainted(false);
        btnSubmit.addActionListener(e -> handleRegister());
        btnPanel.add(btnSubmit);

        JButton btnBack = new JButton("返回登入");
        btnBack.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        btnBack.setBackground(new Color(42, 71, 94));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        btnPanel.add(btnBack);

        contentPane.add(btnPanel, BorderLayout.SOUTH);
    }

    private void handleRegister() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String email = txtEmail.getText().trim();
        String nickname = txtNickname.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || nickname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "請填妥所有註冊欄位！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            SteamController controller = SteamController.getInstance();
            if (controller.register(username, password, email, nickname)) {
                JOptionPane.showMessageDialog(this, "註冊成功，請登入！", "提示", JOptionPane.INFORMATION_MESSAGE);
                new LoginFrame().setVisible(true);
                dispose();
            }
        } catch (SteamException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "註冊失敗", JOptionPane.ERROR_MESSAGE);
        }
    }
}
