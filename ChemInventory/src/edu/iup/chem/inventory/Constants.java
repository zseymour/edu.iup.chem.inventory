package edu.iup.chem.inventory;

import static javax.measure.unit.SI.GRAM;
import static javax.measure.unit.SI.MILLI;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;

import edu.iup.chem.inventory.amount.ChemicalMass;
import edu.iup.chem.inventory.db.inventory.tables.records.UserRecord;
import edu.iup.chem.inventory.misc.CaseInsensitiveMap;

public class Constants {
	private static Toolkit						toolkit						= Toolkit
																					.getDefaultToolkit();

	public static final Dimension				SCREEN_SIZE					= new Dimension(
																					toolkit.getScreenSize().width * 98 / 100,
																					toolkit.getScreenSize().height);

	public static final Dimension				HALF_SCREEN_SIZE			= new Dimension(
																					SCREEN_SIZE.width,
																					SCREEN_SIZE.height / 2);

	public static final Dimension				QUARTER_SCREEN_SIZE			= new Dimension(
																					SCREEN_SIZE.width,
																					SCREEN_SIZE.height / 3);
	public static final Dimension				VERT_QUARTER_SCREEN			= new Dimension(
																					SCREEN_SIZE.width / 4,
																					2 * SCREEN_SIZE.height / 3);

	public static final Dimension				HALF_VERT_QUARTER_SCREEN	= new Dimension(
																					SCREEN_SIZE.width / 4,
																					1 * SCREEN_SIZE.height / 3);

	public static final Dimension				VERT_HALF_SCREEN_SIZE		= new Dimension(
																					SCREEN_SIZE.width / 2,
																					SCREEN_SIZE.height / 2);

	public static UserRecord					CURRENT_USER				= null;

	public static final String					ADMIN_ROLE					= "administrator";

	public static final String					SITE_ADMIN_ROLE				= "site admin";

	public static final String					DATA_ENTRY_ROLE				= "data entry";

	public static final String					FACULTY_ROLE				= "faculty researcher";

	public static final String					RESEARCHER_ROLE				= "researcher";
	public static final String					GUEST_ROLE					= "guest";

	public static final String					CHEMSPIDER_TOKEN			= "d4161535-a3e1-4750-a249-fe1c4402fa9f";

