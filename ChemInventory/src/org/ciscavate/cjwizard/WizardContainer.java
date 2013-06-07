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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ciscavate.cjwizard.pagetemplates.DefaultPageTemplate;
import org.ciscavate.cjwizard.pagetemplates.PageTemplate;
import org.ciscavate.utilities.ExceptionUtilities;
import org.netbeans.validation.api.Problem;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationGroupProvider;
import org.netbeans.validation.api.ui.ValidationUI;
import org.netbeans.validation.api.ui.swing.SwingValidationGroup;

/**
 * This is the primary "Wizard" class. It must be instantiated with a
 * PageFactory and then treated as a JPanel.
 * 
 * @author rcreswick
 * 
 */
public class WizardContainer extends JPanel implements WizardController,
		ValidationGroupProvider {

	private SwingValidationGroup		group;

	/**
	 * Commons logging log instance
	 */
	private static Log					log				= LogFactory
																.getLog(WizardContainer.class);

	/**
	 * Storage for all the collected information.
	 */
	private final WizardSettings		_settings		= new WizardSettings();

	/**
	 * The path from the start of the dialog to the current location.
	 */
	private final List<WizardPage>		_path			= new LinkedList<WizardPage>();

	/**
	 * List of listeners to update on wizard events.
	 */
	private final List<WizardListener>	_listeners		= new LinkedList<WizardListener>();

	/**
	 * The template to surround the wizard pages of this dialog.
	 */
	private PageTemplate				_template		= null;

	/**
	 * The factory that generates pages for this wizard.
	 */
	private final PageFactory			_factory;

	private final AbstractAction		_prevAction		= new AbstractAction(
																"< Prev") {
															{
																setEnabled(false);
															}

															@Override
															public void actionPerformed(
																	final ActionEvent e) {
																prev();
															}
														};

	private final AbstractAction		_nextAction		= new AbstractAction(
																"Next >") {
															@Override
															public void actionPerformed(
																	final ActionEvent e) {
																next();
															}
														};

	private final AbstractAction		_finishAction	= new AbstractAction(
																"Finish") {
															{
																setEnabled(false);
															}

															@Override
															public void actionPerformed(
																	final ActionEvent e) {
																finish();
															}
														};

	private final AbstractAction		_cancelAction	= new AbstractAction(
																"Cancel") {
															@Override
															public void actionPerformed(
																	final ActionEvent e) {
																cancel();
															}
														};

	/**
	 * Constructor, uses default PageTemplate.
	 */
	public WizardContainer(final PageFactory factory) {
		this(factory, new DefaultPageTemplate());
	}

	/**
	 * Constructor.
	 */
	public WizardContainer(final PageFactory factory,
			final PageTemplate template) {
		_factory = factory;
		_template = template;

		initComponents();
		_template.registerController(this);

		// get the first page:
		next();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ciscavate.cjwizard.WizardController#addWizardListener(com.stottlerhenke
	 * .presentwell.wizard.WizardListener)
	 */
	@Override
	public void addWizardListener(final WizardListener listener) {
		ExceptionUtilities.checkNull(listener, "listener");
		if (!_listeners.contains(listener)) {
			_listeners.add(listener);
			final WizardPage curPage = _path.get(_path.size() - 1);
			listener.onPageChanged(curPage, getPath());
		}
	}

	/**
    * 
    */
	@Override
	public void cancel() {
		log.debug("cancel");

		for (final WizardListener l : _listeners) {
			l.onCanceled(getPath(), getSettings());
		}
	}

	/**
    * 
    */
	@Override
	public void finish() {
		log.debug("finish");

		for (final WizardListener l : _listeners) {
			l.onFinished(getPath(), getSettings());
		}
	}

	/**
	 * @param nextPage
	 * @param path
	 */
	private void firePageChanged(final WizardPage curPage,
			final List<WizardPage> path) {
		for (final WizardListener l : _listeners) {
			l.onPageChanged(curPage, getPath());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ciscavate.cjwizard.WizardController#getPath()
	 */
	@Override
	public List<WizardPage> getPath() {
		return _path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ciscavate.cjwizard.WizardController#getSettings()
	 */
	@Override
	public WizardSettings getSettings() {
		return _settings;
	}

	@Override
	public ValidationGroup getValidationGroup() {
		return group;
	}

	/**
    * 
    */
	private void initComponents() {
		final JButton prevBtn = new JButton(_prevAction);
		final JButton nextBtn = new JButton(_nextAction);
		final JButton finishBtn = new JButton(_finishAction);
		final JButton cancelBtn = new JButton(_cancelAction);
		group = SwingValidationGroup.create(new ValidationUI() {

			@Override
			public void clearProblem() {
				showProblem(null);
			}

			@Override
			public void showProblem(final Problem problem) {
				if (problem == null || !problem.isFatal()) {
					setNextEnabled(true && _nextAction.isEnabled());
					setFinishEnabled(true && _nextAction.isEnabled());
				} else {
					setNextEnabled(false);
					setFinishEnabled(false);
				}
			}

		});

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(prevBtn);
		buttonPanel.add(Box.createHorizontalStrut(5));
		buttonPanel.add(nextBtn);
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(finishBtn);
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(cancelBtn);
		final JComponent problemLabel = group.createProblemLabel();

		final JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
		bottomPanel.add(buttonPanel);
		bottomPanel.add(problemLabel);
		setLayout(new BorderLayout());

		this.add(_template, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);
	}

	/**
    * 
    */
	@Override
	public void next() {
		log.debug("next page");

		if (0 != _path.size()) {
			// get the settings from the page that is going away:
			final WizardPage lastPage = _path.get(_path.size() - 1);
			getSettings().newPage(lastPage.getId());
			lastPage.updateSettings(getSettings());
		}

		final WizardPage curPage = _factory
				.createPage(getPath(), getSettings());
		curPage.registerController(this);

		_path.add(curPage);
		if (_path.size() > 1) {
			setPrevEnabled(true);
		}

		// tell the page that it is about to be rendered:
		curPage.rendering(getPath(), getSettings());
		_template.setPage(curPage);

		firePageChanged(curPage, getPath());
	}

	/**
	 * The PageFactory is not queried for pages when moving *backwards*.
	 */
	@Override
	public void prev() {
		log.debug("prev. page");

		_path.remove(_path.size() - 1);
		// roll-back the settings:
		getSettings().rollBack();

		assert 1 <= _path.size() : "Invalid path size! " + _path.size();
		if (_path.size() <= 1) {
			setPrevEnabled(false);
		}

		final WizardPage curPage = _path.get(_path.size() - 1);

		setNextEnabled(true);
		// tell the page that it is about to be rendered:
		curPage.rendering(getPath(), getSettings());
		_template.setPage(curPage);

		firePageChanged(curPage, getPath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ciscavate.cjwizard.WizardController#removeWizardListener(com.
	 * stottlerhenke.presentwell.wizard.WizardListener)
	 */
	@Override
	public void removeWizardListener(final WizardListener listener) {
		ExceptionUtilities.checkNull(listener, "listener");
		_listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ciscavate.cjwizard.WizardController#setFinishEnabled(boolean)
	 */
	@Override
	public void setFinishEnabled(final boolean enabled) {
		_finishAction.setEnabled(enabled);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ciscavate.cjwizard.WizardController#setNextEnabled(boolean)
	 */
	@Override
	public void setNextEnabled(final boolean enabled) {
		_nextAction.setEnabled(enabled);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ciscavate.cjwizard.WizardController#setPrevEnabled(boolean)
	 */
	@Override
	public void setPrevEnabled(final boolean enabled) {
		_prevAction.setEnabled(enabled);
	}

	@Override
	public void visitPage(final WizardPage page) {
		final int idx = _path.indexOf(page);

		if (-1 == idx) {
			// new page
			if (0 != _path.size()) {
				// get the settings from the page that is going away:
				final WizardPage lastPage = _path.get(_path.size() - 1);
				getSettings().newPage(lastPage.getId());
				lastPage.updateSettings(getSettings());
			}

			getPath().add(page);
		} else {
			// page is in the path at idx.

			// first, roll back the settings and trim the path:
			for (int i = _path.size() - 1; i > idx; i--) {
				getSettings().rollBack();
				_path.remove(i);
			}
		}

		if (_path.size() > 1) {
			setPrevEnabled(true);
		}

		setNextEnabled(true);
		page.rendering(_path, getSettings());
		_template.setPage(page);
		firePageChanged(page, _path);
	}
}
