package edu.iup.chem.inventory.reporting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;

import edu.iup.chem.inventory.Utils;
import edu.iup.chem.inventory.dao.LocationDao;
import edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord;

public class GasReportGenerator {

	private static final String[]	regularHeaders		= new String[] {
			"Barcode (Bottle Number)", "Product Name", "Arrival Date",
			"Departure Date", "Months", "Gas Cost", "Rental Fee",
			"Cost of Rental"							};

	private static final String[]	totalHeaders		= new String[] {
			"Department", "Total Cost"					};

	private static final String[]	productHeaders		= new String[] {
			"Product", "Gas Cost", "Rental Fee"		};

	private static final DateFormat	df					= new SimpleDateFormat(
																"MM/dd/yyyy");
	private static final String		monthFormulaFormat	= "MAX(1,(YEAR(D%d)-YEAR(C%d))*12+MONTH(D%d)-MONTH(C%d))";

	private static final Logger		LOG					= Logger.getLogger(GasReportGenerator.class);

	public static boolean createReport(final Date start, final Date end,
			final File tempFile) {
		final Workbook wb = new HSSFWorkbook();
		final Map<String, CellStyle> styles = createStyles(wb);
		final Map<String, Name> totals = new HashMap<>();
		final Map<String, Name> gasCosts = new HashMap<>();
		final Map<String, Name> rentalCosts = new HashMap<>();

		final String titleFormat = "%s - Gas Cost " + df.format(start) + " - "
				+ df.format(end);
		final String[] departments = LocationDao.getGasDepartments(start, end);

		boolean returnVal = true;
		if (departments.length > 0) {

			final String[] products = LocationDao.getGasProducts(start, end);

			final Sheet productSheet = wb.createSheet("Products");

			productSheet.setColumnWidth(0, 20 * 256);
			productSheet.setColumnWidth(1, 10 * 256);
			productSheet.setColumnWidth(2, 15 * 256);

			final Row titleRow = productSheet.createRow(0);
			titleRow.setHeightInPoints(45);
			final Cell titleCell = titleRow.createCell(0);
			titleCell.setCellValue("Product Costs");
			titleCell.setCellStyle(styles.get("title"));
			productSheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$C$1"));

			final Row headerRow = productSheet.createRow(1);
			headerRow.setHeightInPoints(20);
			Cell headerCell;
			for (int i = 0; i < productHeaders.length; i++) {
				headerCell = headerRow.createCell(i);
				headerCell.setCellValue(productHeaders[i]);
				headerCell.setCellStyle(styles.get("header"));
			}

			int rownum = 1;
			for (final String product : products) {
				final Name gasName = wb.createName();
				final Name rentalName = wb.createName();

				final Row productRow = productSheet.createRow(++rownum);

				final Cell productCell = productRow.createCell(0);
				productCell.setCellValue(product);
				productCell.setCellStyle(styles.get("cell"));

				final Cell gasCell = productRow.createCell(1);
				gasCell.setCellStyle(styles.get("money"));

				final Cell rentalCell = productRow.createCell(2);
				rentalCell.setCellStyle(styles.get("money"));

				gasName.setNameName(product.replaceAll("\\s+", "") + "Gas");
				gasName.setRefersToFormula("Products!B"
						+ (gasCell.getRowIndex() + 1));
				gasCosts.put(product, gasName);

				rentalName.setNameName(product.replaceAll("\\s+", "")
						+ "Rental");
				rentalName.setRefersToFormula("Products!C"
						+ (rentalCell.getRowIndex() + 1));
				rentalCosts.put(product, rentalName);
			}

			for (final String dept : departments) {
				final Sheet deptSheet = wb.createSheet(WorkbookUtil
						.createSafeSheetName(dept));
				final PrintSetup printSetup = deptSheet.getPrintSetup();
				printSetup.setLandscape(true);

				deptSheet.setFitToPage(true);
				deptSheet.setHorizontallyCenter(true);

				deptSheet.setColumnWidth(0, 30 * 256);
				deptSheet.setColumnWidth(1, 15 * 256);
				deptSheet.setColumnWidth(2, 15 * 256);
				deptSheet.setColumnWidth(3, 20 * 256);
				deptSheet.setColumnWidth(4, 9 * 256);
				deptSheet.setColumnWidth(5, 10 * 256);
				deptSheet.setColumnWidth(6, 15 * 256);
				deptSheet.setColumnWidth(7, 15 * 256);

				// title row
				final Row titleRow2 = deptSheet.createRow(0);
				titleRow2.setHeightInPoints(45);
				final Cell titleCell2 = titleRow2.createCell(0);
				titleCell2.setCellValue(String.format(titleFormat, dept));
				titleCell2.setCellStyle(styles.get("title"));
				deptSheet
						.addMergedRegion(CellRangeAddress.valueOf("$A$1:$I$1"));

				// header row
				final Row headerRow2 = deptSheet.createRow(1);
				headerRow2.setHeightInPoints(20);
				Cell headerCell2;
				for (int i = 0; i < regularHeaders.length; i++) {
					headerCell2 = headerRow2.createCell(i);
					headerCell2.setCellValue(regularHeaders[i]);
					headerCell2.setCellStyle(styles.get("header"));
				}

				final List<LocationRecord> departmentRecords = LocationDao
						.getGasBottlesByDepartmentInRange(dept, start, end);

				rownum = 1;
				for (final LocationRecord rec : departmentRecords) {
					final Row row = deptSheet.createRow(++rownum);
					final int realRow = rownum + 1;
					final Cell bottleCell = row.createCell(0);
					bottleCell.setCellValue(rec.getBottle());
					bottleCell.setCellStyle(styles.get("cell"));

					final Cell productCell = row.createCell(1);
					productCell.setCellValue(rec.getName());
					productCell.setCellStyle(styles.get("cell"));

					final Cell arrivalCell = row.createCell(2);
					arrivalCell.setCellValue(new Date(rec.getArrival()
							.getTime()));
					arrivalCell.setCellStyle(styles.get("date"));

					final Cell departureCell = row.createCell(3);
					departureCell.setCellValue(new Date(rec.getExpiration()
							.getTime()));
					departureCell.setCellStyle(styles.get("date"));

					final Cell monthCell = row.createCell(4);
					monthCell.setCellFormula(String.format(monthFormulaFormat,
							realRow, realRow, realRow, realRow));
					monthCell.setCellStyle(styles.get("formula"));

					final Cell gasCell = row.createCell(5);
					gasCell.setCellFormula(gasCosts.get(rec.getName())
							.getRefersToFormula());
					gasCell.setCellStyle(styles.get("money_formula"));

					final Cell feeCell = row.createCell(6);
					final String feeFormula = rentalCosts.get(rec.getName())
							.getRefersToFormula();
					LOG.debug(feeFormula);
					feeCell.setCellFormula(feeFormula);
					feeCell.setCellStyle(styles.get("money_formula"));

					final Cell rentalCell = row.createCell(7);
					rentalCell.setCellFormula("E" + realRow + "*G" + realRow);
					rentalCell.setCellStyle(styles.get("money_formula"));
				}

				deptSheet.createRow(++rownum);
				final Row subtotalRow = deptSheet.createRow(++rownum);

				final Cell subtotalLabel = subtotalRow.createCell(4);
				subtotalLabel.setCellValue("Subtotal");
				subtotalLabel.setCellStyle(styles.get("cell"));

				final Cell gasSubTotalCell = subtotalRow.createCell(5);
				String cellRange = "F3:F" + (3 + departmentRecords.size() - 1);
				gasSubTotalCell.setCellFormula("SUM(" + cellRange + ")");
				gasSubTotalCell.setCellStyle(styles.get("money_formula"));

				final Cell rentalSubTotalCell = subtotalRow.createCell(7);
				cellRange = "H3:H" + (3 + departmentRecords.size() - 1);
				rentalSubTotalCell.setCellFormula("SUM(" + cellRange + ")");
				rentalSubTotalCell.setCellStyle(styles.get("money_formula"));

				deptSheet.createRow(++rownum);
				final Row totalRow = deptSheet.createRow(++rownum);

				final Cell totalLabel = totalRow.createCell(4);
				totalLabel.setCellValue("Total");
				totalLabel.setCellStyle(styles.get("total"));

				final Cell totalCell = totalRow.createCell(5);
				totalCell.setCellFormula("F"
						+ (gasSubTotalCell.getRowIndex() + 1) + "+H"
						+ (rentalSubTotalCell.getRowIndex() + 1));
				totalCell.setCellStyle(styles.get("total_formula"));

				final Name totalName = wb.createName();
				totalName.setNameName(dept.replaceAll("\\s+", "") + "Total");
				totalName.setRefersToFormula(deptSheet.getSheetName() + "!F"
						+ (totalCell.getRowIndex() + 1));

				totals.put(dept, totalName);
			}

			final Sheet totalSheet = wb.createSheet("Totals");
			totalSheet.setColumnWidth(0, 15 * 256);
			totalSheet.setColumnWidth(1, 15 * 256);

			final Row titleRow3 = totalSheet.createRow(0);
			titleRow3.setHeightInPoints(45);
			final Cell titleCell3 = titleRow3.createCell(0);
			titleCell3.setCellValue(String.format(titleFormat,
					"All Departments"));
			titleCell3.setCellStyle(styles.get("title"));
			totalSheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$H$1"));

			// header row
			final Row headerRow3 = totalSheet.createRow(1);
			headerRow3.setHeightInPoints(20);
			Cell headerCell3;
			for (int i = 0; i < totalHeaders.length; i++) {
				headerCell3 = headerRow3.createCell(i);
				headerCell3.setCellValue(totalHeaders[i]);
				headerCell3.setCellStyle(styles.get("header"));
			}

			rownum = 1;
			for (final String dept : departments) {
				final Row deptRow = totalSheet.createRow(++rownum);
				final int realRow = rownum + 1;

				final Cell deptCell = deptRow.createCell(0);
				deptCell.setCellValue(dept);
				deptCell.setCellStyle(styles.get("cell"));

				final Cell totalCell = deptRow.createCell(1);
				totalCell.setCellFormula(totals.get(dept).getRefersToFormula());
				totalCell.setCellStyle(styles.get("money_formula"));

			}

			totalSheet.createRow(++rownum);
			final Row totalRow = totalSheet.createRow(++rownum);

			final Cell totalLabel = totalRow.createCell(0);
			totalLabel.setCellValue("Overall Cost");
			totalLabel.setCellStyle(styles.get("total"));

			final Cell totalCell = totalRow.createCell(1);
			final String cellRange = "B3:B" + (3 + departments.length - 1);
			totalCell.setCellFormula("SUM(" + cellRange + ")");
			totalCell.setCellStyle(styles.get("total_formula"));
		} else {
			LOG.info("No bottles were found.");
			returnVal = false;
		}

		try (final FileOutputStream fileOut = new FileOutputStream(tempFile);) {
			wb.write(fileOut);
		} catch (final IOException e) {
			LOG.error("Could not write gas report.", e);
			Utils.showMessage("Warning", "Could not generate gas report.");
			returnVal = false;
		}

		LOG.info("Successfully generated gas report.");

		return returnVal;

	}

