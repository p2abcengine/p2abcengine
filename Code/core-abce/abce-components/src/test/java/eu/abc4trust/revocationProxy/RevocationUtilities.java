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
