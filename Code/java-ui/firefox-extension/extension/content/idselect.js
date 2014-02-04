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

var selectionUIWindow = null;

/* ------------------------------------------------------------------------------------------------
 * @name : openSelectionUI
 * -----------------------------------------------------------------------------------------------*/
function openSelectionUI(sessionID, mode, language)
{
//	alert("openSelectionUINEW : " + sessionID + " " + mode + " " + language);
//	var selectionUIURL = "http://localhost:9093/user-ui?mode=" + mode + "&demo=false&language=" + language + "&sessionid=" + sessionID
	var selectionUIURL = USER_UI_SERVICE + "?mode=" + mode + "&demo=false&language=" + language + "&sessionid=" + sessionID;

	var params = "";
	//Fixed window size 
	var width = 1200; //+ 280 //original : 980 
	if(width > screen.width) {
		width = screen.width;
	}

	var height = 680; //default 
	var left = (screen.width - width)/2;
	var top = (screen.height - height)/2;
	var params = 'width=' + width + ', height=' + height;
	
	params += ', top=' + top + ', left=' + left;
	params += ',resizable= yes';
	params += ',location= no';
	params += ',directories= no';
	// logMessage("IDSelect-Open-" + mode, "PARAMS : " + params);
	
	logMessage("IDSelect-Open-" + mode, "Open ID Selector : " + mode + " : " + selectionUIURL);
	
	selectionUIWindow = window.open(selectionUIURL, 'selection UI', params);

	if(mode=="management") {
		logMessage("IDSelect-Open-" + mode, "Credential Management UI Window Opened...");
	} else {
	    // 'spawn' thread to catch that UI Selector Window Closes...
		logMessage("IDSelect-Open-" + mode, "UI Window Opened - enter loop to wait for close...");
    	if(mode=="presentation") {
	   		setTimeout("checkUISelectorClosed('present');", 0);
    	} else {
	   		setTimeout("checkUISelectorClosed('issue');", 0);
    	}
	}
}

function checkUISelectorClosed(command) {
	try {
		var w = selectionUIWindow; 
	
		var done = w.document.getElementById("idselectDone");
		// logMessage("IDSelect-Check-" + command, "UI Try to detect close! Windows : " + w + " - DONE : " + done )
		if(done) { 
			logMessage("IDSelect-Check-" + command, "Signal from UI - element 'idselectDone' found");

			// true -> try to close window..
			dispatchUIClosed(command);
		} else {
			// sleep 200 millis and try again!
			setTimeout("checkUISelectorClosed('" + command + "');", 200);
		}
	} catch(buggerException) {
		logMessage("IDSelect-Check-" + command, "UI NEW CLOSED FAILED - by Exception : " + buggerException);

		// false -> windows seems to be gonetry to close window..
		dispatchUIClosed(command);
	}
}

function dispatchUIClosed(command) {

	// Windows could be gone - but try to close anyway...
	try {
		var w = selectionUIWindow; 

		logMessage("IDSelect-Close-" + command, "dispatchUIClosed try to close windows for : " + command + " : " + w);
		w.close();
	} catch(deadObjectException) {
		logMessage("IDSelect-Close-" + command, "UI Selector Window Closed - Got Exception - windows could be gone already : " + deadObjectException);
	}

    // new dispatch - trying to hit method continuing after selection...
    var element = document.createElement("ABC4TrustDataElement");
    element.setAttribute("abc4trust_command", "tokenSelected_" + command);
	
    document.documentElement.appendChild(element);

    var evt = document.createEvent("Events");
    evt.initEvent("ABC4TrustEvent", true, false);
    element.dispatchEvent(evt);
	
}

// orig code - where window closed itself...
	
function checkUISelectorClosedOrig(command) {
	var w = selectionUIWindow; 
	var closed = false;
	try {
		if(w == null) {
			closed = true;
		} else {
			closed = w.closed;
		}
//		aConsoleService.logStringMessage("UI Selector Window Closed : " + closed)
		// closed = w && w.closed;
	} catch(deadObjectException) {
		aConsoleService.logStringMessage("UI Selector Window Closed - by Exception : " + deadObjectException)
		closed = true;
	}
	if (closed) {
		aConsoleService.logStringMessage("Window has been closed!")
		dispatchUIClosedOrig(command);
  	} else if (w && !w.closed) {
    	setTimeout("checkUISelectorClosedOrig('" + command + "');", 200);
  	}
}

function dispatchUIClosedOrig(command) {
	var element = document.createElement("ABC4TrustDataElement");
    element.setAttribute("abc4trust_command", "tokenSelected_" + command);
	
    document.documentElement.appendChild(element);

    var evt = document.createEvent("Events");
    evt.initEvent("ABC4TrustEvent", true, false);
    element.dispatchEvent(evt);
}

