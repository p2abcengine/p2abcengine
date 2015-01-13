//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

// * Licensed Materials - Property of IBM, Miracle A/S, and *
// * Alexandra Instituttet A/S *
// * eu.abc4trust.pabce.1.14 *
// * (C) Copyright IBM Corp. 2012. All Rights Reserved. *
// * (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved. *
// * (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All *
// * Rights Reserved. *
// * US Government Users Restricted Rights - Use, duplication or *
// * disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
// * *
// * This file is licensed under the Apache License, Version 2.0 (the *
// * "License"); you may not use this file except in compliance with *
// * the License. You may obtain a copy of the License at: *
// * http://www.apache.org/licenses/LICENSE-2.0 *
// * Unless required by applicable law or agreed to in writing, *
// * software distributed under the License is distributed on an *
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY *
// * KIND, either express or implied. See the License for the *
// * specific language governing permissions and limitations *
// * under the License. *
// */**/****************************************************************

package eu.abc4trust.ri.servicehelper.issuer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.ObjectFactory;

/**
 * Helper class. Holds CredentialsSpecification + matching IssuancePolicy
 */
public class SpecAndPolicy {
  public final CryptoTechnology cryptoTechnology;
  public final int maximalNumberOfAttributes;

  public final String key;
  public final String specResource;
  public final String policyResource;

  public final String issuerParamsUid;
  public final String revocationParamsUid;
  public final int numberOfTokens;

  public URI issuerParamsUid_URI;
  public URI revocationParamsUid_URI;

  public final List<FriendlyDescription> friendlyDescriptions;

  public SpecAndPolicy(String key, CryptoTechnology cryptoTechnology,
      int maximalNumberOfAttributes, int numberOfTokens, String specResource, String policyResource) {
    this.key = key;
    this.cryptoTechnology = cryptoTechnology;
    this.maximalNumberOfAttributes = maximalNumberOfAttributes;
    this.specResource = specResource;
    this.policyResource = policyResource;
    this.issuerParamsUid = null;
    this.revocationParamsUid = null;
    this.friendlyDescriptions = null;
    this.numberOfTokens = numberOfTokens;
  }



  /**
   * @param key
   * @param specResource
   * @param policyResource
   * @param issuerParamsUid
   * @param revocationParamsUid
   * @param friendlyDescription : on the form <lang1> <desc1> <lang2> <desc2>
   */
  public SpecAndPolicy(String key, CryptoTechnology cryptoTechnology, String issuerParamsUid,
      int maximalNumberOfAttributes, int numberOfTokens, String specResource,
      String policyResource, String revocationParamsUid, String... friendlyDescription) {
    this.key = key;
    this.cryptoTechnology = cryptoTechnology;
    this.maximalNumberOfAttributes = maximalNumberOfAttributes;
    this.specResource = specResource;
    this.policyResource = policyResource;
    this.issuerParamsUid = issuerParamsUid;
    if (this.issuerParamsUid != null) {
      this.issuerParamsUid_URI = URI.create(issuerParamsUid);
    }
    this.numberOfTokens = numberOfTokens;

    this.revocationParamsUid = revocationParamsUid;
    this.friendlyDescriptions = new ArrayList<FriendlyDescription>();
    String locale = null;
    int i = 0;
    ObjectFactory of = new ObjectFactory();
    for (String s : friendlyDescription) {
      if (i % 2 == 1) {
        FriendlyDescription fd = of.createFriendlyDescription();
        fd.setLang(locale);
        fd.setValue(s);
        this.friendlyDescriptions.add(fd);
      } else {
        locale = s;
      }
      i++;
    }
  }

  public SpecAndPolicy(SpecAndPolicy cloneThisSap) throws Exception {
    this.key = new String(cloneThisSap.key);
    this.cryptoTechnology = cloneThisSap.cryptoTechnology;
    this.maximalNumberOfAttributes = cloneThisSap.maximalNumberOfAttributes;
    this.specResource = new String(cloneThisSap.specResource);
    this.policyResource = new String(cloneThisSap.policyResource);
    this.issuerParamsUid =
        cloneThisSap.issuerParamsUid != null ? new String(cloneThisSap.issuerParamsUid) : null;
    this.revocationParamsUid =
        cloneThisSap.revocationParamsUid != null
            ? new String(cloneThisSap.revocationParamsUid)
            : null;
    this.numberOfTokens = cloneThisSap.numberOfTokens;

    this.friendlyDescriptions = cloneJaxBList(cloneThisSap.friendlyDescriptions);

    this.issuerParamsUid_URI =
        cloneThisSap.issuerParamsUid_URI != null ? new URI(
            cloneThisSap.issuerParamsUid_URI.toString()) : null;
    this.revocationParamsUid_URI =
        cloneThisSap.revocationParamsUid_URI != null ? new URI(
            cloneThisSap.revocationParamsUid_URI.toString()) : null;

    this.credentialSpecification = cloneJaxB(cloneThisSap.credentialSpecification);
    this.issuancePolicy = cloneJaxB(cloneThisSap.issuancePolicy);
  }


  private CredentialSpecification credentialSpecification;
  private IssuancePolicy issuancePolicy;


  public CredentialSpecification getCredentialSpecification() {
    return this.credentialSpecification;
  }

  public void setCredentialSpecification(CredentialSpecification credentialSpecification) {
    this.credentialSpecification = credentialSpecification;
  }

  public IssuancePolicy getIssuancePolicy() {
    return this.issuancePolicy;
  }

  public void setIssuancePolicy(IssuancePolicy issuancePolicy) {
    this.issuancePolicy = issuancePolicy;
  }

  private <T extends Serializable> List<T> cloneJaxBList(List<T> original) throws Exception {
    if (original == null) {
      return null;
    }
    List<T> list = new ArrayList<T>();
    for (T t : original) {
      list.add(cloneJaxB(t));
    }
    return list;
  }

  public <T extends Serializable> T cloneJaxB(T jaxbObject) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ObjectOutputStream o = new ObjectOutputStream(out);
    o.writeObject(jaxbObject);
    o.flush();

    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    ObjectInputStream i = new ObjectInputStream(in);
    return (T) i.readObject();
  }

  public IssuancePolicy cloneIssuancePolicy() throws Exception {
    return cloneJaxB(issuancePolicy);
  }

}
