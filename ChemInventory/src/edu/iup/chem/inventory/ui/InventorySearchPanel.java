package edu.iup.chem.inventory.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.text.Document;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ComboBoxCellEditor;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.FormatStringValue;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.jdesktop.swingx.table.DatePickerCellEditor;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openscience.cdk.Molecule;

import edu.iup.chem.inventory.dao.LocationDao;
import edu.iup.chem.inventory.dao.RoomDao;
import edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord;
import edu.iup.chem.inventory.db.inventory.tables.records.RoomRecord;
import edu.iup.chem.inventory.lists.celleditor.AmountCellEditor;
import edu.iup.chem.inventory.lists.comparators.ChemicalAmountComparator;
import edu.iup.chem.inventory.lists.tablemodels.LocationTableModel;
import edu.iup.chem.inventory.misc.Stacker;

public class InventorySearchPanel extends DataPanel {

	private class DataLoader extends
			SwingWorker<List<LocationRecord>, LocationRecord> {

		private final List<LocationRecord>	chemicals	= new ArrayList<>();
		private final LocationTableModel	invModel;
		private final String				cas;

		public DataLoader(final LocationTableModel invModel, final String cas) {
			this.invModel = invModel;
			this.cas = cas;
		}

		@Override
		protected List<LocationRecord> doInBackground() throws Exception {
			List<LocationRecord> fetched = null;
			final int size = LocationDao.getAllCountWhere(cas);
			LOG.debug("Size: " + size);

			new LocationDao();
			fetched = LocationDao.getAllWhere(cas);
			for (final LocationRecord c : fetched) {
				publish(c);
				chemicals.add(c);
				final int progress = 100 * chemicals.size() / size;
				// LOG.debug("Setting progress to " + progress);
				setProgress(progress);
				Thread.sleep(1);
			}
			// We need to close the Connection associated with the cursor

			return chemicals;

		}

		@Override
		protected void done() {
			dataPanel.hideMessageLayer();
			setProgress(100);
		}

		@Override
		protected void process(final List<LocationRecord> moreChemicals) {
			// LOG.debug("Adding " + moreChemicals.size() +
			// " more chemicals to table.");
			invModel.add(moreChemicals);
		}

	}

	protected class SearchFilterListener implements DocumentListener {
		@Override
		public void changedUpdate(final DocumentEvent e) {
			changeFilter(e);
		}

		protected void changeFilter(final DocumentEvent event) {
			final Document document = event.getDocument();
			try {
				setFilterString(document.getText(0, document.getLength()));

			} catch (final Exception ex) {
				ex.printStackTrace();
				System.err.println(ex);
			}
		}

		@Override
		public void insertUpdate(final DocumentEvent e) {
			changeFilter(e);
		}

		@Override
		public void removeUpdate(final DocumentEvent e) {
			changeFilter(e);
		}
	}

	/**
	 * 
	 */
	private static final long						serialVersionUID	= 8404219483536160218L;

	private static final Logger						LOG					= Logger.getLogger(InventorySearchPanel.class);
	private LocationTableModel						invModel;
	private Stacker									dataPanel;
	private JPanel									controlPanel;
	private JXTable									invTable;
	private JTextField								filterField;
	private Box										statusBarLeft;

	private JLabel									actionStatus;
	private JLabel									tableStatus;

	// private Color[] rowColors;
	private String									statusLabelString;

	private String									searchLabelString;

	private String									filterString		= null;

	private RowFilter<LocationTableModel, Integer>	searchFilter;

	public InventorySearchPanel() {
		initModel();
		initComponents();
		// initSortingFiltering();
	}

	private String buildSearchPattern(final String[] array) {
		final StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		for (final String element : array) {
			if (element == null || element.trim().isEmpty()) {
				continue;
			}
			if (firstTime) {
				firstTime = false;
			} else {
				sb.append("|");
			}
			sb.append("(.*" + element.trim() + ".*)");
		}
		return sb.toString();
	}

