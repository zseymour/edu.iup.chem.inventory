package edu.iup.chem.inventory.search;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chemspider.www.MassSpecAPIStub;
import com.chemspider.www.MassSpecAPIStub.ArrayOfInt;
import com.chemspider.www.MassSpecAPIStub.ExtendedCompoundInfo;
import com.chemspider.www.MassSpecAPIStub.GetExtendedCompoundInfoArray;
import com.chemspider.www.MassSpecAPIStub.GetRecordMol;
import com.chemspider.www.SearchStub;
import com.chemspider.www.SearchStub.GetCompoundThumbnail;
import com.chemspider.www.SearchStub.SimpleSearch;
import com.chemspider.www.SpectraStub;
import com.chemspider.www.SpectraStub.CSSpectrumInfo;
import com.chemspider.www.SpectraStub.GetCompoundSpectraInfo;

import edu.iup.chem.inventory.Constants;
import edu.iup.chem.inventory.amount.ChemicalAmount;
import edu.iup.chem.inventory.amount.ChemicalAmountFactory;
import edu.iup.chem.inventory.amount.ChemicalDensity;
import edu.iup.chem.inventory.amount.ChemicalMass;
import edu.iup.chem.inventory.com.chemspider.www.SynonymsStub;
import edu.iup.chem.inventory.com.chemspider.www.SynonymsStub.GetStructureSynonyms;
import edu.iup.chem.inventory.dao.ChemicalDao;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalCarc;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalCold;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalFlamm;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalNfpaS;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalStorageClass;
import edu.iup.chem.inventory.db.inventory.enums.ChemicalToxic;
import edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord;
import edu.iup.chem.inventory.lists.comparators.ChemicalAmountComparator;

public class ChemicalWebSearch {

	private final static Logger			LOG			= Logger.getLogger(ChemicalWebSearch.class);
	private static Map<String, String>	hazardTable	= null;

	private static double _fetchBoilingPoint(final String baseURL)
			throws IOException {
		final String content = Jsoup.connect(baseURL + ":bp").get().text();

		LOG.debug("TOXNET Boiling Point Response: " + content);

		final Pattern p = Pattern.compile("([\\d.]+) (deg|DEG)");
		final Matcher m = p.matcher(content);

		while (m.find()) {
			final String bp = m.group(1);
			LOG.debug("Boiling point: " + bp);
			final double boiling = Double.parseDouble(bp);
			return boiling;
		}

		throw new IOException();
	}

	private static ChemicalDensity _fetchDensity(final String baseURL)
			throws IOException, NumberFormatException {
		final String content = Jsoup.connect(baseURL + ":den").get().text();

		LOG.debug("TOXNET Density Response: " + content);

		final Pattern solidPattern = Pattern
				.compile("([\\d.]+) ([A-Za-z]+/[A-Za-z]+ ?[A-Za-z]+)");
		final Matcher solidMatcher = solidPattern.matcher(content);

		final Pattern liquidPattern = Pattern
				.compile("Gravity: ([\\d.]+)(-[\\d.]+)? (at|@)?");
		final Matcher liquidMatcher = liquidPattern.matcher(content);

		if (solidMatcher.find()) {
			final String value = solidMatcher.group(1);
			String unit = solidMatcher.group(2);
			unit = unit.replace("at", "");
			LOG.debug("Density: " + value + " " + unit);

			return (ChemicalDensity) ChemicalAmountFactory.getChemicalAmount(
					value, unit);
		} else if (liquidMatcher.find()) {
			final String gravity = liquidMatcher.group(1);

			LOG.debug("Specific gravity: " + gravity);

			return (ChemicalDensity) ChemicalAmountFactory.getChemicalAmount(
					gravity, "specific gravity");
		}

		throw new NumberFormatException(
				"Density information improperly formatted");
	}

	private static double _fetchFlashPoint(final String baseURL)
			throws IOException {
		final String content = Jsoup.connect(baseURL + ":flpt").get().text();

		LOG.debug("TOXNET Flash Point Response: " + content);

		final Pattern p = Pattern.compile("([\\d.]+) (deg|DEG) F");
		final Matcher m = p.matcher(content);

		while (m.find()) {
			final String fp = m.group(1);
			LOG.debug("Flash point: " + fp);
			return Double.parseDouble(fp);
		}

		throw new IOException();
	}

	private static Double _fetchMeltingPoint(final String baseURL)
			throws IOException {
		final String content = Jsoup.connect(baseURL + ":mp").get().text();

		LOG.debug("TOXNET Melting Point Response: " + content);

		final Pattern p = Pattern.compile("([\\d.]+) (deg|DEG)");
		final Matcher m = p.matcher(content);

		while (m.find()) {
			final String mp = m.group(1);
			LOG.debug("Melting point: " + mp);
			final double melting = Double.parseDouble(mp);
			return melting;
		}

		throw new IOException();
	}

