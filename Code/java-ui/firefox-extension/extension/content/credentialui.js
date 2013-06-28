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

	selectionUIhtml = "chrome://abc4trust/content/html/CredentialUI.html"; 
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
		embedCredentialInfo("no credential", "No Credentials", 'chrome://abc4trust/content/resources/no_credential.jpg', 0, null, null);

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

function showLanguageSpecificElements(name, preferredLanguage, defaultLanguage) {
	var languageUsed = defaultLanguage;
	var htmlElement = selectionUIWindow.document.getElementById(name + "_" + preferredLanguage);
	if(htmlElement != null) {
		languageUsed = preferredLanguage;
	} else {
		htmlElement = selectionUIWindow.document.getElementById(name + "_" + defaultLanguage);
		if(htmlElement != null) {
			languageUsed = defaultLanguage;
       	}
	}		
	if(htmlElement != null) {
		htmlElement.style.visibility = 'visible';
	}
	else
	{
		alert("showLanguageSpecificElements - not found : " + name + " - preferred : " + preferredLanguage + " - default : " + defaultLanguage) ;
	}
	
	removeLanguageSpecificElements(name, languageUsed);
}

function removeLanguageSpecificElements(name, preferredLanguage) {
	var languageUsed = null;
	if(preferredLanguage!=null) {
		var defaultLanguage = "en";
		var htmlElement = selectionUIWindow.document.getElementById(name + "_" + preferredLanguage);
		if(htmlElement != null) {
			languageUsed = preferredLanguage;
			htmlElement.style.visibility = 'visible';
		} else {
			htmlElement = selectionUIWindow.document.getElementById(name + "_" + defaultLanguage);
			if(htmlElement == null) {
				alert("Problems with UI Selection HTML - language specific element not defined for : " + preferredLanguage + " - and does not have default language : " + defaultLanguage + " - element name : " + name)
				return;
	       	} else {
				languageUsed = defaultLanguage;
				htmlElement.style.visibility = 'visible';
			}
		}		
	}
	var languagesInHtml = ["en", "sv", "el", "da", "de", "fr"];
	for(var i=0; i<languagesInHtml.length; i++) {
		if(languagesInHtml[i] != languageUsed) {
			htmlElement = selectionUIWindow.document.getElementById(name + "_" + languagesInHtml[i]);
			if(htmlElement != null) {
				htmlElement.parentNode.removeChild(htmlElement);
			}
		}
	}
}

