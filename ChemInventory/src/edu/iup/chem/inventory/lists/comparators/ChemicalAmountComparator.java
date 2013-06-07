package edu.iup.chem.inventory.lists.comparators;

import java.util.Comparator;

import edu.iup.chem.inventory.amount.ChemicalAmount;
import edu.iup.chem.inventory.amount.ChemicalDensity;
import edu.iup.chem.inventory.amount.ChemicalMass;
import edu.iup.chem.inventory.amount.ChemicalVolume;

public class ChemicalAmountComparator implements Comparator<ChemicalAmount> {

	@Override
	public int compare(final ChemicalAmount c1, final ChemicalAmount c2) {
		if (c1 instanceof ChemicalMass && c2 instanceof ChemicalMass) {
			final ChemicalMass mass1 = (ChemicalMass) c1;
			final ChemicalMass mass2 = (ChemicalMass) c2;
			return mass1.getAmount().compareTo(mass2.getAmount());
		} else if (c1 instanceof ChemicalVolume && c2 instanceof ChemicalVolume) {
			final ChemicalVolume vol1 = (ChemicalVolume) c1;
			final ChemicalVolume vol2 = (ChemicalVolume) c2;
			return vol1.getAmount().compareTo(vol2.getAmount());
		} else if (c1 instanceof ChemicalDensity
				&& c2 instanceof ChemicalDensity) {
			final ChemicalDensity vol1 = (ChemicalDensity) c1;
			final ChemicalDensity vol2 = (ChemicalDensity) c2;
			return vol1.getAmount().compareTo(vol2.getAmount());
		}

		return 0;
	}

}