	private static ChemicalMass _getLD50(final String baseURL)
			throws IOException {
		final String content = Jsoup.connect(baseURL + ":ntxv").get().text();
		LOG.debug("TOXNET Toxicity Response: " + content);
		final Pattern p = Pattern
				.compile("LD50 (r|R)at .*? oral (\\d+) ([A-Za-z]+)");
		final Matcher m = p.matcher(content);
		ChemicalMass bestLD50 = (ChemicalMass) ChemicalAmountFactory
				.getChemicalAmount(0.0, "mg");
		while (m.find()) {
			final String amount = m.group(2);
			final String unit = m.group(3);

			LOG.debug("Rat LD50: " + amount + " " + unit);

			final ChemicalAmount ld50 = ChemicalAmountFactory
					.getChemicalAmount(amount, unit);

			if (new ChemicalAmountComparator().compare(ld50, bestLD50) > 0) {
				bestLD50 = (ChemicalMass) ld50;
			}
		}

		return bestLD50;
	}

	/**
	 * We store an array of LD50 amounts corresponding to the toxicity levels.
	 * We then insert the given LD50 into that list, sort it, and based on its
	 * final index, determine the toxicity level of the compound.
	 * 
	 * @param ld50PerKilo
	 * @return
	 */
	public static ChemicalToxic amountToToxicity(final ChemicalMass ld50PerKilo) {
		if (ld50PerKilo.getQuantity() == 0.0) {
			return ChemicalToxic.Unknown;
		}

		final ArrayList<ChemicalMass> levels = new ArrayList<>(
				Constants.LD50_LEVELS);
		levels.add(ld50PerKilo);
		Collections.sort(levels, new ChemicalAmountComparator());

		final int index = levels.indexOf(ld50PerKilo);

		switch (index) {
			case 0:
				return ChemicalToxic.Extremely_toxic;
			case 1:
				return ChemicalToxic.Highly_toxic;
			case 2:
				return ChemicalToxic.Moderately_toxic;
			case 3:
				return ChemicalToxic.Slightly_toxic;
			default:
				return ChemicalToxic.Practically_nontoxic;
		}
	}

	private final static String cleanFormula(final String formula) {
		return formula.replaceAll("[ _{}]", "");
	}

	public static Double fetchBoilingPoint(final String cas) {
		try {
			return _fetchBoilingPoint(getTOXNETBaseUrl(cas));
		} catch (final IOException e) {
			return Double.MAX_VALUE;
		}
	}

	public static ChemicalDensity fetchDensity(final String cas) {
		try {
			return _fetchDensity(getTOXNETBaseUrl(cas));
		} catch (final IOException | NumberFormatException e) {
			return (ChemicalDensity) ChemicalAmountFactory.getChemicalAmount(
					1.0, "specific gravity");
		}
	}

	public static Double fetchFlashPoint(final String cas) {
		Double flash;
		try {
			flash = _fetchFlashPoint(getTOXNETBaseUrl(cas));
		} catch (final IOException e) {
			flash = Double.MAX_VALUE;
		}

		return flash;
	}

	public static Double fetchMeltingPoint(final String cas) {
		try {
			return _fetchMeltingPoint(getTOXNETBaseUrl(cas));
		} catch (final IOException e) {
			return Double.MAX_VALUE;
		}
	}

	private static void fillCancerInformation(final ChemicalRecord r) {
		if (ChemicalDao.isCarcinogenic(r)) {
			r.setCarc(ChemicalCarc.Yes);
		} else {
			r.setCarc(ChemicalCarc.No);
		}

		LOG.debug("Setting "
				+ r.getName()
				+ " to "
				+ (r.getCarc().equals(ChemicalCarc.Yes) ? "carcinogenic"
						: "not carcinogenic"));

	}

	private static void fillHazardInformation(final ChemicalRecord r) {
		r.setNfpaF(0);
		r.setNfpaH(0);
		r.setNfpaR(0);
		r.setNfpaS(ChemicalNfpaS.None);

		try {
			final String url = getWebWiserPage(r.getCas());

			if (url == null) {
				return;
			}

			final Document doc = Jsoup.connect(
					url + "&selectedDataMenuItemID=47").get();
			final Elements hazards = doc.select("div#substanceDataContent")
					.select("b");
			for (final Element e : hazards) {
				final String text = e.text();
				final Integer level = Integer.parseInt(text.replaceAll("[\\D]",
						""));
				if (text.contains("Health")) {
					r.setNfpaH(level);
				} else if (text.contains("Flammability")) {
					r.setNfpaF(level);
				} else if (text.contains("Instability")) {
					r.setNfpaR(level);
				}
			}

			LOG.debug("NFPA Fire Diamond for " + r.getName() + ": "
					+ r.getNfpaH() + " " + r.getNfpaF() + " " + r.getNfpaR()
					+ " " + r.getNfpaS().getLiteral());
		} catch (final IOException e) {
			LOG.debug("Error connecting to WebWISER.", e.getCause());
		} catch (final NumberFormatException e) {
			LOG.debug("Error parsing NFPA codes.");
			return;
		}
	}

