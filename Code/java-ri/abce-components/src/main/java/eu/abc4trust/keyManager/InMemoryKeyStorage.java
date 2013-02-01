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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class InMemoryKeyStorage implements KeyStorage {

    private final Map<URI, byte[]> keys;

    public InMemoryKeyStorage() {
        super();
        this.keys = new HashMap<URI, byte[]>();
    }

    @Override
    public byte[] getValue(URI uri) {
        return this.keys.get(uri);
    }

    @Override
    public void addValue(URI uri, byte[] key) {
        this.keys.put(uri, key);
    }

    @Override
    public URI[] listUris() {
        return this.keys.keySet().toArray(new URI[0]);
    }

}
