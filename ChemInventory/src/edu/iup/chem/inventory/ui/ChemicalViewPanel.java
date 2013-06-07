/*
 * To change this templileate, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.iup.chem.inventory.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.AtomNumberGenerator;
import org.openscience.cdk.renderer.generators.AtomNumberGenerator.WillDrawAtomNumbers;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.generators.ExtendedAtomGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.RadicalGenerator;
import org.openscience.cdk.renderer.generators.RingGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.iup.chem.inventory.Constants;
import edu.iup.chem.inventory.amount.ChemicalDensity;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalCold;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalFlamm;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;
import edu.iup.chem.inventory.search.ChemicalSubstructureSearcher;
import edu.iup.chem.inventory.search.ChemicalWebSearch;
import edu.iup.chem.inventory.search.SpectraResult;

/**
 * 
 * @author Zach
 */
public class ChemicalViewPanel extends JScrollPane {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -5239291987178761103L;

	private static Icon[] getImageIcons(final Image[] structures) {
		final Icon[] icons = new ImageIcon[structures.length];
		for (int i = 0; i < structures.length; i++) {
			icons[i] = new ImageIcon(structures[i]);
		}

		return icons;
	}

	public static Image[] renderMolecule(final IAtomContainer molecule)
			throws CDKException {
		final CDKHydrogenAdder adder = CDKHydrogenAdder
				.getInstance(DefaultChemObjectBuilder.getInstance());
		adder.addImplicitHydrogens(molecule);
		final StructureDiagramGenerator sdg = new StructureDiagramGenerator();
		final IAtomContainerSet molecules = new AtomContainerSet();
		if (!ConnectivityChecker.isConnected(molecule)) {

			for (final IAtomContainer m : ConnectivityChecker
					.partitionIntoMolecules(molecule).atomContainers()) {
				sdg.setMolecule(m, false);
				sdg.generateExperimentalCoordinates();
				molecules.addAtomContainer(sdg.getMolecule());
			}
		} else {
			sdg.setMolecule(molecule, false);
			sdg.generateCoordinates();
			molecules.addAtomContainer(sdg.getMolecule());
		}

		final List<Image> images = new ArrayList<>();
		for (final IAtomContainer m : molecules.atomContainers()) {

			final int moleculeDiameter = PathTools.getMolecularGraphDiameter(m);
			log.debug("Molecule diameter: " + moleculeDiameter);
			int sideLength = (int) ((moleculeDiameter + 0.5) * new BasicBondGenerator.BondLength()
					.getDefault());
			if (sideLength > Constants.VERT_HALF_SCREEN_SIZE.width / 3) {
				sideLength = Constants.VERT_HALF_SCREEN_SIZE.width / 3;
			}
			log.debug("Side length: " + sideLength);
			final int DRAW_WIDTH = sideLength;
			final int DRAW_HEIGHT = sideLength;
			// the draw area and the image should be the same size
			final Rectangle drawArea = new Rectangle(DRAW_WIDTH, DRAW_HEIGHT);
			final Image image = new BufferedImage(DRAW_WIDTH, DRAW_WIDTH,
					BufferedImage.TYPE_INT_ARGB);
			// generators make the image elements
			final List<IGenerator<IAtomContainer>> generators = new ArrayList<>();
			generators.add(new BasicSceneGenerator());
			generators.add(new BasicBondGenerator());
			generators.add(new ExtendedAtomGenerator());
			generators.add(new AtomNumberGenerator());
			// generators.add(new BasicAtomGenerator());
			generators.add(new RadicalGenerator());
			generators.add(new RingGenerator());
			// the renderer needs to have a toolkit-specific font manager
			final AtomContainerRenderer renderer = new AtomContainerRenderer(
					generators, new AWTFontManager());
			final RendererModel model = renderer.getRenderer2DModel();
			model.set(BasicSceneGenerator.BackgroundColor.class,
					UIManager.getColor("Panel.background"));
			model.set(WillDrawAtomNumbers.class, Boolean.FALSE);
			// the call to 'setup' only needs to be done on the first paint
			renderer.setup(m, drawArea);
			// paint the background
			final Graphics2D g2 = (Graphics2D) image.getGraphics();
			g2.setBackground(Color.WHITE);
			g2.setColor(Color.WHITE);
			g2.setPaint(Color.WHITE);
			g2.fillRect(0, 0, WIDTH, HEIGHT);
			// the paint method also needs a toolkit-specific renderer
			renderer.paint(m, new AWTDrawVisitor(g2), drawArea, false);
			images.add(image);
		}

		// Collections.reverse(images);

		return images.toArray(new Image[images.size()]);
	}

