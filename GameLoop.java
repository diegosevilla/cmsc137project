import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.*;
import java.net.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.text.*;
import javax.swing.border.EtchedBorder;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.Rectangle;
import javax.imageio.ImageIO;

public class GameLoop extends JPanel implements Runnable{
	DatagramSocket socket = new DatagramSocket();
	private static Socket client;

	private boolean[] keys = new boolean[256];
	JFrame frame= new JFrame();
	JPanel bottomPanel = new JPanel();
	JPanel southPanel = new JPanel();
	JPanel statsPanel = new JPanel();
	int xspeed=2,yspeed=2,prevX,prevY, x= 10, y=10;
	Thread t=new Thread(this);
	String name, serverData="";
	Camera camera;
	int direction;

	RaceCar myCar;
	int angle=90;

	String server="";
	boolean connected=false;
	boolean start=false;
	Map map;
	BufferedImage mapCopy;
	HashMap<String, RaceCar> racecars = null;

	ArrayList plist;
	int playerCount=0;
	JTextPane chatbox = new JTextPane();
	JTextField chat= new JTextField();
	JProgressBar healthBar = new JProgressBar();
	Timer timer;
	JLabel ammoLabel;
	JLabel placeLabel;

	// three image makes one bomb
	BufferedImage image1, image2, image3 = null;
	Vector<Bomb> bombs = new Vector<Bomb>();

	//bullet images
	BufferedImage bullet1, bullet2 = null;


	static final int WIDTH = 500;
	static final int HEIGHT = 700;

	private final int WAITING = 1;
	private final int GAME_START = 2;
	private final int IN_PROGRESS = 3;

