# 🎮 Java 遊戲對接 API 與資料庫實作手冊

本手冊將引導您如何在本地部署 **Spring Boot 後端服務**，並透過 Java 原生 HTTP Client 與遊戲進行對接，實現「自動上傳分數、持久化寫入 MySQL」的完整功能。

---

## 🏗️ 系統架構圖
```
+-----------------------+              HTTP POST              +----------------------------+
|   Java Swing 遊戲端    |  ================================>  |   Spring Boot REST API     |
| (GameNetworkManager)  |    Payload: {"playerName",score}    |     (ScoreController)      |
+-----------------------+                                     +----------------------------+
                                                                            ||
                                                                            || (Spring Data JPA / Hibernate)
                                                                            \/
                                                              +----------------------------+
                                                              |      MySQL 資料庫 (steam_db) |
                                                              |     資料表: game_score_api  |
                                                              +----------------------------+
```

---

## 📂 專案包目錄說明
下載解壓後的 ZIP 壓縮檔內含兩個主要區塊：
1. **Java Swing 遊戲客戶端 (根目錄)**：
   - 包含您原有的 5 大內建遊戲與 MVC + DAO 模組。
   - 新增：`com.steam.util.GameNetworkManager.java` 分數發送客戶端。
   - 整合：`SteamController.java` 中記錄分數時，會**自動觸發異步 HTTP 請求**將暱稱與分數上報。
2. **Spring Boot 後端服務 (`steam-score-backend-api/` 資料夾)**：
   - 採用最熱門的 Spring Boot 3 + Spring Data JPA 技術棧。
   - 負責監聽 `8080` 通訊埠，接收並記錄所有遊戲上傳之分數。

---

## 🚀 步驟一：啟動 Spring Boot 後端 API 伺服器

### 1. 匯入專案
1. 開啟您的開發工具（建議使用 **IntelliJ IDEA** 或 **Eclipse IDE**）。
2. 匯入專案：選擇 `Import -> Existing Maven Projects`，瀏覽並選擇 `steam-score-backend-api` 資料夾。

### 2. 配置資料庫
打開 `src/main/resources/application.properties`，修改您的 MySQL 帳號密碼：
```properties
# MySQL 連線設定
spring.datasource.url=jdbc:mysql://localhost:3306/steam_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=您的MySQL帳號 (例如: root)
spring.datasource.password=您的MySQL密碼 (例如: 123456)
```
> 💡 **註**：`createDatabaseIfNotExist=true` 會在連線時，自動在您的 MySQL 中建立名為 `steam_db` 的資料庫，免去手動建立的繁瑣。

### 3. 運行 Spring Boot
在 IDE 中展開 `com.steam.score`，右鍵點擊 `ScoreApplication.java`，選擇 **Run As -> Java Application**。
見到控制台輸出以下字樣，代表伺服器啟動成功：
```
==========================================================
   🎮 Steam Game Platform Score REST API 伺服器啟動成功！
   👉 接收分數端點: POST http://localhost:8080/api/score
   👉 排行榜查詢端點: GET  http://localhost:8080/api/score
==========================================================
```

---

## 🔗 步驟二：遊戲端是如何進行對接的？

在 Swing 客戶端中，我們已經為您設計好了對接核心。以下是關鍵的對接步驟與機制說明：

### 1. 核心發送類別：`GameNetworkManager.java`
這是一個輕量、無須引入第三方 Jar 的發送模組。它使用 Java 11 的 `java.net.http.HttpClient` 作為通訊媒介：

```java
package com.steam.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class GameNetworkManager {
    private static final String API_URL = "http://localhost:8080/api/score";

    public static CompletableFuture<String> submitScore(String playerName, int score) {
        try {
            String jsonPayload = String.format("{\"playerName\":\"%s\",\"score\":%d}", playerName, score);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> response.statusCode() == 200 ? "成功" : "失敗: " + response.statusCode());
        } catch (Exception e) {
            CompletableFuture<String> failed = new CompletableFuture<>();
            failed.complete("ERROR: " + e.getMessage());
            return failed;
        }
    }
}
```

### 2. 中央控制層自動攔截發送
在您原有的 `com.steam.controller.SteamController.java` 的 `recordScore(int gameId, int score)` 中，我們新增了以下攔截行：
```java
// 異步發送遊戲分數至 Spring Boot REST API
try {
    String nickname = currentLoggedInMember.getNickname();
    com.steam.util.GameNetworkManager.submitScore(nickname, score);
} catch (Exception apiEx) {
    System.err.println("自動發送分數至 Spring Boot API 失敗: " + apiEx.getMessage());
}
```
這代表**不論是誰在玩 Tetris、Pacman、Flappy Bird、五子棋或打磚塊**，一旦觸發「遊戲結束」或「刷新紀錄」，分數不僅會安全寫入您原有的 JDBC 客戶端資料庫，更會同時透過網路 HTTP POST 發送給您的 **Spring Boot 連線後端 API**！

---

## 🛠️ 步驟三：自主上架之外部 JAR 小遊戲如何對接？

若您開發了獨立的 Swing Java 小遊戲並打包成 JAR 檔，上架到本平台上：
1. **API端點**：直接在您的 JAR 專案中寫入分數上報端點：`POST http://localhost:8080/api/score`
2. **通訊程式碼**：直接複製上述 `GameNetworkManager` 類別至您自主開發的小遊戲專案中。
3. **調用時機**：在您遊戲邏輯的「遊戲結束 (Game Over)」或「過關 (Victory)」監聽器內，調用此方法：
   ```java
   // 範例：提交玩家 "王小明" 的分數 2500 分
   GameNetworkManager.submitScore("王小明", 2500);
   ```

這將帶給您的專案一個超酷、極具現代全端開發氣質的「分數即時回報系統」！🎉
