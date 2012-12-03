
/**
 * SynonymsCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */

    package edu.iup.chem.inventory.com.chemspider.www;

    /**
     *  SynonymsCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class SynonymsCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public SynonymsCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public SynonymsCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getStructureSynonyms method
            * override this method for handling normal response from getStructureSynonyms operation
            */
           public void receiveResultgetStructureSynonyms(
                    edu.iup.chem.inventory.com.chemspider.www.SynonymsStub.GetStructureSynonymsResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getStructureSynonyms operation
           */
            public void receiveErrorgetStructureSynonyms(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getStructureSynonymsInfo method
            * override this method for handling normal response from getStructureSynonymsInfo operation
            */
           public void receiveResultgetStructureSynonymsInfo(
                    edu.iup.chem.inventory.com.chemspider.www.SynonymsStub.GetStructureSynonymsInfoResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getStructureSynonymsInfo operation
           */
            public void receiveErrorgetStructureSynonymsInfo(java.lang.Exception e) {
            }
                


    }
    