package com.steam.service.impl;

import com.steam.dao.MemberDao;
import com.steam.dao.PurchaseDao;
import com.steam.dao.impl.MemberDaoImpl;
import com.steam.dao.impl.PurchaseDaoImpl;
import com.steam.exception.SteamException;
import com.steam.model.Game;
import com.steam.model.Member;
import com.steam.model.Purchase;
import com.steam.service.PurchaseService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class PurchaseServiceImpl implements PurchaseService {
    private PurchaseDao purchaseDao = new PurchaseDaoImpl();
    private MemberDao memberDao = new MemberDaoImpl();

    @Override
    public boolean purchaseGame(int memberId, Game game) throws SteamException {
        try {
            // 1. Check if already purchased
            if (purchaseDao.isOwned(memberId, game.getId())) {
                throw new SteamException("您已擁有此遊戲，不需重複購買！");
            }

            // 2. Check balance
            Member m = memberDao.findById(memberId);
            if (m == null) {
                throw new SteamException("會員不存在！");
            }

            if (m.getBalance().compareTo(game.getPrice()) < 0) {
                throw new SteamException("餘額不足！遊戲售價：" + game.getPrice() + "，您的餘額：" + m.getBalance());
            }

            // Deduct balance and insert purchase (Simulating Transaction)
            m.setBalance(m.getBalance().subtract(game.getPrice()));
            memberDao.update(m);

            Purchase purchase = new Purchase(memberId, game.getId());
            return purchaseDao.buy(purchase);

        } catch (SQLException e) {
            throw new SteamException("購買遊戲資料庫操作異常", e);
        }
    }

    @Override
    public List<Purchase> getOwnedPurchases(int memberId) throws SteamException {
        try {
            return purchaseDao.findByMemberId(memberId);
        } catch (SQLException e) {
            throw new SteamException("取得已購遊戲失敗", e);
        }
    }

    @Override
    public boolean isOwned(int memberId, int gameId) throws SteamException {
        try {
            return purchaseDao.isOwned(memberId, gameId);
        } catch (SQLException e) {
            throw new SteamException("查詢擁有狀態失敗", e);
        }
    }

    @Override
    public List<Purchase> getAllPurchases() throws SteamException {
        try {
            return purchaseDao.findAll();
        } catch (SQLException e) {
            throw new SteamException("查詢所有交易失敗", e);
        }
    }

    @Override
    public boolean refundPurchase(int purchaseId) throws SteamException {
        try {
            return purchaseDao.delete(purchaseId);
        } catch (SQLException e) {
            throw new SteamException("退款/移除收藏庫遊戲失敗", e);
        }
    }

    @Override
    public boolean addPurchaseAdmin(int memberId, int gameId) throws SteamException {
        try {
            if (purchaseDao.isOwned(memberId, gameId)) {
                throw new SteamException("該會員已擁有此遊戲！");
            }
            Purchase p = new Purchase(memberId, gameId);
            return purchaseDao.buy(p);
        } catch (SQLException e) {
            throw new SteamException("管理員新增訂單失敗: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updatePurchaseAdmin(int id, int memberId, int gameId) throws SteamException {
        try {
            Purchase p = purchaseDao.findById(id);
            if (p == null) {
                throw new SteamException("訂單不存在！");
            }
            p.setMemberId(memberId);
            p.setGameId(gameId);
            return purchaseDao.update(p);
        } catch (SQLException e) {
            throw new SteamException("管理員更新訂單失敗: " + e.getMessage(), e);
        }
    }

    @Override
    public Purchase getPurchaseById(int id) throws SteamException {
        try {
            return purchaseDao.findById(id);
        } catch (SQLException e) {
            throw new SteamException("取得訂單失敗: " + e.getMessage(), e);
        }
    }
}
