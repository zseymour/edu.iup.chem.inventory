package edu.iup.chem.inventory.dao;

import static edu.iup.chem.inventory.db.inventory.Tables.CANCER;
import static edu.iup.chem.inventory.db.inventory.Tables.CHEMICAL;
import static edu.iup.chem.inventory.db.inventory.Tables.SYNONYM;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jooq.Cursor;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;

import edu.iup.chem.inventory.ConnectionPool;
import edu.iup.chem.inventory.db.inventory.InventoryFactory;
import edu.iup.chem.inventory.db.inventory.tables.records.CancerRecord;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;
import edu.iup.chem.inventory.db.inventory.tables.records.SynonymRecord;
import edu.iup.chem.inventory.search.ChemicalWebSearch;

public class ChemicalDao extends DataDao<ChemicalRecord> {
	private final static Logger					LOG		= Logger.getLogger(ChemicalDao.class);
	private static Map<String, ChemicalRecord>	RECORDS	= new HashMap<>();

	public static void delete(final ChemicalRecord rec) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final String cas = rec.getCas();
			LOG.debug("Deleting ChemicalRecord " + cas);
			final InventoryFactory create = new InventoryFactory(conn);
			rec.attach(create);
			rec.delete();
			LocationDao.deleteByCas(cas);
		} catch (final SQLException e) {
			LOG.error("SQL Error while storing record.", e);
		}

	}

	public static boolean exists(final String cas) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			return create.select().from(CHEMICAL).where(CHEMICAL.CAS.eq(cas))
					.fetchAny() != null;

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching chemical count.", e);
			return false;
		}
	}

	public static final int getAllCount() {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			return create.selectCount().from(CHEMICAL).fetchOne()
					.getValueAsInteger(0, 1);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching chemical count.", e);
		}

		return 0;
	}

	public static ChemicalRecord getByCas(final String cas) {
		if (RECORDS.containsKey(cas)) {
			return RECORDS.get(cas);
		}

		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			final ChemicalRecord rec = create.select().from(CHEMICAL)
					.where(CHEMICAL.CAS.equal(cas)).fetchOne()
					.into(ChemicalRecord.class);
			rec.attach(create);
			RECORDS.put(cas, rec);
			return rec;
		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching chemical by CAS.", e);
		}

		return null;
	}

	public static List<ChemicalRecord> getByCSID(final String CSID) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			return create.select().from(CHEMICAL).where(CHEMICAL.CSID.eq(CSID))
					.fetchInto(ChemicalRecord.class);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching chemical count.", e);
		}

		return new ArrayList<>();
	}

	public static List<ChemicalRecord> getBySMILES(final String smiles) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			return create.select().from(CHEMICAL)
					.where(CHEMICAL.SMILES.eq(smiles))
					.fetchInto(ChemicalRecord.class);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching chemical count.", e);
		}

		return new ArrayList<>();
	}

	public static List<String> getListOfCAS() {
		List<String> casList = new ArrayList<>();
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			casList = create.select(CHEMICAL.CAS).from(CHEMICAL)
					.fetch(CHEMICAL.CAS);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching all chemicals.", e);
		}

		return casList;
	}

	public static String getNameByCAS(final String cas) {
		final ChemicalRecord rec = getByCas(cas);

		return rec.getName();
	}

	public static boolean isCarcinogenic(final ChemicalRecord rec) {
		try (final Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			return create.select().from(CANCER)
					.where(CANCER.CAS.eq(rec.getCas()))
					.fetchInto(CancerRecord.class).size() > 0;

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching all chemicals lazily.", e);
		}

		return false;

	}

	public static void store(final ChemicalRecord rec) {
		try (Connection conn = ConnectionPool.getConnection()) {
			LOG.debug("Inserting/updating new ChemicalRecord");
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
				create.executeUpdate(rec, CHEMICAL.CAS.equal(rec.getCas()));
			} catch (final SQLException e1) {
				LOG.error("SQL Error while storing record.", e1);
			}
		}

	}

	public static void storeNames(final ChemicalRecord record) {
		final SynonymRecord syns = new SynonymRecord();
		try (Connection conn = ConnectionPool.getConnection()) {
			LOG.debug("Inserting/updating synonyms for " + record.getName());
			final InventoryFactory create = new InventoryFactory(conn);
			final String names = ChemicalWebSearch.getNames(record.getInchi());
			syns.setCas(record.getCas());
			syns.setNames(names);
			syns.attach(create);
			syns.store();
		} catch (final SQLException e) {
			LOG.error("SQL Error while storing names.", e);
		} catch (final DataAccessException e) {
			// Tried to INSERT when we should have done UPDATE
			try (Connection conn = ConnectionPool.getConnection()) {
				// Force an UPDATE
				final InventoryFactory create = new InventoryFactory(conn);
				create.executeUpdate(syns, SYNONYM.CAS.eq(record.getCas()));
			} catch (final SQLException e1) {
				LOG.error("SQL Error while storing names.", e1);
			}
		}
	}

	@Override
	public List<ChemicalRecord> getAll() {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			return create.select().from(CHEMICAL).fetch()
					.into(ChemicalRecord.class);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching all chemicals.", e);
		}

		return new ArrayList<>();
	}

	@Override
	public Cursor<Record> getAllLazy() {
		try {
			final Connection conn = ConnectionPool.getConnection();
			final InventoryFactory create = new InventoryFactory(conn);

			return create.select().from(CHEMICAL).fetchLazy();

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching all chemicals lazily.", e);
		}

		return null;
	}
}
