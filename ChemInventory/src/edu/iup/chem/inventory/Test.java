package edu.iup.chem.inventory;

import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import uk.ac.ebi.rhea.mapper.util.lucene.LuceneSearcher;
import edu.iup.chem.inventory.index.ChemicalDirectory;
import edu.iup.chem.inventory.index.ChemicalSearchResult;
import edu.iup.chem.inventory.index.Index;

public class Test {
	final static Logger	log	= Logger.getLogger(Test.class);

	/**
	 * @param args
	 */
	public static void main(final String[] args) {

		ConnectionPool.initializePool();
		Driver.login(new JFrame());
		Index.initializeDirectories();

		final ChemicalDirectory chemDir = Index.getChemicalDirectory();

		final LuceneSearcher chemicalSearch = new LuceneSearcher(chemDir);

		while (true) {
			final String queryStr = JOptionPane.showInputDialog("Enter query");
			if (queryStr == null || queryStr.length() < 1) {
				System.exit(0);
			}
			final Collection<ChemicalSearchResult> hits = chemicalSearch
					.searchCompoundName(queryStr);
			log.info("Number of hits: " + hits.size());
			log.info("-------------------------------");
			int index = 1;
			for (final ChemicalSearchResult hit : hits) {
				log.info(String.format("%2d: %s (%f)", index,
						hit.getAdditionalField(), hit.getScore()));
				index++;
			}
		}

		// final List<List<ChemicalRecord>> records = Index
		// .splitChemicals(new ChemicalDao().getAll());
		//
		// log.info("Number of sublists: " + records.size());
		// log.info("---------------------------------------");
		// int index = 1;
		// for (final List<ChemicalRecord> r : records) {
		// log.info(String.format("List %d: %d elements", index, r.size()));
		// index++;
		// }

	}
}
