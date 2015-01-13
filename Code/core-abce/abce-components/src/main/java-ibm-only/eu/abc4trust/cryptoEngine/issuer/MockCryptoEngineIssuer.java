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

package eu.abc4trust.cryptoEngine.issuer;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.returnTypes.IssuerParametersAndSecretKey;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuanceLogEntry;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.IssuanceTokenAndIssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.TestCryptoParams;
import eu.abc4trust.xml.util.XmlUtils;

public class MockCryptoEngineIssuer implements CryptoEngineIssuer {

  public static final String ABC4TRUST_EU_MOCKISSUER_PUBLICKEY = "abc4trust.eu/mockissuer/publickey";
  private final Map<URI, List<Attribute>> attributeCache;
  private final Map<URI, IssuancePolicy> issuancePolicyCache;

  @Inject
  public MockCryptoEngineIssuer() {
    this.attributeCache = new HashMap<URI, List<Attribute>>();
    this.issuancePolicyCache = new HashMap<URI, IssuancePolicy>();
    System.out.println("Hello from CryptoEngineIssuerImpl()");
  }

  @Override
  public IssuanceMessageAndBoolean initIssuanceProtocol(IssuancePolicy ip,
      List<Attribute> atts, URI ctxt) {
    attributeCache.put(ctxt, atts);
    issuancePolicyCache.put(ctxt, ip);
    
    ObjectFactory of = new ObjectFactory();
    IssuanceMessage ret = of.createIssuanceMessage();
    
    ret.getContent().add(of.createIssuancePolicy(ip));
    ret.setContext(ctxt);
    
    IssuanceMessageAndBoolean imab = new IssuanceMessageAndBoolean();
    imab.setLastMessage(false);
    imab.setIssuanceMessage(ret);
    return imab;
  }

  @Override
  public IssuanceMessageAndBoolean issuanceProtocolStep(IssuanceMessage m) {
    ObjectFactory of = new ObjectFactory();
    IssuanceMessage ret = of.createIssuanceMessage();
    //IssuanceProtocolMetadata counter = of.createIssuanceProtocolMetadata();
    //counter.setCounter(BigInteger.valueOf(2));
    //ret.getAny().add(of.createIssuanceProtocolMetadata(counter));
    
    CredentialDescription cd = of.createCredentialDescription();
    cd.setCredentialSpecificationUID(URI.create("no-spec-yet"));
    cd.setCredentialUID(URI.create("no-uid-yet"));
    cd.setIssuerParametersUID(URI.create(ABC4TRUST_EU_MOCKISSUER_PUBLICKEY));
    for (Attribute a: attributeCache.get(m.getContext())) {
      cd.getAttribute().add(a);
    }
    
    ret.getContent().add(of.createCredentialDescription(cd));
    ret.setContext(m.getContext());
    
    IssuanceMessageAndBoolean imab = new IssuanceMessageAndBoolean();
    imab.setLastMessage(true);
    imab.setIssuanceMessage(ret);
    return imab;   
  }
  
  @Override
  public IssuerParametersAndSecretKey setupIssuerParameters(SystemParameters syspars,
      int numberOfAttributes, URI technology, URI uid, URI revParsUid,
      List<FriendlyDescription> friendlyDescriptions) {
    ObjectFactory of = new ObjectFactory();
    IssuerParameters ip = of.createIssuerParameters();

    ip.setAlgorithmID(URI.create("Idemix/RSA"));
    ip.setMaximalNumberOfAttributes(numberOfAttributes);
    ip.setCryptoParams(getBogusCryptoEvidence());
    ip.setHashAlgorithm(URI.create("SHA-256"));
    ip.setParametersUID(uid);
    ip.setRevocationParametersUID(revParsUid);
    ip.setSystemParametersUID(syspars.getSystemParametersUID());
    ip.setVersion("1.0");

    IssuerParametersAndSecretKey ret = new IssuerParametersAndSecretKey();
    ret.issuerSecretKey = new SecretKey();
    ret.issuerParameters = ip;
    return ret;
  }

  @Override
  public SystemParameters setupSystemParameters(int keyLength) {
    ObjectFactory of = new ObjectFactory();
    SystemParameters ret = of.createSystemParameters();
    ret.setCryptoParams(of.createCryptoParams());
    ret.setVersion("1.0");
    return ret;
  }
  
  private CryptoParams getBogusCryptoEvidence() {
    ObjectFactory of = new ObjectFactory();
    CryptoParams cryptoEvidence = of.createCryptoParams();
    TestCryptoParams cryptoParams = of.createTestCryptoParams();
    cryptoParams.getData().add("I am MockCryptoEngineUser, and I approve of this message.");
    cryptoEvidence.getContent().add(of.createTestCryptoParams(cryptoParams));
    return cryptoEvidence;
  }

  @Override
  public IssuanceLogEntry getIssuanceLogEntry(URI issuanceEntryUid) throws CryptoEngineException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IssuanceTokenDescription extractIssuanceTokenDescription(IssuanceMessage issuanceMessage) {
    IssuanceToken it = (IssuanceToken) XmlUtils.unwrap(issuanceMessage.getContent().get(0), IssuanceToken.class);
    if(it == null) {
      return null;
    } else {
      return it.getIssuanceTokenDescription();
    }
  }

  @Override
  public IssuanceTokenAndIssuancePolicy extractIssuanceTokenAndPolicy(
      IssuanceMessage issuanceMessage) throws CryptoEngineException {
    IssuanceToken it = (IssuanceToken) XmlUtils.unwrap(issuanceMessage.getContent().get(0), IssuanceToken.class);
    if(it == null) {
      return null;
    }
    IssuanceTokenAndIssuancePolicy ret = new ObjectFactory().createIssuanceTokenAndIssuancePolicy();
    ret.setIssuanceToken(it);
    IssuancePolicy ip = issuancePolicyCache.get(issuanceMessage.getContext());
    ret.setIssuancePolicy(ip);
    return ret;
  }

}
