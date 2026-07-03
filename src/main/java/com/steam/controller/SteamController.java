package com.steam.controller;

import com.steam.exception.SteamException;
import com.steam.model.Game;
import com.steam.model.Member;
import com.steam.model.Purchase;
import com.steam.model.CartItem;
import com.steam.service.GameService;
import com.steam.service.MemberService;
import com.steam.service.PurchaseService;
import com.steam.service.CartService;
import com.steam.service.impl.GameServiceImpl;
import com.steam.service.impl.MemberServiceImpl;
import com.steam.service.impl.PurchaseServiceImpl;
import com.steam.service.impl.CartServiceImpl;

import java.math.BigDecimal;
import java.util.List;

public class SteamController {
	//1.唯一的靜態實例(全域共享)
    private static SteamController instance = new SteamController();
    
    private MemberService memberService = new MemberServiceImpl();
    private GameService gameService = new GameServiceImpl();
    private PurchaseService purchaseService = new PurchaseServiceImpl();
    private CartService cartService = new CartServiceImpl();
    //2.核心記憶體暫存，存放當前成功登入的Member 物件(類似網頁的session變數)
    private Member currentLoggedInMember;

    private SteamController() {}

    //私有構造函數，防止外部new 建立多個控制器
    public static SteamController getInstance() {
        return instance;
    }
    //取得當前登入的使用者資訊
    public Member getCurrentLoggedInMember() {
        return currentLoggedInMember;
    }
    //
    public void logout() {
        this.currentLoggedInMember = null;
    }
    
    // Member Controls
    public Member login(String username, String password) throws SteamException {
    	//取得登入者的使用者資訊
    	currentLoggedInMember = memberService.login(username, password);
        return currentLoggedInMember;
    }

    public boolean register(String username, String password, String email, String nickname) throws SteamException {
        Member m = new Member(username, password, email, nickname, new BigDecimal("1000.00"), "USER");
        return memberService.register(m);
    }

    public boolean updateProfile(String password, String email, String nickname) throws SteamException {
        if (currentLoggedInMember == null) throw new SteamException("請先登入！");
        currentLoggedInMember.setPassword(password);
        currentLoggedInMember.setEmail(email);
        currentLoggedInMember.setNickname(nickname);
        return memberService.updateProfile(currentLoggedInMember);
    }

    public boolean updateMemberByAdmin(Member member) throws SteamException {
        return memberService.updateProfile(member);
    }

    public boolean deleteMember(int id) throws SteamException {
        boolean success = memberService.deleteMember(id);
        if (currentLoggedInMember != null && currentLoggedInMember.getId() == id) {
            logout();
        }
        return success;
    }

    public List<Member> listAllMembers() throws SteamException {
        return memberService.getAllMembers();
    }

    // Deposit to wallet
    public void deposit(BigDecimal amount) throws SteamException {
        if (currentLoggedInMember == null) throw new SteamException("請先登入！");
        currentLoggedInMember.setBalance(currentLoggedInMember.getBalance().add(amount));
        memberService.updateProfile(currentLoggedInMember);
    }

    // Game Controls
    public List<Game> listStoreGames() throws SteamException {
        return gameService.getAllGames();
    }

    public boolean addGame(String name, BigDecimal price, String description, String genre) throws SteamException {
        return addGame(name, price, description, genre, "🎮", "from-slate-700 to-slate-900", null, null);
    }

    public boolean addGame(String name, BigDecimal price, String description, String genre, String thumbnail, String bannerColor, String gameUrl) throws SteamException {
        return addGame(name, price, description, genre, thumbnail, bannerColor, gameUrl, null);
    }

    public boolean addGame(String name, BigDecimal price, String description, String genre, String thumbnail, String bannerColor, String gameUrl, String javaClassPath) throws SteamException {
        Game g = new Game(0, name, price, description, genre, thumbnail, bannerColor, gameUrl, javaClassPath);
        return gameService.addGame(g);
    }

