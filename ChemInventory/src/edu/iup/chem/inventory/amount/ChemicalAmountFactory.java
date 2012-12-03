package edu.iup.chem.inventory.amount;

import static javax.measure.unit.NonSI.LITER;
import static javax.measure.unit.SI.GRAM;
import static javax.measure.unit.SI.KILOGRAM;
import static javax.measure.unit.SI.MILLI;

public class ChemicalAmountFactory {
	public static ChemicalAmount getChemicalAmount(final String amount,
			final String unit) {
		final double quantity = Double.parseDouble(amount);

		switch (unit) {
			case "kilograms":
			case "kilogram":
			case "kg":
				return new ChemicalMass(quantity, unit, KILOGRAM);
			case "grams":
			case "gram":
			case "g":
				return new ChemicalMass(quantity, unit, GRAM);
			case "mg":
			case "milligram":
			case "milligrams":
				return new ChemicalMass(quantity, unit, MILLI(GRAM));
			case "mL":
			case "milliliter":
			case "milliliters":
				return new ChemicalVolume(quantity, unit, MILLI(LITER));
			case "L":
			case "liter":
			case "liters":
				return new ChemicalVolume(quantity, unit, LITER);
			default:
				return null;
		}
	}
}
