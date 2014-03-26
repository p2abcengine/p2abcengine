//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.14                                           *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
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

package eu.abc4trust.ri.ui.user.wizard;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.eu.fiware.combineddemo.fiwarelibs.IssuanceRequest;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import eu.abc4trust.returnTypes.IssuanceReturn;
import eu.abc4trust.returnTypes.UiIssuanceReturn;
import eu.abc4trust.returnTypes.ui.CredentialInUi;
import eu.abc4trust.ri.ui.user.Container;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;

public class IssueWizardDialog extends WizardDialog {

	public static class ProtocolHandler {
		// # Init issuance protocol (first step for the issuer).
		// echo "Init issuance protocol"
		// curl -X POST --header 'Content-Type: text/xml' -d
		// @tutorial-resources/issuancePolicyAndAttributes.xml
		// 'http://localhost:9100/issuer/initIssuanceProtocol/' >
		// issuanceMessageAndBoolean.xml
		private static JAXBElement<IssuanceMessage> initIssuanceProtocolAtIssuer(
				IssueWizard wizard) throws JAXBException {
			com.eu.fiware.combineddemo.fiwarelibs.IssuanceRequest ir = new IssuanceRequest();
			ir.setUsername(wizard.getPageOne().getUsername());
			ir.setPassword(wizard.getPageOne().getPassword());
			ir.setCredentialSpecificationUID(wizard.getPageOne()
					.getCredentialUid());

			Client client = Client.create();
			WebResource resource = client.resource(wizard.getPageOne()
					.getIssuerRoot() + "initIssuanceProtocol");

			return resource.type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML)
					.post(new GenericType<JAXBElement<IssuanceMessage>>() {
					}, ir);

		}

		// # First issuance protocol step (first step for the
		// user).
		// echo "First issuance protocol step for the user"
		// curl -X POST --header 'Content-Type: text/xml' -d
		// @firstIssuanceMessage.xml
		// 'http://localhost:9200/user/issuanceProtocolStep/' >
		// issuanceReturn.xml
		private static JAXBElement<IssuanceReturn> normalIssuanceProtocolStepAtUser(
				JAXBElement<IssuanceMessage> im) {
			Client client = Client.create();
			WebResource resource = client
					.resource("http://localhost:9200/user/issuanceProtocolStep/");

			return resource.type(MediaType.TEXT_XML).accept(MediaType.TEXT_XML)
					.post(new GenericType<JAXBElement<IssuanceReturn>>() {
					}, im);
		}

		// # First issuance protocol step - UI (first step for
		// the user).
		// echo
		// "Second issuance protocol step (first step for the user)"
		// curl -X POST --header 'Content-Type: text/xml' -d
		// @uiIssuanceReturn.xml
		// 'http://localhost:9200/user/issuanceProtocolStepUi/'
		// > secondIssuanceMessage.xml
		private static JAXBElement<IssuanceMessage> uiIssuanceProtocolStepAtUser(
				JAXBElement<UiIssuanceReturn> juir) {

			Client client = Client.create();
			WebResource resource = client
					.resource("http://localhost:9200/user/issuanceProtocolStepUi/");

			return resource.type(MediaType.TEXT_XML).accept(MediaType.TEXT_XML)
					.post(new GenericType<JAXBElement<IssuanceMessage>>() {
					}, juir.getValue());
		}

		// # Second issuance protocol step (second step for the
		// issuer).
		// echo
		// "Second issuance protocol step (second step for the issuer)"
		// curl -X POST --header 'Content-Type: text/xml' -d
		// @secondIssuanceMessage.xml
		// 'http://localhost:9100/issuer/issuanceProtocolStep/'
		// > thirdIssuanceMessageAndBoolean.xml
		private static JAXBElement<IssuanceMessage> normalIssuanceProtocolStepAtIssuer(
				JAXBElement<IssuanceMessage> sim, IssueWizard wizard) {
			Client client = Client.create();
			WebResource resource = client.resource(wizard.getPageOne()
					.getIssuerRoot() + "issuanceProtocolStep");

			return resource.type(MediaType.TEXT_XML).accept(MediaType.TEXT_XML)
					.post(new GenericType<JAXBElement<IssuanceMessage>>() {
					}, sim);
		}

