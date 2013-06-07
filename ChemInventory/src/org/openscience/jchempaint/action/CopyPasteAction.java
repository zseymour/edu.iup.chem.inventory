/*
 *  $RCSfile$
 *  $Author: egonw $
 *  $Date: 2007-01-04 17:26:00 +0000 (Thu, 04 Jan 2007) $
 *  $Revision: 7634 $
 *
 *  Copyright (C) 1997-2008 Egon Willighagen, Stefan Kuhn
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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;

import javax.swing.JOptionPane;
import javax.vecmath.Point2d;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemModel;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.IChemObjectWriter;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.RGroupQueryReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.isomorphism.matchers.IRGroupQuery;
import org.openscience.cdk.isomorphism.matchers.RGroupQuery;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.layout.TemplateHandler;
import org.openscience.cdk.smiles.FixBondOrdersTool;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.AtomContainerSetManipulator;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;
import org.openscience.cdk.tools.manipulator.ReactionManipulator;
import org.openscience.jchempaint.GT;
import org.openscience.jchempaint.JChemPaintPanel;
import org.openscience.jchempaint.application.JChemPaint;
import org.openscience.jchempaint.controller.ControllerHub;
import org.openscience.jchempaint.controller.MoveModule;
import org.openscience.jchempaint.controller.RemoveModule;
import org.openscience.jchempaint.controller.SelectSquareModule;
import org.openscience.jchempaint.dialog.TemplateBrowser;
import org.openscience.jchempaint.renderer.RendererModel;
import org.openscience.jchempaint.renderer.selection.IChemObjectSelection;
import org.openscience.jchempaint.renderer.selection.LogicalSelection;
import org.openscience.jchempaint.renderer.selection.RectangleSelection;
import org.openscience.jchempaint.renderer.selection.ShapeSelection;
import org.openscience.jchempaint.renderer.selection.SingleSelection;
import org.openscience.jchempaint.rgroups.RGroupHandler;

/**
 * Action to copy/paste structures.
 */
public class CopyPasteAction extends JCPAction {

	class JcpSelection implements Transferable, ClipboardOwner {
		private final DataFlavor[]	supportedFlavors	= {
																molFlavor,
																DataFlavor.stringFlavor,
																svgFlavor,
																cmlFlavor,
																smilesFlavor };
		String						mol;
		String						smiles;
		String						svg;
		String						cml;

