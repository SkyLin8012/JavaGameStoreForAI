package com.steam.view.game;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

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

/*
 * 俄羅斯方塊主程式類別，繼承自 JFrame(視窗容器)
 * 這代表此遊戲為可顯示在螢幕上的視窗
 * */
public class TetrisGame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	
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
	private Image menuBackgroundImage = null;     //主選單(封面)的底圖
	private Image gameplayBackgroundImage = null; //遊戲進行中的底圖
	private Image gameOverBackgroundImage = null; //結算畫面的底圖
	
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
			{{1,1,1},{1,0,1}}, //索引 3:L 形 (2*3)
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
	public TetrisGame() {
		//設定作業系統視窗標題文字
		setTitle("經典 俄羅斯方塊");
		//設定當使用者按下視窗右上角的【X]時，關閉程式並釋放記憶體		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//禁止使用者手動拖拉改變視窗大小，以免跑版
		setResizable(false);
		
		//建立可客製化繪圖面板 GamePanel(內部類別在程式下方)
		
		
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

	}
	/*============================
	 * 主程式入口:示範圖片載入並啟動整個遊戲
	 *============================
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TetrisGame frame = new TetrisGame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	//================================
	// 內部類別:處理遊戲畫面的蟲會與自訂底圖渲染(繼承自 JPanel 繪圖板)
	//================================
	private class GamePanel extends JPanel{
		/* *
		 * 這是 Swing最核心的繪圖入口
		 * 每次我們呼叫 repaint()時，Java 就會自動來呼叫的方法。 
		 * */
		@Override
		protected void paintComponent(Graphics g) {
			//呼叫父類別的繪圖清理工具，維持畫面清潔
			super.printComponent(g);
			
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
				//備份機制:如國沒有提供圖片，就繪製預設的經典科技感深藍色背景
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
			g.drawString("操作:← →移動 | ↑ 選轉 | ↓ 加速 | 空白鍵瞬間下落", 35, 520);			
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
}


