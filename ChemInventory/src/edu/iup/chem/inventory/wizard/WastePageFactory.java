package edu.iup.chem.inventory.wizard;

import java.util.List;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

public class WastePageFactory extends InventoryPageFactory {

	@Override
	protected WizardPage buildPage(final int pageNo,
			final WizardSettings settings) {
		switch (pageNo) {
			case 0:
				return new WasteWizardPage(settings);
			case 1:
				return new WizardPage("Complete Entry?",
						"Press Finish to enter the new waste bottle.") {
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
