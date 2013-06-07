package edu.iup.chem.inventory;

import java.util.Date;

import edu.iup.chem.inventory.ui.DateDialog;

public class StringTest {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final Date d = DateDialog.showDatePicker("Hi Addie!",
				"You're cool on this date:");
		System.out.println(d.toString());
		System.exit(0);
	}

}
