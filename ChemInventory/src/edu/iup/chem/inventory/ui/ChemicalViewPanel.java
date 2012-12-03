/*
 * To change this templileate, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.iup.chem.inventory.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
import org.openscience.cdk.smiles.SmilesParser;

import edu.iup.chem.inventory.db.inventory.enums.ChemicalCold;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalFlamm;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalToxic;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;
import edu.iup.chem.inventory.search.ChemicalWebSearch;

/**
 * 
 * @author Zach
 */
public class ChemicalViewPanel extends JPanel {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -5239291987178761103L;

	private static Image renderMolecule(final String SMILES) throws Exception {
		final int DRAW_WIDTH = 400;
		final int DRAW_HEIGHT = 400;
		// the draw area and the image should be the same size
		final Rectangle drawArea = new Rectangle(DRAW_WIDTH, DRAW_HEIGHT);
		final Image image = new BufferedImage(DRAW_WIDTH, DRAW_WIDTH,
				BufferedImage.TYPE_INT_ARGB);

		Molecule molecule = (Molecule) new SmilesParser(
				DefaultChemObjectBuilder.getInstance()).parseSmiles(SMILES);

		final StructureDiagramGenerator sdg = new StructureDiagramGenerator();
		sdg.setMolecule(molecule, false);
		sdg.generateCoordinates();
		molecule = (Molecule) sdg.getMolecule();

		// generators make the image elements
		final List<IGenerator<IAtomContainer>> generators = new ArrayList<>();
		generators.add(new BasicSceneGenerator());
		generators.add(new BasicBondGenerator());
		generators.add(new BasicAtomGenerator());
		// the renderer needs to have a toolkit-specific font manager
		final AtomContainerRenderer renderer = new AtomContainerRenderer(
				generators, new AWTFontManager());
		// the call to 'setup' only needs to be done on the first paint
		renderer.setup(molecule, drawArea);
		// paint the background
		final Graphics2D g2 = (Graphics2D) image.getGraphics();
		g2.setBackground(Color.WHITE);
		g2.setColor(Color.WHITE);
		g2.setPaint(Color.WHITE);
		g2.fillRect(0, 0, WIDTH, HEIGHT);
		// the paint method also needs a toolkit-specific renderer
		renderer.paint(molecule, new AWTDrawVisitor(g2));

		return image;
	}

	private ChemicalRecord					chemical;

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private final javax.swing.JLabel		casLabel;
	private final javax.swing.JLabel		coldLabel;
	private final javax.swing.JLabel		flammLabel;
	private final javax.swing.JLabel		formulaLabel;
	private final javax.swing.JPanel		jPanel1;
	private final javax.swing.JScrollPane	jScrollPane1;
	private final javax.swing.JLabel		nameLabel;
	private final FireDiamondLabel			nfpaLabel;
	private final javax.swing.JLabel		smilesLabel;
	private final JLabel					structureLabel;
	private final javax.swing.JLabel		toxicityLabel;
	private static final Logger				log	= Logger.getLogger(ChemicalViewPanel.class);

	public ChemicalViewPanel() {
		chemical = new ChemicalRecord();
		nameLabel = new javax.swing.JLabel();
		formulaLabel = new javax.swing.JLabel();
		casLabel = new javax.swing.JLabel();
		smilesLabel = new javax.swing.JLabel();
		structureLabel = new JLabel();
		nfpaLabel = new FireDiamondLabel();
		toxicityLabel = new javax.swing.JLabel();
		flammLabel = new javax.swing.JLabel();
		coldLabel = new javax.swing.JLabel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jPanel1 = new javax.swing.JPanel();
	}

	// End of variables declaration//GEN-END:variables
	public ChemicalViewPanel(final ChemicalRecord rec) {
		chemical = rec;
		nameLabel = new javax.swing.JLabel();
		formulaLabel = new javax.swing.JLabel();
		casLabel = new javax.swing.JLabel();
		smilesLabel = new javax.swing.JLabel();

		nfpaLabel = new FireDiamondLabel(rec);
		toxicityLabel = new javax.swing.JLabel();
		flammLabel = new javax.swing.JLabel();
		coldLabel = new javax.swing.JLabel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jPanel1 = new javax.swing.JPanel();
		structureLabel = new JLabel();
		initComponents();
	}

	/**
	 * Creates new form ChemicalViewPanel
	 */
	public ChemicalViewPanel(final ChemicalRecord rec, final String searchSmiles) {
		chemical = rec;
		nameLabel = new javax.swing.JLabel();
		formulaLabel = new javax.swing.JLabel();
		casLabel = new javax.swing.JLabel();
		smilesLabel = new javax.swing.JLabel();

		nfpaLabel = new FireDiamondLabel();
		toxicityLabel = new javax.swing.JLabel();
		flammLabel = new javax.swing.JLabel();
		coldLabel = new javax.swing.JLabel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jPanel1 = new javax.swing.JPanel();
		structureLabel = new JLabel();
		initComponents();
	}

