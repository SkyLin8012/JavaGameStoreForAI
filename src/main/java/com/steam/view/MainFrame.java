package com.steam.view;

import com.steam.controller.SteamController;
import com.steam.exception.SteamException;
import com.steam.model.Game;
import com.steam.model.Member;
import com.steam.view.game.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Date;
import java.text.SimpleDateFormat;

// JFreeChart imports for rich data visualizations
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTabbedPane tabbedPane;
    
    // Profiles tab
    private JLabel lblWallet;
    private JTextField txtEmail;
    private JTextField txtNickname;
    private JPasswordField txtPassword;

    // Leaderboard & Achievements fields
    private DefaultTableModel scoreModel;
    private DefaultTableModel achModel;
    private DefaultTableModel salesModel;
    private DefaultTableModel adminGamesModel;
    private DefaultTableModel storeModel;
    private Runnable refreshAnalytics;

    // Shopping Cart fields
    private DefaultTableModel cartModel;
    private JLabel lblCartTotal;
    private JLabel lblCartBalance;
    private JLabel lblCartRemain;
    private JButton btnCartCheckout;
    private JButton btnCartRemove;
    private JButton btnCartClear;

    // Detail and icon preview fields
    private JPanel detailPanel;
    private JLabel lblDetailIcon;
    private JLabel lblDetailName;
    private JLabel lblDetailPrice;
    private JLabel lblDetailGenre;
    private JTextArea txtDetailDesc;
    private JButton btnAddToCart;
    private JButton btnDetailBuy;
    private JPanel iconCardPanel;

    public MainFrame() {
        setTitle("Steam 整合式遊戲模擬平台 - Java SE Swing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 600);
        setLocationRelativeTo(null);

        contentPane = new JPanel();
        contentPane.setBackground(new Color(27, 40, 56));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 10));

        // Header Panel (Current User Info and Logout)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(23, 26, 33));
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        Member currentUser = SteamController.getInstance().getCurrentLoggedInMember();
        String greetText = "您好, " + (currentUser != null ? currentUser.getNickname() : "玩家") + "！ (" + 
                           (currentUser != null ? currentUser.getRole() : "GUEST") + ")";
        JLabel lblUserGreeting = new JLabel(greetText);
        lblUserGreeting.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
        lblUserGreeting.setForeground(new Color(102, 192, 244));
        headerPanel.add(lblUserGreeting, BorderLayout.WEST);

        JButton btnLogout = new JButton("登出");
        btnLogout.setBackground(new Color(192, 57, 43));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            SteamController.getInstance().logout();
            new LoginFrame().setVisible(true);
            dispose();
        });
        headerPanel.add(btnLogout, BorderLayout.EAST);
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Core Tabs
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        tabbedPane.setBackground(new Color(23, 26, 33));
        tabbedPane.setForeground(new Color(103, 112, 123));

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int selectedIndex = tabbedPane.getSelectedIndex();
                if (selectedIndex >= 0) {
                    String title = tabbedPane.getTitleAt(selectedIndex);
                    if ("排行榜 & 成就".equals(title)) {
                        refreshLeaderboard(scoreModel, achModel, salesModel);
                    } else if ("個人帳戶".equals(title)) {
                        updateProfileBalanceLabel();
                    } else if ("購物車".equals(title)) {
                        refreshCart();
                    } else if ("管理控制 (Admin Only)".equals(title)) {
                        if (refreshAnalytics != null) {
                            refreshAnalytics.run();
                        }
                    }
                }
            }
        });

        // Create Tabs
        createStoreTab();
        createCartTab();
        createLibraryTab();
        createLeaderboardTab();
        createProfileTab();

        // Admin Tab (Show only if Admin role)
        if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
            createAdminTab();
        }

        contentPane.add(tabbedPane, BorderLayout.CENTER);
    }

    private void createCartTab() {
        JPanel cartPanel = new JPanel(new BorderLayout(15, 15));
        cartPanel.setBackground(new Color(27, 40, 56));
        cartPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Left Panel: Cart Table
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("您的購物車 🛒");
        lblTitle.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        leftPanel.add(lblTitle, BorderLayout.NORTH);

        String[] columnNames = {"遊戲編號", "遊戲名稱", "類型", "售價"};
        cartModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(cartModel);
        table.setBackground(new Color(23, 26, 33));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(41, 54, 73));
        table.setSelectionBackground(new Color(102, 192, 244));
        table.setSelectionForeground(Color.BLACK);
        table.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        table.getTableHeader().setBackground(new Color(23, 26, 33));
        table.getTableHeader().setForeground(new Color(103, 112, 123));
        table.getTableHeader().setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(new javax.swing.border.LineBorder(new Color(255, 255, 255, 5), 1));
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        // Right Panel: Summary card
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(new Color(23, 26, 33));
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        rightPanel.setPreferredSize(new Dimension(280, 0));

        JLabel lblSummaryTitle = new JLabel("訂單消費摘要");
        lblSummaryTitle.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        lblSummaryTitle.setForeground(new Color(102, 192, 244));
        lblSummaryTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(lblSummaryTitle);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        lblCartTotal = new JLabel("購物車總計: $0.00");
        lblCartTotal.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        lblCartTotal.setForeground(Color.WHITE);
        lblCartTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(lblCartTotal);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        lblCartBalance = new JLabel("您的目前餘額: $0.00");
        lblCartBalance.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        lblCartBalance.setForeground(new Color(103, 112, 123));
        lblCartBalance.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(lblCartBalance);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        lblCartRemain = new JLabel("結帳後剩餘: $0.00");
        lblCartRemain.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        lblCartRemain.setForeground(new Color(103, 112, 123));
        lblCartRemain.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(lblCartRemain);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        btnCartCheckout = new JButton("確認結帳，立即購買");
        btnCartCheckout.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        btnCartCheckout.setBackground(new Color(88, 186, 39));
        btnCartCheckout.setForeground(Color.WHITE);
        btnCartCheckout.setFocusPainted(false);
        btnCartCheckout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnCartCheckout.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(btnCartCheckout);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        btnCartRemove = new JButton("移除選取遊戲");
        btnCartRemove.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        btnCartRemove.setBackground(new Color(192, 57, 43));
        btnCartRemove.setForeground(Color.WHITE);
        btnCartRemove.setFocusPainted(false);
        btnCartRemove.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btnCartRemove.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(btnCartRemove);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        btnCartClear = new JButton("清空購物車");
        btnCartClear.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        btnCartClear.setBackground(new Color(103, 112, 123));
        btnCartClear.setForeground(Color.WHITE);
        btnCartClear.setFocusPainted(false);
        btnCartClear.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btnCartClear.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(btnCartClear);

        cartPanel.add(leftPanel, BorderLayout.CENTER);
        cartPanel.add(rightPanel, BorderLayout.EAST);
        tabbedPane.addTab("購物車 🛒", cartPanel);

        // Action Listeners
        btnCartRemove.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "請先選擇欲自購物車中移除的遊戲！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int gameId = (int) table.getValueAt(selectedRow, 0);
            try {
                if (SteamController.getInstance().removeFromCart(gameId)) {
                    refreshCart();
                    refreshStoreTable(storeModel);
                }
            } catch (SteamException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCartClear.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "確認清空整個購物車？", "清空購物車", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    SteamController.getInstance().clearCart();
                    refreshCart();
                    refreshStoreTable(storeModel);
                } catch (SteamException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnCartCheckout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "確定購買購物車內的所有遊戲？", "確認結帳", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    List<com.steam.model.CartItem> itemsToReceipt = SteamController.getInstance().getMyCart();
                    if (SteamController.getInstance().checkoutCart()) {
                        JOptionPane.showMessageDialog(this, "結帳交易成功！已將遊戲加入您的收藏庫。", "完成結帳", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Show printable receipt!
                        showReceiptFrame(itemsToReceipt);

                        refreshCart();
                        refreshStoreTable(storeModel);
                        refreshLibrary();
                        updateProfileBalanceLabel();
                    }
                } catch (SteamException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "結帳失敗", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        try {
            Member currentUser = SteamController.getInstance().getCurrentLoggedInMember();
            BigDecimal balance = currentUser != null ? currentUser.getBalance() : BigDecimal.ZERO;
            lblCartBalance.setText("您的目前餘額: $" + balance);

            List<com.steam.model.CartItem> items = SteamController.getInstance().getMyCart();
            BigDecimal total = BigDecimal.ZERO;
            for (com.steam.model.CartItem item : items) {
                cartModel.addRow(new Object[]{
                    item.getGameId(),
                    item.getGameName(),
                    item.getGameGenre(),
                    "$" + item.getGamePrice()
                });
                if (item.getGamePrice() != null) {
                    total = total.add(item.getGamePrice());
                }
            }

            lblCartTotal.setText("購物車總計: $" + total);
            BigDecimal remain = balance.subtract(total);
            lblCartRemain.setText("結帳後剩餘: $" + remain);
            if (remain.compareTo(BigDecimal.ZERO) < 0) {
                lblCartRemain.setForeground(new Color(192, 57, 43));
                btnCartCheckout.setEnabled(false);
                btnCartCheckout.setBackground(new Color(103, 112, 123));
            } else {
                lblCartRemain.setForeground(new Color(88, 186, 39));
                btnCartCheckout.setEnabled(!items.isEmpty());
                if (!items.isEmpty()) {
                    btnCartCheckout.setBackground(new Color(88, 186, 39));
                } else {
                    btnCartCheckout.setBackground(new Color(103, 112, 123));
                }
            }

            btnCartRemove.setEnabled(!items.isEmpty());
            btnCartClear.setEnabled(!items.isEmpty());

        } catch (SteamException ex) {
            ex.printStackTrace();
        }
    }

    private void showReceiptFrame(List<com.steam.model.CartItem> items) {
        JFrame receiptFrame = new JFrame("Steam 交易明細與收據列印");
        receiptFrame.setSize(500, 650);
        receiptFrame.setLocationRelativeTo(this);
        receiptFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Receipt Printable Content Area
        JPanel printableArea = new JPanel() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{5f}, 0f));
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawRect(5, 5, getWidth() - 10, getHeight() - 10);
            }
        };
        printableArea.setBackground(Color.WHITE);
        printableArea.setLayout(new BoxLayout(printableArea, BoxLayout.Y_AXIS));

        // Invoice Header
        JLabel lblStoreName = new JLabel("STEAM 整合式遊戲平台 - 收據證明");
        lblStoreName.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        lblStoreName.setForeground(Color.DARK_GRAY);
        lblStoreName.setAlignmentX(Component.CENTER_ALIGNMENT);
        printableArea.add(lblStoreName);
        printableArea.add(Box.createRigidArea(new Dimension(0, 5)));

        JLabel lblSub = new JLabel("VALVE CORPORATION OFFICIAL INVOICE");
        lblSub.setFont(new Font("Arial", Font.PLAIN, 10));
        lblSub.setForeground(Color.GRAY);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        printableArea.add(lblSub);
        printableArea.add(Box.createRigidArea(new Dimension(0, 15)));

        // Metadata
        Member m = SteamController.getInstance().getCurrentLoggedInMember();
        String nickname = m != null ? m.getNickname() : "匿名玩家";
        String username = m != null ? m.getUsername() : "Guest";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(new Date());

        String metaHtml = "<html><table style='width:100%; font-size:11px; font-family:\"Microsoft JhengHei\"; color:#555;'>" +
                "<tr><td><b>收據編號:</b> ST-" + System.currentTimeMillis() % 10000000 + "</td><td style='text-align:right;'><b>交易日期:</b> " + dateStr + "</td></tr>" +
                "<tr><td><b>買受人:</b> " + nickname + " (" + username + ")</td><td style='text-align:right;'><b>付款方式:</b> Steam 錢包餘額</td></tr>" +
                "</table></html>";
        JLabel lblMeta = new JLabel(metaHtml);
        lblMeta.setAlignmentX(Component.CENTER_ALIGNMENT);
        printableArea.add(lblMeta);
        printableArea.add(Box.createRigidArea(new Dimension(0, 15)));

        // Items Table
        StringBuilder itemsHtml = new StringBuilder();
        itemsHtml.append("<html><table style='width:100%; border-collapse:collapse; font-size:12px; font-family:\"Microsoft JhengHei\";'>");
        itemsHtml.append("<tr style='background-color:#f2f2f2; font-weight:bold;'><th style='padding:6px; text-align:left; border-bottom:2px solid #ddd;'>項目名稱</th><th style='padding:6px; text-align:left; border-bottom:2px solid #ddd;'>類型</th><th style='padding:6px; text-align:right; border-bottom:2px solid #ddd;'>小計</th></tr>");
        
        BigDecimal total = BigDecimal.ZERO;
        for (com.steam.model.CartItem item : items) {
            BigDecimal price = item.getGamePrice() != null ? item.getGamePrice() : BigDecimal.ZERO;
            itemsHtml.append("<tr>");
            itemsHtml.append("<td style='padding:8px; border-bottom:1px solid #eee;'>《").append(item.getGameName()).append("》</td>");
            itemsHtml.append("<td style='padding:8px; border-bottom:1px solid #eee;'>").append(item.getGameGenre()).append("</td>");
            itemsHtml.append("<td style='padding:8px; text-align:right; border-bottom:1px solid #eee;'>$").append(price).append("</td>");
            itemsHtml.append("</tr>");
            total = total.add(price);
        }
        
        itemsHtml.append("<tr><td colspan='2' style='padding:10px; text-align:right; font-weight:bold; font-size:14px;'>總計金額:</td>");
        itemsHtml.append("<td style='padding:10px; text-align:right; font-weight:bold; font-size:14px; color:#c0392b;'>$").append(total).append("</td></tr>");
        itemsHtml.append("</table></html>");

        JLabel lblItemsTable = new JLabel(itemsHtml.toString());
        lblItemsTable.setAlignmentX(Component.CENTER_ALIGNMENT);
        printableArea.add(lblItemsTable);
        printableArea.add(Box.createVerticalGlue());

        // Footer Thank you
        JLabel lblThanks = new JLabel("感謝您的支持與購買！本證明已傳送至您的註冊信箱，請至客戶端下載並遊玩。");
        lblThanks.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        lblThanks.setForeground(Color.GRAY);
        lblThanks.setAlignmentX(Component.CENTER_ALIGNMENT);
        printableArea.add(lblThanks);

        // Print Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setOpaque(false);

        JButton btnPrint = new JButton("🖨️ 列印此收據 (Print Order)");
        btnPrint.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        btnPrint.setBackground(new Color(41, 128, 185));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFocusPainted(false);

        JButton btnClose = new JButton("關閉視窗");
        btnClose.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        btnClose.setBackground(new Color(149, 165, 166));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);

        btnPrint.addActionListener(e -> {
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
            job.setJobName("Steam Receipt - " + nickname);
            job.setPrintable(new java.awt.print.Printable() {
                @Override
                public int print(Graphics graphics, java.awt.print.PageFormat pageFormat, int pageIndex) throws java.awt.print.PrinterException {
                    if (pageIndex > 0) {
                        return NO_SUCH_PAGE;
                    }
                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                    
                    double scaleX = pageFormat.getImageableWidth() / printableArea.getWidth();
                    double scaleY = pageFormat.getImageableHeight() / printableArea.getHeight();
                    double scale = Math.min(scaleX, scaleY);
                    g2d.scale(scale, scale);
                    
                    printableArea.paint(g2d);
                    return PAGE_EXISTS;
                }
            });
            boolean ok = job.printDialog();
            if (ok) {
                try {
                    job.print();
                    JOptionPane.showMessageDialog(receiptFrame, "收據發送至印表機列印中...", "列印成功", JOptionPane.INFORMATION_MESSAGE);
                } catch (java.awt.print.PrinterException ex) {
                    JOptionPane.showMessageDialog(receiptFrame, "列印失敗: " + ex.getMessage(), "列印錯誤", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnClose.addActionListener(e -> receiptFrame.dispose());

        btnPanel.add(btnPrint);
        btnPanel.add(btnClose);

        mainPanel.add(printableArea, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        receiptFrame.setContentPane(mainPanel);
        receiptFrame.setVisible(true);
    }

    private void createStoreTab() {
        JPanel storePanel = new JPanel(new BorderLayout(10, 10));
        storePanel.setBackground(new Color(27, 40, 56));
        storePanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Steam 遊戲商店 (預覽與購買)");
        lblTitle.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        storePanel.add(lblTitle, BorderLayout.NORTH);

        // Table
        String[] columns = {"遊戲 ID", "遊戲名稱", "售價", "遊戲分類", "遊戲描述", "擁有狀態"};
        storeModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(storeModel);
        table.setRowHeight(30);
        table.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(23, 26, 33));
        table.getTableHeader().setForeground(Color.WHITE);

        refreshStoreTable(storeModel);

        JScrollPane scrollPane = new JScrollPane(table);

        // Right Detail Preview Panel
        detailPanel = new JPanel(new BorderLayout(10, 10));
        detailPanel.setBackground(new Color(23, 26, 33));
        detailPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        iconCardPanel = new JPanel(new GridBagLayout());
        iconCardPanel.setPreferredSize(new Dimension(100, 100));
        iconCardPanel.setBackground(new Color(33, 44, 57));
        iconCardPanel.setBorder(new javax.swing.border.LineBorder(new Color(255, 255, 255, 15), 1, true));

        lblDetailIcon = new JLabel("🎮");
        lblDetailIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        lblDetailIcon.setForeground(Color.WHITE);
        iconCardPanel.add(lblDetailIcon);

        JPanel topDetails = new JPanel(new GridLayout(3, 1, 3, 3));
        topDetails.setOpaque(false);

        lblDetailName = new JLabel("請選擇遊戲");
        lblDetailName.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
        lblDetailName.setForeground(Color.WHITE);

        lblDetailGenre = new JLabel("分類: -");
        lblDetailGenre.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        lblDetailGenre.setForeground(new Color(102, 192, 244));

        lblDetailPrice = new JLabel("售價: -");
        lblDetailPrice.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        lblDetailPrice.setForeground(Color.WHITE);

        topDetails.add(lblDetailName);
        topDetails.add(lblDetailGenre);
        topDetails.add(lblDetailPrice);

        JPanel upperPanel = new JPanel(new BorderLayout(10, 10));
        upperPanel.setOpaque(false);
        upperPanel.add(iconCardPanel, BorderLayout.WEST);
        upperPanel.add(topDetails, BorderLayout.CENTER);

        detailPanel.add(upperPanel, BorderLayout.NORTH);

        txtDetailDesc = new JTextArea("請由左側點選一項遊戲來預覽其圖示、售價與詳細遊戲說明。");
        txtDetailDesc.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        txtDetailDesc.setForeground(new Color(196, 196, 196));
        txtDetailDesc.setBackground(new Color(16, 24, 34));
        txtDetailDesc.setLineWrap(true);
        txtDetailDesc.setWrapStyleWord(true);
        txtDetailDesc.setEditable(false);
        txtDetailDesc.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane descScroll = new JScrollPane(txtDetailDesc);
        descScroll.setBorder(new javax.swing.border.LineBorder(new Color(255, 255, 255, 5), 1));
        detailPanel.add(descScroll, BorderLayout.CENTER);

        JPanel buyButtonsPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        buyButtonsPanel.setOpaque(false);

        btnAddToCart = new JButton("加入購物車");
        btnAddToCart.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        btnAddToCart.setBackground(new Color(41, 128, 185));
        btnAddToCart.setForeground(Color.WHITE);
        btnAddToCart.setFocusPainted(false);
        btnAddToCart.setEnabled(false);

        btnDetailBuy = new JButton("直接購買");
        btnDetailBuy.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        btnDetailBuy.setBackground(new Color(103, 112, 123));
        btnDetailBuy.setForeground(Color.WHITE);
        btnDetailBuy.setFocusPainted(false);
        btnDetailBuy.setEnabled(false);

        buyButtonsPanel.add(btnAddToCart);
        buyButtonsPanel.add(btnDetailBuy);

        btnAddToCart.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) return;
            int gameId = (int) table.getValueAt(selectedRow, 0);
            String gameName = (String) table.getValueAt(selectedRow, 1);
            try {
                if (SteamController.getInstance().addToCart(gameId)) {
                    JOptionPane.showMessageDialog(this, "🛒 《" + gameName + "》已加入購物車！", "購物車", JOptionPane.INFORMATION_MESSAGE);
                    refreshCart();
                }
            } catch (SteamException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "提示", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnDetailBuy.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) return;
            int gameId = (int) table.getValueAt(selectedRow, 0);
            String gameName = (String) table.getValueAt(selectedRow, 1);
            try {
                List<Game> games = SteamController.getInstance().listStoreGames();
                Game selectedGame = null;
                for (Game g : games) {
                    if (g.getId() == gameId) {
                        selectedGame = g;
                        break;
                    }
                }
                if (selectedGame != null) {
                    if (SteamController.getInstance().buyGame(selectedGame)) {
                        JOptionPane.showMessageDialog(this, "購買成功！《" + gameName + "》已新增到您的收藏庫。", "完成交易", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Show printable receipt
                        List<com.steam.model.CartItem> singleItem = new java.util.ArrayList<>();
                        com.steam.model.CartItem ci = new com.steam.model.CartItem();
                        ci.setGameId(selectedGame.getId());
                        ci.setGameName(selectedGame.getName());
                        ci.setGamePrice(selectedGame.getPrice());
                        ci.setGameGenre(selectedGame.getGenre());
                        singleItem.add(ci);
                        showReceiptFrame(singleItem);

                        refreshStoreTable(storeModel);
                        refreshLibrary();
                        updateProfileBalanceLabel();
                        updateDetailPanel(selectedGame);
                    }
                }
            } catch (SteamException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "購買失敗", JOptionPane.ERROR_MESSAGE);
            }
        });

        detailPanel.add(buyButtonsPanel, BorderLayout.SOUTH);

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(420);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setLeftComponent(scrollPane);
        splitPane.setRightComponent(detailPanel);

        storePanel.add(splitPane, BorderLayout.CENTER);

        // Selection Listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    int gameId = (int) table.getValueAt(row, 0);
                    try {
                        List<Game> games = SteamController.getInstance().listStoreGames();
                        Game selectedGame = null;
                        for (Game g : games) {
                            if (g.getId() == gameId) {
                                selectedGame = g;
                                break;
                            }
                        }
                        if (selectedGame != null) {
                            updateDetailPanel(selectedGame);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        tabbedPane.addTab("商店", storePanel);
    }

    private void updateDetailPanel(Game g) {
        if (g == null) return;
        
        // Handle Thumbnail rendering (actual PNG file or Fallback Emoji)
        String thumbPath = g.getThumbnail();
        if (thumbPath != null && (thumbPath.toLowerCase().endsWith(".png") || thumbPath.startsWith("img/"))) {
            try {
                java.io.File imgFile = new java.io.File(thumbPath);
                if (imgFile.exists()) {
                    ImageIcon origIcon = new ImageIcon(thumbPath);
                    Image img = origIcon.getImage();
                    Image scaledImg = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                    lblDetailIcon.setIcon(new ImageIcon(scaledImg));
                    lblDetailIcon.setText("");
                } else {
                    java.net.URL imgUrl = MainFrame.class.getResource("/" + thumbPath);
                    if (imgUrl != null) {
                        ImageIcon origIcon = new ImageIcon(imgUrl);
                        Image img = origIcon.getImage();
                        Image scaledImg = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                        lblDetailIcon.setIcon(new ImageIcon(scaledImg));
                        lblDetailIcon.setText("");
                    } else {
                        lblDetailIcon.setIcon(null);
                        lblDetailIcon.setText("🎮");
                    }
                }
            } catch (Exception ex) {
                lblDetailIcon.setIcon(null);
                lblDetailIcon.setText("🎮");
            }
        } else {
            lblDetailIcon.setIcon(null);
            lblDetailIcon.setText(thumbPath != null ? thumbPath : "🎮");
        }

        lblDetailName.setText("《" + g.getName() + "》");
        lblDetailPrice.setText("售價: $" + g.getPrice() + " 元");
        lblDetailGenre.setText("分類: " + g.getGenre());
        txtDetailDesc.setText(g.getDescription());

        Color bgTheme = new Color(33, 44, 57);
        if (g.getBannerColor() != null) {
            String color = g.getBannerColor().toLowerCase();
            if (color.contains("cyan") || color.contains("blue")) {
                bgTheme = new Color(20, 50, 90);
            } else if (color.contains("fuchsia") || color.contains("purple")) {
                bgTheme = new Color(75, 20, 95);
            } else if (color.contains("amber") || color.contains("orange")) {
                bgTheme = new Color(90, 45, 10);
            } else if (color.contains("emerald") || color.contains("teal")) {
                bgTheme = new Color(15, 75, 55);
            } else if (color.contains("rose") || color.contains("pink")) {
                bgTheme = new Color(95, 20, 50);
            }
        }
        iconCardPanel.setBackground(bgTheme);

        boolean owned = false;
        try {
            owned = SteamController.getInstance().checkIfOwned(g.getId());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (owned) {
            btnAddToCart.setText("已擁有");
            btnAddToCart.setEnabled(false);
            btnAddToCart.setBackground(new Color(103, 112, 123));
            btnDetailBuy.setText("已擁有此遊戲");
            btnDetailBuy.setEnabled(false);
            btnDetailBuy.setBackground(new Color(103, 112, 123));
        } else {
            btnAddToCart.setText("加入購物車");
            btnAddToCart.setEnabled(true);
            btnAddToCart.setBackground(new Color(41, 128, 185));
            btnDetailBuy.setText("直接購買 ($" + g.getPrice() + ")");
            btnDetailBuy.setEnabled(true);
            btnDetailBuy.setBackground(new Color(88, 186, 39));
        }

        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private void refreshStoreTable(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            List<Game> games = SteamController.getInstance().listStoreGames();
            for (Game g : games) {
                boolean owned = SteamController.getInstance().checkIfOwned(g.getId());
                model.addRow(new Object[]{
                    g.getId(),
                    g.getName(),
                    g.getPrice(),
                    g.getGenre(),
                    g.getDescription(),
                    owned ? "已擁有" : "未購買"
                });
            }
        } catch (SteamException e) {
            e.printStackTrace();
        }
    }

    private DefaultListModel<String> libraryListModel;
    private JList<String> libraryList;

    private void createLibraryTab() {
        JPanel libraryPanel = new JPanel(new BorderLayout(15, 15));
        libraryPanel.setBackground(new Color(27, 40, 56));
        libraryPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("我的遊戲收藏庫 (點擊執行)");
        lblTitle.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        libraryPanel.add(lblTitle, BorderLayout.NORTH);
        
        lblTitle.setForeground(Color.WHITE);
        libraryPanel.add(lblTitle,BorderLayout.NORTH);
        
        libraryListModel = new DefaultListModel<>();
        libraryList = new JList<>(libraryListModel);
        libraryList.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
        libraryList.setBackground(new Color(23, 26, 33));
        libraryList.setForeground(Color.WHITE);
        
        libraryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        refreshLibrary();

        JScrollPane scrollPane = new JScrollPane(libraryList);
        libraryPanel.add(scrollPane, BorderLayout.CENTER);
        //左右並排的按鈕面板
        JPanel libraryActionPanel = new JPanel(new GridLayout(1,2,10,10));
        libraryActionPanel.setOpaque(false);
        
        JButton btnPlay = new JButton("啟動遊戲 (RUN)");
        btnPlay.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
        btnPlay.setBackground(new Color(102, 192, 244));
        btnPlay.setForeground(Color.BLACK);
        btnPlay.addActionListener(e -> launchSelectedGame());
        libraryActionPanel.add(btnPlay);
        
        JButton btnRemove= new JButton("移除並退款(Refund)");
        btnRemove.setFont(new Font("Microsoft Jhenghei",Font.BOLD,15));
        btnRemove.setBackground(new Color(231,76,60));//優雅的紅色
        btnRemove.setForeground(Color.WHITE);
        btnRemove.addActionListener(e-> refundSelectedGame());
        libraryActionPanel.add(btnRemove);
        
        libraryPanel.add(libraryActionPanel,BorderLayout.SOUTH);
        tabbedPane.addTab("我的收藏庫", libraryPanel);
    }

    private void refundSelectedGame() {
    	 String selected = libraryList.getSelectedValue();
    	    if (selected == null || selected.trim().isEmpty() || selected.contains("未擁有任何遊戲") || selected.contains("失敗")) {
    	        JOptionPane.showMessageDialog(this, "請先在收藏庫選擇一個要退款/移除的遊戲！");
    	        return ;
    	    }
    	    int confirm = JOptionPane.showConfirmDialog(this, 
    	            "您確定要申請退款並將此遊戲「" + selected + "」從您的收藏庫中移除嗎？\n移除後該遊戲若有定價，相應的金額將會退回您的帳戶錢包！", 
    	            "確認退款並移除遊戲", 
    	            JOptionPane.YES_NO_OPTION, 
    	            JOptionPane.WARNING_MESSAGE);
    	        
    	        if (confirm == JOptionPane.YES_OPTION) {
    	            try {
    	                int gameId = -1;
    	                List<com.steam.model.Purchase> purchases = SteamController.getInstance().getMyPurchases();
    	                for (com.steam.model.Purchase p : purchases) {
    	                    if (p.getGameName().equals(selected)) {
    	                        gameId = p.getGameId();
    	                        break;
    	                    }
    	                }
    	                
    	                if (gameId != -1) {
    	                    if (SteamController.getInstance().refundGame(gameId)) {
    	                        JOptionPane.showMessageDialog(this, "✅ 成功辦理退款並將「" + selected + "」自您的收藏庫移除！\n相應款項已退回您的錢包。", "操作成功", JOptionPane.INFORMATION_MESSAGE);
    	                        refreshLibrary();              // 刷新收藏庫列表
    	                        updateProfileBalanceLabel();   // 刷新個人帳戶顯示的餘額
    	                        if (storeModel != null) {
    	                            refreshStoreTable(storeModel); // 刷新商店表格狀態（從“已擁有”變回“購買”）
    	                        }
    	                    } else {
    	                        JOptionPane.showMessageDialog(this, "❌ 無法移除此遊戲，請稍後再試。", "錯誤", JOptionPane.ERROR_MESSAGE);
    	                    }
    	                } else {
    	                    JOptionPane.showMessageDialog(this, "❌ 找不到對應的遊戲資料。", "錯誤", JOptionPane.ERROR_MESSAGE);
    	                }
    	            } catch (SteamException e) {
    	                JOptionPane.showMessageDialog(this, "退款失敗：" + e.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
    	            }
    	        }
	}

	private void refreshLibrary() {
        libraryListModel.clear();
        try {
            var purchases = SteamController.getInstance().getMyPurchases();
            if (purchases.isEmpty()) {
                libraryListModel.addElement(" (您目前未擁有任何遊戲。請前往商店選購！) ");
            } else {
                for (var p : purchases) {
                    libraryListModel.addElement(p.getGameName());
                }
            }
        } catch (SteamException e) {
            libraryListModel.addElement("讀取收藏庫失敗：" + e.getMessage());
        }
    }

    private void launchSelectedGame() {
        String selected = libraryList.getSelectedValue();
        if (selected == null || selected.trim().isEmpty() || selected.contains("未擁有任何遊戲")) {
            JOptionPane.showMessageDialog(this, "請先在收藏庫選擇一個遊戲啟動！");
            return;
        }

        // 1. Try to find the owned game's gameUrl from database
        com.steam.model.Purchase selectedPurchase = null;
        try {
            List<com.steam.model.Purchase> purchases = SteamController.getInstance().getMyPurchases();
            for (com.steam.model.Purchase p : purchases) {
                if (p.getGameName().equals(selected)) {
                    selectedPurchase = p;
                    break;
                }
            }
        } catch (SteamException e) {
            e.printStackTrace();
        }

        String relativePath = selectedPurchase != null ? selectedPurchase.getGameUrl() : null;
        String javaClassPath = selectedPurchase != null ? selectedPurchase.getJavaClassPath() : null;
        if (relativePath == null || relativePath.trim().isEmpty()) {
            if (selected.contains("俄羅斯方塊")) {
                relativePath = "game/tetris.jar";
                javaClassPath = "controller.TetrisGame";
            } else if (selected.contains("小精靈")) {
                relativePath = "game/pacman.jar";
                javaClassPath = "controller.PacmanGame";
            } else if (selected.contains("五子棋")) {
                relativePath = "game/gobang.jar";
                javaClassPath = "controller.GobangGame";
            } else if (selected.contains("打磚塊")) {
                relativePath = "game/brick_breaker.jar";
                javaClassPath = "controller.BrickBreakerGame";
            } else if (selected.contains("飛翔的小鳥")) {
                relativePath = "game/flappy_bird.jar";
                javaClassPath = "controller.FlappyBirdGame";
            }
        }

        // 1.5 Try to launch via Java Reflection if javaClassPath is set
        boolean launchedReflection = false;
        if (javaClassPath != null && !javaClassPath.trim().isEmpty()) {
            try {
                Class<?> clazz = Class.forName(javaClassPath);
                JFrame form = (JFrame) clazz.getDeclaredConstructor().newInstance();
                form.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                form.setVisible(true);
                launchedReflection = true;
                JOptionPane.showMessageDialog(this, "🚀 成功透過 Java 反射機制 (Reflection) 載入並啟動類別：" + javaClassPath + "視窗資源已被配置為獨立釋放 (DISPOSE_ON_CLOSE)！", "反射啟動成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (ClassNotFoundException ex) {
                // Not found, print log in dev console and fallback
                System.out.println("Reflection class not found in classpath: " + javaClassPath + ". Fallbacking to JAR or embedded panel.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ 透過 Java 反射機制載入此類別時出錯：" + ex.getMessage() + 
                		"將轉由其他機制嘗試開啟。", "反射載入錯誤", JOptionPane.WARNING_MESSAGE);
            }
        }

        // 2. Resolve absolute path based on main JAR directory (if reflection was not used or failed)
        boolean launchedExternally = false;
        if (!launchedReflection && relativePath != null && !relativePath.trim().isEmpty()) {
            String baseDir = System.getProperty("user.dir");
            try {
                java.io.File jarFile = new java.io.File(MainFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                if (jarFile.isFile()) {
                    baseDir = jarFile.getParent();
                }
            } catch (Exception ex) {
                // fallback to user.dir
            }

            java.io.File targetJar = new java.io.File(baseDir, relativePath);
            if (targetJar.exists() && targetJar.isFile()) {
                try {
                    ProcessBuilder pb = new ProcessBuilder("java", "-jar", targetJar.getAbsolutePath());
                    pb.directory(targetJar.getParentFile());
                    pb.start();
                    launchedExternally = true;
                    JOptionPane.showMessageDialog(this, "🚀 正在啟動外部小遊戲 JAR 檔：" + 
                    targetJar.getName() + "請在外部視窗中遊玩！", "啟動成功", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "無法啟動外部小遊戲：" + ex.getMessage() + 
                    "將使用內建模擬器啟動。", "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        }

        // 3. Fallback to embedded panel if not launched
        if (!launchedReflection && !launchedExternally) {
            JFrame gameFrame = new JFrame("運行遊戲中 (內建模擬器) - " + selected);
            gameFrame.setSize(600, 650);
            gameFrame.setLocationRelativeTo(null);
            gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JPanel panel = null;
            if (selected.contains("俄羅斯方塊")) {
                panel = new TetrisPanel();
            } else if (selected.contains("小精靈")) {
                panel = new PacmanPanel();
            } else if (selected.contains("五子棋")) {
                panel = new GobangPanel();
            } else if (selected.contains("打磚塊")) {
                panel = new BrickBreakerPanel();
            } else if (selected.contains("飛翔的小鳥")) {
                panel = new FlappyBirdPanel();
            }

            if (panel != null) {
                gameFrame.getContentPane().add(panel);
                gameFrame.setVisible(true);
                panel.requestFocusInWindow();
            } else {
                JOptionPane.showMessageDialog(this, "無法載入此遊戲模組，且未找到對應的外部 JAR 檔案！預期路徑: "
                + (relativePath != null ? relativePath : "無"), "啟動失敗", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void createProfileTab() {
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setBackground(new Color(27, 40, 56));
        profilePanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Member m = SteamController.getInstance().getCurrentLoggedInMember();

        // 1. Greet / Nickname
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblNickLabel = new JLabel("個人暱稱:");
        lblNickLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        lblNickLabel.setForeground(Color.WHITE);
        profilePanel.add(lblNickLabel, gbc);

        gbc.gridx = 1;
        txtNickname = new JTextField(m != null ? m.getNickname() : "", 15);
        profilePanel.add(txtNickname, gbc);

        // 2. Email
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblEmailLabel = new JLabel("電子郵件:");
        lblEmailLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        lblEmailLabel.setForeground(Color.WHITE);
        profilePanel.add(lblEmailLabel, gbc);

        gbc.gridx = 1;
        txtEmail = new JTextField(m != null ? m.getEmail() : "", 15);
        profilePanel.add(txtEmail, gbc);

        // 3. Password
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblPassLabel = new JLabel("更改密碼:");
        lblPassLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        lblPassLabel.setForeground(Color.WHITE);
        profilePanel.add(lblPassLabel, gbc);

        gbc.gridx = 1;
        txtPassword = new JPasswordField(m != null ? m.getPassword() : "", 15);
        profilePanel.add(txtPassword, gbc);

        // 4. Wallet Balance Info
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblWalletLabel = new JLabel("蒸汽錢包餘額:");
        lblWalletLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        lblWalletLabel.setForeground(Color.WHITE);
        profilePanel.add(lblWalletLabel, gbc);

        gbc.gridx = 1;
        lblWallet = new JLabel(m != null ? "$" + m.getBalance() : "$0.0");
        lblWallet.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        lblWallet.setForeground(new Color(88, 186, 39));
        profilePanel.add(lblWallet, gbc);

        // Buttons
        JPanel bPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        bPanel.setOpaque(false);

        JButton btnUpdate = new JButton("更新個人資料");
        btnUpdate.setBackground(new Color(102, 192, 244));
        btnUpdate.setForeground(Color.BLACK);
        btnUpdate.addActionListener(e -> {
            String password = new String(txtPassword.getPassword()).trim();
            String email = txtEmail.getText().trim();
            String nickname = txtNickname.getText().trim();

            if (password.isEmpty() || email.isEmpty() || nickname.isEmpty()) {
                JOptionPane.showMessageDialog(this, "請填寫所有修改項目！");
                return;
            }

            try {
                if (SteamController.getInstance().updateProfile(password, email, nickname)) {
                    JOptionPane.showMessageDialog(this, "帳號資料修改成功！");
                }
            } catch (SteamException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
        bPanel.add(btnUpdate);

        JButton btnAddFunds = new JButton("儲值 $500 蒸汽卡");
        btnAddFunds.setBackground(new Color(88, 186, 39));
        btnAddFunds.setForeground(Color.WHITE);
        btnAddFunds.addActionListener(e -> {
            try {
                SteamController.getInstance().deposit(new BigDecimal("500.00"));
                updateProfileBalanceLabel();
                JOptionPane.showMessageDialog(this, "儲值成功！已匯入 $500 到您的蒸汽錢包。");
            } catch (SteamException ex) {
                ex.printStackTrace();
            }
        });
        bPanel.add(btnAddFunds);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        profilePanel.add(bPanel, gbc);

        tabbedPane.addTab("個人帳戶", profilePanel);
    }

    private void updateProfileBalanceLabel() {
        Member m = SteamController.getInstance().getCurrentLoggedInMember();
        if (lblWallet != null && m != null) {
            lblWallet.setText("$" + m.getBalance());
        }
    }

    private void createLeaderboardTab() {
        JPanel leaderPanel = new JPanel(new BorderLayout(10, 10));
        leaderPanel.setBackground(new Color(27, 40, 56));
        leaderPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Steam 排行榜 & 成就中心");
        lblTitle.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        leaderPanel.add(lblTitle, BorderLayout.NORTH);

        JTabbedPane subTab = new JTabbedPane(JTabbedPane.TOP);
        subTab.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        subTab.setBackground(new Color(23, 26, 33));
        subTab.setForeground(new Color(103, 112, 123));

        // 1. Scores sub-panel
        JPanel scoresPanel = new JPanel(new BorderLayout(10, 10));
        scoresPanel.setBackground(new Color(23, 26, 33));
        String[] scoreCols = {"排名", "遊戲名稱", "玩家暱稱", "最高得分", "紀錄時間"};
        scoreModel = new DefaultTableModel(scoreCols, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable scoreTable = new JTable(scoreModel);
        scoreTable.setRowHeight(25);
        scoreTable.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        scoreTable.getTableHeader().setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        scoresPanel.add(new JScrollPane(scoreTable), BorderLayout.CENTER);
        subTab.addTab("遊戲高分榜", scoresPanel);

        // 2. Game Sales sub-panel
        JPanel salesPanel = new JPanel(new BorderLayout(10, 10));
        salesPanel.setBackground(new Color(23, 26, 33));
        String[] salesCols = {"銷售排行", "遊戲 ID", "遊戲名稱", "售價", "總銷量 (套)", "累計銷售額"};
        salesModel = new DefaultTableModel(salesCols, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable salesTable = new JTable(salesModel);
        salesTable.setRowHeight(25);
        salesTable.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        salesTable.getTableHeader().setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        salesPanel.add(new JScrollPane(salesTable), BorderLayout.CENTER);
        subTab.addTab("熱銷遊戲排行榜", salesPanel);

        // 3. Achievements sub-panel
        JPanel achPanel = new JPanel(new BorderLayout(10, 10));
        achPanel.setBackground(new Color(23, 26, 33));
        String[] achCols = {"成就代號", "成就名稱", "成就分類", "解鎖條件", "描述", "我的狀態"};
        achModel = new DefaultTableModel(achCols, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable achTable = new JTable(achModel);
        achTable.setRowHeight(25);
        achTable.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        achTable.getTableHeader().setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        achPanel.add(new JScrollPane(achTable), BorderLayout.CENTER);
        subTab.addTab("成就解鎖中心", achPanel);

        leaderPanel.add(subTab, BorderLayout.CENTER);

        // Refresh scores, sales and achievements
        JButton btnRefresh = new JButton("重新整理排行榜與成就");
        btnRefresh.setBackground(new Color(102, 192, 244));
        btnRefresh.setForeground(Color.BLACK);
        btnRefresh.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> {
            refreshLeaderboard(scoreModel, achModel, salesModel);
        });
        leaderPanel.add(btnRefresh, BorderLayout.SOUTH);

        tabbedPane.addTab("排行榜 & 成就", leaderPanel);
        
        // Initial load
        refreshLeaderboard(scoreModel, achModel, salesModel);
    }

    private void refreshLeaderboard(DefaultTableModel scoreModel, DefaultTableModel achModel, DefaultTableModel salesModel) {
        scoreModel.setRowCount(0);
        achModel.setRowCount(0);
        if (salesModel != null) {
            salesModel.setRowCount(0);
        }

        Member currentUser = SteamController.getInstance().getCurrentLoggedInMember();
        int currentUserId = currentUser != null ? currentUser.getId() : -1;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // Query Game Scores
        try {
            conn = com.steam.util.DBUtil.getConnection();
            String scoreSql = "SELECT s.score, s.recorded_at, g.name AS game_name, m.nickname AS member_nickname " +
                              "FROM game_score s " +
                              "JOIN game g ON s.game_id = g.id " +
                              "JOIN member m ON s.member_id = m.id " +
                              "ORDER BY g.id ASC, s.score DESC";
            pstmt = conn.prepareStatement(scoreSql);
            rs = pstmt.executeQuery();
            int rank = 1;
            String lastGame = "";
            while (rs.next()) {
                String gameName = rs.getString("game_name");
                if (!gameName.equals(lastGame)) {
                    lastGame = gameName;
                    rank = 1;
                }
                scoreModel.addRow(new Object[]{
                    rank++,
                    gameName,
                    rs.getString("member_nickname"),
                    rs.getInt("score"),
                    rs.getTimestamp("recorded_at")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            com.steam.util.DBUtil.close(conn, pstmt, rs);
        }

        // Query Game Sales
        if (salesModel != null) {
            try {
                conn = com.steam.util.DBUtil.getConnection();
                String salesSql = "SELECT g.id, g.name, g.price, COUNT(p.id) AS sales_count, COALESCE(SUM(g.price), 0) AS total_revenue " +
                                  "FROM game g " +
                                  "LEFT JOIN purchase p ON g.id = p.game_id " +
                                  "GROUP BY g.id, g.name, g.price " +
                                  "ORDER BY sales_count DESC, total_revenue DESC";
                pstmt = conn.prepareStatement(salesSql);
                rs = pstmt.executeQuery();
                int rank = 1;
                while (rs.next()) {
                    salesModel.addRow(new Object[]{
                        rank++,
                        rs.getInt("id"),
                        rs.getString("name"),
                        "$" + rs.getBigDecimal("price"),
                        rs.getInt("sales_count"),
                        "$" + rs.getBigDecimal("total_revenue")
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                com.steam.util.DBUtil.close(conn, pstmt, rs);
            }
        }

        // Query Achievements
        try {
            conn = com.steam.util.DBUtil.getConnection();
            String achSql = "SELECT a.*, " +
                            "(SELECT COUNT(*) FROM unlocked_achievement ua WHERE ua.achievement_id = a.id AND ua.member_id = ?) AS is_unlocked " +
                            "FROM achievement a ORDER BY a.category ASC, a.id ASC";
            pstmt = conn.prepareStatement(achSql);
            pstmt.setInt(1, currentUserId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                boolean unlocked = rs.getInt("is_unlocked") > 0;
                achModel.addRow(new Object[]{
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("category"),
                    rs.getString("requirement_text"),
                    rs.getString("description"),
                    unlocked ? "🏆 已解鎖" : "🔒 未解鎖"
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            com.steam.util.DBUtil.close(conn, pstmt, rs);
        }
    }

    private void createAdminTab() {
        JPanel adminPanel = new JPanel(new BorderLayout(15, 15));
        adminPanel.setBackground(new Color(33, 44, 57));
        adminPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("系統管理員控制台");
        lblTitle.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
        lblTitle.setForeground(new Color(230, 126, 34));
        adminPanel.add(lblTitle, BorderLayout.NORTH);

        JTabbedPane adminSubTabs = new JTabbedPane(JTabbedPane.TOP);
        adminSubTabs.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));

        // SUB-TAB 1: Member management
        JPanel memberTabPanel = new JPanel(new BorderLayout(10, 10));
        memberTabPanel.setBackground(new Color(33, 44, 57));

        String[] memberCols = {"會員 ID", "帳號", "暱稱", "Email", "錢包餘額", "角色"};
        DefaultTableModel memberModel = new DefaultTableModel(memberCols, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable memberTable = new JTable(memberModel);
        memberTable.setRowHeight(25);
        refreshAdminTable(memberModel);
        memberTabPanel.add(new JScrollPane(memberTable), BorderLayout.CENTER);

        JPanel memberBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        memberBtns.setOpaque(false);

        JButton btnEditMember = new JButton("修改會員資料");
        btnEditMember.setBackground(new Color(41, 128, 185));
        btnEditMember.setForeground(Color.WHITE);
        btnEditMember.addActionListener(e -> {
            int row = memberTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "請選擇一個會員！");
                return;
            }
            int id = (int) memberModel.getValueAt(row, 0);
            String username = (String) memberModel.getValueAt(row, 1);
            String nickname = (String) memberModel.getValueAt(row, 2);
            String email = (String) memberModel.getValueAt(row, 3);
            java.math.BigDecimal balance = (java.math.BigDecimal) memberModel.getValueAt(row, 4);
            String role = (String) memberModel.getValueAt(row, 5);

            JPanel editPanel = new JPanel(new GridLayout(4, 2, 5, 5));
            editPanel.add(new JLabel("暱稱:"));
            JTextField nickField = new JTextField(nickname);
            editPanel.add(nickField);

            editPanel.add(new JLabel("Email:"));
            JTextField emailField = new JTextField(email);
            editPanel.add(emailField);

            editPanel.add(new JLabel("錢包餘額:"));
            JTextField balField = new JTextField(balance.toString());
            editPanel.add(balField);

            editPanel.add(new JLabel("角色 (USER/ADMIN):"));
            JTextField roleField = new JTextField(role);
            editPanel.add(roleField);

            int result = JOptionPane.showConfirmDialog(this, editPanel, "修改會員資料 [ID: " + id + "]", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    Member m = new Member();
                    m.setId(id);
                    m.setUsername(username);
                    m.setNickname(nickField.getText().trim());
                    m.setEmail(emailField.getText().trim());
                    m.setBalance(new java.math.BigDecimal(balField.getText().trim()));
                    m.setRole(roleField.getText().trim().toUpperCase());
                    
                    // Retrieve existing password to keep it unchanged
                    java.util.List<Member> allM = SteamController.getInstance().listAllMembers();
                    for (Member existing : allM) {
                        if (existing.getId() == id) {
                            m.setPassword(existing.getPassword());
                            break;
                        }
                    }

                    if (SteamController.getInstance().updateMemberByAdmin(m)) {
                        JOptionPane.showMessageDialog(this, "會員資料修改成功！");
                        refreshAdminTable(memberModel);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "修改失敗: " + ex.getMessage());
                }
            }
        });
        memberBtns.add(btnEditMember);

        JButton btnDeleteMember = new JButton("強制註銷此會員");
        btnDeleteMember.setBackground(new Color(192, 57, 43));
        btnDeleteMember.setForeground(Color.WHITE);
        btnDeleteMember.addActionListener(e -> {
            int row = memberTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "請選擇一個會員！");
                return;
            }
            int id = (int) memberModel.getValueAt(row, 0);
            String nickname = (String) memberModel.getValueAt(row, 2);
            int confirm = JOptionPane.showConfirmDialog(this, "確定要永久刪除會員「" + nickname + "」嗎？", "安全確認", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (SteamController.getInstance().deleteMember(id)) {
                        JOptionPane.showMessageDialog(this, "已成功註銷該會員。");
                        refreshAdminTable(memberModel);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "刪除失敗：" + ex.getMessage());
                }
            }
        });
        memberBtns.add(btnDeleteMember);
        memberTabPanel.add(memberBtns, BorderLayout.SOUTH);
        adminSubTabs.addTab("會員帳號管理", memberTabPanel);

        // SUB-TAB 2: Game management
        JPanel gameTabPanel = new JPanel(new BorderLayout(10, 10));
        gameTabPanel.setBackground(new Color(33, 44, 57));

        String[] gameCols = {"遊戲 ID", "遊戲名稱", "售價", "遊戲描述", "遊戲分類", "縮圖符號", "橫幅色彩", "小遊戲路徑", "Java 類別路徑"};
        adminGamesModel = new DefaultTableModel(gameCols, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable gameTable = new JTable(adminGamesModel);
        gameTable.setRowHeight(25);
        refreshAdminGamesTable();
        gameTabPanel.add(new JScrollPane(gameTable), BorderLayout.CENTER);

        JPanel gameBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        gameBtns.setOpaque(false);

        JButton btnAddGame = new JButton("上架新遊戲");
        btnAddGame.setBackground(new Color(39, 174, 96));
        btnAddGame.setForeground(Color.WHITE);
        btnAddGame.addActionListener(e -> {
            JPanel addPanel = new JPanel(new GridLayout(8, 2, 5, 5));
            addPanel.add(new JLabel("遊戲名稱:"));
            JTextField nameField = new JTextField();
            addPanel.add(nameField);

            addPanel.add(new JLabel("售價:"));
            JTextField priceField = new JTextField("0.00");
            addPanel.add(priceField);

            addPanel.add(new JLabel("遊戲描述:"));
            JTextField descField = new JTextField();
            addPanel.add(descField);

            addPanel.add(new JLabel("分類/類型:"));
            JTextField genreField = new JTextField("益智");
            addPanel.add(genreField);

            addPanel.add(new JLabel("預覽圖 (Emoji 或 img/xxx.png):"));
            JPanel thumbUploadPanel = new JPanel(new BorderLayout(5, 5));
            thumbUploadPanel.setOpaque(false);
            JTextField thumbField = new JTextField("img/default.png");
            JButton btnBrowseThumb = new JButton("選擇 PNG...");
            btnBrowseThumb.addActionListener(ev -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG 圖片 (*.png)", "png"));
                int rv = fc.showOpenDialog(this);
                if (rv == JFileChooser.APPROVE_OPTION) {
                    java.io.File sf = fc.getSelectedFile();
                    try {
                        java.io.File imgDir = new java.io.File("img");
                        if (!imgDir.exists()) imgDir.mkdirs();
                        java.io.File df = new java.io.File(imgDir, sf.getName());
                        java.nio.file.Files.copy(sf.toPath(), df.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        thumbField.setText("img/" + sf.getName());
                    } catch (Exception ioex) {
                        thumbField.setText("img/" + sf.getName());
                    }
                }
            });
            thumbUploadPanel.add(thumbField, BorderLayout.CENTER);
            thumbUploadPanel.add(btnBrowseThumb, BorderLayout.EAST);
            addPanel.add(thumbUploadPanel);

            addPanel.add(new JLabel("橫幅色彩 (Gradient):"));
            JTextField bannerField = new JTextField("from-slate-700 to-slate-900");
            addPanel.add(bannerField);

            addPanel.add(new JLabel("JAR 檔小遊戲 (限制英文檔名):"));
            JPanel jarUploadPanel = new JPanel(new BorderLayout(5, 5));
            jarUploadPanel.setOpaque(false);
            JTextField jarUrlField = new JTextField();
            jarUrlField.setEditable(false);
            
            JPanel jarActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
            jarActionPanel.setOpaque(false);
            
            JButton btnBrowseJar = new JButton("上傳 JAR");
            JButton btnDeleteJar = new JButton("刪除 JAR");
            
            btnBrowseJar.addActionListener(ev -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Java Executable (*.jar)", "jar"));
                int rv = fc.showOpenDialog(this);
                if (rv == JFileChooser.APPROVE_OPTION) {
                    java.io.File sf = fc.getSelectedFile();
                    String fileName = sf.getName();              
                    
                    try {
                        String currentBaseDir = System.getProperty("user.dir");
                        try {
                            java.io.File runJarFile = new java.io.File(MainFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                            if (runJarFile.isFile()) {
                                currentBaseDir = runJarFile.getParent();
                            }
                        } catch (Exception ex) {}
                        
                        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                        java.io.File gameDir = new java.io.File(currentBaseDir, "game");
                        java.io.File subDir = new java.io.File(gameDir, baseName);
                        if (!subDir.exists()) subDir.mkdirs();
                        
                        java.io.File df = new java.io.File(subDir, fileName);
                        java.nio.file.Files.copy(sf.toPath(), df.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        
                        jarUrlField.setText("game/" + baseName + "/" + fileName);
                        JOptionPane.showMessageDialog(this, "JAR 檔案上傳成功，並已產生專屬資料夾！路徑: game/" + baseName + "/" + fileName, "上傳成功", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ioex) {
                        JOptionPane.showMessageDialog(this, "檔案上傳/複製失敗: " + ioex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            
            btnDeleteJar.addActionListener(ev -> {
                String path = jarUrlField.getText().trim();
                if (path.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "目前沒有上傳的 JAR 檔案！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int confirmDel = JOptionPane.showConfirmDialog(this, "確定要刪除此 JAR 檔案及資料夾嗎？", "刪除確認", JOptionPane.YES_NO_OPTION);
                if (confirmDel == JOptionPane.YES_OPTION) {
                    deleteFileAndFolder(path);
                    jarUrlField.setText("");
                    JOptionPane.showMessageDialog(this, "JAR 檔案及資料夾已成功刪除！", "刪除成功", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            
            jarActionPanel.add(btnBrowseJar);
            jarActionPanel.add(btnDeleteJar);
            
            jarUploadPanel.add(jarUrlField, BorderLayout.CENTER);
            jarUploadPanel.add(jarActionPanel, BorderLayout.EAST);
            addPanel.add(jarUploadPanel);

            addPanel.add(new JLabel("Java 反射類別路徑 (Class Path):"));
            JTextField classPathField = new JTextField();
            addPanel.add(classPathField);

            int result = JOptionPane.showConfirmDialog(this, addPanel, "上架新遊戲", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String name = nameField.getText().trim();
                    java.math.BigDecimal price = new java.math.BigDecimal(priceField.getText().trim());
                    String desc = descField.getText().trim();
                    String genre = genreField.getText().trim();
                    String thumb = thumbField.getText().trim();
                    String banner = bannerField.getText().trim();
                    String jarUrl = jarUrlField.getText().trim();
                    String classPath = classPathField.getText().trim();

                    if (name.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "遊戲名稱不能為空！");
                        return;
                    }

                    if (SteamController.getInstance().addGame(name, price, desc, genre, thumb, banner, jarUrl.isEmpty() ? null : jarUrl, classPath.isEmpty() ? null : classPath)) {
                        JOptionPane.showMessageDialog(this, "新遊戲上架成功！");
                        refreshAdminGamesTable();
                        if (storeModel != null) {
                            refreshStoreTable(storeModel);
                        }
                        if (refreshAnalytics != null) {
                            refreshAnalytics.run();
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "新增失敗: " + ex.getMessage());
                }
            }
        });
        gameBtns.add(btnAddGame);

        JButton btnEditGame = new JButton("修改遊戲資訊");
        btnEditGame.setBackground(new Color(241, 196, 15));
        btnEditGame.setForeground(Color.BLACK);
        btnEditGame.addActionListener(e -> {
            int row = gameTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "請選擇一個遊戲！");
                return;
            }
            int id = (int) adminGamesModel.getValueAt(row, 0);
            String name = (String) adminGamesModel.getValueAt(row, 1);
            java.math.BigDecimal price = (java.math.BigDecimal) adminGamesModel.getValueAt(row, 2);
            String desc = (String) adminGamesModel.getValueAt(row, 3);
            String genre = (String) adminGamesModel.getValueAt(row, 4);
            String thumb = (String) adminGamesModel.getValueAt(row, 5);
            String banner = (String) adminGamesModel.getValueAt(row, 6);
            String jarUrl = (String) adminGamesModel.getValueAt(row, 7);
            String javaClassPath = (String) adminGamesModel.getValueAt(row, 8);

            JPanel editPanel = new JPanel(new GridLayout(8, 2, 5, 5));
            editPanel.add(new JLabel("遊戲名稱:"));
            JTextField nameField = new JTextField(name);
            editPanel.add(nameField);

            editPanel.add(new JLabel("售價:"));
            JTextField priceField = new JTextField(price.toString());
            editPanel.add(priceField);

            editPanel.add(new JLabel("遊戲描述:"));
            JTextField descField = new JTextField(desc);
            editPanel.add(descField);

            editPanel.add(new JLabel("分類/類型:"));
            JTextField genreField = new JTextField(genre);
            editPanel.add(genreField);

            editPanel.add(new JLabel("預覽圖 (Emoji 或 img/xxx.png):"));
            JPanel thumbUploadPanel = new JPanel(new BorderLayout(5, 5));
            thumbUploadPanel.setOpaque(false);
            JTextField thumbField = new JTextField(thumb);
            JButton btnBrowseThumb = new JButton("選擇 PNG...");
            btnBrowseThumb.addActionListener(ev -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG 圖片 (*.png)", "png"));
                int rv = fc.showOpenDialog(this);
                if (rv == JFileChooser.APPROVE_OPTION) {
                    java.io.File sf = fc.getSelectedFile();
                    try {
                        java.io.File imgDir = new java.io.File("img");
                        if (!imgDir.exists()) imgDir.mkdirs();
                        java.io.File df = new java.io.File(imgDir, sf.getName());
                        java.nio.file.Files.copy(sf.toPath(), df.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        thumbField.setText("img/" + sf.getName());
                    } catch (Exception ioex) {
                        thumbField.setText("img/" + sf.getName());
                    }
                }
            });
            thumbUploadPanel.add(thumbField, BorderLayout.CENTER);
            thumbUploadPanel.add(btnBrowseThumb, BorderLayout.EAST);
            editPanel.add(thumbUploadPanel);

            editPanel.add(new JLabel("橫幅色彩 (Gradient):"));
            JTextField bannerField = new JTextField(banner);
            editPanel.add(bannerField);

            editPanel.add(new JLabel("JAR 檔小遊戲 (限制英文檔名):"));
            JPanel jarUploadPanel = new JPanel(new BorderLayout(5, 5));
            jarUploadPanel.setOpaque(false);
            JTextField jarUrlField = new JTextField(jarUrl != null ? jarUrl : "");
            jarUrlField.setEditable(false);
            
            JPanel jarActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
            jarActionPanel.setOpaque(false);
            
            JButton btnBrowseJar = new JButton("上傳 JAR");
            JButton btnDeleteJar = new JButton("刪除 JAR");
            
            btnBrowseJar.addActionListener(ev -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Java Executable (*.jar)", "jar"));
                int rv = fc.showOpenDialog(this);
                if (rv == JFileChooser.APPROVE_OPTION) {
                    java.io.File sf = fc.getSelectedFile();
                    String fileName = sf.getName();
                
                    
                    try {
                        String currentBaseDir = System.getProperty("user.dir");
                        try {
                            java.io.File runJarFile = new java.io.File(MainFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                            if (runJarFile.isFile()) {
                                currentBaseDir = runJarFile.getParent();
                            }
                        } catch (Exception ex) {}
                        
                        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                        java.io.File gameDir = new java.io.File(currentBaseDir, "game");
                        java.io.File subDir = new java.io.File(gameDir, baseName);
                        if (!subDir.exists()) subDir.mkdirs();
                        
                        java.io.File df = new java.io.File(subDir, fileName);
                        java.nio.file.Files.copy(sf.toPath(), df.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        
                        jarUrlField.setText("game/" + baseName + "/" + fileName);
                        JOptionPane.showMessageDialog(this, "JAR 檔案上傳成功，並已產生專屬資料夾！路徑: game/" + baseName + "/" + fileName, "上傳成功", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ioex) {
                        JOptionPane.showMessageDialog(this, "檔案上傳/複製失敗: " + ioex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            
            btnDeleteJar.addActionListener(ev -> {
                String path = jarUrlField.getText().trim();
                if (path.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "目前沒有上傳的 JAR 檔案！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int confirmDel = JOptionPane.showConfirmDialog(this, "確定要刪除此 JAR 檔案及資料夾嗎？", "刪除確認", JOptionPane.YES_NO_OPTION);
                if (confirmDel == JOptionPane.YES_OPTION) {
                    deleteFileAndFolder(path);
                    jarUrlField.setText("");
                    JOptionPane.showMessageDialog(this, "JAR 檔案及資料夾已成功刪除！", "刪除成功", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            
            jarActionPanel.add(btnBrowseJar);
            jarActionPanel.add(btnDeleteJar);
            
            jarUploadPanel.add(jarUrlField, BorderLayout.CENTER);
            jarUploadPanel.add(jarActionPanel, BorderLayout.EAST);
            editPanel.add(jarUploadPanel);

            editPanel.add(new JLabel("Java 反射類別路徑 (Class Path):"));
            JTextField classPathField = new JTextField(javaClassPath != null ? javaClassPath : "");
            editPanel.add(classPathField);

            int result = JOptionPane.showConfirmDialog(this, editPanel, "修改遊戲資訊 [ID: " + id + "]", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String uName = nameField.getText().trim();
                    java.math.BigDecimal uPrice = new java.math.BigDecimal(priceField.getText().trim());
                    String uDesc = descField.getText().trim();
                    String uGenre = genreField.getText().trim();
                    String uThumb = thumbField.getText().trim();
                    String uBanner = bannerField.getText().trim();
                    String uJarUrl = jarUrlField.getText().trim();
                    String uClassPath = classPathField.getText().trim();

                    if (uName.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "遊戲名稱不能為空！");
                        return;
                    }

                    if (SteamController.getInstance().updateGame(id, uName, uPrice, uDesc, uGenre, uThumb, uBanner, uJarUrl.isEmpty() ? null : uJarUrl, uClassPath.isEmpty() ? null : uClassPath)) {
                        JOptionPane.showMessageDialog(this, "遊戲資訊修改成功！");
                        refreshAdminGamesTable();
                        if (storeModel != null) {
                            refreshStoreTable(storeModel);
                        }
                        if (refreshAnalytics != null) {
                            refreshAnalytics.run();
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "修改失敗: " + ex.getMessage());
                }
            }
        });
        gameBtns.add(btnEditGame);

        JButton btnDeleteGame = new JButton("下架此遊戲");
        btnDeleteGame.setBackground(new Color(192, 57, 43));
        btnDeleteGame.setForeground(Color.WHITE);
        btnDeleteGame.addActionListener(e -> {
            int row = gameTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "請選擇一個遊戲！");
                return;
            }
            int id = (int) adminGamesModel.getValueAt(row, 0);
            String name = (String) adminGamesModel.getValueAt(row, 1);
            String existingJarUrl = (String) adminGamesModel.getValueAt(row, 7);

            int confirm = JOptionPane.showConfirmDialog(this, "確定要下架並刪除遊戲「" + name + "」嗎？此動作將影響所有已購玩家！", "下架確認", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (SteamController.getInstance().deleteGame(id)) {
                        if (existingJarUrl != null && !existingJarUrl.trim().isEmpty()) {
                            deleteFileAndFolder(existingJarUrl);
                        }
                        JOptionPane.showMessageDialog(this, "遊戲已成功下架，對應之 JAR 與資料夾已一併刪除。");
                        refreshAdminGamesTable();
                        if (storeModel != null) {
                            refreshStoreTable(storeModel);
                        }
                        if (refreshAnalytics != null) {
                            refreshAnalytics.run();
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "下架失敗：" + ex.getMessage());
                }
            }
        });
        gameBtns.add(btnDeleteGame);
        gameTabPanel.add(gameBtns, BorderLayout.SOUTH);
        adminSubTabs.addTab("遊戲上下架管理 (CRUD)", gameTabPanel);

        // SUB-TAB 3: Game Data Analysis (Data Analytics & Charts)
        JPanel analyticsTabPanel = new JPanel(new BorderLayout(15, 15));
        analyticsTabPanel.setBackground(new Color(33, 44, 57));
        analyticsTabPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // KPI Cards Row (Total Sales, Order Count, AOV)
        JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        kpiPanel.setOpaque(false);
        
        JPanel kpiRev = createKpiCard("平台總銷售金額", "$0.00", new Color(46, 204, 113));
        JPanel kpiTx = createKpiCard("平台累計交易筆數", "0 筆", new Color(52, 152, 219));
        JPanel kpiAov = createKpiCard("客單均價 (Average Order Value)", "$0.00", new Color(155, 89, 182));
        
        kpiPanel.add(kpiRev);
        kpiPanel.add(kpiTx);
        kpiPanel.add(kpiAov);
        
        analyticsTabPanel.add(kpiPanel, BorderLayout.NORTH);
        
        // Charts container
        JPanel chartsContainer = new JPanel(new GridLayout(2, 1, 15, 15));
        chartsContainer.setOpaque(false);
        
        JPanel pieChartsPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        pieChartsPanel.setOpaque(false);
        
        JPanel trendPanel = new JPanel(new BorderLayout());
        trendPanel.setOpaque(false);
        
        chartsContainer.add(pieChartsPanel);
        chartsContainer.add(trendPanel);
        
        analyticsTabPanel.add(chartsContainer, BorderLayout.CENTER);
        
        refreshAnalytics = () -> {
            try {
                List<Game> gamesList = SteamController.getInstance().listStoreGames();
                List<com.steam.model.Purchase> purchasesList = SteamController.getInstance().listAllPurchasesAdmin();
                
                Map<Integer, Game> gMap = new HashMap<>();
                for (Game g : gamesList) {
                    gMap.put(g.getId(), g);
                }
                
                double totalRevenue = 0.0;
                int totalTx = purchasesList.size();
                
                Map<String, Double> genreS = new HashMap<>();
                Map<String, Double> gameS = new HashMap<>();
                Map<String, Double> dailyS = new TreeMap<>();
                
                SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd");
                
                for (com.steam.model.Purchase p : purchasesList) {
                    Game g = gMap.get(p.getGameId());
                    if (g != null) {
                        double price = g.getPrice() != null ? g.getPrice().doubleValue() : 0.0;
                        totalRevenue += price;
                        
                        String genre = g.getGenre() != null ? g.getGenre() : "未分類";
                        genreS.put(genre, genreS.getOrDefault(genre, 0.0) + price);
                        
                        String gName = g.getName();
                        gameS.put(gName, gameS.getOrDefault(gName, 0.0) + price);
                    }
                    
                    if (p.getPurchaseTime() != null) {
                        String dStr = dFormat.format(new Date(p.getPurchaseTime().getTime()));
                        Game g1 = gMap.get(p.getGameId());
                        double price = (g1 != null && g1.getPrice() != null) ? g1.getPrice().doubleValue() : 0.0;
                        dailyS.put(dStr, dailyS.getOrDefault(dStr, 0.0) + price);
                    }
                }
                
                double aov = totalTx > 0 ? totalRevenue / totalTx : 0.0;
                
                // Update KPI Cards
                updateKpiCardValue(kpiRev, String.format("$%.2f", totalRevenue));
                updateKpiCardValue(kpiTx, totalTx + " 筆");
                updateKpiCardValue(kpiAov, String.format("$%.2f", aov));
                
                // Redraw charts
                pieChartsPanel.removeAll();
                trendPanel.removeAll();
                
                // 1. Genre Pie
                DefaultPieDataset gDataset = new DefaultPieDataset();
                for (Map.Entry<String, Double> ent : genreS.entrySet()) {
                    gDataset.setValue(ent.getKey(), ent.getValue());
                }
                JFreeChart gChart = ChartFactory.createPieChart("各遊戲類別銷售分佈 ($)", gDataset, true, true, false);
                styleChart(gChart);
                pieChartsPanel.add(new ChartPanel(gChart));
                
                // 2. Hot Game Pie
                DefaultPieDataset gmDataset = new DefaultPieDataset();
                for (Map.Entry<String, Double> ent : gameS.entrySet()) {
                    gmDataset.setValue(ent.getKey(), ent.getValue());
                }
                JFreeChart gmChart = ChartFactory.createPieChart("各遊戲銷售金額排行 ($)", gmDataset, true, true, false);
                styleChart(gmChart);
                pieChartsPanel.add(new ChartPanel(gmChart));
                
                // 3. Daily Trend Line
                DefaultCategoryDataset tDataset = new DefaultCategoryDataset();
                if (dailyS.isEmpty()) {
                    tDataset.addValue(0.0, "銷售額", "無數據");
                } else {
                    for (Map.Entry<String, Double> ent : dailyS.entrySet()) {
                        tDataset.addValue(ent.getValue(), "銷售額", ent.getKey());
                    }
                }
                JFreeChart tChart = ChartFactory.createLineChart("平台每日銷售走勢圖 ($)", "日期 (Date)", "金額 (USD)", tDataset, PlotOrientation.VERTICAL, false, true, false);
                styleChart(tChart);
                trendPanel.add(new ChartPanel(tChart));
                
                pieChartsPanel.revalidate();
                pieChartsPanel.repaint();
                trendPanel.revalidate();
                trendPanel.repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };
        
        JPanel southControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southControls.setOpaque(false);
        JButton btnRefreshAnalytics = new JButton("重新整理走勢與圓餅圖");
        btnRefreshAnalytics.setBackground(new Color(230, 126, 34));
        btnRefreshAnalytics.setForeground(Color.WHITE);
        btnRefreshAnalytics.setFont(new Font("Microsoft JhengHei", Font.BOLD, 12));
        btnRefreshAnalytics.addActionListener(e -> refreshAnalytics.run());
        southControls.add(btnRefreshAnalytics);
        analyticsTabPanel.add(southControls, BorderLayout.SOUTH);
        
        adminSubTabs.addTab("遊戲數據與銷售分析 (Charts)", analyticsTabPanel);

        adminPanel.add(adminSubTabs, BorderLayout.CENTER);
        tabbedPane.addTab("管理控制 (Admin Only)", adminPanel);
    }

    private JPanel createKpiCard(String title, String initialValue, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(new Color(23, 26, 33));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 10), 1),
            new EmptyBorder(12, 15, 12, 15)
        ));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        lblTitle.setForeground(Color.LIGHT_GRAY);
        card.add(lblTitle, BorderLayout.NORTH);
        
        JLabel lblValue = new JLabel(initialValue);
        lblValue.setFont(new Font("Microsoft JhengHei", Font.BOLD, 22));
        lblValue.setForeground(valueColor);
        card.add(lblValue, BorderLayout.CENTER);
        
        return card;
    }
    
    private void updateKpiCardValue(JPanel card, String value) {
        JLabel lblValue = (JLabel) card.getComponent(1);
        lblValue.setText(value);
    }

    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(new Color(23, 26, 33));
        chart.getTitle().setPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(new Color(23, 26, 33));
            chart.getLegend().setItemPaint(Color.LIGHT_GRAY);
            chart.getLegend().setItemFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
        }
        
        if (chart.getPlot() instanceof PiePlot) {
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setBackgroundPaint(new Color(33, 44, 57));
            plot.setOutlinePaint(null);
            plot.setLabelBackgroundPaint(new Color(23, 26, 33));
            plot.setLabelPaint(Color.WHITE);
            plot.setLabelFont(new Font("Microsoft JhengHei", Font.PLAIN, 10));
            plot.setLabelShadowPaint(null);
        } else if (chart.getPlot() instanceof CategoryPlot) {
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.setBackgroundPaint(new Color(33, 44, 57));
            plot.setRangeGridlinePaint(Color.GRAY);
            plot.setDomainGridlinePaint(Color.GRAY);
            plot.getDomainAxis().setLabelPaint(Color.WHITE);
            plot.getDomainAxis().setLabelFont(new Font("Microsoft JhengHei", Font.BOLD, 10));
            plot.getDomainAxis().setTickLabelPaint(Color.LIGHT_GRAY);
            plot.getDomainAxis().setTickLabelFont(new Font("Microsoft JhengHei", Font.PLAIN, 9));
            plot.getRangeAxis().setLabelPaint(Color.WHITE);
            plot.getRangeAxis().setLabelFont(new Font("Microsoft JhengHei", Font.BOLD, 10));
            plot.getRangeAxis().setTickLabelPaint(Color.LIGHT_GRAY);
            plot.getRangeAxis().setTickLabelFont(new Font("Microsoft JhengHei", Font.PLAIN, 9));
            
            LineAndShapeRenderer renderer = new LineAndShapeRenderer();
            renderer.setSeriesPaint(0, new Color(102, 192, 244));
            renderer.setSeriesStroke(0, new BasicStroke(2.5f));
            plot.setRenderer(renderer);
        }
    }

    private void refreshAdminTable(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            List<Member> members = SteamController.getInstance().listAllMembers();
            for (Member m : members) {
                model.addRow(new Object[]{
                    m.getId(),
                    m.getUsername(),
                    m.getNickname(),
                    m.getEmail(),
                    m.getBalance(),
                    m.getRole()
                });
            }
        } catch (SteamException e) {
            e.printStackTrace();
        }
    }

    private void refreshAdminGamesTable() {
        if (adminGamesModel == null) return;
        adminGamesModel.setRowCount(0);
        try {
            List<Game> games = SteamController.getInstance().listStoreGames();
            for (Game g : games) {
                adminGamesModel.addRow(new Object[]{
                    g.getId(),
                    g.getName(),
                    g.getPrice(),
                    g.getDescription(),
                    g.getGenre(),
                    g.getThumbnail(),
                    g.getBannerColor(),
                    g.getGameUrl(),
                    g.getJavaClassPath()
                });
            }
        } catch (SteamException e) {
            e.printStackTrace();
        }
    }

    private void deleteFileAndFolder(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) return;
        String baseDir = System.getProperty("user.dir");
        try {
            java.io.File jarFile = new java.io.File(MainFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (runJarFileIsFile(jarFile)) {
                baseDir = jarFile.getParent();
            }
        } catch (Exception ex) {}

        java.io.File file = new java.io.File(baseDir, relativePath);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        java.io.File parentDir = file.getParentFile();
        if (parentDir != null && parentDir.exists() && parentDir.isDirectory() && !"game".equals(parentDir.getName())) {
            java.io.File[] files = parentDir.listFiles();
            if (files != null) {
                for (java.io.File f : files) {
                    f.delete();
                }
            }
            parentDir.delete();
        }
    }

    private boolean runJarFileIsFile(java.io.File jarFile) {
        return jarFile.isFile();
    }
}
