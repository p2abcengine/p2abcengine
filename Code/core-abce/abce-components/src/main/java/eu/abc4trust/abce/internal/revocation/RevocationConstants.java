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

package eu.abc4trust.abce.internal.revocation;

import java.net.URI;

public class RevocationConstants {

    public static String REVOCATION_HANDLE_STR = "http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle";
    public static final URI REVOCATION_HANDLE = URI
            .create(REVOCATION_HANDLE_STR);
    public static final URI REVOCATION_HANDLE_DATA_TYPE = URI
            .create("xs:integer");
    public static final URI REVOCATION_HANDLE_ENCODING = URI
            .create("urn:abc4trust:1.0:encoding:integer:unsigned");
}
