package edu.iup.chem.inventory;

import java.security.MessageDigest;

import org.jsoup.Jsoup;

public class Utils {
	private static String getString(final byte[] bytes) {
		final StringBuffer sb = new StringBuffer();
		for (final byte b : bytes) {
			final String hex = Integer.toHexString(0x00FF & b);
			if (hex.length() == 1) {
				sb.append("0");
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	public static boolean isAdmin() {
		final String role = Constants.CURRENT_USER.getRoleName();
		if (role == null || !userHasEditingPerm()
				|| role.equals(Constants.DATA_ENTRY_ROLE)) {
			return false;
		}

		return true;
	}

	public static boolean isNumeric(final String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (final Exception e) {
			return false;
		}
	}

	public static String md5(final char[] password) {
		final String source = new String(password);
		try {
			final MessageDigest md = MessageDigest.getInstance("MD5");
			final byte[] bytes = md.digest(source.getBytes("UTF-8"));
			return getString(bytes);
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String stripHtmlTags(final String name) {
		return Jsoup.parse(name).text();
	}

	public static boolean userHasEditingPerm() {
		final String role = Constants.CURRENT_USER.getRoleName();
		if (role == null || role.equals(Constants.RESEARCHER_ROLE)
				|| role.equals(Constants.FACULTY_ROLE)
				|| role.equals(Constants.GUEST_ROLE)) {
			return false;
		}

		return true;
	}

}
