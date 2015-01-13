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
package eu.abc4trust.abce.internal.inspector.credentialManager;

import java.net.URI;
import java.util.List;

import com.google.inject.Inject;

import eu.abc4trust.db.PersistentStorage;
import eu.abc4trust.db.SimpleParamTypes;
import eu.abc4trust.util.ByteSerializer;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.util.XmlUtils;

public class NewInspectorKeyManager implements CredentialManager {

  private final PersistentStorage ps;

  @Inject
  public NewInspectorKeyManager(PersistentStorage ps) {
    this.ps = ps;
  }

  @Override
  public List<URI> listInspectorSecretKeys() throws CredentialManagerException {
    return ps.listItems(SimpleParamTypes.INSPECTOR_SECRET_KEY);
  }

  @Override
  public SecretKey getInspectorSecretKey(URI inspectorKeyUID) throws CredentialManagerException {
    final SecretKey ret = (SecretKey) ByteSerializer.readFromBytes(ps.getItem(
        SimpleParamTypes.INSPECTOR_SECRET_KEY, inspectorKeyUID));
    if (ret != null && ret.getCryptoParams() != null) {
    	XmlUtils.fixNestedContent(ret.getCryptoParams());
    }
	return ret;
  }

  @Override
  public void storeInspectorSecretKey(URI inspectorKeyUID, SecretKey inspectorSecretKey)
      throws CredentialManagerException {
    ps.replaceItem(SimpleParamTypes.INSPECTOR_SECRET_KEY, inspectorKeyUID,
        ByteSerializer.writeAsBytes(inspectorSecretKey));
  }

}
