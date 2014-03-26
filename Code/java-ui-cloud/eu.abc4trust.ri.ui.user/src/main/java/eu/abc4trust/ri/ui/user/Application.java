//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.14                                           *
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

package eu.abc4trust.ri.ui.user;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.internal.widgets.JSExecutor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import eu.abc4trust.returnTypes.UiPresentationArguments;
import eu.abc4trust.returnTypes.UiPresentationReturn;
import eu.abc4trust.returnTypes.ui.InspectableAttribute;
import eu.abc4trust.ri.ui.user.utils.ApplicationParameters;
import eu.abc4trust.ri.ui.user.utils.Messages;
import eu.abc4trust.ri.ui.user.utils.UIMode;
import eu.abc4trust.ri.ui.user.utils.UIProperties;
import eu.abc4trust.ri.ui.user.utils.XmlUtils;
import eu.abc4trust.ri.ui.user.view.LoginDialog;
import eu.abc4trust.xml.ABCEBoolean;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PseudonymMetadata;

/**
 * This class controls all aspects of the application's execution and is
 * contributed through the plugin.xml.
 */
public class Application implements IApplication {

	public static final String userAbceEngineServiceBaseUrl = "http://localhost:9300/idselect-user-service/user"; //$NON-NLS-1$
	public static final String REQUESTPARAMNAME_UIMODE = "mode"; //$NON-NLS-1$
	public static final String REQUESTPARAMNAME_DEMO = "demo"; //$NON-NLS-1$
	public static final String REQUESTPARAMNAME_SESSIONID = "sessionid"; //$NON-NLS-1$
	public static final String REQUESTPARAM_ISSUANCEMODE = "issuance"; //$NON-NLS-1$
	public static final String REQUESTPARAM_PRESENTATIONMODE = "presentation"; //$NON-NLS-1$
	public static final String REQUESTPARAM_MANAGEMENTMODE = "management"; //$NON-NLS-1$
	public static final Locale LOCALE_FALLBACK = UIProperties
			.getSessionSingletonInstance().uiFallbackLocale(); // Messages.SWEDISH;
																// //
																// Locale.ENGLISH;

