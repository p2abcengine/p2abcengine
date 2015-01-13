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

package eu.abc4trust.ri.servicehelper.revocation;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.zurich.idmix.abc4trust.XmlUtils;

import eu.abc4trust.abce.external.revocation.RevocationAbcEngine;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.guice.configuration.AbceConfigurationImpl;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;

public class RevocationHelper extends AbstractHelper {

  private static final Logger log = Logger.getLogger(RevocationHelper.class.toString());

  public static class RevocationReferences {
    public final URI revocationAuthorityUID;
    public final URI revocationInfoServiceURI;
    public final URI nonRevocationEvidenceServiceURI;
    public final URI nonRevocationUpdateServiceURI;

    public RevocationReferences(final URI revocationAuthorityUID,
        final URI revocationInfoServiceURI, final URI nonRevocationEvidenceServiceURI,
        final URI nonRevocationUpdateServiceURI) {
      this.revocationAuthorityUID = revocationAuthorityUID;
      this.revocationInfoServiceURI = revocationInfoServiceURI;
      this.nonRevocationEvidenceServiceURI = nonRevocationEvidenceServiceURI;
      this.nonRevocationUpdateServiceURI = nonRevocationUpdateServiceURI;
    }

    public RevocationReferences(final String revocationAuthority,
        final String revocationInfoService, final String nonRevocationEvidenceService,
        final String nonRevocationUpdateService) {
      this.revocationAuthorityUID = URI.create(revocationAuthority);
      this.revocationInfoServiceURI = URI.create(revocationInfoService);
      this.nonRevocationEvidenceServiceURI = URI.create(nonRevocationEvidenceService);
      this.nonRevocationUpdateServiceURI = URI.create(nonRevocationUpdateService);
    }

    public Reference getRevocationInfoReference() {
      Reference revocationInfoReference = new Reference();
      revocationInfoReference
          .setReferenceType(URI.create(this.revocationInfoServiceURI.getScheme()));
      revocationInfoReference.getReferences().add(this.revocationInfoServiceURI);
      return revocationInfoReference;
    }

    public Reference getNonRevocationEvidenceReference() {
      Reference nonRevocationEvidenceReference = new Reference();
      nonRevocationEvidenceReference.setReferenceType(URI
          .create(this.nonRevocationEvidenceServiceURI.getScheme()));
      nonRevocationEvidenceReference.getReferences().add(this.nonRevocationEvidenceServiceURI);
      return nonRevocationEvidenceReference;
    }

    public Reference getNonRevocationUpdateReference() {
      Reference nonRevocationUpdateReference = new Reference();
      nonRevocationUpdateReference.setReferenceType(URI.create(this.nonRevocationUpdateServiceURI
          .getScheme()));
      nonRevocationUpdateReference.getReferences().add(this.nonRevocationUpdateServiceURI);
      return nonRevocationUpdateReference;
    }
  }


  private static RevocationHelper instance;

  /**
   * @return true if IssuanceHelper has been initialized
   */
  public static synchronized boolean isInit() {
    return instance != null;
  }

  /**
   * Only used in test - can reset static instance
   */
  public static synchronized void resetInstance() {
    log.warning("RevocationHelper.resetInstance : " + instance);
    instance = null;
  }

  /**
   * @return initialized instance of IssuanceHelper
   */
  public static synchronized RevocationHelper getInstance() {
    log.info("RevocationHelper.getInstance : " + instance
        + (instance == null ? "" : " : " + instance));
    if (instance == null) {
      throw new IllegalStateException("getInstance not called before using IssuanceHelper!");
    }
    return instance;
  }

  /**
   * @param cryptoEngine
   * @param revocationStoragePrefix - private storage files (private keys) will be stored here
   * @param revocationResourcesPrefix - public keys will be exported here
   * @param systemParametersResource - the system parameter
   * @param revocationReferences ...
   * @return
   * @throws Exception
   */
  public static synchronized RevocationHelper initInstance(String revocationStoragePrefix,
      String revocationResourcesPrefix, SystemParameters systemParameters,
      RevocationReferences... revocationReferences) throws Exception {
    if (instance != null) {
      throw new IllegalStateException("initInstance can only be called once!");
    }
    log.info("RevocationHelper.initInstance");

    instance = new RevocationHelper(revocationStoragePrefix, revocationResourcesPrefix);
    instance.setSystemParams(systemParameters);

    instance.setupRevocationReferences(revocationReferences, revocationResourcesPrefix);
    //
    return instance;
  }

