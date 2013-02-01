//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.ri.servicehelper.issuer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBElement;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenStorageIssuer;
import eu.abc4trust.cryptoEngine.uprove.util.UProveBindingManager;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.guice.ProductionModule;
import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.guice.configuration.AbceConfigurationImpl;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.returnTypes.IssuanceMessageAndBoolean;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper.SpecAndPolicy;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class IssuanceHelper extends AbstractHelper {


  /**
   * Helper class. Holds response From IssuerABC - marshaled to String
   */
  public static class IssuanceResponse_String {
    public final String xml;
    public final boolean lastmessage;
    public final String context;

    public IssuanceResponse_String(String xml, boolean lastmessage) {
      this.xml = xml;
      this.lastmessage = lastmessage;
      this.context = null;
    }

    public IssuanceResponse_String(String xml, boolean lastmessage, String context) {
      this.xml = xml;
      this.lastmessage = lastmessage;
      this.context = context;
    }

    public IssuanceResponse_String(String xml, String context) {
      this.xml = xml;
      this.lastmessage = false;
      this.context = context;
    }
  }

  /**
   * Helper class. Holds CredentialsSpecification + matching IssuancePolicy
   */
  public static class SpecAndPolicy {
    public final String key;
    public final String specResource;
    public final String policyResource;

    public final String issuerParamsUid;
    public final String revocationParamsUid;

    public URI issuerParamsUid_URI;
    public URI revocationParamsUid_URI;

    public SpecAndPolicy(String key, String specResource, String policyResource) {
      this.key = key;
      this.specResource = specResource;
      this.policyResource = policyResource;
      this.issuerParamsUid = null;
      this.revocationParamsUid = null;
    }

    public SpecAndPolicy(String key, String specResource, String policyResource,
        String issuerParamsUid, String revocationParamsUid) {
      this.key = key;
      this.specResource = specResource;
      this.policyResource = policyResource;
      this.issuerParamsUid = issuerParamsUid;
      this.revocationParamsUid = revocationParamsUid;
    }

    public SpecAndPolicy(SpecAndPolicy cloneThisSap) {
      this.key = cloneThisSap.key;
      this.specResource = cloneThisSap.specResource;
      this.policyResource = cloneThisSap.policyResource;
      this.issuerParamsUid = cloneThisSap.issuerParamsUid;
      this.revocationParamsUid = cloneThisSap.revocationParamsUid;
      this.issuanceBytes = cloneThisSap.issuanceBytes;
    }

    private CredentialSpecification credentialSpecification;
    private IssuancePolicy issuancePolicy;
    private byte[] issuanceBytes;

    public CredentialSpecification getCredentialSpecification() {
      return this.credentialSpecification;
    }

    public void setCredentialSpecification(CredentialSpecification credentialSpecification) {
      this.credentialSpecification = credentialSpecification;
    }

    public IssuancePolicy getIssuancePolicy() {
      return this.issuancePolicy;
    }

    public void setIssuancePolicy(IssuancePolicy issuancePolicy) {
      this.issuancePolicy = issuancePolicy;
    }

    public void setIssuancePolicyBytes(byte[] bytes) {
      issuanceBytes = bytes;
    }

    public IssuancePolicy cloneIssuancePolicy() throws Exception {
      JAXBElement<?> clone = XmlUtils.getJaxbElementFromXml(new ByteArrayInputStream(issuanceBytes), true);
      IssuancePolicy cloneValue = (IssuancePolicy) clone.getValue();
      cloneValue.getCredentialTemplate().setIssuerParametersUID(issuerParamsUid_URI);
      return cloneValue;
    }

  }


  private static IssuanceHelper instance;

  /**
   * @param fileStoragePrefix this prefix will be prepended on storage files needed by the
   *        IssuerAbcEnginge
   * @param specAndPolicyList list of CredentialSpecifications + IssuancePolices
   * @return
   * @throws URISyntaxException
   */
  public static synchronized IssuanceHelper initInstance(ProductionModule.CryptoEngine cryptoEngine,
      String systemAndIssuerParamsPrefix, String fileStoragePrefix,
      ArrayList<SpecAndPolicy> specAndPolicyList) throws URISyntaxException {
    if (instance != null) {
      throw new IllegalStateException("initInstance can only be called once!");
    }
    System.out.println("IssuanceHelper.initInstance(ArrayList)");
    instance =
        new IssuanceHelper(oldCryptoEngineToNewCryptoEngine(cryptoEngine), systemAndIssuerParamsPrefix, fileStoragePrefix, new String[0], 
            specAndPolicyList);

    return instance;
  }

  /**
   * @param fileStoragePrefix this prefix will be prepended on storage files needed by the
   *        IssuerAbcEnginge
   * @param specAndPolicyList list of CredentialSpecifications + IssuancePolices
   * @return
   * @throws URISyntaxException
   */
  public static synchronized IssuanceHelper initInstance(CryptoEngine cryptoEngine,
      String systemAndIssuerParamsPrefix, String fileStoragePrefix,
      ArrayList<SpecAndPolicy> specAndPolicyList) throws URISyntaxException {
    if (instance != null) {
      throw new IllegalStateException("initInstance can only be called once!");
    }
    System.out.println("IssuanceHelper.initInstance(ArrayList)");
    instance =
        new IssuanceHelper(cryptoEngine, systemAndIssuerParamsPrefix, fileStoragePrefix, new String[0],
            specAndPolicyList);

    return instance;
  }

  /**
   * @param fileStoragePrefix this prefix will be prepended on storage files needed by the
   *        IssuerAbcEnginge
   * @param specAndPolicyList list of CredentialSpecifications + IssuancePolices
   * @return
   * @throws URISyntaxException
   */
  public static synchronized IssuanceHelper initInstance(CryptoEngine cryptoEngine,
      String systemAndIssuerParamsPrefix, String fileStoragePrefix,
      SpecAndPolicy... specAndPolicyList) throws URISyntaxException {
    if (instance != null) {
      throw new IllegalStateException("initInstance can only be called once!");
    }
    System.out.println("IssuanceHelper.initInstance(Array)");

    ArrayList<SpecAndPolicy> list = new ArrayList<SpecAndPolicy>();
    for (SpecAndPolicy sap : specAndPolicyList) {
      list.add(sap);
    }
    instance =
        new IssuanceHelper(cryptoEngine, systemAndIssuerParamsPrefix, fileStoragePrefix, new String[0], list);

    return instance;
  }

  

  /**
   * @param fileStoragePrefix this prefix will be prepended on storage files needed by the
   *        IssuerAbcEnginge
   * @param specAndPolicyList list of CredentialSpecifications + IssuancePolices
   * @return
   * @throws URISyntaxException
   */
  public static synchronized IssuanceHelper initInstance(ProductionModule.CryptoEngine cryptoEngine,
      String systemAndIssuerParamsPrefix, String fileStoragePrefix,
      SpecAndPolicy... specAndPolicyList) throws URISyntaxException {
    if (instance != null) {
      throw new IllegalStateException("initInstance can only be called once!");
    }
    System.out.println("IssuanceHelper.initInstance(Array)");

    ArrayList<SpecAndPolicy> list = new ArrayList<SpecAndPolicy>();
    for (SpecAndPolicy sap : specAndPolicyList) {
      list.add(sap);
    }
    instance =
        new IssuanceHelper(oldCryptoEngineToNewCryptoEngine(cryptoEngine), systemAndIssuerParamsPrefix, fileStoragePrefix, new String[0], list);

    return instance;
  }

  public static synchronized IssuanceHelper initInstance(ProductionModule.CryptoEngine cryptoEngine, String systemAndIssuerParamsPrefix, String fileStoragePrefix, 
                                                         SpecAndPolicy[] specAndPolicyList, String[] revocationAuthorityParametersResourcesList) throws URISyntaxException {
    return initInstance(oldCryptoEngineToNewCryptoEngine(cryptoEngine), systemAndIssuerParamsPrefix, fileStoragePrefix, specAndPolicyList, revocationAuthorityParametersResourcesList);
  }
  
  public static synchronized IssuanceHelper initInstance(CryptoEngine cryptoEngine, String systemAndIssuerParamsPrefix, String fileStoragePrefix, 
      SpecAndPolicy[] specAndPolicyList, String[] revocationAuthorityParametersResourcesList) throws URISyntaxException {
    
    if (instance != null) {
      throw new IllegalStateException("initInstance can only be called once!");
    }
    System.out.println("IssuanceHelper.initInstance(Array)");

    ArrayList<SpecAndPolicy> list = new ArrayList<SpecAndPolicy>();
    for (SpecAndPolicy sap : specAndPolicyList) {
      list.add(sap);
    }
    instance =
        new IssuanceHelper(cryptoEngine, systemAndIssuerParamsPrefix, fileStoragePrefix, revocationAuthorityParametersResourcesList, list);

    
    return instance;
  }


  
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
    System.err.println("WARNING IssuanceHelper.resetInstance : " + instance);
    if(instance!=null && (instance.uproveBindingManager != null) ) {
      try {
          // try to stop uprove engine/service if running...
          instance.uproveBindingManager.stop();
      } catch(Exception ignore) {
          System.err.println("Failed to stop UProve service : " + ignore);
      }

    }
    instance = null;
  }

  /**
   * @return initialized instance of IssuanceHelper
   */
  public static synchronized IssuanceHelper getInstance() {
    System.out.println("IssuanceHelper.getInstance : " + instance
        + (instance == null ? "" : " : " + instance.cryptoEngine));
    if (instance == null) {
      throw new IllegalStateException("getInstance not called before using IssuanceHelper!");
    }
    return instance;
  }

  private IssuerAbcEngine singleEngine = null;
  private IssuerAbcEngine uproveEngine = null;
  private IssuerAbcEngine idemixEngine = null;

  // needed for 'reset'
  private UProveBindingManager uproveBindingManager = null;
  
  private final Map<String, SpecAndPolicy> specAndPolicyMap = new HashMap<String, SpecAndPolicy>();
  
  private Random random;

  private final ObjectFactory of = new ObjectFactory();

  private List<TokenStorageIssuer> issuerStorageManagerList = new ArrayList<TokenStorageIssuer>();

  /**
   * Private constructor
   * 
   * @param fileStoragePrefix this prefix will be prepended on storage files needed by the
   *        IssuerAbcEnginge
   * @param specAndPolicyList list of CredentialSpecifications + IssuancePolices
   * @throws URISyntaxException
   */
  private IssuanceHelper(CryptoEngine cryptoEngine, String systemAndIssuerParamsPrefix,
      String fileStoragePrefix, String[] revocationAuthorityParametersResourcesList, ArrayList<SpecAndPolicy> specAndPolicyList)
      throws URISyntaxException {
    System.out.println("IssuanceHelper : create instance " + cryptoEngine + " : "
        + fileStoragePrefix + " : " + specAndPolicyList);
    this.cryptoEngine = cryptoEngine;
    try {
      //keyManager = injector.getInstance(KeyManager.class);
//      System.out.println("keymanager : " + keyManager);

      //
      //issuerStorageManager = injector.getInstance(TokenStorageIssuer.class);

      
      //this.engine = injector.getInstance(IssuerAbcEngine.class);
      UProveUtils uproveUtils = new UProveUtils();
      
      switch (cryptoEngine) {
        case BRIDGED:
//          context
        {
          //
          AbceConfigurationImpl idemix_configuration = setupStorageFilesForConfiguration(getFileStoragePrefix(fileStoragePrefix, CryptoEngine.IDEMIX), cryptoEngine);
          idemix_configuration.setUProvePathToExe(new UProveUtils().getPathToUProveExe().getAbsolutePath());
          idemix_configuration.setUProvePortNumber(uproveUtils.getIssuerServicePort());
          idemix_configuration.setUProveNumberOfCredentialsToGenerate(UPROVE_ISSUER_NUMBER_OF_CREDENTIAL_TOKENS_TO_GENERATE);
          idemix_configuration.setUProveRetryTimeout(UPROVE_SERVICE_TIMEOUT);
          
          //
          Injector idemix_injector = Guice.createInjector(ProductionModuleFactory.newModule(idemix_configuration, CryptoEngine.IDEMIX));
          this.idemixEngine = idemix_injector.getInstance(IssuerAbcEngine.class);
          KeyManager keyManager = idemix_injector.getInstance(KeyManager.class);
          
          initSystemParameters(fileStoragePrefix, keyManager);
          initParamsForEngine(CryptoEngine.IDEMIX, this.idemixEngine, keyManager, specAndPolicyList, systemAndIssuerParamsPrefix);

          issuerStorageManagerList.add(idemix_injector.getInstance(TokenStorageIssuer.class));
          addRevocationAuthorities(keyManager, revocationAuthorityParametersResourcesList);
        }
        {
          AbceConfigurationImpl uprove_configuration = setupStorageFilesForConfiguration(getFileStoragePrefix(fileStoragePrefix, CryptoEngine.UPROVE), cryptoEngine);
          uprove_configuration.setUProvePathToExe(new UProveUtils().getPathToUProveExe().getAbsolutePath());
          uprove_configuration.setUProvePortNumber(uproveUtils.getIssuerServicePort());
          uprove_configuration.setUProveNumberOfCredentialsToGenerate(UPROVE_ISSUER_NUMBER_OF_CREDENTIAL_TOKENS_TO_GENERATE);
          uprove_configuration.setUProveRetryTimeout(UPROVE_SERVICE_TIMEOUT);

          random = uprove_configuration.getPrng(); // new SecureRandom(); // new Random(1985);

          Injector uprove_injector = Guice.createInjector(ProductionModuleFactory.newModule(uprove_configuration, CryptoEngine.UPROVE));
          this.uproveEngine = uprove_injector.getInstance(IssuerAbcEngine.class);
          this.uproveBindingManager = uprove_injector.getInstance(UProveBindingManager.class);
          KeyManager keyManager = uprove_injector.getInstance(KeyManager.class);

          initSystemParameters(fileStoragePrefix, keyManager);
          initParamsForEngine(CryptoEngine.UPROVE, this.uproveEngine, keyManager, specAndPolicyList, systemAndIssuerParamsPrefix);

          issuerStorageManagerList.add(uprove_injector.getInstance(TokenStorageIssuer.class));
          addRevocationAuthorities(keyManager, revocationAuthorityParametersResourcesList);
        }
          break;

        default:
          //this.engine = injector.getInstance(IssuerAbcEngine.class);
          AbceConfigurationImpl configuration = setupStorageFilesForConfiguration(getFileStoragePrefix(fileStoragePrefix, cryptoEngine), cryptoEngine);
          configuration.setUProvePathToExe(new UProveUtils().getPathToUProveExe().getAbsolutePath());
          configuration.setUProvePortNumber(uproveUtils.getIssuerServicePort());
          configuration.setUProveNumberOfCredentialsToGenerate(UPROVE_ISSUER_NUMBER_OF_CREDENTIAL_TOKENS_TO_GENERATE);
          configuration.setUProveRetryTimeout(UPROVE_SERVICE_TIMEOUT);

          random = configuration.getPrng(); // new SecureRandom(); // new Random(1985);

          Injector injector = Guice.createInjector(ProductionModuleFactory.newModule(configuration, cryptoEngine));
          this.singleEngine = injector.getInstance(IssuerAbcEngine.class);
          if(cryptoEngine==CryptoEngine.UPROVE) {
            this.uproveBindingManager = injector.getInstance(UProveBindingManager.class);
          }
          keyManager = injector.getInstance(KeyManager.class);
          
          initSystemParameters(fileStoragePrefix, keyManager);
          initParamsForEngine(cryptoEngine, singleEngine, keyManager, specAndPolicyList, systemAndIssuerParamsPrefix);

          issuerStorageManagerList.add(injector.getInstance(TokenStorageIssuer.class));

          addRevocationAuthorities(keyManager, revocationAuthorityParametersResourcesList);

          break;
      }
      

    } catch (Exception e) {
      System.err.println("Init Failed");
      e.printStackTrace();
      throw new IllegalStateException("Could not setup issuer !", e);
    }
  }
  

  private String getFileStoragePrefix(String fileStoragePrefix, CryptoEngine cryptoEngine) {
    if(fileStoragePrefix!=null && fileStoragePrefix.length()>0) {
      if(fileStoragePrefix.endsWith("_")) {
        return fileStoragePrefix + ("" +cryptoEngine).toLowerCase() + "_";
      } else {
        if(fileStoragePrefix.endsWith("/") || fileStoragePrefix.endsWith("\\")) {
            // this is a folder...
            return fileStoragePrefix + ("" +cryptoEngine).toLowerCase() + "_";
        } else {
            return fileStoragePrefix + ("_" +cryptoEngine).toLowerCase() + "_";
        }
      }
    }
    return ("" +cryptoEngine).toLowerCase();
  }

  private SystemParameters generatedSystemParameters = null;
  
  
  private void initSystemParameters(String fileStoragePrefix, KeyManager keyManager) throws Exception {
      System.out.println("initSystemParameters");
      if (keyManager.hasSystemParameters()) {
          System.out.println("- already in keyManager");
          generatedSystemParameters = keyManager.getSystemParameters();
          return;
      }
      // have they just been generated - eg when BRIDGED - and initializing UPROVE... 
      if(generatedSystemParameters!=null) {
          System.out.println("- previously generated / read from storage");
          keyManager.storeSystemParameters(generatedSystemParameters);
          return;
      }
      // if not present - check 'file system'
      String systemParametersResource = fileStoragePrefix + SYSTEM_PARAMS_NAME_BRIDGED;
      try {
          generatedSystemParameters = loadObjectFromResource(systemParametersResource);
      } catch(IllegalStateException ignore) {
          System.out.println("- could not read from storage : " + systemParametersResource);
      } catch(Throwable ignore) {
      }
      if(generatedSystemParameters!=null) {
          System.out.println("- read from storage " + systemParametersResource);
          keyManager.storeSystemParameters(generatedSystemParameters);
          return;
      }
      System.out.println("- create new!");
      // ok - we have to generate them from scratch...
      generatedSystemParameters = new SystemParametersUtil().generatePilotSystemParameters();

      storeObjectInFile(generatedSystemParameters, systemParametersResource);

      // store in keyManager
      keyManager.storeSystemParameters(generatedSystemParameters);

//      String asXml = XmlUtils.toXml(of.createSystemParameters(generatedSystemParameters));
//      storeObjectInFile(asXml, fileStoragePrefix + SYSTEM_PARAMS_NAME + ".xml");
//      System.out.println("- new SystemParameters : " + asXml);
  }
  
  //
  private void initParamsForEngine(CryptoEngine cryptoEngine, IssuerAbcEngine initEngine, KeyManager keyManager, ArrayList<SpecAndPolicy> specAndPolicyList, String systemAndIssuerParamsPrefix) throws Exception {
      System.out.println("initParamsForEngine : " + cryptoEngine + " : " + initEngine + " : " + keyManager);

      if (!keyManager.hasSystemParameters() || generatedSystemParameters == null) {
          throw new IllegalAccessException("initSystemParameters - should have setup SystemParameters");
      }
        
      SystemParameters systemParameters = null;
      systemParameters = keyManager.getSystemParameters();
      System.out.println(" systemParameters : " + systemParameters);
      //

      for (SpecAndPolicy currentSap : specAndPolicyList) {

        SpecAndPolicy sap = initSpecAndPolicyFromResouces(currentSap);
        CredentialSpecification credSpec = sap.getCredentialSpecification();
        URI policyIssuerParametersUID =
            sap.getIssuancePolicy().getCredentialTemplate().getIssuerParametersUID();

        System.out.println("Check Credential Specification / Issuance Policy : "
            + credSpec.getSpecificationUID() + " : " + policyIssuerParametersUID);


        CredentialSpecification credSpecInKeystore =
            keyManager.getCredentialSpecification(credSpec.getSpecificationUID());
        if (credSpecInKeystore != null) {
          System.out.println(" - credspec already in keystore : " + credSpec.getSpecificationUID()
              + " : " + credSpec);
        } else {
          System.out.println(" - store credspec in keystre : ");
          keyManager.storeCredentialSpecification(credSpec.getSpecificationUID(), credSpec);
        }


        URI hash = new URI("urn:abc4trust:1.0:hashalgorithm:sha-256");

        
        URI issuerParamsUid;
        if (sap.issuerParamsUid != null) {
          issuerParamsUid = new URI(sap.issuerParamsUid);
        } else {
          // use default!
          issuerParamsUid = policyIssuerParametersUID;
        }
        boolean urnScheme = "urn".equals(issuerParamsUid.getScheme());
        issuerParamsUid =  URI.create(issuerParamsUid + ((urnScheme ? ":" : "/") + cryptoEngine).toLowerCase());
        
        sap.issuerParamsUid_URI = issuerParamsUid;
        
        URI revocationParamsUid;
        if (sap.revocationParamsUid != null) {
          revocationParamsUid = new URI(sap.revocationParamsUid);
        } else {
          revocationParamsUid = new URI(credSpec.getSpecificationUID() + (urnScheme ? ":" : "/") + "revocationUID");
        }
        sap.revocationParamsUid_URI = revocationParamsUid;

        System.out.println("Check Issuance Parameters : " + issuerParamsUid + " : "
            + revocationParamsUid);

        IssuerParameters issuerParameters = keyManager.getIssuerParameters(issuerParamsUid);
        if (issuerParameters != null) {
          System.out.println(" - issuer params exists! " + issuerParamsUid + " - with UID : "
              + issuerParameters.getParametersUID());

          System.out.println(" : " + issuerParameters.getSystemParameters());
        } else {
          // store credential specification
          System.out.println(" - create Issuer Parameters!");
          System.out.println(" : " + systemParameters);

          issuerParameters =
              initEngine.setupIssuerParameters(credSpec, systemParameters, issuerParamsUid, hash,
                  /*URI.create("Idemix")*/URI.create(cryptoEngine.toString()), revocationParamsUid);

          System.out.println(" : " + issuerParameters.getSystemParameters());

          System.out.println(" - store Issuer Parameters! " + issuerParamsUid + " : "
              + issuerParameters);

          keyManager.storeIssuerParameters(issuerParamsUid, issuerParameters);
          //
          // check if keys have been generated!
          String issuer_params_filename = "issuer_params_";
          if (urnScheme) {
            issuer_params_filename += issuerParamsUid.toASCIIString().replaceAll(":", "_");
          } else {
            issuer_params_filename +=
                issuerParamsUid.getHost().replace(".", "_")
                    + issuerParamsUid.getPath().replace("/", "_");
          }

          System.out.println(" - save in file - spec : " + credSpec.getSpecificationUID()
              + " - key : " + issuerParamsUid + " - filename : " + issuer_params_filename);

          storeObjectInFile(issuerParameters, systemAndIssuerParamsPrefix, issuer_params_filename);

          System.out.println(" - created issuerParameters with UID : "
              + issuerParameters.getParametersUID());
        }

        // Needed ??
        // sap.issuancePolicy.getCredentialTemplate().setIssuerParametersUID(
        // issuerParameters.getParametersUID());

        sap.getIssuancePolicy().getCredentialTemplate().setIssuerParametersUID(issuerParamsUid);

        String sapKey = sap.key + "::" + cryptoEngine;
        System.out.println(" - add spec/policy : " + sapKey + " : "
            + sap.getCredentialSpecification().getSpecificationUID());
        this.specAndPolicyMap.put(sapKey, sap);
      }

  }

  public void registerSmartcardScopeExclusivePseudonym(BigInteger pse) throws IOException {
    // TODO : VERIFY : Must match the way pseudonums are created...
    String primaryKey = DatatypeConverter.printBase64Binary(pse.toByteArray());

    for(TokenStorageIssuer issuerStorageManager : issuerStorageManagerList) {
      if(! issuerStorageManager.checkForPseudonym(primaryKey)) {
        System.out.println("registerSmartcardScopeExclusivePseudonym - register new pseudonym  - BigInteger: " + pse + " - PseudonymPrimaryKey : " + primaryKey);
        issuerStorageManager.addPseudonymPrimaryKey(primaryKey);
      } else {
        System.out.println("registerSmartcardScopeExclusivePseudonym - already registered");
      }
    }
  }

  /**
   * Creates a new instance of SpecAndPolicy
   * 
   * @param credspecAndPolicyKey
   * @return
   * @throws Exception
   */
  public SpecAndPolicy initSpecAndPolicy(String credspecAndPolicyKey) throws Exception {
    SpecAndPolicy orig = this.specAndPolicyMap.get(credspecAndPolicyKey);
    SpecAndPolicy sap =
        new SpecAndPolicy(credspecAndPolicyKey, orig.specResource, orig.policyResource);

    this.initSpecAndPolicyFromResouces(sap);
    return sap;
  }

  private SpecAndPolicy initSpecAndPolicyFromResouces(SpecAndPolicy cloneThisSap) throws Exception {
    SpecAndPolicy sap = new SpecAndPolicy(cloneThisSap);
    
    System.out.println("initSpecAndPolicyFromResouces : " + sap.specResource + " : "
        + sap.policyResource);
    InputStream is = getInputStream(sap.specResource);
    if (is == null) {
      throw new IllegalStateException("Illegal resource name for CredSpec : " + sap.specResource);
    }
    CredentialSpecification credSpec =
        (CredentialSpecification) XmlUtils.getObjectFromXML(is, true);

    is = getInputStream(sap.policyResource);
    if (is == null) {
      throw new IllegalStateException("Illegal resource name for IssuancePolicy : "
          + sap.policyResource);
    }
    IssuancePolicy issuancePolicy = (IssuancePolicy) XmlUtils.getObjectFromXML(is, true);
    
    if (!credSpec.getSpecificationUID().equals(
        issuancePolicy.getCredentialTemplate().getCredentialSpecUID())) {
      throw new IllegalStateException(
          "SpecificationUID must mactch for CredentialSpecification and IssuancePolicy : "
              + credSpec.getSpecificationUID() + " != "
              + issuancePolicy.getCredentialTemplate().getCredentialSpecUID());
    }

    sap.setCredentialSpecification(credSpec);
    sap.setIssuancePolicy(issuancePolicy);
    
    sap.setIssuancePolicyBytes(XmlUtils.toXml(of.createIssuancePolicy(issuancePolicy), true).getBytes());

    return sap;
  }

  /**
   * Process next step of issuance on IssuanceMessager
   * 
   * @param issuanceMessageXML IssuanceMessager as String
   * @return
   * @throws Exception
   */
  public IssuanceResponse_String issueStep_String(String issuanceMessageXML) throws Exception {
    if(singleEngine==null) {
      throw new IllegalStateException("IssuanceHelper.issueStep called without specifying CryptoEngine!");
  }
    IssuanceMessage issuanceMessage =
        (IssuanceMessage) XmlUtils.getObjectFromXML(
            new ByteArrayInputStream(issuanceMessageXML.getBytes()), false);
    System.out.println("IssuanceHelper - step_string - marchalled object: " + issuanceMessage
        + " - xml : " + issuanceMessageXML);

    IssuanceMessageAndBoolean response = this.issueStep(issuanceMessage);

    IssuanceMessage im = response.im;
    String xml = XmlUtils.toXml(this.of.createIssuanceMessage(im), false);

    // System.out.println("IssuanceService - step - return  XML : " + xml);

    return new IssuanceResponse_String(xml, response.lastMessage, response.im.getContext()
        .toString());
  }


  /**
   * Process next step of issuance on IssuanceMessager
   * 
   * @param issuanceMessage IssuanceMessager as String
   * @return
   * @throws Exception
   */
  public IssuanceMessageAndBoolean issueStep(IssuanceMessage issuanceMessage) throws Exception {
    System.out.println("IssuanceHelper - step_jaxb - marchalled object: " + issuanceMessage);

    if(singleEngine==null) {
        throw new IllegalStateException("IssuanceHelper.issueStep called without specifying CryptoEngine!");
    }
    return issueStep(singleEngine, issuanceMessage);
  }

  public IssuanceMessageAndBoolean issueStep(ProductionModule.CryptoEngine cryptoEngine, IssuanceMessage issuanceMessage) throws Exception {
    return issueStep(oldCryptoEngineToNewCryptoEngine(cryptoEngine), issuanceMessage);
  }
  /**
   * Process next step of issuance on IssuanceMessager
   * 
   * @param issuanceMessage IssuanceMessager as String
   * @return
   * @throws Exception
   */
  public IssuanceMessageAndBoolean issueStep(CryptoEngine cryptoEngine, IssuanceMessage issuanceMessage) throws Exception {
    System.out.println("IssuanceHelper - step_jaxb - marchalled object: " + issuanceMessage);

    IssuerAbcEngine useEngine;
    if(singleEngine!=null) {
      if(this.cryptoEngine != cryptoEngine) {
        throw new IllegalStateException("IssuanceHelper.issueStep called specifying CryptoEngine - but not initialized using BRIDGED! - running " + this.cryptoEngine + " - requesting " + cryptoEngine);
      } else {
        useEngine = singleEngine;
      }
    } else {
      if(cryptoEngine == CryptoEngine.IDEMIX) {
        useEngine = idemixEngine;
      } else if(cryptoEngine == CryptoEngine.UPROVE) {
        useEngine = uproveEngine;
      } else {
        throw new IllegalStateException("IssuanceHelper.issueStep : idemix/uprove engine not initialized...");
      }
    }
    return issueStep(useEngine, issuanceMessage);
  }
    
    
  private IssuanceMessageAndBoolean issueStep(IssuerAbcEngine useEngine, IssuanceMessage issuanceMessage) throws Exception {

    IssuanceMessageAndBoolean response;
    try {
        response = useEngine.issuanceProtocolStep(issuanceMessage);
    } catch (Exception e) {
      System.err.println("- IssuerABCE could not process Step IssuanceMessage from UserABCE : " + e);

      throw new Exception("Could not process next step on issuauce : ", e);
    }
    if (response.lastMessage) {
      System.out.println(" - last step - on server");
    } else {
      System.out.println(" - continue steps");
    }

      // String xml = XmlUtils.toXml(of.createIssuanceMessage(response.im), false);
      // System.out.println("IssuanceService - step - return  XML : " + xml);

    return response;
  }




  /**
   * Performs first step of issuance
   * 
   * @param credspecAndPolicyKey key identifying SpecAndPolicy
   * @param attributeValueMap attribute values defined by issuer
   * @return
   * @throws Exception
   */
  public IssuanceResponse_String initIssuance_String(String credspecAndPolicyKey,
      Map<String, Object> attributeValueMap) throws Exception {
    if(singleEngine==null) {
      throw new IllegalStateException("IssuanceHelper.initIssuance_String called without specifying CryptoEngine!");
    }

    IssuanceMessage jaxb = this.initIssuance(credspecAndPolicyKey, attributeValueMap);

    String xml = XmlUtils.toXml(this.of.createIssuanceMessage(jaxb), true);

    // System.out.println("IssuanceMessageAndBoolean XML : " + xml);

    return new IssuanceResponse_String(xml, jaxb.getContext().toString());
  }

  /**
   * Performs first step of issuance
   * 
   * @param credspecAndPolicyKey key identifying SpecAndPolicy
   * @param attributeValueMap attribute values defined by issuer
   * @return
   * @throws Exception
   */
  public IssuanceMessage initIssuance(String credspecAndPolicyKey,
      Map<String, Object> attributeValueMap) throws Exception {
    System.out.println("IssuanceHelper - initIssuance : " + credspecAndPolicyKey);
    if(singleEngine==null) {
      throw new IllegalStateException("IssuanceHelper.initIssuance called without specifying CryptoEngine!");
    }

    String sapKey = credspecAndPolicyKey + "::" + cryptoEngine;

    SpecAndPolicy specAndPolicy = this.specAndPolicyMap.get(sapKey);
    if (specAndPolicy == null) {
      System.out.println("IssuanceHelper - initIssuance : " + sapKey + " : "
          + this.specAndPolicyMap);
      throw new IllegalStateException("Unknown Spec And Policy Key " + sapKey);
    }
    return this.initIssuance(singleEngine, specAndPolicy, attributeValueMap);
  }

  public IssuanceMessage initIssuance(ProductionModule.CryptoEngine cryptoEngine, String credspecAndPolicyKey,
                                      Map<String, Object> attributeValueMap) throws Exception {
    return initIssuance(oldCryptoEngineToNewCryptoEngine(cryptoEngine), credspecAndPolicyKey, attributeValueMap);
  }
  /**
   * Performs first step of issuance
   * 
   * @param credspecAndPolicyKey key identifying SpecAndPolicy
   * @param attributeValueMap attribute values defined by issuer
   * @return
   * @throws Exception
   */
  public IssuanceMessage initIssuance(CryptoEngine cryptoEngine, String credspecAndPolicyKey,
      Map<String, Object> attributeValueMap) throws Exception {
    System.out.println("IssuanceHelper - initIssuance : " + cryptoEngine + " : " + credspecAndPolicyKey);
    IssuerAbcEngine useEngine;
    if(singleEngine!=null) {
      if(this.cryptoEngine != cryptoEngine) {
        throw new IllegalStateException("IssuanceHelper.initIssuance called specifying CryptoEngine - but not initialized using BRIDGED! - initialzied to : " + this.cryptoEngine + " - wanted : " + cryptoEngine);
      } else {
        useEngine = singleEngine;
      }
    } else {
      if(cryptoEngine == CryptoEngine.IDEMIX) {
        useEngine = idemixEngine;
      } else if(cryptoEngine == CryptoEngine.UPROVE) {
        useEngine = uproveEngine;
      } else {
        throw new IllegalStateException("IssuanceHelper.initIssuance : idemix/uprove engine not specified : " + cryptoEngine);
      }
    }
    String sapKey = credspecAndPolicyKey + "::" + cryptoEngine;

    SpecAndPolicy specAndPolicy = this.specAndPolicyMap.get(sapKey);
    if (specAndPolicy == null) {
      System.out.println("IssuanceHelper - initIssuance : " + sapKey + " : "
          + this.specAndPolicyMap);
      throw new IllegalStateException("Unknown Spec And Policy Key " + sapKey);
    }
    return this.initIssuance(useEngine, specAndPolicy, attributeValueMap);
  }

  /**
   * Performs first step of issuance
   * 
   * @param specAndPolicy
   * @param attributeValueMap attribute values defined by issuer
   * @return
   * @throws Exception
   */
  public IssuanceMessage initIssuance(SpecAndPolicy specAndPolicy,
      Map<String, Object> attributeValueMap) throws Exception {
    if(singleEngine==null) {
      throw new IllegalStateException("IssuanceHelper.initIssuance called without specifying CryptoEngine!");
    }
    return initIssuance(singleEngine, specAndPolicy, attributeValueMap);
  }
  

  private IssuanceMessage initIssuance(IssuerAbcEngine useEngine, SpecAndPolicy specAndPolicy,
                                         Map<String, Object> attributeValueMap) throws Exception {
    
    List<Attribute> issuerAtts = new ArrayList<Attribute>();

    this.populateIssuerAttributes(specAndPolicy.getCredentialSpecification(), issuerAtts,
        attributeValueMap);

//    try {
//      URI uid = specAndPolicy.getIssuancePolicy().getCredentialTemplate().getIssuerParametersUID();
//
//      System.out.println("IssuerParametersUID : " + uid);
//      IssuerParameters issuerParameters = keyManager.getIssuerParameters(uid);
//      System.out.println("IssuerParameters : " + issuerParameters);
//    } catch (Exception e) {
//      System.err.println("FAILED TO GET ISSUER PARAMS UID");
//    }
    
    IssuancePolicy clonedIssuancePolicy = specAndPolicy.cloneIssuancePolicy();
    
    IssuanceMessageAndBoolean response = null;
    try {
//      URI policyIssuerParametersUID = 
//          specAndPolicy.getIssuancePolicy().getCredentialTemplate().getIssuerParametersUID();
      URI policyIssuerParametersUID = specAndPolicy.issuerParamsUid_URI;

      
      System.out.println(" - call ABCE - policy : " + clonedIssuancePolicy + " : " + policyIssuerParametersUID 
          + " - attributes : " + issuerAtts);
      response = useEngine.initIssuanceProtocol(clonedIssuancePolicy, issuerAtts);

    } catch (Exception e) {
      System.err.println("- got Exception from ABCE Engine - try to create sample XML");
      e.printStackTrace();
      throw new Exception("Failed to initIsuanceProtocol", e);
    }

    if (response.lastMessage) {
      // cannot be last message
      throw new IllegalStateException(
          "Internal error in IssuerABCEngine - lastmessage returned from initIssuanceProtocol");
    }

    return response.im;
  }


  private void populateIssuerAttributes(CredentialSpecification credSpec,
      List<Attribute> issuerAtts, Map<String, Object> attributeValueMap) {

    if (credSpec.getAttributeDescriptions().getAttributeDescription().size() < attributeValueMap
        .size()) {
      throw new IllegalStateException("Wrong number of attributes ? - in credspec : "
          + credSpec.getAttributeDescriptions().getAttributeDescription().size() + " - in map "
          + attributeValueMap.size());
    }


    Map<String, AttributeDescription> adMap = new HashMap<String, AttributeDescription>();
    for (AttributeDescription ad : credSpec.getAttributeDescriptions().getAttributeDescription()) {
      adMap.put(ad.getType().toString(), ad);
    }

    Set<String> definedAttributes = attributeValueMap.keySet();
    for (String key : definedAttributes) {
      AttributeDescription ad = adMap.get(key);
      if (ad == null) {
        throw new IllegalStateException("No Attribute in Credspec with type : " + key);
      }

      Object value = attributeValueMap.get(key);

      // System.out.println("ad - type " + ad.getType() + " : " + ad.getDataType() + " - value : "
      // + value);

      Attribute attribute = this.of.createAttribute();
      // TODO : Howto create vaules ??
      attribute.setAttributeUID(URI.create("" + this.random.nextLong()));

      Object xmlValue = this.convertValue(ad.getDataType().toString(), value);
      // System.out.println("- xml Value : " + xmlValue);
      attribute.setAttributeValue(xmlValue);
      attribute.setAttributeDescription(this.of.createAttributeDescription());
      attribute.getAttributeDescription().setDataType(URI.create(ad.getDataType().toString()));
      attribute.getAttributeDescription().setEncoding(URI.create(ad.getEncoding().toString()));
      attribute.getAttributeDescription().setType(URI.create(ad.getType().toString()));

      // TODO:SETTING Friendly's should be handled inside User Engine!!!
      attribute.getAttributeDescription().getFriendlyAttributeName()
          .addAll(ad.getFriendlyAttributeName());

      //
      issuerAtts.add(attribute);
    }
  }


  private Object convertValue(String dataType, Object value) {
    // System.out.println("convertValue : " + dataType + " : " + value);
    try {
      if ("xs:string".equals(dataType)) {
        return value.toString();
      } else if ("xs:integer".equals(dataType)) {
        if ((value instanceof Integer) || (value instanceof Long) || (value instanceof BigInteger)) {
          return value;
        }
        throw new IllegalStateException(
            "Attributes of type integer must be either Integer, Long or BigInteger : "
                + value.getClass());
      } else if ("xs:dateTime".equals(dataType)) {
        Calendar cal = this.valueToCalendar(value);
        return DatatypeConverter.printDateTime(cal);
      } else if ("xs:date".equals(dataType)) {
        SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'Z'");
        if(value instanceof String) {
          try {
            // verify that sting is correct formatted!
            xmlDateFormat.parse((String)value);
            return (String) value;
          } catch(ParseException e) {
            throw new IllegalStateException(
              "Attributes of type xs:date - when presented as String must correctly formattet : yyyy-MM-dd'Z' - value was : " + value);
          }
        } else {
          Calendar cal = this.valueToCalendar(value);
          return xmlDateFormat.format(cal.getTime());
        }
      } else if ("xs:time".equals(dataType)) {
        Calendar cal = this.valueToCalendar(value);
        return DatatypeConverter.printTime(cal);
      } else if ("xs:anyURI".equals(dataType)) {
        return new URI(value.toString());
      } else if ("xs:boolean".equals(dataType)) {
        if(value instanceof Boolean) {
          return value;
        } else if(value instanceof String) {
          return Boolean.valueOf((String)value);
        }
        throw new IllegalStateException(
          "Attributes of type xs:boolean must be either Boolean or String (value == 'true' or 'false') : "
              + value.getClass());
      }
    } catch (IllegalStateException e) {
      // rethrow 
      throw e;
    } catch (Exception e) {
      throw new IllegalStateException("Could not convert value to correct data type : " + value
          + " - of class : " + value.getClass() + " - datatype " + dataType);
    }
    System.out.println("UNKNON ?? [" + dataType + "]");

    throw new IllegalStateException("Attributes dataType not supported (yet) : " + dataType);
  }


  private Calendar valueToCalendar(Object value) {
    if (value instanceof Date) {
      Calendar cal = Calendar.getInstance();
      cal.setTime((Date) value);
      return cal;
    } else if (value instanceof Calendar) {
      return (Calendar) value;
    }
    throw new IllegalStateException(
        "Attributes of type date/dateTime/time must be either Date or Calendar (or for xs:date correctly formattet String): "
            + value.getClass());
  }

}
