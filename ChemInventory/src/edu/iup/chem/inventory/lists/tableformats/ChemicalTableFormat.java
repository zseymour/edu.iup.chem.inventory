package edu.iup.chem.inventory.lists.tableformats;

import java.util.Comparator;

import ca.odell.glazedlists.gui.AdvancedTableFormat;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;
import edu.iup.chem.inventory.lists.comparators.CustomStringComparator;

public class ChemicalTableFormat implements AdvancedTableFormat<ChemicalRecord> {

	@SuppressWarnings("rawtypes")
	@Override
	public Class getColumnClass(final int column) {
		return String.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Comparator getColumnComparator(final int column) {
		return new CustomStringComparator();
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public String getColumnName(final int column) {
		switch (column) {
			case 0:
				return "CAS Number";
			case 1:
				return "Name";
			case 2:
				return "Formula";
			case 3:
				return "SMILES";
			default:
				throw new IllegalStateException();
		}

	}

	@Override
	public Object getColumnValue(final ChemicalRecord chemical, final int column) {
		switch (column) {
			case 0:
				return chemical.getCas();
			case 1:
				return chemical.getName();
			case 2:
				return chemical.getFormula();
			case 3:
				return chemical.getSmiles();
			default:
				throw new IllegalStateException();
		}

	}

}
