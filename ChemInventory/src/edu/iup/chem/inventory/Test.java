package edu.iup.chem.inventory;

import static edu.iup.chem.inventory.db.inventory.Tables.CHEMICAL;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import edu.iup.chem.inventory.dao.ChemicalDao;
import edu.iup.chem.inventory.db.inventory.InventoryFactory;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;

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
					.from(CHEMICAL).fetch().into(ChemicalRecord.class);
			for (final ChemicalRecord c : chemicals) {
				ChemicalDao.storeNames(c);
			}
		} catch (final SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
