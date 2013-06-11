/*
Copyright (c) 2005 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.biobabel.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import uk.ac.ebi.biobabel.validator.SyntaxValidator;


/**
 * StringUtil provides static helper methods which allow easy manipulation
 * or validation of strings used throughout.
 * <p/>
 * Logger implementations: Log4J-1.2.8<br>
 * JDK: Java 1.4.2.08<br>
 *
 * @author P. de Matos
 * @version $id 11-Jul-2005 15:50:16
 *          <p/>
 *          History:
 * <table>
 * <tr><th>Developer</th><th>Date</th><th>Description</th></tr>
 * <tr><td>P. de Matos</td><td>11-Jul-2005</td><td>Created class</td></tr>
 * <tr><td>P. de Matos</td><td>14-Jul-2005</td><td>String Reverse Sorting comparator added.</td></tr>
 * <tr><td>P. de Matos</td><td>09-Aug-2005</td><td>Adding method isInteger.</td></tr>
 * </table>
 * @see java.lang.String
 */
public final class StringUtil {

   //-------------------------- VARIABLES -----------------------------------//

   /** Private Log4j Logger.*/
   private static Logger logger = Logger.getLogger(StringUtil.class);

   /** Static convenience variable of the String sorting comparator. */
   private static final StringReverseCasingComparator STRING_REVERSE_CASING_COMPARATOR =
         new StringReverseCasingComparator();
   /** Pattern used to determine whether a string is uppercased. */
   private static final Pattern UPPER_CASE = Pattern.compile( "[\\p{Upper}\\p{Digit}]+?" );

   //-------------------------- CONSTRUCTORS --------------------------------//

   /** Private constructor as this class provides only static methods. */
   private StringUtil () { }

   /**
    * This method tests whether a string is null and if its not null it
    * checks if its empty.<br>
    * Empty is defined when the string equals("")<br>
    * <p/>
    * Notes:<br>
    * Absolutely no manipulation is done on the string.<br>
    * It is upto the user to manipulate or trim the string before hand.<br>
    * No null pointer is thrown as the string is first checked for null
    * values.<br>
    * <strong>History:</strong><br/>
    * <table>
    * <tr><th>Developer</th><th>Date</th><th>Description</th></tr>
    * <tr><td>P. de Matos</td><td>11-Jul-2005</td>
    * <td>Created method and tested it</td></tr>
    * </table>
    *
    * @param value String to be tested.
    * @return true if the string provided is null or empty.
    */
   public static boolean isNullOrEmpty (String value) {

      // Variable used to store result is set to false by default;
      boolean isNullOrEmpty = false;
      logger.debug("StringUtil.java - isNullOrEmpty(" + value + ")");

      // Test first whether null and then whether is empty.
      // If value is neither null nor empty then result will be false.
      if (value != null && !value.equals("")) {
         isNullOrEmpty = false;
      // If value is null or empty then result will be true.
      } else {
         isNullOrEmpty = true;
      }

      // Return result and log
      logger.debug("StringUtil.java - isNullOrEmpty: " + isNullOrEmpty);
      return isNullOrEmpty;
   }

   /**
    * Compares two strings lexicographically ignoring and reversing the case
    * ordering if they are equal.<br/>
    * The comparison is based on the Unicode value of each character in
    * the strings. The character sequence represented by this
    * <code>String</code> object is compared lexicographically to the
    * character sequence represented by the argument string. The result is
    * a negative integer if this <code>String</code> object
    * lexicographically precedes the argument string. The result is a
    * positive integer if this <code>String</code> object lexicographically
    * follows the argument string. The result is zero if the strings
    * are equal; <code>compareTo</code> returns <code>0</code> exactly when
    * the {@link #equals(Object)} method would return <code>true</code>.
    * The comparison followed is identical to the normal <code>compareTo</code>
    * method of the <code>String</code> class.
    * <p>
    * The exception occurs when the two String comparisons are identical excluding
    * the case. This means that every character is equal to the last one ignoring
    * an uppercasing or lowercasing. If one string is shorter than the other then
    * the strings are considered identical upto the last valid character. The
    * comparator will then order the two strings with priority to lowercasing.
    * In which case the ordering is provided [aA-zA].<br/>
    *
    * Examples:<br/>
    * String a: "TeStInG"<br/>
    * String b: "tEsT"<br/>
    * In this case "b" will be ordered first over "a".<br/>
    *
    * @return Comparator which compares two Strings lexicographically and
    *         reverses the order when they are identical but with different
    *         casing.<br/>
    */
   public static Comparator getStringReverseCasingComparator() {
      return STRING_REVERSE_CASING_COMPARATOR;
   }

