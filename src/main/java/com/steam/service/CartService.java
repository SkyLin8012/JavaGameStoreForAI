package com.steam.service;

import com.steam.model.CartItem;
import com.steam.exception.SteamException;
import java.util.List;

public interface CartService {
    boolean addToCart(int memberId, int gameId) throws SteamException;
    boolean removeFromCart(int memberId, int gameId) throws SteamException;
    boolean clearCart(int memberId) throws SteamException;
    List<CartItem> getMyCart(int memberId) throws SteamException;
    boolean checkout(int memberId) throws SteamException;
}
