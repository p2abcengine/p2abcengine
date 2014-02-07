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

package eu.abc4trust.guice.credCompressor;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

public class CredentialCompressorListModule extends AbstractModule {

  @Override
  protected void configure() {
    List<String> prefixes = new ArrayList<String>();
    //Soderhamn pilot specific strings.
    prefixes.add("urn:soderhamn:credspec:credSchool");
    prefixes.add("urn:soderhamn:credspec:credChild");
    prefixes.add("urn:soderhamn:credspec:credGuardian");
    prefixes.add("urn:soderhamn:credspec:credClass");
    prefixes.add("urn:soderhamn:credspec:credRole");
    prefixes.add("urn:soderhamn:credspec:credSubject");
    prefixes.add("urn:soderhamn:issuer:credSchool:uprove");
    prefixes.add("urn:soderhamn:issuer:credChild:uprove");
    prefixes.add("urn:soderhamn:issuer:credGuardian:uprove");
    prefixes.add("urn:soderhamn:issuer:credClass:uprove");
    prefixes.add("urn:soderhamn:issuer:credRole:uprove");
    prefixes.add("urn:soderhamn:issuer:credSubject:uprove");
    prefixes.add("urn:soderhamn:issuer:credSchool:idemix");
    prefixes.add("urn:soderhamn:issuer:credChild:idemix");
    prefixes.add("urn:soderhamn:issuer:credGuardian:idemix");
    prefixes.add("urn:soderhamn:issuer:credClass:idemix");
    prefixes.add("urn:soderhamn:issuer:credRole:idemix");
    prefixes.add("urn:soderhamn:issuer:credSubject:idemix");
    prefixes.add("urn:soderhamn:revocationauthority:default");
    prefixes.add("urn:soderhamn:revocationauthority:");
    prefixes.add("urn:abc4trust:1.0:nonrevocation:evidence/");
    prefixes.add("urn:soderhamn:");
    prefixes.add("urn:abc4trust:1.0:");
    this.bind(new TypeLiteral<List<String>>() {})
        .annotatedWith(Names.named("listOfPrefixesForCompressor")).toInstance(prefixes);
  }

}
