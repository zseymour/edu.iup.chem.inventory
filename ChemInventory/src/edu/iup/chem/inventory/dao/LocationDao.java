package edu.iup.chem.inventory.dao;

import static edu.iup.chem.inventory.db.inventory.Tables.ACCESS;
import static edu.iup.chem.inventory.db.inventory.Tables.LOCATION;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.Loader;
import org.jooq.LoaderError;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.conf.Settings;
import org.jooq.exception.DataAccessException;

import edu.iup.chem.inventory.ConnectionPool;
import edu.iup.chem.inventory.Constants;
import edu.iup.chem.inventory.Utils;
import edu.iup.chem.inventory.csv.CSVBottle;
import edu.iup.chem.inventory.db.inventory.InventoryFactory;
import edu.iup.chem.inventory.db.inventory.tables.Chemical;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;
import edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord;
import edu.iup.chem.inventory.search.ChemicalWebSearch;

public class LocationDao extends DataDao<LocationRecord> {

	private final static Logger	LOG					= Logger.getLogger(LocationDao.class);
	private static Condition	justAccessClause	= ACCESS.UID
															.equal(Constants.CURRENT_USER
																	.getUid()
																	.intValue());
	private static Condition	whereClause			= justAccessClause
															.and(LOCATION.TYPE
																	.isNull()
																	.or(LOCATION.TYPE
																			.notEqual("W")));

	public static void clearAllWasteBottles() {
		try (Connection conn = ConnectionPool.getConnection()) {
			LOG.debug("Deleting LocationRecords with type W.");
			final InventoryFactory create = new InventoryFactory(conn);
			create.delete(LOCATION).where(LOCATION.TYPE.equal("W")).execute();
		} catch (final SQLException e) {
			LOG.error("SQL Error while deleting waste records.", e);
		}
	}

	public static void clearAllWasteBottlesExcept(
			final List<String> bottlesToSave) {
		try (Connection conn = ConnectionPool.getConnection()) {
			LOG.debug("Deleting some LocationRecords with type W.");
			final InventoryFactory create = new InventoryFactory(conn);
			create.delete(LOCATION)
					.where(LOCATION.TYPE.equal("W"))
					.and(LOCATION.BOTTLE_NO.notIn(Utils
							.stringListToIntList(bottlesToSave))).execute();
		} catch (final SQLException e) {
			LOG.error("SQL Error while deleting waste records.", e);
		}

	}

	public static void delete(final LocationRecord rec) {
		try (Connection conn = ConnectionPool.getConnection()) {
			LOG.debug("Deleting LocationRecord for Bottle " + rec.getBottle());
			final InventoryFactory create = new InventoryFactory(conn);
			rec.attach(create);
			rec.delete();
		} catch (final SQLException e) {
			LOG.error("SQL Error while deleting record.", e);
			deleteByBottle(rec.getType(), rec.getBottleNo());
		}

	}

	private static void deleteByBottle(final String type, final Integer bottleNo) {
		try (Connection conn = ConnectionPool.getConnection()) {
			LOG.debug("Deleting LocationRecords with BottleNo " + type
					+ bottleNo);
			final InventoryFactory create = new InventoryFactory(conn);
			create.delete(LOCATION)
					.where(LOCATION.BOTTLE_NO.equal(bottleNo).and(
							LOCATION.TYPE.equal(type))).execute();
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

	public static void deleteById(final Integer id) {
		try (Connection conn = ConnectionPool.getConnection()) {
			LOG.debug("Deleting LocationRecords with ID " + id);
			final InventoryFactory create = new InventoryFactory(conn);
			create.delete(LOCATION).where(LOCATION.CID.equal(id)).execute();
		} catch (final SQLException e) {
			LOG.error("SQL Error while storing record.", e);
		}
	}

	public static boolean exists(final String bottle) {
		final String[] values = Utils.splitBottle(bottle);
		return exists(values[0], Integer.parseInt(values[1]));
	}

	public static boolean exists(final String type, final Integer bottle) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			return create
					.select()
					.from(LOCATION)
					.where(LOCATION.BOTTLE_NO.eq(bottle).and(
							LOCATION.TYPE.equal(type))).fetchAny() != null;

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching chemical count.", e);
			return false;
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

	public static final int getAllCountWhere(final Integer id) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			return create
					.selectCount()
					.from(LOCATION)
					.join(ACCESS)
					.on(LOCATION.ROOM.equal(ACCESS.ROOM))
					.where(LOCATION.CID.equal(id))
					.and(ACCESS.UID.equal(Constants.CURRENT_USER.getUid()
							.intValue())).fetchOne().getValueAsInteger(0, 1);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching inventory count.", e);
		}

		return 0;
	}

