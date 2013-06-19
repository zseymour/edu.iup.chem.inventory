package edu.iup.chem.inventory.reporting;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

import org.apache.log4j.Logger;

import edu.iup.chem.inventory.ConnectionPool;

public class ReportGenerator {
	public enum ReportType {
		WASTE, ROOM_126A, ROOM_146
	}

	private static void generateReport(final Connection conn,
			final HashMap params, final JasperReport report) {
		try {
			final JasperPrint printer = JasperFillManager.fillReport(report,
					params, conn);
			// JasperViewer viewer = new JasperViewer(printer);
			JasperViewer.viewReport(printer, false);
		} catch (final JRException e) {
			LOG.error("Could not generate report.", e);
		}
	}

	private JasperReport		wasteReport;
	private JasperReport		storageReport;
	private JasperReport		roomReport;
	private JasperReport		room126Report;

	private JasperReport		room146Report;

	private static final Logger	LOG	= Logger.getLogger(ReportGenerator.class);

	public ReportGenerator() {
		try {
			wasteReport = JasperCompileManager.compileReport(getClass()
					.getResourceAsStream("reports/waste.jrxml"));
			storageReport = JasperCompileManager.compileReport(getClass()
					.getResourceAsStream("reports/shelf_classes.jrxml"));
			roomReport = JasperCompileManager.compileReport(getClass()
					.getResourceAsStream("reports/room_chemical.jrxml"));
			room126Report = JasperCompileManager.compileReport(getClass()
					.getResourceAsStream("reports/126.jrxml"));
			room146Report = JasperCompileManager.compileReport(getClass()
					.getResourceAsStream("reports/146.jrxml"));

		} catch (final JRException e) {
			LOG.error("Could not compile report.", e.getCause());
		}
	}

	public void generateReport(final ReportType type) {
		JasperReport report;
		switch (type) {
			case ROOM_126A:
				report = room126Report;
				break;
			case ROOM_146:
				report = room146Report;
				break;
			case WASTE:
				report = wasteReport;
				break;
			default:
				report = room146Report;
				break;
		}

		final HashMap params = new HashMap();

		try (Connection conn = ConnectionPool.getConnection()) {
			generateReport(conn, params, report);
		} catch (final SQLException e) {
			LOG.warn("Error establishing connection to generate report.");
		}

	}

	public void generateRoomReport(final String room) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final HashMap params = new HashMap();
			params.put("room", room);
			generateReport(conn, params, roomReport);
		} catch (final SQLException e) {
			LOG.warn("Error establishing connection for room report.");
		}
	}

	public void generateStorageReport(final String room) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final HashMap storageParams = new HashMap();
			final JasperReport subReport = JasperCompileManager
					.compileReport(getClass().getResourceAsStream(
							"reports/shelf_classes_subreport1.jrxml"));
			final JasperReport subReport2 = JasperCompileManager
					.compileReport(getClass()
							.getResourceAsStream(
									"reports/shelf_classes_subreport1_subreport1.jrxml"));
			storageParams.put("room", room);
			storageParams.put("subreport", subReport);
			storageParams.put("subreport2", subReport2);
			generateReport(conn, storageParams, storageReport);
		} catch (final SQLException e) {
			LOG.warn("Error establishing connection for storage report.");
		} catch (final JRException e) {
			LOG.error("Could not compile report.", e);
		}
	}

}
