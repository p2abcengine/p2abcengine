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

package eu.abc4trust.ri.servicehelper.smartcard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Random;

import com.ibm.zurich.idmix.abc4trust.facades.SmartcardParametersFacade;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.smartcard.RSAKeyPair;
import eu.abc4trust.smartcard.RSASignatureSystem;
import eu.abc4trust.smartcard.RSAVerificationKey;
import eu.abc4trust.smartcard.Smartcard;
import eu.abc4trust.smartcard.SmartcardParameters;
import eu.abc4trust.smartcard.SmartcardStatusCode;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.SmartcardSystemParameters;
import eu.abc4trust.xml.SystemParameters;

public class PKIKeyTool {

  public static boolean TEST_KEYS = false;

  public static final int SIGNING_KEY_LENGTH = 1024;
  public static final int MAC_KEY_LENGTH = 128;

  //
  public static final Random random = new SecureRandom();

  @SuppressWarnings("unused")
  public static void generateSignatureKeys(String signatureKeysFolder, String signatureKeysPrefix)
      throws IOException {
    System.out.println("generateSignatureKeys : " + signatureKeysPrefix);



    RSAKeyPair sk_root;
    if (true) { // DONOT USER TEST KEYS!!! if(!TEST_KEYS) {
      sk_root = RSASignatureSystem.generateSigningKey(SIGNING_KEY_LENGTH / 8);
    } else {
      // if (signatureKeysPrefix.startsWith("pki")) {
      // sk_root = eu.abc4trust.smartcard.RSASignatureSystemTest.getSigningKeyForTest();
      // } else {
      // sk_root = eu.abc4trust.smartcard.RSASignatureSystemTest.getAnotherSigningKeyForTest();
      // }
    }

    RSAVerificationKey pk_root = RSASignatureSystem.getVerificationKey(sk_root);

    // TODO Verify if files exists...
    FileSystem.storeObjectInFile(sk_root, signatureKeysFolder + "/" + signatureKeysPrefix, "_sk");
    FileSystem.storeObjectInFile(pk_root, signatureKeysFolder + "/" + signatureKeysPrefix, "_pk");
  }

  public static URI signIssuerParametersWithAttendance(CryptoEngine engine,
      IssuerParameters issuerParameters, SystemParameters sp, Smartcard ssc, RSAKeyPair sk_root,
      int keyIDForCounter, RSAVerificationKey coursePk, int minimumAttendance, BigInteger q,
      BigInteger p) {
    System.out.println("signIssuerParameters");
    SmartcardParametersFacade spf = new SmartcardParametersFacade(sp, issuerParameters);
    SmartcardParameters credBases;
    try {
      credBases = spf.getSmartcardParameters();
    } catch (CryptoEngineException e) {
      throw new RuntimeException(e);
    }
    ssc.getNewNonceForSignature();

    System.out.println("params URI : " + issuerParameters.getParametersUID());
    URI parametersUri = issuerParameters.getParametersUID();

    SmartcardStatusCode result =
        ssc.addIssuerParametersWithAttendanceCheck(sk_root, parametersUri, keyIDForCounter,
            credBases, coursePk, minimumAttendance);
    System.out.println("RESULT OF ADDING!" + result);
    if (!(result == SmartcardStatusCode.OK)) {
      throw new IllegalStateException("Could not add issuer params..." + result);
    }
    return parametersUri;
  }

  public static URI signIssuerParameters(CryptoEngine engine, IssuerParameters issuerParameters,
      SystemParameters sp, Smartcard ssc, RSAKeyPair sk_root, BigInteger q, BigInteger p) {
    System.out.println("signIssuerParameters");
    SmartcardParametersFacade spf = new SmartcardParametersFacade(sp, issuerParameters);
    SmartcardParameters credBases;
    try {
      credBases = spf.getSmartcardParameters();
    } catch (CryptoEngineException e) {
      throw new RuntimeException(e);
    }

    ssc.getNewNonceForSignature();

    System.out.println("params URI : " + issuerParameters.getParametersUID());
    URI parametersUri = issuerParameters.getParametersUID();

    SmartcardStatusCode result = ssc.addIssuerParameters(sk_root, parametersUri, credBases);
    System.out.println("RESULT OF ADDING! " + result);
    if (!(result == SmartcardStatusCode.OK)) {
      throw new IllegalStateException("Could not add issuer params... " + result);
    }
    return parametersUri;
  }

  // copy from ABCE-COMPONENTS
  public static eu.abc4trust.smartcard.SystemParameters createSmartcardSystemParameters(
      SystemParameters sysParams) {

    SmartcardSystemParameters scSysParams = new SmartcardSystemParameters();

    EcryptSystemParametersWrapper spw = new EcryptSystemParametersWrapper(sysParams);
    BigInteger p, g, subgroupOrder;
    try {
      p = spw.getDHModulus().getValue();
      g = spw.getDHGenerator1().getValue();
      subgroupOrder = spw.getDHSubgroupOrder().getValue();
    } catch (ConfigurationException e1) {
      throw new RuntimeException(e1);
    }
    int zkChallengeSizeBytes = 256 / 8;
    int zkStatisticalHidingSizeBytes = 80 / 8;
    int deviceSecretSizeBytes = 256 / 8;
    int signatureNonceLengthBytes = 128 / 8;
    int zkNonceSizeBytes = 256 / 8;
    int zkNonceOpeningSizeBytes = 256 / 8;

    scSysParams.setPrimeModulus(p);
    scSysParams.setGenerator(g);
    scSysParams.setSubgroupOrder(subgroupOrder);
    scSysParams.setZkChallengeSizeBytes(zkChallengeSizeBytes);
    scSysParams.setZkStatisticalHidingSizeBytes(zkStatisticalHidingSizeBytes);
    scSysParams.setDeviceSecretSizeBytes(deviceSecretSizeBytes);
    scSysParams.setSignatureNonceLengthBytes(signatureNonceLengthBytes);
    scSysParams.setZkNonceSizeBytes(zkNonceSizeBytes);
    scSysParams.setZkNonceOpeningSizeBytes(zkNonceOpeningSizeBytes);
    // return scSysParams;
    return new eu.abc4trust.smartcard.SystemParameters(scSysParams);

  }

  protected static InputStream getInputStream(String resource) throws IOException {
    InputStream is = AbstractHelper.class.getResourceAsStream(resource);
    if (is == null) {
      File f = new File(resource);
      if (!f.exists()) {
        throw new IllegalStateException("Resource not found :  " + resource);
      }

      is = new FileInputStream(f);
    }
    return is;
  }


  public static String toHex(byte[] mac) {
    StringBuilder macStr = new StringBuilder(); // "hex:");
    for (byte element : mac) {
      String hex = String.format("%02x", element);
      // System.out.println("- " + hex + " == " + mac[j]);
      macStr.append(hex);
    }
    return macStr.toString();
  }

}
