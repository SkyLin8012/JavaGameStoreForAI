package com.steam.score.controller;

import com.steam.score.model.ScoreRecord;
import com.steam.score.repository.ScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RESTful API 控制層 - 提供遊戲對接的 HTTP 端點
 */
@RestController
@RequestMapping("/api/score")
@CrossOrigin(origins = "*") // 允許跨來源請求 (CORS) 方便前端或遊戲端調用
public class ScoreController {

    @Autowired
    private ScoreRepository scoreRepository;

    /**
     * 1. 接收遊戲上傳分數 (POST /api/score)
     */
    @PostMapping
    public ResponseEntity<?> submitScore(@RequestBody ScoreRecord record) {
        if (record.getPlayerName() == null || record.getPlayerName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("{\"message\":\"Player name cannot be empty\"}");
        }
        if (record.getScore() < 0) {
            return ResponseEntity.badRequest().body("{\"message\":\"Score cannot be negative\"}");
        }
        
        ScoreRecord saved = scoreRepository.save(record);
        System.out.println("[API Server] 收到新分數回報 -> 玩家: " + saved.getPlayerName() + ", 分數: " + saved.getScore());
        return ResponseEntity.ok(saved);
    }

    /**
     * 2. 獲取所有分數排行榜記錄 (GET /api/score)
     */
    @GetMapping
    public List<ScoreRecord> getLeaderboard() {
        return scoreRepository.findAll();
    }
}
