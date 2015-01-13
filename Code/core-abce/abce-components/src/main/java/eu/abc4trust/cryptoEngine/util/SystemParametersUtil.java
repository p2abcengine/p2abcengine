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

package eu.abc4trust.cryptoEngine.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.inject.Injector;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;

import eu.abc4trust.TestConfiguration;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SmartcardSystemParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class SystemParametersUtil {

  private static final String SYSTEM_PARAMETERS_1024 = "eu/abc4trust/systemparameters/system_params_1024.xml";
  private static final String SYSTEM_PARAMETERS_2048 = "eu/abc4trust/systemparameters/system_params_2048.xml";

  private static final Logger logger = Logger
      .getLogger(SystemParametersUtil.class.toString());

  private static final ObjectFactory of = new ObjectFactory();

  public static SystemParameters getDefaultSystemParameters_1024(){
    return loadSystemParameters(1024);
  }
  
  public static SystemParameters getDefaultSystemParameters_2048(){
    return loadSystemParameters(2048);
  }
  
  private static SystemParameters loadSystemParameters(int keyLength) {
    switch(keyLength){
      case 1024:
        return getSystemParametersFromResource(SYSTEM_PARAMETERS_1024);
      case 2048:
        return getSystemParametersFromResource(SYSTEM_PARAMETERS_2048);
      default:
        throw new RuntimeException("Test-system only knows system parameters with keylength 1024 and 2048");
    }
  } 

  public static SystemParameters generateSystemParameters(CryptoEngineIssuer cei, int keyLength) {
    try {
      if (TestConfiguration.OVERRIDE_SECURITY_LEVEL) {
        System.err.println("!!! OVERRIDE SECURITY LEVEL " + keyLength
          + " -> 750 because of TestConfiguration.OVERRIDE_SECURITY_LEVEL");
        keyLength = 750;
      }
      SystemParameters sp = cei.setupSystemParameters(keyLength);
      return sp;
    } catch (CryptoEngineException e) {
      throw new RuntimeException(e);
    }
  }
  public static SystemParameters generateSystemParameters(Injector inj, int keyLength) {
    CryptoEngineIssuer cei = inj.getInstance(CryptoEngineIssuer.class);
    return generateSystemParameters(cei, keyLength);
  }

  public static SystemParameters getSystemParametersFromResource(String filename) {
    SystemParameters sp;
    try {
      sp = (SystemParameters) XmlUtils.getObjectFromXML(
        SystemParametersUtil.class.getClassLoader().getResourceAsStream(filename), true);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
    if(sp == null) {
      throw new RuntimeException("Could not find default system parameters");
    }
    return sp;
  }

  public static SystemParameters serialize(
                                           SystemParameters systemParameters) {
    return systemParameters;
  }

  public static SmartcardSystemParameters createSmartcardSystemParameters(
                                                                          SystemParameters sysParams) {


    try {
      EcryptSystemParametersWrapper spw = new EcryptSystemParametersWrapper(sysParams);        
      BigInteger p, g, subgroupOrder;
      try {
        p = spw.getDHModulus().getValue();
        g = spw.getDHGenerator1().getValue();
        subgroupOrder = spw.getDHSubgroupOrder().getValue();
      } catch (ConfigurationException e1) {
        throw new RuntimeException(e1);
      }


      SmartcardSystemParameters scSysParams = new SmartcardSystemParameters();

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
      scSysParams
      .setZkStatisticalHidingSizeBytes(zkStatisticalHidingSizeBytes);
      scSysParams.setDeviceSecretSizeBytes(deviceSecretSizeBytes);
      scSysParams.setSignatureNonceLengthBytes(signatureNonceLengthBytes);
      scSysParams.setZkNonceSizeBytes(zkNonceSizeBytes);
      scSysParams.setZkNonceOpeningSizeBytes(zkNonceOpeningSizeBytes);
      return scSysParams;
    } catch (RuntimeException ex) {
      logger.info("Did you forget to add the system parameters the Idemix Struture store? ("
          + ex.getMessage() + ")");
      throw ex;
    }
  }

    public static String getHashOfSystemParameters(
            SystemParameters systemParameters) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");

            JAXBElement<SystemParameters> element = of
                    .createSystemParameters(systemParameters);
            String xml = XmlUtils.toNormalizedXML(element);
            md.update(xml.getBytes(Charset.forName("UTF-8")));
            byte[] mdbytes = md.digest();

            // convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (byte mdbyte : mdbytes) {
                sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16)
                        .substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
