//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.cryptoEngine.idemix.util;


import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ibm.zurich.idmx.dm.structure.CredentialStructure;

import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.util.XmlUtils;

public class IdemixUtilsTest{
  
  @Test
  public void testIdemixUtilsGenerateCredStructForOneOf() throws Exception {		
	  	 // Step 1. Load credspec from XML.
	     CredentialSpecification creditCardSpec =
	                (CredentialSpecification) XmlUtils.getObjectFromXML(getClass().getResourceAsStream(
	                		"/eu/abc4trust/sampleXml/soderhamn/credentialSpecificationSoderhamnClassOneOf.xml"), true);  
	     
	     // Step 2. Generate the credential structure
	     CredentialStructure cs = IdemixUtils.createIdemixCredentialStructure(creditCardSpec);
	     
	     assertTrue(cs!=null);
  }
  

}
