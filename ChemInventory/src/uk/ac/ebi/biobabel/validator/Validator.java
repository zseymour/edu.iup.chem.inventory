/*
Copyright (c) 2005 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.biobabel.validator;

import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import uk.ac.ebi.biobabel.util.StringUtil;


/**
 * Validator abstract class is the uppermost class of all validators in this
 * package. It provides a public method called validate which all validations
 * are called from. The ValidatorType specifies which type of validation is
 * performed. All validator types are found in the validator class themselves.
 *
 * @author P. de Matos
 * @version $id 06-Oct-2005 13:48:04
 *          <p/>
 *          History:<br> <table> <tr><th>Developer</th><th>Date</th><th>Description</th></tr>
 *          <tr><td>P.de Matos</td><td>06-Oct-2005</td><td>Created
 *          class</td></tr> </table>
 */
public abstract class Validator {

   //-------------------------- VARIABLES -----------------------------------//
   /**
    * Parameter type which is used for invoking methods.
    */
   protected static Class[] stringParaType = new Class[]{String.class};

   /**
    * Private Log4j Logger.
    */
   private static Logger logger = Logger.getLogger(Validator.class);

   //-------------------------- PUBLIC METHODS ------------------------------//

   /**
    * This method validates the data parameter by invoking the methods provided
    * in the ValidatorType object.
    *
    * @param dataToValidate The data that should be validated.
    * @param type           This a prespecified dataformat found defined withing
    *                       the class.
    * @return true if the data item is valid and false if not.
    */
   public boolean validate (String dataToValidate, ValidatorType type) {
      logger.debug(
            "Validator.java - validate (" + dataToValidate + " , " + type.toString() + " )");

      checkValidatorType(type);

      boolean result = true;


      // Reflection has been used here to prevent the many if-else
      // statements that we would encounter otherwise.
      MethodType[] methodTypes = type.getMethods();
      for ( int iii = 0, length = methodTypes.length; iii < length; iii++ ) {

         MethodType methodType = methodTypes[iii];
         try {
            // invokation of the method.
            result = this.invokeMethod( dataToValidate, methodType );
            if ( !result ) return result;

         } catch ( IllegalAccessException e ) {
            logger.error("Exception on "+type.toString()+" dataToValidate "+dataToValidate+e.getMessage());
         } catch ( InvocationTargetException e ) {
            logger.error("Exception on "+type.toString()+" dataToValidate "+dataToValidate+e.getMessage());
         } catch ( NoSuchMethodException e ) {
            logger.error("Exception on "+type.toString()+" dataToValidate "+dataToValidate+e.getMessage());
         }
      }

      logger.debug("Validator.java - validate return: " + result);

      // return the result
      return result;
   }

   //-------------------------- PROTECTED METHODS ---------------------------//

   /**
    * This method returns the allowed validator types for the specific
    * validator. All validator types allowed for the specific validator are
    * initialised in the constructor.
    *
    * @return A set containing instances of ValidatorType
    * @see ValidatorType
    */
   protected abstract Set getAllowedValidatorTypeSet ();


   protected boolean isDigit(String data){
      return StringUtil.isInteger( data );
   }

   //-------------------------- PRIVATE METHODS -----------------------------//

   /**
    * Method used to invoke a method within the class using reflection.
    *
    * @param dataToValidate
    * @param methodType
    * @return the result of the methods invoked.
    * @throws IllegalAccessException
    * @throws java.lang.reflect.InvocationTargetException
    *
    * @throws NoSuchMethodException
    */
   private boolean invokeMethod ( String dataToValidate, MethodType methodType )
         throws IllegalAccessException,
         InvocationTargetException,
         NoSuchMethodException {

      logger.debug(
            "Validator.java - invokeMethod (" + methodType + " , " + dataToValidate + " )");

      Method method = methodType.getClazz().getDeclaredMethod(methodType.getMethodName(),
            methodType.getParameterTypes());
      boolean result = ((Boolean) method.invoke(this,
            new Object[]{dataToValidate})).booleanValue();

      logger.debug("Validator.java - invokeMethod return: " + result);
      return result;
   }

   /**
    * This a validation method used to make sure that the specified
    * ValidatorType provided as a parameter is part of this validators set. If
    * it is not the case then a run time exception will be thrown.
    *
    * @param type
    * @throws IllegalArgumentException Thrown if the type is part of this
    *                                  validators type set.
    */
   private void checkValidatorType (ValidatorType type) {
      logger.debug(
            "Validator.java - checkValidatorType (" + type.toString() + " )");
      // Check that the ValidatorType specified is one of the constants defined
      // in this class
      if ( !getAllowedValidatorTypeSet().contains(type) ) {
         logger.debug(
               "Validator.java - checkValidatorType: IllegalArgumentException thrown due to incorrect type.");
         throw new IllegalArgumentException(
               "The parameter ValidatorType called type is not defined as part of these constants");
      }
      // Return void
      return;
   }


}
