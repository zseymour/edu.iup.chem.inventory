package edu.iup.chem.inventory.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.JButton;
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
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Document;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ComboBoxCellEditor;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.prompt.PromptSupport;
import org.jdesktop.swingx.prompt.PromptSupport.FocusBehavior;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jooq.Cursor;
import org.jooq.Record;
import org.openscience.cdk.Molecule;

import edu.iup.chem.inventory.Constants;
import edu.iup.chem.inventory.Utils;
import edu.iup.chem.inventory.dao.ChemicalDao;
import edu.iup.chem.inventory.dao.LocationDao;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalNfpaS;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalStorageClass;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalToxic;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;
import edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord;
import edu.iup.chem.inventory.lists.tablemodels.ChemicalTableModel;
import edu.iup.chem.inventory.misc.Stacker;
import edu.iup.chem.inventory.search.ChemicalSubstructureSearcher;

public class ChemicalSearchPanel extends DataPanel {

	private class DataLoader extends
			SwingWorker<List<ChemicalRecord>, ChemicalRecord> {

		private final List<ChemicalRecord>	chemicals	= new ArrayList<>();
		private final ChemicalTableModel	chemModel;

		public DataLoader(final ChemicalTableModel chemModel) {
			this.chemModel = chemModel;
		}

		@Override
		protected List<ChemicalRecord> doInBackground() throws Exception {
			// JXBusyLabel message = new JXBusyLabel(new Dimension(50,50));
			// message.getBusyPainter().setHighlightColor(new Color(44, 61,
			// 146).darker());
			// message.getBusyPainter().setBaseColor(new Color(168, 204,
			// 241).brighter());
			// message.getBusyPainter().setTrailLength(15);
			// message.setDelay(5);
			// message.setBusy(true);
			// dataPanel.showMessageLayer(message, 1f);
			Cursor<Record> fetched = null;
			final int size = ChemicalDao.getAllCount();
			LOG.debug("Size: " + size);
			try {
				fetched = new ChemicalDao().getAllLazy();
				while (fetched.hasNext()) {
					final ChemicalRecord c = fetched
							.fetchOneInto(ChemicalRecord.class);
					publish(c);
					chemicals.add(c);
					final int progress = 100 * chemicals.size() / size;
					// LOG.debug("Setting progress to " + progress);
					setProgress(progress);
					Thread.sleep(1);
				}

				return chemicals;
			} finally {
				if (fetched != null) {
					fetched.close();
				}
			}

		}

		@Override
		protected void done() {
			dataPanel.hideMessageLayer();
			setProgress(100);
		}

		@Override
		protected void process(final List<ChemicalRecord> moreChemicals) {
			// LOG.debug("Adding " + moreChemicals.size() +
			// " more chemicals to table.");
			chemModel.add(moreChemicals);
		}

	}

	protected class SearchActionListener implements ActionListener {

