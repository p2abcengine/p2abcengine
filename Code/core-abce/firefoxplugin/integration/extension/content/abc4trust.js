function checkSmartcard(elm, sessionID) {
	var DEBUG = false;
	// Check if smartcard is available!
	var abcquery = new XMLHttpRequest();
	abcquery.open("GET","http://localhost:9100/service-user/user/checkSmartcard/"+sessionID,false);
	try {
		abcquery.send();
	} catch(connect_userabce_failed) {
		if(DEBUG) {alert("User ABCE Not Running (Issuance) : " + connect_userabce_failed);};
		if(elm!=null) {
			elm.setAttribute("abc_return_status",400);
			elm.setAttribute("abc_return_msg","User ABCE Not Running (Issuance)");
		}
		return false;
	}
	
	if(abcquery.status == 204) {
		if(elm!=null) {
			elm.setAttribute("abc_pin", 0);
		}
		return true;
	}
	
	abcquery.open("GET","http://localhost:9100/service-user/user/smartcardStatus",false);
	abcquery.send();
	if(abcquery.status<200||abcquery.status>204){
		return false;
	}
	
	// Not available in UserABCE - ask for PIN
	var prompts = Components.classes["@mozilla.org/embedcomp/prompt-service;1"]
	                        .getService(Components.interfaces.nsIPromptService);

	var password = {value: ""};              // default the password to pass
    var check = {value: false};                   // default the checkbox to true

	var result = prompts.promptPassword(null, "Authenticate Smartcard", "Enter Pin:", password, null, check);
	
	if(! result) {
		// user cancelled
		if(DEBUG) {alert("User cancelled Smartcard Pin");};
		if(elm!=null) {
			elm.setAttribute("abc_return_status",400);
			elm.setAttribute("abc_return_msg","Pin not entered");
		}
		return false;
	}
	// post pin to User ABCE
	abcquery.open("POST","http://localhost:9100/service-user/user/unlockSmartcards/"+sessionID,false);
	abcquery.setRequestHeader("Content-type", "text/plain");
	abcquery.send(password.value);
	
	if(abcquery.status>=200 && abcquery.status<=204) {
		if(elm!=null) {
			elm.setAttribute("abc_pin", password.value);
		}
		return true;
	} else {
		if(DEBUG) {alert("Smartcard Not Accepted");};
		if(elm!=null) {
			elm.setAttribute("abc_return_status",abcquery.status);
			if(abcquery.status == 401){
				//Unauthorized
				elm.setAttribute("abc_return_msg","Smartcard Not Accepted - wrong PIN entered. Try again");
			}else if(abcquery.status = 403){
				//forbidden. Card locked
				elm.setAttribute("abc_return_msg","Smartcard Not Accepted - wrong PIN entered. Your card is now locked. Unlock using your PUK code");
			}else{
				elm.setAttribute("abc_return_msg","Smartcard Not Accepted - Unknown cause");
			}
			
		}
		return false;
	}
}

function XXXcheckPseudonym(elm, sessionID){
	var DEBUG = false;
	var abcquery = new XMLHttpRequest();
	var pin = elm.getAttribute("abc_pin");
	abcquery.open("POST","http://localhost:9100/service-user/user/generatePseudonym/"+sessionID,false);
	try {
		abcquery.send(pin);
	} catch(connect_userabce_failed) {
		if(DEBUG) {alert("User ABCE Not Running (Issuance) : " + connect_userabce_failed);};
		elm.setAttribute("abc_return_status",400);
		elm.setAttribute("abc_return_msg","User ABCE Not Running (Issuance)");
		return false;
	}
	
	if(abcquery.status>=200 && abcquery.status<=204) {
		return true;
	}else{
		elm.setAttribute("abc_return_status", 400);
		elm.setAttribute("abc_return_msg", "Pseudonym not generated.");
		return false;
	}
}


