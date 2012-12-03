package edu.iup.chem.inventory.wizard;

import java.util.List;

import javax.swing.JDialog;

import org.ciscavate.cjwizard.WizardListener;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

public abstract class InventoryWizardListener implements WizardListener {

	private final JDialog	container;

	public InventoryWizardListener(final JDialog c) {
		container = c;
	}

	@Override
	public void onCanceled(final List<WizardPage> path,
			final WizardSettings settings) {
		container.dispose();
	}

	@Override
	public abstract void onFinished(List<WizardPage> path,
			final WizardSettings settings);

	@Override
	public void onPageChanged(final WizardPage newPage,
			final List<WizardPage> path) {
		// Set the dialog title to match the description of the new page:
		container.setTitle(newPage.getDescription());
	}

}
