package edu.iup.chem.inventory.search;

import org.apache.log4j.Logger;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;

public class ChemicalSubstructureSearcher {

	private static final Logger				log			= Logger.getLogger(ChemicalSubstructureSearcher.class);

	private static final SmilesParser		parser		= new SmilesParser(
																DefaultChemObjectBuilder
																		.getInstance());
	private static final SmilesGenerator	generator	= new SmilesGenerator();

	public static IAtomContainer getMoleculeFromSMILES(final String SMILES) {
		try {
			return parser.parseSmiles(SMILES);
		} catch (final InvalidSmilesException e) {
			log.debug("Invalid SMILES ---> " + SMILES);
			return new Molecule();
		}
	}

	public static String getSMILESFromMolecule(final IAtomContainer molecule) {
		return generator.createSMILES(molecule);
	}

	private final IAtomContainer	substructure;

	public ChemicalSubstructureSearcher(final IAtomContainer molecule) {
		substructure = molecule;
	}

	public ChemicalSubstructureSearcher(final String SMILES) {
		substructure = getMoleculeFromSMILES(SMILES);
	}

	public IAtomContainer getSubstructure() {
		return substructure;
	}

	public boolean isSubstructureOf(final String SMILES) {
		if (SMILES == null) {
			return false;
		}

		final IAtomContainer mol = getMoleculeFromSMILES(SMILES);

		try {
			return UniversalIsomorphismTester.isSubgraph(mol, substructure);
		} catch (final CDKException e) {
			log.debug("Error checking subgraph", e.getCause());
			return false;
		}

	}

}
