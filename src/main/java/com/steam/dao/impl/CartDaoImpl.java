package com.steam.dao.impl;

import com.steam.dao.CartDao;
import com.steam.model.CartItem;
import com.steam.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDaoImpl implements CartDao {
    @Override
    public boolean add(CartItem item) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO cart_item (member_id, game_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE id=id";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, item.getMemberId());
            pstmt.setInt(2, item.getGameId());
            return pstmt.executeUpdate() > 0;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    public boolean remove(int memberId, int gameId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "DELETE FROM cart_item WHERE member_id = ? AND game_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, gameId);
            return pstmt.executeUpdate() > 0;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    public boolean clear(int memberId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "DELETE FROM cart_item WHERE member_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            return pstmt.executeUpdate() > 0;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    public List<CartItem> findByMemberId(int memberId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<CartItem> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT c.*, g.name AS game_name, g.price AS game_price, g.genre AS game_genre, g.thumbnail AS game_thumbnail FROM cart_item c " +
                         "JOIN game g ON c.game_id = g.id " +
                         "WHERE c.member_id = ? ORDER BY c.created_at DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                CartItem item = new CartItem();
                item.setId(rs.getInt("id"));
                item.setMemberId(rs.getInt("member_id"));
                item.setGameId(rs.getInt("game_id"));
                item.setGameName(rs.getString("game_name"));
                item.setGamePrice(rs.getBigDecimal("game_price"));
                item.setGameGenre(rs.getString("game_genre"));
                item.setGameThumbnail(rs.getString("game_thumbnail"));
                list.add(item);
            }
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    @Override
    public boolean isInCart(int memberId, int gameId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT 1 FROM cart_item WHERE member_id = ? AND game_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, gameId);
            rs = pstmt.executeQuery();
            return rs.next();
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
    }
}
