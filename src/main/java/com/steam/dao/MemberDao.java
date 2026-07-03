package com.steam.dao;

import com.steam.model.Member;
import java.sql.SQLException;
import java.util.List;

public interface MemberDao {
    Member findByUsername(String username) throws SQLException;
    Member findById(int id) throws SQLException;
    boolean insert(Member member) throws SQLException;
    boolean update(Member member) throws SQLException;
    boolean delete(int id) throws SQLException;
    List<Member> findAll() throws SQLException;
}
