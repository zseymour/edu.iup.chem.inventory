package edu.iup.chem.inventory.index;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import edu.iup.chem.inventory.dao.ChemicalDao;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;

public class Index {
	private static class ChemicalIndexer implements Callable<Directory> {

		private static Map<String, Analyzer> getChemicalFieldAnalyzers() {
			final Map<String, Analyzer> map = new HashMap<>();

			map.put("name", new WhitespaceAnalyzer(Version.LUCENE_40));
			map.put("syn", new WhitespaceAnalyzer(Version.LUCENE_40));

			return map;
		}

		@Override
		public Directory call() throws Exception {
			final Directory dir = new RAMDirectory();
			final Analyzer analyzer = new PerFieldAnalyzerWrapper(
					new StandardAnalyzer(Version.LUCENE_40),
					getChemicalFieldAnalyzers());
			final IndexWriterConfig config = new IndexWriterConfig(
					Version.LUCENE_40, analyzer);
			try (final IndexWriter iwriter = new IndexWriter(dir, config);) {
				final List<ChemicalRecord> records = new ChemicalDao().getAll();
				for (final ChemicalRecord r : records) {
					final Document doc = new Document();
					doc.add(new StringField("cas", r.getCas(), Field.Store.YES));
					doc.add(new StringField("name", r.getName(),
							Field.Store.YES));
					doc.add(new StringField("syn", r.getOtherNames(),
							Field.Store.NO));

					iwriter.addDocument(doc);
				}
			}

			return dir;
		}

	}

	private static final Logger	LOG				= Logger.getLogger(Index.class);

	private static Directory	CHEM_DIRECTORY;

	private static final int	DIRECTORY_COUNT	= 1;

	private static final int	POOL_SIZE		= Runtime.getRuntime()
														.availableProcessors()
														* DIRECTORY_COUNT;

	public static Directory getChemicalDirectory() {
		return CHEM_DIRECTORY;
	}

	public static void initializeDirectories() {
		final ExecutorService executor = Executors
				.newFixedThreadPool(POOL_SIZE);
		try {
			final Future<Directory> chemFuture = executor
					.submit(new ChemicalIndexer());

			CHEM_DIRECTORY = chemFuture.get();
		} catch (InterruptedException | ExecutionException e) {
			LOG.debug("Error with thread pool", e.getCause());
		} finally {
			executor.shutdownNow();
		}
	}
}
