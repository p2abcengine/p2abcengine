//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
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
    this.bind(new TypeLiteral<List<String>>() {})
        .annotatedWith(Names.named("listOfPrefixesForCompressor")).toInstance(prefixes);
  }

}
