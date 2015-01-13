//* Licensed Materials - Property of                                  *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*                                                                   *
//* This file is licensed under the Apache License, Version 2.0 (the  *
//* "License"); you may not use this file except in compliance with   *
//* the License. You may obtain a copy of the License at:             *
//*   http://www.apache.org/licenses/LICENSE-2.0                      *
//* Unless required by applicable law or agreed to in writing,        *
//* software distributed under the License is distributed on an       *
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY            *
//* KIND, either express or implied.  See the License for the         *
//* specific language governing permissions and limitations           *
//* under the License.                                                *
//*/**/****************************************************************

package eu.abc4trust.services.revocation;

import static org.junit.Assert.assertNotNull;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import eu.abc4trust.services.ITAbstract;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;

public class ITGenerateNonRevocationEvidence extends ITAbstract {

    static ObjectFactory of = new ObjectFactory();

    @Test
    public void generateNonRevocationEvidence() throws Exception {

    	RevocationServiceFactory revocationServiceFactory = new RevocationServiceFactory();

        String revocationAuthorityParametersUid = "urn:revocationauthorityparameters:test:foobar";
        List<Attribute> attributes = new ArrayList<Attribute>(1);
        
        Attribute attribute1 = of.createAttribute();
        
        attribute1.setAttributeValue(BigInteger.ONE);
    	attribute1.setAttributeUID(new URI("urn:abc4trust:1.0:attribute/NOT_USED"));
    	AttributeDescription attrDesc = of.createAttributeDescription();
    	attrDesc.setEncoding(new URI("urn:abc4trust:1.1:encodign:integer:unsigned"));
    	attrDesc.setDataType(new URI("xs:integer"));
    	attrDesc.setType(new URI("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"));
    	attribute1.setAttributeDescription(attrDesc);
    	
    	attributes.add(attribute1);
        
        NonRevocationEvidence nre = revocationServiceFactory
        		.generateNonRevocationEvidence(revocationAuthorityParametersUid, attributes);
        assertNotNull(nre);
    }
   
}