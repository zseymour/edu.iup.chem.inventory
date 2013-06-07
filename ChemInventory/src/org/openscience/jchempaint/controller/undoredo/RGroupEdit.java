/*
 *  Copyright (C) 2010 Mark Rijnbeek
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

package org.openscience.jchempaint.controller.undoredo;

import java.util.HashMap;
import java.util.Map;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.isomorphism.matchers.IRGroupQuery;
import org.openscience.cdk.isomorphism.matchers.RGroup;
import org.openscience.cdk.isomorphism.matchers.RGroupList;
import org.openscience.jchempaint.controller.IChemModelRelay;
import org.openscience.jchempaint.rgroups.RGroupHandler;

/**
 * Undo-redo class for clicking together an R-Group query in JCP.
 * 
 * @author markr
 */
public class RGroupEdit implements IUndoRedoable {

	private final String							type;
	private final boolean							isNewRgrp;
	private final IChemModelRelay					hub;
	private final RGroupHandler						rgrpHandler;
	private final Map<IAtom, IAtomContainer>		existingAtomDistr;
	private final Map<IBond, IAtomContainer>		existingBondDistr;
	private final IAtomContainer					existingRoot;
	private final Map<IAtom, Map<Integer, IBond>>	existingRootAttachmentPoints;
	private Map<Integer, RGroupList>				existingRgroupLists	= null;
	private final Map<RGroup, Map<Integer, IAtom>>	existingRGroupApo;
	private final IAtomContainer					redoRootStructure;
	private Map<IAtom, Map<Integer, IBond>>			redoRootAttachmentPoints;
	private Map<Integer, RGroupList>				redoRgroupLists		= null;
	private Map<RGroup, Map<Integer, IAtom>>		redoRGroupApo		= null;
	private final IAtomContainer					userSelection;

