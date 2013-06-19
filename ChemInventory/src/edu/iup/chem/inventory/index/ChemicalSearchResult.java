package edu.iup.chem.inventory.index;

public class ChemicalSearchResult {
	private String	primaryKey;
	private String	additionalField;
	private float	score;

	public ChemicalSearchResult(final String primaryKey,
			final String additionalField, final float score) {
		super();
		this.primaryKey = primaryKey;
		this.additionalField = additionalField;
		this.score = score;
	}

	/**
	 * @return the additionalField
	 */
	public String getAdditionalField() {
		return additionalField;
	}

	/**
	 * @return the primaryKey
	 */
	public String getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * @return the score
	 */
	public float getScore() {
		return score;
	}

	/**
	 * @param additionalField
	 *            the additionalField to set
	 */
	public void setAdditionalField(final String additionalField) {
		this.additionalField = additionalField;
	}

	/**
	 * @param primaryKey
	 *            the primaryKey to set
	 */
	public void setPrimaryKey(final String primaryKey) {
		this.primaryKey = primaryKey;
	}

	/**
	 * @param score
	 *            the score to set
	 */
	public void setScore(final float score) {
		this.score = score;
	}

}