function issue(elm){
	var DEBUG = false;
	var enableDebug = elm.getAttribute("debug_in_extension");
	if(enableDebug != null&&(enableDebug==true||enableDebug=='true')) {
		DEBUG = true;
	}

	var sessionID = "Issuance" + new Date().getTime()+""+Math.floor(Math.random()*99999);
	elm.setAttribute("session_id", sessionID); 

	// Check that Smartcard is present!
	var smartcardAvailable = checkSmartcard(elm, sessionID);
	if(! smartcardAvailable) {
		return true;
	}
	
	//Ensure that the pseudonym is present on user side. elm contains the pin
/* SMARTCARD ISSUANCE SHOULD TAKE CARE OF THIS!
	var pseudonymInPlace = checkPseudonym(elm, sessionID);
	if(! pseudonymInPlace){
		return true;
	}
*/
	// Read parameters from dataelement
	var start_request = elm.getAttribute("start_request");
	var step_request = elm.getAttribute("step_request");
	var start_request_mod = encodeURIComponent(start_request);
	var step_request_mod = encodeURIComponent(step_request);
	var language = elm.getAttribute("language");
	if(language == null) {
		language = "en";
	}
	
	// Contact issuance server to obtain Issuance policy
	var xmlhttp = new XMLHttpRequest();
	xmlhttp.open("POST",start_request,false);
//	if(DEBUG) {alert("user getting initial xml from issuer");}
	xmlhttp.setRequestHeader("Content-type", "application/xml");
	xmlhttp.setRequestHeader("Accept", "text/xml,application/xml");
	try {
		xmlhttp.send();
	} catch(connect_verifier_failed) {
		if(DEBUG) {alert("Connect to Issuer Failed : " + connect_verifier_failed);};
		elm.setAttribute("abc_return_status",400);
		elm.setAttribute("abc_return_msg","Could not contact issuance service");
		return true;
	}
	
	while(true) {
		if(DEBUG) {alert("got xml:\n"+xmlhttp.responseText);}
		if(xmlhttp.status !=200){
			elm.setAttribute("abc_return_status",xmlhttp.status);
			elm.setAttribute("abc_return_msg","Failed to obtain issuance policy from issuance server");
			if(DEBUG) {alert("Failed to get issuance policy contact issuance server.");} 
			// return 'initialError' : true
			return true;
		} else {
			var abcquery = new XMLHttpRequest();
			abcquery.open("POST","http://localhost:9100/service-user/user/issuanceProtocolStep/"+sessionID+
					"?startRequest="+start_request_mod+
					"&stepRequest="+step_request_mod,false);
			//abcquery.open("POST","http://localhost:9100/service-user/user/issuanceProtocolStep/"+sessionID,false);
			abcquery.setRequestHeader("Content-type", "application/xml");
			if(DEBUG) {alert("sending received xml to user abce");}
			try {
 				abcquery.send(xmlhttp.responseText);
			} catch(connect_userabce_failed) {
				if(DEBUG) {alert("User ABCE Not Running (Issuance) : " + connect_userabce_failed);};
				elm.setAttribute("abc_return_status",400);
				elm.setAttribute("abc_return_msg","User ABCE Not Running (Issuance)");
				reportIssuanceStatus(elm, false);
				return true;
			}
			
			if(abcquery.status!=200 && abcquery.status != 203 && abcquery.status != 204) {
				elm.setAttribute("abc_return_status",abcquery.status);
				elm.setAttribute("abc_return_msg","Local ABC engine did not return an Issuancemessage.");
				if(DEBUG) {alert("Failed to contact local ABC engine, IssuanceToken (first msg): "+abcquery.status);}
				reportIssuanceStatus(elm, false);
				// return 'initialError' : true
				return true;
			} else if(abcquery.status == 203){
				//we got some JSON, present it and send it to abc engine
				if(DEBUG){alert("this is ISSUANCE id Selection:\n"+abcquery.responseText);}
				//var choice = "this is the choice returned by ID Selection"; //should be obtained using the id selection plugin
		 		var jsonChoices = eval('('+abcquery.responseText+')');
				hasChoices = false;
				populateSelectionUI(jsonChoices, "issue", language); //idselect.js
				return;
			} else if(abcquery.status == 200){
				if(DEBUG) {alert("got some xml from the ABCE:\n"+abcquery.responseText);}
				xmlhttp = new XMLHttpRequest();
				xmlhttp.open("POST", step_request, false);
				xmlhttp.setRequestHeader("Content-type", "application/xml");
				xmlhttp.setRequestHeader("Accept", "text/xml,application/xml");
				xmlhttp.send(abcquery.responseText);
			} else if(abcquery.status == 204){
				elm.setAttribute("abc_return_status",200);
				elm.setAttribute("abc_return_msg","Credential obtained... SUCCESS");
				if(DEBUG) {alert("credential obtained.... success!");}	
				reportIssuanceStatus(elm, true);
				return;
			} 		
		}
	}
}

