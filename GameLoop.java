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

public class GameLoop extends JPanel implements Runnable{
	DatagramSocket socket = new DatagramSocket();
	private static Socket client;

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
	int angle=180;

	String server="";
	boolean connected=false;
	boolean start=false;
	Map map;
	BufferedImage mapCopy;

	ArrayList plist;
	int playerCount=0;
	JTextPane chatbox = new JTextPane();
	JTextField chat= new JTextField();
	JProgressBar healthBar = new JProgressBar();
	Timer timer;
	JLabel ammoLabel;
	JLabel placeLabel;

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

		myCar = new RaceCar(name, x,y, playertype);
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

		timer = new Timer(50, new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int val = healthBar.getValue();
				//full health
				if(myCar.getHealth()==100){
					if (val >= 100) {
						timer.stop();
					}
					healthBar.setValue(++val);
				}else{	//with damage
					if (val <= myCar.getHealth()) {
						timer.stop();
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
		ammoLabel = new JLabel(Integer.toString(myCar.getAmmo()) +" / 50", JLabel.CENTER);
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
		map = new Map("try.txt", 500, 550);
		mapCopy = new BufferedImage(map.width, map.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) mapCopy.getGraphics();
		camera = new Camera(map.startX, map.startY);
		g.setBackground(new Color(255,255,255,255));
		frame.addKeyListener(new KeyHandler());

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
			HashMap<String, RaceCar> racecars = null;
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
			}catch(Exception e){}

			if (!connected && serverData.startsWith("CONNECTED")){
				connected=true;
				myCar.gameStage = 2;
				send();
				System.out.println("Connected.");
			}else if (!connected){
				// System.out.println("Connecting..");
				x = map.startX;
				y = map.startY;
				myCar.setX(x);
				myCar.setY(y);
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
				}else if (serverData.startsWith("START")){
					start = true;
					myCar.gameStage = 3;
					send();
				}
				else if (serverData.startsWith("PLAYER") && racecars != null){
					Graphics2D g = (Graphics2D) mapCopy.getGraphics();
					g.setBackground(new Color(255,255,255,0));
					g.clearRect(0,0, map.width, map.height);

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
					}
					//show the changes
					frame.repaint();
				}
			}
		}
	}


	public String getPlayer(int pcount){
		return plist.get(pcount).toString();
	}

	/**
	 * Repainting method
	 */
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D) g;
		g2d.translate(camera.x, camera.y);
		if(map != null && map.mapImage != null)
			g.drawImage(map.mapImage, 0,0,null);
		if(mapCopy != null)
			g.drawImage(mapCopy, 0, 0, null);
		g2d.translate(-camera.x, -camera.y);
	}


	class KeyHandler extends KeyAdapter{
		public void keyReleased(KeyEvent ke){
		   switch (ke.getKeyCode()){
			 case KeyEvent.VK_S:yspeed = 2;break;
			 case KeyEvent.VK_W:yspeed = 2;break;
			 case KeyEvent.VK_D:xspeed = 2; break;// % 640;break;
			 case KeyEvent.VK_A:xspeed = 2; break;// % 640;break;
		   }
		 }

		public void keyPressed(KeyEvent ke){
			prevX=x;prevY=y;
			switch (ke.getKeyCode()){
				case KeyEvent.VK_S:
						if(map.checkCollision(x,y+yspeed)){
							y += yspeed; 
							yspeed += yspeed == 5? 0 : 1;
							angle = 180;
						}
						break; // % 640;break;
				case KeyEvent.VK_W:
						if(map.checkCollision(x,y-yspeed)){
							y -=  yspeed;
							yspeed += yspeed == 5? 0 : 1;
							angle = 0;
						}
						break;
				case KeyEvent.VK_D:
						if(map.checkCollision(x+xspeed,y)){
							x += xspeed;
							xspeed += xspeed == 5? 0 : 1;
							angle = 90;
						}
						break;
				case KeyEvent.VK_A:
						if(map.checkCollision(x-xspeed, y)){
							x -=  xspeed;
							xspeed += xspeed == 5? 0 : 1;
							angle = 270;
						}
						break;
				case KeyEvent.VK_ESCAPE:
						chat.setFocusable(true);
						chat.requestFocus();
						break;
			}
			if ((prevX != x || prevY != y) && (myCar.gameStage==3)){
				
				myCar.setX(x);
				myCar.setY(y);
				myCar.setAngle(angle);
				myCar.message = "PLAYER ";
				send();
			}
			camera.tick(x,y);
		}
	}
}