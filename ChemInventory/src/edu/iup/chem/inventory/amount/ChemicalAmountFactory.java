package edu.iup.chem.inventory.amount;

import static javax.measure.unit.NonSI.GALLON_LIQUID_US;
import static javax.measure.unit.NonSI.LITER;
import static javax.measure.unit.NonSI.OUNCE;
import static javax.measure.unit.NonSI.OUNCE_LIQUID_US;
import static javax.measure.unit.NonSI.POUND;
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
			case "pound":
			case "pounds":
			case "lb":
			case "lbs":
				return new ChemicalMass(quantity, unit, POUND);
			case "oz":
			case "ounce":
			case "ounces":
				return new ChemicalMass(quantity, unit, OUNCE);
			case "gal":
			case "gals":
			case "gallon":
			case "gallons":
				return new ChemicalVolume(quantity, unit, GALLON_LIQUID_US);
			case "fl. oz":
			case "fluid ounce":
			case "fluid ounces":
				return new ChemicalVolume(quantity, unit, OUNCE_LIQUID_US);
			default:
				return null;
		}
	}
}
