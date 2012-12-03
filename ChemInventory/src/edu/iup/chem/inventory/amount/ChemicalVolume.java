package edu.iup.chem.inventory.amount;

import javax.measure.quantity.Volume;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

public class ChemicalVolume extends ChemicalAmount {
	
	private String unit;
	private double quantity;
	private Amount<Volume> amount;

	public ChemicalVolume(double quantity, String unit, Unit<Volume> units) {
		this.unit = unit;
		this.quantity = quantity;
		this.amount = Amount.valueOf(quantity, units);
	}
	
	
	@Override
	public String getUnit() {
		return unit;
	}


	public void setUnit(String unit) {
		this.unit = unit;
	}


	@Override
	public double getQuantity() {
		return quantity;
	}


	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}


	public Amount<Volume> getAmount() {
		return amount;
	}


	public void setAmount(Amount<Volume> amount) {
		this.amount = amount;
	}


	@Override
	public String toString(){
		return String.valueOf(quantity) + " " + unit;
	}
	
}