	public static void fillHazardInformation(final ChemicalRecord r,
			final String baseURL) throws IOException {
		r.setNfpaF(0);
		r.setNfpaH(0);
		r.setNfpaR(0);
		r.setNfpaS(ChemicalNfpaS.None);

		final String content = Jsoup.connect(baseURL + ":nfpa").get().text();
		LOG.debug("TOXNET Hazard Response: " + content);
		final Pattern p = Pattern.compile("([A-Za-z]+): ([0-9])\\.");
		final Matcher m = p.matcher(content);
		while (m.find()) {
			final String field = m.group(1);
			final String value = m.group(2);
			final Integer intValue = Integer.parseInt(value);
			LOG.debug(field + ": " + value);

			switch (field) {
				case "Health":
					r.setNfpaH(intValue);
					break;
				case "Flammability":
					r.setNfpaF(intValue);
					break;
				case "Instability":
					r.setNfpaR(intValue);
					break;
				default:
					break;
			}

		}

	}

	public static void fillStorageClass(final ChemicalRecord r,
			final String baseURL) {
		ChemicalStorageClass thisClass = ChemicalStorageClass.Unknown;

		final double dotHazard = lookupDOTHazard(r.getName());
		if (dotHazard > 0) {
			thisClass = getStorageFromDOTClass(Double.toString(dotHazard), r);
		}

		switch (thisClass) {
			case Unknown:
			case Inorganic_acids:
				thisClass = testForSubstructures(r);
				break;
			default:
				break;
		}

		r.setStorageClass(thisClass);
	}

	private static void fillStorageTypes(final ChemicalRecord r) {
		r.setCold(ChemicalCold.No);
		r.setFlamm(ChemicalFlamm.No);

		try {
			final String url = getWebWiserPage(r.getCas());
			if (url == null) {
				return;
			}

			// First, boiling point.
			Document doc = Jsoup.connect(url + "&selectedDataMenuItemID=29")
					.get();
			final Elements bpElements = doc.select("div#substanceDataContent")
					.select("div.elementContent");
			if (bpElements.size() > 0) {

				String boilingPointField = bpElements.first().text();
				boilingPointField = boilingPointField.toLowerCase();

				if (!boilingPointField.contains("decomposes")
						&& !boilingPointField.contains("sublimes")) {

					String bp = boilingPointField.substring(0,
							boilingPointField.indexOf("d") - 1);
					int index = bp.indexOf("-", 1);
					if (index > 0) {
						bp = bp.substring(0, index);
					}

					index = bp.indexOf("=");
					if (index > 0) {
						bp = bp.substring(index + 1, bp.length());
					}
					final double boilingPoint = Double.parseDouble(bp);
					if (boilingPoint < 36.0) {
						r.setCold(ChemicalCold.Yes);
						LOG.debug(r.getName() + " needs cold storage.");
					}
				}
			}

			// Now, flash point
			doc = Jsoup.connect(url + "&selectedDataMenuItemID=32").get();
			final Elements fpElements = doc.select("div#substanceDataContent")
					.select("div.elementContent").select("p");

			String fpField = null;

			for (final Element e : fpElements) {
				if (e.text().contains("(closed cup)")) {
					fpField = e.text();
					break;
				}
			}

			if (fpField == null && !fpElements.isEmpty()) {
				fpField = fpElements.first().text();
			} else if (fpField == null) {
				return;
			}

			fpField = fpField.toLowerCase();
			String fp = fpField.substring(0, fpField.indexOf("d"));
			int index = fp.indexOf("-", 1);
			if (index > 0) {
				fp = fp.substring(0, index);
			}

			index = fp.indexOf("=");
			if (index > 0) {
				fp = fp.substring(index + 1, fp.length());
			}
			final double flashPoint = Double.parseDouble(fp);
			// Flash points are in Fahrenheit?
			if (flashPoint <= 100.4) {
				r.setFlamm(ChemicalFlamm.Yes);
				LOG.debug(r.getName() + " is flammable.");
			}

		} catch (final IOException e) {
			LOG.debug("Error connecting to WebWISER.");
		} catch (final NumberFormatException e) {
			return;
		}

	}

