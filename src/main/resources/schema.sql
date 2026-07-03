-- MySQL 8.0 Database Schema for Steam Platform
CREATE DATABASE IF NOT EXISTS steam_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE steam_db;

-- 1. Member Table
CREATE TABLE IF NOT EXISTS member (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    balance DECIMAL(10,2) DEFAULT 1000.00,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Game Table
CREATE TABLE IF NOT EXISTS game (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    price DECIMAL(10,2) NOT NULL,
    description TEXT,
    genre VARCHAR(50),
    thumbnail VARCHAR(100) DEFAULT '🎮',
    banner_color VARCHAR(100) DEFAULT 'from-slate-700 to-slate-900',
    game_url VARCHAR(255) DEFAULT NULL,
    java_class_path VARCHAR(255) DEFAULT NULL
) ENGINE=InnoDB;

-- 3. Purchase Order Table (Checkout Transactions)
CREATE TABLE IF NOT EXISTS purchase_order (
    id VARCHAR(50) PRIMARY KEY,
    member_id INT NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    purchase_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Purchase Table (Inventory / Owned Games)
CREATE TABLE IF NOT EXISTS purchase (
    id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    game_id INT NOT NULL,
    purchase_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    transaction_id VARCHAR(50) DEFAULT NULL,
    price_at_purchase DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE,
    FOREIGN KEY (transaction_id) REFERENCES purchase_order(id) ON DELETE SET NULL,
    UNIQUE KEY unique_member_game (member_id, game_id)
) ENGINE=InnoDB;

-- 5. Cart Item Table (Shopping Cart Persistent State)
CREATE TABLE IF NOT EXISTS cart_item (
    id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    game_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE,
    UNIQUE KEY unique_member_cart_game (member_id, game_id)
) ENGINE=InnoDB;

-- 6. Game Score Table (Leaderboard)
CREATE TABLE IF NOT EXISTS game_score (
    id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    game_id INT NOT NULL,
    score INT NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. Achievement Table
CREATE TABLE IF NOT EXISTS achievement (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    requirement_text VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- 8. Unlocked Achievement Table
CREATE TABLE IF NOT EXISTS unlocked_achievement (
    id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    achievement_id VARCHAR(50) NOT NULL,
    unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    FOREIGN KEY (achievement_id) REFERENCES achievement(id) ON DELETE CASCADE,
    UNIQUE KEY unique_member_achievement (member_id, achievement_id)
) ENGINE=InnoDB;

-- Seed initial data
INSERT INTO member (username, password, email, nickname, balance, role) VALUES 
('admin', 'admin123', 'admin@steam.com', '系統管理員', 9999.00, 'ADMIN'),
('user1', 'user123', 'user1@gmail.com', '玩家一號', 500.00, 'USER'),
('user2', 'user123', 'user2@gmail.com', '玩家二號', 100.00, 'USER')
ON DUPLICATE KEY UPDATE username=username;

INSERT INTO game (id, name, price, description, genre, thumbnail, banner_color, game_url, java_class_path) VALUES
(1, '俄羅斯方塊 (Tetris)', 50.00, '經典落體方塊遊戲，挑戰極速消除！', '益智', 'img/tetris.png', 'from-cyan-700 to-blue-900', 'game/tetris.jar', 'controller.TetrisGame'),
(2, '小精靈 (Pacman)', 80.00, '在迷宮中躲避幽靈並吃掉所有豆子！', '經典街機', 'img/pacman.png', 'from-fuchsia-700 to-purple-900', 'game/pacman.jar', 'controller.PacmanGame'),
(3, '五子棋 (Gobang)', 30.00, '兩人黑白博弈，先連成五子者勝！', '棋牌', 'img/gobang.png', 'from-amber-600 to-orange-800', 'game/gobang.jar', 'controller.GobangGame'),
(4, '打磚塊 (Brick Breaker)', 40.00, '移動擋板反彈彈珠，粉碎所有彩色磚塊！', '動作街機', 'img/brick_breaker.png', 'from-emerald-700 to-teal-900', 'game/brick_breaker.jar', 'controller.BrickBreakerGame'),
(5, '飛翔的小鳥 (Flappy Bird)', 20.00, '點擊屏幕讓小鳥飛過綠色管道，極具挑戰性！', '休閒', 'img/flappy_bird.png', 'from-rose-700 to-pink-900', 'game/flappy_bird.jar', 'controller.FlappyBirdGame')
ON DUPLICATE KEY UPDATE name=name, game_url=VALUES(game_url), java_class_path=VALUES(java_class_path);

INSERT INTO purchase (member_id, game_id) VALUES
(2, 3)
ON DUPLICATE KEY UPDATE member_id=member_id;

-- Seed achievements data
INSERT INTO achievement (id, title, description, category, requirement_text) VALUES
('ach-tetris-1000', '方塊大師 (Tetris Master)', '俄羅斯方塊單局得分達到 1000 分。', '俄羅斯方塊', 'Tetris 積分 >= 1000'),
('ach-pacman-2000', '大胃王 (Pacman Feast)', '小精靈單局得分達到 2000 分。', '小精靈', 'Pacman 積分 >= 2000'),
('ach-gobang-win', '棋聖下凡 (Gobang Master)', '在五子棋中成功擊敗 AI 電腦。', '五子棋', '五子棋獲得 1 場勝場'),
('ach-brick-150', '磚塊粉碎者 (Brick Destroyer)', '打磚塊單局得分達到 150 分。', '打磚塊', 'Brick Breaker 積分 >= 150'),
('ach-brick-perfect', '絕代神射手 (Perfect Aim)', '打磚塊單局得分達到 300 分。', '打磚塊', 'Brick Breaker 積分 >= 300'),
('ach-bird-15', '黃金羽翼 (Golden Wings)', '飛翔的小鳥單局飛過 15 根水管。', '飛翔的小鳥', 'Flappy Bird 積分 >= 15'),
('ach-millionaire', '石油大亨 (Steam Millionaire)', '您的蒸汽錢包餘額超過 $1,000 元。', '平台全域', '錢包餘額 >= $1000'),
('ach-collector', '遊戲收藏家 (Game Collector)', '在商店成功購買並擁有 3 款或以上遊戲。', '平台全域', '收藏庫遊戲數量 >= 3'),
('ach-first-buy', '蒸汽初體驗 (First Trade)', '首次成功購買任意一款付費遊戲。', '平台全域', '完成任意 1 筆購買記錄'),
('ach-admin', '神之特權 (Administrator)', '身為系統管理員 (ADMIN) 帳號登入平台。', '平台全域', '帳號角色為 ADMIN')
ON DUPLICATE KEY UPDATE title=title;

-- Seed scores data
INSERT INTO game_score (member_id, game_id, score) VALUES
(1, 1, 1250),
(2, 1, 750),
(1, 2, 2800),
(3, 2, 950),
(1, 3, 15),
(2, 3, 8),
(1, 4, 220),
(2, 4, 140),
(1, 5, 25),
(3, 5, 5);
