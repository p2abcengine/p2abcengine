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