	private static Map<String, CellStyle> createStyles(final Workbook wb) {
		final Map<String, CellStyle> styles = new HashMap<>();

		CellStyle style;

		final Font titleFont = wb.createFont();
		titleFont.setFontHeightInPoints((short) 18);
		titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFont(titleFont);
		styles.put("title", style);

		final Font monthFont = wb.createFont();
		monthFont.setFontHeightInPoints((short) 11);
		monthFont.setColor(IndexedColors.WHITE.getIndex());
		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setFont(monthFont);
		style.setWrapText(true);
		styles.put("header", style);

		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setWrapText(true);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		styles.put("cell", style);

		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styles.put("formula", style);

		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setDataFormat(wb.createDataFormat().getFormat("m/d/yy"));
		styles.put("date", style);

		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setWrapText(true);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setDataFormat(wb.createDataFormat().getFormat("$##,##0.00"));
		styles.put("money", style);

		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setDataFormat(wb.createDataFormat().getFormat("$##,##0.00"));
		styles.put("money_formula", style);

		final Font totalFont = wb.createFont();
		totalFont.setColor(IndexedColors.WHITE.getIndex());

		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFillForegroundColor(IndexedColors.BLACK.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setFont(totalFont);
		styles.put("total", style);

		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFillForegroundColor(IndexedColors.BLACK.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setDataFormat(wb.createDataFormat().getFormat("$##,##0.00"));
		style.setFont(totalFont);
		styles.put("total_formula", style);

		return styles;
	}
}
