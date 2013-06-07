/* $Revision: 7636 $ $Author: egonw $ $Date: 2007-01-04 18:46:10 +0100 (Thu, 04 Jan 2007) $
 *
 * Copyright (C) 2007-2008  Egon Willighagen <egonw@users.sf.net>
 *               2005-2007  Christoph Steinbeck <steinbeck@users.sf.net>
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All I ask is that proper credit is given for my work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.jchempaint.controller;

import java.awt.Cursor;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IBond.Order;
import org.openscience.cdk.interfaces.IBond.Stereo;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.interfaces.ISingleElectron;
import org.openscience.cdk.layout.AtomPlacer;
import org.openscience.cdk.layout.RingPlacer;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.layout.TemplateHandler;
import org.openscience.cdk.tools.SaturationChecker;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.AtomContainerSetManipulator;
import org.openscience.cdk.tools.manipulator.BondManipulator;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.openscience.cdk.tools.manipulator.ReactionManipulator;
import org.openscience.cdk.validate.ProblemMarker;
import org.openscience.jchempaint.RenderPanel;
import org.openscience.jchempaint.applet.JChemPaintAbstractApplet;
import org.openscience.jchempaint.controller.undoredo.IUndoRedoFactory;
import org.openscience.jchempaint.controller.undoredo.IUndoRedoable;
import org.openscience.jchempaint.controller.undoredo.UndoRedoHandler;
import org.openscience.jchempaint.renderer.BoundsCalculator;
import org.openscience.jchempaint.renderer.IRenderer;
import org.openscience.jchempaint.renderer.RendererModel;
import org.openscience.jchempaint.renderer.generators.IGenerator;
import org.openscience.jchempaint.renderer.generators.RGroupGenerator;
import org.openscience.jchempaint.renderer.selection.IChemObjectSelection;
import org.openscience.jchempaint.renderer.selection.IncrementalSelection;
import org.openscience.jchempaint.rgroups.RGroupHandler;

/**
 * Class that will central interaction point between a mouse event throwing
 * widget (SWT or Swing) and the Controller2D modules. IMPORTANT: All actions in
 * this class must adhere to the following rules: - They keep any fragments in
 * separate Molecules in the SetOfMolecules, i. e. if splits or merges are done,
 * they must handle this (precondition and postcondition: Each Molecule in
 * SetOfMolecules is a linked graph). - The chemModel always contains a
 * SetOfMolecules with at least one Molecule, this can be empty. No other
 * containers are allowed to be empty (precondition and postcondition:
 * SetOfMolecules.getAtomContainerCount>0, atomCount>0 for all Molecules in
 * SetOfMolecules where index>0).
 * 
 * @cdk.svnrev $Revision: 9162 $
 * @cdk.module controlbasic
 * @author Niels Out
 * @author egonw
 */
public class ControllerHub implements IMouseEventRelay, IChemModelRelay {

	public static void avoidOverlap(final IChemModel chemModel) {
		// we avoid overlaps
		// first we we shift down the reactions
		Rectangle2D usedReactionbounds = null;
		if (chemModel.getReactionSet() != null) {
			for (final IReaction reaction : chemModel.getReactionSet()
					.reactions()) {
				// now move it so that they don't overlap
				final Rectangle2D reactionbounds = BoundsCalculator
						.calculateBounds(reaction);
				if (usedReactionbounds != null) {
					final double bondLength = GeometryTools
							.getBondLengthAverage(reaction);
					final Rectangle2D shiftedBounds = GeometryTools
							.shiftReactionVertical(reaction, reactionbounds,
									usedReactionbounds, bondLength);
					usedReactionbounds = usedReactionbounds
							.createUnion(shiftedBounds);
				} else {
					usedReactionbounds = reactionbounds;
				}
			}
		}
		// then we shift the molecules not to overlap
		Rectangle2D usedBounds = null;
		if (chemModel.getMoleculeSet() != null) {
			for (final IAtomContainer container : AtomContainerSetManipulator
					.getAllAtomContainers(chemModel.getMoleculeSet())) {
				// now move it so that they don't overlap
				final Rectangle2D bounds = BoundsCalculator
						.calculateBounds(container);
				if (usedBounds != null) {
					final double bondLength = GeometryTools
							.getBondLengthAverage(container);
					final Rectangle2D shiftedBounds = GeometryTools
							.shiftContainer(container, bounds, usedBounds,
									bondLength);
					usedBounds = usedBounds.createUnion(shiftedBounds);
				} else {
					usedBounds = bounds;
				}
			}
		}
		// and the products/reactants in every reaction
		if (chemModel.getReactionSet() != null) {
			for (final IReaction reaction : chemModel.getReactionSet()
					.reactions()) {
				usedBounds = null;
				double gap = 0;
				double centerY = 0;
				for (final IAtomContainer container : ReactionManipulator
						.getAllAtomContainers(reaction)) {
					// now move it so that they don't overlap
					final Rectangle2D bounds = BoundsCalculator
							.calculateBounds(container);
					if (usedBounds != null) {
						if (gap == 0) {
							gap = GeometryTools.getBondLengthAverage(container);
							if (Double.isNaN(gap)) {
								gap = 1.5;
							}
						}
						final Rectangle2D shiftedBounds = GeometryTools
								.shiftContainer(container, bounds, usedBounds,
										gap * 2);
						final double yshift = centerY - bounds.getCenterY();
						final Vector2d shift = new Vector2d(0.0, yshift);
						GeometryTools.translate2D(container, shift);
						usedBounds = usedBounds.createUnion(shiftedBounds);
					} else {
						usedBounds = bounds;
						centerY = bounds.getCenterY();
					}
				}
				// we shift the products an extra bit to make a larget gap
				// between products and reactants
				for (final IAtomContainer container : reaction.getProducts()
						.atomContainers()) {
					final Vector2d shift = new Vector2d(gap * 2, 0.0);
					GeometryTools.translate2D(container, shift);
				}
			}
		}
		// TODO overlaps of molecules in molecule set and reactions (ok, not too
		// common, but still...)
	}

	/**
	 * Change the stereo bond from start->end to start<-end.
	 * 
	 * @param bond
	 *            the bond to change
	 * @param stereo
	 *            the current stereo of that bond
	 */
	private static void flipDirection(final IBond bond,
			final IBond.Stereo stereo) {
		if (stereo == IBond.Stereo.UP) {
			bond.setStereo(IBond.Stereo.UP_INVERTED);
		} else if (stereo == IBond.Stereo.UP_INVERTED) {
			bond.setStereo(IBond.Stereo.UP);
		} else if (stereo == IBond.Stereo.DOWN_INVERTED) {
			bond.setStereo(IBond.Stereo.DOWN);
		} else if (stereo == IBond.Stereo.DOWN) {
			bond.setStereo(IBond.Stereo.DOWN_INVERTED);
		} else if (stereo == IBond.Stereo.UP_OR_DOWN) {
			bond.setStereo(IBond.Stereo.UP_OR_DOWN_INVERTED);
		} else if (stereo == IBond.Stereo.UP_OR_DOWN_INVERTED) {
			bond.setStereo(IBond.Stereo.UP_OR_DOWN);
		}
	}

	public static void generateNewCoordinates(final IAtomContainer container) {
		final IChemObjectBuilder builder = DefaultChemObjectBuilder
				.getInstance();

		if (diagramGenerator == null) {
			diagramGenerator = new StructureDiagramGenerator();
			diagramGenerator.setTemplateHandler(new TemplateHandler(builder));
		}
		if (container instanceof IAtomContainer) {
			diagramGenerator.setMolecule(container);
		} else {
			diagramGenerator.setMolecule(builder.newInstance(
					IAtomContainer.class, container));
		}

		try {
			diagramGenerator.generateExperimentalCoordinates();
			final IAtomContainer cleanedMol = diagramGenerator.getMolecule();
			// now copy/paste coordinates
			for (int i = 0; i < cleanedMol.getAtomCount(); i++) {
				container.getAtom(i).setPoint2d(
						cleanedMol.getAtom(i).getPoint2d());
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static boolean isDown(final IBond.Stereo stereo) {
		return stereo == IBond.Stereo.DOWN
				|| stereo == IBond.Stereo.DOWN_INVERTED;
	}

	private static boolean isUndefined(final IBond.Stereo stereo) {
		return stereo == IBond.Stereo.UP_OR_DOWN
				|| stereo == IBond.Stereo.UP_OR_DOWN_INVERTED;
	}

	private static boolean isUp(final IBond.Stereo stereo) {
		return stereo == IBond.Stereo.UP || stereo == IBond.Stereo.UP_INVERTED;
	}

	public static void removeEmptyContainers(final IChemModel chemModel) {
		final Iterator<IAtomContainer> it = ChemModelManipulator
				.getAllAtomContainers(chemModel).iterator();
		while (it.hasNext()) {
			final IAtomContainer ac = it.next();
			if (ac.getAtomCount() == 0) {
				chemModel.getMoleculeSet().removeAtomContainer(ac);
			}
		}
		if (chemModel.getMoleculeSet().getAtomContainerCount() == 0) {
			chemModel.getMoleculeSet().addAtomContainer(
					chemModel.getBuilder().newInstance(IAtomContainer.class));
		}
	}

	private IChemModel							chemModel;
	private final IControllerModel				controllerModel;

	private final IRenderer						renderer;

	private final RenderPanel					eventRelay;

	private final List<IControllerModule>		generalModules;

	private final List<IChangeModeListener>		changeModeListeners	= new ArrayList<>();

	private static StructureDiagramGenerator	diagramGenerator;

	private IControllerModule					activeDrawModule;

	private IControllerModule					fallbackModule;

	private final static RingPlacer				ringPlacer			= new RingPlacer();

	private IAtomContainer						phantoms;

	private IChemModelEventRelayHandler			changeHandler;

	private final IUndoRedoFactory				undoredofactory;

	private final UndoRedoHandler				undoredohandler;

	private final CDKAtomTypeMatcher			matcher;

	private static RGroupHandler				rGroupHandler;

	int											oldMouseCursor		= Cursor.DEFAULT_CURSOR;

	private Point2d								phantomArrowStart	= null;

	private Point2d								phantomArrowEnd		= null;

	private Point2d								phantomTextPosition	= null;

	private String								phantomText			= null;

	public ControllerHub(final IControllerModel controllerModel,
			final IRenderer renderer, final IChemModel chemModel,
			final RenderPanel eventRelay,
			final UndoRedoHandler undoredohandler,
			final IUndoRedoFactory undoredofactory, final boolean isViewer,
			final JChemPaintAbstractApplet applet) {
		this.controllerModel = controllerModel;
		this.renderer = renderer;
		this.chemModel = chemModel;
		this.eventRelay = eventRelay;
		phantoms = chemModel.getBuilder().newInstance(IAtomContainer.class);
		this.undoredofactory = undoredofactory;
		this.undoredohandler = undoredohandler;
		generalModules = new ArrayList<>();
		if (!isViewer) {
			registerGeneralControllerModule(new ZoomModule(this));
		}
		registerGeneralControllerModule(new HighlightModule(this, applet));
		matcher = CDKAtomTypeMatcher.getInstance(chemModel.getBuilder());
	}

	// OK
	@Override
	public IAtom addAtom(final String atomType, final IAtom atom,
			final boolean makePseudoAtom) {
		final IAtomContainer undoRedoContainer = atom.getBuilder().newInstance(
				IAtomContainer.class);
		undoRedoContainer.addAtom(addAtomWithoutUndo(atomType, atom,
				makePseudoAtom));
		final IAtomContainer atomContainer = ChemModelManipulator
				.getRelevantAtomContainer(getIChemModel(),
						undoRedoContainer.getAtom(0));
		final IBond newBond = atomContainer.getBond(atom,
				undoRedoContainer.getAtom(0));
		undoRedoContainer.addBond(newBond);
		if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getAddAtomsAndBondsEdit(chemModel, undoRedoContainer,
							null, "Add Atom", this);
			getUndoRedoHandler().postEdit(undoredo);
		}
		return undoRedoContainer.getAtom(0);
	}

	// OK
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openscience.cdk.controller.IChemModelRelay#addAtom(java.lang.String,
	 * int, javax.vecmath.Point2d)
	 */
	@Override
	public IAtom addAtom(final String atomType, final int isotopeNumber,
			final Point2d worldCoord, final boolean makePseudoAtom) {
		final IAtomContainer undoRedoContainer = chemModel.getBuilder()
				.newInstance(IAtomContainer.class);
		undoRedoContainer.addAtom(addAtomWithoutUndo(atomType, isotopeNumber,
				worldCoord, makePseudoAtom));
		if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getAddAtomsAndBondsEdit(chemModel, undoRedoContainer,
							null, "Add Atom", this);
			getUndoRedoHandler().postEdit(undoredo);
		}
		return undoRedoContainer.getAtom(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openscience.cdk.controller.IChemModelRelay#addAtom(java.lang.String,
	 * javax.vecmath.Point2d)
	 */
	@Override
	public IAtom addAtom(final String atomType, final Point2d worldCoord,
			final boolean makePseudoAtom) {
		return addAtom(atomType, 0, worldCoord, makePseudoAtom);
	}

	// OK
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openscience.cdk.controller.IChemModelRelay#addAtomWithoutUndo(java
	 * .lang.String, org.openscience.cdk.interfaces.IAtom)
	 */
	@Override
	public IAtom addAtomWithoutUndo(final String atomType, final IAtom atom,
			final boolean makePseudoAtom) {
		return addAtomWithoutUndo(atomType, atom, IBond.Stereo.NONE,
				makePseudoAtom);
	}

