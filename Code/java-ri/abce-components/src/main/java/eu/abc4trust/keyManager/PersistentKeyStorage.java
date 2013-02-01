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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.abc4trust.util.StorageUtil;

public class PersistentKeyStorage implements KeyStorage {

    private final File file;

    @Inject
    public PersistentKeyStorage(@Named("KeyStorageFile") File file) {
        super();
        this.file = file;
    }

    @Override
    public byte[] getValue(URI uri) throws Exception {
        return StorageUtil.getData(this.file, uri);
    }

    @Override
    public void addValue(URI uri, byte[] key) throws IOException {
        StorageUtil.appendData(this.file, uri, key);
    }

    @Override
    public URI[] listUris() throws Exception {
        FileInputStream fis = new FileInputStream(this.file);
        long fileSize = this.file.length();
        if (fileSize < 4) {
        	fis.close();
            return new URI[0];
        }
        DataInputStream in = new DataInputStream(new BufferedInputStream(fis));

        List<URI> ls = new ArrayList<URI>();
        try {
            while (true) {
                int n = in.readInt();
                byte[] uriBytes = new byte[n];
                URI storedUri = StorageUtil.readUriBytes(in, n, uriBytes);
                ls.add(storedUri);
                int sizeOfValue = in.readInt();
                StorageUtil.skip(in, sizeOfValue);
            }
        } catch (EOFException ex) {
            return ls.toArray(new URI[0]);
        }
    }

}
