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

package eu.abc4trust.abce.internal.issuer.credentialManager;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public interface CredentialStorage {

    List<URI> listIssuerSecretKeys() throws Exception;

    byte[] getIssuerSecretKey(URI issuerParamsUid) throws Exception;

    void addIssuerSecret(URI issuerParamsUid, byte[] bytes) throws IOException;

}
