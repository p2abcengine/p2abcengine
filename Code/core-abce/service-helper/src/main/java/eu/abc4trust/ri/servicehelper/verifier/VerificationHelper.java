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

package eu.abc4trust.ri.servicehelper.verifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.abce.external.verifier.SynchronizedVerifierAbcEngineImpl;
import eu.abc4trust.abce.external.verifier.VerifierAbcEngine;
import eu.abc4trust.abce.internal.verifier.tokenManager.TokenStorage;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.guice.ProductionModuleFactory;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.ri.servicehelper.AbstractHelper;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.issuer.IssuanceHelper;
import eu.abc4trust.xml.ApplicationData;
import eu.abc4trust.xml.CredentialInPolicy;
import eu.abc4trust.xml.CredentialInPolicy.IssuerAlternatives;
import eu.abc4trust.xml.CredentialInPolicy.IssuerAlternatives.IssuerParametersUID;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.Message;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;
import eu.abc4trust.xml.VerifierParameters;

/**
 * @author hgk
 * 
 */
public class VerificationHelper extends AbstractHelper {

	static Logger log = Logger.getLogger(IssuanceHelper.class.getName());

	private static VerificationHelper instance;

	public VerifierAbcEngine engine;
	private Random random;
	private TokenStorage tokenStorage;
	private VerifierParameters verifierParameters = null;

	/**
	 * holds map resources by filename (without path) and the bytes of resource
	 */
	private final Map<String, byte[]> policyResourceMap = new HashMap<String, byte[]>();
	private final ObjectFactory of = new ObjectFactory();

	/**
	 * Used when creating a presentationPolicy to be later fetched when verifying a presentation
	 * token.
	 */
	Map<URI, RevocationInformation> revocationInformationStore =
			new HashMap<URI, RevocationInformation>();
    public static boolean cacheRevocationInformation = false;
    public static long REVOCATION_INFORMATION_MAX_TIME_TO_EXPIRE_IN_MINUTTES = 60;
    private static Map<URI, RevocationInformation> revocationInformationCache = new HashMap<URI, RevocationInformation>();



	/**
	 * Private constructor - initializes ABCE
	 * 
	 * @param fileStoragePrefix
	 * @throws URISyntaxException
	 */
	private VerificationHelper() throws URISyntaxException {
		log.info("VerificationHelper : create instance..");
		try {
			Injector injector = Guice.createInjector(ProductionModuleFactory.newModule());

			VerifierAbcEngine e = injector.getInstance(VerifierAbcEngine.class);

			this.engine = new SynchronizedVerifierAbcEngineImpl(e);
			this.keyManager = injector.getInstance(KeyManager.class);

			this.random = injector.getInstance(Random.class);

		//	this.tokenStorage = injector.getInstance(TokenStorage.class);

		} catch (Exception e) {
			System.err.println("Init Failed");
			e.printStackTrace();
			throw new IllegalStateException("Could not setup Verifier !", e);
		}
	}

	public static synchronized VerificationHelper initInstance() throws URISyntaxException{
		if (instance != null) {
			throw new IllegalStateException("initInstance can only be called once!");
		}
		log.info("VerificationHelper.initInstance ([])");
		instance = new VerificationHelper();
		return instance;
	}

	/**
	 * @param credSpecResourceList
	 * @param inspectorPublicKeyResourceList
	 * @param fileStoragePrefix this prefix will be prepended on storage files needed by the
	 *        VerifierAbcEnginge
	 * @param presentationPolicyResourceList initialize helper with list of resources of
	 *        PresentationPolicyAlternatives
	 * @throws URISyntaxException
	 */
	public static synchronized VerificationHelper initInstance(SystemParameters systemParams,
			List<IssuerParameters> issuerParamsList, List<CredentialSpecification> credSpecList,
			List<InspectorPublicKey> inspectorPublicKeyList,
			List<RevocationAuthorityParameters> revocationAuthorityParametersList, String fileStoragePrefix,
			String... presentationPolicyResourceList) throws URISyntaxException {
		if (instance != null) {
			throw new IllegalStateException("initInstance can only be called once!");
		}
		log.info("VerificationHelper.initInstance ([])");
		instance = new VerificationHelper();
		//
		instance.setSystemParams(systemParams);
		//
		instance.addCredentialSpecifications(credSpecList);
		instance.addIssuerParameters(issuerParamsList);

		instance.addInspectorPublicKeys(inspectorPublicKeyList);
		//
		instance.addPresentationPolicy(presentationPolicyResourceList);
		//
		instance.addRevocationAuthorities(instance.keyManager, revocationAuthorityParametersList);

		log.info("VerificationHelper.initInstance : DONE");

		return instance;
	}



