package com.steam.dao.impl;

import com.steam.dao.MemberDao;
import com.steam.model.Member;
import com.steam.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDaoImpl implements MemberDao {

    @Override
    public Member findByUsername(String username) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM member WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToMember(rs);
            }
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public Member findById(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM member WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToMember(rs);
            }
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public boolean insert(Member member) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO member (username, password, email, nickname, balance, role) VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, member.getUsername());
            pstmt.setString(2, member.getPassword());
            pstmt.setString(3, member.getEmail());
            pstmt.setString(4, member.getNickname());
            pstmt.setBigDecimal(5, member.getBalance());
            pstmt.setString(6, member.getRole());
            return pstmt.executeUpdate() > 0;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    public boolean update(Member member) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE member SET password = ?, email = ?, nickname = ?, balance = ?, role = ? WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, member.getPassword());
            pstmt.setString(2, member.getEmail());
            pstmt.setString(3, member.getNickname());
            pstmt.setBigDecimal(4, member.getBalance());
            pstmt.setString(5, member.getRole());
            pstmt.setInt(6, member.getId());
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
            String sql = "DELETE FROM member WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } finally {
            DBUtil.close(conn, pstmt, null);
        }
    }

    @Override
    public List<Member> findAll() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<Member> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            stmt = conn.createStatement();
            String sql = "SELECT * FROM member ORDER BY id ASC";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(mapResultSetToMember(rs));
            }
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return list;
    }

    private Member mapResultSetToMember(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setId(rs.getInt("id"));
        m.setUsername(rs.getString("username"));
        m.setPassword(rs.getString("password"));
        m.setEmail(rs.getString("email"));
        m.setNickname(rs.getString("nickname"));
        m.setBalance(rs.getBigDecimal("balance"));
        m.setRole(rs.getString("role"));
        m.setCreatedAt(rs.getTimestamp("created_at"));
        return m;
    }
}
