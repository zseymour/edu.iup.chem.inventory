package edu.iup.chem.inventory.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.jooq.Record;
import org.jooq.exception.MappingException;

import com.google.common.base.Stopwatch;

import edu.iup.chem.inventory.dao.ChemicalDao;
import edu.iup.chem.inventory.dao.LocationDao;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;
import edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord;

public class Index {
	private static class BottleIndexer implements Callable<Directory> {

		public static Analyzer getAnalyzer() {
			return new PerFieldAnalyzerWrapper(new StandardAnalyzer(
					Version.LUCENE_40), getBottleFieldAnalyzers());

			// return new StandardAnalyzer(Version.LUCENE_40);

		}

		private static Map<String, Analyzer> getBottleFieldAnalyzers() {
			final Map<String, Analyzer> map = new HashMap<>();
			map.put("cas", new WhitespaceAnalyzer(Version.LUCENE_40));

			return map;
		}

		public static void indexBottle(final IndexWriter iwriter,
				final LocationRecord... records) throws IOException {
			for (final LocationRecord r : records) {
				final Document doc = new Document();
				doc.add(new StringField("cas", r.getCas(), Field.Store.NO));
				doc.add(new StringField("bottle", r.getBottle().toString(),
						Field.Store.NO));
				doc.add(new IntField("cid", r.getCid(), Field.Store.YES));
				doc.add(new LongField("exp", r.getExpiration().getTime(),
						Field.Store.NO));
				doc.add(new StringField("shelf", r.getShelf(), Field.Store.NO));
				doc.add(new StringField("room", r.getRoom(), Field.Store.NO));

				iwriter.addDocument(doc);
			}
		}

		private final List<LocationRecord>	records;

		public BottleIndexer(final List<LocationRecord> records) {
			this.records = records;
		}

		@Override
		public Directory call() throws Exception {
			final Stopwatch watch = new Stopwatch();
			watch.start();
			LOG.info("Building bottle index.");
			final Directory dir = new RAMDirectory();
			// final Directory dir = FSDirectory.open(new File("index/bottle"));
			final Analyzer analyzer = getAnalyzer();
			final IndexWriterConfig config = new IndexWriterConfig(
					Version.LUCENE_40, analyzer);
			try (final IndexWriter iwriter = new IndexWriter(dir, config);) {
				indexBottle(iwriter,
						records.toArray(new LocationRecord[records.size()]));
			}

			watch.stop();
			LOG.info("Finished building bottle index in "
					+ watch.elapsedTime(TimeUnit.SECONDS) + " seconds.");
			return dir;
		}

	}

	private static class ChemicalIndexer implements Callable<Directory> {

		public static Analyzer getAnalyzer() {
			return new PerFieldAnalyzerWrapper(new StandardAnalyzer(
					Version.LUCENE_40), getChemicalFieldAnalyzers());

			// return new StandardAnalyzer(Version.LUCENE_40);

		}

		private static Map<String, Analyzer> getChemicalFieldAnalyzers() {
			final Map<String, Analyzer> map = new HashMap<>();
			final Analyzer chemical = new ChemicalNameAnalyzer();
			map.put("name", chemical);
			map.put("syn", chemical);
			map.put("cas", new WhitespaceAnalyzer(Version.LUCENE_40));
			map.put("bottle", new WhitespaceAnalyzer(Version.LUCENE_40));
			map.put("formula", new MolecularFormulaAnalyzer());

			return map;
		}

		public static void indexChemical(final IndexWriter iwriter,
				final Record... record) throws IOException {
			final Document doc = new Document();
			final StringField cas = new StringField("cas", "cas",
					Field.Store.NO);
			final IntField id = new IntField("id", 0, Field.Store.YES);
			final TextField name = new TextField("name", "name", Field.Store.NO);
			final TextField syn = new TextField("syn", "syn", Field.Store.NO);

			doc.add(id);
			doc.add(cas);
			doc.add(name);
			doc.add(syn);
			for (final Record r : record) {
				try {
					final ChemicalRecord c = r.into(ChemicalRecord.class);
					id.setIntValue(c.getCid());
					cas.setStringValue(c.getCas());
					name.setStringValue(c.getName().replaceAll(","," "));
					syn.setStringValue(c.getOtherNames());

					iwriter.addDocument(doc);
				} catch (final MappingException e) {
					continue;
				}
			}
		}

		private final List<Record>	records;

		public ChemicalIndexer(final List<Record> records) {
			this.records = records;
		}

		@Override
		public Directory call() throws Exception {
			records.removeAll(Collections.singleton(null));
			final Stopwatch watch = new Stopwatch();
			watch.start();
			LOG.info("Building chemical index.");
			final Directory dir = new RAMDirectory();
			// final Directory dir = FSDirectory.open(new
			// File("index/chemical"));
			final Analyzer analyzer = getAnalyzer();
			final IndexWriterConfig config = new IndexWriterConfig(
					Version.LUCENE_40, analyzer);
			try (final IndexWriter iwriter = new IndexWriter(dir, config);) {
				indexChemical(iwriter,
						records.toArray(new Record[records.size()]));
			}

			watch.stop();
			LOG.info("Finished building chemical index in "
					+ watch.elapsedTime(TimeUnit.SECONDS) + " seconds.");
			return dir;
		}
	}

