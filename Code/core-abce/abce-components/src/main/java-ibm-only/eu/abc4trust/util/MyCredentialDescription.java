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
import java.util.List;
import java.util.Map;

import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.util.attributeTypes.EnumAllowedValues;
import eu.abc4trust.util.attributeTypes.MyAttributeValue;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.AttributeInPolicy;
import eu.abc4trust.xml.AttributeInToken;
import eu.abc4trust.xml.CarriedOverAttribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialInPolicy;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.JointlyRandomAttribute;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.UnknownAttributes;

/**
 * A class providing better methods that the JAXB CredentialDescription class.
 * 
 * @author enr
 * 
 */
public class MyCredentialDescription {

  private final CredentialDescription credentialDesc;
  private final CredentialSpecification credSpec;
  private final IssuerParameters issuerParameters;
  private final RevocationAuthorityParameters revAuthParam;
  
  public CredentialSpecification getCredSpec() {
    return credSpec;
  }


  private final Map<URI, EnumAllowedValues> allowedValuesPerAttribute;
  
  public CredentialDescription getCredentialDesc() {
    return credentialDesc;
  }
  
  public IssuerParameters getIssuerParameters() {
    return issuerParameters;
  }
  
  public RevocationAuthorityParameters getRevocationAuthorityParameters() {
    return revAuthParam;
  }


  private final Map<URI, MyAttribute> attributesByType;

  public MyCredentialDescription(CredentialDescription credential, CredentialSpecification credSpec, IssuerParameters ip, RevocationAuthorityParameters rap) {
    this.credentialDesc = credential;
    this.credSpec = credSpec;
    this.issuerParameters = ip;
    if(credSpec.isRevocable()) {
      this.revAuthParam = rap;
    } else {
      this.revAuthParam = null;
    }
    attributesByType = new HashMap<URI, MyAttribute>();
    allowedValuesPerAttribute = new HashMap<URI, EnumAllowedValues>();
    makeAllowedValuesMap();

    addAttributes(credential.getAttribute(), false);
  }
  
  public MyCredentialDescription(CredentialDescription credDesc, CredentialSpecification credSpec, IssuerParameters ip, KeyManager km) throws KeyManagerException {
    this(credDesc, credSpec, ip, (ip.getRevocationParametersUID()==null||!credSpec.isRevocable())?null:km.getRevocationAuthorityParameters(ip.getRevocationParametersUID()));
  }
   
  public MyCredentialDescription(CredentialDescription credDesc, CredentialSpecification credSpec, KeyManager km) throws KeyManagerException {
    this(credDesc, credSpec, km.getIssuerParameters(credDesc.getIssuerParametersUID()), km);
  }
  
  public MyCredentialDescription(CredentialDescription credDesc, KeyManager km) throws KeyManagerException {
    this(credDesc, km.getCredentialSpecification(credDesc.getCredentialSpecificationUID()), km);
  }

  private void makeAllowedValuesMap() {
    for(AttributeDescription ad: credSpec.getAttributeDescriptions().getAttributeDescription()) {
      URI type = ad.getType();
      EnumAllowedValues eav = new EnumAllowedValues(ad);
      allowedValuesPerAttribute.put(type, eav);
    }
  }

  public boolean hasAttributeType(URI attributeType) {
    MyAttribute a = attributesByType.get(attributeType);
    return (a != null);
  }

  public URI getSecretReference() {
    return credentialDesc.getSecretReference();
  }

  public MyAttributeValue getAttributeValue(URI attributeType) {
    MyAttribute attribute = attributesByType.get(attributeType);
    if (attribute != null) {
      return attribute.getValue();
    } else {
      return null;
    }
  }
  
  public MyAttribute getAttribute(URI attributeType) {
    return attributesByType.get(attributeType);
  }
  
  public Object getAttributeValueAsObject(URI attributeType) {
    MyAttribute attribute = attributesByType.get(attributeType);
    if (attribute != null) {
      return attribute.getAttributeValue();
    } else {
      return null;
    }
  }

  public URI getUid() {
    return credentialDesc.getCredentialUID();
  }

  public CredentialDescription getCredentialDescription() {
    return credentialDesc;
  }

