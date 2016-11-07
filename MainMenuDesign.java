import javax.swing.*;

public class MainMenuDesign {
    private static void assembleLaunchUI() {

        //Create and set up the window.
        JFrame frame = new JFrame("MadRace BETA");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();

        //Add title
        JLabel label = new JLabel("MadRace BETA");
        panel.add(label);

        JButton newGameButton = new JButton("New Game");
        panel.add(newGameButton);

        JButton helpButton = new JButton("Help");
        panel.add(helpButton);

        JButton quitButton = new JButton("Quit");
        panel.add(quitButton);

        frame.add(panel);
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        assembleLaunchUI();
    }
}
