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
