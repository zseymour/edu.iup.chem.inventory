package edu.iup.chem.inventory.wizard;

import java.util.List;

import org.apache.log4j.Logger;
import org.ciscavate.cjwizard.PageFactory;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

public abstract class InventoryPageFactory implements PageFactory {

	protected static WizardPage getErrorPage() {
		return new WizardPage("Error.",
				"This page shouldn't really be here at all.") {

			/**
					 * 
					 */
			private static final long	serialVersionUID	= -7424202289691205528L;
		};
	}

	private final Logger	log	= Logger.getLogger(InventoryPageFactory.class);

	protected abstract WizardPage buildPage(int size, WizardSettings settings);

	@Override
	public WizardPage createPage(final List<WizardPage> path,
			final WizardSettings settings) {
		log.debug("creating page " + path.size());

		// Get the next page to display. The path is the list of all wizard
		// pages that the user has proceeded through from the start of the
		// wizard, so we can easily see which step the user is on by taking
		// the length of the path. This makes it trivial to return the next
		// WizardPage:
		final WizardPage page = buildPage(path.size(), settings);

		// if we wanted to, we could use the WizardSettings object like a
		// Map<String, Object> to change the flow of the wizard pages.
		// In fact, we can do arbitrarily complex computation to determine
		// the next wizard page.

		log.debug("Returning page: " + page);
		return page;
	}

}
