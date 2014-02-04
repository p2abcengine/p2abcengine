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

function getConsoleLogger() {
	var aConsoleService = Components.classes["@mozilla.org/consoleservice;1"].
	     getService(Components.interfaces.nsIConsoleService);
	return aConsoleService;
}
function logMessage(method, message) {
	try {
		var aConsoleService = getConsoleLogger();
		aConsoleService.logStringMessage(method + " : " + message);
	} catch(cannot_recover_so_ignore) {
	}
}

function checkSmartcardInserted(sessionID) {
	logMessage("checkSmartcardInserted", "Start : " + sessionID);
	var DEBUG = false;
	var strbundle = document.getElementById("overlayStrings");

	var result = new Object();
	// Check if smartcard is available!
	var abcquery = new XMLHttpRequest();
	abcquery.open("GET",USER_ABCE_SERVICE + "/user/checkSmartcard/"+sessionID,false);
	try {
		abcquery.send();
		result.status = abcquery.status;
	} catch(connect_userabce_failed) {
	    result.status = 400;
	}

	if(DEBUG){alert("- check smartcard done : " + result.status);}

	if(result.status>=400) {
		if(result.status == 400){
			result.message = strbundle.getString("user_service_unavailable");
			logMessage("checkSmartcardInserted", "Error : UserService not running! " + result.message);
		} else if(result.status == 410){
			result.message = strbundle.getString("smartcard_connected");
			logMessage("checkSmartcardInserted", "Error : No card found! " + result.message);
		} else if(result.status == 406){
			result.message = "PIN Needed";
			logMessage("checkSmartcardInserted", "PIN : " + result.message);
		} else {
			result.message = "checkSmartcardInserted - UNKNOWN ERROR STATUS CODE : " + result.status;
			logMessage("checkSmartcardInserted", "Error : " + result.message);
		}
	} else {
		result.message = "Card is present";
		logMessage("checkSmartcardInserted", "Info : Smartcard found " + result.status + " : " + result.message);
	}
	return result;
}

function checkSmartcard(elm, sessionID) {
	var DEBUG = false;
	var strbundle = document.getElementById("overlayStrings");
	
	// Check if smartcard is available!
	var result = checkSmartcardInserted(sessionID);
	if(result.status != 406 && result.status >= 400){
		if(DEBUG) {alert("User ABCE Not Running : " + connect_userabce_failed);};
		if(elm!=null) {
			elm.setAttribute("abc_return_status",result.status);
			elm.setAttribute("abc_return_msg",result.message);
		}
		alert(result.message);
		return false;
	}
	if(result.status == 204) {
		//Same card as before - no actions need to be taken.
		if(elm!=null) {
			elm.setAttribute("abc_pin", 0);
		}
		return true;
	}	
	
	// Not available in UserABCE - ask for PIN
	var prompts = Components.classes["@mozilla.org/embedcomp/prompt-service;1"]
	                        .getService(Components.interfaces.nsIPromptService);

	var password = {value: ""};              // default the password to pass
    var check = {value: false};                   // default the checkbox to true

	var result = prompts.promptPassword(null, strbundle.getString("authenticate_password"), strbundle.getString("enter_current_pin")+":", password, null, check);
	
	if(! result) {
		// user cancelled
		if(DEBUG) {alert("User cancelled Smartcard Pin");};
		if(elm!=null) {
			elm.setAttribute("abc_return_status",400);
			elm.setAttribute("abc_return_msg","Pin not entered");
		}
		return false;
	}
	if(password.value.length != 4){
		alert(strbundle.getString("pin_long"));
		return false;
	}
	// post pin to User ABCE
	var abcquery = new XMLHttpRequest();
	abcquery.open("POST",USER_ABCE_SERVICE + "/user/unlockSmartcards/"+sessionID,false);
	abcquery.setRequestHeader("Content-type", "text/plain");
	abcquery.send(password.value);
	
	if(abcquery.status>=200 && abcquery.status<=204) {
		if(elm!=null) {
			elm.setAttribute("abc_pin", password.value);
		}
		return true;
	} else {
		if(DEBUG) {alert("Smartcard Not Accepted : " + abcquery.status);};
		if(elm!=null) {
			elm.setAttribute("abc_return_status",abcquery.status);
			if(abcquery.status == 401){
				//Unauthorized
				elm.setAttribute("abc_return_msg","Smartcard Not Accepted - wrong PIN entered. Try again");
				alert(strbundle.getString("wrong_pin_try_again"));
				return checkSmartcard(elm, sessionID);
			}else if(abcquery.status = 403){
				//forbidden. Card locked				
				elm.setAttribute("abc_return_msg","Smartcard Not Accepted - wrong PIN entered. Your card is now locked. Unlock using your PUK code");
				alert(strbundle.getString("wrong_pin_locked"));
			}else{
				elm.setAttribute("abc_return_msg","Smartcard Not Accepted - Unknown cause");
				alert(strbundle.getString("unknown_return_code_unlock"));
			}

		}
		return false;
	}
}

