/* Copyright (C) 2006  The JSpecView Development Team
 *
 * Contact: robert.lancashire@uwimona.edu.jm
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package jspecview.application;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyVetoException;

import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 * Private class used to replace the standard DesktopManager for JDesktopPane.
 * Used to provide scrollbar functionality.
 */
class MDIDesktopManager extends DefaultDesktopManager {
	private final ScrollableDesktopPane	desktop;

	public MDIDesktopManager(final ScrollableDesktopPane desktop) {
		this.desktop = desktop;
	}

	@Override
	public void endDraggingFrame(final JComponent f) {
		super.endDraggingFrame(f);
		resizeDesktop();
	}

	@Override
	public void endResizingFrame(final JComponent f) {
		super.endResizingFrame(f);
		resizeDesktop();
	}

	public JScrollPane getScrollPane() {
		if (desktop.getParent() instanceof JViewport) {
			final JViewport viewPort = (JViewport) desktop.getParent();
			if (viewPort.getParent() instanceof JScrollPane) {
				return (JScrollPane) viewPort.getParent();
			}
		}
		return null;
	}

	private Insets getScrollPaneInsets() {
		final JScrollPane scrollPane = getScrollPane();
		if (scrollPane == null) {
			return new Insets(0, 0, 0, 0);
		} else {
			return getScrollPane().getBorder().getBorderInsets(scrollPane);
		}
	}

	protected void resizeDesktop() {
		int x = 0;
		int y = 0;
		final JScrollPane scrollPane = getScrollPane();
		final Insets scrollInsets = getScrollPaneInsets();

		if (scrollPane != null) {
			final JInternalFrame allFrames[] = desktop.getAllFrames();
			for (final JInternalFrame allFrame : allFrames) {
				if (allFrame.getX() + allFrame.getWidth() > x) {
					x = allFrame.getX() + allFrame.getWidth();
				}
				if (allFrame.getY() + allFrame.getHeight() > y) {
					y = allFrame.getY() + allFrame.getHeight();
				}
			}
			final Dimension d = scrollPane.getVisibleRect().getSize();
			if (scrollPane.getBorder() != null) {
				d.setSize(
						d.getWidth() - scrollInsets.left - scrollInsets.right,
						d.getHeight() - scrollInsets.top - scrollInsets.bottom);
			}

			if (x <= d.getWidth()) {
				x = (int) d.getWidth() - 20;
			}
			if (y <= d.getHeight()) {
				y = (int) d.getHeight() - 20;
			}
			desktop.setAllSize(x, y);
			scrollPane.invalidate();
			scrollPane.validate();
		}
	}

	public void setNormalSize() {
		final JScrollPane scrollPane = getScrollPane();
		final int x = 0;
		final int y = 0;
		final Insets scrollInsets = getScrollPaneInsets();

		if (scrollPane != null) {
			final Dimension d = scrollPane.getVisibleRect().getSize();
			if (scrollPane.getBorder() != null) {
				d.setSize(
						d.getWidth() - scrollInsets.left - scrollInsets.right,
						d.getHeight() - scrollInsets.top - scrollInsets.bottom);
			}

			d.setSize(d.getWidth() - 20, d.getHeight() - 20);
			desktop.setAllSize(x, y);
			scrollPane.invalidate();
			scrollPane.validate();
		}
	}
}

/**
 * An extension of JDesktopPane that supports often used MDI functionality. This
 * class also handles setting scroll bars for when windows move too far to the
 * left or bottom, providing the ScrollableDesktopPane is in a ScrollPane. Taken
 * from www.javaworld.com (originally mditest package)
 * 
 * @author http://www.javaworld.com
 */
public class ScrollableDesktopPane extends JDesktopPane {
	private static int				FRAME_OFFSET	= 20;
	private final MDIDesktopManager	manager;

	public ScrollableDesktopPane() {
		manager = new MDIDesktopManager(this);
		setDesktopManager(manager);
		setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
	}