	protected void configureFilters() {
		if (hasFilterString()) {
			invTable.setRowFilter(searchFilter);
		} else {
			invTable.setRowFilter(null);
		}

		tableStatus.setText((hasFilterString() ? searchLabelString
				: statusLabelString) + invTable.getRowCount());

	}

	@SuppressWarnings("unchecked")
	private TableColumnModel createColumnModel() {
		final DefaultTableColumnModel columnModel = new DefaultTableColumnModel();

		final TableCellRenderer cellRenderer = new DefaultTableCellRenderer();

		TableColumnExt column = new TableColumnExt();
		column.setModelIndex(LocationTableModel.BOTTLE_COLUMN);
		column.setHeaderValue("Bottle Number");
		column.setPreferredWidth(45);
		column.setCellRenderer(cellRenderer);
		columnModel.addColumn(column);

		column = new TableColumnExt();
		column.setModelIndex(LocationTableModel.CAS_COLUMN);
		column.setHeaderValue("CAS Number");
		column.setPreferredWidth(26);
		column.setCellRenderer(cellRenderer);
		columnModel.addColumn(column);

		column = new TableColumnExt();
		column.setModelIndex(LocationTableModel.NAME_COLUMN);
		column.setHeaderValue("Chemical Name");
		column.setPreferredWidth(100);
		column.setCellRenderer(cellRenderer);
		columnModel.addColumn(column);

		final ListComboBoxModel<RoomRecord> roomModel = new ListComboBoxModel<>(
				RoomDao.getAllRoomRecords());
		final JComboBox<RoomRecord> room = new JComboBox<>(roomModel);
		room.setRenderer(new DefaultListRenderer(new StringValue() {
			@Override
			public String getString(final Object value) {
				if (value instanceof RoomRecord) {
					return ((RoomRecord) value).getRoom();
				}

				return StringValues.TO_STRING.getString(value);
			}
		}));
		AutoCompleteDecorator.decorate(room);
		final ComboBoxCellEditor roomEditor = new ComboBoxCellEditor(room);
		column = new TableColumnExt();
		column.setModelIndex(LocationTableModel.ROOM_COLUMN);
		column.setHeaderValue("Room");
		column.setPreferredWidth(180);
		column.setCellRenderer(cellRenderer);
		column.setCellEditor(roomEditor);
		columnModel.addColumn(column);

		column = new TableColumnExt();
		column.setModelIndex(LocationTableModel.SHELF_COLUMN);
		column.setHeaderValue("Shelf");
		column.setPreferredWidth(120);
		column.setCellRenderer(cellRenderer);
		columnModel.addColumn(column);

		column = new TableColumnExt();
		column.setModelIndex(LocationTableModel.INSTRUCTOR_COLUMN);
		column.setHeaderValue("Instructor");
		column.setPreferredWidth(120);
		column.setCellRenderer(cellRenderer);
		columnModel.addColumn(column);

		column = new TableColumnExt();
		column.setModelIndex(LocationTableModel.AMOUNT_COLUMN);
		column.setHeaderValue("Amount");
		column.setPreferredWidth(30);
		column.setCellRenderer(cellRenderer);
		column.setComparator(new ChemicalAmountComparator());
		column.setCellEditor(new AmountCellEditor());
		columnModel.addColumn(column);

		final StringValue stringValue = new FormatStringValue(
				DateFormat.getDateInstance(DateFormat.FULL));
		final TableCellRenderer dateCellRenderer = new DefaultTableRenderer(
				stringValue, JLabel.RIGHT);
		final TableCellEditor dateCellEditor = new DatePickerCellEditor();

		column = new TableColumnExt();
		column.setModelIndex(LocationTableModel.ARRIVAL_COLUMN);
		column.setHeaderValue("Arrival Date");
		column.setPreferredWidth(120);
		column.setCellRenderer(dateCellRenderer);
		column.setCellEditor(dateCellEditor);
		columnModel.addColumn(column);

		column = new TableColumnExt();
		column.setModelIndex(LocationTableModel.EXP_COLUMN);
		column.setHeaderValue("Expiration Date");
		column.setPreferredWidth(120);
		column.setCellRenderer(dateCellRenderer);
		column.setCellEditor(dateCellEditor);
		columnModel.addColumn(column);

		return columnModel;
	}

