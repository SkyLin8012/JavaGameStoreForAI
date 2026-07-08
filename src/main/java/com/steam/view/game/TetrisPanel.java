package com.steam.view.game;

import javax.swing.border.EmptyBorder;

import com.steam.model.Member;

import javax.imageio.ImageIO; //圖片讀取
import javax.sound.sampled.*; //處理音訊(音樂、音效)播放
import javax.swing.*; 		  //建立視窗、計時器等GUI元件的Swing類別庫
import java.awt.*;			  //處理顏色、字型、版面配置與基礎繪圖的AWT類別庫
import java.awt.event.KeyAdapter;//處理鍵盤按鍵監聽的類別庫
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;//快取圖片格式(記憶體中的圖片檔)的類別庫
import java.io.File; 		  //處理當按讀取與列外狀況(錯誤處理)的類別庫
import java.io.IOException;   
import java.util.Random;	  //產生亂數(隨機選擇方塊形狀)的類別庫
import java.net.URL;

/*
 * 俄羅斯方塊主程式類別，繼承自 JFrame(視窗容器)
 * 這代表此遊戲為可顯示在螢幕上的視窗
 * */
public class TetrisPanel extends JFrame {
	//定義遊戲的三種狀態:MENU(主選單)、PLAYING(進行中)、GAME_OVER(遊戲結束)
	enum GameState{MENU,PLAYING,GAME_OVER}
	//宣告目前遊戲狀態、預設為主選單(MENU)
	private GameState state= GameState.MENU;
	
	//定義遊戲地圖的寬度:10個格子
	private static final int BOARD_WIDTH=10;
	//定義遊戲地圖的高度:20個格子
	private static final int BOARD_HEIGHT=20;
	//定義單一方形格子的寬高 30像素(Pexels)
	private static final int CELL_SIZE=30;
	//==============================
	//建立 20行* 10列的二微陣列，用來儲存已經落底部的【死方塊】
	//==============================
	//陣列值為0代表空格，1-7代表不圖顏色的方塊
	private int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
	//儲存目前玩家的分數
	private int score=0;
	//儲存累計消除的總行數
	private int linesCleared =0;
	// Swing 的計時器，用來控制方塊定時自動往下落(Tick迴圈)
	private Timer gameTimer;
	
	//儲存[目前控制中]的方塊的形狀(列如 2*4 或 3*3的二維小陣列)
	private int[][] currentPiece;
	//儲存目前空助中的方塊種類編號(1-7)
	private int currentPieceType;
	//curX 與 curY紀錄【目前方塊】在 10*20大地圖上的【左上角】座標位置
	private int curX, curY;
	
	//儲存【下一顆即將出現】的方塊形狀，用來繪製右側預覽畫面
	private int[][] nextPiece;
	//儲存下一顆方塊的種類編號(1-7)
	private int nextPieceType;
	
	//音訊播放物件，用來載入與控制背景音樂(BGM)的播放、停止或循環
	private Clip bgmClip;
	
	/*===============================================
	 *【視覺插圖與背景圖程式介面 (API HooKs)】
	 * 開放對外接口，允許自外部傳入可製化的Image物件
	 *=============================================== 
	 * */
	URL menuURL =  getClass().getResource("img/TetrisG/menu_art.jpg");
	URL gameplayURL = getClass().getResource("img/TetrisG/gameplay_bg.png");
	URL gameOverURL = getClass().getResource("img/TetrisG/gameover_art.jpg");
	
	private Image menuBackgroundImage =  new ImageIcon(menuURL).getImage();     //主選單(封面)的底圖
	private Image gameplayBackgroundImage = new ImageIcon(gameplayURL).getImage(); //遊戲進行中的底圖
	private Image gameOverBackgroundImage = new ImageIcon(gameOverURL).getImage(); //結算畫面的底圖
	
	/* *
	 * 設定主選單(封面)背景插圖
	 * @param img 載入進來的Image 物件
	 * */
	public void setMenuBackgroundImage(Image img) {
		this.menuBackgroundImage = img;  //請傳入的圖片指派給變數
		repaint();						 //立即重新繪製畫面，讓圖片顯現
	}
	
