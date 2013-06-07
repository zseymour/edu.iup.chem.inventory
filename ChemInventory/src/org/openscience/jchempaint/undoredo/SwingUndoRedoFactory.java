/*
 *  $RCSfile$
 *  $Author: egonw $
 *  $Date: 2007-01-04 17:26:00 +0000 (Thu, 04 Jan 2007) $
 *  $Revision: 7634 $
 *
 *  Copyright (C) 2008 Stefan Kuhn
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
package org.openscience.jchempaint.undoredo;

import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IElectronContainer;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.isomorphism.matchers.RGroup;
import org.openscience.cdk.isomorphism.matchers.RGroupList;
import org.openscience.jchempaint.controller.IChemModelRelay;
import org.openscience.jchempaint.controller.undoredo.IUndoRedoFactory;
import org.openscience.jchempaint.controller.undoredo.IUndoRedoable;
import org.openscience.jchempaint.rgroups.RGroupHandler;

/**
 * A class returning Swing-Implementations of all the undo-redo edits
 * 
 */
public class SwingUndoRedoFactory implements IUndoRedoFactory {

	@Override
	public IUndoRedoable getAddAtomsAndBondsEdit(final IChemModel chemModel,
			final IAtomContainer undoRedoContainer,
			final IAtomContainer removedAtomContainer, final String type,
			final IChemModelRelay c2dm) {
		return new SwingAddAtomsAndBondsEdit(chemModel, undoRedoContainer,
				removedAtomContainer, type, c2dm);
	}

	@Override
	public IUndoRedoable getAdjustBondOrdersEdit(
			final Map<IBond, IBond.Order[]> changedBonds,
			final Map<IBond, IBond.Stereo[]> changedBondsStereo,
			final String type, final IChemModelRelay chemModelRelay) {
		return new SwingAdjustBondOrdersEdit(changedBonds, changedBondsStereo,
				type, chemModelRelay);
	}

	@Override
	public IUndoRedoable getChangeAtomSymbolEdit(final IAtom atom,
			final String formerSymbol, final String symbol, final String type,
			final IChemModelRelay chemModelRelay) {
		return new SwingChangeAtomSymbolEdit(atom, formerSymbol, symbol, type,
				chemModelRelay);
	}

	@Override
	public IUndoRedoable getChangeChargeEdit(final IAtom atomInRange,
			final int formerCharge, final int newCharge, final String type,
			final IChemModelRelay chemModelRelay) {
		return new SwingChangeChargeEdit(atomInRange, formerCharge, newCharge,
				type, chemModelRelay);
	}

	@Override
	public IUndoRedoable getChangeCoordsEdit(
			final Map<IAtom, Point2d[]> atomCoordsMap, final String type) {
		return new SwingChangeCoordsEdit(atomCoordsMap, type);
	}

	@Override
	public IUndoRedoable getChangeHydrogenCountEdit(
			final Map<IAtom, Integer[]> atomHydrogenCountsMap, final String type) {
		return new SwingChangeHydrogenCountEdit(atomHydrogenCountsMap, type);
	}

	@Override
	public IUndoRedoable getChangeIsotopeEdit(final IAtom atom,
			final Integer formerIsotopeNumber, final Integer newIstopeNumber,
			final String type) {
		return new SwingChangeIsotopeEdit(atom, formerIsotopeNumber,
				newIstopeNumber, type);
	}

	@Override
	public IUndoRedoable getChangeValenceEdit(final IAtom atomInRange,
			final Integer formerValence, final Integer valence,
			final String text, final IChemModelRelay chemModelRelay) {
		return new SwingChangeValenceEdit(atomInRange, formerValence, valence,
				text, chemModelRelay);
	}

	@Override
	public IUndoRedoable getClearAllEdit(final IChemModel chemModel,
			final IAtomContainerSet som, final IReactionSet sor,
			final String type) {
		return new SwingClearAllEdit(chemModel, som, sor, type);
	}

