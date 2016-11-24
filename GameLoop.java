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
import java.net.*;
import java.io.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.text.*;
import javax.swing.border.EtchedBorder;

public class GameLoop extends JPanel implements Runnable{
	DatagramSocket socket = new DatagramSocket();
  private static Socket client;	
		
	JFrame frame= new JFrame();
	JPanel southPanel = new JPanel();
	int xspeed=2,yspeed=2,prevX,prevY, x= 10, y=10;
	Thread t=new Thread(this);
	String pname, name, serverData;
	Camera camera;
	int direction;
	
	String server="";
	boolean connected=false;
	RaceCar myCar;
	Map map;
	BufferedImage mapCopy;

	JTextPane chatbox = new JTextPane();
	JTextField chat= new JTextField();

	static final int WIDTH = 500;
	static final int HEIGHT = 700;

	private final int WAITING = 1;
	private final int GAME_START = 2;
	private final int IN_PROGRESS = 3;

	public GameLoop(String server,String name) throws Exception{
		
		this.server=server;
		this.name=name;
		
		client = new Socket(server, 8080);
		
		OutputStream outToServer = client.getOutputStream();
    DataOutputStream out = new DataOutputStream(outToServer);
		myCar = new RaceCar(name, x,y, "anek");
		frame.setTitle("MadRace: "+name);
		//set some timeout for the socket
		socket.setSoTimeout(100);
		//Some gui stuff i hate.
		frame.setLayout(new BorderLayout());
		frame.getContentPane().add(this, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(WIDTH, HEIGHT);
		frame.setVisible(true);

		southPanel.setLayout(new BorderLayout());
		southPanel.setSize(WIDTH, 200);
		southPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

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
       	}catch(Exception e){
       	}
			}	
		};

		//add chat to southpanel and to frame
		southPanel.add(chatscroll, BorderLayout.NORTH);
		southPanel.add(chat, BorderLayout.SOUTH);
		frame.getContentPane().add(southPanel, BorderLayout.SOUTH);

		//create the buffer
		map = new Map("try.txt", 1200, 1200);
		mapCopy = new BufferedImage(map.width, map.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) mapCopy.getGraphics();
		camera = new Camera(map.startX, map.startY);
		g.setBackground(new Color(255,255,255,255));
		frame.addKeyListener(new KeyHandler());

		t.start();
		listenForMessage.start();
	}

	/**
	 * Helper method for sending data to server
	 * @param msg
	 */
	public void send(String msg){
		try{
			byte[] buf = msg.getBytes();
			InetAddress address = InetAddress.getByName(server);
			DatagramPacket packet = new DatagramPacket(buf, buf.length, address, GameServer.port);
			socket.send(packet);
	  }catch(Exception e){}
	}

	public void run(){
		while(true){
			try{
				Thread.sleep(1);
			}catch(Exception ioe){}

			//Get the data from players
			byte[] buf = new byte[256];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try{
		 		socket.receive(packet);
			}catch(Exception ioe){/*lazy exception handling :)*/}

			serverData=new String(buf);
			serverData=serverData.trim();
			//Study the following kids.
			if (!connected && serverData.startsWith("CONNECTED")){
				connected=true;
				System.out.println("Connected.");
			}else if (!connected){
				System.out.println("Connecting..");
				x = map.startX;
				y = map.startY;
				camera.tick(x,y);
				send("CONNECT "+name + " " + map.startX + " " + map.startY);
			}else if (connected){
				/*if (serverData.startsWith("INITIALPLACES:")){
					String[] playersInfo = serverData.split(":");
					for (int i=0;i<playersInfo.length;i++){
						String[] playerInfo = playersInfo[i].split(" ");
						String pname =playerInfo[1];
						int x = Integer.parseInt(playerInfo[2]);
						int y = Integer.parseInt(playerInfo[3]);
						//draw on the offscreen image
						//mapCopy.getGraphics().fillOval(x, y, 5, 5);
						mapCopy.getGraphics().drawImage(myCar.img, x,y,null);
						mapCopy.getGraphics().drawString(pname,x-10,y+30);
					}
				}*/
				if (serverData.startsWith("PLAYER")){
					String[] playersInfo = serverData.split(":");
					Graphics2D g = (Graphics2D) mapCopy.getGraphics();
					g.setBackground(new Color(255,255,255,0));
					g.clearRect(0,0, map.width, map.height);
					for (int i=0;i<playersInfo.length;i++){
						String[] playerInfo = playersInfo[i].split(" ");
						String pname =playerInfo[1];
						int x = Integer.parseInt(playerInfo[2]);
						int y = Integer.parseInt(playerInfo[3]);
						//draw on the offscreen image
						//mapCopy.getGraphics().fillOval(x, y, 5, 5);
						mapCopy.getGraphics().drawImage(myCar.img, x,y,null);
						mapCopy.getGraphics().drawString(pname,x-10,y+30);
					}
					//show the changes
					frame.repaint();
				}
			}
		}
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
						}
						break; // % 640;break;
				case KeyEvent.VK_W:
						if(map.checkCollision(x,y-yspeed)){
							y -=  yspeed;
							yspeed += yspeed == 5? 0 : 1;
						}
						break;
				case KeyEvent.VK_D:
						if(map.checkCollision(x+xspeed,y)){
							x += xspeed;
							xspeed += xspeed == 5? 0 : 1;
						}
						break;
				case KeyEvent.VK_A:
						if(map.checkCollision(x-xspeed, y)){
							x -=  xspeed;
							xspeed += xspeed == 5? 0 : 1;
						}
						break;
 			case KeyEvent.VK_ESCAPE:
						chat.setFocusable(true);
						chat.requestFocus();
						System.out.println("ESCAPE");
						break;
			}
			if (prevX != x || prevY != y){
				send("PLAYER "+name+" "+x+" "+y);
			}
			camera.tick(x,y);
		}
	}

	/*
	public static void main(String args[]) throws Exception{
		new GameLoop("localhost",JOptionPane.showInputDialog("enter name: "));
	}*/
}
