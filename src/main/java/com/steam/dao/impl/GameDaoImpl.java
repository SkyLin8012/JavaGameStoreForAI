package com.steam.dao.impl;

import com.steam.dao.GameDao;
import com.steam.model.Game;
import com.steam.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDaoImpl implements GameDao {

    @Override
    public Game findById(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM game WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToGame(rs);
            }
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public List<Game> findAll() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<Game> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT * FROM game ORDER BY price ASC";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(mapResultSetToGame(rs));
            }
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return list;
    }

    @Override
    public boolean insert(Game game) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO game (name, price, description, genre, thumbnail, banner_color, game_url, java_class_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, game.getName());
            pstmt.setBigDecimal(2, game.getPrice());
            pstmt.setString(3, game.getDescription());
            pstmt.setString(4, game.getGenre());
            pstmt.setString(5, game.getThumbnail() != null ? game.getThumbnail() : "🎮");
            pstmt.setString(6, game.getBannerColor() != null ? game.getBannerColor() : "from-slate-700 to-slate-900");
            pstmt.setString(7, game.getGameUrl());
            pstmt.setString(8, game.getJavaClassPath());
            return pstmt.executeUpdate() > 0;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    public boolean update(Game game) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE game SET name = ?, price = ?, description = ?, genre = ?, thumbnail = ?, banner_color = ?, game_url = ?, java_class_path = ? WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, game.getName());
            pstmt.setBigDecimal(2, game.getPrice());
            pstmt.setString(3, game.getDescription());
            pstmt.setString(4, game.getGenre());
            pstmt.setString(5, game.getThumbnail());
            pstmt.setString(6, game.getBannerColor());
            pstmt.setString(7, game.getGameUrl());
            pstmt.setString(8, game.getJavaClassPath());
            pstmt.setInt(9, game.getId());
            return pstmt.executeUpdate() > 0;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "DELETE FROM game WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    private Game mapResultSetToGame(ResultSet rs) throws SQLException {
        Game g = new Game();
        g.setId(rs.getInt("id"));
        g.setName(rs.getString("name"));
        g.setPrice(rs.getBigDecimal("price"));
        g.setDescription(rs.getString("description"));
        g.setGenre(rs.getString("genre"));
        g.setThumbnail(rs.getString("thumbnail"));
        g.setBannerColor(rs.getString("banner_color"));
        g.setGameUrl(rs.getString("game_url"));
        g.setJavaClassPath(rs.getString("java_class_path"));
        return g;
    }
}