	public GameLoop(String server,String name, String playertype) throws Exception{
		plist = new ArrayList();

		this.server=server;
		this.name=name;

		client = new Socket(server, 8080);

		OutputStream outToServer = client.getOutputStream();
		DataOutputStream out = new DataOutputStream(outToServer);

		myCar = new RaceCar(name, playertype);
		myCar.gameStage = 1;

		frame.setTitle("MadRace: "+name);
		//set some timeout for the socket
		socket.setSoTimeout(100);
		//Some gui stuff i hate.
		frame.setLayout(new BorderLayout());
		frame.getContentPane().add(this, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(WIDTH, HEIGHT);
		frame.setVisible(true);

		bottomPanel.setLayout(new BorderLayout());

		southPanel.setLayout(new BorderLayout());
		southPanel.setSize(300, 200);
		southPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
		statsPanel.setSize(200, 200);
		statsPanel.setMaximumSize(new Dimension(200, 200));
		statsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		//chat box
		JScrollPane chatscroll = new JScrollPane(chatbox);
		chatbox.setPreferredSize(new Dimension(50, 100));
		chatbox.setEditable(false);
		chatbox.setFocusable(false);
		chatbox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		StyledDocument doc = chatbox.getStyledDocument();
		Style style = chatbox.addStyle("I'm a Style", null);
		StyleConstants.setForeground(style, Color.GRAY);
		StyleConstants.setBold(style, true);

		//new message
		chat.setPreferredSize(new Dimension(50, 40));
		chat.setFocusable(false);
		chat.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

		//ENTER KeyEvent of JTextField chat
		chat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chat.setFocusable(false);
				try {
					out.writeUTF(name+": "+chat.getText() + "\n");
					out.flush();
				}catch (Exception err){}
				chat.setText("");
				frame.requestFocus();
			}
		});

		Thread listenForMessage = new Thread(){
			public void run(){
				try{
					InputStream inFromServer = client.getInputStream();
					DataInputStream in = new DataInputStream(inFromServer);
					String msg = "";
					while(true){
						msg = in.readUTF();
						doc.insertString(doc.getLength(), msg,style);
					}
				}catch(Exception e){}
			}
		};

		//health bar
		healthBar.setMaximumSize(new Dimension(100, 20));
		healthBar.setMinimumSize(new Dimension(100, 20));
		healthBar.setPreferredSize(new Dimension(100, 20));
		healthBar.setAlignmentX(0f);

		timer = new Timer(100, new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int val = healthBar.getValue();
				//full health
				if(myCar.getHealth()==100){
					if (val >= 100) {
						timer.stop();
					}
					healthBar.setValue(++val);
				}else{	//with damage
					System.out.println("HEalthbar "+val + " " + myCar.getHealth());
					if (val <= myCar.getHealth()) {
						return;
					}
					healthBar.setValue(--val);
				}
			}
		});

		timer.start();	//NOOOOOOOOOTE: called whenever damage is inflicted

		JLabel healthLabel = new JLabel("Health:", JLabel.CENTER);
		healthLabel.setFont(new Font("Serif", Font.BOLD, 15));
		JLabel ammolbl = new JLabel("Ammo:", JLabel.CENTER);
		ammolbl.setFont(new Font("Serif", Font.BOLD, 15));
		ammoLabel = new JLabel(Integer.toString(myCar.getAmmo()) +" / " + myCar.getAmmoLimit(), JLabel.CENTER);
		ammoLabel.setFont(new Font("Serif", Font.BOLD, 25));
		JLabel placelbl = new JLabel("Rank:", JLabel.CENTER);
		placelbl.setFont(new Font("Serif", Font.BOLD, 15));
		placeLabel = new JLabel(Integer.toString(myCar.getPlace()), JLabel.CENTER);
		placeLabel.setFont(new Font("Serif", Font.BOLD, 25));

		statsPanel.add(healthLabel);
		statsPanel.add(healthBar);
		statsPanel.add(ammolbl);
		statsPanel.add(ammoLabel);
		statsPanel.add(placelbl);
		statsPanel.add(placeLabel);

		//add southpanel(west) and statspanel(east) to bottompanel
		southPanel.add(chatscroll, BorderLayout.NORTH);
		southPanel.add(chat, BorderLayout.SOUTH);
		bottomPanel.add(southPanel, BorderLayout.CENTER);
		bottomPanel.add(statsPanel, BorderLayout.EAST);
		frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		// frame.getContentPane().add(statsPanel, BorderLayout.SOUTH);

		//create the buffer
		map = new Map("map-config.txt", 550, 550);
		mapCopy = new BufferedImage(map.width, map.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) mapCopy.getGraphics();
		camera = new Camera(map.startX, map.startY);
		g.setBackground(new Color(255,255,255,255));
		frame.addKeyListener(new KeyHandler());

		//load bullet images
		bullet1 = ImageIO.read(new File("piks/gbullet.png"));	//gun bullet
		bullet2 = ImageIO.read(new File("piks/rbullet.png"));	//rocket launcher bullet

		//load bomb images
		image1 = ImageIO.read(new File("piks/bomb_1.gif"));
		image2 = ImageIO.read(new File("piks/bomb_2.gif"));
		image3 = ImageIO.read(new File("piks/bomb_3.gif"));
		// create a bomb
		Bomb newbomb = new Bomb(myCar.getX(), myCar.getY());
		bombs.add(newbomb);

		t.start();
		listenForMessage.start();
	}

	public void send(){
		try{
			InetAddress address = InetAddress.getByName(server);
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(5000);
			ObjectOutputStream os = new ObjectOutputStream(byteStream);
			os.flush();
			os.writeObject(myCar);	//convert object
			os.flush();
			//retrieves byte array
			byte[] sendBuf = byteStream.toByteArray();
			DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address, GameServer.port);
			socket.send(packet);
		}catch(Exception e){}
	}

	public void run(){
		while(true){
			try{
				Thread.sleep(1);
			}catch(Exception ioe){}

			//Get the data from players
			byte[] buf = new byte[5000];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try{
				socket.receive(packet);
				ByteArrayInputStream byteStream = new ByteArrayInputStream(buf);
				ObjectInputStream is = new ObjectInputStream(byteStream);
				racecars = (HashMap<String, RaceCar>)is.readObject();
			}catch(Exception ioe){ }

			//get server message stored in race car
			try{
				int temp=0;
				for(String key : racecars.keySet()){
					if(temp==0){
						serverData = racecars.get(key).message;
						temp++;
					}
				}
			}catch(Exception e){
			}

			if (!connected && serverData.startsWith("CONNECTED")){
				connected=true;
				myCar.gameStage = 2;
				send();
				System.out.println("Connected.");
			}else if (!connected){
				// System.out.println("Connecting..");
				// x = map.startX;
	 			// y = map.startY;
				// myCar.setX(x);
				// myCar.setY(y);
				send();
			}else if (connected){
				//player names
				if (serverData.startsWith("NAMES")){
					String[] playersInfo = serverData.split(" ");
					int tempCount = Integer.parseInt(playersInfo[1]);
					if(tempCount>playerCount){
						for (int i=(2+playerCount);i<playersInfo.length;i++){
							plist.add(playersInfo[i]);
						}
						playerCount = tempCount;
					}
					for(int i = 0 ; i < plist.size() ; i++){
						if(plist.get(i).equals(myCar.getName())){
							x = map.startX + myCar.getImage().getWidth() * (i / 3) + 5;
				 			y = map.startY + myCar.getImage().getHeight()* (i % 3) + 5;
							myCar.setX(x);
							myCar.setY(y);
						}
					}
					send();
				}else if (serverData.startsWith("START")){
					start = true;
					myCar.gameStage = 3;
					send();
				}
				else if (serverData.startsWith("PLAYER") && racecars != null){
					if(!start) continue;
					Graphics2D g = (Graphics2D) mapCopy.getGraphics();
					g.setBackground(new Color(255,255,255,0));
					g.clearRect(0,0, map.width, map.height);

					if(racecars.size() == 1)
						new YouWinWindow(frame);
					for(String key : racecars.keySet()){
						RaceCar playerInfo= (RaceCar)racecars.get(key);
						playerInfo.setImage(playerInfo.getPlayerType());
						int x = playerInfo.getX();
						int y = playerInfo.getY();
						int pAngle = playerInfo.getAngle();
						// Rotation
						double rotationRequired = Math.toRadians (pAngle);
						double locationX = playerInfo.getImage().getWidth() / 2;
						double locationY = playerInfo.getImage().getHeight() / 2;
						AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
						AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

						// Drawing the rotated image
						mapCopy.getGraphics().drawImage(op.filter(playerInfo.getImage(), null), x,y,null);
						mapCopy.getGraphics().drawString(playerInfo.getName(),x-10,y+30);

						//Draw fired bullets
						for (int i = 0; i < playerInfo.bullets.size(); i++) {
							Bullet b = playerInfo.bullets.get(i);
							BufferedImage img = playerInfo.getPlayerType().equals("launcha")? bullet2 : bullet1;

							double rotationRequired2 = Math.toRadians (pAngle);
							double locationX2 = img.getWidth() / 2;
							double locationY2 = img.getHeight() / 2;
							AffineTransform tx2 = AffineTransform.getRotateInstance(rotationRequired2, locationX2, locationY2);
							AffineTransformOp op2 = new AffineTransformOp(tx2, AffineTransformOp.TYPE_BILINEAR);

							img = op2.filter(img, null);
							mapCopy.getGraphics().drawImage(img, b.x, b.y, null);
							if(!(playerInfo.getName().equals(myCar.getName())) && b.collide(myCar)){
								playerInfo.bullets.remove(i);
								i--;
								continue;
							}

							b.minusLen();
							if (b.len == 0) {
								playerInfo.bullets.remove(i);
								i--;
							}
						}

						if(myCar.getName().equals(playerInfo.getName())){
							myCar.bullets = playerInfo.bullets;
							timer.start();
						}

						//check car collisions
						if(!(key.equals(myCar.getName())) && (x>20 && y>20)) {
							try{
								RaceCar temp = ((RaceCar)racecars.get(myCar.getName()));
								int x1 = temp.getX();
								int y1 = temp.getY();
								int h = temp.getImage().getHeight();
								int w = temp.getImage().getWidth();
								int h2 = playerInfo.getImage().getHeight();
								int w2 = playerInfo.getImage().getWidth();
								int x2 = playerInfo.getX();
								int y2 = playerInfo.getY();
								Rectangle r1 = new Rectangle(x1,y1,w,h);
								Rectangle r2 = new Rectangle(x2,y2,w2,h2);

								if(checkIntersection(r1, r2)){
									if(myCar.getPlayerType()=="ramma")
										myCar.damage(1);
									else if(playerInfo.getPlayerType().equals("ramma"))
										myCar.damage(5);
									System.out.println(myCar.getHealth());
								}
							}catch(Exception bd){}
						}
						if(myCar.getHealth() <= 0)
								new GameOverWindow(frame);
					}
					//show the changes
					frame.repaint();
				} else if(serverData.startsWith("GameOver")){
						String data[] = serverData.split(" ");
						System.out.println("winner " + data[1]);
						frame.setVisible(false);
						if(data[1].equals(myCar.getName())) {
							new YouWinWindow(frame);
						}
						else {
							new GameOverWindow(frame);
						}
						break;
				}
			}
		}
		start = false;
	}

	public boolean checkIntersection(Rectangle r1, Rectangle r2){
	    return r1.x < r2.x + r2.width && r1.x + r1.width > r2.x && r1.y < r2.y + r2.height && r1.y + r1.height > r2.y;
	}


	public String getPlayer(int pcount){
		return plist.get(pcount).toString();
	}

	/*Repainting method*/
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D) g;

		g2d.translate(camera.x, camera.y);
			if(map != null && map.mapImage != null)
				g.drawImage(map.mapImage, 0,0,null);
			if(mapCopy != null)
				g.drawImage(mapCopy, 0, 0, null);

		g2d.translate(-camera.x, -camera.y);
	}

	class KeyHandler implements KeyListener{
		public void keyTyped(KeyEvent ke) {}

		public void keyReleased(KeyEvent ke){
		  keys[ke.getKeyCode()] = false;
			if(ke.getKeyCode() == KeyEvent.VK_W || ke.getKeyCode() == KeyEvent.VK_S)
			 		yspeed = 2;
			if(ke.getKeyCode() == KeyEvent.VK_A || ke.getKeyCode() == KeyEvent.VK_D)
					xspeed = 2;
		}

		public void keyPressed(KeyEvent ke){
			if(!start) return;
			keys[ke.getKeyCode()] = true;
			update();
			if(ke.isShiftDown()){
				if(myCar.getPlayerType()!="ramma" && myCar.getAmmo() != 0){
					BufferedImage img = myCar.getPlayerType().equals("launcha")? bullet2 : bullet1;
					Bullet newbullet = new Bullet(myCar.getX(), myCar.getY(), img, myCar.getAngle());
					myCar.bullets.add(newbullet);
					myCar.setAmmo(myCar.getAmmo() - 1);
					ammoLabel.setText(Integer.toString(myCar.getAmmo()) +" / "+myCar.getAmmoLimit());
				}
			}
			if (myCar.gameStage==3){
				if(x!=10 && y != 10){
					myCar.setX(x);
					myCar.setY(y);
				}
				myCar.setAngle(angle);
				myCar.message = "PLAYER ";

				if(map.checkWin(x,y, angle))
					myCar.message = "GameOver ";
				send();
			}
			camera.tick(x,y);
		}

		public void update(){
			prevX=x;prevY=y;
			if(keys[KeyEvent.VK_S]){
				if(map.checkCollision(x,y+yspeed, KeyEvent.VK_S)){
					y += yspeed;
					yspeed += yspeed == 8? 0 : 1;
					angle = 180;
				}
			}
			if(keys[KeyEvent.VK_W]){
				if(map.checkCollision(x,y-yspeed,KeyEvent.VK_W)){
					y -= yspeed;
					yspeed += yspeed == 8? 0 : 1;
					angle = 0;
				}
			}
			if(keys[KeyEvent.VK_D]){
				if(map.checkCollision(x+xspeed,y,KeyEvent.VK_D)){
					x += xspeed;
					xspeed += xspeed == 8? 0 : 1;
					angle = 90;
				}
			}
			if(keys[KeyEvent.VK_A]){
				if(map.checkCollision(x-xspeed, y, KeyEvent.VK_A)){
					x -= xspeed;
					xspeed += xspeed == 8? 0 : 1;
					angle = 270;
				}
			}
			if(keys[KeyEvent.VK_ESCAPE]){
					chat.setFocusable(true);
					chat.requestFocus();
					keys[KeyEvent.VK_ESCAPE] = false;
			}
		}
	}
}

