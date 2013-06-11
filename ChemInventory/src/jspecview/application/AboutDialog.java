/* Copyright (c) 2002-2011 The University of the West Indies
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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/**
 * The <code>About Dialog</code> class is the <i>help | about</i> window for the
 * JSpecView.
 * 
 * @author Debbie-Ann Facey
 * @author Khari A. Bryan
 * @author Prof. Robert J. Lancashire
 */
public class AboutDialog extends JDialog {
	JPanel		p	= new JPanel();
	JTextArea	txt;

	/**
	 * Constructor that initalises the Dialog without a parent frame, no title
	 * and modality to false
	 */
	public AboutDialog() {
		this(null, "", false);
	}

	/**
	 * Constructor that initalises the Dialog a parent frame, no title and
	 * modality to true
	 * 
	 * @param frame
	 *            parent container for the About dialog
	 */
	public AboutDialog(final Frame frame) {
		this(frame, "", true);
	}

	/**
	 * Constructor
	 * 
	 * @param owner
	 *            the parent frame
	 * @param title
	 *            the title of the frame
	 * @param modal
	 *            true is the dialog should be modal
	 */
	public AboutDialog(final Frame owner, final String title,
			final boolean modal) {
		super(owner, title, modal);

		try {
			jbInit();

			// dialog properties
			setTitle("About JSpecView");
			pack();
			setResizable(false);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		// Sets the location to the middle of the parent frame if it has one
		if (owner != null) {
			setLocation((owner.getLocation().x + owner.getSize().width) / 2,
					(owner.getLocation().y + owner.getSize().height) / 2);
		}
		setVisible(true);
	}

	private JTextArea drawMessage(final String message, final String fontType,
			final int fontStyle, final int fontSize) {
		final JTextArea text = new JTextArea(message);
		text.setBorder(new EmptyBorder(5, 10, 5, 10));
		text.setFont(new Font(fontType, fontStyle, fontSize));
		text.setEditable(false);
		text.setBackground(getBackground());
		return text;
	}

	void jbInit() throws Exception {
		final JLabel lbl = new JLabel(new ImageIcon(AboutDialog.class
				.getClassLoader().getResource("icons/about.gif")));

		final Border b1 = new BevelBorder(BevelBorder.LOWERED);

		final Border b2 = new EmptyBorder(5, 5, 5, 5);
		lbl.setBorder(new CompoundBorder(b1, b2));

		p.add(lbl);

		// add image to About dialog
		getContentPane().add(p, BorderLayout.WEST);

		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		String message = "Distributed under the GNU Lesser Public License\n";
		message += "via sourceforge at http://jspecview.sf.net";
		txt = drawMessage(message, "Arial", Font.PLAIN, 12);
		p.add(txt);

		message = "Authors:\nD. Facey, K. Bryan, C. Walters\nProf. Robert J. Lancashire and\n";
		message += "volunteer developers through sourceforge.";
		txt = drawMessage(message, "Arial", Font.BOLD, 12);
		p.add(txt);

		message = "Copyright (c) 2011, Department of Chemistry\nUniversity of the West Indies, Mona Campus\nJAMAICA";
		txt = drawMessage(message, "Arial", Font.PLAIN, 12);
		p.add(txt);

		// add text to About dialog
		getContentPane().add(p, BorderLayout.CENTER);

		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				setVisible(false);
			}
		});

		p = new JPanel();
		p.add(okButton);

		// add OK button to About dialog
		getContentPane().add(p, BorderLayout.SOUTH);
	}
}
