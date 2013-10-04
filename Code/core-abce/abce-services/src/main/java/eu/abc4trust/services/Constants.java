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

package eu.abc4trust.services;

import java.io.File;
import java.net.URI;

import eu.abc4trust.ri.servicehelper.issuer.SpecAndPolicy;

public class Constants {

    private static String BASE_FOLDER = "";
    static {
        if (new File("target").exists()) {
            BASE_FOLDER = "target/";
        } else {
            BASE_FOLDER = "abce-services/target/";
        }
    }
    public static final String SCOPE_STRING = "urn:soderhamn:registration";
    public static final URI SCOPE_URI = URI.create(SCOPE_STRING);

    public static final String ISSUER_RESOURCES_FOLDER = BASE_FOLDER + "issuer_resources";
    public static final String ISSUER_STORAGE_FOLDER = BASE_FOLDER + "issuer_storage";

    public static final String REVOCATION_STORAGE_FOLDER = BASE_FOLDER + "revocation_storage";

    public static final String CREDENTIAL_SPECIFICATION_FOLDER = BASE_FOLDER + "xml";

    public static final String INSPECTOR_STORAGE_FOLDER = BASE_FOLDER + "inspector_storage";

    public static final String USER_STORAGE_FOLDER = BASE_FOLDER + "user_storage";

    public static final String VERIFIER_STORAGE_FOLDER = BASE_FOLDER + "verifier_storage";

    public static final URI SODERHAMN_REVOCATION_AUTHORITY = URI.create("urn:soderhamn:revocationauthority:default");

    public static final String SAP_ID_CHILD = "credSpecUniversity";
    public static final String SAP_ID_CLASS = "credSpecClass";
    public static final String SAP_ID_GUARDIAN = "credSpecGurardian";
    public static final String SAP_ID_ROLE = "credSpecRole";
    public static final String SAP_ID_SCHOOL = "credSpecSchool";
    public static final String SAP_ID_SUBJECT = "credSpecSubject";

    public static final SpecAndPolicy SAP_CHILD = new SpecAndPolicy(SAP_ID_CHILD, "/xml/credentialSpecificationSoderhamnChild.xml","/xml/issuancePolicySoderhamnChild.xml", null, SODERHAMN_REVOCATION_AUTHORITY.toString());
    public static final SpecAndPolicy SAP_CLASS = new SpecAndPolicy(SAP_ID_CLASS, "/xml/credentialSpecificationSoderhamnClass.xml","/xml/issuancePolicySoderhamnClass.xml", null, SODERHAMN_REVOCATION_AUTHORITY.toString());
    public static final SpecAndPolicy SAP_GUARDIAN = new SpecAndPolicy(SAP_ID_GUARDIAN, "/xml/credentialSpecificationSoderhamnGuardian.xml","/xml/issuancePolicySoderhamnGuardian.xml", null, SODERHAMN_REVOCATION_AUTHORITY.toString());
    public static final SpecAndPolicy SAP_ROLE = new SpecAndPolicy(SAP_ID_ROLE, "/xml/credentialSpecificationSoderhamnRole.xml","/xml/issuancePolicySoderhamnRole.xml", null, SODERHAMN_REVOCATION_AUTHORITY.toString());
    public static final SpecAndPolicy SAP_SCHOOL = new SpecAndPolicy(SAP_ID_SCHOOL, "/xml/credentialSpecificationSoderhamnSchool.xml","/xml/issuancePolicySoderhamnSchool.xml", null, SODERHAMN_REVOCATION_AUTHORITY.toString());
    public static final SpecAndPolicy SAP_SUBJECT = new SpecAndPolicy(SAP_ID_SUBJECT, "/xml/credentialSpecificationSoderhamnSubject.xml","/xml/issuancePolicySoderhamnSubject.xml", null, SODERHAMN_REVOCATION_AUTHORITY.toString());

    public static final String[] CRED_SPEC_RESOURCE_LIST =
        { "/xml/credentialSpecificationSoderhamnChild.xml",
        "/xml/credentialSpecificationSoderhamnClass.xml",
        "/xml/credentialSpecificationSoderhamnGuardian.xml",
        "/xml/credentialSpecificationSoderhamnRole.xml",
        "/xml/credentialSpecificationSoderhamnSchool.xml",
        "/xml/credentialSpecificationSoderhamnSubject.xml" };

    public static final String[] INSPECTION_CRED_SPEC_RESOURCE_LIST =
        { "/xml/credentialSpecificationSoderhamnSchool.xml" };


