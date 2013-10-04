//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.cryptoEngine.idemix.util;

import java.io.File;
import java.net.URI;

public class IdemixConstants {
	
	 /**
     * Number of attributes an issuer key supports (i.e., number of bases
     * excluding the reserved attributes such as the master secret).
     */
    public static final int NBR_ATTRS = 9;
    /**
     * Issuer public key should have epoch length of 120 days -- 432000 seconds.
     * Note that this will require him to issuer an update for each credential
     * every 120 days.
     */
    public static final int EPOCH_LENGTH = 432000;
    
	public static final String nameOfNewCredential = "newCredential";
    public static final String tempNameOfNewCredential = "credentialToBeIssued";
	public static final String nameOfNewSecret = "secretToBeGenerated";
	
	  ///////////////////////////////////////////////////////////////
	  //All locations
	  ///////////////////////////////////////////////////////////////
		// Locations and file names
		// TODO: replace with resources
		public static final URI BASE_LOCATION 				= new File(System.getProperty("user.dir")).toURI().resolve("src/test/java-ibm-only/eu/abc4trust/idemix/resources/");
		public static final URI credStructBase 				= BASE_LOCATION.resolve("credentialTypes/");
		public static final URI iskBase 					= BASE_LOCATION.resolve("issuerSecretKeys/");
		public static final URI ipkBase 					= BASE_LOCATION.resolve("issuerPublicKeys/");
		public static final URI userBase 					= BASE_LOCATION.resolve("resourcesUser/");
		public static final URI paramBase 					= BASE_LOCATION.resolve("parameter/");
		public static final String baseId 					= "http://www.zurich.ibm.com/security/idmx/v2/";
		public static final String masterSecretName			= "ms.xml";
		public static final String groupParamFileName		= "gp.xml";
		public static final String sysParamFileName			= "sp.xml";
		
		// Common IDs and locations
		public static final String groupParameterId 		= baseId + groupParamFileName;
		public static final URI groupParameterLocation 		= paramBase.resolve(groupParamFileName);
		public static final String systemParameterId 		= baseId + sysParamFileName;
		public static final URI systemParameterLocation 	= paramBase.resolve(sysParamFileName);
		
		// URIs and locations for the user
	  public static final URI userMasterSecretLocation 		= userBase.resolve(masterSecretName);
	  public static final String nameOfMockSmartCard        = "secret://sample-1234";
	  //= "theUltimateSmartcard";
	  public static final String nymName                    = "IdmxNym";
	public static String messageName                        = "msgToSignIdmx";
	  

}