	public static Image[] renderMolecule(final String SMILES)
			throws CDKException {
		return renderMolecule(ChemicalSubstructureSearcher
				.getMoleculeFromSMILES(SMILES));
	}

	private ChemicalRecord				chemical;
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private final javax.swing.JLabel	casLabel;
	private final javax.swing.JLabel	coldLabel;
	private final javax.swing.JLabel	flammLabel;
	private final javax.swing.JLabel	formulaLabel;
	private javax.swing.JPanel			jPanel1;
	private javax.swing.JScrollPane		jScrollPane1;
	private final javax.swing.JLabel	nameLabel;
	private final FireDiamondLabel		nfpaLabel;
	private final javax.swing.JLabel	toxicityLabel;
	private final JLabel				meltingLabel;
	private final JLabel				boilingLabel;
	private final JLabel				densityLabel;
	private final JLabel				massLabel;
	private final JLabel				classLabel;
	private final Map<String, File>		foundSpectra	= new HashMap<>();

	private static final Logger			log				= Logger.getLogger(ChemicalViewPanel.class);

	public ChemicalViewPanel() {
		chemical = new ChemicalRecord();
		nameLabel = new javax.swing.JLabel();
		formulaLabel = new javax.swing.JLabel();
		casLabel = new javax.swing.JLabel();
		nfpaLabel = new FireDiamondLabel();
		toxicityLabel = new javax.swing.JLabel();
		flammLabel = new javax.swing.JLabel();
		coldLabel = new javax.swing.JLabel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jPanel1 = new javax.swing.JPanel();
		meltingLabel = new JLabel();
		boilingLabel = new JLabel();
		densityLabel = new JLabel();
		massLabel = new JLabel();
		classLabel = new JLabel();
	}

	// End of variables declaration//GEN-END:variables
	public ChemicalViewPanel(final ChemicalRecord rec) {
		chemical = rec;
		nameLabel = new javax.swing.JLabel();
		formulaLabel = new javax.swing.JLabel();
		casLabel = new javax.swing.JLabel();
		nfpaLabel = new FireDiamondLabel(rec);
		toxicityLabel = new javax.swing.JLabel();
		flammLabel = new javax.swing.JLabel();
		coldLabel = new javax.swing.JLabel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jPanel1 = new javax.swing.JPanel();
		meltingLabel = new JLabel();
		boilingLabel = new JLabel();
		densityLabel = new JLabel();
		massLabel = new JLabel();
		classLabel = new JLabel();
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
		nfpaLabel = new FireDiamondLabel(rec);
		toxicityLabel = new javax.swing.JLabel();
		flammLabel = new javax.swing.JLabel();
		coldLabel = new javax.swing.JLabel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jPanel1 = new javax.swing.JPanel();
		meltingLabel = new JLabel();
		boilingLabel = new JLabel();
		densityLabel = new JLabel();
		massLabel = new JLabel();
		classLabel = new JLabel();
		initComponents();
	}

	private String getFormulaHTML() {
		return "<html>"
				+ chemical.getFormulaHTML() + "</html>";
	}

	public List<SpectraResult> getSpectra() {
		List<SpectraResult> spectra = new ArrayList<>();
		if (chemical.getCsid() != null) {
			spectra = ChemicalWebSearch.getSpectraFromCSID(chemical.getCsid());
		}

		return spectra;
	}

