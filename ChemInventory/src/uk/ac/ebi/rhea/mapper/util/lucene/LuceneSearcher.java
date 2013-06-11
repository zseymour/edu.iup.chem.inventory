package uk.ac.ebi.rhea.mapper.util.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.Version;

import uk.ac.ebi.biobabel.lucene.LuceneParser;
import uk.ac.ebi.biobabel.lucene.LuceneParser.Operator;

import com.google.common.base.Stopwatch;

import edu.iup.chem.inventory.index.ChemicalDirectory;
import edu.iup.chem.inventory.index.ChemicalSearchResult;

/**
 * This implementation of {@link ICompoundNameSearchStrategy} uses a Lucene
 * index to get ChEBI IDs from compound names. <br>
 * Please note that:
 * <ul>
 * <li>searches are <b>case insensitive</b>.</li>
 * <li>queries should allow any standard Lucene wildcards.</li>
 * <li>results are ordered by score, with more weight given to ChEBI and ASCII
 * names, then synonyms and IUPAC names, then INNs and cross references.</li>
 * </ul>
 * 
 * @author rafalcan
 * 
 */
public class LuceneSearcher {

	private static final Logger	LOG	= Logger.getLogger(LuceneSearcher.class);

	private final QueryParser	queryParser;
	private final Analyzer		analyzer;
	private IndexSearcher		indexSearcher;
	private final LuceneParser	luceneParser;
	private final String		docField;
	private final String		primary;
	private final String[]		termFields;

	/**
	 * Maximum number of results retrieved from the ChEBI index.
	 */
	private int					maxHits;

	public LuceneSearcher(final ChemicalDirectory dir) {
		this(dir, 500);
	}

	/**
	 * Basic constructor.
	 * 
	 * @param chebiIndex
	 *            Location of the ChEBI Lucene index.
	 * @param maxHits
	 *            maximum number of results retrieved from the ChEBI index.
	 * @throws IOException
	 */
	public LuceneSearcher(final ChemicalDirectory dir, final int maxHits) {
		try {
			indexSearcher = new IndexSearcher(DirectoryReader.open(dir
					.getDirectory()));
		} catch (final IOException e) {
			indexSearcher = null;
			LOG.fatal("Could not open directory for searching", e.getCause());
			System.exit(1);
		}
		analyzer = dir.getAnalyzer();
		queryParser = new QueryParser(Version.LUCENE_40, dir.getDefaultField(),
				analyzer);
		queryParser.setAllowLeadingWildcard(true);

		luceneParser = new LuceneParser();
		docField = dir.getDefaultField();
		primary = dir.getPrimaryKey();
		termFields = dir.getSearchFields();
		setMaxHits(maxHits);
	}

	public List<ChemicalSearchResult> searchCompoundName(final String name) {
		return searchCompoundName(name, false);
	}

	public List<ChemicalSearchResult> searchCompoundName(final String name,
			final boolean onlyChecked) {
		// Translate any wildcards intended for Oracle
		// (don't appear in compound names):
		final String n = name.replace('%', '*').replace('_', '?');
		List<ChemicalSearchResult> results = Collections.emptyList();
		try {

			final StringBuilder indexQuery = new StringBuilder();
			final String[] terms = new String[termFields.length];
			for (int i = 0; i < termFields.length; i++) {
				terms[i] = LuceneParser.parseUserTerms(n, termFields[i]);
			}

			indexQuery.append(LuceneParser.group(Operator.AND, null, false,
					null, LuceneParser.group(null, null, false, 20.0f, terms)));
			results = executeSearchQuery(indexQuery.toString());

		} catch (final IOException e) {
			LOG.error("Error searching", e.getCause());
		} catch (final ParseException e) {
			LOG.error("Error parsing query", e.getCause());
		}

		return results;
	}
	
	public List<ChemicalSearchResult> doRawSearch(String query) {
		List<ChemicalSearchResult> results = Collections.emptyList();
		
		try {
			results = executeSearchQuery(query);
		} catch (ParseException e) {
			LOG.error("Error parsing query", e.getCause());
		} catch (IOException e) {
			LOG.error("Error searching", e.getCause());
		}
		
		return results;
	}
	
	private List<ChemicalSearchResult> executeSearchQuery(String queryStr) throws ParseException, IOException {
		final Stopwatch watch = new Stopwatch();
		watch.start();
		final Query query = queryParser.parse(queryStr);
		final TopDocsCollector<?> collector = TopScoreDocCollector.create(
				maxHits, true);
		indexSearcher.search(query, collector);
		final ScoreDoc[] hits = collector.topDocs().scoreDocs;
		LOG.info("*** Found " + hits.length
				+ " compound(s) matching query '" + queryStr
				+ "' in " + watch.elapsedMillis() + " ms");
		watch.reset();
		ArrayList<ChemicalSearchResult> results = new ArrayList<>();
		if (hits.length > 0) {
			for (final ScoreDoc hit : hits) {
				final Document doc = indexSearcher.doc(hit.doc);
				results.add(new ChemicalSearchResult(doc.get(primary), doc
						.get(docField), hit.score));
			}
		}
		LOG.debug("*** Returned results in " + watch.elapsedMillis()
				+ " ms");
		
		return results;
	}

	/**
	 * Sets the maximum number of results retrieved from the ChEBI index.
	 * 
	 * @param maxHits
	 *            a positive integer.
	 */
	public final void setMaxHits(final int maxHits) {
		if (maxHits <= 0) {
			throw new IllegalArgumentException("Max hits must be positive");
		}
		this.maxHits = maxHits;
	}

}
