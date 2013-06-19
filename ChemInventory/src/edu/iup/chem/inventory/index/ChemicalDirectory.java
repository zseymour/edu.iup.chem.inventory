package edu.iup.chem.inventory.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;

public class ChemicalDirectory {
	private Directory		directory;
	private String			defaultField;

	private String			primaryKey;

	private Analyzer		analyzer;

	private final String[]	searchFields;

	public ChemicalDirectory(final Directory dir, final String defaultField,
			final String primaryKey, final Analyzer analyzer,
			final String[] fields) {
		super();
		directory = dir;
		this.defaultField = defaultField;
		this.primaryKey = primaryKey;
		this.analyzer = analyzer;
		searchFields = fields;
	}

	/**
	 * @return the analyzer
	 */
	public Analyzer getAnalyzer() {
		return analyzer;
	}

	/**
	 * @return the defaultField
	 */
	public String getDefaultField() {
		return defaultField;
	}

	/**
	 * @return the directory
	 */
	public Directory getDirectory() {
		return directory;
	}

	/**
	 * @return the primaryKey
	 */
	public String getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * @return the searchFields
	 */
	public String[] getSearchFields() {
		return searchFields;
	}

	/**
	 * @param analyzer
	 *            the analyzer to set
	 */
	public void setAnalyzer(final Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	/**
	 * @param defaultField
	 *            the defaultField to set
	 */
	public void setDefaultField(final String defaultField) {
		this.defaultField = defaultField;
	}

	/**
	 * @param directory
	 *            the directory to set
	 */
	public void setDirectory(final Directory directory) {
		this.directory = directory;
	}

	/**
	 * @param primaryKey
	 *            the primaryKey to set
	 */
	public void setPrimaryKey(final String primaryKey) {
		this.primaryKey = primaryKey;
	}

}
