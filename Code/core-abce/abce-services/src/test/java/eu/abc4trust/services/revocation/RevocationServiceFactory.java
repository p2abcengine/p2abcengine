//* Licensed Materials - Property of                                  *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
//* Rights Reserved.                                                  *
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

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.sun.jersey.api.client.WebResource.Builder;

import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.services.AbstractTestFactory;
import eu.abc4trust.xml.ABCEBoolean;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.AttributeList;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.NonRevocationEvidenceUpdate;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.RevocationReferences;
import eu.abc4trust.xml.SystemParameters;

public class RevocationServiceFactory extends AbstractTestFactory {

    static ObjectFactory of = new ObjectFactory();

    final String baseUrl = "http://localhost:9200/abce-services/revocation";

    public void storeSystemParameters(SystemParameters systemParameters) {
        String requestString = "/storeSystemParameters/";
        Builder resource = this.getHttpBuilder(requestString, this.baseUrl);

        ABCEBoolean b = resource.post(ABCEBoolean.class,
                of.createSystemParameters(systemParameters));
        assertTrue(b.isValue());
    }

    public RevocationAuthorityParameters getRevocationAuthorityParameters(
    		String revocationAuthorityParametersUid) throws Exception{

    	SystemParameters systemParameters = SystemParametersUtil.getDefaultSystemParameters_1024();
    	this.storeSystemParameters(systemParameters);
    	
    	String requestString = "/setupRevocationAuthorityParameters?keyLength=1024&uid="+revocationAuthorityParametersUid;

    	Builder resource = this.getHttpBuilder(requestString, this.baseUrl);

    	RevocationReferences revocationReferences = of.createRevocationReferences();
    	Reference nonRevocationEvidenceReference = of.createReference();
    	nonRevocationEvidenceReference.getReferences().add(URI.create("http://localhost:9200/something-nre"));
    	nonRevocationEvidenceReference.setReferenceType(URI.create("http"));
    	Reference nonRevocationEvidenceUpdateReference = of.createReference();
    	nonRevocationEvidenceUpdateReference.getReferences().add(URI.create("http://localhost:9200/something-update"));
    	nonRevocationEvidenceUpdateReference.setReferenceType(URI.create("http"));
    	Reference revocationInfoReference = of.createReference();
    	revocationInfoReference.getReferences().add(URI.create("http://localhost:9200/something-rev-info"));
    	revocationInfoReference.setReferenceType(URI.create("http"));
    	revocationReferences.setNonRevocationEvidenceReference(nonRevocationEvidenceReference);
    	revocationReferences.setNonRevocationEvidenceUpdateReference(nonRevocationEvidenceUpdateReference);
    	revocationReferences.setRevocationInfoReference(revocationInfoReference);


    	RevocationAuthorityParameters revocationAuthorityParameters = resource
    			.post(
    					RevocationAuthorityParameters.class, of.createRevocationReferences(revocationReferences));

    	return revocationAuthorityParameters;
    }

	public NonRevocationEvidence generateNonRevocationEvidence(
			String revocationAuthorityParametersUid, List<Attribute> attributes) throws Exception {

		setupRevocationAuthority(revocationAuthorityParametersUid);

		String requestString = "/generatenonrevocationevidence/"+revocationAuthorityParametersUid;

		AttributeList attributeList = of.createAttributeList();
		attributeList.getAttributes().addAll(attributes);
		
    	Builder resource = this.getHttpBuilder(requestString, this.baseUrl);
    	return resource.post(
    					NonRevocationEvidence.class, of.createAttributeList(attributeList));
	}

	public NonRevocationEvidenceUpdate generateNonRevocationEvidenceUpdate(
			String revocationAuthorityParametersUid, int epoch) throws Exception {
 
		setupRevocationAuthority(revocationAuthorityParametersUid);

		String requestString = "/generatenonrevocationevidenceupdate/"+revocationAuthorityParametersUid+"?epoch="+epoch;

    	Builder resource = this.getHttpBuilder(requestString, this.baseUrl);
    	return resource.post(
    					NonRevocationEvidenceUpdate.class);
	}

	public RevocationInformation getRevocationInformation(
			String revocationAuthorityParametersUid) throws Exception {
		setupRevocationAuthority(revocationAuthorityParametersUid);

		String requestString = "/getrevocationinformation/"+revocationAuthorityParametersUid;

    	Builder resource = this.getHttpBuilder(requestString, this.baseUrl);
    	return resource.post(
    					RevocationInformation.class);
	}

	public RevocationInformation updateRevocationInformation(
			String revocationAuthorityParametersUid) throws Exception {
		setupRevocationAuthority(revocationAuthorityParametersUid);

		String requestString = "/updaterevocationinformation/"+revocationAuthorityParametersUid;

    	Builder resource = this.getHttpBuilder(requestString, this.baseUrl);
    	return resource.get(
    					RevocationInformation.class);

	}
	
	public RevocationInformation revoke(
			String revocationAuthorityParametersUid) throws Exception {
		
        List<Attribute> attributes = new ArrayList<Attribute>(1);
        
        Attribute attribute1 = of.createAttribute();
        
    	attribute1.setAttributeUID(new URI("urn:abc4trust:1.0:attribute/NOT_USED"));
    	AttributeDescription attrDesc = of.createAttributeDescription();
    	attrDesc.setEncoding(new URI("urn:abc4trust:1.0:encoding:integer:unsigned"));
    	attrDesc.setDataType(new URI("xs:integer"));
    	attrDesc.setType(new URI("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"));
    	attribute1.setAttributeDescription(attrDesc);
    	attribute1.setAttributeValue(new BigInteger("2222"));
    	attributes.add(attribute1);
		
		AttributeList attributeList = of.createAttributeList();
		attributeList.getAttributes().addAll(attributes);
		
		NonRevocationEvidence nre = generateNonRevocationEvidence(revocationAuthorityParametersUid, attributes);
		
		attributes = nre.getAttribute();
		attributeList = of.createAttributeList();
		attributeList.getAttributes().addAll(attributes);

		String requestString = "/revoke/"+revocationAuthorityParametersUid;

    	Builder resource = this.getHttpBuilder(requestString, this.baseUrl);
    	return resource.post(
    					RevocationInformation.class, of.createAttributeList(attributeList));
	}
	
	private void setupRevocationAuthority(String revocationAuthorityParametersUid) throws Exception{
		deleteStorageDirectory();
		getRevocationAuthorityParameters(revocationAuthorityParametersUid);
	}
}