	public RGroupEdit(
			final String _type,
			final boolean _isNewRgrp,
			final IChemModelRelay _hub,
			final RGroupHandler _rgrpHandler,
			final Map<IAtom, IAtomContainer> _existingAtomDistr,
			final Map<IBond, IAtomContainer> _existingBondDistr,
			final IAtomContainer _existingRoot,
			final Map<IAtom, Map<Integer, IBond>> _existingRootAttachmentPoints,
			final Map<RGroup, Map<Integer, IAtom>> _existingRGroupApo,
			final Map<Integer, RGroupList> _existingRgroupLists,
			final IAtomContainer _userSelection) {
		type = _type;
		isNewRgrp = _isNewRgrp;
		hub = _hub;
		rgrpHandler = _rgrpHandler;
		existingRoot = _existingRoot;
		existingRootAttachmentPoints = _existingRootAttachmentPoints;
		existingRGroupApo = _existingRGroupApo;
		existingAtomDistr = _existingAtomDistr;
		existingBondDistr = _existingBondDistr;
		existingRgroupLists = _existingRgroupLists;
		redoRootStructure = rgrpHandler.getrGroupQuery().getRootStructure();
		userSelection = _userSelection;
		if (_existingRgroupLists != null) {
			redoRgroupLists = new HashMap<Integer, RGroupList>();
			for (final Integer integer : rgrpHandler.getrGroupQuery()
					.getRGroupDefinitions().keySet()) {
				final int rNum = integer;
				redoRgroupLists.put(rNum, rgrpHandler.getrGroupQuery()
						.getRGroupDefinitions().get(rNum));
			}
		}
		if (existingRGroupApo != null) {
			final RGroup undoRGroup = existingRGroupApo.keySet().iterator()
					.next();
			for (final Integer integer : hub.getRGroupHandler()
					.getrGroupQuery().getRGroupDefinitions().keySet()) {
				for (final RGroup rgrp : hub.getRGroupHandler()
						.getrGroupQuery().getRGroupDefinitions().get(integer)
						.getRGroups()) {
					if (rgrp.equals(undoRGroup)) {
						redoRGroupApo = new HashMap<RGroup, Map<Integer, IAtom>>();
						final HashMap<Integer, IAtom> map = new HashMap<Integer, IAtom>();
						map.put(1, rgrp.getFirstAttachmentPoint());
						map.put(2, rgrp.getSecondAttachmentPoint());
						redoRGroupApo.put(rgrp, map);
					}
				}
			}
		}

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

	/**
	 * Redo actions
	 */
	@Override
	public void redo() {

		if (type.equals("setRoot") || type.equals("setSubstitute")) {

			if (isNewRgrp) {
				hub.setRGroupHandler(rgrpHandler);
			}

			final IRGroupQuery rgrpQ = rgrpHandler.getrGroupQuery();
			for (final IAtom atom : existingAtomDistr.keySet()) {
				existingAtomDistr.get(atom).removeAtom(atom);
			}
			for (final IBond bond : existingBondDistr.keySet()) {
				existingBondDistr.get(bond).removeBond(bond);
			}
			hub.getChemModel().getMoleculeSet().addAtomContainer(userSelection);

			if (type.equals("setRoot")) {
				rgrpQ.setRootStructure(redoRootStructure);
				rgrpQ.getRootStructure().setProperty(CDKConstants.TITLE,
						RGroup.ROOT_LABEL);
				rgrpQ.setRootAttachmentPoints(redoRootAttachmentPoints);
			} else if (type.equals("setSubstitute")) {
				if (redoRgroupLists != null) {
					for (final Integer integer : redoRgroupLists.keySet()) {
						final int rNum = integer;
						rgrpQ.getRGroupDefinitions().put(rNum,
								redoRgroupLists.get(rNum));
					}
				}
			}
		} else if (type.startsWith("setAtomApoAction")) {
			final RGroup redoRGroup = redoRGroupApo.keySet().iterator().next();
			for (final Integer integer : hub.getRGroupHandler()
					.getrGroupQuery().getRGroupDefinitions().keySet()) {
				for (final RGroup rgrp : hub.getRGroupHandler()
						.getrGroupQuery().getRGroupDefinitions().get(integer)
						.getRGroups()) {
					if (rgrp.equals(redoRGroup)) {
						final IAtom apo1 = redoRGroupApo.get(redoRGroup).get(1);
						final IAtom apo2 = redoRGroupApo.get(redoRGroup).get(2);
						rgrp.setFirstAttachmentPoint(apo1);
						rgrp.setSecondAttachmentPoint(apo2);
					}
				}
			}
		} else if (type.startsWith("setBondApoAction")) {
			for (final IAtom rAtom : redoRootAttachmentPoints.keySet()) {
				final Map<Integer, IBond> apoBonds = hub.getRGroupHandler()
						.getrGroupQuery().getRootAttachmentPoints().get(rAtom);

				apoBonds.remove(1);
				apoBonds.remove(2);
				final Map<Integer, IBond> redoApo = redoRootAttachmentPoints
						.get(rAtom);

				if (redoApo.get(1) != null) {
					apoBonds.put(1, redoApo.get(1));
				}
				if (redoApo.get(2) != null) {
					apoBonds.put(2, redoApo.get(2));
				}
			}
		} else if (type.equals("clearRgroup")) {
			hub.unsetRGroupHandler();
		}

	}

	/**
	 * Undo actions
	 */
	@Override
	public void undo() {

		final IRGroupQuery rgrpQ = rgrpHandler.getrGroupQuery();

		if (type.equals("setSubstitute") || type.equals("setRoot")) {
			redoRootAttachmentPoints = rgrpHandler.getrGroupQuery()
					.getRootAttachmentPoints();
			for (final IAtom atom : existingAtomDistr.keySet()) {
				existingAtomDistr.get(atom).addAtom(atom);
			}
			for (final IBond bond : existingBondDistr.keySet()) {
				existingBondDistr.get(bond).addBond(bond);
			}
			hub.getChemModel().getMoleculeSet()
					.removeAtomContainer(userSelection);

			if (type.equals("setRoot")) {
				if (isNewRgrp) {
					rgrpQ.setRootStructure(null);
					rgrpQ.setRootAttachmentPoints(null);
					for (final IAtomContainer atc : hub.getIChemModel()
							.getMoleculeSet().atomContainers()) {
						atc.removeProperty(CDKConstants.TITLE);
					}
					hub.unsetRGroupHandler();
				} else {
					existingRoot.setProperty(CDKConstants.TITLE,
							RGroup.ROOT_LABEL);
					rgrpQ.setRootStructure(existingRoot);
					rgrpQ.setRootAttachmentPoints(existingRootAttachmentPoints);
				}
			}

			else if (type.equals("setSubstitute")) {
				if (existingRgroupLists != null) {
					for (final Integer integer : existingRgroupLists.keySet()) {
						final int rNum = integer;
						rgrpQ.getRGroupDefinitions().put(rNum,
								existingRgroupLists.get(rNum));
					}
				}
			}
		}

		else if (type.startsWith("setAtomApoAction")) {
			final RGroup undoRGroup = existingRGroupApo.keySet().iterator()
					.next();
			for (final Integer integer : hub.getRGroupHandler()
					.getrGroupQuery().getRGroupDefinitions().keySet()) {
				for (final RGroup rgrp : hub.getRGroupHandler()
						.getrGroupQuery().getRGroupDefinitions().get(integer)
						.getRGroups()) {
					if (rgrp.equals(undoRGroup)) {
						final IAtom apo1 = existingRGroupApo.get(undoRGroup)
								.get(1);
						final IAtom apo2 = existingRGroupApo.get(undoRGroup)
								.get(2);
						rgrp.setFirstAttachmentPoint(apo1);
						rgrp.setSecondAttachmentPoint(apo2);
					}
				}
			}
		} else if (type.startsWith("setBondApoAction")) {
			for (final IAtom rAtom : existingRootAttachmentPoints.keySet()) {
				final Map<Integer, IBond> undoApo = existingRootAttachmentPoints
						.get(rAtom);
				final Map<Integer, IBond> apoBonds = rgrpQ
						.getRootAttachmentPoints().get(rAtom);

				redoRootAttachmentPoints = new HashMap<IAtom, Map<Integer, IBond>>();
				final Map<Integer, IBond> redoApo = new HashMap<Integer, IBond>();
				if (apoBonds.get(1) != null) {
					redoApo.put(1, apoBonds.get(1));
				}
				if (apoBonds.get(2) != null) {
					redoApo.put(2, apoBonds.get(2));
				}
				redoRootAttachmentPoints.put(rAtom, redoApo);

				apoBonds.remove(1);
				apoBonds.remove(2);
				if (undoApo.get(1) != null) {
					apoBonds.put(1, undoApo.get(1));
				}
				if (undoApo.get(2) != null) {
					apoBonds.put(2, undoApo.get(2));
				}
			}
		} else if (type.equals("clearRgroup")) {
			hub.setRGroupHandler(rgrpHandler);
			rgrpQ.getRootStructure().setProperty(CDKConstants.TITLE,
					RGroup.ROOT_LABEL);
			for (final Integer integer : hub.getRGroupHandler()
					.getrGroupQuery().getRGroupDefinitions().keySet()) {
				final int rNum = integer;
				for (final RGroup rgrp : hub.getRGroupHandler()
						.getrGroupQuery().getRGroupDefinitions().get(rNum)
						.getRGroups()) {
					rgrp.getGroup().setProperty(CDKConstants.TITLE,
							RGroup.makeLabel(rNum));
				}
			}
		}
	}

}
