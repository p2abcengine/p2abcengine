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

package eu.abc4trust.ui.idSelection;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.abc4trust.returnTypes.ObjectFactoryReturnTypes;
import eu.abc4trust.returnTypes.SitdArguments;
import eu.abc4trust.returnTypes.SitdReturn;
import eu.abc4trust.returnTypes.SptdArguments;
import eu.abc4trust.returnTypes.SptdReturn;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.InspectorDescription;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PolicyDescription;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymDescription;
import eu.abc4trust.xml.util.XmlUtils;

public class IdentitySelectionPrinter implements IdentitySelection {

  private final IdentitySelection is;
  private static final String PATH = "target/outputXml/";
  
  @Inject
  public IdentitySelectionPrinter(@Named("RealIdSelector") IdentitySelection is) {
    this.is = is;
  }

  @Override
  public SptdReturn selectPresentationTokenDescription(Map<URI, PolicyDescription> policies,
      Map<URI, CredentialDescription> credentialDescriptions,
      Map<URI, PseudonymDescription> pseudonyms, Map<URI, InspectorDescription> inspectors,
      List<PresentationTokenDescription> tokens, List<List<URI>> credentialUids,
      List<Set<List<URI>>> pseudonymChoice, List<List<Set<URI>>> inspectorChoice) {
    
    BigInteger r = new BigInteger(30, new SecureRandom());
    String filename1 = PATH + "ids-p-" + r + "-q";
    String filename2 = PATH + "ids-p-" + r + "-r";
    
    SptdArguments args =
      new SptdArguments(policies, credentialDescriptions, pseudonyms, inspectors, tokens,
        credentialUids, pseudonymChoice, inspectorChoice);
    
    try {
      (new File(PATH)).mkdir();
      String xml = XmlUtils.toXml(ObjectFactoryReturnTypes.wrap(args), false);
      PrintWriter out = new PrintWriter(filename1);
      out.println(xml);
      out.close();
      System.out.println("Stored id selector call  at " + filename1);
    } catch(Exception e) {
      System.err.println("Could not stored id selector call  at " + filename1);
    }  
    
    SptdReturn ret = 
     is.selectPresentationTokenDescription(
      policies, credentialDescriptions, pseudonyms, inspectors, tokens,
        credentialUids, pseudonymChoice, inspectorChoice);
    
    try {
      String xml = XmlUtils.toXml(ObjectFactoryReturnTypes.wrap(ret), false);
      PrintWriter out = new PrintWriter(filename2);
      out.println(xml);
      out.close();
      System.out.println("Stored id selector call  at " + filename2);
    } catch(Exception e) {
      System.err.println("Could not stored id selector call  at " + filename2);
    }
    
    return ret;
  }

  @Override
  public SitdReturn selectIssuanceTokenDescription(Map<URI, PolicyDescription> policies,
      Map<URI, CredentialDescription> credentialDescriptions,
      Map<URI, PseudonymDescription> pseudonyms, Map<URI, InspectorDescription> inspectors,
      List<IssuanceTokenDescription> tokens, List<List<URI>> credentialUids,
      List<Attribute> selfClaimedAttributes, List<Set<List<URI>>> pseudonymChoice,
      List<List<Set<URI>>> inspectorChoice) {
    
    BigInteger r = new BigInteger(30, new SecureRandom());
    String filename1 = PATH + "ids-i-" + r + "-q";
    String filename2 = PATH + "ids-i-" + r + "-r";
    
    SitdArguments args = new SitdArguments(policies, credentialDescriptions,
      pseudonyms, inspectors, tokens, credentialUids, selfClaimedAttributes,
      pseudonymChoice, inspectorChoice);
    
    try {
      (new File(PATH)).mkdir();
      String xml = XmlUtils.toXml(ObjectFactoryReturnTypes.wrap(args), false);
      PrintWriter out = new PrintWriter(filename1);
      out.println(xml);
      out.close();
      System.out.println("Stored id selector call  at " + filename1);
    } catch(Exception e) {
      System.err.println("Could not stored id selector call  at " + filename1);
    }
    
    SitdReturn ret = is.selectIssuanceTokenDescription(policies, credentialDescriptions,
      pseudonyms, inspectors, tokens, credentialUids, selfClaimedAttributes,
      pseudonymChoice, inspectorChoice);
    
    try {
      String xml = XmlUtils.toXml(ObjectFactoryReturnTypes.wrap(ret), false);
      PrintWriter out = new PrintWriter(filename2);
      out.println(xml);
      out.close();
      System.out.println("Stored id selector call  at " + filename2);
    } catch(Exception e) {
      System.err.println("Could not stored id selector call  at " + filename2);
    }
    
    return ret;
  }

}
