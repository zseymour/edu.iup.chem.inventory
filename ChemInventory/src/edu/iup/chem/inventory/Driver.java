package edu.iup.chem.inventory;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXLoginPane;

import edu.iup.chem.inventory.index.Index;
import edu.iup.chem.inventory.ui.ChemLoginService;
import edu.iup.chem.inventory.ui.MainFrame;
import edu.iup.chem.inventory.ui.SplashScreen;

public class Driver {
	final static Logger	log	= Logger.getLogger(Driver.class);

	public static JXLoginPane.Status login(final JFrame f) {
		final ChemLoginService service = new ChemLoginService();
		final JXLoginPane pane = new JXLoginPane(service);
		JXLoginPane.Status status = JXLoginPane.Status.FAILED;
		status = JXLoginPane.showLoginDialog(f, pane);
		log.info("Log-in occurred with status " + status);
		return status;
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {

		ConnectionPool.initializePool();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			log.error("Failed to set look and feel.", e);
		}
		final MainFrame f = new MainFrame();

		final JXLoginPane.Status status = login(f);

		if (status == JXLoginPane.Status.CANCELLED
				|| status == JXLoginPane.Status.NOT_STARTED) {
			System.exit(0);
		} else if (status == JXLoginPane.Status.SUCCEEDED) {
			final JFrame loading = new SplashScreen().showLoadAnimation();
			Index.initializeDirectories();
			if (loading != null) {
				loading.dispose();
			}
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					f.init();
					f.pack();
					f.setVisible(true);
				}

			});

		}

	}

}