    public boolean updateGame(int id, String name, BigDecimal price, String description, String genre) throws SteamException {
        return updateGame(id, name, price, description, genre, "🎮", "from-slate-700 to-slate-900", null, null);
    }

    public boolean updateGame(int id, String name, BigDecimal price, String description, String genre, String thumbnail, String bannerColor, String gameUrl) throws SteamException {
        return updateGame(id, name, price, description, genre, thumbnail, bannerColor, gameUrl, null);
    }

    public boolean updateGame(int id, String name, BigDecimal price, String description, String genre, String thumbnail, String bannerColor, String gameUrl, String javaClassPath) throws SteamException {
        Game g = new Game(id, name, price, description, genre, thumbnail, bannerColor, gameUrl, javaClassPath);
        return gameService.updateGame(g);
    }

    public boolean deleteGame(int id) throws SteamException {
        return gameService.removeGame(id);
    }

    // Purchase Controls
    public boolean buyGame(Game game) throws SteamException {
        if (currentLoggedInMember == null) throw new SteamException("請先登入！");
        boolean success = purchaseService.purchaseGame(currentLoggedInMember.getId(), game);
        if (success) {
            // Refresh local session model balance
            currentLoggedInMember = memberService.getMemberById(currentLoggedInMember.getId());
        }
        return success;
    }

    public List<Purchase> getMyPurchases() throws SteamException {
        if (currentLoggedInMember == null) throw new SteamException("請先登入！");
        return purchaseService.getOwnedPurchases(currentLoggedInMember.getId());
    }

    public boolean checkIfOwned(int gameId) throws SteamException {
        if (currentLoggedInMember == null) return false;
        return purchaseService.isOwned(currentLoggedInMember.getId(), gameId);
    }

    public boolean refundGame(int gameId) throws SteamException {
        if (currentLoggedInMember == null) throw new SteamException("請先登入！");
        int purchaseId = -1;
        try {
            List<Purchase> purchases = purchaseService.getOwnedPurchases(currentLoggedInMember.getId());
            for (Purchase p : purchases) {
                if (p.getGameId() == gameId) {
                    purchaseId = p.getId();
                    break;
                }
            }
        } catch (Exception e) {
            throw new SteamException("無法查詢收藏庫記錄", e);
        }

        if (purchaseId == -1) {
            throw new SteamException("您尚未擁有此遊戲！");
        }

        boolean success = purchaseService.refundPurchase(purchaseId);
        if (success) {
            try {
                Game game = gameService.getGameById(gameId);
                if (game != null && game.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                    deposit(game.getPrice());
                }
            } catch (Exception ex) {
                // ignore refund error or balance sync
            }
            // Refresh local session model balance
            currentLoggedInMember = memberService.getMemberById(currentLoggedInMember.getId());
        }
        return success;
    }

    public List<Purchase> listAllPurchasesAdmin() throws SteamException {
        return purchaseService.getAllPurchases();
    }

    public boolean addPurchaseAdmin(int memberId, int gameId) throws SteamException {
        return purchaseService.addPurchaseAdmin(memberId, gameId);
    }

    public boolean updatePurchaseAdmin(int id, int memberId, int gameId) throws SteamException {
        return purchaseService.updatePurchaseAdmin(id, memberId, gameId);
    }

    public boolean deletePurchaseAdmin(int id) throws SteamException {
        return purchaseService.refundPurchase(id);
    }

    // Cart Controls
    public boolean addToCart(int gameId) throws SteamException {
        if (currentLoggedInMember == null) throw new SteamException("請先登入！");
        return cartService.addToCart(currentLoggedInMember.getId(), gameId);
    }

    public boolean removeFromCart(int gameId) throws SteamException {
        if (currentLoggedInMember == null) throw new SteamException("請先登入！");
        return cartService.removeFromCart(currentLoggedInMember.getId(), gameId);
    }

    public boolean clearCart() throws SteamException {
        if (currentLoggedInMember == null) throw new SteamException("請先登入！");
        return cartService.clearCart(currentLoggedInMember.getId());
    }

