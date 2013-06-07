package edu.iup.chem.inventory.amount;

import org.jscience.physics.amount.Amount;

public abstract class ChemicalAmount {
	public abstract Amount getAmount();

	public abstract double getQuantity();

	public abstract String getUnit();

	@Override
	public abstract String toString();

}
