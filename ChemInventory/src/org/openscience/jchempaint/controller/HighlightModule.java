/* $Revision: 7636 $ $Author: nielsout $ $Date: 2007-01-04 18:46:10 +0100 (Thu, 04 Jan 2007) $
 *
 * Copyright (C) 2007  Niels Out <nielsout@users.sf.net>
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All I ask is that proper credit is given for my work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
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
package org.openscience.jchempaint.controller;

import java.applet.Applet;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.jchempaint.applet.JChemPaintAbstractApplet;
import org.openscience.jchempaint.renderer.RendererModel;

/**
 * This should highlight the atom/bond when moving over with the mouse
 * 
 * @author Niels Out
 * @cdk.svnrev $Revision: 9162 $
 * @cdk.module controlbasic
 */
public class HighlightModule extends ControllerModuleAdapter {

	private IAtom							prevHighlightAtom;

	private IBond							prevHighlightBond;
	private String							ID;
	private final JChemPaintAbstractApplet	jcpApplet;
	private Applet							spectrumApplet;
	int										oldnumber	= -1;

	public HighlightModule(final IChemModelRelay chemObjectRelay,
			final JChemPaintAbstractApplet applet) {
		super(chemObjectRelay);
		assert chemObjectRelay != null;
		jcpApplet = applet;
	}

	@Override
	public String getDrawModeString() {
		return "Highlighting";
	}

	@Override
	public String getID() {
		return ID;
	}

	private Applet getSpectrumApplet() {
		if (spectrumApplet == null) {
			final String s = jcpApplet.getParameter("spectrumRenderer");
			if (s != null && s.length() > 0) {
				spectrumApplet = jcpApplet.getAppletContext().getApplet(s);
			}
		}
		return spectrumApplet;
	}

	private void highlightPeakInSpectrumApplet(final IAtom atom) {
		if (jcpApplet == null
				|| jcpApplet.getParameter("spectrumRenderer") == null) {
			return;
		}
		try {
			final Method highlightMethod = getSpectrumApplet().getClass()
					.getMethod("highlightPeakInSpectrum",
							new Class[] { Integer.TYPE });
			highlightMethod.invoke(getSpectrumApplet(),
					new Object[] { new Integer(chemModelRelay.getIChemModel()
							.getMoleculeSet().getAtomContainer(0)
							.getAtomNumber(atom)) });
			spectrumApplet.repaint();
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Handles interaction with a peak table
	 * 
	 * @param atomNumber
	 *            atom number of peaks highlighted in table
	 */
	public void highlightPeakInTable(final int atomNumber)
			throws ClassNotFoundException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		if (jcpApplet == null
				|| jcpApplet.getParameter("highlightTable") == null) {
			return;
		}
		final Class[] paratypes = { new Applet().getClass() };
		final Class jso = Class.forName("netscape.javascript.JSObject");
		final Method getWindowMethod = jso.getMethod("getWindow", paratypes);
		final Object win = getWindowMethod.invoke(jso,
				new Object[] { jcpApplet });
		final Class[] paratypes2 = { new String("").getClass() };
		final Method evalMethod = jso.getMethod("eval", paratypes2);
		final Class[] paratypes3 = { new String("").getClass(),
				new Object().getClass() };
		final Method setMemberMethod = jso.getMethod("setMember", paratypes3);

		if (oldnumber != -1) {
			final Object tr = evalMethod.invoke(win,
					new Object[] { "document.getElementById(\"tableid"
							+ oldnumber + "\").style" });
			if ((oldnumber + 1) % 2 == 0) {
				setMemberMethod.invoke(tr, new Object[] { "backgroundColor",
						"#D3D3D3" });
			} else {
				setMemberMethod.invoke(tr, new Object[] { "backgroundColor",
						"white" });
			}
		}
		final Object tr = evalMethod.invoke(win,
				new Object[] { "document.getElementById(\"tableid" + atomNumber
						+ "\").style" });
		if (tr == null) {
			oldnumber = -1;
		} else {
			setMemberMethod.invoke(tr, new Object[] { "backgroundColor",
					"#FF6600" });
			oldnumber = atomNumber;
		}
	}

	@Override
	public void mouseMove(final Point2d worldCoord) {
		final IAtom atom = chemModelRelay.getClosestAtom(worldCoord);
		final IBond bond = chemModelRelay.getClosestBond(worldCoord);
		final RendererModel model = chemModelRelay.getRenderer()
				.getRenderer2DModel();

		final IChemObject obj = getHighlighted(worldCoord, atom, bond);
		if (obj == null) {
			unsetHighlights(model);
		} else {
			update(obj, model);
		}
	}

	@Override
	public void setID(final String ID) {
		this.ID = ID;
	}

	private void unsetHighlights(final RendererModel model) {
		if (prevHighlightAtom != null || prevHighlightBond != null) {
			model.setHighlightedAtom(null);
			model.setHighlightedBond(null);
			prevHighlightAtom = null;
			prevHighlightBond = null;
			chemModelRelay.updateView();
		}
	}

	private void update(final IChemObject obj, final RendererModel model) {
		if (obj instanceof IAtom) {
			updateAtom((IAtom) obj, model);
		} else if (obj instanceof IBond) {
			updateBond((IBond) obj, model);
		}
	}

	private void updateAtom(final IAtom atom, final RendererModel model) {
		if (prevHighlightAtom != atom) {
			model.setHighlightedAtom(atom);
			prevHighlightAtom = atom;
			prevHighlightBond = null;
			model.setHighlightedBond(null);
			try {
				if (chemModelRelay.getIChemModel().getMoleculeSet() != null
						&& chemModelRelay.getIChemModel().getMoleculeSet()
								.getAtomContainerCount() > 0) {
					highlightPeakInTable(chemModelRelay.getIChemModel()
							.getMoleculeSet().getAtomContainer(0)
							.getAtomNumber(atom));
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
			highlightPeakInSpectrumApplet(atom);
			chemModelRelay.updateView();
		}
	}

	private void updateBond(final IBond bond, final RendererModel model) {
		if (prevHighlightBond != bond) {
			model.setHighlightedBond(bond);
			prevHighlightBond = bond;
			prevHighlightAtom = null;
			model.setHighlightedAtom(null);
			chemModelRelay.updateView();
		}
	}
}
