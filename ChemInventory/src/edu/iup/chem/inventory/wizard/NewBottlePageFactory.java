package edu.iup.chem.inventory.wizard;

import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.apache.log4j.Logger;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

import edu.iup.chem.inventory.dao.ChemicalDao;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;

public class NewBottlePageFactory extends InventoryPageFactory {
	private static final Logger	log	= Logger.getLogger(NewBottlePageFactory.class);

	@Override
	protected WizardPage buildPage(final int pageNo,
			final WizardSettings settings) {
		switch (pageNo) {
			case 0:
				return new WizardPage("Choose CAS Number",
						"Enter CAS number for the new bottle.") {
					{
						log.debug("Settings: " + settings);
						final ListComboBoxModel<String> casModel = new ListComboBoxModel<>(
								new ChemicalDao().getListOfCAS());
						final JComboBox<String> field = new JComboBox<>(
								casModel);
						AutoCompleteDecorator.decorate(field);

						field.setName("cas");

						field.setPreferredSize(new Dimension(75, 20));
						add(new JLabel("Enter CAS Number:"));
						add(field);
					}

					@Override
					public void updateSettings(final WizardSettings newSettings) {
						super.updateSettings(newSettings);

						final String cas = (String) newSettings.get("cas");

						if (ChemicalDao.exists(cas)) {
							final ChemicalRecord rec = ChemicalDao
									.getByCas(cas);

							newSettings.put("chemicalRecord", rec);
						} else {

						}

					}
				};
			case 1:
				return new VerifyBottleWizardPage(settings);
			default:
				return getErrorPage();
		}

	}

}
