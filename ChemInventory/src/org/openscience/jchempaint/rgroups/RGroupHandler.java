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

package org.openscience.jchempaint.rgroups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.vecmath.Point2d;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.isomorphism.matchers.IRGroupQuery;
import org.openscience.cdk.isomorphism.matchers.RGroup;
import org.openscience.cdk.isomorphism.matchers.RGroupList;
import org.openscience.cdk.isomorphism.matchers.RGroupQuery;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;
import org.openscience.jchempaint.GT;
import org.openscience.jchempaint.controller.IChemModelRelay;

/**
 * Provides common functionality for JChempants to handle R-groups, such as lay
 * out, clean up and verifying operations.
 */
public class RGroupHandler {

	private IRGroupQuery	rGroupQuery;

	public RGroupHandler(final IRGroupQuery _rGroupQuery) {
		rGroupQuery = _rGroupQuery;
	}

	/**
	 * The RGroupQuery references atom containers (the root and the
	 * substitutes). However, other JCP modules can re-create the atom
	 * containers, such as happens in
	 * {@link org.openscience.jchempaint.controller.undoredo.RemoveAtomsAndBondsEdit}
	 * . In such cases, this method needs to be called to reset the atom
	 * containers in the RGroup to the newly created ones.
	 * 
	 * @param newSet
	 *            molecule set with freshly created containers (but existing
	 *            atoms)
	 * @throws CDKException
	 */
	public void adjustAtomContainers(final IAtomContainerSet newSet)
			throws CDKException {
		// System.out.println("^^^ adjustAtomContainers(IAtomContainerSet newSet)");
		boolean hasRoot = false;
		if (rGroupQuery != null) {
			for (final IAtomContainer newAtc : newSet.atomContainers()) {
				atoms: for (final IAtom movedAtom : newAtc.atoms()) {

					if (rGroupQuery.getRootStructure().contains(movedAtom)) {
						// System.out.println("set root "+newAtc.hashCode());
						rGroupQuery.setRootStructure(newAtc);
						newAtc.setProperty(CDKConstants.TITLE,
								RGroup.ROOT_LABEL);
						hasRoot = true;
						break atoms;
					} else {
						final Map<Integer, RGroupList> def = rGroupQuery
								.getRGroupDefinitions();
						for (final Integer integer : def.keySet()) {
							final int rgrpNum = integer;
							final List<RGroup> rgpList = def.get(rgrpNum)
									.getRGroups();
							for (int i = 0; i < rgpList.size(); i++) {

								if (rgpList.get(i).getGroup()
										.contains(movedAtom)
										|| rgpList.get(i)
												.getFirstAttachmentPoint() != null
										&& rgpList.get(i)
												.getFirstAttachmentPoint()
												.equals(movedAtom)) {
									rgpList.get(i).setGroup(newAtc);
									/*
									 * //makes undo of deleting all atoms seq
									 * work better.. never mind garbage
									 * 
									 * if (!newAtc.contains(rgpList.get(i).
									 * getFirstAttachmentPoint()) ) {
									 * rgpList.get
									 * (i).setFirstAttachmentPoint(null); } if
									 * (!newAtc.contains(rgpList.get(i).
									 * getSecondAttachmentPoint()) ) {
									 * rgpList.get
									 * (i).setSecondAttachmentPoint(null); }
									 */
									newAtc.setProperty(CDKConstants.TITLE,
											RGroup.makeLabel(rgrpNum));
									break atoms;
								}
							}
						}
					}
				}
			}
			if (!hasRoot) {
				System.err.println(">>BAD: lost track of the R-group");
				rGroupQuery = null;
				for (final IAtomContainer atc : newSet.atomContainers()) {
					atc.setProperty(CDKConstants.TITLE, null);
				}
				throw new CDKException("R-group invalidated");
			}
		}
	}