	public static enum INDEX {
		CHEMICAL, BOTTLE
	}

	private static final Logger			LOG					= Logger.getLogger(Index.class);

	private static ChemicalDirectory	CHEM_DIRECTORY		= null;

	private static final String[]		CHEM_FIELDS			= new String[] {
			"name", "syn", "cas"							};

	private static ChemicalDirectory	BOTTLE_DIRECTORY	= null;

	private static final String[]		BOTTLE_FIELDS		= new String[] {
			"bottle", "cas", "exp","shelf","room"							};

	private static final int			DIRECTORY_COUNT		= 2;

	private static final int			POOL_SIZE			= Runtime
																	.getRuntime()
																	.availableProcessors()
																	* DIRECTORY_COUNT;

	public static void addBottle(final LocationRecord loc) {
		final Analyzer analyzer = getAnalyzer(INDEX.BOTTLE);
		final IndexWriterConfig config = new IndexWriterConfig(
				Version.LUCENE_40, analyzer);
		try (final IndexWriter iwriter = new IndexWriter(
				BOTTLE_DIRECTORY.getDirectory(), config);) {
			BottleIndexer.indexBottle(iwriter, loc);
		} catch (final IOException e) {
			LOG.error("Failed adding new bottle.");
		}

	}

	public static void addChemical(final ChemicalRecord rec) {
		final Analyzer analyzer = getAnalyzer(INDEX.CHEMICAL);
		final IndexWriterConfig config = new IndexWriterConfig(
				Version.LUCENE_40, analyzer);
		try (final IndexWriter iwriter = new IndexWriter(
				CHEM_DIRECTORY.getDirectory(), config);) {
			ChemicalIndexer.indexChemical(iwriter, rec);
		} catch (final IOException e) {
			LOG.error("Failed adding new chemical.");
		}
	}

	public static Analyzer getAnalyzer(final INDEX index) {
		switch (index) {
			case CHEMICAL:
				return ChemicalIndexer.getAnalyzer();
			case BOTTLE:
				return BottleIndexer.getAnalyzer();
			default:
				return ChemicalIndexer.getAnalyzer();
		}
	}

	public static ChemicalDirectory getBottleDirectory() {
		if (BOTTLE_DIRECTORY == null) {
			initializeDirectories();
		}

		return BOTTLE_DIRECTORY;
	}

	public static ChemicalDirectory getChemicalDirectory() {
		if (CHEM_DIRECTORY == null) {
			initializeDirectories();
		}

		return CHEM_DIRECTORY;
	}

	public static void initializeDirectories() {
		final ExecutorService executor = Executors
				.newFixedThreadPool(POOL_SIZE);
		try {
			final List<Callable<Directory>> chemTasks = splitChemicals(ChemicalDao
					.getAllRecords());

			final Future<Directory> bottleFuture = executor
					.submit(new BottleIndexer(new LocationDao().getAll()));
			BOTTLE_DIRECTORY = new ChemicalDirectory(bottleFuture.get(),
					"bottle", "cid", BottleIndexer.getAnalyzer(), BOTTLE_FIELDS);

			final List<Future<Directory>> chemFutures = executor
					.invokeAll(chemTasks);
			CHEM_DIRECTORY = mergeChemicalIndices(chemFutures);

		} catch (InterruptedException | ExecutionException e) {
			LOG.debug("Error with thread pool", e.getCause());
		} finally {
			executor.shutdownNow();
		}
	}

	private static ChemicalDirectory mergeChemicalIndices(
			final List<Future<Directory>> chemFutures) {
		Directory dir = null;
		// dir = FSDirectory.open(new File("index/chemical"));
		dir = new RAMDirectory();
		try {
			final Analyzer analyzer = getAnalyzer(INDEX.CHEMICAL);
			final IndexWriterConfig config = new IndexWriterConfig(
					Version.LUCENE_40, analyzer);
			try (final IndexWriter iwriter = new IndexWriter(dir, config);) {
				final List<Directory> dirs = new ArrayList<>();
				for (final Future<Directory> f : chemFutures) {
					dirs.add(f.get());
				}

				final Directory[] dirArray = new Directory[dirs.size()];
				iwriter.addIndexes(dirs.toArray(dirArray));
			} catch (final InterruptedException e) {
				LOG.error("Directory building interrupted.", e.getCause());
			} catch (final ExecutionException e) {
				LOG.error("Failed to merge indices.", e.getCause());
			}
		} catch (final IOException e) {
			LOG.error("Error merging indices", e.getCause());
		}

		return new ChemicalDirectory(dir, "name", "id",
				getAnalyzer(INDEX.CHEMICAL), CHEM_FIELDS);

	}

	public static List<Callable<Directory>> splitChemicals(
			final List<Record> records) {
		final List<Callable<Directory>> splitRecords = new ArrayList<>();
		LOG.debug("Original list size: " + records.size());
		final float division = records.size() / (float) POOL_SIZE;

		for (int i = 0; i < POOL_SIZE; i++) {
			final List<Record> sub = records.subList(Math.round(division * i),
					Math.round(division * (i + 1)));
			final ArrayList<Record> subList = new ArrayList<>(sub);
			splitRecords.add(new ChemicalIndexer(subList));
		}

		return splitRecords;
	}
}
