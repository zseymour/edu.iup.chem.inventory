package edu.iup.chem.inventory.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
import org.openscience.cdk.AtomContainer;

import com.ibm.icu.util.Calendar;

import edu.iup.chem.inventory.Utils;
import edu.iup.chem.inventory.dao.LocationDao;
import edu.iup.chem.inventory.dao.RoomDao;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;
import edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord;
import edu.iup.chem.inventory.db.inventory.tables.records.RoomRecord;
import edu.iup.chem.inventory.index.Index;
import edu.iup.chem.inventory.lists.celleditor.AmountCellEditor;
import edu.iup.chem.inventory.lists.comparators.InventoryAmountComparator;
import edu.iup.chem.inventory.lists.tablemodels.LocationTableModel;
import edu.iup.chem.inventory.misc.Stacker;

public class InventorySearchPanel extends DataPanel {

	private class DataLoader extends
			SwingWorker<List<LocationRecord>, LocationRecord> {

		private final List<LocationRecord>	chemicals	= new ArrayList<>();
		private final LocationTableModel	invModel;
		private final Integer				cid;

		public DataLoader(final LocationTableModel invModel, final Integer id) {
			this.invModel = invModel;
			cid = id;
		}

		@Override
		protected List<LocationRecord> doInBackground() throws Exception {
			List<LocationRecord> fetched = null;
			final int size = LocationDao.getAllCountWhere(cid);
			LOG.debug("Size: " + size);

			fetched = LocationDao.getAllWhere(cid);
			for (final LocationRecord c : fetched) {
				publish(c);
				chemicals.add(c);
				final int progress = 100 * chemicals.size() / size;
				// LOG.debug("Setting progress to " + progress);
				setProgress(progress);
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

	private class MenuActionListener implements ActionListener {

		@Override
		public void actionPerformed(final ActionEvent e) {
			switch (e.getActionCommand()) {
				case "DELETE":
					LOG.debug("Deleting selected rows.");
					deleteRows(true);
					break;
				case "DUPE":
					duplicateBottle();
					break;
				case "WASTE":
					markWaste();
					deleteRows(false);
					break;
				case "GAS":
					markGas();
					break;
				default:
					break;
			}

		}

		private void duplicateBottle() {
			final LocationRecord mainRec = getSelectedBottle();
			final LocationRecord newRec = new LocationRecord();

			newRec.setBottle(LocationDao.getNextAvailableBottle());
			newRec.setAmount(mainRec.getAmount());
			newRec.setUnits(mainRec.getUnits());
			newRec.setArrival(mainRec.getArrival());
			newRec.setExpiration(mainRec.getExpiration());
			newRec.setCas(mainRec.getCas());
			newRec.setInstructor(mainRec.getInstructor());
			newRec.setRoom(mainRec.getRoom());
			newRec.setShelf(mainRec.getShelf());
			newRec.setActive(mainRec.getActive());
			newRec.setPartNo(mainRec.getPartNo());
			newRec.setSupplier(mainRec.getSupplier());
			newRec.setCid(mainRec.getCid());

			LocationDao.store(newRec);
			fireBottleAdded(newRec);

		}

		private void markGas() {
			final LocationRecord rec = getSelectedBottle();
			rec.setType("G");
			rec.setExpiration(new java.sql.Date(Calendar.getInstance()
					.getTimeInMillis()));
			LocationDao.store(rec);
		}

		private void markWaste() {
			final LocationRecord rec = getSelectedBottle();
			rec.setType("W");
			rec.setExpiration(new java.sql.Date(Calendar.getInstance()
					.getTimeInMillis()));
			LocationDao.store(rec);
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

	// private Color[] rowColors;
	private String									statusLabelString;

	private String									searchLabelString;

	private String									filterString		= null;

	private RowFilter<LocationTableModel, Integer>	searchFilter;

	private JPopupMenu								popUpMenu;

	public InventorySearchPanel() {
		initModel();
		initComponents();
		if (Utils.userHasEditingPerm()) {
			buildPopupMenu();
		}
	}

	private void buildPopupMenu() {
		popUpMenu = new JPopupMenu();
		final ActionListener a = new MenuActionListener();
		JMenuItem menuItem = new JMenuItem("Delete Bottle", KeyEvent.VK_D);
		menuItem.addActionListener(a);
		menuItem.setActionCommand("DELETE");
		popUpMenu.add(menuItem);

		popUpMenu.addSeparator();

		menuItem = new JMenuItem("Duplicate Bottle", KeyEvent.VK_B);
		menuItem.addActionListener(a);
		menuItem.setActionCommand("DUPE");
		popUpMenu.add(menuItem);

		menuItem = new JMenuItem("Mark as Waste", KeyEvent.VK_W);
		menuItem.addActionListener(a);
		menuItem.setActionCommand("WASTE");
		popUpMenu.add(menuItem);

		menuItem = new JMenuItem("Mark as Gas Cylinder", KeyEvent.VK_G);
		menuItem.addActionListener(a);
		menuItem.setActionCommand("GAS");
		popUpMenu.add(menuItem);
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
		column.setComparator(new InventoryAmountComparator());
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

		if (Utils.userHasEditingPerm()) {
			column = new TableColumnExt();
			column.setModelIndex(LocationTableModel.ACTIVE_COLUMN);
			column.setHeaderValue("Active");
			// column.setCellRenderer(cellRenderer);
			columnModel.addColumn(column);
		}

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

	@Override
	public void deleteRows(final boolean deleteFromDB) {
		final int row = invTable.getSelectedRow();
		if (row < 0) {
			showMessage("Invalid Selection", "Please select a row to remove.");
			return;
		}
		final LocationRecord rec = getRowAtIndex(row);
		int selection = JOptionPane.CANCEL_OPTION;
		if (deleteFromDB) {
			selection = JOptionPane.showConfirmDialog(
					InventorySearchPanel.this,
					"Are you sure you want to delete Bottle #"
							+ rec.getBottle() + "?", "Delete selected row?",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
		}
		if (!deleteFromDB || selection == JOptionPane.YES_OPTION) {
			final int index = invTable.convertRowIndexToModel(row);
			invModel.removeChemical(index, deleteFromDB);
		}
	}

	public void fireBottleAdded(final LocationRecord rec) {
		invModel.add(rec);
		Index.addBottle(rec);

	}

	@Override
	public void fireChemicalsAdded(final ChemicalRecord rec) {
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

	public LocationRecord getSelectedBottle() {
		if (hasSelectedRows()) {
			return getRowAtIndex(invTable.getSelectedRow());
		}

		return null;
	}

	@Override
	public ChemicalRecord getSelectedChemical() {
		throw new UnsupportedOperationException();
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
		invTable.setSortOrder(LocationTableModel.AMOUNT_COLUMN,
				SortOrder.DESCENDING);
		invTable.setRowHeight(26);
		invTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		invTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		invTable.setIntercellSpacing(new Dimension(0, 0));
		// invTable.setColumnControlVisible(true);
		invTable.setShowGrid(false);
		invTable.setHighlighters(HighlighterFactory.createAlternateStriping());
		// </snip>

		invTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				showPopUp(e);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				showPopUp(e);
			}

			public void showPopUp(final MouseEvent e) {
				final int r = invTable.rowAtPoint(e.getPoint());
				if (r >= 0 && r < invTable.getRowCount()) {
					invTable.setRowSelectionInterval(r, r);
				} else {
					invTable.clearSelection();
				}

				final int rowindex = invTable.getSelectedRow();
				if (rowindex < 0) {
					return;
				}
				if (e.isPopupTrigger() && popUpMenu != null) {
					popUpMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

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
		final DataLoader loader = new DataLoader(invModel,
				Integer.parseInt(filter));

		loader.execute();

	}

	@Override
	public void search(final AtomContainer substructure) {
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
