package edu.iup.chem.inventory.amount;

import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

public class ChemicalDensity extends ChemicalAmount {

	private final String					unit;
	private final double					quantity;
	private final Amount<VolumetricDensity>	amount;

	public ChemicalDensity(final double quantity, final String unit,
			final Unit<VolumetricDensity> units) {
		this.unit = unit;
		this.quantity = quantity;
		amount = Amount.valueOf(quantity, units);
	}

	@Override
	public Amount<VolumetricDensity> getAmount() {
		return amount;
	}

	@Override
	public double getQuantity() {
		return quantity;
	}

	@Override
	public String getUnit() {
		return unit;
	}

	@Override
	public String toString() {
		double printQuantity = quantity;
		if (unit.equals("specific gravity")) {
			printQuantity /= 1000;
		}
		return String.format("%.2f", printQuantity) + " " + unit;
	}

}
