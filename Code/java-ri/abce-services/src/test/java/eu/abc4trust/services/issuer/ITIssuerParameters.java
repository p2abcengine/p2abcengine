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

package eu.abc4trust.services.issuer;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import eu.abc4trust.services.ITAbstract;
import eu.abc4trust.xml.IssuerParameters;

public class ITIssuerParameters extends ITAbstract {


    @Test
    public void setupIssuerParametersIdemix() throws Exception {
        String issuerParametersUid = "urn:issuerparameters:test:foobar";
        IssuerServiceFactory issuerServiceFactory = new IssuerServiceFactory();
        IssuerParameters issuerParameters = issuerServiceFactory
                .getIssuerParameters(issuerParametersUid);

        assertNotNull(issuerParameters);
    }

    @Test
    public void setupIssuerParametersUProve() throws Exception {
        String issuerParametersUid = "urn:issuerparameters:test:foobar:uprove";
        IssuerServiceFactory issuerServiceFactory = new IssuerServiceFactory();
        IssuerParameters issuerParameters = issuerServiceFactory
                .getIssuerParameters(issuerParametersUid);

        assertNotNull(issuerParameters);
    }



}
