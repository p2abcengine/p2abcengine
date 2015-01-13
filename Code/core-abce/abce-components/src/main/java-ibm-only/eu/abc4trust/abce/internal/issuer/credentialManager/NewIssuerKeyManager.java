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
package eu.abc4trust.abce.internal.issuer.credentialManager;

import java.net.URI;
import java.util.List;

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.db.PersistentStorage;
import eu.abc4trust.db.SimpleParamTypes;
import eu.abc4trust.util.ByteSerializer;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.util.XmlUtils;

public class NewIssuerKeyManager implements CredentialManager {

  private final PersistentStorage ps;

  @Inject
  public NewIssuerKeyManager(PersistentStorage ps) {
    this.ps = ps;
  }

  @Override
  public List<URI> listIssuerSecretKeys() throws CredentialManagerException {
    return ps.listItems(SimpleParamTypes.ISSUER_SECRET_KEY);
  }

  @Override
  public SecretKey getIssuerSecretKey(URI issuerParamsUid) throws CredentialManagerException {
    final SecretKey ret = (SecretKey) ByteSerializer.readFromBytes(ps.getItem(SimpleParamTypes.ISSUER_SECRET_KEY,
        issuerParamsUid));
    if (ret != null && ret.getCryptoParams() != null) {
    	XmlUtils.fixNestedContent(ret.getCryptoParams());
    }
	return ret;
  }

  @Override
  public void storeIssuerSecretKey(URI issuerParamsUid, SecretKey issuerSecretKey)
      throws CredentialManagerException {
    ps.replaceItem(SimpleParamTypes.ISSUER_SECRET_KEY, issuerParamsUid,
        ByteSerializer.writeAsBytes(issuerSecretKey));
  }

}
