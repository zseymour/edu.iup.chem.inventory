package edu.iup.chem.inventory.amount;

public class InventoryAmountFactory {
	public static InventoryAmount getAmount(final ChemicalAmount am,
			final ChemicalDensity den) {
		if (am instanceof ChemicalMass) {
			return new InventoryAmount((ChemicalMass) am, den);
		}
		return new InventoryAmount((ChemicalVolume) am, den);
	}
}
