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

package eu.abc4trust.abce.internal.verifier.tokenManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import com.google.inject.Inject;

import eu.abc4trust.abce.internal.verifier.tokenManagerVerifier.TokenManagerVerifier;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PseudonymInToken;

public class TokenManagerImpl implements TokenManagerVerifier {

    private final TokenStorage storage;

    @Inject
    public TokenManagerImpl(TokenStorage storage) {
        this.storage = storage;
    }

    @Override
    public PresentationToken getToken(URI tokenuid) {

        PresentationToken tokenResult = null;

        try {
            byte[] tokenBytes = this.storage.getToken(tokenuid);
            if(tokenBytes != null) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(tokenBytes);
                ObjectInput objectInput = new ObjectInputStream(byteArrayInputStream);
                tokenResult = (PresentationToken)objectInput.readObject();

                // Close the streams..
                objectInput.close();
                byteArrayInputStream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return tokenResult;
    }

    @Override
    public boolean isEstablishedPseudonym(PseudonymInToken p) {
        String primaryKey = DatatypeConverter.printBase64Binary(p.getPseudonymValue());
        try {
            return this.storage.checkForPseudonym(primaryKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public URI storeToken(PresentationToken t) {
        URI tokenuid = t.getPresentationTokenDescription().getTokenUID();

        // Check if a pseudonym is present in the PresentationToken
        List<PseudonymInToken> pseudonyms = t.getPresentationTokenDescription().getPseudonym();

        try {
            t.getPresentationTokenDescription().setTokenUID(tokenuid);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutput objectOutput = new ObjectOutputStream(byteArrayOutputStream);
            objectOutput.writeObject(t);
            byte[] tokenBytes = byteArrayOutputStream.toByteArray();
            this.storage.addToken(tokenuid, tokenBytes);

            // Close the streams..
            objectOutput.close();
            byteArrayOutputStream.close();

            // For faster lookup in isEstablishedPseudonym(): Store the pseudonym value lexical representation of xsd:base64Binary if a pseudonym was present.
            for(PseudonymInToken p: pseudonyms) {
                String primaryKey = DatatypeConverter.printBase64Binary(p.getPseudonymValue());
                this.storage.addPseudonymPrimaryKey(primaryKey);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return tokenuid;
    }

    @Override
    public boolean deleteToken(URI tokenuid) {
        try {
            return this.storage.deleteToken(tokenuid);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