	public static Cursor<Record> getAllLazyWhere(final Integer id) {
		try {
			final Connection conn = ConnectionPool.getConnection();
			final InventoryFactory create = new InventoryFactory(conn);

			return create
					.select(LOCATION.CID, LOCATION.TYPE, LOCATION.BOTTLE_NO,
							LOCATION.CAS, LOCATION.ROOM, LOCATION.INSTRUCTOR,
							LOCATION.SHELF, LOCATION.AMOUNT, LOCATION.UNITS,
							LOCATION.ARRIVAL, LOCATION.EXPIRATION,
							LOCATION.SUPPLIER, LOCATION.PART_NO,
							LOCATION.ACTIVE).from(LOCATION).join(ACCESS)
					.on(LOCATION.ROOM.equal(ACCESS.ROOM))
					.where(LOCATION.CID.equal(id)).and(whereClause).fetchLazy();

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching all inventory lazily.", e);
		}

		return null;
	}

	public static Integer[] getAllWasteBottles() {
		try (Connection conn = ConnectionPool.getConnection()) {
			LOG.debug("Selecting LocationRecords with type W.");
			final InventoryFactory create = new InventoryFactory(conn);
			final List<Integer> bottles = create.select(LOCATION.BOTTLE_NO)
					.from(LOCATION).where(LOCATION.TYPE.equal("W"))
					.fetch(LOCATION.BOTTLE_NO);

			return bottles.toArray(new Integer[bottles.size()]);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching waste records.", e);
		}
		return new Integer[1];
	}

	public static List<LocationRecord> getAllWhere(final Integer id) {
		try (Connection conn = ConnectionPool.getConnection()) {
			LOG.debug("Fetching bottles for ID #" + id);
			final Settings settings = new Settings();
			settings.setExecuteLogging(true);
			final InventoryFactory create = new InventoryFactory(conn, settings);

			SelectConditionStep where = create
					.select(LOCATION.CID, LOCATION.TYPE, LOCATION.BOTTLE_NO,
							LOCATION.CAS, LOCATION.ROOM, LOCATION.INSTRUCTOR,
							LOCATION.SHELF, LOCATION.AMOUNT, LOCATION.UNITS,
							LOCATION.ARRIVAL, LOCATION.EXPIRATION,
							LOCATION.SUPPLIER, LOCATION.PART_NO,
							LOCATION.ACTIVE).from(LOCATION).join(ACCESS)
					.on(LOCATION.ROOM.equal(ACCESS.ROOM))
					.where(LOCATION.CID.equal(id)).and(whereClause);
			if (!Utils.userHasEditingPerm()) {
				where = where.and(LOCATION.ACTIVE.equal((byte) 1));
			}
			return where.fetch().into(LocationRecord.class);
		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching all inventory.", e);
		}

		return null;
	}

