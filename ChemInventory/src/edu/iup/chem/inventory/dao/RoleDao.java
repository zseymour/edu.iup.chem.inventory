package edu.iup.chem.inventory.dao;

import static edu.iup.chem.inventory.db.inventory.Tables.ROLE;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.iup.chem.inventory.ConnectionPool;
import edu.iup.chem.inventory.db.inventory.InventoryFactory;
import edu.iup.chem.inventory.db.inventory.tables.records.RoleRecord;

public class RoleDao {

	private static final Logger	LOG	= Logger.getLogger(RoleDao.class);

	public static List<RoleRecord> getRoles() {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);
			return create.select().from(ROLE).fetch().into(RoleRecord.class);
		} catch (final SQLException e) {
			LOG.error("Error fetching roles.", e.getCause());
		}
		return new ArrayList<>();
	}

}
