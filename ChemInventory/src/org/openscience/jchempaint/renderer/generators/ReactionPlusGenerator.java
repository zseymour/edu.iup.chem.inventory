/* Copyright (C) 2009  Stefan Kuhn <shk3@users.sf.net>
 *               2009  Gilleain Torrance <gilleain@users.sf.net>
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
package org.openscience.jchempaint.renderer.generators;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.jchempaint.renderer.Renderer;
import org.openscience.jchempaint.renderer.RendererModel;
import org.openscience.jchempaint.renderer.elements.ElementGroup;
import org.openscience.jchempaint.renderer.elements.IRenderingElement;
import org.openscience.jchempaint.renderer.elements.TextElement;

/**
 * Generate the arrow for a reaction.
 * 
 * @author maclean
 * @cdk.module renderextra
 * 
 */
public class ReactionPlusGenerator implements IReactionGenerator {

	@Override
	public IRenderingElement generate(final IReaction reaction,
			final RendererModel model) {
		final ElementGroup diagram = new ElementGroup();
		final Color color = model.getForeColor();

		final IAtomContainerSet reactants = reaction.getReactants();
		if (reactants.getAtomContainerCount() > 0) {
			final Rectangle2D totalBoundsReactants = Renderer
					.calculateBounds(reactants);
			Rectangle2D bounds1 = Renderer.calculateBounds(reactants
					.getAtomContainer(0));
			final double axis = totalBoundsReactants.getCenterY();
			for (int i = 1; i < reaction.getReactantCount(); i++) {
				final Rectangle2D bounds2 = Renderer.calculateBounds(reactants
						.getAtomContainer(i));
				diagram.add(makePlus(bounds1, bounds2, axis, color));
				bounds1 = bounds2;
			}
		}

		final IAtomContainerSet products = reaction.getProducts();
		if (products.getAtomContainerCount() > 0) {
			final Rectangle2D totalBoundsProducts = Renderer
					.calculateBounds(products);
			final double axis = totalBoundsProducts.getCenterY();
			Rectangle2D bounds1 = Renderer.calculateBounds(products
					.getAtomContainer(0));
			for (int i = 1; i < reaction.getProductCount(); i++) {
				final Rectangle2D bounds2 = Renderer.calculateBounds(products
						.getAtomContainer(i));

				diagram.add(makePlus(bounds1, bounds2, axis, color));
				bounds1 = bounds2;
			}
		}
		return diagram;
	}

	public TextElement makePlus(final Rectangle2D a, final Rectangle2D b,
			final double axis, final Color color) {
		final double x = (a.getCenterX() + b.getCenterX()) / 2;
		return new TextElement(x, axis, "+", color);
	}
}
