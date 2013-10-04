//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.abce.external.inspector;

import java.net.URI;
import java.util.List;

import com.google.inject.Inject;

import eu.abc4trust.cryptoEngine.inspector.CryptoEngineInspector;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.PresentationToken;

public class InspectorAbcEngineImpl implements InspectorAbcEngine {

  private final CryptoEngineInspector cryptoEngine;
  
  @Inject
  public InspectorAbcEngineImpl(CryptoEngineInspector cryptoEngine) {
    this.cryptoEngine = cryptoEngine;
  }
  
  @Override
  public List<Attribute> inspect(PresentationToken t) throws Exception {
	     return cryptoEngine.inspect(t);
  }

  @Override
  public InspectorPublicKey setupInspectorPublicKey(int keyLength, URI mechanism, URI uid) throws Exception {	     
	  return cryptoEngine.setupInspectorPublicKey(keyLength, mechanism, uid);   
  }


}
