package edu.iup.chem.inventory.search;

public class SpectraResult {
	String	csid;
	String	type;
	String	url;

	public SpectraResult(final String csid, final String type, final String url) {
		super();
		this.csid = csid;
		this.type = type;
		this.url = url;
	}

	/**
	 * @return the csid
	 */
	public String getCsid() {
		return csid;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

}
