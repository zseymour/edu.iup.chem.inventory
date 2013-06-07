/*  Copyright (C) 2008-2009  Gilleain Torrance <gilleain.torrance@gmail.com>
 *                2008-2009  Arvid Berg <goglepox@users.sf.net>
 *                     2009  Stefan Kuhn <shk3@users.sf.net>
 *                     2009  Egon Willighagen <egonw@users.sf.net>
 *
 *  Contact: cdk-devel@list.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
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
package org.openscience.jchempaint.renderer;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.jchempaint.RenderPanel;
import org.openscience.jchempaint.renderer.elements.ElementGroup;
import org.openscience.jchempaint.renderer.elements.IRenderingElement;
import org.openscience.jchempaint.renderer.font.IFontManager;
import org.openscience.jchempaint.renderer.generators.IGenerator;
import org.openscience.jchempaint.renderer.generators.IReactionGenerator;
import org.openscience.jchempaint.renderer.generators.IReactionSetGenerator;
import org.openscience.jchempaint.renderer.visitor.IDrawVisitor;

/**
 * A general renderer for {@link IChemModel}s, {@link IReaction}s, and
 * {@link IAtomContainer}s. The chem object is converted into a 'diagram' made
 * up of {@link IRenderingElement}s. It takes an {@link IDrawVisitor} to do the
 * drawing of the generated diagram. Various display properties can be set using
 * the {@link RendererModel}.
 * <p>
 * 
 * This class has several usage patterns. For just painting fit-to-screen do:
 * 
 * <pre>
 * renderer.paintMolecule(molecule, visitor, drawArea)
 * </pre>
 * 
 * for painting at a scale determined by the bond length in the RendererModel:
 * 
 * <pre>
 * if (moleculeIsNew) {
 * 	renderer.setup(molecule, drawArea);
 * }
 * Rectangle diagramSize = renderer.paintMolecule(molecule, visitor);
 * // ...update scroll bars here
 * </pre>
 * 
 * to paint at full screen size, but not resize with each change:
 * 
 * <pre>
 * if (moleculeIsNew) {
 * 	renderer.setScale(molecule);
 * 	Rectangle diagramBounds = renderer.calculateDiagramBounds(molecule);
 * 	renderer.setZoomToFit(diagramBounds, drawArea);
 * 	renderer.paintMolecule(molecule, visitor);
 * } else {
 * 	Rectangle diagramSize = renderer.paintMolecule(molecule, visitor);
 * 	// ...update scroll bars here
 * }
 * </pre>
 * 
 * finally, if you are scrolling, and have not changed the diagram:
 * 
 * <pre>
 * renderer.repaint(visitor)
 * </pre>
 * 
 * will just repaint the previously generated diagram, at the same scale.
 * <p>
 * 
 * There are two sets of methods for painting IChemObjects - those that take a
 * Rectangle that represents the desired draw area, and those that return a
 * Rectangle that represents the actual draw area. The first are intended for
 * drawing molecules fitted to the screen (where 'screen' means any drawing
 * area) while the second type of method are for drawing bonds at the length
 * defined by the {@link RendererModel} parameter bondLength.
 * <p>
 * 
 * There are two numbers used to transform the model so that it fits on screen.
 * The first is <tt>scale</tt>, which is used to map model coordinates to screen
 * coordinates. The second is <tt>zoom</tt> which is used to, well, zoom the on
 * screen coordinates. If the diagram is fit-to-screen, then the ratio of the
 * bounds when drawn using bondLength and the bounds of the screen is used as
 * the zoom.
 * <p>
 * 
 * So, if the bond length on screen is set to 40, and the average bond length of
 * the model is 2 (unitless, but roughly &Aring;ngstrom scale) then the scale
 * will be 20. If the model is 10 units wide, then the diagram drawn at 100%
 * zoom will be 10 * 20 = 200 in width on screen. If the screen is 400 pixels
 * wide, then fitting it to the screen will make the zoom 200%. Since the zoom
 * is just a floating point number, 100% = 1 and 200% = 2.
 * 
 * @author maclean
 * @cdk.module renderextra
 */
