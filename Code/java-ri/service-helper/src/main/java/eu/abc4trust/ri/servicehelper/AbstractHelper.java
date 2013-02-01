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

package eu.abc4trust.ri.servicehelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.w3c.dom.Element;

import com.ibm.zurich.idmx.key.VEPublicKey;
import com.ibm.zurich.idmx.showproof.accumulator.AccumulatorPublicKey;
import com.ibm.zurich.idmx.utils.GroupParameters;
import com.ibm.zurich.idmx.utils.Parser;
import com.ibm.zurich.idmx.utils.StructureStore;

import eu.abc4trust.cryptoEngine.idemix.util.IdemixConstants;
import eu.abc4trust.cryptoEngine.idemix.util.IdemixSystemParameters;
import eu.abc4trust.guice.ProductionModule;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.guice.configuration.AbceConfigurationImpl;
import eu.abc4trust.keyManager.KeyManager;
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

public abstract class AbstractHelper {

    public static final int IDEMIX_KEY_LENGTH = 2048; // TODO: define the security level & revocation
    public static final URI IDEMIX_CRYPTO_MECHANISM = URI.create("urn:abc4trust:1.0:algorithm:idemix");

    public static final int UPROVE_KEY_LENGTH = 2048; // TODO: define the security level & revocation
    public static final URI UPROVE_CRYPTO_MECHANISM = URI.create("urn:abc4trust:1.0:algorithm:uprove");
    public static final String UPROVE_GROUP_OID = "1.3.6.1.4.1.311.75.1.1.1";

    public static final String SYSTEM_PARAMS_NAME = "system_params";
    public static final String SYSTEM_PARAMS_NAME_BRIDGED = "system_params_bridged";

    public static final int INSPECTOR_KEY_LENGTH = 1024;
    public static final int REVOCATION_KEY_LENGTH = 1024;
    
    
    public static int UPROVE_SERVICE_TIMEOUT = 5;

    // TODO : Find out how to solve UProve Token renewal and FIX!
    public static int UPROVE_ISSUER_NUMBER_OF_CREDENTIAL_TOKENS_TO_GENERATE = 5;
    
    public static CryptoEngine oldCryptoEngineToNewCryptoEngine(ProductionModule.CryptoEngine old) {
      if(old == ProductionModule.CryptoEngine.IDEMIX) {
        return CryptoEngine.IDEMIX;
      }
      if(old == ProductionModule.CryptoEngine.UPROVE) {
        return CryptoEngine.UPROVE;
      }
      if(old == ProductionModule.CryptoEngine.BRIDGED) {
        return CryptoEngine.BRIDGED;
      }
      throw new IllegalStateException("Only IDEMIX, UPROVE and BRIDGED 'CryptoEngine' supported!");
  }
    
    public static class TEST_CONSTANTS {
        //    String patrasPseudonymValue_Idemix_BigIntegerString = "13113619309688455399943141047039588255012224402685936272493453785722607281295670863960311415709688624896181140637153337931764122797063639228028292556539597983270104663481061345843768312132112143065003200113982068496676609329599900685254308588931392680059865648913882240137003267016796606864192446927431006063143596502930331212298127485942432631258503855952748174323704253071654542663409996358469311691467823181733982163671649264944640161635575269481101875411220966819942294763879176180443963538681672879852525484918494941678429730605927666250736885168466826481197438440303225695399549810605219740737926981960982885828";
        public static final String patrasPseudonymValue_Idemix_BigIntegerString = "25857229784541950268499875786182194003912581012187598417993596190791281748949698234605396926046696671077573914597275416061331818118473783282384728255916745921462280014008445649682087461734700964789607228639009302452404285248645023843129815124210857632896341028924739292645923533780366309070264375800185394454295440256643140500550702706871928773704753337490927304959243736580922629491750647692563045587048364058572536032645378627198886767474620842333393078215239246465543681612873371633027985434701148270761075020461821745946654087910218203819101989773183522591341673956631480219443014917104022744117905645428210110942";
        public static final BigInteger patrasPseudonymValue_Idemix = new BigInteger(patrasPseudonymValue_Idemix_BigIntegerString);
    
