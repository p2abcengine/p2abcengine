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

package eu.abc4trust.cryptoEngine.uprove.user;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import org.datacontract.schemas._2004._07.abc4trust_uprove.FirstIssuanceMessageComposite;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.org.apache.xpath.internal.compiler.PsuedoNames;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.abce.internal.user.policyCredentialMatcher.PolicyCredentialMatcher;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.exceptions.IdentitySelectionException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.xml.AttributeInPolicy;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialInPolicy;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.CredentialInPolicy.IssuerAlternatives.IssuerParametersUID;
import eu.abc4trust.xml.PseudonymInPolicy;
import eu.abc4trust.xml.util.XmlUtils;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.ObjectFactory;

public class ReloadTokensWebServiceCommunicationStrategy implements ReloadTokensCommunicationStrategy {
	private final UProveIssuanceHandling issuanceHandling;
	private final CredentialManager credManager;
	private final PolicyCredentialMatcher policyCredMatcher;
	private final ReloadStorageManager storageManager;

	private final Map<String, ReloadInformation> infoMap = new HashMap<String, ReloadInformation>();

	@Inject
	public ReloadTokensWebServiceCommunicationStrategy(UProveIssuanceHandling issuanceHandling, CredentialManager credManager, 
			PolicyCredentialMatcher policyCredMatcher, ReloadStorageManager storageManager) {
		this.issuanceHandling = issuanceHandling;	
		this.credManager = credManager;
		this.policyCredMatcher = policyCredMatcher;
		this.storageManager = storageManager;
	}
	
	@Override
	public Credential reloadTokens(Credential cred)
			throws ReloadException {

		System.out.println("reloadTokens called");
		try {

			ReloadInformation info = storageManager.retrive(cred);
			System.out.println("info: "+ info.issuanceUrl + ", "+info.issuanceStepUrl+ ", "+info.creduids+ ", "+info.pseudonyms+", "+info.inspectors);

			ObjectFactory of = new ObjectFactory();
			Client client = Client.create();
//	        client.setFollowRedirects(false);
	        client.setConnectTimeout(15000);
	        client.setReadTimeout(30000);

			Builder issueStartResource = client.resource(info.issuanceUrl)
					.type(MediaType.APPLICATION_XML).accept(MediaType.TEXT_XML);

			IssuanceMessage serverIm = issueStartResource.post(IssuanceMessage.class);

			IssuMsgOrCredDesc userIm = new IssuMsgOrCredDesc();			

			Object o = serverIm.getAny().get(0);
			if(o instanceof Element){
				Element element = (Element) o;
				String elementName = element.getLocalName() != null ? element.getLocalName() : element.getNodeName();

				// This is for the first step in the UProve issuance protocol for the user side, the issuer side has returned a FirstIssuanceMessage object
				if(elementName.equalsIgnoreCase("firstIssuanceMessageComposite")) {
					@SuppressWarnings("unchecked")
					IssuancePolicy ip = ((JAXBElement<IssuancePolicy>) serverIm.getAny().get(1)).getValue();
					fillInInspectorAndPseudonyms(ip, info);
				}
			}else if(o instanceof JAXBElement<?>){
				JAXBElement<?> elm = (JAXBElement<?>)o;
				if(elm.getValue() instanceof IssuancePolicy){
					fillInInspectorAndPseudonyms((IssuancePolicy)elm.getValue(), info);
				}else{
					System.err.println("RELOAD TOKENS :: Received object from issuer not known. Was of type (wrapped in JAXB): "+elm.getValue().getClass());
				}
			}else{
				System.err.println("RELOAD TOKENS :: Received object from issuer not known. Was of type: "+o.getClass());
			}
			
			ReloadTokensIdentitySelection idSelection = new ReloadTokensIdentitySelection(cred, info);
			UProveCryptoEngineUserImpl.RELOADING_TOKENS = true;
			userIm.im = policyCredMatcher.createIssuanceToken(serverIm, idSelection);
			UProveCryptoEngineUserImpl.RELOADING_TOKENS = false;
			boolean lastmessage = false;

			while (!lastmessage) {
				Builder issueStepResource = client.resource(info.issuanceStepUrl).type(MediaType.APPLICATION_XML)
						.accept(MediaType.TEXT_XML);
				serverIm = issueStepResource.post(IssuanceMessage.class, of.createIssuanceMessage(userIm.im));

				userIm = issuanceHandling.issuanceProtocolStep(serverIm, 
						cred.getCredentialDescription().getCredentialUID());
				lastmessage = (userIm.cd != null);
			}

			//assert ( userIm.cd.getCredentialUID() == credUid )
			return credManager.getCredential(userIm.cd.getCredentialUID());
		} catch (CredentialManagerException e) {
			e.printStackTrace();
			throw new ReloadTokensCommunicationStrategy.ReloadException("reloadTokens failed re-issuance:" + e.getMessage());
		} catch (CryptoEngineException e) {
			e.printStackTrace();
			throw new ReloadTokensCommunicationStrategy.ReloadException("reloadTokens failed re-issuance:" + e.getMessage());
        } catch (IdentitySelectionException e) {
          e.printStackTrace();
          throw new ReloadTokensCommunicationStrategy.ReloadException("reloadTokens failed re-issuance:" + e.getMessage());
		} catch (KeyManagerException e) {
			e.printStackTrace();
			throw new ReloadTokensCommunicationStrategy.ReloadException("reloadTokens failed re-issuance:" + e.getMessage());
		}finally{
			UProveCryptoEngineUserImpl.RELOADING_TOKENS = false;
		}
	}

	private void fillInInspectorAndPseudonyms(IssuancePolicy ip, ReloadInformation info) throws CredentialManagerException{
		try{System.out.println("issuancePolicy used for re-issuance:\n" + XmlUtils.toXml(new ObjectFactory().createIssuancePolicy(ip)));}
		catch(Exception e){e.printStackTrace();}
		List<CredentialInPolicy> creds = ip.getPresentationPolicy().getCredential();
		for(CredentialInPolicy c : creds){
			for(AttributeInPolicy att : c.getDisclosedAttribute()){		
				if(att.getInspectorAlternatives() != null){
					info.inspectors.add(att.getInspectorAlternatives().getInspectorPublicKeyUID().get(0));
					System.out.println("Adding an inspector alternative to info: " + att.getInspectorAlternatives().getInspectorPublicKeyUID().get(0));
				}
			}
		}
		for(PseudonymInPolicy pip: ip.getPresentationPolicy().getPseudonym()){
			List<PseudonymWithMetadata> pseudonyms = this.credManager.listPseudonyms(pip.getScope(), pip.isExclusive());
			for(PseudonymWithMetadata pwm : pseudonyms){
				info.pseudonyms.add(pwm.getPseudonym().getPseudonymUID());
			}
		}
	}
	
	@Override
	public void setCredentialInformation(URI context, ReloadInformation info) {
		infoMap.put(context.toString(), info);
	}

	@Override
	public void addCredentialIssuer(URI context, CredentialDescription credDesc, String issuanceUrl, String issuanceStepUrl)  {
		ReloadInformation info = infoMap.get(context.toString());
		if (info==null) {
			System.out.println("NOTE: ReloadTokens: No credential information found for \"" + context.toString()+ "\" cannot store reload information. Will not be able to reload tokens later");
      return;
    }
		System.out.println("Storing reload info: "+ issuanceUrl + ", "+issuanceStepUrl+ ", "+info.creduids+ ", "+info.pseudonyms+", "+info.inspectors);
		storageManager.store(credDesc, issuanceUrl, issuanceStepUrl, info);
	}



}