	public Component add(final JInternalFrame frame) {
		final JInternalFrame[] array = getAllFrames();
		final Point p;
		int w;
		int h;

		final Component retval = super.add(frame);
		checkDesktopSize();
		/*
		 * if (array.length > 0) { p = array[0].getLocation(); p.x = p.x +
		 * FRAME_OFFSET; p.y = p.y + FRAME_OFFSET; } else { p = new Point(0, 0);
		 * } frame.setLocation(p.x, p.y);
		 */
		if (frame.isResizable()) {
			w = getWidth() - getWidth() / 3;
			h = getHeight() - getHeight() / 3;
			if (w < frame.getMinimumSize().getWidth()) {
				w = (int) frame.getMinimumSize().getWidth();
			}
			if (h < frame.getMinimumSize().getHeight()) {
				h = (int) frame.getMinimumSize().getHeight();
			}
			frame.setSize(w, h);
		}
		moveToFront(frame);
		frame.setVisible(true);
		try {
			frame.setSelected(true);
		} catch (final PropertyVetoException e) {
			frame.toBack();
		}
		return retval;
	}

	/**
	 * Cascade all internal frames
	 */
	public void cascadeFrames() {
		int x = 0;
		int y = 0;
		final JInternalFrame allFrames[] = getAllFrames();

		manager.setNormalSize();
		final int frameHeight = getBounds().height - 5 - allFrames.length
				* FRAME_OFFSET;
		final int frameWidth = getBounds().width - 5 - allFrames.length
				* FRAME_OFFSET;
		for (int i = allFrames.length - 1; i >= 0; i--) {
			allFrames[i].setSize(frameWidth, frameHeight);
			allFrames[i].setLocation(x, y);
			x = x + FRAME_OFFSET;
			y = y + FRAME_OFFSET;
		}
	}

	private void checkDesktopSize() {
		if (getParent() != null && isVisible()) {
			manager.resizeDesktop();
		}
	}

	@Override
	public void remove(final Component c) {
		super.remove(c);
		checkDesktopSize();
	}

	/*
	 * Sets all component size properties ( maximum, minimum, preferred) to the
	 * given dimension.
	 */
	public void setAllSize(final Dimension d) {
		setMinimumSize(d);
		setMaximumSize(d);
		setPreferredSize(d);
	}

	/*
	 * Sets all component size properties ( maximum, minimum, preferred) to the
	 * given width and height.
	 */
	public void setAllSize(final int width, final int height) {
		setAllSize(new Dimension(width, height));
	}

	@Override
	public void setBounds(final int x, final int y, final int w, final int h) {
		super.setBounds(x, y, w, h);
		checkDesktopSize();
	}

	/**
	 * Tile all internal frames
	 */
	public void tileFrames() {
		final JInternalFrame[] frames = getAllFrames();
		manager.setNormalSize();

		final JScrollPane scrollPane = manager.getScrollPane();
		Rectangle viewP;
		if (scrollPane != null) {
			viewP = scrollPane.getViewport().getViewRect();
		} else {
			viewP = getBounds();
		}

		int totalNonIconFrames = 0;

		for (int i = 0; i < frames.length; i++) {
			if (!frames[i].isIcon()) { // don't include iconified frames...
				totalNonIconFrames++;
			}
		}

		int curCol = 0;
		int curRow = 0;
		int i = 0;

		if (totalNonIconFrames > 0) {
			// compute number of columns and rows then tile the frames
			final int numCols = (int) Math.sqrt(totalNonIconFrames);

			final int frameWidth = viewP.width / numCols;

			for (curCol = 0; curCol < numCols; curCol++) {
				int numRows = totalNonIconFrames / numCols;
				final int remainder = totalNonIconFrames % numCols;

				if (numCols - curCol <= remainder) {
					numRows++; // add an extra row for this guy
				}

				final int frameHeight = viewP.height / numRows;

				for (curRow = 0; curRow < numRows; curRow++) {
					while (frames[i].isIcon()) { // find the next visible frame
						i++;
					}

					frames[i].setBounds(curCol * frameWidth, curRow
							* frameHeight, frameWidth, frameHeight);

					i++;
				}
			}
		}
	}
}
