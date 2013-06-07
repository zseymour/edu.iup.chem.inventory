package edu.iup.chem.inventory.dao;

import static edu.iup.chem.inventory.db.inventory.Tables.CANCER;
import static edu.iup.chem.inventory.db.inventory.Tables.CHEMICAL;
import static edu.iup.chem.inventory.db.inventory.Tables.SYNONYM;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jooq.Condition;
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
	private final static Logger					LOG			= Logger.getLogger(ChemicalDao.class);
	private static Map<Object, ChemicalRecord>	RECORDS		= new HashMap<>();
	private final static Condition				whereClause	= CHEMICAL.CAS
																	.notEqual("000-00-0");

	public static void delete(final ChemicalRecord rec) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final String cas = rec.getCas();
			LOG.debug("Deleting ChemicalRecord " + cas);
			final InventoryFactory create = new InventoryFactory(conn);
			rec.attach(create);
			rec.delete();
			LocationDao.deleteById(rec.getCid());
		} catch (final SQLException e) {
			LOG.error("SQL Error while storing record.", e);
		}

	}

	public static boolean exists(final String cas) {
		boolean retVal = false;
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);
			final Record chem = create.select().from(CHEMICAL)
					.where(CHEMICAL.CAS.eq(cas)).fetchAny();
			retVal = chem != null;

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching chemical count.", e);
		}

		return retVal;
	}

	public static void forceUpdate(final ChemicalRecord rec, final String oldCas) {
		try (Connection conn = ConnectionPool.getConnection()) {
			// Force an UPDATE
			final InventoryFactory create = new InventoryFactory(conn);
			create.executeUpdate(rec, CHEMICAL.CAS.equal(oldCas));
		} catch (final SQLException e1) {
			LOG.error("SQL Error while storing record.", e1);
		}

	}

	public static final int getAllCount() {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			return create.selectCount().from(CHEMICAL).where(whereClause)
					.fetchOne().getValueAsInteger(0, 1);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching chemical count.", e);
		}

		return 0;
	}

	public static List<Record> getAllRecords() {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			return create.select().from(CHEMICAL).where(whereClause).fetch();

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching all chemicals.", e);
		}

		return new ArrayList<>();
	}

	public static ChemicalRecord getByCas(final String cas) {
		if (RECORDS.containsKey(cas)) {
			return RECORDS.get(cas);
		}

		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			final ChemicalRecord rec = create.select().from(CHEMICAL)
					.where(CHEMICAL.CAS.equal(cas)).fetchAny()
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

	public static ChemicalRecord getById(final Integer id) {
		if (RECORDS.containsKey(id)) {
			return RECORDS.get(id);
		}

		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			final ChemicalRecord rec = create.select().from(CHEMICAL)
					.where(CHEMICAL.CID.equal(id)).fetchOne()
					.into(ChemicalRecord.class);
			rec.attach(create);
			RECORDS.put(id, rec);
			return rec;
		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching chemical by CAS.", e);
		}

		return null;
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
					.where(whereClause).fetch(CHEMICAL.CAS);

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching all chemicals.", e);
		}

		return casList;
	}

	public static InputStream getMSDS(final String cas) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final PreparedStatement ps = conn
					.prepareStatement("SELECT msds FROM msds WHERE cas=?");
			ps.setString(1, cas);
			final ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				final Blob blob = rs.getBlob("msds");
				return blob.getBinaryStream();
			}
		} catch (final SQLException e) {
			LOG.error("Unable to establish connection to fetch MSDS.", e);
		}

		return null;
	}

	public static String getNameByCAS(final String cas) {
		final ChemicalRecord rec = getByCas(cas);

		return rec.getName();
	}

	public static SynonymRecord getNames(final String cas) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);

			return create.select(SYNONYM.NAMES).from(SYNONYM)
					.where(SYNONYM.CAS.eq(cas)).fetchAny()
					.into(SynonymRecord.class);
		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching synonyms.", e.getCause());
		} catch (final NullPointerException e) {
			storeNames(getByCas(cas));
			return getNames(cas);
		}

		return null;
	}

	public static boolean hasMSDS(final String cas) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final PreparedStatement ps = conn
					.prepareStatement("SELECT msds FROM msds WHERE cas=?");
			ps.setString(1, cas);
			final ResultSet rs = ps.executeQuery();

			return rs.next();
		} catch (final SQLException e) {
			LOG.error("Unable to establish connection to fetch MSDS.", e);
		}

		return false;
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

	public static Integer lastID() {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);
			final Integer id = create.lastID().intValue();
			LOG.debug("Last insert ID: " + id);
			return id;
		} catch (final SQLException e) {
			LOG.warn("Failed to fetch last ID. Returning null.");
			return null;
		}
	}

	public static ChemicalRecord store(final ChemicalRecord rec) {
		try (Connection conn = ConnectionPool.getConnection()) {
			LOG.debug("Inserting/updating new ChemicalRecord");
			final InventoryFactory create = new InventoryFactory(conn);
			rec.attach(create);
			rec.store();
			return rec;
		} catch (final SQLException e) {
			LOG.error("SQL Error while storing record.", e);
		} catch (final DataAccessException e) {
			// Tried to INSERT when we should have done UPDATE
			try (Connection conn = ConnectionPool.getConnection()) {
				// Force an UPDATE
				final InventoryFactory create = new InventoryFactory(conn);
				create.executeUpdate(rec, CHEMICAL.CID.equal(rec.getCid()));

				return rec;
			} catch (final SQLException e1) {
				LOG.error("SQL Error while storing record.", e1);
			}
		}

		return null;

	}

	public static boolean storeMSDS(final ChemicalRecord chemicalRecord,
			final File msds) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final PreparedStatement ps = conn
					.prepareStatement("REPLACE INTO msds (cas, msds) VALUES (?,?)");

			final FileInputStream in = new FileInputStream(msds);

			ps.setString(1, chemicalRecord.getCas());
			ps.setBinaryStream(2, in, (int) msds.length());
			ps.executeUpdate();
			chemicalRecord.recheckComplete();
			return true;
		} catch (final SQLException e) {
			LOG.error("Error establishing connection to store MSDS.", e);
		} catch (final FileNotFoundException e) {
			LOG.error("Error converting MSDS for storage (File not found.)", e);
		}

		return false;

	}

	public static void storeNames(final ChemicalRecord record) {
		final SynonymRecord syns = new SynonymRecord();
		try (Connection conn = ConnectionPool.getConnection()) {
			LOG.debug("Inserting/updating synonyms for " + record.getName());
			final InventoryFactory create = new InventoryFactory(conn);
			String id = record.getInchi();
			if (id == null) {
				id = record.getSmiles();
			}
			final String names = ChemicalWebSearch.getNames(id);
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

			return create.select().from(CHEMICAL).where(whereClause).fetch()
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

			return create.select().from(CHEMICAL).where(whereClause)
					.fetchLazy();

		} catch (final SQLException e) {
			LOG.error("SQL Error while fetching all chemicals lazily.", e);
		}

		return null;
	}

}
