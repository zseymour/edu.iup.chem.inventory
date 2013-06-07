package edu.iup.chem.inventory.ui;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import org.apache.log4j.Logger;
import org.openscience.cdk.AtomContainer;

import edu.iup.chem.inventory.Constants;
import edu.iup.chem.inventory.Utils;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;

public class ChemicalMultiSplitPane extends DataPanel {
	/**
	 * 
	 */
	private static final long		serialVersionUID	= 860775838680723526L;
	protected static final Logger	LOG					= Logger.getLogger(ChemicalMultiSplitPane.class);

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

	private final boolean				collapsed	= false;
	private final ChemicalViewSplitPane	chemicalPanel;

	private final InventorySearchPanel	inventoryPanel;

	private JSplitPane					splitPane;

	public ChemicalMultiSplitPane() {
		chemicalPanel = new ChemicalViewSplitPane();
		inventoryPanel = new InventorySearchPanel();
		initComponents();
	}

	@Override
	public void deleteRows(final boolean removeFromDB) {

	}

	@Override
	public void fireChemicalsAdded(final ChemicalRecord rec) {
		chemicalPanel.fireChemicalsAdded(rec);
		inventoryPanel.fireChemicalsAdded(rec);
	}

	@Override
	public ChemicalRecord getSelectedChemical() {
		return chemicalPanel.getSelectedChemical();
	}

	private void initComponents() {
		final JPanel chemCard = new JPanel(new CardLayout());
		chemCard.add(chemicalPanel, "list");
		chemCard.add(Utils.getTextPanel("No chemicals to show"), "empty");

		final JPanel invCard = new JPanel(new CardLayout());
		invCard.add(inventoryPanel, "list");
		invCard.add(
				Utils.getTextPanel("Please select a chemical to view available inventory"),
				"empty");
		// Create a split pane with the two scroll panes in it.
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chemCard, invCard);
		splitPane.setOneTouchExpandable(true);
		// splitPane.setDividerLocation(300);
		splitPane.setResizeWeight(0.3);
		chemCard.setPreferredSize(Constants.HALF_SCREEN_SIZE);
		invCard.setPreferredSize(Constants.QUARTER_SCREEN_SIZE);
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
					inventoryPanel.start(record.getCid().toString());
					((CardLayout) invCard.getLayout()).show(invCard, "list");
				} else {
					inventoryPanel.start(null);
					((CardLayout) invCard.getLayout()).show(invCard, "empty");
				}

			}

		};

		chemicalPanel.addSelectionListener(l);
		((CardLayout) invCard.getLayout()).show(invCard, "empty");
		add(splitPane);

	}

	@Override
	public void search(final AtomContainer substructure) {
		chemicalPanel.search(substructure);

	}

	@Override
	public void start(final String filter) {
		chemicalPanel.start(filter);
	}

}
