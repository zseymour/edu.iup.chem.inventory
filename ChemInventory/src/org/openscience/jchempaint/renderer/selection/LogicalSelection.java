/* Copyright (C) 2009  Gilleain Torrance <gilleain@users.sf.net>
 *               2009  Arvid Berg <goglepox@users.sf.net>
 *
 * Contact: cdk-devel@list.sourceforge.net
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
package org.openscience.jchempaint.renderer.selection;

import java.awt.Color;
import java.util.Collection;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;
import org.openscience.jchempaint.renderer.elements.IRenderingElement;

/**
 * 
 * @author maclean
 * @cdk.module rendercontrol
 */
public class LogicalSelection implements IChemObjectSelection {

	public enum Type {
		ALL, NONE
	};

	private Type		type;

	private IChemModel	chemModel;

	public LogicalSelection(final LogicalSelection.Type type) {
		this.type = type;
	}

	public void clear() {
		type = Type.NONE;
		chemModel = null;
	}

	@Override
	public boolean contains(final IChemObject obj) {
		if (type == Type.NONE) {
			return false;
		}

		for (final IAtomContainer other : ChemModelManipulator
				.getAllAtomContainers(chemModel)) {
			if (other == obj) {
				return true;
			}

			if (obj instanceof IBond) {
				if (other.contains((IBond) obj)) {
					return true;
				}
			}
			if (obj instanceof IAtom) {
				if (other.contains((IAtom) obj)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public <E extends IChemObject> Collection<E> elements(final Class<E> clazz) {
		throw new UnsupportedOperationException();
	}

	public IRenderingElement generate(final Color color) {
		return null;
	}

	@Override
	public IAtomContainer getConnectedAtomContainer() {
		if (chemModel != null) {
			final IAtomContainer ac = chemModel.getBuilder().newInstance(
					IAtomContainer.class);
			for (final IAtomContainer other : ChemModelManipulator
					.getAllAtomContainers(chemModel)) {
				ac.add(other);
			}
			return ac;
		}
		return null;
	}

	public Type getType() {
		return type;
	}

	@Override
	public boolean isFilled() {
		return chemModel != null;
	}

	public boolean isFinished() {
		return true;
	}

	public void select(final IAtomContainer atomContainer) {
		chemModel = atomContainer.getBuilder().newInstance(IChemModel.class);
		final IAtomContainerSet molSet = atomContainer.getBuilder()
				.newInstance(IAtomContainerSet.class);
		molSet.addAtomContainer(atomContainer);
		chemModel.setMoleculeSet(molSet);
	}

	@Override
	public void select(final IChemModel chemModel) {
		if (type == Type.ALL) {
			this.chemModel = chemModel;
		}
	}
}
