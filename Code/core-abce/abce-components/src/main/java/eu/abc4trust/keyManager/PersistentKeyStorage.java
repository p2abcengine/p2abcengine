//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
//* Rights Reserved.                                                  *
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
	public void addValueAndOverwrite(URI uri, byte[] key) throws IOException {
    	try {
			if(StorageUtil.HasUri(this.file, uri)){
				StorageUtil.deleteData(this.file, uri);
			}
			this.addValue(uri, key);
		} catch (Exception e) {
			throw new IOException(e);
		}
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
