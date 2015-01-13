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

/*
 * ~~~~ Copyright notice IBM ~~~~
 */

package eu.abc4trust.abce.integrationtests;

import java.net.URI;
import java.util.Random;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClSignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsSignatureBuildingBlock;
import com.ibm.zurich.idmx.exception.ConfigurationException;

import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;

/**
 * 
 */
public class Helper {

  private static BuildingBlockFactory buildingBlockFactory;  

  static {
    Injector injector =
        Guice.createInjector(IntegrationModuleFactory.newModule(new Random(1234),
          CryptoEngine.IDEMIX));
    buildingBlockFactory = injector.getInstance(BuildingBlockFactory.class);
  }

  public static URI getSignatureTechnologyURI(String technology) throws ConfigurationException {
    if (technology.endsWith("cl") || technology.endsWith("idemix")) {
      return buildingBlockFactory.getBuildingBlockByClass(ClSignatureBuildingBlock.class)
          .getImplementationId();
    } else if (technology.endsWith("uprove") || technology.endsWith("brands")) {
      return buildingBlockFactory.getBuildingBlockByClass(BrandsSignatureBuildingBlock.class)
          .getImplementationId();
    }
    throw new RuntimeException("Technology cannot be found: " + technology);
  }

  public static URI getRevocationTechnologyURI(String technology) throws ConfigurationException {
    if (technology.endsWith("cl") || technology.endsWith("idemix")) {
      return buildingBlockFactory.getBuildingBlockByClass(ClRevocationBuildingBlock.class)
          .getImplementationId();
    }
    throw new RuntimeException("Technology cannot be found: " + technology);
  }
  
}
