package edu.iup.chem.inventory.dao;

import static edu.iup.chem.inventory.db.inventory.Tables.ACCESS;
import static edu.iup.chem.inventory.db.inventory.Tables.LOCATION;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jooq.Cursor;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.Factory;

import edu.iup.chem.inventory.ConnectionPool;
import edu.iup.chem.inventory.Constants;
import edu.iup.chem.inventory.db.inventory.InventoryFactory;
import edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord;

public class LocationDao extends DataDao<LocationRecord> {

	private final static Logger	LOG	= Logger.getLogger(LocationDao.class);

	public static void delete(final LocationRecord rec) {
		try (Connection conn = ConnectionPool.getConnection()) {
			LOG.debug("Deleting LocationRecord for Bottle " + rec.getBottleNo());
			final InventoryFactory create = new InventoryFactory(conn);
			rec.attach(create);
			rec.delete();
		} catch (final SQLException e) {
			LOG.error("SQL Error while deleting record.", e);
		}

	}

	public static void deleteByCas(final String cas) {
		try (Connection conn = ConnectionPool.getConnection()) {
			LOG.debug("Deleting LocationRecords with CAS " + cas);
			final InventoryFactory create = new InventoryFactory(conn);
			create.delete(LOCATION).where(LOCATION.CAS.equal(cas)).execute();
		} catch (final SQLException e) {
			LOG.error("SQL Error while storing record.", e);
		}
	}

	public static final int getAllCount() {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			return create.selectCount().from(LOCATION).fetchOne()
					.getValueAsInteger(0, 1);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching inventory count.", e);
		}

		return 0;
	}

	public static final int getAllCountWhere(final String cas) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			return create
					.selectCount()
					.from(LOCATION)
					.join(ACCESS)
					.on(LOCATION.ROOM.equal(ACCESS.ROOM))
					.where(LOCATION.CAS.equal(cas))
					.and(ACCESS.UID.equal(Constants.CURRENT_USER.getUid()
							.intValue())).fetchOne().getValueAsInteger(0, 1);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching inventory count.", e);
		}

		return 0;
	}

	public static Cursor<Record> getAllLazyWhere(final String cas) {
		try {
			final Connection conn = ConnectionPool.getConnection();
			final InventoryFactory create = new InventoryFactory(conn);

			return create
					.select(LOCATION.BOTTLE_NO, LOCATION.CAS, LOCATION.ROOM,
							LOCATION.INSTRUCTOR, LOCATION.SHELF,
							LOCATION.AMOUNT, LOCATION.UNITS, LOCATION.ARRIVAL,
							LOCATION.EXPIRATION, LOCATION.SUPPLIER,
							LOCATION.PART_NO)
					.from(LOCATION)
					.join(ACCESS)
					.on(LOCATION.ROOM.equal(ACCESS.ROOM))
					.where(LOCATION.CAS.equal(cas))
					.and(ACCESS.UID.equal(Constants.CURRENT_USER.getUid()
							.intValue())).fetchLazy();

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching all inventory lazily.", e);
		}

		return null;
	}

	public static List<LocationRecord> getAllWhere(final String cas) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			return create
					.select(LOCATION.BOTTLE_NO, LOCATION.CAS, LOCATION.ROOM,
							LOCATION.INSTRUCTOR, LOCATION.SHELF,
							LOCATION.AMOUNT, LOCATION.UNITS, LOCATION.ARRIVAL,
							LOCATION.EXPIRATION, LOCATION.SUPPLIER,
							LOCATION.PART_NO)
					.from(LOCATION)
					.join(ACCESS)
					.on(LOCATION.ROOM.equal(ACCESS.ROOM))
					.where(LOCATION.CAS.equal(cas))
					.and(ACCESS.UID.equal(Constants.CURRENT_USER.getUid()
							.intValue())).fetch().into(LocationRecord.class);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching all inventory.", e);
		}

		return null;
	}

	public static List<LocationRecord> getByCasWhereBottleLike(
			final String cas, final String filterString) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			return create
					.select(LOCATION.BOTTLE_NO, LOCATION.CAS, LOCATION.ROOM,
							LOCATION.INSTRUCTOR, LOCATION.SHELF,
							LOCATION.AMOUNT, LOCATION.UNITS, LOCATION.ARRIVAL,
							LOCATION.EXPIRATION, LOCATION.SUPPLIER,
							LOCATION.PART_NO)
					.from(LOCATION)
					.join(ACCESS)
					.on(LOCATION.ROOM.equal(ACCESS.ROOM))
					.where(LOCATION.CAS.equal(cas))
					.and(ACCESS.UID.equal(Constants.CURRENT_USER.getUid()
							.intValue()))
					.and(LOCATION.BOTTLE_NO.like(filterString + "%")).fetch()
					.into(LocationRecord.class);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching all inventory.", e);
		}

		return null;
	}

	public static String getNextAvailableBottle() {
		String bottle = null;
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);
			bottle = create
					.select(Factory.max(LOCATION.BOTTLE_NO).add(1)
							.as("next_bottle")).fetchAny().into(String.class);
		} catch (final SQLException e) {
			LOG.error("SQL Error fetching next bottle number.", e.getCause());
		}

		return bottle;
	}

	public static void store(final LocationRecord rec) {
		try (Connection conn = ConnectionPool.getConnection()) {
			LOG.debug("Storing new/updated LocationRecord.");
			final InventoryFactory create = new InventoryFactory(conn);
			rec.attach(create);
			rec.store();
		} catch (final SQLException e) {
			LOG.error("SQL Error while storing record.", e);
		} catch (final DataAccessException e) {
			// Tried to INSERT when we should have done UPDATE
			try (Connection conn = ConnectionPool.getConnection()) {
				final InventoryFactory create = new InventoryFactory(conn);
				// Force an UPDATE
				create.executeUpdate(rec,
						LOCATION.BOTTLE_NO.equal(rec.getBottleNo()));
			} catch (final SQLException e1) {
				LOG.error("SQL Error while storing record.", e1);
			}
		}

	}

	@Override
	public List<LocationRecord> getAll() {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			return create
					.select(LOCATION.BOTTLE_NO, LOCATION.CAS, LOCATION.ROOM,
							LOCATION.INSTRUCTOR, LOCATION.SHELF,
							LOCATION.AMOUNT, LOCATION.UNITS, LOCATION.ARRIVAL,
							LOCATION.EXPIRATION, LOCATION.SUPPLIER,
							LOCATION.PART_NO)
					.from(LOCATION)
					.join(ACCESS)
					.on(LOCATION.ROOM.equal(ACCESS.ROOM))
					.where(ACCESS.UID.equal(Constants.CURRENT_USER.getUid()
							.intValue())).fetch().into(LocationRecord.class);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching all inventory.", e);
		}

		return null;
	}

	@Override
	public Cursor<Record> getAllLazy() {
		try {
			final Connection conn = ConnectionPool.getConnection();
			final InventoryFactory create = new InventoryFactory(conn);

			return create.select().from(LOCATION).fetchLazy();

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching all inventory lazily.", e);
		}

		return null;
	}

}
