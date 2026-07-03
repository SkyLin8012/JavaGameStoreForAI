package com.steam.dao.impl;

import com.steam.dao.PurchaseDao;
import com.steam.model.Purchase;
import com.steam.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDaoImpl implements PurchaseDao {

    @Override
    public boolean buy(Purchase purchase) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO purchase (member_id, game_id) VALUES (?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, purchase.getMemberId());
            pstmt.setInt(2, purchase.getGameId());
            return pstmt.executeUpdate() > 0;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    public List<Purchase> findByMemberId(int memberId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Purchase> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT p.*, g.name AS game_name, g.game_url AS game_url, g.java_class_path AS java_class_path FROM purchase p JOIN game g ON p.game_id = g.id WHERE p.member_id = ? ORDER BY p.purchase_time DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Purchase p = new Purchase();
                p.setId(rs.getInt("id"));
                p.setMemberId(rs.getInt("member_id"));
                p.setGameId(rs.getInt("game_id"));
                p.setPurchaseTime(rs.getTimestamp("purchase_time"));
                p.setGameName(rs.getString("game_name"));
                p.setGameUrl(rs.getString("game_url"));
                p.setJavaClassPath(rs.getString("java_class_path"));
                list.add(p);
            }
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    @Override
    public List<Purchase> findAll() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<Purchase> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT p.*, g.name AS game_name, m.nickname AS member_nickname FROM purchase p " +
                         "JOIN game g ON p.game_id = g.id " +
                         "JOIN member m ON p.member_id = m.id " +
                         "ORDER BY p.purchase_time DESC";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Purchase p = new Purchase();
                p.setId(rs.getInt("id"));
                p.setMemberId(rs.getInt("member_id"));
                p.setGameId(rs.getInt("game_id"));
                p.setPurchaseTime(rs.getTimestamp("purchase_time"));
                p.setGameName(rs.getString("game_name"));
                p.setMemberNickname(rs.getString("member_nickname"));
                list.add(p);
            }
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return list;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "DELETE FROM purchase WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    public boolean isOwned(int memberId, int gameId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT 1 FROM purchase WHERE member_id = ? AND game_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, gameId);
            rs = pstmt.executeQuery();
            return rs.next();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }

    @Override
    public boolean update(Purchase purchase) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE purchase SET member_id = ?, game_id = ? WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, purchase.getMemberId());
            pstmt.setInt(2, purchase.getGameId());
            pstmt.setInt(3, purchase.getId());
            return pstmt.executeUpdate() > 0;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    public Purchase findById(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT p.*, g.name AS game_name, m.nickname AS member_nickname FROM purchase p " +
                         "JOIN game g ON p.game_id = g.id " +
                         "JOIN member m ON p.member_id = m.id " +
                         "WHERE p.id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Purchase p = new Purchase();
                p.setId(rs.getInt("id"));
                p.setMemberId(rs.getInt("member_id"));
                p.setGameId(rs.getInt("game_id"));
                p.setPurchaseTime(rs.getTimestamp("purchase_time"));
                p.setGameName(rs.getString("game_name"));
                p.setMemberNickname(rs.getString("member_nickname"));
                return p;
            }
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }
}
