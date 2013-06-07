package edu.iup.chem.inventory.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.jchempaint.JChemPaintPanel;

import edu.iup.chem.inventory.Constants;

public class StructureSketchDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long		serialVersionUID	= 8904658108218468771L;
	private final JChemPaintPanel	sketchPane;
	private static final Logger		LOG					= Logger.getLogger(StructureSketchDialog.class);

	public StructureSketchDialog(final JFrame parent) {
		super(parent, "Draw Structure", true);

		setLayout(new BorderLayout());

		final Dimension screen = Constants.SCREEN_SIZE;
		final Dimension thirdOfScreen = new Dimension(screen.width / 3,
				screen.height / 2);
		setPreferredSize(thirdOfScreen);
		setSize(thirdOfScreen);

		final IChemModel chemModel = DefaultChemObjectBuilder.getInstance()
				.newInstance(IChemModel.class);
		chemModel.setMoleculeSet(chemModel.getBuilder().newInstance(
				IAtomContainerSet.class));
		chemModel.getMoleculeSet().addAtomContainer(
				chemModel.getBuilder().newInstance(IAtomContainer.class));

		sketchPane = new JChemPaintPanel(chemModel);
		add(sketchPane, BorderLayout.CENTER);

		final JButton search = new JButton("Search");
		search.addActionListener(this);
		add(search, BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(final ActionEvent evt) {
		try {
			LOG.debug("Firing new structure change.");
			sketchPane.get2DHub().cleanup();
			final SmilesParser parse = new SmilesParser(
					DefaultChemObjectBuilder.getInstance());
			final String smiles = sketchPane.getSmiles();
			final AtomContainer m = (AtomContainer) parse.parseSmiles(smiles);
			firePropertyChange("sketch", null, m);
			// dispose();
		} catch (ClassNotFoundException | CDKException | IOException
				| CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
