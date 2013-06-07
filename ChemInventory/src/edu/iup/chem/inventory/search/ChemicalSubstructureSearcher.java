package edu.iup.chem.inventory.search;

import org.apache.log4j.Logger;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.fingerprint.ExtendedFingerprinter;
import org.openscience.cdk.fingerprint.FingerprinterTool;
import org.openscience.cdk.fingerprint.IBitFingerprint;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.similarity.Tanimoto;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smsd.Isomorphism;
import org.openscience.cdk.smsd.interfaces.Algorithm;

public class ChemicalSubstructureSearcher {

	private static final Logger				log			= Logger.getLogger(ChemicalSubstructureSearcher.class);

	private static final SmilesParser		parser		= new SmilesParser(
																DefaultChemObjectBuilder
																		.getInstance());
	private static final SmilesGenerator	generator	= new SmilesGenerator(
																true);
	private static final Isomorphism		comparer	= new Isomorphism(
																Algorithm.TurboSubStructure,
																true);

	public static boolean contains(final String structureSMILES,
			final String substructureSMILES) {
		if (structureSMILES == null) {
			return false;
		}

		final IAtomContainer structureMol = getMoleculeFromSMILES(structureSMILES);
		final IAtomContainer substructureMol = getMoleculeFromSMILES(substructureSMILES);
		try {
			return new UniversalIsomorphismTester().isSubgraph(structureMol,
					substructureMol)
					&& FingerprinterTool.isSubset(getFingerprint(structureMol)
							.asBitSet(), getFingerprint(substructureMol)
							.asBitSet());
		} catch (final CDKException e) {
			log.debug("Error checking subgraph", e.getCause());
			return false;
		}
	}

	public static IBitFingerprint getFingerprint(final IAtomContainer mol)
			throws CDKException {
		return new ExtendedFingerprinter().getBitFingerprint(mol);
	}

	public static IAtomContainer getMoleculeFromSMILES(final String SMILES) {
		parser.setPreservingAromaticity(true);
		try {
			return parser.parseSmiles(SMILES);
		} catch (final InvalidSmilesException e) {
			log.debug("Invalid SMILES ---> " + SMILES, e.getCause());
			return new AtomContainer();
		}
	}

	public static String getSMILESFromMolecule(final IAtomContainer molecule) {
		return generator.createSMILES(molecule);
	}

	public static boolean isAlcohol(final String SMILES) {
		return contains(SMILES, "OC") || contains(SMILES, "COC")
				|| contains(SMILES, "CC(C)=O");
	}

	public static boolean isAromatic(final String SMILES) {
		return contains(SMILES, "c1ccccc1");
	}

	public static boolean isCyanohydrin(final String SMILES) {
		return contains(SMILES, "N#CCO");
	}

	public static boolean isOrganicAcid(final String SMILES) {
		return contains(SMILES, "O=CO") || contains(SMILES, "O=S(=O)O")
				|| contains(SMILES, "Oc1ccccc1") || contains(SMILES, "O=COC=O");
	}

	public static boolean isOrganicBase(final String SMILES) {
		return isAlcohol(SMILES) && contains(SMILES, "CN")
				|| contains(SMILES, "CN");
	}

	public static boolean isOxidizer(final String SMILES) {
		return contains(SMILES, "OO");
	}

	private final IAtomContainer	substructure;

	public ChemicalSubstructureSearcher(final IAtomContainer molecule) {
		substructure = molecule;
	}

	public ChemicalSubstructureSearcher(final String SMILES) {
		substructure = getMoleculeFromSMILES(SMILES);
	}

	public boolean contains(final IAtomContainer structure) {
		try {
			return new UniversalIsomorphismTester().isSubgraph(structure,
					substructure)
					&& FingerprinterTool.isSubset(getFingerprint(structure)
							.asBitSet(), getFingerprint(substructure)
							.asBitSet());
		} catch (final CDKException e) {
			log.debug("Error checking subgraph", e.getCause());
			return false;
		}
	}

	public boolean containsSMSD(final IAtomContainer superStructure) {
		try {
			if (superStructure.isEmpty()) {
				return false;
			}
			comparer.init(substructure, superStructure, false, false);
			comparer.setChemFilters(false, false, false);
			return comparer.isSubgraph();
		} catch (final CDKException e) {
			log.error("Substructure search failed.");
			return false;
		}
	}

	private float getSimilarity(final IAtomContainer molecule) {
		try {
			return (float) Tanimoto.calculate(getFingerprint(molecule),
					getFingerprint(substructure));
		} catch (final CDKException e) {
			log.warn("Could not calculate similarity");
			return 0f;
		}
	}

	public float getSimilarity(final String SMILES) {
		return getSimilarity(getMoleculeFromSMILES(SMILES));
	}

	public IAtomContainer getSubstructure() {
		return substructure;
	}

	public boolean isSubstructureOf(final String SMILES) {
		return contains(getMoleculeFromSMILES(SMILES));
	}
}
