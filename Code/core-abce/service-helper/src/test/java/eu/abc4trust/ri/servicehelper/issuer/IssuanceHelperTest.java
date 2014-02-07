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

package eu.abc4trust.ri.servicehelper.issuer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Unit test for simple App.
 */
public class IssuanceHelperTest {

    @Test(expected = IllegalStateException.class)
    public void test_notInit() throws Exception {
        IssuanceHelper.getInstance();
    }

    static ObjectFactory of = new ObjectFactory();

    private final SpecAndPolicy idcard = new SpecAndPolicy("idcard",
            "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml",
            "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml");
    @SuppressWarnings("unused")
    private final SpecAndPolicy idcard_missmatch = new SpecAndPolicy(
            "idcard_missmatch",
            "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml",
            "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml");

    private final SpecAndPolicy creditcard_visa = new SpecAndPolicy("creditcard_visa",
            "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocableCreditcard.xml",
            "/eu/abc4trust/sampleXml/issuance/issuancePolicyRevocableCreditcardVisa.xml");
    private final SpecAndPolicy creditcard_amex = new SpecAndPolicy("creditcard_amex",
            "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocableCreditcardAmex.xml",
            "/eu/abc4trust/sampleXml/issuance/issuancePolicyRevocableCreditcardAmex.xml");
    private final SpecAndPolicy passport_ch = new SpecAndPolicy("passport_ch",
            "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPassport.xml",
            "/eu/abc4trust/sampleXml/issuance/issuancePolicyPassport.xml");

    private final SpecAndPolicy patras_university =
            new SpecAndPolicy("patras_university",
                    "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
                    "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml");
    private final SpecAndPolicy patras_cource =
            new SpecAndPolicy("patras_course",
                    "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasCourse.xml",
                    "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasCourse.xml");

    @SuppressWarnings("unused")
    private final String credSpecUID = "http://my.country/identitycard/specification";

    private String getFolderName() {
        if (new File("target").exists()) {
            return "target";
        } else {
            return "service-helper/target";
        }
    }

    private String getCurrentIssuerPrefix(String name) {
        final String file_part_of_name;
        file_part_of_name = "issuer_ut_" + name + "_";
        return this.getFolderName() + "/" + file_part_of_name;
    }

    private void test_init(CryptoEngine cryptoEngine, String test_name, SpecAndPolicy... list) throws Exception {
        this.test_init(cryptoEngine, false, test_name, list);
    }