		@Override
		public void actionPerformed(final ActionEvent e) {
			configureFilters();
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
	private static final long	serialVersionUID	= -6953109918696666740L;

	private static final Logger	LOG					= Logger.getLogger(ChemicalSearchPanel.class);

	private static String buildSearchPattern(final String[] array) {
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

	@SuppressWarnings({ "serial", "unchecked" })
	private static TableColumnModel createColumnModel() {
		final DefaultTableColumnModel columnModel = new DefaultTableColumnModel();

		final TableCellRenderer cellRenderer = new DefaultTableCellRenderer();

		TableColumnExt column = new TableColumnExt();
		column.setModelIndex(ChemicalTableModel.CAS_COLUMN);
		column.setHeaderValue("CAS Number");
		column.setPreferredWidth(26);
		column.setCellRenderer(cellRenderer);
		columnModel.addColumn(column);

		column = new TableColumnExt();
		column.setModelIndex(ChemicalTableModel.NAME_COLUMN);
		column.setHeaderValue("Chemical Name");
		column.setPreferredWidth(100);
		column.setCellRenderer(cellRenderer);
		columnModel.addColumn(column);

		if (Utils.userHasEditingPerm()) {
			column = new TableColumnExt();
			column.setModelIndex(ChemicalTableModel.SMILES_COLUMN);
			column.setHeaderValue("SMILES");
			column.setPreferredWidth(120);
			column.setCellRenderer(cellRenderer);
			columnModel.addColumn(column);
		}

		column = new TableColumnExt();
		column.setModelIndex(ChemicalTableModel.FORMULA_COLUMN);
		column.setHeaderValue("Molecular Formula");
		column.setPreferredWidth(120);
		column.setCellRenderer(cellRenderer);
		columnModel.addColumn(column);

		if (Utils.userHasEditingPerm()) {
			final JComboBox<Integer> numbers = new JComboBox<>(new Integer[] {
					0, 1, 2, 3, 4 });
			AutoCompleteDecorator.decorate(numbers);
			final ComboBoxCellEditor comboEditor = new ComboBoxCellEditor(
					numbers);

			column = new TableColumnExt();
			column.setModelIndex(ChemicalTableModel.NFPA_HEALTH);
			column.setHeaderValue("Health Hazard");
			column.setPrototypeValue("0");
			column.setCellRenderer(cellRenderer);
			column.setCellEditor(comboEditor);
			columnModel.addColumn(column);

			column = new TableColumnExt();
			column.setModelIndex(ChemicalTableModel.NFPA_FIRE);
			column.setHeaderValue("Flammability");
			column.setPrototypeValue("0");
			column.setCellRenderer(cellRenderer);
			column.setCellEditor(comboEditor);
			columnModel.addColumn(column);

			column = new TableColumnExt();
			column.setModelIndex(ChemicalTableModel.NFPA_REACT);
			column.setHeaderValue("Reactivity");
			column.setPrototypeValue("0");
			column.setCellRenderer(cellRenderer);
			column.setCellEditor(comboEditor);
			columnModel.addColumn(column);

			final ListComboBoxModel<ChemicalNfpaS> specModel = new ListComboBoxModel<>(
					Arrays.asList(ChemicalNfpaS.values()));
			final JComboBox<ChemicalNfpaS> specCombo = new JComboBox<>(
					specModel);
			specCombo.setRenderer(new DefaultListRenderer(new StringValue() {
				@Override
				public String getString(final Object value) {
					if (value instanceof ChemicalToxic) {
						return ((ChemicalToxic) value).getLiteral();
					}

					return StringValues.TO_STRING.getString(value);
				}
			}));
			AutoCompleteDecorator.decorate(specCombo);
			final ComboBoxCellEditor specEditor = new ComboBoxCellEditor(
					specCombo);
			column = new TableColumnExt();
			column.setModelIndex(ChemicalTableModel.NFPA_SPECIAL);
			column.setHeaderValue("Special");
			column.setCellRenderer(cellRenderer);
			column.setCellEditor(specEditor);
			columnModel.addColumn(column);

			column = new TableColumnExt();
			column.setModelIndex(ChemicalTableModel.COLD_STORAGE);
			column.setHeaderValue("Requires Cold Storage?");
			// column.setCellRenderer(cellRenderer);
			columnModel.addColumn(column);

			column = new TableColumnExt();
			column.setModelIndex(ChemicalTableModel.FLAMMABLE_COLUMN);
			column.setHeaderValue("Is Flammable?");
			// column.setCellRenderer(cellRenderer);
			columnModel.addColumn(column);

			column = new TableColumnExt();
			column.setModelIndex(ChemicalTableModel.CARC_COLUMN);
			column.setHeaderValue("Is Carcinogenic?");
			// column.setCellRenderer(cellRenderer);
			columnModel.addColumn(column);

			final ListComboBoxModel<ChemicalToxic> toxicModel = new ListComboBoxModel<>(
					Arrays.asList(ChemicalToxic.values()));
			final JComboBox<ChemicalStorageClass> toxicCombo = new JComboBox<>(
					toxicModel);
			toxicCombo.setRenderer(new DefaultListRenderer(new StringValue() {
				@Override
				public String getString(final Object value) {
					if (value instanceof ChemicalToxic) {
						return ((ChemicalToxic) value).getLiteral();
					}

					return StringValues.TO_STRING.getString(value);
				}
			}));
			AutoCompleteDecorator.decorate(toxicCombo);
			final ComboBoxCellEditor toxicEditor = new ComboBoxCellEditor(
					toxicCombo);
			column = new TableColumnExt();
			column.setModelIndex(ChemicalTableModel.TOXIC_COLUMN);
			column.setHeaderValue("Toxicity Information");
			column.setCellRenderer(cellRenderer);
			column.setCellEditor(toxicEditor);
			columnModel.addColumn(column);

			final ListComboBoxModel<ChemicalStorageClass> storageModel = new ListComboBoxModel<>(
					Arrays.asList(ChemicalStorageClass.values()));
			final JComboBox<ChemicalStorageClass> storageCombo = new JComboBox<>(
					storageModel);
			storageCombo.setRenderer(new DefaultListRenderer(new StringValue() {
				@Override
				public String getString(final Object value) {
					if (value instanceof ChemicalStorageClass) {
						return ((ChemicalStorageClass) value).getLiteral();
					}

					return StringValues.TO_STRING.getString(value);
				}
			}));
			AutoCompleteDecorator.decorate(storageCombo);
			final ComboBoxCellEditor classEditor = new ComboBoxCellEditor(
					storageCombo);
			column = new TableColumnExt();
			column.setModelIndex(ChemicalTableModel.CLASS_COLUMN);
			column.setHeaderValue("Storage Class");
			column.setCellRenderer(cellRenderer);
			column.setCellEditor(classEditor);
			columnModel.addColumn(column);
		}

		return columnModel;
	}

	private ChemicalTableModel						chemModel;
	private JPanel									controlPanel;
	private Stacker									dataPanel;
	private JXTable									chemicalTable;

	private JTextField								filterField;
	private JButton									filterButton;
	private Box										statusBarLeft;

	private JLabel									actionStatus;

	private JLabel									tableStatus;
	// private Color[] rowColors;
	private String									statusLabelString;

	private String									searchLabelString;

	private String									filterString		= null;

	private TableRowSorter<ChemicalTableModel>		sorter;

	private RowFilter<ChemicalTableModel, Integer>	searchFilter;

	private RowFilter<ChemicalTableModel, Integer>	structureFilter;

	private Molecule								searchSubstructure	= null;

	private ChemicalSubstructureSearcher				subSearcher			= null;

	public ChemicalSearchPanel() {
		initModel();
		initComponents();
		initSortingFiltering();
	}

	public void changeFilter(final String filter) {
		setFilterString(filter);
		configureFilters();

	}

	protected void configureFilters() {
		if (hasFilterString()) {
			sorter.setRowFilter(searchFilter);
		} else if (hasSubStructure()) {
			sorter.setRowFilter(structureFilter);
		} else {
			sorter.setRowFilter(null);
		}

		tableStatus.setText((hasFilterString() ? searchLabelString
				: statusLabelString) + chemicalTable.getRowCount());

	}

	private JPanel createControlPanel() {
		final JPanel controlPanel1 = new JPanel();
		final GridBagLayout gridbag = new GridBagLayout();
		final GridBagConstraints c = new GridBagConstraints();
		controlPanel1.setLayout(gridbag);

		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 1;
		c.insets = new Insets(20, 10, 0, 10);
		c.anchor = GridBagConstraints.SOUTHWEST;
		final JLabel searchLabel = new JLabel(
				"Search CAS Number, Chemical Name, or SMILES String");
		controlPanel1.add(searchLabel, c);

		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 1.0;
		c.insets.top = 0;
		c.insets.bottom = 12;
		c.anchor = GridBagConstraints.SOUTHWEST;
		// c.fill = GridBagConstraints.HORIZONTAL;
		filterField = new JTextField(24);
		filterField.getDocument().addDocumentListener(
				new SearchFilterListener());
		PromptSupport.setPrompt("e.g. 100-10-1, benzene, CC#C, etc.",
				filterField);
		PromptSupport.setFocusBehavior(FocusBehavior.HIDE_PROMPT, filterField);
		filterField.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(final KeyEvent e) {
				LOG.debug("KEY PRESSED.");
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					LOG.debug("Enter pressed.");
					configureFilters();
				}

			}

			@Override
			public void keyReleased(final KeyEvent arg0) {

			}

			@Override
			public void keyTyped(final KeyEvent e) {

			}

		});
		controlPanel1.add(filterField, c);

