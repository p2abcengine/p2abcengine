Components.utils.import("resource://sampleapp/EventConnector.jsm");

var sampleapp = {

  
		
  init: function() {
	if(gBrowser) gBrowser.addEventListener("DOMContentLoaded", this.onPageLoad, false);

  },

  listCredentials: function() {
	showCredentialUI();
  },

  backupSmartcard: function() {		  
	alert("Please ensure that your smartcard is connected before continuing and do not remove it during the process!");
	var elm = document.createElement("ABC4TrustDataElement");
	elm.setAttribute("abc4trust_command", "checkSmartcard");
	var sessionID = "checkSmartcard" + new Date().getTime()+""+Math.floor(Math.random()*99999);
	elm.setAttribute("session_id", sessionID);
	var smartcardAvailable = checkSmartcard(elm, sessionID);
	if(!smartcardAvailable){
		alert("Smartcard not available.");
		return false;
	}
	
	var abcquery = new XMLHttpRequest();
	abcquery.open("GET","http://localhost:9100/service-user/user/backupExists/"+sessionID,false);
	abcquery.send();
	if(abcquery.status == 204){
		var choice = confirm("Backup file already exists. Pressing ok will overwrite it. Are you sure you want to continue?");
		if(choice == false){
			return false;
		}
	}
	
	var prompts = Components.classes["@mozilla.org/embedcomp/prompt-service;1"]
    .getService(Components.interfaces.nsIPromptService);

	var password = {value: ""};              // default the password to pass
	var check = {value: false};                   // default the checkbox to true
	
	var result = prompts.promptPassword(null, "Authenticate Password", "Enter Password for backup (exactly 8 characters long):", password, null, check);
	
	if(! result) {
	// user cancelled
	if(DEBUG) {alert("User cancelled backup procedure");};
		elm.setAttribute("abc_return_status",400);
		elm.setAttribute("abc_return_msg","Password not entered");
		return false;
	}
	
	abcquery = new XMLHttpRequest();
	abcquery.open("POST","http://localhost:9100/service-user/user/backupSmartcard/"+sessionID,false);	
	abcquery.send(password.value);
	if(abcquery.status >= 200 && abcquery.status <= 204){
		alert("backupSmartcard: Card backed up successfully!");
	}else{
		alert("backupSmartcard : Something went wrong during backup!");
	}
  },

  restoreSmartcard: function() {
	  alert("Please ensure that your smartcard is connected before continuing and do not remove it during the process!");
		var elm = document.createElement("ABC4TrustDataElement"); 
		elm.setAttribute("abc4trust_command", "checkSmartcard");
		var sessionID = "checkSmartcard" + new Date().getTime()+""+Math.floor(Math.random()*99999);
		elm.setAttribute("session_id", sessionID);
		var smartcardAvailable = checkSmartcard(elm, sessionID);
		if(!smartcardAvailable){
			alert("Smartcard not available.");
			return false;
		}
		
		var prompts = Components.classes["@mozilla.org/embedcomp/prompt-service;1"]
	    .getService(Components.interfaces.nsIPromptService);

		var password = {value: ""};              // default the password to pass
		var check = {value: false};                   // default the checkbox to true
		
		var result = prompts.promptPassword(null, "Authenticate Password", "Enter Password for backup:", password, null, check);
		
		if(! result) {
		// user cancelled
		if(DEBUG) {alert("User cancelled backup procedure");};
			elm.setAttribute("abc_return_status",400);
			elm.setAttribute("abc_return_msg","Password not entered");
			return false;
		}
		
		var abcquery = new XMLHttpRequest();
		abcquery.open("POST","http://localhost:9100/service-user/user/restoreSmartcard/"+sessionID,false);	
		abcquery.send(password.value);
		
		if(abcquery.status >= 200 && abcquery.status <= 204){
			alert("restoreSmartcard: Card restored successfully!");
		}else{
			alert("restoreSmartcard : Something went wrong during restoration!");
		}
  },
  
  changePin: function(){	  
	  var abcquery = new XMLHttpRequest();
	  abcquery.open("GET","http://localhost:9100/service-user/user/smartcardStatus",false);
	  abcquery.send();
	  if(abcquery.status<200||abcquery.status>204){
		  alert("Smartcard/Server not available");
		  return false;
	  }
	  
	  var prompts = Components.classes["@mozilla.org/embedcomp/prompt-service;1"]
	  .getService(Components.interfaces.nsIPromptService);

	  var currentPin = {value: ""};              // default the password to pass
	  var newPin = {value: ""};              // default the password to pass
	  var check = {value: false};                   // default the checkbox to true
		
	  var result1 = prompts.promptPassword(null, "Authenticate Password", "Enter current PIN:", currentPin, null, check);
		
	  var result2 = prompts.promptPassword(null, "Authenticate Password", "Enter new PIN:", newPin, null, check);
			  
	  abcquery.open("POST","http://localhost:9100/service-user/user/changePin",false);
	  
	  abcquery.send(currentPin.value+" "+newPin.value);
	  if(abcquery.status == 401){
		  //unauthorized - wrong pin
		  alert("Wrong current pin entered. Try again");
	  }else if(abcquery.status == 402){
		  //forbidden - locked card.
		  alert("Wrong current pin entered - card has been locked! Use the puk-code to unlock");
	  }else if(abcquery.status >= 200 && abcquery.status <= 204){
		  alert("Successfully changed the pin");
	  }else{
		  alert("Unknown return code. Could not change pin. Maybe you forgot to connect your smartcard?");
	  }
  },
  
  unlockCard: function(){
	  var abcquery = new XMLHttpRequest();
	  abcquery.open("GET","http://localhost:9100/service-user/user/smartcardStatus",false);
	  abcquery.send();
	  if(abcquery.status<200||abcquery.status>204){
		  alert("Smartcard/Server not available");
		  return false;
	  }
	  
	  var prompts = Components.classes["@mozilla.org/embedcomp/prompt-service;1"]
	  .getService(Components.interfaces.nsIPromptService);

	  var puk = {value: ""};              // default the password to pass
	  var pin = {value: ""};              // default the password to pass
	  var check = {value: false};                   // default the checkbox to true
		
	  var result1 = prompts.promptPassword(null, "Authenticate Password", "Enter your PUK:", puk, null, check);
		
	  var result2 = prompts.promptPassword(null, "Authenticate Password", "Enter new PIN:", pin, null, check);

	  abcquery.open("POST","http://localhost:9100/service-user/user/unlockCard",false);	
	  abcquery.send(puk.value+" "+pin.value);
	  if(abcquery.status == 401){
		  alert("Wrong puk - try again.");
	  }else if(abcquery.status == 402){
		  alert("Wrong puk 10 times in a row - card is now dead. Take it to an admin in order to restore the card.");
	  }else if(abcquery.status >= 200 && abcquery.status <= 204){
		  alert("Unlocked the card. Pin is now set to your chosen pin");
	  }else{
		  alert("Unknown return code. Could not unlock the card");
	  }
  },

  dispatchResult: function(evt, moveAttributeList) {
	
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
	// alert("ABC4Trust plugin called object: "+connector.element); //in event: "+evt.target.getAttribute("attribute1"));

	var method = evt.target.getAttribute("abc4trust_command");
//	this.origElement = "init time: "+ (new Date().getTime());
//	connector.element=  "init time: "+ (new Date().getTime());
	//alert("we have now initiated the element: "+connector.element);
	
	switch(method){
		case "issue":
			connector.element = evt.target;
			var initialError = issue(evt.target);
			if(initialError!=null && initialError == true) {
				sampleapp.dispatchResult(evt, null);
			}
			break;
		case "verify":
			connector.element = evt.target;
			var initialError = verify(evt.target);
			if(initialError!=null && initialError == true) {
				sampleapp.dispatchResult(evt, null);
			}
			break;
		case "tokenSelected_verify":
			var DEBUG = true;
			// alert("tokenSelected_VERIFY : selectionType 2 : " + evt.target.getAttribute("selectionType"));
			// alert("policy_server : " + evt.target.getAttribute("policy_request"));
			
			evt.target.setAttribute("policy_server",connector.element.getAttribute("policy_server"));
			evt.target.setAttribute("verify_server",connector.element.getAttribute("verify_server"));
			evt.target.setAttribute("verify_credentials",connector.element.getAttribute("verify_credentials"));

			evt.target.setAttribute("policy_request",connector.element.getAttribute("policy_request"));
			evt.target.setAttribute("verify_request",connector.element.getAttribute("verify_request"));
			evt.target.setAttribute("session_id",connector.element.getAttribute("session_id"));
			evt.target.setAttribute("debug_in_extension",connector.element.getAttribute("debug_in_extension"));

			hasChoice_verify(evt.target);

			sampleapp.dispatchResult(evt, null);
			break;
		case "tokenSelected_issue":
			var DEBUG = true;
			// alert("tokenSelected_ISSUE : selectionType 2 : " + evt.target.getAttribute("selectionType"));
			// alert("start_request : " + evt.target.getAttribute("start_request"));
			
			evt.target.setAttribute("start_request",connector.element.getAttribute("start_request"));
			evt.target.setAttribute("step_request",connector.element.getAttribute("step_request"));
			evt.target.setAttribute("status_request",connector.element.getAttribute("status_request"));
			evt.target.setAttribute("session_id",connector.element.getAttribute("session_id"));
			evt.target.setAttribute("debug_in_extension",connector.element.getAttribute("debug_in_extension"));

			hasChoice_issue(evt.target);

			sampleapp.dispatchResult(evt, null);
			break;
			
		case "storeData":
			connector.element = evt.target;
			storeData(evt.target);

			sampleapp.dispatchResult(evt, null);

			break;
		case "loadData":
			connector.element = evt.target;
			loadData(evt.target);

			var moveAttributeList = new Array();
			moveAttributeList[0] = "value";
			sampleapp.dispatchResult(evt, moveAttributeList);

			break;
		default:
			alert("default reached : " + method);	
		
	}


  }
};

window.addEventListener("load", function() { sampleapp.init(); }, false); 
document.addEventListener("ABC4TrustEvent", function(e) { sampleapp.ABC4TrustListener(e); }, false, true); // The last value is a Mozilla-specific value to indicate untrusted content is allowed to trigger the event.  
