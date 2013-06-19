package edu.iup.chem.inventory.ui;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

public class SplashScreen {
	private final Logger	log	= Logger.getLogger(SplashScreen.class);

	public JFrame showLoadAnimation() {
		final JFrame f = new JFrame("IUP Chemical Inventory");
		f.setUndecorated(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final JPanel panel = (JPanel) f.getContentPane();
		final JLabel imageLabel = new JLabel();

		final ImageIcon loadAnimation = new ImageIcon(getClass().getResource(
				"img/splash.gif"));
		f.setSize(loadAnimation.getIconWidth(), loadAnimation.getIconHeight());
		imageLabel.setIcon(loadAnimation);
		panel.add(imageLabel);
		f.setLocationRelativeTo(null);

		f.setVisible(true);

		return f;
	}
}
