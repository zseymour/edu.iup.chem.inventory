package edu.iup.chem.inventory.lists.tablemodels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import edu.iup.chem.inventory.Utils;
import edu.iup.chem.inventory.amount.ChemicalAmount;
import edu.iup.chem.inventory.amount.ChemicalAmountFactory;
import edu.iup.chem.inventory.dao.LocationDao;
import edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord;

public class LocationTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final Logger			log					= Logger.getLogger(LocationTableModel.class);
	private static final long			serialVersionUID	= 3115452005385577879L;
	private final List<LocationRecord>	inventory			= new ArrayList<>();
	public static final int				BOTTLE_COLUMN		= 0;
	public static final int				CAS_COLUMN			= 1;
	public static final int				NAME_COLUMN			= 2;
	public static final int				INSTRUCTOR_COLUMN	= 3;
	public static final int				ROOM_COLUMN			= 4;
	public static final int				SHELF_COLUMN		= 5;
	public static final int				AMOUNT_COLUMN		= 6;
	public static final int				ARRIVAL_COLUMN		= 7;
	public static final int				EXP_COLUMN			= 8;
	public static final int				COLUMN_COUNT		= 9;

	public void add(final List<LocationRecord> list) {
		final int first = inventory.size();
		final int last = first + list.size() - 1;
		inventory.addAll(list);
		fireTableRowsInserted(first, last);
	}

	public void add(final LocationRecord chem) {
		final int index = inventory.size();
		inventory.add(chem);
		fireTableRowsInserted(index, index);
	}

	public LocationRecord getChemical(final int row) {
		return inventory.get(row);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class getColumnClass(final int column) {
		switch (column) {
			case BOTTLE_COLUMN:
				return Integer.class;
			case CAS_COLUMN:
				return String.class;
			case ARRIVAL_COLUMN:
				return Date.class;
			case EXP_COLUMN:
				return Date.class;
			case AMOUNT_COLUMN:
				return ChemicalAmount.class;
			default:
				return String.class;
		}
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public int getRowCount() {
		return inventory.size();
	}

	@Override
	public Object getValueAt(final int row, final int column) {
		final LocationRecord rec = getChemical(row);

		switch (column) {
			case BOTTLE_COLUMN:
				return rec.getBottleNo();
			case CAS_COLUMN:
				return rec.getCas();
			case AMOUNT_COLUMN:
				return ChemicalAmountFactory.getChemicalAmount(rec.getAmount()
						.toString(), rec.getUnits());
			case ARRIVAL_COLUMN:
				return rec.getArrival();
			case EXP_COLUMN:
				return rec.getExpiration();
			case NAME_COLUMN:
				return "<html>" + rec.getName() + "</html>";
			case ROOM_COLUMN:
				return rec.getRoom();
			case SHELF_COLUMN:
				return rec.getShelf();
			case INSTRUCTOR_COLUMN:
				return rec.getInstructor();
			default:
				return null;
		}
	}

	@Override
	public boolean isCellEditable(final int row, final int column) {
		if (!Utils.userHasEditingPerm()) {
			return false;
		}

		switch (column) {
			case ROOM_COLUMN:
			case SHELF_COLUMN:
			case INSTRUCTOR_COLUMN:
			case ARRIVAL_COLUMN:
			case EXP_COLUMN:
			case AMOUNT_COLUMN:
				return true;
			default:
				return false;
		}
	}

	public void removeChemical(final int index) {
		final LocationRecord rec = inventory.get(index);
		LocationDao.delete(rec);
		inventory.remove(index);

		fireTableRowsDeleted(index, index);

	}

	@Override
	public void setValueAt(final Object obj, final int row, final int column) {
		final LocationRecord rec = getChemical(row);

		try {
			switch (column) {
				case BOTTLE_COLUMN:
					rec.setBottleNo((Integer) obj);
					break;
				case CAS_COLUMN:
					rec.setCas((String) obj);
					break;
				case ARRIVAL_COLUMN:
					rec.setArrival(new java.sql.Date(((Date) obj).getTime()));
					break;
				case EXP_COLUMN:
					rec.setExpiration(new java.sql.Date(((Date) obj).getTime()));
					break;
				case ROOM_COLUMN:
					rec.setRoom((String) obj);
					break;
				case SHELF_COLUMN:
					rec.setShelf((String) obj);
					break;
				case INSTRUCTOR_COLUMN:
					rec.setInstructor((String) obj);
					break;
				case AMOUNT_COLUMN:
					final ChemicalAmount am = (ChemicalAmount) obj;
					if (am != null) {
						rec.setAmount(am.getQuantity());
						rec.setUnits(am.getUnit());
					} else {
						rec.setAmount((double) 0);
					}
					break;
				default:
					break;
			}
		} catch (final ClassCastException e) {
			log.debug("Failed to cast cell value. Ignoring.");
		}

		LocationDao.store(rec);

		fireTableCellUpdated(row, column);

	}

	public void update(final List<LocationRecord> list) {
		inventory.clear();

		if (list != null) {
			inventory.addAll(list);
		}

		fireTableDataChanged();
	}

}