	/* *
	 * 設定遊戲進行中的背景底圖(在畫面上會自動套用防干擾半透明遮罩)
	 * @param img 載入進來的 Image 物件
	 * */
	public void setGameplayBackgroundImage(Image img) {
		this.gameplayBackgroundImage=img;	// 指派圖片
		repaint();						    // 重新繪製畫面
	}
	/* *
	 * 設定遊戲結束結算畫面背景插圖
	 * @param img 進入進來的 Image 物件
	 * */
	public void setGameOverBackgroundImage(Image img){
		this.gameOverBackgroundImage = img;	//指派圖片
		repaint();							//重新繪製畫面
	}
	//==================================================
	
	/* *
	 * 定義俄羅斯方塊的 7 種形狀(使用三維診列)
	 * 第一個維度代表形狀索引: 0是空白、1是I、2是J、3是L、4是O、5是S、6是T、7是Z 
	 * 每個元素都是一個二維陣列，用1代表實體格子，0代表中空位置
	 * */
	private final int[][][] SHAPES= {
			{}, //索引 0:空白預留
			{{1,1,1,1}}, //索引 1:I 條型(1*4)
			{{1,1,1},{0,0,1}}, //索引 2:J 形 (2*3)
			{{1,1,1},{1,0,0}}, //索引 3:L 形 (2*3)
			{{1,1},{1,1}}, //索引 4:O形 (2*2 正方形)
			{{0,1,1},{1,1,0}}, //索引 5:S 形 (2*3)
			{{0,1,0},{1,1,1}}, //索引 6:T 形 (2*3 凸字)
			{{1,1,0},{0,1,1}}  //索引 7:Z 形 (2*3)
	};
	
	/* 定義7種方塊加上背景黑色的經典配色陣列 (對應上述索引 0-7)
	 * 為了在自訂背景圖忠看得清楚，有特別桃高亮度與飽和度 
	 * */
	private final Color[] COLORS = {
			Color.BLACK,			//索引 0: 空格(通常不繪製)
			new Color(0,240,240),	//I: 亮青藍色(Cyan)
			new Color(0,0,240),		//J: 深藍色(Blue)
			new Color(240,160,0),	//L: 亮橘色(Orange)
			new Color(240,240,0),	//O: 黃色(Yellow)
			new Color(0,240,0),		//S: 綠色(Green)
			new Color(160,0,240),	//T: 紫色(Magenta)
			new Color(240,0,0)		//Z: 紅色(Red)
	};
	
