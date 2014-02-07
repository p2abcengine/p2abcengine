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

package eu.abc4trust.cryptoEngine.uprove.user;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import eu.abc4trust.cryptoEngine.uprove.util.UProveBindingManager;
import eu.abc4trust.util.MyAttribute;

/**
 * Context with cached values and the binding (webservice connection) to be share amongst the 
 * classes doing work for UProveCryptoEngineUserImpl
 * 
 * 
 */
public class CryptoEngineContext {
	public final Map<URI, String> sessionKeyCache;
	public final Map<URI, URI> uidOfIssuedCredentialCache;
	public final Map<URI, List<MyAttribute>> attributeCache;
	public final HashMap<URI, URI> secretCache;
	public final UProveBindingManager binding;
    
	@Inject
    public CryptoEngineContext(UProveBindingManager binding) {
        this.attributeCache = new HashMap<URI, List<MyAttribute>>();
        this.sessionKeyCache = new HashMap<URI, String>();
        this.secretCache = new HashMap<URI, URI>();
        this.uidOfIssuedCredentialCache = new HashMap<URI, URI>();
        
        // Setup WebService connection to .NET based UProve..
        this.binding = binding;
        this.binding.setupBiding("UserEngine");
    }
}