	@SuppressWarnings("unused")
	private JPanel createControlPanel() {
		final JPanel controlPanel = new JPanel();
		final GridBagLayout gridbag = new GridBagLayout();
		final GridBagConstraints c = new GridBagConstraints();
		controlPanel.setLayout(gridbag);

		c.gridheight = 1;

		c.insets.top = 0;
		c.insets.bottom = 12;

		c.gridx = 2;
		c.gridy = 2;
		c.weightx = 1;
		c.anchor = GridBagConstraints.BASELINE;

		return controlPanel;
	}

	private Container createStatusBar() {
		statusLabelString = "Showing ";
		searchLabelString = "Search found ";

		final Box statusBar = Box.createHorizontalBox();

		// Left status area
		statusBar.add(Box.createRigidArea(new Dimension(10, 22)));
		statusBarLeft = Box.createHorizontalBox();
		statusBar.add(statusBarLeft);
		actionStatus = new JLabel("No data loaded");
		actionStatus.setHorizontalAlignment(JLabel.LEADING);
		statusBarLeft.add(actionStatus);

		// Middle (should stretch)
		statusBar.add(Box.createHorizontalGlue());
		statusBar.add(Box.createHorizontalGlue());
		statusBar.add(Box.createVerticalGlue());

		// Right status area
		tableStatus = new JLabel(statusLabelString + "0");
		statusBar.add(tableStatus);
		statusBar.add(Box.createHorizontalStrut(12));

		// <snip>Track number of rows currently displayed
		invModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(final TableModelEvent e) {
				// Get rowCount from *table*, not model, as the view row count
				// may be different from the model row count due to filtering
				tableStatus.setText((hasFilterString() ? searchLabelString
						: statusLabelString) + invTable.getRowCount());
			}
		});
		// </snip>

		return statusBar;
	}

	@Override
	public void deleteRows() {
		final int row = invTable.getSelectedRow();
		if (row < 0) {
			showMessage("Invalid Selection", "Please select a row to remove.");
			return;
		}
		final LocationRecord rec = getRowAtIndex(row);
		final int selection = JOptionPane.showConfirmDialog(
				InventorySearchPanel.this,
				"Are you sure you want to delete Bottle #"
						+ rec.getBottleNo().toString() + "?",
				"Delete selected row?", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (selection == JOptionPane.YES_OPTION) {
			final int index = invTable.convertRowIndexToModel(row);
			invModel.removeChemical(index);
		}
	}

	@Override
	public void fireChemicalsAdded() {
		invModel.fireTableDataChanged();
	}

	private String getFilterRegex() {
		String pattern;
		String[] filters = null;
		if (filterString.startsWith("\"") && filterString.endsWith("\"")) {
			pattern = ".*" + filterString.replace("\"", "") + ".*";
		} else if (filterString.startsWith("\"")) {
			filters = filterString.replaceAll("\"", "").split(" ");
			pattern = buildSearchPattern(filters);
		} else if (filterString.contains(" ")) {
			filters = filterString.split(" ");
			pattern = buildSearchPattern(filters);
		} else {
			pattern = ".*" + filterString + ".*";
		}

		return pattern;
	}

	private LocationRecord getRowAtIndex(final int row) {
		final int dataIndex = invTable.convertRowIndexToModel(row);
		return invModel.getChemical(dataIndex);
	}

	protected boolean hasFilterString() {
		return filterString != null && !filterString.equals("");
	}

	public boolean hasSelectedRows() {
		return invTable.getSelectedRow() >= 0;
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		// controlPanel = createControlPanel();
		// add(controlPanel, BorderLayout.NORTH);

		// <snip>Create JTable
		invTable = new JXTable(invModel);
		// </snip>

		// </snip>Set JTable display properties
		invTable.setColumnModel(createColumnModel());
		invTable.setAutoCreateRowSorter(true);
		invTable.setSortOrder(LocationTableModel.CAS_COLUMN,
				SortOrder.ASCENDING);
		invTable.setRowHeight(26);
		invTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		invTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		invTable.setIntercellSpacing(new Dimension(0, 0));
		// invTable.setColumnControlVisible(true);
		invTable.setShowGrid(false);
		invTable.setHighlighters(HighlighterFactory.createAlternateStriping());
		// </snip>

		// <snip>Initialize preferred size for table's viewable area
		final Dimension viewSize = new Dimension();
		viewSize.width = invTable.getColumnModel().getTotalColumnWidth();
		viewSize.height = 10 * invTable.getRowHeight();
		invTable.setPreferredScrollableViewportSize(viewSize);
		// </snip>

		// <snip>Customize height and alignment of table header
		final JTableHeader header = invTable.getTableHeader();
		header.setPreferredSize(new Dimension(30, 26));
		final TableCellRenderer headerRenderer = header.getDefaultRenderer();
		if (headerRenderer instanceof JLabel) {
			((JLabel) headerRenderer).setHorizontalAlignment(JLabel.CENTER);
		}
		// </snip>

		final JScrollPane scrollpane = new JScrollPane(invTable);
		dataPanel = new Stacker(scrollpane);
		add(dataPanel, BorderLayout.CENTER);

		add(createStatusBar(), BorderLayout.SOUTH);

	}

	private void initModel() {
		invModel = new LocationTableModel();
	}

	@SuppressWarnings("unused")
	private void initSortingFiltering() {

		searchFilter = new RowFilter<LocationTableModel, Integer>() {
			@Override
			public boolean include(
					final Entry<? extends LocationTableModel, ? extends Integer> entry) {
				final LocationTableModel chemicalModel = entry.getModel();
				final LocationRecord chemical = chemicalModel.getChemical(entry
						.getIdentifier().intValue());

				final boolean matches = false;
				final String pattern = getFilterRegex();

				final Pattern p = Pattern.compile(pattern,
						Pattern.CASE_INSENSITIVE);
				// LOG.debug("Filter string = " + p.pattern());

				String field = chemical.getCas();
				if (field != null) {
					// Returning true indicates this row should be shown.
					if (p.matcher(field).matches()) {
						return true;
					}
				}

				field = chemical.getName();
				if (field != null) {
					// Returning true indicates this row should be shown.
					if (p.matcher(field).matches()) {
						return true;
					}
				}

				return matches;
			}
		};

	}

	public void loadData(final String filter) {
		// create SwingWorker which will load the data on a separate thread
		final DataLoader loader = new DataLoader(invModel, filter);

		actionStatus.setText("Loading data: ");

		// display progress bar while data loads
		final JProgressBar progressBar = new JProgressBar();
		statusBarLeft.add(progressBar);
		loader.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				if (event.getPropertyName().equals("progress")) {
					final int progress = ((Integer) event.getNewValue())
							.intValue();
					// LOG.debug("Progress set to " + progress);
					progressBar.setValue(progress);

					if (progress == 100) {
						statusBarLeft.remove(progressBar);
						actionStatus.setText("");
						revalidate();
					}
				}
			}
		});
		loader.execute();

		// Launch thread to keep table in sync with database
		new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(120 * 1000);
					} catch (final InterruptedException e) {
						LOG.error("Refresh thread interrupted.", e);
					}
					if (hasFilterString()) {
						final List<LocationRecord> chemicals = LocationDao
								.getAllWhere(filterString);
						invModel.update(chemicals);
						LOG.debug("Refreshed table content.");
					}
				}
			}
		}.start();

	}

	@Override
	public void search(final Molecule substructure) {
		// TODO Auto-generated method stub

	}

	public void setFilterString(final String filterString) {
		// String oldFilterString = this.filterString;
		this.filterString = filterString;
		if (filterField.getText().isEmpty()) {
			filterField.setText(filterString);
		}
		configureFilters();
		// firePropertyChange("filterString", oldFilterString, filterString);
	}

	protected void showMessage(final String title, final String message) {
		JOptionPane.showMessageDialog(this, message, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void start(final String filter) {
		filterString = filter;
		invModel.update(null);
		if (filter == null) {
			// There's no data to load
			return;
		}
		loadData(filter);
	}

}
