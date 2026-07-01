package com.steam.model;

import java.sql.Timestamp;

public class UnlockedAchievement {
    private int id;
    private int memberId;
    private String achievementId;
    private Timestamp unlockedAt;

    private String achievementTitle;
    private String achievementDescription;

    public UnlockedAchievement() {}

    public UnlockedAchievement(int memberId, String achievementId) {
        this.memberId = memberId;
        this.achievementId = achievementId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public String getAchievementId() { return achievementId; }
    public void setAchievementId(String achievementId) { this.achievementId = achievementId; }

    public Timestamp getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(Timestamp unlockedAt) { this.unlockedAt = unlockedAt; }

    public String getAchievementTitle() { return achievementTitle; }
    public void setAchievementTitle(String achievementTitle) { this.achievementTitle = achievementTitle; }

    public String getAchievementDescription() { return achievementDescription; }
    public void setAchievementDescription(String achievementDescription) { this.achievementDescription = achievementDescription; }
}