function hasChoice_issue(elm){
	var DEBUG = false;
	var enableDebug = elm.getAttribute("debug_in_extension");
	if(enableDebug != null&&(enableDebug==true||enableDebug=='true')) {
		DEBUG = true;
	}

	// Read parameters from dataelement
	var start_request = elm.getAttribute("start_request");
	var step_request = elm.getAttribute("step_request");
	var language = elm.getAttribute("language");
	if(language == null) {
		language = "en";
	}
    
	var sessionID = elm.getAttribute("session_id");

	var choice = elm.getAttribute("return");
	var hasChocies = true;
	
	var evt = document.createEvent("Events");

    if(DEBUG){alert("dispatchEvent");}
	evt.initEvent("ABC4TrustResultEvent", true, false);
	elm.dispatchEvent(evt);
	
//	if(DEBUG){alert("FIREFOX PLUGIN : ISSUE : Got choice from idselect: "+choice+"\n\n"+sessionID);}
	
	// request to Issuer
	var xmlhttp = new XMLHttpRequest();

	// request to local User ABCE
	var abcquery = new XMLHttpRequest();
	abcquery = new XMLHttpRequest();
	var start_request_mod = encodeURIComponent(start_request);
	var step_request_mod = encodeURIComponent(step_request);
	if(DEBUG){
		alert("start request encoded: "+start_request_mod);
		alert("step request encoded: "+step_request_mod);
	}
	abcquery.open("POST","http://localhost:9100/service-user/user/issuanceProtocolStepSelect/"+sessionID+
			"?startRequest="+start_request_mod+
			"&stepRequest="+step_request_mod,false);
	abcquery.setRequestHeader("Content-type", "application/json");
	abcquery.setRequestHeader("Accept", "text/xml,application/xml");
	if(DEBUG) { alert("now sending our choice back to user abce:\n"+choice);}
	abcquery.send(choice);
	
	while(true) {
		if(abcquery.status == 200){
			// send to issuer
			if(DEBUG) {alert("got some xml from the ABCE, sending it to issuer:\n"+abcquery.responseText);}
			xmlhttp = new XMLHttpRequest();
			xmlhttp.open("POST", step_request, false);
			xmlhttp.setRequestHeader("Content-type", "application/xml");
			xmlhttp.setRequestHeader("Accept", "text/xml,application/xml");
			
			xmlhttp.send(abcquery.responseText);
		} else if(abcquery.status == 204){
			// user ABC finished
			elm.setAttribute("abc_return_status",200);
			elm.setAttribute("abc_return_msg","Credential obtained... SUCCESS");
			if(DEBUG) {alert("credential obtained.... success!");}
			reportIssuanceStatus(elm, true);
			return;
		} else{
			// error
			elm.setAttribute("abc_return_status",abcquery.status);
			elm.setAttribute("abc_return_msg","Local ABC engine did not return an Issuancemessage.");
			if(DEBUG) {alert("Failed to contact local ABC engine: "+abcquery.status);}
			reportIssuanceStatus(elm, false);
			return;
		}

		// we got response from issuer server 
		
		// send to user
		abcquery = new XMLHttpRequest();
		abcquery.open("POST","http://localhost:9100/service-user/user/issuanceProtocolStep/"+sessionID+
				"?startRequest="+start_request_mod+
				"&stepRequest="+step_request_mod,false);
		abcquery.setRequestHeader("Content-type", "application/xml");
		abcquery.setRequestHeader("Accept", "text/xml,application/xml");
		if(DEBUG) {alert("plugin sending received xml to user abce  - session : " +sessionID + " - XML from server : "+ xmlhttp.responseText);}
		abcquery.send(xmlhttp.responseText);
		if(abcquery.status!=200 && abcquery.status != 203 && abcquery.status != 204) {
			elm.setAttribute("abc_return_status",abcquery.status);
			elm.setAttribute("abc_return_msg","Local ABC engine did not return an Issuancemessage.");
			if(DEBUG) {alert("Failed to contact local ABC engine, IssuanceToken (first msg): "+abcquery.status);}
			reportIssuanceStatus(elm, false);
			return;
		} else if(abcquery.status == 203){
			//we got some JSON, present it and send it to abc engine
			if(DEBUG){alert("this is ISSUANCE id Selection:\n"+abcquery.responseText);}
			//var choice = "this is the choice returned by ID Selection"; //should be obtained using the id selection plugin
		 	var jsonChoices = eval('('+abcquery.responseText+')');
			hasChoices = false;
			populateSelectionUI(jsonChoices, "issue", language); //idselect.js
			return;
				
		} 
		// 200 + 204 handled in start of loop
	}
}