	public Image[] getStructureImage() {
		final int DRAW_WIDTH = 400;
		final int DRAW_HEIGHT = 400;
		// the draw area and the image should be the same size
		Image[] images = new Image[] { new BufferedImage(DRAW_WIDTH,
				DRAW_HEIGHT, BufferedImage.TYPE_INT_RGB) };

		if (chemical != null) {
			try {
				images = renderMolecule(chemical.getMolecule());
			} catch (final CDKException e) {
				// Failed to generate an image, so we'll try to download one
				if (chemical.getCsid() != null) {
					images[0] = ChemicalWebSearch
							.getStructureThumbnailFromCSID(chemical.getCsid());
				}
			}
		}
		return images;
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

		// setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		nameLabel.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
		nameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		nameLabel.setText(chemical.getName());

		formulaLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
		formulaLabel.setText(getFormulaHTML());

		casLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		casLabel.setText("CAS: " + chemical.getCas());

		massLabel.setText(chemical.getMolarMass());
		massLabel.setHorizontalAlignment(SwingConstants.CENTER);

		final List<JLabel> infoLabels = new ArrayList<>();

		final String classString = chemical.getStorageClass().getClassLetter()
				+ (chemical.isColdStorage() ? "-C" : "")
				+ (chemical.isFlammable() ? "-F" : "");
		final String storageLabel = String.format("Storage class: %s (%s)",
				classString, chemical.getStorageClass().getLiteral());
		classLabel.setText(storageLabel);
		infoLabels.add(classLabel);

		toxicityLabel.setText("Toxicity: " + chemical.getToxic().getLiteral());
		toxicityLabel.setToolTipText(chemical.getToxic().getDescription());

		infoLabels.add(toxicityLabel);

		if (chemical.getFlamm().equals(ChemicalFlamm.Yes)) {
			flammLabel.setText("This chemical is flammable.");
			infoLabels.add(flammLabel);
		}

		if (chemical.getCold().equals(ChemicalCold.Yes)) {
			coldLabel.setText("This chemical requires cold storage.");
			infoLabels.add(coldLabel);
		}

		meltingLabel.setText("Melting point: "
				+ chemical.getMeltingPointString());
		infoLabels.add(meltingLabel);
		boilingLabel.setText("Boiling point: "
				+ chemical.getBoilingPointString());
		infoLabels.add(boilingLabel);
		final ChemicalDensity density = chemical.getDensityWithUnits();
		if (density.getUnit().equals("specific gravity")) {
			densityLabel.setText("Specific gravity: "
					+ String.format("%.2f", density.getQuantity() / 1000));
		} else {
			densityLabel
					.setText("Density: " + chemical.getDensity().toString());
		}
		infoLabels.add(densityLabel);

		jPanel1.setLayout(new BorderLayout());

		final JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
		titlePanel.add(nameLabel);
		titlePanel.add(casLabel);
		titlePanel.add(formulaLabel);
		titlePanel.add(massLabel);

		jPanel1.add(titlePanel, BorderLayout.PAGE_START);

		final JPanel bodyPanel = new JPanel();
		bodyPanel.setLayout(new BorderLayout());

		final JPanel structurePanel = new JPanel();
		structurePanel
				.setLayout(new BoxLayout(structurePanel, BoxLayout.Y_AXIS));
		final Icon[] icons = getImageIcons(getStructureImage());

		for (final Icon i : icons) {
			structurePanel.add(new JLabel(i, SwingConstants.CENTER));
		}

		structurePanel.repaint();
		// structurePanel.setCollapsed(true);
		// structurePanel.setTitle("Structure Diagram");

		// bodyPanel.add(structurePanel, BorderLayout.PAGE_START);

		final JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

		for (final JLabel lbl : infoLabels) {
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			infoPanel.add(lbl);
		}

		final JLabel disclaimer = new JLabel(
				"NOTE: Always refer to the official SDS before using any chemical.");
		infoPanel.add(disclaimer);

		final JButton viewMSDS = new JButton("View SDS");
		viewMSDS.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				chemical.getMSDS();

			}

		});

		infoPanel.add(viewMSDS);

		final JPanel spectraPanel = new JPanel();
		spectraPanel.setLayout(new BoxLayout(spectraPanel, BoxLayout.X_AXIS));
		final List<SpectraResult> spectra = getSpectra();
		final SpectraFrame f = new SpectraFrame();
		for (final SpectraResult s : spectra) {
			final JButton spectraButton = new JButton(s.getType());
			spectraButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {

					try {
						final URL url = new URL(s.getUrl());
						final File spectraTemp;
						if (foundSpectra.containsKey(s.getCsid() + s.getType())) {
							spectraTemp = foundSpectra.get(s.getCsid()
									+ s.getType());
						} else {
							spectraTemp = readJDX(url);
							foundSpectra.put(s.getCsid() + s.getType(),
									spectraTemp);
						}

						f.openFile(spectraTemp);

						f.pack();
						f.setVisible(true);
					} catch (final MalformedURLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (final IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}

			});

			spectraPanel.add(spectraButton);
		}
		infoPanel.add(spectraPanel);

		final JPanel dataPanel = new JPanel();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
		final JPanel diamondPanel = new JPanel();
		diamondPanel.setLayout(new BoxLayout(diamondPanel, BoxLayout.X_AXIS));
		diamondPanel.add(nfpaLabel);
		// final JButton nfpaButton = new JButton("Show NFPA Diamond");
		// nfpaButton.addActionListener(new ActionListener() {
		//
		// @Override
		// public void actionPerformed(final ActionEvent e) {
		// final JDialog frame = new JDialog();
		// final FireDiamondLabel panel = new FireDiamondLabel(chemical);
		// frame.add(panel);
		// panel.repaint();
		// frame.pack();
		// frame.setModalityType(ModalityType.APPLICATION_MODAL);
		// frame.setVisible(true);
		//
		// }
		//
		// });
		// dataPanel.add(nfpaButton);
		nfpaLabel.repaint();

		final JLabel legend = new JLabel(
				"<html>"
						+ "Blue: Health Hazard<br/>"
						+ "Red: Flammability<br/>"
						+ "Yellow: Reactivity<br/>"
						+ "White: Special Hazards (OX: Oxidizer, <br/>&nbsp;&nbsp;W: Water reactive)"
						+ "</div>" + "</html>");
		legend.setOpaque(true);
		// legend.setBackground(Color.GRAY);
		final int legendWidth = legend.getFontMetrics(legend.getFont())
				.stringWidth("Flammability");
		legend.setSize(new Dimension(legendWidth, legend.getHeight()));
		diamondPanel.add(legend);

		diamondPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		dataPanel.add(diamondPanel);

		infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		dataPanel.add(infoPanel);

		bodyPanel.add(dataPanel, BorderLayout.CENTER);
		bodyPanel.repaint();
		jPanel1.add(bodyPanel, BorderLayout.CENTER);
		jPanel1.repaint();
		final JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		mainPanel.add(jPanel1);
		mainPanel.add(structurePanel);
		final JPanel framePanel = new JPanel();
		framePanel.add(mainPanel);
		setViewportView(framePanel);
		revalidate();
		repaint();
	}// </editor-fold>//GEN-END:initComponents

	protected File readJDX(final URL url) throws IOException {
		final File spectra = File.createTempFile("inventory", ".jdx");
		final URLConnection conn = url.openConnection();
		final InputStream in = conn.getInputStream();

		final OutputStream out = new FileOutputStream(spectra);

		int read = 0;
		final byte[] bytes = new byte[1024];

		while ((read = in.read(bytes)) != -1) {
			out.write(bytes, 0, read);
		}

		in.close();
		out.flush();
		out.close();

		return spectra;
	}

	public void start(final ChemicalRecord rec) {
		if (rec != null) {
			chemical = rec;
			jScrollPane1 = new JScrollPane();
			jPanel1 = new JPanel();
			initComponents();

		}
	}
}
