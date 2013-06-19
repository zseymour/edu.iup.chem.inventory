package edu.iup.chem.inventory.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.exolab.jms.tools.admin.ChangePasswordDialog;

import edu.iup.chem.inventory.Utils;
import edu.iup.chem.inventory.dao.UserDao;
import edu.iup.chem.inventory.db.inventory.tables.records.RoleRecord;
import edu.iup.chem.inventory.db.inventory.tables.records.RoomRecord;
import edu.iup.chem.inventory.db.inventory.tables.records.UserRecord;
import edu.iup.chem.inventory.wizard.InventoryWizardListener;
import edu.iup.chem.inventory.wizard.NewUserPageFactory;

public class UserManagementDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -5940122456485929379L;
	private static final Logger	LOG					= Logger.getLogger(UserManagementDialog.class);

	private static void showUserDialog(final UserRecord user) {
		final InventoryWizardDialog iwd = new InventoryWizardDialog(
				new NewUserPageFactory(user));
		iwd.addWizardListener(new InventoryWizardListener(iwd) {

			@Override
			public void onFinished(final List<WizardPage> path,
					final WizardSettings settings) {
				iwd.dispose();

				final UserRecord rec = new UserRecord();
				rec.setName((String) settings.get("name"));
				rec.setUsername((String) settings.get("username"));
				rec.setPass(user.getPass());
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

				LOG.info("Updating user: " + rec.getUsername());
				UserDao.storeUserAndAccess(rec, rooms);

			}

		});

		iwd.pack();
		iwd.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		iwd.setVisible(true);

	}

	private final List<UserRecord>	users;
	private final JList<UserRecord>	userList;
	private final JButton			manageButton;

	private final JButton			deleteButton;
	private final JButton			passwordButton;

	public UserManagementDialog() {
		manageButton = new JButton();
		manageButton.setActionCommand("MANAGE");
		manageButton.setText("Manage Selected User");
		manageButton.addActionListener(this);
		manageButton.setEnabled(false);

		deleteButton = new JButton();
		deleteButton.setActionCommand("DELETE");
		deleteButton.setText("Delete Selected User");
		deleteButton.addActionListener(this);
		deleteButton.setEnabled(false);

		passwordButton = new JButton();
		passwordButton.setActionCommand("PASSWORD");
		passwordButton.setText("Change User Password");
		passwordButton.addActionListener(this);
		passwordButton.setEnabled(false);

		users = UserDao.getAll();
		final DefaultListModel<UserRecord> model = new DefaultListModel<>();
		for (final UserRecord u : users) {
			model.addElement(u);
		}
		userList = new JList<>(model);
		userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		initComponents();
	}

	@Override
	public void actionPerformed(final ActionEvent e) {

		final UserRecord user = userList.getSelectedValue();

		LOG.debug("Action fired: " + e.getActionCommand());
		switch (e.getActionCommand()) {
			case "MANAGE":
				showUserDialog(user);
				break;
			case "DELETE":
				removeUserAndAccess(user);
				break;
			case "PASSWORD":
				changePassword(user);
				break;
			default:
				break;
		}

	}

	private void changePassword(final UserRecord user) {
		final ChangePasswordDialog pd = ChangePasswordDialog.create(this);
		pd.displayChangePassword(user.getName());

		if (pd.isConfirmed()) {
			final String pass = pd.getPassword();
			final String hashPassword = Utils.md5(pass.toCharArray());

			user.setPass(hashPassword);
			UserDao.store(user);
		}

	}

	private void initComponents() {
		setLayout(new BorderLayout());
		final JToolBar toolbar = new JToolBar();
		toolbar.add(manageButton);
		toolbar.add(deleteButton);
		toolbar.add(passwordButton);
		add(toolbar, BorderLayout.PAGE_START);

		final ListSelectionListener l = new ListSelectionListener() {

			@Override
			public void valueChanged(final ListSelectionEvent evt) {
				if (evt.getValueIsAdjusting()) {
					return;
				}

				LOG.debug("Selected Index: " + userList.getSelectedIndex());
				final ListSelectionModel model = ((JList) evt.getSource())
						.getSelectionModel();
				if (!model.isSelectionEmpty()) {
					manageButton.setEnabled(true);
					deleteButton.setEnabled(true);
					passwordButton.setEnabled(true);
				} else {
					manageButton.setEnabled(false);
					deleteButton.setEnabled(false);
					passwordButton.setEnabled(false);
				}

			}

		};

		userList.addListSelectionListener(l);

		add(userList, BorderLayout.CENTER);

	}

	private void removeUserAndAccess(final UserRecord user) {

		final int selection = JOptionPane
				.showConfirmDialog(this,
						"Are you sure you would like to delete this user and all access?");

		if (selection == JOptionPane.YES_OPTION) {
			UserDao.deleteUser(user);
			((DefaultListModel) userList.getModel()).remove(userList
					.getSelectedIndex());
		}
	}

}
