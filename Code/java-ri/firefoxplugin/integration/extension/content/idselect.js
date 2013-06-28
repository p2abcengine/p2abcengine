/* ------------------------------------------------------------------------------------------
 * Revision Date : May 8. 2012
 * Due to the UI requirement has been fixed by the meeting on 7th May 
 * ----------------------------------------------------------------------------------------*/
//TODO move/remove the get_test_json methods


/* ------------------------------------------------------------------------------------------
 * Global Variables 
 * ----------------------------------------------------------------------------------------*/
var numberOfTokens = 0;
var numberOfCredentials = 0;
var numberOfPolicies = 0;
var selectionUIWindow = null;
var idSelectJSON; //read JSON input from plugin 
var selectionUIhtml;
var sizeOfCandidatePresentationTokenList;
var idSelectionUIType = null;
var idSelectionUIType_NotUsed = null;
var preferedLanguage = null;

/* ------------------------------------------------------------------------------------------
 * @name : about_idselect() 
 * @description : inform the function from idselect.js is called 
 * -----------------------------------------------------------------------------------------*/
function about_idselect()
{
	alert("[DEBUG INFO] idselect.js loaded");
	var ret = "idSelected";
	
	return ret; 
}




/* ------------------------------------------------------------------------------------------
 * @name : setParam() 
 * @description : Set parameters of Selection UI window frame (popup window)
 * -----------------------------------------------------------------------------------------*/
function setParam()
{
	var default_height = 680;
	var numPolicy = countNumPolicy(idSelectJSON); 
	numberOfPolicies = numPolicy;
	//alert("numberOfPolicies : " + numberOfPolicies);
	
	//Fixed window size 
	var width = 1160; //+ 280 //original : 980 
	var height = 680; //default 
	
	//Add height depends on the number of Presentation Policy
	if(numPolicy > 0)
	{
		height = height + (numPolicy * 40); 
		//alert("height : " + height);
	}
	else if(numPolicy == 0)
	{
		height = default_height;
		//alert("height : " + height);
	}	
	
	//TODO : relative screen size depends on the screen, but at the moment we use fixed size window
	/*
	var width = (screen.width)/2 + (screen.width)/7; 
	var height = (screen.height)/2 + (screen.height)/7; 
	var heightAdd = screen.height/7; //to give some margin in the bottom

	height = height + heightAdd;
	*/
	
	var left = (screen.width - width)/2;
	var top = (screen.height - height)/2;
	var params = 'width=' + width + ', height=' + height;
	
	params += ', top=' + top + ', left=' + left;
	params += ',resizable= yes';
	params += ',location= no';
	params += ',directories= no';
	
	return params;
}



/* -----------------------------------------------------------------------------------------------
 * @name : countNumPolicy(idSelectJSON)
 * @parameter : idSelectJSON is input JSON object
 * @description : count the number of presentation policy 
 * ----------------------------------------------------------------------------------------------*/
function countNumPolicy(idSelectJSON)
{
	var numPolicy = 0; //default
	var PolicyDescriptions = idSelectJSON.PolicyDescriptions;
	//alert("PolicyDescriptions : " + PolicyDescriptions );
	
	var PolicyDescriptions_entry = PolicyDescriptions.entry;
	//alert("PolicyDescriptions Entry : " + PolicyDescriptions_entry);
	
	var numEntry = PolicyDescriptions_entry.length; 
	//alert("numEntry : " + numEntry);
	
	if(numEntry > 0)
	{
		numPolicy = numEntry;
	}
	else if(numEntry == null)
	{
		numPolicy = 1; 
	}	

	return numPolicy; 
}


/* -----------------------------------------------------------------------------------------------
 * @name : populateSelectionUI(idSelectJSON)
 * @parameter : idSelectJSON is input JSON object passed from FireFox plugin which interface ABCE
 * @description : The entry point of populating UI, showing Selection UI 
 * ----------------------------------------------------------------------------------------------*/
