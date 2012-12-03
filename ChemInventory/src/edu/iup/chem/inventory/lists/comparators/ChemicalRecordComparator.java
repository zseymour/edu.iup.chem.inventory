package edu.iup.chem.inventory.lists.comparators;

import java.util.Comparator;

import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;

public class ChemicalRecordComparator implements Comparator<ChemicalRecord> {

	@Override
	public int compare(final ChemicalRecord o1, final ChemicalRecord o2) {
		return o1.getCas().compareTo(o2.getCas());
	}

}