  /**
   * @param cryptoEngine
   * @param revocationStoragePrefix - private storage files (private keys) will be stored here
   * @param revocationResourcesPrefix - public keys will be exported here
   * @param systemParametersResource - the system parameter
   * @param issuerParamsResourceList - issuer params
   * @param credSpecResourceList - supported credSpecs
   * @param revocationReferences ...
   * @return
   * @throws Exception
   */
  public static synchronized RevocationHelper initInstance(String revocationStoragePrefix,
      String revocationResourcesPrefix, SystemParameters systemParameters,
      List<IssuerParameters> issuerParamsList, List<CredentialSpecification> credSpecList,
      RevocationReferences... revocationReferences) throws Exception {
    if (instance != null) {
      throw new IllegalStateException("initInstance can only be called once!");
    }
    log.info("RevocationHelper.initInstance");

    instance = new RevocationHelper(revocationStoragePrefix, revocationResourcesPrefix);
    instance.addCredentialSpecifications(credSpecList);
    instance.setSystemParams(systemParameters);
    instance.addIssuerParameters(issuerParamsList);

    instance.setupRevocationReferences(revocationReferences, revocationResourcesPrefix);
    //
    return instance;
  }

  public static synchronized RevocationHelper XXXinitInstancXe(String revocationStoragePrefix,
      String[] issuerParamsResourceList, String[] credSpecResourceList,
      String systemParametersResource, String[] revocationAuthorityResourceList) throws Exception {
    if (instance != null) {
      throw new IllegalStateException("initInstance can only be called once!");
    }
    log.info("RevocationHelper.initInstance(for service)");

    instance = new RevocationHelper(revocationStoragePrefix, revocationStoragePrefix);
    instance.addCredentialSpecifications(credSpecResourceList);
    instance.setSystemParams(systemParametersResource);
    instance.addIssuerParameters(issuerParamsResourceList);

    if (revocationAuthorityResourceList.length != 0) {
      for (String resource : revocationAuthorityResourceList) {
        try {
          RevocationAuthorityParameters revocationAuthorityParameters =
              FileSystem.loadObjectFromResource(resource);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    return instance;
  }

  //
  public RevocationAbcEngine engine;
  public RevocationProxyAuthority revocationProxyAuthority;

  /**
   * Private constructor
   * 
   * @param fileStoragePrefix this prefix will be prepended on storage files needed by the
   *        IssuerAbcEnginge
   * @param specAndPolicyList list of CredentialSpecifications + IssuancePolices
   * @throws URISyntaxException
   */
  private RevocationHelper(String revocationStoragePrefix, String revocationResourcesPrefix)
      throws URISyntaxException {
    log.info("RevocationHelper : create instance - storage prefix : " + revocationStoragePrefix
        + " - resouces prefix : " + revocationResourcesPrefix);
    try {
      // this.cryptoEngine = cryptoEngine;

      AbceConfigurationImpl configuration =
          this.setupStorageFilesForConfiguration(revocationStoragePrefix, CryptoEngine.IDEMIX);


      Injector injector =
          Guice.createInjector(ProductionModuleFactory
              .newModule(configuration, CryptoEngine.IDEMIX));

      this.engine = injector.getInstance(RevocationAbcEngine.class);

      this.revocationProxyAuthority = injector.getInstance(RevocationProxyAuthority.class);

      this.keyManager = injector.getInstance(KeyManager.class);
      // this.credentialManager = injector.getInstance(CredentialManager.class);
      log.fine("keymanager : " + this.keyManager);
    } catch (Exception e) {
      log.log(Level.SEVERE, "Init Failed", e);
      throw new IllegalStateException("Could not setup issuer !", e);
    }
  }


  public static synchronized RevocationAuthorityParameters setupParameters(
      URI revocationTechnology, int keyLength, URI uid, Reference revocationInfoReference,
      Reference nonRevocationEvidenceReference, Reference nonRevocationUpdateReference,
      String revocationResourcesPrefix) throws CryptoEngineException {


    log.warning("setupRevocationAuthorityParameters - before 2 : " + uid
        + " - revocationTechnology : " + revocationTechnology);
    log.warning("setupRevocationAuthorityParameters - before 2 : " + revocationInfoReference);
    log.warning("setupRevocationAuthorityParameters - before 2 : " + nonRevocationEvidenceReference);
    log.warning("setupRevocationAuthorityParameters - before 2 : " + nonRevocationUpdateReference);

    RevocationAuthorityParameters revocationAuthorityParameters =
        instance.engine.setupRevocationAuthorityParameters(keyLength, revocationTechnology, uid,
            revocationInfoReference, nonRevocationEvidenceReference, nonRevocationUpdateReference);


    boolean urnScheme = "urn".equals(uid.getScheme());
    String revocation_authority_filename = "revocation_authority_";
    if (urnScheme) {
      revocation_authority_filename += uid.toASCIIString().replaceAll(":", "_");
    } else {
      revocation_authority_filename +=
          uid.getHost().replace(".", "_") + uid.getPath().replace("/", "_");
    }
    revocation_authority_filename += ".xml";

    try {
      if (!new File(revocationResourcesPrefix + revocation_authority_filename).exists()) {
        JAXBElement<RevocationAuthorityParameters> revocationAuthorityParametersJaxb = new ObjectFactory().createRevocationAuthorityParameters(revocationAuthorityParameters);
        FileSystem.storeObjectAsXMLInFile(revocationAuthorityParametersJaxb, revocationResourcesPrefix,
            revocation_authority_filename);
      }
    } catch (Exception e) {
      throw new IllegalStateException("Failed to store RevocationAuthorityParameters : " + uid
          + " : " + revocationResourcesPrefix + revocation_authority_filename, e);
    }



    return revocationAuthorityParameters;
  }

  private void setupRevocationReferences(RevocationReferences[] revocationReferences,
      String revocationResourcesPrefix) {

    for (RevocationReferences r : revocationReferences) {
      RevocationAuthorityParameters revocationAuthorityParameters;
      try {
        revocationAuthorityParameters =
            this.keyManager.getRevocationAuthorityParameters(r.revocationAuthorityUID);
      } catch (KeyManagerException e) {
        throw new IllegalStateException(
            "Failed to get RevocationAuthorityParameters from KeyStore : "
                + r.revocationAuthorityUID, e);
      }
      if (revocationAuthorityParameters != null) {
        log.info("RevocationAuthorityParameters already exists for : " + r.revocationAuthorityUID);
      } else {
        URI revocationTechnology = RevocationTechnology.IDEMIX.getURI();
        log.info("Initialize RevocationAuthorityParameters for : " + r.revocationAuthorityUID
            + " - revocationTechnology " + revocationTechnology);
        try {
          revocationAuthorityParameters =
              this.engine.setupRevocationAuthorityParameters(1024,
                  RevocationTechnology.IDEMIX.getURI(), r.revocationAuthorityUID,
                  r.getRevocationInfoReference(), r.getNonRevocationEvidenceReference(),
                  r.getNonRevocationUpdateReference());
          if (revocationAuthorityParameters.getVersion() == null) {
            // Version not set... Set to 1.0
            revocationAuthorityParameters.setVersion("1.0");
          }
//          JAXBElement<?> j =
//              (JAXBElement<?>) revocationAuthorityParameters.getCryptoParams().getContent().get(0);
          XmlUtils.fixNestedContent(revocationAuthorityParameters.getCryptoParams());
          PublicKey j = (PublicKey) revocationAuthorityParameters.getCryptoParams().getContent().get(0);
//          if (j.getValue() instanceof PublicKey) {
//            PublicKey pk = (PublicKey) j.getValue();
            j.setVersion("1.0");
//          }
        } catch (CryptoEngineException e) {
          throw new IllegalStateException("Failed to setup RevocationAuthorityParameters : "
              + r.revocationAuthorityUID, e);
        } catch (Exception e) {
          throw new IllegalStateException("Failed to setup RevocationAuthorityParameters : "
              + r.revocationAuthorityUID, e);
        }
      }
      boolean urnScheme = "urn".equals(r.revocationAuthorityUID.getScheme());
      String revocation_authority_filename = "revocation_authority_";
      if (urnScheme) {
        revocation_authority_filename +=
            r.revocationAuthorityUID.toASCIIString().replaceAll(":", "_");
      } else {
        revocation_authority_filename +=
            r.revocationAuthorityUID.getHost().replace(".", "_")
                + r.revocationAuthorityUID.getPath().replace("/", "_");
      }
      revocation_authority_filename += ".xml";

      try {
        if (!new File(revocationResourcesPrefix + revocation_authority_filename).exists()) {
          JAXBElement<RevocationAuthorityParameters> revocationAuthorityParametersJaxb = new ObjectFactory().createRevocationAuthorityParameters(revocationAuthorityParameters);
          FileSystem.storeObjectAsXMLInFile(revocationAuthorityParametersJaxb, revocationResourcesPrefix,
              revocation_authority_filename);
        }
      } catch (Exception e) {
        throw new IllegalStateException("Failed to store RevocationAuthorityParameters : "
            + r.revocationAuthorityUID + " : " + revocationResourcesPrefix
            + revocation_authority_filename, e);
      }

    }
  }



}
