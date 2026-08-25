# 擬Steam 遊戲購買平台 (Mock-Steam Platform)

![Java](https://img.shields.io/badge/Language-Java-orange.svg)
![MySQL](https://img.shields.io/badge/Database-MySQL-blue.svg)
![IDE](https://img.shields.io/badge/IDE-Eclipse-purple.svg)
![AI-Assisted](https://img.shields.io/badge/Dev_Tool-AI_Studio-flash.svg)

這是一個基於 **Java SE 17**、**Swing (WindowBuilder)**、**JFreeChart** 與 **MySQL** 所開發的「Steam 整合式遊戲模擬平台」桌面應用程式。
---

## 核心功能

專案主要分為 **玩家前台** 與 **管理員後台** 兩大模組：

### 🎮 玩家前台功能
*   **遊戲目錄瀏覽**：支援分類檢索、關鍵字搜尋，即時查看遊戲詳情與價格。
*   **購物車系統**：支援合併結帳、刪除商品，並在結帳時自動計算總金額。
*   **分數排行榜**：整合玩家遊玩數據，即時更新並展示高分玩家排行。

### 📊 管理員後台功能
*   **遊戲上架管理**：提供直覺的介面供管理員新增、修改或下架遊戲資訊。
*   **銷售數據分析**：統計各遊戲的銷售量、平台總營收，並以圖表或條列數據呈現，輔助商業決策。

---

## 開發工具與技術棧

*   **程式語言**：Java (JDK 17+)
*   **開發環境**：Eclipse IDE
*   **資料庫**：MySQL (負責儲存使用者、遊戲商品、訂單及排行榜數據)
*   **輔助開發**：Google AI Studio (用於程式碼架構優化與邏輯輔助)

## 資料庫設定

在執行專案前，請先在 MySQL 中建立資料庫並匯入相關資料表：

請在 MySQL 建立名為 steam_db 的資料庫，並執行以下 SQL 命令。本結構已包含防止外鍵約束失敗 (Cannot add or update a child row: a foreign key constraint fails) 的串聯刪除 (ON DELETE CASCADE) 機制。


## 📐 系統架構設計 (MVC + DAO Pattern)

本專案採用分層架構，達到高內聚、低耦合的維護指標：

```text
com.steam
├── controller       (Controller - 業務邏輯與流程控制中心)
│   └── SteamController.java
├── dao              (DAO - 資料庫直接對接與實體 CRUD 層)
│   ├── MemberDAO.java
│   ├── GameDAO.java
│   ├── PurchaseDAO.java
│   ├── AchievementDAO.java
│   └── GameScoreDAO.java
├── model            (Model - 封裝資料結構的 JavaBean/Entity)
│   ├── Member.java
│   ├── Game.java
│   ├── Purchase.java
│   ├── Achievement.java
│   └── GameScore.java
├── util             (Utilities - 工具類，如資料庫連線、安全加密)
│   └── DBUtil.java
├── exception        (Custom Exceptions - 自訂異常處理層)
│   └── SteamException.java
└── view             (View - Swing 介面視窗與底層遊戲渲染)
    ├── LoginFrame.java
    ├── RegisterFrame.java
    ├── MainFrame.java         (核心主視窗 - 內含 WindowBuilder 頁籤群)
    └── game/                  (各款內建模擬小遊戲元件面板)
        ├── TetrisPanel.java
        ├── PacmanPanel.java
        ├── GobangPanel.java
        ├── BrickBreakerPanel.java
        └── FlappyBirdPanel.java
---

