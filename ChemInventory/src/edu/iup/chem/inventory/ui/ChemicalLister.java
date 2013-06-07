package edu.iup.chem.inventory.ui;

import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;

public interface ChemicalLister {

	void deleteRows(boolean deleteFromDB);

	public void fireChemicalsAdded(ChemicalRecord rec);

	public ChemicalRecord getSelectedChemical();
}
