package edu.iup.chem.inventory.lists.celleditor;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import edu.iup.chem.inventory.amount.ChemicalAmountFactory;
import edu.iup.chem.inventory.amount.ChemicalDensity;

public class DensityCellEditor extends AbstractCellEditor implements
		TableCellEditor {

	private ChemicalDensity		amount		= null;
	private final JTextField	amountField	= new JTextField();

	@Override
	public Object getCellEditorValue() {
		final String[] fields = amountField.getText().split(" ", 2);
		return ChemicalAmountFactory.getChemicalAmount(fields[0], fields[1]);
	}

	@Override
	public Component getTableCellEditorComponent(final JTable table,
			final Object value, final boolean isSelected, final int row,
			final int column) {
		amount = (ChemicalDensity) value;
		amountField.setText(amount.toString());
		return amountField;
	}

}
