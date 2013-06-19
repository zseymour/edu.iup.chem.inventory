package edu.iup.chem.inventory.index;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.formula.MolecularFormula;
import org.openscience.cdk.interfaces.IElement;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

public class MolecularFormulaTokenizer extends Tokenizer {
	private final boolean			done		= false;
	private final CharTermAttribute	termAtt		= addAttribute(CharTermAttribute.class);
	private List<IElement>			elements	= null;

	protected MolecularFormulaTokenizer(final Reader input) {
		super(input);
	}

	@Override
	public boolean incrementToken() throws IOException {

		if (elements == null) {
			final char[] arr = new char[8 * 1024];
			final StringBuffer sb = new StringBuffer();
			int numChars;
			while ((numChars = input.read(arr, 0, arr.length)) > 0) {
				sb.append(arr, 0, numChars);
			}
			final String formula = sb.toString();
			MolecularFormula mf;
			try {
				mf = (MolecularFormula) MolecularFormulaManipulator
						.getMolecularFormula(formula,
								DefaultChemObjectBuilder.getInstance());
			} catch (final Exception e) {
				return false;
			}
			elements = MolecularFormulaManipulator.elements(mf);
			System.out.println(elements.size());
			termAtt.append(elements.get(0).getSymbol());
			elements.remove(0);

			return true;
		} else if (!elements.isEmpty()) {
			addAttribute(CharTermAttribute.class).append(
					elements.get(0).getSymbol());
			elements.remove(0);

			return true;
		}

		return false;

	}
}