	public static void fillStorageTypes(final ChemicalRecord r,
			final String baseURL) throws IOException {
		r.setCold(ChemicalCold.No);
		r.setFlamm(ChemicalFlamm.No);

		double boiling;
		try {
			boiling = _fetchBoilingPoint(baseURL);
		} catch (final IOException e) {
			boiling = Double.MAX_VALUE;
		}

		if (boiling < 36.0) {
			r.setCold(ChemicalCold.Yes);
		}

		double flash;
		try {
			flash = _fetchFlashPoint(baseURL);
		} catch (final IOException e) {
			flash = Double.MAX_VALUE;
		}

		if (flash < 100.4) {
			r.setFlamm(ChemicalFlamm.Yes);
		}

		// adjust NFPA Flammability
		if (flash < 73 && boiling < 37.8) {
			r.setNfpaF(4);
		} else if (flash < 73 && boiling >= 37.8 || flash >= 73 && flash < 100) {
			r.setNfpaF(3);
		} else if (flash >= 100 && flash < 200) {
			r.setNfpaF(2);
		} else if (flash >= 200) {
			r.setNfpaF(1);
		}

	}

	private static void fillToxicityInformation(final ChemicalRecord r) {
		r.setToxic(ChemicalToxic.Practically_nontoxic);
		try {
			final String url = getWebWiserPage(r.getCas());

			if (url == null) {
				return;
			}

			final Document doc = Jsoup.connect(
					url + "&selectedDataMenuItemID=78").get();
			final Elements ld50s = doc.select("div.elementContent").select("p");
			LOG.debug("Got ld50s: " + ld50s.size());

			String ratLD50 = null;
			final String typeName = "LD50 Rat oral";

			for (final Element e : ld50s) {
				final String ld50 = e.text();
				if (ld50.startsWith(typeName)
						&& !ld50.toLowerCase().contains("ml")) {
					ratLD50 = ld50;
					break;
				}
			}

			if (ratLD50 == null) {
				r.setToxic(ChemicalToxic.Practically_nontoxic);
				return;
			}

			ratLD50 = ratLD50.replace("range from", "");
			ratLD50 = ratLD50.replace("=", "");

			ratLD50 = ratLD50.substring(typeName.length() + 1,
					ratLD50.indexOf("/")).trim();

			final String[] fields = ratLD50.split("[\\s]+");
			final int index = fields[0].indexOf("-");
			if (index >= 0) {
				fields[0] = fields[0].substring(0, index);
			}
			fields[0] = fields[0].replace(",", "");
			final ChemicalAmount amount = ChemicalAmountFactory
					.getChemicalAmount(fields[0], fields[1]);
			if (amount instanceof ChemicalMass) {
				final ChemicalMass ld50PerKilo = (ChemicalMass) amount;
				final ChemicalToxic tox = amountToToxicity(ld50PerKilo);

				LOG.debug(r.getName() + " is " + tox.getLiteral().toLowerCase());
				r.setToxic(tox);
			}
		} catch (final IOException e) {
			LOG.debug("Error connection to WebWISER.", e.getCause());
		}
	}

	public static void fillToxicityInformation(final ChemicalRecord r,
			final String baseURL) throws IOException {
		r.setToxic(ChemicalToxic.Unknown);

		final ChemicalAmount ld50 = _getLD50(baseURL);
		final ChemicalToxic tox = amountToToxicity((ChemicalMass) ld50);

		r.setLd50WithUnits((ChemicalMass) ld50);
		if (ld50.getQuantity() != 0 && tox.compareTo(ChemicalToxic.Unknown) > 0) {
			r.setToxic(tox);
		}

	}

	private static void fillTOXNETInformation(final ChemicalRecord record) {
		final String url = getTOXNETBaseUrl(record.getCas());

		try {
			fillHazardInformation(record, url);
			fillToxicityInformation(record, url);
			fillStorageTypes(record, url);
			fillCancerInformation(record);
			fillStorageClass(record, url);

		} catch (final IOException e) {
			LOG.error("Failed to retreive TOXNET information", e.getCause());
		}

	}

	private static List<ChemicalRecord> filterByName(
			final List<ChemicalRecord> records, final String name) {
		final List<ChemicalRecord> listByName = searchByName(name);
		final List<ChemicalRecord> matchedRecords = new ArrayList<>();
		if (listByName != null) {
			for (final ChemicalRecord r : records) {
				for (final ChemicalRecord n : listByName) {
					if (r.getCsid().equals(n.getCsid())) {
						if (r.getName() == null) {
							r.setName(n.getName());
						}
						matchedRecords.add(r);
					}
				}
			}
		}

		return matchedRecords;
	}

	public static String getCSID(final String identifier) {
		final String cirURL = String.format(Constants.CIR_URL_FORMAT,
				identifier, "chemspider_id");

		String csid = null;

		try {
			final URL url = new URL(cirURL);
			final HttpURLConnection conn = (HttpURLConnection) url
					.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();

			if (conn.getResponseCode() == 200) {
				final BufferedReader buf = new BufferedReader(
						new InputStreamReader(conn.getInputStream()));
				csid = buf.readLine();
			}
		} catch (final MalformedURLException e) {
			LOG.error("Improperly formatted CIR URL", e.getCause());
		} catch (final IOException e) {
			LOG.error("Error opening CIR for reading.", e.getCause());
		}

		if (csid == null) {
			csid = getCSIDfromChemSpider(identifier);
		}

		return csid;

	}

