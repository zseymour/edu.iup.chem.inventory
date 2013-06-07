package edu.iup.chem.inventory.ui;

import javax.swing.JPanel;

import org.openscience.cdk.AtomContainer;

public abstract class DataPanel extends JPanel implements ChemicalLister {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 8368137306376041566L;

	public abstract void search(AtomContainer substructure);

	public abstract void start(String filter);

}
