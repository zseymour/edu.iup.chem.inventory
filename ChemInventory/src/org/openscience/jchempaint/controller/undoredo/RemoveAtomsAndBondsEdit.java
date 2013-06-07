/* $RCSfile$
 * $Author: egonw $
 * $Date: 2008-05-12 07:29:49 +0100 (Mon, 12 May 2008) $
 * $Revision: 10979 $
 *
 * Copyright (C) 2005-2007  The Chemistry Development Kit (CDK) project
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

import java.util.Iterator;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;
import org.openscience.jchempaint.controller.IChemModelRelay;

/**
 * @cdk.module controlbasic
 * @cdk.svnrev $Revision: 10979 $
 */
public class RemoveAtomsAndBondsEdit implements IUndoRedoable {

	private static final long		serialVersionUID	= -143712173063846054L;

	private final String			type;

	private final IAtomContainer	undoRedoContainer;

	private final IChemModel		chemModel;

	private final IAtomContainer	container;

	private IChemModelRelay			chemModelRelay		= null;

	public RemoveAtomsAndBondsEdit(final IChemModel chemModel,
			final IAtomContainer undoRedoContainer, final String type,
			final IChemModelRelay chemModelRelay) {
		this.chemModel = chemModel;
		this.undoRedoContainer = undoRedoContainer;
		container = chemModel.getBuilder().newInstance(IAtomContainer.class);
		final Iterator<IAtomContainer> containers = ChemModelManipulator
				.getAllAtomContainers(chemModel).iterator();
		while (containers.hasNext()) {
			container.add(containers.next());
		}
		this.type = type;
		this.chemModelRelay = chemModelRelay;
	}

	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	public String getPresentationName() {
		return type;
	}

	@Override
	public void redo() {
		for (int i = 0; i < undoRedoContainer.getBondCount(); i++) {
			final IBond bond = undoRedoContainer.getBond(i);
			container.removeBond(bond);
		}
		for (int i = 0; i < undoRedoContainer.getAtomCount(); i++) {
			final IAtom atom = undoRedoContainer.getAtom(i);
			container.removeAtom(atom);
		}
		chemModelRelay.updateAtoms(container, container.atoms());
		final IAtomContainer molecule = container.getBuilder().newInstance(
				IAtomContainer.class, container);
		final IAtomContainerSet moleculeSet = ConnectivityChecker
				.partitionIntoMolecules(molecule);
		chemModel.setMoleculeSet(moleculeSet);
		if (chemModelRelay.getRGroupHandler() != null) {
			try {
				chemModelRelay.getRGroupHandler().adjustAtomContainers(
						moleculeSet);
			} catch (final CDKException e) {
				e.printStackTrace();
				chemModelRelay.unsetRGroupHandler();
			}
		}
	}

	@Override
	public void undo() {
		for (int i = 0; i < undoRedoContainer.getBondCount(); i++) {
			final IBond bond = undoRedoContainer.getBond(i);
			container.addBond(bond);
		}
		for (int i = 0; i < undoRedoContainer.getAtomCount(); i++) {
			final IAtom atom = undoRedoContainer.getAtom(i);
			container.addAtom(atom);
		}
		chemModelRelay.updateAtoms(container, container.atoms());
		final IAtomContainer molecule = container.getBuilder().newInstance(
				IAtomContainer.class, container);
		final IAtomContainerSet moleculeSet = ConnectivityChecker
				.partitionIntoMolecules(molecule);
		chemModel.setMoleculeSet(moleculeSet);
		if (chemModelRelay.getRGroupHandler() != null) {
			try {
				chemModelRelay.getRGroupHandler().adjustAtomContainers(
						moleculeSet);
			} catch (final CDKException e) {
				chemModelRelay.unsetRGroupHandler();
				e.printStackTrace();
			}
		}
	}

}