		c.gridx = GridBagConstraints.RELATIVE;
		// c.gridy = GridBagConstraints.RELATIVE;
		// c.weightx = 1;
		// c.anchor = GridBagConstraints.SOUTHWEST;
		filterButton = new JButton("Search");
		filterButton.addActionListener(new SearchActionListener());
		controlPanel1.add(filterButton, c);

		return controlPanel1;
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
		chemModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(final TableModelEvent e) {
				// Get rowCount from *table*, not model, as the view row count
				// may be different from the model row count due to filtering
				tableStatus.setText((hasFilterString() ? searchLabelString
						: statusLabelString) + chemicalTable.getRowCount());
			}
		});
		// </snip>

		return statusBar;
	}

	@Override
	public void deleteRows() {
		final int row = chemicalTable.getSelectedRow();
		if (row < 0) {
			showMessage("Invalid Selection", "Please select a row to remove.");
			return;
		}
		final ChemicalRecord rec = getRowAtIndex(row);
		final int selection = JOptionPane.showConfirmDialog(
				ChemicalSearchPanel.this, "Are you sure you want to delete "
						+ rec.getName()
						+ " and all associated inventory records?",
				"Delete selected row?", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (selection == JOptionPane.YES_OPTION) {
			final int index = chemicalTable.convertRowIndexToModel(row);
			chemModel.removeChemical(index);
		}
	}

	@Override
	public void fireChemicalsAdded() {
		chemModel.fireTableDataChanged();

	}

	public ChemicalRecord getRowAtIndex(final int index) {
		final int dataIndex = chemicalTable.convertRowIndexToModel(index);
		return chemModel.getChemical(dataIndex);
	}

	protected boolean hasFilterString() {
		return filterString != null && !filterString.equals("");
	}

	public boolean hasSelectedRows() {
		return chemicalTable.getSelectedRow() >= 0;
	}

	private boolean hasSubStructure() {
		return searchSubstructure != null && !searchSubstructure.isEmpty();
	}

	// </snip>

	protected void initComponents() {
		setLayout(new BorderLayout());

		controlPanel = createControlPanel();
		add(controlPanel, BorderLayout.NORTH);

		// <snip>Create JTable
		chemicalTable = new JXTable(chemModel);
		// </snip>

		// </snip>Set JTable display properties
		chemicalTable.setColumnModel(createColumnModel());
		chemicalTable.setAutoCreateRowSorter(true);
		chemicalTable.setRowHeight(26);
		chemicalTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		chemicalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		chemicalTable.setIntercellSpacing(new Dimension(0, 0));
		chemicalTable.setShowGrid(false);
		chemicalTable.setHighlighters(HighlighterFactory
				.createAlternateStriping());
		// </snip>

		// <snip>Initialize preferred size for table's viewable area
		final Dimension viewSize = new Dimension();
		viewSize.width = chemicalTable.getColumnModel().getTotalColumnWidth();
		viewSize.height = 10 * chemicalTable.getRowHeight();
		chemicalTable.setPreferredScrollableViewportSize(viewSize);
		// </snip>

		// <snip>Customize height and alignment of table header
		final JTableHeader header = chemicalTable.getTableHeader();
		header.setPreferredSize(new Dimension(30, 26));
		final TableCellRenderer headerRenderer = header.getDefaultRenderer();
		if (headerRenderer instanceof JLabel) {
			((JLabel) headerRenderer).setHorizontalAlignment(JLabel.CENTER);
		}
		// </snip>

		final JScrollPane scrollpane = new JScrollPane(chemicalTable);
		dataPanel = new Stacker(scrollpane);
		add(dataPanel, BorderLayout.CENTER);

		add(createStatusBar(), BorderLayout.SOUTH);

	}

	protected void initModel() {
		chemModel = new ChemicalTableModel();

	}

	private void initSortingFiltering() {
		sorter = new TableRowSorter<>(chemModel);
		chemicalTable.setRowSorter(sorter);

		searchFilter = new RowFilter<ChemicalTableModel, Integer>() {
			@Override
			public boolean include(
					final Entry<? extends ChemicalTableModel, ? extends Integer> entry) {
				final ChemicalTableModel chemicalModel = entry.getModel();
				final ChemicalRecord chemical = chemicalModel.getChemical(entry
						.getIdentifier().intValue());
				final boolean matches = false;
				String pattern;
				String[] filters = null;
				if (filterString.startsWith("\"")
						&& filterString.endsWith("\"")) {
					pattern = ".*" + filterString.replace("\"", "") + ".*";
				} else if (filterString.startsWith("\"")) {
					filters = filterString.replaceAll("\"", "").split(" ");
					pattern = buildSearchPattern(filters);
				} else {
					filters = filterString.split(" ");
					pattern = buildSearchPattern(filters);
				}

				final Pattern p = Pattern.compile(pattern,
						Pattern.CASE_INSENSITIVE);
				final Pattern pSensitive = Pattern.compile(pattern);
				// LOG.debug("Filter string = " + p.pattern());
				if (filters != null && filters.length > 0) {
					final String[] symbols = Constants.CHEMICALS
							.getAll(filters);
					pattern = buildSearchPattern(symbols);
				}
				final Pattern s = Pattern.compile(pattern);
				// LOG.debug("Symbol filter = " + s.pattern());
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

				field = chemical.getFormula();
				if (field != null) {
					// Returning true indicates this row should be shown.
					if (p.matcher(field).matches()
							|| s.matcher(field).matches()) {
						return true;
					}
				}

				if (filterString.length() >= 4 && Utils.isNumeric(filterString)) {
					final List<LocationRecord> list = LocationDao
							.getByCasWhereBottleLike(chemical.getCas(),
									filterString);
					if (!list.isEmpty()) {
						return true;
					}
				}

				return matches;
			}
		};

		structureFilter = new RowFilter<ChemicalTableModel, Integer>() {

			@Override
			public boolean include(
					final javax.swing.RowFilter.Entry<? extends ChemicalTableModel, ? extends Integer> entry) {
				final ChemicalTableModel chemicalModel = entry.getModel();
				final ChemicalRecord chemical = chemicalModel.getChemical(entry
						.getIdentifier().intValue());

				return subSearcher.isSubstructureOf(chemical.getSmiles());

			}

		};

	}

	public void loadData(final String filter) {
		// create SwingWorker which will load the data on a separate thread
		final DataLoader loader = new DataLoader(chemModel);

		actionStatus.setText("Loading data: ");

		// display progress bar while data loads
		final JProgressBar progressBar = new JProgressBar();
		statusBarLeft.add(progressBar);
		loader.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				if (event.getPropertyName().equals("progress")) {
					if (!hasFilterString() && filter != null) {
						changeFilter(filter);
					}
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
						Thread.sleep(360 * 1000);
					} catch (final InterruptedException e) {
						LOG.error("Refresh thread interrupted.", e);
					}
					final List<ChemicalRecord> chemicals = new ChemicalDao()
							.getAll();
					chemModel.update(chemicals);
					LOG.debug("Refreshed table content.");

				}
			}
		}.start();

	}

	@Override
	public void search(final Molecule substructure) {
		searchSubstructure = substructure;
		subSearcher = new ChemicalSubstructureSearcher(substructure);
		configureFilters();
	}

	public void setFilterString(final String filter) {
		final String oldFilterString = filterString;
		filterString = filter;
		if (filterField.getText().isEmpty()) {
			filterField.setText(filter);
		}

		LOG.debug("Filter string set to: " + filter);

		firePropertyChange("filterString", oldFilterString, filterString);
	}

	public void setSelectionListener(final ListSelectionListener l) {
		chemicalTable.getSelectionModel().addListSelectionListener(l);
	}

	protected void showMessage(final String title, final String message) {
		JOptionPane.showMessageDialog(this, message, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void start(final String filter) {
		if (chemModel.getRowCount() == 0) {
			loadData(filter);
		} else if (filter != null) {
			changeFilter(filter);
		}

	}

}
