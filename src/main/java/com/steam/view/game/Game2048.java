package com.steam.view.game;

import java.awt.Color;//顏色類別，設定方塊及背景的RBG顏色
import java.awt.Dimension;//尺寸類別，封裝畫布的寬度與高度像素
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;//影像基礎類別，儲存讀入的圖片資料
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;//事件類別，用以加收定時器刷新的訊號
import java.awt.event.ActionListener;//動作監聽器介面，用於接收並處理ActionEvent
import java.awt.event.KeyAdapter; //鍵盤適配器，用以實作鍵盤介面，挑選需要複寫即可
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.sql.Time;//計時工具
import java.util.ArrayList; //動態陣列集合，用以暫存盤面的空網格
import java.util.Random; //亂數產生器

import javax.swing.Timer; //引入計時工具，用於處理畫面動態重新整理

import com.steam.model.Member;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;//引訊剪輯物件，可把音樂檔反覆播放
import javax.swing.ImageIcon;
import javax.swing.JFrame;//視窗框架元件，用於建立桌面應用程式主視窗
import javax.swing.JPanel; //引入面板元件，用於建立自訂的遊戲畫布
import javax.swing.SwingUtilities; 
//引入執行緒工具，確保圖形介面於安全的事件衍生執行緒中啟動

public class Game2048 extends JFrame {
	
	public Game2048(){
		this.setTitle("Java 2048"); 
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//設定點擊關閉按鈕時完全結束程式並釋放記憶體
		this.setResizable(false);//禁止手動調整視窗大小
		
		GamePanel panel = new GamePanel();
		//實體化核心遊戲畫布
		this.add(panel);//將核心畫布裝入視窗框架
		this.pack();//自動調適外框用以適配畫布面板
		
		this.setLocationRelativeTo(null);//遊戲初始於畫面中央顯示
		this.setVisible(true);//將視窗設為可見，顯示遊戲畫面
		
	}
	
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(()->new Game2048());
		//安全的UI執行續中啟動並建立遊戲視窗
	}
	
}
//定義核心遊戲畫布類別，繼承JPanel 並實作監聽器
class GamePanel extends JPanel implements ActionListener{
	private static final int SCREEN_SIZE=500;//遊戲總高500像素
	private static final int GRID_SIZE=4;//遊戲矩陣為4x4的網格結構
	private static final int TILE_SIZE=100;//定義每個數字方塊寬高維100像素
	private static final int GAP_SIZE =16;//定義方塊與方塊間的間隔為16像素
	
	private enum GameState{//宣告列舉型態，用來定義遊戲經歷的三種狀態
		START,RUNNING,GAME_OVER		
	}
	private GameState state= GameState.START; 
	//宣告一個變數紀錄當前狀態，預設是START
	
	private final int[][] board = new int[GRID_SIZE][GRID_SIZE];
	//建立4x4二維陣列作為遊戲的盤面(0代表空位)
	
	private int score=0;//遊戲分數初始為0
	private boolean gameOver=false; //遊戲失敗狀態標記為false
	private boolean gameWon=false;//勝利狀態初始化為false
	private final Random random = new Random(); //實體化亂數工具
	private Timer timer; //計時器物件，用於刷新遊戲畫面
	private Clip bgmClip; //背景音樂控制物件，可啟動或關閉音樂
	URL title_bg = getClass().getResource("G2048/title_bg.png");
	private final Image titleBg = new ImageIcon(title_bg).getImage();
	//啟動時，載入封面背景圖
	
