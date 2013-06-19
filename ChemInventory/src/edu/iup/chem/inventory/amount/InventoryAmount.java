package edu.iup.chem.inventory.amount;

import static javax.measure.unit.NonSI.LITER;
import static javax.measure.unit.SI.GRAM;
import static javax.measure.unit.SI.KILO;
import static javax.measure.unit.SI.MILLI;

import javax.measure.quantity.Mass;
import javax.measure.quantity.Volume;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

public class InventoryAmount {
	private final ChemicalMass		mass;
	private final ChemicalVolume	volume;
	private final ChemicalDensity	density;
	// flag to check if mass is the true measure
	private final boolean			massFirst;

	public InventoryAmount(final ChemicalMass mass,
			final ChemicalDensity density) {
		this.mass = mass;
		this.density = density;
		massFirst = true;
		Unit<Volume> units = MILLI(LITER);
		final Amount<Volume> amount = mass.getAmount()
				.divide(density.getAmount()).to(units);
		if (amount.isGreaterThan(Amount.valueOf(1.0, LITER))) {
			units = LITER;
		}

		volume = (ChemicalVolume) ChemicalAmountFactory.getChemicalAmount(
				amount.doubleValue(units), units.toString());
	}

	public InventoryAmount(final ChemicalVolume volume,
			final ChemicalDensity density) {
		this.volume = volume;
		this.density = density;
		Unit<Mass> units = GRAM;
		massFirst = false;
		final Amount<Mass> amount = volume.getAmount()
				.times(density.getAmount()).to(units);
		if (amount.isGreaterThan(Amount.valueOf(1.0, KILO(GRAM)))) {
			units = KILO(GRAM);
		}

		mass = (ChemicalMass) ChemicalAmountFactory.getChemicalAmount(
				amount.doubleValue(units), units.toString());

	}

	public ChemicalAmount getAmount() {
		ChemicalAmount am;
		if (massFirst) {
			am = mass;
		} else {
			am = volume;
		}

		return am;
	}

	public Amount<Mass> getMass() {
		return mass.getAmount();
	}

	public String log() {
		return "InventoryAmount ["
				+ (mass != null ? "mass=" + mass + ", " : "")
				+ (volume != null ? "volume=" + volume + ", " : "")
				+ (density != null ? "density=" + density : "") + "]";
	}

	@Override
	public String toString() {
		final String formatStr = "%.2f %s";
		final String massStr = String.format(formatStr, mass.getQuantity(),
				mass.getUnit());
		final String volStr = String.format(formatStr, volume.getQuantity(),
				volume.getUnit());

		String toStr;
		String fullFormat = "%s (~%s)";

		// Don't show other measure if specific gravity is 1.0
		if (density.getAmount().approximates(
				Amount.valueOf(1.0, KILO(GRAM).divide(LITER)))) {
			fullFormat = "%s";
		}

		if (massFirst) {
			toStr = String.format(fullFormat, massStr, volStr);
		} else {
			toStr = String.format(fullFormat, volStr, massStr);
		}

		return toStr;
	}

}
