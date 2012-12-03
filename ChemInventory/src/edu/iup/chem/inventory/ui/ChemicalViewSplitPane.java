package edu.iup.chem.inventory.ui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;

import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import org.apache.log4j.Logger;
import org.openscience.cdk.Molecule;

import edu.iup.chem.inventory.Constants;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;

public class ChemicalViewSplitPane extends DataPanel implements ChemicalLister {

	private static final Logger	LOG	= Logger.getLogger(ChemicalViewSplitPane.class);

	public static void toggle(final JSplitPane sp, final boolean collapse) {
		try {
			final BasicSplitPaneDivider bspd = ((BasicSplitPaneUI) sp.getUI())
					.getDivider();
			final Field buttonField = BasicSplitPaneDivider.class
					.getDeclaredField(collapse ? "rightButton" : "leftButton");
			buttonField.setAccessible(true);
			final JButton button = (JButton) buttonField
					.get(((BasicSplitPaneUI) sp.getUI()).getDivider());
			button.getActionListeners()[0].actionPerformed(new ActionEvent(
					bspd, MouseEvent.MOUSE_CLICKED, "bum"));
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private boolean						collapsed	= false;
	private final ChemicalSearchPanel	chemicalPanel;
	private final ChemicalViewPanel		viewPanel;

	private JSplitPane					splitPane;

	public ChemicalViewSplitPane() {
		chemicalPanel = new ChemicalSearchPanel();
		viewPanel = new ChemicalViewPanel();
		initComponents();
	}

	@Override
	public void deleteRows() {
		if (chemicalPanel.hasSelectedRows()) {
			chemicalPanel.deleteRows();
		}
	}

	@Override
	public void fireChemicalsAdded() {
		chemicalPanel.fireChemicalsAdded();
	}

	public ChemicalRecord getRowAtIndex(final int selectedIndex) {
		return chemicalPanel.getRowAtIndex(selectedIndex);
	}

	public boolean hasSelectedRows() {
		return chemicalPanel.hasSelectedRows();
	}

	private void initComponents() {
		// Create a split pane with the two scroll panes in it.
		viewPanel.setPreferredSize(Constants.VERT_HALF_SCREEN_SIZE);
		chemicalPanel.setPreferredSize(Constants.VERT_HALF_SCREEN_SIZE);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chemicalPanel,
				viewPanel);
		splitPane.setOneTouchExpandable(true);
		// splitPane.setDividerLocation(300);
		splitPane.setResizeWeight(0.3);

		final ListSelectionListener l = new ListSelectionListener() {

			@Override
			public void valueChanged(final ListSelectionEvent evt) {
				if (evt.getValueIsAdjusting()) {
					return;
				}

				final ListSelectionModel model = (ListSelectionModel) evt
						.getSource();
				if (!model.isSelectionEmpty()) {
					final int selectedIndex = model.getMinSelectionIndex();
					LOG.debug("Index " + selectedIndex + " selected.");
					final ChemicalRecord record = chemicalPanel
							.getRowAtIndex(selectedIndex);
					viewPanel.start(record);
					if (collapsed) {
						collapsed = false;
						toggle(splitPane, collapsed);
					}
				} else {
					viewPanel.start(null);
					collapsed = true;
					toggle(splitPane, collapsed);
				}

			}

		};

		chemicalPanel.setSelectionListener(l);

		add(splitPane);

	}

	@Override
	public void search(final Molecule substructure) {
		chemicalPanel.search(substructure);

	}

	public void setSelectionListener(final ListSelectionListener l) {
		chemicalPanel.setSelectionListener(l);

	}

	@Override
	public void start(final String filter) {
		// viewPanel = new ChemicalViewPanel();
		chemicalPanel.start(filter);
	}
}