	public static List<String> getBottleNumbers(final Integer id) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			return create.select(LOCATION.BOTTLE_NO).from(LOCATION)
					.join(ACCESS).on(LOCATION.ROOM.equal(ACCESS.ROOM))
					.where(LOCATION.CID.equal(id)).and(whereClause).fetch()
					.into(String.class);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching all inventory.", e);
		}

		return null;
	}

	@Deprecated
	public static List<LocationRecord> getByCasWhereBottleLike(
			final String cas, final String filterString) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			return create
					.select(LOCATION.CID, LOCATION.TYPE, LOCATION.BOTTLE_NO,
							LOCATION.CAS, LOCATION.ROOM, LOCATION.INSTRUCTOR,
							LOCATION.SHELF, LOCATION.AMOUNT, LOCATION.UNITS,
							LOCATION.ARRIVAL, LOCATION.EXPIRATION,
							LOCATION.SUPPLIER, LOCATION.PART_NO,
							LOCATION.ACTIVE).from(LOCATION).join(ACCESS)
					.on(LOCATION.ROOM.equal(ACCESS.ROOM))
					.where(LOCATION.CAS.equal(cas)).and(whereClause)
					.and(LOCATION.BOTTLE_NO.like(filterString + "%")).fetch()
					.into(LocationRecord.class);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching all inventory.", e);
		}

		return null;
	}

	public static List<LocationRecord> getGasBottlesByDepartmentInRange(
			final String dept, final Date start, final Date end) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);
			final java.sql.Date startSQL = new java.sql.Date(start.getTime());
			final java.sql.Date endSQL = new java.sql.Date(end.getTime());
			return create.select().from(LOCATION)
					.where(LOCATION.TYPE.equal("G"))
					.and(LOCATION.ARRIVAL.between(startSQL, endSQL))
					.and(LOCATION.EXPIRATION.between(startSQL, endSQL))
					.and(LOCATION.INSTRUCTOR.equal(dept))
					.fetchInto(LocationRecord.class);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching gas departments.", e);
		}

		return new ArrayList<>();
	}

	public static String[] getGasDepartments(final Date start, final Date end) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);
			final java.sql.Date startSQL = new java.sql.Date(start.getTime());
			final java.sql.Date endSQL = new java.sql.Date(end.getTime());
			return create.selectDistinct(LOCATION.INSTRUCTOR).from(LOCATION)
					.where(LOCATION.TYPE.equal("G"))
					.and(LOCATION.ARRIVAL.between(startSQL, endSQL))
					.and(LOCATION.EXPIRATION.between(startSQL, endSQL))
					.fetchArray(LOCATION.INSTRUCTOR);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching gas departments.", e);
		}

		return new String[] {};
	}

	public static String[] getGasProducts(final Date start, final Date end) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);
			final java.sql.Date startSQL = new java.sql.Date(start.getTime());
			final java.sql.Date endSQL = new java.sql.Date(end.getTime());
			return create.selectDistinct(Chemical.CHEMICAL.NAME).from(LOCATION)
					.join(Chemical.CHEMICAL)
					.on(LOCATION.CID.equal(Chemical.CHEMICAL.CID))
					.where(LOCATION.TYPE.equal("G"))
					.and(LOCATION.ARRIVAL.between(startSQL, endSQL))
					.and(LOCATION.EXPIRATION.between(startSQL, endSQL))
					.fetchArray(Chemical.CHEMICAL.NAME);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching gas products.", e);
		}

		return new String[] {};
	}

	public static String getNextAvailableBottle() {
		String bottle = null;
		try (Connection conn = ConnectionPool.getConnection()) {
			final CallableStatement cs = conn
					.prepareCall("{call next_bottle()}");
			final ResultSet rs = cs.executeQuery();

			if (rs.next()) {
				bottle = rs.getString("id");
			}
		} catch (final SQLException e) {
			LOG.error("SQL Error fetching next bottle number.", e.getCause());
		}

		return bottle;
	}

	public static List<String> getRoomsWithBottles() {
		final List<String> rooms = new ArrayList<>();
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);
			rooms.addAll(create.selectDistinct(LOCATION.ROOM).from(LOCATION)
					.fetch(LOCATION.ROOM));
		} catch (final SQLException e) {
			LOG.error("Error fetching all rooms.");
		}

		return rooms;
	}

	public static List<String> importBottles(final List<CSVBottle> bottles) {
		final List<String> results = new ArrayList<>();
		for (final CSVBottle b : bottles) {
			final LocationRecord l = new LocationRecord();
			if (!ChemicalDao.exists(b.getCas())) {
				final ChemicalRecord rec = ChemicalWebSearch.getNewChemical(
						b.getCas(), b.getName());
				if (rec != null) {
					ChemicalDao.store(rec);
				} else {
					results.add("Could not add bottles for CAS " + b.getCas()
							+ ". Chemical could not be found.");
					continue;
				}
			}
			l.setCas(b.getCas());
			// TODO setCid
			final String[] amountAndUnit = Utils.splitUnits(b.getAmount());
			l.setAmount(Double.parseDouble(amountAndUnit[0]));
			l.setUnits(amountAndUnit[1]);
			l.setRoom(b.getRoom());
			l.setShelf(b.getShelf());
			l.setInstructor("Chem. Dept.");
			l.setActive((byte) 1);
			l.setPartNo(0);
			l.setSupplier("None");
			final java.sql.Date date = new java.sql.Date(
					new java.util.Date().getTime());
			l.setArrival(date);
			l.setExpiration(date);
			for (int i = 0; i < b.getQuantity(); i++) {
				final String bottleNumber = getNextAvailableBottle();
				results.add("New bottle: " + b.getAmount() + " of "
						+ b.getName() + " --> " + bottleNumber);
				l.setBottle(bottleNumber);
				LocationDao.store(l);
				try {
					Thread.sleep(100);
				} catch (final InterruptedException e) {

				}
			}
		}

		return results;
	}

	public static List<LoaderError> importCSV(final File f) {
		List<LoaderError> errors = new ArrayList<>();
		Loader<LocationRecord> loader;
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);
			loader = create
					.loadInto(LOCATION)
					.commitEach()
					.onDuplicateKeyIgnore()
					.onErrorIgnore()
					.loadCSV(f)
					.fields(LOCATION.CAS, LOCATION.ROOM, LOCATION.INSTRUCTOR,
							LOCATION.SHELF, LOCATION.AMOUNT, LOCATION.UNITS,
							LOCATION.ARRIVAL, LOCATION.EXPIRATION).execute();

			errors = loader.errors();
		} catch (final SQLException e) {
			LOG.error("SQL Error importing bottle CSV");
		} catch (final FileNotFoundException e) {
			LOG.error("CSV file not available.");
		} catch (final IOException e) {
			LOG.error("Error reading CSV file.");
		}

		return errors;

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
					.select(LOCATION.CID, LOCATION.TYPE, LOCATION.BOTTLE_NO,
							LOCATION.CAS, LOCATION.ROOM, LOCATION.INSTRUCTOR,
							LOCATION.SHELF, LOCATION.AMOUNT, LOCATION.UNITS,
							LOCATION.ARRIVAL, LOCATION.EXPIRATION,
							LOCATION.SUPPLIER, LOCATION.PART_NO,
							LOCATION.ACTIVE).from(LOCATION).join(ACCESS)
					.on(LOCATION.ROOM.equal(ACCESS.ROOM)).where(whereClause)
					.fetch().into(LocationRecord.class);

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
