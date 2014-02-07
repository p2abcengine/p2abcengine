//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
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

package eu.abc4trust.abce.integrationtests;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.abc4trust.abce.external.issuer.IssuerAbcEngine;
import eu.abc4trust.abce.external.user.UserAbcEngine;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.testharness.IntegrationModuleFactory;
import eu.abc4trust.abce.testharness.IssuanceHelper;
import eu.abc4trust.cryptoEngine.issuer.MockCryptoEngineIssuer;
import eu.abc4trust.cryptoEngine.uprove.util.UProveUtils;
import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * This test tests the (complex) issuance of a CreditCard. The user must present
 * a student ID that is not expired, and must disclose the issuedBy attribute of
 * that card. The generated credit card carries over the names and expiration
 * date of the student card. The credit card number is generated jointly at
 * random. The user chooses the security code of his card. The issuer chooses
 * the card type and the status attributes. (There is also some
 * issuerDrivenRevocation in the issuancePolicy, but this is only incompletely
 * supported by our code until now). This test is similar to the test
 * {@link eu.abc4trust.integrationtest.IssueCredentialTest}.
 * 
 * @see eu.abc4trust.integrationtest.IssueCredentialTest
 */
public class ComplexCredentialIssuanceIntegrationTest {

    private static final String HTTP_VISA_COM_CREDITCARD_REVOCATION_PARAMETERS = "http://visa.com/creditcard/revocation/parameters";
    private static final int SECURITY_CODE = 42;
    private static final String STUDENT = "Student";
    private static final String SWISS_EXPRESS = "SwissExpress";
    private static final URI CREATE = URI.create(STUDENT);
    private static final URI HTTP_VISA_COM_CREDITCARD_SPECIFICATION = URI
            .create("http://visa.com/creditcard/specification");

    @Test()
    public void test() throws Exception {
        // Step 1. Prepare the credential manager with the student id
        // credential.

        Credential studentCard = (Credential) XmlUtils
                .getObjectFromXML(
                        this.getClass()
                        .getResourceAsStream(
                                "/eu/abc4trust/sampleXml/credentials/credentialStudentId.xml"),
                                true);

        CryptoEngine cryptoEngine = CryptoEngine.MOCK;

        Injector issuerInjector = Guice
                .createInjector(IntegrationModuleFactory.newModule(new Random(1984),
                        cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));
        Injector userInjector = Guice.createInjector(IntegrationModuleFactory.newModule(
                new Random(1985), cryptoEngine, UProveUtils.UPROVE_COMMON_PORT));

        // Step 2. Load the credit card specification into the keystore.
        CredentialSpecification creditCard = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        this.getClass()
                        .getResourceAsStream(
                                "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocableCreditcard.xml"),
                                true);
        CredentialSpecification studentcardSpec = (CredentialSpecification) XmlUtils
            .getObjectFromXML(
                    this.getClass()
                    .getResourceAsStream(
                            "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationStudentCardEth.xml"),
                            true);
        
        IssuerParameters iparamStudentCard = new IssuerParameters();
        URI ipuidStudentCard = URI.create("http://ethz.ch/passport/issuancekey_v1.0");
        iparamStudentCard.setParametersUID(ipuidStudentCard);

        IssuerParameters iparamCreditCard = new IssuerParameters();
        URI ipuidCreditCard = URI.create("http://thebestbank.com/cc/issuancekey_v1.0");
        iparamCreditCard.setParametersUID(ipuidCreditCard);
        
        IssuerParameters iparamMock = new IssuerParameters();
        iparamMock.setParametersUID(URI.create(MockCryptoEngineIssuer.ABC4TRUST_EU_MOCKISSUER_PUBLICKEY));

        KeyManager keyManager = issuerInjector.getInstance(KeyManager.class);
        keyManager.storeCredentialSpecification(
                creditCard.getSpecificationUID(), creditCard);
        keyManager.storeCredentialSpecification(
          studentcardSpec.getSpecificationUID(), studentcardSpec);
        keyManager.storeIssuerParameters(ipuidStudentCard, iparamStudentCard);
        keyManager.storeIssuerParameters(ipuidCreditCard, iparamCreditCard);
        keyManager.storeIssuerParameters(iparamMock.getParametersUID(), iparamMock);

        KeyManager userKeyManager = userInjector.getInstance(KeyManager.class);
        userKeyManager.storeCredentialSpecification(
                creditCard.getSpecificationUID(), creditCard);
        userKeyManager.storeCredentialSpecification(
          studentcardSpec.getSpecificationUID(), studentcardSpec);
        userKeyManager.storeIssuerParameters(ipuidStudentCard, iparamStudentCard);
        userKeyManager.storeIssuerParameters(ipuidCreditCard, iparamCreditCard);
        userKeyManager.storeIssuerParameters(iparamMock.getParametersUID(), iparamMock);

