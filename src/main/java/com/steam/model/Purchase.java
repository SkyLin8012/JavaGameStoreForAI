package com.steam.model;

import java.sql.Timestamp;

public class Purchase {
    private int id;
    private int memberId;
    private int gameId;
    private Timestamp purchaseTime;

    // View helper attributes
    private String memberNickname;
    private String gameName;
    private String gameUrl;
    private String javaClassPath;
    private String gamePrice;

    public String getGamePrice() {
		return gamePrice;
	}

	public void setGamePrice(String gamePrice) {
		this.gamePrice = gamePrice;
	}

	public Purchase() {}

    public Purchase(int memberId, int gameId) {
        this.memberId = memberId;
        this.gameId = gameId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }

    public Timestamp getPurchaseTime() { return purchaseTime; }
    public void setPurchaseTime(Timestamp purchaseTime) { this.purchaseTime = purchaseTime; }

    public String getMemberNickname() { return memberNickname; }
    public void setMemberNickname(String memberNickname) { this.memberNickname = memberNickname; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public String getGameUrl() { return gameUrl; }
    public void setGameUrl(String gameUrl) { this.gameUrl = gameUrl; }

    public String getJavaClassPath() { return javaClassPath; }
    public void setJavaClassPath(String javaClassPath) { this.javaClassPath = javaClassPath; }
}