	public GamePanel() {//建構子，初始化畫布屬性
		//設定此面板的尺寸500*500像素
		this.setPreferredSize(new Dimension(SCREEN_SIZE,SCREEN_SIZE+50));
		//畫布的背景顏色(經典2048土黃)
		this.setBackground(new Color(0xBBADA0));
		//允許此面板獲取鍵盤焦點，用以接收玩家鍵盤輸入
		this.setFocusable(true);
		//掛載自訂的鍵盤監聽器，監控玩家操作
		this.addKeyListener(new MyKeyAdapter());
		timer = new Timer(16,this); 
		//初始化計時器，每秒刷新60次畫面(16毫秒)
		timer.start();//啟動計時器，讓 actionPerformed 迴圈開始執行
	}
	//新局初始化方法
	private void startGame() {
		score=0; //遊戲分數歸零		
		gameWon = false;//重設遊戲狀態:未勝利
		for(int r=0; r<GRID_SIZE;r++) //外迴圈遍歷盤面每一行
		{
		 for(int c=0; c< GRID_SIZE;c++)//內迴圈遍歷盤面每一列 
		 {
			 board[r][c]=0;//將所有16個網格值通通清空為0
		 }
		}
		spawnTile();//隨機投放第一個初始方塊
		spawnTile();//隨機投放第二個初始方塊
		state = GameState.RUNNING; //將遊戲狀態正式切換為進行中
		URL bgm = getClass().getResource("G2048/bgm.wav");
		playBGM(bgm);//開始循環背景音樂
		
	}
	//隨機生成新方塊
	private void spawnTile() {
		ArrayList<int[]> emptyTiles = new ArrayList<>();
		//建立動態陣列，用來儲存所有當前為0的格子座標
		for(int r=0;r<GRID_SIZE;r++)//橫向掃描二維陣列
		{
			for(int c=0;c<GRID_SIZE;c++)//縱向掃描二維陣列
			{
				if(board[r][c]==0)//若該位置的值為0(代表空位)
				{
					emptyTiles.add(new int[] {r,c});
					//將該空位的列索引與行索引以陣列型式存入集合中
				}
			}
		}
		if(!emptyTiles.isEmpty()) {//如果盤面上還有剩餘空格
			int[] tile =emptyTiles.get(random.nextInt(emptyTiles.size()));
			//從所有空位名單中隨機抽中一個位置
			board[tile[0]][tile[1]]=(random.nextInt(10)==0)?4:2;
			//有10%的機率生成數字4，90%機率生成數字2	
		}
	}
	//核心算法:向左滑動與合併邏輯
	private boolean moveLeft() {
		boolean moved = false; //【本輪是否移動】標記為false
		boolean mergedInThisTurn=false;//【本輪是否方生方塊合併】標記為false
		
		for(int r=0;r<GRID_SIZE;r++) //逐行處理4個橫列
		{
			int[]row=board[r]; //取出當前整行的陣列數據
			int[]newRow=new int[GRID_SIZE];
			//建立長度為4的暫存陣列，用來存放靠左對齊後的新數據
			int index=0;//設定暫存陣列的填入索引指標
			//第一步:排除所有0，把有數字方塊通通靠左壓縮
			for(int c=0; c<GRID_SIZE;c++)
			{
				if(board[r][c]!=0) {
					newRow[index++]=board[r][c];
				}
			}
			//第二步:合併相鄰且相同的數字(注意迴圈邊界在GRID_SIZE-1以防越界)
			for(int c=0;c<GRID_SIZE-1;c++) 
			{
			 if(newRow[c]!= 0 && newRow[c]==newRow[c+1])//若目前格子有數字，且與右手邊相鄰的格子數值完全相同
			 {
				newRow[c]*=2;//目前格子的數值翻倍(完全合併)
				score+= newRow[c];//將合併後產生的新數值加進玩家的總分中
				mergedInThisTurn = true;//標記本輪發生合併
				
				if(newRow[c]==2048) {//關鍵判定
					gameWon=true; //若合併出2048直接標記勝利狀態為true
				}
				//合併後，後方所有數字必須往前遞補一格
				for(int i = c+1;i<GRID_SIZE-1;i++)
				{
					newRow[i]=newRow[i+1];//後方格子集體向前挪動
				}
				newRow[GRID_SIZE-1]=0;//挪動完畢後，最後尾端一定會空出一個網格，直接補0
			 }
			}
			//第三步:將計算好的新整行結果，寫回原來的遊戲盤面
			for(int c=0;c<GRID_SIZE;c++)
			{
				if(board[r][c]!=newRow[c]) {//若原盤面數據與合併後數據不一致
					moved=true;//位移狀態為true
				}
				board[r][c]=newRow[c];//將新數據正是覆蓋回主盤面陣列中
			}
		}
		if(mergedInThisTurn) {//任何一對數字合併成功，播放合併音效
			URL soun_merge = getClass().getResource("G2048/merge.wav");
			playSound(soun_merge);
		}
		return moved;//告訴外層本輪滑動是否產生變化
	}
	
