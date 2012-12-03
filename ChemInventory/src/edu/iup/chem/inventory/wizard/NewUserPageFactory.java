package edu.iup.chem.inventory.wizard;

import java.util.List;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

public class NewUserPageFactory extends InventoryPageFactory {

	@Override
	protected WizardPage buildPage(final int size, final WizardSettings settings) {
		switch (size) {
			case 0:
				return new NewUserWizardPage(settings);
			case 1:
				return new WizardPage("Complete Entry?",
						"Press Finish to enter the new chemical.") {
					@Override
					public void rendering(final List<WizardPage> path,
							final WizardSettings settings1) {
						setFinishEnabled(true);
						setNextEnabled(false);
					}
				};

			default:
				return getErrorPage();
		}

	}

}
