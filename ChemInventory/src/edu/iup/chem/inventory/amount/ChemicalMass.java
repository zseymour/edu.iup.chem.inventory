package edu.iup.chem.inventory.amount;

import javax.measure.quantity.Mass;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

public class ChemicalMass extends ChemicalAmount {

	private String unit;
	private double quantity;
	private Amount<Mass> amount;
	
	public ChemicalMass(double quantity, String unit, Unit<Mass> units) {
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


	public Amount<Mass> getAmount() {
		return amount;
	}


	public void setAmount(Amount<Mass> amount) {
		this.amount = amount;
	}


	@Override
	public String toString(){
		return String.valueOf(quantity) + " " + unit;
	}

}
