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

package eu.abc4trust.ri.servicehelper.issuer;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.util.XmlUtils;

/**
 * Helper class. Holds CredentialsSpecification + matching IssuancePolicy
 */
public class SpecAndPolicy {
    public final String key;
    public final String specResource;
    public final String policyResource;

    public final String issuerParamsUid;
    public final String revocationParamsUid;

    public URI issuerParamsUid_URI;
    public URI revocationParamsUid_URI;

    public final List<FriendlyDescription> friendlyDescriptions;

    public SpecAndPolicy(String key, String specResource, String policyResource) {
        this.key = key;
        this.specResource = specResource;
        this.policyResource = policyResource;
        this.issuerParamsUid = null;
        this.revocationParamsUid = null;
        this.friendlyDescriptions = null;
    }

    
    
    /**
     * @param key
     * @param specResource
     * @param policyResource
     * @param issuerParamsUid
     * @param revocationParamsUid
     * @param friendlyDescription : on the form <lang1> <desc1> <lang2> <desc2>
     */
    public SpecAndPolicy(String key, String specResource,
            String policyResource, String issuerParamsUid,
            String revocationParamsUid, String... friendlyDescription) {
        this.key = key;
        this.specResource = specResource;
        this.policyResource = policyResource;
        this.issuerParamsUid = issuerParamsUid;
        this.revocationParamsUid = revocationParamsUid;
        this.friendlyDescriptions = new ArrayList<FriendlyDescription>();
        String locale = null;
        int i = 0;
        ObjectFactory of = new ObjectFactory();
        for (String s : friendlyDescription) {
            if (i % 2 == 1) {
                // System.out.println("Add Issuer Param Friendly : " + locale + " : " + s);
                FriendlyDescription fd = of.createFriendlyDescription();
                fd.setLang(locale);
                fd.setValue(s);
                this.friendlyDescriptions.add(fd);
            } else {
                locale = s;
            }
            i++;
        }
    }
    public SpecAndPolicy(SpecAndPolicy cloneThisSap) {
        this.key = cloneThisSap.key;
        this.specResource = cloneThisSap.specResource;
        this.policyResource = cloneThisSap.policyResource;
        this.issuerParamsUid = cloneThisSap.issuerParamsUid;
        this.revocationParamsUid = cloneThisSap.revocationParamsUid;
        this.issuanceBytes = cloneThisSap.issuanceBytes;
        this.friendlyDescriptions = cloneThisSap.friendlyDescriptions;
        
        this.issuerParamsUid_URI = cloneThisSap.issuerParamsUid_URI;
        this.revocationParamsUid_URI = cloneThisSap.revocationParamsUid_URI;
    }

    private CredentialSpecification credentialSpecification;
    private IssuancePolicy issuancePolicy;
    private byte[] issuanceBytes;

    public CredentialSpecification getCredentialSpecification() {
        return this.credentialSpecification;
    }

    public void setCredentialSpecification(
            CredentialSpecification credentialSpecification) {
        this.credentialSpecification = credentialSpecification;
    }

    public IssuancePolicy getIssuancePolicy() {
        return this.issuancePolicy;
    }

    public void setIssuancePolicy(IssuancePolicy issuancePolicy) {
        this.issuancePolicy = issuancePolicy;
    }

    public void setIssuancePolicyBytes(byte[] bytes) {
        this.issuanceBytes = bytes;
    }

    public IssuancePolicy cloneIssuancePolicy() throws Exception {
        JAXBElement<?> clone = XmlUtils.getJaxbElementFromXml(
                new ByteArrayInputStream(this.issuanceBytes), true);
        IssuancePolicy cloneValue = (IssuancePolicy) clone.getValue();
        cloneValue.getCredentialTemplate().setIssuerParametersUID(
                this.issuerParamsUid_URI);
        return cloneValue;
    }

}