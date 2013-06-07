package edu.iup.chem.inventory.wizard;

import java.util.List;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

import edu.iup.chem.inventory.db.inventory.tables.records.UserRecord;

public class NewUserPageFactory extends InventoryPageFactory {
	private final UserRecord	user;

	public NewUserPageFactory(final UserRecord rec) {
		user = rec;
	}

	@Override
	protected WizardPage buildPage(final int size, final WizardSettings settings) {
		switch (size) {
			case 0:
				if (user != null) {
					settings.put("user", user);
				}
				return new NewUserWizardPage(settings);
			case 1:
				return new WizardPage("Complete Entry?",
						"Press Finish to enter the new user.") {
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
