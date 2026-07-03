package com.steam.dao;

import com.steam.model.Game;
import java.sql.SQLException;
import java.util.List;

public interface GameDao {
    Game findById(int id) throws SQLException;
    List<Game> findAll() throws SQLException;
    boolean insert(Game game) throws SQLException;
    boolean update(Game game) throws SQLException;
    boolean delete(int id) throws SQLException;
}
