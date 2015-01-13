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

package eu.abc4trust.smartcard;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;

public class StaticUriToIDMap {

	public static boolean Patras = true;
	
	private Map<URI, Byte> patrasIssuers;
	private Map<URI, Byte> soderhamnIssuersIdemix;
	private Map<URI, Byte> soderhamnIssuersUProve;
	private static StaticUriToIDMap instance;
	public static final byte credUnivIdemixIssuer = 1,
			credCourseUProveIssuer = 2,
			credTombolaIdemixIssuer = 3;
	
	public static final byte credSchoolIdemixIssuer = 1,
	    credChildIdemixIssuer = 2,
	    credGuardianIdemixIssuer = 3,
	    credClassIdemixIssuer = 4,
	    credSubjectIdemixIssuer = 5,
	    credRoleIdemixIssuer = 6;
    public static final byte credSchoolUProveIssuer = 1,
        credChildUProveIssuer = 2,
        credGuardianUProveIssuer = 3,
        credClassUProveIssuer = 4,
        credSubjectUProveIssuer = 5,
        credRoleUProveIssuer = 6;
	    
    public static final URI courseIssuerUID = URI.create("urn:patras:issuer:credCourse:uprove");
    
	private StaticUriToIDMap(){
	    // patras
		URI credUnivIssuerIdemix = URI.create("urn:patras:issuer:credUniv:idemix");
		URI credCourseIssuerUprove = courseIssuerUID;
		URI credTombolaIssuerIdemix = URI.create("urn:patras:issuer:credTombola:idemix");

		// soderhamn 
        URI credSchoolIssuerIdemix = URI.create("urn:soderhamn:issuer:credSchool:idemix");
        URI credChildIssuerIdemix = URI.create("urn:soderhamn:issuer:credChild:idemix");
        URI credGuardianIssuerIdemix = URI.create("urn:soderhamn:issuer:credGuardian:idemix");
        URI credClassIssuerIdemix = URI.create("urn:soderhamn:issuer:credClass:idemix");
        URI credSubjectIssuerIdemix = URI.create("urn:soderhamn:issuer:credSubject:idemix");
        URI credRoleIssuerIdemix = URI.create("urn:soderhamn:issuer:credRole:idemix");

        URI credSchoolIssuerUProve = URI.create("urn:soderhamn:issuer:credSchool:uprove");
        URI credChildIssuerUProve = URI.create("urn:soderhamn:issuer:credChild:uprove");
        URI credGuardianIssuerUProve = URI.create("urn:soderhamn:issuer:credGuardian:uprove");
        URI credClassIssuerUProve = URI.create("urn:soderhamn:issuer:credClass:uprove");
        URI credSubjectIssuerUProve = URI.create("urn:soderhamn:issuer:credSubject:uprove");
        URI credRoleIssuerUProve = URI.create("urn:soderhamn:issuer:credRole:uprove");

		//
		patrasIssuers = new HashMap<URI, Byte>();
		soderhamnIssuersIdemix = new HashMap<URI, Byte>();
		soderhamnIssuersUProve = new HashMap<URI, Byte>();
		// patras		
		patrasIssuers.put(credUnivIssuerIdemix, credUnivIdemixIssuer);
		patrasIssuers.put(credCourseIssuerUprove, credCourseUProveIssuer);
		patrasIssuers.put(credTombolaIssuerIdemix, credTombolaIdemixIssuer);
		
		// soderhamn
		soderhamnIssuersIdemix.put(credSchoolIssuerIdemix, credSchoolIdemixIssuer);
		soderhamnIssuersIdemix.put(credChildIssuerIdemix, credChildIdemixIssuer);
		soderhamnIssuersIdemix.put(credGuardianIssuerIdemix, credGuardianIdemixIssuer);
		soderhamnIssuersIdemix.put(credClassIssuerIdemix, credClassIdemixIssuer);
		soderhamnIssuersIdemix.put(credSubjectIssuerIdemix, credSubjectIdemixIssuer);
		soderhamnIssuersIdemix.put(credRoleIssuerIdemix, credRoleIdemixIssuer);
		
		soderhamnIssuersUProve.put(credSchoolIssuerUProve, credSchoolUProveIssuer);
		soderhamnIssuersUProve.put(credChildIssuerUProve, credChildUProveIssuer);
		soderhamnIssuersUProve.put(credGuardianIssuerUProve, credGuardianUProveIssuer);
		soderhamnIssuersUProve.put(credClassIssuerUProve, credClassUProveIssuer);
		soderhamnIssuersUProve.put(credSubjectIssuerUProve, credSubjectUProveIssuer);
		soderhamnIssuersUProve.put(credRoleIssuerUProve, credRoleUProveIssuer);
	}
	
	public static StaticUriToIDMap getInstance(){
		if(instance == null){
			instance = new StaticUriToIDMap();
		}
		return instance;
	}
	
	public Byte getIssuerIDFromUri(URI issuerUri){
		Byte id;
		if(Patras){
			id = patrasIssuers.get(issuerUri);
		}else{
			id = soderhamnIssuersIdemix.get(issuerUri);
			if(id == null){
				id = soderhamnIssuersUProve.get(issuerUri);
			}
		}
		if(id == null){
			throw new RuntimeException("[StaticMap] The given IssuerURI: " +issuerUri.toString()+ " does not map to an ID");
		}
		return id;
	}

	public URI getIssuerUriFromID(byte ID, CryptoEngine engine) {		
		Map<URI, Byte> issuers = null;		
		if(Patras){
			issuers = patrasIssuers;
		}else{
			if(engine == CryptoEngine.IDEMIX){
				issuers = soderhamnIssuersIdemix;
			}else{
				issuers = soderhamnIssuersUProve;
			}
		}
		for(URI uri : issuers.keySet()){
			if(issuers.get(uri) == ID){
				return uri;
			}
		}
		throw new RuntimeException("[StaticMap] The given ID: " + ID+" does not map to an issuer URI");
	}
}