function parseAndEmbedCredential(credential, ix) {
    
    // alert("parseAndEmbedCredential " + credential.CredentialUID);

	//information for HTML embedding
	var CredentialUID = credential.CredentialUID;
	// alert("Embed Credentila In HTML : " + CredentialUID);
	var CredentialFriendlyName = '';//null; 
	var CredentialImageReference = null; 
	var CredentialAttributeInfo = '';//null; 
	var attributeCount = 0;
	var attributeLabel = new Array();
	var attributeValue = new Array();
	var credentialIsRevoked = false;
	
	//FriendlyCredentialName
	if(credential.hasOwnProperty('FriendlyCredentialName'))
	{
		
		CredentialFriendlyName = getFriendlyByLanguage(credential.FriendlyCredentialName);
	}		
	else // 'FriendlyCredentialName' is not existing	
	{
//		alert("FriendlyCredentialName is not existing");
		CredentialFriendlyName = 'Credential'+ix;
	}	
	
	//ImageReference
	if(credential.hasOwnProperty('ImageReference'))
	{
		CredentialImageReference = credential.ImageReference;
		//TODO : Temporary Test
		//CredentialImageReference = "file:///C:/Users/soh/ABC4Trust_SourceCode/java/ri/trunk/firefoxplugin/abc4trust/extension/content/testJSON/master.jpg"
	//	alert("CredentialImageReference : " + CredentialImageReference);
	}		
	else // 'FriendlyCredentialName' is not existing	
	{
		//alert("CredentialImageReference is not existing");
		CredentialImageReference = 'default image';
	}
	
	//Attribute 
	if(credential.hasOwnProperty('Attribute'))
	{
		var attributeArray = credential.Attribute;
		if(attributeArray.length > 1) //there are more than 2 Attribute
		{
			attributeCount = attributeArray.length;
			for(var j = 0; j < attributeArray.length; j++)
			{
				var friendlyAttributeName = ''; 
				var friendlyAttributeValue = '';
				
				if(attributeArray[j].hasOwnProperty('AttributeDescription'))
				{
					var attributeDesc = attributeArray[j].AttributeDescription;
					if(attributeDesc["@Type"] =="urn:abc4trust:gui:isRevoked") {
						credentialIsRevoked = attributeArray[j].AttributeValue.$;
						attributeCount = attributeCount-1;
						continue;
					}
					friendlyAttributeName = getAttributeFriendlyAttributeName(attributeDesc, j);
				}
				else
				{
					alert("there is no AttributeDescription");
				}
				
				if(attributeArray[j].hasOwnProperty('AttributeValue'))
				{
					friendlyAttributeValue = attributeArray[j].AttributeValue.$;
				//	alert(friendlyAttributeValue);
				}
				else
				{
					alert("there is no AttributeValue");
				}	
				attributeLabel[j] = friendlyAttributeName;
				attributeValue[j] = friendlyAttributeValue;
				
				CredentialAttributeInfo = CredentialAttributeInfo + friendlyAttributeName + ' : ' + friendlyAttributeValue + '    ';
				CredentialAttributeInfo = CredentialAttributeInfo + '</br>\n';
				//alert(CredentialAttributeInfo);
				
			}//for - attribute array	
		}
		else if(attributeArray.length == null && attributeArray != null) //only one Attribute
		{
			attributeCount = 1;
			
			var friendlyAttributeName = ''; 
			var friendlyAttributeValue = '';
			
			if(attributeArray.hasOwnProperty('AttributeDescription'))
			{
				var attributeDesc = attributeArray.AttributeDescription;
//				var attributeDesc = attributeArray[j].AttributeDescription;
				if(attributeDesc["@Type"] =="urn:abc4trust:gui:isRevoked") {
					credentialIsRevoked = attributeArray.AttributeValue.$;
					CredentialAttributeInfo = CredentialAttributeInfo + friendlyAttributeName + ' : ' + friendlyAttributeValue + '     ';
					CredentialAttributeInfo = CredentialAttributeInfo + '\n';
					embedCredentialInfo(CredentialUID, CredentialFriendlyName, CredentialImageReference, CredentialAttributeInfo, attributeCount-1, attributeLabel, attributeValue, credentialIsRevoked);
					return;
				}

				friendlyAttributeName = getAttributeFriendlyAttributeName(attributeDesc, 0);
			}
			else
			{
				alert("there is no AttributeDescription");
			}
			
			if(attributeArray.hasOwnProperty('AttributeValue'))
			{
				friendlyAttributeValue = attributeArray.AttributeValue.$;
				//alert(friendlyAttributeValue);
			}
			else
			{
				alert("there is no AttributeValue");
			}
			
			attributeLabel[0] = friendlyAttributeName;
			attributeValue[0] = friendlyAttributeValue;
			
			CredentialAttributeInfo = CredentialAttributeInfo + friendlyAttributeName + ' : ' + friendlyAttributeValue + '     ';
			CredentialAttributeInfo = CredentialAttributeInfo + '\n';
			//alert(CredentialAttributeInfo);
		}	
	}	
	
	//print out parsed credential information 
	// alert("embedCredentialInfo - uid : " + CredentialUID + '\n - Friendly Name : ' + CredentialFriendlyName + '\n - Image Reference : ' + CredentialImageReference + '\n -AttributeInfo : ' + CredentialAttributeInfo); 

	embedCredentialInfo(CredentialUID, CredentialFriendlyName, CredentialImageReference, CredentialAttributeInfo, attributeCount, attributeLabel, attributeValue, credentialIsRevoked);
}//for - array of credentials


