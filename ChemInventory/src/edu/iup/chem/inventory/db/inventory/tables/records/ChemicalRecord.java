/**
 * This class is generated by jOOQ
 */
package edu.iup.chem.inventory.db.inventory.tables.records;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value = { "http://www.jooq.org", "2.5.1" }, comments = "This class is generated by jOOQ")
public class ChemicalRecord
		extends
		org.jooq.impl.UpdatableRecordImpl<edu.iup.chem.inventory.db.inventory.tables.records.ChemicalRecord> {

	private static final long	serialVersionUID	= 492614303;

	/**
	 * Create a detached ChemicalRecord
	 */
	public ChemicalRecord() {
		super(edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL);
	}

	/**
	 * The table column <code>inventory.chemical.carc</code>
	 */
	public edu.iup.chem.inventory.db.inventory.enums.ChemicalCarc getCarc() {
		return getValue(edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.CARC);
	}

	/**
	 * The table column <code>inventory.chemical.cas</code>
	 * <p>
	 * This column is part of the table's PRIMARY KEY
	 */
	public java.lang.String getCas() {
		return getValue(edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.CAS);
	}

	/**
	 * The table column <code>inventory.chemical.cold</code>
	 */
	public edu.iup.chem.inventory.db.inventory.enums.ChemicalCold getCold() {
		return getValue(edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.COLD);
	}

	/**
	 * ID for ChemSpider searches
	 */
	public java.lang.String getCsid() {
		return getValue(edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.CSID);
	}

	/**
	 * The table column <code>inventory.chemical.flamm</code>
	 */
	public edu.iup.chem.inventory.db.inventory.enums.ChemicalFlamm getFlamm() {
		return getValue(edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.FLAMM);
	}

	/**
	 * The table column <code>inventory.chemical.formula</code>
	 */
	public java.lang.String getFormula() {
		return getValue(edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.FORMULA);
	}

	/**
	 * International Chemical Identifier (unique)
	 */
	public java.lang.String getInchi() {
		return getValue(edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.INCHI);
	}

	/**
	 * The table column <code>inventory.chemical.name</code>
	 */
	public java.lang.String getName() {
		return getValue(edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.NAME);
	}

	/**
	 * The table column <code>inventory.chemical.nfpa_f</code>
	 */
	public java.lang.Integer getNfpaF() {
		return getValue(edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.NFPA_F);
	}

	/**
	 * The table column <code>inventory.chemical.nfpa_h</code>
	 */
	public java.lang.Integer getNfpaH() {
		return getValue(edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.NFPA_H);
	}

	/**
	 * The table column <code>inventory.chemical.nfpa_r</code>
	 */
	public java.lang.Integer getNfpaR() {
		return getValue(edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.NFPA_R);
	}

	/**
	 * The table column <code>inventory.chemical.nfpa_s</code>
	 */
	public edu.iup.chem.inventory.db.inventory.enums.ChemicalNfpaS getNfpaS() {
		return getValue(edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.NFPA_S);
	}

	/**
	 * The table column <code>inventory.chemical.smiles</code>
	 */
	public java.lang.String getSmiles() {
		return getValue(edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.SMILES);
	}

	/**
	 * The table column <code>inventory.chemical.storage_class</code>
	 */
	public edu.iup.chem.inventory.db.inventory.enums.ChemicalStorageClass getStorageClass() {
		return getValue(edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.STORAGE_CLASS);
	}

	/**
	 * The table column <code>inventory.chemical.toxic</code>
	 */
	public edu.iup.chem.inventory.db.inventory.enums.ChemicalToxic getToxic() {
		return getValue(edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.TOXIC);
	}

	public String log() {
		return "ChemicalRecord ["
				+ (getCas() != null ? "getCas()=" + getCas() + ", " : "")
				+ (getStorageClass() != null ? "getStorageClass()="
						+ getStorageClass() + ", " : "")
				+ (getToxic() != null ? "getToxic()=" + getToxic() + ", " : "")
				+ (getCold() != null ? "getCold()=" + getCold() + ", " : "")
				+ (getFlamm() != null ? "getFlamm()=" + getFlamm() + ", " : "")
				+ (getCarc() != null ? "getCarc()=" + getCarc() + ", " : "")
				+ (getNfpaH() != null ? "getNfpaH()=" + getNfpaH() + ", " : "")
				+ (getNfpaF() != null ? "getNfpaF()=" + getNfpaF() + ", " : "")
				+ (getNfpaR() != null ? "getNfpaR()=" + getNfpaR() + ", " : "")
				+ (getNfpaS() != null ? "getNfpaS()=" + getNfpaS() + ", " : "")
				+ (getSmiles() != null ? "getSmiles()=" + getSmiles() + ", "
						: "")
				+ (getName() != null ? "getName()=" + getName() + ", " : "")
				+ (getFormula() != null ? "getFormula()=" + getFormula() + ", "
						: "")
				+ (getCsid() != null ? "getCsid()=" + getCsid() + ", " : "")
				+ (getInchi() != null ? "getInchi()=" + getInchi() : "") + "]";
	}

	/**
	 * The table column <code>inventory.chemical.carc</code>
	 */
	public void setCarc(
			final edu.iup.chem.inventory.db.inventory.enums.ChemicalCarc value) {
		setValue(
				edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.CARC,
				value);
	}

	/**
	 * The table column <code>inventory.chemical.cas</code>
	 * <p>
	 * This column is part of the table's PRIMARY KEY
	 */
	public void setCas(final java.lang.String value) {
		setValue(
				edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.CAS,
				value);
	}

	/**
	 * The table column <code>inventory.chemical.cold</code>
	 */
	public void setCold(
			final edu.iup.chem.inventory.db.inventory.enums.ChemicalCold value) {
		setValue(
				edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.COLD,
				value);
	}

	/**
	 * ID for ChemSpider searches
	 */
	public void setCsid(final java.lang.String value) {
		setValue(
				edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.CSID,
				value);
	}

	/**
	 * The table column <code>inventory.chemical.flamm</code>
	 */
	public void setFlamm(
			final edu.iup.chem.inventory.db.inventory.enums.ChemicalFlamm value) {
		setValue(
				edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.FLAMM,
				value);
	}

	/**
	 * The table column <code>inventory.chemical.formula</code>
	 */
	public void setFormula(final java.lang.String value) {
		setValue(
				edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.FORMULA,
				value);
	}

	/**
	 * International Chemical Identifier (unique)
	 */
	public void setInchi(final java.lang.String value) {
		setValue(
				edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.INCHI,
				value);
	}

	/**
	 * The table column <code>inventory.chemical.name</code>
	 */
	public void setName(final java.lang.String value) {
		setValue(
				edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.NAME,
				value);
	}

	/**
	 * The table column <code>inventory.chemical.nfpa_f</code>
	 */
	public void setNfpaF(final java.lang.Integer value) {
		setValue(
				edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.NFPA_F,
				value);
	}

	/**
	 * The table column <code>inventory.chemical.nfpa_h</code>
	 */
	public void setNfpaH(final java.lang.Integer value) {
		setValue(
				edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.NFPA_H,
				value);
	}

	/**
	 * The table column <code>inventory.chemical.nfpa_r</code>
	 */
	public void setNfpaR(final java.lang.Integer value) {
		setValue(
				edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.NFPA_R,
				value);
	}

	/**
	 * The table column <code>inventory.chemical.nfpa_s</code>
	 */
	public void setNfpaS(
			final edu.iup.chem.inventory.db.inventory.enums.ChemicalNfpaS value) {
		setValue(
				edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.NFPA_S,
				value);
	}

	/**
	 * The table column <code>inventory.chemical.smiles</code>
	 */
	public void setSmiles(final java.lang.String value) {
		setValue(
				edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.SMILES,
				value);
	}

	/**
	 * The table column <code>inventory.chemical.storage_class</code>
	 */
	public void setStorageClass(
			final edu.iup.chem.inventory.db.inventory.enums.ChemicalStorageClass value) {
		setValue(
				edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.STORAGE_CLASS,
				value);
	}

	/**
	 * The table column <code>inventory.chemical.toxic</code>
	 */
	public void setToxic(
			final edu.iup.chem.inventory.db.inventory.enums.ChemicalToxic value) {
		setValue(
				edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL.TOXIC,
				value);
	}

	@Override
	public String toString() {
		return getCas() + ": " + getName();
	}
}
