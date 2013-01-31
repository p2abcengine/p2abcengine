//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.guice.ui;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import eu.abc4trust.ui.idSelection.IdentitySelection;
import eu.abc4trust.ui.idSelection.IdentitySelectionConverter;
import eu.abc4trust.ui.idSelection.IdentitySelectionPrinter;
import eu.abc4trust.ui.idSelection.IdentitySelectionUi;
import eu.abc4trust.ui.idSelection.IdentitySelectionUiPrinter;
import eu.abc4trust.ui.idSelection.IdentitySelectionXml;
import eu.abc4trust.ui.idSelection.MockIdentitySelectionUi;
import eu.abc4trust.ui.idSelection.MockIdentitySelectionXml;

public class MockUiModule extends AbstractModule {

  @Override
  protected void configure() {
    // New UI
    this.bind(IdentitySelectionUi.class).to(IdentitySelectionUiPrinter.class).in(Singleton.class);
    this.bind(IdentitySelectionUi.class).annotatedWith(Names.named("RealIdSelector")).to(MockIdentitySelectionUi.class).in(Singleton.class);
    
    // Old UI
    this.bind(IdentitySelection.class).to(IdentitySelectionPrinter.class).in(Singleton.class);
    this.bind(IdentitySelection.class).annotatedWith(Names.named("RealIdSelector")).to(IdentitySelectionConverter.class).in(Singleton.class);
    this.bind(IdentitySelectionXml.class).to(MockIdentitySelectionXml.class).in(Singleton.class);
  }

}
