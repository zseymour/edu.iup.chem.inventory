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
import java.util.List;
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

	private final static Logger	LOG	= Logger.getLogger(ChemicalWebSearch.class);

	private static double _fetchBoilingPoint(final String baseURL)
			throws IOException {
		final String content = Jsoup.connect(baseURL + ":bp").get().text();

		LOG.debug("TOXNET Boiling Point Response: " + content);

		final Pattern p = Pattern.compile("([\\d.]+) deg");
		final Matcher m = p.matcher(content);

		while (m.find()) {
			final String bp = m.group(1);
			LOG.debug("Boiling point: " + bp);
			final double boiling = Double.parseDouble(bp);
			return boiling;
		}

		return 0;
	}

	private static ChemicalDensity _fetchDensity(final String baseURL)
			throws IOException, NumberFormatException {
		final String content = Jsoup.connect(baseURL + ":den").get().text();

		LOG.debug("TOXNET Density Response: " + content);

		final Pattern solidPattern = Pattern
				.compile("([\\d.]+) ([A-Za-z]+/[A-Za-z ]+)");
		final Matcher solidMatcher = solidPattern.matcher(content);

		final Pattern liquidPattern = Pattern
				.compile("Gravity: ([\\d.]+) (at|@)?");
		final Matcher liquidMatcher = liquidPattern.matcher(content);

		if (solidMatcher.find()) {
			final String value = solidMatcher.group(1);
			final String unit = solidMatcher.group(2);

			LOG.debug("Density: " + value + " " + unit);

			return (ChemicalDensity) ChemicalAmountFactory.getChemicalAmount(
					value, unit);
		} else if (liquidMatcher.find()) {
			final String gravity = liquidMatcher.group(1);

			LOG.debug("Specific gravity: " + gravity);

			return (ChemicalDensity) ChemicalAmountFactory.getChemicalAmount(
					gravity, "kg/m3");
		}

		throw new NumberFormatException(
				"Density information improperly formatted");
	}

	private static Double _fetchMeltingPoint(final String baseURL)
			throws IOException {
		final String content = Jsoup.connect(baseURL + ":mp").get().text();

		LOG.debug("TOXNET Melting Point Response: " + content);

		final Pattern p = Pattern.compile("([\\d.]+) deg");
		final Matcher m = p.matcher(content);

		while (m.find()) {
			final String mp = m.group(1);
			LOG.debug("Melting point: " + mp);
			final double melting = Double.parseDouble(mp);
			return melting;
		}

		return 0.0;
	}

	/**
	 * We store an array of LD50 amounts corresponding to the toxicity levels.
	 * We then insert the given LD50 into that list, sort it, and based on its
	 * final index, determine the toxicity level of the compound.
	 * 
	 * @param ld50PerKilo
	 * @return
	 */
	private static ChemicalToxic amountToToxicity(final ChemicalMass ld50PerKilo) {
		final ArrayList<ChemicalMass> levels = Constants.LD50_LEVELS;
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

	private static ChemicalStorageClass dotClassToIUPClass(final String dotClass) {
		final String classStr = dotClass.substring("Class".length() + 1,
				dotClass.indexOf("-") - 1).trim();
		final int classLevel = Integer.parseInt(classStr);

		switch (classLevel) {
			case 1:
				return ChemicalStorageClass.Explosives;
			case 2:
				return ChemicalStorageClass.Flammable;
			case 3:
				return ChemicalStorageClass.Flammable;
			case 4:
				return ChemicalStorageClass.Flammable;
			case 5:
				return ChemicalStorageClass.Oxidizers;
			case 6:
				return ChemicalStorageClass.Highly_Toxic;
			case 7:
				return ChemicalStorageClass.Radioactive;
			case 8:
				return ChemicalStorageClass.Inorganic_Acids;
			default:
				return ChemicalStorageClass.Low_Hazard;
		}
	}

	private static ChemicalStorageClass dotDivisionToIUPClass(
			final String dotDivision) {
		final String classStr = dotDivision.substring("Divison".length() + 1,
				dotDivision.indexOf("-") - 1).trim();

		switch (classStr) {
			case "1.1":
				return ChemicalStorageClass.Explosives;
			case "1.2":
				return ChemicalStorageClass.Explosives;
			case "1.3":
				return ChemicalStorageClass.Explosives;
			case "2.1":
				return ChemicalStorageClass.Flammable;
			case "2.3":
				return ChemicalStorageClass.Highly_Toxic;
			case "3":
				return ChemicalStorageClass.Flammable;
			case "4.1":
				return ChemicalStorageClass.Flammable;
			case "4.2":
				return ChemicalStorageClass.Pyrophoric_Materials;
			case "5.1":
				return ChemicalStorageClass.Oxidizers;
			case "5.2":
				return ChemicalStorageClass.Organic_Peroxides;
			case "6.1":
				return ChemicalStorageClass.Highly_Toxic;
			case "7":
				return ChemicalStorageClass.Radioactive;
			case "8":
				return ChemicalStorageClass.Inorganic_Acids;
			default:
				return ChemicalStorageClass.Low_Hazard;
		}
	}

	public static Double fetchBoilingPoint(final String cas) {
		try {
			return _fetchBoilingPoint(getTOXNETBaseUrl(cas));
		} catch (final IOException e) {
			return 0.0;
		}
	}

	public static ChemicalDensity fetchDensity(final String cas) {
		try {
			return _fetchDensity(getTOXNETBaseUrl(cas));
		} catch (final IOException | NumberFormatException e) {
			return (ChemicalDensity) ChemicalAmountFactory.getChemicalAmount(
					1.0, "kg/m3");
		}
	}

	public static Double fetchMeltingPoint(final String cas) {
		try {
			return _fetchMeltingPoint(getTOXNETBaseUrl(cas));
		} catch (final IOException e) {
			return 0.0;
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

	private static void fillStorageClass(final ChemicalRecord r) {
		r.setStorageClass(ChemicalStorageClass.Low_Hazard);

		try {
			final String url = getWebWiserPage(r.getCas());

			if (url == null) {
				return;
			}

			final Document doc = Jsoup.connect(
					url + "&selectedDataMenuItemID=86").get();
			final Elements dotClasses = doc.select(
					"div#substanceDataContent ul").select("li");

			String dotClass = null;
			String dotDivision = null;
			for (final Element e : dotClasses) {
				final String currentClass = e.text();
				if (currentClass.startsWith("Class") && dotClass == null) {
					// This is the highest level class this compound has.
					dotClass = currentClass;
					continue;
				} else if (currentClass.startsWith("Division")) {
					// break after we find division, because this is the most
					// specific, highest numbered class
					dotDivision = currentClass;
				}
			}

			ChemicalStorageClass storageClass = ChemicalStorageClass.Low_Hazard;
			if (dotClass == null) {
				// Do nothing
			} else if (dotDivision == null) {
				storageClass = dotClassToIUPClass(dotClass);
			} else {
				storageClass = dotDivisionToIUPClass(dotDivision);
			}

			LOG.debug(r.getName() + " is " + storageClass.getLiteral());

			r.setStorageClass(storageClass);

		} catch (final IOException e) {
			LOG.debug("Error connecting to WebWISER.");
		}
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

		final double boiling = _fetchBoilingPoint(baseURL);

		if (boiling < 36.0) {
			r.setCold(ChemicalCold.Yes);
		}

		final String content = Jsoup.connect(baseURL + ":flpt").get().text();

		LOG.debug("TOXNET Flash Point Response: " + content);

		final Pattern p = Pattern.compile("([\\d.]+) (deg|DEG) F");
		final Matcher m = p.matcher(content);

		while (m.find()) {
			final String fp = m.group(1);
			LOG.debug("Flash point: " + fp);
			final double flash = Double.parseDouble(fp);
			if (flash <= 100.4) {
				r.setFlamm(ChemicalFlamm.Yes);
			}
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
		r.setToxic(ChemicalToxic.Practically_nontoxic);
		final String content = Jsoup.connect(baseURL + ":ntxv").get().text();
		LOG.debug("TOXNET Toxicity Response: " + content);
		final Pattern p = Pattern.compile("LD50 Rat oral (\\d+) ([a-z]+)");
		final Matcher m = p.matcher(content);
		while (m.find()) {
			final String amount = m.group(1);
			final String unit = m.group(2);

			LOG.debug("Rat LD50: " + amount + " " + unit);

			final ChemicalAmount ld50 = ChemicalAmountFactory
					.getChemicalAmount(amount, unit);
			final ChemicalToxic tox = amountToToxicity((ChemicalMass) ld50);

			if (tox.compareTo(r.getToxic()) < 0) {
				r.setToxic(tox);
			}
		}

	}

	private static void fillTOXNETInformation(final ChemicalRecord record)
			throws IOException {
		final String url = getTOXNETBaseUrl(record.getCas());

		fillHazardInformation(record, url);
		fillToxicityInformation(record, url);
		fillStorageTypes(record, url);

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
			csidsInts.set_int(new int[] { Integer.parseInt(csid) });
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
					fillHazardInformation(r);
					fillCancerInformation(r);
					fillToxicityInformation(r);
					fillStorageTypes(r);
					fillStorageClass(r);

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

		if (newRecord.getStorageClass().equals(ChemicalStorageClass.Low_Hazard)) {
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
