package com.steam.service;

import com.steam.exception.SteamException;
import com.steam.model.Game;
import com.steam.model.Purchase;
import java.util.List;

public interface PurchaseService {
    boolean purchaseGame(int memberId, Game game) throws SteamException;
    List<Purchase> getOwnedPurchases(int memberId) throws SteamException;
    boolean isOwned(int memberId, int gameId) throws SteamException;
    List<Purchase> getAllPurchases() throws SteamException;
    boolean refundPurchase(int purchaseId) throws SteamException;
    boolean addPurchaseAdmin(int memberId, int gameId) throws SteamException;
    boolean updatePurchaseAdmin(int id, int memberId, int gameId) throws SteamException;
    Purchase getPurchaseById(int id) throws SteamException;
}
