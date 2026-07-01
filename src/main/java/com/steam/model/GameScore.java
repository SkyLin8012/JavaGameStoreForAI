package com.steam.model;

import java.sql.Timestamp;

public class GameScore {
    private int id;
    private int memberId;
    private int gameId;
    private int score;
    private Timestamp recordedAt;

    private String memberNickname;
    private String gameName;

    public GameScore() {}

    public GameScore(int memberId, int gameId, int score) {
        this.memberId = memberId;
        this.gameId = gameId;
        this.score = score;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public Timestamp getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Timestamp recordedAt) { this.recordedAt = recordedAt; }

    public String getMemberNickname() { return memberNickname; }
    public void setMemberNickname(String memberNickname) { this.memberNickname = memberNickname; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }
}
