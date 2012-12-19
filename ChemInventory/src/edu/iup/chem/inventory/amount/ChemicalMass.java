package edu.iup.chem.inventory.amount;

import javax.measure.quantity.Mass;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

public class ChemicalMass extends ChemicalAmount {

	private String			unit;
	private double			quantity;
	private Amount<Mass>	amount;

	public ChemicalMass(final double quantity, final String unit,
			final Unit<Mass> units) {
		this.unit = unit;
		this.quantity = quantity;
		amount = Amount.valueOf(quantity, units);

	}

	@Override
	public Amount<Mass> getAmount() {
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

	public void setAmount(final Amount<Mass> amount) {
		this.amount = amount;
	}

	public void setQuantity(final double quantity) {
		this.quantity = quantity;
	}

	public void setUnit(final String unit) {
		this.unit = unit;
	}

	@Override
	public String toString() {
		return String.valueOf(quantity) + " " + unit;
	}

}
