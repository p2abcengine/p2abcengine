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
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.logging.Logger;

import eu.abc4trust.ri.servicehelper.AbstractHelper;

public class ServiceResourceStorage {

    static Logger log = Logger
            .getLogger(ServiceResourceStorage.class.getName());

    private static ServiceResourceStorage instance;

    /**
     * @return initialized instance of ServiceResourceStorage
     */
    public static synchronized ServiceResourceStorage getInstance() {
        log.info("ServiceResourceStorage.getInstance : " + instance);
        if (instance == null) {
            instance = new ServiceResourceStorage();
        }
        return instance;
    }

    private ServiceResourceStorage() {

    }

    public String[] loadIssuerParametersResourceList()
            throws FileNotFoundException {
        String[] issuerParamsResourceList = null;
        try {
            issuerParamsResourceList = this.getFilesFromDirContainingString(
                    Constants.ISSUER_RESOURCES_FOLDER, "issuer_params");
        } catch (NullPointerException npe) {
            throw new FileNotFoundException(
                    "Issuer resources folder does not exist!");
        }
        return issuerParamsResourceList;
    }

    public String[] loadCredentialSpecificationResourceList()
            throws FileNotFoundException {
        String[] credSpecResourceList = null;
        // try {
        credSpecResourceList = this.getFilesFromDirContainingString(
                Constants.CREDENTIAL_SPECIFICATION_FOLDER,
                "credentialSpecification");
        // } catch (NullPointerException npe) {
        // throw new FileNotFoundException(
        // "Credential Specification resources folder does not exist!");
        // }
        return credSpecResourceList;
    }

    public String[] getFilesFromDirContainingString(String folderName,
            final String filter) {
        String[] resourceList;
        ServiceResourceStorage.log.info("Get files from directory: \""
                + folderName + "\" containing strings: " + filter);
        URL url = AbstractHelper.class.getResource(folderName);
        File folder = null;
        if (url != null) {
            folder = new File(url.getFile());
        } else {
            folder = new File(folderName);
        }

        File parentFolder = folder.getParentFile();
        if ((!folder.exists()) && parentFolder.exists()) {
            folder.mkdirs();
        }

        File[] fileList = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File arg0, String arg1) {
                if (arg1.indexOf(filter) != -1) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        if (fileList != null) {
            resourceList = new String[fileList.length];
            for (int i = 0; i < fileList.length; i++) {
                resourceList[i] = fileList[i].getAbsolutePath();
            }
        } else {
            resourceList = new String[0];
        }
        return resourceList;
    }
}
