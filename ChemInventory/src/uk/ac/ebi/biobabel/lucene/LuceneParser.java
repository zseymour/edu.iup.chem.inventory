package uk.ac.ebi.biobabel.lucene;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ebi.biobabel.util.StringUtil;

/**
 * Class to parse user-provided search terms usable by Lucene queries. <br>
 * Complex queries can be built by concatenating Strings resulting from the
 * different <code>parse*Terms</code> methods. This is already implemented in
 * the convenience methods
 * {@link #parseQuery(String, String, String, String, String, boolean)} and
 * {@link #parseQuery(String, String, String, String, String, String, String, String, boolean)}
 * . <br>
 * Alternatively, a user can enter a complex query in a single search box, by
 * using the <code>+</code> and <code>-</code> prefixes for <code>AND</code> and
 * <code>NOT</code> terms, and double quotes for phrase search (see
 * {@link #parseUserTerms(String)} and {@link #parseUserTerms(String, String)}.
 * 
 * @author rafalcan
 * 
 */
public class LuceneParser {

	/**
	 * Lucene query operator
	 * 
	 * @author rafalcan
	 * 
	 */
	public enum Operator {
		OR, AND, NOT
	}

	/**
	 * Escapes characters with special meaning for Lucene, except asterisk (*)
	 * and question mark (?), as these should be allowed to use as wildcards. <br>
	 * Besides, it removes these two characters from the beginning of the
	 * string, as they are not allowed by Lucene.
	 * 
	 * @param s
	 *            the String to be escaped.
	 * @return an escaped String which can be passed to Lucene.
	 */
	public static String escapeLuceneSpecialChars(final String s) {
		return s.replaceAll(
				"(\\+|-|&|\\||!|\\(|\\)|\\{|\\}|\\[|\\]|\\^|\"|~|:|\\\\)",
				"\\\\$1").replaceAll("^(\\?)+", "");
	}

	/**
	 * Groups several terms, or even subgroups, into one Lucene query string.
	 * 
	 * @param op
	 *            Operator applied to the whole group (defaults to OR).
	 * @param field
	 *            Field name applied to every term of the group.
	 * @param escape
	 *            Escape Lucene special characters?
	 * @param boost
	 *            boost value (<code>null</code> safe, but must be greater than
	 *            zero) applied to the whole group.
	 * @param qq
	 *            terms or subgroups to group.
	 * @return a grouped Lucene query string.
	 * @since 1.0.1
	 */
	public static String group(final Operator op, final String field,
			final boolean escape, final Float boost, final String... qq) {
		if (boost != null && boost < 0) {
			throw new IllegalArgumentException(
					"Lucene boost value less than zero");
		}
		final StringBuilder sb = new StringBuilder("(");
		if (!StringUtil.isNullOrEmpty(field)) {
			sb.insert(0, ':').insert(0, field);
		}
		if (op != null) {
			switch (op) {
				case AND:
					sb.insert(0, '+');
					break;
				case NOT:
					sb.insert(0, '-');
					break;
				default:
					break;
			}
		}
		for (int i = 0; i < qq.length; i++) {
			if (i > 0) {
				sb.append(' ');
			}
			sb.append(escape ? escapeLuceneSpecialChars(qq[i]) : qq[i]);
		}
		sb.append(')');
		if (boost != null) {
			sb.append('^').append(boost.toString());
		}
		return sb.toString();
	}

