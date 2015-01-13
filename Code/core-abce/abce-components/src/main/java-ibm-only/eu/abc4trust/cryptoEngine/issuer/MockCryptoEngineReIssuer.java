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

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.returnTypes.IssuerParametersAndSecretKey;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.SystemParameters;

public class MockCryptoEngineReIssuer implements CryptoEngineReIssuer {

  @Override
  public IssuanceMessageAndBoolean initReIssuanceProtocol(IssuancePolicy issuancePolicy, URI context)
      throws CryptoEngineException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IssuanceMessageAndBoolean reIssuanceProtocolStep(IssuanceMessage m)
      throws CryptoEngineException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SystemParameters setupSystemParameters(int keyLength, URI cryptographicMechanism) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IssuerParametersAndSecretKey setupIssuerParameters(CredentialSpecification credspec,
      SystemParameters syspars, URI uid, URI hash, URI revParsUid) {
    // TODO Auto-generated method stub
    return null;
  }

}
