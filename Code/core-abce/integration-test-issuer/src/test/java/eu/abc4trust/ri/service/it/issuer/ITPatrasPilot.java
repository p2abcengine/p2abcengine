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

public class ITPatrasPilot extends AbstractIT {

    public ITPatrasPilot() {
        System.out.println("ITIssuer");
    }

    private void setupCryptoEngines(CryptoEngine cryptoEngine, CryptoEngine clientEngine,
            int matNumber) throws Exception {
        this.initIssuer(cryptoEngine, clientEngine);

        String storagePrefix = "student_" + matNumber;
        if(cryptoEngine == CryptoEngine.BRIDGED) {
            storagePrefix += "_bridged";
        }
        storagePrefix += "_" + clientEngine.toString().toLowerCase() + "_";

        this.initHelper(cryptoEngine, clientEngine, storagePrefix);
    }

    private void issuePatrasCredentials(CryptoEngine cryptoEngine, CryptoEngine clientEngine, int matNumber) throws Exception {
        System.out.println("-- issuePatrasCredentials - cryptoEngine : " + cryptoEngine + " - clientEngine : " + clientEngine + " - matNumber : " + matNumber);

        this.setupCryptoEngines(cryptoEngine, clientEngine, matNumber);

        String scope = "urn:patras:registration";
        this.initPseudonym(clientEngine, scope, matNumber);

        // issue university credential
        this.runIssuance("startUniversity", "issuanceKey?matriculationnumber=" + matNumber, scope);

        String courceScope = "urn:patras:evaluation";
        // issue course credential
        this.runIssuance("startCourse", "issuanceKey?matriculationnumber=" + matNumber, courceScope);

        List<URI> list = UserHelper.getInstance().credentialManager.listCredentials();
        System.out.println("# of credentials : " + list.size() + " : " + list);

    }

    private void verifyPatrasCredentials(CryptoEngine cryptoEngine, CryptoEngine clientEngine, int matNumber) throws Exception {
        System.out.println("-- verifyPatrasCredentials - cryptoEngine : " + cryptoEngine + " - clientEngine : " + clientEngine + " - matNumber : " + matNumber);

        this.setupCryptoEngines(cryptoEngine, clientEngine, matNumber);

        //
        String scope = "urn:patras:registration";
        this.runVerification(cryptoEngine, clientEngine, "presentationPolicyPatrasUniversityLogin.xml", true, scope);

        //
        String courceScope = "urn:patras:evaluation";
        this.runVerification(cryptoEngine, clientEngine, "presentationPolicyPatrasCourseEvaluation.xml", true, courceScope);

    }

    @Test
    public void testStudent_42_idemix() throws Exception {
        this.copySystemParameters();
        this.issuePatrasCredentials(CryptoEngine.IDEMIX, CryptoEngine.IDEMIX, 42);
        this.verifyPatrasCredentials(CryptoEngine.IDEMIX, CryptoEngine.IDEMIX, 42);
    }

    @Test
    public void testStudent_42_uprove() throws Exception {
        this.issuePatrasCredentials(CryptoEngine.UPROVE, CryptoEngine.UPROVE, 42);
        this.verifyPatrasCredentials(CryptoEngine.UPROVE, CryptoEngine.UPROVE, 42);
    }

    @Test
    public void testStudent_42_bridged_idemix() throws Exception {
        this.copySystemParameters();
        this.issuePatrasCredentials(CryptoEngine.BRIDGED, CryptoEngine.IDEMIX, 42);
        this.verifyPatrasCredentials(CryptoEngine.BRIDGED, CryptoEngine.IDEMIX, 42);
    }

    @Test
    public void testStudent_42_bridged_uprove() throws Exception {
        this.issuePatrasCredentials(CryptoEngine.BRIDGED, CryptoEngine.UPROVE, 42);
        this.verifyPatrasCredentials(CryptoEngine.BRIDGED, CryptoEngine.UPROVE, 42);
    }

    // @Test
    public void testStudent_1235332() throws Exception {
        //    System.out.println("---- testStudent_1235332 ----");
        //    initHelper("1235332");
        //
        //    // issue university credential
        //    runIssuance("startUniversity", "issuanceKey?matriculationnumber=1235332");
        //
        //    // issue course credential
        //    runIssuance("startCourse", "issuanceKey?matriculationnumber=1235332");
        //
        //    // issue attendance credential
        //    runIssuance("startAttendance", "?attendanceId=2");
        //    List<URI> list = UserHelper.getInstance().credentialManager.listCredentials();
        //    System.out.println("# of credentials : " + list.size() + " : " + list);
    }

}
