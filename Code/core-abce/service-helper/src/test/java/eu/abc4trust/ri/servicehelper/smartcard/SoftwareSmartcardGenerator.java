//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

package eu.abc4trust.ri.servicehelper.smartcard;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.issuer.CryptoTechnology;
import eu.abc4trust.smartcard.RSAKeyPair;
import eu.abc4trust.smartcard.RSASignatureSystem;
import eu.abc4trust.smartcard.RSAVerificationKey;
import eu.abc4trust.smartcard.SmartcardInitializeTool;
import eu.abc4trust.smartcard.SmartcardInitializeTool.InitializeResult;
import eu.abc4trust.smartcard.SoftwareSmartcard;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.SystemParameters;

public class SoftwareSmartcardGenerator {

  public static RSAKeyPair getSigningKeyPair() throws Exception {
    return eu.abc4trust.smartcard.RSASignatureSystemTest.getSigningKeyForTest();
  }

  public static RSAKeyPair getCASKeyPair() {
    return eu.abc4trust.smartcard.RSASignatureSystemTest.getAnotherSigningKeyForTest();
  }

  public static RSAVerificationKey getCASPublicKey() {
    RSAKeyPair cas_keypair =
        eu.abc4trust.smartcard.RSASignatureSystemTest.getAnotherSigningKeyForTest();
    RSAVerificationKey cas_public = RSASignatureSystem.getVerificationKey(cas_keypair);
    return cas_public;
  }

  public static SoftwareSmartcard OLDinitSmartCard(int pin, URI scope,
      String systemParametersResource, String[] issuerParametersResourceList,
      String issuerParamWithAttendanceResource, int minAttendance) throws Exception {
    SystemParameters systemParameters = FileSystem.loadObjectFromResource(systemParametersResource);

    List<IssuerParameters> issuerParametersList =
        new ArrayList<IssuerParameters>();
    IssuerParameters issuerParamWithAttendance = null;
    for (String resource : issuerParametersResourceList) {
      issuerParametersList.add((IssuerParameters)FileSystem.loadObjectFromResource(resource));
    }
    if (issuerParamWithAttendanceResource != null) {
      issuerParamWithAttendance =
          FileSystem.loadObjectFromResource(issuerParamWithAttendanceResource);
    }
    return initSmartCard(pin, scope, systemParameters, issuerParametersList,
        issuerParamWithAttendance, minAttendance);
  }

  public static SoftwareSmartcard initSmartCard(int pin, URI scope,
      SystemParameters systemParameters, List<IssuerParameters> issuerParametersList,
      IssuerParameters issuerParamWithAttendance, int minAttendance) throws Exception {

    Random random = new Random(42); // PKIKeyTool.random;
    short deviceID = (short) pin; // 42;
    // gen pin : 7388 with Random 42
    int newPin = pin; // random.nextInt(pin);
    // gen puk
    int newPuk = random.nextInt(999999);
    // gen mac
    byte[] macKeyForBackup = new byte[PKIKeyTool.MAC_KEY_LENGTH / 8];
    random.nextBytes(macKeyForBackup);

    int sc_id_int = random.nextInt(999999999);
    String sc_id = String.format("%09d", sc_id_int);

    // max_length_256
    URI deviceUri = URI.create("secret://software-smartcard-" + sc_id);


    SmartcardInitializeTool smartcardTool =
        new SmartcardInitializeTool(getSigningKeyPair(), systemParameters, scope);

    CryptoEngine ceList =
        CryptoEngine.valueOf(CryptoTechnology.fromTechnologyURI(
            issuerParametersList.get(0).getAlgorithmID()).toString());
    smartcardTool.setIssuerParameters(ceList, issuerParametersList);
    if (issuerParamWithAttendance != null) {
      CryptoEngine ceCourse =
          CryptoEngine.valueOf(CryptoTechnology.fromTechnologyURI(
              issuerParamWithAttendance.getAlgorithmID()).toString());
      smartcardTool.setIssuerParametersForCounterCredential(ceCourse, issuerParamWithAttendance,
          getCASPublicKey());
    }
    SoftwareSmartcard softwareSmartcard = new SoftwareSmartcard();

    InitializeResult result =
        smartcardTool.initializeSmartcard(softwareSmartcard, newPin, deviceID, deviceUri,
            minAttendance);
    System.out.println("Result of initializeSmartcard : " + result);
    System.out.println("Result of initializeSmartcard : " + newPin);
    System.out.println("Result of initializeSmartcard : "
        + softwareSmartcard.computeScopeExclusivePseudonym(newPin, scope));

    return softwareSmartcard;
  }


}
