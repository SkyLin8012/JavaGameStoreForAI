package com.steam.service;

import com.steam.exception.SteamException;
import com.steam.model.Game;
import java.util.List;

public interface GameService {
    List<Game> getAllGames() throws SteamException;
    Game getGameById(int id) throws SteamException;
    boolean addGame(Game game) throws SteamException;
    boolean updateGame(Game game) throws SteamException;
    boolean removeGame(int id) throws SteamException;
}
