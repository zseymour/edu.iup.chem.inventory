/**
 * Copyright 2008  Eugene Creswick
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ciscavate.cjwizard;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.swingx.JXDatePicker;

/**
 * @author rcreswick
 * 
 */
public abstract class WizardPage extends JPanel {

	/**
	 * Listener to keep track of the components as they are added and removed
	 * from this wizard page.
	 * 
	 * @author rogue
	 */
	private class WPContainerListener implements ContainerListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ContainerListener#componentAdded(java.awt.event.
		 * ContainerEvent)
		 */
		@Override
		public void componentAdded(final ContainerEvent e) {
			log.trace("component added: " + e.getChild());
			final Component newComp = e.getChild();

			storeIfNamed(newComp);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ContainerListener#componentRemoved(java.awt.event.
		 * ContainerEvent)
		 */
		@Override
		public void componentRemoved(final ContainerEvent e) {
			log.trace("component removed: " + e.getChild());
			_namedComponents.remove(e.getChild());
		}

		/**
		 * @param newComp
		 */
		private void storeIfNamed(final Component newComp) {
			if (newComp instanceof CustomWizardComponent
					&& null != newComp.getName()) {
				_namedComponents.add(newComp);
				// don't recurse into custom components.
				return;
			}

			if (newComp instanceof Container) {
				// recurse:
				final Component[] children = ((Container) newComp)
						.getComponents();
				for (final Component c : children) {
					storeIfNamed(c);
				}
			}

			if (null != newComp.getName()) {
				_namedComponents.add(newComp);
			}
		}
	}

	/**
	 * Commons logging log instance
	 */
	private static Log			log					= LogFactory
															.getLog(WizardPage.class);

	/**
	 * Count of WizardPages, used to get unique IDs
	 */
	private static long			_idCounter			= 0;

	/**
	 * Unique ID for this wizard page.
	 */
	private final long			_id					= _idCounter++;

	/**
	 * The title of this wizard page (a 1-2 word string)
	 */
	private final String		_title;

	/**
	 * A longer description of this wizard page.
	 */
	private final String		_description;

	/**
	 * The WizardController that contains this wizard page. (often the
	 * WizardContainer)
	 */
	private WizardController	_controller;

	/**
	 * The collection of components that have been added to this wizard page
	 * with set names.
	 */
	protected Set<Component>	_namedComponents	= new HashSet<Component>();

	/**
	 * Constructor. Sets the title and description for this wizard panel.
	 * 
	 * @param title
	 *            The short (1-3 word) name of this page.
	 * @param description
	 *            A possibly longer description (but still under 1 sentence)
	 */
	public WizardPage(final String title, final String description) {
		_title = title;
		_description = description;

		addContainerListener(new WPContainerListener());
		setDoubleBuffered(true);
	}

	/**
	 * Gets a longer description of this WizardPage.
	 * 
	 * @return The WizardPage description.
	 */
	public String getDescription() {
		return _description;
	}

	/**
	 * Gets the unique identifier for this wizard page;
	 * 
	 * @return
	 */
	public final String getId() {
		return "" + _id;
	}

	/**
	 * @return
	 */
	protected Set<Component> getNamedComponents() {
		return _namedComponents;
	}

	/**
	 * Gets the short 1-2 word description of this WizardPage
	 * 
	 * @return The WizardPage title
	 */
	public String getTitle() {
		return _title;
	}

	/**
	 * Gets the value from a component.
	 * 
	 * @param c
	 *            The component.
	 * @return The value.
	 */
	private Object getValue(final Component c) {
		Object val = null;

		if (c instanceof CustomWizardComponent) {
			val = ((CustomWizardComponent) c).getValue();
		} else if (c instanceof JTextComponent) {
			val = ((JTextComponent) c).getText();
		} else if (c instanceof AbstractButton) {
			val = ((AbstractButton) c).isSelected();
		} else if (c instanceof JComboBox) {
			val = ((JComboBox) c).getSelectedItem();
		} else if (c instanceof JList) {
			val = ((JList) c).getSelectedValuesList();
		} else if (c instanceof JXDatePicker) {
			val = ((JXDatePicker) c).getDate();
		} else {
			log.warn("Unknown component: " + c);
		}

		return val;
	}

	/**
	 * Registers the controller with this WizardPage.
	 * 
	 * The default visibility is intentional, but protected would be fine too.
	 * 
	 * @param controller
	 */
	void registerController(final WizardController controller) {
		_controller = controller;
	}

	/**
	 * Invoked immediately prior to rendering the wizard page on screen.
	 * 
	 * This provides an opportunity to adjust the next/finish buttons and
	 * customize the ui based on feedback.
	 */
	public void rendering(final List<WizardPage> path,
			final WizardSettings settings) {
		// intentionally empty. (default implementation)
	}

	/**
	 * Set the enabled status of the Finished button.
	 * 
	 * @param enabled
	 *            true to enable it, false otherwise.
	 */
	protected void setFinishEnabled(final boolean enabled) {
		if (null != _controller) {
			_controller.setFinishEnabled(enabled);
		}
	}

	/**
	 * Set the enabled status of the Next button.
	 * 
	 * @param enabled
	 *            true to enable it, false otherwise.
	 */
	protected void setNextEnabled(final boolean enabled) {
		if (null != _controller) {
			_controller.setNextEnabled(enabled);
		}
	}

	/**
	 * Set the enabled status of the Prev button.
	 * 
	 * @param enabled
	 *            true to enable it, false otherwise.
	 */
	protected void setPrevEnabled(final boolean enabled) {
		if (null != _controller) {
			_controller.setPrevEnabled(enabled);
		}
	}

	/**
	 * Returns a string reperesntation of this wizard page.
	 */
	@Override
	public String toString() {
		return getId() + ": " + getTitle();
	}

	/**
	 * Updates the settings map after this page has been used by the user.
	 * 
	 * This method should update the WizardSettings Map so that it contains the
	 * new key/value pairs from this page.
	 * 
	 */
	public void updateSettings(final WizardSettings settings) {
		for (final Component c : _namedComponents) {
			settings.put(c.getName(), getValue(c));
		}
	}

}
