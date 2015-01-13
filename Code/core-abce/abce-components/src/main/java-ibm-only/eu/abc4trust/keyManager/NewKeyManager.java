//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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
package eu.abc4trust.keyManager;

import java.net.URI;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.google.inject.Inject;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationMessageFacade;

import eu.abc4trust.db.PersistentStorage;
import eu.abc4trust.db.SimpleParamTypes;
import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.util.ByteSerializer;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.RevocationMessage;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class NewKeyManager implements KeyManager {

  private final PersistentStorage ps;
	
  //copied
  private final RevocationProxy revocationProxy;
  public static final String CURRENT_REVOCATION_UID_STR = ":current_revocation_information_uid";

  @Inject
  public NewKeyManager(PersistentStorage ps, RevocationProxy revocationProxy) {
	this.ps = ps;
	this.revocationProxy = revocationProxy;
  }

  @Override
  public IssuerParameters getIssuerParameters(URI issuid) throws KeyManagerException {
    final IssuerParameters ret = (IssuerParameters) ByteSerializer.readFromBytes(ps.getItem(
        SimpleParamTypes.ISSUER_PARAMS, issuid));
    if (ret != null && ret.getCryptoParams() != null) {
    	XmlUtils.fixNestedContent(ret.getCryptoParams());
    }
	return ret;
  }

  @Override
  public boolean storeIssuerParameters(URI issuid, IssuerParameters issuerParameters)
      throws KeyManagerException {
    return ps.replaceItem(SimpleParamTypes.ISSUER_PARAMS, issuid,
        ByteSerializer.writeAsBytes(issuerParameters)) > 0;
  }

  @Override
  public InspectorPublicKey getInspectorPublicKey(URI ipkuid) throws KeyManagerException {
    final InspectorPublicKey ret = (InspectorPublicKey) ByteSerializer.readFromBytes(ps.getItem(
        SimpleParamTypes.INSPECTOR_PUBLIC_KEY, ipkuid));
    if (ret != null && ret.getCryptoParams() != null) {
    	XmlUtils.fixNestedContent(ret.getCryptoParams());
    }
    return ret;
  }

  @Override
  public boolean storeInspectorPublicKey(URI ipkuid, InspectorPublicKey inspectorPublicKey)
      throws KeyManagerException {
    return ps.replaceItem(SimpleParamTypes.INSPECTOR_PUBLIC_KEY, ipkuid,
        ByteSerializer.writeAsBytes(inspectorPublicKey)) > 0;
  }

  @Override
  public RevocationAuthorityParameters getRevocationAuthorityParameters(URI rapuid)
      throws KeyManagerException {
    final RevocationAuthorityParameters ret = (RevocationAuthorityParameters) ByteSerializer.readFromBytes(ps.getItem(
    		SimpleParamTypes.REV_AUTH_PARAMS, rapuid));
    if (ret != null && ret.getCryptoParams() != null) {
    	XmlUtils.fixNestedContent(ret.getCryptoParams());
    }
    return ret;
  }

	/**
	 * Retrieves the revocation information from the revocation authority. If the
	 * revocationInformationUID is null, the latest revocation information is fetched.
	 * 
	 * @throws KeyManagerException
	 */
  private RevocationInformation getRevocationInformationFromRevocationAuthority(
		  RevocationAuthorityParameters raParameters, URI revocationInformationUID)
				  throws KeyManagerException {
	  try {
		  // Wrap the request into the Revocation message.
		  RevocationMessageFacade revocationMessageFacade = new RevocationMessageFacade();
		  revocationMessageFacade.setContext(URI.create("NO-CONTEXT"));
		  revocationMessageFacade.setRevocationAuthorityParametersUID(raParameters.getParametersUID());
		  revocationMessageFacade.setRequestLatestRevocationInformation();
		  revocationMessageFacade.setRevocationInformationUID(revocationInformationUID);

		  // TODO (pbi) the revocation proxy should be found using the revocation information UID
		  // (raParameters.getRevocationInfoReference())

		  RevocationMessage rm =
				  this.revocationProxy.processRevocationMessage(
						  revocationMessageFacade.getDelegateeValue(), raParameters);

		  // Unwrap RevocationInformation from RevocationMessage.
		  RevocationInformation revocationInformation =
				  new RevocationMessageFacade(rm).getRevocationInformation();
		  if (revocationInformation == null) {
			  throw new KeyManagerException(
					  "Revocation information cannot be retrieved from revocation authority: "
							  + raParameters.getParametersUID());
		  }
		  return revocationInformation;

	  } catch (Exception ex) {
		  throw new KeyManagerException(ex);
	  }
  }

  @Override
  public RevocationInformation getRevocationInformation(URI raParametersUID, URI revocationInformationUID)
		  throws KeyManagerException {
	  try {
		  RevocationInformation revocationInformation = 
				  (RevocationInformation) ByteSerializer.readFromBytes(
						  ps.getRevocationInformation(revocationInformationUID, raParametersUID));

		  if (revocationInformation == null) {
			  RevocationAuthorityParameters revocationAuthorityParameters =
					  this.getRevocationAuthorityParameters(raParametersUID);

			  if (revocationAuthorityParameters == null) {
				  throw new KeyManagerException("Unkown revocation authority parameters");
			  }

			  revocationInformation =
					  this.getRevocationInformationFromRevocationAuthority(revocationAuthorityParameters,
							  revocationInformationUID);

			  if (revocationInformation == null) {
				  return null;
			  }

			  try {
				  this.storeRevocationInformation(revocationInformation.getRevocationInformationUID(),
						  revocationInformation);
			  } catch (KeyManagerException ex) {
				  return null;
			  }
		  }
		  return revocationInformation;
	  } catch (Exception ex) {
		  throw new KeyManagerException(ex);
	  }
  }

  @Override
  public RevocationInformation getCurrentRevocationInformation(URI raParametersUID)
		  throws KeyManagerException {

	  if (raParametersUID == null) {
		  throw new KeyManagerException(
				  "The revocation authority UID is null. Did you forget to initialize the revocation authority parameters?");
	  }	  

	  RevocationInformation revocationInformation =
			  (RevocationInformation) ByteSerializer.readFromBytes(ps.getLatestRevocationInformation(raParametersUID));

	  // Check whether the revocation information is not expired.
	  if ((revocationInformation != null)
			  && (revocationInformation.getExpires().compareTo(new GregorianCalendar()) > 0)) {
		  return revocationInformation;
	  }

	  // TODO note that the user/issuer/verifier MUST use getLatestRevocationInformation next...
	  // This is done to allow for the application to control when requests to the RA are made,
	  // if this is not an issue, the keymanager can automatically call getLatestRevocationInformation(..)
	  // return this.getLatestRevocationInformation(raParametersUID);	  
	  return null;
  }

  @Override
  public RevocationInformation getLatestRevocationInformation(URI raParametersUID)
		  throws KeyManagerException {

	  if (raParametersUID == null) {
		  throw new KeyManagerException(
				  "The revocation authority UID is null. Did you forget to initialize the revocation authority parameters?");
	  }

	  RevocationAuthorityParameters revocationAuthorityParameters =
			  this.getRevocationAuthorityParameters(raParametersUID);
	  if (revocationAuthorityParameters == null) {
		  throw new KeyManagerException("Unkown revocation authority parameters : " + raParametersUID);
	  }

	  RevocationInformation revocationInformation =
			  this.getRevocationInformationFromRevocationAuthority(revocationAuthorityParameters, null);

	  this.ps.insertRevocationInformation(revocationInformation.getRevocationInformationUID(), 
			  revocationInformation.getRevocationAuthorityParametersUID(), 
			  Calendar.getInstance(), ByteSerializer.writeAsBytes(revocationInformation));
	  return revocationInformation;
  }

  @Override
  public void storeRevocationInformation(URI informationUID,
		  RevocationInformation revocationInformation) throws KeyManagerException {

	  this.ps.insertRevocationInformation(informationUID, 
			  revocationInformation.getRevocationAuthorityParametersUID(), Calendar.getInstance(), 
			  ByteSerializer.writeAsBytes(revocationInformation));
  }

	@Override
	public void storeCurrentRevocationInformation(
			RevocationInformation delegateeElement) throws KeyManagerException {
		// TODO Auto-generated method stub
		// I can't see the use of this method, maybe it shoudl be removed from the interface?
	}

  @Override
  public boolean storeRevocationAuthorityParameters(URI issuid,
      RevocationAuthorityParameters revocationAuthorityParameters) throws KeyManagerException {
    return ps.replaceItem(SimpleParamTypes.REV_AUTH_PARAMS, issuid,
        ByteSerializer.writeAsBytes(revocationAuthorityParameters)) > 0;
  }

  @Override
  public CredentialSpecification getCredentialSpecification(URI credspec)
      throws KeyManagerException {
    return (CredentialSpecification) ByteSerializer.readFromBytes(ps.getItem(
        SimpleParamTypes.CRED_SPEC, credspec));
  }

  @Override
  public boolean storeCredentialSpecification(URI uid,
      CredentialSpecification credentialSpecification) throws KeyManagerException {
    return ps.replaceItem(SimpleParamTypes.CRED_SPEC, uid,
        ByteSerializer.writeAsBytes(credentialSpecification)) > 0;
  }

  private URI DEFAULT_SYSTEM_PARAMETERS = URI.create("default-sp");

  @Override
  public boolean storeSystemParameters(SystemParameters systemParameters)
      throws KeyManagerException {
    return ps.replaceItem(SimpleParamTypes.SYSTEM_PARAMS, DEFAULT_SYSTEM_PARAMETERS,
        ByteSerializer.writeAsBytes(systemParameters)) > 0;
  }

  @Override
  public SystemParameters getSystemParameters() throws KeyManagerException {
    final SystemParameters ret = (SystemParameters) ByteSerializer.readFromBytes(ps.getItem(
        SimpleParamTypes.SYSTEM_PARAMS, DEFAULT_SYSTEM_PARAMETERS));
    if (ret != null && ret.getCryptoParams() != null) {
    	XmlUtils.fixNestedContent(ret.getCryptoParams());
    }
    return ret;
  }

  @Override
  public boolean hasSystemParameters() throws KeyManagerException {
    return getSystemParameters() != null;
  }

  @Override
  public List<URI> listIssuerParameters() throws KeyManagerException {
    return ps.listItems(SimpleParamTypes.ISSUER_PARAMS);
  }
}
