/* $RCSfile$
 * $Author: gilleain $
 * $Date: 2008-11-26 16:01:05 +0000 (Wed, 26 Nov 2008) $
 * $Revision: 13311 $
 *
 * Copyright (C) 2005-2008 Tobias Helmus, Stefan Kuhn
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
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
package org.openscience.jchempaint.controller.undoredo;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.tools.manipulator.ReactionSetManipulator;

/**
 * @cdk.module controlextra
 * @cdk.svnrev $Revision: 13311 $
 */
public class MakeReactantOrProductInNewReactionEdit implements IUndoRedoable {

	private static final long		serialVersionUID	= -7667903450980188402L;

	private final IAtomContainer	movedContainer;

	private final IAtomContainer	oldContainer;

	private final String			type;

	private final IChemModel		chemModel;

	private final String			reactionID;

	private final boolean			reactantOrProduct;

	/**
	 * @param chemModel
	 * @param undoRedoContainer
	 * @param c2dm
	 *            The controller model; if none, set to null
	 */
	public MakeReactantOrProductInNewReactionEdit(final IChemModel chemModel,
			final IAtomContainer ac, final IAtomContainer oldcontainer,
			final boolean reactantOrProduct, final String type) {
		this.type = type;
		movedContainer = ac;
		oldContainer = oldcontainer;
		this.chemModel = chemModel;
		reactionID = ReactionSetManipulator.getReactionByAtomContainerID(
				chemModel.getReactionSet(), movedContainer.getID()).getID();
		this.reactantOrProduct = reactantOrProduct;
	}

	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.undo.UndoableEdit#getPresentationName()
	 */
	public String getPresentationName() {
		return type;
	}

	@Override
	public void redo() {
		chemModel.getMoleculeSet().removeAtomContainer(movedContainer);
		final IReaction reaction = chemModel.getBuilder().newInstance(
				IReaction.class);
		reaction.setID(reactionID);
		final IAtomContainer mol = chemModel.getBuilder().newInstance(
				IAtomContainer.class, movedContainer);
		mol.setID(movedContainer.getID());
		if (reactantOrProduct) {
			reaction.addReactant(mol);
		} else {
			reaction.addProduct(mol);
		}
		if (chemModel.getReactionSet() == null) {
			chemModel.setReactionSet(chemModel.getBuilder().newInstance(
					IReactionSet.class));
		}
		chemModel.getReactionSet().addReaction(reaction);
		chemModel.getMoleculeSet().removeAtomContainer(oldContainer);
	}

	@Override
	public void undo() {
		if (chemModel.getMoleculeSet() == null) {
			chemModel.setMoleculeSet(chemModel.getBuilder().newInstance(
					IAtomContainerSet.class));
		}
		chemModel.getMoleculeSet().addAtomContainer(oldContainer);
		chemModel.getReactionSet().removeReaction(
				ReactionSetManipulator.getReactionByAtomContainerID(
						chemModel.getReactionSet(), movedContainer.getID()));
	}
}