	private void rotate() { //矩陣順時針旋轉90度演算法
		int[][] temp= new int[GRID_SIZE][GRID_SIZE];
		//建立一個臨時的4*4 二微陣列存放旋轉後的數據
		for(int r=0;r<GRID_SIZE;r++)
		{ //掃描原盤面的行
			for(int c=0;c<GRID_SIZE;c++)
			{//掃描原盤面的列
				temp[c][GRID_SIZE-1-r]=board[r][c];
				//依據數學矩陣旋轉公式進行座標映射轉換
			}
		}
		for(int r=0;r<GRID_SIZE;r++)
		{//旋轉完成後，將臨時陣列的數據複製回遊戲盤面
			System.arraycopy(temp[r], 0, board[r],0, GRID_SIZE);
			//逐行高效複製記憶體陣列
		}
	}
	
	//用以判定遊戲是否結束
	private void checkGameOver(){
		if(gameWon) { //若達成勝利條件
			state = GameState.GAME_OVER; //將狀態轉為結束
			stopBGM();//關閉背景音樂
			//寫入分數到資料庫
			//將分數載入紀錄
			com.steam.controller.SteamController.getInstance().recordScore(3, score);
			return; //結束整個方法
		}
		//遍歷整個盤面
		for(int r=0;r<GRID_SIZE;r++){ //遍歷行
			for(int c=0;c<GRID_SIZE;c++){//遍歷列 
				if(board[r][c]==0)return; 
				//(1.若盤面上還有任一空位(0)代表還沒輸，直接結束判定
				if(r < GRID_SIZE -1 && board[r][c] == board[r+1][c])return;
				//(2.若垂直相鄰的格子數字相同，尚能合併，結束判定
				if(c < GRID_SIZE -1 && board[r][c] == board[r][c+1])return;
				//(3.若水平相鄰格子有數字相同，尚能合併，結束判定				
			}			
		}
		//若上面的return都沒有觸發，代表已無路可走，正式宣告GAME_OVER!
		state=GameState.GAME_OVER;
		stopBGM();//關閉背景音樂
		//將分數載入紀錄
		com.steam.controller.SteamController.getInstance().recordScore(3, score);
	}
    //將記錄寫入資料庫
    private void insertLog(int source)
    {
    	//載入cookie
  		Member me=null;
  		com.steam.controller.SteamController.getInstance().recordScore(3, score);
  		//me=(Member) Tool.readFile("member.txt");
  		//Log log = new Log(1,me.getUid(),me.getName(),"1","經典貪食蛇",Integer.toString(source),LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
  		//new LogServiceImpl().createLog(log);

    }
	
	
	@Override 
	protected void paintComponent(Graphics g){//Java Swing 繪圖主入口 
		super.paintComponent(g);//呼叫父類別，自動清空畫布背景
		Graphics2D g2d = (Graphics2D)g;
		//將基礎畫筆強轉為2D進階畫筆
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
		//開啟文字消除鋸齒
		
		switch(state) {//狀態機的畫布分流
		  case START:drawStartScreen(g2d);break;
		  //若目前是START狀態，就去執行畫封面的方法
		  case RUNNING: drawBoard(g2d); break;
		  //若目前是RUNNING狀態，就去繪製4*4主要盤面
		  case GAME_OVER: drawGameOverScreen(g2d); break;
		  //若目前是 GAME_OVER狀態，繪製最後計分板
		  default:break;
		}
	}
	//繪製遊戲封面
	private void drawStartScreen(Graphics2D g2d) {
		g2d.drawImage(titleBg, 0, 0, SCREEN_SIZE, SCREEN_SIZE+50, this);
		//在最底層鋪上寬高500*500的封面背景圖
		//1.大標題
		g2d.setColor(new Color(0x776E65));//設定文字顏色為深灰褐色
		g2d.setFont(new Font("Microsoft JhengHei",Font.BOLD,55));
		//設定大標題字體
		FontMetrics metrics1 = g2d.getFontMetrics();//獲取測量工具
		g2d.drawString("2048 遊戲",( SCREEN_SIZE-metrics1.stringWidth("2048 遊戲"))/2,SCREEN_SIZE/3);
		// 畫出置中的大標題
		//2.提示副標題
		g2d.setColor(Color.DARK_GRAY); //將畫筆切換為深灰色
		g2d.setFont(new Font("Microsoft JhengHei",Font.PLAIN,20));
		//設定提示副標題字型
		FontMetrics metrics2 = g2d.getFontMetrics();//獲取測量工具
		g2d.drawString("按 [ ENTER ] 開始挑戰", ( SCREEN_SIZE-metrics2.stringWidth("按 [ ENTER ] 開始挑戰"))/2,SCREEN_SIZE/2+20);
		//畫出開始提示
		//操作指南
		g2d.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16)); 
		FontMetrics metrics3 =g2d.getFontMetrics();//獲取大小為 16 的測量工具
		// 切換為更小的字體
        g2d.drawString("使用 方向鍵 或 WASD 控制移動與合併", (SCREEN_SIZE - metrics3.stringWidth("使用 方向鍵 或 WASD 控制移動與合併")) / 2 + 15, SCREEN_SIZE / 2 + 80); 
        // 畫出操作指南
		
		
	}
	
	//繪製主盤面數據
	private void drawBoard(Graphics2D g2d) {
		g2d.setColor(new Color(0x776E65));//設定文字顏色為深灰褐色
		g2d.setFont(new Font("Microsoft JhengHei",Font.BOLD,24));
		//設定分數文字字型
		g2d.drawString("Score:"+ score, GRID_SIZE, 35);
		//在左上角印出目前的累計得分
		int startY=60; //將方塊網格起點Y軸訂在60像素處
		
		for(int r=0;r<GRID_SIZE;r++) { //控制列的雙層迴圈
			for(int c=0;c<GRID_SIZE;c++) {//控制行的雙層迴圈
				int value = board[r][c];//獲取數值
				int x=GAP_SIZE+c*(TILE_SIZE + GAP_SIZE);
				//計算方塊X座標
				int y = startY + GAP_SIZE+r*(TILE_SIZE+GAP_SIZE);
				//計算方塊Y座標
				
				g2d.setColor(getTileBackground(value));//動態獲取方塊背景色
				g2d.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, 8, 8);//繪製圓角方塊
				
				if(value >0) { //如果格子有數字
					g2d.setColor(getTileForeground(value));
					//動態決定字體顏色
					g2d.setFont(getTitleFont(value));
					//動態調整字體大小防止爆框
					
					FontMetrics fm=g2d.getFontMetrics();
					//獲取測量工具
					String s = String.valueOf(value);
					int textX= x+(TILE_SIZE-fm.stringWidth(s))/2;
					//置中X計算
					int textY = y+(TILE_SIZE-fm.getHeight())/2+fm.getAscent();
					//置中Y計算
					
					g2d.drawString(s, textX, textY);//在方塊內部中央寫上數字
				}				
			}
		}
	}
	//繪製最後計分板畫面的方法
	private void drawGameOverScreen(Graphics2D g2d) {
		drawBoard(g2d);//先在底層，將死掉時的最後方塊布局給繪製出來
		
		g2d.setColor(new Color(255,255,255,195));
		//設定半透明白色
		g2d.fillRect(0, 0, SCREEN_SIZE, SCREEN_SIZE+50);
		//塗滿整個畫布，形成霧白色的記分板底色
		
		g2d.setColor(new Color(0x776E65)); //設定文字顏色為深灰褐色
		g2d.setFont(new Font("Microsoft JhengHei",Font.BOLD,50));
		//設定結束時，大標題的字型
		
		String msg = gameWon? "達成2048勝利!":"遊戲結束";//依據是不是贏了，決定要印出設立還是結束
		FontMetrics fm1 = g2d.getFontMetrics();//獲取大字測量工具
		g2d.drawString(msg,(SCREEN_SIZE-fm1.stringWidth(msg))/2, SCREEN_SIZE/3); 
		//置中寫出結果狀態
		
		g2d.setFont(new Font("Microsoft JhengHei",Font.BOLD,28));
		//切換為普通說明小字
		
		String scoMsg ="分數:"+score;
		FontMetrics fm2 = g2d.getFontMetrics();//獲取小字測量工具
		g2d.drawString(scoMsg,(SCREEN_SIZE-fm2.stringWidth(scoMsg))/2, SCREEN_SIZE/2);
		String subMsg ="按[ENTER]重新挑戰";
		FontMetrics fm3 = g2d.getFontMetrics();//獲取小字測量工具
		g2d.drawString(subMsg,(SCREEN_SIZE-fm3.stringWidth(subMsg))/2, SCREEN_SIZE/2+80);
		//畫出新的引導文字		 				
	}
	//依據方塊數值回傳背景顏色對照表
	private Color getTileBackground(int value) {
		switch(value) {
		case  0  : return new Color(0xCDC1B4);
		case  2  : return new Color(0xEEE4DA);
		case  4  : return new Color(0xEDE0C8);
		case  8  : return new Color(0xF2B179);
		case 16  : return new Color(0xF59563);
		case 32  : return new Color(0xF67C5f);
		case 64  : return new Color(0xF65E3B);
		case 128 : return new Color(0xEDCF72);
		case 256 : return new Color(0xEDCC61);
		case 512 : return new Color(0xEDC53F);
		case 1024: return new Color(0xEDC53F);
		default  : return new Color(0x3C3A32);
		}
		
	}
	//依據方塊數值決定字體顏色
	private Color getTileForeground(int value) {		
		return (value <=4)? new Color(0x776E65):Color.WHITE;
	}
	//依據數字大小動態調整字型尺寸以防爆框
	private Font getTitleFont(int value) {
		if(value<128) {
			return new Font("Consolas",Font.BOLD,42);
		}else if(value < 1024) {
			return new Font("Consolas",Font.BOLD,36);
		}else {
			return new Font("Consolas",Font.BOLD,28);
		}		
	}
	
	private void playSound(URL soundFile) {
		try {
			//File soundFile=new File(soundFileName);
			if(soundFile!=null) {
				AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
				//開啟數位音訊流
				Clip clip = AudioSystem.getClip();
				//申請一個播放剪輯物件
				clip.open(audioStream);//載入音訊
				clip.start(); //播放一次
			}
		}catch(Exception e){
			e.printStackTrace();//如果載入失敗，在主控台印處錯誤軌跡
		}
	}
	//循環播放背景音樂的核心方式
	private void playBGM(URL soundFile) {
		try {
			stopBGM();//先安全關閉上一首可能正在播放音樂，防止堆疊卡死
			//File soundFile = new File(soundFileName);
			//建立檔案指標
			if(soundFile!=null) {//若檔案存在的話
				AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
				//讀取音訊資料串流
				bgmClip = AudioSystem.getClip();
				//實體化播放控制權
				bgmClip.open(audioStream); //載入音樂
				bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
				//設定無限循環播放				
			}
		}catch(Exception e) {
			e.printStackTrace();
		}		
	}
	//安全的關閉背景音樂方法
	private void stopBGM() {
		//如果音樂控制物件不為空且正在播放
		if(bgmClip != null && bgmClip.isRunning()) {
			bgmClip.stop();//停止音樂
			bgmClip.close();//播放音樂所占用的系統資源
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	//定時器每16毫秒自動呼叫核心更新入口
		repaint();//不間斷強迫Java刷新畫面，以保證狀態切換與音效流暢載入
	}
	//內嵌入類別，用以接收玩家鍵盤輸入
	private class MyKeyAdapter extends KeyAdapter{
		@Override
		public void keyPressed(KeyEvent e) {//當按鍵按下去時
			int keyCode = e.getKeyCode(); //取得按鍵程式碼
			if(keyCode==KeyEvent.VK_ENTER) { //如果按下Enter鍵
				if(state==GameState.START||state==GameState.GAME_OVER)
				{//若目前為【封面】或【結算】狀態
					startGame(); //啟動新一局遊戲
					return;//結束方法
				}
			}
			
			if(state != GameState.RUNNING)return; 
			//安全檢查:如果目前不是在RUNNING
			boolean moved=false; //初始化位移標記
			
			switch(keyCode) { //使用經典矩陣旋轉法分流處理上下左右
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_A:
					moved = moveLeft(); //向左滑動不需要旋轉
					break;
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_S:
					rotate(); //下轉左:轉1次
					moved = moveLeft(); //左滑
					rotate(); rotate(); rotate(); //轉回原位:再轉3次
					break;
				case KeyEvent.VK_RIGHT:
				case KeyEvent.VK_D:
					rotate(); rotate(); //右轉左:轉2次
					moved = moveLeft(); //左滑
					rotate(); rotate(); //轉回原位:再轉2次
					break;
				case KeyEvent.VK_UP:
				case KeyEvent.VK_W:
					rotate(); rotate(); rotate();//上轉左:轉3次
					moved=moveLeft();//左滑
					rotate();//轉回原位:再轉1次
					break;
				default:
					break;
			}
			
			if(moved) {//如果這一滑造成圖面變動
				spawnTile(); //投第一個的數字2或4
				
			}
			checkGameOver(); //馬上檢查是不是被塞滿而GAME OVER 或達成 2048獲勝
		}
	}
}
