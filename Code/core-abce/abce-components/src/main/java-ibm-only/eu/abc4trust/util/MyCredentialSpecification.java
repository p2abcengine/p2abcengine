//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

package eu.abc4trust.util;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.abc4trust.util.attributeEncoding.MyAttributeEncodingFactory;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;

public class MyCredentialSpecification {
  private final CredentialSpecification credSpec;
  private final Map<URI, AttributeDescription> attributeDescriptions;
  public static final String REVOCATION_HANDLE = "http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle";

  public MyCredentialSpecification(CredentialSpecification credSpec) {
    this.credSpec = credSpec;

    attributeDescriptions = new HashMap<URI, AttributeDescription>();
    for (AttributeDescription ad : credSpec.getAttributeDescriptions().getAttributeDescription()) {
      attributeDescriptions.put(ad.getType(), ad);
    }
    
    if(credSpec.isRevocable()) {
      if (! attributeDescriptions.containsKey(URI.create(REVOCATION_HANDLE))) {
        throw new RuntimeException("Credential is revocable, yet it does not contain a revocation handle.");
      }
    }
  }

  public URI getSpecificationUid() {
    return credSpec.getSpecificationUID();
  }

  public AttributeDescription getAttributeDescriptionFromType(URI attributeType) {
    return attributeDescriptions.get(attributeType);
  }

  public long getAttributeBitLength() {
    return credSpec.getAttributeDescriptions().getMaxLength();
  }

  /**
   * Check if the current credential description matches the given credential specification. The
   * order of attributes in the credential does not matter.
   * 
   * @param credSpec The credential specification to validate against
   */
  public void validateOrThrow(CredentialDescription credDesc) {
    Set<URI> seenAttributeTypes = new HashSet<URI>();
    for (Attribute ad : credDesc.getAttribute()) {
      URI attributeType = ad.getAttributeDescription().getType();
      if (!seenAttributeTypes.add(attributeType)) {
        String errorMessage = "Duplicate attribute type " + attributeType + ".";
        throw new RuntimeException(errorMessage);
      }
    }

    if (credDesc.getAttribute().size() != attributeDescriptions.size()) {
      StringBuilder errorMessage = new StringBuilder();
      errorMessage.append("Created Credential size does not match spec. ");
      errorMessage.append("Actual size: " + credDesc.getAttribute().size() + ".");
      errorMessage.append("Expected size:" + attributeDescriptions.size() + ".");
      errorMessage.append("Expected types: ");
      for (URI key : attributeDescriptions.keySet()) {
        errorMessage.append(key + ",");
      }
      errorMessage.append("  Actual types: ");
      for (Attribute ad : credDesc.getAttribute()) {
        errorMessage.append(ad.getAttributeDescription().getType() + ",");
      }
      throw new RuntimeException(errorMessage.toString());
    }

    for (Attribute ad : credDesc.getAttribute()) {
      URI attributeType = ad.getAttributeDescription().getType();
      AttributeDescription expectedAttDesc = attributeDescriptions.get(attributeType);

      if (expectedAttDesc == null) {
        String errorMessage = "Unknown attribute type: " + attributeType;
        throw new RuntimeException(errorMessage);
      }

      URI expectedDataType = expectedAttDesc.getDataType();
      URI actualDataType = ad.getAttributeDescription().getDataType();
      if (!expectedDataType.equals(actualDataType)) {
        String errorMessage =
            "Wrong data type in " + attributeType + ". Expected " + expectedDataType + " actual "
                + actualDataType;
        throw new RuntimeException(errorMessage);
      }

      URI expectedEncoding = expectedAttDesc.getEncoding();
      URI actualEncoding = ad.getAttributeDescription().getEncoding();
      if (!expectedEncoding.equals(actualEncoding)) {
        String errorMessage =
            "Wrong encoding in " + attributeType + ". Expected " + expectedEncoding + " actual "
                + actualEncoding;
        throw new RuntimeException(errorMessage);
      }
      
      URI compatibleDatatype = MyAttributeEncodingFactory.getDatatypeFromEncoding(actualEncoding);
      if (!compatibleDatatype.equals(actualDataType)) {
        String errorMessage =
            "Incompatible dataType in " + attributeType
                + " for encoding " + actualEncoding
                + ". Expected " + compatibleDatatype
                + " actual " + actualDataType;
        throw new RuntimeException(errorMessage);
      }
    }
  }
  
  public List<FriendlyDescription> getFriendlyDescryptionsForAttributeType(URI attributeType){
	  AttributeDescription attrDescription = getAttributeDescriptionFromType(attributeType);
	  if (attrDescription!=null){
		  return attrDescription.getFriendlyAttributeName();	
	  } else throw new RuntimeException("No attribute description for attribute type: " + attributeType.toString());	
  }
}
