package edu.iup.chem.inventory.wizard;

import java.util.List;

import org.apache.log4j.Logger;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;

public class NewBottlePageFactory extends InventoryPageFactory {
	private static final Logger		log	= Logger.getLogger(NewBottlePageFactory.class);
	private static ChemicalRecord	rec;

	public NewBottlePageFactory(final ChemicalRecord selectedChemical) {
		rec = selectedChemical;
	}

	@Override
	protected WizardPage buildPage(int pageNo, final WizardSettings settings) {
		if (settings.containsKey("next")) {
			pageNo = (int) settings.get("next");
		}

		switch (pageNo) {
			case 0:
				settings.put("chemicalRecord", rec);
				return new VerifyBottleWizardPage(settings);
			case 1:
				return new WizardPage("Complete Entry?",
						"Press Finish to enter the new bottle.") {
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
