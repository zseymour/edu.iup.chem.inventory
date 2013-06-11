package uk.ac.ebi.biobabel.validator;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ebi.biobabel.util.StringUtil;

public class SyntaxValidator extends Validator {
	
	public static final ValidatorType BRACKETS =
		new ValidatorType("Brackets validator", new MethodType[]{
			new MethodType(SyntaxValidator.class, "bracketsMatch", stringParaType)
	});

	public static final ValidatorType PUNCTUATION_END =
		new ValidatorType("Brackets validator", new MethodType[]{
			new MethodType(SyntaxValidator.class, "isValidPunctuationEnd", stringParaType)
	});

    private static final String INVALID_PUNCTUATION_END = ".+?[\\.\\:;,\\?]$";
	private static Validator instance = new SyntaxValidator();
	
	private Set<ValidatorType> validatorTypeSet;
	
	private SyntaxValidator(){
		validatorTypeSet = new HashSet<ValidatorType>();
		validatorTypeSet.add(BRACKETS);
        validatorTypeSet.add(PUNCTUATION_END);
	}
	
	public static Validator getInstance(){
		return instance;
	}
	
	protected Set getAllowedValidatorTypeSet() {
		return validatorTypeSet;
	}

	/**
	 * Checks that brackets in the parameter String are balanced and
	 * don't overlap.
	 * @param s
	 * @return <code>true</code> if all brackets are balanced and don't overlap,
	 * 		or if there are no brackets in the String.
	 */
	protected boolean bracketsMatch(String s){
		int[][] matches = null;
		try {
			matches = StringUtil.getMatchingBrackets(s);
		} catch (IllegalArgumentException e){
			return false;
		}
        if (matches!=null) {
            for (int i = 0; i < matches.length; i++) {
                int[] firstPair = matches[i];
                for (int j = i+1; j < matches.length; j++) {
                    int[] secondPair = matches[j];
                    // Overlapping pairs of brackets:
                    if (firstPair[1] > secondPair[0] && firstPair[1] < secondPair[1]){
                        return false;
                    }
                }
            }
        }
		return true;
	}

    /**
     * This method checks whether the string ends in an invalid punctuation mark.
     * .+?[\.\:;,\?]$
     * @param s parameter to validate
     * @return false if it ends in an invalid pucntioant mark
     */
    protected boolean isValidPunctuationEnd(String s){
        boolean isValid = true;
        if(! StringUtil.isNullOrEmpty(s)){
            if (s.matches(INVALID_PUNCTUATION_END)) {
                isValid = false;
            }
        }
        return isValid;
    }
}
