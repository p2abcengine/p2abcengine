//* Licensed Materials - Property of                                  *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
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

package eu.abc4trust.services.helpers;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.smartcard.BasicSmartcard;
import eu.abc4trust.smartcard.CardStorage;
import eu.abc4trust.smartcard.SoftwareSmartcard;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

public class UserDebugger {

    private final UserHelper instance;

    private final ObjectFactory of = new ObjectFactory();

    public UserDebugger(UserHelper instance) {
        this.instance = instance;
    }

    public void validate(URI issuerParametersUid) {
        this.printSmartcardSecretsHash();
        this.printSystemParametersHash();
        this.printIssuerParametersHash(issuerParametersUid);
    }

    private void printIssuerParametersHash(URI uid) {
        KeyManager keyManager = this.instance.keyManager;
        try {
            IssuerParameters issuerParameters = keyManager
                    .getIssuerParameters(uid);
            if (issuerParameters != null) {
                String hash = this.getHash(issuerParameters);
                System.out.println("IssuerParameters hash: " + hash);
            }
        } catch (KeyManagerException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String getHash(IssuerParameters issuerParameters) {
        MessageDigest md;
        String hash = "";
        try {
            md = MessageDigest.getInstance("SHA-256");

            JAXBElement<IssuerParameters> element = this.of
                    .createIssuerParameters(issuerParameters);
            String xml = XmlUtils.toNormalizedXML(element);
            md.update(xml.getBytes(Charset.forName("UTF-8")));
            byte[] mdbytes = md.digest();

            // convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (byte mdbyte : mdbytes) {
                sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16)
                        .substring(1));
            }

            hash = sb.toString();
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
        return hash;
    }

    private void printSystemParametersHash() {
        KeyManager keyManager = this.instance.keyManager;
        try {
            SystemParameters systemParameters = keyManager
                    .getSystemParameters();
            if (systemParameters != null) {
                String hash = SystemParametersUtil
                        .getHashOfSystemParameters(systemParameters);
                System.out.println("SystemParameters hash: " + hash);
            }
        } catch (KeyManagerException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void printSmartcardSecretsHash() {
        CardStorage cardStorage = this.instance.cardStorage;
        Map<URI, BasicSmartcard> smartcards = cardStorage.getSmartcards();
        Collection<BasicSmartcard> valueSet = smartcards.values();
        for (BasicSmartcard basicSmartcard : valueSet) {
            if (basicSmartcard instanceof SoftwareSmartcard) {
                String hash = ((SoftwareSmartcard) basicSmartcard)
                        .getHashOfDeviceSecret();
                System.out.println("Smart card secret hash: " + hash);
                String systemParametersHash = ((SoftwareSmartcard) basicSmartcard)
                        .getHashOfSystemParameters();
                System.out.println("Smart card systemParametersHash: "
                        + systemParametersHash);
                String issuerParametersHash = ((SoftwareSmartcard) basicSmartcard)
                        .getHashOfIssuerParameters();
                System.out.println("Smart card issuerParametersHash: "
                        + issuerParametersHash);
                String credentialKeysHash = ((SoftwareSmartcard) basicSmartcard)
                        .getHashOfCredentialKeys();
                System.out.println("Smart card credentialKeysHash: "
                        + credentialKeysHash);
            }
        }
    }
    //
    // private void printIssuerPublicKey() {
    //
    // IssuerPublicKey ipk = (IssuerPublicKey) StructureStore.getInstance()
    // .get(issuerPublicKeyId);
    //
    // }

}
