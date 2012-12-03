package edu.iup.chem.inventory.lists.celleditor;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import edu.iup.chem.inventory.amount.ChemicalAmount;
import edu.iup.chem.inventory.amount.ChemicalAmountFactory;

public class AmountCellEditor extends AbstractCellEditor implements
		TableCellEditor {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1136549965407374170L;
	private ChemicalAmount amount = null;
	private JTextField amountField = new JTextField();
	@Override
	
	public Object getCellEditorValue() {
			String[] fields = amountField.getText().split(" ");
			return ChemicalAmountFactory.getChemicalAmount(fields[0], fields[1]);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		amount = (ChemicalAmount) value;
		amountField.setText(amount.toString());
		return amountField;
	}

}
