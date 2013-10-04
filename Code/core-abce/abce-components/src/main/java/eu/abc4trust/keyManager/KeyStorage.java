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

package eu.abc4trust.keyManager;

import java.io.IOException;
import java.net.URI;

/**
 * Storage for cryptographic keys and parameters.
 * 
 * @author Janus Dam Nielsen
 */
public interface KeyStorage {

    URI[] listUris() throws Exception;

    byte[] getValue(URI uri) throws Exception;

    void addValue(URI uri, byte[] key) throws IOException;
    
    void addValueAndOverwrite(URI uri, byte[] key) throws IOException;
}
