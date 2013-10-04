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

package eu.abc4trust.services;

import java.io.File;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;

public class ITAbstract {

    public Builder getHttpBuilder(String string, String baseUrl) {
        Client client = Client.create();
        Builder resource = client.resource(baseUrl + string)
                .type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML);
        return resource;
    }

    protected void deleteStorageDirectory(String storageDirectory) {
        File directory1 = new File("target" + File.separatorChar
                + storageDirectory);
        File directory2 = new File("abce-services" + File.separatorChar
                + "target" + File.separatorChar + storageDirectory);

        this.delete(directory1);
        this.delete(directory2);

    }

    private void delete(File directory) {
        if (directory.exists()) {
            this.deleteBody(directory);
        }

    }

    private void deleteBody(File file) {
        if (file.isDirectory()) {

            // directory is empty, then delete it
            if (file.list().length == 0) {

                file.delete();
                System.out.println("Directory is deleted : "
                        + file.getAbsolutePath());

            } else {

                // list all the directory contents
                String files[] = file.list();

                for (String temp : files) {
                    // construct the file structure
                    File fileDelete = new File(file, temp);

                    // recursive delete
                    this.deleteBody(fileDelete);
                }

                // check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();
                    System.out.println("Directory is deleted : "
                            + file.getAbsolutePath());
                }
            }

        } else {
            // if file, then delete it
            file.delete();
            System.out.println("File is deleted : " + file.getAbsolutePath());
        }
    }

}
