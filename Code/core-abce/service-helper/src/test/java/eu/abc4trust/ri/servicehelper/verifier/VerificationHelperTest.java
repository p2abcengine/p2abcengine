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
import java.io.File;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.abc4trust.cryptoEngine.util.SystemParametersUtil;
import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Unit test for simple App.
 */
@SuppressWarnings("unused")
public class VerificationHelperTest {

	ObjectFactory of = new ObjectFactory();

	@Test(expected = IllegalStateException.class)
	public void test_notInit() throws Exception {
		VerificationHelper.resetInstance();
		VerificationHelper.getInstance();
	}

	// @Test
	// public void test_notIsInit() throws Exception {
	// boolean isInit = VerificationHelper.isInit();
	// Assert.assertFalse(isInit);
	// }


	private String[] presentationPolicyResources =
		{"/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyPatrasCourseEvaluation.xml",
			"/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicySimpleIdentitycard.xml",
		"/eu/abc4trust/sampleXml/presentationPolicies/presentationPolicyAlternativesHotelBooking.xml"};

	private String[] presentationPolicy = {"presentationPolicyPatrasCourseEvaluation.xml",
			"presentationPolicySimpleIdentitycard.xml", "presentationPolicyAlternativesHotelBooking.xml"};

	private String[] credSpecResourceList =
		{"/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml"};

	@Test()
	public void test_init() throws Exception {
		System.out.println("---- test_init ---- ");
		VerificationHelper.resetInstance();
		
		String fileStoragePrefix;
		if (new File("target").exists()) {
			fileStoragePrefix = "target/verifier_";
		} else {
			fileStoragePrefix = "service-helper/target/verifier_";
		}

		// String systemParamsResource = null;
		String systemParamResource = fileStoragePrefix + "_" + UserHelper.SYSTEM_PARAMS_XML_NAME;
		SystemParameters systemParameters = SystemParametersUtil.getDefaultSystemParameters_1024();

		FileSystem.storeObjectAsXMLInFile(of.createSystemParameters(systemParameters),
				systemParamResource);

		List<CredentialSpecification> credSpecList =
				FileSystem.loadXmlListFromResources(credSpecResourceList);
		List<IssuerParameters> issuerParamsList = null;
		List<InspectorPublicKey> inspectorPublicKeyList = null;
		List<RevocationAuthorityParameters> revAuthParamsList = null;

		VerificationHelper.initInstance(systemParameters, issuerParamsList, credSpecList,
				inspectorPublicKeyList, revAuthParamsList, fileStoragePrefix, presentationPolicyResources);
	}

	@Test()
	public void test_initIssuance() throws Exception {
		System.out.println("---- test_initIssuance ---- ");
		initHelper();
		VerificationHelper helper = VerificationHelper.getInstance();

		byte[] nonce = VerificationHelper.getInstance().generateNonce();
		PresentationPolicyAlternatives xml =
				helper.createPresentationPolicy("presentationPolicySimpleIdentitycard.xml", nonce, null,
						null);

		System.out.println("test_initIssuance  XML : "
				+ XmlUtils.toXml(of.createPresentationPolicyAlternatives(xml)));
	}


	@Test()
	public void test_initApplicationData() throws Exception {
		System.out.println("---- test_initApplicationData ---- ");
		initHelper();
		VerificationHelper helper = VerificationHelper.getInstance();

		byte[] nonce = VerificationHelper.getInstance().generateNonce();
		PresentationPolicyAlternatives ppa =
				helper.createPresentationPolicy("presentationPolicySimpleIdentitycard.xml", nonce, null,
						null);

		// PresentationPolicyAlternatives ppa =
		// helper.createPresentationPolicy("presentationPolicyAlternativesHotelBooking.xml", null);

		System.out.println("PresentationPolicyAlternatives : " + ppa);
		System.out.println("- original  : "
				+ ppa.getPresentationPolicy().get(0).getMessage().getApplicationData().getContent().size());

		String xml = XmlUtils.toXml(of.createPresentationPolicyAlternatives(ppa));
		System.out.println("XML : " + xml);

		PresentationPolicyAlternatives converted =
				(PresentationPolicyAlternatives) XmlUtils.getJaxbElementFromXml(
						new ByteArrayInputStream(xml.getBytes()), true).getValue();
		System.out.println("- converted : "
				+ converted.getPresentationPolicy().get(0).getMessage().getApplicationData().getContent()
				.size());

	}

	@Test()
	public void pactchXml() {
		System.out.println("---- pactchXml ---- ");
		String xml =
				"<abc:ConstantValue xmlns=\"http://abc4trust.eu/wp2/abcschemav1.0\">1994-01-06Z</abc:ConstantValue>";

		String patched =
				xml.replace("ConstantValue xmlns=\"http://abc4trust.eu/wp2/abcschemav1.0\"",
						"ConstantValue");
		System.out.println("XML : " + patched);
	}

	private void initHelper() throws Exception{
		if (VerificationHelper.isInit()) {
			return;
		}
		String fileStoragePrefix;
		if (new File("target").exists()) {
			fileStoragePrefix = "target/verifier_";
		} else {
			fileStoragePrefix = "service-helper/target/verifier_";
		}

		// String systemParamsResource = null;
		String systemParamResource = fileStoragePrefix + "_" + UserHelper.SYSTEM_PARAMS_XML_NAME;
		SystemParameters systemParameters = SystemParametersUtil.getDefaultSystemParameters_1024();

		FileSystem.storeObjectAsXMLInFile(of.createSystemParameters(systemParameters),
				systemParamResource);

		List<CredentialSpecification> credSpecList =
				FileSystem.loadXmlListFromResources(credSpecResourceList);
		List<IssuerParameters> issuerParamsList = null;
		List<InspectorPublicKey> inspectorPublicKeyList = null;
		List<RevocationAuthorityParameters> revAuthParamsList = null;

		VerificationHelper.initInstance(systemParameters, issuerParamsList, credSpecList,
				inspectorPublicKeyList, revAuthParamsList, fileStoragePrefix, presentationPolicyResources);

	}

}
