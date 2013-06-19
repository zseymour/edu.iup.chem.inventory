package edu.iup.chem.inventory.lists.comparators;

import java.util.Comparator;

import edu.iup.chem.inventory.amount.InventoryAmount;

public class InventoryAmountComparator implements Comparator<InventoryAmount> {
	@Override
	public int compare(final InventoryAmount c1, final InventoryAmount c2) {
		return c1.getMass().compareTo(c2.getMass());
	}
}
