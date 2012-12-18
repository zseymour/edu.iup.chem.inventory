package edu.iup.chem.inventory.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.openscience.cdk.Molecule;

import edu.iup.chem.inventory.Constants;
import edu.iup.chem.inventory.Utils;
import edu.iup.chem.inventory.dao.AccessDao;
import edu.iup.chem.inventory.dao.ChemicalDao;
import edu.iup.chem.inventory.dao.LocationDao;
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
import edu.iup.chem.inventory.search.ChemicalSubstructureSearcher;
import edu.iup.chem.inventory.wizard.InventoryWizardListener;
import edu.iup.chem.inventory.wizard.NewBottlePageFactory;
import edu.iup.chem.inventory.wizard.NewChemicalPageFactory;
import edu.iup.chem.inventory.wizard.NewUserPageFactory;

public class MainFrame extends JFrame {
	private class MenuActionListener implements ActionListener {

		@Override
		public void actionPerformed(final ActionEvent e) {
			switch (e.getActionCommand()) {
				case "NEW_CHEMICAL":
					showNewChemicalWizard();
					break;
				case "NEW_BOTTLE":
					showNewBottleWizard();
					break;
				case "DELETE":
					LOG.debug("Deleting selected rows.");
					deleteRows();
					break;
				case "NEW_USER":
					LOG.debug("Launching create user dialog");
					showNewUserWizard();
					break;
				case "SUB_SEARCH":
					showSketchDialog();
					break;
				default:
					break;
			}

		}

		private void deleteRows() {
			search.deleteRows();

		}

		private void showNewBottleWizard() {
			final InventoryWizardDialog iwd = new InventoryWizardDialog(
					new NewBottlePageFactory());
			iwd.addWizardListener(new InventoryWizardListener(iwd) {

				@Override
				public void onFinished(final List<WizardPage> path,
						final WizardSettings settings) {
					iwd.dispose();
					if (DEBUG) {
						log.debug("WizardSettings: " + settings);
						return;
					}

					final ChemicalRecord rec = (ChemicalRecord) settings
							.get("chemicalRecord");
					final LocationRecord loc = new LocationRecord();
					loc.setCas(rec.getCas());
					loc.setActive((byte) 0b1);
					loc.setAmount(Double.parseDouble((String) settings
							.get("amount")));
					loc.setArrival((Date) settings.get("arrival"));
					loc.setBottleNo(Integer.parseInt((String) settings
							.get("bottle")));
					loc.setExpiration((Date) settings.get("expiration"));
					loc.setInstructor((String) settings.get("instructor"));
					loc.setPartNo(0);
					loc.setRoom(((RoomRecord) settings.get("room")).getRoom());
					loc.setShelf((String) settings.get("shelf"));
					loc.setSupplier("None");
					loc.setUnits((String) settings.get("units"));

					LocationDao.store(loc);
					search.fireChemicalsAdded();

				}

			});

			iwd.pack();
			iwd.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			iwd.setVisible(true);
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
					final ChemicalRecord rec = (ChemicalRecord) settings
							.get("chemicalRecord");
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

					log.debug(rec.log());

					ChemicalDao.store(rec);
					search.fireChemicalsAdded();
				}

			});

			iwd.pack();
			iwd.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			iwd.setVisible(true);
		}

		private void showNewUserWizard() {
			final InventoryWizardDialog iwd = new InventoryWizardDialog(
					new NewUserPageFactory());
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

					final List<Object> objects = (List<Object>) settings
							.get("rooms");
					final List<RoomRecord> rooms = new ArrayList<>();

					for (final Object o : objects) {
						rooms.add((RoomRecord) o);
					}

					if (!rooms.isEmpty()) {
						rec.setRoom(rooms.get(0));
					}

					log.debug("Adding user: " + rec.getUsername());
					UserDao.store(rec);
					AccessDao.grantUserAccess(rec, rooms);

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
						final Molecule substructure = (Molecule) evt
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

			sketch.setVisible(true);

		}
	}

	private static Logger			log					= Logger.getLogger(MainFrame.class);

	/**
	 * 
	 */
	private static final long		serialVersionUID	= 987106188401169152L;
	private static final Logger		LOG					= Logger.getLogger(MainFrame.class);
	private static final boolean	DEBUG				= true;

	private JTabbedPane				pane;
	private DataPanel				search;

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

		button = newMenuButton("Add New Bottle", "NEW_BOTTLE");
		toolBar.add(button);

		button = newMenuButton("Delete Selected Item(s)", "DELETE");
		toolBar.add(button);

		if (Utils.isAdmin()) {
			button = newMenuButton("Add New User", "NEW_USER");
			toolBar.add(button);
		}

	}

	public void init() {
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

	}

	private JButton newMenuButton(final String label, final String action) {
		final JButton button = new JButton();
		button.setText(label);
		button.setActionCommand(action);
		button.addActionListener(new MenuActionListener());

		return button;
	}
}
