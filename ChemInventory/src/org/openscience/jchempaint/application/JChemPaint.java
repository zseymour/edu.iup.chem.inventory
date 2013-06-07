/*
 *  $RCSfile$
 *  $Author: egonw $
 *  $Date: 2007-01-04 17:26:00 +0000 (Thu, 04 Jan 2007) $
 *  $Revision: 7634 $
 *
 *  Copyright (C) 1997-2008 Stefan Kuhn
 *  Some portions Copyright (C) 2009 Konstantin Tokarev
 *
 *  Contact: cdk-jchempaint@lists.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *  All we ask is that proper credit is given for our work, which includes
 *  - but is not limited to - adding the above copyright notice to the beginning
 *  of your source code files, and to any copyright notice that you may distribute
 *  with programs based on this work.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.jchempaint.application;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemModel;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.RGroupQueryReader;
import org.openscience.cdk.io.SMILESReader;
import org.openscience.cdk.isomorphism.matchers.IRGroupQuery;
import org.openscience.cdk.isomorphism.matchers.RGroupQuery;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.layout.TemplateHandler;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;
import org.openscience.cdk.tools.manipulator.ReactionSetManipulator;
import org.openscience.jchempaint.AbstractJChemPaintPanel;
import org.openscience.jchempaint.GT;
import org.openscience.jchempaint.JCPPropertyHandler;
import org.openscience.jchempaint.JChemPaintPanel;
import org.openscience.jchempaint.controller.ControllerHub;
import org.openscience.jchempaint.controller.undoredo.IUndoRedoFactory;
import org.openscience.jchempaint.controller.undoredo.IUndoRedoable;
import org.openscience.jchempaint.controller.undoredo.UndoRedoHandler;
import org.openscience.jchempaint.dialog.WaitDialog;
import org.openscience.jchempaint.io.FileHandler;
import org.openscience.jchempaint.rgroups.RGroupHandler;

public class JChemPaint {

	public static int			instancecounter	= 1;
	public static List<JFrame>	frameList		= new ArrayList<JFrame>();
	public final static String	GUI_APPLICATION	= "application";

	private static void checkCoordinates(final IChemModel chemModel)
			throws CDKException {
		for (final IAtomContainer next : ChemModelManipulator
				.getAllAtomContainers(chemModel)) {
			if (!GeometryTools.get2DCoordinateCoverage(next).equals(
					GeometryTools.CoordinateCoverage.FULL)) {
				final String error = GT._("Not all atoms have 2D coordinates."
						+ " JCP can only show full 2D specified structures."
						+ " Shall we lay out the structure?");
				final int answer = JOptionPane.showConfirmDialog(null, error,
						"No 2D coordinates", JOptionPane.YES_NO_OPTION);

				if (answer == JOptionPane.NO_OPTION) {
					throw new CDKException(
							GT._("Cannot display without 2D coordinates"));
				} else {
					// CreateCoordinatesForFileDialog frame =
					// new CreateCoordinatesForFileDialog(chemModel);
					// frame.pack();
					// frame.show();

					WaitDialog.showDialog();
					final List<IAtomContainer> acs = ChemModelManipulator
							.getAllAtomContainers(chemModel);
					generate2dCoordinates(acs);
					WaitDialog.hideDialog();
					return;
				}
			}
		}

		/*
		 * Add implicit hydrogens (in ControllerParameters,
		 * autoUpdateImplicitHydrogens is true by default, so we need to do that
		 * anyway)
		 */
		final CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(chemModel
				.getBuilder());
		for (final IAtomContainer molecule : ChemModelManipulator
				.getAllAtomContainers(chemModel)) {
			if (molecule != null) {
				try {
					hAdder.addImplicitHydrogens(molecule);
				} catch (final CDKException e) {
					// do nothing
				}
			}
		}
	}

	/**
	 * Clean up chemical model ,removing duplicates empty molecules etc
	 * 
	 * @param chemModel
	 * @param avoidOverlap
	 * @throws CDKException
	 */
	public static void cleanUpChemModel(final IChemModel chemModel,
			final boolean avoidOverlap, final AbstractJChemPaintPanel panel)
			throws CDKException {
		JChemPaint.setReactionIDs(chemModel);
		JChemPaint.replaceReferencesWithClones(chemModel);

		// check the model is not completely empty
		if (ChemModelManipulator.getBondCount(chemModel) == 0
				&& ChemModelManipulator.getAtomCount(chemModel) == 0) {
			throw new CDKException(
					"Structure does not have bonds or atoms. Cannot depict structure.");
		}
		JChemPaint.removeDuplicateAtomContainers(chemModel);
		JChemPaint.checkCoordinates(chemModel);
		JChemPaint.removeEmptyAtomContainers(chemModel);

		if (avoidOverlap) {
			try {
				ControllerHub.avoidOverlap(chemModel);
			} catch (final Exception e) {
				JOptionPane.showMessageDialog(panel,
						GT._("Structure could not be generated"));
				throw new CDKException("Cannot depict structure");
			}
		}

		// We update implicit Hs in any case
		final CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher
				.getInstance(chemModel.getBuilder());
		for (final IAtomContainer container : ChemModelManipulator
				.getAllAtomContainers(chemModel)) {
			for (final IAtom atom : container.atoms()) {
				if (!(atom instanceof IPseudoAtom)) {
					try {
						final IAtomType type = matcher.findMatchingAtomType(
								container, atom);
						if (type != null
								&& type.getFormalNeighbourCount() != null) {
							final int connectedAtomCount = container
									.getConnectedAtomsCount(atom);
							atom.setImplicitHydrogenCount(type
									.getFormalNeighbourCount()
									- connectedAtomCount);
						}
					} catch (final CDKException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static IChemModel emptyModel() {
		final IChemModel chemModel = DefaultChemObjectBuilder.getInstance()
				.newInstance(IChemModel.class);
		chemModel.setMoleculeSet(chemModel.getBuilder().newInstance(
				IAtomContainerSet.class));
		chemModel.getMoleculeSet().addAtomContainer(
				chemModel.getBuilder().newInstance(IAtomContainer.class));
		return chemModel;
	}

	/**
	 * Helper method to generate 2d coordinates when JChempaint loads a molecule
	 * without 2D coordinates. Typically happens for SMILES strings.
	 * 
	 * @param molecules
	 * @throws Exception
	 */
	private static void generate2dCoordinates(
			final List<IAtomContainer> molecules) {
		final StructureDiagramGenerator sdg = new StructureDiagramGenerator();
		for (int atIdx = 0; atIdx < molecules.size(); atIdx++) {
			final IAtomContainer mol = molecules.get(atIdx);
			sdg.setMolecule(mol.getBuilder().newInstance(IAtomContainer.class,
					mol));
			try {
				sdg.generateCoordinates();
			} catch (final Exception e) {
				e.printStackTrace();
			}
			final IAtomContainer ac = sdg.getMolecule();
			for (int i = 0; i < ac.getAtomCount(); i++) {
				mol.getAtom(i).setPoint2d(ac.getAtom(i).getPoint2d());
			}
		}
	}

	/**
	 * Inserts a molecule into the current set, usually from Combobox or Insert
	 * field, with possible shifting of the existing set.
	 * 
	 * @param chemPaintPanel
	 * @param molecule
	 * @param generateCoordinates
	 * @param shiftPanel
	 * @throws CDKException
	 */
	public static void generateModel(
			final AbstractJChemPaintPanel chemPaintPanel,
			IAtomContainer molecule, final boolean generateCoordinates,
			final boolean shiftPasted) throws CDKException {
		if (molecule == null) {
			return;
		}

		final IChemModel chemModel = chemPaintPanel.getChemModel();
		IAtomContainerSet moleculeSet = chemModel.getMoleculeSet();
		if (moleculeSet == null) {
			moleculeSet = new AtomContainerSet();
		}

		// On copy & paste on top of an existing drawn structure, prevent the
		// pasted section to be drawn exactly on top or to far away from the
		// original by shifting it to a fixed position next to it.

		if (shiftPasted) {
			double maxXCurr = Double.NEGATIVE_INFINITY;
			double minXPaste = Double.POSITIVE_INFINITY;

			for (final IAtomContainer atc : moleculeSet.atomContainers()) {
				// Detect the right border of the current structure..
				for (final IAtom atom : atc.atoms()) {
					if (atom.getPoint2d().x > maxXCurr) {
						maxXCurr = atom.getPoint2d().x;
					}
				}
				// Detect the left border of the pasted structure..
				for (final IAtom atom : molecule.atoms()) {
					if (atom.getPoint2d().x < minXPaste) {
						minXPaste = atom.getPoint2d().x;
					}
				}
			}

			if (maxXCurr != Double.NEGATIVE_INFINITY
					&& minXPaste != Double.POSITIVE_INFINITY) {
				// Shift the pasted structure to be nicely next to the existing
				// one.
				final int MARGIN = 1;
				final double SHIFT = maxXCurr - minXPaste;
				for (final IAtom atom : molecule.atoms()) {
					atom.setPoint2d(new Point2d(atom.getPoint2d().x + MARGIN
							+ SHIFT, atom.getPoint2d().y));
				}
			}
		}

		if (generateCoordinates) {
			// now generate 2D coordinates
			final StructureDiagramGenerator sdg = new StructureDiagramGenerator();
			sdg.setTemplateHandler(new TemplateHandler(moleculeSet.getBuilder()));
			try {
				sdg.setMolecule(molecule);
				sdg.generateCoordinates(new Vector2d(0, 1));
				molecule = sdg.getMolecule();
			} catch (final Exception exc) {
				JOptionPane.showMessageDialog(chemPaintPanel,
						GT._("Structure could not be generated"));
				throw new CDKException("Cannot depict structure");
			}
		}

		if (moleculeSet.getAtomContainer(0).getAtomCount() == 0) {
			moleculeSet.getAtomContainer(0).add(molecule);
		} else {
			moleculeSet.addAtomContainer(molecule);
		}

		final IUndoRedoFactory undoRedoFactory = chemPaintPanel.get2DHub()
				.getUndoRedoFactory();
		final UndoRedoHandler undoRedoHandler = chemPaintPanel.get2DHub()
				.getUndoRedoHandler();

		if (undoRedoFactory != null) {
			final IUndoRedoable undoredo = undoRedoFactory
					.getAddAtomsAndBondsEdit(chemPaintPanel.get2DHub()
							.getIChemModel(), molecule, null, "Paste",
							chemPaintPanel.get2DHub());
			undoRedoHandler.postEdit(undoredo);
		}

		chemPaintPanel.getChemModel().setMoleculeSet(moleculeSet);
		chemPaintPanel.updateUndoRedoControls();
		chemPaintPanel.get2DHub().updateView();
	}

	/**
	 * Returns an IChemModel, using the reader provided (picked).
	 * 
	 * @param cor
	 * @param panel
	 * @return
	 * @throws CDKException
	 */
	public static IChemModel getChemModelFromReader(
			final ISimpleChemObjectReader cor,
			final AbstractJChemPaintPanel panel) throws CDKException {
		panel.get2DHub().setRGroupHandler(null);
		String error = null;
		ChemModel chemModel = null;
		IChemFile chemFile = null;
		if (cor.accepts(IChemFile.class) && chemModel == null) {
			// try to read a ChemFile
			try {
				chemFile = (IChemFile) cor.read((IChemObject) new ChemFile());
				if (chemFile == null) {
					error = "The object chemFile was empty unexpectedly!";
				}
			} catch (final Exception exception) {
				error = "Error while reading file: " + exception.getMessage();
				exception.printStackTrace();
			}
		}
		if (error != null) {
			throw new CDKException(error);
		}
		if (chemModel == null && chemFile != null) {
			chemModel = (ChemModel) chemFile.getChemSequence(0).getChemModel(0);
		}
		if (cor.accepts(ChemModel.class) && chemModel == null) {
			// try to read a ChemModel
			try {

				chemModel = (ChemModel) cor.read((IChemObject) new ChemModel());
				if (chemModel == null) {
					error = "The object chemModel was empty unexpectedly!";
				}
			} catch (final Exception exception) {
				error = "Error while reading file: " + exception.getMessage();
				exception.printStackTrace();
			}
		}

		// Smiles reading
		if (cor.accepts(AtomContainerSet.class) && chemModel == null) {
			// try to read a AtomContainer set
			try {
				final IAtomContainerSet som = cor.read(new AtomContainerSet());
				chemModel = new ChemModel();
				chemModel.setMoleculeSet(som);
				if (chemModel == null) {
					error = "The object chemModel was empty unexpectedly!";
				}
			} catch (final Exception exception) {
				error = "Error while reading file: " + exception.getMessage();
				exception.printStackTrace();
			}
		}

		// MDLV3000 reading
		if (cor.accepts(AtomContainer.class) && chemModel == null) {
			// try to read a AtomContainer
			final IAtomContainer mol = cor.read(new AtomContainer());
			if (mol != null) {
				try {
					final IAtomContainerSet newSet = new AtomContainerSet();
					newSet.addAtomContainer(mol);
					chemModel = new ChemModel();
					chemModel.setMoleculeSet(newSet);
					if (chemModel == null) {
						error = "The object chemModel was empty unexpectedly!";
					}
				} catch (final Exception exception) {
					error = "Error while reading file: "
							+ exception.getMessage();
					exception.printStackTrace();
				}
			}
		}

		// RGroupQuery reading
		if (cor.accepts(RGroupQuery.class) && chemModel == null) {
			final IRGroupQuery rgroupQuery = cor.read(new RGroupQuery());
			if (rgroupQuery != null) {
				try {
					chemModel = new ChemModel();
					final RGroupHandler rgHandler = new RGroupHandler(
							rgroupQuery);
					panel.get2DHub().setRGroupHandler(rgHandler);
					chemModel.setMoleculeSet(rgHandler
							.getMoleculeSet(chemModel));
					rgHandler.layoutRgroup();

				} catch (final Exception exception) {
					error = "Error while reading file: "
							+ exception.getMessage();
					exception.printStackTrace();
				}
			}
		}

		if (error != null) {
			throw new CDKException(error);
		}

		if (chemModel == null && chemFile != null) {
			chemModel = (ChemModel) chemFile.getChemSequence(0).getChemModel(0);
		}

		// SmilesParser sets valencies, switch off by default.
		if (cor instanceof SMILESReader) {
			final IAtomContainer allinone = JChemPaintPanel
					.getAllAtomContainersInOne(chemModel);
			for (int k = 0; k < allinone.getAtomCount(); k++) {
				allinone.getAtom(k).setValency(null);
			}
		}
		return chemModel;
	}

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		try {
			final String vers = System.getProperty("java.version");
			final String requiredJVM = "1.5.0";
			final Package self = Package
					.getPackage("org.openscience.jchempaint");
			String version = GT._("Could not determine JCP version");
			if (self != null) {
				version = JCPPropertyHandler.getInstance(true).getVersion();
			}
			if (vers.compareTo(requiredJVM) < 0) {
				System.err
						.println(GT
								._("WARNING: JChemPaint {0} must be run with a Java VM version {1} or higher.",
										new String[] { version, requiredJVM }));
				System.err.println(GT._("Your JVM version is {0}", vers));
				System.exit(1);
			}

			final Options options = new Options();
			options.addOption("h", "help", false, GT._("gives this help page"));
			options.addOption("v", "version", false,
					GT._("gives JChemPaints version number"));
			options.addOption("d", "debug", false,
					"switches on various debug options");
			options.addOption(OptionBuilder.withArgName("property=value")
					.hasArg().withValueSeparator()
					.withDescription(GT._("supported options are given below"))
					.create("D"));

			CommandLine line = null;
			try {
				final CommandLineParser parser = new PosixParser();
				line = parser.parse(options, args);
			} catch (final UnrecognizedOptionException exception) {
				System.err.println(exception.getMessage());
				System.exit(-1);
			} catch (final ParseException exception) {
				System.err.println("Unexpected exception: "
						+ exception.toString());
			}

			if (line.hasOption("v")) {
				System.out.println("JChemPaint v." + version + "\n");
				System.exit(0);
			}

			if (line.hasOption("h")) {
				System.out.println("JChemPaint v." + version + "\n");

				final HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("JChemPaint", options);

				// now report on the -D options
				System.out.println();
				System.out
						.println("The -D options are as follows (defaults in parathesis):");
				System.out.println("  cdk.debugging     [true|false] (false)");
				System.out.println("  cdk.debug.stdout  [true|false] (false)");
				System.out
						.println("  user.language     [ar|ca|cs|de|en|es|hu|nb|nl|pl|pt|ru|th] (en)");
				System.out
						.println("  user.language     [ar|ca|cs|de|hu|nb|nl|pl|pt_BR|ru|th] (EN)");

				System.exit(0);
			}
			boolean debug = false;
			if (line.hasOption("d")) {
				debug = true;
			}

			// Set Look&Feel
			final Properties props = JCPPropertyHandler.getInstance(true)
					.getJCPProperties();
			try {
				UIManager.setLookAndFeel(props.getProperty("LookAndFeelClass"));
			} catch (final Throwable e) {
				final String sys = UIManager.getSystemLookAndFeelClassName();
				UIManager.setLookAndFeel(sys);
				props.setProperty("LookAndFeelClass", sys);
			}

			// Language
			props.setProperty("General.language",
					System.getProperty("user.language", "en"));

			// Process command line arguments
			String modelFilename = "";
			args = line.getArgs();
			if (args.length > 0) {
				modelFilename = args[0];
				final File file = new File(modelFilename);
				if (!file.exists()) {
					System.err.println(GT._("File does not exist") + ": "
							+ modelFilename);
					System.exit(-1);
				}
				showInstance(file, null, null, debug);
			} else {
				showEmptyInstance(debug);
			}

		} catch (final Throwable t) {
			System.err.println("uncaught exception: " + t);
			t.printStackTrace(System.err);
		}
	}

	/**
	 * Read an IChemModel from a given file.
	 * 
	 * @param file
	 * @param type
	 * @param panel
	 * @return
	 * @throws CDKException
	 * @throws FileNotFoundException
	 */
	public static IChemModel readFromFile(final File file, final String type,
			final AbstractJChemPaintPanel panel) throws CDKException,
			FileNotFoundException {
		final String url = file.toURI().toString();
		ISimpleChemObjectReader cor = null;
		try {
			cor = FileHandler.createReader(file.toURI().toURL(), url, type);
		} catch (final MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (cor instanceof CMLReader) {
			cor.setReader(new FileInputStream(file)); // hack
		} else {
			cor.setReader(new FileReader(file)); // hack
		}

		final IChemModel chemModel = JChemPaint.getChemModelFromReader(cor,
				panel);
		boolean avoidOverlap = true;
		if (cor instanceof RGroupQueryReader) {
			avoidOverlap = false;
		}

		JChemPaint.cleanUpChemModel(chemModel, avoidOverlap, panel);

		return chemModel;
	}

	public static IChemModel readFromFileReader(final URL fileURL,
			final String url, final String type,
			final AbstractJChemPaintPanel panel) throws CDKException {

		IChemModel chemModel = null;
		WaitDialog.showDialog();

		// InChI workaround - guessing for InChI results into an INChIReader
		// (this does not work, we'd need an INChIPlainTextReader..)
		// Instead here we use STDInChIReader, to be consistent throughout JCP
		// using the nestedVm based classes.
		try {
			ISimpleChemObjectReader cor = null;
			if (url.endsWith("txt")) {
				// chemModel = StdInChIReader.readInChI(fileURL);
			} else {
				cor = FileHandler.createReader(fileURL, url, type);
				chemModel = JChemPaint.getChemModelFromReader(cor, panel);
			}
			boolean avoidOverlap = true;
			if (cor instanceof RGroupQueryReader) {
				avoidOverlap = false;
			}
			JChemPaint.cleanUpChemModel(chemModel, avoidOverlap, panel);

		} finally {
			WaitDialog.hideDialog();
		}
		return chemModel;
	}

	private static void removeDuplicateAtomContainers(final IChemModel chemModel) {
		// we remove molecules which are in AtomContainerSet as well as in a
		// reaction
		final IReactionSet reactionSet = chemModel.getReactionSet();
		final IAtomContainerSet moleculeSet = chemModel.getMoleculeSet();
		if (reactionSet != null && moleculeSet != null) {
			final List<IAtomContainer> aclist = ReactionSetManipulator
					.getAllAtomContainers(reactionSet);
			for (int i = moleculeSet.getAtomContainerCount() - 1; i >= 0; i--) {
				for (int k = 0; k < aclist.size(); k++) {
					final String label = moleculeSet.getAtomContainer(i)
							.getID();
					if (aclist.get(k).getID().equals(label)) {
						chemModel.getMoleculeSet().removeAtomContainer(i);
						break;
					}
				}
			}
		}
	}

	private static void removeEmptyAtomContainers(final IChemModel chemModel) {
		final IAtomContainerSet moleculeSet = chemModel.getMoleculeSet();
		if (moleculeSet != null && moleculeSet.getAtomContainerCount() == 0) {
			chemModel.setMoleculeSet(null);
		}
	}

	private static void replaceReferencesWithClones(final IChemModel chemModel)
			throws CDKException {
		// we make references in products/reactants clones, since same compounds
		// in different reactions need separate layout (different positions etc)
		if (chemModel.getReactionSet() != null) {
			for (final IReaction reaction : chemModel.getReactionSet()
					.reactions()) {
				int i = 0;
				final IAtomContainerSet products = reaction.getProducts();
				for (final IAtomContainer product : products.atomContainers()) {
					try {
						products.replaceAtomContainer(i, product.clone());
					} catch (final CloneNotSupportedException e) {
					}
					i++;
				}
				i = 0;
				final IAtomContainerSet reactants = reaction.getReactants();
				for (final IAtomContainer reactant : reactants.atomContainers()) {
					try {
						reactants.replaceAtomContainer(i, reactant.clone());
					} catch (final CloneNotSupportedException e) {
					}
					i++;
				}
			}
		}
	}

	private static void setReactionIDs(final IChemModel chemModel) {
		// we give all reactions an ID, in case they have none
		// IDs are needed for handling in JCP
		final IReactionSet reactionSet = chemModel.getReactionSet();
		if (reactionSet != null) {
			int i = 0;
			for (final IReaction reaction : reactionSet.reactions()) {
				if (reaction.getID() == null) {
					reaction.setID("Reaction " + ++i);
				}
			}
		}
	}

	public static void showEmptyInstance(final boolean debug) {
		final IChemModel chemModel = emptyModel();
		showInstance(chemModel, GT._("Untitled") + " " + instancecounter++,
				debug);
	}

	public static void showInstance(final File inFile, final String type,
			final AbstractJChemPaintPanel jcpPanel, final boolean debug) {
		try {
			final IChemModel chemModel = JChemPaint.readFromFile(inFile, type,
					jcpPanel);

			final String name = inFile.getName();
			final JChemPaintPanel p = JChemPaint.showInstance(chemModel, name,
					debug);
			p.setCurrentWorkDirectory(inFile.getParentFile());
			p.setLastOpenedFile(inFile);
			p.setIsAlreadyAFile(inFile);
		} catch (final CDKException ex) {
			JOptionPane.showMessageDialog(jcpPanel, ex.getMessage());
			return;
		} catch (final FileNotFoundException e) {
			JOptionPane.showMessageDialog(jcpPanel, GT._("File does not exist")
					+ ": " + inFile.getPath());
			return;
		}
	}

	public static JChemPaintPanel showInstance(final IChemModel chemModel,
			final String title, final boolean debug) {
		final JFrame f = new JFrame(title + " - JChemPaint");
		chemModel.setID(title);
		f.addWindowListener(new JChemPaintPanel.AppCloser());
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		final JChemPaintPanel p = new JChemPaintPanel(chemModel,
				GUI_APPLICATION, debug, null, new ArrayList<String>());
		p.updateStatusBar();
		f.setPreferredSize(new Dimension(800, 494)); // 1.618
		f.add(p);
		f.pack();
		final Point point = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getCenterPoint();
		final int w2 = f.getWidth() / 2;
		final int h2 = f.getHeight() / 2;
		f.setLocation(point.x - w2, point.y - h2);
		f.setVisible(true);
		frameList.add(f);
		return p;
	}

}
