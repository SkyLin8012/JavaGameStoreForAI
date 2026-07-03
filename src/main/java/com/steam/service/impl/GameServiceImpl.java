package com.steam.service.impl;

import com.steam.dao.GameDao;
import com.steam.dao.impl.GameDaoImpl;
import com.steam.exception.SteamException;
import com.steam.model.Game;
import com.steam.service.GameService;

import java.sql.SQLException;
import java.util.List;

public class GameServiceImpl implements GameService {
    private GameDao gameDao = new GameDaoImpl();

    @Override
    public List<Game> getAllGames() throws SteamException {
        try {
            return gameDao.findAll();
        } catch (SQLException e) {
            throw new SteamException("取得遊戲列表失敗", e);
        }
    }

    @Override
    public Game getGameById(int id) throws SteamException {
        try {
            return gameDao.findById(id);
        } catch (SQLException e) {
            throw new SteamException("查詢遊戲失敗", e);
        }
    }

    @Override
    public boolean addGame(Game game) throws SteamException {
        try {
            return gameDao.insert(game);
        } catch (SQLException e) {
            throw new SteamException("新增遊戲失敗", e);
        }
    }

    @Override
    public boolean updateGame(Game game) throws SteamException {
        try {
            return gameDao.update(game);
        } catch (SQLException e) {
            throw new SteamException("更新遊戲失敗", e);
        }
    }

    @Override
    public boolean removeGame(int id) throws SteamException {
        try {
            return gameDao.delete(id);
        } catch (SQLException e) {
            throw new SteamException("刪除遊戲失敗", e);
        }
    }
}
