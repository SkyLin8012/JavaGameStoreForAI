package com.steam.service.impl;

import com.steam.dao.CartDao;
import com.steam.dao.GameDao;
import com.steam.dao.MemberDao;
import com.steam.dao.PurchaseDao;
import com.steam.dao.impl.CartDaoImpl;
import com.steam.dao.impl.GameDaoImpl;
import com.steam.dao.impl.MemberDaoImpl;
import com.steam.dao.impl.PurchaseDaoImpl;
import com.steam.model.CartItem;
import com.steam.model.Game;
import com.steam.model.Member;
import com.steam.model.Purchase;
import com.steam.exception.SteamException;
import com.steam.service.CartService;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class CartServiceImpl implements CartService {
    private CartDao cartDao = new CartDaoImpl();
    private GameDao gameDao = new GameDaoImpl();
    private MemberDao memberDao = new MemberDaoImpl();
    private PurchaseDao purchaseDao = new PurchaseDaoImpl();

    @Override
    public boolean addToCart(int memberId, int gameId) throws SteamException {
        try {
            if (purchaseDao.isOwned(memberId, gameId)) {
                throw new SteamException("您已擁有此遊戲，不需加入購物車！");
            }
            if (cartDao.isInCart(memberId, gameId)) {
                throw new SteamException("此遊戲已在購物車中！");
            }
            CartItem item = new CartItem(memberId, gameId);
            return cartDao.add(item);
        } catch (SQLException e) {
            throw new SteamException("加入購物車失敗: " + e.getMessage());
        }
    }

    @Override
    public boolean removeFromCart(int memberId, int gameId) throws SteamException {
        try {
            return cartDao.remove(memberId, gameId);
        } catch (SQLException e) {
            throw new SteamException("自購物車移除失敗: " + e.getMessage());
        }
    }

    @Override
    public boolean clearCart(int memberId) throws SteamException {
        try {
            return cartDao.clear(memberId);
        } catch (SQLException e) {
            throw new SteamException("清空購物車失敗: " + e.getMessage());
        }
    }

    @Override
    public List<CartItem> getMyCart(int memberId) throws SteamException {
        try {
            return cartDao.findByMemberId(memberId);
        } catch (SQLException e) {
            throw new SteamException("載入購物車失敗: " + e.getMessage());
        }
    }

    @Override
    public boolean checkout(int memberId) throws SteamException {
        try {
            List<CartItem> items = cartDao.findByMemberId(memberId);
            if (items.isEmpty()) {
                throw new SteamException("您的購物車是空的，無法結帳！");
            }

            // 1. Calculate total cost and verify owns
            BigDecimal totalCost = BigDecimal.ZERO;
            for (CartItem item : items) {
                if (purchaseDao.isOwned(memberId, item.getGameId())) {
                    throw new SteamException("購物車中的《" + item.getGameName() + "》已在您收藏庫中，請先移除！");
                }
                if (item.getGamePrice() != null) {
                    totalCost = totalCost.add(item.getGamePrice());
                }
            }

            // 2. Check balance
            Member m = memberDao.findById(memberId);
            if (m == null) {
                throw new SteamException("帳戶不存在！");
            }
            if (m.getBalance().compareTo(totalCost) < 0) {
                throw new SteamException("餘額不足！購物車總金額為 $" + totalCost + "，您目前餘額為 $" + m.getBalance());
            }

            // 3. Complete transactions
            for (CartItem item : items) {
                Purchase p = new Purchase(memberId, item.getGameId());
                purchaseDao.buy(p);
            }

            // Deduct balance
            m.setBalance(m.getBalance().subtract(totalCost));
            memberDao.update(m);

            // Clear Cart
            cartDao.clear(memberId);
            return true;

        } catch (SQLException e) {
            throw new SteamException("交易結帳失敗: " + e.getMessage());
        }
    }
}
