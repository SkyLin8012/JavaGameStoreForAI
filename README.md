# 擬Steam 遊戲購買平台 (Mock-Steam Platform)

![Java](https://img.shields.io/badge/Language-Java-orange.svg)
![MySQL](https://img.shields.io/badge/Database-MySQL-blue.svg)
![IDE](https://img.shields.io/badge/IDE-Eclipse-purple.svg)
![AI-Assisted](https://img.shields.io/badge/Dev_Tool-AI_Studio-flash.svg)

一個使用 Java 語言開發的虛擬 Steam 遊戲購買平台。本專案模擬了從前端使用者瀏覽、購買遊戲，到後端管理員進行上架與銷售數據分析的完整電商流程。

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

---

## 資料庫設定

在執行專案前，請先在 MySQL 中建立資料庫並匯入相關資料表：

1. 建立資料庫：
   ```sql
   CREATE DATABASE mock_steam DEFAULT CHARACTER SET utf8mb4;