  public void populateDisclosedAttributes(CredentialInToken c,
      CredentialInPolicy credInPolicy) {
    ObjectFactory of = new ObjectFactory();

    for (AttributeInPolicy attInPolicy : credInPolicy.getDisclosedAttribute()) {
      AttributeInToken attInPt = of.createAttributeInToken();
      c.getDisclosedAttribute().add(attInPt);
      
      attInPt.setAttributeType(attInPolicy.getAttributeType());
      attInPt.setDataHandlingPolicy(attInPolicy.getDataHandlingPolicy());
      
      if (attInPolicy.getInspectorAlternatives() != null) {
        // Inspector public key uid will be set after the UI step
        attInPt.setInspectorPublicKeyUID(URI.create(""));
        attInPt.setInspectionGrounds(attInPolicy.getInspectionGrounds());
        attInPt.setAttributeValue(null);
      } else {
        // If no inspector: reveal attribute
        attInPt.setInspectorPublicKeyUID(null);
        attInPt.setInspectionGrounds(null);
        attInPt.setAttributeValue(attributesByType.get(attInPolicy.getAttributeType())
          .getAttributeValue());
      }
    }
  }

  public void addAttributes(List<Attribute> list, boolean alsoToCredDesc) {
    for(Attribute a: list) {
      addAttribute(a, alsoToCredDesc);
    } 
  }
  

  public void addAttribute(Attribute a, boolean alsoToCredDesc) {
    addAttribute(new MyAttribute(a), alsoToCredDesc);
  }
  
  public void addAttribute(MyAttribute a, boolean alsoToCredDesc) {
    URI attributeType = a.getType();
    attributesByType.put(attributeType, a);
    if(alsoToCredDesc) {
      credentialDesc.getAttribute().add(a.getXmlAttribute());
    }
  }

  public void populateFromTemplate(CredentialTemplate credTemplate,
                                MyCredentialSpecification credSpec,
                                Map<URI, Credential> credentialsFromAlias,
                                ContextGenerator contextGen,
                                KeyManager km) {
    ObjectFactory of = new ObjectFactory();
    
    if(!credSpec.getSpecificationUid().equals(credTemplate.getCredentialSpecUID())) {
      throw new RuntimeException("Incompatible Credential Specification");
    }
    
    URI credSpecUri = credTemplate.getCredentialSpecUID();
    credentialDesc.setCredentialSpecificationUID(credSpecUri);
    credentialDesc.setIssuerParametersUID(credTemplate.getIssuerParametersUID());
    credentialDesc.setCredentialUID(null);
    credentialDesc.setSecretReference(null);
    
    UnknownAttributes unknownAtts = credTemplate.getUnknownAttributes();
    for(CarriedOverAttribute coa: unknownAtts.getCarriedOverAttribute()) {
      Attribute newAtt = of.createAttribute();
      AttributeDescription specAttDesc = credSpec.getAttributeDescriptionFromType(coa.getTargetAttributeType());
      newAtt.setAttributeDescription(specAttDesc);
      newAtt.setAttributeUID(contextGen.getUniqueContext(coa.getTargetAttributeType()));
      
      if (coa.getSourceCredentialInfo() != null) {
        URI credAlias = coa.getSourceCredentialInfo().getAlias();
        URI attributeType = coa.getSourceCredentialInfo().getAttributeType();
        CredentialDescription c = credentialsFromAlias.get(credAlias).getCredentialDescription();
        try {
          MyCredentialDescription myc = new MyCredentialDescription(c, km);
          newAtt.setAttributeValue(myc.getAttributeValueAsObject(attributeType));
        } catch(Exception e) {
          throw new RuntimeException(e);
        }
      } else {
        String errorMessage = "Cannot generate value for carriedOverAttribute: no source" +
                              "credential info, and jointly random not set";
        throw new RuntimeException(errorMessage);
      }
      addAttribute(newAtt, true);
    }
    for(JointlyRandomAttribute jra: unknownAtts.getJointlyRandomAttribute()) {
      Attribute newAtt = of.createAttribute();
      AttributeDescription specAttDesc = credSpec.getAttributeDescriptionFromType(jra.getTargetAttributeType());
      newAtt.setAttributeDescription(specAttDesc);
      newAtt.setAttributeUID(contextGen.getUniqueContext(jra.getTargetAttributeType()));
      URI coaDataType = credSpec.getAttributeDescriptionFromType(jra.getTargetAttributeType()).getDataType();
      if(coaDataType.toString().equals("xs:integer")) {
        // TODO(enr): Random number is not random over the whole domain.
        newAtt.setAttributeValue(contextGen.getRandomNumber(credSpec.getAttributeBitLength()));
      } else {
        String errorMessage = "Cannot generate random element of type " + coaDataType + " only xs:integer supported";
        throw new RuntimeException(errorMessage);
      }
      addAttribute(newAtt, true);
    }    
  }

}
