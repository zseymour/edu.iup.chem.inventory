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

	private final boolean				collapsed	= false;
	private final ChemicalSearchPanel	chemicalPanel;
	private final ChemicalViewPanel		viewPanel;

	private JSplitPane					splitPane;

	public ChemicalViewSplitPane() {
		chemicalPanel = new ChemicalSearchPanel();
		viewPanel = new ChemicalViewPanel();
		initComponents();
	}

	public void addSelectionListener(final ListSelectionListener l) {
		chemicalPanel.addSelectionListener(l);

	}

	@Override
	public void deleteRows(final boolean removeFromDB) {

	}

	@Override
	public void fireChemicalsAdded(final ChemicalRecord rec) {
		chemicalPanel.fireChemicalsAdded(rec);
	}

	public ChemicalRecord getRowAtIndex(final int selectedIndex) {
		return chemicalPanel.getRowAtIndex(selectedIndex);
	}

	@Override
	public ChemicalRecord getSelectedChemical() {
		return chemicalPanel.getSelectedChemical();
	}

	public boolean hasSelectedRows() {
		return chemicalPanel.hasSelectedRows();
	}

	private void initComponents() {
		final JPanel chemCard = new JPanel(new CardLayout());
		chemCard.add(chemicalPanel, "list");
		chemCard.add(Utils.getTextPanel("No chemicals to show"), "empty");

		final JPanel viewCard = new JPanel(new CardLayout());
		viewCard.add(viewPanel, "list");
		viewCard.add(
				Utils.getTextPanel("Please select a chemical to view additional information"),
				"empty");

		// Create a split pane with the two scroll panes in it.
		viewCard.setPreferredSize(Constants.VERT_HALF_SCREEN_SIZE);
		chemCard.setPreferredSize(Constants.VERT_HALF_SCREEN_SIZE);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chemCard,
				viewCard);
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
					((CardLayout) viewCard.getLayout()).show(viewCard, "list");
				} else {
					viewPanel.start(null);
					((CardLayout) viewCard.getLayout()).show(viewCard, "empty");
				}

			}

		};

		chemicalPanel.addSelectionListener(l);
		((CardLayout) viewCard.getLayout()).show(viewCard, "empty");
		add(splitPane);

	}

	@Override
	public void search(final AtomContainer substructure) {
		chemicalPanel.search(substructure);

	}

	@Override
	public void start(final String filter) {
		// viewPanel = new ChemicalViewPanel();
		chemicalPanel.start(filter);
	}
}
