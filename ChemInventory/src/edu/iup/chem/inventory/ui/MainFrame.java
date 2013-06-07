package edu.iup.chem.inventory.ui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.jdesktop.swingx.JXFrame;
import org.openscience.cdk.AtomContainer;

import com.ibm.icu.util.Calendar;

import edu.iup.chem.inventory.Constants;
import edu.iup.chem.inventory.Utils;
import edu.iup.chem.inventory.csv.CSVBottle;
import edu.iup.chem.inventory.csv.InventoryCSV;
import edu.iup.chem.inventory.dao.ChemicalDao;
import edu.iup.chem.inventory.dao.LocationDao;
import edu.iup.chem.inventory.dao.RoomDao;
import edu.iup.chem.inventory.dao.UserDao;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalCarc;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalCold;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalFlamm;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalNfpaS;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalStorageClass;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalToxic;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;
import edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord;
import edu.iup.chem.inventory.db.inventory.tables.records.RoleRecord;
import edu.iup.chem.inventory.db.inventory.tables.records.RoomRecord;
import edu.iup.chem.inventory.db.inventory.tables.records.UserRecord;
import edu.iup.chem.inventory.misc.ListDialog;
import edu.iup.chem.inventory.reporting.GasReportGenerator;
import edu.iup.chem.inventory.reporting.ReportGenerator;
import edu.iup.chem.inventory.reporting.ReportGenerator.ReportType;
import edu.iup.chem.inventory.search.ChemicalSubstructureSearcher;
import edu.iup.chem.inventory.wizard.InventoryWizardListener;
import edu.iup.chem.inventory.wizard.NewChemicalPageFactory;
import edu.iup.chem.inventory.wizard.NewUserPageFactory;
import edu.iup.chem.inventory.wizard.WastePageFactory;

public class MainFrame extends JXFrame {
	private class MenuActionListener implements ActionListener {

		@Override
		public void actionPerformed(final ActionEvent e) {
			switch (e.getActionCommand()) {
				case "NEW_CHEMICAL":
					showNewChemicalWizard();
					break;
				case "NEW_USER":
					LOG.debug("Launching create user dialog");
					showNewUserWizard();
					break;
				case "SUB_SEARCH":
					showSketchDialog();
					break;
				case "MANAGE_USER":
					showUserManagement();
					break;
				case "IMPORT":
					importBottles();
					break;
				case "WASTE":
					showWasteDialog();
					break;
				case "REPORT":
					viewReport();
					break;
				default:
					break;
			}

		}

		private void importBottles() {
			final Object[] rooms = RoomDao.getAllRoomNames().toArray();
			final String room = (String) JOptionPane.showInputDialog(
					MainFrame.this, "Select room to add bottles to: ",
					"Import to Room", JOptionPane.QUESTION_MESSAGE, null,
					rooms, "Weyandt 146");
			if (room == null) {
				return;
			}
			final JFileChooser chooser = new JFileChooser();
			final FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"Comma-separated files", "csv");
			chooser.setFileFilter(filter);
			final int returnVal = chooser.showOpenDialog(MainFrame.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				final File f = chooser.getSelectedFile();

				final List<CSVBottle> bottles = InventoryCSV
						.loadBottlesFromFile(f, room);
				final JTextArea message = new JTextArea();
				message.setLineWrap(true);
				message.setWrapStyleWord(true);
				message.setColumns(30);
				for (final String result : LocationDao.importBottles(bottles)) {
					message.append(result + "\n");
				}

				JOptionPane.showMessageDialog(null, message, "Import Results",
						JOptionPane.INFORMATION_MESSAGE);
			}

		}