	public Object start(IApplicationContext context) throws Exception {

		ApplicationParameters p = ApplicationParameters
				.getSessionSingletonInstance();

		p.setUserNonce(RWT.getRequest().getParameter("userNonce"));

		// Obtain parameters from HTTP request
		String param_demoMode = RWT.getRequest().getParameter(
				REQUESTPARAMNAME_DEMO);
		if (param_demoMode != null)
			p.setDemoMode(new Boolean(param_demoMode).booleanValue());
		else
			p.setDemoMode(true);

		String param_uiMode = RWT.getRequest().getParameter(
				REQUESTPARAMNAME_UIMODE);
		if (param_uiMode != null
				&& param_uiMode.equalsIgnoreCase(REQUESTPARAM_ISSUANCEMODE))
			p.setUiMode(UIMode.ISSUANCE);
		else if (param_uiMode != null
				&& param_uiMode.equalsIgnoreCase(REQUESTPARAM_PRESENTATIONMODE))
			p.setUiMode(UIMode.PRESENTATION);
		else
			p.setUiMode(UIMode.MANAGEMENT);

		String param_sessionID = RWT.getRequest().getParameter(
				REQUESTPARAMNAME_SESSIONID);
		p.setSessionID(param_sessionID);

		List<Locale> userAcceptedLocales = Collections.list(RWT.getRequest()
				.getLocales());
		System.out.println("userAcceptedLocales : " + userAcceptedLocales);
		if (!userAcceptedLocales.contains(LOCALE_FALLBACK)) {
			// add fallback to list
			userAcceptedLocales.add(LOCALE_FALLBACK);
		}
		p.setUserAcceptedLocales(userAcceptedLocales);

		System.out
				.println("Handling UI request: " + //$NON-NLS-1$
						MessageFormat.format(
								Messages.get().IdentitySelectionView_msg_defaultStatus,
								p.getUIMode().toString(), p
										.getUserAcceptedLocales().toString(), p
										.getSessionID())
						+ (p.isDemo() ? " " + Messages.get().IdentitySelectionView_demoMode + "." : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$);

		// Create the Application
		Display display = PlatformUI.createDisplay();

		WorkbenchAdvisor advisor = new ApplicationWorkbenchAdvisor();
		context.applicationRunning(); // to bring down a splash screen if it
										// exists

		if (login(display.getActiveShell(), new Shell(display))) {
			return PlatformUI.createAndRunWorkbench(display, advisor);
		} else {
			return null;
		}
	}

	public void stop() {
		// Do nothing
	}

	public static void closeApplication() {
		System.out.println("UI CLOSE APPLICATION!! ");
		Display.getCurrent().disposeExec(new Runnable() {
			@Override
			public void run() {
				// OLD mehtod for closing - but did not work for IE
				// http://www.eclipse.org/forums/index.php/m/635947/
				//              JSExecutor.executeJS("window.close(); "); //$NON-NLS-1$

				try {
					String script = "";
					// System.out.println("Excecute Javascript on app close! " +
					// script);
					// lock ui - might not be needed..
					script += "var blurDiv = document.createElement(\"div\");"
							+ "blurDiv.id = \"blurDiv\";"
							+ "blurDiv.style.cssText = \"position:absolute; top:0; right:0; width:\" + screen.width + \"px; height:\" + screen.height + \"px; background-color: #000000; opacity:0.5;\";"
							+ ""
							+ "document.getElementsByTagName(\"body\")[0].appendChild(blurDiv);";
					// add 'idselectDone'..
					script += "var done = document.createElement(\"div\"); "
							+ "done.id = \"idselectDone\"; "
							+ "done.style.cssText=\"display: none;\";"
							+ "document.getElementsByTagName(\"head\")[0].appendChild(done);";

					// System.out.println("Excecute Javascript on app close! " +
					// script);
					JSExecutor.executeJS(script); //$NON-NLS-1$
				} catch (Exception e) {
					System.err
							.println("Excecute Javascript on app close! Failed!");
					e.printStackTrace();
				}
			}
		});
		Display.getCurrent().dispose();
	}

	private boolean login(Shell shell, Shell dummyShell) {
		LoginDialog loginDialog = new LoginDialog(shell);

		loginDialog.open();
		if (ApplicationParameters.getSessionSingletonInstance().getUserNonce() != null) {
			// do proof
			try {
				Client client = Client.create();
				PresentationContainer pc = Container.INSTANCE
						.getPresentation(ApplicationParameters
								.getSessionSingletonInstance().getUserNonce());
				System.out.println(pc.getPolicyXml());
				@SuppressWarnings({ "unchecked", "deprecation" })
				JAXBElement<PresentationPolicyAlternatives> ppa = (JAXBElement<PresentationPolicyAlternatives>) XmlUtils
						.getJaxbElementFromXml(pc.getPolicyXml(), false);

				WebResource canBe = client
						.resource("http://localhost:9200/user/canBeSatisfied/");
				JAXBElement<ABCEBoolean> canBeSatisfied = canBe
						.type(MediaType.TEXT_XML).accept(MediaType.TEXT_XML)
						.post(new GenericType<JAXBElement<ABCEBoolean>>() {
						}, ppa);

				if (canBeSatisfied.getValue().isValue()) {
					WebResource presTokenResource = client
							.resource("http://localhost:9200/user/createPresentationToken/");

					JAXBElement<UiPresentationArguments> upa = presTokenResource
							.type(MediaType.TEXT_XML)
							.accept(MediaType.TEXT_XML)
							.post(new GenericType<JAXBElement<UiPresentationArguments>>() {
							}, ppa);

					UiPresentationReturn fakeUiReturn = new UiPresentationReturn();
					fakeUiReturn.uiContext = upa.getValue().uiContext;
					fakeUiReturn.metadataToChange = new HashMap<String, PseudonymMetadata>();
					fakeUiReturn.chosenInspectors = new ArrayList<String>();
					for (InspectableAttribute is : upa.getValue().tokenCandidatesPerPolicy
							.get(0).tokenCandidates.get(0).inspectableAttributes) {
						fakeUiReturn.chosenInspectors
								.add(is.inspectorAlternatives.get(0).uri);
					}

					System.out.println(fakeUiReturn.uiContext);
					// JAXBElement<UiPresentationReturn> jaxbFakeUiReturn = new
					// JAXBElement<UiPresentationReturn>(
					// new QName("UiPresentationReturn"),
					// UiPresentationReturn.class, fakeUiReturn);

					WebResource presTokenResourceUi = client
							.resource("http://localhost:9200/user/createPresentationTokenUi/");
					JAXBElement<PresentationToken> pt = presTokenResourceUi
							.type(MediaType.TEXT_XML)
							.accept(MediaType.TEXT_XML)
							.post(new GenericType<JAXBElement<PresentationToken>>() {
							}, fakeUiReturn);

					// PresentationPolicyAlternativesAndPresentationToken ppaapt
					// =
					// new PresentationPolicyAlternativesAndPresentationToken();
					// ppaapt.setPresentationPolicyAlternatives(ppa.getValue());
					// ppaapt.setPresentationToken(pt.getValue());
					//
					// eu.abc4trust.xml.ObjectFactory of = new ObjectFactory();
					// JAXBElement<PresentationPolicyAlternativesAndPresentationToken>
					// el = of
					// .createPresentationPolicyAlternativesAndPresentationToken(ppaapt);
					//
					String stringToken = XmlUtils.toXml(pt);
					// stringToken = stringToken.replace("xmlns=\"\" ", ""); //
					// actually not necessary
					stringToken = stringToken.replace("ns7:", "");
					System.out
							.println("Removed the \"ns7\" namespaces from the presentation token to circumvent bug in idemix Parser.");
					System.out.println(stringToken);

					pc.setToken(stringToken);
				} else {
				    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				    System.out.println(">>>>> Policy not satisfiable with the current credentials.");
                    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
					pc.setToken("not satisfiable");
				}
				ApplicationParameters.getSessionSingletonInstance().setUiMode(
						UIMode.AUTO_REDIRECT);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (JAXBException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}
