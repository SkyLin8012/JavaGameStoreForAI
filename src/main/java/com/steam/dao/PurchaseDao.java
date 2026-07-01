package com.steam.dao;

import com.steam.model.Purchase;
import java.sql.SQLException;
import java.util.List;

public interface PurchaseDao {
    boolean buy(Purchase purchase) throws SQLException;
    List<Purchase> findByMemberId(int memberId) throws SQLException;
    List<Purchase> findAll() throws SQLException;
    boolean delete(int id) throws SQLException;
    boolean isOwned(int memberId, int gameId) throws SQLException;
}