		private void showNewChemicalWizard() {
			final InventoryWizardDialog iwd = new InventoryWizardDialog(
					new NewChemicalPageFactory());
			iwd.addWizardListener(new InventoryWizardListener(iwd) {

				@Override
				public void onFinished(final List<WizardPage> path,
						final WizardSettings settings) {
					iwd.dispose();
					if (DEBUG) {
						log.debug("WizardSettings: " + settings);
						return;
					}
					ChemicalRecord rec = (ChemicalRecord) settings
							.get("chemicalRecord");
					if (rec == null) {
						rec = new ChemicalRecord();
						rec.setCas((String) settings.get("cas"));
					}
					rec.setName((String) settings.get("name"));
					rec.setFormula((String) settings.get("formula"));
					rec.setSmiles((String) settings.get("smiles"));

					if ((boolean) settings.get("cold")) {
						rec.setCold(ChemicalCold.Yes);
					} else {
						rec.setCold(ChemicalCold.No);
					}

					if ((boolean) settings.get("carc")) {
						rec.setCarc(ChemicalCarc.Yes);
					} else {
						rec.setCarc(ChemicalCarc.No);
					}

					if ((boolean) settings.get("flamm")) {
						rec.setFlamm(ChemicalFlamm.Yes);
					} else {
						rec.setFlamm(ChemicalFlamm.No);
					}

					rec.setStorageClass((ChemicalStorageClass) settings
							.get("storage_class"));
					rec.setToxic((ChemicalToxic) settings.get("toxic"));

					rec.setNfpaF((Integer) settings.get("nfpaf"));
					rec.setNfpaH((Integer) settings.get("nfpah"));
					rec.setNfpaR((Integer) settings.get("nfpar"));
					rec.setNfpaS((ChemicalNfpaS) settings.get("nfpas"));

					rec.setComplete((byte) 0);

					final File msds = (File) settings.get("msds");

					rec.setCid(null);

					log.debug(rec.log());

					rec = ChemicalDao.store(rec);

					// if (rec.getCid() == null) {
					// rec.setCid(ChemicalDao.lastID());
					// }

					ChemicalDao.storeNames(rec);
					if (msds != null) {
						ChemicalDao.storeMSDS(rec, msds);
					}

					search.fireChemicalsAdded(rec);
				}

			});

			iwd.pack();
			iwd.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			iwd.setVisible(true);
		}

		private void showNewUserWizard() {
			final InventoryWizardDialog iwd = new InventoryWizardDialog(
					new NewUserPageFactory(null));
			iwd.addWizardListener(new InventoryWizardListener(iwd) {

				@Override
				public void onFinished(final List<WizardPage> path,
						final WizardSettings settings) {
					iwd.dispose();
					if (DEBUG) {
						log.debug("WizardSettings: " + settings);
						return;
					}
					final UserRecord rec = new UserRecord();
					rec.setName((String) settings.get("name"));
					rec.setUsername((String) settings.get("username"));
					final String password = (String) settings.get("password");
					final String hashedPassword = Utils.md5(password
							.toCharArray());
					rec.setPass(hashedPassword);
					rec.setEmail((String) settings.get("email"));

					final RoleRecord role = (RoleRecord) settings.get("role");
					rec.setRid(role);
					rec.setRoleName(role);

					final java.util.Date expiration = (Date) settings
							.get("expiration");
					rec.setExpirationFromUtil(expiration);

					final List<Object> objects = (List<Object>) settings
							.get("rooms");
					final List<RoomRecord> rooms = new ArrayList<>();

					for (final Object o : objects) {
						rooms.add((RoomRecord) o);
					}

					if (!rooms.isEmpty()) {
						rec.setRoom(rooms.get(0));
					}

					log.info("Adding user: " + rec.getUsername());
					UserDao.storeUserAndAccess(rec, rooms);

				}

			});

			iwd.pack();
			iwd.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			iwd.setVisible(true);

		}