		@SuppressWarnings("unchecked")
		public JcpSelection(final IAtomContainer tocopy1) {
			final IAtomContainer tocopy = tocopy1.getBuilder().newInstance(
					IAtomContainer.class, tocopy1);
			// MDL mol output
			StringWriter sw = new StringWriter();
			try {
				new MDLV2000Writer(sw).writeMolecule(tocopy);
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mol = sw.toString();
			final SmilesGenerator sg = new SmilesGenerator();
			smiles = sg.createSMILES(tocopy);
			// SVG output
			svg = jcpPanel.getSVGString();
			// CML output
			sw = new StringWriter();
			Class cmlWriterClass = null;
			try {
				cmlWriterClass = this.getClass().getClassLoader()
						.loadClass("org.openscience.cdk.io.CMLWriter");
				if (cmlWriterClass != null) {
					IChemObjectWriter cow = (IChemObjectWriter) cmlWriterClass
							.newInstance();
					final Constructor constructor = cow.getClass()
							.getConstructor(new Class[] { Writer.class });
					cow = (IChemObjectWriter) constructor
							.newInstance(new Object[] { sw });
					cow.write(tocopy);
					cow.close();
				}
				cml = sw.toString();
			} catch (final Exception exception) {
				logger.error("Could not load CMLWriter: ",
						exception.getMessage());
				logger.debug(exception);
			}
		}

		@Override
		public synchronized Object getTransferData(final DataFlavor parFlavor)
				throws UnsupportedFlavorException {
			if (parFlavor.equals(molFlavor)) {
				return new StringReader(mol);
			} else if (parFlavor.equals(smilesFlavor)) {
				return new StringReader(smiles);
			} else if (parFlavor.equals(DataFlavor.stringFlavor)) {
				return mol;
			} else if (parFlavor.equals(cmlFlavor)) {
				return new StringReader(cml);
			} else if (parFlavor.equals(svgFlavor)) {
				return new StringReader(svg);
			} else {
				throw new UnsupportedFlavorException(parFlavor);
			}
		}

		@Override
		public synchronized DataFlavor[] getTransferDataFlavors() {
			return supportedFlavors;
		}

		@Override
		public boolean isDataFlavorSupported(final DataFlavor parFlavor) {
			for (final DataFlavor supportedFlavor : supportedFlavors) {
				if (supportedFlavor.equals(parFlavor)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void lostOwnership(final Clipboard parClipboard,
				final Transferable parTransferable) {
			System.out.println("Lost ownership");
		}
	}

	class SmilesSelection implements Transferable, ClipboardOwner {
		private final DataFlavor[]	supportedFlavors	= { DataFlavor.stringFlavor };

		String						smiles;

		public SmilesSelection(final String smiles) throws Exception {
			this.smiles = smiles;
		}

		@Override
		public synchronized Object getTransferData(final DataFlavor parFlavor)
				throws UnsupportedFlavorException {
			if (parFlavor.equals(DataFlavor.stringFlavor)) {
				return smiles;
			} else {
				throw new UnsupportedFlavorException(parFlavor);
			}
		}

		@Override
		public synchronized DataFlavor[] getTransferDataFlavors() {
			return supportedFlavors;
		}

		@Override
		public boolean isDataFlavorSupported(final DataFlavor parFlavor) {
			for (final DataFlavor supportedFlavor : supportedFlavors) {
				if (supportedFlavor.equals(parFlavor)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void lostOwnership(final Clipboard parClipboard,
				final Transferable parTransferable) {
			System.out.println("Lost ownership");
		}
	}

	private static final long	serialVersionUID	= -3343207264261279526L;
	private final DataFlavor	molFlavor			= new DataFlavor(
															"chemical/x-mdl-molfile",
															"mdl mol file format");
	private final DataFlavor	svgFlavor			= new DataFlavor(
															"image/svg+xml",
															"scalable vector graphics");

	private final DataFlavor	cmlFlavor			= new DataFlavor(
															"image/cml",
															"chemical markup language");

	private final DataFlavor	smilesFlavor		= new DataFlavor(
															"chemical/x-daylight-smiles",
															"smiles format");

	@Override
	public void actionPerformed(final ActionEvent e) {
		logger.info("  type  ", type);
		logger.debug("  source ", e.getSource());

		final RendererModel renderModel = jcpPanel.get2DHub().getRenderer()
				.getRenderer2DModel();
		IChemModel chemModel = jcpPanel.getChemModel();
		final Clipboard sysClip = jcpPanel.getToolkit().getSystemClipboard();

		if ("copy".equals(type)) {
			handleSystemClipboard(sysClip);
			IAtom atomInRange = null;
			final IChemObject object = getSource(e);
			logger.debug("Source of call: ", object);
			if (object instanceof IAtom) {
				atomInRange = (IAtom) object;
			} else {
				atomInRange = renderModel.getHighlightedAtom();
			}
			if (atomInRange != null) {
				final IAtomContainer tocopyclone = atomInRange.getBuilder()
						.newInstance(IAtomContainer.class);
				try {
					tocopyclone.addAtom((IAtom) atomInRange.clone());
				} catch (final CloneNotSupportedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				addToClipboard(sysClip, tocopyclone);
			} else if (renderModel.getHighlightedBond() != null) {
				final IBond bond = renderModel.getHighlightedBond();
				if (bond != null) {
					final IAtomContainer tocopyclone = bond.getBuilder()
							.newInstance(IAtomContainer.class);
					try {
						tocopyclone.addAtom((IAtom) bond.getAtom(0).clone());
						tocopyclone.addAtom((IAtom) bond.getAtom(1).clone());
					} catch (final CloneNotSupportedException e1) {
						e1.printStackTrace();
					}
					tocopyclone.addBond(bond.getBuilder().newInstance(
							IBond.class, tocopyclone.getAtom(0),
							tocopyclone.getAtom(1), bond.getOrder()));
					addToClipboard(sysClip, tocopyclone);
				}
			} else if (renderModel.getSelection().getConnectedAtomContainer() != null) {
				addToClipboard(sysClip, renderModel.getSelection()
						.getConnectedAtomContainer());
			} else {
				addToClipboard(sysClip,
						JChemPaintPanel.getAllAtomContainersInOne(chemModel));
			}
		} else if ("copyAsSmiles".equals(type)) {
			handleSystemClipboard(sysClip);
			try {
				if (renderModel.getSelection().getConnectedAtomContainer() != null) {
					final SmilesGenerator sg = new SmilesGenerator();
					sysClip.setContents(
							new SmilesSelection(
									sg.createSMILES(renderModel
											.getSelection()
											.getConnectedAtomContainer()
											.getBuilder()
											.newInstance(
													IAtomContainer.class,
													renderModel
															.getSelection()
															.getConnectedAtomContainer()))),
							null);
				} else {
					sysClip.setContents(
							new SmilesSelection(CreateSmilesAction
									.getSmiles(chemModel)), null);
				}
			} catch (final Exception e1) {
				e1.printStackTrace();
			}
		} else if ("eraser".equals(type)) {
			final RemoveModule newActiveModule = new RemoveModule(
					jcpPanel.get2DHub());
			newActiveModule.setID(type);
			jcpPanel.get2DHub().setActiveDrawModule(newActiveModule);
			IAtom atomInRange = null;
			IBond bondInRange = null;
			final IChemObject object = getSource(e);
			logger.debug("Source of call: ", object);
			if (object instanceof IAtom) {
				atomInRange = (IAtom) object;
			} else {
				atomInRange = renderModel.getHighlightedAtom();
			}
			if (object instanceof IBond) {
				bondInRange = (IBond) object;
			} else {
				bondInRange = renderModel.getHighlightedBond();
			}
			if (atomInRange != null) {
				jcpPanel.get2DHub().removeAtom(atomInRange);
				renderModel.setHighlightedAtom(null);
			} else if (bondInRange != null) {
				jcpPanel.get2DHub().removeBond(bondInRange);
			} else if (renderModel.getSelection() != null
					&& renderModel.getSelection().getConnectedAtomContainer() != null) {
				final IChemObjectSelection selection = renderModel
						.getSelection();
				final IAtomContainer selected = selection
						.getConnectedAtomContainer();
				jcpPanel.get2DHub().deleteFragment(selected);
				renderModel.setSelection(new LogicalSelection(
						LogicalSelection.Type.NONE));
				jcpPanel.get2DHub().updateView();
			}
		} else if (type.indexOf("pasteTemplate") > -1) {
			// if templates are shown, we extract the tab to show if any
			String templatetab = "";
			if (type.indexOf("_") > -1) {
				templatetab = type.substring(type.indexOf("_") + 1);
			}
			final TemplateBrowser templateBrowser = new TemplateBrowser(
					templatetab);
			if (templateBrowser.getChosenmolecule() != null) {
				scaleStructure(templateBrowser.getChosenmolecule());
				insertStructure(templateBrowser.getChosenmolecule(),
						renderModel);
				jcpPanel.getRenderPanel().setZoomWide(true);
				jcpPanel.get2DHub().getRenderer().getRenderer2DModel()
						.setZoomFactor(1);
			}
		} else if ("paste".equals(type)) {
			handleSystemClipboard(sysClip);
			final Transferable transfer = sysClip.getContents(null);
			ISimpleChemObjectReader reader = null;
			String content = null;

			if (supported(transfer, molFlavor)) {
				final StringBuffer sb = new StringBuffer();
				try {
					// StringBufferInputStream sbis=null;
					// sbis = (StringBufferInputStream)
					// transfer.getTransferData(molFlavor);

					StringReader sbis = null;
					sbis = (StringReader) transfer.getTransferData(molFlavor);

					int x;
					while ((x = sbis.read()) != -1) {
						sb.append((char) x);
					}
					reader = new MDLReader(new StringReader(sb.toString()));
				} catch (final UnsupportedFlavorException e1) {
					e1.printStackTrace();
				} catch (final IOException e1) {
					e1.printStackTrace();
				} catch (final Exception e1) {
					reader = new RGroupQueryReader(new StringReader(
							sb.toString()));
				}

			} else if (supported(transfer, DataFlavor.stringFlavor)) {
				try {
					content = (String) transfer
							.getTransferData(DataFlavor.stringFlavor);
					reader = new ReaderFactory().createReader(new StringReader(
							content));
					// System.out.println(reader.getClass());
				} catch (final Exception e1) {
					e1.printStackTrace();
				}
			}

			// if looks like CML - InputStream required. Reader throws error.
			if (content != null && content.indexOf("cml") > -1) {
				reader = new CMLReader(new ByteArrayInputStream(
						content.getBytes()));
			}

			IAtomContainer toPaste = null;
			boolean rgrpQuery = false;
			if (reader != null) {
				final IAtomContainer readAtomContainer = chemModel.getBuilder()
						.newInstance(IAtomContainer.class);
				try {
					if (reader.accepts(AtomContainer.class)) {
						toPaste = reader.read(readAtomContainer);
					} else if (reader.accepts(ChemFile.class)) {
						toPaste = readAtomContainer;
						final IChemFile file = reader.read(new ChemFile());
						for (final IAtomContainer ac : ChemFileManipulator
								.getAllAtomContainers(file)) {
							toPaste.add(ac);

						}
					} else if (reader.accepts(RGroupQuery.class)) {
						rgrpQuery = true;
						final IRGroupQuery rgroupQuery = reader
								.read(new RGroupQuery());
						chemModel = new ChemModel();
						final RGroupHandler rgHandler = new RGroupHandler(
								rgroupQuery);
						jcpPanel.get2DHub().setRGroupHandler(rgHandler);
						chemModel.setMoleculeSet(rgHandler
								.getMoleculeSet(chemModel));
						rgHandler.layoutRgroup();

					}

				} catch (final CDKException e1) {
					e1.printStackTrace();
				}
			}

			// Attempt SMILES or InChI if no reader is found for content.
			if (rgrpQuery != true && toPaste == null
					&& supported(transfer, DataFlavor.stringFlavor)) {
				try {
					if (content.toLowerCase().indexOf("inchi") > -1) {
						// toPaste = (IAtomContainer) new
						// StdInChIParser().parseInchi(content);
					} else {
						final SmilesParser sp = new SmilesParser(
								DefaultChemObjectBuilder.getInstance());
						toPaste = sp.parseSmiles(((String) transfer
								.getTransferData(DataFlavor.stringFlavor))
								.trim());
						toPaste = new FixBondOrdersTool()
								.kekuliseAromaticRings(toPaste);

						final IAtomContainerSet mols = ConnectivityChecker
								.partitionIntoMolecules(toPaste);
						for (int i = 0; i < mols.getAtomContainerCount(); i++) {
							final StructureDiagramGenerator sdg = new StructureDiagramGenerator(
									mols.getAtomContainer(i));

							sdg.setTemplateHandler(new TemplateHandler(toPaste
									.getBuilder()));
							sdg.generateCoordinates();
						}
						// SMILES parser sets valencies, unset
						for (int i = 0; i < toPaste.getAtomCount(); i++) {
							toPaste.getAtom(i).setValency(null);
						}
					}
				} catch (final Exception ex) {
					jcpPanel.announceError(ex);
					ex.printStackTrace();
				}
			}

			if (toPaste != null || rgrpQuery == true) {
				jcpPanel.getRenderPanel().setZoomWide(true);
				jcpPanel.get2DHub().getRenderer().getRenderer2DModel()
						.setZoomFactor(1);
				if (rgrpQuery == true) {
					jcpPanel.setChemModel(chemModel);
				} else {
					scaleStructure(toPaste);
					insertStructure(toPaste, renderModel);
				}
			} else {
				JOptionPane
						.showMessageDialog(
								jcpPanel,
								GT._("The content you tried to copy could not be read to any known format"),
								GT._("Could not process content"),
								JOptionPane.WARNING_MESSAGE);
			}

		} else if (type.equals("cut")) {
			handleSystemClipboard(sysClip);
			IAtom atomInRange = null;
			IBond bondInRange = null;
			final IChemObject object = getSource(e);
			logger.debug("Source of call: ", object);
			if (object instanceof IAtom) {
				atomInRange = (IAtom) object;
			} else {
				atomInRange = renderModel.getHighlightedAtom();
			}
			if (object instanceof IBond) {
				bondInRange = (IBond) object;
			} else {
				bondInRange = renderModel.getHighlightedBond();
			}
			final IAtomContainer tocopyclone = jcpPanel.getChemModel()
					.getBuilder().newInstance(IAtomContainer.class);
			if (atomInRange != null) {
				tocopyclone.addAtom(atomInRange);
				jcpPanel.get2DHub().removeAtom(atomInRange);
				renderModel.setHighlightedAtom(null);
			} else if (bondInRange != null) {
				tocopyclone.addBond(bondInRange);
				jcpPanel.get2DHub().removeBond(bondInRange);
			} else if (renderModel.getSelection() != null
					&& renderModel.getSelection().getConnectedAtomContainer() != null) {
				final IChemObjectSelection selection = renderModel
						.getSelection();
				final IAtomContainer selected = selection
						.getConnectedAtomContainer();
				tocopyclone.add(selected);
				jcpPanel.get2DHub().deleteFragment(selected);
				renderModel.setSelection(new LogicalSelection(
						LogicalSelection.Type.NONE));
				jcpPanel.get2DHub().updateView();
			}
			if (tocopyclone.getAtomCount() > 0
					|| tocopyclone.getBondCount() > 0) {
				addToClipboard(sysClip, tocopyclone);
			}

		} else if (type.equals("selectAll")) {
			final ControllerHub hub = jcpPanel.get2DHub();
			final IChemObjectSelection allSelection = new LogicalSelection(
					LogicalSelection.Type.ALL);

			allSelection.select(hub.getIChemModel());
			renderModel.setSelection(allSelection);
			final SelectSquareModule succusorModule = new SelectSquareModule(
					hub);
			succusorModule.setID("select");
			final MoveModule newActiveModule = new MoveModule(hub,
					succusorModule);
			newActiveModule.setID("move");
			hub.setActiveDrawModule(newActiveModule);

		} else if (type.equals("selectFromChemObject")) {

			// FIXME: implement for others than Reaction, Atom, Bond
			final IChemObject object = getSource(e);
			if (object instanceof IAtom) {
				final SingleSelection<IAtom> container = new SingleSelection<IAtom>(
						(IAtom) object);
				renderModel.setSelection(container);
			} else if (object instanceof IBond) {
				final SingleSelection<IBond> container = new SingleSelection<IBond>(
						(IBond) object);
				renderModel.setSelection(container);
			} else if (object instanceof IReaction) {
				final IAtomContainer wholeModel = jcpPanel.getChemModel()
						.getBuilder().newInstance(IAtomContainer.class);
				for (final IAtomContainer container : ReactionManipulator
						.getAllAtomContainers((IReaction) object)) {
					wholeModel.add(container);
				}
				final ShapeSelection container = new RectangleSelection();
				for (final IAtom atom : wholeModel.atoms()) {
					container.atoms.add(atom);
				}
				for (final IBond bond : wholeModel.bonds()) {
					container.bonds.add(bond);
				}
				renderModel.setSelection(container);
			} else {
				logger.warn("Cannot select everything in : ", object);
			}
		} else if (type.equals("selectReactionReactants")) {
			final IChemObject object = getSource(e);
			if (object instanceof IReaction) {
				final IReaction reaction = (IReaction) object;
				final IAtomContainer wholeModel = jcpPanel.getChemModel()
						.getBuilder().newInstance(IAtomContainer.class);
				for (final IAtomContainer container : AtomContainerSetManipulator
						.getAllAtomContainers(reaction.getReactants())) {
					wholeModel.add(container);
				}
				final ShapeSelection container = new RectangleSelection();
				for (final IAtom atom : wholeModel.atoms()) {
					container.atoms.add(atom);
				}
				for (final IBond bond : wholeModel.bonds()) {
					container.bonds.add(bond);
				}
				renderModel.setSelection(container);
			} else {
				logger.warn("Cannot select reactants from : ", object);
			}
		} else if (type.equals("selectReactionProducts")) {
			final IChemObject object = getSource(e);
			if (object instanceof IReaction) {
				final IReaction reaction = (IReaction) object;
				final IAtomContainer wholeModel = jcpPanel.getChemModel()
						.getBuilder().newInstance(IAtomContainer.class);
				for (final IAtomContainer container : AtomContainerSetManipulator
						.getAllAtomContainers(reaction.getProducts())) {
					wholeModel.add(container);
				}
				final ShapeSelection container = new RectangleSelection();
				for (final IAtom atom : wholeModel.atoms()) {
					container.atoms.add(atom);
				}
				for (final IBond bond : wholeModel.bonds()) {
					container.bonds.add(bond);
				}
				renderModel.setSelection(container);
			} else {
				logger.warn("Cannot select reactants from : ", object);
			}
		}
		jcpPanel.get2DHub().updateView();
		renderModel.setRecalculationRequiredForSSSR(true);
		jcpPanel.updateStatusBar();

	}

	private void addToClipboard(final Clipboard clipboard,
			final IAtomContainer container) {
		try {
			for (final IBond bond : container.bonds()) {
				if (bond.getAtomCount() < 2
						|| !container.contains(bond.getAtom(0))
						|| !container.contains(bond.getAtom(1))) {
					container.removeBond(bond);
				}
			}
			if (container.getAtomCount() > 0) {
				final JcpSelection jcpselection = new JcpSelection(
						container.clone());
				clipboard.setContents(jcpselection, null);
			}
		} catch (final CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void handleSystemClipboard(final Clipboard clipboard) {
		final Transferable clipboardContent = clipboard.getContents(this);
		final DataFlavor flavors[] = clipboardContent.getTransferDataFlavors();
		String text = "System.clipoard content";
		for (final DataFlavor flavor : flavors) {
			text += "\n\n Name: " + flavor.getHumanPresentableName();
			text += "\n MIME Type: " + flavor.getMimeType();
			text += "\n Class: ";
			final Class cl = flavor.getRepresentationClass();
			if (cl == null) {
				text += "null";
			} else {
				text += cl.getName();
			}
		}
		logger.debug(text);
	}

	/**
	 * Inserts a structure into the panel. It adds Hs if needed and highlights
	 * the structure after insert.
	 * 
	 * @param toPaste
	 *            The structure to paste.
	 * @param renderModel
	 *            The current renderer model.
	 */
	private void insertStructure(final IAtomContainer toPaste,
			final RendererModel renderModel) {

		// add implicit hs
		if (jcpPanel.get2DHub().getController2DModel()
				.getAutoUpdateImplicitHydrogens()) {
			try {
				AtomContainerManipulator
						.percieveAtomTypesAndConfigureAtoms(toPaste);
				final CDKHydrogenAdder hAdder = CDKHydrogenAdder
						.getInstance(toPaste.getBuilder());
				hAdder.addImplicitHydrogens(toPaste);
			} catch (final CDKException ex) {
				ex.printStackTrace();
				// do nothing
			}
			// valencies are set when doing atom typing, which we don't want in
			// jcp
			for (int i = 0; i < toPaste.getAtomCount(); i++) {
				toPaste.getAtom(i).setValency(null);
			}
		}

		// somehow, in case of single atoms, there are no coordinates
		if (toPaste.getAtomCount() == 1
				&& toPaste.getAtom(0).getPoint2d() == null) {
			toPaste.getAtom(0).setPoint2d(new Point2d(0, 0));
		}

		try {
			JChemPaint.generateModel(jcpPanel, toPaste, false, true);
		} catch (final CDKException e) {
			e.printStackTrace();
			return;
		}
		jcpPanel.get2DHub().fireStructureChangedEvent();

		// We select the inserted structure
		final IChemObjectSelection selection = new LogicalSelection(
				LogicalSelection.Type.ALL);
		selection.select(ChemModelManipulator.newChemModel(toPaste));
		renderModel.setSelection(selection);
		final SelectSquareModule successorModule = new SelectSquareModule(
				jcpPanel.get2DHub());
		successorModule.setID("select");
		final MoveModule newActiveModule = new MoveModule(jcpPanel.get2DHub(),
				successorModule);
		newActiveModule.setID("move");
		jcpPanel.get2DHub().setActiveDrawModule(newActiveModule);
	}

	/**
	 * Scale the structure to be pasted to the same scale of the current drawing
	 * 
	 * @param topaste
	 */
	private void scaleStructure(final IAtomContainer topaste) {
		final double bondLengthModel = jcpPanel.get2DHub()
				.calculateAverageBondLength(
						jcpPanel.get2DHub().getIChemModel().getMoleculeSet());
		final double bondLengthInsert = GeometryTools
				.getBondLengthAverage(topaste);
		final double scale = bondLengthModel / bondLengthInsert;
		for (final IAtom atom : topaste.atoms()) {
			if (atom.getPoint2d() != null) {
				atom.setPoint2d(new Point2d(atom.getPoint2d().x * scale, atom
						.getPoint2d().y * scale));
			}
		}
	}

	private boolean supported(final Transferable transfer,
			final DataFlavor flavor) {
		return transfer != null && transfer.isDataFlavorSupported(flavor);
	}
}
