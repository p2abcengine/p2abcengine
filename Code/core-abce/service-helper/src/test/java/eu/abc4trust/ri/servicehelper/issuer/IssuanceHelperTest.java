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

package eu.abc4trust.ri.servicehelper.issuer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.abc4trust.ri.servicehelper.user.UserHelper;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Unit test for simple App.
 */
public class IssuanceHelperTest {
  @Before
  public void alwaysResetEngine() throws Exception {
    IssuanceHelper.resetInstance();
    UserHelper.resetInstance();
  }

  @Test(expected = IllegalStateException.class)
  public void test_notInit() throws Exception {
    IssuanceHelper.getInstance();
  }

  static ObjectFactory of = new ObjectFactory();

  private final SpecAndPolicy idcard_idemix = new SpecAndPolicy("idcard_idemix",
      CryptoTechnology.IDEMIX, null, 10, 0,
      "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml",
      "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml",
      "http://my.country/identitycard/issuancekey_v1.0/idemix");

  private final SpecAndPolicy idcard_uprove = new SpecAndPolicy("idcard_uprove",
      CryptoTechnology.UPROVE, null, 10, 1,
      "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml",
      "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml",
      "http://my.country/identitycard/issuancekey_v1.0/uprove");


  @SuppressWarnings("unused")
  private final SpecAndPolicy idcard_missmatch = new SpecAndPolicy("idcard_missmatch",
      CryptoTechnology.IDEMIX, 10, 0,
      "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationSimpleIdentitycard.xml",
      "/eu/abc4trust/sampleXml/issuance/issuancePolicySimpleIdentitycard.xml");

  private final SpecAndPolicy creditcard_visa = new SpecAndPolicy("creditcard_visa",
      CryptoTechnology.IDEMIX, 10, 0,
      "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocableCreditcard.xml",
      "/eu/abc4trust/sampleXml/issuance/issuancePolicyRevocableCreditcardVisa.xml");
  private final SpecAndPolicy creditcard_amex = new SpecAndPolicy("creditcard_amex",
      CryptoTechnology.IDEMIX, 10, 0,
      "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationRevocableCreditcardAmex.xml",
      "/eu/abc4trust/sampleXml/issuance/issuancePolicyRevocableCreditcardAmex.xml");
  private final SpecAndPolicy passport_ch = new SpecAndPolicy("passport_ch",
      CryptoTechnology.IDEMIX, 10, 0,
      "/eu/abc4trust/sampleXml/credspecs/credentialSpecificationPassport.xml",
      "/eu/abc4trust/sampleXml/issuance/issuancePolicyPassport.xml");

  private final SpecAndPolicy patras_university = new SpecAndPolicy("patras_university",
      CryptoTechnology.IDEMIX, 10, 0,
      "/eu/abc4trust/sampleXml/patras/credentialSpecificationPatrasUniversity.xml",
      "/eu/abc4trust/sampleXml/patras/issuancePolicyPatrasUniversity.xml");
  private final SpecAndPolicy patras_cource = new SpecAndPolicy("patras_course",
      CryptoTechnology.IDEMIX, 10, 0,
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

  private void test_init(String test_name, SpecAndPolicy... list) throws Exception {
    this.test_init(false, test_name, list);
  }

  private void test_init(boolean clearFiles, String test_name, SpecAndPolicy... list)
      throws Exception {
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

    IssuanceHelper.initInstance(1024, systemAndIssuerParamsPrefix, fileStoragePrefix, list,
        new ArrayList<RevocationAuthorityParameters>());
  }

  // @Test()
  public void test_initIssuance() throws Exception {
    IssuanceHelper.resetInstance();
    this.test_init("idcard", this.idcard_idemix);
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
    this.test_init("idcard", this.idcard_idemix);
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
    this.test_init("patras", this.patras_university, this.patras_cource);
  }

  @Test()
  public void test_creditcard_visa() throws Exception {

    IssuanceHelper.resetInstance();
    this.test_init("visa", this.creditcard_visa);
  }

  @Test()
  public void test_creditcard_amex() throws Exception {

    IssuanceHelper.resetInstance();
    this.test_init("amex", this.creditcard_amex);
  }

  @Test()
  public void test_passport_ch() throws Exception {

    IssuanceHelper.resetInstance();
    this.test_init("passport", this.passport_ch);

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

  @Test
  public void test_idcard_init_idemix() throws Exception {

    IssuanceHelper.resetInstance();
    this.test_init("bridged_idcard_idemix", this.idcard_idemix);
    IssuanceHelper helper = IssuanceHelper.getInstance();

    Map<String, Object> attributeValueMap = new HashMap<String, Object>();
    attributeValueMap.put("FirstName", "Hans Guldager");
    attributeValueMap.put("LastName", "Knudsen");
    attributeValueMap.put("Birthday", new Date());

    IssuanceMessage im = helper.initIssuance("idcard_idemix", attributeValueMap);
    System.out
        .println("IssuanceMessage - Idemix : " + XmlUtils.toXml(of.createIssuanceMessage(im)));
  }

  @Test
  public void test_idcard_init_uprove() throws Exception {

    IssuanceHelper.resetInstance();
    this.test_init("bridged_idcard_uprove", this.idcard_uprove);
    IssuanceHelper helper = IssuanceHelper.getInstance();

    Map<String, Object> attributeValueMap = new HashMap<String, Object>();
    attributeValueMap.put("FirstName", "Hans Guldager");
    attributeValueMap.put("LastName", "Knudsen");
    attributeValueMap.put("Birthday", new Date());

    IssuanceMessage im = helper.initIssuance("idcard_uprove", attributeValueMap);
    System.out
        .println("IssuanceMessage - Idemix : " + XmlUtils.toXml(of.createIssuanceMessage(im)));
  }

  @Test
  public void test_bridged_init_both() throws Exception {

    IssuanceHelper.resetInstance();
    this.test_init("bridged_idcard_both", this.idcard_idemix, this.idcard_uprove);
    IssuanceHelper helper = IssuanceHelper.getInstance();

    Map<String, Object> attributeValueMap = new HashMap<String, Object>();
    attributeValueMap.put("FirstName", "Hans Guldager");
    attributeValueMap.put("LastName", "Knudsen");
    attributeValueMap.put("Birthday", new Date());

    IssuanceMessage im_idemix = helper.initIssuance("idcard_idemix", attributeValueMap);
    System.out.println("IssuanceMessage - Idemix : "
        + XmlUtils.toXml(of.createIssuanceMessage(im_idemix)));
    IssuanceMessage im_uprove = helper.initIssuance("idcard_uprove", attributeValueMap);
    System.out.println("IssuanceMessage - UProve : "
        + XmlUtils.toXml(of.createIssuanceMessage(im_uprove)));
  }

}
