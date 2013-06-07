package edu.iup.chem.inventory.lists.tablemodels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import edu.iup.chem.inventory.Utils;
import edu.iup.chem.inventory.amount.ChemicalAmount;
import edu.iup.chem.inventory.amount.ChemicalAmountFactory;
import edu.iup.chem.inventory.amount.ChemicalDensity;
import edu.iup.chem.inventory.amount.ChemicalMass;
import edu.iup.chem.inventory.dao.ChemicalDao;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalCarc;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalCold;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalFlamm;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalNfpaS;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalStorageClass;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;

public class ChemicalTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final Logger			log					= Logger.getLogger(ChemicalTableModel.class);
	private final List<ChemicalRecord>	chemicals			= new ArrayList<>();
	private HashMap<Integer, Float>		results				= new HashMap<>();
	private static final long			serialVersionUID	= 1516590841060374968L;
	public static final int				CAS_COLUMN			= 0;
	public static final int				NAME_COLUMN			= 1;
	public static final int				FORMULA_COLUMN		= 2;
	public static final int				SMILES_COLUMN		= 3;
	public static final int				NFPA_HEALTH			= 4;
	public static final int				NFPA_FIRE			= 5;
	public static final int				NFPA_REACT			= 6;
	public static final int				NFPA_SPECIAL		= 7;
	public static final int				COLD_STORAGE		= 8;
	public static final int				FLAMMABLE_COLUMN	= 9;
	public static final int				CARC_COLUMN			= 10;
	public static final int				TOXIC_COLUMN		= 11;
	public static final int				CLASS_COLUMN		= 12;
	public static final int				MELTING_COLUMN		= 13;
	public static final int				BOILING_COLUMN		= 14;
	public static final int				DENSITY_COLUMN		= 15;
	public static final int				SCORE_COLUMN		= 16;
	public static final int				FLASH_COLUMN		= 17;
	public static final int				COLUMN_COUNT		= 18;

	public void add(final ChemicalRecord chem) {
		final int index = chemicals.size();
		chemicals.add(chem);
		fireTableRowsInserted(index, index);
	}

	public void add(final List<ChemicalRecord> list) {
		final int first = chemicals.size();
		final int last = first + list.size() - 1;
		chemicals.addAll(list);
		fireTableRowsInserted(first, last);
	}

	public List<ChemicalRecord> chemicals() {
		return chemicals;
	}

	public void clearResults() {
		results = null;
	}

	public ChemicalRecord getChemical(final int row) {
		return chemicals.get(row);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class getColumnClass(final int column) {
		switch (column) {
			case NFPA_FIRE:
			case NFPA_HEALTH:
			case NFPA_REACT:
				return Integer.class;
			case CARC_COLUMN:
			case COLD_STORAGE:
			case FLAMMABLE_COLUMN:
				return Boolean.class;
			case DENSITY_COLUMN:
				return ChemicalDensity.class;
			case SCORE_COLUMN:
				return Float.class;
			case TOXIC_COLUMN:
				return ChemicalMass.class;
			default:
				return String.class;
		}
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	public int getRow(final ChemicalRecord c) {
		// TODO Auto-generated method stub
		return chemicals.indexOf(c);
	}

	@Override
	public int getRowCount() {
		return chemicals.size();
	}

	@Override
	public Object getValueAt(final int row, final int column) {
		final ChemicalRecord chem = chemicals.get(row);

		switch (column) {
			case CAS_COLUMN:
				return chem.getCas();
			case NAME_COLUMN:
				return "<html>" + chem.getName() + "</html>";
			case FORMULA_COLUMN:
				return chem.getFormulaString();
			case SMILES_COLUMN:
				return chem.getSmiles();
			case NFPA_HEALTH:
				return chem.getNfpaH();
			case NFPA_FIRE:
				return chem.getNfpaF();
			case NFPA_REACT:
				return chem.getNfpaR();
			case NFPA_SPECIAL:
				return chem.getNfpaS().getLiteral();
			case CARC_COLUMN:
				return chem.getCarc().getLiteral().equals("Yes");
			case CLASS_COLUMN:
				final ChemicalStorageClass c = chem.getStorageClass();
				return String.format("%s (%s)", c.getClassLetter(),
						c.getLiteral());
			case COLD_STORAGE:
				return chem.getCold().getLiteral().equals("Yes");
			case FLAMMABLE_COLUMN:
				return chem.getFlamm().getLiteral().equals("Yes");
			case TOXIC_COLUMN:
				return chem.getLd50WithUnits();
			case MELTING_COLUMN:
				return chem.getMeltingPointString();
			case BOILING_COLUMN:
				return chem.getBoilingPointString();
			case FLASH_COLUMN:
				return chem.getFlashPointString();
			case DENSITY_COLUMN:
				return chem.getDensityWithUnits();
			case SCORE_COLUMN:
				if (results == null || results.isEmpty()) {
					return 0f;
				}
				return lookUpScore(chem);
			default:
				return null;
		}

	}

	@Override
	public boolean isCellEditable(final int row, final int column) {
		return Utils.userHasEditingPerm();
	}

	private float lookUpScore(final ChemicalRecord chem) {
		if (results.containsKey(chem.getCid())) {
			return results.get(chem.getCid());
		}
		return 0f;
	}

	public void removeChemical(final int row, final boolean removeFromDB) {
		final ChemicalRecord rec = chemicals.get(row);
		if (removeFromDB) {
			ChemicalDao.delete(rec);
		}
		chemicals.remove(row);

		fireTableRowsDeleted(row, row);

	}

	public void setResults(final HashMap<Integer, Float> results2) {
		results = results2;

	}

	@Override
	public void setValueAt(final Object value, final int row, final int column) {
		final ChemicalRecord rec = getChemical(row);

		try {
			switch (column) {
				case CAS_COLUMN:
					final String oldCas = rec.getCas();
					rec.setCas((String) value);
					// We need to force an update to the row so that we don't
					// accidentally
					// insert a duplicate with a different CAS#
					ChemicalDao.forceUpdate(rec, oldCas);
					// return rather than break
					// and make sure to update the table
					fireTableCellUpdated(row, column);
					return;
				case NAME_COLUMN:
					rec.setName(Utils.stripHtmlTags((String) value));
					break;
				case FORMULA_COLUMN:
					final String formula = (String) value;
					rec.setFormula(formula);
					rec.setMolecularFormula(formula);
					break;
				case SMILES_COLUMN:
					rec.setSmiles((String) value);
					break;
				case NFPA_HEALTH:
					rec.setNfpaH((Integer) value);
					break;
				case NFPA_FIRE:
					rec.setNfpaF((Integer) value);
					break;
				case NFPA_REACT:
					rec.setNfpaR((Integer) value);
					break;
				case NFPA_SPECIAL:
					rec.setNfpaS((ChemicalNfpaS) value);
					break;
				case COLD_STORAGE:
					if ((boolean) value) {
						rec.setCold(ChemicalCold.Yes);
					} else {
						rec.setCold(ChemicalCold.No);
					}

					break;
				case FLAMMABLE_COLUMN:
					if ((boolean) value) {
						rec.setFlamm(ChemicalFlamm.Yes);
					} else {
						rec.setFlamm(ChemicalFlamm.No);
					}

					break;
				case CARC_COLUMN:
					if ((boolean) value) {
						rec.setCarc(ChemicalCarc.Yes);
					} else {
						rec.setCarc(ChemicalCarc.No);
					}

					break;
				case TOXIC_COLUMN:
					final ChemicalAmount am = (ChemicalAmount) value;
					if (am != null) {
						rec.setLd50WithUnits((ChemicalMass) am);
					} else {
						rec.setLd50WithUnits((ChemicalMass) ChemicalAmountFactory
								.getChemicalAmount(0.0, "mg"));
					}
					break;
				case CLASS_COLUMN:
					rec.setStorageClass((ChemicalStorageClass) value);
					break;
				case MELTING_COLUMN:
					final String point = ((String) value).split(" ")[0];
					rec.setMeltingPoint(Double.parseDouble(point));
					break;
				case BOILING_COLUMN:
					final String point1 = ((String) value).split(" ")[0];
					rec.setBoilingPoint(Double.parseDouble(point1));
					break;
				case FLASH_COLUMN:
					final String point2 = ((String) value).split(" ")[0];
					rec.setFlashPoint(Double.parseDouble(point2));
					break;
				case DENSITY_COLUMN:
					final ChemicalDensity am1 = (ChemicalDensity) value;
					if (am1 != null) {
						rec.setChemicalDensity(am1);
					} else {
						rec.setChemicalDensity((ChemicalDensity) ChemicalAmountFactory
								.getChemicalAmount(1.0, "specific gravity"));
					}
					break;
				default:
					break;
			}
		} catch (final ClassCastException e) {
			log.debug("Failed to properly cast cell value. Ignoring...");
		}

		ChemicalDao.store(rec);

		fireTableCellUpdated(row, column);
	}

	public void update(final List<ChemicalRecord> list) {
		chemicals.clear();
		chemicals.addAll(list);
		fireTableDataChanged();
	}
}
