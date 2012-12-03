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

public class ChemicalInventorySplitPane extends DataPanel {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1870213331984230869L;
	private static final Logger	LOG					= Logger.getLogger(ChemicalInventorySplitPane.class);

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
	private final InventorySearchPanel	inventoryPanel;

	private JSplitPane					splitPane;

	public ChemicalInventorySplitPane() {
		chemicalPanel = new ChemicalSearchPanel();
		inventoryPanel = new InventorySearchPanel();
		initComponents();
	}

	@Override
	public void deleteRows() {
		if (chemicalPanel.hasSelectedRows()
				&& !inventoryPanel.hasSelectedRows()) {
			chemicalPanel.deleteRows();
		} else if (inventoryPanel.hasSelectedRows()) {
			inventoryPanel.deleteRows();
		}

	}

	@Override
	public void fireChemicalsAdded() {
		chemicalPanel.fireChemicalsAdded();

	}

	private void initComponents() {
		// Create a split pane with the two scroll panes in it.
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chemicalPanel,
				inventoryPanel);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(300);
		splitPane.setResizeWeight(0.3);
		chemicalPanel.setPreferredSize(Constants.HALF_SCREEN_SIZE);
		inventoryPanel.setPreferredSize(Constants.QUARTER_SCREEN_SIZE);
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
					inventoryPanel.start(record.getCas());
					if (collapsed) {
						collapsed = false;
						toggle(splitPane, collapsed);
					}
				} else {
					inventoryPanel.start(null);
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

	@Override
	public void start(final String filter) {
		chemicalPanel.start(filter);
	}

}
