import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.EtchedBorder;
import javax.imageio.ImageIO;
import java.io.IOException;
import javax.swing.text.*;

public class YouWinWindow {
	// static JFrame mainFrame = new JFrame("MadRace BETA");
	// static Container mainC = mainFrame.getContentPane();
	static Container mainMenu = new Container();

	public YouWinWindow(JFrame mainFrame){

		mainFrame.setResizable(false);

		JPanel panel = new JPanel();
		//change this to a generic "Game Over" image
		ImagePanel menu = new ImagePanel(new ImageIcon("piks/youwin.png").getImage());

		mainMenu.setLayout(new BorderLayout());
		mainMenu.setPreferredSize(new Dimension(500, 550));
		mainMenu.add(menu, BorderLayout.CENTER);

		JButton quitButton = new JButton("Quit");
		panel.add(quitButton);
		quitButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.exit(0);
			}
		});

		mainMenu.add(panel, BorderLayout.SOUTH);
		mainFrame.	setContentPane(mainMenu);
		mainFrame.pack();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.pack();
		mainFrame.setVisible(true);
	}
}
