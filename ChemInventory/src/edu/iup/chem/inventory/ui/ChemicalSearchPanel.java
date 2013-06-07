package edu.iup.chem.inventory.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Document;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ComboBoxCellEditor;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.prompt.PromptSupport;
import org.jdesktop.swingx.prompt.PromptSupport.FocusBehavior;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jooq.Cursor;
import org.jooq.Record;
import org.openscience.cdk.AtomContainer;

import uk.ac.ebi.rhea.mapper.util.lucene.LuceneSearcher;
import edu.iup.chem.inventory.Utils;
import edu.iup.chem.inventory.dao.ChemicalDao;
import edu.iup.chem.inventory.dao.LocationDao;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalNfpaS;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalStorageClass;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalToxic;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;
import edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord;
import edu.iup.chem.inventory.db.inventory.tables.records.RoomRecord;
import edu.iup.chem.inventory.index.ChemicalSearchResult;
import edu.iup.chem.inventory.index.Index;
import edu.iup.chem.inventory.lists.celleditor.DensityCellEditor;
import edu.iup.chem.inventory.lists.celleditor.MassEditor;
import edu.iup.chem.inventory.lists.comparators.ChemicalAmountComparator;
import edu.iup.chem.inventory.lists.tablemodels.ChemicalTableModel;
import edu.iup.chem.inventory.misc.Stacker;
import edu.iup.chem.inventory.search.ChemicalSubstructureSearcher;
import edu.iup.chem.inventory.wizard.InventoryWizardListener;
import edu.iup.chem.inventory.wizard.NewBottlePageFactory;

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
			} catch (final Exception e) {
				LOG.error("Error in data loader", e.getCause());
				return new ArrayList<>();
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

	private class MenuActionListener implements ActionListener {

		@Override
		public void actionPerformed(final ActionEvent e) {
			switch (e.getActionCommand()) {
				case "BOTTLE":
					showNewBottleWizard();
					break;
				case "DELETE":
					LOG.debug("Deleting selected rows.");
					deleteRows(true);
					break;
				case "ADD_MSDS":
					storeMSDS();
					break;
				case "VIEW_MSDS":
					getSelectedChemical().getMSDS();
					break;
				case "PREVIEW":
					showChemicalPanel();
					break;
				case "COMPLETE":
					markComplete();
					break;
				case "DUPLICATE":
					duplicateChemical();
					break;
				default:
					break;
			}

		}

		private void duplicateChemical() {
			final ChemicalRecord mainRec = getSelectedChemical();
			final ChemicalRecord newRec = mainRec.copy();

			ChemicalDao.store(newRec);
			fireChemicalsAdded(newRec);

		}

		private void markComplete() {
			final ChemicalRecord rec = getSelectedChemical();
			if (rec.getComplete().equals((byte) 1)) {
				rec.setComplete((byte) 0);
			} else {
				rec.setComplete((byte) 1);
			}
			ChemicalDao.store(rec);

		}

		private void showChemicalPanel() {
			final JFrame f = new JFrame();

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					final ChemicalViewPanel p = new ChemicalViewPanel(
							getSelectedChemical());
					f.add(p);
					f.pack();
					f.setVisible(true);
				}

			});

		}

		private void showNewBottleWizard() {

			final InventoryWizardDialog iwd = new InventoryWizardDialog(
					new NewBottlePageFactory(getSelectedChemical()));
			iwd.addWizardListener(new InventoryWizardListener(iwd) {

				@Override
				public void onFinished(final List<WizardPage> path,
						final WizardSettings settings) {
					iwd.dispose();

					final ChemicalRecord rec = (ChemicalRecord) settings
							.get("chemicalRecord");
					final LocationRecord loc = new LocationRecord();
					loc.setCas(rec.getCas());
					loc.setActive((byte) 0b1);
					loc.setAmount(Double.parseDouble((String) settings
							.get("amount")));
					loc.setArrival(new java.sql.Date(((java.util.Date) settings
							.get("arrival")).getTime()));
					loc.setBottle((String) settings.get("bottle"));
					loc.setExpiration(new java.sql.Date(
							((java.util.Date) settings.get("expiration"))
									.getTime()));
					loc.setInstructor((String) settings.get("instructor"));
					loc.setPartNo(0);
					loc.setRoom(((RoomRecord) settings.get("room")).getRoom());
					loc.setShelf((String) settings.get("shelf"));
					loc.setSupplier("None");
					loc.setUnits((String) settings.get("units"));
					loc.setCid(rec.getCid());

					LocationDao.store(loc);
					firePropertyChange("bottle", null, loc);

					bottleSearch = new LuceneSearcher(Index
							.getBottleDirectory());

				}

			});

			iwd.pack();
			iwd.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			iwd.setVisible(true);
		}

		private void storeMSDS() {
			final JFileChooser chooser = new JFileChooser();
			final FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"PDF files", "pdf");
			chooser.setFileFilter(filter);
			final int returnVal = chooser.showOpenDialog(chemicalTable
					.getParent());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				final File f = chooser.getSelectedFile();
				if (ChemicalDao.storeMSDS(getSelectedChemical(), f)) {
					showMessage("SDS", "SDS successfully added.");
				} else {
					showMessage("SDS", "Failed to update SDS.");
				}
			}
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

	@SuppressWarnings({ "serial", "unchecked" })
	private static TableColumnModel createColumnModel() {
		final DefaultTableColumnModel columnModel = new DefaultTableColumnModel();

		final TableCellRenderer cellRenderer = new DefaultTableCellRenderer();
		final DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);

		TableColumnExt column = new TableColumnExt();
		column.setModelIndex(ChemicalTableModel.CAS_COLUMN);
		column.setHeaderValue("CAS Number");
		column.setPreferredWidth(26);
		column.setCellRenderer(cellRenderer);
		columnModel.addColumn(column);

		// column = new TableColumnExt();
		// column.setModelIndex(ChemicalTableModel.SCORE_COLUMN);
		// column.setHeaderValue("Relevance");
		// column.setCellRenderer(centerRenderer);
		// column.setVisible(false);
		// columnModel.addColumn(column);

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
			column.setCellRenderer(centerRenderer);
			column.setCellEditor(comboEditor);
			columnModel.addColumn(column);

			column = new TableColumnExt();
			column.setModelIndex(ChemicalTableModel.NFPA_FIRE);
			column.setHeaderValue("Flammability");
			column.setPrototypeValue("0");
			column.setCellRenderer(centerRenderer);
			column.setCellEditor(comboEditor);
			columnModel.addColumn(column);

			column = new TableColumnExt();
			column.setModelIndex(ChemicalTableModel.NFPA_REACT);
			column.setHeaderValue("Reactivity");
			column.setPrototypeValue("0");
			column.setCellRenderer(centerRenderer);
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
			column.setCellRenderer(centerRenderer);
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

			// final ListComboBoxModel<ChemicalToxic> toxicModel = new
			// ListComboBoxModel<>(
			// Arrays.asList(ChemicalToxic.values()));
			// final JComboBox<ChemicalStorageClass> toxicCombo = new
			// JComboBox<>(
			// toxicModel);
			// toxicCombo.setRenderer(new DefaultListRenderer(new StringValue()
			// {
			// @Override
			// public String getString(final Object value) {
			// if (value instanceof ChemicalToxic) {
			// return ((ChemicalToxic) value).getLiteral();
			// }
			//
			// return StringValues.TO_STRING.getString(value);
			// }
			// }));
			// AutoCompleteDecorator.decorate(toxicCombo);
			// final ComboBoxCellEditor toxicEditor = new ComboBoxCellEditor(
			// toxicCombo);
			// column = new TableColumnExt();
			// column.setModelIndex(ChemicalTableModel.TOXIC_COLUMN);
			// column.setHeaderValue("Toxicity Information");
			// column.setCellRenderer(cellRenderer);
			// column.setCellEditor(toxicEditor);
			// columnModel.addColumn(column);

			column = new TableColumnExt();
			column.setModelIndex(ChemicalTableModel.TOXIC_COLUMN);
			column.setHeaderValue("LD50 (rat oral per kg)");
			column.setPreferredWidth(30);
			column.setCellRenderer(cellRenderer);
			column.setComparator(new ChemicalAmountComparator());
			column.setCellEditor(new MassEditor());
			columnModel.addColumn(column);

			final ListComboBoxModel<ChemicalStorageClass> storageModel = new ListComboBoxModel<>(
					Arrays.asList(ChemicalStorageClass.values()));
			final JComboBox<ChemicalStorageClass> storageCombo = new JComboBox<>(
					storageModel);
			storageCombo.setRenderer(new DefaultListRenderer(new StringValue() {
				@Override
				public String getString(final Object value) {
					if (value instanceof ChemicalStorageClass) {
						final ChemicalStorageClass c = (ChemicalStorageClass) value;
						return String.format("%s (%s)", c.getClassLetter(),
								c.getLiteral());
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

			column = new TableColumnExt();
			column.setModelIndex(ChemicalTableModel.MELTING_COLUMN);
			column.setHeaderValue("Melting Point (deg. C)");
			column.setCellRenderer(cellRenderer);
			columnModel.addColumn(column);

			column = new TableColumnExt();
			column.setModelIndex(ChemicalTableModel.BOILING_COLUMN);
			column.setHeaderValue("Boiling Point (deg. C)");
			column.setCellRenderer(cellRenderer);
			columnModel.addColumn(column);

			column = new TableColumnExt();
			column.setModelIndex(ChemicalTableModel.FLASH_COLUMN);
			column.setHeaderValue("Flash Point (deg. F)");
			column.setCellRenderer(cellRenderer);
			columnModel.addColumn(column);

			column = new TableColumnExt();
			column.setModelIndex(ChemicalTableModel.DENSITY_COLUMN);
			column.setHeaderValue("Density");
			column.setCellRenderer(cellRenderer);
			column.setCellEditor(new DensityCellEditor());
			column.setComparator(new ChemicalAmountComparator());
			columnModel.addColumn(column);

		}

		return columnModel;
	}

	private static JDialog getProgressBar() {
		final JDialog progressWindow = new JDialog(null,
				"Refreshing table ...", Dialog.ModalityType.APPLICATION_MODAL);
		final JProgressBar pb = new JProgressBar(0, 100);
		pb.setString("Loading");
		pb.setStringPainted(true);
		pb.setIndeterminate(true);

		final JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(pb, BorderLayout.CENTER);

		centerPanel.setPreferredSize(new Dimension(320, 80));

		progressWindow.getContentPane().add(centerPanel, BorderLayout.CENTER);
		progressWindow.setLocationRelativeTo(null);
		progressWindow.pack();

		return progressWindow;
	}

	private List<String>							filterResults		= new ArrayList<>();

	private LuceneSearcher							chemicalSearch		= new LuceneSearcher(
																				Index.getChemicalDirectory());

	private LuceneSearcher							bottleSearch		= new LuceneSearcher(
																				Index.getBottleDirectory());
	private ChemicalTableModel						chemModel;
	private JPanel									controlPanel;
	private Stacker									dataPanel;

	private JXTable									chemicalTable;
	private JTextField								filterField;
	private JButton									filterButton;
	private JButton									resetButton;

	private Box										statusBarLeft;

	private JLabel									actionStatus;

	private JLabel									tableStatus;
	private JLabel									searchStatus;

	// private Color[] rowColors;
	private String									statusLabelString;

	private String									searchLabelString;

	private String									filterString		= null;

	private TableRowSorter<ChemicalTableModel>		sorter;

	private RowFilter<ChemicalTableModel, Integer>	searchFilter;

	private AtomContainer							searchSubstructure	= null;
	private ChemicalSubstructureSearcher			subSearcher			= null;

	private JPopupMenu								popUpMenu			= null;

	private JMenuItem								completedButton;

	public ChemicalSearchPanel() {
		initModel();
		initComponents();
		initSortingFiltering();
		if (Utils.userHasEditingPerm()) {
			buildPopupMenu();
		}
	}

	public void addSelectionListener(final ListSelectionListener l) {
		chemicalTable.getSelectionModel().addListSelectionListener(l);
	}

	private void buildPopupMenu() {
		popUpMenu = new JPopupMenu();
		final ActionListener a = new MenuActionListener();
		JMenuItem menuItem = new JMenuItem("Preview Data Panel", KeyEvent.VK_P);
		menuItem.addActionListener(a);
		menuItem.setActionCommand("PREVIEW");
		popUpMenu.add(menuItem);

		completedButton = new JMenuItem("Toggle Completeness", KeyEvent.VK_C);
		completedButton.addActionListener(a);
		completedButton.setActionCommand("COMPLETE");
		popUpMenu.add(completedButton);

		popUpMenu.addSeparator();

		menuItem = new JMenuItem("Duplicate Chemical", KeyEvent.VK_U);
		menuItem.addActionListener(a);
		menuItem.setActionCommand("DUPLICATE");
		popUpMenu.add(menuItem);

		menuItem = new JMenuItem("Add New Bottle", KeyEvent.VK_B);
		menuItem.addActionListener(a);
		menuItem.setActionCommand("BOTTLE");
		popUpMenu.add(menuItem);

		menuItem = new JMenuItem("Add SDS", KeyEvent.VK_M);
		menuItem.addActionListener(a);
		menuItem.setActionCommand("ADD_MSDS");
		popUpMenu.add(menuItem);

		menuItem = new JMenuItem("View SDS", KeyEvent.VK_V);
		menuItem.addActionListener(a);
		menuItem.setActionCommand("VIEW_MSDS");
		popUpMenu.add(menuItem);

		popUpMenu.addSeparator();

		menuItem = new JMenuItem("Delete Chemical", KeyEvent.VK_D);
		menuItem.addActionListener(a);
		menuItem.setActionCommand("DELETE");
		popUpMenu.add(menuItem);

	}

	public void changeFilter(final String filter) {
		setFilterString(filter);
		searchSubstructure = null;
		configureFilters();

	}

	protected void configureFilters() {
		chemModel.clearResults();
		sorter.setRowFilter(null);
		final JDialog progress = getProgressBar();
		searchStatus.setText("	Searching...");

		final SwingWorker<Double, Double> task = new SwingWorker<Double, Double>() {

			@Override
			protected Double doInBackground() throws Exception {
				if (hasFilterString()) {
					boolean doRawSearch = false;
					if(filterString.contains(":")) {
						doRawSearch = true;
					} 
					doSearch(doRawSearch);
				} else if (hasSubStructure()) {
					findSubstructures();
				} else {
					searchStatus.setText(null);
				}

				publish(1.0);
				return 1.0;
			}

			@Override
			protected void done() {
				tableStatus.setText((hasFilterString() ? searchLabelString
						: statusLabelString) + chemicalTable.getRowCount());
				progress.dispose();
			}

		};

		task.execute();
		progress.pack();
		progress.setVisible(true);

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

		final JPanel filterPanel = new JPanel();
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));

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

		filterPanel.add(filterField);

		filterButton = new JButton("Search");
		filterButton.addActionListener(new SearchActionListener());
		filterPanel.add(filterButton);

		resetButton = new JButton("Clear");
		resetButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				changeFilter(null);
			}

		});

		filterPanel.add(resetButton);

		searchStatus = new JLabel();
		searchStatus.setHorizontalAlignment(JLabel.CENTER);

		filterPanel.add(searchStatus);

		controlPanel1.add(filterPanel, c);
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
	public void deleteRows(final boolean removeFromDB) {
		final int row = chemicalTable.getSelectedRow();
		if (row < 0) {
			showMessage("Invalid Selection", "Please select a row to remove.");
			return;
		}
		final ChemicalRecord rec = getRowAtIndex(row);
		int selection = JOptionPane.CANCEL_OPTION;
		if (removeFromDB) {
			selection = JOptionPane.showConfirmDialog(ChemicalSearchPanel.this,
					"Are you sure you want to delete " + rec.getName()
							+ " and all associated inventory records?",
					"Delete selected row?", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
		}
		if (!removeFromDB || selection == JOptionPane.YES_OPTION) {
			final int index = chemicalTable.convertRowIndexToModel(row);
			chemModel.removeChemical(index, removeFromDB);
		}
	}

	private void doSearch(final boolean doRawSearch) {
		filterResults.clear();

		if (!hasFilterString()) {
			changeFilter(null);
			return;
		}

		final Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				List<ChemicalSearchResult> chemHits = Collections.emptyList();
				if(doRawSearch) {
					chemHits = chemicalSearch.doRawSearch(filterString);
				} else {
				chemHits = chemicalSearch
						.searchCompoundName(filterString);
				}
				LOG.debug("Found " + chemHits.size() + " chemical results.");

				final String bottleFilter = filterString/*
														 * .replaceAll("[A-Za-z ]+"
														 * , "")
														 */;
				Collection<ChemicalSearchResult> bottleHits = new ArrayList<>();
				if (!(bottleFilter == null) && !bottleFilter.isEmpty()) {
					if(doRawSearch) {
					bottleHits = bottleSearch.doRawSearch(bottleFilter);	
					} else {
					bottleHits = bottleSearch.searchCompoundName(bottleFilter);
					}
					LOG.debug("Found " + bottleHits.size() + " bottle results.");
				}

				final List<ChemicalSearchResult> hits = new ArrayList<>();
				hits.addAll(chemHits);

				hits.addAll(bottleHits);

				LOG.info("Number of hits: " + hits.size());
				LOG.info("-------------------------------");
				int index = 1;
				for (final ChemicalSearchResult hit : hits) {
					filterResults.add(hit.getPrimaryKey());
					LOG.info(String.format("%2d: %s (%f)", index,
							hit.getPrimaryKey(), hit.getScore()));
					index++;
				}

				if (!filterResults.isEmpty()) {
					final HashMap<Integer, Float> results = new HashMap<>();
					LOG.debug("Filter for " + filterResults.size()
							+ " results.");
					if (filterResults.size() > 50) {
						searchStatus.setText("Search results truncated to 50.");
						filterResults = filterResults.subList(0, 50);
					} else {
						searchStatus.setText(null);
					}

					for (int i = 0; i < filterResults.size(); i++) {
						final ChemicalSearchResult hit = hits.get(i);
						results.put(Integer.parseInt(hit.getPrimaryKey()),
								hit.getScore());
					}

					sorter.setRowFilter(searchFilter);
					chemModel.setResults(results);
					sorter.toggleSortOrder(ChemicalTableModel.SCORE_COLUMN);
					sorter.toggleSortOrder(ChemicalTableModel.SCORE_COLUMN);
				} else {
					LOG.debug("Search returned no results.");
					searchStatus.setText("Search returned no results.");
					sorter.setRowFilter(null);
				}
			}

		});

		t.start();
		try {
			t.join();
		} catch (final InterruptedException e) {
			LOG.warn("Search thread interrupted.");
		}

	}

	private void findSubstructures() {
		filterResults.clear();

		if (!hasSubStructure()) {
			changeFilter(null);
			return;
		}
		final Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				final HashMap<Integer, Float> results = new HashMap<>();
				synchronized (chemModel.chemicals()) {
					for (final ChemicalRecord r : chemModel.chemicals()) {
						// if (subSearcher.isSubstructureOf(r.getSmiles())) {
						if (r.getSmiles() == null) {
							continue;
						}
						if (subSearcher.containsSMSD(r.getMolecule())) {
							LOG.debug(r.getName()
									+ " matches substructure search.");
							filterResults.add(r.getCid().toString());
							results.put(r.getCid(),
									subSearcher.getSimilarity(r.getSmiles()));
						}
					}
				}

				if (!filterResults.isEmpty()) {

					LOG.debug("Filter for " + filterResults.size()
							+ " results.");

					sorter.setRowFilter(searchFilter);
					chemModel.setResults(results);
					searchStatus.setText(null);
					sorter.toggleSortOrder(ChemicalTableModel.SCORE_COLUMN);
					sorter.toggleSortOrder(ChemicalTableModel.SCORE_COLUMN);
				} else {
					LOG.debug("Search returned no results.");
					sorter.setRowFilter(null);
					searchStatus.setText("Search returned no results.");

				}

			}

		});

		t.start();
		try {
			t.join();
		} catch (final InterruptedException e) {
			LOG.warn("Substructure search thread interrupted.");
		}

	}

	@Override
	public void fireChemicalsAdded(final ChemicalRecord rec) {
		chemModel.add(rec);
		Index.addChemical(rec);
		chemicalSearch = new LuceneSearcher(Index.getChemicalDirectory());
		configureFilters();

	}

	public void getMSDS() {
		// final JFileChooser chooser = new JFileChooser();
		// final FileNameExtensionFilter filter = new FileNameExtensionFilter(
		// "PDF files", "pdf");
		// chooser.setFileFilter(filter);
		// final int returnVal =
		// chooser.showSaveDialog(chemicalTable.getParent());
		// if (returnVal == JFileChooser.APPROVE_OPTION) {

		try {
			// final File selected = chooser.getSelectedFile();
			// File f;
			// if (selected.getCanonicalPath().endsWith("pdf")) {
			// f = selected;
			// } else {
			// f = new File(selected.getAbsolutePath() + ".pdf");
			// }
			final File f = File.createTempFile("inventory", ".pdf");
			final OutputStream outputStream = new FileOutputStream(f);
			final InputStream inputStream = ChemicalDao
					.getMSDS(getSelectedChemical().getCas());
			if (inputStream != null) {
				IOUtils.copy(inputStream, outputStream);
				outputStream.close();
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().open(f);
					} catch (final IOException ex) {
						showMessage("Error",
								"Could not open PDF.  Perhaps no program is available?");
					}
				} else {
					showMessage("Warning",
							"Unable to open MSDS, but it has been saved to disk at "
									+ f.getAbsolutePath());
				}
			} else {
				outputStream.close();
				showMessage("No MSDS",
						"No MSDS has been stored for this chemical.");
			}
		} catch (final IOException e) {
			showMessage("Warning",
					"Failed to load MSDS from database. Please try again.");
		}
	}

	public ChemicalRecord getRowAtIndex(int index) {
		if (index > chemicalTable.getRowCount()) {
			index = chemicalTable.getRowCount();
		}
		final int dataIndex = chemicalTable.convertRowIndexToModel(index);
		return chemModel.getChemical(dataIndex);
	}

	// </snip>

	@Override
	public ChemicalRecord getSelectedChemical() {
		if (hasSelectedRows()) {
			return getRowAtIndex(chemicalTable.getSelectedRow());
		}

		return null;
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

	protected void initComponents() {
		setLayout(new BorderLayout());

		controlPanel = createControlPanel();
		add(controlPanel, BorderLayout.NORTH);

		// <snip>Create JTable
		chemicalTable = new JXTable(chemModel);
		// </snip>

		// </snip>Set JTable display properties
		chemicalTable.setColumnModel(createColumnModel());
		// chemicalTable.setAutoCreateRowSorter(true);
		chemicalTable.setRowHeight(26);
		chemicalTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		chemicalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		chemicalTable.setIntercellSpacing(new Dimension(0, 0));
		chemicalTable.setShowGrid(false);
		if (Utils.userHasEditingPerm()) {
			chemicalTable.setColumnControlVisible(true);
		}
		final HighlightPredicate completePredicate = new HighlightPredicate() {

			@Override
			public boolean isHighlighted(final Component renderer,
					final ComponentAdapter adapter) {
				final ChemicalRecord rec = getRowAtIndex(adapter.row);
				return rec.isComplete();
			}
		};

		final HighlightPredicate incompletePredicate = new HighlightPredicate() {

			@Override
			public boolean isHighlighted(final Component renderer,
					final ComponentAdapter adapter) {
				final ChemicalRecord rec = getRowAtIndex(adapter.row);
				return !rec.isComplete();
			}
		};

		final ColorHighlighter completeHighlighter = new ColorHighlighter(
				completePredicate, Color.GREEN, null);
		final ColorHighlighter incompleteHighlighter = new ColorHighlighter(
				incompletePredicate, Color.PINK, null);

		if (Utils.userHasEditingPerm()) {
			chemicalTable.setHighlighters(completeHighlighter,
					incompleteHighlighter);
		} else {
			chemicalTable.addHighlighter(HighlighterFactory
					.createAlternateStriping());
		}

		chemicalTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				showPopUp(e);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				showPopUp(e);
			}

			public void showPopUp(final MouseEvent e) {
				final int r = chemicalTable.rowAtPoint(e.getPoint());
				if (r >= 0 && r < chemicalTable.getRowCount()) {
					chemicalTable.setRowSelectionInterval(r, r);
				} else {
					chemicalTable.clearSelection();
				}

				final int rowindex = chemicalTable.getSelectedRow();
				if (rowindex < 0) {
					return;
				}
				if (e.isPopupTrigger() && popUpMenu != null) {
					if (getSelectedChemical().isComplete()) {
						completedButton.setText("Mark Incomplete");
					} else {
						completedButton.setText("Mark Completed");
					}
					popUpMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
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
				return filterResults.contains(chemical.getCid().toString());
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
					final int selection = chemicalTable
							.convertRowIndexToModel(chemicalTable
									.getSelectionModel().getMinSelectionIndex());
					chemModel.update(chemicals);
					LOG.debug("Refreshed table content.");
					if (selection > -1) {
						final ChemicalRecord c = chemModel
								.getChemical(selection);

						final int index = chemModel.getRow(c);
						chemicalTable.getSelectionModel().setSelectionInterval(
								index, index);
					}

				}
			}
		}.start();

	}

	@Override
	public void search(final AtomContainer substructure) {
		searchSubstructure = substructure;
		subSearcher = new ChemicalSubstructureSearcher(substructure);
		configureFilters();
	}

	public void setFilterString(final String filter) {
		final String oldFilterString = filterString;
		filterString = filter;
		if (filterField.getText().isEmpty() || filter == null) {
			filterField.setText(filter);
		}

		LOG.debug("Filter string set to: " + filter);

		firePropertyChange("filterString", oldFilterString, filterString);
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
