/*
 *  $RCSfile$
 *  $Author: egonw $
 *  $Date: 2007-01-04 17:26:00 +0000 (Thu, 04 Jan 2007) $
 *  $Revision: 7634 $
 *
 *  Copyright (C) 1997-2008 Stefan Kuhn
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
package org.openscience.jchempaint;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.openscience.jchempaint.action.ZoomAction;
import org.openscience.jchempaint.applet.JChemPaintAbstractApplet;
import org.openscience.jchempaint.controller.ControllerHub;
import org.openscience.jchempaint.controller.ControllerModel;
import org.openscience.jchempaint.controller.IControllerModule;
import org.openscience.jchempaint.controller.IViewEventRelay;
import org.openscience.jchempaint.controller.PhantomArrowGenerator;
import org.openscience.jchempaint.controller.PhantomBondGenerator;
import org.openscience.jchempaint.controller.PhantomTextGenerator;
import org.openscience.jchempaint.controller.SwingMouseEventRelay;
import org.openscience.jchempaint.controller.undoredo.IUndoListener;
import org.openscience.jchempaint.controller.undoredo.IUndoRedoable;
import org.openscience.jchempaint.controller.undoredo.UndoRedoHandler;
import org.openscience.jchempaint.renderer.Renderer;
import org.openscience.jchempaint.renderer.font.AWTFontManager;
import org.openscience.jchempaint.renderer.generators.AtomContainerBoundsGenerator;
import org.openscience.jchempaint.renderer.generators.AtomContainerTitleGenerator;
import org.openscience.jchempaint.renderer.generators.BoundsGenerator;
import org.openscience.jchempaint.renderer.generators.ExtendedAtomGenerator;
import org.openscience.jchempaint.renderer.generators.ExternalHighlightAtomGenerator;
import org.openscience.jchempaint.renderer.generators.ExternalHighlightBondGenerator;
import org.openscience.jchempaint.renderer.generators.HighlightAtomGenerator;
import org.openscience.jchempaint.renderer.generators.HighlightBondGenerator;
import org.openscience.jchempaint.renderer.generators.IGenerator;
import org.openscience.jchempaint.renderer.generators.IReactionGenerator;
import org.openscience.jchempaint.renderer.generators.LonePairGenerator;
import org.openscience.jchempaint.renderer.generators.MappingGenerator;
import org.openscience.jchempaint.renderer.generators.MergeAtomsGenerator;
import org.openscience.jchempaint.renderer.generators.ProductsBoxGenerator;
import org.openscience.jchempaint.renderer.generators.RGroupGenerator;
import org.openscience.jchempaint.renderer.generators.RadicalGenerator;
import org.openscience.jchempaint.renderer.generators.ReactantsBoxGenerator;
import org.openscience.jchempaint.renderer.generators.ReactionArrowGenerator;
import org.openscience.jchempaint.renderer.generators.ReactionBoxGenerator;
import org.openscience.jchempaint.renderer.generators.ReactionPlusGenerator;
import org.openscience.jchempaint.renderer.generators.RingGenerator;
import org.openscience.jchempaint.renderer.generators.SelectAtomGenerator;
import org.openscience.jchempaint.renderer.generators.SelectBondGenerator;
import org.openscience.jchempaint.renderer.generators.TooltipGenerator;
import org.openscience.jchempaint.renderer.selection.IChemObjectSelection;
import org.openscience.jchempaint.renderer.visitor.AWTDrawVisitor;
import org.openscience.jchempaint.renderer.visitor.SVGGenerator;
import org.openscience.jchempaint.undoredo.SwingUndoRedoFactory;

public class RenderPanel extends JPanel implements IViewEventRelay,
		IUndoListener {

	protected Renderer					renderer;

	private boolean						isNewChemModel;

	private ControllerHub				hub;

	private ControllerModel				controllerModel;

	private SwingMouseEventRelay		mouseEventRelay;

	private final boolean				fitToScreen;

	private boolean						zoomWide;

	private boolean						shouldPaintFromCache	= false;

	private final UndoManager			undoManager				= new UndoManager();

	private boolean						debug					= false;

	private final PhantomBondGenerator	pbg						= new PhantomBondGenerator();

	private final PhantomArrowGenerator	pag						= new PhantomArrowGenerator();

	private final PhantomTextGenerator	ptg						= new PhantomTextGenerator();

	boolean								isFirstDrawing			= true;

	public RenderPanel(final IChemModel chemModel, final int width,
			final int height, final boolean fitToScreen, final boolean debug,
			final boolean isViewer, final JChemPaintAbstractApplet applet)
			throws IOException {
		this.debug = debug;
		setupMachinery(chemModel, fitToScreen, isViewer, applet);
		setupPanel(width, height);
		this.fitToScreen = fitToScreen;
		final int limit = Integer.parseInt(JCPPropertyHandler.getInstance(true)
				.getJCPProperties().getProperty("General.UndoStackSize"));
		undoManager.setLimit(limit);
	}

	@Override
	public void doUndo(final IUndoRedoable undoredo) {
		undoManager.addEdit((UndoableEdit) undoredo);
		final Container root = getParent().getParent().getParent();
		if (root instanceof JChemPaintPanel) {
			((JChemPaintPanel) root).updateUndoRedoControls();
		}
	}

	public IChemModel getChemModel() {
		return hub.getIChemModel();
	}

	public ControllerHub getHub() {
		return hub;
	}

	public Renderer getRenderer() {
		return renderer;
	}

	/**
	 * Returns one of the status strings at the given position
	 * 
	 * @param position
	 * @return the current status
	 */
	public String getStatus(final int position) {
		String status = "";
		if (position == 0) {
			// depict editing mode
			final IControllerModule activeDrawModule = hub
					.getActiveDrawModule();
			if (activeDrawModule == null) {
				return "";
			} else {
				final String mode = activeDrawModule.getDrawModeString();
				status = JCPMenuTextMaker.getInstance("applet").getText(mode);
			}
		} else if (position == 1) {
			// depict bruto formula
			final IChemModel chemModel = hub.getIChemModel();
			final IAtomContainerSet molecules = chemModel.getMoleculeSet();
			if (molecules != null && molecules.getAtomContainerCount() > 0) {
				final Iterator<IAtomContainer> containers = ChemModelManipulator
						.getAllAtomContainers(chemModel).iterator();
				int implicitHs = 0;
				while (containers.hasNext()) {
					for (final IAtom atom : containers.next().atoms()) {
						if (atom.getImplicitHydrogenCount() != null) {
							implicitHs += atom.getImplicitHydrogenCount();
						}
					}
				}
				status = makeStatusBarString(hub.getFormula(), implicitHs);
			}
		} else if (position == 2) {
			// depict brutto formula of the selected molecule or part of
			// molecule
			final IChemObjectSelection selection = renderer
					.getRenderer2DModel().getSelection();

			if (selection != null) {
				final IAtomContainer ac = selection.getConnectedAtomContainer();
				if (ac != null) {
					int implicitHs = 0;
					for (final IAtom atom : ac.atoms()) {
						if (atom.getImplicitHydrogenCount() != null) {
							implicitHs += atom.getImplicitHydrogenCount();
						}
					}
					final String formula = MolecularFormulaManipulator
							.getHTML(MolecularFormulaManipulator
									.getMolecularFormula(ac), true, false);
					status = makeStatusBarString(formula, implicitHs);
				}
			}
		} else if (position == 3) {
			status = GT._("Zoomfactor")
					+ ": "
					+ NumberFormat.getPercentInstance().format(
							renderer.getRenderer2DModel().getZoomFactor());
		}
		return status;
	}

	public UndoManager getUndoManager() {
		return undoManager;
	}

	protected boolean isValidChemModel(final IChemModel chemModel) {
		return chemModel != null
				&& (chemModel.getMoleculeSet() != null || chemModel
						.getReactionSet() != null);
	}

	private List<IGenerator> makeGenerators(final IChemModel chemModel)
			throws IOException {
		final List<IGenerator> generators = new ArrayList<IGenerator>();
		if (debug) {
			generators.add(new AtomContainerBoundsGenerator());
		}
		generators.add(new RGroupGenerator());
		generators.add(new RingGenerator());
		generators.add(new ExtendedAtomGenerator());
		generators.add(new LonePairGenerator());
		generators.add(new RadicalGenerator());
		generators.add(new ExternalHighlightAtomGenerator());
		generators.add(new ExternalHighlightBondGenerator());
		generators.add(new HighlightAtomGenerator());
		generators.add(new HighlightBondGenerator());
		generators.add(new SelectAtomGenerator());
		generators.add(new SelectBondGenerator());
		generators.add(new MergeAtomsGenerator());
		generators.add(new AtomContainerTitleGenerator());
		generators.add(new TooltipGenerator());
		generators.add(pbg);
		generators.add(pag);
		generators.add(ptg);
		return generators;
	}

	private List<IReactionGenerator> makeReactionGenerators() {
		final List<IReactionGenerator> generators = new ArrayList<IReactionGenerator>();
		// generate the bounds first, so that they are to the back
		if (debug) {
			generators.add(new BoundsGenerator());
		}
		generators.add(new ReactionBoxGenerator());
		generators.add(new ReactionArrowGenerator());
		generators.add(new ReactionPlusGenerator());
		generators.add(new ReactantsBoxGenerator());
		generators.add(new ProductsBoxGenerator());
		generators.add(new MappingGenerator());
		return generators;
	}

	private String makeStatusBarString(final String formula,
			final int implicitHs) {
		final DecimalFormat df1 = new DecimalFormat("####.0000");
		return "<html>"
				+ formula
				+ (implicitHs == 0 ? "" : " ( " + implicitHs + " "
						+ GT._("Hs implicit") + ")") + "</html>";
	}

	@Override
	public void paint(final Graphics g) {
		setBackground(renderer.getRenderer2DModel().getBackColor());
		super.paint(g);

		final Graphics2D g2 = (Graphics2D) g;

		if (shouldPaintFromCache) {
			renderer.repaint(new AWTDrawVisitor(g2));
		} else {
			final IChemModel chemModel = hub.getIChemModel();

			if (!isValidChemModel(chemModel)) {
				return;
			}

			/*
			 * It is more correct to use a rectangle starting at (0,0) than to
			 * use getBounds() as the RenderPanel may be a child of some
			 * container window, and its Graphics will be translated relative to
			 * its parent.
			 */
			final Rectangle screen = new Rectangle(0, 0,
					getParent().getWidth() - 20, getParent().getHeight() - 20);

			if (renderer.getRenderer2DModel().isFitToScreen() || zoomWide) {
				paintChemModelFitToScreen(chemModel, g2, screen);
				if (zoomWide) {
					zoomWide = false;
				}

			} else {
				paintChemModel(chemModel, g2, screen);
			}
		}
		// for some reason, the first drawing of a string takes around 0.5
		// seconds.
		// If the user experiences this delay, it's annoying, so we do a dummy
		// draw.
		if (isFirstDrawing && isVisible()) {
			g.setFont(((AWTFontManager) renderer.getFontManager()).getFont());
			g.setColor(getBackground());
			g.drawString("Black", 100, 100);
			isFirstDrawing = false;
		}
	}

	/**
	 * Paint the chem model not fit-to-screen
	 * 
	 * @param chemModel
	 * @param g
	 * @param screen
	 */
	private void paintChemModel(

	final IChemModel chemModel, final Graphics2D g, final Rectangle screen) {

		if (isNewChemModel) {
			renderer.setup(chemModel, screen);
		}

		final Rectangle diagram = renderer.calculateDiagramBounds(chemModel);
		isNewChemModel = false;

		shouldPaintFromCache = false;

		// determine the size the canvas needs to be to fit the model
		if (diagram != null) {
			final Rectangle result = shift(screen, diagram);

			// this makes sure the toolbars get drawn
			setPreferredSize(new Dimension(result.width, result.height));
			revalidate();
			renderer.paintChemModel(chemModel, new AWTDrawVisitor(g));

		}
	}

	private void paintChemModelFitToScreen(final IChemModel chemModel,
			final Graphics2D g, final Rectangle screen) {

		renderer.paintChemModel(chemModel, new AWTDrawVisitor(g), screen, true);

	}

	public void setChemModel(final IChemModel model) {
		hub.setChemModel(model);
	}

	public void setFitToScreen(final boolean fitToScreen) {
		renderer.getRenderer2DModel().setFitToScreen(fitToScreen);
	}

	public void setIsNewChemModel(final boolean isNewChemModel) {
		this.isNewChemModel = isNewChemModel;
	}

	private void setupMachinery(final IChemModel chemModel,
			final boolean fitToScreen, final boolean isViewer,
			final JChemPaintAbstractApplet applet) throws IOException {
		// setup the Renderer and the controller 'model'

		if (renderer == null) {
			renderer = new Renderer(makeGenerators(chemModel),
					makeReactionGenerators(), new AWTFontManager(), this, true);
			// any specific rendering settings defaults that overwrite user
			// settings should go here
			// this.renderer.getRenderer2DModel().setShowEndCarbons(false);
			// this.renderer.getRenderer2DModel().setShowAromaticity(false);
		}
		setFitToScreen(fitToScreen);
		controllerModel = new ControllerModel();

		final UndoRedoHandler undoredohandler = new UndoRedoHandler();
		undoredohandler.addIUndoListener(this);
		// connect the Renderer to the Hub
		hub = new ControllerHub(controllerModel, renderer, chemModel, this,
				undoredohandler, new SwingUndoRedoFactory(), isViewer, applet);
		pbg.setControllerHub(hub);
		pag.setControllerHub(hub);
		ptg.setControllerHub(hub);

		// connect mouse events from Panel to the Hub
		mouseEventRelay = new SwingMouseEventRelay(hub);
		addMouseListener(mouseEventRelay);
		addMouseMotionListener(mouseEventRelay);
		addMouseWheelListener(mouseEventRelay);
		isNewChemModel = true;
	}

	private void setupPanel(final int width, final int height) {
		setBackground(renderer.getRenderer2DModel().getBackColor());
		setPreferredSize(new Dimension(width, height));
	}

	public void setZoomWide(final boolean zoomWide) {
		this.zoomWide = zoomWide;
	}

	public Rectangle shift(final Rectangle screenBounds,
			final Rectangle diagramBounds) {

		final int LABEL_MARGIN = 50; // prevents text or labels from dropping
										// off screen
		final int screenMaxX = screenBounds.x + screenBounds.width
				- LABEL_MARGIN;
		final int screenMaxY = screenBounds.y + screenBounds.height
				- LABEL_MARGIN;
		final int diagramMaxX = diagramBounds.x + diagramBounds.width;
		final int diagramMaxY = diagramBounds.y + diagramBounds.height;
		final int leftOverlap = screenBounds.x - diagramBounds.x;
		final int rightOverlap = diagramMaxX - screenMaxX;
		final int topOverlap = screenBounds.y - diagramBounds.y;
		final int bottomOverlap = diagramMaxY - screenMaxY;

		int dx = 0;
		int dy = 0;
		int w = screenBounds.width;
		int h = screenBounds.height;

		if (leftOverlap > 0) {
			dx = leftOverlap;
		}

		if (rightOverlap > 0) {
			w += rightOverlap;
		}

		if (topOverlap > 0) {
			dy = topOverlap;
		}

		if (bottomOverlap > 0) {
			h += bottomOverlap;
		}

		if (dx != 0 || dy != 0) {
			// System.out.println("shifting "+dx+" "+dy);
			renderer.shiftDrawCenter(dx, dy);
		}

		else {
			int dxShiftBack = 0, dyShiftBack = 0;
			if (diagramBounds.x > screenMaxX / 3) {
				/* prevent drifting off horizontally */
				dxShiftBack = -1 * (diagramBounds.x - screenMaxX / 3);
			}
			if (diagramBounds.y > screenMaxY / 3) {
				/* prevent drifting off vertically ! */
				dyShiftBack = -1 * (diagramBounds.y - screenMaxY / 3);
			}

			if (dxShiftBack != 0 || dyShiftBack != 0) {

				if (ZoomAction.zoomDone) {
					ZoomAction.zoomDone = false;
					renderer.shiftDrawCenter(dxShiftBack, dyShiftBack);
					// System.out.println("shifting back");
				}
			}
		}

		return new Rectangle(dx, dy, w, h);
	}

	public Image takeSnapshot() {
		final IChemModel chemModel = hub.getIChemModel();
		if (isValidChemModel(chemModel)) {
			final Rectangle2D modelBounds = Renderer.calculateBounds(chemModel);
			final Rectangle bounds = renderer
					.calculateScreenBounds(modelBounds);
			bounds.height *= 1.1;
			bounds.width *= 1.1;
			final Image image = GraphicsEnvironment
					.getLocalGraphicsEnvironment().getScreenDevices()[0]
					.getDefaultConfiguration().createCompatibleImage(
							bounds.width, bounds.height);
			final Graphics2D g = (Graphics2D) image.getGraphics();
			takeSnapshot(g, chemModel, bounds, modelBounds);
			return image;
		} else {
			return null;
		}
	}

	public void takeSnapshot(final Graphics2D g) {
		final IChemModel chemModel = hub.getIChemModel();
		final Rectangle2D modelBounds = Renderer.calculateBounds(chemModel);
		final Rectangle bounds = renderer.calculateScreenBounds(modelBounds);
		this.takeSnapshot(g, hub.getIChemModel(), bounds, modelBounds);
	}

	public void takeSnapshot(final Graphics2D g, final IChemModel chemModel,
			final Rectangle s, final Rectangle2D m) {
		g.setColor(renderer.getRenderer2DModel().getBackColor());
		g.fillRect(0, 0, s.width, s.height);

		renderer.setDrawCenter(s.getWidth() / 2, s.getHeight() / 2);
		renderer.setModelCenter(m.getCenterX(), m.getCenterY());

		renderer.paintChemModel(chemModel, new AWTDrawVisitor(g));
	}

	public String toSVG() {
		final IChemModel chemModel = hub.getIChemModel();
		if (chemModel != null && chemModel.getMoleculeSet() != null) {
			final SVGGenerator svgGenerator = new SVGGenerator();
			renderer.paintChemModel(chemModel, svgGenerator);
			return svgGenerator.getResult();
		} else {
			return "<svg></svg>";
		}
	}

	@Override
	public void update(final Graphics g) {
		// System.out.println("renderpanel update");
		paint(g);
	}

	@Override
	public void updateView() {
		/*
		 * updateView should only be called in a ControllerModule where we
		 * assume that things have changed so we can't use the cache
		 */
		shouldPaintFromCache = false;
		this.repaint();
	}
}
