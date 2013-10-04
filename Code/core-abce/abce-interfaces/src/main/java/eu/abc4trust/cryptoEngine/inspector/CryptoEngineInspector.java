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

package eu.abc4trust.cryptoEngine.inspector;

import java.net.URI;
import java.util.List;

import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.PresentationToken;

public interface CryptoEngineInspector {
    /**
     * This method generates a fresh decryption key and corresponding encryption
     * key for the given key length and cryptographic mechanism. It stores the
     * decryption key in the trusted storage and returns the inspector public
     * key with the given identifier uid. The identifier associated with the key
     * will be used in presentation/issuance policies as the unique reference to
     * the dedicated Inspector.
     * In case of error it will return null.
     * 
     * @param keylength
     * @param mechanism
     * @param uid
     * @return
     * @throws Exception 
     */
    public InspectorPublicKey setupInspectorPublicKey(int keylength,
            URI mechanism, URI uid) throws Exception;

    /**
     * This method takes as input a presentation token with inspectable
     * attributes and returns the decrypted attribute type-value pairs for which
     * the Inspector has the inspection secret key.
     * 
     * @param t
     * @return
     * @throws Exception 
     */
    public List<Attribute> inspect(PresentationToken t) throws Exception;
}
