package edu.iup.chem.inventory;

import static edu.iup.chem.inventory.db.inventory.Tables.CHEMICAL;
import static edu.iup.chem.inventory.db.inventory.Tables.LOCATION;
import static javax.measure.unit.NonSI.LITER;
import static javax.measure.unit.SI.GRAM;
import static javax.measure.unit.SI.MILLI;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import edu.iup.chem.inventory.amount.ChemicalAmount;
import edu.iup.chem.inventory.amount.ChemicalDensity;
import edu.iup.chem.inventory.amount.ChemicalMass;
import edu.iup.chem.inventory.amount.ChemicalVolume;
import edu.iup.chem.inventory.db.inventory.InventoryFactory;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;
import edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord;

public class Test {
	final static Logger	log	= Logger.getLogger(Test.class);

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		ConnectionPool.initializePool();
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);
			final List<ChemicalRecord> chemicals = create.select()
					.from(CHEMICAL).where(CHEMICAL.CAS.eq("100-41-4")).fetch()
					.into(ChemicalRecord.class);
			final ChemicalRecord r = chemicals.get(0);

			final LocationRecord i = create.select().from(LOCATION)
					.where(LOCATION.CAS.eq(r.getCas())).fetchOne()
					.into(LocationRecord.class);
			final ChemicalAmount am = i.getChemicalAmount();
			final ChemicalDensity den = r.getDensity();

			if (am instanceof ChemicalMass) {
				log.debug("Volume: "
						+ am.getAmount().divide(den.getAmount())
								.to(MILLI(LITER)));
			} else if (am instanceof ChemicalVolume) {
				log.debug("Mass: "
						+ am.getAmount().times(den.getAmount()).to(GRAM));
			}

		} catch (final SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