	public IAtom addAtomWithoutUndo(final String atomType, final IAtom atom,
			final IBond.Stereo stereo, final boolean makePseudoAtom) {
		return addAtomWithoutUndo(atomType, atom, stereo, IBond.Order.SINGLE,
				makePseudoAtom);
	}

	// OK
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openscience.cdk.controller.IChemModelRelay#addAtomWithoutUndo(java
	 * .lang.String, org.openscience.cdk.interfaces.IAtom, int)
	 */
	@Override
	public IAtom addAtomWithoutUndo(final String atomType, final IAtom atom,
			final IBond.Stereo stereo, final Order order,
			final boolean makePseudoAtom) {
		IAtom newAtom;
		if (makePseudoAtom) {
			newAtom = chemModel.getBuilder().newInstance(IPseudoAtom.class,
					atomType);
		} else {
			newAtom = chemModel.getBuilder().newInstance(IAtom.class, atomType);
		}
		IBond newBond;
		if (order == IBond.Order.DOUBLE) {
			newBond = chemModel.getBuilder().newInstance(IBond.class, atom,
					newAtom, CDKConstants.BONDORDER_DOUBLE, stereo);
		} else if (order == IBond.Order.TRIPLE) {
			newBond = chemModel.getBuilder().newInstance(IBond.class, atom,
					newAtom, CDKConstants.BONDORDER_TRIPLE, stereo);
		} else {
			newBond = chemModel.getBuilder().newInstance(IBond.class, atom,
					newAtom, CDKConstants.BONDORDER_SINGLE, stereo);
		}

		IAtomContainer atomCon = ChemModelManipulator.getRelevantAtomContainer(
				chemModel, atom);
		if (atomCon == null) {
			atomCon = chemModel.getBuilder().newInstance(IAtomContainer.class);
			IAtomContainerSet moleculeSet = chemModel.getMoleculeSet();
			if (moleculeSet == null) {
				moleculeSet = chemModel.getBuilder().newInstance(
						IAtomContainerSet.class);
				chemModel.setMoleculeSet(moleculeSet);
			}
			moleculeSet.addAtomContainer(atomCon);
		}

		// The AtomPlacer generates coordinates for the new atom
		final AtomPlacer atomPlacer = new AtomPlacer();
		atomPlacer.setMolecule(chemModel.getBuilder().newInstance(
				IAtomContainer.class, atomCon));
		double bondLength;
		if (atomCon.getBondCount() >= 1) {
			bondLength = GeometryTools.getBondLengthAverage(atomCon);
		} else {
			bondLength = calculateAverageBondLength(chemModel.getMoleculeSet());
		}

		// determine the atoms which define where the
		// new atom should not be placed
		final List<IAtom> connectedAtoms = atomCon.getConnectedAtomsList(atom);

		if (connectedAtoms.size() == 0) {
			final Point2d newAtomPoint = new Point2d(atom.getPoint2d());
			final double angle = Math.toRadians(-30);
			final Vector2d vec1 = new Vector2d(Math.cos(angle), Math.sin(angle));
			vec1.scale(bondLength);
			newAtomPoint.add(vec1);
			newAtom.setPoint2d(newAtomPoint);
		} else if (connectedAtoms.size() == 1) {
			final IAtomContainer ac = atomCon.getBuilder().newInstance(
					IAtomContainer.class);
			ac.addAtom(atom);
			ac.addAtom(newAtom);
			final Point2d distanceMeasure = new Point2d(0, 0); // XXX not sure
																// about
			// this?
			final IAtom connectedAtom = connectedAtoms.get(0);
			final Vector2d v = atomPlacer.getNextBondVector(atom,
					connectedAtom, distanceMeasure, true);
			atomPlacer.placeLinearChain(ac, v, bondLength);
		} else {
			final IAtomContainer placedAtoms = atomCon.getBuilder()
					.newInstance(IAtomContainer.class);
			for (final IAtom conAtom : connectedAtoms) {
				placedAtoms.addAtom(conAtom);
			}
			final Point2d center2D = GeometryTools.get2DCenter(placedAtoms);

			final IAtomContainer unplacedAtoms = atomCon.getBuilder()
					.newInstance(IAtomContainer.class);
			unplacedAtoms.addAtom(newAtom);

			atomPlacer.distributePartners(atom, placedAtoms, center2D,
					unplacedAtoms, bondLength);
		}

		atomCon.addAtom(newAtom);
		atomCon.addBond(newBond);
		updateAtom(newBond.getAtom(0));
		updateAtom(newBond.getAtom(1));

		// shift the new atom a bit if it is in range of another atom
		final RendererModel model = getRenderer().getRenderer2DModel();
		final double nudgeDistance = model.getHighlightDistance()
				/ model.getScale();
		if (getClosestAtom(newAtom) != null) {
			newAtom.getPoint2d().x += nudgeDistance;
		}

		structureChanged();
		return newAtom;
	}

	// OK
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openscience.cdk.controller.IChemModelRelay#addAtomWithoutUndo(java
	 * .lang.String, int, javax.vecmath.Point2d)
	 */
	public IAtom addAtomWithoutUndo(final String atomType,
			final int isotopeNumber, final Point2d worldCoord,
			final boolean makePseudoAtom) {
		IAtom newAtom;
		if (makePseudoAtom) {
			newAtom = chemModel.getBuilder().newInstance(IPseudoAtom.class,
					atomType, worldCoord);
		} else {
			newAtom = chemModel.getBuilder().newInstance(IAtom.class, atomType,
					worldCoord);
		}
		if (isotopeNumber != 0) {
			newAtom.setMassNumber(isotopeNumber);
		}
		// FIXME : there should be an initial hierarchy?
		IAtomContainerSet molSet = chemModel.getMoleculeSet();
		if (molSet == null) {
			molSet = chemModel.getBuilder()
					.newInstance(IAtomContainerSet.class);
			final IAtomContainer ac = chemModel.getBuilder().newInstance(
					IAtomContainer.class);
			ac.addAtom(newAtom);
			molSet.addAtomContainer(ac);
			chemModel.setMoleculeSet(molSet);
		}
		IAtomContainer newAtomContainer = chemModel.getBuilder().newInstance(
				IAtomContainer.class);
		if (chemModel.getMoleculeSet().getAtomContainer(0).getAtomCount() == 0) {
			newAtomContainer = chemModel.getMoleculeSet().getAtomContainer(0);
		} else {
			molSet.addAtomContainer(newAtomContainer);
		}
		newAtomContainer.addAtom(newAtom);
		updateAtom(newAtom);
		final RendererModel model = getRenderer().getRenderer2DModel();
		final double nudgeDistance = model.getHighlightDistance()
				/ model.getScale();
		if (getClosestAtom(newAtom) != null) {
			newAtom.getPoint2d().x += nudgeDistance;
		}
		structureChanged();
		return newAtom;
	}

	// OK
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openscience.cdk.controller.IChemModelRelay#addAtomWithoutUndo(java
	 * .lang.String, javax.vecmath.Point2d)
	 */
	@Override
	public IAtom addAtomWithoutUndo(final String atomType,
			final Point2d worldCoord, final boolean makePseudoAtom) {
		return addAtomWithoutUndo(atomType, 0, worldCoord, makePseudoAtom);
	}

	// OK
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openscience.cdk.controller.IChemModelRelay#addBond(org.openscience
	 * .cdk.interfaces.IAtom, org.openscience.cdk.interfaces.IAtom)
	 */
	@Override
	public IBond addBond(final IAtom fromAtom, final IAtom toAtom) {
		return addBond(fromAtom, toAtom, IBond.Stereo.NONE, IBond.Order.SINGLE);
	}

	@Override
	public IBond addBond(final IAtom fromAtom, final IAtom toAtom,
			final IBond.Stereo stereo) {
		return addBond(fromAtom, toAtom, stereo, IBond.Order.SINGLE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openscience.cdk.controller.IChemModelRelay#addBond(org.openscience
	 * .cdk.interfaces.IAtom, org.openscience.cdk.interfaces.IAtom, int)
	 */
	@Override
	public IBond addBond(final IAtom fromAtom, final IAtom toAtom,
			final IBond.Stereo stereo, final IBond.Order order) {
		final IBond newBond = chemModel.getBuilder().newInstance(IBond.class,
				fromAtom, toAtom, order, stereo);
		final IAtomContainer fromContainer = ChemModelManipulator
				.getRelevantAtomContainer(chemModel, fromAtom);
		final IAtomContainer toContainer = ChemModelManipulator
				.getRelevantAtomContainer(chemModel, toAtom);

		// we need to check if this merges two atom containers or not
		if (fromContainer != toContainer) {
			fromContainer.add(toContainer);
			chemModel.getMoleculeSet().removeAtomContainer(toContainer);
		}
		fromContainer.addBond(newBond);
		updateAtom(newBond.getAtom(0));
		updateAtom(newBond.getAtom(1));
		structureChanged();
		return newBond;
	}

	public void addChangeModeListener(final IChangeModeListener listener) {
		changeModeListeners.add(listener);
	}

	// OK
	@Override
	public void addFragment(final IAtomContainer toPaste,
			IAtomContainer moleculeToAddTo, final IAtomContainer toRemove) {
		IAtomContainerSet newMoleculeSet = chemModel.getMoleculeSet();
		if (newMoleculeSet == null) {
			newMoleculeSet = chemModel.getBuilder().newInstance(
					IAtomContainerSet.class);
		}
		final IAtomContainerSet oldMoleculeSet = chemModel.getBuilder()
				.newInstance(IAtomContainerSet.class);
		if (moleculeToAddTo == null) {
			newMoleculeSet.addAtomContainer(toPaste);
			moleculeToAddTo = toPaste;
		} else {
			final IAtomContainer mol = chemModel.getBuilder().newInstance(
					IAtomContainer.class);
			for (final IAtom atom : moleculeToAddTo.atoms()) {
				mol.addAtom(atom);
			}
			for (final IBond bond : moleculeToAddTo.bonds()) {
				mol.addBond(bond);
			}
			oldMoleculeSet.addAtomContainer(mol);
			moleculeToAddTo.add(toPaste);
		}
		if (toRemove != null) {
			oldMoleculeSet.addAtomContainer(toRemove);
			moleculeToAddTo.add(toRemove);
			updateAtoms(toRemove, toRemove.atoms());
			newMoleculeSet.removeAtomContainer(toRemove);
		}
		for (final IAtomContainer ac : newMoleculeSet.atomContainers()) {
			updateAtoms(ac, ac.atoms());
		}
		if (undoredofactory != null && undoredohandler != null) {
			final IUndoRedoable undoredo = undoredofactory.getLoadNewModelEdit(
					getIChemModel(), this, oldMoleculeSet, null,
					newMoleculeSet, null, "Add Chain Fragment");
			undoredohandler.postEdit(undoredo);
		}
		chemModel.setMoleculeSet(newMoleculeSet);
		structureChanged();
	}

	// OK
	@Override
	public void addNewBond(final Point2d worldCoordinate,
			final boolean makePseudoAtom) {
		final IAtomContainer undoRedoContainer = getIChemModel().getBuilder()
				.newInstance(IAtomContainer.class);

		// add the first atom in the new bond
		final String atomType = getController2DModel().getDrawElement();
		final IAtom atom = addAtomWithoutUndo(atomType, worldCoordinate,
				makePseudoAtom);
		undoRedoContainer.addAtom(atom);

		// add the second atom to this
		final IAtom newAtom = addAtomWithoutUndo(atomType, atom, makePseudoAtom);
		undoRedoContainer.addAtom(newAtom);

		final IAtomContainer atomContainer = ChemModelManipulator
				.getRelevantAtomContainer(getIChemModel(), newAtom);

		final IBond newBond = atomContainer.getBond(atom, newAtom);
		undoRedoContainer.addBond(newBond);
		updateAtom(newBond.getAtom(0));
		updateAtom(newBond.getAtom(1));

		structureChanged();
		if (undoredofactory != null && undoredohandler != null) {
			final IUndoRedoable undoredo = undoredofactory
					.getAddAtomsAndBondsEdit(getIChemModel(),
							undoRedoContainer, null, "Add Bond", this);
			undoredohandler.postEdit(undoredo);
		}
	}

	@Override
	public void addPhantomAtom(final IAtom atom) {
		phantoms.addAtom(atom);
	}

	@Override
	public void addPhantomBond(final IBond bond) {
		phantoms.addBond(bond);
	}

	// OK
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openscience.cdk.controller.IChemModelRelay#addPhenyl(org.openscience
	 * .cdk.interfaces.IAtom, boolean)
	 */
	@Override
	public IRing addPhenyl(final IAtom atom, final boolean phantom) {
		final IAtomContainer sourceContainer = ChemModelManipulator
				.getRelevantAtomContainer(chemModel, atom);
		final IAtomContainer sharedAtoms = atom.getBuilder().newInstance(
				IAtomContainer.class);
		sharedAtoms.addAtom(atom);

		// make a benzene ring
		final IRing newRing = createAttachRing(sharedAtoms, 6, "C", phantom);
		newRing.getBond(0).setOrder(IBond.Order.DOUBLE);
		newRing.getBond(2).setOrder(IBond.Order.DOUBLE);
		newRing.getBond(4).setOrder(IBond.Order.DOUBLE);

		double bondLength;
		if (sourceContainer.getBondCount() == 0) {
			/*
			 * Special case of adding a ring to a single, unconnected atom -
			 * places the ring centered on the place where the atom was.
			 */
			bondLength = calculateAverageBondLength(chemModel.getMoleculeSet());
			final Point2d ringCenter = new Point2d(atom.getPoint2d());
			ringPlacer.placeRing(newRing, ringCenter, bondLength,
					RingPlacer.jcpAngles);
		} else {
			bondLength = GeometryTools.getBondLengthAverage(sourceContainer);
			final Point2d conAtomsCenter = getConnectedAtomsCenter(sharedAtoms,
					chemModel);

			final Point2d sharedAtomsCenter = atom.getPoint2d();
			final Vector2d ringCenterVector = new Vector2d(sharedAtomsCenter);
			ringCenterVector.sub(conAtomsCenter);

			if (ringCenterVector.x == 0 && ringCenterVector.y == 0) {
				return chemModel.getBuilder().newInstance(IRing.class);
			} else {
				ringPlacer.placeSpiroRing(newRing, sharedAtoms,
						sharedAtomsCenter, ringCenterVector, bondLength);
			}
		}

		// add the ring to the source container/phantoms
		for (final IAtom ringAtom : newRing.atoms()) {
			if (ringAtom != atom) {
				if (phantom) {
					addPhantomAtom(ringAtom);
				} else {
					sourceContainer.addAtom(ringAtom);
				}
			}
		}

		for (final IBond ringBond : newRing.bonds()) {
			if (phantom) {
				addPhantomBond(ringBond);
			} else {
				sourceContainer.addBond(ringBond);
			}
		}
		if (!phantom) {
			updateAtoms(newRing, newRing.atoms());
		}
		for (final IAtom newatom : newRing.atoms()) {
			if (atom != newatom && getClosestAtom(atom) != null) {
				final RendererModel rModel = getRenderer().getRenderer2DModel();
				final double d = rModel.getHighlightDistance()
						/ rModel.getScale();
				atom.getPoint2d().x += d;
			}
		}
		structureChanged();
		return newRing;
	}

	// OK
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openscience.cdk.controller.IChemModelRelay#addPhenyl(org.openscience
	 * .cdk.interfaces.IBond, boolean)
	 */
	@Override
	public IRing addPhenyl(final IBond bond, final boolean phantom) {
		final IAtomContainer sharedAtoms = bond.getBuilder().newInstance(
				IAtomContainer.class);
		final IAtom firstAtom = bond.getAtom(0); // Assumes two-atom bonds only
		final IAtom secondAtom = bond.getAtom(1);
		sharedAtoms.addAtom(firstAtom);
		sharedAtoms.addAtom(secondAtom);
		sharedAtoms.addBond(bond);
		final IAtomContainer sourceContainer = ChemModelManipulator
				.getRelevantAtomContainer(chemModel, firstAtom);

		final Point2d sharedAtomsCenter = GeometryTools
				.get2DCenter(sharedAtoms);

		// calculate two points that are perpendicular to the highlighted bond
		// and have a certain distance from the bond center
		final Point2d firstPoint = firstAtom.getPoint2d();
		final Point2d secondPoint = secondAtom.getPoint2d();
		final Vector2d diff = new Vector2d(secondPoint);
		diff.sub(firstPoint);
		final double bondLength = firstPoint.distance(secondPoint);
		final double angle = GeometryTools.getAngle(diff.x, diff.y);
		final Point2d newPoint1 = new Point2d( // FIXME: what is this point??
				Math.cos(angle + Math.PI / 2) * bondLength / 4
						+ sharedAtomsCenter.x, Math.sin(angle + Math.PI / 2)
						* bondLength / 4 + sharedAtomsCenter.y);
		final Point2d newPoint2 = new Point2d( // FIXME: what is this point??
				Math.cos(angle - Math.PI / 2) * bondLength / 4
						+ sharedAtomsCenter.x, Math.sin(angle - Math.PI / 2)
						* bondLength / 4 + sharedAtomsCenter.y);

		// decide on which side to draw the ring??
		final IAtomContainer connectedAtoms = bond.getBuilder().newInstance(
				IAtomContainer.class);
		for (final IAtom atom : sourceContainer
				.getConnectedAtomsList(firstAtom)) {
			if (atom != secondAtom) {
				connectedAtoms.addAtom(atom);
			}
		}
		for (final IAtom atom : sourceContainer
				.getConnectedAtomsList(secondAtom)) {
			if (atom != firstAtom) {
				connectedAtoms.addAtom(atom);
			}
		}
		final Point2d conAtomsCenter = GeometryTools
				.get2DCenter(connectedAtoms);
		final double distance1 = newPoint1.distance(conAtomsCenter);
		final double distance2 = newPoint2.distance(conAtomsCenter);
		final Vector2d ringCenterVector = new Vector2d(sharedAtomsCenter);
		if (distance1 < distance2) {
			ringCenterVector.sub(newPoint1);
		} else { // distance2 <= distance1
			ringCenterVector.sub(newPoint2);
		}

		// construct a new Ring that contains the highlighted bond an its two
		// atoms
		final IRing newRing = createAttachRing(sharedAtoms, 6, "C", phantom);
		ringPlacer.placeFusedRing(newRing, sharedAtoms, sharedAtomsCenter,
				ringCenterVector, bondLength);
		if (sourceContainer.getMaximumBondOrder(bond.getAtom(0)) == IBond.Order.SINGLE
				&& sourceContainer.getMaximumBondOrder(bond.getAtom(1)) == IBond.Order.SINGLE) {
			newRing.getBond(1).setOrder(IBond.Order.DOUBLE);
			newRing.getBond(3).setOrder(IBond.Order.DOUBLE);
			newRing.getBond(5).setOrder(IBond.Order.DOUBLE);
		} else { // assume Order.DOUBLE, so only need to add 2 double bonds
			newRing.getBond(2).setOrder(IBond.Order.DOUBLE);
			newRing.getBond(4).setOrder(IBond.Order.DOUBLE);
		}
		// add the new atoms and bonds
		for (final IAtom ringAtom : newRing.atoms()) {
			if (ringAtom != firstAtom && ringAtom != secondAtom) {
				if (phantom) {
					addPhantomAtom(ringAtom);
				} else {
					sourceContainer.addAtom(ringAtom);
				}
			}
		}
		for (final IBond ringBond : newRing.bonds()) {
			if (ringBond != bond) {
				if (phantom) {
					addPhantomBond(ringBond);
				} else {
					sourceContainer.addBond(ringBond);
				}
			}
		}
		if (!phantom) {
			updateAtoms(newRing, newRing.atoms());
		}

		final RendererModel rModel = getRenderer().getRenderer2DModel();
		final double d = rModel.getHighlightDistance() / rModel.getScale();
		for (final IAtom atom : newRing.atoms()) {
			if (atom != firstAtom && atom != secondAtom
					&& getClosestAtom(atom) != null) {
				atom.getPoint2d().x += d;
			}
		}
		structureChanged();
		return newRing;
	}

	// OK
	@Override
	public IRing addPhenyl(final Point2d worldcoord, final boolean undoable) {
		final IRing ring = chemModel.getBuilder().newInstance(IRing.class, 6,
				"C");
		ring.getBond(0).setOrder(IBond.Order.DOUBLE);
		ring.getBond(2).setOrder(IBond.Order.DOUBLE);
		ring.getBond(4).setOrder(IBond.Order.DOUBLE);

		final double bondLength = calculateAverageBondLength(chemModel
				.getMoleculeSet());
		ringPlacer
				.placeRing(ring, worldcoord, bondLength, RingPlacer.jcpAngles);
		IAtomContainerSet set = chemModel.getMoleculeSet();

		// the molecule set should not be null, but just in case...
		if (set == null) {
			set = chemModel.getBuilder().newInstance(IAtomContainerSet.class);
			chemModel.setMoleculeSet(set);
		}
		IAtomContainer newAtomContainer = chemModel.getBuilder().newInstance(
				IAtomContainer.class);
		if (chemModel.getMoleculeSet().getAtomContainer(0).getAtomCount() == 0) {
			newAtomContainer = chemModel.getMoleculeSet().getAtomContainer(0);
		} else {
			chemModel.getMoleculeSet().addAtomContainer(newAtomContainer);
		}
		newAtomContainer.add(ring);
		newAtomContainer.add(ring);
		updateAtoms(ring, ring.atoms());
		structureChanged();
		if (undoable && getUndoRedoFactory() != null
				&& getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getAddAtomsAndBondsEdit(
							getIChemModel(),
							ring.getBuilder().newInstance(IAtomContainer.class,
									ring), null, "Benzene", this);
			getUndoRedoHandler().postEdit(undoredo);
		}
		return ring;
	}

	// OK
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openscience.cdk.controller.IChemModelRelay#addRing(org.openscience
	 * .cdk.interfaces.IAtom, int, boolean)
	 */
	@Override
	public IRing addRing(final IAtom atom, final int ringSize,
			final boolean phantom) {
		final IAtomContainer sourceContainer = ChemModelManipulator
				.getRelevantAtomContainer(chemModel, atom);
		final IAtomContainer sharedAtoms = atom.getBuilder().newInstance(
				IAtomContainer.class);
		sharedAtoms.addAtom(atom);

		final IRing newRing = createAttachRing(sharedAtoms, ringSize, "C",
				phantom);
		final double bondLength = GeometryTools
				.getBondLengthAverage(sourceContainer);
		final Point2d conAtomsCenter = getConnectedAtomsCenter(sharedAtoms,
				chemModel);

		final Point2d sharedAtomsCenter = atom.getPoint2d();
		final Vector2d ringCenterVector = new Vector2d(sharedAtomsCenter);
		ringCenterVector.sub(conAtomsCenter);

		if (ringCenterVector.x == 0 && ringCenterVector.y == 0) {
			// Rare bug case:
			// the spiro ring can not be attached, it will lead
			// to NaN values deeper down and serious picture distortion.
			// Instead, return empty ring, let user try otherwise..
			return chemModel.getBuilder().newInstance(IRing.class);
		}
		ringPlacer.placeSpiroRing(newRing, sharedAtoms, sharedAtomsCenter,
				ringCenterVector, bondLength);

		for (final IAtom ringAtom : newRing.atoms()) {
			if (ringAtom != atom) {
				if (phantom) {
					addPhantomAtom(ringAtom);
				} else {
					sourceContainer.addAtom(ringAtom);
				}
			}
		}

		for (final IBond ringBond : newRing.bonds()) {
			if (phantom) {
				addPhantomBond(ringBond);
			} else {
				sourceContainer.addBond(ringBond);
			}
		}
		if (!phantom) {
			updateAtoms(newRing, newRing.atoms());
		}

		final RendererModel rModel = getRenderer().getRenderer2DModel();
		final double d = rModel.getHighlightDistance() / rModel.getScale();
		for (final IAtom newatom : newRing.atoms()) {
			if (atom != newatom && getClosestAtom(atom) != null) {
				atom.getPoint2d().x += d;
			}
		}
		structureChanged();
		return newRing;
	}

	// OK
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openscience.cdk.controller.IChemModelRelay#addRing(org.openscience
	 * .cdk.interfaces.IBond, int, boolean)
	 */
	@Override
	public IRing addRing(final IBond bond, final int size, final boolean phantom) {
		final IAtomContainer sharedAtoms = bond.getBuilder().newInstance(
				IAtomContainer.class);
		final IAtom firstAtom = bond.getAtom(0); // Assumes two-atom bonds only
		final IAtom secondAtom = bond.getAtom(1);
		sharedAtoms.addAtom(firstAtom);
		sharedAtoms.addAtom(secondAtom);
		sharedAtoms.addBond(bond);
		final IAtomContainer sourceContainer = ChemModelManipulator
				.getRelevantAtomContainer(chemModel, firstAtom);

		final Point2d sharedAtomsCenter = GeometryTools
				.get2DCenter(sharedAtoms);

		// calculate two points that are perpendicular to the highlighted bond
		// and have a certain distance from the bond center
		final Point2d firstPoint = firstAtom.getPoint2d();
		final Point2d secondPoint = secondAtom.getPoint2d();
		final Vector2d diff = new Vector2d(secondPoint);
		diff.sub(firstPoint);
		final double bondLength = firstPoint.distance(secondPoint);
		final double angle = GeometryTools.getAngle(diff.x, diff.y);
		final Point2d newPoint1 = new Point2d( // FIXME: what is this point??
				Math.cos(angle + Math.PI / 2) * bondLength / 4
						+ sharedAtomsCenter.x, Math.sin(angle + Math.PI / 2)
						* bondLength / 4 + sharedAtomsCenter.y);
		final Point2d newPoint2 = new Point2d( // FIXME: what is this point??
				Math.cos(angle - Math.PI / 2) * bondLength / 4
						+ sharedAtomsCenter.x, Math.sin(angle - Math.PI / 2)
						* bondLength / 4 + sharedAtomsCenter.y);

		// decide on which side to draw the ring??
		final IAtomContainer connectedAtoms = bond.getBuilder().newInstance(
				IAtomContainer.class);
		for (final IAtom atom : sourceContainer
				.getConnectedAtomsList(firstAtom)) {
			if (atom != secondAtom) {
				connectedAtoms.addAtom(atom);
			}
		}
		for (final IAtom atom : sourceContainer
				.getConnectedAtomsList(secondAtom)) {
			if (atom != firstAtom) {
				connectedAtoms.addAtom(atom);
			}
		}
		final Point2d conAtomsCenter = GeometryTools
				.get2DCenter(connectedAtoms);
		final double distance1 = newPoint1.distance(conAtomsCenter);
		final double distance2 = newPoint2.distance(conAtomsCenter);
		final Vector2d ringCenterVector = new Vector2d(sharedAtomsCenter);
		if (distance1 < distance2) {
			ringCenterVector.sub(newPoint1);
		} else { // distance2 <= distance1
			ringCenterVector.sub(newPoint2);
		}

		// construct a new Ring that contains the highlighted bond an its two
		// atoms
		final IRing newRing = createAttachRing(sharedAtoms, size, "C", phantom);
		ringPlacer.placeFusedRing(newRing, sharedAtoms, sharedAtomsCenter,
				ringCenterVector, bondLength);
		// add the new atoms and bonds
		for (final IAtom ringAtom : newRing.atoms()) {
			if (ringAtom != firstAtom && ringAtom != secondAtom) {
				if (phantom) {
					addPhantomAtom(ringAtom);
				} else {
					sourceContainer.addAtom(ringAtom);
				}
			}
		}
		for (final IBond ringBond : newRing.bonds()) {
			if (ringBond != bond) {
				if (phantom) {
					addPhantomBond(ringBond);
				} else {
					sourceContainer.addBond(ringBond);
				}
			}
		}
		if (!phantom) {
			updateAtoms(newRing, newRing.atoms());
		}

		final RendererModel rModel = getRenderer().getRenderer2DModel();
		final double d = rModel.getHighlightDistance() / rModel.getScale();
		for (final IAtom atom : newRing.atoms()) {
			if (atom != firstAtom && atom != secondAtom
					&& getClosestAtom(atom) != null) {
				atom.getPoint2d().x += d;
			}
		}
		structureChanged();
		return newRing;
	}

	@Override
	public IRing addRing(final int ringSize, final Point2d worldcoord,
			final boolean undoable) {
		final IRing ring = chemModel.getBuilder().newInstance(IRing.class,
				ringSize, "C");
		final double bondLength = calculateAverageBondLength(chemModel
				.getMoleculeSet());
		ringPlacer
				.placeRing(ring, worldcoord, bondLength, RingPlacer.jcpAngles);
		IAtomContainerSet set = chemModel.getMoleculeSet();

		// the molecule set should not be null, but just in case...
		if (set == null) {
			set = chemModel.getBuilder().newInstance(IAtomContainerSet.class);
			chemModel.setMoleculeSet(set);
		}
		IAtomContainer newAtomContainer = chemModel.getBuilder().newInstance(
				IAtomContainer.class);
		if (chemModel.getMoleculeSet().getAtomContainer(0).getAtomCount() == 0) {
			newAtomContainer = chemModel.getMoleculeSet().getAtomContainer(0);
		} else {
			chemModel.getMoleculeSet().addAtomContainer(newAtomContainer);
		}
		newAtomContainer.add(ring);
		updateAtoms(ring, ring.atoms());
		structureChanged();
		if (undoable && getUndoRedoFactory() != null
				&& getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getAddAtomsAndBondsEdit(
							getIChemModel(),
							ring.getBuilder().newInstance(IAtomContainer.class,
									ring), null, "Ring" + " " + ringSize, this);
			getUndoRedoHandler().postEdit(undoredo);
		}
		return ring;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.openscience.cdk.controller.IAtomBondEdits#addSingleElectron(org.
	 * openscience.cdk.interfaces.IAtom)
	 */
	@Override
	public void addSingleElectron(final IAtom atom) {
		final IAtomContainer relevantContainer = ChemModelManipulator
				.getRelevantAtomContainer(chemModel, atom);
		final ISingleElectron singleElectron = atom.getBuilder().newInstance(
				ISingleElectron.class, atom);
		relevantContainer.addSingleElectron(singleElectron);
		updateAtom(atom);
		if (undoredofactory != null && undoredohandler != null) {
			final IUndoRedoable undoredo = undoredofactory
					.getSingleElectronEdit(relevantContainer, singleElectron,
							true, this, atom, "Add Single Electron");
			undoredohandler.postEdit(undoredo);
		}
	}

	@Override
	public void adjustBondOrders() throws IOException, ClassNotFoundException,
			CDKException {
		// TODO also work on reactions ?!?
		final SaturationChecker satChecker = new SaturationChecker();
		final List<IAtomContainer> containersList = ChemModelManipulator
				.getAllAtomContainers(chemModel);
		final Iterator<IAtomContainer> iterator = containersList.iterator();
		final Map<IBond, IBond.Order[]> changedBonds = new HashMap<IBond, IBond.Order[]>();
		while (iterator.hasNext()) {
			final IAtomContainer ac = iterator.next();
			for (final IBond bond : ac.bonds()) {
				final IBond.Order[] orders = new IBond.Order[2];
				orders[1] = bond.getOrder();
				changedBonds.put(bond, orders);
			}
			satChecker.saturate(ac);
			for (final IBond bond : ac.bonds()) {
				final IBond.Order[] orders = changedBonds.get(bond);
				orders[0] = bond.getOrder();
				changedBonds.put(bond, orders);
			}
		}
		if (getController2DModel().getAutoUpdateImplicitHydrogens()) {
			updateImplicitHydrogenCounts();
		}
		if (undoredofactory != null && undoredohandler != null) {
			final IUndoRedoable undoredo = undoredofactory
					.getAdjustBondOrdersEdit(changedBonds,
							new HashMap<IBond, IBond.Stereo[]>(),
							"Adjust Bond Order of Molecules", this);
			undoredohandler.postEdit(undoredo);
		}
	}

	private void adjustRgroup() {
		if (rGroupHandler != null) {
			try {
				rGroupHandler.adjustAtomContainers(chemModel.getMoleculeSet());
			} catch (final CDKException e) {
				unsetRGroupHandler();
				// e.printStackTrace();
			}
		}
	}

	/**
	 * Calculates average bond length. Returns a default value when nothing has
	 * been drawn yet.
	 * 
	 * @param moleculeSet
	 * @return
	 */
	public double calculateAverageBondLength(final IAtomContainerSet moleculeSet) {
		Double averageBondModelLength = 0.0;
		for (final IAtomContainer atomContainer : ChemModelManipulator
				.getAllAtomContainers(chemModel)) {
			averageBondModelLength += GeometryTools
					.getBondLengthAverage(atomContainer);
		}
		if (!averageBondModelLength.isNaN() && averageBondModelLength != 0) {
			return averageBondModelLength
					/ ChemModelManipulator.getAllAtomContainers(chemModel)
							.size();
		} else {
			return 1.5; // some default value for an empty canvas
		}
	}

	// OK
	@Override
	public void changeBond(final IBond bond, final Order order,
			final Stereo stereo) {
		final Map<IBond, IBond.Order[]> changedBonds = new HashMap<IBond, IBond.Order[]>();
		changedBonds.put(bond, new Order[] { order, bond.getOrder() });

		final Map<IBond, IBond.Stereo[]> changedStereo = new HashMap<IBond, IBond.Stereo[]>();
		changedStereo.put(bond, new Stereo[] { stereo, bond.getStereo() });

		bond.setOrder(order);
		bond.setStereo(stereo);

		updateAtom(bond.getAtom(0));
		updateAtom(bond.getAtom(1));
		structurePropertiesChanged();

		if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getAdjustBondOrdersEdit(
							changedBonds,
							changedStereo,
							"Changed Bond Order/Stereo to " + order + "/"
									+ stereo, this);
			getUndoRedoHandler().postEdit(undoredo);
		}
	}

	// OK
	@Override
	public void cleanup() {
		final Map<IAtom, Point2d[]> coords = new HashMap<IAtom, Point2d[]>();
		for (final IAtomContainer container : ChemModelManipulator
				.getAllAtomContainers(chemModel)) {
			for (final IAtom atom : container.atoms()) {
				final Point2d[] coordsforatom = new Point2d[2];
				coordsforatom[1] = atom.getPoint2d();
				coords.put(atom, coordsforatom);
				atom.setPoint2d(null);
			}

			if (ConnectivityChecker.isConnected(container)) {
				generateNewCoordinates(container);
			} else {
				// deal with disconnected atom containers
				final IAtomContainerSet molecules = ConnectivityChecker
						.partitionIntoMolecules(container);
				for (final IAtomContainer subContainer : molecules
						.atomContainers()) {
					generateNewCoordinates(subContainer);
				}
			}

			for (final IAtom atom : container.atoms()) {
				final Point2d[] coordsforatom = coords.get(atom);
				coordsforatom[0] = atom.getPoint2d();
			}
		}
		avoidOverlap(chemModel);
		coordinatesChanged();
		if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getChangeCoordsEdit(coords, "Clean Up");
			getUndoRedoHandler().postEdit(undoredo);
		}
	}

	@Override
	public void clearPhantoms() {
		phantoms.removeAllElements();
	}

	@Override
	public void clearValidation() {
		final Iterator<IAtomContainer> containers = ChemModelManipulator
				.getAllAtomContainers(chemModel).iterator();
		while (containers.hasNext()) {
			final IAtomContainer atoms = containers.next();
			for (int i = 0; i < atoms.getAtomCount(); i++) {
				ProblemMarker.unmark(atoms.getAtom(i));
			}
		}
	}

	// OK
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openscience.cdk.controller.IChemModelRelay#convertToPseudoAtom(org
	 * .openscience.cdk.interfaces.IAtom, java.lang.String)
	 */
	@Override
	public IPseudoAtom convertToPseudoAtom(final IAtom atom, final String label) {
		final IPseudoAtom pseudo = atom.getBuilder().newInstance(
				IPseudoAtom.class, atom);
		pseudo.setLabel(label);
		replaceAtom(pseudo, atom);
		return pseudo;
	}

	private void coordinatesChanged() {
		if (changeHandler != null) {
			changeHandler.coordinatesChanged();
		}
	}

	// OK
	/**
	 * Constructs a new Ring of a certain size that contains all the atoms and
	 * bonds of the given AtomContainer and is filled up with new Atoms and
	 * Bonds.
	 * 
	 * @param sharedAtoms
	 *            The AtomContainer containing the Atoms and bonds for the new
	 *            Ring
	 * @param ringSize
	 *            The size (number of Atoms) the Ring will have
	 * @param symbol
	 *            The element symbol the new atoms will have
	 * @param phantom
	 *            If true we assume this is a phantom ring and do not put it
	 *            into undo.
	 * @return The constructed Ring
	 */
	private IRing createAttachRing(final IAtomContainer sharedAtoms,
			final int ringSize, final String symbol, final boolean phantom) {
		final IRing newRing = sharedAtoms.getBuilder().newInstance(IRing.class,
				ringSize);
		final IAtom[] ringAtoms = new IAtom[ringSize];
		for (int i = 0; i < sharedAtoms.getAtomCount(); i++) {
			ringAtoms[i] = sharedAtoms.getAtom(i);
		}
		for (int i = sharedAtoms.getAtomCount(); i < ringSize; i++) {
			ringAtoms[i] = sharedAtoms.getBuilder().newInstance(IAtom.class,
					symbol);
		}
		for (final IBond bond : sharedAtoms.bonds()) {
			newRing.addBond(bond);
		}
		for (int i = sharedAtoms.getBondCount(); i < ringSize - 1; i++) {
			newRing.addBond(sharedAtoms.getBuilder().newInstance(IBond.class,
					ringAtoms[i], ringAtoms[i + 1], IBond.Order.SINGLE));
		}
		newRing.addBond(sharedAtoms.getBuilder().newInstance(IBond.class,
				ringAtoms[ringSize - 1], ringAtoms[0], IBond.Order.SINGLE));
		newRing.setAtoms(ringAtoms);
		if (!phantom && getUndoRedoFactory() != null
				&& getUndoRedoHandler() != null) {
			final IAtomContainer undoRedoContainer = newRing.getBuilder()
					.newInstance(IAtomContainer.class, newRing);
			for (final IAtom atom : sharedAtoms.atoms()) {
				undoRedoContainer.removeAtom(atom);
			}
			for (final IBond bond : sharedAtoms.bonds()) {
				undoRedoContainer.removeBond(bond);
			}
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getAddAtomsAndBondsEdit(getIChemModel(),
							undoRedoContainer, null, "Ring" + " " + ringSize,
							this);
			getUndoRedoHandler().postEdit(undoredo);
		}
		return newRing;
	}

	// OK
	@Override
	public void cycleBondValence(final IBond bond) {
		cycleBondValence(bond, IBond.Order.SINGLE);
	}

	@Override
	public void cycleBondValence(final IBond bond, final IBond.Order order) {
		final IBond.Order[] orders = new IBond.Order[2];
		final IBond.Stereo[] stereos = new IBond.Stereo[2];
		orders[1] = bond.getOrder();
		stereos[1] = bond.getStereo();
		// special case : reset stereo bonds
		if (bond.getStereo() != IBond.Stereo.NONE) {
			bond.setStereo(IBond.Stereo.NONE);
			bond.setOrder(order);
		} else {
			if (order == IBond.Order.SINGLE) {
				// cycle the bond order up to maxOrder
				final IBond.Order maxOrder = getController2DModel()
						.getMaxOrder();
				if (BondManipulator.isLowerOrder(bond.getOrder(), maxOrder)) {
					BondManipulator.increaseBondOrder(bond);
				} else {
					bond.setOrder(IBond.Order.SINGLE);
				}
			} else {
				if (bond.getOrder() != order) {
					bond.setOrder(order);
				} else {
					bond.setOrder(IBond.Order.SINGLE);
				}
			}
		}
		orders[0] = bond.getOrder();
		stereos[0] = bond.getStereo();
		final Map<IBond, IBond.Order[]> changedBonds = new HashMap<IBond, IBond.Order[]>();
		final Map<IBond, IBond.Stereo[]> changedBondsStereo = new HashMap<IBond, IBond.Stereo[]>();
		changedBonds.put(bond, orders);
		changedBondsStereo.put(bond, stereos);
		// set hybridization from bond order
		bond.getAtom(0).setHybridization(null);
		bond.getAtom(1).setHybridization(null);
		updateAtom(bond.getAtom(0));
		updateAtom(bond.getAtom(1));
		structureChanged();
		if (undoredofactory != null && undoredohandler != null) {
			final IUndoRedoable undoredo = undoredofactory
					.getAdjustBondOrdersEdit(changedBonds, changedBondsStereo,
							"Adjust Bond Order", this);
			undoredohandler.postEdit(undoredo);
		}
	}

	// OK
	@Override
	public IAtomContainer deleteFragment(final IAtomContainer selected) {

		final IAtomContainer removed = selected.getBuilder().newInstance(
				IAtomContainer.class);
		if (rGroupHandler != null
				&& !rGroupHandler.checkRGroupOkayForDelete(selected, this)) {
			return removed;
		}

		for (int i = 0; i < selected.getAtomCount(); i++) {
			final IAtom atom = selected.getAtom(i);
			removed.addAtom(atom);
			final Iterator<IBond> it = ChemModelManipulator
					.getRelevantAtomContainer(chemModel, atom)
					.getConnectedBondsList(atom).iterator();
			final IAtomContainer ac = selected.getBuilder().newInstance(
					IAtomContainer.class);
			while (it.hasNext()) {
				final IBond bond = it.next();
				if (!removed.contains(bond)) {
					removed.addBond(bond);
					ac.addBond(bond);
				}
			}
			ChemModelManipulator.removeAtomAndConnectedElectronContainers(
					chemModel, atom);
			for (final IBond bond : ac.bonds()) {
				if (bond.getAtom(0) == atom) {
					updateAtom(bond.getAtom(1));
				} else {
					updateAtom(bond.getAtom(0));
				}
			}
		}
		removeEmptyContainers(chemModel);
		if (undoredofactory != null && undoredohandler != null) {
			final IUndoRedoable undoredo = undoredofactory
					.getRemoveAtomsAndBondsEdit(chemModel, removed, "Cut", this);
			undoredohandler.postEdit(undoredo);
		}
		adjustRgroup();
		structureChanged();
		return removed;
	}

	private void fireEvents(final Collection<Changed> events) {
		for (final Changed changed : events) {
			switch (changed) {
				case Structure:
					changeHandler.structureChanged();
					break;
				case Properties:
					changeHandler.structurePropertiesChanged();
					break;
				case Coordinates:
					changeHandler.coordinatesChanged();
					break;
				case Selection:
					changeHandler.selectionChanged();
					break;
				case Zoom:
					changeHandler.zoomChanged();
					break;
			}
		}
	}

	@Override
	public void fireStructureChangedEvent() {

		changeHandler.structureChanged();
	}

	@Override
	public void fireZoomEvent() {
		changeHandler.zoomChanged();
	}

	// OK
	@Override
	public void flip(final boolean horizontal) {
		final HashMap<IAtom, Point2d[]> atomCoordsMap = new HashMap<IAtom, Point2d[]>();
		final RendererModel renderModel = renderer.getRenderer2DModel();
		IAtomContainer toflip;
		if (renderModel.getSelection().getConnectedAtomContainer() != null
				&& renderModel.getSelection().getConnectedAtomContainer()
						.getAtomCount() != 0) {
			toflip = renderModel.getSelection().getConnectedAtomContainer();
		} else {
			final List<IAtomContainer> toflipall = ChemModelManipulator
					.getAllAtomContainers(chemModel);
			toflip = toflipall.get(0).getBuilder()
					.newInstance(IAtomContainer.class);
			for (final IAtomContainer atomContainer : toflipall) {
				toflip.add(atomContainer);
			}
		}
		final Point2d center = GeometryTools.get2DCenter(toflip);
		for (int i = 0; i < toflip.getAtomCount(); i++) {
			final IAtom atom = toflip.getAtom(i);
			final Point2d p2d = atom.getPoint2d();
			final Point2d oldCoord = new Point2d(p2d.x, p2d.y);
			if (horizontal) {
				p2d.y = 2.0 * center.y - p2d.y;
			} else {
				p2d.x = 2.0 * center.x - p2d.x;
			}
			final Point2d newCoord = p2d;
			if (!oldCoord.equals(newCoord)) {
				final Point2d[] coords = new Point2d[2];
				coords[0] = newCoord;
				coords[1] = oldCoord;
				atomCoordsMap.put(atom, coords);
			}
		}
		// Stereo bonds must be flipped as well to keep the structure
		for (final IBond bond : toflip.bonds()) {
			if (bond.getStereo() == IBond.Stereo.UP) {
				bond.setStereo(IBond.Stereo.DOWN);
			} else if (bond.getStereo() == IBond.Stereo.DOWN) {
				bond.setStereo(IBond.Stereo.UP);
			} else if (bond.getStereo() == IBond.Stereo.UP_INVERTED) {
				bond.setStereo(IBond.Stereo.DOWN_INVERTED);
			} else if (bond.getStereo() == IBond.Stereo.DOWN_INVERTED) {
				bond.setStereo(IBond.Stereo.UP_INVERTED);
			}
		}
		coordinatesChanged();
		if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getChangeCoordsEdit(atomCoordsMap, "Clean Up");
			getUndoRedoHandler().postEdit(undoredo);
		}
	}

	public IControllerModule getActiveDrawModule() {
		return activeDrawModule;
	}

	// OK
	@Override
	public IAtom getAtomInRange(final Collection<IAtom> toIgnore,
			final IAtom atom) {
		final Point2d atomPosition = atom.getPoint2d();
		final RendererModel rModel = getRenderer().getRenderer2DModel();
		final double highlight = rModel.getHighlightDistance()
				/ rModel.getScale();

		IAtom bestClosestAtom = null;
		double bestDistance = -1;
		for (final IAtomContainer atomContainer : ChemModelManipulator
				.getAllAtomContainers(getIChemModel())) {

			final IAtom closestAtom = GeometryTools.getClosestAtom(
					atomContainer, atom);

			if (closestAtom != null) {
				final double distance = closestAtom.getPoint2d().distance(
						atomPosition);
				if (distance > highlight || toIgnore != null
						&& toIgnore.contains(closestAtom)) {
					continue;
				} else {
					if (bestClosestAtom == null || distance < bestDistance) {
						bestClosestAtom = closestAtom;
						bestDistance = distance;
					}
				}
			}
		}
		return bestClosestAtom;
	}

	@Override
	public IChemModel getChemModel() {
		return chemModel;
	}

	// OK
	@Override
	public IAtom getClosestAtom(final IAtom atom) {
		return getAtomInRange(null, atom);
	}

	// OK
	@Override
	public IAtom getClosestAtom(final Point2d worldCoord) {
		IAtom closestAtom = null;
		double closestDistanceSQ = Double.MAX_VALUE;

		for (final IAtomContainer atomContainer : ChemModelManipulator
				.getAllAtomContainers(chemModel)) {

			for (final IAtom atom : atomContainer.atoms()) {
				if (atom.getPoint2d() != null) {
					final double distanceSQ = atom.getPoint2d()
							.distanceSquared(worldCoord);
					if (distanceSQ < closestDistanceSQ) {
						closestAtom = atom;
						closestDistanceSQ = distanceSQ;
					}
				}
			}
		}

		return closestAtom;
	}

	// OK
	@Override
	public IBond getClosestBond(final Point2d worldCoord) {
		IBond closestBond = null;
		double closestDistanceSQ = Double.MAX_VALUE;

		for (final IAtomContainer atomContainer : ChemModelManipulator
				.getAllAtomContainers(chemModel)) {

			for (final IBond bond : atomContainer.bonds()) {
				boolean hasCenter = true;
				for (final IAtom atom : bond.atoms()) {
					hasCenter = hasCenter && atom.getPoint2d() != null;
				}
				if (hasCenter) {
					final double distanceSQ = bond.get2DCenter()
							.distanceSquared(worldCoord);
					if (distanceSQ < closestDistanceSQ) {
						closestBond = bond;
						closestDistanceSQ = distanceSQ;
					}
				}
			}
		}
		return closestBond;
	}

	// OK
	/**
	 * Searches all the atoms attached to the Atoms in the given AtomContainer
	 * and calculates the center point of them.
	 * 
	 * @param sharedAtoms
	 *            The Atoms the attached partners are searched of
	 * @return The Center Point of all the atoms found
	 */
	private Point2d getConnectedAtomsCenter(final IAtomContainer sharedAtoms,
			final IChemModel chemModel) {
		final IAtomContainer conAtoms = sharedAtoms.getBuilder().newInstance(
				IAtomContainer.class);
		for (final IAtom sharedAtom : sharedAtoms.atoms()) {
			conAtoms.addAtom(sharedAtom);
			final IAtomContainer atomCon = ChemModelManipulator
					.getRelevantAtomContainer(chemModel, sharedAtom);
			for (final IAtom atom : atomCon.getConnectedAtomsList(sharedAtom)) {
				conAtoms.addAtom(atom);
			}
		}
		return GeometryTools.get2DCenter(conAtoms);
	}

	@Override
	public IControllerModel getController2DModel() {
		return controllerModel;
	}

	/**
	 * Tells the mouse cursor shown on the renderPanel.
	 * 
	 * @return One of the constants from java.awt.Cursor.
	 */
	@Override
	public int getCursor() {
		return eventRelay.getCursor().getType();
	}

	/**
	 * Tells the molecular formula of the model. This includes all fragments
	 * currently displayed and all their implicit and explicit Hs.
	 * 
	 * @return The formula.
	 */
	public String getFormula() {
		final IMolecularFormula wholeModel = getIChemModel().getBuilder()
				.newInstance(IMolecularFormula.class);
		final Iterator<IAtomContainer> containers = ChemModelManipulator
				.getAllAtomContainers(chemModel).iterator();
		int implicitHs = 0;
		while (containers.hasNext()) {
			for (final IAtom atom : containers.next().atoms()) {
				wholeModel.addIsotope(atom);
				if (atom.getImplicitHydrogenCount() != null) {
					implicitHs += atom.getImplicitHydrogenCount();
				}
			}
		}
		try {
			if (implicitHs > 0) {
				wholeModel.addIsotope(
						IsotopeFactory.getInstance(wholeModel.getBuilder())
								.getMajorIsotope(1), implicitHs);
			}
		} catch (final IOException e) {
			// do nothing
		}
		return MolecularFormulaManipulator.getHTML(wholeModel, true, false);
	}

	@Override
	public IChemModel getIChemModel() {
		return chemModel;
	}

	@Override
	public Point2d[] getPhantomArrow() {
		return new Point2d[] { phantomArrowStart, phantomArrowEnd };
	}

	@Override
	public IAtomContainer getPhantoms() {
		return phantoms;
	}

	public String getPhantomText() {
		return phantomText;
	}

	public Point2d getPhantomTextPosition() {
		return phantomTextPosition;
	}

	@Override
	public IRenderer getRenderer() {
		return renderer;
	}

	@Override
	public RGroupHandler getRGroupHandler() {
		return rGroupHandler;
	}

	@Override
	public IUndoRedoFactory getUndoRedoFactory() {
		return undoredofactory;
	}

	@Override
	public UndoRedoHandler getUndoRedoHandler() {
		return undoredohandler;
	}

	@Override
	public void invertStereoInSelection() {
		IAtomContainer toflip;
		final RendererModel renderModel = renderer.getRenderer2DModel();
		if (renderModel.getSelection().getConnectedAtomContainer() != null
				&& renderModel.getSelection().getConnectedAtomContainer()
						.getAtomCount() != 0) {
			toflip = renderModel.getSelection().getConnectedAtomContainer();
		} else {
			return;
		}

		for (final IBond bond : toflip.bonds()) {
			if (bond.getStereo() == IBond.Stereo.UP) {
				bond.setStereo(IBond.Stereo.DOWN);
			} else if (bond.getStereo() == IBond.Stereo.DOWN) {
				bond.setStereo(IBond.Stereo.UP);
			} else if (bond.getStereo() == IBond.Stereo.UP_INVERTED) {
				bond.setStereo(IBond.Stereo.DOWN_INVERTED);
			} else if (bond.getStereo() == IBond.Stereo.DOWN_INVERTED) {
				bond.setStereo(IBond.Stereo.UP_INVERTED);
			}
		}
	}

	// OK
	@Override
	public void makeAllExplicitImplicit() {
		final IAtomContainer undoRedoContainer = chemModel.getBuilder()
				.newInstance(IAtomContainer.class);
		final List<IAtomContainer> containers = ChemModelManipulator
				.getAllAtomContainers(chemModel);
		for (int i = 0; i < containers.size(); i++) {
			final IAtomContainer removeatoms = chemModel.getBuilder()
					.newInstance(IAtomContainer.class);
			for (final IAtom atom : containers.get(i).atoms()) {
				if (atom.getSymbol().equals("H")) {
					removeatoms.addAtom(atom);
					removeatoms.addBond(containers.get(i)
							.getConnectedBondsList(atom).get(0));
					containers
							.get(i)
							.getConnectedAtomsList(atom)
							.get(0)
							.setImplicitHydrogenCount(
									containers.get(i)
											.getConnectedAtomsList(atom).get(0)
											.getImplicitHydrogenCount() + 1);
				}
			}
			containers.get(i).remove(removeatoms);
			undoRedoContainer.add(removeatoms);
		}
		if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getRemoveAtomsAndBondsEdit(chemModel, undoRedoContainer,
							"Make explicit Hs implicit", this);
			getUndoRedoHandler().postEdit(undoredo);
		}
		structureChanged();
	}

	// OK
	@Override
	public void makeAllImplicitExplicit() {
		final IAtomContainer undoRedoContainer = chemModel.getBuilder()
				.newInstance(IAtomContainer.class);
		final List<IAtomContainer> containers = ChemModelManipulator
				.getAllAtomContainers(chemModel);
		for (int i = 0; i < containers.size(); i++) {
			for (final IAtom atom : containers.get(i).atoms()) {
				final int hcount = atom.getImplicitHydrogenCount();
				for (int k = 0; k < hcount; k++) {
					final IAtom newAtom = this.addAtomWithoutUndo("H", atom,
							false);
					final IAtomContainer atomContainer = ChemModelManipulator
							.getRelevantAtomContainer(getIChemModel(), newAtom);
					final IBond newBond = atomContainer.getBond(atom, newAtom);
					undoRedoContainer.addAtom(newAtom);
					undoRedoContainer.addBond(newBond);
				}
			}
		}
		if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getAddAtomsAndBondsEdit(chemModel, undoRedoContainer,
							null, "Make implicit Hs explicit", this);
			getUndoRedoHandler().postEdit(undoredo);
		}
		structureChanged();
	}

	// OK
	@Override
	public void makeBondStereo(final IBond bond,
			final Direction desiredDirection) {
		final IBond.Stereo stereo = bond.getStereo();
		final boolean isUp = isUp(stereo);
		final boolean isDown = isDown(stereo);
		final boolean isUndefined = isUndefined(stereo);
		if (isUp && desiredDirection == Direction.UP) {
			flipDirection(bond, stereo);
		} else if (isDown && desiredDirection == Direction.DOWN) {
			flipDirection(bond, stereo);
		} else if (isUndefined && desiredDirection == Direction.UNDEFINED) {
			flipDirection(bond, stereo);
		} else if (desiredDirection == Direction.EZ_UNDEFINED) {
			bond.setStereo(Stereo.E_OR_Z);
		} else if (desiredDirection == Direction.UNDEFINED) {
			bond.setStereo(Stereo.UP_OR_DOWN);
		} else if (desiredDirection == Direction.UP) {
			bond.setStereo(Stereo.UP);
		} else if (desiredDirection == Direction.DOWN) {
			bond.setStereo(Stereo.DOWN);
		}
		final IBond.Stereo[] stereos = new IBond.Stereo[2];
		stereos[1] = stereo;
		stereos[0] = bond.getStereo();
		final Map<IBond, IBond.Order[]> changedBonds = new HashMap<IBond, IBond.Order[]>();
		final Map<IBond, IBond.Stereo[]> changedBondsStereo = new HashMap<IBond, IBond.Stereo[]>();
		changedBondsStereo.put(bond, stereos);
		updateAtom(bond.getAtom(0));
		updateAtom(bond.getAtom(1));
		structureChanged();
		if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getAdjustBondOrdersEdit(changedBonds, changedBondsStereo,
							"Adjust Bond Stereo", this);
			getUndoRedoHandler().postEdit(undoredo);
		}
	}

	// OK
	@Override
	public IBond makeNewStereoBond(final IAtom atom,
			final Direction desiredDirection) {
		final String atomType = getController2DModel().getDrawElement();
		final IAtom newAtom = addAtomWithoutUndo(atomType, atom,
				controllerModel.getDrawPseudoAtom());
		final IAtomContainer undoRedoContainer = getIChemModel().getBuilder()
				.newInstance(IAtomContainer.class);

		// XXX these calls would not be necessary if addAtom returned a bond
		final IAtomContainer atomContainer = ChemModelManipulator
				.getRelevantAtomContainer(getIChemModel(), newAtom);
		final IBond newBond = atomContainer.getBond(atom, newAtom);

		if (desiredDirection == Direction.UP) {
			newBond.setStereo(IBond.Stereo.UP);
		} else if (desiredDirection == Direction.DOWN) {
			newBond.setStereo(IBond.Stereo.DOWN);
		} else if (desiredDirection == Direction.UNDEFINED) {
			newBond.setStereo(IBond.Stereo.UP_OR_DOWN);
		} else {
			newBond.setStereo(IBond.Stereo.E_OR_Z);
		}
		undoRedoContainer.addAtom(newAtom);
		undoRedoContainer.addBond(newBond);
		if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getAddAtomsAndBondsEdit(getIChemModel(),
							undoRedoContainer, null, "Add Stereo Bond", this);
			getUndoRedoHandler().postEdit(undoredo);
		}
		return newBond;
	}

	// OK
	/**
	 * Merge molecules when a selection is moved onto another part of the
	 * molecule set
	 * 
	 */
	@Override
	public void mergeMolecules(final Vector2d movedDistance) {

		final RendererModel model = getRenderer().getRenderer2DModel();
		Iterator<IAtom> it = null;
		if (rGroupHandler != null) {
			if (!rGroupHandler.isMergeAllowed(this)) {
				model.getMerge().clear();
				updateView();
				throw new RuntimeException("Merge not allowed by RGroupHandler");
			}
		}

		// First try to shift the selection to be exactly on top of
		// the target of the merge. This makes the end results visually
		// more attractive and avoid tilted rings
		//
		final Map<IAtom, IAtom> mergeMap = model.getMerge();
		it = model.getMerge().keySet().iterator();
		if (it.hasNext()) {
			final IAtomContainer movedAtomContainer = renderer
					.getRenderer2DModel().getSelection()
					.getConnectedAtomContainer();
			if (movedAtomContainer != null) {
				final IAtom atomA = it.next();
				final IAtom atomB = mergeMap.get(atomA);
				final Vector2d shift = new Vector2d();
				shift.sub(atomB.getPoint2d(), atomA.getPoint2d());

				for (final IAtom shiftAtom : movedAtomContainer.atoms()) {
					shiftAtom.getPoint2d().add(shift);
				}
			}
		}
		final List<IAtom> mergedAtoms = new ArrayList<>();
		final List<IAtomContainer> containers = new ArrayList<IAtomContainer>();
		final List<IAtomContainer> droppedContainers = new ArrayList<IAtomContainer>();

		final List<List<IBond>> removedBondss = new ArrayList<>();
		final List<Map<IBond, Integer>> bondsWithReplacedAtoms = new ArrayList<>();
		final List<IAtom> mergedPartnerAtoms = new ArrayList<>();

		// Done shifting, now the actual merging.
		it = model.getMerge().keySet().iterator();
		while (it.hasNext()) {
			final List<IBond> removedBonds = new ArrayList<IBond>();
			final Map<IBond, Integer> bondsWithReplacedAtom = new HashMap<IBond, Integer>();
			final IAtom mergedAtom = it.next();

			mergedAtoms.add(mergedAtom);
			final IAtom mergedPartnerAtom = model.getMerge().get(mergedAtom);
			mergedPartnerAtoms.add(mergedPartnerAtom);

			final IAtomContainer container1 = ChemModelManipulator
					.getRelevantAtomContainer(chemModel, mergedAtom);
			containers.add(container1);

			final IAtomContainer container2 = ChemModelManipulator
					.getRelevantAtomContainer(chemModel, mergedPartnerAtom);

			// If the atoms are in different atom containers till now, we merge
			// the atom containers first.
			if (container1 != container2) {
				container1.add(container2);
				chemModel.getMoleculeSet().removeAtomContainer(container2);
				droppedContainers.add(container2);
			} else {
				droppedContainers.add(null);
			}

			// Handle the case of a bond between mergedAtom and
			// mergedPartnerAtom.
			// This bond should be removed.
			final IBond rb = container1.getBond(mergedAtom, mergedPartnerAtom);
			if (rb != null) {
				container1.removeBond(rb);
				removedBonds.add(rb);
			}

			// In the next loop we remove bonds that are redundant, that is
			// to say bonds that exist on both sides of the parts to be merged
			// and would cause duplicate bonding in the end result.
			for (final IAtom atom : container1.atoms()) {

				if (!atom.equals(mergedAtom)) {
					if (container1.getBond(mergedAtom, atom) != null) {
						if (model.getMerge().containsKey(atom)) {
							for (final IAtom atom2 : container2.atoms()) {
								if (!atom2.equals(mergedPartnerAtom)) {
									if (container1.getBond(mergedPartnerAtom,
											atom2) != null) {
										if (model.getMerge().get(atom)
												.equals(atom2)) {
											final IBond redundantBond = container1
													.getBond(atom, mergedAtom);
											container1
													.removeBond(redundantBond);
											removedBonds.add(redundantBond);
										}
									}
								}
							}
						}
					}
				}
			}
			removedBondss.add(removedBonds);

			// After the removal of redundant bonds, the actual merge is done.
			// One half of atoms in the merge map are removed and their bonds
			// are mapped to their replacement atoms.
			for (final IBond bond : container1.bonds()) {
				if (bond.contains(mergedAtom)) {
					if (bond.getAtom(0).equals(mergedAtom)) {
						bond.setAtom(mergedPartnerAtom, 0);
						bondsWithReplacedAtom.put(bond, 0);
					} else {
						bond.setAtom(mergedPartnerAtom, 1);
						bondsWithReplacedAtom.put(bond, 1);
					}
				}
			}
			container1.removeAtom(mergedAtom);
			updateAtom(mergedPartnerAtom);
			bondsWithReplacedAtoms.add(bondsWithReplacedAtom);
		}

		Map<Integer, Map<Integer, Integer>> oldRGroupHash = null;
		Map<Integer, Map<Integer, Integer>> newRGroupHash = null;

		if (rGroupHandler != null) {
			try {
				oldRGroupHash = rGroupHandler.makeHash();
				rGroupHandler.adjustAtomContainers(chemModel.getMoleculeSet());
				newRGroupHash = rGroupHandler.makeHash();

			} catch (final CDKException e) {
				unsetRGroupHandler();
				for (final IAtomContainer atc : droppedContainers) {
					atc.setProperty(CDKConstants.TITLE, null);
				}
				e.printStackTrace();
			}
		}

		// Undo section to undo/redo the merge
		final IUndoRedoFactory factory = getUndoRedoFactory();
		final UndoRedoHandler handler = getUndoRedoHandler();
		if (movedDistance != null && factory != null && handler != null) {
			// we look if anything has been moved which was not merged
			final IAtomContainer undoRedoContainer = getIChemModel()
					.getBuilder().newInstance(IAtomContainer.class);

			if (renderer.getRenderer2DModel().getSelection()
					.getConnectedAtomContainer() != null) {
				undoRedoContainer.add(renderer.getRenderer2DModel()
						.getSelection().getConnectedAtomContainer());
			}

			final Iterator<IAtom> it2 = mergeMap.keySet().iterator();
			while (it2.hasNext()) {
				final IAtom remove = it2.next();
				undoRedoContainer.removeAtom(remove);
			}
			final IUndoRedoable moveundoredo = getUndoRedoFactory()
					.getMoveAtomEdit(undoRedoContainer, movedDistance,
							"Move atom");
			final IUndoRedoable undoredo = factory.getMergeMoleculesEdit(
					mergedAtoms, containers, droppedContainers, removedBondss,
					bondsWithReplacedAtoms, movedDistance, mergedPartnerAtoms,
					moveundoredo, oldRGroupHash, newRGroupHash,
					"Move and merge atoms", this);
			handler.postEdit(undoredo);

		}

		model.getMerge().clear();
		structureChanged();
		updateView();
	}

	@Override
	public void mouseClickedDouble(final int screenCoordX,
			final int screenCoordY) {
		final Point2d worldCoord = renderer.toModelCoordinates(screenCoordX,
				screenCoordY);

		// Relay the mouse event to the general handlers
		for (final IControllerModule module : generalModules) {
			module.mouseClickedDouble(worldCoord);
		}

		// Relay the mouse event to the active
		final IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) {
			activeModule.mouseClickedDouble(worldCoord);
		}
	}

	@Override
	public void mouseClickedDown(final int screenX, final int screenY) {
		final Point2d modelCoord = renderer
				.toModelCoordinates(screenX, screenY);

		// Relay the mouse event to the general handlers
		for (final IControllerModule module : generalModules) {
			module.mouseClickedDown(modelCoord);
		}

		// Relay the mouse event to the active
		final IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) {
			activeModule.mouseClickedDown(modelCoord);
		}

		if (getCursor() == Cursor.HAND_CURSOR
				|| getCursor() == Cursor.HAND_CURSOR) {
			setCursor(Cursor.MOVE_CURSOR);
			oldMouseCursor = Cursor.HAND_CURSOR;
		} else {
			oldMouseCursor = Cursor.DEFAULT_CURSOR;

		}
	}

	@Override
	public void mouseClickedDownRight(final int screenX, final int screenY) {
		final Point2d modelCoord = renderer
				.toModelCoordinates(screenX, screenY);

		// Relay the mouse event to the active
		final IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) {
			activeModule.mouseClickedDownRight(modelCoord);
		}
		if (activeModule != null && activeModule.wasEscaped()) {
			setActiveDrawModule(null);
			return;
		}

		// Relay the mouse event to the general handlers
		for (final IControllerModule module : generalModules) {
			module.mouseClickedDownRight(modelCoord);
		}
	}

	@Override
	public void mouseClickedUp(final int screenX, final int screenY) {
		final Point2d modelCoord = renderer
				.toModelCoordinates(screenX, screenY);

		// Relay the mouse event to the general handlers
		for (final IControllerModule module : generalModules) {
			module.mouseClickedUp(modelCoord);
		}

		// Relay the mouse event to the active
		final IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) {
			activeModule.mouseClickedUp(modelCoord);
		}

		setCursor(oldMouseCursor);
	}

	@Override
	public void mouseClickedUpRight(final int screenX, final int screenY) {
		final Point2d modelCoord = renderer
				.toModelCoordinates(screenX, screenY);

		// Relay the mouse event to the general handlers
		for (final IControllerModule module : generalModules) {
			module.mouseClickedUpRight(modelCoord);
		}

		// Relay the mouse event to the active
		final IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) {
			activeModule.mouseClickedUpRight(modelCoord);
		}
	}

	@Override
	public void mouseDrag(final int screenXFrom, final int screenYFrom,
			final int screenXTo, final int screenYTo) {
		final Point2d modelCoordFrom = renderer.toModelCoordinates(screenXFrom,
				screenYFrom);
		final Point2d modelCoordTo = renderer.toModelCoordinates(screenXTo,
				screenYTo);

		// Relay the mouse event to the general handlers
		for (final IControllerModule module : generalModules) {
			module.mouseDrag(modelCoordFrom, modelCoordTo);
		}

		// Relay the mouse event to the active
		final IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) {
			activeModule.mouseDrag(modelCoordFrom, modelCoordTo);
		}
	}

	@Override
	public void mouseEnter(final int screenX, final int screenY) {
		final Point2d worldCoord = renderer
				.toModelCoordinates(screenX, screenY);

		// Relay the mouse event to the general handlers
		for (final IControllerModule module : generalModules) {
			module.mouseEnter(worldCoord);
		}

		// Relay the mouse event to the active
		final IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) {
			activeModule.mouseEnter(worldCoord);
		}
	}

	@Override
	public void mouseExit(final int screenX, final int screenY) {
		final Point2d worldCoord = renderer
				.toModelCoordinates(screenX, screenY);

		// Relay the mouse event to the general handlers
		for (final IControllerModule module : generalModules) {
			module.mouseExit(worldCoord);
		}

		// Relay the mouse event to the active
		final IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) {
			activeModule.mouseExit(worldCoord);
		}
	}

	@Override
	public void mouseMove(final int screenX, final int screenY) {
		final Point2d worldCoord = renderer
				.toModelCoordinates(screenX, screenY);

		// Relay the mouse event to the general handlers
		for (final IControllerModule module : generalModules) {
			module.mouseMove(worldCoord);
		}

		// Relay the mouse event to the active
		final IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) {
			activeModule.mouseMove(worldCoord);
		}
	}

	@Override
	public void mouseWheelMovedBackward(final int clicks) {
		for (final IControllerModule module : generalModules) {
			module.mouseWheelMovedBackward(clicks);
		}
		final IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) {
			activeModule.mouseWheelMovedBackward(clicks);
		}

	}

	@Override
	public void mouseWheelMovedForward(final int clicks) {
		for (final IControllerModule module : generalModules) {
			module.mouseWheelMovedForward(clicks);
		}
		final IControllerModule activeModule = getActiveDrawModule();
		if (activeModule != null) {
			activeModule.mouseWheelMovedForward(clicks);
		}

	}

	// OK
	@Override
	public void moveBy(final Collection<IAtom> atoms, final Vector2d move,
			final Vector2d totalmove) {
		if (totalmove != null && getUndoRedoFactory() != null
				&& getUndoRedoHandler() != null) {
			final IAtomContainer undoRedoContainer = chemModel.getBuilder()
					.newInstance(IAtomContainer.class);
			for (final IAtom atom : atoms) {
				undoRedoContainer.addAtom(atom);
			}
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getMoveAtomEdit(undoRedoContainer, totalmove, "Move atom");
			getUndoRedoHandler().postEdit(undoredo);
		}
		if (move != null) {
			for (final IAtom atom : atoms) {
				final Point2d newpoint = new Point2d(atom.getPoint2d());
				newpoint.add(move);
				moveToWithoutUndo(atom, newpoint);
			}
		}
	}

	// OK
	@Override
	public void moveTo(final IAtom atom, final Point2d worldCoords) {
		if (atom != null) {
			if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
				final IAtomContainer undoRedoContainer = chemModel.getBuilder()
						.newInstance(IAtomContainer.class);
				undoRedoContainer.addAtom(atom);
				final Vector2d end = new Vector2d();
				end.sub(worldCoords, atom.getPoint2d());
				final IUndoRedoable undoredo = getUndoRedoFactory()
						.getMoveAtomEdit(undoRedoContainer, end, "Move atom");
				getUndoRedoHandler().postEdit(undoredo);
			}
			moveToWithoutUndo(atom, worldCoords);
		}
	}

	// OK
	@Override
	public void moveTo(final IBond bond, final Point2d point) {
		if (bond != null) {
			if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
				final IAtomContainer undoRedoContainer = chemModel.getBuilder()
						.newInstance(IAtomContainer.class);
				undoRedoContainer.addAtom(bond.getAtom(0));
				undoRedoContainer.addAtom(bond.getAtom(1));
				final Vector2d end = new Vector2d();
				end.sub(point, bond.getAtom(0).getPoint2d());
				final IUndoRedoable undoredo = getUndoRedoFactory()
						.getMoveAtomEdit(undoRedoContainer, end, "Move atom");
				getUndoRedoHandler().postEdit(undoredo);
			}
			moveToWithoutUndo(bond, point);
		}
	}

	// OK
	@Override
	public void moveToWithoutUndo(final IAtom atom, final Point2d worldCoords) {
		if (atom != null) {
			final Point2d atomCoord = new Point2d(worldCoords);
			atom.setPoint2d(atomCoord);
		}
		coordinatesChanged();
	}

	// OK
	@Override
	public void moveToWithoutUndo(final IBond bond, final Point2d point) {
		if (bond != null) {
			final Point2d center = bond.get2DCenter();
			for (final IAtom atom : bond.atoms()) {
				final Vector2d offset = new Vector2d();
				offset.sub(atom.getPoint2d(), center);
				final Point2d result = new Point2d();
				result.add(point, offset);

				atom.setPoint2d(result);
			}
		}
		coordinatesChanged();
	}

	/**
	 * Adds a general IController2DModule which will catch all mouse events.
	 */
	public void registerGeneralControllerModule(final IControllerModule module) {
		module.setChemModelRelay(this);
		generalModules.add(module);
	}

	// OK TODO this could do with less partitioning
	@Override
	public IAtomContainer removeAtom(final IAtom atom) {
		final IAtomContainer ac = removeAtomWithoutUndo(atom);
		removeEmptyContainers(chemModel);
		if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getRemoveAtomsAndBondsEdit(getIChemModel(), ac,
							"Remove Atom", this);
			getUndoRedoHandler().postEdit(undoredo);
		}
		return ac;
	}

	// OK
	@Override
	public IAtomContainer removeAtomWithoutUndo(final IAtom atom) {

		final IAtomContainer ac = atom.getBuilder().newInstance(
				IAtomContainer.class);
		if (rGroupHandler != null
				&& !rGroupHandler.checkRGroupOkayForDelete(atom, this)) {
			return ac;
		}

		ac.addAtom(atom);
		final Iterator<IBond> connbonds = ChemModelManipulator
				.getRelevantAtomContainer(chemModel, atom)
				.getConnectedBondsList(atom).iterator();
		while (connbonds.hasNext()) {
			final IBond connBond = connbonds.next();
			ac.addBond(connBond);
		}
		ChemModelManipulator.removeAtomAndConnectedElectronContainers(
				chemModel, atom);
		for (final IBond bond : ac.bonds()) {
			if (bond.getAtom(0) == atom) {
				updateAtom(bond.getAtom(1));
			} else {
				updateAtom(bond.getAtom(0));
			}
		}
		structureChanged();
		adjustRgroup();
		return ac;
	}

	// OK TODO this could do with less partitioning
	@Override
	public void removeBond(final IBond bond) {
		removeBondWithoutUndo(bond);
		final IAtomContainer undAtomContainer = bond.getBuilder().newInstance(
				IAtomContainer.class);
		undAtomContainer.addBond(bond);
		if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getRemoveAtomsAndBondsEdit(getIChemModel(),
							undAtomContainer, "Remove Bond", this);
			getUndoRedoHandler().postEdit(undoredo);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openscience.cdk.controller.IChemModelRelay#removeBondAndLoneAtoms
	 * (org.openscience.cdk.interfaces.IBond)
	 */
	@Override
	public void removeBondAndLoneAtoms(final IBond bondToRemove) {

		final IAtomContainer container = ChemModelManipulator
				.getRelevantAtomContainer(chemModel, bondToRemove.getAtom(0));
		final IAtomContainer undoRedoContainer = chemModel.getBuilder()
				.newInstance(IAtomContainer.class);
		undoRedoContainer.addBond(bondToRemove);

		removeBondWithoutUndo(bondToRemove);

		if (container != null) {
			for (int i = 0; i < 2; i++) {
				if (container.getConnectedAtomsCount(bondToRemove.getAtom(i)) == 0) {
					removeAtomWithoutUndo(bondToRemove.getAtom(i));
					undoRedoContainer.addAtom(bondToRemove.getAtom(i));
				}
			}
		}
		removeEmptyContainers(chemModel);
		final IUndoRedoable undoredo = getUndoRedoFactory()
				.getRemoveAtomsAndBondsEdit(chemModel, undoRedoContainer,
						"Delete Bond", this);
		getUndoRedoHandler().postEdit(undoredo);

		if (rGroupHandler != null
				&& !rGroupHandler.checkRGroupOkayForDelete(undoRedoContainer,
						this)) {
			undoredo.undo();
			return;
		}

	}

	// OK
	@Override
	public void removeBondWithoutUndo(final IBond bond) {
		ChemModelManipulator.removeElectronContainer(chemModel, bond);
		// set hybridization from bond order
		bond.getAtom(0).setHybridization(null);
		bond.getAtom(1).setHybridization(null);
		updateAtom(bond.getAtom(0));
		updateAtom(bond.getAtom(1));
		adjustRgroup();
		structureChanged();
	}

	public void removeChangeModeListener(final IChangeModeListener listener) {
		changeModeListeners.remove(listener);
	}

	// OK
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openscience.cdk.controller.IAtomBondEdits#removeSingleElectron(org
	 * .openscience.cdk.interfaces.IAtom)
	 */
	@Override
	public void removeSingleElectron(final IAtom atom) {
		final IAtomContainer relevantContainer = ChemModelManipulator
				.getRelevantAtomContainer(chemModel, atom);
		if (relevantContainer.getConnectedSingleElectronsCount(atom) > 0) {
			final ISingleElectron removedElectron = relevantContainer
					.removeSingleElectron(relevantContainer
							.getConnectedSingleElectronsCount(atom) - 1);
			updateAtom(atom);
			if (undoredofactory != null && undoredohandler != null) {
				final IUndoRedoable undoredo = undoredofactory
						.getSingleElectronEdit(relevantContainer,
								removedElectron, false, this, atom,
								"Remove Single Electron");
				undoredohandler.postEdit(undoredo);
			}
		}
	}

	// OK
	@Override
	public void replaceAtom(final IAtom atomnew, final IAtom atomold) {
		final IAtomContainer relevantContainer = ChemModelManipulator
				.getRelevantAtomContainer(chemModel, atomold);
		AtomContainerManipulator.replaceAtomByAtom(relevantContainer, atomold,
				atomnew);
		updateAtom(atomnew);
		structureChanged();
		if (undoredofactory != null && undoredohandler != null) {
			final IUndoRedoable undoredo = undoredofactory.getReplaceAtomEdit(
					chemModel, atomold, atomnew, "Replace Atom");
			undoredohandler.postEdit(undoredo);
		}
	}

	// OK
	@Override
	public void resetBondOrders() {
		final List<IAtomContainer> containersList = ChemModelManipulator
				.getAllAtomContainers(chemModel);
		final Iterator<IAtomContainer> iterator = containersList.iterator();
		final Map<IBond, IBond.Order[]> changedBonds = new HashMap<IBond, IBond.Order[]>();
		while (iterator.hasNext()) {
			final IAtomContainer ac = iterator.next();
			for (final IBond bond : ac.bonds()) {
				final IBond.Order[] orders = new IBond.Order[2];
				orders[1] = bond.getOrder();
				orders[0] = Order.SINGLE;
				changedBonds.put(bond, orders);
				bond.setOrder(Order.SINGLE);
			}
		}
		if (getController2DModel().getAutoUpdateImplicitHydrogens()) {
			updateImplicitHydrogenCounts();
		}
		if (undoredofactory != null && undoredohandler != null) {
			final IUndoRedoable undoredo = undoredofactory
					.getAdjustBondOrdersEdit(changedBonds,
							new HashMap<IBond, IBond.Stereo[]>(),
							"Reset Bond Order of Molecules", this);
			undoredohandler.postEdit(undoredo);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openscience.cdk.controller.IChemModelRelay#select(org.openscience
	 * .cdk.renderer.selection.IChemObjectSelection)
	 */
	@Override
	public void select(final IChemObjectSelection selection) {
		getRenderer().getRenderer2DModel().setSelection(selection);
		selectionChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openscience.cdk.controller.IChemModelRelay#select(org.openscience
	 * .cdk.renderer.selection.IncrementalSelection)
	 */
	@Override
	public void select(final IncrementalSelection selection) {
		if (selection != null) {
			selection.select(chemModel);
		}
		selectionChanged();
	}

	private void selectionChanged() {
		if (changeHandler != null) {
			changeHandler.selectionChanged();
		}
	}

	@Override
	public void setActiveDrawModule(IControllerModule activeDrawModule) {
		if (activeDrawModule == null) {
			activeDrawModule = fallbackModule;
		}
		this.activeDrawModule = activeDrawModule;
		for (int i = 0; i < changeModeListeners.size(); i++) {
			changeModeListeners.get(i).modeChanged(this.activeDrawModule);
		}
	}

	// OK
	@Override
	public void setCharge(final IAtom atom, final int charge) {
		if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getChangeChargeEdit(atom, atom.getFormalCharge(), charge,
							"Change charge to " + charge, this);
			getUndoRedoHandler().postEdit(undoredo);
		}
		atom.setFormalCharge(charge);
		updateAtom(atom);
		structurePropertiesChanged();
	}

	@Override
	public void setChemModel(final IChemModel model) {
		chemModel = model;
		structureChanged();
	}

	/**
	 * Sets the mouse cursor shown on the renderPanel.
	 * 
	 * @param cursor
	 *            One of the constants from java.awt.Cursor.
	 */
	@Override
	public void setCursor(final int cursor) {
		eventRelay.setCursor(new Cursor(cursor));
	}

	@Override
	public void setEventHandler(final IChemModelEventRelayHandler handler) {
		changeHandler = handler;
	}

	public void setFallbackModule(final IControllerModule m) {
		fallbackModule = m;
	}

	// OK
	@Override
	public void setImplicitHydrogenCount(final IAtom atom, final int intValue) {
		if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
			final HashMap<IAtom, Integer[]> atomhydrogenmap = new HashMap<IAtom, Integer[]>();
			atomhydrogenmap
					.put(atom,
							new Integer[] { intValue,
									atom.getImplicitHydrogenCount() });
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getChangeHydrogenCountEdit(atomhydrogenmap,
							"Change hydrogen count to " + intValue);
			getUndoRedoHandler().postEdit(undoredo);
		}
		atom.setImplicitHydrogenCount(intValue);
		structureChanged();
	}

	// OK
	@Override
	public void setMassNumber(final IAtom atom, final int massNumber) {
		if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getChangeIsotopeEdit(atom, atom.getMassNumber(),
							massNumber, "Change Atomic Mass to " + massNumber);
			getUndoRedoHandler().postEdit(undoredo);
		}
		atom.setMassNumber(massNumber);
		structurePropertiesChanged();
	}

	@Override
	public void setPhantomArrow(final Point2d start, final Point2d end) {
		phantomArrowStart = start;
		phantomArrowEnd = end;
	}

	@Override
	public void setPhantoms(final IAtomContainer phantoms) {
		this.phantoms = phantoms;

	}

	@Override
	public void setPhantomText(final String text, final Point2d position) {
		phantomText = text;
		phantomTextPosition = position;
	}

	/**
	 * See unsetRGroupHandler() to nullify the R-group aspects.
	 */
	@Override
	public void setRGroupHandler(final RGroupHandler rGroupHandler) {
		ControllerHub.rGroupHandler = rGroupHandler;
		if (rGroupHandler != null) {
			for (final IGenerator generator : renderer.getGenerators()) {
				if (generator instanceof RGroupGenerator) {
					((RGroupGenerator) generator).setRGroupQuery(rGroupHandler
							.getrGroupQuery());
				}
			}
		}
	}

	/**
	 * Change the Atom Symbol to the given element symbol, setting also its
	 * massNumber. If an exception happens, the massNumber is set to null.
	 * 
	 * @see org.openscience.jchempaint.controller.IAtomBondEdits#setSymbol(org.openscience.cdk.interfaces.IAtom,
	 *      java.lang.String)
	 */
	@Override
	public void setSymbol(IAtom atom, final String symbol) {
		if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getChangeAtomSymbolEdit(atom, atom.getSymbol(), symbol,
							"Change Atom Symbol to " + symbol, this);
			getUndoRedoHandler().postEdit(undoredo);
		}
		if (atom instanceof IPseudoAtom) {
			final IAtom newAtom = atom.getBuilder().newInstance(IAtom.class,
					symbol, atom.getPoint2d());
			replaceAtom(newAtom, atom);
			atom = newAtom;
		} else {
			atom.setSymbol(symbol);
		}
		// configure the atom, so that the atomic number matches the symbol
		try {
			atom.setMassNumber(null);
			IsotopeFactory.getInstance(atom.getBuilder()).configure(atom);
		} catch (final Exception exception) {
			atom.setMassNumber(null);
			exception.printStackTrace();
		}
		updateAtom(atom);
		structurePropertiesChanged();
	}

	// OK
	@Override
	public void setValence(final IAtom atom, final Integer newValence) {
		if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getChangeValenceEdit(atom, atom.getValency(), newValence,
							"Change valence to " + newValence, this);
			getUndoRedoHandler().postEdit(undoredo);
		}
		if (!(atom instanceof IPseudoAtom)) {
			atom.setValency(newValence);
		}
		updateAtom(atom);
		structurePropertiesChanged();
	}

	protected void structureChanged() {
		if (renderer.getRenderer2DModel().getSelection() instanceof IncrementalSelection) {
			select((IncrementalSelection) renderer.getRenderer2DModel()
					.getSelection());
		}
		if (changeHandler != null) {
			changeHandler.structureChanged();
		}

		final RendererModel renderModel = renderer.getRenderer2DModel();
		renderModel.setRecalculationRequiredForSSSR(true);

	}

	private void structurePropertiesChanged() {
		if (changeHandler != null) {
			changeHandler.structurePropertiesChanged();
		}
	}

	/**
	 * Unregister all general IController2DModules.
	 */
	public void unRegisterAllControllerModule() {
		generalModules.clear();
	}

	@Override
	public void unsetRGroupHandler() {
		ControllerHub.rGroupHandler = null;
		for (final IGenerator generator : getRenderer().getGenerators()) {
			if (generator instanceof RGroupGenerator) {
				((RGroupGenerator) generator).setRGroupQuery(null);
			}
		}
		if (chemModel.getMoleculeSet() != null) {
			for (final IAtomContainer atc : chemModel.getMoleculeSet()
					.atomContainers()) {
				atc.removeProperty(CDKConstants.TITLE);
			}
		}
	}

	// OK
	/**
	 * Updates an atom with respect to its hydrogen count
	 * 
	 * @param container
	 *            The AtomContainer to work on
	 * @param atom
	 *            The Atom to update
	 */
	@Override
	public void updateAtom(final IAtom atom) {
		final IAtomContainer container = ChemModelManipulator
				.getRelevantAtomContainer(chemModel, atom);
		if (container != null) {
			updateAtom(container, atom);
		}
	}

	// OK
	/**
	 * Updates an atom with respect to its hydrogen count
	 * 
	 * @param container
	 *            The AtomContainer to work on
	 * @param atom
	 *            The Atom to update
	 */
	private void updateAtom(final IAtomContainer container, final IAtom atom) {
		if (getController2DModel().getAutoUpdateImplicitHydrogens()) {
			atom.setImplicitHydrogenCount(0);
			try {
				final IAtomType type = matcher.findMatchingAtomType(container,
						atom);
				if (type != null) {
					final Integer neighbourCount = type
							.getFormalNeighbourCount();
					if (neighbourCount != null) {
						atom.setImplicitHydrogenCount(neighbourCount
								- container.getConnectedAtomsCount(atom));
					}
					// for some reason, the neighbour count takes into account
					// only
					// one single electron
					if (container.getConnectedSingleElectronsCount(atom) > 1
							&& atom.getImplicitHydrogenCount()
									- container
											.getConnectedSingleElectronsCount(atom)
									+ 1 > -1) {
						atom.setImplicitHydrogenCount(atom
								.getImplicitHydrogenCount()
								- container
										.getConnectedSingleElectronsCount(atom)
								+ 1);
					}
					atom.setFlag(CDKConstants.IS_TYPEABLE, false);
				} else {
					atom.setFlag(CDKConstants.IS_TYPEABLE, true);
				}
			} catch (final CDKException e) {
				e.printStackTrace();
			}
		}
	}

	// OK
	/**
	 * Updates an array of atoms with respect to its hydrogen count
	 * 
	 * @param container
	 *            The AtomContainer to work on
	 * @param atoms
	 *            The Atoms to update
	 */
	@Override
	public void updateAtoms(final IAtomContainer container,
			final Iterable<IAtom> atoms) {
		for (final IAtom atom : atoms) {
			updateAtom(container, atom);
		}
	}

	// OK
	@Override
	public void updateImplicitHydrogenCounts() {
		final Map<IAtom, Integer[]> atomHydrogenCountsMap = new HashMap<IAtom, Integer[]>();
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
							atomHydrogenCountsMap.put(
									atom,
									new Integer[] {
											type.getFormalNeighbourCount()
													- connectedAtomCount,
											atom.getImplicitHydrogenCount() });
							atom.setImplicitHydrogenCount(type
									.getFormalNeighbourCount()
									- connectedAtomCount);
						}
					} catch (final CDKException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getChangeHydrogenCountEdit(atomHydrogenCountsMap,
							"Update implicit hydrogen count");
			getUndoRedoHandler().postEdit(undoredo);
		}
		structurePropertiesChanged();
	}

	@Override
	public void updateView() {
		// call the eventRelay method here to update the view..
		eventRelay.updateView();
	}

	@Override
	public void zap() {
		if (getUndoRedoFactory() != null && getUndoRedoHandler() != null) {
			final IUndoRedoable undoredo = getUndoRedoFactory()
					.getClearAllEdit(chemModel, chemModel.getMoleculeSet(),
							chemModel.getReactionSet(), "Clear Panel");
			getUndoRedoHandler().postEdit(undoredo);
		}
		if (chemModel.getMoleculeSet() != null) {
			final IAtomContainerSet molSet = chemModel.getBuilder()
					.newInstance(IAtomContainerSet.class);
			final IAtomContainer ac = chemModel.getBuilder().newInstance(
					IAtomContainer.class);
			molSet.addAtomContainer(ac);
			chemModel.setMoleculeSet(molSet);

		}
		if (chemModel.getReactionSet() != null) {
			chemModel.setReactionSet(chemModel.getBuilder().newInstance(
					IReactionSet.class));
		}
		structureChanged();
	}
}
