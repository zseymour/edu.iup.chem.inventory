package edu.iup.chem.inventory.misc;

import java.util.HashMap;

public class CaseInsensitiveMap extends HashMap<String, String> {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 4706303777833921898L;

	public String get(final String key) {
		return super.get(key.toLowerCase());
	}

	public String[] getAll(final String[] keys) {
		final String[] values = new String[keys.length];
		for (int i = 0; i < keys.length; i++) {
			values[i] = get(keys[i]);
		}

		return values;
	}

	public String put(final String key) {
		return this.put(key, null);
	}

	@Override
	public String put(final String key, final String value) {
		return super.put(key.toLowerCase(), value);
	}

}
