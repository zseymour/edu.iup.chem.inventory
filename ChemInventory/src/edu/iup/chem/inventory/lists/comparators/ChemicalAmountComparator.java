package edu.iup.chem.inventory.lists.comparators;

import java.util.Comparator;

import edu.iup.chem.inventory.amount.ChemicalAmount;
import edu.iup.chem.inventory.amount.ChemicalMass;
import edu.iup.chem.inventory.amount.ChemicalVolume;

public class ChemicalAmountComparator implements Comparator<ChemicalAmount> {

	@Override
	public int compare(ChemicalAmount c1, ChemicalAmount c2) {
		if(c1 instanceof ChemicalMass && c2 instanceof ChemicalMass){
			ChemicalMass mass1 = (ChemicalMass) c1;
			ChemicalMass mass2 = (ChemicalMass) c2;
			return mass1.getAmount().compareTo(mass2.getAmount());
		} else if(c1 instanceof ChemicalVolume && c2 instanceof ChemicalVolume) {
			ChemicalVolume vol1 = (ChemicalVolume) c1;
			ChemicalVolume vol2 = (ChemicalVolume) c2;
			return vol1.getAmount().compareTo(vol2.getAmount());
		}
		
		return 0;
	}

}
