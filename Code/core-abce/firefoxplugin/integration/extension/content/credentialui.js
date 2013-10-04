/*
    NOTE: Some method here depend on 'idselect.js'
*/

/* -----------------------------------------------------------------------------------------------
 * @name :  populateCredentialUI(inputJSON, lang)
 * @parameter : inputJson : 'CredentialDescriptions' as JSon, lang - try to use this language.
 * @description : show credential list. 
 * ----------------------------------------------------------------------------------------------*/
function populateCredentialUI(inputJSON, lang)
{
	//alert("CREDENTIAL UI - JS populateCredentialUI : " + lang + " : " + inputJSON);
	idSelectJSON = inputJSON; 
	//alert("TEST : " + idSelectJSON);
	
	preferedLanguage = lang;

	var params = "";
	//Fixed window size 
	var width = 560; //+ 280 //original : 980 
	var height = 680; //default 
	var left = (screen.width - width)/2;
	var top = (screen.height - height)/2;
	var params = 'width=' + width + ', height=' + height;
	
	params += ', top=' + top + ', left=' + left;
	params += ',resizable= yes';
	params += ',location= no';
	params += ',directories= no';

	selectionUIhtml = "chrome://sampleapp/content/html/CredentialUI.html"; 
	selectionUIWindow = window.open(selectionUIhtml, 'credential UI', params);

	// method waits until windows is loaded and continues below in continue_openSelectionUI
	checkCredentialHtmlLoaded();

}

// check if contentCenterElement exits in html - then create GUI... otherwise sleep and try again...
function checkCredentialHtmlLoaded() {
	var w = selectionUIWindow; 
	
	if (w && !w.closed && w.document && w.document.getElementById('credentialPane') !=null) {
		continue_openCredentialUI();
  	} else if (w && !w.closed) {
    	setTimeout("checkCredentialHtmlLoaded();", 200);
  	}
}

function continue_openCredentialUI()
{
	removeLanguageSpecificElements("credentialList", preferedLanguage);
	
//	parseAndEmbedCredentialMap(); 

	var credentialPane = selectionUIWindow.document.getElementById('credentialPane');
	var credentialListElement = selectionUIWindow.document.createElement('input'); 
	credentialListElement.setAttribute('id', "credentialListPane");
	credentialListElement.setAttribute('type', 'hidden');

	if(idSelectJSON == null) {
		//
		// alert("?? NO Credentials ?? " + credentials);

		// always embed 'no credentail' - in case a CandidatePresentationToken only relies on pseudonyms
		embedCredentialInfo("no credential", "No Credentials", 'chrome://sampleapp/content/resources/no_credential.jpg', 0, null, null);

		credentialListElement.setAttribute('credentialCount', 1);
		credentialListElement.setAttribute('credentialUID_0', "no credential");
	} else {
		var credentials = idSelectJSON.entry;
		if(credentials.length == null) {
			// not a list!
			var singleCredential = idSelectJSON.entry.value;
			// alert("Single Credential " + credentials + " : " + singleCredential);
			parseAndEmbedCredential(singleCredential, 0);

			credentialListElement.setAttribute('credentialCount', 1);
			credentialListElement.setAttribute('credentialUID_0', singleCredential.CredentialUID);
		}
		else 
		{
			credentialListElement.setAttribute('credentialCount', credentials.length);
//	    	alert(" - more than one credential " + credentials.length + " : " + credentials);
			for(var i = 0; i < credentials.length; i++)
			{
				var currentCredential = credentials[i].value;
//		   	alert(" - parse credential ix " + i + " : " + currentCredential + " : " + currentCredential.CredentialUID);
				parseAndEmbedCredential(currentCredential, i);
				credentialListElement.setAttribute('credentialUID_' + i, currentCredential.CredentialUID);
			}
		}
	}
	
	// add credentialListElement to pane
	credentialPane.appendChild(credentialListElement);
	
	// make HTML display list...
	var showButton = selectionUIWindow.document.getElementById('showButton');
	showButton.click();
}


