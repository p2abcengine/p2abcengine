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

// * Licensed Materials - Property of IBM, Miracle A/S, and *
// * Alexandra Instituttet A/S *
// * eu.abc4trust.pabce.1.14 *
// * (C) Copyright IBM Corp. 2012. All Rights Reserved. *
// * (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved. *
// * (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All *
// * Rights Reserved. *
// * US Government Users Restricted Rights - Use, duplication or *
// * disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
// * *
// * This file is licensed under the Apache License, Version 2.0 (the *
// * "License"); you may not use this file except in compliance with *
// * the License. You may obtain a copy of the License at: *
// * http://www.apache.org/licenses/LICENSE-2.0 *
// * Unless required by applicable law or agreed to in writing, *
// * software distributed under the License is distributed on an *
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY *
// * KIND, either express or implied. See the License for the *
// * specific language governing permissions and limitations *
// * under the License. *
// */**/****************************************************************

package eu.abc4trust.ri.servicehelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;

import org.w3c.dom.Element;

import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.guice.configuration.AbceConfigurationImpl;
import eu.abc4trust.guice.configuration.StorageFiles;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymInToken;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public abstract class AbstractHelper extends FileSystem {
  static Logger log = Logger.getLogger(AbstractHelper.class.getName());

  public static int IDEMIX_KEY_LENGTH = 2048; // TODO: define the security level & revocation
  public static final URI IDEMIX_CRYPTO_MECHANISM = URI
      .create("urn:abc4trust:1.0:algorithm:idemix");

  public static int UPROVE_KEY_LENGTH = 2048; // TODO: define the security level & revocation
  public static final URI UPROVE_CRYPTO_MECHANISM = URI
      .create("urn:abc4trust:1.0:algorithm:uprove");
  public static final String UPROVE_GROUP_OID = "1.3.6.1.4.1.311.75.1.1.1";

  public static final String SYSTEM_PARAMS_XML_NAME = "system_params.xml";
  public static final String SYSTEM_PARAMS_OBJ_NAME = "system_params.obj";

  public static final int INSPECTOR_KEY_LENGTH = 1024;
  public static final int REVOCATION_KEY_LENGTH = 1024;

  public static int UPROVE_SERVICE_TIMEOUT = 5;

  // TODO : Find out how to solve UProve Token renewal and FIX!
  public static int UPROVE_ISSUER_NUMBER_OF_CREDENTIAL_TOKENS_TO_GENERATE = 50;

  protected AbceConfigurationImpl setupStorageFilesForConfiguration(String fileStoragePrefix,
      CryptoEngine cryptoEngine) throws Exception {
    return this.setupStorageFilesForConfiguration(fileStoragePrefix, cryptoEngine, false);
  }

  protected AbceConfigurationImpl setupStorageFilesForConfiguration(String fileStoragePrefix,
      CryptoEngine cryptoEngine, boolean wipe_existing_storage) throws Exception {
    StorageFiles storageFiles = verifyFiles(wipe_existing_storage, fileStoragePrefix);
    return this.setupStorageFilesForConfiguration(cryptoEngine, storageFiles);
  }

  protected AbceConfigurationImpl setupStorageFilesForConfiguration(CryptoEngine cryptoEngine,
      StorageFiles storageFiles) throws Exception {
    AbceConfigurationImpl configuration = new AbceConfigurationImpl();
    configuration.setKeyStorageFile(storageFiles.keyStorageFile);
    configuration.setCredentialFile(storageFiles.credentialFile);
    configuration.setPseudonymsFile(storageFiles.pseudonymsFile);
    configuration.setTokensFile(storageFiles.tokensFile);
    configuration.setSecretStorageFile(storageFiles.secretStorageFile);
    Random random = new SecureRandom(); // new Random(1985)
    configuration.setPrng(random);
    configuration.setIssuerSecretKeyFile(storageFiles.issuerSecretKeyFile);
    configuration.setIssuerLogFile(storageFiles.issuerLogFile);
    configuration.setInspectorSecretKeyFile(storageFiles.inspectorSecretKeyFile);
    configuration
        .setRevocationAuthoritySecretStorageFile(storageFiles.revocationAuthoritySecretStorageFile);
    configuration.setRevocationAuthorityStorageFile(storageFiles.revocationAuthorityStorageFile);

    File keyStorageFile = configuration.getKeyStorageFile();
    File fileStorageFolder;
    String filePrefix = "";


    fileStorageFolder = keyStorageFile.getParentFile();

    // check images

    File imagesFolder = new File(fileStorageFolder, "images");
    if (imagesFolder.exists()) {
      if (!imagesFolder.isDirectory()) {
        throw new IllegalStateException("File exists with name for 'images' folder : "
            + imagesFolder.getAbsolutePath());
      }
    } else {
      imagesFolder.mkdir();
    }
    configuration.setImageCacheDir(imagesFolder); // new File(fileStoragePrefix + "images"));
    File defaultImage = new File(imagesFolder, "default.jpg");
    configuration.setDefaultImagePath(defaultImage.getAbsolutePath()); // "default.jpg");
    if (!defaultImage.exists()) {
      InputStream is = FileSystem.getInputStream("/ui/ABC4TrustCredential_default.jpg");
      FileOutputStream fos = new FileOutputStream(defaultImage);
      byte[] b = new byte[1];
      while (is.read(b) != -1) {
        fos.write(b);
      }
      is.close();
      fos.close();
    }
    configuration.setUProveRetryTimeout(4);

    return configuration;
  }

  public static StorageFiles verifyFiles(boolean wipe_existing_storage, String fileStoragePrefix)
      throws Exception {
    StorageFiles storageFiles = new StorageFiles();
    storageFiles.keyStorageFile =
        FileSystem.getFile(fileStoragePrefix + "keystorage", wipe_existing_storage);
    storageFiles.credentialFile =
        FileSystem.getFile(fileStoragePrefix + "credential", wipe_existing_storage);
    storageFiles.pseudonymsFile =
        FileSystem.getFile(fileStoragePrefix + "pseudonyms", wipe_existing_storage);
    storageFiles.tokensFile =
        FileSystem.getFile(fileStoragePrefix + "tokens", wipe_existing_storage);
    storageFiles.secretStorageFile =
        FileSystem.getFile(fileStoragePrefix + "secrets", wipe_existing_storage);
    storageFiles.issuerSecretKeyFile =
        FileSystem.getFile(fileStoragePrefix + "issuerSecretKeys", wipe_existing_storage);
    storageFiles.issuerLogFile =
        FileSystem.getFile(fileStoragePrefix + "issuerLog", wipe_existing_storage);
    storageFiles.inspectorSecretKeyFile =
        FileSystem.getFile(fileStoragePrefix + "inspectorSecrets", wipe_existing_storage);
    storageFiles.revocationAuthoritySecretStorageFile =
        FileSystem.getFile(fileStoragePrefix + "revocationAuthoritySecrets", wipe_existing_storage);
    storageFiles.revocationAuthorityStorageFile =
        FileSystem.getFile(fileStoragePrefix + "revocationAuthorityStorage", wipe_existing_storage);
    return storageFiles;
  }

  //
  // public CryptoEngine cryptoEngine;
  public KeyManager keyManager;
  protected final Set<URI> credSpecSet = new HashSet<URI>();


  protected void setSystemParams(SystemParameters systemParameters) {
    log.info("AbstractHelper setSystemParams with Instance : " + systemParameters);
    if (systemParameters == null) {
      throw new IllegalStateException("systemparameters cannot be null!");
    }
    SystemParameters verifySystemParameters = null;
    try {
      verifySystemParameters = this.keyManager.getSystemParameters();
    } catch (KeyManagerException ignore) {}
    try {
      if (verifySystemParameters == null) {
        log.info(" - add system parameters to keyManager");

        this.keyManager.storeSystemParameters(systemParameters);
      } else {
        log.info(" - system parameters exists!");
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Init Failed", e);
      throw new IllegalStateException("Could not setup issuer ! Error SystemParameters", e);
    }
  }

  protected SystemParameters setSystemParams(String systemParamsResource) {
    log.info("AbstractHelper setSystemParams from resoucres : " + systemParamsResource);
    try {
      if (systemParamsResource == null) {
        throw new IllegalStateException("systemparameters resource not specified!");
      } else {
        SystemParameters systemParameters = null;
        try {
          systemParameters = FileSystem.loadObjectFromResource(systemParamsResource);
        } catch (IOException e) {
          try{
            systemParameters = FileSystem.loadXmlFromResource(systemParamsResource);
          }catch(IOException ex){
            throw new IllegalStateException("systemparameters resource does not exist!", e);
          }
        }
        try {
          SystemParameters exists = keyManager.getSystemParameters();

          if (systemParameters.getSystemParametersUID().equals(exists.getSystemParametersUID())) {
            log.info(" - SystemParameters from resource matches params in keystore : "
                + systemParameters.getSystemParametersUID());
          } else {
            throw new IllegalStateException(
                "SystemParameters from resource does NOT match params in keystore - resource UID : "
                    + systemParameters.getSystemParametersUID() + " keystore - "
                    + exists.getSystemParametersUID());
          }
        } catch (KeyManagerException e) {
          log.info(" - SystemParameters loaded from resource : "
              + systemParameters.getSystemParametersUID());
          this.keyManager.storeSystemParameters(systemParameters);
        }
        return systemParameters;
      }
    } catch (Exception e) {
      System.err.println("Init Failed");
      e.printStackTrace();
      throw new IllegalStateException("Could not setup issuer ! Error SystemParameters", e);
    }
  }

  protected void addIssuerParameters(String[] issuerParamsResourceList) {
    log.info("AbstractHelper addIssuerParameters from resoucres : "
        + Arrays.toString(issuerParamsResourceList));
    try {
      SystemParameters systemParameters = keyManager.getSystemParameters();
      for (String resource : issuerParamsResourceList) {
        IssuerParameters issuerParameters = FileSystem.loadObjectFromResource(resource);

        localAddIssuerParameters(systemParameters, issuerParameters);

      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Init Failed", e);
      throw new IllegalStateException("Could not setup issuer ! Error Adding IssuerParameters", e);
    }
  }

  protected void addIssuerParameters(List<IssuerParameters> issuerParameters) {
    try {
      SystemParameters systemParameters = keyManager.getSystemParameters();
      if(issuerParameters==null) {
        return;
      }
      for (IssuerParameters ip : issuerParameters) {
        localAddIssuerParameters(systemParameters, ip);
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Init Failed", e);
      throw new IllegalStateException("Could not setup issuer ! Error Adding IssuerParameters", e);
    }
  }

  private void localAddIssuerParameters(SystemParameters systemParameters,
      IssuerParameters issuerParameters) throws Exception {
    IssuerParameters exists =
        this.keyManager.getIssuerParameters(issuerParameters.getParametersUID());
    if (exists != null) {
      if (issuerParameters.getVersion().equals(exists.getVersion())) {
        log.info(" - issuer parameters present in storager with version number : "
            + issuerParameters.getVersion());
      } else {
        log.warning("WARNING ! : issuerparameter mismatch! " + issuerParameters.getParametersUID()
            + " - in storage : " + exists.getVersion() + " - version in resource : "
            + issuerParameters.getVersion());
      }
    } else {
      if (issuerParameters.getSystemParametersUID().equals(
          systemParameters.getSystemParametersUID())) {
        this.keyManager
            .storeIssuerParameters(issuerParameters.getParametersUID(), issuerParameters);
      } else {
        throw new Exception(
            "IssuerParemeters uses wrong SystemParameters - IP SystemParametersUID : "
                + issuerParameters.getSystemParametersUID() + " - SP SystemParametersUID "
                + systemParameters.getSystemParametersUID());
      }

    }
  }

  protected void addCredentialSpecifications(List<CredentialSpecification> credSpecResourceList) {
    log.info("AbstractHelper addCredentialSpecification  : " + credSpecResourceList);
    if(credSpecResourceList==null) {
      return;
    }
    try {
      for (CredentialSpecification credSpec : credSpecResourceList) {
        CredentialSpecification existInStore =
            this.keyManager.getCredentialSpecification(credSpec.getSpecificationUID());
        if (existInStore == null) {
          this.keyManager.storeCredentialSpecification(credSpec.getSpecificationUID(), credSpec);

          log.info(" - credspec added : " + credSpec.getSpecificationUID());
        } else {
          log.info(" - credspec already in store : " + credSpec.getSpecificationUID());
        }
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Init Failed", e);
      throw new IllegalStateException(
          "Could not setup helper ! Error reading CredentialSpecifications", e);
    }

  }

  protected void addCredentialSpecifications(String[] credSpecResourceList) {
    log.info("AbstractHelper addCredentialSpecification from resources : " + credSpecResourceList);
    try {

      for (String resource : credSpecResourceList) {
        InputStream is = FileSystem.getInputStream(resource);
        if (is == null) {
          throw new IllegalStateException("CredSpec resource not found :  " + resource);
        }
        CredentialSpecification credSpec =
            (CredentialSpecification) XmlUtils.getObjectFromXML(is, true);

        CredentialSpecification existInStore =
            this.keyManager.getCredentialSpecification(credSpec.getSpecificationUID());
        if (existInStore == null) {
          this.keyManager.storeCredentialSpecification(credSpec.getSpecificationUID(), credSpec);

          log.info(" - credspec added : " + credSpec.getSpecificationUID());
        } else {
          log.info(" - credspec already in store : " + credSpec.getSpecificationUID());
        }
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Init Failed", e);
      throw new IllegalStateException(
          "Could not setup helper ! Error reading CredentialSpecifications", e);
    }
  }

  protected void XXXaddSystemParameters(String resource) {
    try {
      SystemParametersHelper.checkAndLoadSystemParametersIfAbsent(keyManager, resource);
    } catch (KeyManagerException e) {
      throw new IllegalStateException("Could not setup helper ! Error reading system parameters", e);
    }
  }

  protected void XXXaddInspectorPublicKeys(String[] inspectorPublicKeyResourceList) {
    log.info("AbstractHelper addInspectorPublicKeys from resouces : "
        + inspectorPublicKeyResourceList);
    try {
      for (String resource : inspectorPublicKeyResourceList) {
        log.info(" - read inspector public key" + resource);

        InspectorPublicKey ipk = FileSystem.loadObjectFromResource(resource);
        log.info("- loaded Inspector Public Key - with UID : " + ipk.getPublicKeyUID());

        localAddInspectorPublicKey(ipk);

      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Init Failed", e);
      throw new IllegalStateException("Could not setup helper ! Error reading InspectorPublicKeys",
          e);
    }
  }

  protected void addInspectorPublicKeys(List<InspectorPublicKey> inspectorPublicKeyList) {
    log.info("AbstractHelper addInspectorPublicKeys : " + inspectorPublicKeyList);
    if(inspectorPublicKeyList==null) {
      return;
    }
    try {
      for (InspectorPublicKey ipk : inspectorPublicKeyList) {
        localAddInspectorPublicKey(ipk);
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Init Failed", e);
      throw new IllegalStateException("Could not setup helper ! Error reading InspectorPublicKeys",
          e);
    }
  }

  private void localAddInspectorPublicKey(InspectorPublicKey ipk) throws KeyManagerException {
    InspectorPublicKey verifyPk = this.keyManager.getInspectorPublicKey(ipk.getPublicKeyUID());
    if (verifyPk != null) {
      log.info("- InspectorPublicKey already in keyManager " + verifyPk.getPublicKeyUID());
    } else {
      log.info("- add InspectorPublicKey to keyManager: "+ipk.getPublicKeyUID());
      this.keyManager.storeInspectorPublicKey(ipk.getPublicKeyUID(), ipk);
    }
  }


  protected void addRevocationAuthorities(KeyManager engineKeyManager,
      String[] revocationAuthorityParametersResourcesList) {
    log.info("AbstractHelper addRevocationAuthorities from resouces : "
        + revocationAuthorityParametersResourcesList + " - list length: "
        + revocationAuthorityParametersResourcesList.length);
    try {
      for (String resource : revocationAuthorityParametersResourcesList) {
        log.info(" - read revocationAuthorityParameters" + resource);

        RevocationAuthorityParameters rap = FileSystem.loadObjectFromResource(resource);

        localAddRevocationAuthorities(engineKeyManager, rap);
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Init Failed", e);
      throw new IllegalStateException(
          "Could not setup helper ! Error reading RevocationAuthorityParameters", e);
    }
  }

  protected void addRevocationAuthorities(KeyManager engineKeyManager,
      List<RevocationAuthorityParameters> revocationAuthorityParametersList) {
    log.info("AbstractHelper addRevocationAuthorities : " + revocationAuthorityParametersList);
    if(revocationAuthorityParametersList==null) {
      return;
    }
    try {
      for (RevocationAuthorityParameters rap : revocationAuthorityParametersList) {
        localAddRevocationAuthorities(engineKeyManager, rap);

      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Init Failed", e);
      throw new IllegalStateException(
          "Could not setup helper ! Error reading RevocationAuthorityParameters", e);
    }
  }

  private void localAddRevocationAuthorities(KeyManager engineKeyManager,
      RevocationAuthorityParameters rap) throws KeyManagerException {
    URI revAuthParamsUid = rap.getParametersUID();
    log.info("- loaded revocationAuthorityParameters - with UID : " + revAuthParamsUid);

    RevocationAuthorityParameters verifyRap =
        engineKeyManager.getRevocationAuthorityParameters(revAuthParamsUid);
    if (verifyRap != null) {
      log.info("- revocationAuthorityParameters in keyManager " + verifyRap);
    } else {
      log.info("- add key to keyManager");
      engineKeyManager.storeRevocationAuthorityParameters(revAuthParamsUid, rap);
    }
  }

  public static BigInteger getPseudonymValue(PresentationToken presentationToken, URI scope) {
    return getPseudonymValue(presentationToken, scope.toString());
  }

  public static BigInteger getPseudonymValue(PresentationToken presentationToken, String scope) {
    PseudonymInToken pse =
        findPseudonym(presentationToken.getPresentationTokenDescription(), scope);
    if (pse != null) {
      byte[] pse_from_presentation_bytes = pse.getPseudonymValue();
      BigInteger pse_from_presentation = new BigInteger(pse_from_presentation_bytes);
      return pse_from_presentation;
    } else {
      return null;
    }
  }


  /**
   * Note! Uses OLD CryptoEngine - to provide BackWard Compatibility!
   * 
   * @param presentationToken
   * @param scope
   * @return
   */
  public static CryptoEngine getCryptoEngineForPseudonym(PresentationToken presentationToken,
      URI scope) {
    return getCryptoEngineForPseudonym(presentationToken, scope.toString());
  }

  /**
   * Note! Uses OLD CryptoEngine - to provide BackWard Compatibility!
   * 
   * @param presentationToken
   * @param scope
   * @return
   */
  public static CryptoEngine getCryptoEngineForPseudonym(PresentationToken presentationToken,
      String scope) {
    PseudonymInToken pse =
        findPseudonym(presentationToken.getPresentationTokenDescription(), scope);
    if (pse != null) {
      CryptoParams cryptoEvidence = presentationToken.getCryptoEvidence();
      XmlUtils.fixNestedContent(cryptoEvidence);
      for (Object o : cryptoEvidence.getContent()) {
        if (o instanceof Element) {
          Element element = (Element) o;
          String elementName =
              element.getLocalName() != null ? element.getLocalName() : element.getNodeName();
          if ("IdmxProof".equals(elementName)) {
            return CryptoEngine.IDEMIX;
          }
          if ("UProvePseudonym".equals(elementName)) {
            return CryptoEngine.UPROVE;
          }
        }
      }
      // ?? illegal state ???
      return null;
    } else {
      return null;
    }
  }

  private static PseudonymInToken findPseudonym(
      PresentationTokenDescription presentationTokenDescription, String scope) {
    for (PseudonymInToken pse : presentationTokenDescription.getPseudonym()) {
      // Verify Pseudonym is correct scope
      if (scope.equals(pse.getScope().toString())) {
        return pse;
      }
    }
    return null;
  }

  public static BigInteger getPseudonymValue(IssuanceMessage issuanceMessage, URI scope) {
    return getPseudonymValue(issuanceMessage, scope.toString());
  }

  public static BigInteger getPseudonymValue(IssuanceMessage issuanceMessage, String scope) {
    if (issuanceMessage != null) {
      for (Object o : issuanceMessage.getContent()) {
        if ((o instanceof JAXBElement<?>)
            && (((JAXBElement<?>) o).getValue() instanceof IssuanceToken)) {
          IssuanceToken it = (IssuanceToken) ((JAXBElement<?>) o).getValue();
          PseudonymInToken pse =
              findPseudonym(it.getIssuanceTokenDescription().getPresentationTokenDescription(),
                  scope);
          if (pse != null) {
            byte[] pse_from_presentation_bytes = pse.getPseudonymValue(); // it.getPresentationTokenDescription().getPseudonym().get(0).getPseudonymValue();
            BigInteger pse_from_presentation = new BigInteger(pse_from_presentation_bytes);
            return pse_from_presentation;
          } else {
            return null;
          }
        }
      }
    }
    return null;
  }

}