	public static final CaseInsensitiveMap		CHEMICALS					= new CaseInsensitiveMap() {
																				/**
		 * 
		 */
																				private static final long	serialVersionUID	= 5002000351248414648L;

																				{
																					put("hydrogen",
																							"H");
																					put("helium",
																							"He");
																					put("lithium",
																							"Li");
																					put("beryllium",
																							"Be");
																					put("boron",
																							"B");
																					put("carbon",
																							"C");
																					put("nitrogen",
																							"N");
																					put("oxygen",
																							"O");
																					put("flourine",
																							"F");
																					put("neon",
																							"Ne");
																					put("sodium",
																							"Na");
																					put("magnesium",
																							"Mg");
																					put("aluminum",
																							"Al");
																					put("silicon",
																							"Si");
																					put("phosphorus",
																							"P");
																					put("sulfur",
																							"S");
																					put("sulphur",
																							"S");
																					put("chlorine",
																							"Cl");
																					put("argon",
																							"Ar");
																					put("potassium",
																							"K");
																					put("calcium",
																							"Ca");
																					put("scandium",
																							"Sc");
																					put("titanium",
																							"Ti");
																					put("vanadium",
																							"V");
																					put("chromium",
																							"Cr");
																					put("manganese",
																							"Mn");
																					put("iron",
																							"Fe");
																					put("cobalt",
																							"Co");
																					put("nickel",
																							"Ni");
																					put("copper",
																							"Cu");
																					put("zinc",
																							"Zn");
																					put("gallium",
																							"Ga");
																					put("germanium",
																							"Ge");
																					put("arsenic",
																							"As");
																					put("selenium",
																							"Se");
																					put("bromine",
																							"Br");
																					put("krypton",
																							"Kr");
																					put("rubdium",
																							"Rb");
																					put("strontium",
																							"Sr");
																					put("yttrium",
																							"Y");
																					put("Zirconium",
																							"Zr");
																					put("niobium",
																							"Nb");
																					put("molybdenum",
																							"Mo");
																					put("technetium",
																							"Tc");
																					put("ruthenium",
																							"Ru");
																					put("rhodium",
																							"Rh");
																					put("Silver",
																							"Ag");
																					put("Cadmium",
																							"Cd");
																					put("indium",
																							"In");
																					put("tin",
																							"Sn");
																					put("antimony",
																							"Sn");
																					put("tellurium",
																							"Te");
																					put("iodine",
																							"I");
																					put("xenon",
																							"Xe");
																					put("cesium",
																							"Cs");
																					put("caesium",
																							"Cs");
																					put("barium",
																							"Ba");
																					put("lathanum",
																							"La");
																					put("cerium",
																							"Ce");
																					put("praseodymium",
																							"Pr");
																					put("neodymium",
																							"Nd");
																					put("promethium",
																							"Pm");
																					put("samarium",
																							"Sm");
																					put("Europium",
																							"Eu");
																					put("Gadolinium",
																							"Gd");
																					put("Terbium",
																							"Tb");
																					put("Dysprosium",
																							"Dy");
																					put("Holmium",
																							"Ho");
																					put("Erbium",
																							"Er");
																					put("thulium",
																							"Tm");
																					put("Ytterbium",
																							"Yb");
																					put("lutetium",
																							"Lu");
																					put("hafnium",
																							"Hf");
																					put("tantalum",
																							"Ta");
																					put("tungsten",
																							"W");
																					put("Rhenium",
																							"Re");
																					put("osmium",
																							"Os");
																					put("iridium",
																							"Ir");
																					put("platinum",
																							"Pt");
																					put("gold",
																							"Au");
																					put("mercury",
																							"Hg");
																					put("thallium",
																							"Tl");
																					put("lead",
																							"Pb");
																					put("bismuth",
																							"Bi");
																					put("polonium",
																							"Po");
																					put("astatine",
																							"At");
																					put("Radon",
																							"Rn");
																					put("francium",
																							"Fr");
																					put("radium",
																							"Ra");
																					put("actinium",
																							"Ac");
																					put("thorium",
																							"Th");
																					put("proactinium",
																							"Pa");
																					put("uranium",
																							"U");
																					put("neptunium",
																							"Np");
																					put("plutonium",
																							"Pu");
																					put("americium",
																							"Am");
																					put("curium",
																							"Cm");
																					put("berkelium",
																							"Bk");
																					put("californium",
																							"Cf");
																					put("einstenium",
																							"Es");
																					put("fermium",
																							"Fm");
																					put("mendelevium",
																							"Md");
																					put("nobelium",
																							"No");
																					put("lawrencium",
																							"Lr");
																					put("rutherfordium",
																							"Rf");
																					put("dubnium",
																							"Db");
																					put("Seaborgium",
																							"Sg");
																					put("Bohrium",
																							"Bh");
																					put("Hassium",
																							"Hs");
																					put("meitnerium",
																							"Mt");
																					put("Darmstadtium",
																							"Ds");
																					put("Roentgenium",
																							"Rg");
																					put("copernicium",
																							"Cn");
																					put("ununtrium",
																							"Uut");
																					put("Flerovium",
																							"Fl");
																					put("ununpentium",
																							"Uup");
																					put("Livermorium",
																							"Lv");
																					put("Ununseptium",
																							"Uus");
																					put("ununoctium",
																							"Uuo");

																				}
																			};
	public static final String					WEBWISER_ROOT				= "http://webwiser.nlm.nih.gov/knownSubstanceSearch.do?method=search&currentSearchText=&currentSearchBy=CAS_RN&currentGoToItem=1&currentPageSize=20&appMode=3&searchBy=CAS_RN&Search=Search&goToItem=1&pageSize=20&searchText=";
	public static final String					CIR_URL_FORMAT				= "http://cactus.nci.nih.gov/chemical/structure/%s/%s";
	public static final ArrayList<ChemicalMass>	LD50_LEVELS					= new ArrayList<ChemicalMass>() {
																				{
																					add(new ChemicalMass(
																							5,
																							"mg",
																							MILLI(GRAM)));
																					add(new ChemicalMass(
																							50,
																							"mg",
																							MILLI(GRAM)));
																					add(new ChemicalMass(
																							500,
																							"mg",
																							MILLI(GRAM)));
																					add(new ChemicalMass(
																							5,
																							"g",
																							GRAM));
																				}
																			};
}
