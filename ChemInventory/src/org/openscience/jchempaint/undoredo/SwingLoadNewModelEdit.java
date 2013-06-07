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

import javax.swing.undo.UndoableEdit;

import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.jchempaint.controller.IChemModelRelay;
import org.openscience.jchempaint.controller.undoredo.LoadNewModelEdit;

/**
 * A swing undo-redo implementation for loading a new ChemModel.
 * 
 */
public class SwingLoadNewModelEdit extends LoadNewModelEdit implements
		UndoableEdit {
	public SwingLoadNewModelEdit(final IChemModel chemModel,
			final IChemModelRelay relay, final IAtomContainerSet oldsom,
			final IReactionSet oldsor, final IAtomContainerSet newsom,
			final IReactionSet newsor, final String type) {
		super(chemModel, relay, oldsom, oldsor, newsom, newsor, type);
	}

	@Override
	public boolean addEdit(final UndoableEdit arg0) {
		return false;
	}

	@Override
	public void die() {
	}

	@Override
	public String getRedoPresentationName() {
		return getPresentationName();
	}

	@Override
	public String getUndoPresentationName() {
		return getPresentationName();
	}

	@Override
	public boolean isSignificant() {
		return true;
	}

	@Override
	public boolean replaceEdit(final UndoableEdit arg0) {
		return false;
	}
}