    private void test_init(CryptoEngine cryptoEngine, boolean clearFiles, String test_name, SpecAndPolicy... list) throws Exception {
        String fileStoragePrefix;

        String folderName = this.getFolderName();
        File folder = new File(folderName);
        fileStoragePrefix = this.getCurrentIssuerPrefix(test_name);

        System.out.println("FILESTORAGE PREFIX!!!" + fileStoragePrefix);
        if (clearFiles && folder.exists()) {
            File[] storage_file_list = folder.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File arg0, String arg1) {
                    if (arg1.startsWith("issuer_ut_")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            System.out.println("DELETE FILES : " + storage_file_list);
            for (File f : storage_file_list) {
                System.out.println("Delete Files : " + f.getAbsolutePath());
                f.delete();
            }
        }

        String systemAndIssuerParamsPrefix = fileStoragePrefix;

        IssuanceHelper.initInstance(cryptoEngine, systemAndIssuerParamsPrefix,
                fileStoragePrefix, list);
    }

    // @Test()
    public void test_initIssuance() throws Exception {
        this.test_init(CryptoEngine.IDEMIX, "idcard" , this.idcard);
        IssuanceHelper helper = IssuanceHelper.getInstance();

        Map<String, Object> attributeValueMap = new HashMap<String, Object>();
        attributeValueMap.put("Firstname", "Hans Guldager");
        attributeValueMap.put("Lastname", "Knudsen");
        attributeValueMap.put("Birthday", new Date());

        helper.initIssuance("idcard", attributeValueMap);
    }

    // @Test()
    public void test_initIssuance_reuse() throws Exception {
        IssuanceHelper.resetInstance();
        this.test_init(CryptoEngine.IDEMIX, "idcard", this.idcard);
        IssuanceHelper helper = IssuanceHelper.getInstance();

        Map<String, Object> attributeValueMap = new HashMap<String, Object>();
        attributeValueMap.put("Firstname", "Hans Guldager");
        attributeValueMap.put("Lastname", "Knudsen");
        attributeValueMap.put("Birthday", new Date());

        helper.initIssuance("idcard", attributeValueMap);
    }

    @Test()
    public void test_patrasCredSpecs() throws Exception {

        IssuanceHelper.resetInstance();
        this.test_init(CryptoEngine.IDEMIX,"patras", this.patras_university, this.patras_cource);
    }

    @Test()
    public void test_creditcard_visa() throws Exception {

        IssuanceHelper.resetInstance();
        this.test_init(CryptoEngine.IDEMIX, "visa", this.creditcard_visa);
    }

    @Test()
    public void test_creditcard_amex() throws Exception {

        IssuanceHelper.resetInstance();
        this.test_init(CryptoEngine.IDEMIX, "amex", this.creditcard_amex);
    }

    @Test()
    public void test_passport_ch() throws Exception {

        IssuanceHelper.resetInstance();
        this.test_init(CryptoEngine.IDEMIX, "passport", this.passport_ch);

        Map<String, Object> attributeValueMap = new HashMap<String, Object>();
        attributeValueMap.put("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
                "http://admin.ch/passport/revocation/parameters");
        attributeValueMap.put("Name", "Alice");
        attributeValueMap.put("LastName", "Nextdoor");
        attributeValueMap.put("PassportNumber", 42);
        Calendar cal = Calendar.getInstance();
        cal.set(2011, 1, 1, 0, 0, 0);
        attributeValueMap.put("Issued", cal.getTime());
        cal.set(2014, 1, 1, 0, 0, 0);
        attributeValueMap.put("Expires", cal.getTime());
        attributeValueMap.put("IssuedBy", "service_issuer_integration_test");

        IssuanceMessage server_im =
                IssuanceHelper.getInstance().initIssuance("passport_ch", attributeValueMap);
        System.out.println(" - initial message - server : "
                + XmlUtils.toXml(of.createIssuanceMessage(server_im), false));

    }

    // @Test()
    public void test_generatePKI() throws Exception {

        IssuanceHelper.resetInstance();
        this.test_init(CryptoEngine.IDEMIX, "visa", this.creditcard_visa);
    }


    ////  @Test()
    //  public void test_generateSigningKeys() throws Exception {
    //    IssuanceHelper.resetInstance();
    //    test_init("visa", creditcard_visa);
    //
    //    System.out.println("test_generateSigningKeys");
    //    String folderName = getFolderName();
    //    long start = System.currentTimeMillis();
    //    System.out.println(" - pki");
    //    IssuanceHelper.getInstance().generateSignatureKeys(folderName, "pki_keys");
    //    System.out.println(" - cas : " + (System.currentTimeMillis()-start));
    //    start = System.currentTimeMillis();
    //    IssuanceHelper.getInstance().generateSignatureKeys(folderName, "cas_keys");
    //    System.out.println(" - DONE : " + (System.currentTimeMillis()-start));
    //  }

    //  @Test()
    //  public void test_generateMacKeys() throws Exception {
    //    System.out.println("test_generateKeys");
    //    String folderName = getFolderName();
    //    long start = System.currentTimeMillis();
    //    IssuanceHelper.generateMacKeys(folderName + "/", 20);
    //    System.out.println(" - DONE : " + (System.currentTimeMillis()-start));
    //  }

    ////  @Test()
    //  public void test_initSmartcard() throws Exception {
    //    IssuanceHelper.resetInstance();
    //    test_init(true, "visa_sc", creditcard_visa);
    //
    //    System.out.println("test_initSmartcard");
    //    long start = System.currentTimeMillis();
    //    String systemAndIssuerParamsPrefix = getCurrentIssuerPrefix("visa_sc");
    //    String signatureKeysFolder = getFolderName();
    //    String signatureKeysPrefix = "pki_keys";
    //
    //
    //    String[] issuerParamsResourceList = { signatureKeysFolder + "/issuer_ut_visa_sc_issuer_params_thebestbank_com_cc_issuancekey_v1.0"};
    //    String fileStoragePrefix = getFolderName() + "/user_smartcard_";
    //    String[] credSpecResourceList = { creditcard_visa.specResource };
    //    UserHelper userHelper = UserHelper.initInstance(CryptoEngine.IDEMIX, issuerParamsResourceList, fileStoragePrefix, credSpecResourceList);
    //
    //    userHelper.initSmartCard_Software(systemAndIssuerParamsPrefix, signatureKeysFolder + "/issuer_ut_visa_sc_issuer_params_thebestbank_com_cc_issuancekey_v1.0", signatureKeysFolder, signatureKeysPrefix);
    //    System.out.println(" - DONE : " + (System.currentTimeMillis()-start));
    //
    //  }

    @Test(expected=IllegalStateException.class)
    public void test_engine_mismatch() throws Exception {

        IssuanceHelper.resetInstance();
        this.test_init(CryptoEngine.IDEMIX, "bridged_engine_mismatch" , this.idcard);
        IssuanceHelper helper = IssuanceHelper.getInstance();

        Map<String, Object> attributeValueMap = new HashMap<String, Object>();
        attributeValueMap.put("Firstname", "Hans Guldager");
        attributeValueMap.put("Lastname", "Knudsen");
        attributeValueMap.put("Birthday", new Date());

        helper.initIssuance(CryptoEngine.UPROVE, "idcard", attributeValueMap);
    }


    @Test(expected=IllegalStateException.class)
    public void test_bridged_wrongusage() throws Exception {

        IssuanceHelper.resetInstance();
        this.test_init(CryptoEngine.BRIDGED, "bridged_idcard_wrongusage" , this.idcard);
        IssuanceHelper helper = IssuanceHelper.getInstance();

        Map<String, Object> attributeValueMap = new HashMap<String, Object>();
        attributeValueMap.put("Firstname", "Hans Guldager");
        attributeValueMap.put("Lastname", "Knudsen");
        attributeValueMap.put("Birthday", new Date());

        helper.initIssuance("idcard", attributeValueMap);
    }

    //    @Test
    public void test_bridged_init_uprove() throws Exception {

        IssuanceHelper.resetInstance();
        this.test_init(CryptoEngine.BRIDGED, "bridged_idcard_uprove" , this.idcard);
        IssuanceHelper helper = IssuanceHelper.getInstance();

        Map<String, Object> attributeValueMap = new HashMap<String, Object>();
        attributeValueMap.put("Firstname", "Hans Guldager");
        attributeValueMap.put("Lastname", "Knudsen");
        attributeValueMap.put("Birthday", new Date());

        IssuanceMessage im = helper.initIssuance(CryptoEngine.UPROVE, "idcard", attributeValueMap);
        System.out.println("IssuanceMessage - UProve : " + XmlUtils.toXml(of.createIssuanceMessage(im)));
    }

    @Test
    @Ignore
    public void test_bridged_init_idemix() throws Exception {

        IssuanceHelper.resetInstance();
        this.test_init(CryptoEngine.BRIDGED, "bridged_idcard_idemix" , this.idcard);
        IssuanceHelper helper = IssuanceHelper.getInstance();

        Map<String, Object> attributeValueMap = new HashMap<String, Object>();
        attributeValueMap.put("Firstname", "Hans Guldager");
        attributeValueMap.put("Lastname", "Knudsen");
        attributeValueMap.put("Birthday", new Date());

        IssuanceMessage im = helper.initIssuance(CryptoEngine.IDEMIX, "idcard", attributeValueMap);
        System.out.println("IssuanceMessage - Idemix : " + XmlUtils.toXml(of.createIssuanceMessage(im)));
    }

    @Test
    @Ignore
    public void test_bridged_init_both() throws Exception {

        IssuanceHelper.resetInstance();
        this.test_init(CryptoEngine.BRIDGED, "bridged_idcard_both" , this.idcard);
        IssuanceHelper helper = IssuanceHelper.getInstance();

        Map<String, Object> attributeValueMap = new HashMap<String, Object>();
        attributeValueMap.put("Firstname", "Hans Guldager");
        attributeValueMap.put("Lastname", "Knudsen");
        attributeValueMap.put("Birthday", new Date());

        IssuanceMessage im_idemix = helper.initIssuance(CryptoEngine.IDEMIX, "idcard", attributeValueMap);
        System.out.println("IssuanceMessage - Idemix : " + XmlUtils.toXml(of.createIssuanceMessage(im_idemix)));
        IssuanceMessage im_uprove = helper.initIssuance(CryptoEngine.UPROVE, "idcard", attributeValueMap);
        System.out.println("IssuanceMessage - UProve : " + XmlUtils.toXml(of.createIssuanceMessage(im_uprove)));
    }

    //        IssuanceHelper.resetInstance();
    //        this.test_init(CryptoEngine.IDEMIX, "passport", this.passport_ch);
    //
    //        Map<String, Object> attributeValueMap = new HashMap<String, Object>();
    //        attributeValueMap.put("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
    //                "http://admin.ch/passport/revocation/parameters");
    //        attributeValueMap.put("Name", "Alice");
    //        attributeValueMap.put("LastName", "Nextdoor");
    //        attributeValueMap.put("PassportNumber", 42);
    //        Calendar cal = Calendar.getInstance();
    //        cal.set(2011, 1, 1, 0, 0, 0);
    //        attributeValueMap.put("Issued", cal.getTime());
    //        cal.set(2014, 1, 1, 0, 0, 0);
    //        attributeValueMap.put("Expires", cal.getTime());
    //        attributeValueMap.put("IssuedBy", "service_issuer_integration_test");
    //
    //        IssuanceMessage server_im =
    //                IssuanceHelper.getInstance().initIssuance("passport_ch", attributeValueMap);
    //        System.out.println(" - initial message - server : "
    //                + XmlUtils.toXml(of.createIssuanceMessage(server_im), false));
    //
    //    }

}