    public List<CartItem> getMyCart() throws SteamException {
        if (currentLoggedInMember == null) throw new SteamException("請先登入！");
        return cartService.getMyCart(currentLoggedInMember.getId());
    }

    public boolean checkoutCart() throws SteamException {
        if (currentLoggedInMember == null) throw new SteamException("請先登入！");
        boolean success = cartService.checkout(currentLoggedInMember.getId());
        if (success) {
            currentLoggedInMember = memberService.getMemberById(currentLoggedInMember.getId());
        }
        return success;
    }

    // Score & Achievement Controls
    public boolean recordScore(int gameId, int score) {
        if (currentLoggedInMember == null) return false;
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        try {
            conn = com.steam.util.DBUtil.getConnection();
            String sql = "INSERT INTO game_score (member_id, game_id, score) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentLoggedInMember.getId());
            pstmt.setInt(2, gameId);
            pstmt.setInt(3, score);
            boolean ok = pstmt.executeUpdate() > 0;
            if (ok) {
                checkAndUnlockAchievements();
            }
            // 異步發送遊戲分數至 Spring Boot REST API (對接外部資料庫與API服務)
            try {
                String nickname = currentLoggedInMember.getNickname();
                com.steam.util.GameNetworkManager.submitScore(nickname, score);
            } catch (Exception apiEx) {
                System.err.println("自動發送分數至 Spring Boot API 失敗: " + apiEx.getMessage());
            }
            return ok;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            com.steam.util.DBUtil.close(conn, pstmt, null);
        }
    }

    public boolean unlockAchievement(String achievementId) {
        if (currentLoggedInMember == null) return false;
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        try {
            conn = com.steam.util.DBUtil.getConnection();
            String sql = "INSERT INTO unlocked_achievement (member_id, achievement_id) VALUES (?, ?) " +
                         "ON DUPLICATE KEY UPDATE member_id = member_id";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentLoggedInMember.getId());
            pstmt.setString(2, achievementId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            com.steam.util.DBUtil.close(conn, pstmt, null);
        }
    }

    public void checkAndUnlockAchievements() {
        if (currentLoggedInMember == null) return;
        
        int memberId = currentLoggedInMember.getId();
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.ResultSet rs = null;
        try {
            conn = com.steam.util.DBUtil.getConnection();
            
            // 1. Millionaire: balance >= 1000
            if (currentLoggedInMember.getBalance().doubleValue() >= 1000) {
                unlockAchievement("ach-millionaire");
            }
            
            // 2. Collector: Owned >= 3 games
            String countSql = "SELECT COUNT(*) FROM purchase WHERE member_id = ?";
            pstmt = conn.prepareStatement(countSql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) >= 3) {
                unlockAchievement("ach-collector");
            }
            rs.close();
            pstmt.close();
            
            // 3. First trade: Owned >= 1
            pstmt = conn.prepareStatement(countSql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) >= 1) {
                unlockAchievement("ach-first-buy");
            }
            rs.close();
            pstmt.close();
            
            // 4. Admin
            if ("ADMIN".equals(currentLoggedInMember.getRole())) {
                unlockAchievement("ach-admin");
            }
            
            // 5. Game specific score milestones
            String scoreSql = "SELECT game_id, MAX(score) FROM game_score WHERE member_id = ? GROUP BY game_id";
            pstmt = conn.prepareStatement(scoreSql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int gid = rs.getInt(1);
                int mscore = rs.getInt(2);
                if (gid == 1 && mscore >= 1000) unlockAchievement("ach-tetris-1000");
                if (gid == 2 && mscore >= 2000) unlockAchievement("ach-pacman-2000");
                if (gid == 3 && mscore >= 1) unlockAchievement("ach-gobang-win");
                if (gid == 4 && mscore >= 150) unlockAchievement("ach-brick-150");
                if (gid == 4 && mscore >= 300) unlockAchievement("ach-brick-perfect");
                if (gid == 5 && mscore >= 15) unlockAchievement("ach-bird-15");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            com.steam.util.DBUtil.close(conn, pstmt, rs);
        }
    }
}