function populateSelectionUI(inputJSON, uiType, lang)
{
	//alert("Integration Test");
	idSelectJSON = inputJSON; 
	//alert("TEST : " + idSelectJSON);
	
	idSelectionUIType = uiType;
	if("verify" == idSelectionUIType) {
		idSelectionUIType_NotUsed = "issue";
	} else {
		idSelectionUIType_NotUsed = "verify";
	}
	
	preferedLanguage = lang;

	//0. initialize the global variables in case user open Selection UI window several time
	initializeGlobalVariables(inputJSON); 
	
	//1. Getting test JSON, this code moved to FireFox Plugin
	//testJSONinput = getTestJSON10();
	
	//2. Show Selection UI
	openSelectionUI(idSelectJSON);
	
	//3. Make all global variables initialize for next use
	invalidateVariables(); 
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

/* ------------------------------------------------------------------------------------------------
 * @name : openSelectionUI
 * @description : open predefine UI framework
 * -----------------------------------------------------------------------------------------------*/
function openSelectionUI(idSelectJSON)
{
	save_idSelectJSON = idSelectJSON;

	var params = setParam(); 
	
	/*
	 * Set up Selection UI frame
	 */ 
	//selectionUIhtml = "file:///C:/Users/soh/ABC4Trust_SourceCode/java/ri/trunk/firefoxplugin/abc4trust/testhtml/SelectionUI.html";
	selectionUIhtml = "chrome://sampleapp/content/html/SelectionUI.html"; 
	selectionUIWindow = window.open(selectionUIhtml, 'selection UI', params);

	// method waits until windows is loaded and continues below in continue_openSelectionUI
	checkHtmlLoaded();
}

var save_idSelectJSON;
// check if contentCenterElement exits in html - then create GUI... otherwise sleep and try again...
function checkHtmlLoaded() {
	var w = selectionUIWindow; 
	
	if (w && !w.closed && w.document && w.document.getElementById('InspectorInfo') !=null) {
		continue_openSelectionUI();
  	} else if (w && !w.closed) {
    	setTimeout("checkHtmlLoaded();", 200);
  	}
}

function continue_openSelectionUI()
{
	//alert("DO WE GET HERE??");
	idSelectJSON = save_idSelectJSON;
	
	
	if( selectionUIWindow == null || selectionUIWindow.closed )
	{
		alert("[Error] Selection UI doesn't exist!");
	}
	else 
	{
		//TODO : Handle multiple language when policy's friendly description has
		// remove all HTML elements not used
		removeLanguageSpecificElements("selectionTypeHeader_" + idSelectionUIType, preferedLanguage);
		removeLanguageSpecificElements("selectionTypeHeader_" + idSelectionUIType_NotUsed, null);
		removeLanguageSpecificElements("policyHeader_" + idSelectionUIType, preferedLanguage);
		removeLanguageSpecificElements("policyHeader_" + idSelectionUIType_NotUsed, null);
		removeLanguageSpecificElements("messageSelectInstructions", preferedLanguage);
		// add more here...
		
        var globalVariables = selectionUIWindow.document.getElementById('globalVariables');

		var selectionType = selectionUIWindow.document.createElement('input');
		selectionType.setAttribute('type', 'hidden');
		selectionType.setAttribute('id', 'selectionType');
		selectionType.setAttribute('value', idSelectionUIType);
		
		globalVariables.appendChild(selectionType);

		/*
		 * Parse Presentation Policy
		 */
		var numPolicy = idSelectJSON.PolicyDescriptions.entry.length;
		
		if(idSelectJSON.PolicyDescriptions.entry.length == null)
		{
			numPolicy = 1; 
			numberOfPolicies = 1; 
		}	
		
		if (numPolicy == 1)
		{
			parsePolicyDescription(idSelectJSON.PolicyDescriptions.entry.value, 0);
		}	
		else if(numPolicy > 1)
		{
			for(var policyIndex = 0; policyIndex < numPolicy; policyIndex++)
			{
				parsePolicyDescription(idSelectJSON.PolicyDescriptions.entry[policyIndex].value, policyIndex);
			}
		}
		
		/*
		 * Parse Presentation Tokens which follows the policy and Embed information 
		 */
		// Parsing Presentation Token
		sizeOfCandidateTokenList = getSizeCandidateTokenList(idSelectJSON);
		// alert("sizeOfCandidateTokenList : " + sizeOfCandidateTokenList);
		if( sizeOfCandidateTokenList == 0) //in case no exist ??
		{
			alert("sizeOfCandidateTokenList == 0 ? why");
		}
		else if( sizeOfCandidateTokenList > 1) //in case when more than on presentation token exists 
		{
			for(var ix=0; ix<sizeOfCandidateTokenList; ix++) {
				var currentCandidateToken;
				if("verify" == idSelectionUIType) 
				{
					currentCandidateToken = idSelectJSON.CandidatePresentationTokenList.CandidatePresentationToken[ix];
				} 
				else
				{
					currentCandidateToken = idSelectJSON.CandidateIssuanceTokenList.CandidateIssuanceToken[ix];
				}
				// alert("currentCandidateToken [ix] : " + currentCandidateToken + " + ix " + ix  + " - ui type " + idSelectionUIType );
				
	            parseCandidatePresentationToken(selectionUIWindow, currentCandidateToken, ix, sizeOfCandidateTokenList);
				parsePseudonymChoiceForToken(currentCandidateToken);
				parseInspectorChoiceForToken(currentCandidateToken);
			}
		}
		else //only one presentation token exists
		{
			var currentCandidateToken;
			if("verify" == idSelectionUIType) 
			{
				currentCandidateToken = idSelectJSON.CandidatePresentationTokenList.CandidatePresentationToken;
			} 
			else
			{
				currentCandidateToken = idSelectJSON.CandidateIssuanceTokenList.CandidateIssuanceToken;
			}
			// alert("currentCandidateToken ONE : " + currentCandidateToken + " - ui type " + idSelectionUIType );
            parseCandidatePresentationToken(selectionUIWindow, currentCandidateToken, 0, 1);
			parsePseudonymChoiceForToken(currentCandidateToken);
			parseInspectorChoiceForToken(currentCandidateToken);
		}	
	
		// alert("parsing presentation token is done " + currentCandidateToken);
		
		
		/*
		 * Add Credential Information inside HTML
		 */
		parseAndEmbedCredentialMap(); 
		
		//Parsing credentials are done
		//alert('Parsing credentials are done');

	}
	
}


function parsePolicyDescription(policyDescriptions, ix) {
	
	var policyUID; 
	var policyFriendlyName;
	var policyFriendlyDescription;
	policyUID = policyDescriptions.PolicyUID;

	if(policyDescriptions.Message.FriendlyPolicyName == null) {
		policyFriendlyName = policyUID;
	} else {
		policyFriendlyName = getFriendlyByLanguage(policyDescriptions.Message.FriendlyPolicyName);
	}

	if(policyDescriptions.Message.FriendlyPolicyDescription == null) {
		policyFriendlyDescription = policyUID;
	} else {
		policyFriendlyDescription = getFriendlyByLanguage(policyDescriptions.Message.FriendlyPolicyDescription);
	}
	//
	addPresentationPolicyRadioButton(ix, policyUID, policyFriendlyName, policyFriendlyDescription, numberOfPolicies);
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


/* ------------------------------------------------------------------------------------------
 * @name : parseInspectorChoiceForToken(CandidatePresentationToken) 
 * @description : parse InspectorChoiceList in parseInspectorChoiceForToken 
 * ----------------------------------------------------------------------------------------*/
function parseInspectorChoiceForToken(presentationToken)
{
	var policyUID = presentationToken.Token["@PolicyUID"];//.@PolicyUID;
	var tokenUID = presentationToken.Token["@TokenUID"];//.@TokenUID;
	var InspectorChoiceList = presentationToken.InspectorChoiceList;
		
	if(InspectorChoiceList == null || InspectorChoiceList.URISet == null)  {
		// no inspectors...
		//alert("InspectorChoiceList is null, there is nothing to parse");

		//embed this information 
		var InspectorInfoNode = selectionUIWindow.document.getElementById('InspectorInfo');

		var tokenToInspectorList = selectionUIWindow.document.createElement('input');
		tokenToInspectorList.setAttribute('type', 'hidden');
		tokenToInspectorList.setAttribute('id', 'inspector_for_'+ tokenUID ); //policyUID);
		tokenToInspectorList.setAttribute('value', 'There is no inspector information');

		InspectorInfoNode.appendChild(tokenToInspectorList);
		//alert("TEST HERE : inspector_for_+policyUID " + 'inspector_for_'+policyUID); 
			
	} else if(InspectorChoiceList.URISet.length == null) {
		// one inspector
		// alert("TODO: parseInspectorChoiceForToken : InspectorChoiceList URISet.length is null there is One Inspector");
		var ListOfURISet = new Array()
		ListOfURISet[0] = InspectorChoiceList.URISet;
		parseAndEmbedInspectorInfoPerToken(tokenUID, ListOfURISet);
	} else {
		// alert("TODO: parseInspectorChoiceForToken : InspectorChoiceList URISet has # of Inspectors : " + InspectorChoiceList.URISet.length);
		parseAndEmbedInspectorInfoPerToken(tokenUID, InspectorChoiceList.URISet);
	}
}

function parseAndEmbedInspectorInfoPerToken(tokenUID, URISet)
{
	//Instantiate array
	var arraySet = new Array(); 
	var arrayCountSet = new Array();
		
    //alert("parseAndEmbedInspectorInfoPerToken : " + tokenUID + " : " + URISet + " : " + URISet.length);		
	for(var j = 0; j < URISet.length; j++) //URISet level iteration
	{	
		var URIs = URISet[j].URI; 
	    // alert("parseAndEmbedInspectorInfoPerToken : URIs " + URIs + " : URIs .length " + URIs.length);
		arrayURI = new Array();
				
		// alert("URIs[0].length : " + URIs[0].length + " : " + URIs[0]);
		if(URIs[0].length == 1) {
			// alert("- just one pseudonym" + URIs);
		    
			// we have a URI of chars...
			//alert("URIs.length == null && URIs != null");
					
			arrayURI[0] = URIs;
 			//alert("testing URI : " + arrayURI[j] );
					
			arraySet[j] = arrayURI;
			//alert("j is " + j + " arraySet[j] : " + arraySet[j]);
		    arrayCountSet[j] = 1;	
		} 
		else
		{
			alert("- list of pseudonym" + URIs.length);
			//alert("the set has more than 1 URI");
			for(var k = 0; k < URIs.length; k++) //URI level iteration 
			{
				arrayURI[k] = URIs[k];
				//alert("testing URI : " + arrayURI[k] );
			}
					
			arraySet[j] = arrayURI;
			//alert("j is " + j + " arraySet[j] : " + arraySet[j]);
		    arrayCountSet[j] = URIs.length;	
					
		}
	}
			
	//embed parsed info
	embedInspectorInfoPerToken(tokenUID, arraySet, arrayCountSet);
}



/* ------------------------------------------------------------------------------------------
 * @name : embedInspectorInfoPerToken(tokenUID, arraySet)
 * @description : embedding Inspector information per Token
 * ----------------------------------------------------------------------------------------*/
function embedInspectorInfoPerToken(tokenUID, arraySet, arrayCountSet)
{
	//placeholder for parsed information 
	var InspectorInfoNode = selectionUIWindow.document.getElementById('InspectorInfo');

	//policyToInspectorList 
	var tokenToInspectorList = selectionUIWindow.document.createElement('input');
	tokenToInspectorList.setAttribute('type', 'hidden');
	tokenToInspectorList.setAttribute('id', 'inspector_for_'+tokenUID);

	//alert("XXX : embedInspectorInfoPerToken " + tokenUID + " : " + arraySet);	
	for(var i = 0; i < arraySet != null && i< arraySet.length; i++)
	{
		
		var URISetList = selectionUIWindow.document.createElement('ul');
		URISetList.setAttribute('type', 'hidden');
		URISetList.setAttribute('id', 'URISet_'+i);
	
		//alert("arraySet[i].length " + arraySet[i].length);
		var countForArraySet = arrayCountSet[i];
		//alert("before : III loop : : " + i + " < " + arraySet.length + " - " + countForArraySet);
		
		for(var j = 0; j < countForArraySet ; j++)
		{
			//alert("in loop : : " + i + " < " + arraySet.length + " - and " + j + " < " + countForArraySet);
			if( arraySet[i][j] == null )
			{
				alert("value is null " + i + " : " + j);
			}
			else
			{	
				var URIListItem = selectionUIWindow.document.createElement('li');
				URIListItem.setAttribute('type', 'hidden');
				URIListItem.setAttribute('id', 'URIListItem_'+j);
				
				var inspectorId = arraySet[i][j];
				// alert("get PS Description for : inspectorId : " + inspectorId);
				
				URIListItem.setAttribute('value', arraySet[i][j]);
				//alert("arraySet[i][j] : " + arraySet[i][j]);
				var frindlyDesc = getFriendlyDescInspector(arraySet[i][j]);
				// alert(" - got frindly " + frindlyDesc);
				
				URIListItem.setAttribute('friendlyDesc', frindlyDesc); 
				
				URISetList.appendChild(URIListItem);
			}	
		}
		
		tokenToInspectorList.appendChild(URISetList);
		
	}//end of for loop

	// alert("XXX : embedInspectorInfoPerToken DONE!");	

	InspectorInfoNode.appendChild(tokenToInspectorList);
	
}


/* ---------------------------------------------------------------------------------------------
 * @name : getFriendlyDescInspector(inspectorUID) 
 * @description : get friendly description of Inspector and put into Inspector element's value
 * --------------------------------------------------------------------------------------------*/
function getFriendlyDescInspector(inspectorUID)
{
	//alert("getting friendly desc of inspector...");
	
	// NOTE when rewriting wrt 'language' try refactor getting description - same code 3 times it seems
	// check 'psedonym'...
	
	var friendlyDesc; 
	
	var inspectorDescs = idSelectJSON.InspectorDescriptions;
	if(inspectorDescs != null)
	{
		//alert("idSelectJSON.InspectorDescriptions exists");
		
		if(inspectorDescs.entry != null)
		{
			//alert("length of inspectorDescs.entry : " + inspectorDescs.entry.length);
			if(inspectorDescs.entry.length > 1) //more than 2 inspector
			{
				//alert("there is " + inspectorDescs.entry.length + " in JSON");
				
				for(var i = 0; i < inspectorDescs.entry.length; i++)
				{
					if(inspectorDescs.entry[i].key == inspectorUID)
					{
						
						friendlyDesc = getFriendlyByLanguage(inspectorDescs.entry[i].value.FriendlyInspectorDescription);
					}
					else if(inspectorDescs.entry[i].key.$ == inspectorUID)
					{
						friendlyDesc = getFriendlyByLanguage(inspectorDescs.entry[i].value.FriendlyInspectorDescription);
					}
				}	
			}
			else if(inspectorDescs.entry.length == 1) //there is only one pseudonym
			{
				if(inspectorDescs.entry.key == inspectorUID)
				{
					friendlyDesc = getFriendlyByLanguage(inspectorDescs.entry.value.FriendlyInspectorDescription);
				}
				else if(inspectorDescs.entry.key.$ == inspectorUID)
				{
					friendlyDesc = getFriendlyByLanguage(inspectorDescs.entry.value.FriendlyInspectorDescription);
				}
			}
			else if(inspectorDescs.entry.length == null) //there is only one pseudonym
			{
				if(inspectorDescs.entry.key == inspectorUID)
				{
					friendlyDesc = getFriendlyByLanguage(inspectorDescs.entry.value.FriendlyInspectorDescription);
				}
				else if(inspectorDescs.entry.key.$ == inspectorUID)
				{
					friendlyDesc = getFriendlyByLanguage(inspectorDescs.entry.value.FriendlyInspectorDescription);
				}
			}
		}	
	}
	else
	{
//		alert("idSelectJSON.InspectorDescriptions is null");
		friendlyDesc = "No Description of Inspector : " + inspectorUID;
	}	
// 	alert("getFriendlyDescInspector " + friendlyDesc);
	return friendlyDesc;
}





/* ------------------------------------------------------------------------------------------
 * @name : parsePseudonymChoiceForToken(PresentationToken) 
 * @description : parse PseudonymChoiceList for a token 
 * ----------------------------------------------------------------------------------------*/
function parsePseudonymChoiceForToken(presentationToken)
{
	// TODO: METHOD  parseInspectorChoiceList_OLD Should contain info about how to parse this...
	
	// var policyUID = token.Token["@PolicyUID"];//.@PolicyUID;
	var tokenUID = presentationToken.Token["@TokenUID"];//.@TokenUID;
	var PseudonymChoiceList = presentationToken.PseudonymChoiceList;

	if(PseudonymChoiceList == null || PseudonymChoiceList.URISet == null)  {
		// no pseudonyms...
		// alert("PseudonymChoiceList is null, there is nothing to parse");

		//embed this information 
		var PseudonymInfoNode = selectionUIWindow.document.getElementById('PseudonymInfo');

		var tokenToPseudonymList = selectionUIWindow.document.createElement('input');
		tokenToPseudonymList.setAttribute('type', 'hidden');
		tokenToPseudonymList.setAttribute('id', 'pseudonym_for_'+ tokenUID ); //policyUID);
		tokenToPseudonymList.setAttribute('value', 'There is no pseudonym information');

		PseudonymInfoNode.appendChild(tokenToPseudonymList);
		//alert("TEST HERE : inspector_for_+policyUID " + 'inspector_for_'+policyUID); 
			
	} else if(PseudonymChoiceList.URISet.length == null) {
		// one pseudonym
		// alert("parsePseudonymChoiceForToken : PseudonymChoiceList URISet.length is null there is One Pseudonym");
		var ListOfURISet = new Array()
		ListOfURISet[0] = PseudonymChoiceList.URISet;
		parseAndEmbedPseudonymChoiceList(tokenUID, ListOfURISet);
	} else {
		// alert("TODO: parsePseudonymChoiceForToken : PseudonymChoiceList URISet has # of Pseudonyms SETS : " + PseudonymChoiceList.URISet.length);
		parseAndEmbedPseudonymChoiceList(tokenUID, PseudonymChoiceList.URISet);
	}
}

function parseAndEmbedPseudonymChoiceList(tokenUID, URISet)
{
	//Instantiate array
	var arraySet = new Array(); 
	var arrayCountSet = new Array();
		
    //alert("parseAndEmbedPseudonymChoiceList : " + tokenUID + " : " + URISet + " : " + URISet.length);		
	for(var j = 0; j < URISet.length; j++) //URISet level iteration
	{	
		var URIs = URISet[j].URI; 
	    // alert("parseAndEmbedPseudonymChoiceList : URIs " + URIs + " : URIs .length " + URIs.length);
		arrayURI = new Array();
				
		// alert("URIs[0].length : " + URIs[0].length + " : " + URIs[0]);
		if(URIs[0].length == 1) {
			// alert("- just one pseudonym" + URIs);
		    
			// we have a URI of chars...
			//alert("URIs.length == null && URIs != null");
					
			arrayURI[0] = URIs;
 			//alert("testing URI : " + arrayURI[j] );
					
			arraySet[j] = arrayURI;
			//alert("j is " + j + " arraySet[j] : " + arraySet[j]);
		    arrayCountSet[j] = 1;	
		} 
		else
		{
			alert("- list of pseudonym" + URIs.length);
			//alert("the set has more than 1 URI");
			for(var k = 0; k < URIs.length; k++) //URI level iteration 
			{
				arrayURI[k] = URIs[k];
				//alert("testing URI : " + arrayURI[k] );
			}
					
			arraySet[j] = arrayURI;
			//alert("j is " + j + " arraySet[j] : " + arraySet[j]);
		    arrayCountSet[j] = URIs.length;	
					
		}
	}
			
	//embed parsed info
	embedPseudonymInfoPerToken(tokenUID, arraySet, arrayCountSet);
}


/* ------------------------------------------------------------------------------------------
 * @name : embedPseudonymInfoPerToken(tokenUID, arraySet)
 * @description : embedding Pseudonym information per Token
 * ----------------------------------------------------------------------------------------*/
function embedPseudonymInfoPerToken(tokenUID, arraySet, arrayCountSet)
{
	//alert("inside embedPseudonymInfo ");
	//alert("arraySet[0]: " + arraySet[0] + "\n" + "arraySet[1]: " + arraySet[1] );
	//placeholder for parsed information 
	var pseudonymInfoNode = selectionUIWindow.document.getElementById('PseudonymInfo');

	//tokenToPseudonymList 
	var tokenToPseudonymList = selectionUIWindow.document.createElement('input');
	tokenToPseudonymList.setAttribute('type', 'hidden');
	tokenToPseudonymList.setAttribute('id', 'pseudonym_for_'+tokenUID);
	
	for(var i = 0; arraySet != null && i < arraySet.length; i++)
	{
		var URISetList = selectionUIWindow.document.createElement('ul');
		URISetList.setAttribute('type', 'hidden');
		URISetList.setAttribute('id', 'URISet_'+i);
	
		//alert("arraySet[i].length " + arraySet[i].length);
		var countForArraySet = arrayCountSet[i];
		
		for(var j = 0; j < countForArraySet ; j++)
		{
			if( arraySet[i][j] == null )
			{
				alert("value is null " + i + " : " + j);
			}
			else
			{	
				var URIListItem = selectionUIWindow.document.createElement('li');
				URIListItem.setAttribute('type', 'hidden');
				URIListItem.setAttribute('id', 'URIListItem_'+j);
				
				var pseudonymId = arraySet[i][j];

				// alert("get PS Description for : pseudonymId : " + pseudonymId);
				
				URIListItem.setAttribute('value', arraySet[i][j]);
				var friendlyDesc = getFriendlyDescPseudonym(arraySet[i][j]);//pseudonymUID); //Add friendly description of pseudonym
				//alert("TEST : " + arraySet[i][j] + " friendly desc : " + friendlyDesc );
					
				URIListItem.setAttribute('friendlyDesc', friendlyDesc);
					
				URISetList.appendChild(URIListItem);
			}	
		}
		
		tokenToPseudonymList.appendChild(URISetList);
	}//end of for loop

	pseudonymInfoNode.appendChild(tokenToPseudonymList);
	
	//alert("END");
}



/* ---------------------------------------------------------------------------------------------
 * @name : getFriendlyDescPseudonym(pseudonymUID) 
 * @description : get friendly description of Pseudonym and put into Pseudonym element's value
 * --------------------------------------------------------------------------------------------*/
function getFriendlyDescPseudonym(pseudonymUID)
{
	// alert("parsing friendly desc of pseudonym... " +pseudonymUID);
	
	var friendlyDesc = "No Descripton for Pseudonym : " + pseudonymUID; 
	
	var pseudoDescs = idSelectJSON.PseudonymDescriptions;
	if(pseudoDescs != null && pseudoDescs.entry != null)
	{
		//alert("idSelectJSON.PseudonymDescriptions exists");
		
		// alert("length of pseudoDesc.entry : " + pseudoDescs.entry.length);
		if(pseudoDescs.entry.length == null)
		{
			// only one
			return getFriendlyPseudonymDescription(pseudoDescs.entry.value.PseudonymDescription)
		} 
		else 
		{
			for(var i = 0; i < pseudoDescs.entry.length; i++) {
				if(pseudoDescs.entry[i].key == pseudonymUID) {
					return getFriendlyPseudonymDescription(pseudoDescs.entry[i].value.PseudonymDescription);
				}
			}
		}
	}
	else
	{
		alert("idSelectJSON.PseudonymDescriptions is null");
	}	

	return friendlyDesc;
}

function getFriendlyPseudonymDescription(pseudonymDescription) {
	var pseudonymDescription;
	var scope = pseudonymDescription["@Scope"];
	var uid = pseudonymDescription["@PseudonymUID"];
	if(scope==null) {
		scope = "Unknown";
	}
	
	var metadata = pseudonymDescription.PseudonymMetadata;
	if(metadata == null) {
		pseudonymDescription = "No Description\n<br>Scope :" + scope;
	} else {
		if(metadata.FriendlyPseudonymDescription != null) {
			pseudonymDescription = getFriendlyByLanguage(metadata.FriendlyPseudonymDescription);
		}
		else
		{
			pseudonymDescription = "No Description";
		}
		var humanReadableData = metadata.HumanReadableData;
		if(humanReadableData != null) {
			pseudonymDescription = pseudonymDescription + "\n<br>" + humanReadableData;
		}
		pseudonymDescription = pseudonymDescription + "\n<br>Scope : " + scope;
	}
	// alert("Return Freindly PseudonymDescriptions " + pseudonymDescription);
	
	return pseudonymDescription;
}	

/* ------------------------------------------------------------------------------------------
 * @name : parseAndEmbedCredential() 
 * @description : parse credential information and embed it into HTML
 * ----------------------------------------------------------------------------------------*/
function parseAndEmbedCredentialMap()
{
//    alert("test : number of credentials " + credentials.length);
	//alert(credentials.length);
	if(idSelectJSON.CredentialDescriptions==null) {
	    // alert("Credentials ?? " + credentials);
		// add reference to 'no credential' Description
		// alert("add reference to 'no credential' Description");
//		embedCredentialInfo("no credential", "No Credential : Select Pseudonym", 'default image', "");
	}
	else
	{
		var credentials = idSelectJSON.CredentialDescriptions.entry;
		if(credentials.length == null) {
			// not a list!
            var singleCredential = idSelectJSON.CredentialDescriptions.entry.value;
		    // alert("Single Credential " + credentials + " : " + singleCredential);
			parseAndEmbedCredential(singleCredential, 0);
		}
		else 
		{
//	    	alert(" - more than one credential " + credentials.length + " : " + credentials);
			for(var i = 0; i < credentials.length; i++)
			{
//		    	alert(" - parse credential ix " + i + " : " + credentials[i]);
            	var curentCredential = credentials[i].value;
				parseAndEmbedCredential(curentCredential, i);
			}
		}
	}
	// always embed 'no credentail' - in case a CandidatePresentationToken only relies on pseudonyms
	embedCredentialInfo("no credential", "No Credential : Select Pseudonym :", 'chrome://sampleapp/content/resources/no_credential.jpg', 0, null, null);
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




/* ------------------------------------------------------------------------------------------
 * @name : addPresentationPolicRadioButton
 * @description : Adding radio button for presentation policy 
 * ----------------------------------------------------------------------------------------*/
function addPresentationPolicyRadioButton(policyIndex, policyUID, policyFriendlyName, policyFriendlyDescription, numberOfPolicies )
{
	var PresentationPolicyElement = selectionUIWindow.document.getElementById('PresentationPolicy');
	var presentationPolicy = selectionUIWindow.document.createElement('input'); 
	presentationPolicy.setAttribute('type', 'radio');
	presentationPolicy.setAttribute('name', policyFriendlyName);
	presentationPolicy.setAttribute('value', policyUID);
	presentationPolicy.setAttribute('id', 'PolicyRadioButton'+policyIndex);
	
	presentationPolicy.setAttribute('onclick', 'PolicyRadioButtonClicked(' + policyIndex + ', ' + numberOfPolicies +')'); //add event of RatioButton
	presentationPolicy.setAttribute('title', policyFriendlyDescription );
	
	PresentationPolicyElement.appendChild(presentationPolicy);
	PresentationPolicyElement.appendChild(selectionUIWindow.document.createTextNode(policyFriendlyName));
	
	var breakline = selectionUIWindow.document.createElement('br'); 
	PresentationPolicyElement.appendChild(breakline);
	
	//Toggle radtiobutton
	var policyRadioButtonStatus = selectionUIWindow.document.createElement('input');
	policyRadioButtonStatus.setAttribute('type', 'hidden');
	policyRadioButtonStatus.setAttribute('id', 'PolicyRadioButtonStatus'+policyIndex);
	policyRadioButtonStatus.setAttribute('value', 'unchecked');
	presentationPolicy.appendChild(policyRadioButtonStatus);
}



/* ------------------------------------------------------------------------------------------
 * @name : setElementForSelectedToken()
 * @description : Set placeholder of selected 'Presentation Token' in Window (selection UI) 
 * ----------------------------------------------------------------------------------------*/
function setElementForSelectedToken()
{
	var selectedTokenDiv = selectionUIWindow.document.getElementById('selectedToken');
	selectedTokenDiv.setAttribute('type', 'hidden');

	var selectedTokenUID = selectionUIWindow.document.createElement('input'); 
	selectedTokenUID.setAttribute('type', 'hidden');
	selectedTokenUID.setAttribute('id', 'selectedTokenUID');
	selectedTokenUID.setAttribute('value', 'default');
	
	selectedTokenDiv.appendChild(selectedTokenUID);
}





/* --------------------------------------------------------------------------------------------------
 * @name : addParsedPseudonymIntoContentPane(pseudonymIndex, pseudonymPane, key, exclusive, scope)
 * @description : Adding parsed pseudonym into content pane
 * ------------------------------------------------------------------------------------------------*/
/*
function addParsedPseudonymIntoContentPane(pseudonymPaneNode, pseudonymIndex, key, exclusive, scope)
{
	alert("in addParsedPseudonymIntoContentPane");
	
	//Pseudonym Contents 
	var pseudonymPane = selectionUIWindow.document.createElement('p'); 
	pseudonymPane.setAttribute('id', key); //pseudonymUID);
	pseudonymPane.setAttribute('align', 'left');
	pseudonymPaneNode.appendChild(pseudonymPane);
	
	//alert("In case when only one Pseudonym!");
	var newPseudonymPane = selectionUIWindow.document.createElement('div');
	newPseudonymPane.setAttribute('dojoType', 'dijit.layout.ContentPane');
	newPseudonymPane.setAttribute('style', 'background-color:#E6E6E6; width:590px; height:150px; margin-top:3px; margin-left:2px');
	newPseudonymPane.setAttribute('id', pseudonymIndex);
	newPseudonymPane.setAttribute('align', 'center');
	
	newPseudonymPane.appendChild(selectionUIWindow.document.createTextNode('exclusive : ' + exclusive ));
	newPseudonymPane.appendChild(selectionUIWindow.document.createElement('br'));
	newPseudonymPane.appendChild(selectionUIWindow.document.createTextNode('scope : ' + scope ));
	newPseudonymPane.appendChild(selectionUIWindow.document.createElement('br'));
	newPseudonymPane.appendChild(selectionUIWindow.document.createTextNode(' pseudonymUID : ' + key));// pseudonymUID ));

	pseudonymPane.appendChild(newPseudonymPane);
	
	 // Add Button for Pseudonym ContentPane

	//create newline 
	var brElement = selectionUIWindow.document.createElement('br');
	var brElement2 = selectionUIWindow.document.createElement('br');
	
	//create div element
	var divElement = selectionUIWindow.document.createElement('div');
	divElement.setAttribute('align', 'center');

	//create RadioButton
	var inputElement = selectionUIWindow.document.createElement('input');
	inputElement.setAttribute('type', 'radio');
	inputElement.setAttribute('dojoType', 'dijit.form.RadioButton');
	inputElement.setAttribute('value', key);
	inputElement.setAttribute('id', 'RadioButtonForPseudonym'+ pseudonymIndex);
	inputElement.setAttribute('onclick', 'radioButtonForPseudonymClicked(' + pseudonymIndex + ')'); //add event of RatioButton
    
	//hack for toggle radtiobutton
	var radioButtonStatus = selectionUIWindow.document.createElement('input');
	radioButtonStatus.setAttribute('type', 'hidden');
	radioButtonStatus.setAttribute('id', 'RadioButtonStatusForPseudonym' + pseudonymIndex);
	radioButtonStatus.setAttribute('value', 'unchecked');
	inputElement.appendChild(radioButtonStatus);
	
	var labelElement = selectionUIWindow.document.createElement('label');
	newPseudonymPane.appendChild(brElement);
	newPseudonymPane.appendChild(brElement2);
	divElement.appendChild(inputElement);
	divElement.appendChild(labelElement);
	newPseudonymPane.appendChild(divElement);
}
*/

/* --------------------------------------------------------------------------------------------------------------------
 * @name :  parseCandidatePresentationToken(selectionUIWindow, CandidatePresentationToken, ix)
 * @description : parse Candidate Presentation Token
 * ------------------------------------------------------------------------------------------------------------------*/
function parseCandidatePresentationToken(selectionUIWindow, candidatePresentationToken, ix, tokenCount)
{
	// alert("In NEW COMBINED parse CandidatePresentationToken "  + ix);
	var tokenNode = selectionUIWindow.document.getElementById('candidateTokens');

	var currentToken = candidatePresentationToken;
	var tokenInfo = "";

	var tokenPolicyUID = currentToken.Token["@PolicyUID"];//.@PolicyUID;
	var tokenUID = currentToken.Token["@TokenUID"];//.@TokenUID;
	var policyUID = currentToken.Token["@PolicyUID"];//.@PolicyUID;
//	var credentialUids = currentToken.CredentialUidList.CredentialUid; 
	
	//token information to be shown in the UI
	//tokenInfo = "PolicyUID : " + tokenPolicyUID + "  TokenUID : " + tokenUID + "  ";
	if(currentToken.FriendlyTokenDescription == null) {
		tokenInfo = "TokenInfo : " + tokenPolicyUID + "/" + tokenUID; 
	} 
/*	else if(currentToken.FriendlyTokenDescription.length > 1)
	{
		tokenInfo = currentToken.FriendlyTokenDescription[0].$; 
		//alert("tokenInfo : " + tokenInfo); 
	}
*/	
//	else if(currentToken.FriendlyTokenDescription.length == null)
	else
	{
		tokenInfo = getFriendlyByLanguage(currentToken.FriendlyTokenDescription);
		
		// tokenInfo = currentToken.FriendlyTokenDescription.$; 
		//alert("tokenInfo : " + tokenInfo); 
	}	
	
	//------------------------------------
	//  save policyUID for current token
	//------------------------------------
	var policyUIDforToken = selectionUIWindow.document.createElement('input');
	policyUIDforToken.setAttribute('type', 'hidden');
		
	//	policyUIDforToken.setAttribute('id', 'only_token'); 
	// - only one - ix = 0
	policyUIDforToken.setAttribute('id', ix + 'th_token');
	policyUIDforToken.setAttribute('value', tokenUID);
	policyUIDforToken.setAttribute('name', 'token');
	policyUIDforToken.setAttribute('policyUID', policyUID );
	policyUIDforToken.setAttribute('totalNumToken', tokenCount);
	policyUIDforToken.setAttribute('tokenIndex', ix);

	tokenNode.appendChild(policyUIDforToken);
	//alert("tokenUID embedded : " + tokenUID + ' ' + policyUID);
	
	//Get number of credentials in the current token 
	var numCredentialsInToken = getNumCredentialsInToken(currentToken);
	
	if( numCredentialsInToken > 1 ) 
	{
		// alert("numCredentialsInToken : " + numCredentialsInToken);
		var credentialUids = currentToken.CredentialUidList.CredentialUid; 
		
		for( var j = 0; j < numCredentialsInToken; j++ )
		{
			var currentCredential = credentialUids[j];
			//alert("currentCredential : " + currentCredential);
			
			for( var credentialAttribute in currentCredential )
			{
				//already save in above
				//tokenInfo = tokenInfo + credentialAttribute + " : " + currentCredential[credentialAttribute] + "   ";
			}
		}
		
	
		//placeholder for token in selection UI window
		var hiddenToken = selectionUIWindow.document.createElement('input');
		hiddenToken.setAttribute('type', 'hidden');
		hiddenToken.setAttribute('id', tokenUID); 
		hiddenToken.setAttribute('value', tokenInfo); 
		hiddenToken.setAttribute('numCredentials', numCredentialsInToken); //also set number of credential in token
	
		//get all credentials in the current token
		var allCredentialsInToken = "";
		var numCredentialsInToken = 0; 
		var credentialUidList = currentToken.CredentialUidList; 

		if(credentialUidList != null)
		{
			if(credentialUidList.CredentialUid.length > 0)
			{
				numCredentialsInToken = credentialUidList.CredentialUid.length;
			
				for( var k = 0; k < credentialUidList.CredentialUid.length; k++ )
				{
					var credentialForToken = selectionUIWindow.document.createElement('li');
					credentialForToken.setAttribute('type', 'hidden');
					credentialForToken.setAttribute('id', 'credential_'+k+'_in_'+tokenUID);
					credentialForToken.setAttribute('name', 'credential' );
					credentialForToken.setAttribute('value', credentialUidList.CredentialUid[k]);
					credentialForToken.appendChild(selectionUIWindow.document.createTextNode(credentialUidList.CredentialUid[k]));
		
					hiddenToken.appendChild(credentialForToken);
				}
			}
		}
		else //if(credentialUidList == null) 
		{
			var credentialForToken = selectionUIWindow.document.createElement('li');
			credentialForToken.setAttribute('type', 'hidden');
			credentialForToken.setAttribute('id', 'credential_null_in_'+tokenUID);
			credentialForToken.setAttribute('name', 'credential' );
			credentialForToken.setAttribute('value', 'null');
			credentialForToken.appendChild(selectionUIWindow.document.createTextNode('no credential'));

			hiddenToken.appendChild(credentialForToken);
		}	
	
		tokenNode.appendChild(hiddenToken);
	}
	else if(numCredentialsInToken == 1)
	{	
		//placeholder for token in selection UI window
		var hiddenToken = selectionUIWindow.document.createElement('input');
		hiddenToken.setAttribute('type', 'hidden');
		hiddenToken.setAttribute('id', tokenUID); 
		hiddenToken.setAttribute('value', tokenInfo); 
		hiddenToken.setAttribute('numCredentials', 1); //also set number of credential in token
	
		var credentialUidList = currentToken.CredentialUidList; 
		// alert("numCredentialsInToken : is only one : " + credentialUidList.CredentialUid);
		if(credentialUidList != null)
		{
			//alert("credentialUidList.CredentialUid.length : " + credentialUidList.CredentialUid.length);
			var credentialForToken = selectionUIWindow.document.createElement('li');
			credentialForToken.setAttribute('type', 'hidden');
			credentialForToken.setAttribute('id', 'credential_0_in_'+tokenUID);
			credentialForToken.setAttribute('name', 'credential' );
			credentialForToken.setAttribute('value', credentialUidList.CredentialUid);
			credentialForToken.appendChild(selectionUIWindow.document.createTextNode(credentialUidList.CredentialUid));
		
			hiddenToken.appendChild(credentialForToken);
		}
		
		tokenNode.appendChild(hiddenToken);
	}
	else if( numCredentialsInToken == 0) //there is no credentials in token
	{
		// alert(" - NO Credentials In Token!");
		
		tokenInfo = "There are no credentials in token";
		
		//placeholder for token in selection UI window
		var hiddenToken = selectionUIWindow.document.createElement('input');
		hiddenToken.setAttribute('type', 'hidden');
		hiddenToken.setAttribute('id', tokenUID); 
		hiddenToken.setAttribute('value', tokenInfo); 
		hiddenToken.setAttribute('numCredentials', 0); //also set number of credential in token
	
		// alert("Add Place Holder - for no credential !!!");
			
		var credentialForToken = selectionUIWindow.document.createElement('li');
		credentialForToken.setAttribute('type', 'hidden');
		credentialForToken.setAttribute('id', 'credential_null_in_'+tokenUID);
		credentialForToken.setAttribute('name', 'credential' );
		credentialForToken.setAttribute('value', 'no credential');
		credentialForToken.appendChild(selectionUIWindow.document.createTextNode('no credential'));

		hiddenToken.appendChild(credentialForToken);
	
		tokenNode.appendChild(hiddenToken);
	}
}



/* -------------------------------------------------------------------------------------
 * @name : embedTokenInfo()
 * @description : embed token information into html 
 * ------------------------------------------------------------------------------------*/
function embedTokenInfo(tokenUID, tokenInfo, selectionUIWindow, allCredentialsInToken, numCredentialsInToken)
{
	//embed token information into hidden input HTML element
	var hiddenToken = selectionUIWindow.document.createElement('input');
	hiddenToken.setAttribute('type', 'hidden');
	hiddenToken.setAttribute('id', tokenUID); 
	hiddenToken.setAttribute('value', tokenInfo); 
	
	//embed all credentials of token as a child element
	for(var i = 0; i < numCredentialsInToken; i++ )
	{
		var credentialForToken = selectionUIWindow.document.createElement('li');
		credentialForToken.setAttribute('type', 'hidden');
		credentialForToken.setAttribute('name', 'credential' );
		credentialForToken.setAttribute('value', 'haha');
		
		hiddenToken.appendChild(credentialForToken);
	}
	
	var tokenNode = selectionUIWindow.document.getElementById('candidateTokens');
	tokenNode.appendChild(hiddenToken);
}




/* ------------------------------------------------------------------------------------
 * @name : getNumCredentialsInToken(token) 
 * @description : get the number of credentials belongs to the token
 * -----------------------------------------------------------------------------------*/
function getNumCredentialsInToken(currentToken)
{
	var numCredentialsInToken = 0; 
	
	var credentialUidList = currentToken.CredentialUidList; 
    
//	alert("getNumCredentialsInToken  - start - credentialUidList : " + credentialUidList);
	if( credentialUidList == null )// COMMENT : 'undefined' is not working!! 
	{
		// alert("credentialUidLists do not exist in this token.");
		numCredentialsInToken = 0; 
	}
	else 
	{	
//		alert("getNumCredentialsInToken  - check length : " + credentialUidList.length);
		if(credentialUidList.length == null) { //if this is undefined, means it's not array
//			alert("getNumCredentialsInToken  - check credentialUidList.CredentialUid : " + credentialUidList.CredentialUid);
			if(credentialUidList.CredentialUid == null && credentialUidList.CredentialUid.length == null) {
//				alert("getNumCredentialsInToken  - No Credential - we think!");
				numCredentialsInToken = 0; 
			} else if (credentialUidList.CredentialUid[0].length == 1) {
//				alert("getNumCredentialsInToken  - only 1!");
				numCredentialsInToken = 1; 
			} else {
//				alert("getNumCredentialsInToken  - Why end here ??");
				numCredentialsInToken = credentialUidList.CredentialUid.length; 
			}
		} else {
//			alert("getNumCredentialsInToken  - just get length : " + credentialUidList.CredentialUid.length);
			numCredentialsInToken = credentialUidList.CredentialUid.length;
		}
	}	
//	alert("getNumCredentialsInToken : " + numCredentialsInToken);
	return numCredentialsInToken; 
}



/* -----------------------------------------------------------------------------------------------
 * @name : getSizeCandidateTokenList(testJSONinput) 
 * @description : get the number of candidate presentation tokens 
 * ---------------------------------------------------------------------------------------------*/
function getSizeCandidateTokenList(idSelectJSON)
{
	var arrayOfCandidateToken;
	if("verify" == idSelectionUIType) 
	{
		arrayOfCandidateToken = idSelectJSON.CandidatePresentationTokenList.CandidatePresentationToken;
	}
	else
	{
		arrayOfCandidateToken = idSelectJSON.CandidateIssuanceTokenList.CandidateIssuanceToken;
	}
	if(arrayOfCandidateToken == null ) 
	{
		return 0;
	} 
	if(arrayOfCandidateToken.length == null) {
		//alert("There is only one candidatePresentationToken");
		return 1; 
	}	
	else 
	{
		//alert("arrayOfCandidateToken.length : " + arrayOfCandidateToken.length);
		return arrayOfCandidateToken.length;
	}
}



/* ---------------------------------------------------------------------------------------------
 * @name : addContentPane(numberOfCredentials)
 * @description : Adding contentPane which contains radio Button
 * --------------------------------------------------------------------------------------------*/
function addContentPane(numberOfCredentials, selectionUIWindow, idSelectJSON)
{
	//alert("numberOfCredentials : " + numberOfCredentials);
	//alert("In addContentPane : selectionUIWindow " + selectionUIWindow);
	//alert("In addContentPane : selectionUIWindow.document " + selectionUIWindow.document);
	
	var credentialPaneNode = selectionUIWindow.document.getElementById('credentialPane');
	 
	if( credentialPaneNode == null )
	{
		alert("[DEBUG INFO] credentialPaneNode is null");
		credentialPaneNode = selectionUIWindow.document.getElementById('credentialPane'); 
	}	
	else 
	{	
		for(var i = 0; i < numberOfCredentials; i++ )
		{
			//add contentPane into 'Credential Selection' tabContentPane
			//alert("index : " + i );
			var elementID = 'credential'+i;
			var credentialIndex = i; 
			var credentialPane = selectionUIWindow.document.createElement('p'); 
			credentialPane.setAttribute('id', elementID);
			credentialPaneNode.appendChild(credentialPane);
		
			if(numberOfCredentials > 1) //which is array of credentials 
			{	
				//alert("in case there are more than 1 credentials");
				var node = addCredentialDescriptionContentPane(elementID, credentialIndex, selectionUIWindow, idSelectJSON);
				var credentialUID = idSelectJSON.credentialDescriptions.entry[credentialIndex].value.CredentialUID;
				//alert("credentialUID : " + credentialUID);
				//add radioButton
				addRadioButton(node, credentialIndex, selectionUIWindow, idSelectJSON, credentialUID);
			}
			else if(numberOfCredentials == 1) 
			{
				//alert("testJSONinput.credentialDescriptions.entry.value.CredentialUID :" + testJSONinput.credentialDescriptions.entry.value.CredentialUID);
				
				var node = addOneCredentialDescriptionContentPane(elementID, credentialIndex, selectionUIWindow, idSelectJSON);
				var credentialUID = idSelectJSON.credentialDescriptions.entry.value.CredentialUID;
				//alert("credentialUID : " + credentialUID);
				//add radioButton
				addRadioButton(node, credentialIndex, selectionUIWindow, idSelectJSON, credentialUID);
			}
		}//for
		
		//As a default, add one default content pane which indicate 'No Credential Selection'
		addNoCredentialSelectionOption(credentialPaneNode);
	}
}



/* -------------------------------------------------------------------------------------------
 * @name : addNoCredentialSelectionOption(credentialPaneNode)
 * @description : add contentPane which contains that user will not select any Credentials
 * ------------------------------------------------------------------------------------------*/
function addNoCredentialSelectionOption(credentialPaneNode)
{
	//Add content pane which indicating 'No Credential Selection' 
	var noCredentialSelectionPane = selectionUIWindow.document.createElement('p'); 
	noCredentialSelectionPane.setAttribute('id', 'noCredentialSelection');
	credentialPaneNode.appendChild(noCredentialSelectionPane);

	var newContentPane = selectionUIWindow.document.createElement('div');
	newContentPane.setAttribute('dojoType', 'dijit.layout.ContentPane');
	newContentPane.setAttribute('style', 'background-color:#E6E6E6; width:480px; height:210px; margin-left:2px');
	newContentPane.setAttribute('id', 'noCredentialSelectionOption');
	newContentPane.setAttribute('align', 'left');

	newContentPane.appendChild(selectionUIWindow.document.createTextNode('No credential selection'));

	noCredentialSelectionPane.appendChild(newContentPane);
	
	//Adding Radio Button
	var brElement = selectionUIWindow.document.createElement('br');	//create newline 

	var divElement = selectionUIWindow.document.createElement('div');	//create div element
	divElement.setAttribute('align', 'center');

	var inputElement = selectionUIWindow.document.createElement('input');	//create RadioButton
	inputElement.setAttribute('type', 'radio');
	inputElement.setAttribute('dojoType', 'dijit.form.RadioButton');
	inputElement.setAttribute('id', 'RadioButtonForNoCredential');
	inputElement.setAttribute('onclick', 'radioButtonClickedForNoCredential('+ numberOfCredentials + ')'); //add event of RatioButton
    
	//Toggle radtiobutton
	var radioButtonStatus = selectionUIWindow.document.createElement('input');
	radioButtonStatus.setAttribute('type', 'hidden');
	radioButtonStatus.setAttribute('id', 'RadioButtonStatusForNoCredential');
	radioButtonStatus.setAttribute('value', 'unchecked');
	inputElement.appendChild(radioButtonStatus);
	
	var labelElement = selectionUIWindow.document.createElement('label');
	newContentPane.appendChild(brElement);
	divElement.appendChild(inputElement);
	divElement.appendChild(labelElement);
	newContentPane.appendChild(divElement);
}



/* -------------------------------------------------------------------------------------------
 * @name : addOneCredentialDescription(elementID, credentialIndex)
 * @description : add contentPane which contains Credential Description
 * ------------------------------------------------------------------------------------------*/
function addOneCredentialDescriptionContentPane(elementID, credentialIndex, selectionUIWindow, idSelectJSON)
{
	var node = selectionUIWindow.document.getElementById(elementID);
	if(node != null)
	{	
		//alert("only one credential");
		var newContentPane = selectionUIWindow.document.createElement('div');
		newContentPane.setAttribute('dojoType', 'dijit.layout.ContentPane');
		newContentPane.setAttribute('style', 'background-color:#E6E6E6; width:480px; height:210px; margin-left:2px');
		newContentPane.setAttribute('id', 'credentialIndex'+ elementID);
		newContentPane.setAttribute('align', 'left');
	
		newContentPane.appendChild(selectionUIWindow.document.createTextNode('Credential UID : ' + idSelectJSON.credentialDescriptions.entry.value.CredentialUID));
		newContentPane.appendChild(selectionUIWindow.document.createElement('br'));
		newContentPane.appendChild(selectionUIWindow.document.createTextNode('CredentialSpecification UID : ' + idSelectJSON.credentialDescriptions.entry.value.CredentialSpecificationUID));
		newContentPane.appendChild(selectionUIWindow.document.createElement('br'));
		newContentPane.appendChild(selectionUIWindow.document.createTextNode('IssuerParameters UID : ' + idSelectJSON.credentialDescriptions.entry.value.IssuerParametersUID));
	
		var attributesArray = idSelectJSON.credentialDescriptions.entry.value.Attribute; //passed object is array 
		for(var i = 0; i < attributesArray.length; i++)
		{
			for(var attrDescProperty in attributesArray[i].AttributeDescription)
			{
				if(attrDescProperty == '@Type')
				{
					newContentPane.appendChild(selectionUIWindow.document.createTextNode(attributesArray[i].AttributeDescription[attrDescProperty] + ' : ' + attributesArray[i].AttributeValue.$));
					newContentPane.appendChild(selectionUIWindow.document.createElement('br'));
				}
			}
		}
		
		node.appendChild(newContentPane);
	
		return newContentPane; //return newly created node
	}
	else
	{
		alert("node is null");
	}	
}



/* -------------------------------------------------------------------------------------------
 * @name : addCredentialDescription(elementID, credentialIndex)
 * @description : add contentPane which contains Credential Description
 * ------------------------------------------------------------------------------------------*/
function addCredentialDescriptionContentPane(elementID, credentialIndex, selectionUIWindow, idSelectJSON)
{
	var node = selectionUIWindow.document.getElementById(elementID);
	if(node != null)
	{	
		var newContentPane = selectionUIWindow.document.createElement('div');
		newContentPane.setAttribute('dojoType', 'dijit.layout.ContentPane');
		newContentPane.setAttribute('style', 'background-color:#E6E6E6; width:480px; height:210px; margin-left:2px'); //width:590px; height:200px;
		newContentPane.setAttribute('id', 'credentialIndex'+ elementID);
		newContentPane.setAttribute('align', 'left');
	
		newContentPane.appendChild(selectionUIWindow.document.createTextNode('Credential UID : ' + idSelectJSON.credentialDescriptions.entry[credentialIndex].value.CredentialUID));
		newContentPane.appendChild(selectionUIWindow.document.createElement('br'));
		newContentPane.appendChild(selectionUIWindow.document.createTextNode('CredentialSpecification UID : ' + idSelectJSON.credentialDescriptions.entry[credentialIndex].value.CredentialSpecificationUID));
		newContentPane.appendChild(selectionUIWindow.document.createElement('br'));
		newContentPane.appendChild(selectionUIWindow.document.createTextNode('IssuerParameters UID : ' + idSelectJSON.credentialDescriptions.entry[credentialIndex].value.IssuerParametersUID));
		newContentPane.appendChild(selectionUIWindow.document.createElement('br'));
		
		var attributesArray = idSelectJSON.credentialDescriptions.entry[credentialIndex].value.Attribute; //passed object is array 
		for(var i = 0; i < attributesArray.length; i++)
		{
			for(var attrDescProperty in attributesArray[i].AttributeDescription)
			{
				if(attrDescProperty == '@Type')
				{
					newContentPane.appendChild(selectionUIWindow.document.createTextNode(attributesArray[i].AttributeDescription[attrDescProperty] + ' : ' + attributesArray[i].AttributeValue.$));
					newContentPane.appendChild(selectionUIWindow.document.createElement('br'));
				}
			}
		}
		
		node.appendChild(newContentPane);
	
		return newContentPane; //return newly created node
	}
	else
	{
		alert("node is null");
	}	
}




/* -----------------------------------------------------------------------------------------------
 * @name : addRadioButton(elementID)
 * @decription : add Radio Button to ContentPane whose id is elementID
 * ----------------------------------------------------------------------------------------------*/
function addRadioButton(parentElement, credentialIndex, selectionUIWindow, idSelectJSON, credentialUID)
{
	if(parentElement != null)
	{
		//create newline 
		var brElement = selectionUIWindow.document.createElement('br');
		
		//create div element
		var divElement = selectionUIWindow.document.createElement('div');
		divElement.setAttribute('align', 'center');
  
		//create RadioButton
		var inputElement = selectionUIWindow.document.createElement('input');
		inputElement.setAttribute('type', 'radio');
		inputElement.setAttribute('dojoType', 'dijit.form.RadioButton');
		inputElement.setAttribute('value', credentialUID);
		inputElement.setAttribute('id', 'RadioButton'+credentialIndex);
		inputElement.setAttribute('onclick', 'radioButtonClicked(' + credentialIndex + ', ' + numberOfCredentials +')'); //add event of RatioButton
	    
		//hack for toggle radtiobutton
		var radioButtonStatus = selectionUIWindow.document.createElement('input');
		radioButtonStatus.setAttribute('type', 'hidden');
		radioButtonStatus.setAttribute('id', 'RadioButtonStatus'+credentialIndex);
		radioButtonStatus.setAttribute('value', 'unchecked');
		inputElement.appendChild(radioButtonStatus);
		
		var labelElement = selectionUIWindow.document.createElement('label');
		parentElement.appendChild(brElement);
		divElement.appendChild(inputElement);
		divElement.appendChild(labelElement);
		parentElement.appendChild(divElement);
	}
}



/* ----------------------------------------------------------------------------------------------
 * @name : invalidateVariables()
 * @description : invalidate (initialize) global variables
 * ---------------------------------------------------------------------------------------------*/
function invalidateVariables()
{
	numberOfTokens = 0;
	numberOfCredentials = 0;	
}



/* ----------------------------------------------------------------------------------------------
 * @name : initializeGlobalVariables()
 * @description : initialize global variables
 * ---------------------------------------------------------------------------------------------*/
function initializeGlobalVariables(inputJSON)
{
	numberOfTokens = 0;
	numberOfCredentials = 0;	
	idSelectJSON = inputJSON; //set input JSON
}




/* ----------------------------------------------------------------------------------------------
 * @name : getNumCredentials()
 * @description : find the number of credentials 
 * --------------------------------------------------------------------------------------------*/
function getNumCredentials(idSelectJSON)
{
	//find numbers of credentials 
	
	if(idSelectJSON.hasOwnProperty('credentialDescriptions')) //previous JSON was 'credentialDescriptions'
	{
		//To parse old JSON
		//var credentialDescription_rval = testJSONinput.credentialDescriptions; //rval means 'right value'. key : value 
		
		var credentialDescription_rval = idSelectJSON.credentialDescriptions; //rval means 'right value'. key : value
		//alert("credentialDescription_rval : " + credentialDescription_rval);
		if( credentialDescription_rval != null )
		{	
			if(credentialDescription_rval.hasOwnProperty('entry'))
			{
				var entry_rval = credentialDescription_rval.entry;
			    //alert("entry_rval : " + entry_rval);
				//find the number of entry 
				if( entry_rval != null )
				{
					//alert("entry_rval : " + entry_rval);
					
					for( var x in entry_rval ) //in case entry_rval is an array of object, x is index of array
					{
						if( x == 'key' ) //case 1 : there is only one credentail so x is property of entry_rval 'key'
						{
							numberOfCredentials = 1; 
							return numberOfCredentials;
						}
						else  //case 2 : there are more than one credential which is entry_rval is array, x is index
						{
							//alert("entry_rval[x] : " + entry_rval[x]);
							if( entry_rval[x].hasOwnProperty('key') )
							{
								numberOfCredentials = numberOfCredentials + 1;
								//alert("numberOfCredentials : " + numberOfCredentials);
							}
						}	
					}
				    //alert("numberOfCredentials : " + numberOfCredentials );
				}
				else 
				{
					alert("[DEBUG INFO] credentialDescription entry is null. ");
					return 0;
				}
			}
			else
			{
				alert("[DEBUG INFO] credentials entry is null. ");
				return 0; 
			}
		} 
		else 
		{
			alert("[DEBUG INFO] There is no credential description. ");
			return 0;
		}	
	 }
	else 
	{	
		alert("[DEBUG INFO] credentialDescriptions doesn't exist");
		return 0;
	}
	
	return numberOfCredentials; 
}



/* ----------------------------------------------------------------------------------------------
 * @name : parseCandidatePresentationTokenList(testJSONinput)
 * @description : parse CandidatePresentationTokenList
 * --------------------------------------------------------------------------------------------*/
function parseCandidatePresentationTokenList(idSelectJSON)
{
	var candidatePresentationTokenList = idSelectJSON.CandidatePresentationTokenList;
	
	//get the number of Credential Token
	var tokenListLength = getNumObjects(candidatePresentationTokenList);
	
	if(tokenListLength > 0)
	{
		var candidatePresentationTokenArray = candidatePresentationTokenList.CandidatePresentationToken;
		var tokenNum = getNumObjects(candidatePresentationTokenArray);
		numberOfTokens = tokenNum;
	}
}



/* ---------------------------------------------------------------------------------------------
 * @name : getNumObject(JSONobj
 * @description : get the number of object in the input object
 * -------------------------------------------------------------------------------------------*/
function getNumObjects(JSONobj)
{
	var numObj = 0; 
	for(var x in JSONobj)
	{
		if(JSONobj.hasOwnProperty(x))
		{
			numObj = numObj + 1; 
		}
	}

	return numObj;
}