   /**
    * This method tests whether the string given as a parameter is an integer.
    * An integer is defined as numbers between [0-9] with no decimal points.
    * It will throw a null pointer exception if the string is null. If the string
    * is empty it will also return false as it contains no digits.
    * Examples:<br/>
    * "9.0" = false<br/>
    * "99" = true<br/>
    * <strong>History:</strong><br/>
    * <table>
    * <tr><th>Developer</th><th>Date</th><th>Description</th></tr>
    * <tr><td>P. de Matos</td><td>11-Jul-2005</td>
    * <td>Was originally created by mzbinden but was moved across to biobabel.</td></tr>
    * </table>
    * @param strInteger
    * @return true if the string provided is an integer.
    */
   public static boolean isInteger(String strInteger) {
      logger.debug("StringUtil - isInteger("+strInteger+")");
      // converts to a char array
        char[] strChar = strInteger.toCharArray();
        boolean bolInteger = true;
      // if the char is empty then we return false
        if(strChar.length==0) {
            bolInteger = false;
        }
      // iterate through the array and if there is one occurence
      // that is not a digit return false.
        for(int k=0; k<strChar.length; k++) {
            if(!Character.isDigit(strChar[k])) {
                bolInteger = false;
                break;
            }
        }
      logger.debug("StringUtil - isInteger("+bolInteger+")");
      return bolInteger;
    }

   /**
    * This method takes a string and determines whether the string is all
    * uppercased. It is fully tested for all alphanumeric [a-Z,0-9] characters
    * but it is unkown what the result will be for any special characters
    * such as unicode.<br/>
    * Digit characters are assumed to be both lower and upper case
    * and will return true. Spaces are treated as lowercase.
    * An empty string will also return true as it is neither.<br/>
    *
    * The implementation used is the <code>Character.isUpperCase</code> found
    * in the jdk.<br/>
    *
    * Examples:<br/>
    * "9op" returns false<br/>
    * "OP123" returns true<br/>
    *
    * @see Character
    *
    * @param string The string parameter.
    * @return true if the input parameter is upper cased.
    */
   public static boolean isUpperCase (String string) {
      logger.debug("StringUtil - isUpperCase("+string+")");
      boolean isUpperCase = true;
      char[] strChar = string.toCharArray();

      // iterate through the array and if there is one occurence
      // that is not an uppercase return false.
        for(int k=0; k<strChar.length; k++) {
           if(!Character.isDigit( strChar[k])){
               if(!Character.isUpperCase(strChar[k])) {
                   isUpperCase = false;
                   break;
               }
           }
        }

      logger.debug("isUpperCase - result : "+isUpperCase);
      return isUpperCase;
   }

   /**
    * This method trims the string into chunkSize of data specified by the length
    * specified in the second parameter of the int.
    *
    * Note:
    * This method will throw a null pointer if the string is null.<br/>
    * If the chunk size is zero then the string will be returned in its entirety
    * as an array of one element.<br/>
    * If the string is empty then it will be return in an array with one element.
    * <br/>
    *
    * Example:<br/>
    * Input: subString("test", 2);
    * Returns: a string array of lenght 2 containing {te}{st} in the array.
    *
    * @param data the data to be cut up into chunks
    * @param chunkSize the char size of each chunk
    * @return an array of chunkSize of data
    */
   public static String [] toArray ( String data, int chunkSize ){
      logger.debug("StringUtil - subString(" + data + " , " + chunkSize + ")");
      // variables
      int length = data.length();
      // start of chunkSize
      int start = 0;
      // end of chunkSize
      int end = chunkSize;
      // index counter of array
      int counter = 0;
      // array to return
      String[] array;
      // the lenght that the array should be
      int initializeArrayLength = 0;

      // If the chunkSize are zero or the lenght of the string is emptyr
      // then return the string in array.
      if (chunkSize ==0 || length == 0) {
         array = new String[]{data};
         return array;
      }

      initializeArrayLength = length/chunkSize;
      // make an extra space for the remainder
      if(length%chunkSize!=0) ++initializeArrayLength;

      array = new String[initializeArrayLength];

      // Iterate through array sub stringing the data into the correct chunkSize.
      while( start < length && counter<array.length){
         if( end > length) end = length;
         array[counter] = data.substring(start, end);
         counter++;
         start = end;
         end = end + chunkSize;
      }
      logger.debug("subString - result (" + array.toString() + ")");
      return array;
   }