	private String getFormulaHTML() {
		final String molecularFormula = chemical.getFormula();
		final Pattern digitPattern = Pattern.compile("\\d+");
		final Matcher digitMatcher = digitPattern.matcher(molecularFormula);
		return "<html>" + digitMatcher.replaceAll("<sub>$0</sub>") + "</html>";
	}

	public Image getStructureImage() {
		final int DRAW_WIDTH = 400;
		final int DRAW_HEIGHT = 400;
		// the draw area and the image should be the same size
		Image image = new BufferedImage(DRAW_WIDTH, DRAW_HEIGHT,
				BufferedImage.TYPE_INT_RGB);

		if (chemical != null) {
			try {
				image = renderMolecule(chemical.getSmiles());
			} catch (final Exception e) {
				// Failed to generate an image, so we'll try to download one
				if (chemical.getCsid() != null) {
					image = ChemicalWebSearch
							.getStructureThumbnailFromCSID(chemical.getCsid());
				}
			}
		}
		return image;
	}

	private String getToxicTooltip() {
		final ChemicalToxic toxicity = chemical.getToxic();

		switch (toxicity) {
			case Extremely_toxic:
				return "A taste (less than 7 drops)";
			case Highly_toxic:
				return "Between 7 drops and 1 teaspoonful";
			case Moderately_toxic:
				return "Between 1 teaspoonful and 1 ounce";
			case Slightly_toxic:
				return "Between 1 ounce and 1 pint";
			case Practically_nontoxic:
				return "More than 1 pint";
			default:
				return "Unknown";
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */

	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		nfpaLabel.redraw(chemical);

		nameLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
		nameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		nameLabel.setText(chemical.getName());

		formulaLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
		formulaLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		formulaLabel.setText(getFormulaHTML());

		casLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		casLabel.setText(chemical.getCas());

		smilesLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		smilesLabel.setText(chemical.getSmiles());

		structureLabel.setHorizontalAlignment(SwingConstants.CENTER);
		structureLabel.setIcon(new ImageIcon(getStructureImage()));

		toxicityLabel.setText(chemical.getToxic().getLiteral());
		toxicityLabel.setToolTipText(getToxicTooltip());

		if (chemical.getFlamm().equals(ChemicalFlamm.Yes)) {
			flammLabel.setText("This chemical is flammable.");
		} else {
			flammLabel.setText("");
		}

		if (chemical.getCold().equals(ChemicalCold.Yes)) {
			coldLabel.setText("This chemical requires cold storage.");
		} else {
			coldLabel.setText("");
		}

		final javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(
				jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout
				.setHorizontalGroup(jPanel1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel1Layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																javax.swing.GroupLayout.Alignment.TRAILING,
																jPanel1Layout
																		.createSequentialGroup()
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																				103,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addComponent(
																				structureLabel,
																				0,
																				500,
																				500)
																		.addGap(117,
																				117,
																				117))
														.addComponent(
																formulaLabel,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																nameLabel,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addGroup(
																jPanel1Layout
																		.createSequentialGroup()
																		.addComponent(
																				casLabel,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				Short.MAX_VALUE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																		.addComponent(
																				smilesLabel,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				188,
																				javax.swing.GroupLayout.PREFERRED_SIZE))
														.addGroup(
																javax.swing.GroupLayout.Alignment.TRAILING,
																jPanel1Layout
																		.createSequentialGroup()
																		.addComponent(
																				nfpaLabel,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				96,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																		.addGroup(
																				jPanel1Layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(
																								toxicityLabel,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(
																								flammLabel,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(
																								coldLabel,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE))))
										.addContainerGap()));
		jPanel1Layout
				.setVerticalGroup(jPanel1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel1Layout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(nameLabel)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(formulaLabel)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(casLabel)
														.addComponent(
																smilesLabel))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(
												structureLabel,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												147,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addGap(18, 18, 18)
										.addGroup(
												jPanel1Layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(
																nfpaLabel,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																95,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addGroup(
																jPanel1Layout
																		.createSequentialGroup()
																		.addComponent(
																				toxicityLabel)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																		.addComponent(
																				flammLabel)
																		.addGap(13,
																				13,
																				13)
																		.addComponent(
																				coldLabel)))
										.addContainerGap()));

		jScrollPane1.setViewportView(jPanel1);

		final javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(jScrollPane1,
								javax.swing.GroupLayout.DEFAULT_SIZE, 380,
								Short.MAX_VALUE).addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(jScrollPane1,
								javax.swing.GroupLayout.PREFERRED_SIZE, 391,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(104, Short.MAX_VALUE)));
	}// </editor-fold>//GEN-END:initComponents

	public void start(final ChemicalRecord rec) {
		if (rec != null) {
			chemical = rec;
			initComponents();
		}
	}
}
