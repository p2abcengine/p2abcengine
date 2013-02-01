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

package eu.abc4trust.cryptoEngine.uprove.util;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceClient;

import org.datacontract.schemas._2004._07.abc4trust_uprove.ArrayOfUProveKeyAndTokenComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.FirstIssuanceMessageComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.IssuerKeyAndParametersComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.IssuerParametersComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.PresentationProofComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.PseudonymComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.SecondIssuanceMessageComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.ThirdIssuanceMessageComposite;
import org.datacontract.schemas._2004._07.abc4trust_uprove.UProveTokenComposite;

import abc4trust_uprove.service1.IService1;
import abc4trust_uprove.service1.Service1;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfint;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfstring;

import edu.rice.cs.plt.lambda.Thunk;
import eu.abc4trust.cryptoEngine.uprove.issuer.UProveCryptoEngineIssuerImpl;

public class UProveBindingManager {

    private IService1 binding;
    private final UProveLauncher uproveLauncher;
    private boolean bindingManagerLaunchedUProveService = false;
    private final int timeoutSeconds;
    private final int port;
    private String name;

    @Inject
    public UProveBindingManager(UProveLauncher uproveLauncher,
            @Named("RetryTimeout") Integer timeoutSeconds,
            @Named("UProvePortNumber") Integer port) {
        this.uproveLauncher = uproveLauncher;
        this.timeoutSeconds = timeoutSeconds;
        this.port = port;
        System.out.println("UProveBindingManager.construct : " + port + " : " + this);
    }

    public void setupBiding(String name) {
        // System.out.println("UProveBindingManager.setupBiding : " + this + " - port : " + this.port);
        this.name = name;
        URL wsdlUrl = UProveCryptoEngineIssuerImpl.class
                .getResource("/uprove/WEB-INF/wsdl/abc4trust-uprove.wsdl");

        WebServiceClient ann = Service1.class
                .getAnnotation(WebServiceClient.class);
        Service1 service = new Service1(wsdlUrl, new QName(
                ann.targetNamespace(), ann.name()));

        this.binding = service.getWSHttpBindingIService1();
        BindingProvider bp = ((BindingProvider) this.binding);
        String address = "http://127.0.0.1:" + this.port + "/abc4trust-webservice/";
        System.out.println("UProveBindingManager.setupBinding : " + address);
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                address);