		private void showSketchDialog() {
			final StructureSketchDialog sketch = new StructureSketchDialog(
					MainFrame.this);
			sketch.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

			sketch.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(final PropertyChangeEvent evt) {
					log.debug("Detected property change.");
					if (evt.getPropertyName().equals("sketch")) {
						final AtomContainer substructure = (AtomContainer) evt
								.getNewValue();
						sketch.dispose();
						log.debug("Searching for "
								+ ChemicalSubstructureSearcher
										.getSMILESFromMolecule(substructure)
								+ " from SketchPane.");
						search.search(substructure);

					}

				}

			});

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					sketch.setVisible(true);
				}

			});

		}

		private void showUserManagement() {
			final UserManagementDialog umd = new UserManagementDialog();
			umd.pack();
			umd.setModalityType(ModalityType.APPLICATION_MODAL);
			umd.setVisible(true);

		}

		private void showWasteDialog() {
			final InventoryWizardDialog iwd = new InventoryWizardDialog(
					new WastePageFactory());
			iwd.addWizardListener(new InventoryWizardListener(iwd) {

				@Override
				public void onFinished(final List<WizardPage> path,
						final WizardSettings settings) {
					iwd.dispose();
					if (DEBUG) {
						log.debug("WizardSettings: " + settings);
						return;
					}
					final LocationRecord rec = new LocationRecord();
					rec.setCas("000-00-0");
					rec.setCid(1);
					String bottle = (String) settings.get("bottle");

					if (bottle == null) {
						return;
					}

					if (!bottle.startsWith("W")) {
						bottle = "W" + bottle;
					}

					rec.setBottle(bottle);

					final String description = (String) settings
							.get("description");

					rec.setDescription(description);

					rec.setActive((byte) 1);
					rec.setAmount(Double.parseDouble((String) settings
							.get("amount")));
					rec.setArrival(new java.sql.Date(((java.util.Date) settings
							.get("arrival")).getTime()));
					final java.util.Date arrival = (java.util.Date) settings
							.get("arrival");
					rec.setArrival(new java.sql.Date(arrival.getTime()));
					final Calendar cal = Calendar.getInstance();
					cal.setTime(arrival);
					cal.add(Calendar.MONTH, 1);
					rec.setExpiration(new java.sql.Date(cal.getTimeInMillis()));
					rec.setInstructor("Chemistry");
					rec.setPartNo(0);
					rec.setRoom(((RoomRecord) settings.get("room")).getRoom());
					rec.setShelf((String) settings.get("shelf"));
					rec.setSupplier("None");
					rec.setUnits((String) settings.get("units"));

					LocationDao.store(rec);

				}

			});

			iwd.pack();
			iwd.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			iwd.setVisible(true);

		}

		private void viewReport() {
			final String[] options = new String[] { "Waste Report",
					"Gas Cylinder Report", "Storage Class Report by Room",
					"List of Chemicals in Room", "Room 126A Report",
					"Room 146 Report" };
			final String reportSelection = (String) JOptionPane
					.showInputDialog(MainFrame.this,
							"Which report would you like to view?",
							"Select Report", JOptionPane.QUESTION_MESSAGE,
							null, options, options[0]);

			if (reportSelection == null) {
				return;
			}

			switch (reportSelection) {
				case "Waste Report":
					reports.generateReport(ReportType.WASTE);
					final int response = JOptionPane
							.showOptionDialog(
									null,
									"Would you like to delete records for these waste bottles now?",
									"Remove waste records?",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE, null,
									new String[] { "Now", "Later" }, "Now");
					if (response == JOptionPane.YES_OPTION) {
						final String[] allWaste = Utils
								.intArrayToStringArray(LocationDao
										.getAllWasteBottles());
						final List<String> bottlesToSave = ListDialog
								.showDialog(null, MainFrame.this,
										"Select waste bottles to retain: ",
										"Current Waste Bottles", allWaste,
										allWaste[0], null);
						if (bottlesToSave.size() == allWaste.length) {
							return;
						} else if (!bottlesToSave.isEmpty()) {
							LocationDao
									.clearAllWasteBottlesExcept(bottlesToSave);
						} else {
							LocationDao.clearAllWasteBottles();
						}
					}
					break;
				case "Gas Cylinder Report":
					final Date start = DateDialog.showDatePicker(
							"Choose start date",
							"Select earliest arrival date:");
					final Date end = DateDialog
							.showDatePicker("Choose end date",
									"Select latest expiration date:");
					try {
						final File f = File.createTempFile("inventory", ".xls");
						final boolean success = GasReportGenerator
								.createReport(start, end, f);

						if (success) {
							if (Desktop.isDesktopSupported()) {
								try {
									Desktop.getDesktop().open(f);
								} catch (final IOException ex) {
									Utils.showMessage("Error",
											"Could not open spreadsheet.  Perhaps no program is available?");
								}
							} else {
								Utils.showMessage("Warning",
										"Unable to open gas report, but it has been saved to disk at "
												+ f.getAbsolutePath());
							}
						} else {
							Utils.showMessage("Empty report",
									"No bottles were found in this date range.");
						}

					} catch (final IOException e) {
						Utils.showMessage("Warning",
								"Failed to load gas cylinders from database. Please try again.");
					}

					break;
				case "Storage Class Report by Room":
					final Object[] rooms = LocationDao.getRoomsWithBottles()
							.toArray();
					final String roomSelection = (String) JOptionPane
							.showInputDialog(MainFrame.this,
									"Which room would you like to view?",
									"Select Room",
									JOptionPane.QUESTION_MESSAGE, null, rooms,
									rooms[0]);
					if (roomSelection != null) {
						reports.generateStorageReport(roomSelection);
					}
					break;
				case "List of Chemicals in Room":
					final Object[] roomArray = LocationDao
							.getRoomsWithBottles().toArray();
					final String room = (String) JOptionPane.showInputDialog(
							MainFrame.this,
							"Which room would you like to view?",
							"Select Room", JOptionPane.QUESTION_MESSAGE, null,
							roomArray, roomArray[0]);
					if (room != null) {
						reports.generateRoomReport(room);
					}
					break;
				case "Room 126A Report":
					reports.generateReport(ReportType.ROOM_126A);
				case "Room 146 Report":
					reports.generateReport(ReportType.ROOM_146);
				default:
					break;
			}
		}
	}

	private static Logger			log					= Logger.getLogger(MainFrame.class);

	/**
	 * 
	 */
	private static final long		serialVersionUID	= 987106188401169152L;
	private static final Logger		LOG					= Logger.getLogger(MainFrame.class);
	private static final boolean	DEBUG				= false;

	private JTabbedPane				pane;
	private DataPanel				search;

	private ReportGenerator			reports;

	public MainFrame() {
		super();
	}

	private void buildAdminSearchPane() {
		search = new ChemicalInventorySplitPane();
		search.setPreferredSize(new Dimension(1200, 700));
		add(search, BorderLayout.CENTER);
		search.start(null);

	}

	private void buildBasicSearchPane() {
		final ChemicalViewSplitPane chemSearch = new ChemicalViewSplitPane();
		add(chemSearch);
		chemSearch.start(null);
	}

	private void buildSearchBar(final JToolBar searchBar) {
		final JButton button = newMenuButton("Structure Search", "SUB_SEARCH");
		searchBar.add(button);

	}

	private void buildSplitSearchPane() {
		search = new ChemicalMultiSplitPane();
		search.setPreferredSize(new Dimension(1200, 700));
		add(search, BorderLayout.CENTER);
		search.start(null);

	}

	private void buildToolBar(final JToolBar toolBar) {
		JButton button = newMenuButton("Add New Chemical", "NEW_CHEMICAL");
		toolBar.add(button);

		button = newMenuButton("Add Waste Bottle", "WASTE");
		toolBar.add(button);

		button = newMenuButton("Import Bottles from CSV", "IMPORT");
		toolBar.add(button);

		if (Utils.isAdmin()) {
			button = newMenuButton("Add New User", "NEW_USER");
			toolBar.add(button);
			button = newMenuButton("Manage Users/Access", "MANAGE_USER");
			toolBar.add(button);

			button = newMenuButton("View Report...", "REPORT");
			toolBar.add(button);
		}

	}

	public void init() {
		reports = new ReportGenerator();
		setTitle("IUP Chemical Inventory");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		setPreferredSize(Constants.SCREEN_SIZE);
		setSize(Constants.SCREEN_SIZE); // default size is 0,0
		// setLocation(10,200); // default is 0,0 (top left corner)
		final GraphicsEnvironment env = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		setMaximizedBounds(env.getMaximumWindowBounds());
		setExtendedState(getExtendedState() | Frame.MAXIMIZED_BOTH);
		final JPanel toolBarPanel = new JPanel();
		toolBarPanel
				.setLayout(new BoxLayout(toolBarPanel, BoxLayout.LINE_AXIS));

		final JToolBar toolBar = new JToolBar();
		buildToolBar(toolBar);

		// add(toolBar, BorderLayout.PAGE_START);

		String role = Constants.CURRENT_USER.getRoleName();
		if (role == null) {
			role = "guest";
		}
		switch (role) {
			case Constants.ADMIN_ROLE:
			case Constants.DATA_ENTRY_ROLE:
			case Constants.SITE_ADMIN_ROLE:

				toolBarPanel.add(toolBar);

				buildAdminSearchPane();
				break;
			case Constants.FACULTY_ROLE:
			case Constants.RESEARCHER_ROLE:
				buildSplitSearchPane();
				break;
			default:
				buildBasicSearchPane();
				break;
		}

		final JToolBar searchBar = new JToolBar();
		buildSearchBar(searchBar);
		toolBarPanel.add(searchBar);
		add(toolBarPanel, BorderLayout.PAGE_START);
		// This is a lousy place to have to put this, but it will have to do.
		UserDao.clearExpiredUsers();

	}

	private JButton newMenuButton(final String label, final String action) {
		final JButton button = new JButton();
		button.setText(label);
		button.setActionCommand(action);
		button.addActionListener(new MenuActionListener());

		return button;
	}
}
