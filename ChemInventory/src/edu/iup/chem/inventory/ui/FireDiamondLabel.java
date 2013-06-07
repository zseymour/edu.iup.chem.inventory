package edu.iup.chem.inventory.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.log4j.Logger;

import edu.iup.chem.inventory.ConnectionPool;
import edu.iup.chem.inventory.dao.ChemicalDao;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalNfpaS;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;

public class FireDiamondLabel extends JLabel {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 4583895860597073715L;

	private static final Logger	LOG					= Logger.getLogger(FireDiamondLabel.class);

	private static void drawLabel(final Object hazard, final Rectangle rec,
			final Graphics2D g2d) {
		final String label = hazard.toString();
		Point p = rec.getLocation();
		final AffineTransform rotate = AffineTransform.getRotateInstance(Math
				.toRadians(45));
		p = (Point) rotate.transform(p, new Point());
		final int strWidth = (int) g2d.getFontMetrics()
				.getStringBounds(label, g2d).getWidth();
		final int strHeight = (int) g2d.getFontMetrics()
				.getStringBounds(label, g2d).getHeight();
		final int width = rec.width;
		final int dx = width / 2 - strWidth / 2;
		final int dy = width / 2 - strHeight / 2;
		p.translate(-dx, 22);
		g2d.drawString(label, p.x, p.y);
	}

	public static void main(final String[] args) {
		final JFrame frame = new JFrame();
		ConnectionPool.initializePool();

		final ChemicalRecord r = new ChemicalDao().getAll().get(0);
		final FireDiamondLabel panel = new FireDiamondLabel(r);
		frame.add(panel);
		panel.repaint();
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private int				healthHazard	= 0;
	private int				fireHazard		= 0;
	private int				reactHazard		= 0;
	private String			specialHazard	= "";
	private int				startX			= 0;
	private int				startY			= 0;
	private int				sideLength		= 0;
	private final Color		healthColor		= Color.BLUE;
	private final Color		fireColor		= Color.RED;

	private final Color		reactColor		= Color.YELLOW;

	private final Color		specialColor	= Color.WHITE;

	private final Dimension	size			= new Dimension(105, 105);

	public FireDiamondLabel() {
		super();
		setPreferredSize(size);
	}

	public FireDiamondLabel(final ChemicalRecord chemical) {
		super();
		healthHazard = chemical.getNfpaH();
		fireHazard = chemical.getNfpaF();
		reactHazard = chemical.getNfpaR();
		specialHazard = chemical.getNfpaS().getLiteral();
		setPreferredSize(size);
		initComponents();
	}

	public FireDiamondLabel(final int healthHazard, final int fireHazard,
			final int reactHazard, final String specialHazard) {
		super();
		this.healthHazard = healthHazard;
		this.fireHazard = fireHazard;
		this.reactHazard = reactHazard;
		this.specialHazard = specialHazard;
		setPreferredSize(size);
		initComponents();
	}

	public void drawDiamond(final Graphics2D g2d) {
		startX = size.width / 2;
		startY = -30;
		sideLength = size.width / 4;

		g2d.setPaint(Color.BLACK);
		final Rectangle outline = new Rectangle(startX, startY, 2 * sideLength,
				2 * sideLength);

		final Rectangle healthRectangle = new Rectangle(startX, startY
				+ sideLength, sideLength, sideLength);
		final Rectangle fireRectangle = new Rectangle(startX, startY,
				sideLength, sideLength);
		final Rectangle reactRectangle = new Rectangle(startX + sideLength,
				startY, sideLength, sideLength);
		final Rectangle specialRectangle = new Rectangle(startX + sideLength,
				startY + sideLength, sideLength, sideLength);

		// Rotate so that our square is a diamond
		g2d.rotate(Math.toRadians(45));

		// Draw the bounding boxes
		g2d.draw(outline);
		g2d.draw(healthRectangle);
		g2d.draw(fireRectangle);
		g2d.draw(reactRectangle);
		g2d.draw(specialRectangle);

		// fill them in
		g2d.setPaint(healthColor);
		g2d.fill(healthRectangle);
		g2d.setPaint(fireColor);
		g2d.fill(fireRectangle);
		g2d.setPaint(reactColor);
		g2d.fill(reactRectangle);
		g2d.setPaint(specialColor);
		g2d.fill(specialRectangle);

		// Rotate back
		g2d.rotate(Math.toRadians(-45));
		// Set font
		final Font f = new Font("Tacoma", Font.BOLD, 18);
		g2d.setFont(f);
		// Draw the white numbers
		g2d.setPaint(Color.WHITE);
		drawLabel(healthHazard, healthRectangle, g2d);
		drawLabel(fireHazard, fireRectangle, g2d);
		// Draw the black numbers
		g2d.setPaint(Color.BLACK);
		drawLabel(reactHazard, reactRectangle, g2d);
		if (!ChemicalNfpaS.None.getLiteral().equals(specialHazard)) {
			drawLabel(specialHazard, specialRectangle, g2d);
		}
	}

	public void initComponents() {
		setIcon(null);
		final Image image = new BufferedImage(size.width, size.height,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = (Graphics2D) image.getGraphics();
		drawDiamond(g2d);

		setIcon(new ImageIcon(image));
	}

	// @Override
	// public void paintComponent(final Graphics g) {
	// LOG.debug("Drawing fire diamond");
	// super.paintComponent(g);
	// final Graphics2D g2d = (Graphics2D) g;
	//
	// drawDiamond(g2d);
	//
	// }

	public void redraw(final ChemicalRecord chemical) {
		healthHazard = chemical.getNfpaH();
		fireHazard = chemical.getNfpaF();
		reactHazard = chemical.getNfpaR();
		specialHazard = chemical.getNfpaS().getLiteral();
		initComponents();
		revalidate();
		repaint();
	}
}