        this.checkAndLaunchUProveIfNeeded(this.binding);
    }

    public int stop() {
        if(bindingManagerLaunchedUProveService) {
            System.out.println("UProveBindingManager.stop : " + this + " - port : " + this.port);
            int exitCode = this.uproveLauncher.stop();
            this.uproveLauncher.waitFor(this.timeoutSeconds);
            return exitCode;
        } else {
            System.out.println("UProveBindingManager.stop - Process Being reused - leave running : " + this + " - port : " + this.port);
            return 0;
        }
    }

    private void checkAndLaunchUProveIfNeeded(IService1 binding) {
        // System.out.println("UProveBindingManager.checkAndLaunchUProveIfNeeded : " + this + " - port : " + this.port + " - Launcher : " + this.uproveLauncher);
        try {
            System.out.println("Pinging port : " + this.port);
            binding.ping();
            System.out.println("Pinging done - already running on port : " + this.port);
        } catch (Exception ex) {
            System.out.println("starting UProve on port: " + this.port);
            this.uproveLauncher.start(this.port, this.name);
            this.uproveLauncher.waitFor(this.timeoutSeconds);
            this.bindingManagerLaunchedUProveService = true;
        }
    }

    public String login(final boolean useVirtualDevice, final String pinCode, final int credId, final int groupId, final int proverId, final int securityLevel) {
    	Thunk<String> thunk = new edu.rice.cs.plt.lambda.Thunk<String>() {

            @Override
            public String value() {
                return UProveBindingManager.this.binding.login(useVirtualDevice, pinCode, credId, groupId, proverId, securityLevel);
            }

        };
        return this.retryCallOnceIfEngineIsUnAvailable(thunk);
    }
    
    public Void setIssuerPrivateKey(final byte[] secretKey, final String sessionKey) {
        Thunk<Void> thunk = new edu.rice.cs.plt.lambda.Thunk<Void>() {

            @Override
            public Void value() {
                UProveBindingManager.this.binding.setIssuerPrivateKey(secretKey, sessionKey);
                return null;
            }

        };
        return this.retryCallOnceIfEngineIsUnAvailable(thunk);
    }

    private <T> T retryCallOnceIfEngineIsUnAvailable(Thunk<T> thunk) {
        try {
            return thunk.value();
        } catch (IllegalThreadStateException ex) {
            this.uproveLauncher.start(this.port, this.name);
            this.uproveLauncher.waitFor(this.timeoutSeconds);
            try {
                return thunk.value();
            } catch (IllegalThreadStateException ex1) {
                throw new RuntimeException(
                        "U-Prove engine is not running and could not be restarted");
            }
        }
    }

    public FirstIssuanceMessageComposite getFirstMessage(
            final ArrayOfstring arrayOfStringAttributesParam,
            final IssuerParametersComposite ipc,
            final Integer numberOfTokensParam, final String sessionKey,
            final byte[] hd) {
        Thunk<FirstIssuanceMessageComposite> thunk = new edu.rice.cs.plt.lambda.Thunk<FirstIssuanceMessageComposite>() {

            @Override
            public FirstIssuanceMessageComposite value() {
                return UProveBindingManager.this.binding.getFirstMessage(
                        arrayOfStringAttributesParam, ipc, numberOfTokensParam,
                        sessionKey, hd);
            }

        };
        return this.retryCallOnceIfEngineIsUnAvailable(thunk);
    }

    public IssuerKeyAndParametersComposite setupIssuerParameters(
            final String uniqueIdentifier, final byte[] attributeEncoding,
            final String hash, final String sessionKey) {
        Thunk<IssuerKeyAndParametersComposite> thunk = new edu.rice.cs.plt.lambda.Thunk<IssuerKeyAndParametersComposite>() {

            @Override
            public IssuerKeyAndParametersComposite value() {
                return UProveBindingManager.this.binding.setupIssuerParameters(
                        uniqueIdentifier, attributeEncoding, hash, sessionKey);
            }

        };
        return this.retryCallOnceIfEngineIsUnAvailable(thunk);
    }

    public ThirdIssuanceMessageComposite getThirdMessage(
            final SecondIssuanceMessageComposite convertSecondIssuanceMessage, final String sessionKey) {
        Thunk<ThirdIssuanceMessageComposite> thunk = new edu.rice.cs.plt.lambda.Thunk<ThirdIssuanceMessageComposite>() {

            @Override
            public ThirdIssuanceMessageComposite value() {
                return UProveBindingManager.this.binding
                        .getThirdMessage(convertSecondIssuanceMessage, sessionKey);
            }

        };
        return this.retryCallOnceIfEngineIsUnAvailable(thunk);
    }

    public SecondIssuanceMessageComposite getSecondMessage(
            final ArrayOfstring arrayOfStringAttributesParam,
            final IssuerParametersComposite ipc,
            final Integer numberOfTokensParam,
            final FirstIssuanceMessageComposite convertFirstIssuanceMessage, final String sessionKey) {
        Thunk<SecondIssuanceMessageComposite> thunk = new edu.rice.cs.plt.lambda.Thunk<SecondIssuanceMessageComposite>() {

            @Override
            public SecondIssuanceMessageComposite value() {
                return UProveBindingManager.this.binding.getSecondMessage(
                        arrayOfStringAttributesParam, ipc, numberOfTokensParam,
                        convertFirstIssuanceMessage, sessionKey);
            }

        };
        return this.retryCallOnceIfEngineIsUnAvailable(thunk);
    }

    public ArrayOfUProveKeyAndTokenComposite generateTokens(
            final ThirdIssuanceMessageComposite thirdIssuanceMessage, final String sessionKey) {
        Thunk<ArrayOfUProveKeyAndTokenComposite> thunk = new edu.rice.cs.plt.lambda.Thunk<ArrayOfUProveKeyAndTokenComposite>() {

            @Override
            public ArrayOfUProveKeyAndTokenComposite value() {
                return UProveBindingManager.this.binding
                        .generateTokens(thirdIssuanceMessage, sessionKey);
            }

        };
        return this.retryCallOnceIfEngineIsUnAvailable(thunk);
    }

    public void setSecret(final byte[] byteArray, final String sessionKey) {
        Thunk<Void> thunk = new edu.rice.cs.plt.lambda.Thunk<Void>() {

            @Override
            public Void value() {
                UProveBindingManager.this.binding.setSecret(byteArray, sessionKey);
                return null;
            }

        };
        this.retryCallOnceIfEngineIsUnAvailable(thunk);
    }

    public PseudonymComposite presentPseudonym(final String message,
            final String scope, final String sessionKey) {
        Thunk<PseudonymComposite> thunk = new edu.rice.cs.plt.lambda.Thunk<PseudonymComposite>() {

            @Override
            public PseudonymComposite value() {
                return UProveBindingManager.this.binding.presentPseudonym(
                        message, scope, sessionKey);
            }

        };
        return this.retryCallOnceIfEngineIsUnAvailable(thunk);
    }

    public PresentationProofComposite proveToken(
            final ArrayOfstring arrayOfStringAttributesParam,
            final ArrayOfint arrayOfIntDisclosedParam,
            final ArrayOfint committedAttributes,
            final String applicationMessage, final String verifierScopeParam,
            final IssuerParametersComposite ipc,
            final UProveTokenComposite compositeToken, final byte[] privateKey, final String sessionKey) {
        Thunk<PresentationProofComposite> thunk = new edu.rice.cs.plt.lambda.Thunk<PresentationProofComposite>() {

            @Override
            public PresentationProofComposite value() {
                return UProveBindingManager.this.binding.proveToken(
                        arrayOfStringAttributesParam, arrayOfIntDisclosedParam,
                        committedAttributes, applicationMessage,
                        verifierScopeParam, ipc, compositeToken, privateKey, sessionKey);
            }

        };
        return this.retryCallOnceIfEngineIsUnAvailable(thunk);
    }

    public boolean verifyTokenProof(
            final PresentationProofComposite presentationProof,
            final ArrayOfint arrayOfIntDisclosedParam,
            final ArrayOfint arrayOfIntCommittedParam,
            final String applicationMessage, final String verifierScopeParam,
            final IssuerParametersComposite ipc,
            final UProveTokenComposite compositeToken, final String sessionKey) {
        Thunk<Boolean> thunk = new edu.rice.cs.plt.lambda.Thunk<Boolean>() {

            @Override
            public Boolean value() {
                return UProveBindingManager.this.binding.verifyTokenProof(
                        presentationProof, arrayOfIntDisclosedParam,
                        arrayOfIntCommittedParam, applicationMessage,
                        verifierScopeParam, ipc, compositeToken, sessionKey);
            }

        };
        return this.retryCallOnceIfEngineIsUnAvailable(thunk);
    }

    public boolean verifyPseudonym(final String applicationMessage,
            final String scope, final PseudonymComposite pseudonymComposite, final String sessionKey) {
        Thunk<Boolean> thunk = new edu.rice.cs.plt.lambda.Thunk<Boolean>() {

            @Override
            public Boolean value() {
                return UProveBindingManager.this.binding.verifyPseudonym(
                        applicationMessage, scope, pseudonymComposite, sessionKey);
            }

        };
        return this.retryCallOnceIfEngineIsUnAvailable(thunk);
    }

    public void verifyIssuerParameters(final IssuerParametersComposite ipc, final String sessionKey) {
        Thunk<Void> thunk = new edu.rice.cs.plt.lambda.Thunk<Void>() {

            @Override
            public Void value() {
                UProveBindingManager.this.binding.verifyIssuerParameters(ipc, sessionKey);
                return null;
            }

        };
        this.retryCallOnceIfEngineIsUnAvailable(thunk);
    }

    public List<String> getDebugOutputFromUProve() {
        return this.uproveLauncher.getOutput();
    }

    public void printDebugOutputFromUProve() {
        for (String s : this.uproveLauncher.getOutput()) {
            System.out.println(s);
        }
    }

	public void logout(final String sessionKey) {
		Thunk<Void> thunk = new edu.rice.cs.plt.lambda.Thunk<Void>() {

            @Override
            public Void value() {
                UProveBindingManager.this.binding.logout(sessionKey);
                return null;
            }

        };
        this.retryCallOnceIfEngineIsUnAvailable(thunk);
		
	}

}
