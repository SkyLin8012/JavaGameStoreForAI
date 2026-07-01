package com.steam.view.game;

import javax.swing.JFrame; // 視窗框架工具，用來做出桌面的視窗外框
import javax.swing.JPanel;// 面板畫布工具，用來當作遊戲的畫面畫布
import javax.swing.Timer;// 計時器工具，用來當作遊戲運作的心跳（遊戲迴圈）

//import model.entity.Log;
import com.steam.model.*;
//port servise.impl.LogServiceImpl;
//import util.Tool;

import javax.swing.SwingUtilities;// Swing 工具箱，用來確保畫面執行的安全執行緒
import javax.swing.ImageIcon;// 圖像載入工具，用來把硬碟裡的圖片讀進程式
import java.awt.Dimension; // 幾何尺寸工具，用來封裝寬度和高度的數值
import java.awt.Color; // 顏色工具，提供 Color.BLACK 或自訂 RGB 顏色
import java.awt.Graphics; // 畫筆工具，提供在畫布上畫線、畫方塊、寫字的方法
import java.awt.Graphics2D; // 進階畫筆工具，支援消除鋸齒、旋轉等進階繪圖
import java.awt.RenderingHints;  // 繪圖參數設定，用來開啟或關閉特定的渲染效果（如文字平滑）
import java.awt.Font;   // 字型工具，用來設定英文字型、微軟正黑體、大小與粗體
import java.awt.FontMetrics; // 字體測量工具，可以計算出特定字串在畫面上佔用的像素寬高
import java.awt.Point; // 座標工具，封裝了 (x, y) 兩個整數數值，代表網格位置
import java.awt.Image; // 影像基礎類別，用來儲存讀入的圖片資料
import java.awt.event.ActionEvent; // 動作事件，當按鈕被按或計時器時間到時會產生此事件
import java.awt.event.ActionListener;// 動作監聽器介面，用來接收並處理 ActionEvent
import java.awt.event.KeyAdapter; // 鍵盤適配器，已經幫你實作好鍵盤介面，只要挑需要的覆寫即可
import java.awt.event.KeyEvent;   // 鍵盤事件，包含了玩家按下哪一個按鍵的資訊（如 A、W、S、D）
import java.util.LinkedList; // 鏈結串列資料結構，用來當作蛇的身體，方便動態加頭去尾
import java.util.Random; // 亂數產生器，用來隨機決定食物出生的座標位置
import javax.sound.sampled.AudioInputStream; // 音訊輸入串流，將音檔轉為程式讀得懂的數位訊號
import javax.sound.sampled.AudioSystem; // 音訊系統總管，負責向作業系統調用麥克風或喇叭
import javax.sound.sampled.Clip;  // 音訊剪輯物件，可以把一小段音檔讀入記憶體，反覆播放
import java.io.File; // 檔案物件，指向硬碟中的實體檔案路徑
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SnakeGame extends JFrame {

    public SnakeGame() {
        this.setTitle("Java 經典貪食蛇");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//設定預設的關閉行為。當使用者按下視窗右上角的「X」按鈕時，要完全結束這個 Java 程式並釋放記憶體。
        this.setResizable(false);
        
        SGamePanel panel = new SGamePanel();
        this.add(panel);
		//將剛剛實體化出來的 panel 畫布，放進（加進）目前的 JFrame 視窗大框框裡
        this.pack();
        //確保畫布呈現出完整的規格，不會被標題列或邊框吃掉空間
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SnakeGame());
		//使用 Lambda 語法將建立視窗的任務
		//避免遊戲畫面在開啟時發生初始化凍結或崩潰
    }
}
//義了遊戲的網格系統、遊戲狀態機，以及蛇、食物、音樂圖片等記憶體配置
class SGamePanel extends JPanel implements ActionListener {

    private static final int SCREEN_WIDTH = 600;
    private static final int SCREEN_HEIGHT = 600;
    private static final int UNIT_SIZE = 25;
    private static final int DELAY = 140;
    //宣告一個列舉（Enum）型態用來定義這款遊戲會經歷的三種狀態
    private enum GameState {
        START, RUNNING, GAME_OVER
		//START（封面狀態）、RUNNING（正在玩）、GAME_OVER（死亡結算）
    }
    private GameState state = GameState.START;

    private final LinkedList<Point> snake = new LinkedList<>();
    private Point food;
    private char direction = 'R';
    private int score = 0;
   