	/**
	 * @return true if VerificationHelper has been initialized
	 */
	public static synchronized boolean isInit() {
		return instance != null;
	}

	/**
	 * Only used in test - can reset static instance
	 */
	public static synchronized void resetInstance() {
		System.err.println("WARNING VerificationHelper.resetInstance : " + instance);
		instance = null;
	}

	/**
	 * @return initialized instance of VerificationHelper
	 */
	public static synchronized VerificationHelper getInstance() {
		log.info("VerificationHelper.getInstance : " + instance);
		if (instance == null) {
			throw new IllegalStateException("initInstance not called before using VerificationHelper!");
		}
		return instance;
	}

	/**
	 * Adds extra policy resorces to VerificationHelper
	 * 
	 * @param presentationPolicyResourceList
	 */
	public void addPresentationPolicy(String[] presentationPolicyResourceList) {
		log.info("VerificationHelper addPresentationPolicy from resources : "
				+ presentationPolicyResourceList);
		ArrayList<String> list = new ArrayList<String>();
		for (String r : presentationPolicyResourceList) {
			list.add(r);
		}
		this.addPresentationPolicy(list);
	}

	/**
	 * Adds extra policy resorces to VerificationHelper
	 * 
	 * @param presentationPolicyResourceList
	 */
	public void addPresentationPolicy(ArrayList<String> presentationPolicyResourceList) {
		log.info("VerificationHelper addPresentationPolicy from resoucres : "
				+ presentationPolicyResourceList);
		String current = null;
		try {

			for (String resource : presentationPolicyResourceList) {
				current = resource;
				int ix = resource.lastIndexOf("/");
				if (ix == -1) {
					ix = resource.lastIndexOf("\\");
				}
				String key = resource;
				if (ix != -1) {
					key = resource.substring(ix + 1);
				}
				log.info(" - add policy : " + key + " - resource : " + resource);

				byte[] b = new byte[1];
				InputStream is = FileSystem.getInputStream(resource);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				while (is.read(b) != -1) {
					os.write(b);
				}
				byte[] policyBytes = os.toByteArray();
				@SuppressWarnings("unused")
				PresentationPolicyAlternatives presentationPolicy =
				(PresentationPolicyAlternatives) XmlUtils.getObjectFromXML(new ByteArrayInputStream(
						policyBytes), false);
				// ok...
				this.policyResourceMap.put(key, policyBytes);
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Init Failed - policy : " + current, e);
			throw new IllegalStateException("Init Failed - policy ! : " + current, e);
		}
	}

	public PresentationPolicyAlternatives modifyPresentationPolicy(
			PresentationPolicyAlternatives ppa, byte[] nonce, String applicationData,
			Map<URI, URI> revInfoUIDs) throws Exception {
		this.modifyPPA(ppa, applicationData, nonce, revInfoUIDs);
		return ppa;
	}

	/**
	 * @param policyName name of policy resource (without path)
	 * @param applicationData if present - will be inserted on all presentation policies
	 * @param revInfoUIDs if present - will try to fetch revocation information based on the uids.
	 * @return PresentationPolicyAlternatives - patched with applicationData
	 * @throws Exception
	 */
	public PresentationPolicyAlternatives createPresentationPolicy(String policyName, byte[] nonce,
			String applicationData, Map<URI, URI> revInfoUIDs) throws Exception {
		log.info("VerificationHelper - create policy : " + policyName + " - data : " + applicationData);

		PresentationPolicyAlternatives pp_alternatives;
		byte[] policyBytes = this.policyResourceMap.get(policyName);
		if (policyBytes == null) {
			log.warning(" - policy not found : " + policyName + " - map : " + this.policyResourceMap);
			throw new IllegalStateException("PresentationPolicy not found : " + policyName);
		}
		try {
			pp_alternatives =
					(PresentationPolicyAlternatives) XmlUtils.getObjectFromXML(new ByteArrayInputStream(
							policyBytes), false);
		} catch (Exception e) {
			log.log(Level.SEVERE, " - could init sample XML - create default", e);
			throw new IllegalStateException(
					"Could not init PresentationPolicy - event though it should have been verifed...");
		}

		this.modifyPPA(pp_alternatives, applicationData, nonce, revInfoUIDs);

		return pp_alternatives;
	}

	private PresentationPolicyAlternatives modifyPPA(PresentationPolicyAlternatives pp_alternatives,
			String applicationData, byte[] nonce, Map<URI, URI> revInfoUIDs) throws Exception {

		// try to make sure that RevocationInformation is only fetch once per RevAuth
		Map<URI, RevocationInformation> revocationInformationMap =
				new HashMap<URI, RevocationInformation>();

		for (PresentationPolicy pp : pp_alternatives.getPresentationPolicy()) {

			Message message = pp.getMessage();
			// set nonce
			message.setNonce(nonce);

			// set application data
			if (applicationData != null) {

				log.fine(" - SET APPLICATION DATA : " + applicationData);
				// message.setApplicationData(applicationData.getBytes());
				ApplicationData a = message.getApplicationData();
				a.getContent().clear();
				a.getContent().add(applicationData);
			}

			// REVOCATION!
			for (CredentialInPolicy cred : pp.getCredential()) {
				List<URI> credSpecURIList = cred.getCredentialSpecAlternatives().getCredentialSpecUID();
				boolean containsRevoceableCredential = false;
				CredentialSpecification credSpec = null;
				for (URI uri : credSpecURIList) {
					try {
						credSpec = this.keyManager.getCredentialSpecification(uri);
						if (credSpec== null) {
							throw new IllegalStateException("CredentialSpecification : " + uri + " - is in policy BUT is not registered in ABCE");
						}
						if (credSpec.isRevocable()) {
							containsRevoceableCredential = true;
							break;
						}
					} catch (KeyManagerException ignore) {}
				}
				if (containsRevoceableCredential) {
					IssuerAlternatives ia = cred.getIssuerAlternatives();
					log.info("WE HAVE REVOCEABLE CREDENTIAL : " + ia);
					for (IssuerParametersUID ipUid : ia.getIssuerParametersUID()) {
						IssuerParameters ip = this.keyManager.getIssuerParameters(ipUid.getValue());
						if ((ip != null) && (ip.getRevocationParametersUID() != null)) {
							// issuer params / credspec has revocation...
							RevocationInformation ri;
							log.info("revInfoUIDs: " + revInfoUIDs);
							if (revInfoUIDs != null) {
								log.info("Trying to get revInfo under " + credSpec.getSpecificationUID());
								URI revInformationUid = revInfoUIDs.get(credSpec.getSpecificationUID());
								ri = this.revocationInformationStore.get(revInformationUid);
                                if (ri != null) {
    								log.info("Got revInfo: " + ri.getRevocationInformationUID()
    										+ ", which should be the same as: " + revInformationUid);
                                } else {
                                    log.info("Revocation information is not there");
                                }

							} else {
								ri = revocationInformationMap.get(ip.getRevocationParametersUID());
                                if(ri != null) {
                                    log.info(" - revocationInformation found in (reuse) map");
                                }
							}
							log.info("RevocationInformation : " + ri);
                            if((ri==null) && cacheRevocationInformation) {
                                ri = revocationInformationCache.get(ip.getRevocationParametersUID());
                                if(ri!=null) {
                                    Calendar now = Calendar.getInstance();
                                    if(now.getTimeInMillis() > ri.getExpires().getTimeInMillis()) {
                                        log.info(" - revocationInformation has expired! - now : " + now.getTime() + " - created : " + ri.getCreated().getTime() + " : - expires : " + ri.getExpires().getTime() );
                                    } else if(now.getTimeInMillis() > (ri.getExpires().getTimeInMillis()- (REVOCATION_INFORMATION_MAX_TIME_TO_EXPIRE_IN_MINUTTES * 60 * 1000))) {
                                        long millis_to_expiration = (ri.getExpires().getTimeInMillis() - (REVOCATION_INFORMATION_MAX_TIME_TO_EXPIRE_IN_MINUTTES * 60 * 1000)) - now.getTimeInMillis();
                                        log.info(" - revocationInformation was invalidated ! - now : " + now.getTime() + " - created : " + ri.getCreated().getTime() + " : - expires : " + ri.getExpires().getTime() + " MILLIS TO EXPIRE " + millis_to_expiration);
                                        ri = null;
                                    } else {
                                        long millis_to_expiration = (ri.getExpires().getTimeInMillis() - (REVOCATION_INFORMATION_MAX_TIME_TO_EXPIRE_IN_MINUTTES * 60 * 1000)) - now.getTimeInMillis();
                                        log.info(" - valid revocationInformation found in Cache - now : " + now.getTime() + " - created : " + ri.getCreated().getTime() + " : - expires : " + ri.getExpires().getTime() + " MILLES TO EXPIRE : " + millis_to_expiration);
                                        revocationInformationMap.put(ip.getRevocationParametersUID(), ri);
                                    }
                                }
                            }
							
							if (ri == null) {
								log.info("Getting rev parameters uid information: "
										+ ip.getRevocationParametersUID());
								log.info("Getting rev parameters uid information: "
										+ keyManager.getRevocationAuthorityParameters(ip.getRevocationParametersUID()));
								ri =
										this.keyManager.getLatestRevocationInformation(ip.getRevocationParametersUID());
								revocationInformationMap.put(ip.getRevocationParametersUID(), ri);
								this.revocationInformationStore.put(ri.getRevocationInformationUID(), ri);
                                if(cacheRevocationInformation) {
                                    log.info(" - storage RevocationInformation in cache : " + ip.getRevocationParametersUID());
                                    revocationInformationCache.put(ip.getRevocationParametersUID(), ri);
                                }

							}
							log.info("RevocationInformation : " + ri.getRevocationInformationUID());
							URI revInfoUid = ri.getRevocationInformationUID();
							ipUid.setRevocationInformationUID(revInfoUid);
						}
					}
				}
			}
		}
		if(pp_alternatives.getVerifierParameters()==null) {
			pp_alternatives.setVerifierParameters(getVerifierParameters());
		}
		log.fine(" - presentationPolicy created");

		return pp_alternatives;
	}

	/**
	 * TODO : Remove when ABCE-Components can properly compare XML Private Helper
	 * 
	 * @param orig XML as string
	 * @return patched XML as JaxB
	 * @throws Exception
	 */
	private PresentationToken getPatchedPresetationToken(String orig) throws Exception {
		String patched =
				orig.replace("ConstantValue xmlns=\"http://abc4trust.eu/wp2/abcschemav1.0\"",
						"ConstantValue");

		patched = patched.replace(" xmlns=\"\"", "");
		patched = patched.replace("xmlns:ns2=\"http://abc4trust.eu/wp2/abcschemav1.0\"", "");

		return (PresentationToken) XmlUtils.getObjectFromXML(
				new ByteArrayInputStream(patched.getBytes()), true);

	}

	/**
	 * @param policyName name of policy resource (without path)
	 * @param applicationData if present - will be inserted on all presentation policies - must match
	 *        application data supplied when creating Policy
	 * @param presentationToken
	 * @return
	 * @throws Exception
	 */
	public boolean verifyToken(String policyName, byte[] nonce, String applicationData,
			PresentationToken presentationToken) throws Exception {
		log.info("VerificationHelper - verify token : " + policyName + " - applicationData : "
				+ applicationData);

		String orig = XmlUtils.toXml(this.of.createPresentationToken(presentationToken));
		presentationToken = this.getPatchedPresetationToken(orig);

		Map<URI, URI> revInfoUIDs = this.extractRevInfoUIDs(presentationToken);

		PresentationPolicyAlternatives pp =
				this.createPresentationPolicy(policyName, nonce, applicationData, revInfoUIDs);

		return this.verifyToken(pp, presentationToken);
	}

	private Map<URI, URI> extractRevInfoUIDs(PresentationToken pt) {
		Map<URI, URI> revInfoUIDs = null;
		for (CredentialInToken cred : pt.getPresentationTokenDescription().getCredential()) {
			boolean containsRevoceableCredential = false;
			try {
				CredentialSpecification credSpec =
						this.keyManager.getCredentialSpecification(cred.getCredentialSpecUID());
				if (credSpec.isRevocable()) {
					containsRevoceableCredential = true;
				}
			} catch (KeyManagerException ignore) {}
			if (containsRevoceableCredential) {
				if (revInfoUIDs == null) {
					revInfoUIDs = new HashMap<URI, URI>();
				}
				revInfoUIDs.put(cred.getCredentialSpecUID(), cred.getRevocationInformationUID());
			}
		}
		return revInfoUIDs;
	}

	/**
	 * @param ppXml PresentationPolicyAlternatives as String
	 * @param presentationTokenXml as String
	 * @return
	 * @throws Exception
	 */
	public boolean verifyToken_String(String ppaXml, String presentationTokenXml) throws Exception {

		PresentationPolicyAlternatives ppa =
				(PresentationPolicyAlternatives) XmlUtils.getObjectFromXML(
						new ByteArrayInputStream(ppaXml.getBytes()), true);

		return this.verifyToken(ppa, this.getPatchedPresetationToken(presentationTokenXml));
	}

	/**
	 * @param ppa PresentationPolicyAlternatives
	 * @param presentationToken
	 * @return
	 * @throws Exception
	 */
	public boolean verifyToken(PresentationPolicyAlternatives ppa, PresentationToken presentationToken)
			throws Exception {
		try {
			// verify in ABCE
			this.engine.verifyTokenAgainstPolicy(ppa, presentationToken, true);
			log.info(" - OK!");
			return true;
		} catch (TokenVerificationException e) {
			log.warning(" - TokenVerificationException : " + e);
			throw e;
		} catch (Exception e) {
			log.log(Level.SEVERE, " - Failed verifying token : ", e);
			return false;
		}

	}

	public byte[] generateNonce() {
		// TODO : is 10 bytes correct ?
		byte[] nonceBytes = new byte[10];
		this.random.nextBytes(nonceBytes);
		return nonceBytes;
	}


	public void registerSmartcardScopeExclusivePseudonym(BigInteger pse) throws IOException {
		String primaryKey = DatatypeConverter.printBase64Binary(pse.toByteArray());
		this.registerSmartcardScopeExclusivePseudonym(primaryKey);
	}

	public void registerSmartcardScopeExclusivePseudonym(byte[] pseValueAsBytes) throws IOException {
		String primaryKey = DatatypeConverter.printBase64Binary(pseValueAsBytes);
		this.registerSmartcardScopeExclusivePseudonym(primaryKey);
	}

	public void registerSmartcardScopeExclusivePseudonym(String b64Encoded_pseudonymValue)
			throws IOException {
		String primaryKey = b64Encoded_pseudonymValue;

		if (!this.tokenStorage.checkForPseudonym(primaryKey)) {
			log.info("registerSmartcardScopeExclusivePseudonym - register new pseudonym  - PseudonymPrimaryKey : "
					+ primaryKey);
			this.tokenStorage.addPseudonymPrimaryKey(primaryKey);
		} else {
			log.info("registerSmartcardScopeExclusivePseudonym - already registered");
		}
	}

	@Override
	public void addIssuerParameters(List<IssuerParameters> ips){
		super.addIssuerParameters(ips);
	}

	@Override
	public void addCredentialSpecifications(List<CredentialSpecification> credSpecs){
		super.addCredentialSpecifications(credSpecs);
	}

	@Override
	public void setSystemParams(SystemParameters syspar){
		super.setSystemParams(syspar);
	}


	public VerifierParameters getVerifierParameters() throws Exception {
		if(verifierParameters == null) {
			try {
				SystemParameters sp = keyManager.getSystemParameters();
				verifierParameters = engine.createVerifierParameters(sp);
			} catch(Exception e) {
				log.log(Level.WARNING, "Failed to generate VerifierParameters : " + e.getMessage());
			}
		}
		return verifierParameters;
	}
	
}
