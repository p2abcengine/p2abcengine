//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.cryptoEngine.inspector;

import java.net.URI;
import java.util.List;

import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.PresentationToken;

public class MockCryptoEngineInspector implements CryptoEngineInspector {

  @Override
  public InspectorPublicKey setupInspectorPublicKey(int keylength, URI mechanism, URI uid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Attribute> inspect(PresentationToken t) {
    // TODO Auto-generated method stub
    return null;
  }

}
