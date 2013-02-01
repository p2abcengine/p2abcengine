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

import java.net.URI;
import java.net.URL;

public interface ImageCache {

    /**
     * This method stores the image at the given URI in a cache on the local
     * machine. The image is downloaded again even if it is already cached. If
     * the download fails for some reason, then the returned URI points to a
     * default image. An exception is thrown if the image could not be
     * persisted.
     * 
     * @param image
     * @return returns a URL to a local copy of the image.
     * @throws ImageCacheException
     */
    URL storeImage(URI image) throws ImageCacheException;

    URL getDefaultImage();

}