        CredentialManager credManager = userInjector
                .getInstance(CredentialManager.class);
        credManager.storeCredential(studentCard);

        // Step 3. Load the issuance policy and attributes.
        IssuancePolicy ip = (IssuancePolicy) XmlUtils
                .getObjectFromXML(
                        this.getClass()
                        .getResourceAsStream(
                                "/eu/abc4trust/sampleXml/issuance/issuancePolicyAdvanced.xml"),
                                true);

        List<Attribute> issuerAtts = new ArrayList<Attribute>();

        this.populateAttributes(issuerAtts);

        // Step 4. Issue a credit card credential.
        IssuerAbcEngine issuerEngine = issuerInjector
                .getInstance(IssuerAbcEngine.class);
        UserAbcEngine userEngine = userInjector
                .getInstance(UserAbcEngine.class);

        IssuanceHelper testHelper = new IssuanceHelper();
        CredentialDescription actualCredDesc = testHelper.runIssuanceProtocol(
                issuerEngine, userEngine, ip, issuerAtts);

        // Step 5. Check if the stored credential is the same as the expected
        // credential.
        assertEquals(actualCredDesc.getCredentialSpecificationUID(),
                HTTP_VISA_COM_CREDITCARD_SPECIFICATION);
        List<Attribute> attributes = actualCredDesc.getAttribute();
        assertEquals(attributes.get(0).getAttributeValue(),
                HTTP_VISA_COM_CREDITCARD_REVOCATION_PARAMETERS);
        assertEquals(attributes.get(1).getAttributeValue(), CREATE);
        assertEquals(attributes.get(2).getAttributeValue(), SWISS_EXPRESS);
        assertEquals(attributes.get(3).getAttributeValue(), SECURITY_CODE);
        assertEquals(attributes.get(4).getAttributeValue(), "Pol");
        assertEquals(attributes.get(5).getAttributeValue(),
                "Fischer");
        //        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        //        gregorianCalendar.set(2005, 4, 31, 0, 0, 0);
        //        gregorianCalendar.set(Calendar.MILLISECOND, 0);
        //        gregorianCalendar.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        //        XMLGregorianCalendar date2 = DatatypeFactory.newInstance()
        //                .newXMLGregorianCalendar(gregorianCalendar);
        assertEquals(attributes.get(6).getAttributeValue().toString(),
                "2005-05-31Z");
    }

    private void populateAttributes(
            List<Attribute> issuerAtts) throws Exception {
        ObjectFactory of = new ObjectFactory();

        Attribute revocation = of.createAttribute();
        revocation.setAttributeUID(new URI("4630887629667084853"));
        revocation
        .setAttributeValue(HTTP_VISA_COM_CREDITCARD_REVOCATION_PARAMETERS);
        revocation.setAttributeDescription(of.createAttributeDescription());
        revocation.getAttributeDescription().setDataType(new URI("xs:string"));
        revocation.getAttributeDescription().setEncoding(
                new URI("urn:abc4trust:1.0:encoding:string:sha-256"));
        revocation.getAttributeDescription().setType(new URI("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"));
        issuerAtts.add(revocation);

        Attribute status = of.createAttribute();
        status.setAttributeUID(new URI("status:blablablabla"));
        status.setAttributeValue(new URI(STUDENT));
        status.setAttributeDescription(of.createAttributeDescription());
        status.getAttributeDescription().setDataType(new URI("xs:anyURI"));
        status.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:anyUri:sha-256"));
        status.getAttributeDescription().setType(new URI("Status"));
        issuerAtts.add(status);

        Attribute cardType = of.createAttribute();
        cardType.setAttributeUID(new URI("card:type:blibliblib"));
        cardType.setAttributeValue(SWISS_EXPRESS);
        cardType.setAttributeDescription(of.createAttributeDescription());
        cardType.getAttributeDescription().setDataType(new URI("xs:string"));
        cardType.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:string:sha-256"));
        cardType.getAttributeDescription().setType(new URI("CardType"));
        issuerAtts.add(cardType);

        Attribute securityCode = of.createAttribute();
        securityCode.setAttributeUID(new URI("security:code:blablabla"));
        securityCode.setAttributeValue(SECURITY_CODE);
        securityCode.setAttributeDescription(of.createAttributeDescription());
        securityCode.getAttributeDescription().setDataType(
                new URI("xs:integer"));
        securityCode.getAttributeDescription().setEncoding(new URI("urn:abc4trust:1.0:encoding:integer:signed"));
        securityCode.getAttributeDescription().setType(new URI("SecurityCode"));
        issuerAtts.add(securityCode);
    }

}