	@Override
	public IUndoRedoable getLoadNewModelEdit(final IChemModel chemModel,
			final IChemModelRelay relay, final IAtomContainerSet oldsom,
			final IReactionSet oldsor, final IAtomContainerSet newsom,
			final IReactionSet newsor, final String type) {
		return new SwingLoadNewModelEdit(chemModel, relay, oldsom, oldsor,
				newsom, newsor, type);
	}

	@Override
	public IUndoRedoable getMakeReactantOrProductInExistingReactionEdit(
			final IChemModel chemModel, final IAtomContainer newContainer,
			final IAtomContainer oldcontainer, final String s,
			final boolean reactantOrProduct, final String type) {
		return new SwingMakeReactantInExistingReactionEdit(chemModel,
				newContainer, oldcontainer, s, reactantOrProduct, type);
	}

	@Override
	public IUndoRedoable getMakeReactantOrProductInNewReactionEdit(
			final IChemModel chemModel, final IAtomContainer ac,
			final IAtomContainer oldcontainer, final boolean reactantOrProduct,
			final String type) {
		return new SwingMakeReactantOrProductInNewReactionEdit(chemModel, ac,
				oldcontainer, reactantOrProduct, type);
	}

	public IUndoRedoable getMergeMoleculesEdit(final List<IAtom> deletedAtom,
			final List<IAtomContainer> containers,
			final List<IAtomContainer> droppedContainers,
			final List<List<IBond>> deletedBonds,
			final List<Map<IBond, Integer>> bondsWithReplacedAtom,
			final Vector2d offset, final List<IAtom> atomwhichwasmoved,
			final IUndoRedoable moveundoredo,
			final Map<Integer, Map<Integer, Integer>> oldRgrpHash,
			final Map<Integer, Map<Integer, Integer>> newRgrpHash,
			final String type, final IChemModelRelay c2dm) {
		return new SwingMergeMoleculesEdit(deletedAtom, containers,
				droppedContainers, deletedBonds, bondsWithReplacedAtom, offset,
				atomwhichwasmoved, moveundoredo, oldRgrpHash, newRgrpHash,
				type, c2dm);
	}

	@Override
	public IUndoRedoable getMoveAtomEdit(
			final IAtomContainer undoRedoContainer, final Vector2d offset,
			final String type) {
		return new SwingMoveAtomEdit(undoRedoContainer, offset, type);
	}

	@Override
	public IUndoRedoable getRemoveAtomsAndBondsEdit(final IChemModel chemModel,
			final IAtomContainer undoRedoContainer, final String type,
			final IChemModelRelay chemModelRelay) {
		return new SwingRemoveAtomsAndBondsEdit(chemModel, undoRedoContainer,
				type, chemModelRelay);
	}

	@Override
	public IUndoRedoable getReplaceAtomEdit(final IChemModel chemModel,
			final IAtom oldAtom, final IAtom newAtom, final String type) {
		return new SwingReplaceAtomEdit(chemModel, oldAtom, newAtom, type);
	}

	@Override
	public IUndoRedoable getRGroupEdit(final String type,
			final boolean isNewRGroup, final IChemModelRelay hub,
			final RGroupHandler rgrpHandler,
			final Map<IAtom, IAtomContainer> existingAtomDistr,
			final Map<IBond, IAtomContainer> existingBondDistr,
			final IAtomContainer existingRoot,
			final Map<IAtom, Map<Integer, IBond>> existingRootAttachmentPoints,
			final Map<RGroup, Map<Integer, IAtom>> existingRGroupApo,
			final Map<Integer, RGroupList> rgroupLists,
			final IAtomContainer userSelection) {
		return new SwingRGroupEdit(type, isNewRGroup, hub, rgrpHandler,
				existingAtomDistr, existingBondDistr, existingRoot,
				existingRootAttachmentPoints, existingRGroupApo, rgroupLists,
				userSelection);

	}

	@Override
	public IUndoRedoable getSingleElectronEdit(
			final IAtomContainer relevantContainer,
			final IElectronContainer electronContainer,
			final boolean addSingleElectron,
			final IChemModelRelay chemModelRelay, final IAtom atom,
			final String type) {
		return new SwingConvertToRadicalEdit(relevantContainer,
				electronContainer, addSingleElectron, chemModelRelay, atom,
				type);
	}
}
