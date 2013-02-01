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

package eu.abc4trust.ri.service.it.issuer;

import java.net.URI;
import java.util.List;

import org.junit.Test;

import eu.abc4trust.guice.ProductionModuleFactory.CryptoEngine;
import eu.abc4trust.ri.servicehelper.user.UserHelper;

public class ITSoderhamnPilot extends AbstractIT {

    public ITSoderhamnPilot() {
        System.out.println("ITIssuer");
    }

    private void setupCryptoEngines(CryptoEngine cryptoEngine, CryptoEngine clientEngine,
                                    String pupil) throws Exception {
        initIssuer(cryptoEngine, clientEngine);

        String storagePrefix = pupil.toLowerCase();
        if(cryptoEngine == CryptoEngine.BRIDGED) {
            storagePrefix += "_bridged";
        }
        storagePrefix += "_" + clientEngine.toString().toLowerCase();
        initHelper(cryptoEngine, clientEngine, storagePrefix);
    }


    private void issueSoederhamnCredentials(CryptoEngine cryptoEngine, CryptoEngine clientEngine, String pupil) throws Exception {
        System.out.println("-- issueSoederhamnCredentials - cryptoEngine : " + cryptoEngine + " - clientEngine : " + clientEngine + " - pupil : " + pupil);
        setupCryptoEngines(cryptoEngine, clientEngine, pupil);
        
        String soderhamnScope = "urn:soderhamn:registration";

        this.initPseudonym(clientEngine, soderhamnScope, 42);

        // issue university credential
        this.runIssuance("startSchool", "issuanceKey?pupil=" + pupil, soderhamnScope);

        // issue course credential
        this.runIssuance("startSubject", "issuanceKey?pupil=" + pupil, soderhamnScope);

        List<URI> list = UserHelper.getInstance().credentialManager.listCredentials();
        System.out.println("# of credentials : " + list.size() + " : " + list);

    }

    private void verifySoederhamnCredentials(CryptoEngine cryptoEngine, CryptoEngine clientEngine, String pupil) throws Exception {
        System.out.println("-- issueSoederhamnCredentials - cryptoEngine : " + cryptoEngine + " - clientEngine : " + clientEngine + " - pupil : " + pupil);
        setupCryptoEngines(cryptoEngine, clientEngine, pupil);

        // School credential
        System.out.println("Present Soderhamn Smartcard Pseudonym!");
        String soderhamnScope = "urn:soderhamn:registration";
        this.runVerification(cryptoEngine, clientEngine, "presentationPolicySoderhamnSchool.xml", true, soderhamnScope);
        
        // Subject credential
        String frenchScope = "urn:soderhamn:restrictedarea:french";
        System.out.println("Present Soderhamn Subject Credential - pseudonym being established!");
        this.runVerification(cryptoEngine, clientEngine, "presentationPolicyRASubjectMustBeFrench.xml", true, frenchScope);

        // Subject pseudonym
        System.out.println("Present Soderhamn Subject Credential - pseudonym is used!");
        this.runVerification(cryptoEngine, clientEngine, "presentationPolicyRASubjectMustBeFrench.xml", true, frenchScope);

        // Subject credential - english not satisfied
        String englishScope = "urn:soderhamn:restrictedarea:english";
        System.out.println("Present Soderhamn Subject Credential - Not Satisfied!!");
        this.runVerification(cryptoEngine, clientEngine, "presentationPolicyRASubjectMustBeEnglish.xml", false, englishScope);

    }
    
    @Test
    public void testPupil_Emil_Idemix() throws Exception {
        this.issueSoederhamnCredentials(CryptoEngine.IDEMIX, CryptoEngine.IDEMIX, "Emil");
        this.verifySoederhamnCredentials(CryptoEngine.IDEMIX, CryptoEngine.IDEMIX, "Emil");
    }

    @Test
    public void testPupil_Emil_UProve() throws Exception {
        this.issueSoederhamnCredentials(CryptoEngine.UPROVE, CryptoEngine.UPROVE, "Emil");
        this.verifySoederhamnCredentials(CryptoEngine.UPROVE, CryptoEngine.UPROVE, "Emil");
    }

    @Test
    public void testPupil_Emil_Bridged_Idemix() throws Exception {
        this.issueSoederhamnCredentials(CryptoEngine.BRIDGED, CryptoEngine.IDEMIX, "Emil");
        this.verifySoederhamnCredentials(CryptoEngine.BRIDGED, CryptoEngine.IDEMIX,"Emil");
    }

    // @Test
    public void testPupil_Emil_Bridged_UProve() throws Exception {
        this.issueSoederhamnCredentials(CryptoEngine.BRIDGED, CryptoEngine.UPROVE, "Emil");
        this.verifySoederhamnCredentials(CryptoEngine.BRIDGED, CryptoEngine.UPROVE, "Emil");
    }

}
