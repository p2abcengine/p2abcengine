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

package eu.abc4trust.abce.internal.verifier.tokenManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.URI;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.abc4trust.util.StorageUtil;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PseudonymInToken;

/**
 * Persistent file storage for Presentation tokens.
 * 
 * @author Raphael Dobers
 */

public class PersistentFileTokenStorage implements TokenStorage {

    private final File tokensFile;
    private final File pseudonymsFile;

    @Inject
    public PersistentFileTokenStorage(@Named("TokenStorageFile") File tokensFile, @Named("PseudonymsStorageFile") File pseudonymsFile) {
        super();
        this.tokensFile = tokensFile;
        this.pseudonymsFile = pseudonymsFile;
    }

    @Override
    public boolean checkForPseudonym(String primaryKey) throws IOException {

        FileInputStream fis = new FileInputStream(this.pseudonymsFile);
        long fileSize = this.pseudonymsFile.length();
        if (fileSize < 4) {
        	fis.close();
            return false;
        }
        DataInputStream in = new DataInputStream(new BufferedInputStream(fis));

        try {
            while (true) {
                String primaryKeyTemp = in.readUTF(); // readUTF itself will take care of separating the single keys...
                if(primaryKeyTemp.equals(primaryKey)) {
                	fis.close();
                	in.close();
                    return true;
                }
            }
        } catch (EOFException ex) {
        	fis.close();
        	in.close();
            return false;
        }
    }

    @Override
    public void addPseudonymPrimaryKey(String primaryKey) throws IOException {
        FileOutputStream fos = new FileOutputStream(this.pseudonymsFile, true);
        DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(fos));

        out.writeUTF(primaryKey); // writeUTF itself will take care of separating the single keys...
        out.flush();
        out.close();
    }

    @Override
    public byte[] getToken(URI tokenuid) throws Exception {
        return StorageUtil.getData(this.tokensFile, tokenuid);
    }

    @Override
    public void addToken(URI tokenuid, byte[] token) throws IOException {
        StorageUtil.appendData(this.tokensFile, tokenuid, token);
    }

	@Override
	public boolean deleteToken(URI tokenuid) throws Exception {
		
		// first remove stored pseudonyms for this token
		
		byte[] result = getToken(tokenuid);
		if (result == null) return false;
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(result);
		ObjectInput objectInput = new ObjectInputStream(byteArrayInputStream);
		PresentationToken tokenResult = (PresentationToken)objectInput.readObject();

		// Close the streams..
		objectInput.close();
		byteArrayInputStream.close();
		
		List<PseudonymInToken> pseudonyms = tokenResult.getPresentationTokenDescription().getPseudonym();
		
		for(PseudonymInToken p: pseudonyms) {
			String primaryKey = DatatypeConverter.printBase64Binary(p.getPseudonymValue());
			StorageUtil.deleteData(this.pseudonymsFile, primaryKey);
		}
		
		// Delete the PresentationToken
		StorageUtil.deleteData(this.tokensFile, tokenuid);
		
		return true;
	}

}