function isSameSmartcard(elm){
	var sessionID = "isSameSmartcard" + new Date().getTime()+""+Math.floor(Math.random()*99999);
	logMessage("isSameSmartcard", "Info : Start " + sessionID);

	elm.setAttribute("session_id", sessionID);
    var result = checkSmartcardInserted(sessionID);
	if(result.status>=400) {
	    // on all errors return 400
		elm.setAttribute("abc_return_status",400);
		elm.setAttribute("abc_return_msg", "Missing UserService/No Card Inserted/PIN Needed");
		return;
	}
	var abcquery = new XMLHttpRequest();
	abcquery.open("GET",USER_ABCE_SERVICE + "/user/isSameSmartcard/"+sessionID,false);
	try {
		abcquery.send();
		elm.setAttribute("abc_return_status",200);
		elm.setAttribute("abc_return_msg","Same Smartcard");
		logMessage("isSameSmartcard", "Info : isSameSmartcard" + sessionID);
		return;
	} catch(connect_userabce_failed) {
		// ignore - always 410
	}
	elm.setAttribute("abc_return_status",410);
	elm.setAttribute("abc_return_msg","NOT Same Smartcard");
	logMessage("isSameSmartcard", "Info : NOT SameSmartcard" + sessionID);
	return;
}
	

function XXXcheckPseudonym(elm, sessionID){
	var DEBUG = false;
	var abcquery = new XMLHttpRequestRequest();
	var pin = elm.getAttribute("abc_pin");
	abcquery.open("POST",USER_ABCE_SERVICE + "/user/generatePseudonym/"+sessionID,false);
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
	var strbundle = document.getElementById("overlayStrings");
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
			abcquery.open("POST",USER_ABCE_SERVICE + "/user/issuanceProtocolStep/"+sessionID+
					"?startRequest="+start_request_mod+
					"&stepRequest="+step_request_mod,false);
			//abcquery.open("POST",USER_ABCE_SERVICE + "/user/issuanceProtocolStep/"+sessionID,false);
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
			
			if(abcquery.status!=200 && abcquery.status != 203 && abcquery.status != 204 && abcquery.status != 501) {
				elm.setAttribute("abc_return_status",abcquery.status);
				elm.setAttribute("abc_return_msg","Local ABC engine did not return an Issuancemessage.");
				if(DEBUG) {alert("Failed to contact local ABC engine, IssuanceToken (first msg): "+abcquery.status);}
				// return 'initialError' : true
				reportIssuanceStatus(elm, false);
				return true;
			} else if(abcquery.status == 203){
				//we got some JSON, present it and send it to abc engine
				if(DEBUG){alert("this is ISSUANCE id Selection:\n"+abcquery.responseText);}
				//var choice = "this is the choice returned by ID Selection"; //should be obtained using the id selection plugin
		 		//var jsonChoices = eval('('+abcquery.responseText+')');
				hasChoices = false;
				openSelectionUI(sessionID, "issuance", language); //idselect.js
				return;
			} else if(abcquery.status == 200){
				if(DEBUG) {alert("got some xml from the ABCE:\n"+abcquery.responseText);}
				xmlhttp = new XMLHttpRequest();
				xmlhttp.open("POST", step_request, false);
				xmlhttp.setRequestHeader("Content-type", "application/xml; charset=utf-8");
				xmlhttp.setRequestHeader("Accept", "text/xml,application/xml");
				xmlhttp.send(abcquery.responseText);
			} else if(abcquery.status == 204){
				elm.setAttribute("abc_return_status",200);
				elm.setAttribute("abc_return_msg","Credential obtained... SUCCESS");
				if(DEBUG) {alert("credential obtained.... success!");}
				reportIssuanceStatus(elm, true);
				return;
			} else if(abcquery.status == 501){
				//insufficient storage
				alert(strbundle.getString("insufficient_storage"));
				return;
			}
		}
	}
}