public class Renderer extends AtomContainerRenderer implements IRenderer {

	public static double calculateAverageBondLength(
			final IAtomContainerSet moleculeSet) {
		double averageBondModelLength = 0.0;
		for (final IAtomContainer atomContainer : moleculeSet.atomContainers()) {
			averageBondModelLength += GeometryTools
					.getBondLengthAverage(atomContainer);
		}
		return averageBondModelLength / moleculeSet.getAtomContainerCount();
	}

	/**
	 * 
	 * 
	 * @param model
	 *            the model for which to calculate the average bond length
	 */
	public static double calculateAverageBondLength(final IChemModel model) {

		// empty models have to have a scale
		final IAtomContainerSet moleculeSet = model.getMoleculeSet();
		if (moleculeSet == null) {
			final IReactionSet reactionSet = model.getReactionSet();
			if (reactionSet != null) {
				return Renderer.calculateAverageBondLength(reactionSet);
			}
			return 0.0;
		}

		return Renderer.calculateAverageBondLength(moleculeSet);
	}

	public static double calculateAverageBondLength(final IReaction reaction) {

		final IAtomContainerSet reactants = reaction.getReactants();
		double reactantAverage = 0.0;
		if (reactants != null) {
			reactantAverage = Renderer.calculateAverageBondLength(reactants)
					/ reactants.getAtomContainerCount();
		}

		final IAtomContainerSet products = reaction.getProducts();
		double productAverage = 0.0;
		if (products != null) {
			productAverage = Renderer.calculateAverageBondLength(products)
					/ products.getAtomContainerCount();
		}

		if (productAverage == 0.0 && reactantAverage == 0.0) {
			return 1.0;
		} else {
			return (productAverage + reactantAverage) / 2.0;
		}
	}

	public static double calculateAverageBondLength(
			final IReactionSet reactionSet) {
		double averageBondModelLength = 0.0;
		for (final IReaction reaction : reactionSet.reactions()) {
			averageBondModelLength += Renderer
					.calculateAverageBondLength(reaction);
		}
		return averageBondModelLength / reactionSet.getReactionCount();
	}

