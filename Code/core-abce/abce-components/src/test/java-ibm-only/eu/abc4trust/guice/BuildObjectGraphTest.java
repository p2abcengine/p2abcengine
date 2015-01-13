//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

package eu.abc4trust.guice;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.abce.external.inspector.InspectorAbcEngine;
import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;

// TODO(enr): For now these methods do not test if the correct object graph was
// actually created. You can read the output on the console for now :-)
/**
 * Test that Guice does not throw an exception when creating an ABC Engine.
 * @author enr
 *
 */
public class BuildObjectGraphTest{
  
  @Test
  public void userObjectGraph() {
    System.out.println("---- User object graph ----");
    Injector injector = Guice.createInjector(ProductionModuleFactory.newModule()); 
    @SuppressWarnings("unused")
    UserAbcEngine engine = injector.getInstance(UserAbcEngine.class);
  }
  
  @Test
  public void verifierObjectGraph() {
    System.out.println("---- Verifier object graph ----");
    Injector injector = Guice.createInjector(ProductionModuleFactory.newModule()); 
    @SuppressWarnings("unused")
    VerifierAbcEngine engine = injector.getInstance(VerifierAbcEngine.class);
  }
  
  @Test
  public void issuerObjectGraph() {
    System.out.println("---- Issuer object graph ----");
    Injector injector = Guice.createInjector(ProductionModuleFactory.newModule()); 
    @SuppressWarnings("unused")
    IssuerAbcEngine engine = injector.getInstance(IssuerAbcEngine.class);
  }
  
  @Test
  public void revocationObjectGraph() {
    System.out.println("---- Revocation object graph ----");
    Injector injector = Guice.createInjector(ProductionModuleFactory.newModule()); 
    @SuppressWarnings("unused")
    RevocationAbcEngine engine = injector.getInstance(RevocationAbcEngine.class);
  }
  
  @Test
  public void inspectorObjectGraph() {
    System.out.println("---- Inspector object graph ----");
    Injector injector = Guice.createInjector(ProductionModuleFactory.newModule()); 
    @SuppressWarnings("unused")
    InspectorAbcEngine engine = injector.getInstance(InspectorAbcEngine.class);
  }
}
