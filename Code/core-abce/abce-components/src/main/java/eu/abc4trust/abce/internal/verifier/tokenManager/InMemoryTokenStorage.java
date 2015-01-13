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

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PseudonymInToken;


/**
 * In memory storage for Presentation tokens.
 * 
 * @author Raphael Dobers
 */

public class InMemoryTokenStorage implements TokenStorage {

    private final Map<URI, byte[]> tokens;
    private final Map<String, String> pseudonyms;

    public InMemoryTokenStorage() {
        super();
        this.tokens = new HashMap<URI, byte[]>();
        this.pseudonyms = new HashMap<String, String>();
    }

    @Override
    public byte[] getToken(URI tokenuid) {
        return this.tokens.get(tokenuid);
    }

    @Override
    public void addToken(URI tokenuid, byte[] token) {
        this.tokens.put(tokenuid, token);
    }

    @Override
    public boolean checkForPseudonym(String primaryKey) {
        // Instead of iterating over all stored tokens, we use a seperate HashMap for storing pseudonyms from PresentationTokens
        String result = this.pseudonyms.get(primaryKey);
        if (result == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void addPseudonymPrimaryKey(String primaryKey) {
        this.pseudonyms.put(primaryKey, "");
    }

    @Override
    public boolean deleteToken(URI tokenuid) throws Exception {
        byte[] result = this.tokens.remove(tokenuid);
        if(result != null) {
            try {

                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(result);
                ObjectInput objectInput = new ObjectInputStream(byteArrayInputStream);
                PresentationToken tokenResult = (PresentationToken)objectInput.readObject();

                // Close the streams..
                objectInput.close();
                byteArrayInputStream.close();
                List<PseudonymInToken> pseudonyms = tokenResult.getPresentationTokenDescription().getPseudonym();

                for(PseudonymInToken p: pseudonyms) {
                    String primaryKey = DatatypeConverter.printBase64Binary(p.getPseudonymValue());
                    this.pseudonyms.remove(primaryKey);
                }


            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        else {
            return false;
        }
    }
}
