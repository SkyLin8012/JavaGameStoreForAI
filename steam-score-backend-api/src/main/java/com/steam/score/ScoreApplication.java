package com.steam.score;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 遊戲分數接收端 API 伺服器啟動入口
 */
@SpringBootApplication
public class ScoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScoreApplication.class, args);
        System.out.println("==========================================================");
        System.out.println("   🎮 Steam Game Platform Score REST API 伺服器啟動成功！   ");
        System.out.println("   👉 接收分數端點: POST http://localhost:8080/api/score");
        System.out.println("   👉 排行榜查詢端點: GET  http://localhost:8080/api/score");
        System.out.println("==========================================================");
    }
}