function reportIssuanceStatus(elm, status) {
	var DEBUG = false;
	var enableDebug = elm.getAttribute("debug_in_extension");
	if(enableDebug != null&&(enableDebug==true||enableDebug=='true')) {
		DEBUG = true;
	}
	var status_request = elm.getAttribute("status_request");
	if(status_request!=null) {
		var xmlhttp = new XMLHttpRequest();
		xmlhttp.open("POST", status_request, false);
		xmlhttp.setRequestHeader("Content-type", "text/plain");
		var statusBooleanAsText = status ? "1" : "0";
		try {
			xmlhttp.send(statusBooleanAsText);
		} catch(ignore) {
		}
	}
}


function verify(elm){
	var DEBUG = false;
	var enableDebug = elm.getAttribute("debug_in_extension");
	if(enableDebug != null&&(enableDebug==true||enableDebug=='true')) {
		DEBUG = true;
	}
	
	
	var sessionID = "Verify" + new Date().getTime()+""+Math.floor(Math.random()*99999);
	// Check that Smartcard is present!
	var smartcardAvailable = checkSmartcard(elm, sessionID);
	if(! smartcardAvailable) {
		return true;
	}
	
	//
	var policy_request = elm.getAttribute("policy_request");
 	var verify_request = elm.getAttribute("verify_request");
    var language = elm.getAttribute("language");
	if(language == null) {
		language = "en";
	}

	//read parameters from dataelement
	verify_server = elm.getAttribute("verify_server"); 
    verify_credentials = elm.getAttribute("verify_credentials");  // MyCredentialRequest
	policy_server = elm.getAttribute("policy_server");

	elm.setAttribute("session_id", sessionID); 

	if(verify_credentials == null) {
		verify_credentials = sessionID;
	}

	
	// Obtain the presentation policy
	var xmlhttp = new XMLHttpRequest();
//	var req = policy_request;
	var req = null;
	if(policy_request != null) {
		req = policy_request; 
	} else {
		req = policy_server+"/policy/"+verify_credentials;
	}
	
	if(DEBUG){alert("policy request : "+req);}	
	xmlhttp.open("GET",req,false);
	
	xmlhttp.setRequestHeader("Content-type", "application/xml");
	try {
		xmlhttp.send(null);
	} catch(connect_verifier_failed) {
		if(DEBUG) {alert("Connect to Verifier Failed : " + connect_verifier_failed);};
		elm.setAttribute("abc_return_status",400);
		elm.setAttribute("abc_return_msg","Could not contact verification service");
		return true;
	}
	
	if(xmlhttp.status!=200) {
		elm.setAttribute("abc_return_status",xmlhttp.status);
		elm.setAttribute("abc_return_msg","Failed to obtain presentation policy");
		if(DEBUG){alert("Failed to obtain presentation policy");}
		// return 'initialError' : true
		return true;
	}
	if(DEBUG){alert("policy:\n"+xmlhttp.responseText);}	
	// Pass the presentationpolicy to the local user abce
	var abcquery = new XMLHttpRequest();
	abcquery.open("POST","http://localhost:9100/service-user/user/createPresentationToken/"+sessionID,false);
	abcquery.setRequestHeader("Content-type", "application/xml");
	try {
		abcquery.send(xmlhttp.responseText);
	} catch(connect_userabce_failed) {
		if(DEBUG) {alert("User ABCE Not Running (Verification) : " + connect_userabce_failed);};
		elm.setAttribute("abc_return_status",400);
		elm.setAttribute("abc_return_msg","User ABCE Not Running (Verification)");
		return true;
	}
	if(DEBUG){alert("send a post to local abce, should get a json object with choices back "+abcquery.status);}
//	if(DEBUG){alert(abcquery.responseText);}
	
	if(abcquery.status==422) {
		elm.setAttribute("abc_return_status",abcquery.status);
		elm.setAttribute("abc_return_msg","Can not satisfy policy");
		if(DEBUG){alert("Cannot satisfy policy");}
		// return 'initialError' : true
		return true;
	}
	
	if(abcquery.status!=200 && abcquery.status !=203) {
		elm.setAttribute("abc_return_status",abcquery.status);
		elm.setAttribute("abc_return_msg","Failed to contact local ABC engine");
		if(DEBUG){alert("failed to contact local ABC engine, first msg");}
		// return 'initialError' : true
		return true;
	}
	
	if(abcquery.status== 203){
		//we got some JSON, present it and send it to abc engine
		if(DEBUG){alert("this is id Selection:\n"+abcquery.responseText);}
		//var choice = "this is the choice returned by ID Selection"; //should be obtained using the id selection plugin
 		var jsonChoices = eval('('+abcquery.responseText+')');
		hasChoices = false;
		populateSelectionUI(jsonChoices, "verify", language); //idselect.js
		return;
	}

	var verifyquery = new XMLHttpRequest();
	var verify_req = null;
	if(verify_request!=null) {
		verify_req = verify_request;
	} else {
		verify_req = verify_server+"/verify/"+verify_credentials;
	}
	verifyquery.open("POST",verify_req,false);

	verifyquery.setRequestHeader("Content-type", "application/xml");
	if(DEBUG){alert("plugin sending presentationtoken to verify server : " + verify_req);}	
	verifyquery.send(abcquery.responseText);
	if(verifyquery.status!=200 && verifyquery.status!=202) {
		elm.setAttribute("abc_return_status",verifyquery.status);
		elm.setAttribute("abc_return_msg","Failed to contact verifier service");
		if(DEBUG){alert("failed to contact verifier : " + verifyquery.status);}
		return;
	} 
	elm.setAttribute("abc_return_status",verifyquery.status);
	elm.setAttribute("abc_return_msg","Verfication was succesful");
	return;
}

