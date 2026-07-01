package com.steam.model;

import java.math.BigDecimal;

public class CartItem {
    private int id;
    private int memberId;
    private int gameId;

    // View helper attributes
    private String gameName;
    private BigDecimal gamePrice;
    private String gameGenre;
    private String gameThumbnail;

    public CartItem() {}

    public CartItem(int memberId, int gameId) {
        this.memberId = memberId;
        this.gameId = gameId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public BigDecimal getGamePrice() { return gamePrice; }
    public void setGamePrice(BigDecimal gamePrice) { this.gamePrice = gamePrice; }

    public String getGameGenre() { return gameGenre; }
    public void setGameGenre(String gameGenre) { this.gameGenre = gameGenre; }

    public String getGameThumbnail() { return gameThumbnail; }
    public void setGameThumbnail(String gameThumbnail) { this.gameThumbnail = gameThumbnail; }
}
