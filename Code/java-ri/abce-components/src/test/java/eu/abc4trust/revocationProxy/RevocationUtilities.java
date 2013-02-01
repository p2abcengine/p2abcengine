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

package eu.abc4trust.revocationProxy;

import java.net.URI;
import java.net.URISyntaxException;

public class RevocationUtilities {
    private static URI REVOCATION_REFERENCE_TYPE = null;
    
    public static URI getTestURI()
    {
        if (RevocationUtilities.REVOCATION_REFERENCE_TYPE==null)
        {
            try
            {
                RevocationUtilities.REVOCATION_REFERENCE_TYPE = new URI("http://test.revocationproxy.abc4trust.eu");
            }
            catch (URISyntaxException e)
            {
                //hardcoded uri, this shouldn't happen
                System.out.println("TestUtilities.getTestUri throws URISyntaxException please fix.");
                assert(false);
            }
        }   
        return RevocationUtilities.REVOCATION_REFERENCE_TYPE;
    }    
}