   //-------------------------- PRIVATE METHODS -----------------------------//

   /**
    * This private class is based on the comparator interface and
    * its primary goal is to sort String data in the order
    * [aA-zZ] as opposed to the framework [Aa-Zz].<br>
    * The sorting algorithm is based on the normal ascending
    * java.lang.String order comparison. The only difference occurs
    * where the elements are identical regardless of case. The lowercase
    * always take precedence in this instance.<br>
    * The objects passed to the <code>compare</code> method must be mutually
    * comparable i.e. they must be an instance of java.lang.String else a
    * ClassCastException is thrown.<br>
    * @see Comparator
    */
   private static class StringReverseCasingComparator implements Comparator {

      /**
       * This method provides a basic comparison for the two <code>String</code>
       * objects on ascending order and based on the sorting [aA-zZ].<br>
       *
       * @param o The first String object to be passed.
       * @param o1 The second String object to be passed.
       * @return a negative integer, zero, or a positive integer as the
       * first argument is less than, equal to, or greater than the
       *	second.
       * @throws ClassCastException if the arguments' types prevent them from
       * being compared by this Comparator.
       */
      public int compare (Object o, Object o1) {
         // Casting variables
         String s0 = (String) o;
         String s1 = (String) o1;

         // Compare first to see if they are equal regardless of case.
         int comparison = s0.compareToIgnoreCase(s1);
         // If they are equal invert their comparison when done normally without case.
         // This will provide the reverse sorting i.e. [aA-zZ] instead of [Aa-Zz].
         if (comparison == 0) {
            int result = s0.compareTo(s1);
            result = (-1) * result;
            return result;
         }

         // return the int which determines whether s0 is bigger than s1
         return comparison;
      }
   }
   
   /**
    * Gets pairs of matching (plain, square, curly) brackets.
    * @param s A <code>String</code>
    * @return an array of two dimensions, the second one being 2 - the pair
    * 		of indexes for the matching brackets, or <code>null</code>
    * 		if there are no brackets.
    * @throws IllegalArgumentException if there are any brackets without match.
    * 		Note that overlapping brackets (ex. <code>abc(de[fg)hi]</code>)
    * 		will be accepted, though. These can be caught using
    * 		{@link SyntaxValidator}.
    */
   public static int[][] getMatchingBrackets(String s){
	   int[][] indexes = null;
	   final String OPENINGS = "([{";
	   final String CLOSINGS = ")]}";
	   Map matches = new TreeMap();
	   for (int i = 0; i < s.length(); i++){
		   char c1 = s.charAt(i);
		   int openingChar = OPENINGS.indexOf(c1);
		   if (openingChar > -1){
			   int openBrackets = 0;
			   boolean foundMatch = false;
			   for (int j = i+1; j < s.length(); j++){
				   char c2 = s.charAt(j);
				   if (OPENINGS.indexOf(c2) == openingChar){
					   openBrackets++;
					   continue;
				   }
				   int closingChar = CLOSINGS.indexOf(c2);
				   if (closingChar > -1 && closingChar == openingChar){
					   if (openBrackets > 0){
						   openBrackets--;
						   continue;
					   }
					   matches.put(new Integer(i), new Integer(j));
					   foundMatch = true;
					   break;
				   }
			   }
			   if (!foundMatch){
				   throw new IllegalArgumentException(
						   "Match not found for opening bracket at position "+i+" of String '"+s+"'");
			   }
		   }
		   if (CLOSINGS.indexOf(c1) > -1 && !matches.values().contains(new Integer(i))){
			   throw new IllegalArgumentException(
					   "Match not found for closing bracket at position "+i+" of String '"+s+"'");			   
		   }
	   }
	   if (!matches.isEmpty()){
		   indexes = new int[matches.size()][2];
		   int i = 0;
		   for (Iterator it = matches.entrySet().iterator(); it.hasNext();) {
			   Entry entry = (Entry) it.next();
			   indexes[i][0] = ((Integer) entry.getKey()).intValue();
			   indexes[i][1] = ((Integer) entry.getValue()).intValue();
			   i++;
		   }
	   }
	   return indexes;
   }
}
