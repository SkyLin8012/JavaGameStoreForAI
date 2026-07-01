package com.steam.score.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 遠端分數記錄資料實體 (對應資料庫: game_score_api 表)
 */
@Entity
@Table(name = "game_score_api")
public class ScoreRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_name", nullable = false, length = 100)
    private String playerName;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    public ScoreRecord() {}

    public ScoreRecord(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }

    @PrePersist
    protected void onCreate() {
        this.recordedAt = LocalDateTime.now();
    }

    // --- Getter 和 Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
