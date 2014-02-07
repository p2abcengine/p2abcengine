//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
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

package eu.abc4trust.ri.servicehelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBElement;

import eu.abc4trust.xml.util.XmlUtils;

public class FileSystem {

    public static File getFile(String filename, boolean wipe_existing_file)
            throws IOException {
        System.out.println("getFile : " + filename + " - wipe : "
                + wipe_existing_file);

        URL file = FileSystem.class.getResource(filename);

        File f = null;
        if (file != null) {
            f = new File(file.getFile());
        } else {
            f = new File(filename);
        }
        if (f.exists() && !wipe_existing_file) {
            System.out.println("getFile : " + filename + " - exists!");
            return f;
        } else {
            if (f.exists() && wipe_existing_file) {
                System.out.println("file exits - wipe it! " + filename);
                f.delete();
            }

            File folder = f.getParentFile();
            if ((f.getParentFile() != null) && !folder.exists()) {
                System.out.println("create folders : " + folder);
                folder.mkdirs();
            }
            System.out.println("create new file : " + filename);
            // System.out.println("Folder : " + folder.getAbsolutePath() + " : "
            // + folder.exists());
            boolean created = f.createNewFile();
            if (!created) {
                throw new IOException("Could not create new file : " + filename);
            }
            return f;
        }
    }

    public static void storeObjectInFile(Object object, String prefix,
            String name) throws IOException {
        File file = getFile(prefix + name, false);// new File(prefix + name);
        System.out.println("Store Object " + object + " - in file "
                + file.getAbsolutePath());

        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(object);
        fos.close();
    }

    public static void storeObjectInFile(Object object, String resourceName)
            throws IOException {
        File file = getFile(resourceName, false);
        System.out.println("store Object " + object + " - in file "
                + file.getAbsolutePath());

        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(object);
        fos.close();
    }

    public static void storeObjectAsXMLInFile(JAXBElement<?> element,
            String prefix, String resourceName) throws Exception {

        File file = new File(prefix + resourceName
                + "_human_readable_only_for_reference.xml");
        System.out.println("store Object: " + element + " - as XML in file "
                + file.getAbsolutePath());
        String normalizedXml = XmlUtils.toNormalizedXML(element);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(normalizedXml.getBytes(Charset.forName("UTF-8")));
        fos.flush();
        fos.close();
    }

    public static InputStream getInputStream(String resource)
            throws IOException {
        InputStream is = AbstractHelper.class.getResourceAsStream(resource);
        if (is == null) {
            File f = new File(resource);
            if (!f.exists()) {
                throw new IllegalStateException("Resource not found :  "
                        + resource);
            }

            is = new FileInputStream(f);
        }
        return is;
    }

    @SuppressWarnings("unchecked")
    public static <T> T loadObjectFromResource(String name) throws IOException,
    ClassNotFoundException {
        System.out.println("Load Object from Resource : " + name);
        InputStream is = getInputStream(name);
        ObjectInputStream ois = new ObjectInputStream(is);

        Object object = ois.readObject();
        ois.close();
        is.close();

        return (T) object;
    }
}