function hasChoice_issue(elm){

	logMessage("Issue-TokenSelected", "Start...");
	
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
	var hasChoices = true;

	//	if(DEBUG){alert("FIREFOX PLUGIN : ISSUE : Got choice from idselect: "+choice+"\n\n"+sessionID);}
	
	// request to Issuer
	var xmlhttp = new XMLHttpRequest();

	// request to local User ABCE
	var abcquery = new XMLHttpRequest();
	//encode URL for re-issuance
	var start_request_mod = encodeURIComponent(start_request);
	var step_request_mod = encodeURIComponent(step_request);
	if(DEBUG){
		alert("start request encoded: "+start_request_mod);
		alert("step request encoded: "+step_request_mod);
	}
	abcquery.open("POST",USER_ABCE_SERVICE + "/user/issuanceProtocolStepSelect/"+sessionID+
			"?startRequest="+start_request_mod+
			"&stepRequest="+step_request_mod,false);
	//abcquery.open("POST",USER_ABCE_SERVICE + "/user/issuanceProtocolStepSelect/"+sessionID,false);
	abcquery.setRequestHeader("Content-type", "application/json");
	abcquery.setRequestHeader("Accept", "text/xml,application/xml");
	if(DEBUG) { alert("now sending our choice back to user abce:\n"+choice);}
	logMessage("Issue-TokenSelected", "now sending our choice back to user abce:\n"+choice);
	try {
		abcquery.send(choice);
	} catch(failed) {
		logMessage("Issue-TokenSelected", "- failed sending to service");
	}

	if(DEBUG) { alert(" - got response : "+abcquery.status);}
	logMessage("Issue-TokenSelected", " - got response : "+abcquery.status);
	
	while(true) {
		if(abcquery.status == 200){
			// send to issuer
			if(DEBUG) {alert("got some xml from the ABCE, sending it to issuer:\n"+abcquery.responseText);}
			xmlhttp = new XMLHttpRequest();
			xmlhttp.open("POST", step_request, false);
			xmlhttp.setRequestHeader("Content-type", "application/xml; charset=utf-8");
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
		abcquery.open("POST",USER_ABCE_SERVICE + "/user/issuanceProtocolStep/"+sessionID+
				"?startRequest="+start_request_mod+
				"&stepRequest="+step_request_mod,false);
		//abcquery.open("POST",USER_ABCE_SERVICE + "/user/issuanceProtocolStep/"+sessionID,false);
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
		 	//var jsonChoices = eval('('+abcquery.responseText+')');
			hasChoices = false;
			openSelectionUI(sessionID, "issuance", language); //idselect.js
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

// NOTE : Was 'verify'
function present(elm){
	var DEBUG = false;
	var enableDebug = elm.getAttribute("debug_in_extension");
	if(enableDebug != null&&(enableDebug==true||enableDebug=='true')) {
		DEBUG = true;
	}
	
	var sessionID = "Present" + new Date().getTime()+""+Math.floor(Math.random()*99999);
	logMessage("Present", "Run presentation with sessionID : " + sessionID);

	// Check that Smartcard is present!
	var smartcardAvailable = checkSmartcard(elm, sessionID);
	if(! smartcardAvailable) {
		return true;
	}
	
	//
	var policy_request_url = elm.getAttribute("policy_request");

	// first check old verify_request
 	var present_request_url = elm.getAttribute("verify_request");

	if(present_request_url == null || present_request_url == "") {
		// try new present_request
 		present_request_url = elm.getAttribute("present_request");
		if(present_request_url == null || present_request_url == "") {
			logMessage("Present", "ERROR - neither 'present_request' or 'verify_request' was specified on call to Firefox Extension.");
			elm.setAttribute("abc_return_status",500);
			elm.setAttribute("abc_return_msg","Neither 'present_request' or (old'verify_request') was specified on call to Firefox Extension");
			return true;
		}
	}
	elm.setAttribute("actual_present_request", present_request_url);
	logMessage("Present", "Verify/Present start - session : " + sessionID + " - policy url : " + policy_request_url + " - verify/present url : " + present_request_url);
	
    var language = elm.getAttribute("language");
	if(language == null) {
		language = "en";
	}

	elm.setAttribute("session_id", sessionID); 

	// Obtain the presentation policy
	var policyRequest = new XMLHttpRequest();
	
	if(DEBUG){alert("policy request : "+policy_request_url);}	
	logMessage("Present", "Get Policy from : " + policy_request_url);
	policyRequest.open("GET",policy_request_url,false);
	
	policyRequest.setRequestHeader("Content-type", "application/xml");
	try {
		policyRequest.send(null);
	} catch(connect_verifier_failed) {
		logMessage("Present", "Connect to Verifier Failed :  " + policyRequest.status + " : " + policyRequest.statusText);
		if(DEBUG) {alert("Connect to Verifier Failed : " + connect_verifier_failed);};
		elm.setAttribute("abc_return_status",400);
		elm.setAttribute("abc_return_msg","Could not contact verification service");
		return true;
	}
	
	if(policyRequest.status!=200) {
		logMessage("Present", "Status should be 200 (OK) retrieving Policy : " + policyRequest.status + " : " + policyRequest.statusText);
		elm.setAttribute("abc_return_status",policyRequest.status);
		elm.setAttribute("abc_return_msg","Failed to obtain presentation policy");
		if(DEBUG){alert("Failed to obtain presentation policy");}
		// return 'initialError' : true
		return true;
	}
	// sanity check of policy
	if(policyRequest.responseText==null||policyRequest.responseText.length==0) {
		logMessage("Present", "ERROR : PresentationPolicy Seems to be empty! But Response From server was : (" + policyRequest.responseText + ") - HTTP Status : " + policyRequest.status + " : " + policyRequest.statusText);
	} else {
		logMessage("Present", "PresentationPolicy has content : " + policyRequest.responseText.length);
	}
	
	if(DEBUG){alert("policy:\n"+policyRequest.responseText);}	
	// Pass the presentationpolicy to the local user abce
	var abcquery = new XMLHttpRequest();
	abcquery.open("POST",USER_ABCE_SERVICE + "/user/createPresentationToken/"+sessionID,false);
	abcquery.setRequestHeader("Content-type", "application/xml");
	try {
		logMessage("Present", "Send Policy to UserABCE...");
		abcquery.send(policyRequest.responseText);
	} catch(connect_userabce_failed) {
		logMessage("Present", "User ABCE Not Running (Verification) : " + connect_userabce_failed);
		if(DEBUG) {alert("User ABCE Not Running (Verification) : " + connect_userabce_failed);};
		elm.setAttribute("abc_return_status",400);
		elm.setAttribute("abc_return_msg","User ABCE Not Running (Verification)");
		return true;
	}
	if(DEBUG){alert("send a post to local abce, should get a json object with choices back "+abcquery.status);}
//	if(DEBUG){alert(abcquery.responseText);}
	
	if(abcquery.status==422) {
		logMessage("Present", "Can not satisfy policy : " + abcquery.status + " : " + abcquery.statusText);
		elm.setAttribute("abc_return_status",abcquery.status);
		elm.setAttribute("abc_return_msg","Can not satisfy policy");
		if(DEBUG){alert("Cannot satisfy policy");}
		// return 'initialError' : true
		return true;
	}
	
	if(abcquery.status!=200 && abcquery.status !=203) {
		logMessage("Present", "UserABCE return unknown error : " + abcquery.status + " : " + abcquery.statusText);
		elm.setAttribute("abc_return_status",abcquery.status);
		elm.setAttribute("abc_return_msg","Failed to contact local ABC engine");
		if(DEBUG){alert("failed to contact local ABC engine, first msg");}
		// return 'initialError' : true
		return true;
	}
	
	if(abcquery.status== 203){
		logMessage("Present", "UserABCE return 203 - Show ID Selector...");
		//we got some JSON, present it and send it to abc engine
		if(DEBUG){alert("this is id Selection:\n"+abcquery.responseText);}
		//var choice = "this is the choice returned by ID Selection"; //should be obtained using the id selection plugin
// 		var jsonChoices = eval('('+abcquery.responseText+')');
		hasChoices = false;
		openSelectionUI(sessionID, "presentation", language); //idselect.js
		return;
	}

	logMessage("Present", "UserABCE return 200 - We got PresentationToken without ID Selector...");

	var presentRequest = new XMLHttpRequest();
	presentRequest.open("POST",present_request_url,false);

	presentRequest.setRequestHeader("Content-type", "application/xml");
	if(DEBUG){alert("plugin sending presentationtoken to Verifier : " + present_request_url);}
	try{
		logMessage("Present", "Send PresentationToken to Verifier : " + present_request_url);	
		presentRequest.send(abcquery.responseText);
	}catch(connect_verifier_failed){
		logMessage("Present", "Connect to Verifier Failed :  " + presentRequest.status + " : " + presentRequest.statusText);
		if(DEBUG) {alert("Connect to Verifier Failed : " + connect_verifier_failed);};
		elm.setAttribute("abc_return_status",400);
		elm.setAttribute("abc_return_msg","Could not contact verification service");
		return true;
	}
	
	var responseText = presentRequest.responseText;
	if(responseText != null && responseText.length>100) {
		logMessage("Present", "ResponseText was too long # chars > 100");
		responseText = "Error length > 100 - was : " + responseText.length;
	} 
	if(presentRequest.status!=200&&presentRequest.status!=202&&presentRequest.status!=204) {
		logMessage("Present", "Verifier REJECTED PresentationToken : " + presentRequest.status + " : " + presentRequest.statusText + " - Token From Verifier : (" + responseText + ")");
		if(responseText!=null) {
			elm.setAttribute("abc_return_token",responseText);
		}	
		elm.setAttribute("abc_return_status",presentRequest.status);
		elm.setAttribute("abc_return_msg","Verifier rejected PresentationToken : check status variables : abc_return_status and abc_return_token");
		if(DEBUG){alert("failed to contact verifier");}

		return;
	} 
	logMessage("Present", "Verifier Accepted PresentationToken : " + presentRequest.status + " : " + presentRequest.statusText + " - Token From Verifier : (" + responseText + ") " + presentRequest.getResponseHeader('content-type'));
	if(responseText!=null) {
		elm.setAttribute("abc_return_token",responseText);
	}	
	elm.setAttribute("abc_return_status",presentRequest.status);
	elm.setAttribute("abc_return_msg","Verfication was succesful");

	logMessage("Present", "Presentation OK!");
	return;
}

function hasChoice_present(elm){
	var DEBUG = false;
	var enableDebug = elm.getAttribute("debug_in_extension");
	if(enableDebug != null&&(enableDebug==true||enableDebug=='true')) {
		DEBUG = true;
	}
	var sessionID = elm.getAttribute("session_id"); 
	
	logMessage("Present-TokenSelected", "IDSelector Closed - continue Session : " + sessionID);
	
 	var present_request_url = elm.getAttribute("actual_present_request");
	if(present_request_url == null) {
		logMessage("Present-TokenSelected", " - present_request_url NULL ???");
//		present_request_url = elm.getAttribute("verify_request");
	}
	// logMessage("Present-TokenSelected", " - present_request_url " + present_request_url);
	var policy_request_url = elm.getAttribute("policy_request");
	logMessage("Present-TokenSelected", "Verify/Present continue - session : " + sessionID + " - policy url : " + policy_request_url + " - verify/present url : " + present_request_url);
	
	elm.setAttribute("abc_return_status","test status");
	elm.setAttribute("abc_return_msg","test msg");
	
	var choice = elm.getAttribute("return");
	var hasChoices = true;

	if(DEBUG){alert("FIREFOX PLUGIN: Got choice from idselect: "+choice+"\n\n"+sessionID + "\n" + present_request_url);}
	
	var abcquery = new XMLHttpRequest();
	logMessage("Present-TokenSelected", "Get PresentationToken from UserABCE...");
	abcquery.open("POST",USER_ABCE_SERVICE + "/user/createPresentationTokenIdentitySelection/"+sessionID,false);
	abcquery.setRequestHeader("Content-type", "text/plain");
	try{
		abcquery.send(choice);
	}catch(connect_userABCE_failed){
		logMessage("Present-TokenSelected", "Connect to User ABCE Failed :  " + abcquery.status + " : " + abcquery.statusText);
		if(DEBUG) {alert("Connect to User ABCE Failed : " + connect_userABCE_failed);};
		elm.setAttribute("abc_return_status",400);
		elm.setAttribute("abc_return_msg","Could not contact user service");
		return true;
	}
	if(DEBUG){alert("sent choice to local ABC engine: "+abcquery.status);}
	
	if(abcquery.status!=200) {
		logMessage("Present-TokenSelected", "Failed to contact local ABC engine : " + abcquery.status + " : " + abcquery.statusText);
		elm.setAttribute("abc_return_status",abcquery.status);
		elm.setAttribute("abc_return_msg","Failed to contact local ABC engine");
		if(DEBUG){alert("failed to contact local ABC engine, first msg");}
		
		return;
	}
	
	logMessage("Present-TokenSelected", "User selected Credentials for PresentationToken...");

	var presentRequest = new XMLHttpRequest();
	presentRequest.open("POST",present_request_url,false);

	presentRequest.setRequestHeader("Content-type", "application/xml; charset=utf-8");
	if(DEBUG){alert("plugin sending presentationtoken to Verifier");}
	try{
		logMessage("Present-TokenSelected", "Send PresentationToken to Verifier - url : " + present_request_url);
		presentRequest.send(abcquery.responseText);
	}catch(connect_verifier_failed){
		logMessage("Present-TokenSelected", "Connect to Verifier Failed :  " + presentRequest.status + " : " + presentRequest.statusText);
		if(DEBUG) {alert("Connect to Verifier Failed : " + connect_verifier_failed);};
		elm.setAttribute("abc_return_status",400);
		elm.setAttribute("abc_return_msg","Could not contact verification service");
		return true;
	}
	var responseText = presentRequest.responseText;
	if(responseText != null && responseText.length>100) {
		logMessage("Present-TokenSelected", "ResponseText was too long # chars > 100");
		responseText = "Error length > 100 - was : " + responseText.length;
	} 
	if(presentRequest.status!=200&&presentRequest.status!=202&&presentRequest.status!=204) {
		logMessage("Present-TokenSelected", "Verifier REJECTED PresentationToken : " + presentRequest.status + " : " + presentRequest.statusText + " - Token From Verifier : (" + responseText + ")");
		if(responseText!=null) {
			elm.setAttribute("abc_return_token",responseText);
		}	
		elm.setAttribute("abc_return_status",presentRequest.status);
		elm.setAttribute("abc_return_msg","Verifier rejected PresentationToken : check status variables : abc_return_status and abc_return_token");
		if(DEBUG){alert("failed to contact verifier");}

		return;
	} 
	logMessage("Present-TokenSelected", "Verifier Accepted PresentationToken : " + presentRequest.status + " : " + presentRequest.statusText + " - Token From Verifier : (" + responseText + ") " + presentRequest.getResponseHeader('content-type'));
	if(responseText!=null) {
		elm.setAttribute("abc_return_token",responseText);
	}	
	elm.setAttribute("abc_return_status",presentRequest.status);
	elm.setAttribute("abc_return_msg","Verfication was succesful");

	logMessage("Present-TokenSelected", "Presentation OK!");
    	
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
	abcquery.open("POST",USER_ABCE_SERVICE + "/user/storeData/"+sessionID,false);
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
	abcquery.open("GET",USER_ABCE_SERVICE + "/user/loadData/"+sessionID,false);
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

function doingNothing(){
	alert("waiting");
}

