import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


public class MainMenuDesign {
	//Create and set up the window.
	static JFrame mainFrame = new JFrame("MadRace BETA");
	static Container mainC = mainFrame.getContentPane();
	static Container mainMenu = new Container();
	static Container howTo = new Container();	
	static int htpCount = 1;	//for traversing "Help" images

	private static void assembleLaunchUI() {
		JPanel panel = new JPanel();
		ImagePanel menu = new ImagePanel(new ImageIcon("piks/MadRace.jpg").getImage());
				  
		mainMenu.setLayout(new BorderLayout());
		mainMenu.setPreferredSize(new Dimension(500, 550));
		mainMenu.add(menu, BorderLayout.CENTER); 

		JButton newGameButton = new JButton("New Game");
		newGameButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
			}
		});
		panel.add(newGameButton);

		JButton helpButton = new JButton("Help");
		panel.add(helpButton);
		helpButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				mainMenu.setVisible(false);
				howTo.setVisible(true);
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

	public static void main(String[] args) {
		howToPlay();
		howTo.setVisible(false);
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