    private Timer timer;
    private final Random random = new Random();
    private Clip bgmClip;
    URL snake_head = getClass().getResource("img/snake_head.png");
    URL snake_body = getClass().getResource("img/snake_body.png");
    URL snake_food = getClass().getResource("img/food.png");
    private final Image headImage = new ImageIcon(snake_head).getImage();
    private final Image bodyImage = new ImageIcon(snake_body).getImage();
    private final Image foodImage = new ImageIcon(snake_food).getImage();
	//畫布建構子
    public SGamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        timer = new Timer(DELAY, this);
    }
	//開始遊戲
    private void startGame() {
        snake.clear();
        snake.add(new Point(100, 300));
        snake.add(new Point(75, 300));
        snake.add(new Point(50, 300));
        
        direction = 'R';
        score = 0;
        spawnFood();
        
        state = GameState.RUNNING;
        timer.start(); 
        URL snake_bgm = getClass().getResource("song/bgm.wav");
        if(snake_bgm!=null)
        {
        	playBGM(snake_bgm);
        }
        else
        {
        	System.out.println("沒有背景音樂");
        }
        
    }
	//生成食物
    private void spawnFood() {
        int x = random.nextInt((SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        int y = random.nextInt((SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
        food = new Point(x, y);
        
        if (snake.contains(food)) {
            spawnFood();
        }
    }
	//把記憶體裡的數據畫給人類看
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        switch (state) {
            case START: drawStartScreen(g); break;
            case RUNNING: drawGamePlay(g); break;
            case GAME_OVER: drawGameOverScreen(g);insertLog(score); break;
			default: break;
        }
    }
	//畫開始封面
    private void drawStartScreen(Graphics g) {
        g.setColor(Color.GREEN);
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 50));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("經典貪食蛇", (SCREEN_WIDTH - metrics1.stringWidth("經典貪食蛇")) / 2, SCREEN_HEIGHT / 3);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 20));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("按 [ ENTER ] 開始遊戲", (SCREEN_WIDTH - metrics2.stringWidth("按 [ ENTER ] 開始遊戲")) / 2, SCREEN_HEIGHT / 2);
        
        g.setColor(Color.GRAY);
        g.drawString("使用 方向鍵 或 WASD 控制移動", (SCREEN_WIDTH - metrics2.stringWidth("使用 方向鍵 或 WASD 控制移動")) / 2, SCREEN_HEIGHT / 2 + 80);
    }
	//畫遊戲進行畫面
    private void drawGamePlay(Graphics g) {
        g.drawImage(foodImage, food.x, food.y, UNIT_SIZE, UNIT_SIZE, this);

		Graphics2D g2d = (Graphics2D) g.create();
		//複製一支獨立的畫筆，避免汙染畫筆座標軸，就能安全釋放這支臨時畫筆
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < snake.size(); i++) {
            if (i == 0) {
               int headX = snake.get(i).x;
			   int headY = snake.get(i).y;
			   int centerX = headX + UNIT_SIZE/2;
			   int centerY = headY + UNIT_SIZE/2;
			   double angle=0;

			   switch(direction){
				case'R':angle=0; break;//direction=='R'旋轉角度0
				case'D':angle=Math.toRadians(90); break;
				case'L':angle=Math.toRadians(180);break;
				case'U':angle=Math.toRadians(270);break;
			   }
			   g2d.translate(centerX,centerY);
			   //用translate把座標軸的原點(0,0)暫時【平移搬家】到蛇頭格子正中央
			   g2d.rotate(angle);
			   //rotate()接受弧度非角度，必須用Math.toradians()換算成弧度
			   g2d.drawImage(headImage, -UNIT_SIZE / 2, -UNIT_SIZE / 2, UNIT_SIZE, UNIT_SIZE, this);
			   //因座標原點搬到格子中央，用圖片往左上角回推半個格子寬(-UNIT_SIZE/2)
			   //來畫圖片，使圖片在正中心貼合中心點上，達到順暢旋轉!
			   g2d.rotate(-angle);
			   g2d.translate(-centerX, -centerY);
			} else {
                g.drawImage(bodyImage, snake.get(i).x, snake.get(i).y, UNIT_SIZE, UNIT_SIZE, this);
            }
        }
		g2d.dispose();

        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 20));
        g.drawString("Score: " + score, 15, 30);
    }
	//畫遊戲結束畫面
    private void drawGameOverScreen(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Consolas", Font.BOLD, 60));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("GAME OVER", (SCREEN_WIDTH - metrics1.stringWidth("GAME OVER")) / 2, SCREEN_HEIGHT / 3);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Microsoft JhengHei", Font.BOLD, 30));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("您的最終得分: " + score, (SCREEN_WIDTH - metrics2.stringWidth("您的最終得分: " + score)) / 2, SCREEN_HEIGHT / 2);
        
        g.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 20));
        FontMetrics metrics3 = getFontMetrics(g.getFont());
        g.drawString("按 [ ENTER ] 重新挑戰", (SCREEN_WIDTH - metrics3.stringWidth("按 [ ENTER ] 重新挑戰")) / 2, SCREEN_HEIGHT / 2 + 100);
    }
    //將記錄寫入資料庫
    private void insertLog(int source)
    {
    	//載入cookie
  		Member me=null;
  		com.steam.controller.SteamController.getInstance().recordScore(6, score);
  		//me=(Member) Tool.readFile("member.txt");
  		//Log log = new Log(1,me.getUid(),me.getName(),"1","經典貪食蛇",Integer.toString(source),LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
  		//new LogServiceImpl().createLog(log);

    }
	//蛇的移動邏輯 (核心！)
    private void move() {
        Point head = snake.getFirst();
        Point newHead = new Point(head.x, head.y);

        switch (direction) {
            case 'U': newHead.y -= UNIT_SIZE; break;
            case 'D': newHead.y += UNIT_SIZE; break;
            case 'L': newHead.x -= UNIT_SIZE; break;
            case 'R': newHead.x += UNIT_SIZE; break;
			default: break;
        }

        snake.addFirst(newHead);

        if (newHead.equals(food)) {
            score += 10;
            URL snake_food = getClass().getResource("song/eat.wav");
            if(snake_food !=null)
            {
            	playSound(snake_food);
            }else
            {
            	System.out.println("找不到音效!");
            }
            
            spawnFood();
        } else {
            snake.removeLast();
        }
    }
	//碰撞判定
    private void checkCollision() {
        Point head = snake.getFirst();

        if (head.x < 0 || head.x >= SCREEN_WIDTH || head.y < 0 || head.y >= SCREEN_HEIGHT) {
            gameOver();
            return;
        }

        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                gameOver();
                return;
            }
        }
    }
	//執行死亡程序
    private void gameOver() {
        state = GameState.GAME_OVER;
        timer.stop();
        stopBGM();
        repaint();
    }
	//播放短音效（如吃東西）
    private void playSound(URL soundUrl) {
        try {
            
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundUrl);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	//播放背景音樂
    private void playBGM(URL soundUrl) {
        try {
            stopBGM();
           
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundUrl);
                bgmClip = AudioSystem.getClip();
                bgmClip.open(audioStream);
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	//安全關閉背景音樂
    private void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }
    }
	//負責將計時器與鍵盤硬體，和我們前面的邏輯串接在一起
    @Override
    public void actionPerformed(ActionEvent e) {
        if (state == GameState.RUNNING) {
            move();
            checkCollision();
        }
        repaint();
    }
	//用來接收鍵盤實體按鍵
	private class MyKeyAdapter extends KeyAdapter{
		@Override
		public void keyPressed(KeyEvent e){
			int keyCode = e.getKeyCode();
			//抓取按鍵程式碼
			if(keyCode == KeyEvent.VK_ENTER){
				if(state==GameState.START|| state== GameState.GAME_OVER){
					startGame();
					return;
				}
			}
			//按下Enter鍵(VK_ENTER)，若遊戲在【封面】、【結束】狀態
			//觸發StartGame() 展開新遊戲
			if(state==GameState.RUNNING){
				switch ((keyCode)) {
					case KeyEvent.VK_LEFT:						
					case KeyEvent.VK_A:
						if(direction !='R')direction='L';
						break;
						//當遊戲進行，只要目前方向不是(R)右，就把方向改為(L)
						//防止自殺
						case KeyEvent.VK_RIGHT:
						case KeyEvent.VK_D:
							if(direction !='L')direction='R';
							break;
						case KeyEvent.VK_UP:
						case KeyEvent.VK_W:
							if(direction!='D')direction='U';
							break;
						case KeyEvent.VK_DOWN:
						case KeyEvent.VK_S:
							if(direction != 'U')direction='D';
							break;			
					default:
						break;
				}
			}
			//右鍵/D鍵限制不能向左折返；上鍵/W鍵限制不能向下折返；
			//下鍵/S鍵限制不能向上折返。等待140毫秒，蛇會朝新方向
		}
	}
}
