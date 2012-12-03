package edu.iup.chem.inventory.amount;

public abstract class ChemicalAmount {
	@Override
	public abstract String toString();
	
	public abstract double getQuantity();
	
	public abstract String getUnit();
}
