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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;

import com.google.inject.Inject;


public class PersistenceStrategy {

    private final KeyStorage storage;

    @Inject
    public PersistenceStrategy(KeyStorage storage) {
        this.storage = storage;
    }

    public URI[] listObjects() throws PersistenceException {
      try {
        return this.storage.listUris();
      } catch (Exception e) {
        throw new PersistenceException(e);
      }
  }
    
    public Object loadObject(URI uid) throws PersistenceException {
        ObjectInputStream objectInput = null;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
             
            byte[] tokenBytes = this.storage.getValue(uid);
            if (tokenBytes == null) {
                return null;
            }
            byteArrayInputStream = new ByteArrayInputStream(tokenBytes);
            objectInput = new ObjectInputStream(byteArrayInputStream);
            return objectInput.readObject();
        } catch (Exception ex) {
            throw new PersistenceException(ex);
        } finally {
            // Close the streams.
            this.closeIgnoringException(objectInput);
            this.closeIgnoringException(byteArrayInputStream);
        }
    }

    public boolean writeObject(URI uid, Object o) throws PersistenceException {
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream objectOutput = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutput = new ObjectOutputStream(byteArrayOutputStream);
            objectOutput.writeObject(o);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            this.storage.addValue(uid, bytes);
            return true;
        } catch (Exception ex) {
            throw new PersistenceException(ex);
        } finally {
            // Close the streams.
            this.closeIgnoringException(objectOutput);
            this.closeIgnoringException(byteArrayOutputStream);
        }
    }

    public boolean writeObjectAndOverwrite(URI uid, Object o) throws PersistenceException{
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream objectOutput = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutput = new ObjectOutputStream(byteArrayOutputStream);
            objectOutput.writeObject(o);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            this.storage.addValueAndOverwrite(uid, bytes);
            return true;
        } catch (Exception ex) {
            throw new PersistenceException(ex);
        } finally {
            // Close the streams.
            this.closeIgnoringException(objectOutput);
            this.closeIgnoringException(byteArrayOutputStream);
        }
    }

    private void closeIgnoringException(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ex) {
                // Ignore, there is nothing we can do if close fails.
            }
        }
    }

}
