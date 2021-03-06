/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.iup.chem.inventory.wizard;

import java.util.Arrays;
import java.util.Calendar;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.StringValues;

import edu.iup.chem.inventory.dao.LocationDao;
import edu.iup.chem.inventory.dao.RoomDao;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;
import edu.iup.chem.inventory.db.inventory.tables.records.RoomRecord;

/**
 * 
 * @author Zach
 */
public class VerifyBottleWizardPage extends WizardPage {

	private static ChemicalRecord getNewRecord(final WizardSettings settings) {
		return (ChemicalRecord) settings.get("chemicalRecord");
	}

	private final WizardSettings				settings;

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JTextField				amount;
	private JXDatePicker						arrival;
	private javax.swing.JTextField				bottle;
	private JXDatePicker						expiration;
	private javax.swing.JTextField				instructor;
	private javax.swing.JLabel					jLabel1;
	private javax.swing.JLabel					jLabel2;
	private javax.swing.JLabel					jLabel3;
	private javax.swing.JLabel					jLabel4;
	private javax.swing.JLabel					jLabel5;
	private javax.swing.JLabel					jLabel6;
	private javax.swing.JLabel					jLabel7;
	private javax.swing.JLabel					jLabel8;
	private javax.swing.JComboBox<RoomRecord>	room;
	private javax.swing.JTextField				shelf;
	private javax.swing.JComboBox<String>		units;

