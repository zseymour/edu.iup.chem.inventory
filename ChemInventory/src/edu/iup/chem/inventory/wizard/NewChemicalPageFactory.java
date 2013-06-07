package edu.iup.chem.inventory.wizard;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

import edu.iup.chem.inventory.dao.ChemicalDao;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;
import edu.iup.chem.inventory.search.ChemicalWebSearch;

public class NewChemicalPageFactory extends InventoryPageFactory {

	private static final Logger	log	= Logger.getLogger(NewChemicalPageFactory.class);

	@Override
	@SuppressWarnings("serial")
	protected WizardPage buildPage(int pageNo, final WizardSettings settings) {
		if (settings.containsKey("next")) {
			pageNo = (int) settings.get("next");
		}

		switch (pageNo) {
			case 0:
				return new WizardPage("Choose CAS Number",
						"Enter CAS number of the new chemical.") {
					{
						log.debug("Settings: " + settings);
						final JTextField field = new JTextField();
						// set a name on any component that you want to collect
						// values
						// from. Be sure to do this *before* adding the
						// component to
						// the WizardPage.
						field.setName("cas");

						field.setPreferredSize(new Dimension(75, 20));
						add(new JLabel("Enter CAS Number:"));
						add(field);
						// final ValidationGroup group = getContainer()
						// .getValidationGroup();
						// group.add(field,
						// StringValidators.REQUIRE_NON_EMPTY_STRING,
						// StringValidators.regexp("\\d{2,6}-\\d{2}-\\d",
						// "CAS Improperly Formatted", false),
						// new CASNotExistValidator());
					}

					@Override
					public void updateSettings(final WizardSettings newSettings) {
						super.updateSettings(newSettings);

						final String cas = (String) newSettings.get("cas");
						final ChemicalRecord rec;
						if (ChemicalDao.exists(cas)) {
							// JOptionPane
							// .showMessageDialog(this,
							// "A chemical with that CAS already exists in our database.");
							// settings.put("next", 0);
							// return;

							rec = ChemicalDao.getByCas(cas);

						} else {

							final List<ChemicalRecord> options = ChemicalWebSearch
									.searchByCAS(cas);

							if (options == null || options.isEmpty()) {
								rec = null;
							} else if (options.size() > 1) {
								rec = (ChemicalRecord) JOptionPane
										.showInputDialog(this,
												"Did you mean: ",
												"Choose a chemical to add",
												JOptionPane.QUESTION_MESSAGE,
												null, options.toArray(),
												options.get(0));
							} else {
								rec = options.get(0);
							}
						}
						newSettings.put("chemicalRecord", rec);

					}
				};
			case 1:
				return new VerifyChemicalFieldsWizardPage(settings);
			case 2:
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
