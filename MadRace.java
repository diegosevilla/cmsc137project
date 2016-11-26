import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.EtchedBorder;
import javax.imageio.ImageIO;
import java.io.IOException;
import javax.swing.text.*;


public class MadRace  implements Runnable{
	//Create and set up the window.
	static JFrame mainFrame = new JFrame("MadRace BETA");
	static Container mainC = mainFrame.getContentPane();
	static Container mainMenu = new Container();
	static Container howTo = new Container();
	static Container startPanel = new Container();
	static Container lobbyPanel = new Container();
	static int htpCount = 1;	//for traversing "Help" images

	static String name;
	static JTextField getName = new JTextField();
	static JComboBox ptypebox = null;	//player type
	static JLabel nameLabel;

	static int playertype=1;	//type of players
	static int pnum;	//number of players
	static int pcount = 0;	//number of players
	static JTextPane plist = new JTextPane();	//player list

	static String server;
	static boolean isServer;
	static GameServer gameServer;
	static GameLoop gameLoop;

	static StyledDocument doc;	//add player
	static Style style;

	private static void assembleLaunchUI() {
		mainFrame.setResizable(false);

		JPanel panel = new JPanel();
		ImagePanel menu = new ImagePanel(new ImageIcon("piks/MadRace.jpg").getImage());

		mainMenu.setLayout(new BorderLayout());
		mainMenu.setPreferredSize(new Dimension(500, 550));
		mainMenu.add(menu, BorderLayout.CENTER);

		JButton newGameButton = new JButton("Start Game");
		newGameButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				mainMenu.setVisible(false);
				startPanel.setVisible(true);
				howTo.setVisible(false);
				lobbyPanel.setVisible(false);
			}
		});
		panel.add(newGameButton);

		JButton helpButton = new JButton("Help");
		panel.add(helpButton);
		helpButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				htpCount = 1;
				mainMenu.setVisible(false);
				howTo.setVisible(true);
				startPanel.setVisible(false);
				lobbyPanel.setVisible(false);
			}
		});

		JButton quitButton = new JButton("Quit");
		panel.add(quitButton);
		quitButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.exit(0);
			}
		});

		mainMenu.add(panel, BorderLayout.SOUTH);
		mainC.add(mainMenu);
	}

	public static void startGame(){
		CardLayout cardLayout = new CardLayout();
		JPanel panel = new JPanel();
		JPanel choosePlayer = new JPanel();
		JPanel cardPanel = new JPanel();
		JLabel nameLabel;		//player name
		JLabel typeLabel;		//player type
		cardPanel.setLayout(cardLayout);

		ImagePanel htp1 = new ImagePanel(new ImageIcon("piks/clauncha.jpg").getImage());
		ImagePanel htp2 = new ImagePanel(new ImageIcon("piks/cramma.jpg").getImage());
		ImagePanel htp3 = new ImagePanel(new ImageIcon("piks/cgunna.jpg").getImage());

		cardPanel.add(htp1, "1");
        cardPanel.add(htp2, "2");
        cardPanel.add(htp3, "3");

		startPanel.setLayout(new BorderLayout());
		startPanel.setPreferredSize(new Dimension(500, 550));
		startPanel.add(cardPanel, BorderLayout.CENTER);

		//get player name
		getName.setPreferredSize(new Dimension(120, 25));
		getName.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		nameLabel = new JLabel("Player Name: ", JLabel.CENTER);
		choosePlayer.add(nameLabel);
		choosePlayer.add(getName);

		//get player type
		String[] ptype = {"LAUNCHA", "RAMMA", "GUNNA"};
		ptypebox = new JComboBox<String>(ptype);
		typeLabel = new JLabel("Player Type: ", JLabel.CENTER);
		choosePlayer.add(typeLabel);
		choosePlayer.add(ptypebox);
		ptypebox.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent itemEvent){
				if(ptypebox.getSelectedItem().toString() == "LAUNCHA"){
					cardLayout.show(cardPanel, "1");
				}else if(ptypebox.getSelectedItem().toString() == "RAMMA"){
					cardLayout.show(cardPanel, "2");
				}else if(ptypebox.getSelectedItem().toString() == "GUNNA"){
					cardLayout.show(cardPanel, "3");
				}
			}
		});

		//bottom panel buttons
		JButton startButton = new JButton("New Game");
		panel.add(startButton);
		startButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try{
					name = getName.getText();
					server = "localhost";
					isServer = true;
					pnum = Integer.parseInt(JOptionPane.showInputDialog("Enter number of players: "));
					gameServer = new GameServer(pnum);
					new ChatServer();
					(new Thread(new MadRace())).start();
					try{ gameLoop = new GameLoop(server, name, playertype);
					}catch(Exception f){}

					startPanel.setVisible(false);
					howTo.setVisible(false);
					mainMenu.setVisible(false);
					lobbyPanel.setVisible(true);
				}catch(Exception f){
					f.printStackTrace();
				}
			}
		});

		JButton joinButton = new JButton("Join Game");
		panel.add(joinButton);
		joinButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				server = JOptionPane.showInputDialog("Enter server address: ");
				isServer= false;
				name = getName.getText();

				startPanel.setVisible(false);
				howTo.setVisible(false);
				mainMenu.setVisible(false);
				lobbyPanel.setVisible(true);
				(new Thread(new MadRace())).start();
				try{ gameLoop = new GameLoop(server, name, playertype);
				}catch(Exception f){}
			}
		});

		JButton menuButton = new JButton("Main Menu");
		panel.add(menuButton);
		menuButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				startPanel.setVisible(false);
				howTo.setVisible(false);
				lobbyPanel.setVisible(false);
				mainMenu.setVisible(true);
			}
		});

		startPanel.add(choosePlayer, BorderLayout.NORTH);
		startPanel.add(panel, BorderLayout.SOUTH);
		mainC.add(startPanel);
		mainFrame.pack();
	}

	public static void howToPlay(){
		CardLayout cardLayout = new CardLayout();
		JPanel panel = new JPanel();
		JPanel cardPanel = new JPanel();
		cardPanel.setLayout(cardLayout);

		ImagePanel htp1 = new ImagePanel(new ImageIcon("piks/Game Obj.jpg").getImage());
		ImagePanel htp2 = new ImagePanel(new ImageIcon("piks/Controls.jpg").getImage());
		ImagePanel htp3 = new ImagePanel(new ImageIcon("piks/launcha.jpg").getImage());
		ImagePanel htp4 = new ImagePanel(new ImageIcon("piks/ramma.jpg").getImage());
		ImagePanel htp5 = new ImagePanel(new ImageIcon("piks/gunna.jpg").getImage());

		cardPanel.add(htp1, "1");
        cardPanel.add(htp2, "2");
        cardPanel.add(htp3, "3");
        cardPanel.add(htp4, "4");
        cardPanel.add(htp5, "5");

		howTo.setLayout(new BorderLayout());
		howTo.setPreferredSize(new Dimension(500, 550));
		howTo.add(cardPanel, BorderLayout.CENTER);

		JButton menuButton = new JButton("Main Menu");
		panel.add(menuButton);
		menuButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				howTo.setVisible(false);
				startPanel.setVisible(false);
				lobbyPanel.setVisible(false);
				mainMenu.setVisible(true);
			}
		});

		JButton nextButton = new JButton("Next");
		panel.add(nextButton);
		nextButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				htpCount++;
				if(htpCount==6) htpCount=1;
				cardLayout.show(cardPanel, Integer.toString(htpCount));
			}
		});

		JButton quitButton = new JButton("Quit");
		panel.add(quitButton);
		quitButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.exit(0);
			}
		});

		howTo.add(panel, BorderLayout.SOUTH);
		mainC.add(howTo);
		mainFrame.pack();
	}

	public static void lobby(){
		JPanel playerPanel = new JPanel(){
			@Override	//set background image
			protected void paintComponent(Graphics g){
				try{
					Image bg = ImageIO.read(getClass().getResource("piks/bg.jpg"));
					g.drawImage(bg, 0, 0, this.getWidth(), this.getHeight(), null);
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		};

		lobbyPanel.setLayout(new BorderLayout());
		lobbyPanel.setPreferredSize(new Dimension(500, 550));

		//display player name
		nameLabel = new JLabel("MadRace", JLabel.CENTER);
		nameLabel.setFont(new Font("Serif", Font.BOLD, 60));
		lobbyPanel.add(nameLabel, BorderLayout.NORTH);

		//set of players
		JScrollPane pscroll = new JScrollPane(plist);		//player scrollable
		plist.setPreferredSize(new Dimension(450, 428));
		plist.setEditable(false);
		plist.setFocusable(false);
		plist.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		doc = plist.getStyledDocument();
		style = plist.addStyle("I'm a Style", null);
		StyleConstants.setForeground(style, Color.GRAY);
		StyleConstants.setBold(style, true);
		playerPanel.add(pscroll);

		//bottom panel
		JPanel downPanel = new JPanel();
		JButton menuButton = new JButton("Main Menu");
		downPanel.add(menuButton);
		menuButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				howTo.setVisible(false);
				startPanel.setVisible(false);
				lobbyPanel.setVisible(false);
				mainMenu.setVisible(true);
			}
		});

		JButton startButton = new JButton("Start");
		downPanel.add(startButton);
		startButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					if(gameLoop.start){
						JOptionPane.showMessageDialog(new JPanel(), "Game already in progress. Catch up!", "Warning", JOptionPane.WARNING_MESSAGE);
					}else{
						JOptionPane.showMessageDialog(new JPanel(), "Still waiting for players", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}catch(Exception f){
					f.printStackTrace();
				}
			}
		});

		JButton quitButton = new JButton("Quit");
		downPanel.add(quitButton);
		quitButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.exit(0);
			}
		});

		lobbyPanel.add(playerPanel, BorderLayout.CENTER);
		lobbyPanel.add(downPanel, BorderLayout.SOUTH);
		mainC.add(lobbyPanel);
		mainFrame.pack();
	}

	public void run() {

		while(true){
			try{
				//add to list of players in lobby
				if(isServer){
					if(gameServer.playerCount > pcount){
						doc.insertString(doc.getLength(), gameServer.getPlayer(pcount) + "\n",style);
						pcount++;
					}
				}else{
					if(gameLoop.playerCount > pcount){
						doc.insertString(doc.getLength(), gameLoop.getPlayer(pcount) + "\n",style);
						pcount++;
					}
				}
				Thread.sleep(1000); 	//1second
			}catch(Exception e){}
		}
	}

	public static void main(String[] args) {
		howToPlay();
		howTo.setVisible(false);
		startGame();
		startPanel.setVisible(false);
		lobby();
		lobbyPanel.setVisible(false);
		assembleLaunchUI();

		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.pack();
		mainFrame.setVisible(true);
	}
}

class ImagePanel extends JPanel {
	private Image img;

	public ImagePanel(String img){
		this(new ImageIcon(img).getImage());
	}

	public ImagePanel(Image img) {
		this.img = img;
		Dimension size = new Dimension(550, 650);
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setSize(size);
		setLayout(null);
	}

	public void paintComponent(Graphics g){
		g.drawImage(img, 0, 0, getWidth(),  getHeight(), this);
	}
}
