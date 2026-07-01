package com.steam.model;
import java.math.BigDecimal;

public class Game {
    private int id;
    private String name;
    private BigDecimal price;
    private String description;
    private String genre;
    private String thumbnail;
    private String bannerColor;
    private String gameUrl;
    private String javaClassPath;

    public Game() {}

    public Game(int id, String name, BigDecimal price, String description, String genre) {
        this(id, name, price, description, genre, "🎮", "from-slate-700 to-slate-900", null, null);
    }

    public Game(int id, String name, BigDecimal price, String description, String genre, String thumbnail, String bannerColor) {
        this(id, name, price, description, genre, thumbnail, bannerColor, null, null);
    }

    public Game(int id, String name, BigDecimal price, String description, String genre, String thumbnail, String bannerColor, String gameUrl) {
        this(id, name, price, description, genre, thumbnail, bannerColor, gameUrl, null);
    }

    public Game(int id, String name, BigDecimal price, String description, String genre, String thumbnail, String bannerColor, String gameUrl, String javaClassPath) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.genre = genre;
        this.thumbnail = thumbnail;
        this.bannerColor = bannerColor;
        this.gameUrl = gameUrl;
        this.javaClassPath = javaClassPath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public String getBannerColor() { return bannerColor; }
    public void setBannerColor(String bannerColor) { this.bannerColor = bannerColor; }

    public String getGameUrl() { return gameUrl; }
    public void setGameUrl(String gameUrl) { this.gameUrl = gameUrl; }

    public String getJavaClassPath() { return javaClassPath; }
    public void setJavaClassPath(String javaClassPath) { this.javaClassPath = javaClassPath; }
}
