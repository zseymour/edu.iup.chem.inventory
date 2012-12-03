package edu.iup.chem.inventory.ui;

import java.awt.Dimension;

import javax.swing.JDialog;

import org.apache.log4j.Logger;
import org.ciscavate.cjwizard.PageFactory;
import org.ciscavate.cjwizard.WizardContainer;
import org.ciscavate.cjwizard.WizardListener;
import org.ciscavate.cjwizard.pagetemplates.TitledPageTemplate;

public class InventoryWizardDialog extends JDialog {

	/**
	 * 
	 */
	private static final long		serialVersionUID	= 5748959709659696897L;
	private static final Logger		log					= Logger.getLogger(InventoryWizardDialog.class);
	private final WizardContainer	wc;

	public InventoryWizardDialog(final PageFactory pf) {
		// first, build the wizard. The TestFactory defines the
		// wizard content and behavior.
		wc = new WizardContainer(pf, new TitledPageTemplate());

		// add a wizard listener to update the dialog titles and notify the
		// surrounding application of the state of the wizard:
		// wc.addWizardListener(wl);

		// Set up the standard bookkeeping stuff for a dialog, and
		// add the wizard to the JDialog:
		setPreferredSize(new Dimension(600, 400));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		getContentPane().add(wc);
	}

	public void addWizardListener(final WizardListener wl) {
		wc.addWizardListener(wl);
	}
}