	/**
	 * Parses a user query as prohibited terms.
	 * 
	 * @param qNot
	 *            Prohibited search terms.
	 * @param notField
	 *            Field where prohibited terms are searched. If
	 *            <code>null</code>, the default Lucene field will be searched.
	 * @return A Lucene query string with any special characters properly
	 *         escaped.
	 */
	public static String parseNotTerms(final String qNot, final String notField) {
		final StringBuilder sb = new StringBuilder();
		for (final String word : qNot.split("\\s+")) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append('-');
			if (!StringUtil.isNullOrEmpty(notField)) {
				sb.append(notField).append(":");
			}
			sb.append(escapeLuceneSpecialChars(word));
		}
		return sb.toString();
	}

	/**
	 * Parses a user query as Lucene OR'ed terms.
	 * 
	 * @param qOr
	 *            Optional (OR'ed) search terms.
	 * @param orField
	 *            Field where OR'ed terms are searched. If <code>null</code>,
	 *            the default Lucene field will be searched.
	 * @return A Lucene query string with any special characters properly
	 *         escaped.
	 */
	public static String parseOrTerms(final String qOr, final String orField) {
		final StringBuilder sb = new StringBuilder();
		for (final String word : qOr.split("\\s+")) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			if (!StringUtil.isNullOrEmpty(orField)) {
				sb.append(orField).append(":");
			}
			sb.append(escapeLuceneSpecialChars(word));
		}
		return sb.toString();
	}

	/**
	 * Parses a user query as a Lucene phrase.
	 * 
	 * @param qPhrase
	 *            Search terms as a whole phrase.
	 * @param phraseField
	 *            Field where the phrase is searched. If <code>null</code>, the
	 *            default Lucene field will be searched.
	 * @return A Lucene query string. Special characters are <i>not</i> escaped.
	 */
	public static String parsePhraseTerms(final String qPhrase,
			final String phraseField) {
		final StringBuilder sb = new StringBuilder();
		if (!StringUtil.isNullOrEmpty(phraseField)) {
			sb.append(phraseField).append(':');
		}
		sb.append('"').append(qPhrase).append('"');
		return sb.toString();
	}

	/**
	 * Convenience method to parse text values - most probably taken from a user
	 * interface - into a query string to search in a single index field.
	 * 
	 * @see #parseQuery(String, String, String, String, String, String, String,
	 *      String, boolean)
	 * @param qOr
	 *            Optional (OR'ed) search terms.
	 * @param qReq
	 *            Required search terms.
	 * @param qPhrase
	 *            Search terms as a whole phrase.
	 * @param qNot
	 *            Prohibited search terms.
	 * @param field
	 *            Field where terms are searched. If <code>null</code>, the
	 *            default Lucene field is used.
	 * @param greedy
	 *            If this is set to true all fields will be built using the
	 *            greedy operator '+'
	 * @return a String in Lucene syntax, with any special characters properly
	 *         escaped.
	 */
	public static String parseQuery(final String qOr, final String qReq,
			final String qPhrase, final String qNot, final String field,
			final boolean greedy) {
		return parseQuery(qOr, field, qReq, field, qPhrase, field, qNot, field,
				greedy);
	}

	/**
	 * Builds a Lucene query string from text values - most probably taken from
	 * a user interface.<br>
	 * Any <code>null</code> or empty query terms (<code>q*</code> parameters)
	 * are simply ignored.<br>
	 * In case any <code>*Field</code> parameter is <code>null</code>, the
	 * default Lucene field will be searched.
	 * 
	 * @param qOr
	 *            Optional (OR'ed) search terms.
	 * @param orField
	 *            Field where OR'ed terms are searched.
	 * @param qReq
	 *            Required search terms.
	 * @param reqField
	 *            Field where required terms are searched.
	 * @param qPhrase
	 *            Search terms as a whole phrase.
	 * @param phraseField
	 *            Field where the phrase is searched.
	 * @param qNot
	 *            Prohibited search terms.
	 * @param notField
	 *            Field where prohibited terms are searched.
	 * @param greedy
	 *            If this is set to true all fields will be built using the
	 *            greedy operator '+'
	 * @return a String in Lucene syntax, with any special characters properly
	 *         escaped.
	 */
	public static String parseQuery(final String qOr, final String orField,
			final String qReq, final String reqField, final String qPhrase,
			final String phraseField, final String qNot, final String notField,
			final boolean greedy) {
		final StringBuilder parsedQuery = new StringBuilder();
		if (!StringUtil.isNullOrEmpty(qOr)) {
			parsedQuery.append(parseOrTerms(qOr, orField));
		}
		if (!StringUtil.isNullOrEmpty(qReq)) {
			if (parsedQuery.length() > 0) {
				parsedQuery.append(' ');
			}
			parsedQuery.append(parseReqTerms(qReq, reqField, greedy));
		}
		if (!StringUtil.isNullOrEmpty(qPhrase)) {
			if (parsedQuery.length() > 0) {
				parsedQuery.append(' ');
			}
			parsedQuery.append(parsePhraseTerms(qPhrase, phraseField));
		}
		if (!StringUtil.isNullOrEmpty(qNot)) {
			if (parsedQuery.length() > 0) {
				parsedQuery.append(' ');
			}
			parsedQuery.append(parseNotTerms(qNot, notField));
		}
		return parsedQuery.toString();
	}

	/**
	 * This method parsers range queries. For example it will result in
	 * something like this: charge:[ 0 TO 2 ]
	 * 
	 * This will search for all charges between 0 and 2 inclusive.
	 * 
	 * @param rangeStart
	 *            the start of the query.
	 * @param rangeEnd
	 *            the end of the range query.
	 * @param field
	 *            the field to query.
	 * @param greedy
	 *            If this is set to true all fields will be built using the
	 *            greedy operator '+'
	 * @return a string to parse.
	 */
	public static String parseRangeQueries(final String rangeStart,
			final String rangeEnd, final String field, final boolean greedy) {
		final StringBuilder parsedQuery = new StringBuilder();
		if (greedy) {
			parsedQuery.append('+');
		}
		parsedQuery.append(field).append(':').append("[").append(rangeStart)
				.append(" TO ").append(rangeEnd).append("]");
		return parsedQuery.toString();
	}

	/**
	 * Parses a user query as Lucene required terms.
	 * 
	 * @param qReq
	 *            Required search terms.
	 * @param andField
	 *            Field where required terms are searched. If <code>null</code>,
	 *            the default Lucene field will be searched.
	 * @param greedy
	 *            If this is set to true all fields will be built using the
	 *            greedy operator '+'
	 * @return A Lucene query string with any special characters properly
	 *         escaped.
	 */
	public static String parseReqTerms(final String qReq,
			final String andField, final boolean greedy) {
		final StringBuilder sb = new StringBuilder();
		for (final String word : qReq.split("\\s+")) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			if (greedy) {
				sb.append('+');
			}
			if (!StringUtil.isNullOrEmpty(andField)) {
				sb.append(andField).append(":");
			}
			sb.append(escapeLuceneSpecialChars(word));
		}
		return sb.toString();
	}

	public static String parseUserTerms(final String userTerms) {
		return parseUserTerms(userTerms, null);
	}

	/**
	 * Parses an advanced user query, which may use <code>+</code> and
	 * <code>-</code> prefixes for <code>AND</code> and <code>NOT</code> terms,
	 * and double quotes for phrase search.
	 * 
	 * @param userTerms
	 * @param field
	 * @return
	 */
	public static String parseUserTerms(final String userTerms,
			final String field) {
		final StringBuilder parsedQuery = new StringBuilder();
		for (String term : splitQueryTerms(userTerms)) {
			if (parsedQuery.length() > 0) {
				parsedQuery.append(' ');
			}
			if (term.startsWith("+")) {
				parsedQuery.append('+');
				term = term.substring(1);
			} else if (term.startsWith("-")) {
				parsedQuery.append('-');
				term = term.substring(1);
			}
			parsedQuery.append(term.startsWith("\"") ? term
					: escapeLuceneSpecialChars(term));
		}
		if (!StringUtil.isNullOrEmpty(field)) {
			parsedQuery.insert(0, '(').insert(0, ':').insert(0, field);
			parsedQuery.append(')');
		}
		return parsedQuery.toString();
	}

	/**
	 * Splits the user query in terms, delimited by
	 * <ol>
	 * <li>double quotes</li>
	 * <li>spaces</li>
	 * </ol>
	 * , so that <code>-a "b c" +d</code> will result in
	 * <code>[ "-a", "\"b c\"", "+d" ]</code>.<br>
	 * The characters <code>+</code> and <code>-</code> at the beginning of a
	 * quoted term don't interfere with the splitting, i.e.
	 * <code>a +"b c" +d</code> will result in
	 * <code>[ "a", "+\"b c\"", "+d" ]</code>.<br>
	 * Missing closing quotes results in the rest of the terms in the query
	 * being considered as one.
	 * 
	 * @param userTerms
	 * @return a list of terms in the query
	 * @since 1.0.2
	 */
	public static List<String> splitQueryTerms(final String userTerms) {
		final List<String> spliTerms = new ArrayList<>();
		final String[] terms = userTerms.split("\\s+");
		for (int i = 0; i < terms.length; i++) {
			if (terms[i].matches("^[\\+-]?\".*")) {
				final StringBuilder splitTerm = new StringBuilder(terms[i]);
				while (!terms[i].endsWith("\"")) {
					if (++i < terms.length) {
						splitTerm.append(' ').append(terms[i]);
					} else { // no more terms
						splitTerm.append('"'); // missing quote
						break;
					}
				}
				spliTerms.add(splitTerm.toString());
			} else {
				spliTerms.add(terms[i]);
			}
		}
		return spliTerms;
	}
}
