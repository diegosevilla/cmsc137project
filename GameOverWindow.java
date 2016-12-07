import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.border.EtchedBorder;
import javax.imageio.ImageIO;
import java.io.IOException;
import javax.swing.text.*;

public class GameOverWindow {
	static JFrame mainFrame = new JFrame("MadRace BETA");
	static Container mainC;
	static Container mainMenu = new Container();

	public GameOverWindow(JFrame mainFrame){
		// mainC = mainFrame.getContentPane();
		mainFrame.setResizable(false);

		JPanel panel = new JPanel();
		//change this to a generic "Game Over" image
		ImagePanel menu = new ImagePanel(new ImageIcon("piks/gameover.png").getImage());

		mainMenu.setLayout(new BorderLayout());
		mainMenu.setPreferredSize(new Dimension(500, 550));
		mainMenu.add(menu, BorderLayout.CENTER);
		//	mainC.setPreferredSize(new Dimension(500, 550));
		JButton quitButton = new JButton("Quit");
		panel.add(quitButton);
		quitButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.exit(0);
			}
		});

		mainMenu.add(panel, BorderLayout.SOUTH);
		mainFrame.setContentPane(mainMenu);
		mainFrame.pack();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.pack();
		mainFrame.setVisible(true);
	}
}
