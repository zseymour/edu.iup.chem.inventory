package edu.iup.chem.inventory;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXLoginPane;

import edu.iup.chem.inventory.ui.ChemLoginService;
import edu.iup.chem.inventory.ui.MainFrame;

public class Driver {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {

		final Logger log = Logger.getLogger(Driver.class);
		ConnectionPool.initializePool();
		final ChemLoginService service = new ChemLoginService();
		try {
			UIManager.setLookAndFeel(UIManager
					.getCrossPlatformLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			log.error("Failed to set look and feel.", e);
		}
		final MainFrame f = new MainFrame();
		final JXLoginPane pane = new JXLoginPane(service);
		JXLoginPane.Status status = JXLoginPane.Status.FAILED;
		try {
			status = JXLoginPane.showLoginDialog(f, pane);
		} catch (final Exception e) {
			log.error("Handling missing ComponentUI.");
		}

		log.debug("Log-in occurred with status " + status);
		if (status == JXLoginPane.Status.CANCELLED
				|| status == JXLoginPane.Status.NOT_STARTED) {
			System.exit(0);
		} else if (status == JXLoginPane.Status.SUCCEEDED) {
			f.init();
			f.pack();
			f.setVisible(true);
		}

	}

}