        public static final String patrasPseudonymValue_UProve_BigIntegerString = "-6319502014825409402233010261015910260358045955898128433811342357573627989456098568587671344205053518132308526251667147670729054054863211133458126502740897278585454093958008698289532399317470297691503724004310715492241647016057779934099594778202128388930003157117400458103382257005747196628054630592602292965298474756631847386724249598666210487135999116562368377720243256008500687276514870861404768306021894069994421554646587203956216354007245517374608087545271384583096746900007950035051326689568584243739660706609722826047133974977000138986939047050588408879623514174459206962913661129873467956950877256583696143514";
        public static final BigInteger patrasPseudonymValue_UProve = new BigInteger(patrasPseudonymValue_UProve_BigIntegerString);

        public static final String soderhamnPseudonymValue_Idemix_BigIntegerString = "12256011167884393088598129359662547555057906527194134048743348489386566170151491878893237414768652365112577364138171733183911233904787212958565625155568195930885212743313664997507756225197911466477723828942912450789921055787318029337227826872391541051109730434776550172248076814071773144661715730448269747061887684456376661650406959418211135710743139196024991445078077847823483560028099699808350281155977084430411410242128375707851627711785899536718398708828409948410676156079543397666084906488185865819172665333001020502883400294824481310959414307757622256660538500986240522637416406218034379698119818519136495924833";
        public static final BigInteger soderhamnPseudonymValue_Idemix = new BigInteger(soderhamnPseudonymValue_Idemix_BigIntegerString);

