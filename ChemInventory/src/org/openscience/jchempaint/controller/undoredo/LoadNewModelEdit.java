/* $RCSfile$
 * $Author: gilleain $
 * $Date: 2008-11-26 16:01:05 +0000 (Wed, 26 Nov 2008) $
 * $Revision: 13311 $
 *
 * Copyright (C) 2005-2008 Stefan Kuhn
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
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.jchempaint.controller.IChemModelRelay;

/**
 * @cdk.module controlextra
 * @cdk.svnrev $Revision: 10979 $
 */
public class LoadNewModelEdit implements IUndoRedoable {

	private static final long		serialVersionUID	= -9022673628051651034L;

	private final IChemModel		chemModel;
	private final IAtomContainerSet	oldsom;
	private final IReactionSet		oldsor;
	private final IAtomContainerSet	newsom;
	private final IReactionSet		newsor;
	private final String			type;
	private IChemModelRelay			chemModelRelay		= null;

	public LoadNewModelEdit(final IChemModel chemModel,
			final IChemModelRelay relay, final IAtomContainerSet oldsom,
			final IReactionSet oldsor, final IAtomContainerSet newsom,
			final IReactionSet newsor, final String type) {
		this.chemModel = chemModel;
		this.newsom = newsom;
		this.newsor = newsor;
		this.oldsom = oldsom;
		this.oldsor = oldsor;
		this.type = type;
		chemModelRelay = relay;
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
		if (chemModelRelay != null) {
			for (final IAtomContainer ac : newsom.atomContainers()) {
				chemModelRelay.updateAtoms(ac, ac.atoms());
			}
		}
		chemModel.setMoleculeSet(newsom);
		chemModel.setReactionSet(newsor);
	}

	@Override
	public void undo() {
		if (chemModelRelay != null) {
			for (final IAtomContainer ac : oldsom.atomContainers()) {
				chemModelRelay.updateAtoms(ac, ac.atoms());
			}
		}
		chemModel.setMoleculeSet(oldsom);
		chemModel.setReactionSet(oldsor);
	}
}