	private static String getCSIDfromChemSpider(final String cas) {
		String csid = null;
		try {
			final SearchStub searchStub = new SearchStub();
			final SimpleSearch query = new SimpleSearch();
			query.setQuery(cas);
			query.setToken(Constants.CHEMSPIDER_TOKEN);
			final int[] csids = searchStub.simpleSearch(query)
					.getSimpleSearchResult().get_int();

			if (csids != null) {
				csid = Integer.toString(csids[0]);
			}
		} catch (final RemoteException e) {
			LOG.debug("Error fetching CSID from ChemSpider.");
		}

		return csid;
	}

	public static ChemicalMass getLD50(final String cas) {
		try {
			return _getLD50(getTOXNETBaseUrl(cas));
		} catch (final IOException e) {
			return (ChemicalMass) ChemicalAmountFactory.getChemicalAmount(0.0,
					"mg");
		}
	}

	public static String getNames(final String identifier) {
		final String cirURL = String.format(Constants.CIR_URL_FORMAT,
				identifier, "names");

		String names = null;
		try {
			final URL url = new URL(cirURL);
			final HttpURLConnection conn = (HttpURLConnection) url
					.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();

			if (conn.getResponseCode() == 200) {
				final BufferedReader buf = new BufferedReader(
						new InputStreamReader(conn.getInputStream()));
				final StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = buf.readLine()) != null) {
					sb.append(line);
					sb.append(" ");
				}

				names = sb.toString();
			}

		} catch (final MalformedURLException e) {
			LOG.error("Improperly formatted CIR URL", e.getCause());
		} catch (final IOException e) {
			LOG.error("Error opening CIR for reading.", e.getCause());
		}

		return names;
	}

	public static ChemicalRecord getNewChemical(final String cas, String name) {
		List<ChemicalRecord> records = searchByCAS(cas);

		if (records.size() > 1) {
			// Strip everything after a comma, which may be "lab grade",
			// "anhydrous", etc.
			name = name.substring(0, name.indexOf(","));
			records = filterByName(records, name);
		}

		if (records.isEmpty()) {
			return null;
		}

		final ChemicalRecord rec = records.get(0);

		rec.setName(name);

		return rec;
	}

	public static List<SpectraResult> getSpectraFromCSID(final String csid) {
		final List<SpectraResult> results = new ArrayList<>();
		try {
			final Set<String> types = new HashSet<>();
			final SpectraStub spectraSearch = new SpectraStub();
			final GetCompoundSpectraInfo getSpectra = new GetCompoundSpectraInfo();
			getSpectra.setCsid(Integer.parseInt(csid));
			getSpectra.setToken(Constants.CHEMSPIDER_TOKEN);
			final CSSpectrumInfo[] spectra = spectraSearch
					.getCompoundSpectraInfo(getSpectra)
					.getGetCompoundSpectraInfoResult().getCSSpectrumInfo();
			LOG.trace(spectra == null ? 0 : spectra.length
					+ " spectra returned");
			int i = 1;

			if (spectra != null) {
				for (final CSSpectrumInfo info : spectra) {
					LOG.trace(String.format("%d. %s -- %s", i++,
							info.getSpc_type(), info.getSpc_id()));

					final String type = info.getSpc_type();
					if (types.contains(type)) {
						continue;
					}

					types.add(type);

					final String imageURL = String
							.format("http://www.chemspider.com/FilesHandler.ashx?type=blob&disp=1&id=%s",
									info.getSpc_id());
					final SpectraResult r = new SpectraResult(csid, type,
							imageURL);

					results.add(r);
				}
			}

		} catch (final AxisFault e) {
			LOG.debug("Error initializing spectra search");
		} catch (final RemoteException e) {
			LOG.warn("Error performing spectrum search");
		}

		return results;

	}

	private static ChemicalStorageClass getStorageFromDOTClass(
			final String dotClass, final ChemicalRecord r) {
		ChemicalStorageClass ourClass;

		switch (dotClass) {
			case "1":
			case "1.1":
				r.setNfpaR(4);
				//$FALL-THROUGH$
			case "1.2":
			case "1.3":
			case "1.4":
			case "1.5":
			case "1.6":
				ourClass = ChemicalStorageClass.Flammables;
				break;
			case "2.1":
				ourClass = ChemicalStorageClass.Reactive_gas;
				break;
			case "2.2":
				ourClass = ChemicalStorageClass.Non_reactive_gas;
				break;
			case "2.3":
				ourClass = ChemicalStorageClass.Biomedical;
				r.setNfpaH(3);
				break;
			case "4.3":
				ourClass = ChemicalStorageClass.Water_reactive;
				r.setNfpaS(ChemicalNfpaS.W);
				break;
			case "5.1":
			case "5.2":
				ourClass = ChemicalStorageClass.Oxidizers;
				r.setNfpaS(ChemicalNfpaS.OX);
				break;
			case "6.1":
				ourClass = ChemicalStorageClass.Biomedical;
				r.setNfpaH(3);
				break;
			case "7":
				ourClass = ChemicalStorageClass.Radioactive;
				r.setNfpaH(2);
				break;
			case "8":
				ourClass = ChemicalStorageClass.Inorganic_acids;
				break;
			default:
				ourClass = ChemicalStorageClass.Unknown;
		}

		return ourClass;
	}

	public static BufferedImage getStructureThumbnailFromCSID(final String csid) {
		try {
			final SearchStub search = new SearchStub();
			final GetCompoundThumbnail getThumb = new GetCompoundThumbnail();
			getThumb.setId(csid);
			getThumb.setToken(Constants.CHEMSPIDER_TOKEN);
			final InputStream stream = search.getCompoundThumbnail(getThumb)
					.getGetCompoundThumbnailResult().getInputStream();
			return ImageIO.read(stream);
		} catch (final AxisFault e) {
			LOG.debug("Error initiating thumbnail search.");
		} catch (final RemoteException e) {
			LOG.debug("Error fetching image InputStream.", e.getCause());
		} catch (final IOException e) {
			LOG.debug("Error fetching image.", e.getCause());
		}

		return new BufferedImage(0, 0, BufferedImage.TYPE_INT_RGB);
	}

	public static List<String> getSynonyms(ChemicalRecord rec) {
		if (rec.getCsid() == null) {
			rec = updateRecord(rec);
		}
		try {

			final MassSpecAPIStub massStub = new MassSpecAPIStub();
			final SynonymsStub synStub = new SynonymsStub();

			final GetRecordMol recMol = new GetRecordMol();
			recMol.setCalc3D(false);
			recMol.setCsid(rec.getCsid());
			recMol.setToken(Constants.CHEMSPIDER_TOKEN);

			final String mol = massStub.getRecordMol(recMol)
					.getGetRecordMolResult();

			final GetStructureSynonyms strSyn = new GetStructureSynonyms();
			strSyn.setMol(mol);
			strSyn.setToken(Constants.CHEMSPIDER_TOKEN);

			final String[] synonyms = synStub.getStructureSynonyms(strSyn)
					.getGetStructureSynonymsResult().getString();

			return Arrays.asList(synonyms);

		} catch (final AxisFault e) {
			LOG.debug("Error initiating compound search", e);
		} catch (final RemoteException e) {
			LOG.debug("Error executing compound search: " + e.getCause());
		}

		return null;

	}

	public static String getTOXNETBaseUrl(final String cas) {
		final String response = getTOXNETResponse(cas);
		final Document doc = Jsoup.parse(response);
		final String tempID = doc.select("TemporaryFile").text();

		return String.format(
				"http://toxgate.nlm.nih.gov/cgi-bin/sis/search/f?%s:1", tempID);

	}

	private static String getTOXNETResponse(final String query) {
		final StringBuilder sb = new StringBuilder();
		try {
			URL url;
			URLConnection urlConn;
			DataOutputStream printout;
			BufferedReader input;

			// URL of CGI-Bin script.
			url = new URL("http://toxgate.nlm.nih.gov/cgi-bin/sis/search/");
			// URL connection channel.
			urlConn = url.openConnection();
			// Let the run-time system (RTS) know that we want input.
			urlConn.setDoInput(true);
			// Let the RTS know that we want to do output.
			urlConn.setDoOutput(true);
			// No caching, we want the real thing.
			urlConn.setUseCaches(false);
			// Specify the content type.
			urlConn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			// Send POST output.
			printout = new DataOutputStream(urlConn.getOutputStream());
			final String content = String
					.format("queryxxx=%s&database=hsdb&Stemming=1&and=1&second_search=1&gateway=1&chemsyn=1",
							URLEncoder.encode(query, "UTF-8"));
			printout.writeBytes(content);
			printout.flush();
			printout.close();
			// Get response data.
			input = new BufferedReader(new InputStreamReader(
					urlConn.getInputStream()));
			String str;
			while (null != (str = input.readLine())) {
				sb.append(str);
			}
			input.close();

		}

		catch (final MalformedURLException me) {
			LOG.debug("Improperly formatted TOXNET URL", me.getCause());
		} catch (final IOException ioe) {
			LOG.error("Error connecting to TOXNET", ioe.getCause());
		}

		return sb.toString();
	}

	private static String getWebWiserPage(final String casNo)
			throws IOException {
		final Document doc = Jsoup.connect(Constants.WEBWISER_ROOT + casNo)
				.get();

		final Element link = doc.select("table#results td.even")
				.select("a[href]").first();

		if (link == null) {
			return null;
		}

		final String url = link.absUrl("href");

		return url;
	}

	private static void initializeHazardTable() {
		final InputStream in = ClassLoader
				.getSystemResourceAsStream("res/hazard.html");
		hazardTable = new HashMap<>();
		Document doc;
		try {
			doc = Jsoup.parse(in, null, "/");
			final Element hazards = doc.select("table#hazard").first();

			final Elements cells = hazards.select("td:eq(1),td:eq(2)");
			cells.remove(0);
			cells.remove(0);

			for (int i = 0; i < cells.size() - 1; i += 2) {
				final String key = cells.get(i).text();
				String value = cells.get(i + 1).text();

				if (key == null) {
					LOG.debug("Trying to add null key to hazard table on row "
							+ i);
				}

				if (value != null) {
					value = value.replaceAll("[A-Za-z]", "");
				}

				hazardTable.put(key, value);
			}

			LOG.debug("Intialized hazard table with " + hazardTable.size()
					+ " elements.");
		} catch (final IOException e) {
			LOG.error("Error parsing resource.", e.getCause());
		}

	}

	private static double lookupDOTHazard(String name) {
		if (hazardTable == null) {
			initializeHazardTable();
		}

		double hazardLevel = 100;
		if (name == null) {
			name = "null";
		}
		final Pattern p = Pattern
				.compile("\\b" + Pattern.quote(name) + "[, .]",
						Pattern.CASE_INSENSITIVE);

		for (final Map.Entry<String, String> entry : hazardTable.entrySet()) {
			if (p.matcher(entry.getKey()).find()) {
				try {
					final double thisLevel = Double.parseDouble(entry
							.getValue());
					if (thisLevel < hazardLevel) {
						hazardLevel = thisLevel;
					}
				} catch (final NumberFormatException e) {
					continue;
				}
			}

		}

		return hazardLevel == 100 ? -1 : hazardLevel;
	}

	public static List<ChemicalRecord> searchByCAS(final String cas) {
		final String csid = getCSID(cas);
		final List<ChemicalRecord> records = new ArrayList<>();

		if (csid != null) {
			records.addAll(searchByCSID(csid, cas));
		}

		return records;

	}

	public static List<ChemicalRecord> searchByCSID(final String csid,
			final String cas) {
		try {
			final MassSpecAPIStub massStub = new MassSpecAPIStub();

			final GetExtendedCompoundInfoArray compoundQuery = new GetExtendedCompoundInfoArray();
			final ArrayOfInt csidsInts = new ArrayOfInt();
			csidsInts.set_int(new int[] { Integer.parseInt(csid.trim()) });
			compoundQuery.setCSIDs(csidsInts);
			compoundQuery.setToken(Constants.CHEMSPIDER_TOKEN);
			final ExtendedCompoundInfo[] compounds = massStub
					.getExtendedCompoundInfoArray(compoundQuery)
					.getGetExtendedCompoundInfoArrayResult()
					.getExtendedCompoundInfo();

			final List<ChemicalRecord> records = new ArrayList<>();
			if (compounds != null) {
				for (final ExtendedCompoundInfo c : compounds) {
					final ChemicalRecord r = new ChemicalRecord();
					r.setCas(cas);
					r.setFormula(cleanFormula(c.getMF()));
					r.setName(c.getCommonName());
					r.setSmiles(c.getSMILES());
					r.setCsid(Integer.toString(c.getCSID()));
					r.setInchi(c.getInChI());
					fillTOXNETInformation(r);

					records.add(r);
				}
			}
			return records;

		} catch (final AxisFault e) {
			LOG.debug("Error initiating compound search", e);
		} catch (final RemoteException e) {
			LOG.debug("Error executing compound search: " + e.getCause());
		}

		return null;
	}

	/**
	 * Returns ChemSpider records as searched by name/synonym. Only
	 * ChemicalRecord is only guaranteed to contain a CSID. Any other fields may
	 * be blank.
	 * 
	 * @param name
	 * @return
	 */
	public static List<ChemicalRecord> searchByName(final String name) {
		try {
			final SearchStub searchStub = new SearchStub();
			final MassSpecAPIStub massStub = new MassSpecAPIStub();

			final SimpleSearch query = new SimpleSearch();
			query.setQuery(name);
			query.setToken(Constants.CHEMSPIDER_TOKEN);
			final int[] csids = searchStub.simpleSearch(query)
					.getSimpleSearchResult().get_int();
			LOG.debug("Found " + (csids == null ? 0 : csids.length)
					+ " results");

			final GetExtendedCompoundInfoArray compoundQuery = new GetExtendedCompoundInfoArray();
			final ArrayOfInt csidsInts = new ArrayOfInt();
			csidsInts.set_int(csids);
			compoundQuery.setCSIDs(csidsInts);
			compoundQuery.setToken(Constants.CHEMSPIDER_TOKEN);
			final ExtendedCompoundInfo[] compounds = massStub
					.getExtendedCompoundInfoArray(compoundQuery)
					.getGetExtendedCompoundInfoArrayResult()
					.getExtendedCompoundInfo();

			final List<ChemicalRecord> records = new ArrayList<>();
			if (compounds != null) {
				for (final ExtendedCompoundInfo c : compounds) {
					final ChemicalRecord r = new ChemicalRecord();
					r.setFormula(cleanFormula(c.getMF()));
					r.setName(c.getCommonName());
					r.setSmiles(c.getSMILES());
					r.setCsid(Integer.toString(c.getCSID()));
					r.setInchi(c.getInChI());
					records.add(r);
				}
			}
			return records;

		} catch (final AxisFault e) {
			LOG.debug("Error initiating compound search", e);
		} catch (final RemoteException e) {
			LOG.debug("Error executing compound search: " + e.getCause());
		}

		return null;
	}

	private static ChemicalStorageClass testForSubstructures(
			final ChemicalRecord r) {
		ChemicalStorageClass matchedClass = ChemicalStorageClass.Unknown;
		final String SMILES = r.getSmiles();
		final String name = r.getName() == null ? "null" : r.getName();

		// Do some fancy things checking for substructures
		if (ChemicalSubstructureSearcher.isOrganicBase(SMILES)) {
			matchedClass = ChemicalStorageClass.Organic_base;
		} else if (ChemicalSubstructureSearcher.isOrganicAcid(SMILES)) {
			matchedClass = ChemicalStorageClass.Organic_acids;
		} else if (ChemicalSubstructureSearcher.isOxidizer(SMILES)) {
			matchedClass = ChemicalStorageClass.Oxidizers;
		} else if (Constants.INORGANIC_ACIDS.containsKey(name)) {
			matchedClass = ChemicalStorageClass.Inorganic_acids;
		} else if (ChemicalSubstructureSearcher.isAromatic(SMILES)) {
			matchedClass = ChemicalStorageClass.Aromatics;
		} else if (ChemicalSubstructureSearcher.isCyanohydrin(SMILES)) {
			matchedClass = ChemicalStorageClass.Cyanohydrins;
		} else if (ChemicalSubstructureSearcher.isAlcohol(SMILES)) {
			matchedClass = ChemicalStorageClass.Alcohols;
		}
		// Now check the names for substrings
		else if (name.contains("hydroxide") || name.contains("carbonate")
				|| name.contains("ammonium")) {
			matchedClass = ChemicalStorageClass.Inorganic_base;
		} else if (name.startsWith("Hydro") || name.startsWith("Hypo")
				|| name.contains("acid")) {
			matchedClass = ChemicalStorageClass.Inorganic_acids;
		} else if (name.contains("amine") || name.contains("amino")
				|| name.contains("Amino")) {
			matchedClass = ChemicalStorageClass.Organic_base;
		} else if (name.endsWith("one") || name.endsWith("ol")
				|| name.contains("hydroxy") || name.contains("ether")) {
			matchedClass = ChemicalStorageClass.Alcohols;
		} else if (name.contains("peroxide")) {
			matchedClass = ChemicalStorageClass.Oxidizers;
			r.setNfpaS(ChemicalNfpaS.OX);
		}

		return matchedClass;
	}

	/**
	 * Retrieves data from Internet for a given chemical and stores it back to
	 * the database.
	 * 
	 * @param oldRecord
	 * @return
	 */
	public static ChemicalRecord updateRecord(final ChemicalRecord oldRecord) {
		List<ChemicalRecord> records;
		if (oldRecord.getCsid() != null && !oldRecord.getCsid().isEmpty()) {
			records = searchByCSID(oldRecord.getCsid(), oldRecord.getCas());
		} else {
			records = searchByCAS(oldRecord.getCas());
		}

		// if (records.size() > 1) {
		// // Strip everything after a comma, which may be "lab grade",
		// // "anhydrous", etc.
		// final String name = oldRecord.getName().substring(0,
		// oldRecord.getName().indexOf(","));
		// records = filterByName(records, name);
		// }

		if (records.isEmpty()) {
			return oldRecord;
		}

		final ChemicalRecord newRecord = records.get(0);

		if (newRecord.getCas() == null || newRecord.getCas().isEmpty()) {
			newRecord.setCas(oldRecord.getCas());
		}

		if (newRecord.getCarc().equals(ChemicalCarc.No)) {
			newRecord.setCarc(oldRecord.getCarc());
		}

		if (newRecord.getStorageClass().equals(ChemicalStorageClass.Unknown)) {
			newRecord.setStorageClass(oldRecord.getStorageClass());
		}

		if (newRecord.getCold().equals(ChemicalCold.No)) {
			newRecord.setCold(oldRecord.getCold());
		}

		if (newRecord.getToxic().equals(ChemicalToxic.Practically_nontoxic)) {
			newRecord.setToxic(oldRecord.getToxic());
		}

		if (newRecord.getName() == null) {
			newRecord.setName(oldRecord.getName());
		}

		ChemicalDao.store(newRecord);

		return newRecord;
	}
}