	public static Rectangle2D calculateBounds(
			final IAtomContainerSet moleculeSet) {
		Rectangle2D totalBounds = null;
		for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
			final IAtomContainer ac = moleculeSet.getAtomContainer(i);
			final Rectangle2D acBounds = Renderer.calculateBounds(ac);
			if (totalBounds == null) {
				totalBounds = acBounds;
			} else {
				Rectangle2D.union(totalBounds, acBounds, totalBounds);
			}
		}
		return totalBounds;
	}

	public static Rectangle2D calculateBounds(final IChemModel chemModel) {
		final IAtomContainerSet moleculeSet = chemModel.getMoleculeSet();
		final IReactionSet reactionSet = chemModel.getReactionSet();
		Rectangle2D totalBounds = null;
		if (moleculeSet != null) {
			totalBounds = Renderer.calculateBounds(moleculeSet);
		}

		if (reactionSet != null) {
			if (totalBounds == null) {
				totalBounds = Renderer.calculateBounds(reactionSet);
			} else {
				totalBounds = totalBounds.createUnion(Renderer
						.calculateBounds(reactionSet));
			}
		}
		return totalBounds;
	}

	public static Rectangle2D calculateBounds(final IReaction reaction) {
		// get the participants in the reaction
		final IAtomContainerSet reactants = reaction.getReactants();
		final IAtomContainerSet products = reaction.getProducts();
		if (reactants == null || products == null) {
			return null;
		}

		// determine the bounds of everything in the reaction
		if (reaction.getProducts().getAtomContainerCount() > 0) {
			final Rectangle2D reactantsBounds = Renderer
					.calculateBounds(products);
			if (reaction.getReactantCount() > 0) {
				return reactantsBounds.createUnion(Renderer
						.calculateBounds(reactants));
			} else {
				return reactantsBounds;
			}
		} else if (reaction.getReactantCount() > 0) {
			return Renderer.calculateBounds(reactants);
		} else {
			return null;
		}
	}

	public static Rectangle2D calculateBounds(final IReactionSet reactionSet) {
		Rectangle2D totalBounds = new Rectangle2D.Double();
		for (final IReaction reaction : reactionSet.reactions()) {
			final Rectangle2D reactionBounds = Renderer
					.calculateBounds(reaction);
			if (totalBounds.isEmpty()) {
				totalBounds = reactionBounds;
			} else {
				Rectangle2D.union(totalBounds, reactionBounds, totalBounds);
			}
		}
		return totalBounds;
	}

	/**
	 * Generators specific to reactions
	 */
	private List<IReactionGenerator>	reactionGenerators;

	private List<IReactionSetGenerator>	reactionSetGenerators;

	/**
	 * A renderer that generates diagrams using the specified generators and
	 * manages fonts with the supplied font manager.
	 * 
	 * @param generators
	 *            a list of classes that implement the IGenerator interface
	 * @param fontManager
	 *            a class that manages mappings between zoom and font sizes
	 * @param useUserSettings
	 *            Should user setting (in $HOME/.jchempaint/properties) be used
	 *            or not?
	 */
	public Renderer(final List<IGenerator> generators,
			final IFontManager fontManager, final boolean useUserSettings) {
		super(generators, fontManager, useUserSettings);
	}

	public Renderer(final List<IGenerator> generators,
			final List<IReactionGenerator> reactionGenerators,
			final IFontManager fontManager, final RenderPanel renderPanel,
			final boolean useUserSettings) {
		this(generators, fontManager, useUserSettings);
		this.reactionGenerators = reactionGenerators;
		reactionSetGenerators = new ArrayList<IReactionSetGenerator>();
		this.setup();
		super.renderPanel = renderPanel;
	}

	/**
	 * 
	 * @param generator
	 */
	public void addGenerator(final IGenerator generator) {
		generators.add(generator);
	}

	/**
	 * 
	 * @param reactionGenerator
	 */
	public void addReactionGenerator(final IReactionGenerator reactionGenerator) {
		reactionGenerators.add(reactionGenerator);
	}

	/**
	 * 
	 * @param reactionSetGenerator
	 */
	public void addReactionSetGenerator(
			final IReactionSetGenerator reactionSetGenerator) {
		reactionSetGenerators.add(reactionSetGenerator);
	}

	public Rectangle calculateDiagramBounds(final IAtomContainerSet moleculeSet) {
		return calculateScreenBounds(Renderer.calculateBounds(moleculeSet));
	}

	/**
	 * Given a chem model, calculates the bounding rectangle in screen space.
	 * 
	 * @param model
	 *            the model to draw.
	 * @return a rectangle in screen space.
	 */
	public Rectangle calculateDiagramBounds(final IChemModel model) {
		final IAtomContainerSet moleculeSet = model.getMoleculeSet();
		final IReactionSet reactionSet = model.getReactionSet();
		if (moleculeSet == null && reactionSet == null) {
			return new Rectangle();
		}

		Rectangle2D moleculeBounds = null;
		Rectangle2D reactionBounds = null;
		if (moleculeSet != null) {
			moleculeBounds = Renderer.calculateBounds(moleculeSet);
		}
		if (reactionSet != null) {
			reactionBounds = Renderer.calculateBounds(reactionSet);
		}

		if (moleculeBounds == null && reactionBounds == null) {
			return new Rectangle();
		}
		if (moleculeBounds == null) {
			return calculateScreenBounds(reactionBounds);
		} else if (reactionBounds == null) {
			return calculateScreenBounds(moleculeBounds);
		} else {
			final Rectangle2D allbounds = new Rectangle2D.Double();
			Rectangle2D.union(moleculeBounds, reactionBounds, allbounds);
			return calculateScreenBounds(allbounds);
		}
	}

	public Rectangle calculateDiagramBounds(final IReaction reaction) {
		return calculateScreenBounds(Renderer.calculateBounds(reaction));
	}

	public Rectangle calculateDiagramBounds(final IReactionSet reactionSet) {
		return calculateScreenBounds(Renderer.calculateBounds(reactionSet));
	}

	/**
	 * Given a bond length for a model, calculate the scale that will transform
	 * this length to the on screen bond length in RendererModel.
	 * 
	 * @param modelBondLength
	 * @param reset
	 * @return
	 */
	private double calculateScaleForBondLength(final double modelBondLength) {
		if (Double.isNaN(modelBondLength) || modelBondLength == 0) {
			return Renderer.DEFAULT_SCALE;
		} else {
			return rendererModel.getBondLength() / modelBondLength;
		}
	}

	/**
	 * Calculate the bounds of the diagram on screen, given the current scale,
	 * zoom, and margin.
	 * 
	 * @param modelBounds
	 *            the bounds in model space of the chem object
	 * @return the bounds in screen space of the drawn diagram
	 */
	private Rectangle convertToDiagramBounds(final Rectangle2D modelBounds) {
		final double cx = modelBounds.getCenterX();
		final double cy = modelBounds.getCenterY();
		final double mw = modelBounds.getWidth();
		final double mh = modelBounds.getHeight();

		final Point2d mc = toScreenCoordinates(cx, cy);

		// special case for 0 or 1 atoms
		if (mw == 0 && mh == 0) {
			return new Rectangle((int) mc.x, (int) mc.y, 0, 0);
		}

		final double margin = rendererModel.getMargin();
		final int w = (int) (scale * zoom * mw + 2 * margin);
		final int h = (int) (scale * zoom * mh + 2 * margin);
		final int x = (int) (mc.x - w / 2);
		final int y = (int) (mc.y - h / 2);

		return new Rectangle(x, y, w, h);
	}

	private IRenderingElement generateDiagram(
			final IAtomContainerSet moleculeSet) {
		final ElementGroup diagram = new ElementGroup();
		for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
			final IAtomContainer ac = moleculeSet.getAtomContainer(i);
			for (final IGenerator generator : generators) {
				diagram.add(generator.generate(ac, rendererModel));
			}
		}
		return diagram;
	}

	private IRenderingElement generateDiagram(final IReaction reaction) {
		final ElementGroup diagram = new ElementGroup();

		for (final IReactionGenerator generator : reactionGenerators) {
			diagram.add(generator.generate(reaction, rendererModel));
		}

		diagram.add(generateDiagram(reaction.getReactants()));
		diagram.add(generateDiagram(reaction.getProducts()));

		return diagram;
	}

	private IRenderingElement generateDiagram(final IReactionSet reactionSet) {
		final ElementGroup diagram = new ElementGroup();

		for (final IReactionSetGenerator generator : reactionSetGenerators) {
			diagram.add(generator.generate(reactionSet, rendererModel));
		}
		return diagram;
	}

	@Override
	public Point2d getDrawCenter() {
		return drawCenter;
	}

	/**
	 * Return the list of generators for the Renderer
	 * 
	 * @return
	 */
	@Override
	public List<IGenerator> getGenerators() {
		return new ArrayList<IGenerator>(generators);
	}

	@Override
	public Point2d getModelCenter() {
		return modelCenter;
	}

	@Override
	public RendererModel getRenderer2DModel() {
		return rendererModel;
	}

	/**
	 * The target method for paintChemModel, paintReaction, and paintMolecule.
	 * 
	 * @param drawVisitor
	 *            the visitor to draw with
	 * @param diagram
	 *            the IRenderingElement tree to render
	 */
	private void paint(final IDrawVisitor drawVisitor,
			final IRenderingElement diagram) {
		if (diagram == null) {
			return;
		}

		// cache the diagram for quick-redraw
		cachedDiagram = diagram;

		fontManager.setFontName(rendererModel.getFontName());
		fontManager.setFontStyle(rendererModel.getFontStyle());

		drawVisitor.setFontManager(fontManager);
		drawVisitor.setTransform(transform);
		drawVisitor.setRendererModel(rendererModel);
		diagram.accept(drawVisitor);
	}

	/**
	 * Paint an IChemModel using the IDrawVisitor at a scale determined by the
	 * bond length in RendererModel.
	 * 
	 * @param chemModel
	 *            the chem model to draw
	 * @param drawVisitor
	 *            the visitor used to draw with
	 * @return the rectangular area that the diagram will occupy on screen
	 */
	public Rectangle paintChemModel(final IChemModel chemModel,
			final IDrawVisitor drawVisitor) {

		final IAtomContainerSet moleculeSet = chemModel.getMoleculeSet();
		final IReactionSet reactionSet = chemModel.getReactionSet();

		if (moleculeSet == null && reactionSet != null) {
			return paintReactionSet(reactionSet, drawVisitor);
		}

		if (moleculeSet != null && reactionSet == null) {
			return paintMoleculeSet(moleculeSet, drawVisitor);
		}

		if (moleculeSet != null && moleculeSet.getAtomContainerCount() > 0
				&& reactionSet != null) {
			Rectangle2D totalBounds = Renderer.calculateBounds(reactionSet);
			totalBounds = totalBounds.createUnion(Renderer
					.calculateBounds(moleculeSet));
			setupTransformNatural(totalBounds);
			final ElementGroup diagram = new ElementGroup();
			for (final IReaction reaction : reactionSet.reactions()) {
				diagram.add(this.generateDiagram(reaction));
			}
			diagram.add(this.generateDiagram(moleculeSet));
			diagram.add(this.generateDiagram(reactionSet));
			paint(drawVisitor, diagram);

			// the size of the painted diagram is returned
			return convertToDiagramBounds(totalBounds);
		}
		return new Rectangle(0, 0, 0, 0);
	}

	/**
	 * Paint a ChemModel.
	 * 
	 * @param chemModel
	 * @param drawVisitor
	 *            the visitor that does the drawing
	 * @param bounds
	 *            the bounds of the area to paint on.
	 * @param resetCenter
	 *            if true, set the modelCenter to the center of the ChemModel's
	 *            bounds.
	 */
	public void paintChemModel(final IChemModel chemModel,
			final IDrawVisitor drawVisitor, final Rectangle2D bounds,
			final boolean resetCenter) {
		// check for an empty model
		final IAtomContainerSet moleculeSet = chemModel.getMoleculeSet();
		final IReactionSet reactionSet = chemModel.getReactionSet();

		// nasty, but it seems that reactions can be read in as ChemModels
		// with BOTH a ReactionSet AND a MoleculeSet...
		if (moleculeSet == null || reactionSet != null) {
			if (reactionSet != null) {
				paintReactionSet(reactionSet, drawVisitor, bounds, resetCenter);
			}
			return;
		}

		// calculate the total bounding box
		final Rectangle2D modelBounds = Renderer.calculateBounds(moleculeSet);

		setupTransformToFit(bounds, modelBounds,
				Renderer.calculateAverageBondLength(chemModel), resetCenter);

		// generate the elements
		final IRenderingElement diagram = this.generateDiagram(moleculeSet);

		// paint it
		paint(drawVisitor, diagram);
	}

	public Rectangle paintMoleculeSet(final IAtomContainerSet moleculeSet,
			final IDrawVisitor drawVisitor) {
		// total up the bounding boxes
		Rectangle2D totalBounds = new Rectangle2D.Double();
		for (final IAtomContainer molecule : moleculeSet.atomContainers()) {
			final Rectangle2D modelBounds = Renderer.calculateBounds(molecule);
			if (totalBounds == null) {
				totalBounds = modelBounds;
			} else {
				totalBounds = totalBounds.createUnion(modelBounds);
			}
		}

		// setup and draw
		setupTransformNatural(totalBounds);
		final ElementGroup diagram = new ElementGroup();
		for (final IAtomContainer molecule : moleculeSet.atomContainers()) {
			diagram.add(this.generateDiagram(molecule));
		}
		paint(drawVisitor, diagram);

		return convertToDiagramBounds(totalBounds);
	}

	/**
	 * Paint a set of molecules.
	 * 
	 * @param reaction
	 *            the reaction to paint
	 * @param drawVisitor
	 *            the visitor that does the drawing
	 * @param bounds
	 *            the bounds on the screen
	 * @param resetCenter
	 *            if true, set the draw center to be the center of bounds
	 */
	public void paintMoleculeSet(final IAtomContainerSet molecules,
			final IDrawVisitor drawVisitor, final Rectangle2D bounds,
			final boolean resetCenter) {

		// total up the bounding boxes
		Rectangle2D totalBounds = null;
		for (final IAtomContainer molecule : molecules.atomContainers()) {
			final Rectangle2D modelBounds = Renderer.calculateBounds(molecule);
			if (totalBounds == null) {
				totalBounds = modelBounds;
			} else {
				totalBounds = totalBounds.createUnion(modelBounds);
			}
		}

		setupTransformToFit(bounds, totalBounds,
				Renderer.calculateAverageBondLength(molecules), resetCenter);

		final ElementGroup diagram = new ElementGroup();
		for (final IAtomContainer molecule : molecules.atomContainers()) {
			diagram.add(this.generateDiagram(molecule));
		}

		paint(drawVisitor, diagram);
	}

	public Rectangle paintReaction(final IReaction reaction,
			final IDrawVisitor drawVisitor) {

		// calculate the bounds
		final Rectangle2D modelBounds = Renderer.calculateBounds(reaction);

		// setup and draw
		setupTransformNatural(modelBounds);
		final IRenderingElement diagram = this.generateDiagram(reaction);
		paint(drawVisitor, diagram);

		return convertToDiagramBounds(modelBounds);
	}

	/**
	 * Paint a reaction.
	 * 
	 * @param reaction
	 *            the reaction to paint
	 * @param drawVisitor
	 *            the visitor that does the drawing
	 * @param bounds
	 *            the bounds on the screen
	 * @param resetCenter
	 *            if true, set the draw center to be the center of bounds
	 */
	public void paintReaction(final IReaction reaction,
			final IDrawVisitor drawVisitor, final Rectangle2D bounds,
			final boolean resetCenter) {

		// calculate the bounds
		final Rectangle2D modelBounds = Renderer.calculateBounds(reaction);

		setupTransformToFit(bounds, modelBounds,
				Renderer.calculateAverageBondLength(reaction), resetCenter);

		// generate the elements
		final IRenderingElement diagram = this.generateDiagram(reaction);

		// paint it
		paint(drawVisitor, diagram);
	}

	public Rectangle paintReactionSet(final IReactionSet reactionSet,
			final IDrawVisitor drawVisitor) {
		// total up the bounding boxes
		Rectangle2D totalBounds = new Rectangle2D.Double();
		for (final IReaction reaction : reactionSet.reactions()) {
			final Rectangle2D modelBounds = Renderer.calculateBounds(reaction);
			if (totalBounds == null) {
				totalBounds = modelBounds;
			} else {
				totalBounds = totalBounds.createUnion(modelBounds);
			}
		}

		// setup and draw
		setupTransformNatural(totalBounds);
		final ElementGroup diagram = new ElementGroup();
		for (final IReaction reaction : reactionSet.reactions()) {
			diagram.add(this.generateDiagram(reaction));
		}
		diagram.add(this.generateDiagram(reactionSet));
		paint(drawVisitor, diagram);

		// the size of the painted diagram is returned
		return convertToDiagramBounds(totalBounds);
	}

	/**
	 * Paint a set of reactions.
	 * 
	 * @param reaction
	 *            the reaction to paint
	 * @param drawVisitor
	 *            the visitor that does the drawing
	 * @param bounds
	 *            the bounds on the screen
	 * @param resetCenter
	 *            if true, set the draw center to be the center of bounds
	 */
	public void paintReactionSet(final IReactionSet reactionSet,
			final IDrawVisitor drawVisitor, final Rectangle2D bounds,
			final boolean resetCenter) {

		// total up the bounding boxes
		Rectangle2D totalBounds = null;
		for (final IReaction reaction : reactionSet.reactions()) {
			final Rectangle2D modelBounds = Renderer.calculateBounds(reaction);
			if (totalBounds == null) {
				totalBounds = modelBounds;
			} else {
				totalBounds = totalBounds.createUnion(modelBounds);
			}
		}

		setupTransformToFit(bounds, totalBounds,
				Renderer.calculateAverageBondLength(reactionSet), resetCenter);

		final ElementGroup diagram = new ElementGroup();
		for (final IReaction reaction : reactionSet.reactions()) {
			diagram.add(this.generateDiagram(reaction));
		}
		diagram.add(this.generateDiagram(reactionSet));

		// paint them all
		paint(drawVisitor, diagram);
	}

	/**
	 * Repaint using the cached diagram
	 * 
	 * @param drawVisitor
	 *            the wrapper for the graphics object that draws
	 */
	@Override
	public void repaint(final IDrawVisitor drawVisitor) {
		paint(drawVisitor, cachedDiagram);
	}

	@Override
	public void reset() {
		modelCenter = new Point2d(0, 0);
		drawCenter = new Point2d(200, 200);
		zoom = 1.0;
		setup();
	}

	@Override
	public void setDrawCenter(final double x, final double y) {
		drawCenter = new Point2d(x, y);
		setup();
	}

	@Override
	public void setModelCenter(final double x, final double y) {
		modelCenter = new Point2d(x, y);
		setup();
	}

	/**
	 * Set the scale for an IAtomContainerSet. It calculates the average bond
	 * length of the model and calculates the multiplication factor to transform
	 * this to the bond length that is set in the RendererModel.
	 * 
	 * @param moleculeSet
	 */
	public void setScale(final IAtomContainerSet moleculeSet) {
		final double bondLength = Renderer
				.calculateAverageBondLength(moleculeSet);
		scale = calculateScaleForBondLength(bondLength);

		// store the scale so that other components can access it
		rendererModel.setScale(scale);
	}

	/**
	 * Set the scale for an IChemModel. It calculates the average bond length of
	 * the model and calculates the multiplication factor to transform this to
	 * the bond length that is set in the RendererModel.
	 * 
	 * @param chemModel
	 */
	public void setScale(final IChemModel chemModel) {
		final double bondLength = Renderer
				.calculateAverageBondLength(chemModel);
		scale = calculateScaleForBondLength(bondLength);

		// store the scale so that other components can access it
		rendererModel.setScale(scale);
	}

	/**
	 * Set the scale for an IReaction. It calculates the average bond length of
	 * the model and calculates the multiplication factor to transform this to
	 * the bond length that is set in the RendererModel.
	 * 
	 * @param reaction
	 */
	public void setScale(final IReaction reaction) {
		final double bondLength = Renderer.calculateAverageBondLength(reaction);
		scale = calculateScaleForBondLength(bondLength);

		// store the scale so that other components can access it
		rendererModel.setScale(scale);
	}

	/**
	 * Set the scale for an IReactionSet. It calculates the average bond length
	 * of the model and calculates the multiplication factor to transform this
	 * to the bond length that is set in the RendererModel.
	 * 
	 * @param reactionSet
	 */
	public void setScale(final IReactionSet reactionSet) {
		final double bondLength = Renderer
				.calculateAverageBondLength(reactionSet);
		scale = calculateScaleForBondLength(bondLength);

		// store the scale so that other components can access it
		rendererModel.setScale(scale);
	}

	private void setup() {

		// set the transform
		try {
			transform = new AffineTransform();
			transform.translate(drawCenter.x, drawCenter.y);
			transform.scale(1, -1); // Converts between CDK Y-up & Java2D Y-down
									// coordinate-systems
			transform.scale(scale, scale);
			transform.scale(zoom, zoom);
			transform.translate(-modelCenter.x, -modelCenter.y);
		} catch (final NullPointerException npe) {
			// one of the drawCenter or modelCenter points have not been set!
			System.err.println(String.format(
					"null pointer when setting transform: "
							+ "drawCenter=%s scale=%s zoom=%s modelCenter=%s",
					drawCenter, scale, zoom, modelCenter));
		}
	}

	/**
	 * Setup the transformations necessary to draw this Chem Model.
	 * 
	 * @param chemModel
	 * @param screen
	 */
	public void setup(final IChemModel chemModel, final Rectangle screen) {
		this.setScale(chemModel);
		final Rectangle2D bounds = Renderer.calculateBounds(chemModel);
		modelCenter = new Point2d(bounds.getCenterX(), bounds.getCenterY());
		drawCenter = new Point2d(screen.getCenterX(), screen.getCenterY());
		this.setup();
	}

	/**
	 * Setup the transformations necessary to draw this Reaction.
	 * 
	 * @param reaction
	 * @param screen
	 */
	public void setup(final IReaction reaction, final Rectangle screen) {
		this.setScale(reaction);
		final Rectangle2D bounds = Renderer.calculateBounds(reaction);
		modelCenter = new Point2d(bounds.getCenterX(), bounds.getCenterY());
		drawCenter = new Point2d(screen.getCenterX(), screen.getCenterY());
		this.setup();
	}

	/**
	 * Setup the transformations necessary to draw this Reaction Set.
	 * 
	 * @param reactionSet
	 * @param screen
	 */
	public void setup(final IReactionSet reactionSet, final Rectangle screen) {
		this.setScale(reactionSet);
		final Rectangle2D bounds = Renderer.calculateBounds(reactionSet);
		modelCenter = new Point2d(bounds.getCenterX(), bounds.getCenterY());
		drawCenter = new Point2d(screen.getCenterX(), screen.getCenterY());
		this.setup();
	}

	/**
	 * Set the transform for a non-fit to screen paint.
	 * 
	 * @param modelBounds
	 *            the bounding box of the model
	 */
	private void setupTransformNatural(final Rectangle2D modelBounds) {
		zoom = rendererModel.getZoomFactor();
		fontManager.setFontForZoom(zoom);
		this.setup();
	}

	/**
	 * Sets the transformation needed to draw the model on the canvas when the
	 * diagram needs to fit the screen.
	 * 
	 * @param screenBounds
	 *            the bounding box of the draw area
	 * @param modelBounds
	 *            the bounding box of the model
	 * @param bondLength
	 *            the average bond length of the model
	 * @param reset
	 *            if true, model center will be set to the modelBounds center
	 *            and the scale will be re-calculated
	 */
	private void setupTransformToFit(final Rectangle2D screenBounds,
			final Rectangle2D modelBounds, final double bondLength,
			final boolean reset) {

		if (screenBounds == null) {
			return;
		}

		setDrawCenter(screenBounds.getCenterX(), screenBounds.getCenterY());

		scale = calculateScaleForBondLength(bondLength);

		final double drawWidth = screenBounds.getWidth();
		final double drawHeight = screenBounds.getHeight();

		final double diagramWidth = modelBounds.getWidth() * scale;
		final double diagramHeight = modelBounds.getHeight() * scale;

		setZoomToFit(drawWidth, drawHeight, diagramWidth, diagramHeight);

		// this controls whether editing a molecule causes it to re-center
		// with each change or not
		if (reset || rendererModel.isFitToScreen()) {
			setModelCenter(modelBounds.getCenterX(), modelBounds.getCenterY());
		}

		// set the scale in the renderer model for the generators
		if (reset) {
			rendererModel.setScale(scale);
		}

		this.setup();
	}

	@Override
	public void setZoom(final double z) {
		getRenderer2DModel().setZoomFactor(z);
		zoom = z;
		setup();
	}

	/**
	 * Calculate and set the zoom factor needed to completely fit the diagram
	 * onto the screen bounds.
	 * 
	 * @param diagramBounds
	 * @param drawBounds
	 */
	@Override
	public void setZoomToFit(final double drawWidth, final double drawHeight,
			final double diagramWidth, final double diagramHeight) {

		final double m = rendererModel.getMargin();

		// determine the zoom needed to fit the diagram to the screen
		final double widthRatio = drawWidth / (diagramWidth + 2 * m);
		final double heightRatio = drawHeight / (diagramHeight + 2 * m);

		zoom = Math.min(widthRatio, heightRatio);

		fontManager.setFontForZoom(zoom);

		// record the zoom in the model, so that generators can use it
		rendererModel.setZoomFactor(zoom);

	}

	/**
	 * Move the draw center by dx and dy.
	 * 
	 * @param dx
	 *            the x shift
	 * @param dy
	 *            the y shift
	 */
	@Override
	public void shiftDrawCenter(final double dx, final double dy) {
		drawCenter.set(drawCenter.x + dx, drawCenter.y + dy);
		setup();
	}
}
