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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

import com.google.inject.Inject;

import eu.abc4trust.cryptoEngine.user.credCompressor.CompressorUtils;
import eu.abc4trust.smartcard.BasicSmartcard;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.smartcard.Smartcard;
import eu.abc4trust.smartcard.SmartcardBlob;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;

/**
 * 
 * Handles storage and retrival of reloadTokens related information from the ABC storage (e.g. Smartcard)
 *
 */
public class ReloadStorageManager {
	static private final String ENCODED_EMPTY_STRING = "EMPTY"; 
	static private final String URI_POSTFIX = ":S:UPROVERELOAD"; 
	static final private String ENCODING = "UTF-8";
	
	private final CardStorage cardStorage;

	@Inject
	public ReloadStorageManager(CardStorage cardStorage) {
		this.cardStorage = cardStorage;
	}
	
	/**
	 * store info + Urls on smartcard. If you change the serialize order, you should probably also change the order in retrive
	 * 
	 * @param credDesc
	 * @param issuanceUrl
	 * @param issuanceStepUrl
	 * @param info - we assume none of the lists in this structure are null
	 */
	public void store(CredentialDescription credDesc, String issuanceUrl, String issuanceStepUrl, ReloadInformation info){
		info.issuanceUrl = issuanceUrl;
		info.issuanceStepUrl = issuanceStepUrl;
		
		StringBuilder sb = new StringBuilder((2+info.creduids.size() + info.pseudonyms.size() + info.inspectors.size())*100);
		sb.append(encode(info.issuanceUrl));
		sb.append("\n");
		sb.append(encode(info.issuanceStepUrl));
		sb.append("\n");
		for (URI uri : info.creduids) {
			sb.append(encode(uri.toString()));
			sb.append("\n");
		}
		sb.append("\n");
		for (URI uri : info.pseudonyms) {
			sb.append(encode(uri.toString()));
			sb.append("\n");
		}
		sb.append("\n");
		for (URI uri : info.inspectors) {
			sb.append(encode(uri.toString()));
			sb.append("\n");
		}
		
		putStringToStorage(credDesc, sb.toString());
	}

	/**
	 * Get info + Urls on smartcard. If you change the de-serialize order, you should probably also change the order in store
	 * @param cred
	 * @return
	 */
	public ReloadInformation retrive(Credential cred) {
		ReloadInformation ret = new ReloadInformation();
		
		String fromStorage = getStringFromStorage(cred);
		String[] strings = fromStorage.split("\n");
		
		
		ret.issuanceUrl = decode(strings[0]);
		ret.issuanceStepUrl = decode(strings[1]);
		int i = 2;
		while (i<strings.length && !strings[i].isEmpty()) {
			ret.creduids.add(getURI(strings[i]));
			i++;
		}
		i++;
		while (i<strings.length && !strings[i].isEmpty()) {
			ret.pseudonyms.add(getURI(strings[i]));
			i++;
		}
		i++;
		while (i<strings.length && !strings[i].isEmpty()) { //last string is empty
			ret.inspectors.add(getURI(strings[i]));
			i++;
		}
		//If we have not reached the end of strings, then this is a cause for concern. 
		//However we choose to continue, with the data we have in ret.
		if (i<strings.length-1)
			System.out.println("WARN: ReloadStorageManager.retrive, premature end of data found. Data may be invalid.");
		return ret;
	}
	
	private String getStringFromStorage(Credential cred) {
		URI credUid = cred.getCredentialDescription().getCredentialUID();
		URI cardUid = cred.getCredentialDescription().getSecretReference();

		Smartcard sc = (Smartcard) cardStorage.getSmartcards().get(cardUid);
		if (sc==null)
			throw new ReloadTokensCommunicationStrategy.ReloadException("could not obtain smartcard for credential. Reload of tokens failed.");

		URI storeUri = null;
		try {
			storeUri = new URI(credUid.toString() + URI_POSTFIX);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new ReloadTokensCommunicationStrategy.ReloadException("Failed to encode issuer storage uri for smartcard");
		}
		SmartcardBlob blob = sc.getBlob(cardStorage.getPin(cardUid), storeUri);
		if (blob==null || blob.blob==null || blob.blob.length==0)
			throw new ReloadTokensCommunicationStrategy.ReloadException("could not obtain issuer url for credential. Reload of tokens failed.");

		return CompressorUtils.readStringCompressed(new ByteArrayInputStream(blob.blob));
	}
	
	private void putStringToStorage(CredentialDescription credDesc, String str) {
		URI SCuri = getCurrentSmartcardUri();
		Smartcard sc = (Smartcard) cardStorage.getSmartcards().get(SCuri);

		URI storeUri = null;
		try {
			storeUri = new URI(credDesc.getCredentialUID().toString() + URI_POSTFIX);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to encode issuer storage uri for smartcard");
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream(str.length()/2);

		CompressorUtils.writeStringCompressed(baos, str);
		SmartcardBlob blob = new SmartcardBlob();
		blob.blob = baos.toByteArray();
		sc.storeBlob(cardStorage.getPin(SCuri), storeUri, blob);
	}
	
	/**
	 * UrlEncode string to escape \n. Handles empty string
	 * @param str
	 * @return
	 */
	private String encode(String str)  {
		try {
			String ret = URLEncoder.encode(str, ENCODING);
			if (ret.isEmpty())
				return ENCODED_EMPTY_STRING;
			if (ret.startsWith(ENCODED_EMPTY_STRING))
				return ENCODED_EMPTY_STRING+ret;
			return ret;
		} catch (UnsupportedEncodingException e) {
			//UTF-8 encoding is always supported (and recommended for URLEncode)
			throw new RuntimeException("UnsupportedEncodingException", e); //so this shouldn't happen
		}
	}
	
	/**
	 * UrlDecode string to escape \n. Handles empty string
	 * @param str
	 * @return
	 */
	private String decode(String str) {
		try {
			String ret = URLDecoder.decode(str, ENCODING);
			if (ret.startsWith(ENCODED_EMPTY_STRING))
				return ret.substring(ENCODED_EMPTY_STRING.length());
			return ret;
		} catch (UnsupportedEncodingException e) {
			//UTF-8 encoding is always supported (and recommended for URLEncode/decode)
			throw new RuntimeException("UnsupportedEncodingException", e); //so this shouldn't happen
		}
		
	}
	
	private URI getURI(String uriString) {
		try {
			return new URI(uriString);
		} catch (URISyntaxException e) {
			throw new ReloadTokensCommunicationStrategy.ReloadException("ReloadTokens: Malformed data received from storage (smartcard). Cannot reload tokens.");
		}
	}

	/**
	 * Get the current smartcard uri from cardStorage
	 * @return
	 */
	private URI getCurrentSmartcardUri(){
		Map<URI, BasicSmartcard> scs = cardStorage.getSmartcards();
		for(URI uri : scs.keySet()){
			if((scs.get(uri) instanceof Smartcard)){    			
				return uri; //We require minimum Smartcard. BasicSmartcard is only used in tests
			}
		}
		return null;
	}

}
