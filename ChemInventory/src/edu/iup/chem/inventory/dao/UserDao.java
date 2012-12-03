package edu.iup.chem.inventory.dao;

import static edu.iup.chem.inventory.db.inventory.Tables.USER;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;

import edu.iup.chem.inventory.ConnectionPool;
import edu.iup.chem.inventory.Utils;
import edu.iup.chem.inventory.db.inventory.InventoryFactory;
import edu.iup.chem.inventory.db.inventory.tables.records.UserRecord;

public class UserDao {
	private static final Logger	LOG	= Logger.getLogger(UserDao.class);
	/**
	 * Method to return a jOOQ UserRecord for the user logging in. Password
	 * should not yet be hashed.
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	private static Logger		log	= Logger.getLogger(UserDao.class);

	public static UserRecord getByUsernamePassword(final String username,
			final char[] password) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);
			final Record record = create
					.select()
					.from(USER)
					.where(USER.USERNAME.equal(username).and(
							USER.PASS.equal(Utils.md5(password)))).fetchOne();
			if (record != null) {
				return record.into(UserRecord.class);
			}
		} catch (final SQLException e) {
			log.error("SQL error while logging in user.", e);
		}

		return null;

	}

	public static void store(final UserRecord rec) {
		try (Connection conn = ConnectionPool.getConnection()) {
			LOG.debug("Inserting/updating new UserRecord");
			final InventoryFactory create = new InventoryFactory(conn);
			rec.attach(create);
			rec.store();
		} catch (final SQLException e) {
			LOG.error("SQL Error while storing record.", e);
		} catch (final DataAccessException e) {
			// Tried to INSERT when we should have done UPDATE
			try (Connection conn = ConnectionPool.getConnection()) {
				// Force an UPDATE
				final InventoryFactory create = new InventoryFactory(conn);
				create.executeUpdate(rec, USER.UID.eq(rec.getUid()));
			} catch (final SQLException e1) {
				LOG.error("SQL Error while storing record.", e1);
			}
		}

	}

}
