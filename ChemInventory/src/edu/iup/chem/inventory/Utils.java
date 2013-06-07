package edu.iup.chem.inventory;

import java.awt.BorderLayout;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jsoup.Jsoup;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

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

	/**
	 * Creates a JPanel with the provided text centered in a JLabel
	 * 
	 * @param text
	 * @return
	 */
	public static JPanel getTextPanel(final String text) {
		final JPanel panel = new JPanel(new BorderLayout());
		final JLabel label = new JLabel(text, SwingConstants.CENTER);

		panel.add(label, BorderLayout.CENTER);

		return panel;

	}

	public static String[] intArrayToStringArray(final Integer[] ints) {
		Arrays.sort(ints);
		return Arrays.toString(ints).split("[\\[\\]]")[1].split(", ");
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

	public static void showMessage(final String title, final String message) {
		JOptionPane.showMessageDialog(null, message, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	public static String[] splitBottle(final String value) {
		final String[] result = new String[2];
		if (value.startsWith("W")) {
			result[0] = "W";
			result[1] = value.replace("W", "");
		} else if (value.startsWith("G")) {
			result[0] = "G";
			result[1] = value.replace("G", "");
		} else {
			result[0] = "";
			result[1] = value;
		}

		return result;
	}

	/**
	 * Splits a string like "500g" on the the gap between the numbers and
	 * letters i.e. into "500" and "g"
	 * 
	 * @param amount
	 * @return
	 */
	public static String[] splitUnits(final String amount) {
		return amount.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)\\s*");
	}

	public static List<Integer> stringListToIntList(final List<String> strings) {
		return Lists.transform(strings, new Function<String, Integer>() {
			@Override
			public Integer apply(final String input) {
				return Integer.valueOf(input);
			}
		});
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
