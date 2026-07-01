package com.steam.dao;

import com.steam.model.CartItem;
import java.sql.SQLException;
import java.util.List;

public interface CartDao {
    boolean add(CartItem item) throws SQLException;
    boolean remove(int memberId, int gameId) throws SQLException;
    boolean clear(int memberId) throws SQLException;
    List<CartItem> findByMemberId(int memberId) throws SQLException;
    boolean isInCart(int memberId, int gameId) throws SQLException;
}
