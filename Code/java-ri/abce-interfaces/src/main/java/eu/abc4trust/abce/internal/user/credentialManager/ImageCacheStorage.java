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

package eu.abc4trust.abce.internal.user.credentialManager;

import java.io.InputStream;
import java.net.URL;

public interface ImageCacheStorage {

    /**
     * This method reads the data in the inputstream and stores it to a file on
     * the filesystem.
     * 
     * @param filetype
     * @param inputStream
     * @return returns a path as a url to the file
     * @throws ImageCacheStorageException
     */
    URL store(String filetype, InputStream inputStream)
            throws ImageCacheStorageException;

    /**
     * Returns the absolute path of the directory used for cache storage.
     * 
     * @return returns an absolute path
     */
    String getBasePath();

    /**
     * Return the path to the default image.
     * 
     * @return returns a string containing the path to the default image
     */
    String getDefaultImagePath();

}
