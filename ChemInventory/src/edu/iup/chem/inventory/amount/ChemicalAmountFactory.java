package edu.iup.chem.inventory.amount;

import static javax.measure.unit.NonSI.GALLON_LIQUID_US;
import static javax.measure.unit.NonSI.LITER;
import static javax.measure.unit.NonSI.OUNCE;
import static javax.measure.unit.NonSI.OUNCE_LIQUID_US;
import static javax.measure.unit.NonSI.POUND;
import static javax.measure.unit.SI.GRAM;
import static javax.measure.unit.SI.KILOGRAM;
import static javax.measure.unit.SI.MILLI;

import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

public class ChemicalAmountFactory {
	public static ChemicalAmount getChemicalAmount(final Double quantity,
			final String unit) {
		final String unitSwitch = unit.trim().replaceAll("\\.", "");
		switch (unitSwitch.toLowerCase()) {
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
			case "ml":
				return new ChemicalVolume(quantity, unit, MILLI(LITER));
			case "L":
			case "liter":
			case "liters":
			case "l":
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
			case "fl oz":
			case "fluid ounce":
			case "fluid ounces":
				return new ChemicalVolume(quantity, unit, OUNCE_LIQUID_US);
			case "specific gravity":
				return new ChemicalDensity(quantity * 1000, unit,
						VolumetricDensity.UNIT);
			case "g/cu cm":
			case "g/mL":
			case "g/ml":
			case "g/cc":
			case "g/cu":
			case "G/ML":
				return new ChemicalDensity(quantity, "g/mL",
						(Unit<VolumetricDensity>) GRAM.divide(MILLI(LITER)));
			case "g/L":
			case "g/l":
				return new ChemicalDensity(quantity, "g/L",
						(Unit<VolumetricDensity>) GRAM.divide(LITER));
			case "g/cu m":
				return new ChemicalDensity(quantity, "g/m3",
						(Unit<VolumetricDensity>) GRAM.divide(SI.CUBIC_METRE));
			case "mg/ml":
				return new ChemicalDensity(quantity, "mg/mL",
						(Unit<VolumetricDensity>) MILLI(GRAM).divide(
								MILLI(LITER)));
			default:
				return null;
		}
	}

	public static ChemicalAmount getChemicalAmount(final String amount,
			final String unit) {
		final double quantity = Double.parseDouble(amount);

		return getChemicalAmount(quantity, unit);
	}
}
