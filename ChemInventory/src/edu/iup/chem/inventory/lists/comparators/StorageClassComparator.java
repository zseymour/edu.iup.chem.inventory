package edu.iup.chem.inventory.lists.comparators;

import java.util.Comparator;

import org.apache.log4j.Logger;

import edu.iup.chem.inventory.db.inventory.enums.ChemicalStorageClass;

public class StorageClassComparator implements Comparator<String> {

	@Override
	public int compare(final String o1, final String o2) {
		Logger.getLogger(StorageClassComparator.class).debug(
				"Sorting storage class.");
		final char class1 = ChemicalStorageClass.getClassLetter(o1).charAt(0);
		final char class2 = ChemicalStorageClass.getClassLetter(o2).charAt(0);

		return Integer.compare(class1, class2);

	}

}