function hasChoice_verify(elm){
	var DEBUG = false;
	var enableDebug = elm.getAttribute("debug_in_extension");
	if(enableDebug != null&&(enableDebug==true||enableDebug=='true')) {
		DEBUG = true;
	}
	
 	var verify_request = elm.getAttribute("verify_request");
	
	var verify_server = elm.getAttribute("verify_server"); 
    var verify_credentials = elm.getAttribute("verify_credentials");  // MyCredentialRequest
//	var policy_server = elm.getAttribute("policy_server");
	
	var sessionID = elm.getAttribute("session_id"); 
	
	elm.setAttribute("abc_return_status","test status");
	elm.setAttribute("abc_return_msg","test msg");
	
	var choice = elm.getAttribute("return");
	var hasChocies = true;

	var evt = document.createEvent("Events");

    if(DEBUG){alert("dispatchEvent");}
	evt.initEvent("ABC4TrustResultEvent", true, false);
	elm.dispatchEvent(evt);
	
	if(DEBUG){alert("FIREFOX PLUGIN: Got choice from idselect: "+choice+"\n\n"+sessionID + "\n" + verify_request);}
	
	var abcquery = new XMLHttpRequest();
	abcquery.open("POST","http://localhost:9100/service-user/user/createPresentationTokenIdentitySelection/"+sessionID,false);
	abcquery.setRequestHeader("Content-type", "text/plain");
	abcquery.send(choice);
	if(DEBUG){alert("sent choice to local ABC engine: "+abcquery.status);}
	
	if(abcquery.status!=200) {
		elm.setAttribute("abc_return_status",abcquery.status);
		elm.setAttribute("abc_return_msg","Failed to contact local ABC engine");
		if(DEBUG){alert("failed to contact local ABC engine, first msg");}
		
//		var uiWindow = elm.getAttribute("UIWindow");
//		if(DEBUG){alert("UIWINDOW " + uiWindow);}
//		uiWindow.close();
		
		return;
	}
	
	var verifyquery = new XMLHttpRequest();
	var verify_req;
	if(verify_request != null) {
		verify_req = verify_request;
	} else {
		verify_req = verify_server+"/verify/"+verify_credentials;
	}
	verifyquery.open("POST",verify_req,false);

	verifyquery.setRequestHeader("Content-type", "application/xml");
	if(DEBUG){alert("plugin sending presentationtoken to verify server");}	
	verifyquery.send(abcquery.responseText);
	if(verifyquery.status!=200&&verifyquery.status!=202&&verifyquery.status!=204) {
		elm.setAttribute("abc_return_token",verifyquery.responseText);
		elm.setAttribute("abc_return_status",verifyquery.status);
		elm.setAttribute("abc_return_msg","Failed to contact local ABC engine");
		if(DEBUG){alert("failed to contact verifier");}

//		var uiWindow = elm.getAttribute("UIWindow");
//		uiWindow.close();
		
		return;
	} 
	elm.setAttribute("abc_return_token",verifyquery.responseText);
	elm.setAttribute("abc_return_status",verifyquery.status);
	elm.setAttribute("abc_return_msg","Verfication was succesful");

	if(DEBUG){alert("A OK!");}
    	
//	var uiWindow = elm.getAttribute("UIWindow");
//	uiWindow.close();
	
	return;
	
}

