package com.steam.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Member {
    private int id;
    private String username;
    private String password;
    private String email;
    private String nickname;
    private BigDecimal balance;
    private String role;
    private Timestamp createdAt;

    public Member() {}

    public Member(String username, String password, String email, String nickname, BigDecimal balance, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.balance = balance;
        this.role = role;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