        public static final String soderhamnPseudonymValue_UProve_BigIntegerString = "-6319502014825409402233010261015910260358045955898128433811342357573627989456098568587671344205053518132308526251667147670729054054863211133458126502740897278585454093958008698289532399317470297691503724004310715492241647016057779934099594778202128388930003157117400458103382257005747196628054630592602292965298474756631847386724249598666210487135999116562368377720243256008500687276514870861404768306021894069994421554646587203956216354007245517374608087545271384583096746900007950035051326689568584243739660706609722826047133974977000138986939047050588408879623514174459206962913661129873467956950877256583696143514";
        public static final BigInteger soderhamnPseudonymValue_UProve = new BigInteger(soderhamnPseudonymValue_UProve_BigIntegerString);
    }
    protected AbceConfigurationImpl setupStorageFilesForConfiguration(String fileStoragePrefix, CryptoEngine cryptoEngine) throws Exception {
        return setupStorageFilesForConfiguration(fileStoragePrefix, cryptoEngine, false);
    }
    protected AbceConfigurationImpl setupStorageFilesForConfiguration(String fileStoragePrefix, CryptoEngine cryptoEngine, boolean wipe_existing_storage) throws Exception {
        AbceConfigurationImpl configuration = new AbceConfigurationImpl();
        configuration.setKeyStorageFile(getFile(fileStoragePrefix + "keystorage", wipe_existing_storage));
        configuration.setCredentialFile(getFile(fileStoragePrefix + "credential", wipe_existing_storage));
        configuration.setPseudonymsFile(getFile(fileStoragePrefix + "pseudonyms", wipe_existing_storage));
        configuration.setTokensFile(getFile(fileStoragePrefix + "tokens", wipe_existing_storage));
        configuration.setSecretStorageFile(getFile(fileStoragePrefix + "secrets", wipe_existing_storage));
        Random random = new SecureRandom(); // new Random(1985)
        configuration.setPrng(random);
        configuration.setIssuerSecretKeyFile(getFile(fileStoragePrefix + "issuerSecretKeys", wipe_existing_storage));
        configuration.setIssuerLogFile(getFile(fileStoragePrefix + "issuerLog", wipe_existing_storage));
        configuration.setInspectorSecretKeyFile(getFile(fileStoragePrefix + "inspectorSecrets", wipe_existing_storage));
        configuration.setRevocationAuthoritySecretStorageFile(getFile(fileStoragePrefix + "revocationAuthoritySecrets", wipe_existing_storage));
        configuration.setRevocationAuthorityStorageFile(getFile(fileStoragePrefix + "revocationAuthorityStorage", wipe_existing_storage));

        File fileStoragePrefix_file = new File(fileStoragePrefix);
        File fileStorageFolder;
        String filePrefix = "";
        if(fileStoragePrefix_file.isDirectory()) {
            fileStorageFolder = fileStoragePrefix_file;
        } else {
            // folder + file prefix eg 'patras_user_storage/testcase_' : get folder name
            fileStorageFolder = fileStoragePrefix_file.getParentFile();
            filePrefix = fileStoragePrefix_file.getName();
        }

        // check images
        File imagesFolder = new File(fileStorageFolder, "images");
        if(imagesFolder.exists()) {
            if(! imagesFolder.isDirectory()) {
                throw new IllegalStateException("File exists with name for 'images' folder : " + imagesFolder.getAbsolutePath());
            }
        } else {
            imagesFolder.mkdir();
        }
        configuration.setImageCacheDir(imagesFolder); // new File(fileStoragePrefix + "images"));
        File defaultImage = new File(imagesFolder, "default.jpg");
        configuration.setDefaultImagePath(defaultImage.getAbsolutePath()); // "default.jpg");
        if(! defaultImage.exists()) {
            InputStream is = getInputStream("/ui/ABC4TrustCredential_default.jpg");
            FileOutputStream fos = new FileOutputStream(defaultImage);
            byte[] b = new byte[1];
            while(is.read(b)!= -1) {
                fos.write(b);
            }
            is.close();
            fos.close();
        }
        if(cryptoEngine == CryptoEngine.UPROVE) {
            File uproveWorkDir = new File(fileStorageFolder, filePrefix + "uprove");
            if(! uproveWorkDir.isDirectory()) {
                uproveWorkDir.mkdirs();
            }
            configuration.setUProveWorkingDirectory(uproveWorkDir);
        } else {
            configuration.setUProveWorkingDirectory(null);
        }
        configuration.setUProveRetryTimeout(4);

        return configuration;
    }



    //
    public CryptoEngine cryptoEngine;
    public KeyManager keyManager;
    protected final Set<URI> credSpecSet = new HashSet<URI>();

    protected static File getFile(String filename, boolean wipe_existing_file) throws IOException {
//        System.out.println("getFile : " + filename + " - wipe : " + wipe_existing_file);
        File f = new File(filename);
        if (f.exists() && ! wipe_existing_file) {
//            System.out.println("getFile : " + filename + " - exists!");
            return f;
        } else {
            if (f.exists() && wipe_existing_file) {
               System.out.println("file exits - wipe it! " + filename);
               f.delete();
            }
            
            File folder = f.getParentFile();
            if(! folder.exists()) {
                System.out.println("create folders : " + folder);
                folder.mkdirs();
            }
            System.out.println("create new file : " + filename);
            // System.out.println("Folder : " + folder.getAbsolutePath() + " : " + folder.exists());
            boolean created = f.createNewFile();
            if (!created) {
                throw new IOException("Could not create new file : " + filename);
            }
            return f;
        }
    }

