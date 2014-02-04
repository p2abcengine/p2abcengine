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

Components.utils.import("resource://abc4trust/EventConnector.jsm");

var USER_ABCE_SERVICE = "http://localhost:9300/idselect-user-service";
var USER_UI_SERVICE = "http://localhost:9093/user-ui";


var abc4trust = {

		init: function() {
			if(gBrowser) gBrowser.addEventListener("DOMContentLoaded", this.onPageLoad, false);
		},

		listCredentials: function() {
			var DEBUG = false;
			var strbundle = document.getElementById("overlayStrings");

			logMessage("showCredentialUI", "Start...");

			var sessionID = "CredentialUI" + new Date().getTime()+""+Math.floor(Math.random()*99999);

			var smartcardAvailable = checkSmartcard(null, sessionID);
			if(!smartcardAvailable) {
				logMessage("- error testing smartcard - smartcardStatus  : " + smartcardStatus);
				return true;
			}

			logMessage("showCredentialUI", "open CredentialUI Window");
			if(DEBUG){alert("showCredentialUI");}
			var language = "en";
			openSelectionUI(sessionID, "management", language); //idselect.js

		},

		checkRevocationStatus: function() {	  
			var DEBUG = false;
			var strbundle = document.getElementById("overlayStrings");

			logMessage("CheckRevocation", "Start...");

			var sessionID = "RevocationStatus" + new Date().getTime()+""+Math.floor(Math.random()*99999);

			var smartcardAvailable = checkSmartcard(null, sessionID);
			if(!smartcardAvailable) {
				logMessage("CheckRevocation", "- error testing smartcard - smartcardStatus  : " + smartcardStatus);
				return true;
			}

			alert(strbundle.getString("checking_revocation_status"));
			var abcquery = new XMLHttpRequest();
			abcquery.open("POST",USER_ABCE_SERVICE + "/user/checkRevocationStatus",false);
			try{
				abcquery.send();
			}catch(failed){
				alert(strbundle.getString("server_unavailable"));
			}
			if(abcquery.status == 200){
				alert(strbundle.getString("check_revocation_status_ok"));
			}else{
				alert(strbundle.getString("check_revocation_status_fail"));
			}
		},

		debugInfo: function() {
			// showDebugInfo();
			var strbundle = document.getElementById("overlayStrings");
			var sessionID = "DebugInfo" + new Date().getTime()+""+Math.floor(Math.random()*99999);

			var params = "";
			//Fixed window size 
			var width = 560; //+ 280 //original : 980 
			var height = 680; //default 
			var left = (screen.width - width)/2;
			var top = (screen.height - height)/2;
			var params = 'width=' + width + ', height=' + height;

			params += ', top=' + top + ', left=' + left;
			params += ',resizable= yes';
			params += ',scrollbars= yes';
			params += ',location= no';
			params += ',directories= no';

			try {
				var debugInfoURL =USER_ABCE_SERVICE + "/user/getDebugInfo/"+sessionID;
				var debugInfoWindow = window.open(debugInfoURL, strbundle.getString("debug_info"), params);
			} catch(exception) {
				alert(strbundle.getString("debug_info_fail")+" : " + exception);
			}

		},

		backupSmartcard: function() {
			var DEBUG = false;
			var strbundle = document.getElementById("overlayStrings");

			logMessage("backupSmartcard", "Start...");

			var sessionID = "backupSmartcard" + new Date().getTime()+""+Math.floor(Math.random()*99999);

			var smartcardAvailable = checkSmartcard(null, sessionID);
			if(!smartcardAvailable) {
				logMessage("backupSmartcard", "- error testing smartcard - smartcardStatus  : " + smartcardStatus);
				return true;
			}

			var abcquery = new XMLHttpRequest();
			abcquery.open("GET",USER_ABCE_SERVICE + "/user/backupExists/"+sessionID,false);
			abcquery.send();
			if(abcquery.status == 204){
				var choice = confirm(strbundle.getString("backup_file_exists"));
				if(choice == false){
					return false;
				}
			}

			var prompts = Components.classes["@mozilla.org/embedcomp/prompt-service;1"]
			.getService(Components.interfaces.nsIPromptService);

			var password = {value: ""};              // default the password to pass
			var check = {value: false};                   // default the checkbox to true

			var result = prompts.promptPassword(null, strbundle.getString("authenticate_password"), strbundle.getString("enter_password_backup"), password, null, check);

			if(! result) {
				// user cancelled
				if(DEBUG) {alert("User cancelled backup procedure");};
				elm.setAttribute("abc_return_status",400);
				elm.setAttribute("abc_return_msg","Password not entered");
				return false;
			}

			abcquery = new XMLHttpRequest();
			abcquery.open("POST",USER_ABCE_SERVICE + "/user/backupSmartcard/"+sessionID,false);	
			abcquery.send(password.value);
			if(abcquery.status >= 200 && abcquery.status <= 204){
				alert(strbundle.getString("backup_sc_success"));
			}else{
				alert(strbundle.getString("backup_sc_fail"));
			}
		},

		restoreSmartcard: function() {
			var DEBUG = false;
			var strbundle = document.getElementById("overlayStrings");

			logMessage("restoreSmartcard", "restoreSmartcard Start...");

			var sessionID = "restoreSmartcard" + new Date().getTime()+""+Math.floor(Math.random()*99999);

			var smartcardAvailable = checkSmartcard(null, sessionID);
			if(!smartcardAvailable) {
				logMessage("restoreSmartcard", "- error testing smartcard - smartcardStatus  : " + smartcardStatus);
				return true;
			}

			var prompts = Components.classes["@mozilla.org/embedcomp/prompt-service;1"]
			.getService(Components.interfaces.nsIPromptService);

			var password = {value: ""};              // default the password to pass
			var check = {value: false};                   // default the checkbox to true

			var result = prompts.promptPassword(null, strbundle.getString("authenticate_password"), strbundle.getString("enter_password_restore"), password, null, check);

			if(! result) {
				// user cancelled
				if(DEBUG) {alert("User cancelled backup procedure");};
				elm.setAttribute("abc_return_status",400);
				elm.setAttribute("abc_return_msg","Password not entered");
				return false;
			}

			var abcquery = new XMLHttpRequest();
			abcquery.open("POST",USER_ABCE_SERVICE + "/user/restoreSmartcard/"+sessionID,false);	
			abcquery.send(password.value);

			if(abcquery.status >= 200 && abcquery.status <= 204){
				alert(strbundle.getString("sc_restore_success"));
			}else{
				alert(strbundle.getString("sc_restore_fail"));
			}
		},

		changePin: function(){	  
			var DEBUG = false;
			var strbundle = document.getElementById("overlayStrings");

			logMessage("changePin", "Start...");

			var sessionID = "changePin" + new Date().getTime()+""+Math.floor(Math.random()*99999);
			if(DEBUG){alert("- check smartcard!");}

			var result = checkSmartcardInserted(sessionID);
			if(DEBUG){alert("- check smartcard done : " + result.status + " : " + result.message);}

			if(result.status != 406 && result.status >= 400) {
				logMessage("changePin", "- error testing smartcard - smartcardStatus  : " + result.status + " : " + result.message);
				return true;
			}
			alert(strbundle.getString("change_pin"));	 

			var prompts = Components.classes["@mozilla.org/embedcomp/prompt-service;1"]
			.getService(Components.interfaces.nsIPromptService);

			var currentPin = {value: ""};              // default the password to pass
			var newPin = {value: ""};              // default the password to pass
			var check = {value: false};                   // default the checkbox to true

			var result1 = prompts.promptPassword(null, strbundle.getString("authenticate_password"), strbundle.getString("enter_current_pin")+":", currentPin, null, check);

			var result2 = prompts.promptPassword(null, strbundle.getString("authenticate_password"), strbundle.getString("enter_new_pin")+":", newPin, null, check);

			if((currentPin.value.length != 4) || (newPin.value.length != 4)){
				alert(strbundle.getString("pin_long"));
				return false;
			}

			var abcquery = new XMLHttpRequest();
			abcquery.open("POST",USER_ABCE_SERVICE + "/user/changePin",false);

			abcquery.send(currentPin.value+" "+newPin.value);
			if(abcquery.status == 401){
				//unauthorized - wrong pin
				alert(strbundle.getString("wrong_pin_try_again"));
			}else if(abcquery.status == 402){
				//forbidden - locked card.
				alert(strbundle.getString("wrong_pin_locked"));
			}else if(abcquery.status >= 200 && abcquery.status <= 204){
				alert(strbundle.getString("pin_changed"));
			}else{
				alert(strbundle.getString("unknown_return_code"));
			}
		},

		unlockCard: function(){
			var DEBUG = false;
			var strbundle = document.getElementById("overlayStrings");

			logMessage("unlockCard", "Start...");

			var sessionID = "unlockCard" + new Date().getTime()+""+Math.floor(Math.random()*99999);

			logMessage("unlockCard", "- created tmpElement for response...");
			if(DEBUG){alert("- check smartcard!");}

			var result = checkSmartcardInserted(sessionID);
			if(DEBUG){alert("- check smartcard done : " + result.status + " : " + result.message);}

			if(result.status != 406 && result.status >= 400) {
				logMessage("unlockCard", "- error testing smartcard - smartcardStatus  : " + result.status + " : " + result.message);
				return true;
			}
			alert(strbundle.getString("unlock_sc"));

			var prompts = Components.classes["@mozilla.org/embedcomp/prompt-service;1"]
			.getService(Components.interfaces.nsIPromptService);

			var puk = {value: ""};              // default the password to pass
			var pin = {value: ""};              // default the password to pass
			var check = {value: false};                   // default the checkbox to true

			var result1 = prompts.promptPassword(null, strbundle.getString("authenticate_password"), strbundle.getString("enter_puk")+":", puk, null, check);

			var result2 = prompts.promptPassword(null, strbundle.getString("authenticate_password"), strbundle.getString("enter_new_pin")+":", pin, null, check);

			if((pin.value.length != 4) || (puk.value.length != 8)){
				alert(strbundle.getString("pin_puk_long"));
				return false;
			}

			var abcquery = new XMLHttpRequest();
			abcquery.open("POST",USER_ABCE_SERVICE + "/user/unlockCard",false);	
			abcquery.send(puk.value+" "+pin.value);
			if(abcquery.status == 401){
				alert(strbundle.getString("wrong_puk_try_again"));
			}else if(abcquery.status == 402){
				alert(strbundle.getString("wrong_puk_dead"));
			}else if(abcquery.status >= 200 && abcquery.status <= 204){
				alert(strbundle.getString("card_unlocked"));
			}else{
				alert(strbundle.getString("unknown_return_code_unlock"));
			}
		},
		
		getAttendanceData: function(){
			var sessionID = "getAttendaceData" + new Date().getTime()+""+Math.floor(Math.random()*99999);

			var smartcardAvailable = checkSmartcard(null, sessionID);
			if(!smartcardAvailable) {
				logMessage("getAttendanceData", "- error testing smartcard - smartcardStatus  : " + smartcardStatus);
				return true;
			}
			var abcquery = new XMLHttpRequest();
			abcquery.open("GET",USER_ABCE_SERVICE + "/info/getAttendanceData",false);
			abcquery.send();
			if(abcquery.status == 200){
				if(abcquery.responseText == "-1"){
					alert("Error in getting attendance count. Most likely you do not have a course credential.");
				}else{
					alert("You have an attendance count of: "+abcquery.responseText);
				}
			}else{
				alert("Unknown return code");
			}
		},

		dispatchResult: function(evt, moveAttributeList) {
			var strbundle = document.getElementById("overlayStrings");
			connector.element.setAttribute("abc_return_status",evt.target.getAttribute("abc_return_status"));
			connector.element.setAttribute("abc_return_msg",evt.target.getAttribute("abc_return_msg"));
			if(moveAttributeList != null) {
				for(var ix=0; ix<moveAttributeList.length; ix++) {
					var attributeName = moveAttributeList[ix];
					connector.element.setAttribute(attributeName,evt.target.getAttribute(attributeName));
				}
			}
			var event = document.createEvent("Events");

			event.initEvent("ABC4TrustResultEvent", true, false);
			connector.element.dispatchEvent(event);
		},

		ABC4TrustListener: function(evt) {
			var strbundle = document.getElementById("overlayStrings");

			var method = evt.target.getAttribute("abc4trust_command");
			logMessage("ABC4TrustListener", "Main dispatcher - method : " + method);

			switch(method){
			case "issue":
				connector.element = evt.target;
				var initialError = issue(evt.target);
				if(initialError!=null && initialError == true) {
					abc4trust.dispatchResult(evt, null);
				}
				break;
			case "present":
			case "verify": // OLD WRONG NAME
				connector.element = evt.target;
				var initialError = present(evt.target);
				if(initialError!=null && initialError == true) {
					var moveAttributeList = new Array();
					moveAttributeList[0] = "abc_return_token";
					abc4trust.dispatchResult(evt, moveAttributeList);
				}
				break;
			case "tokenSelected_present":

				evt.target.setAttribute("policy_request",connector.element.getAttribute("policy_request"));
				// 'verify_request' deprecated - and value moved to 'present_request'
				// 	evt.target.setAttribute("verify_request",connector.element.getAttribute("verify_request"));
				evt.target.setAttribute("actual_present_request",connector.element.getAttribute("actual_present_request"));

				evt.target.setAttribute("session_id",connector.element.getAttribute("session_id"));
				evt.target.setAttribute("debug_in_extension",connector.element.getAttribute("debug_in_extension"));

				hasChoice_present(evt.target);

				var moveAttributeList = new Array();
				moveAttributeList[0] = "abc_return_token";
				abc4trust.dispatchResult(evt, moveAttributeList);
				break;
			case "tokenSelected_issue":

				evt.target.setAttribute("start_request",connector.element.getAttribute("start_request"));
				evt.target.setAttribute("step_request",connector.element.getAttribute("step_request"));
				evt.target.setAttribute("status_request",connector.element.getAttribute("status_request"));
				evt.target.setAttribute("session_id",connector.element.getAttribute("session_id"));
				evt.target.setAttribute("debug_in_extension",connector.element.getAttribute("debug_in_extension"));

				hasChoice_issue(evt.target);

				abc4trust.dispatchResult(evt, null);
				break;

			case "storeData":
				connector.element = evt.target;
				storeData(evt.target);

				abc4trust.dispatchResult(evt, null);

				break;
			case "loadData":
				connector.element = evt.target;
				loadData(evt.target);

				var moveAttributeList = new Array();
				moveAttributeList[0] = "value";
				abc4trust.dispatchResult(evt, moveAttributeList);

				break;
			case "isSameSmartcard":
				connector.element = evt.target;
				isSameSmartcard(evt.target);
				abc4trust.dispatchResult(evt, null);
				break;
			case "isInstalled":
				connector.element = evt.target;
				evt.target.setAttribute("installed", "true")
				var moveAttributeList = new Array();
				moveAttributeList[0] = "installed";
				abc4trust.dispatchResult(evt, moveAttributeList);
				break;
			default:
				alert("default reached : " + method);	

			}

		}
		
};

window.addEventListener("load", function() { abc4trust.init(); }, false); 
document.addEventListener("ABC4TrustEvent", function(e) { abc4trust.ABC4TrustListener(e); }, false, true); // The last value is a Mozilla-specific value to indicate untrusted content is allowed to trigger the event.  
