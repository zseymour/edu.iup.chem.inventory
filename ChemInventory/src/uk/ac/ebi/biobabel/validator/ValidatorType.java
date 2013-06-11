/*
Copyright (c) 2005 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.biobabel.validator;



/**
 * ValidatorType describes the type of validation to be performed.
 * Each Validator has associated validator types which can be performed by the
 * selected validator. These instances are final and should be used when
 * validating data.<br/>
 *
 * If the validations to be used are different to the ones that are specified
 * then a user can create their own ValidatorType instances and use that in
 * the validation method. The only prerequisite is that the name be the same
 * as the type of validation to be used.
 *
 * @author P. de Matos
 * @version $id 05-Oct-2005 15:35:42
 *          <p/>
 *          History:<br>
 *          <table>
 *          <tr><th>Developer</th><th>Date</th><th>Description</th></tr>
 *          <tr><td>P.de Matos</td><td>05-Oct-2005</td><td>Created class</td></tr>
 *          </table>
 */
public class ValidatorType {

   //-------------------------- VARIABLES -----------------------------------//

   /** This string array stores all the method names that should be executed. */
   private MethodType[] methods;
   /** This is the unique name of this validator type.*/
   private String name;

   //-------------------------- CONSTRUCTORS --------------------------------//

   /**
    * Constructor to initiliase the parameters.
    * @param name
    * @param methodNames
    */
   public ValidatorType ( String name, MethodType[] methodNames) {
      this.name = name;
      this.methods = methodNames;
   }

   //-------------------------- PUBLIC METHODS ------------------------------//

   /**
    * Gets the unique name.
    * @return
    */
   public String getName () {
      return name;
   }
   /**
    * Sets the unique name.
    * @param name
    */
   public void setName (String name) {
      this.name = name;
   }
   /**
    * Gets the method names associated with this type.
    * @return
    */
   public MethodType[] getMethods () {
      return methods;
   }
   /**
    * Sets the method names associated with this type.
    * @param methods
    */
   public void setMethods (MethodType[] methods) {
      this.methods = methods;
   }

   /**
    * Equals method is based only on the unique name of the validator type.
    * @param o
    * @return
    */
   public boolean equals (Object o) {
      if ( this == o ) return true;
      if ( !(o instanceof ValidatorType) ) return false;

      final ValidatorType validatorType = (ValidatorType) o;

      if ( name != null ? !name.equals(validatorType.name) : validatorType.name != null ) return false;

      return true;
   }

   /**
    * Hashcode also only based on the name.
    * @return
    */
   public int hashCode () {
      return (name != null ? name.hashCode() : 0);
   }

   /**
    * Returns all the information found in this datatype as a string.
    * @return String with the name and method name.
    */
   public String toString(){
      StringBuilder buffer = new StringBuilder(ValidatorType.class.getName());
      buffer.append(name); 
      buffer.append(";");
       for (int iii = 0; iii < methods.length; iii++) {
           MethodType method = methods[iii];
           buffer.append( method.getMethodName() );
       }
      buffer.append(".");
      return buffer.toString();
   }
}
