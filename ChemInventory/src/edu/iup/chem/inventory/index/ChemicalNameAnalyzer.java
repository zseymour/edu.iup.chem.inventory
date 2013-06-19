package edu.iup.chem.inventory.index;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.util.Version;

public class ChemicalNameAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(final String fieldName,
			final Reader reader) {
		final Tokenizer source = new WhitespaceTokenizer(Version.LUCENE_40,
				reader);
		final TokenStream result = new LowerCaseFilter(Version.LUCENE_40,
				source);

		return new TokenStreamComponents(source, result);
	}

}
