/*
Copyright (c) 2005 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.biobabel.validator;


/**
 * MethodType defines the method that will be called to validate the object.
 * The information specified includes the method name, the class where this
 * method resides and the parameter types of this method.
 *
 * @author P. de Matos
 * @version $id 07-Oct-2005 15:45:19
 *          <p/>
 *          History:<br> <table> <tr><th>Developer</th><th>Date</th><th>Description</th></tr>
 *          <tr><td>P.de Matos</td><td>07-Oct-2005</td><td>Created
 *          class</td></tr> </table>
 */
public class MethodType {

   Class clazz;

   String methodName;

   Class[] parameterTypes;

   public MethodType ( Class clazz, String methodName, Class[] parameterTypes ) {
      this.clazz = clazz;
      this.methodName = methodName;
      this.parameterTypes = parameterTypes;
   }

   public Class getClazz () {
      return clazz;
   }

   public void setClazz ( Class clazz ) {
      this.clazz = clazz;
   }

   public String getMethodName () {
      return methodName;
   }

   public void setMethodName ( String methodName ) {
      this.methodName = methodName;
   }

   public Class[] getParameterTypes () {
      return parameterTypes;
   }

   public void setParameterTypes ( Class[] parameterTypes ) {
      this.parameterTypes = parameterTypes;
   }

}