class Bomb {
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getLife() {
		return life;
	}

	public void setLife(int life) {
		this.life = life;
	}

	public boolean isLive() {
		return isLive;
	}

	public void setLive(boolean isLive) {
		this.isLive = isLive;
	}

	int x, y;
	int life = 9;
	boolean isLive = true;

	public Bomb(int x, int y) {
		this.x = x;
		this.y = y;
		this.life = 9;
	}

	public void lifeDown() {
		if (life > 0)
			life--;
		else
			this.isLive = false;
	}
}

class Bullet implements Serializable{
	transient BufferedImage img;
	int x, y, direction;
	int len;	//distance to be traveled by bullet
	boolean isLive = true;	//still not colliding with wall or cars

	public Bullet(int x, int y, BufferedImage img, int angle) {
		this.x = x+5;
		this.y = y+5;
		this.len = 10;

		double rotationRequired = Math.toRadians (angle);
		double locationX = img.getWidth() / 2;
		double locationY = img.getHeight() / 2;
		AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

		this.img = op.filter(img, null);

		switch(angle){
			case 0: this.direction = 3; break;
			case 90: this.direction = 2; break;
			case 180: this.direction = 1; break;
			case 270: this.direction = 4; break;
		}
	}

	public boolean collide(RaceCar myCar){
		int h = myCar.getImage().getWidth();
		int w = myCar.getImage().getHeight();

		if(this.x >= myCar.getX() && this.x <= myCar.getX()+w){
			myCar.damage(1);
			return true;
		} else if(this.y >= myCar.getY() && this.y <= myCar.getY()+h){
			myCar.damage(1);
			return true;
		}
		else return false;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getLen() {
		return len;
	}

	public void minusLen() {
		this.len = this.len-1;
		switch(direction){
			case 1: this.y += 20; break;
			case 2: this.x += 20; break;
			case 3: this.y -= 20; break;
			case 4: this.x -= 20; break;
		}
	}

	public boolean isLive() {
		return isLive;
	}

	public void setLive(boolean isLive) {
		this.isLive = isLive;
	}

	public void setImg(BufferedImage img) {
		this.img = img;
	}
}
