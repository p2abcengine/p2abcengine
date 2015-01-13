//* Licensed Materials - Property of                                  *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
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
package eu.abc4trust.abce.integrationtests;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.zurich.idmix.abc4trust.facades.InspectorParametersFacade;
import com.ibm.zurich.idmix.abc4trust.facades.IssuerParametersFacade;
import com.ibm.zurich.idmx.exception.ConfigurationException;

import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.SystemParameters;

public class Entities {

    private class Entity {
      private String name;
      private URI technology;
      private Injector injector;
      private boolean useRevocation;

      public Entity(String name, URI technology, boolean useRevocation) {
        this.name = name;
        this.technology = technology;
        this.useRevocation = useRevocation;
      }

      public Entity(String name, boolean useRevocation) {
          this.name = name;
          this.useRevocation = useRevocation;
        }
      
      
      public Entity(String name) {
        this.name = name;
      }

      public void setInjector(Injector injector) {
        this.injector = injector;
      }

      public String getName() {
        return name;
      }

      public Injector getInjector() {
        return injector;
      }

      public URI getTechnology() {
        return technology;
      }
      
      public boolean useRevocation(){
        return useRevocation;
      }

      @Override
      public boolean equals(Object entity) {
        if (entity instanceof String) {
          return name.equalsIgnoreCase((String) entity);
        } else if (entity instanceof Entity) {
          return name.equalsIgnoreCase(((Entity) entity).getName());
        } else {
          return false;
        }
      }
    }


    private Map<String, Entity> entities;
    private Collection<Injector> injectors;
    private Random rand;

    /**
     * Use for tests only
     */
    public Entities() {
      entities = new HashMap<String, Entity>();
      injectors = new ArrayList<Injector>();
      rand = new Random(1234);
    }

    public void addEntity(String name) {
      if (entities.containsKey(name)) {
        throw new RuntimeException("Entity already exists. An entity can only be registered once.");
      }
      entities.put(name, new Entity(name));
    }

    public void addEntity(String name, URI technology, boolean useRevocation) {
      if (entities.containsKey(name)) {
        throw new RuntimeException("Entity already exists. An entity can only be registered once.");
      }
      entities.put(name, new Entity(name, technology, useRevocation));
    }

    public void addEntity(String name, boolean useRevocation) {
        if (entities.containsKey(name)) {
          throw new RuntimeException("Entity already exists. An entity can only be registered once.");
        }
        entities.put(name, new Entity(name, useRevocation));
      }
    
    public void setInjector(String name, Injector injector) {
      if (entities.containsKey(name)) {
        // add injector to the given entity
        entities.get(name).setInjector(injector);
        // also add the injector to the general list of injectors
        injectors.add(injector);
      }
      throw new RuntimeException("Entity already exists. An entity can only be registered once.");
    }

    public Collection<Injector> getInjectors() {
      return injectors;
    }

    public boolean contains(String entity) {
      return (entities.containsKey(entity));
    }

    public void initInjectors(){
      initInjectors(null);
    }
    
    public void initInjectors(RevocationProxyAuthority revocationProxyAuthority) {
      for (Entity entity : entities.values()) {
        Injector injector;
        if(entity.useRevocation()){
          injector = initInjector(rand, revocationProxyAuthority);
        }else{
           injector = initInjector(rand, null);
        }
        entity.setInjector(injector);
        injectors.add(injector);
      }
    }
    
    private Injector initInjector(Random rand, RevocationProxyAuthority revocationProxyAuthority) {
      Injector injector;
      if(revocationProxyAuthority == null){
        injector =
            Guice.createInjector(IntegrationModuleFactory.newModule(rand, CryptoEngine.IDEMIX));
      }else{
        injector =
            Guice.createInjector(IntegrationModuleFactory.newModule(rand, CryptoEngine.IDEMIX, revocationProxyAuthority));
      }
      return injector;
    }

    public URI getTechnology(String name) {
      return entities.get(name).getTechnology();
    }

    public Injector getInjector(String name) {
      return entities.get(name).getInjector();
    }
    
    //==============
    //Helper methods
    //==============
    
    public static SystemParameters setupSystemParameters(Entities entities, int keyLength)
        throws KeyManagerException, CryptoEngineException {
      // Generate system parameters and load them into the key managers of all parties
      SystemParameters systemParameters;
      if(keyLength == 1024){
        systemParameters = SystemParametersUtil.getDefaultSystemParameters_1024();
      }else if(keyLength == 2048){
        systemParameters = SystemParametersUtil.getDefaultSystemParameters_2048();
      }else{
        throw new IllegalArgumentException("Supported keylengths for tests are 1024 and 2048");
      }
      loadSystemParametersIntoEntityKeyManagers(systemParameters, entities.getInjectors());
      return systemParameters;
    }

    private static void loadSystemParametersIntoEntityKeyManagers(SystemParameters systemParameters,
                                                           Collection<Injector> injectors) throws KeyManagerException {

      for (Injector injector : injectors) {
        KeyManager keyManager = injector.getInstance(KeyManager.class);
        keyManager.storeSystemParameters(systemParameters);
      }
    }
    
    public void storePublicParametersToKeyManagers(List<Object> publicParameters) 
        throws KeyManagerException, ConfigurationException {

      // Iterate over all key managers
      for (Injector injector : injectors) {
        KeyManager keyManager = injector.getInstance(KeyManager.class);

        // Iterate over all parameters
        for (Object parameters : publicParameters) {

          // Check for issuer parameters
          if (IssuerParameters.class.isAssignableFrom(parameters.getClass())) {
            IssuerParametersFacade ipWrapper =
                new IssuerParametersFacade((IssuerParameters) parameters);
            keyManager.storeIssuerParameters(ipWrapper.getIssuerParametersId(),
              ipWrapper.getIssuerParameters());

          }
          // Check for inspector parameters
          else if (InspectorPublicKey.class.isAssignableFrom(parameters.getClass())) {
            InspectorParametersFacade ipWrapper =
                new InspectorParametersFacade((InspectorPublicKey) parameters);
            keyManager.storeInspectorPublicKey(ipWrapper.getInspectorId(),
              ipWrapper.getInspectorParameters());
          }
        }
      }
    }
  }