	// End of variables declaration//GEN-END:variables
	/**
	 * Creates new form VerifyBottleWizardPage
	 */
	public VerifyBottleWizardPage(final WizardSettings settings) {
		super("Enter bottle information", "Please enter new bottle for "
				+ getNewRecord(settings).getName() + ".");
		this.settings = settings;
		initComponents();

	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		final Calendar cal = Calendar.getInstance();
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		shelf = new javax.swing.JTextField(3);
		instructor = new javax.swing.JTextField();
		jLabel3 = new javax.swing.JLabel();
		amount = new javax.swing.JTextField(10);
		jLabel4 = new javax.swing.JLabel();
		units = new javax.swing.JComboBox<>();
		jLabel5 = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		arrival = new JXDatePicker(cal.getTime());
		jLabel7 = new javax.swing.JLabel();
		cal.add(Calendar.YEAR, 10);
		expiration = new JXDatePicker(cal.getTime());
		jLabel8 = new javax.swing.JLabel();
		bottle = new javax.swing.JTextField(10);

		jLabel1.setText("Room Number:");
		final ListComboBoxModel<RoomRecord> roomModel = new ListComboBoxModel<>(
				RoomDao.getAllRoomRecords());
		room = new JComboBox<>(roomModel);
		room.setEditable(false);
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

		room.setSelectedItem(RoomDao.getDefaultRoom("Weyandt 146"));

		room.setName("room"); // NOI18N

		jLabel2.setText("Shelf:");

		shelf.setName("shelf"); // NOI18N

		final String[] instructors = new String[] { "Chemistry", "Physics",
				"Biology", "Geoscience", "Psychology", "Research Institute" };

		instructor.setName("instructor"); // NOI18N

		instructor.setText("Chemistry");

		AutoCompleteDecorator.decorate(instructor, Arrays.asList(instructors),
				false);

		jLabel3.setText("Instructor:");

		amount.setName("amount"); // NOI18N

		jLabel4.setText("Amount:");

		units.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {
				"mL", "L", "fl. oz", "gal", "g", "kg", "lb", "oz" }));
		units.setName("units"); // NOI18N

		jLabel5.setText("Units:");

		jLabel6.setText("Arrival Date");

		arrival.setName("arrival"); // NOI18N

		jLabel7.setText("Expiration Date:");

		expiration.setName("expiration"); // NOI18N

		jLabel8.setText("Bottle Number:");

		bottle.setName("bottle"); // NOI18N
		final String nextBottle = LocationDao.getNextAvailableBottle();
		bottle.setText(nextBottle);

		AutoCompleteDecorator
				.decorate(bottle, Arrays.asList(nextBottle), false);

		final javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(
														layout.createSequentialGroup()
																.addGap(0,
																		0,
																		Short.MAX_VALUE)
																.addComponent(
																		jLabel8)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																.addComponent(
																		bottle,
																		javax.swing.GroupLayout.PREFERRED_SIZE,
																		159,
																		javax.swing.GroupLayout.PREFERRED_SIZE))
												.addGroup(
														layout.createSequentialGroup()
																.addContainerGap()
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.LEADING,
																				false)
																				.addComponent(
																						arrival)
																				.addGroup(
																						layout.createSequentialGroup()
																								.addGroup(
																										layout.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.LEADING)
																												.addComponent(
																														jLabel1)
																												.addComponent(
																														room,
																														javax.swing.GroupLayout.PREFERRED_SIZE,
																														javax.swing.GroupLayout.DEFAULT_SIZE,
																														javax.swing.GroupLayout.PREFERRED_SIZE))
																								.addGap(18,
																										18,
																										18)
																								.addGroup(
																										layout.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.LEADING)
																												.addComponent(
																														jLabel2)
																												.addComponent(
																														shelf,
																														javax.swing.GroupLayout.PREFERRED_SIZE,
																														57,
																														javax.swing.GroupLayout.PREFERRED_SIZE)))
																				.addComponent(
																						jLabel6))
																.addGap(18, 18,
																		18)
																.addGroup(
																		layout.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.LEADING)
																				.addGroup(
																						layout.createSequentialGroup()
																								.addGroup(
																										layout.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.LEADING,
																												false)
																												.addComponent(
																														jLabel3,
																														javax.swing.GroupLayout.DEFAULT_SIZE,
																														60,
																														Short.MAX_VALUE)
																												.addComponent(
																														instructor))
																								.addPreferredGap(
																										javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																								.addGroup(
																										layout.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.LEADING)
																												.addComponent(
																														jLabel4)
																												.addComponent(
																														amount))
																								.addPreferredGap(
																										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																								.addGroup(
																										layout.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.LEADING)
																												.addComponent(
																														jLabel5)
																												.addComponent(
																														units,
																														javax.swing.GroupLayout.PREFERRED_SIZE,
																														javax.swing.GroupLayout.DEFAULT_SIZE,
																														javax.swing.GroupLayout.PREFERRED_SIZE)))
																				.addGroup(
																						layout.createSequentialGroup()
																								.addGap(10,
																										10,
																										10)
																								.addGroup(
																										layout.createParallelGroup(
																												javax.swing.GroupLayout.Alignment.LEADING)
																												.addGroup(
																														layout.createSequentialGroup()
																																.addComponent(
																																		jLabel7)
																																.addGap(0,
																																		0,
																																		Short.MAX_VALUE))
																												.addComponent(
																														expiration))))))
								.addGap(37, 37, 37)));
		layout.setVerticalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(jLabel1)
												.addComponent(jLabel2)
												.addComponent(jLabel3)
												.addComponent(jLabel4)
												.addComponent(jLabel5))
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(
														room,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														shelf,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														instructor,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														amount,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														units,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGap(18, 18, 18)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(jLabel6)
												.addComponent(jLabel7))
								.addPreferredGap(
										javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(
														expiration,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(
														arrival,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE))
								.addGap(18, 18, 18)
								.addGroup(
										layout.createParallelGroup(
												javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(
														bottle,
														javax.swing.GroupLayout.PREFERRED_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jLabel8))
								.addContainerGap(153, Short.MAX_VALUE)));
	}// </editor-fold>//GEN-END:initComponents

	@Override
	public void updateSettings(final WizardSettings newSettings) {
		super.updateSettings(newSettings);

		final String bottleNo = (String) newSettings.get("bottle");

		if (LocationDao.exists(bottleNo)) {
			JOptionPane
					.showMessageDialog(this,
							"A bottle with that number already exists in our database.");
			settings.put("next", 0);
			return;
		}

	}
}
