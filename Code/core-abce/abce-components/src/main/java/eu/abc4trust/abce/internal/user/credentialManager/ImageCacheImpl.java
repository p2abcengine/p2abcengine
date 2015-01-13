//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import com.google.inject.Inject;

import eu.abc4trust.util.StorageUtil;
import eu.abc4trust.util.TimingsLogger;

public class ImageCacheImpl implements ImageCache {

    private final static Logger logger = Logger.getLogger(ImageCacheImpl.class.getName());

    private final URL DEFAULT_IMAGE;
    private final ImageCacheStorage imStore;

    @Inject
    public ImageCacheImpl(ImageCacheStorage imStore) {
        this.imStore = imStore;
        URL url;
        try {
            url = new URL("file://" + imStore.getDefaultImagePath());
        } catch (MalformedURLException ex) {
            url = null;
        }
        this.DEFAULT_IMAGE = url;
    }

    @Override
    public URL storeImage(URI image) throws ImageCacheException {
        try  {
        	TimingsLogger.logTiming("storeImage", true);
            URL url = image.toURL();
            URLConnection connection = url
                    .openConnection();
            
            connection.setConnectTimeout(5000);            
            connection.connect();

            InputStream reader = connection.getInputStream();

            String filetype = this.getFiletype(image);
            URL imageUrl = this.imStore.store(filetype, reader);

            StorageUtil.closeIgnoringException(reader);
            TimingsLogger.logTiming("storeImage", false);
            return imageUrl;
        } catch (IOException ex) {
            logger.warning("storeImage - failed : " + image + " - exception : " + ex);
            TimingsLogger.logTiming("storeImage", false);
            return this.DEFAULT_IMAGE;
        } catch (Exception ex) {
            throw new ImageCacheException(ex);
        }
    }

    private String getFiletype(URI image) {
        String s = null;
        if (image.getPath() != null) {
        	s = image.getPath();
        } else {
    	    s = image.getSchemeSpecificPart();
        }
        int lastIndexOfDot = s.lastIndexOf(".");
        String type = s.substring(lastIndexOfDot + 1, s.length());
        return type;
    }

    @Override
    public URL getDefaultImage() {
        return this.DEFAULT_IMAGE;
    }
}