function storeData(elm){
	var DEBUG = false;
	var enableDebug = elm.getAttribute("debug_in_extension");
	if(enableDebug != null&&(enableDebug==true||enableDebug=='true')) {
		DEBUG = true;
	}
	
//  do we have key = value
//	var value = elm.getAttribute("value");
	var value = elm.getAttribute("value");

	var sessionID = "StoreData" + new Date().getTime()+""+Math.floor(Math.random()*99999);
	elm.setAttribute("session_id", sessionID); 

	var smartcardAvailable = checkSmartcard(elm, sessionID);
	if(! smartcardAvailable) {
		return true;
	}

	if(DEBUG){alert("storeData : " + value);}	

	// if key = value - add key to request
	var abcquery = new XMLHttpRequest();
	abcquery.open("POST","http://localhost:9100/service-user/user/storeData/"+sessionID,false);
	abcquery.setRequestHeader("Content-type", "text/plain");
	abcquery.send(value);

	if(DEBUG){alert("response from local abce : "+abcquery.status);}
	
	if(abcquery.status==200) {
		elm.setAttribute("abc_return_status",200);
		elm.setAttribute("abc_return_msg","Data stored");
		if(DEBUG){alert("Data stored...");}
		return;
	}
	 
	elm.setAttribute("abc_return_status",500);
	elm.setAttribute("abc_return_msg","Failed to store data");
	if(DEBUG){alert("Failed to store data");}
	return;
}

function loadData(elm){
	var DEBUG = false;
	var enableDebug = elm.getAttribute("debug_in_extension");
	if(enableDebug != null&&(enableDebug==true||enableDebug=='true')) {
		DEBUG = true;
	}

	var sessionID = "LoadData" + new Date().getTime()+""+Math.floor(Math.random()*99999);
	elm.setAttribute("session_id", sessionID); 

	var smartcardAvailable = checkSmartcard(elm, sessionID);
	if(! smartcardAvailable) {
		return true;
	}

	if(DEBUG){alert("load Data");}	

	// if key = value - add key to request
	var abcquery = new XMLHttpRequest();
	abcquery.open("GET","http://localhost:9100/service-user/user/loadData/"+sessionID,false);
	abcquery.setRequestHeader("Content-type", "text/plain");
	abcquery.setRequestHeader("Accept", "text/plain");
	abcquery.send();

	if(DEBUG){alert("response from local abce : "+abcquery.status);}
	
	if(abcquery.status==200) {
		elm.setAttribute("abc_return_status",200);
		elm.setAttribute("abc_return_msg","Data loaded");
		elm.setAttribute("value", abcquery.responseText);
		
		if(DEBUG){alert("Data loaded : " + abcquery.responseText);}
		return;
	}
	 
	elm.setAttribute("abc_return_status",500);
	elm.setAttribute("abc_return_msg","Failed to load data");
	if(DEBUG){alert("Failed to store data");}
	return;
}

function showCredentialUI() {
	var DEBUG = false;
	
	var sessionID = "CredentialUI" + new Date().getTime()+""+Math.floor(Math.random()*99999);

	var smartcardAvailable = checkSmartcard(null, sessionID);
	if(! smartcardAvailable) {
		return true;
	}

	if(DEBUG){alert("showCredentialUI");}

	// if key = value - add key to request
	var abcquery = new XMLHttpRequest();
	abcquery.open("GET","http://localhost:9100/service-user/user/getCredentialDescriptionList/"+sessionID,false);
//	abcquery.setRequestHeader("Content-type", "text/plain");
//	abcquery.setRequestHeader("Accept", "application/json");
	abcquery.send();

	if(abcquery.status==200) {
		if(DEBUG){alert("Credential List : " + abcquery.responseText);}
 		var jsonChoices = eval('('+abcquery.responseText+')');
		populateCredentialUI(jsonChoices, "en"); //idselect.js
	} else {
		if(DEBUG){alert("response from local abce : "+abcquery.status);}
	}
	 
	return;
	
}

function doingNothing(){
	alert("waiting");
}

//stuff under here is from the stub, maybe we can reuse it or maybe we cant
function produceListOfCredentials(xmldocument){
	return ["Credential 1 - facebook.com", "Credential 2 - someurl.gov", "Credential 3 - credentials.com"];
}

function getJSONChoice(){
	return "{\"chosenIssuanceToken\":0, \"metadataToChange\":{}, \"chosenPseudonyms\":[], \"chosenInspectors\":[], \"chosenAttributeValues\":[]}";
}
