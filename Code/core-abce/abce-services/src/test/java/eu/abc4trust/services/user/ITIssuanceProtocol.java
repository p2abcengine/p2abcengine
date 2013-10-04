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

package eu.abc4trust.services.user;

import org.junit.Test;

import eu.abc4trust.ri.servicehelper.FileSystem;
import eu.abc4trust.services.ITAbstract;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuancePolicyAndAttributes;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.util.XmlUtils;

public class ITIssuanceProtocol extends ITAbstract {

    static ObjectFactory of = new ObjectFactory();

    final String baseUrl = "http://localhost:9500/abce-services/issuer";

    @Test
    public void issuanceProtocolIdemix() throws Exception {

        String engineSuffix = "idemix";

        this.issuanceProtocol(engineSuffix);
    }

    @Test
    public void issuanceProtocolUProve() throws Exception {

        String engineSuffix = "uprove";

        this.issuanceProtocol(engineSuffix);
    }

    private void issuanceProtocol(String engineSuffix) throws Exception {
        this.deleteStorageDirectory("issuer_storage");

        String credentialSpecificationFilename = "credentialSpecificationSimpleIdentitycard.xml";
        CredentialSpecification credentialSpecification = (CredentialSpecification) XmlUtils
                .getObjectFromXML(
                        FileSystem.getInputStream("src/test/resources/"
                                + credentialSpecificationFilename), true);

        String issuerParametersUid = "http://my.country/identitycard/issuancekey_v1.0:"
                + engineSuffix;

        UserServiceFactory userServiceFactory = new UserServiceFactory();

        IssuancePolicyAndAttributes issuancePolicyAndAttributes = userServiceFactory
                .loadIssuancePolicyAndAttributes("/issuancePolicyAndAttributes.xml");

        userServiceFactory.issuanceProtocol(engineSuffix,
                credentialSpecification, issuerParametersUid,
                issuancePolicyAndAttributes);
    }
}