	/*======================
	 * TetrisGame 的建構子(Constructor)
	 * 在這裡進行視窗初始化、設定、監聽器掛載與音樂啟動
	 *======================
	 */
	public TetrisPanel() {
		//設定作業系統視窗標題文字
		setTitle("經典 俄羅斯方塊");
		//設定當使用者按下視窗右上角的【X]時，關閉程式並釋放記憶體		
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// 1. 先告訴視窗：當使用者點 X 時，先不要自己亂動（由我們接手處理）
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		// 2. 加入監聽器，在關閉時主動關閉音樂
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent e) {
		        // 呼叫你的停止音樂方法
		        stopBGM(); 
		        
		     // 只關閉、釋放這個子視窗，主程式（其他視窗）還會繼續活著
		        dispose();
		    }
		});
		//禁止使用者手動拖拉改變視窗大小，以免跑版
		setResizable(false);
		
		//建立可客製化繪圖面板 GamePanel(內部類別在程式下方)
		GamePanel panel =new GamePanel();
		//設定繪圖面板的大小:寬度為(地圖寬度10x30像素) + 右邊資訊欄的150像素；高度為 地圖高度20x30像素
		panel.setPreferredSize(new Dimension(BOARD_WIDTH * CELL_SIZE + 150,BOARD_HEIGHT * CELL_SIZE));
		//將繪圖面板加到JFrame 視窗容器中
		add(panel);
		//自動調整視窗大小，使其完美貼合內部繪圖面板的 PreferredSize
		pack();
		//設定視窗開啟時的初始位置，傳入 null 代表讓視窗在螢幕【正中間】顯示
		setLocationRelativeTo(null);
		
		//為視窗註冊鍵盤監聽事件
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				//將玩家按下的鍵盤按鍵代碼(keyCode) 送入 handleInput 方法處理
				handleInput(e.getKeyCode());
				//每次按完案鍵後，強迫更新畫面，否則畫面部會立即變動
				panel.repaint();
			}
		});
		
		// 初始化遊戲的核心計時器:每隔500毫秒(0.5秒)觸法一次Tick動作
		gameTimer = new Timer(500,e->{
			// 只有在【遊戲進行中(PLAYING)]狀態下，計時器才運作
			if(state==GameState.PLAYING) {
				// 試圖將目前的方塊向下移動一格(dy=1)
				// 如果moveing(0,1)回傳 false，代表方塊底下已經有障礙物或底邊.
				if(!moveing(0,1)) {
					lockPiece(); //1.將目前的方塊【烙印/固定】在大地圖陣列 board 裡
					checkLines();//2.檢查有沒有整行塞滿的情況，並禁行消除與加分
					spawnPiece(); //3.生成下一個新方塊到最上方
					
				}
				// 每次計時器震盪、方塊往下走一格後，都要重繪面板更新畫面
				panel.repaint();
			}			
		});
		
		
	}
	/* *
	 * 處理玩家的所有按鍵邏輯
	 * @param keyCode 按下的按鈕虛擬代碼(例如:KeyEvent.VK_UP) 
	 * */
	private void handleInput(int keyCode) {
		//情況 A:目前處於主選單
		if(state == GameState.MENU) {
			// 玩家按下空白鍵(SPACDE) 或 Enter 鍵時，開始遊戲
			if(keyCode == KeyEvent.VK_SPACE || keyCode== KeyEvent.VK_ENTER) {
				startGame();
			}
		}
		//情況B:目前正在遊玩中
		else if (state == GameState.PLAYING) {
			switch(keyCode) {
				//按下方向鍵 ←:往左移動一格(dx=-1)
				case KeyEvent.VK_LEFT:  moveing(-1,0); break;
				//按下方向鍵 →:往右移動一格(dx=1)
				case KeyEvent.VK_RIGHT: moveing(1,0);break;
				//按下方向鍵 ↓:加速往下掉落一格(dy=1)
				case KeyEvent.VK_DOWN: moveing(0,1);break;
				//按下方向鍵 ↑:進行方塊順時針旋轉
				case KeyEvent.VK_UP:
					rotate(); //旋轉方塊
					playSound("song/TetrisG/rotate.wav"); //播放旋轉的音效				
					break;
				// 按下空白鍵(SPACE):瞬間落到底部並固定(hard Drop)
				case KeyEvent.VK_SPACE:hardDrop();break;
			}
		}
		// 情況c:目前處於遊戲結束(結算)畫面
		else if(state == GameState.GAME_OVER) {
			//將分數載入紀錄
			//com.steam.controller.SteamController.getInstance().recordScore(1, score);
			//結束音樂
			stopBGM();
			//按下空白鍵或 Enter 鍵時，回到主選單
			if(keyCode==KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_ENTER) {
				state = GameState.MENU;
			}
		}
	}
	/* *
	 * 點擊開始後，初始化新遊戲所的各項數據
	 * */
	private void startGame() {
		// 重建一個全新、全空(值皆為0)的 20x10 地圖陣列
		board = new int[BOARD_HEIGHT][BOARD_WIDTH];
		score=0;   			//分數歸零
		linesCleared =0; 	//消除行數歸零
		state= GameState.PLAYING; //將遊戲狀態轉移至【進行中】
		
		Random rand = new Random(); //宣告亂數產生器
		// 亂數抽取下一顆方塊的種類:rand.nextInt(7) 會產生 0 到 6,加 1後變為 1到7
		nextPieceType = rand.nextInt(7) + 1;
		// 根據抽取到的種類，從SHAPES 三維陣列中取出對應的二維圖案陣列
		nextPiece = SHAPES[nextPieceType];
		
		// 呼叫產生方法，把一顆方塊產出來並就為
		spawnPiece();
		// 啟動 Swing 定時下落計時器
		gameTimer.start();
		// 啟動音樂播放功能
				playBGM();
	}
	
	/* *
	 * 生成新方塊並放置於頂部，同時隨機抽出【下一顆】方塊
	 * */
	private void spawnPiece() {
		// 將預覽欄位方塊(nextPiece)複製給【目前操作中】的方塊(currentPiece)
		currentPiece = nextPiece;
		currentPieceType =nextPieceType;
		
		//再次隨機決定下一顆方塊類型(1至7)
		Random rand = new Random();
		nextPieceType = rand.nextInt(7) +1;
		nextPiece = SHAPES[nextPieceType];
		
		//計算目前方塊在x軸的起始座標:目標是放在正中間
		//(地圖寬度10/2) - (當前方塊寬度/2)
		curX = BOARD_WIDTH/2 - currentPiece[0].length/2;
		// Y 軸起始座標為0(最頂端)
		curY = 0;
		
		//[死亡判定]:如果剛產生的方塊在起始點(curX,curY)就發生重疊/碰撞。
		//代表玩家的地圖已經堆滿到頂點了，遊戲直接結束。
		if(!isValidPosition(curX,curY, currentPiece)) {
			state = GameState.GAME_OVER;  //狀態轉為結算
			//將分數載入紀錄
			com.steam.controller.SteamController.getInstance().recordScore(1, score);
			//結束音樂
			stopBGM();
			gameTimer.stop();			  //停止定時下墜計時器
		}		
	}
	/* *
	 * 嘗試移動方塊
	 * @param dx 水平位移量(-1 為左移一格、1為幼儀一格，0為不移動
	 * @param dy 垂直位移量(1 為下移一格、0 為不移動)
	 * @return 如果移動成功回傳 ture，若發生碰撞無法移動則回傳false
	 * */
	private boolean moveing(int dx, int dy) {
		//測試【如果】真的移動到了新座標(curX+dx, curY+dy).這個新位置是否合法(不碰撞)
		if(isValidPosition(curX + dx, curY + dy, currentPiece)) {
			curX += dx;
			curY += dy;
			return true;
		}		
		return false;
	}
	
	/* *
	 * 瞬間落到底部(hard Drop)
	 * */
	private void hardDrop() {
		//用一個簡單的while迴圈，持續呼叫moveing(0,1)
		//只要還能往下移動一格，就一直往下移動
		while(moveing(0,1)) {
			//回圈內部不需要做任何事，單純利用moveing的位移功能
		}
		//不能在下移時，強迫進行固定、消除行與產新方塊的三部曲
		lockPiece();
		checkLines();
		spawnPiece();
	}
	
	/* *
	 * 順時針 90度旋轉方塊的邏輯(矩陣旋轉與左右翻轉)
	 * */
	private void rotate() {
		int r= currentPiece.length; 	//取得目前方塊陣列的陣列(Rows)
		int c= currentPiece[0].length;  //取得目前方塊陣列的欄數(Columns)
		
		// 建立一個行列互相對調新陣列:列如原 2x3 的陣列，旋轉後要成為 3x2 大小
		int[][] rotated = new int[c][r];
		
		// 雙重迴圈遍歷原矩陣，套用順時針旋轉 90度的數學公式:
		// 新二維陣列的第j列，倒數第i欄=原陣列的i列、第j欄
		for(int i = 0; i<r;i++) {
			for(int j=0;j<c;j++) {
				rotated[j][r-1-i] = currentPiece[i][j];
			}
		}
		
		// 安全機制:旋轉後的新形狀必須[不與地圖上的方塊或牆壁碰撞】，我們才正式套用旋轉
		if(isValidPosition(curX, curY, rotated)) {
			currentPiece = rotated;//成功旋轉，換上新形狀
		}
	}
	
	/* *
	 * 核心邏輯:檢測方塊的指定座標與形狀下，是否屬於合法位置(無碰撞)
	 * @param nx 測試中的目標 X 座標
	 * @param ny 測試中的目標 Y 座標
	 * @param piece 測試中的方塊形狀陣列
	 * @return 若不重疊，沒出界回傳 true, 否則回傳 false
	 * */
	private boolean isValidPosition(int nx, int ny, int[][] piece) {
		// 遍歷該方塊形狀中的每一個小格子
		for(int i=0;i<piece.length;i++){
			for(int j=0;j<piece[i].length;j++) {
				//如果這個格子的值不等於0(代表是實心方塊)
				if(piece[i][j]!=0) {
					//計算此小格子在10x20 大地圖上的實際絕對座標
					int boardX = nx + j;
					int boardY = ny + i;
					
					// 1. 水平邊界檢測:如果座標跑出了地圖最左邊(0)或最右邊(BOARD_WIDTH-1)，則不合法
					// 2. 垂直下邊界檢測:如果方塊落得比最底端(BOARD_HEIGHT)還要深，則不合法
					if(boardX < 0 || boardX >= BOARD_WIDTH || boardY >= BOARD_HEIGHT) {
						return false;
					}
					
					// 3. 重疊檢測:如果目標座標已大於等於0(屬於地圖範圍內)
					// 且地圖上該位置的值不是0(代表已經有別的方塊佔據了)，則發生重疊，不合法
					if(boardY >=0 && board[boardY][boardX]!=0) {
						return false;
					}
				}
			}			
		}
		// 通過所有邊界與重疊檢測，此位置安全合法!
		return true;
	}
	
	/*
	 * 將落到底部的方塊[烙印]寫入到 board 二為地圖陣列中固定
	 * */
	private void lockPiece() {
		for(int i=0;i < currentPiece.length; i++) {
			for(int j=0;j < currentPiece[i].length;j++) {
				//只有實心格才需要寫入
				if(currentPiece[i][j]!=0) {
					//如果方塊已進入地圖頂部內(Y >=0)，將其種類代碼寫入地圖
					if(curY + i >=0) {
						board[curY + i][curX + j]= currentPieceType;
					}
				}
			}
		}
	}
	/*
	 * 掃描全網格，檢查是否有滿行並進行消除，往下搬移與計分
	 * */
	private void checkLines() {
		int combo = 0; // 紀錄[本次]一共消除幾行，用來計算連消加分
		
		//從最底行(BOARD_HEIGHT-1)往上掃描到第0行
		for(int i=BOARD_HEIGHT -1; i>=0;i--) {
			boolean isFull = true; //預設此行是塞滿的
			
			// 掃描這行的每一列
			for(int j=0;j<BOARD_WIDTH; j++) {
				//只要有一個格子是空的(0),就代表沒滿，不能消除
				if(board[i][j]==0) {
					isFull = false;
					break; //提早跳出內層迴圈
				}
			}
			//如果此行確實全滿了
			if(isFull) {
				combo++; //本次消行數加1
				//[下移邏輯]:將【這一行】以上的所有行，複製到它的下一行去
				for(int k= i; k>0;k--) {
					board[k]=board[k-1].clone(); //整行克隆複製
				}
				//最頂端(第0行)因為大家都往下掉一格了，所以必須補上一行全空的值
				board[0] = new int[BOARD_WIDTH];
				
				//核心技巧:因為上面複製下來的行可能也是滿的，所以我們的行索引i
				//必須加1(原地踏步)，讓下一輪迴圈重新再檢查一次者個高度的行
				i++;
			}						
		}
		
		//如果這次有消除任何行數
		if(combo>0) {
			linesCleared += combo;			// 增加總消除行數
			score += combo * combo * 100;   // 本次得分(消除行數的平方 * 100)，獎勵連消
			playSound("song/TetrisG/clear.wav");			// 播放消除音效
		}
	}
	
	/*
	 * 播放音效檔的輔助方法(短效音，隨播即丟)
	 * @param filename 音效檔案的路徑名稱(例如"clear.wav")
	 * */
	private void playSound(String filename) {
		try {
			URL soundURL = getClass().getResource(filename);  //指向本地音效檔案
			if(soundURL==null) return;		  //如果檔案不存在，直接放棄，不拋出異常
			
			// 建立音訊輸入流
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
			Clip clip = AudioSystem.getClip(); //獲取音訊Clip 線程播放器
			clip.open(audioIn);				   //載入並解碼音效資料
			clip.start();					   //開始播放一次
		}catch(Exception e) {
			//印出載入失敗說明，但部會讓遊戲當機
			System.out.println("音效加載失敗: "+filename);
		}
	}
	/*
	 * 背景音樂(BGM) 的專用播放方法(會自動無限循環播方)
	 * */
	private void playBGM() {
		try {
			stopBGM();
			URL soundFile = getClass().getResource("song/TetrisG/bgm.wav");
			if(soundFile==null) return;
			
			//建立音訊輸入流
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
			bgmClip = AudioSystem.getClip(); 		//獲取音訊 Clip線程播放器
			bgmClip.open(audioIn);					//載入並解碼音效資料
			bgmClip.loop(Clip.LOOP_CONTINUOUSLY); 	//開始播放一次
			//bgmClip.start();
		}catch(Exception e) {
			//印出載入失敗說明，但不會讓遊戲當機
			System.out.println("背景音樂載入失敗");
		}
	}
	//安全的關閉背景音樂方法
	private void stopBGM() {
		//如果音樂控制物件不為空且正在播放
		if(bgmClip != null ) {
			if(bgmClip.isRunning())
			{
				bgmClip.stop();//停止音樂
			}
			bgmClip.close();//播放音樂所占用的系統資源
		}
		
	}

	//================================
	// 內部類別:處理遊戲畫面與自訂底圖渲染(繼承自 JPanel 繪圖板)
	//================================
	private class GamePanel extends JPanel{
		/* *
		 * 這是 Swing最核心的繪圖入口
		 * 每次我們呼叫 repaint()時，Java 就會自動來呼叫的方法。 
		 * */
		@Override
		protected void paintComponent(Graphics g) {
			//呼叫父類別的繪圖清理工具，維持畫面清潔
			super.paintComponent(g);
			
			//將基礎 Graphics 物件強制轉型為功能更強大的 Graphics2D 繪圖引擎
			Graphics2D g2d = (Graphics2D)g;
			
			//設定【抗鋸齒/平滑化】參數，讓繪製出的字體、線條邊緣部會有毛邊鋸齒
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			
			//狀態分流渲染
			if(state== GameState.MENU) {
				drawMenu(g2d);			//A.畫面選單封面
			}else if(state== GameState.PLAYING) {
				drawGameplay(g2d); 		//B.畫遊戲進行中地圖			
			}else if(state== GameState.GAME_OVER) {
				drawGameOver(g2d);		//c.畫遊戲結束畫面
				
				
			}			
		}
		/* *
		 * 1.繪製開始頁面(封面圖) 
		 * */
		
		private void drawMenu(Graphics2D g) {
			//如果外部成功設定了封面背景圖，就滿版畫出
			if(menuBackgroundImage != null) {
				//參數為:圖片，左上角x=0，Y=0，寬度=面板寬，高度=面板高
				g.drawImage(menuBackgroundImage,0,0,getWidth(),getHeight(),this);
				
				//【UI/UX 設計】在背景圖上壓一層半透明黑濾鏡(不透明為90)
				//用以背景圖太明亮，導致白色主選單標題與文字看不清楚
				g.setColor(new Color(0,0,0,90));
				g.fillRect(0, 0, getWidth(), getHeight());
			}else {
				//備份機制:如果沒有提供圖片，就繪製預設的經典科技感深藍色背景
				g.setColor(new Color(20,24,40));
				g.fillRect(0, 0, getWidth(), getHeight());
				
				//畫出幾個幾何裝飾快，讓畫面不顯得單調
				g.setColor(new Color(40,50,80));
				g.fillRect(50, 100, 60, 60);
				g.fillRect(340, 400, 60, 30);
			}
			
			//繪製遊戲標題字樣
			g.setColor(Color.WHITE);
			g.setFont(new Font("Microsoft JhengHei",Font.BOLD,42));//設定微軟正黑體
			g.drawString("TETRIS 2026", getWidth()/2-120, 180);
			
			//繪製閃爍提示引導文字
			g.setFont(new Font("Microsoft JhengHei",Font.PLAIN,18));
			g.setColor(Color.YELLOW);
			g.drawString("按下 SPACDE或 ENTER 開始遊戲", getWidth()/2-135, 340);
			
			//繪製最底部的操作題是說明
			g.setFont(new Font("Microsoft JhengHei",Font.PLAIN,13));
			g.setColor(Color.LIGHT_GRAY);
			g.drawString("操作:← →移動 | ↑ 旋轉 | ↓ 加速 | 空白鍵瞬間下落",getWidth()/2-150, 520);			
		}
		/* *
		 * 2.繪製遊戲進行中(進行中背景圖與地圖)
		 * */
		private void drawGameplay(Graphics2D g) {
			//決定右側灰色資訊攔的起始 X 軸邊界(即10 * 30 = 300像素點)
			int infox = BOARD_WIDTH * CELL_SIZE;
			
			//繪製底層背景
			if(gameplayBackgroundImage != null) {
				//滿版繪製背景圖
				g.drawImage(gameplayBackgroundImage,0,0,getWidth(),getHeight(),this);
				
				//加上高濃度半透明黑色濾鏡(不透明度=100)
				//以便區分背景圖與下落的俄羅斯方塊
				//避免玩家眼睛疲勞，約70%深色遮罩能令圖面深沉，使方塊完美浮現
				g.setColor(new Color(0,0,0,180));
				g.fillRect(0, 0, infox, BOARD_HEIGHT * CELL_SIZE);
			}else {
				//備份機制:如果玩家沒有放圖片，地圖區域預設塗滿黑色
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, infox, BOARD_HEIGHT * CELL_SIZE);
			}
			
			//繪製遊戲區域的格線(輔助線)
			g.setColor(new Color(60,60,60,150)); //微透灰色的低調網格
			//畫垂值格線
			for(int i=0;i<=BOARD_WIDTH;i++) {
				g.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, BOARD_HEIGHT*CELL_SIZE);
			}
			// 畫水平格線
			for(int i=0;i<=BOARD_HEIGHT; i++)
			{
				g.drawLine(0, i* CELL_SIZE, BOARD_WIDTH * CELL_SIZE, i * CELL_SIZE);				
			}
			
			//繪製已經固定的大地圖(board)上的所有方塊
			for(int r=0;r < BOARD_HEIGHT;r++)
			{
				for(int c=0;c< BOARD_WIDTH;c++) {
					//如果該位置不等於0，代表此處有死的方塊，用 drawCell 將其渲染出來
					if(board[r][c] != 0) {
						drawCell(g,c * CELL_SIZE, r * CELL_SIZE, board[r][c]);
					}
				}					
			}			
			//繪製當前正在被玩家操控並落下的方塊(currentPiece)
			if(currentPiece != null) {
				for(int r=0;r<currentPiece.length;r++){
					for(int c=0;c<currentPiece[r].length;c++) {
						//唯有非0的格子才繪製
						if(currentPiece[r][c]!=0) {
							//將方塊內部相對應座標追微地圖絕對像素座標
							int drawX =(curX +c) * CELL_SIZE;
							int drawY =(curY +r) * CELL_SIZE;
							// 只有事業內(Y >=0，即每有出頂端頂邊)的方格子才呼叫繪製
							if(drawY >=0) {
								drawCell(g,drawX,drawY,currentPieceType);
							}
						}						
					}
				}
			}
			//繪製右側資訊攔的底框(採用般透明黑色 220 霧面質感，維持視覺設計的統一性)
			g.setColor(new Color(15,15,20,220));
			g.fillRect(infox, 0, 150, BOARD_HEIGHT* CELL_SIZE);
			
			// 在資訊欄內繪製文字:目前得分(score)
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial",Font.BOLD,16));
			g.drawString("SCORE", infox + 20, 50);
			g.setColor(Color.GREEN);//分數數值用綠色顯著突出
			g.drawString(String.valueOf(score), infox + 20, 75);
			
			//繪製文字:累計消行數(LINES)
			g.setColor(Color.WHITE);
			g.drawString("LINES", infox + 20, 120);
			g.setColor(Color.CYAN); //消行數用青藍色突出
			g.drawString(String.valueOf(linesCleared), infox + 20, 145);
			
			//繪製下一顆方塊預覽圖
			g.setColor(Color.WHITE);
			g.drawString("NESXT", infox + 20, 210);
			if(nextPiece !=null) {
				//遍歷 nextPiece 小診列進行預覽所圖繪製
				for(int r=0; r<nextPiece.length;r++) {
					for(int c=0; c<nextPiece[r].length;c++) {
						if(nextPiece[r][c]!=0) {
							//計算預覽圖在右側面板的絕對繪製像素位置
							int nextX = infox + 30 + c* 20; //每個預覽縮小至20像素寬
							int nextY = 240 + r * 20;		//每個預覽縮小至20像素高
							
							g.setColor(COLORS[nextPieceType]); //著色為方塊原本色彩
							g.fillRect(nextX, nextY, 18, 18);  //填滿
							g.setColor(COLORS[nextPieceType].brighter()); //高亮框
							g.drawRect(nextX, nextY, 18, 18);
						}
					}
				}
			}			
		}
		/* *
		 * 3.繪製結算頁面(Game Over)
		 * */
		private void drawGameOver(Graphics2D g) {
			//如果成功設定了遊戲結束背景圖，就畫出來
			if(gameOverBackgroundImage !=null) {
				g.drawImage(gameOverBackgroundImage, 0, 0, getWidth(), getHeight(), this);
				
				// 套用極深，極高濃度半透明遮罩(不透明度 200)營造凝重死局的感覺
				g.setColor(new Color(0,0,0,200));
				g.fillRect(0, 0, getWidth(), getHeight());
			}else {
				//備份機制:如果玩家沒放結束圖，預設塗滿接近全黑的半透明色
				g.setColor(new Color(0,0,0,225));
				g.fillRect(0, 0, getWidth(), getHeight());
			}
			
			// 繪製巨大的紅色 GAME OVER 文字
			g.setFont(new Font("Microsoft JhengHei", Font.BOLD,42));
			g.setColor(Color.RED);
			g.drawString("GAME OVER", getWidth()/2-120, 180);
			
			// 顯示此局最終戰績
			g.setFont(new Font("Microsoft JhengHei", Font.PLAIN,20));
			g.setColor(Color.WHITE);
			g.drawString("最終得分: "+ score, getWidth()/2-60, 260);
			g.drawString("消除行數: "+ linesCleared, getWidth()/2-60, 300);
			
			// 提示重新開始之功能鍵
			g.setFont(new Font("Microsoft JhengHei", Font.PLAIN,16));
			g.setColor(Color.YELLOW);
			g.drawString("按下 SPACE 回到主選單", getWidth()/2-90, 400);
		}

		/* *
		 * 輔助繪圖方法:專門用來繪製帶有 3D 光澤感的方塊格子
		 * @param g 畫筆元件
		 * @param x 格子渲染起點 x座標
		 * @param y 格子渲染起點 Y座標
		 * @param type 方塊色彩類索引 (1 至 7)
		 * */
		private void drawCell(Graphics2D g, int x, int y, int type) {
			//1.填滿中心基礎顏色
			g.setColor(COLORS[type]);
			g.fillRect(x,y,CELL_SIZE,CELL_SIZE);
			//2.繪製最外圍的【高亮邊框】(讓方塊立體部扁平)
			//COLORS[type].brighter()會自動計算出該顏色加亮後的顏色
			g.setColor(COLORS[type].brighter());
			g.drawRect(x, y, CELL_SIZE-1, CELL_SIZE-1);
			//3.繪製內一圈的【陰影邊框】(增加細節與物理凹凸值感
			//COLORS[type].darker() 會自動計算出該顏色變暗、變深沉後的暗部顏色
			g.setColor(COLORS[type].darker());
			g.drawRect(x + 2,y + 2,CELL_SIZE-5, CELL_SIZE-5);
		}
	}
	/*============================
	 * 主程式入口:示範圖片載入並啟動整個遊戲
	 *============================
	 */
	
	public static void main(String[] args) {		
		//使用SwingUtilities 的線程安全列，啟動視窗避免畫面撕裂或線程衝突
		SwingUtilities.invokeLater(()->{
			//實體化俄羅斯方塊主類別
			TetrisPanel game = new TetrisPanel();
			
			//【圖片接口】
			//將要呈現的圖片放進專案目錄下
			/*
			//下方程式就會自動捕捉並呼叫API hooks填滿背景圖。
			try {
				//1. 嘗試載入開始封面
				//使用類別載入起讀取資源
				
				if(menuURL !=null) {
					BufferedImage menuImg = ImageIO.read(menuURL);//讀取圖檔
					game.setMenuBackgroundImage(menuImg);  //寫入並重繪
					System.out.println("[系統]成功載入自訂封面圖");
				}
				
				//2. 嘗試仔入遊戲進行中的背景圖
				
				if(gameplayURL!=null) {
					BufferedImage gameplayImg = ImageIO.read(gameplayURL); //讀取
					game.setGameplayBackgroundImage(gameplayImg) ;//寫入
					System.out.println("[系統]成功載入自訂遊玩背景圖");
				}
				//3. 嘗試載入結算頁面的背景圖
				
				if(gameOverURL!=null) {
					BufferedImage gameOverImg = ImageIO.read(gameOverURL); //讀取
					game.setGameOverBackgroundImage(gameOverImg); //寫入
					System.out.println("[系統]成功載入自訂結算圖!");
				}
				
			}catch(IOException e) {
				//當發生圖片毀損或檔案鎖定異常時，印出訊息，但遊戲依然會以預設的色系開啟
				System.out.println("[系統]部分背景圖片載入失敗，自動啟用幾何背景保護機制。");
			}
			//啟動顯示遊戲主視窗，讓玩家開始遊玩
			*/
			 
			game.setVisible(true);
		});
	}
}