	/**
	 * TODO
	 * 
	 * @param at
	 * @param hub
	 * @return
	 */
	public boolean checkRGroupOkayForDelete(final IAtom at,
			final IChemModelRelay hub) {
		final IAtomContainer tmp = at.getBuilder().newInstance(
				IAtomContainer.class);
		tmp.addAtom(at);
		return checkRGroupOkayForDelete(tmp, hub);
	}

	/**
	 * Method to detect if removing atoms/bonds has unwanted results for the
	 * R-group.
	 * 
	 * @param atc
	 * @param hub
	 * @return
	 */
	public boolean checkRGroupOkayForDelete(final IAtomContainer atc,
			final IChemModelRelay hub) {
		// Check if the root would still remain there (partly) after a delete..
		if (rGroupQuery != null) {
			boolean rootRemains = false;
			root: for (final IAtom a : rGroupQuery.getRootStructure().atoms()) {
				if (!atc.contains(a)) {
					rootRemains = true;
					break root;
				}
			}
			if (!rootRemains) {
				final int answer = JOptionPane
						.showConfirmDialog(
								hub.getRenderer().getRenderPanel(),
								GT._("This operation would irreversibly remove the R-Group query. Continue?"),
								GT._("R-Group alert"),
								JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.NO_OPTION) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Cleans up atom containers in the R-group that do not exists (anymore) in
	 * the molecule set in the hub, possibly due to deletion or merging.
	 * 
	 * @param moleculeSet
	 */
	public void cleanUpRGroup(final IAtomContainerSet moleculeSet) {
		final List<Integer> rgrpToRemove = new ArrayList<Integer>();
		if (rGroupQuery != null) {
			final Map<Integer, RGroupList> def = rGroupQuery
					.getRGroupDefinitions();
			for (final Integer integer : def.keySet()) {
				// Remove RGroups with empty atom containers from RGroupLists
				final int rgrpNum = integer;
				final List<RGroup> rgpList = def.get(rgrpNum).getRGroups();
				for (int i = 0; i < rgpList.size(); i++) {
					if (!exists(rgpList.get(i).getGroup(), moleculeSet)
							|| rgpList.get(i).getGroup().getAtomCount() == 0) {
						rgpList.remove(i);
					}
				}
				// Drop RGroupLists that don't have any content atom-wise
				int atomCount = 0;
				for (final RGroup rgrp : rGroupQuery.getRGroupDefinitions()
						.get(rgrpNum).getRGroups()) {
					atomCount += rgrp.getGroup().getAtomCount();
				}
				if (atomCount == 0) {
					rgrpToRemove.add(rgrpNum);
				}
			}
			for (final Integer rgrpNum : rgrpToRemove) {
				rGroupQuery.getRGroupDefinitions().remove(rgrpNum);
			}
		}

	}

	/**
	 * Helper method for {@link #cleanUpRGroup(IAtomContainerSet)}, checks if an
	 * atom container referred to in the R-group still exists in the current
	 * molecule set in the hub.
	 * 
	 * @param atcRgrp
	 * @param chemModel
	 */
	private boolean exists(final IAtomContainer atcRgrp,
			final IAtomContainerSet moleculeSet) {
		final int i = 0;
		for (final IAtomContainer atc : moleculeSet.atomContainers()) {
			if (atc == atcRgrp) {
				return true;
			}
		}
		return false;
	}

	/**
	 * TODO
	 * 
	 * @param atHash
	 * @param atc
	 * @return
	 */
	private IAtom findAtom(final Integer atHash, final IAtomContainer atc) {
		if (atHash != null) {
			for (final IAtom at : atc.atoms()) {
				if (at.hashCode() == atHash) {
					return at;
				}
			}
		}
		return null;
	}

	/**
	 * Helper method to find boundaries of a given atom container.
	 * 
	 * @param atc
	 *            atom container
	 * @param isX
	 *            true if interested in X boundary, false for Y
	 * @param smallest
	 *            true if we want smallest, false if largest
	 * @param startVal
	 *            starting point
	 * @return boundary coordinate (x or y)
	 */
	private double findBoundary(final IAtomContainer atc, final boolean isX,
			final boolean smallest, final double startVal) {
		double retVal = startVal;
		for (final IAtom atom : atc.atoms()) {
			if (isX) {
				if (smallest) {
					if (atom.getPoint2d().x < retVal) {
						retVal = atom.getPoint2d().x;
					}
				} else {
					if (atom.getPoint2d().x > retVal) {
						retVal = atom.getPoint2d().x;
					}
				}
			} else if (smallest) {
				if (atom.getPoint2d().y < retVal) {
					retVal = atom.getPoint2d().y;
				}
			} else {
				if (atom.getPoint2d().y > retVal) {
					retVal = atom.getPoint2d().y;
				}
			}
		}
		return retVal;
	}

	/**
	 * TODO
	 * 
	 * @param atcHash
	 * @param mset
	 * @return
	 */
	private IAtomContainer findContainer(final Integer atcHash,
			final IAtomContainerSet mset) {
		if (atcHash != null) {
			for (final IAtomContainer atc : mset.atomContainers()) {
				if (atc.hashCode() == atcHash) {
					return atc;
				}
			}
		}
		return null;
	}

/**
	 * Creates a {@link org.openscience.cdk.interfaces.IAtomContainerSet} from a
	 * provided {@link org.openscience.cdk.isomorphism.matchers.IRGroupQuery).
	 * The root structure becomes the atom container as position zero,  the
	 * substitutes follow on position 1..n, ordered by R-group number.
	 *   
	 * @param chemModel
	 * @param RgroupQuery
	 * @throws CDKException
	 */
	public IAtomContainerSet getMoleculeSet(final IChemModel chemModel)
			throws CDKException {

		if (rGroupQuery == null || rGroupQuery.getRootStructure() == null
				|| rGroupQuery.getRootStructure().getAtomCount() == 0) {
			throw new CDKException("The R-group is empty");
		}

		final IAtomContainerSet moleculeSet = chemModel.getBuilder()
				.newInstance(IAtomContainerSet.class);
		moleculeSet.addAtomContainer(rGroupQuery.getRootStructure());
		chemModel.setMoleculeSet(moleculeSet);
		for (final int rgrpNum : sortRGroupNumbers()) {
			final RGroupList rgrpList = rGroupQuery.getRGroupDefinitions().get(
					rgrpNum);
			for (final RGroup rgrp : rgrpList.getRGroups()) {
				chemModel.getMoleculeSet().addAtomContainer(rgrp.getGroup());
			}
		}
		return moleculeSet;
	}

	public IRGroupQuery getrGroupQuery() {
		return rGroupQuery;
	}

	/**
	 * Method to check whether a given atom is part of one of the substitutes.
	 * 
	 * @param atom
	 */
	public boolean isAtomPartOfSubstitute(final IAtom atom) {
		if (rGroupQuery != null && rGroupQuery.getRGroupDefinitions() != null) {
			for (final Integer integer : rGroupQuery.getRGroupDefinitions()
					.keySet()) {
				final RGroupList rgpList = rGroupQuery.getRGroupDefinitions()
						.get(integer);
				if (rgpList != null && rgpList.getRGroups() != null) {
					for (final RGroup rgrp : rgpList.getRGroups()) {
						if (rgrp.getGroup().contains(atom)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Verifies if a merge is allowed from the R-Group's point of view. Merging
	 * between the root structure and r-group substitutes is not allowed,
	 * because it does not makes sense (plus the root structure could get lost).
	 * 
	 * @param hub
	 *            controller hub that is about to do a merge.
	 */
	public boolean isMergeAllowed(final IChemModelRelay hub) {
		// System.out.println("^^^ isMergeAllowed(IChemModelRelay hub)");

		if (rGroupQuery != null) {
			for (final IAtom mergedAtom : hub.getRenderer()
					.getRenderer2DModel().getMerge().keySet()) {
				final IAtom mergedPartnerAtom = hub.getRenderer()
						.getRenderer2DModel().getMerge().get(mergedAtom);
				final IAtomContainer container1 = ChemModelManipulator
						.getRelevantAtomContainer(hub.getChemModel(),
								mergedAtom);
				final IAtomContainer container2 = ChemModelManipulator
						.getRelevantAtomContainer(hub.getChemModel(),
								mergedPartnerAtom);

				if (container1 != container2) {
					final List<IAtomContainer> substitutes = rGroupQuery
							.getSubstituents();
					if (container1 == rGroupQuery.getRootStructure()
							&& substitutes.contains(container2)
							|| container2 == rGroupQuery.getRootStructure()
							&& substitutes.contains(container1)) {
						JOptionPane
								.showMessageDialog(
										hub.getRenderer().getRenderPanel(),
										GT._("This operation is not allowed in the R-Group configuration."),
										GT._("R-Group alert"),
										JOptionPane.INFORMATION_MESSAGE);
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Method to check whether a given bond exists in the root and is attached
	 * to an R-Group.
	 * 
	 * @param bond
	 */
	public boolean isRGroupRootBond(final IBond bond) {
		if (rGroupQuery != null && rGroupQuery.getRootStructure() != null
				&& rGroupQuery.getRootStructure().contains(bond)) {
			for (final IAtom atom : bond.atoms()) {
				if (atom instanceof IPseudoAtom
						&& RGroupQuery
								.isValidRgroupQueryLabel(((IPseudoAtom) atom)
										.getLabel())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Changes atom coordinates so that the substitutes of the R-group are lined
	 * up underneath the root structure, resulting in a clear presentation.
	 * Method intended to be used after reading an external R-group files (RG
	 * files), overriding the coordinates in the file.
	 * 
	 * @param RgroupQuery
	 * @throws CDKException
	 */
	public void layoutRgroup() throws CDKException {

		if (rGroupQuery == null || rGroupQuery.getRootStructure() == null
				|| rGroupQuery.getRootStructure().getAtomCount() == 0) {
			throw new CDKException("The R-group is empty");
		}

		/*
		 * This is how we want to layout:
		 * 
		 * {Root structure}
		 * 
		 * {R1.a} {R1.b} {R1.c} ... {R2.a} {R2.b} ... {R3.a} {R3.b} {R3.c} ...
		 * .... ..
		 */
		final double MARGIN = 2;
		final IAtomContainer rootStruct = rGroupQuery.getRootStructure();
		final double xLeft = findBoundary(rootStruct, true, true,
				Double.POSITIVE_INFINITY);
		double yBottom = findBoundary(rootStruct, false, true,
				Double.POSITIVE_INFINITY) - MARGIN;
		double minListYBottom = yBottom;

		for (final int rgrpNum : sortRGroupNumbers()) {
			double listXRight = xLeft;

			final RGroupList rgrpList = rGroupQuery.getRGroupDefinitions().get(
					rgrpNum);
			for (final RGroup rgrp : rgrpList.getRGroups()) {

				final double rgrpXleft = findBoundary(rgrp.getGroup(), true,
						true, Double.POSITIVE_INFINITY);
				final double rgrpYtop = findBoundary(rgrp.getGroup(), false,
						false, Double.NEGATIVE_INFINITY);
				final double shiftX = listXRight - rgrpXleft;
				final double shiftY = yBottom - rgrpYtop;
				for (final IAtom atom : rgrp.getGroup().atoms()) {
					atom.setPoint2d(new Point2d(atom.getPoint2d().x + shiftX,
							atom.getPoint2d().y + shiftY));
				}
				minListYBottom = findBoundary(rgrp.getGroup(), false, true,
						minListYBottom);
				final double rgrpXRight = findBoundary(rgrp.getGroup(), true,
						false, Double.NEGATIVE_INFINITY);
				listXRight = rgrpXRight + MARGIN;
			}
			yBottom = minListYBottom - MARGIN;
		}
	}

	/**
	 * Hashes the R-group's atom container-related information. This can be used
	 * in the undo/redo of modules that change/drop/swap atom containers such as
	 * merging.
	 * 
	 * @return hash mash of RGroup data
	 */
	public Map<Integer, Map<Integer, Integer>> makeHash() {
		final Map<Integer, Map<Integer, Integer>> rgrpHash = new HashMap<Integer, Map<Integer, Integer>>();

		if (rGroupQuery != null) {
			final Map<Integer, RGroupList> def = rGroupQuery
					.getRGroupDefinitions();
			for (final Integer integer : def.keySet()) {
				final int rgrpNum = integer;
				final List<RGroup> rgpList = def.get(rgrpNum).getRGroups();
				for (final RGroup rgp : rgpList) {
					if (rgp != null) {
						final Map<Integer, Integer> hash = new HashMap<Integer, Integer>();
						hash.put(0, rgp.getGroup() == null ? null : rgp
								.getGroup().hashCode());
						hash.put(1,
								rgp.getFirstAttachmentPoint() == null ? null
										: rgp.getFirstAttachmentPoint()
												.hashCode());
						hash.put(2,
								rgp.getSecondAttachmentPoint() == null ? null
										: rgp.getSecondAttachmentPoint()
												.hashCode());
						rgrpHash.put(rgp.hashCode(), hash);
					}
				}
			}
			final Map<Integer, Integer> root = new HashMap<Integer, Integer>();
			root.put(0, rGroupQuery.getRootStructure().hashCode());
			rgrpHash.put(-1, root);
		}
		return rgrpHash;
	}

	/**
	 * See restores what was saved by makeHash().
	 */
	public void restoreFromHash(final Map<Integer, Map<Integer, Integer>> mash,
			final IAtomContainerSet mset) {
		if (rGroupQuery != null) {
			final int rootHash = mash.get(-1).get(0);
			rGroupQuery.setRootStructure(findContainer(rootHash, mset));

			final Map<Integer, RGroupList> def = rGroupQuery
					.getRGroupDefinitions();
			for (final Integer integer : mash.keySet()) {
				final int rgpHash = integer;
				restore: for (final Integer integer2 : def.keySet()) {
					final int rgrpNum = integer2;
					final List<RGroup> rgpList = def.get(rgrpNum).getRGroups();
					for (final RGroup rgp : rgpList) {
						if (rgp != null && rgp.hashCode() == rgpHash) {
							rgp.setGroup(findContainer(
									mash.get(rgpHash).get(0), mset));
							if (rgp.getGroup() != null) {
								rgp.setFirstAttachmentPoint(findAtom(
										mash.get(rgpHash).get(1),
										rgp.getGroup()));
								rgp.setSecondAttachmentPoint(findAtom(
										mash.get(rgpHash).get(2),
										rgp.getGroup()));
							}
							break restore;
						}
					}
				}
			}
		}
	}

	public void setrGroupQuery(final IRGroupQuery rGroupQuery) {
		this.rGroupQuery = rGroupQuery;
	}

	/**
	 * Helper to get an ordered list of R-group numbers for a certain
	 * IRGroupQuery.
	 * 
	 * @param RgroupQuery
	 * @return an ordered list of R-group numbers in rgroupQuery.
	 */
	private List<Integer> sortRGroupNumbers() {
		final List<Integer> rNumbers = new ArrayList<Integer>();
		if (rGroupQuery != null) {
			for (final Integer integer : rGroupQuery.getRGroupDefinitions()
					.keySet()) {
				rNumbers.add(integer);
			}
			Collections.sort(rNumbers);
		}
		return rNumbers;
	}

}
