package edu.iup.chem.inventory.index;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;

public class MolecularFormulaAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(final String fieldName,
			final Reader reader) {
		return new TokenStreamComponents(new MolecularFormulaTokenizer(reader));
	}

}