    public static final String ISSUER_PARAM_RESOURCE_CHILD_IDEMIX = Constants.ISSUER_RESOURCES_FOLDER + "/issuer_params_urn_soderhamn_issuer_credChild_idemix";
    public static final String ISSUER_PARAM_RESOURCE_CLASS_IDEMIX = Constants.ISSUER_RESOURCES_FOLDER + "/issuer_params_urn_soderhamn_issuer_credClass_idemix";
    public static final String ISSUER_PARAM_RESOURCE_GUARDIAN_IDEMIX = Constants.ISSUER_RESOURCES_FOLDER + "/issuer_params_urn_soderhamn_issuer_credGuardian_idemix";
    public static final String ISSUER_PARAM_RESOURCE_ROLE_IDEMIX = Constants.ISSUER_RESOURCES_FOLDER + "/issuer_params_urn_soderhamn_issuer_credRole_idemix";
    public static final String ISSUER_PARAM_RESOURCE_SCHOOL_IDEMIX = Constants.ISSUER_RESOURCES_FOLDER + "/issuer_params_urn_soderhamn_issuer_credSchool_idemix";
    public static final String ISSUER_PARAM_RESOURCE_SUBJECT_IDEMIX = Constants.ISSUER_RESOURCES_FOLDER + "/issuer_params_urn_soderhamn_issuer_credSubject_idemix";

    public static final String ISSUER_PARAM_RESOURCE_CHILD_UPROVE = Constants.ISSUER_RESOURCES_FOLDER + "/issuer_params_urn_soderhamn_issuer_credChild_uprove";
    public static final String ISSUER_PARAM_RESOURCE_CLASS_UPROVE = Constants.ISSUER_RESOURCES_FOLDER + "/issuer_params_urn_soderhamn_issuer_credClass_uprove";
    public static final String ISSUER_PARAM_RESOURCE_GUARDIAN_UPROVE = Constants.ISSUER_RESOURCES_FOLDER + "/issuer_params_urn_soderhamn_issuer_credGuardian_uprove";
    public static final String ISSUER_PARAM_RESOURCE_ROLE_UPROVE = Constants.ISSUER_RESOURCES_FOLDER + "/issuer_params_urn_soderhamn_issuer_credRole_uprove";
    public static final String ISSUER_PARAM_RESOURCE_SCHOOL_UPROVE = Constants.ISSUER_RESOURCES_FOLDER + "/issuer_params_urn_soderhamn_issuer_credSchool_uprove";
    public static final String ISSUER_PARAM_RESOURCE_SUBJECT_UPROVE = Constants.ISSUER_RESOURCES_FOLDER + "/issuer_params_urn_soderhamn_issuer_credSubject_uprove";


    public static final String[] ISSUER_PARAMS_RESOURCE_LIST = {
        ISSUER_PARAM_RESOURCE_CHILD_IDEMIX,
        ISSUER_PARAM_RESOURCE_CLASS_IDEMIX,
        ISSUER_PARAM_RESOURCE_GUARDIAN_IDEMIX,
        ISSUER_PARAM_RESOURCE_ROLE_IDEMIX,
        ISSUER_PARAM_RESOURCE_SCHOOL_IDEMIX,
        ISSUER_PARAM_RESOURCE_SUBJECT_IDEMIX,

        ISSUER_PARAM_RESOURCE_CHILD_UPROVE,
        ISSUER_PARAM_RESOURCE_CLASS_UPROVE,
        ISSUER_PARAM_RESOURCE_GUARDIAN_UPROVE,
        ISSUER_PARAM_RESOURCE_ROLE_UPROVE,
        ISSUER_PARAM_RESOURCE_SCHOOL_UPROVE,
        ISSUER_PARAM_RESOURCE_SUBJECT_UPROVE
    };

    public static final String[] IDEMIX_ISSUER_PARAMS_RESOURCE_LIST = {
        ISSUER_PARAM_RESOURCE_CHILD_IDEMIX,
        ISSUER_PARAM_RESOURCE_CLASS_IDEMIX,
        ISSUER_PARAM_RESOURCE_GUARDIAN_IDEMIX,
        ISSUER_PARAM_RESOURCE_ROLE_IDEMIX,
        ISSUER_PARAM_RESOURCE_SCHOOL_IDEMIX,
        ISSUER_PARAM_RESOURCE_SUBJECT_IDEMIX,

    };
    public static final String[] UPROVE_ISSUER_PARAMS_RESOURCE_LIST = {
        ISSUER_PARAM_RESOURCE_CHILD_UPROVE,
        ISSUER_PARAM_RESOURCE_CLASS_UPROVE,
        ISSUER_PARAM_RESOURCE_GUARDIAN_UPROVE,
        ISSUER_PARAM_RESOURCE_ROLE_UPROVE,
        ISSUER_PARAM_RESOURCE_SCHOOL_UPROVE,
        ISSUER_PARAM_RESOURCE_SUBJECT_UPROVE
    };

    public static final URI[] INSPECTOR_PUBLIC_KEY_UIDs = {URI.create("urn:soderhamn:inspectorpk")};
    public static final String[] INSPECTOR_PUBLIC_KEY_RESOURCE_LIST = {
        Constants.ISSUER_RESOURCES_FOLDER + "/inspector_publickey_urn_soderhamn_inspectorpk"
    };

    public static final String INSPECTOR_PRIVATE_KEY = Constants.INSPECTOR_STORAGE_FOLDER + "/inspector_privatekey_urn_soderhamn_inspectorpk";

    public static final String SYSTEM_PARAMETER_RESOURCE = Constants.ISSUER_STORAGE_FOLDER + "/system_params_bridged";

}

