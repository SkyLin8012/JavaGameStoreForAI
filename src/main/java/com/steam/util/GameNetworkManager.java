package com.steam.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * 遊戲分數遠端 REST API 對接管理工具 (使用 Java 11 原生 HttpClient)
 */
public class GameNetworkManager {
    // 預設 Spring Boot 伺服器的分數上傳 API 端點
    private static final String API_URL = "http://localhost:8080/api/score";

    /**
     * 異步發送遊戲分數至 Spring Boot REST 伺服器
     * @param playerName 玩家暱稱 / 帳號
     * @param score 獲得的分數
     */
    public static CompletableFuture<String> submitScore(String playerName, int score) {
        try {
            // 跳脫 JSON 雙引號避免語法錯誤
            String escapedName = playerName.replace("\"", "\\\"");
            String jsonPayload = String.format("{\"playerName\":\"%s\",\"score\":%d}", escapedName, score);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            System.out.println("[API Client] 正在發送分數至: " + API_URL + ", 內容: " + jsonPayload);
            
            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        int code = response.statusCode();
                        System.out.println("[API Client] 伺服器回應狀態碼: " + code);
                        if (code == 200 || code == 201) {
                            return "SUCCESS: " + response.body();
                        } else {
                            return "FAILED: Status Code " + code + " - " + response.body();
                        }
                    });
        } catch (Exception e) {
            System.err.println("[API Client] 網路連線或傳送失敗: " + e.getMessage());
            CompletableFuture<String> failed = new CompletableFuture<>();
            failed.complete("ERROR: " + e.getMessage());
            return failed;
        }
    }
}
