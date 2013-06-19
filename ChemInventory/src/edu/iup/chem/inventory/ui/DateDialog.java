package edu.iup.chem.inventory.ui;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JDialog;
import javax.swing.JLabel;

import org.jdesktop.swingx.JXDatePicker;

import com.ibm.icu.util.Calendar;

public class DateDialog {

	private static JXDatePicker	picker	= new JXDatePicker(Calendar
												.getInstance().getTime());

	public static Date showDatePicker(final String title, final String message) {
		final JDialog dialog = new JDialog(null, ModalityType.APPLICATION_MODAL);
		final JLabel label = new JLabel(message);
		dialog.setLayout(new BorderLayout());
		dialog.setTitle(title);
		picker.setPreferredSize(new Dimension(320, 80));
		dialog.setLocationRelativeTo(null);

		picker.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				dialog.dispose();

			}
		});

		dialog.add(label, BorderLayout.NORTH);
		dialog.add(picker, BorderLayout.CENTER);

		dialog.pack();
		dialog.setVisible(true);

		return picker.getDate();
	}
}
