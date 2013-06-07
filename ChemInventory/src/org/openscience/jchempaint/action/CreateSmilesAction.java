/*
 *  $RCSfile$
 *  $Author: egonw $
 *  $Date: 2007-01-04 17:26:00 +0000 (Thu, 04 Jan 2007) $
 *  $Revision: 7634 $
 *
 *  Copyright (C) 1997-2008 Christoph Steinbeck, Stefan Kuhn
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
package org.openscience.jchempaint.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.JFrame;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.layout.HydrogenPlacer;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;
import org.openscience.jchempaint.GT;
import org.openscience.jchempaint.dialog.TextViewDialog;

/**
 * Creates a SMILES from the current model
 * 
 */
public class CreateSmilesAction extends JCPAction {

	private static final long	serialVersionUID	= -4886982931009753342L;

	public static String getChiralSmiles(final IChemModel model)
			throws CDKException, ClassNotFoundException, IOException,
			CloneNotSupportedException {
		String chiralsmiles = "";
		final SmilesGenerator generator = new SmilesGenerator();
		generator.setUseAromaticityFlag(true);
		final Iterator<IAtomContainer> containers = ChemModelManipulator
				.getAllAtomContainers(model).iterator();
		while (containers.hasNext()) {
			final IAtomContainer container = containers.next();
			final AtomContainer AtomContainerwithh = new AtomContainer(
					container);
			CDKHydrogenAdder.getInstance(AtomContainerwithh.getBuilder())
					.addImplicitHydrogens(AtomContainerwithh);
			AtomContainerManipulator
					.convertImplicitToExplicitHydrogens(AtomContainerwithh);
			final double bondLength = GeometryTools
					.getBondLengthAverage(container);
			new HydrogenPlacer().placeHydrogens2D(AtomContainerwithh,
					bondLength);
			final boolean[] bool = new boolean[AtomContainerwithh
					.getBondCount()];
			for (int i = 0; i < bool.length; i++) {
				if (generator.isValidDoubleBondConfiguration(
						AtomContainerwithh, AtomContainerwithh.getBond(i))) {
					bool[i] = true;
				}
			}
			chiralsmiles += generator.createChiralSMILES(AtomContainerwithh,
					bool);
			AtomContainerManipulator
					.percieveAtomTypesAndConfigureAtoms(container);
			final CDKHydrogenAdder hAdder = CDKHydrogenAdder
					.getInstance(container.getBuilder());
			hAdder.addImplicitHydrogens(container);
			// valencies are set when creating smiles, which we don't want in
			// jcp
			for (int i = 0; i < container.getAtomCount(); i++) {
				container.getAtom(i).setValency(null);
			}
			if (containers.hasNext()) {
				chiralsmiles += ".";
			}
		}
		return chiralsmiles;
	}

	public static String getSmiles(final IChemModel model) throws CDKException,
			ClassNotFoundException, IOException, CloneNotSupportedException {
		String smiles = "";
		final SmilesGenerator generator = new SmilesGenerator();
		generator.setUseAromaticityFlag(true);
		final Iterator<IAtomContainer> containers = ChemModelManipulator
				.getAllAtomContainers(model).iterator();
		while (containers.hasNext()) {
			final IAtomContainer container = containers.next();
			final AtomContainer AtomContainer = new AtomContainer(container);
			smiles += generator.createSMILES(AtomContainer);
			// valencies are set when creating smiles, which we don't want in
			// jcp
			for (int i = 0; i < container.getAtomCount(); i++) {
				container.getAtom(i).setValency(null);
			}
			if (containers.hasNext()) {
				smiles += ".";
			}
		}
		return smiles;
	}

	TextViewDialog	dialog	= null;

	JFrame			frame	= null;

	@Override
	public void actionPerformed(final ActionEvent e) {
		logger.debug("Trying to create smile: ", type);
		if (dialog == null) {
			dialog = new TextViewDialog(frame, "SMILES", null, false, 40, 2);
			dialog.setName("smilestextdialog");
		}
		String smiles = "";
		String chiralsmiles = "";
		try {
			smiles = getSmiles(jcpPanel.getChemModel());
			chiralsmiles = getChiralSmiles(jcpPanel.getChemModel());
			dialog.setMessage(GT._("Generated SMILES:"), "SMILES: " + smiles
					+ System.getProperty("line.separator") + "chiral SMILES: "
					+ chiralsmiles);
		} catch (final Exception exception) {
			final String message = GT._("Error while creating SMILES:") + " "
					+ exception.getMessage();
			logger.error(message);
			logger.debug(exception);
			dialog.setMessage(GT._("Error"), message);
		}
		dialog.setVisible(true);
	}

}
