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

package eu.abc4trust.cryptoEngine.revauth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eu.abc4trust.util.StorageUtil;

public class RevocationAuthorityStorage {

    private final File file;

    @Inject
    public RevocationAuthorityStorage(
            @Named("RevocationAuthorityStorageFile") File file) {
        super();
        this.file = file;
    }

    private byte[] getData(URI inspectorPkUid) throws Exception {
        return StorageUtil.getData(this.file, inspectorPkUid);
    }

    private void addData(URI uid, byte[] bytes) throws Exception {
        // Check to see if we have a key with the uid in the storage
        // layer.
        // If we do delete it and add the new.
        if (StorageUtil.HasUri(this.file, uid)) {
            StorageUtil.deleteData(this.file, uid);
        }
        StorageUtil.appendData(this.file, uid, bytes);
    }

    public RevocationAuthorityState get(URI uid) throws Exception {
    	System.out.println("GETTING REVOCATIONAUTHORITYSTATE FROM UID: "+uid);
        if (uid == null) {
            throw new Exception("UID is null");
        }
        ObjectInputStream objectInput = null;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            byte[] tokenBytes = this.getData(uid);
            if (tokenBytes == null) {
                throw new Exception(
                        "Revocation authrity storage could not find UID: \""
                                + uid
                                + "\" is not in storage");
            }
            byteArrayInputStream = new ByteArrayInputStream(tokenBytes);
            objectInput = new ObjectInputStream(byteArrayInputStream);
            RevocationAuthorityState value = (RevocationAuthorityState) objectInput
                    .readObject();
            return value;
        } catch (Exception ex) {
            throw new Exception(ex);
        } finally {
            // Close the streams.
            StorageUtil.closeIgnoringException(objectInput);
            StorageUtil.closeIgnoringException(byteArrayInputStream);
        }
    }

    public void store(URI revAuthParamsUid,
            RevocationAuthorityState revocationAuthorityState)
                    throws Exception {
    	System.out.println("STORING REVOCATION_AUTHORITY_STATE IN UID: "+revAuthParamsUid);
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream objectOutput = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutput = new ObjectOutputStream(byteArrayOutputStream);
            objectOutput.writeObject(revocationAuthorityState);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            this.addData(revAuthParamsUid, bytes);
        } catch (Exception ex) {
            throw new Exception(ex);
        } finally {
            // Close the streams.
            StorageUtil.closeIgnoringException(objectOutput);
            StorageUtil.closeIgnoringException(byteArrayOutputStream);
        }
    }

}
