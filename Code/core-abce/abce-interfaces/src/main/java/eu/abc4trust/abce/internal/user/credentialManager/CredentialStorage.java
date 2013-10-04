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

package eu.abc4trust.abce.internal.user.credentialManager;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public interface CredentialStorage {

    byte[] getCredential(URI creduid) throws Exception;

    byte[] getPseudonymWithData(URI pseudonymUri) throws Exception;

    List<URI> listCredentials() throws Exception;

    void addCredential(URI credUid, byte[] credBytes) throws IOException;

    void addPseudonymWithMetadata(URI pseudonymUri, byte[] pwmBytes)
            throws IOException;

    void deletePseudonymWithMetadata(URI pseudonymUri) throws Exception;

    void deleteCredential(URI credUid) throws Exception;

    List<byte[]> listPseudonyms() throws Exception;

}