    public static InputStream getInputStream(String resource) throws IOException {
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


    protected void XsetSystemParams(String systemParamsResource) {
        System.out.println("AbstractHelper setSystemParams from resoucres : " + systemParamsResource);
        try {
            if (systemParamsResource == null) {
                // create...
                // SystemParameters systemParameters = this.engine.seSystemParameters(idemix_keyLength,
                // idemix_cryptoMechanism);
                // userKeyManager.storeSystemParameters(systemParameters);

            } else if (this.keyManager.getSystemParameters() == null) {
                System.out.println(" - read system parameters - from resource");

                SystemParameters systemParameters = loadObjectFromResource(systemParamsResource);

                this.keyManager.storeSystemParameters(systemParameters);
            } else {
                System.out.println(" - system parameters exists!");
            }
        } catch (Exception e) {
            System.err.println("Init Failed");
            e.printStackTrace();
            throw new IllegalStateException("Could not setup issuer ! Error SystemParameters", e);
        }
    }

    @SuppressWarnings("unused")
	protected void setupIdemixEngine() {
      if((true || this.cryptoEngine == CryptoEngine.IDEMIX) || (this.cryptoEngine == CryptoEngine.BRIDGED)) {
        try {
          if(this.keyManager.hasSystemParameters()) {
            SystemParameters systemParameters = this.keyManager.getSystemParameters();
            
            IdemixSystemParameters idemixSystemParameters = new IdemixSystemParameters(systemParameters);
            com.ibm.zurich.idmx.utils.SystemParameters sysPar = idemixSystemParameters.getSystemParameters();
            GroupParameters grPar = idemixSystemParameters.getGroupParameters();

            // Load system, group and issuer parameters to Idemix StructureStore
            StructureStore.getInstance().add(IdemixConstants.systemParameterId, sysPar);
            StructureStore.getInstance().add(IdemixConstants.groupParameterId, grPar);
          } else {
            // warning...
          }
        } catch(Exception ignore) {
          ignore.printStackTrace();
        }
      }
      
    }

    protected void addIssuerParameters(String[] issuerParamsResourceList) {
        System.out.println("AbstractHelper addIssuerParameters from resoucres : "
                + issuerParamsResourceList);
        try {
            for (String resource : issuerParamsResourceList) {
//                System.out.println(" - read issuer parameters " + resource);
                IssuerParameters issuerParameters = loadObjectFromResource(resource);

//                System.out.println(" - store issuer parameters ? " + issuerParameters.getParametersUID());

                IssuerParameters exists =
                        this.keyManager.getIssuerParameters(issuerParameters.getParametersUID());
                if (exists != null) {
//                    System.out.println(" - - exists!!");
                } else {
//                    System.out.println(" - - add!! " + issuerParameters.getParametersUID());
                    this.keyManager.storeIssuerParameters(issuerParameters.getParametersUID(), issuerParameters);
                    CredentialSpecification credSpecExists = this.keyManager.getCredentialSpecification(issuerParameters.getCredentialSpecUID());
                    if(credSpecExists==null) {
                        throw new IllegalStateException("Credspec needed by Issuer Parameters - not supplied! : " + issuerParameters.getCredentialSpecUID());
                    }
                    if(! this.keyManager.hasSystemParameters()) {
                      System.out.println("Store SystemParameters");
                      this.keyManager.storeSystemParameters(issuerParameters.getSystemParameters());
                    }
                }

            }
        } catch (Exception e) {
            System.err.println("Init Failed");
            e.printStackTrace();
            throw new IllegalStateException("Could not setup issuer ! Error Adding IssuerParameters", e);
        }

    }

    protected void addCredentialSpecifications(String[] credSpecResourceList) {
        System.out.println("AbstractHelper addCredentialSpecification from resoucres : "
                + credSpecResourceList);
        try {

            for (String resource : credSpecResourceList) {
                InputStream is = getInputStream(resource);
                if (is == null) {
                    throw new IllegalStateException("CredSpec resource not found :  " + resource);
                }
                CredentialSpecification credSpec =
                        (CredentialSpecification) XmlUtils.getObjectFromXML(is, true);

                CredentialSpecification existInStore =
                        this.keyManager.getCredentialSpecification(credSpec.getSpecificationUID());
                if (existInStore == null) {
                    this.keyManager.storeCredentialSpecification(credSpec.getSpecificationUID(), credSpec);

                    System.out.println(" - credspec added : " + credSpec.getSpecificationUID());
                } else {
                    System.out.println(" - credspec already in store : " + credSpec.getSpecificationUID());

                }
            }
        } catch (Exception e) {
            System.err.println("Init Failed");
            e.printStackTrace();
            throw new IllegalStateException(
                    "Could not setup helper ! Error reading CredentialSpecifications", e);
        }
    }

    protected void addInspectorPublicKeys(String[] inspectorPublicKeyResourceList) {
        System.out.println("AbstractHelper addInspectorPublicKeys from resouces : "
            + inspectorPublicKeyResourceList);
        try {
            for (String resource : inspectorPublicKeyResourceList) {
                System.out.println(" - read inspector public key" + resource);
                
                InspectorPublicKey pk = loadObjectFromResource(resource);
                System.out.println("- loaded Inspector Public Key - with UID : " + pk.getPublicKeyUID());
                
                InspectorPublicKey verifyPk = keyManager.getInspectorPublicKey(pk.getPublicKeyUID());
                if (verifyPk != null) {
                  System.out.println("- key already in keyManager " + verifyPk);
                } else {
                  System.out.println("- add key to keyManager");
                  keyManager.storeInspectorPublicKey(pk.getPublicKeyUID(), pk);
                }
                // register for Idemix!
                VEPublicKey vePubKey =
                    (VEPublicKey) Parser.getInstance().parse(
                        (org.w3c.dom.Element) pk.getCryptoParams().getAny().get(0));
                StructureStore.getInstance().add(pk.getPublicKeyUID().toString(), vePubKey);
            }          
        } catch (Exception e) {
            System.err.println("Init Failed");
            e.printStackTrace();
            throw new IllegalStateException(
                    "Could not setup helper ! Error reading InspectorPublicKeys", e);
        }
    }


    protected void addRevocationAuthorities(KeyManager engineKeyManager, String[] revocationAuthorityParametersResourcesList) {
        System.out.println("AbstractHelper addRevocationAuthorities from resouces : "
            + revocationAuthorityParametersResourcesList);
  
        try {
            for (String resource : revocationAuthorityParametersResourcesList) {
                System.out.println(" - read revocationAuthorityParameters" + resource);
                
                RevocationAuthorityParameters rap = loadObjectFromResource(resource);
                URI revAuthParamsUid = rap.getParametersUID();
                System.out.println("- loaded revocationAuthorityParameters - with UID : " + revAuthParamsUid);
                
                RevocationAuthorityParameters verifyRap = engineKeyManager.getRevocationAuthorityParameters(revAuthParamsUid);
                if (verifyRap != null) {
                  System.out.println("- revocationAuthorityParameters in keyManager " + verifyRap);
                } else {
                  System.out.println("- add key to keyManager");
                  engineKeyManager.storeRevocationAuthorityParameters(revAuthParamsUid, rap);
                }
                
                // register key for IDEMIX!
                List<Object> any = rap.getCryptoParams().getAny();
                Element publicKeyStr = (Element) any.get(0);
                Object publicKeyObj = Parser.getInstance().parse(publicKeyStr);

                AccumulatorPublicKey publicKey = (AccumulatorPublicKey) publicKeyObj;

                StructureStore.getInstance().add(publicKey.getUri().toString(),
                        publicKey);
                System.out.println("- registered in IDEMIX StructureStore");
            }          
        } catch (Exception e) {
            System.err.println("Init Failed");
            e.printStackTrace();
            throw new IllegalStateException("Could not setup helper ! Error reading RevocationAuthorityParameters", e);
        }

    }


    
    @SuppressWarnings("unchecked")
    public static <T> T loadObjectFromResource(String name)
            throws IOException, ClassNotFoundException {
        System.out.println("Load Object from Resource : " + name);
        InputStream is = getInputStream(name);
        ObjectInputStream ois = new ObjectInputStream(is);

        Object object = ois.readObject();
        ois.close();
        is.close();

        return (T) object;
    }

    public static void storeObjectInFile(Object object, String prefix, String name)
            throws IOException {
        File file = getFile(prefix + name, false);// new File(prefix + name);
        System.out.println("Store Object " + object + " - in file " + file.getAbsolutePath());

        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(object);
        fos.close();
    }

    public static void storeObjectInFile(Object object, String resourceName)
        throws IOException {
        File file = new File(resourceName);
        System.out.println("storeObject " + object + " - in file " + file.getAbsolutePath());
  
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
    
        oos.writeObject(object);
        fos.close();
    }

    
    public static BigInteger getPseudonymValue(PresentationToken presentationToken, URI scope) {
        return getPseudonymValue(presentationToken, scope.toString());
    }
    public static BigInteger getPseudonymValue(PresentationToken presentationToken, String scope) {
        PseudonymInToken pse = findPseudonym(presentationToken.getPresentationTokenDescription(), scope);
        if(pse != null) {
            byte[] pse_from_presentation_bytes = pse.getPseudonymValue(); // presentationToken.getPresentationTokenDescription().getPseudonym().get(0).getPseudonymValue();
            BigInteger pse_from_presentation = new BigInteger(pse_from_presentation_bytes);
            return pse_from_presentation;
        } else {
            return null;
        }
    }

    
    /**
     * Note! Uses OLD CryptoEngine - to provide BackWard Compatibility!
     * @param presentationToken
     * @param scope
     * @return
     */
    public static ProductionModule.CryptoEngine getCryptoEngineForPseudonym(PresentationToken presentationToken, URI scope) {
        return getCryptoEngineForPseudonym(presentationToken, scope.toString());
    }
    /**
     * Note! Uses OLD CryptoEngine - to provide BackWard Compatibility!
     * @param presentationToken
     * @param scope
     * @return
     */
    public static ProductionModule.CryptoEngine getCryptoEngineForPseudonym(PresentationToken presentationToken, String scope) {
        PseudonymInToken pse = findPseudonym(presentationToken.getPresentationTokenDescription(), scope);
        if(pse != null) {
            CryptoParams cryptoEvidence = presentationToken.getCryptoEvidence();
            for(Object o: cryptoEvidence.getAny()) {
                if(o instanceof Element) {
                    Element element = (Element)o;
                    String elementName = element.getLocalName() != null ? element.getLocalName() : element.getNodeName();
                    if("IdmxProof".equals(elementName)) {
                        return ProductionModule.CryptoEngine.IDEMIX;
                    }
                    if("UProvePseudonym".equals(elementName)) {
                        return ProductionModule.CryptoEngine.UPROVE;
                    }                    
                }
            }
            // ?? illegal state ???
            return null;
        } else {
            return null;
        }
    }

    private static PseudonymInToken findPseudonym(PresentationTokenDescription presentationTokenDescription, String scope) {
        for(PseudonymInToken pse : presentationTokenDescription.getPseudonym()) {
            // Verify Pseudonym is correct scope
            if(scope.equals(pse.getScope().toString())) {
                return pse;
            }
        }
        return null;
    }

    public static BigInteger getPseudonymValue(IssuanceMessage issuanceMessage, URI scope) {
        return getPseudonymValue(issuanceMessage, scope.toString());
    }
    public static BigInteger getPseudonymValue(IssuanceMessage issuanceMessage, String scope) {
        if(issuanceMessage!=null) {
            for(Object o : issuanceMessage.getAny()) {
                System.out.println("Testing : " + o);
                if(o instanceof JAXBElement<?> && ((JAXBElement<?>)o).getValue() instanceof IssuanceToken) {
                    IssuanceToken it = (IssuanceToken) ((JAXBElement<?>)o).getValue();
                    System.out.println("Testing IT : " + it);
                    PseudonymInToken pse = findPseudonym(it.getIssuanceTokenDescription().getPresentationTokenDescription(), scope);
                    if(pse != null) {
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
