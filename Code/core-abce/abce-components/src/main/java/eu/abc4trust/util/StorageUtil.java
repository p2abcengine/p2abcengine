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

package eu.abc4trust.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;


public class StorageUtil {

    public static final String UTF8Charset = "UTF-8";

    public static URI readUriBytes(DataInputStream in, int n, byte[] uriBytes)
            throws IOException, URISyntaxException {
        int v = in.read(uriBytes);
        while (v < n) {
            v += in.read(uriBytes, v, n - v);
        }
        URI storedUri = new URI(new String(uriBytes,
                Charset.forName(UTF8Charset)));
        return storedUri;
    }

    public static void skip(DataInputStream in, int sizeOfValue)
            throws IOException {
        long valuesSkipped = in.skip(sizeOfValue);
        while (valuesSkipped < sizeOfValue) {
            valuesSkipped += in.skip(sizeOfValue - valuesSkipped);
        }
    }

    public static boolean compareUris(URI uri, URI storedUri) {
        return 0 == uri.compareTo(storedUri);
    }

    public static void appendData(File file, URI uri, byte[] data)
            throws IOException {
        FileOutputStream fos = new FileOutputStream(file, true);
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
                fos));
        String uriAsString = uri.toString();
        byte[] uriAsBytes = uriAsString.getBytes(StorageUtil.UTF8Charset);
        out.writeInt(uriAsBytes.length);
        out.write(uriAsBytes);
        out.writeInt(data.length);
        out.write(data);
        out.flush();
        out.close();
    }

    public static byte[] getData(File file, URI uri) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        long fileSize = file.length();
        if (fileSize < 4) {
            fis.close();
            return null;
        }
        DataInputStream in = new DataInputStream(new BufferedInputStream(fis));

        try {
            while (true) {
                int n = in.readInt();
                byte[] uriBytes = new byte[n];
                URI storedUri = StorageUtil.readUriBytes(in, n, uriBytes);
                if (storedUri == null) {
                    return null;
                }
                boolean b = StorageUtil.compareUris(uri, storedUri);
                int sizeOfValue = in.readInt();
                if (b) {
                    return getValueBytes(in, sizeOfValue);
                }
                StorageUtil.skip(in, sizeOfValue);
            }
        } catch (EOFException ex) {
            return null;
        }
    }

    public static boolean HasUri(File file, URI uri) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        long fileSize = file.length();
        if (fileSize < 4) {
            fis.close();
            return false;
        }
        DataInputStream in = new DataInputStream(new BufferedInputStream(fis));

        try {
            while (true) {
                int n = in.readInt();
                byte[] uriBytes = new byte[n];
                URI storedUri = StorageUtil.readUriBytes(in, n, uriBytes);
                boolean b = StorageUtil.compareUris(uri, storedUri);
                int sizeOfValue = in.readInt();
                if (b) {
                    return true;
                }
                StorageUtil.skip(in, sizeOfValue);
            }
        } catch (EOFException ex) {
            return false;
        }
    }

    private static byte[] getValueBytes(DataInputStream in, int sizeOfValue)
            throws IOException {
        byte[] valueBytes = new byte[sizeOfValue];
        in.read(valueBytes);
        return valueBytes;
    }

    public static List<URI> getAllUris(File file) throws Exception {
        List<URI> ls = new LinkedList<URI>();
        FileInputStream fis = new FileInputStream(file);
        long fileSize = file.length();
        if (fileSize < 4) {
            fis.close();
            return ls;
        }
        DataInputStream in = new DataInputStream(new BufferedInputStream(fis));

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
            return ls;
        }
    }

    public static List<byte[]> getAllValues(File file) throws Exception {
        List<byte[]> ls = new LinkedList<byte[]>();
        FileInputStream fis = new FileInputStream(file);
        long fileSize = file.length();
        if (fileSize < 4) {
            fis.close();
            return ls;
        }
        DataInputStream in = new DataInputStream(new BufferedInputStream(fis));

        try {
            while (true) {
                int sizeOfUri = in.readInt();
                StorageUtil.skip(in, sizeOfUri);
                int sizeOfValue = in.readInt();
                ls.add(getValueBytes(in, sizeOfValue));
            }
        } catch (EOFException ex) {
            return ls;
        }
    }

    public static void deleteData(File file, URI uri) throws Exception {

        File tempFile = TemporaryFileFactory.createTemporaryFile();
        FileOutputStream fos = new FileOutputStream(tempFile, true);
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
                fos));

        FileInputStream fis = new FileInputStream(file);
        DataInputStream in = new DataInputStream(new BufferedInputStream(fis));

        try {
            while (true) {
                int n = in.readInt();
                byte[] uriBytes = new byte[n];
                URI storedUri = StorageUtil.readUriBytes(in, n, uriBytes);
                boolean b = StorageUtil.compareUris(uri, storedUri);
                int sizeOfValue = in.readInt();
                byte[] valueBytes = getValueBytes(in, sizeOfValue);
                if (!b) {
                    out.writeInt(n);
                    out.write(uriBytes);
                    out.writeInt(sizeOfValue);
                    out.write(valueBytes);
                }

            }
        } catch (EOFException ex) {
        }
        out.flush();
        out.close();
        in.close();

        copy(tempFile, file);
    }

    public static void deleteData(File file, String primaryKey) throws Exception {

        File tempFile = TemporaryFileFactory.createTemporaryFile();
        FileOutputStream fos = new FileOutputStream(tempFile, true);
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
                fos));

        FileInputStream fis = new FileInputStream(file);
        DataInputStream in = new DataInputStream(new BufferedInputStream(fis));

        try {
            while (true) {
                String primaryKeyTemp = in.readUTF(); // readUTF itself will take care of separating the single keys...
                if(!primaryKeyTemp.equals(primaryKey)) {
                    out.writeUTF(primaryKeyTemp); // writeUTF itself will take care of separating the single keys...
                }
            }
        } catch (EOFException ex) {
        }
        out.flush();
        out.close();
        in.close();
        copy(tempFile, file);
    }

    private static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }


    public static void closeIgnoringException(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ex) {
                // Ignore, there is nothing we can do if close fails.
            }
        }
    }
}
