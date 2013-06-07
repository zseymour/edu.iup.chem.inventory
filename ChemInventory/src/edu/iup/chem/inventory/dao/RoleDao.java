package edu.iup.chem.inventory.dao;

import static edu.iup.chem.inventory.db.inventory.Tables.ROLE;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jooq.tools.unsigned.UInteger;

import edu.iup.chem.inventory.ConnectionPool;
import edu.iup.chem.inventory.db.inventory.InventoryFactory;
import edu.iup.chem.inventory.db.inventory.tables.records.RoleRecord;

public class RoleDao {

	private static final Logger	LOG	= Logger.getLogger(RoleDao.class);

	public static edu.iup.chem.inventory.db.inventory.tables.records.RoleRecord fetchRoleByRid(
			final UInteger rid) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);
			return create
					.selectFrom(
							edu.iup.chem.inventory.db.inventory.tables.Role.ROLE)
					.where(edu.iup.chem.inventory.db.inventory.tables.Role.ROLE.RID
							.equal(rid)).fetchOne();
		} catch (final SQLException e) {
			LOG.error("Error fetching role.");
		}
		return null;
	}

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
