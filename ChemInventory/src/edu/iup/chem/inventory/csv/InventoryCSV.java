package edu.iup.chem.inventory.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import edu.iup.chem.inventory.Utils;

public class InventoryCSV {
	private static final Logger	LOG	= Logger.getLogger(InventoryCSV.class);

	private static CSVReader createReader(final File csv)
			throws FileNotFoundException {
		FileReader reader;
		reader = new FileReader(csv);
		return new CSVReader(reader);
	}

	public static List<CSVBottle> loadBottlesFromFile(final File csv,
			final String room) {
		final List<String[]> rows = InventoryCSV.loadFile(csv);
		final List<CSVBottle> bottles = new ArrayList<>();
		for (final String[] row : rows) {
			if (row[0].isEmpty() || row[1].isEmpty()) {
				continue;
			}

			final CSVBottle b = new CSVBottle(row[0].trim(), row[1].trim(),
					row[2].trim(), room, row[3].isEmpty() ? "A1"
							: row[3].trim(), Integer.parseInt(row[4].trim()));
			bottles.add(b);
		}

		return bottles;
	}

	public static List<String[]> loadFile(final File csv) {
		try {
			final CSVReader csvReader = createReader(csv);
			return csvReader.readAll();
		} catch (final FileNotFoundException e) {
			Utils.showMessage("File not found.",
					"CSV file unavaible for reading.");
		} catch (final IOException e) {
			LOG.error("Problem reading CSV file.", e.getCause());
		}

		return new ArrayList<>();

	}

}
