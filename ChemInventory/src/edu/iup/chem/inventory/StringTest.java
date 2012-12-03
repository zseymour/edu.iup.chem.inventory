package edu.iup.chem.inventory;

public class StringTest {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final String typeName = "LD50 Rat oral";
		String ratLD50 = "LD50 Rat oral range from 3523 mg/kg to 8600 mg/kg. /Mixed Xylenes/";

		ratLD50 = ratLD50.replace("range from", "");

		ratLD50 = ratLD50
				.substring(typeName.length() + 1, ratLD50.indexOf("/"));

		final String[] fields = ratLD50.split("[\\s]+");
		final int index = fields[0].indexOf("-");
		if (index >= 0) {
			fields[0] = fields[0].substring(0, index);
		}
		fields[0] = fields[0].replace(",", "");

		final double d = Double.parseDouble(fields[0]);

	}

}