function getAttributeFriendlyAttributeName(attributeDesc, ix) 
{
   var friendlyAttributeName;

	if(attributeDesc.hasOwnProperty('FriendlyAttributeName'))
	{
		friendlyAttributeName = getFriendlyByLanguage(attributeDesc.FriendlyAttributeName);
	}
	else
	{
		friendlyAttributeName = 'Attribute'+ix;
	}
//	alert("getAttributeFriendlyAttributeName : " +friendlyAttributeName);
	return friendlyAttributeName;
	
}

/* ---------------------------------------------------------------------------------------------------------------------
 * @name : embedCredentialInfo(CredentialUID, CredentialFriendlyName, CredentialImageReference, CredentialAttributeInfo)
 * @description : embed credential information into HTML 
 * -------------------------------------------------------------------------------------------------------------------*/
function embedCredentialInfo(CredentialUID, CredentialFriendlyName, CredentialImageReference, CredentialAttributeInfo, attributeCount, attributeLabel, attributeValue, credentialIsRevoked)
{
	//alert("Inside embedCredentialInfo() ....");
	
	var credentialPaneNode = selectionUIWindow.document.getElementById('credentialPane');
	// alert("TEST : Embedding Credential Information >> " + CredentialUID);
	var credentialPane = selectionUIWindow.document.createElement('input'); 
	credentialPane.setAttribute('id', CredentialUID);
	credentialPane.setAttribute('type', 'hidden');
	credentialPane.setAttribute('name', CredentialFriendlyName);
	credentialPane.setAttribute('isRevoked', credentialIsRevoked);
	//TODO : image where to add??
	credentialPane.setAttribute('src', CredentialImageReference);
	credentialPane.setAttribute('value', CredentialAttributeInfo);
	credentialPane.setAttribute('title', CredentialAttributeInfo);
	credentialPane.setAttribute('attributeCount', attributeCount);
	var attributeDescription;
	if(attributeCount==0) {
		attributeDescription = "No attributes."
	} else {
		attributeDescription = "";
	}
	for(var i=0; i<attributeCount; i++) {
		var attributePane = selectionUIWindow.document.createElement('input'); 
		attributePane.setAttribute('id', CredentialUID + "_" + i);
		attributePane.setAttribute('type', 'hidden');
		attributePane.setAttribute('label', attributeLabel[i]);
		attributePane.setAttribute('value', attributeValue[i]);

		credentialPaneNode.appendChild(attributePane);
		
		attributeDescription = attributeDescription + attributeLabel[i] + " : " + attributeValue[i] + "\n<br>"
	}
	credentialPane.setAttribute('attributeDescription', attributeDescription);
	credentialPaneNode.appendChild(credentialPane);
	
}

function getFriendlyByLanguage(friendlyList)
{
	if(friendlyList.length == null) {
		// no list - only one!
//		alert("friendlyList.length == null - only one ?? " + friendlyList);
		return friendlyList.$
	}
	
	var preferedFriendly;
	var defaultFriendly;
	var defaultLanguage = "en";
	for(var ix=0; ix<friendlyList.length; ix++)
	{
		if(friendlyList[ix]["@lang"] == preferedLanguage) {
			preferedFriendly = friendlyList[ix].$
			break;
		} 
		if(friendlyList[ix]["@lang"] == defaultLanguage) {
			defaultFriendly = friendlyList[ix].$
		} 
	}
	if(preferedFriendly!=null) {
		return preferedFriendly;
	}
	if(defaultFriendly!=null) {
		return defaultFriendly;
	}
	// return 1st element in list
	return friendlyList[0].$
}