		public static void doProtocol(IssueWizard wizard) {
			try {
				JAXBElement<IssuanceMessage> initResponse = initIssuanceProtocolAtIssuer(wizard);
				JAXBElement<IssuanceReturn> returnFromUser = normalIssuanceProtocolStepAtUser(initResponse);

				UiIssuanceReturn fakeUiReturn = new UiIssuanceReturn();
				fakeUiReturn.uiContext = returnFromUser.getValue().uia.uiContext;
				JAXBElement<UiIssuanceReturn> jaxbFakeUiReturn = new JAXBElement<UiIssuanceReturn>(
						new QName("UiIssuanceReturn"), UiIssuanceReturn.class,
						fakeUiReturn);

				JAXBElement<IssuanceMessage> uiIssuanceReturnFromUser = uiIssuanceProtocolStepAtUser(jaxbFakeUiReturn);
				JAXBElement<IssuanceMessage> lastReturnFromIssuer = normalIssuanceProtocolStepAtIssuer(
						uiIssuanceReturnFromUser, wizard);
				JAXBElement<IssuanceReturn> lastReturnFromUser = normalIssuanceProtocolStepAtUser(lastReturnFromIssuer);
				CredentialDescription cd = lastReturnFromUser.getValue().cd;

				JAXBContext jaxbContext1;
				jaxbContext1 = JAXBContext.newInstance(IssuerParameters.class);
				Unmarshaller jaxbUnmarshaller1 = jaxbContext1
						.createUnmarshaller();
				@SuppressWarnings("unchecked")
				JAXBElement<IssuerParameters> ip = (JAXBElement<IssuerParameters>) jaxbUnmarshaller1
						.unmarshal(IssueWizardDialog.class
								.getResourceAsStream("/fiware-demo/issuer_params_nsn_correct.xml"));

				JAXBContext jaxbContext2;
				jaxbContext2 = JAXBContext.newInstance(ObjectFactory.class);
				Unmarshaller jaxbUnmarshaller2 = jaxbContext2
						.createUnmarshaller();
				@SuppressWarnings("unchecked")
				JAXBElement<CredentialSpecification> cp = (JAXBElement<CredentialSpecification>) jaxbUnmarshaller2
						.unmarshal(IssueWizardDialog.class
								.getResourceAsStream("/fiware-demo/credentialSpecificationFiwareUser.xml"));

				CredentialInUi cdui = new CredentialInUi(cd, ip.getValue(),
						cp.getValue(), null);
				Container.INSTANCE.addCredential(cdui);
			} catch (Throwable ex) {
				ex.printStackTrace();
			}

			wizard.refreshSecondPage();
		}
	}

	private static final long serialVersionUID = -4692040141254024387L;
	IssueWizard wizard;

	public IssueWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
		this.wizard = (IssueWizard) newWizard;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);

		Button ok = getButton(IDialogConstants.NEXT_ID);
		ok.setText("Get credential!");
		setButtonLayoutData(ok);
		ok.addMouseListener(new MouseListener() {
			private static final long serialVersionUID = 6732595815149773092L;

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				ProtocolHandler.doProtocol(wizard);
			}

			@Override
			public void mouseUp(MouseEvent arg0) {
			}

		});

		Button finish = getButton(IDialogConstants.FINISH_ID);
		finish.setText("Thanks!");
		setButtonLayoutData(finish);

		Button cancel = getButton(IDialogConstants.CANCEL_ID);
		cancel.setText("I changed my mind");
		setButtonLayoutData(cancel);
	}
}