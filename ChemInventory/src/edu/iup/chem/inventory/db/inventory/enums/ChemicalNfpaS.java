/**
 * This class is generated by jOOQ
 */
package edu.iup.chem.inventory.db.inventory.enums;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "2.5.1"},
                            comments = "This class is generated by jOOQ")
public enum ChemicalNfpaS implements org.jooq.EnumType {
	OX("OX"),

	W("W"),

	None("None"),

	;

	private final java.lang.String literal;

	private ChemicalNfpaS(java.lang.String literal) {
		this.literal = literal;
	}

	@Override
	public java.lang.String getName() {
		return "chemical_nfpa_s";
	}

	@Override
	public java.lang.String getLiteral() {
		return literal;
	}
}
