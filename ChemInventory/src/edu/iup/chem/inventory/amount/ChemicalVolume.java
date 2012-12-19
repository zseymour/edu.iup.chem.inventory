package edu.iup.chem.inventory.amount;

import javax.measure.quantity.Volume;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

public class ChemicalVolume extends ChemicalAmount {

	private String			unit;
	private double			quantity;
	private Amount<Volume>	amount;

	public ChemicalVolume(final double quantity, final String unit,
			final Unit<Volume> units) {
		this.unit = unit;
		this.quantity = quantity;
		amount = Amount.valueOf(quantity, units);
	}

	@Override
	public Amount<Volume> getAmount() {
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

	public void setAmount(final Amount<Volume> amount) {
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
