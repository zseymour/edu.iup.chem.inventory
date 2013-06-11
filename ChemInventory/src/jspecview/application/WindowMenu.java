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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * Menu component that handles the functionality expected of a standard
 * "Windows" menu for MDI applications. Taken from www.javaworld.com (originally
 * mditest package)
 * 
 * @author http://www.javaworld.com
 */
public class WindowMenu extends JMenu {
	/*
	 * This JCheckBoxMenuItem descendant is used to track the child frame that
	 * corresponds to a give menu.
	 */
	class ChildMenuItem extends JCheckBoxMenuItem {
		private final JInternalFrame	frame;

		public ChildMenuItem(final JInternalFrame frame) {
			super(frame.getTitle());
			this.frame = frame;
		}

		public JInternalFrame getFrame() {
			return frame;
		}
	}

	private final ScrollableDesktopPane	desktop;
	private final JMenuItem				cascade	= new JMenuItem("Cascade");

	private final JMenuItem				tile	= new JMenuItem("Tile");

	public WindowMenu(final ScrollableDesktopPane desktop) {
		this.desktop = desktop;
		setText("Window");
		add(cascade);
		add(tile);

		addSeparator();
		cascade.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				WindowMenu.this.desktop.cascadeFrames();
			}
		});
		tile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				WindowMenu.this.desktop.tileFrames();
			}
		});
		addMenuListener(new MenuListener() {
			@Override
			public void menuCanceled(final MenuEvent e) {
			}

			@Override
			public void menuDeselected(final MenuEvent e) {
				final Component[] menuItems = getMenuComponents();
				for (final Component menuItem : menuItems) {
					if (menuItem instanceof JCheckBoxMenuItem) {
						remove(menuItem);
					}
				}
			}

			@Override
			public void menuSelected(final MenuEvent e) {
				buildChildMenus();
			}
		});
	}

	/* Sets up the children menus depending on the current desktop state */
	private void buildChildMenus() {
		int i;
		ChildMenuItem menu;
		final JInternalFrame[] array = desktop.getAllFrames();

		// if (array.length > 0) addSeparator();
		cascade.setEnabled(array.length > 0);
		tile.setEnabled(array.length > 0);

		for (i = 0; i < array.length; i++) {
			menu = new ChildMenuItem(array[i]);
			menu.setState(i == 0);
			menu.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent ae) {
					final JInternalFrame frame = ((ChildMenuItem) ae
							.getSource()).getFrame();
					frame.moveToFront();
					try {
						frame.setSelected(true);
					} catch (final PropertyVetoException e) {
						e.printStackTrace();
					}
				}
			});
			menu.setIcon(array[i].getFrameIcon());
			add(menu);
		}
	}
}
