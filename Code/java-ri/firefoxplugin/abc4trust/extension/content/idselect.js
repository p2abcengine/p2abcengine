/* ------------------------------------------------------------------------------------------
 * Revision Date : May 8. 2012
 * Due to the UI requirement has been fixed by the meeting on 7th May 
 * ----------------------------------------------------------------------------------------*/



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
	
	//Fixed window size 
	var width = 1160; 
	var height = 680; //default 
	
	//Add height depends on the number of Presentation Policy
	if(numPolicy > 0)
	{
		height = height + (numPolicy * 40); 
	}
	else if(numPolicy == 0)
	{
		height = default_height;
	}	
	
	//COMMENT : relative screen size depends on the screen, but at the moment we use fixed size window
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
	var PolicyDescriptions_entry = PolicyDescriptions.entry;
	var numEntry = PolicyDescriptions_entry.length; 
	
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
function populateSelectionUI(inputJSON)
{
	idSelectJSON = inputJSON; 

	//Initialize the global variables in case user open Selection UI window several time
	initializeGlobalVariables(inputJSON); 
	
	//Show Selection UI
	openSelectionUI(idSelectJSON);
	
	//Make all global variables initialize for next use
	invalidateVariables(); 
}





/* ------------------------------------------------------------------------------------------------
 * @name : openSelectionUI
 * @description : open predefine UI framework
 * -----------------------------------------------------------------------------------------------*/
function openSelectionUI(idSelectJSON)
{
	var params = setParam(); 
	
	/*
	 * Set up Selection UI frame
	 */ 
	selectionUIhtml = "chrome://sampleapp/content/html/SelectionUI.html"; 
	selectionUIWindow = window.open(selectionUIhtml, 'selection UI', params);

	if( selectionUIWindow == null || selectionUIWindow.closed )
	{
		alert("[Error] Selection UI doesn't exist!");
	}
	else 
	{
		/*
		 * Parse Presentation Policy
		 */
		var numPolicy = idSelectJSON.PolicyDescriptions.entry.length;
		
		if(idSelectJSON.PolicyDescriptions.entry.length == null)
		{
			numPolicy = 1; 
			numberOfPolicies = 1; 
		}	
		
		//IMPORTANT : This alert is necessary to show policy 
		alert("[Information] You have "+ numPolicy + " presentation policies");
		if (numPolicy == 1)
		{
			var policyUID; 
			var policyFriendlyName;
			var policyFriendlyDescription;
			policyUID = idSelectJSON.PolicyDescriptions.entry.value.PolicyUID;
			
			//----------------------------------------------------
			// Handle array of friendly name (multiple language)
			//----------------------------------------------------
			arrayOfPolicyFriendlyName = idSelectJSON.PolicyDescriptions.entry.value.Message.FriendlyPolicyName;
			if(arrayOfPolicyFriendlyName.length > 1) //i.e. multiple language 
			{
				//COMMENT : Get the first language (English) - Multi-language support would be the future work
				policyFriendlyName = idSelectJSON.PolicyDescriptions.entry.value.Message.FriendlyPolicyName[0].$;
				policyFriendlyDescription = idSelectJSON.PolicyDescriptions.entry.value.Message.FriendlyPolicyDescription[0].$;
			
				addPresentationPolicyRadioButton(0, policyUID, policyFriendlyName, policyFriendlyDescription, numberOfPolicies);
			}
			else if(arrayOfPolicyFriendlyName.length == null) //only one FriendlyName
			{
				policyFriendlyName = idSelectJSON.PolicyDescriptions.entry.value.Message.FriendlyPolicyName.$; //if only one language support
				policyFriendlyDescription = idSelectJSON.PolicyDescriptions.entry[policyIndex].value.Message.FriendlyPolicyDescription.$;
				addPresentationPolicyRadioButton(0, policyUID, policyFriendlyName, policyFriendlyDescription, numberOfPolicies);
			}	
		}	
		else if(numPolicy > 1)
		{
			for(var policyIndex = 0; policyIndex < numPolicy; policyIndex++)
			{
				var policyUID; 
				var policyFriendlyName;
				var policyFriendlyDescription;
				policyUID = idSelectJSON.PolicyDescriptions.entry[policyIndex].value.PolicyUID;
				
				//----------------------------------------------------
				// Handle array of friendly name (multiple language)
				//----------------------------------------------------
				arrayOfPolicyFriendlyName = idSelectJSON.PolicyDescriptions.entry[policyIndex].value.Message.FriendlyPolicyName;
				if(arrayOfPolicyFriendlyName.length > 1) //i.e. multiple language 
				{
					//COMMENT : Get the first language (English) - Multi-language support would be the future work
					policyFriendlyName = idSelectJSON.PolicyDescriptions.entry[policyIndex].value.Message.FriendlyPolicyName[0].$;
					policyFriendlyDescription = idSelectJSON.PolicyDescriptions.entry[policyIndex].value.Message.FriendlyPolicyDescription[0].$;
				
					addPresentationPolicyRadioButton(policyIndex, policyUID, policyFriendlyName, policyFriendlyDescription, numberOfPolicies);
				}
				else if(arrayOfPolicyFriendlyName.length == null) //only one FriendlyName
				{
					policyFriendlyName = idSelectJSON.PolicyDescriptions.entry[policyIndex].value.Message.FriendlyPolicyName.$; //if only one language support
					policyFriendlyDescription = idSelectJSON.PolicyDescriptions.entry[policyIndex].value.Message.FriendlyPolicyDescription.$;
					addPresentationPolicyRadioButton(policyIndex, policyUID, policyFriendlyName, policyFriendlyDescription, numberOfPolicies);
				}	
				
			}//end of for 	
		}//end of if	
		
		/*
		 * Parse Presentation Tokens which follows the policy and Embed information 
		 */
		// Parsing Presentation Token
		sizeOfCandidatePresentationTokenList = getSizeCandidatePresentationTokenList(idSelectJSON);
	
		if( sizeOfCandidatePresentationTokenList > 1) //in case when more than on presentation token exists 
		{
			parseCandidatePresentationToken(selectionUIWindow, idSelectJSON, sizeOfCandidatePresentationTokenList );
		}
		else //only one presentation token exists
		{
			parseOneCandidatePresentationToken(selectionUIWindow, idSelectJSON, sizeOfCandidatePresentationTokenList );
		}	
		
		/*
		 * Add Credential Information inside HTML
		 */
		parseAndembedCredential(); 
	
		//save PseudonymChoiceList for each Token 
		parsePseudonymChoiceList();
		
		//save InspectorChoiceList for each Token
		parseInspectorChoiceList();
	}
}



/* ------------------------------------------------------------------------------------------
 * @name : parseInspectorChoiceList() 
 * @description : parse InspectorChoiceList 
 * @comment : In case of InspectorChoiceList, choose of one from each URISet
 * ----------------------------------------------------------------------------------------*/
function parseInspectorChoiceList()
{
	var tokens = idSelectJSON.CandidatePresentationTokenList.CandidatePresentationToken;
	
	var arraySet; 
	var arrayURI;  

	if(tokens.length > 1) //there are more than 2 tokens
	{
		for(var i = 0; i < tokens.length; i++ )
		{	
			var policyUID = tokens[i].Token["@PolicyUID"]; //.@PolicyUID;
			var tokenUID = tokens[i].Token["@TokenUID"];//@TokenUID;
			var InspectorChoiceList = tokens[i].InspectorChoiceList; 
	
			if(InspectorChoiceList != null)
			{
				var URISet = tokens[i].InspectorChoiceList.URISet;
				
				arraySet = new Array(); 
				if(URISet.length > 1)
				{
					for(var j = 0; j < URISet.length; j++) //URISet level iteration
					{	
						var URIs = URISet[j].URI; 
						arrayURI = new Array();
						
						if(URIs.length > 1 && URIs.length < 20) 
						{
							for(var k = 0; k < URIs.length; k++) //URI level iteration 
							{
								arrayURI[k] = URIs[k];
							}
							
							arraySet[j] = arrayURI;
						}
						else if(URIs.length > 20) 
						{
							arraySet[j] = URIs;
						}	
						else if(URIs.length == null && URIs != null )
						{
							arrayURI[0] = URIs;
							arraySet[j] = arrayURI[0];
						}
					}//for
					
					embedInspectorInfoPerToken(tokenUID, arraySet);
				}	
				else if(URISet.length == null && URISet != null) //only one URISet 
				{
					arraySet = new Array(); 
					arrayURI = new Array();
					
					var URI = URISet.URI; 
					
					if(URI.length > 20)
					{
						arrayURI[0] = URI;
					}
					else if(URI.length > 1 && URI.length < 20 ) 
					{
						for(var k = 0; k < URI.length; k++)
						{
							arrayURI[k] = URI[k];
						}	
					}
					else if(URI.length == null && URI != null)
					{
						arrayURI[0] = URI; 
					}	
					
					arraySet[0] = arrayURI; 
					arraySet[1] = null; //arrayURI;
					
					embedInspectorInfoPerToken(tokenUID, arraySet);
				}	
			}	
			else if(InspectorChoiceList == null)
			{
				//embed this information 
				var InspectorInfoNode = selectionUIWindow.document.getElementById('InspectorInfo');

				var tokenToInspectorList = selectionUIWindow.document.createElement('input');
				tokenToInspectorList.setAttribute('type', 'hidden');
				tokenToInspectorList.setAttribute('id', 'inspector_for_'+ tokenUID ); 
				tokenToInspectorList.setAttribute('value', 'There is no inspector information');
				
				InspectorInfoNode.appendChild(tokenToInspectorList);
			}	
		}//end of for loop
	}
	else if(tokens.length == null && tokens != null) //there is only one token
	{
		var policyUID = tokens[i].Token["@PolicyUID"];//.@PolicyUID;
		var tokenUID = tokens[i].Token["@TokenUID"];//.@TokenUID;
		var InspectorChoiceList = token[i].InspectorChoiceList; 
		
		if(InspectorChoiceList != null)
		{
			var URISet = tokens[i].InspectorChoiceList.URISet;
			
			if(URISet.length > 1)
			{
				arraySet = new Array(); 
				arrayURI = new Array();
				
				for(var j = 0; j < URISet.length; j++) //URISet level iteration
				{	
					var URIs = URISet[j].URI; 
					if(URIs.length > 1)
					{
						for(var kk = 0; kk < URIs.length; kk++) //URI level iteration 
						{
							arrayURI[kk] = URIs[kk];
						}
					}
					else if(URIs.length == null && URIs != null )
					{
						arrayURI[0] = URIs;
					}
					
					arraySet[j] = arrayURI;
				}
			}	
			else if(URISet.length == null && URISet != null) //only one URISet 
			{
				arraySet = new Array(); 
				arrayURI = new Array();
				
				var URI = URISet.URI; 
				
				if(URI.length > 20 ) 
				{
					arrayURI[0] = URI;
				}
				else if(URI.length > 1 && URI.length < 20 ) 
				{
					for(var ii = 0; ii < URI.length; ii++)
					{
						arrayURI[ii] = URI[ii];
					}	
				}
				else if(URI.length == null && URI != null)
				{
					arrayURI[0] = URI; 
				}	
				
				arraySet[0] = arrayURI; 
			}	
		}	
		
		embedInspectorInfoPerToken(tokenUID, arraySet);
	}	
}




/* ------------------------------------------------------------------------------------------
 * @name : embedInspectorInfoPerToken(tokenUID, arraySet)
 * @description : embedding Inspector information per Token
 * ----------------------------------------------------------------------------------------*/
function embedInspectorInfoPerToken(tokenUID, arraySet)
{
	//placeholder for parsed information 
	var InspectorInfoNode = selectionUIWindow.document.getElementById('InspectorInfo');

	//policyToInspectorList 
	var tokenToInspectorList = selectionUIWindow.document.createElement('input');
	tokenToInspectorList.setAttribute('type', 'hidden');
	tokenToInspectorList.setAttribute('id', 'inspector_for_'+tokenUID);
	
	for(var i = 0; i < arraySet.length; i++)
	{
		var URISetList = selectionUIWindow.document.createElement('ul');
		URISetList.setAttribute('type', 'hidden');
		URISetList.setAttribute('id', 'URISet_'+i);
	
		var set = new Array(); 
		set = arraySet[i];
		
		if(arraySet[i] != null)
		{	
			if(arraySet[i].length != null && arraySet[i].length < 10) 
			{	
				for(var j = 0; j < arraySet[i].length ; j++)
				{
					if( arraySet[i][j] == null )
					{
						alert("value is null");
					}
					else
					{	
						var URIListItem = selectionUIWindow.document.createElement('li');
						URIListItem.setAttribute('type', 'hidden');
						URIListItem.setAttribute('id', 'URIListItem_'+j);
				
						URIListItem.setAttribute('value', arraySet[i][j]);
						var friendlyDesc = getFriendlyDescInspector(arraySet[i][j]);
						URIListItem.setAttribute('friendlyDesc', friendlyDesc);  
				
						URISetList.appendChild(URIListItem);
					}	
				}//for
		
				tokenToInspectorList.appendChild(URISetList);
			}
			else if(arraySet[i].length != null && arraySet[i].length > 10) 
			{
				var URIListItem = selectionUIWindow.document.createElement('li');
				URIListItem.setAttribute('type', 'hidden');
				URIListItem.setAttribute('id', 'URIListItem_0');
		
				URIListItem.setAttribute('value', arraySet[i]);
				URIListItem.setAttribute('friendlyDesc', getFriendlyDescInspector(arraySet[i]) ); 
		
				URISetList.appendChild(URIListItem);
				tokenToInspectorList.appendChild(URISetList);
			}	
			else 
			{
				//alert("arraySet[i].length is null. No need to append.");
			}
		}	
	}//end of for loop

	InspectorInfoNode.appendChild(tokenToInspectorList);
}



/* ---------------------------------------------------------------------------------------------
 * @name : getFriendlyDescInspector(inspectorUID) 
 * @description : get friendly description of Inspector and put into Inspector element's value
 * --------------------------------------------------------------------------------------------*/
function getFriendlyDescInspector(inspectorUID)
{
	var friendlyDesc; 
	var inspectorDescs = idSelectJSON.InspectorDescriptions;

	if(inspectorDescs != null)
	{
		if(inspectorDescs.entry != null)
		{
			if(inspectorDescs.entry.length > 1) //more than 2 inspector
			{
				for(var i = 0; i < inspectorDescs.entry.length; i++)
				{
					if(inspectorDescs.entry[i].key == inspectorUID)
					{
						//In case when there is no friendly description 
						if(inspectorDescs.entry[i].value.FriendlyInspectorDescription == null)
						{
							friendlyDesc = "no friendly description exist";
							return friendlyDesc;
						}	
						else 
						{	
							if(inspectorDescs.entry[i].value.FriendlyInspectorDescription.length > 1)
							{
								friendlyDesc = inspectorDescs.entry[i].value.FriendlyInspectorDescription[0].$; 
							}
							else if(inspectorDescs.entry[i].value.FriendlyInspectorDescription.length == null)
							{
								friendlyDesc = inspectorDescs.entry[i].value.FriendlyInspectorDescription.$;  //find the friendly description of pseudonym
							}
						}	
					}
					else if(inspectorDescs.entry[i].key.$ == inspectorUID)
					{
						//In case when there is no friendly description 
						if(inspectorDescs.entry[i].value.FriendlyInspectorDescription == null)
						{
							friendlyDesc = "no friendly description exist";
							return friendlyDesc;
						}	
						else 
						{	
							if(inspectorDescs.entry[i].value.FriendlyInspectorDescription.length > 1)
							{
								friendlyDesc = inspectorDescs.entry[i].value.FriendlyInspectorDescription[0].$; 
							}
							else if(inspectorDescs.entry[i].value.FriendlyInspectorDescription.length == null)
							{
								friendlyDesc = inspectorDescs.entry[i].value.FriendlyInspectorDescription.$;  //find the friendly description of pseudonym
							}
						}	
					}
				}//for	
			}//if
			else if(inspectorDescs.entry.length == 1) //there is only one pseudonym
			{
				if(inspectorDescs.entry.key == inspectorUID)
				{
					//In case when there is no friendly description 
					if(inspectorDescs.entry[i].value.FriendlyInspectorDescription == null)
					{
						friendlyDesc = "no friendly description exist";
						return friendlyDesc;
					}
					else if(inspectorDescs.entry.value.FriendlyInspectorDescription.length > 1)
					{
						friendlyDesc = inspectorDescs.entry.value.FriendlyInspectorDescription[0].$; 
					}
					else if(inspectorDescs.entry.value.FriendlyInspectorDescription.length == null)
					{
						friendlyDesc = inspectorDescs.entry.value.FriendlyInspectorDescription.$;  //find the friendly description of pseudonym
					}
				}
				else if(inspectorDescs.entry.key.$ == inspectorUID)
				{
					//In case when there is no friendly description 
					if(inspectorDescs.entry[i].value.FriendlyInspectorDescription == null)
					{
						friendlyDesc = "no friendly description exist";
						return friendlyDesc;
					}
					else if(inspectorDescs.entry.value.FriendlyInspectorDescription.length > 1)
					{
						friendlyDesc = inspectorDescs.entry.value.FriendlyInspectorDescription[0].$; 
					}
					else if(inspectorDescs.entry.value.FriendlyInspectorDescription.length == null)
					{
						friendlyDesc = inspectorDescs.entry.value.FriendlyInspectorDescription.$;  //find the friendly description of pseudonym
					}
				}
			}
			else if(inspectorDescs.entry.length == null) //there is only one pseudonym
			{
				if(inspectorDescs.entry.key == inspectorUID)
				{
					//In case when there is no friendly description 
					if(inspectorDescs.entry.value.FriendlyInspectorDescription == null)
					{
						friendlyDesc = "no friendly description exist";
						return friendlyDesc;
					}
					else if(inspectorDescs.entry.value.FriendlyInspectorDescription.length > 1)
					{
						friendlyDesc = inspectorDescs.entry.value.FriendlyInspectorDescription[0].$; 
					}
					else if(inspectorDescs.entry.value.FriendlyInspectorDescription.length == null)
					{
						friendlyDesc = inspectorDescs.entry.value.FriendlyInspectorDescription.$;  //find the friendly description of pseudonym
					}
				}
				else if(inspectorDescs.entry.key.$ == inspectorUID)
				{
					//In case when there is no friendly description 
					if(inspectorDescs.entry.value.FriendlyInspectorDescription == null)
					{
						friendlyDesc = "no friendly description exist";
						return friendlyDesc;
					}
					else if(inspectorDescs.entry.value.FriendlyInspectorDescription.length > 1)
					{
						friendlyDesc = inspectorDescs.entry.value.FriendlyInspectorDescription[0].$; 
					}
					else if(inspectorDescs.entry.value.FriendlyInspectorDescription.length == null)
					{
						friendlyDesc = inspectorDescs.entry.value.FriendlyInspectorDescription.$;  //find the friendly description of pseudonym
					}
				}
			}
		}	
	}
	else
	{
		alert("idSelectJSON.InspectorDescriptions is null");
	}	
	
	return friendlyDesc;
}





/* ------------------------------------------------------------------------------------------
 * @name : parsePseudonymChoiceList() 
 * @description : parse PseudonymChoiceList 
 * @comment : In case of PseudonymChoiceList, choose of the array in URISet
 * ----------------------------------------------------------------------------------------*/
function parsePseudonymChoiceList()
{
	var tokens = idSelectJSON.CandidatePresentationTokenList.CandidatePresentationToken;
	var arraySet = null; 
	var arrayURI = null; 
	
	if(tokens.length > 1) //there are more than 2 tokens
	{
		for(var i = 0; i < tokens.length; i++ )
		{	
			var policyUID = tokens[i].Token["@PolicyUID"];//.@PolicyUID;
			var tokenUID = tokens[i].Token["@TokenUID"];//.@TokenUID;
			var PseudonymChoiceList = tokens[i].PseudonymChoiceList; 
	
			if(PseudonymChoiceList != null)
			{
				var URISet = tokens[i].PseudonymChoiceList.URISet;
				
				//Instantiate array
				arraySet = new Array(); 
				
				if(URISet.length > 1)
				{
					for(var j = 0; j < URISet.length; j++) //URISet level iteration
					{	
						var URIs = URISet[j].URI; 
						arrayURI = new Array();
						
						if(URIs.length > 1)
						{
							for(var k = 0; k < URIs.length; k++) //URI level iteration 
							{
								arrayURI[k] = URIs[k];
							}
							
							arraySet[j] = arrayURI;
						}
						else if(URIs.length == null && URIs != null )
						{
							arrayURI[0] = URIs;
							arraySet[j] = arrayURI[0];
						}
					}
					
					//embed parsed info
					embedPseudonymInfoPerToken(tokenUID, arraySet);
				}	
				else if(URISet.length == null && URISet != null) //only one URISet 
				{
					arraySet = new Array(); 
					arrayURI = new Array();
					
					var URI = URISet.URI; 
					
					if(URI.length > 20)
					{
						arrayURI[0] = URI;
					}
					else if(URI.length > 1 && URI.length < 20 )  
					{
						for(var ii = 0; ii < URI.length; ii++)
						{
							arrayURI[ii] = URI[ii];
						}	
					}
					else if(URI.length == null && URI != null)
					{
						arrayURI[0] = URI; 
					}	
					
					arraySet[0] = arrayURI; 
					
					//embed parsed info
					embedPseudonymInfoPerToken(tokenUID, arraySet);
				}	
			}	
		}//end of for loop
	}
	else if(tokens.length == null && tokens != null) //there is only one token
	{
		var policyUID = tokens[i].Token["@PolicyUID"];//.@PolicyUID;
		var tokenUID = tokens[i].Token["@TokenUID"];//.@TokenUID;
		var PseudonymChoiceList = token[i].PseudonymChoiceList; 
		
		if(PseudonymChoiceList != null)
		{
			var URISet = tokens[i].PseudonymChoiceList.URISet;
			
			if(URISet.length > 1)
			{
				arraySet = new Array(); 
				arrayURI = new Array();
				
				for(var j = 0; j < URISet.length; j++) //URISet level iteration
				{	
					var URIs = URISet[j].URI; 
					if(URIs.length > 1)
					{
						for(var k = 0; k < URIs.length; k++) //URI level iteration 
						{
							arrayURI[k] = URIs[k];
						}
					}
					else if(URIs.length == null && URIs != null )
					{
						arrayURI[0] = URIs;
					}
					
					arraySet[j] = arrayURI;
				}
			}	
			else if(URISet.length == null && URISet != null) //only one URISet 
			{
				arraySet = new Array(); 
				arrayURI = new Array();
				
				var URI = URISet.URI; 
				
				if(URI.length > 20 ) 
				{
					arrayURI[0] = URI;
				}
				else if(URI.length > 1 && URI.length < 20) 
				{
					for(var i = 0; i < URI.length; i++)
					{
						arrayURI[i] = URI[i];
					}	
				}
				else if(URI.length == null && URI != null)
				{
					arrayURI[0] = URI; 
				}	
				
				arraySet[0] = arrayURI; 
			}	
		}	
		
		embedPseudonymInfoPerToken(tokenUID, arraySet);
	}	
}




/* ------------------------------------------------------------------------------------------
 * @name : embedPseudonymInfoPerToken(tokenUID, arraySet)
 * @description : embedding Pseudonym information per Token
 * ----------------------------------------------------------------------------------------*/
function embedPseudonymInfoPerToken(tokenUID, arraySet)
{
	//placeholder for parsed information 
	var pseudonymInfoNode = selectionUIWindow.document.getElementById('PseudonymInfo');

	//tokenToPseudonymList 
	var tokenToPseudonymList = selectionUIWindow.document.createElement('input');
	tokenToPseudonymList.setAttribute('type', 'hidden');
	tokenToPseudonymList.setAttribute('id', 'pseudonym_for_'+tokenUID);
	
	for(var i = 0; i < arraySet.length; i++)
	{
		var URISetList = selectionUIWindow.document.createElement('ul');
		URISetList.setAttribute('type', 'hidden');
		URISetList.setAttribute('id', 'URISet_'+i);
	
		if(arraySet[i].length != null)
		{	
			for(var j = 0; j < arraySet[i].length ; j++)
			{
				if( arraySet[i][j] == null )
				{
					//alert("value is null");
				}
				else
				{	
					var URIListItem = selectionUIWindow.document.createElement('li');
					URIListItem.setAttribute('type', 'hidden');
					URIListItem.setAttribute('id', 'URIListItem_'+j);
				
					URIListItem.setAttribute('value', arraySet[i][j]);
					var friendlyDesc = getFriendlyDescPseudonym(arraySet[i][j]);
					
					URIListItem.setAttribute('friendlyDesc', friendlyDesc);
					URISetList.appendChild(URIListItem);
				}	
			}
			tokenToPseudonymList.appendChild(URISetList);
		}
		else
		{
			//alert("arraySet[i].length is null. No need to append.");
		}	
	}//end of for loop

	pseudonymInfoNode.appendChild(tokenToPseudonymList);
}



/* ---------------------------------------------------------------------------------------------
 * @name : getFriendlyDescPseudonym(pseudonymUID) 
 * @description : get friendly description of Pseudonym and put into Pseudonym element's value
 * --------------------------------------------------------------------------------------------*/
function getFriendlyDescPseudonym(pseudonymUID)
{
	var friendlyDesc; 
	var pseudoDescs = idSelectJSON.PseudonymDescriptions;

	if(pseudoDescs != null)
	{
		if(pseudoDescs.entry != null)
		{
			if(pseudoDescs.entry.length > 1) //more than 2 pseudonym
			{
				for(var i = 0; i < pseudoDescs.entry.length; i++)
				{
					if(pseudoDescs.entry[i].key == pseudonymUID)
					{
						if(pseudoDescs.entry[i].value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription.length > 1)
						{
							friendlyDesc = pseudoDescs.entry[i].value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription[0].$;
						}
						else if(pseudoDescs.entry[i].value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription.length == null)
						{
							friendlyDesc = pseudoDescs.entry[i].value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription.$;  //find the friendly description of pseudonym
					
						}	
					}
					else if (pseudoDescs.entry[i].key.$ == pseudonymUID)
					{
						if(pseudoDescs.entry[i].value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription.length > 1)
						{
							friendlyDesc = pseudoDescs.entry[i].value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription[0].$;
						}
						else if(pseudoDescs.entry[i].value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription.length == null)
						{
							friendlyDesc = pseudoDescs.entry[i].value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription.$;  //find the friendly description of pseudonym
					
						}	
					} 
				}	
			}
			else if(pseudoDescs.entry.length == 1)//there is only one pseudonym
			{
				if(pseudoDescs.entry.key == pseudonymUID)
				{
					if(pseudoDescs.entry.value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription.length > 1)
					{
						friendlyDesc = pseudoDescs.entry.value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription[0].$;
					}
					else if(pseudoDescs.entry.value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription.length == null)
					{
						friendlyDesc = pseudoDescs.entry.value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription.$;  //find the friendly description of pseudonym
					}
				}
				else if(pseudoDescs.entry.key.$ == pseudonymUID) //to handle new format of JSON
				{
					if(pseudoDescs.entry.value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription.length > 1)
					{
						friendlyDesc = pseudoDescs.entry.value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription[0].$;
					}
					else if(pseudoDescs.entry.value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription.length == null)
					{
						friendlyDesc = pseudoDescs.entry.value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription.$;  //find the friendly description of pseudonym
					}
				}
			}
			else if(pseudoDescs.entry.length == null) //even though the return is null but there is one pseudonym
			{
				if(pseudoDescs.entry.key == pseudonymUID)
				{
					if(pseudoDescs.entry.value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription.length > 1)
					{
						friendlyDesc = pseudoDescs.entry.value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription[0].$;
					}
					else if(pseudoDescs.entry.value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription.length == null)
					{
						friendlyDesc = pseudoDescs.entry.value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription.$;  //find the friendly description of pseudonym
						//alert("here : pseudonym desc : " + friendlyDesc);
					}
				}
				else if(pseudoDescs.entry.key.$ == pseudonymUID)//testing 
				{	
					if(pseudoDescs.entry.value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription.length > 1)
					{
						friendlyDesc = pseudoDescs.entry.value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription[0].$;
					}
					else if(pseudoDescs.entry.value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription.length == null)
					{
						friendlyDesc = pseudoDescs.entry.value.PseudonymDescription.PseudonymMetadata.FriendlyPseudonymDescription.$;  //find the friendly description of pseudonym
					}	
				}
			}	
		}	
	}
	else
	{
		//alert("idSelectJSON.PseudonymDescriptions is null");
	}	
	
	return friendlyDesc;
}




/* ------------------------------------------------------------------------------------------
 * @name : parseAndembedCredential() 
 * @description : parse credential information and embed it into HTML
 * ----------------------------------------------------------------------------------------*/
function parseAndembedCredential()
{
	var credentials = idSelectJSON.CredentialDescriptions.entry;
	if(credentials.length > 1) //more than 2 credentials 
	{
		for(var i = 0; i < credentials.length; i++)
		{
			//information for HTML embedding
			var CredentialUID = credentials[i].value.CredentialUID;
			var CredentialFriendlyName = '';//null; 
			var CredentialImageReference = null; 
			var CredentialAttributeInfo = '';//null; 
			
			//FriendlyCredentialName
			if(credentials[i].value.hasOwnProperty('FriendlyCredentialName'))
			{
				if(credentials[i].value.FriendlyCredentialName.length > 1)
				{
					CredentialFriendlyName = credentials[i].value.FriendlyCredentialName[0].$;
				}
				else if(credentials[i].value.FriendlyCredentialName.length == null)
				{
					CredentialFriendlyName = credentials[i].value.FriendlyCredentialName.$;
				}	
			}		
			else // 'FriendlyCredentialName' is not existing	
			{
				CredentialFriendlyName = 'Credential'+i;
			}	
			
			//ImageReference
			if(credentials[i].value.hasOwnProperty('ImageReference'))
			{
				CredentialImageReference = credentials[i].value.ImageReference;
			}		
			else // 'FriendlyCredentialName' is not existing	
			{
				CredentialImageReference = 'default image';
			}
			
			//Attribute 
			if(credentials[i].value.hasOwnProperty('Attribute'))
			{
				var attributeArray = credentials[i].value.Attribute;
				if(attributeArray.length > 1) //there are more than 2 Attribute
				{
					for(var j = 0; j < attributeArray.length; j++)
					{
						var friendlyAttributeName = ''; 
						var friendlyAttributeValue = '';
						
						if(attributeArray[j].hasOwnProperty('AttributeDescription'))
						{
							var attributeDesc = attributeArray[j].AttributeDescription;
							if(attributeDesc.hasOwnProperty('FriendlyAttributeName'))
							{
								if(attributeArray[j].AttributeDescription.FriendlyAttributeName.length > 1)
								{
									friendlyAttributeName = attributeArray[j].AttributeDescription.FriendlyAttributeName[0].$;
								}
								else if(attributeArray[j].AttributeDescription.FriendlyAttributeName.length == null)
								{
									friendlyAttributeName = attributeArray[j].AttributeDescription.FriendlyAttributeName.$;
								}	
							}
							else
							{
								friendlyAttributeName = 'Attribute'+j;
							}	
						}
						else
						{
							alert("there is no AttributeDescription");
						}
						
						if(attributeArray[j].hasOwnProperty('AttributeValue'))
						{
							friendlyAttributeValue = attributeArray[j].AttributeValue.$;
						}
						else
						{
							alert("there is no AttributeValue");
						}	
						
						CredentialAttributeInfo = CredentialAttributeInfo + friendlyAttributeName + ' : ' + friendlyAttributeValue + '    ';
						CredentialAttributeInfo = CredentialAttributeInfo + '\n';
					}//for - attribute array	
				}
				else if(attributeArray.length == null && attributeArray != null) //only one Attribute
				{
					var friendlyAttributeName = ''; 
					var friendlyAttributeValue = '';
					
					if(attributeArray.hasOwnProperty('AttributeDescription'))
					{
						var attributeDesc = attributeArray.AttributeDescription;
						if(attributeDesc.hasOwnProperty('FriendlyAttributeName'))
						{
							if(attributeArray.AttributeDescription.FriendlyAttributeName.length > 1)
							{
								friendlyAttributeName = attributeArray.AttributeDescription.FriendlyAttributeName[0].$;
							}
							else if(attributeArray.AttributeDescription.FriendlyAttributeName.length == null)
							{
								friendlyAttributeName = attributeArray.AttributeDescription.FriendlyAttributeName.$;
							}	
						}
						else
						{
							friendlyAttributeName = 'Attribute0';
						}	
					}
					else
					{
						alert("there is no AttributeDescription");
					}
					
					if(attributeArray.hasOwnProperty('AttributeValue'))
					{
						friendlyAttributeValue = attributeArray.AttributeValue.$;
					}
					else
					{
						alert("there is no AttributeValue");
					}
					
					CredentialAttributeInfo = CredentialAttributeInfo + friendlyAttributeName + ' : ' + friendlyAttributeValue + '     ';
					CredentialAttributeInfo = CredentialAttributeInfo + '\n';
				}	
			}	
		
			embedCredentialInfo(CredentialUID, CredentialFriendlyName, CredentialImageReference, CredentialAttributeInfo);
		}//for - array of credentials
	}
	else if(credentials.length == null && credentials != null ) //only one credential exists
	{
		//information for HTML embedding
		var CredentialUID = credentials.value.CredentialUID;
		var CredentialFriendlyName = '';//null; 
		var CredentialImageReference = null; 
		var CredentialAttributeInfo = '';//null; 
		
		//FriendlyCredentialName
		if(credentials.value.hasOwnProperty('FriendlyCredentialName'))
		{
			CredentialFriendlyName = credentials.value.FriendlyCredentialName.$;
		}		
		else // 'FriendlyCredentialName' is not existing	
		{
			alert("FriendlyCredentialName is not existing");
			CredentialFriendlyName = 'Credential0';
		}	
		
		//ImageReference
		if(credentials.value.hasOwnProperty('ImageReference'))
		{
			CredentialImageReference = credentials.value.ImageReference;
		}		
		else // 'FriendlyCredentialName' is not existing	
		{
			CredentialImageReference = 'default image';
		}
		
		//Attribute 
		if(credentials.value.hasOwnProperty('Attribute'))
		{
			var attributeArray = credentials.value.Attribute;
			if(attributeArray.length > 1) //there are more than 2 Attribute
			{
				for(var j = 0; j < attributeArray.length; j++)
				{
					var friendlyAttributeName; 
					var friendlyAttributeValue;
					
					if(attributeArray[j].hasOwnProperty('AttributeDescription'))
					{
						var attributeDesc = attributeArray[j].AttributeDescription;
						if(attributeDesc.hasOwnProperty('FriendlyAttributeName'))
						{
							if(attributeArray[j].AttributeDescription.FriendlyAttributeName.length > 1)
							{
								friendlyAttributeName = attributeArray[j].AttributeDescription.FriendlyAttributeName[0].$;
							}		
							else if(attributeArray[j].AttributeDescription.FriendlyAttributeName.length == null)
							{
								friendlyAttributeName = attributeArray[j].AttributeDescription.FriendlyAttributeName.$;
								alert(friendlyAttributeName);
							}	
						}
						else
						{
							friendlyAttributeName = 'Attribute'+j;
						}	
					}
					else
					{
						alert("there is no AttributeDescription");
					}
					
					if(attributeArray[j].hasOwnProperty('AttributeValue'))
					{
						friendlyAttributeValue = attributeArray[j].AttributeValue.$;
					}
					else
					{
						alert("there is no AttributeValue");
					}
					
					CredentialAttributeInfo = CredentialAttributeInfo + friendlyAttributeName + ' : ' + friendlyAttributeValue + '     ';
					CredentialAttributeInfo = CredentialAttributeInfo + '\n';
				}	
			}
			else if(attributeArray.length == null && attributeArray != null) //only one Attribute
			{
				var friendlyAttributeName =''; 
				var friendlyAttributeValue = '';
				
				if(attributeArray.hasOwnProperty('AttributeDescription'))
				{
					var attributeDesc = attributeArray.AttributeDescription;
					if(attributeDesc.hasOwnProperty('FriendlyAttributeName'))
					{
						if(attributeArray.AttributeDescription.FriendlyAttributeName.length > 1)
						{
							friendlyAttributeName = attributeArray.AttributeDescription.FriendlyAttributeName[0].$;
						}
						else if(attributeArray.AttributeDescription.FriendlyAttributeName.length == null)
						{
							friendlyAttributeName = attributeArray.AttributeDescription.FriendlyAttributeName.$;
						}	
					}
					else
					{
						friendlyAttributeName = 'Attribute0';
					}	
				}
				else
				{
					alert("there is no AttributeDescription");
				}
				
				if(attributeArray.hasOwnProperty('AttributeValue'))
				{
					friendlyAttributeValue = attributeArray.AttributeValue.$;
				}
				else
				{
					alert("there is no AttributeValue");
				}
				
				CredentialAttributeInfo = CredentialAttributeInfo + friendlyAttributeName + ' : ' + friendlyAttributeValue + '     ';
				CredentialAttributeInfo = CredentialAttributeInfo + '\n';
			}	
		}	
		
		embedCredentialInfo(CredentialUID, CredentialFriendlyName, CredentialImageReference, CredentialAttributeInfo);
	}//end of case when only one credential
}



/* ---------------------------------------------------------------------------------------------------------------------
 * @name : embedCredentialInfo(CredentialUID, CredentialFriendlyName, CredentialImageReference, CredentialAttributeInfo)
 * @description : embed credential information into HTML 
 * -------------------------------------------------------------------------------------------------------------------*/
function embedCredentialInfo(CredentialUID, CredentialFriendlyName, CredentialImageReference, CredentialAttributeInfo)
{
	var credentialPaneNode = selectionUIWindow.document.getElementById('credentialPane');
	var credentialPane = selectionUIWindow.document.createElement('input'); 

	credentialPane.setAttribute('id', CredentialUID);
	credentialPane.setAttribute('type', 'hidden');
	credentialPane.setAttribute('name', CredentialFriendlyName);
	credentialPane.setAttribute('src', CredentialImageReference);
	credentialPane.setAttribute('value', CredentialAttributeInfo);
	credentialPane.setAttribute('title', CredentialAttributeInfo);
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



/* ----------------------------------------------------------------------------------------
 * @name : _parsePseudonum(selectionUIWindow, testJSONinput)
 * @description : parse Pseudonym 
 * @deprecated
 * --------------------------------------------------------------------------------------*/
function _parsePseudonym(selectionUIWindow, idSelectJSON)
{
	var pseudonyms = idSelectJSON.pseudonyms; 
	if(pseudonyms != null)
	{
		var pseudonym_entry = pseudonyms.entry; 
		
		if(pseudonym_entry != null && pseudonym_entry.length == null) 
		{
			var key = pseudonym_entry.key;
			if(key != null)
			{
				//put it into pseudonym HTML element 
				var value = pseudonym_entry.value;
				var value_pseudonym = value.Pseudonym;
				var value_pseudonymMetadata = value.PseudonymMetadata;
				var exclusive = value_pseudonym.@Exclusive;
				var scope = value_pseudonym.@Scope;
				var pseudonymUID = value_pseudonym.@PseudonymUID;
				var pseudonymIndex = 0; 
				var numPseudonyms = 1; 
			
				//Add parsed Pseudonym into SelectionUI.html
				var pseudonymPaneNode = selectionUIWindow.document.getElementById('PseudonymPane');
				if(pseudonymPaneNode != null)
				{
					// Add Pseudonym ContentPane
					addParsedPseudonymIntoContentPane(pseudonymPaneNode, pseudonymIndex, key, exclusive, scope);
				}
				else
				{
					alert("pseudonymPane is null");
				}	
			}	
			else 
			{
				alert("Pseudonym is null");
			}	
		}
		else if( pseudonym_entry != null && pseudonym_entry.length > 1 )//Pseudonym is more than one 
		{
			var pseudonymPaneNode = selectionUIWindow.document.getElementById('PseudonymPane');
			if(pseudonymPaneNode != null)
			{
				for(var pseudonymIndex = 0; pseudonymIndex < pseudonym_entry.length; pseudonymIndex++)
				{
					if(pseudonym_entry[pseudonymIndex].key != null)
					{
						var p_value = pseudonym_entry[pseudonymIndex].value;
						if(p_value != null)
						{
							var key = pseudonym_entry[pseudonymIndex].key;
							var exclusive = p_value.Pseudonym.@Exclusive; 
							var scope = p_value.Pseudonym.@Scope;
							var pseudonymUID = p_value.Pseudonym.@Pseudonym;
						
							// Add Pseudonym ContentPane
							addParsedPseudonymIntoContentPane(pseudonymPaneNode, pseudonymIndex, key, exclusive, scope);
						}
						else 
						{
							alert("[DEBUG INFO] pseudonymPaneNode is null!");	
						}	
					} //if(pseudonym_entry[pseudonymIndex].key != null)
					else
					{
						alert("[DEBUG INFO] pseudonym_entry[pi].value!");
					}	
				}//for 
			}	
			else
			{
				alert("[DEBUG INFO] pseudonym_entry[pi].key!");
			}	
		}	
	}
	else //pseudonym 
	{
		alert("pseudonym is null");
	}	
}



/* --------------------------------------------------------------------------------------------------
 * @name : addParsedPseudonymIntoContentPane(pseudonymIndex, pseudonymPane, key, exclusive, scope)
 * @description : Adding parsed pseudonym into content pane
 * ------------------------------------------------------------------------------------------------*/
function addParsedPseudonymIntoContentPane(pseudonymPaneNode, pseudonymIndex, key, exclusive, scope)
{
	/*
	 * Pseudonym Contents 
	 */
	var pseudonymPane = selectionUIWindow.document.createElement('p'); 
	pseudonymPane.setAttribute('id', key); 
	pseudonymPane.setAttribute('align', 'left');
	pseudonymPaneNode.appendChild(pseudonymPane);
	
	var newPseudonymPane = selectionUIWindow.document.createElement('div');
	newPseudonymPane.setAttribute('dojoType', 'dijit.layout.ContentPane');
	newPseudonymPane.setAttribute('style', 'background-color:#E6E6E6; width:590px; height:150px; margin-top:3px; margin-left:2px');
	newPseudonymPane.setAttribute('id', pseudonymIndex);
	newPseudonymPane.setAttribute('align', 'center');
	
	newPseudonymPane.appendChild(selectionUIWindow.document.createTextNode('exclusive : ' + exclusive ));
	newPseudonymPane.appendChild(selectionUIWindow.document.createElement('br'));
	newPseudonymPane.appendChild(selectionUIWindow.document.createTextNode('scope : ' + scope ));
	newPseudonymPane.appendChild(selectionUIWindow.document.createElement('br'));
	newPseudonymPane.appendChild(selectionUIWindow.document.createTextNode(' pseudonymUID : ' + key));

	pseudonymPane.appendChild(newPseudonymPane);
	
	/*
	 * Add Button for Pseudonym ContentPane
	 */
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



/* ------------------------------------------------------------------------------------------------------------
 * @name : (selectionUIWindow, testJSONinput)
 * @description : parse Candidate Presentation Token
 * ----------------------------------------------------------------------------------------------------------*/
function parseCandidatePresentationToken(selectionUIWindow, idSelectJSON, sizeOfCandidatePresentationTokenList)
{
	var tokenNode = selectionUIWindow.document.getElementById('candidateTokens');
	for( var i = 0; i < sizeOfCandidatePresentationTokenList ; i++)
	{
		var currentToken = idSelectJSON.CandidatePresentationTokenList.CandidatePresentationToken[i];
		var tokenInfo = null;
		var tokenPolicyUID = currentToken.Token["@PolicyUID"]; 
		var tokenUID = currentToken.Token["@TokenUID"];  
		var policyUID = currentToken.Token["@PolicyUID"];//.@PolicyUID;
		var credentialUids; 

		if(currentToken.CredentialUidList != null)
		{	
			credentialUids = currentToken.CredentialUidList.CredentialUid; 
			
			if(currentToken.FriendlyTokenDescription.length > 1)
			{
				tokenInfo = currentToken.FriendlyTokenDescription[0].$; 
			}
			else if(currentToken.FriendlyTokenDescription.length == null)
			{
				tokenInfo = currentToken.FriendlyTokenDescription.$; 
			}	
			
			//------------------------------------
			//  save policyUID for current token
			//------------------------------------
			var policyUIDforToken = selectionUIWindow.document.createElement('input');
			policyUIDforToken.setAttribute('type', 'hidden');
			policyUIDforToken.setAttribute('id', i+'th_token');
			policyUIDforToken.setAttribute('name', 'token');
			policyUIDforToken.setAttribute('value', tokenUID);
			policyUIDforToken.setAttribute('policyUID', policyUID );
			policyUIDforToken.setAttribute('totalNumToken', sizeOfCandidatePresentationTokenList);
			policyUIDforToken.setAttribute('tokenIndex', i);
			
			tokenNode.appendChild(policyUIDforToken);
		
			//-----------------------------------
			// save credential info for token
			//-----------------------------------
			var numCredentialsInToken = getNumCredentialsInToken(currentToken);
			
			if( numCredentialsInToken > 1 ) 
			{
				for( var j = 0; j < numCredentialsInToken; j++ )
				{
					var currentCredential = credentialUids[j];
					for( var credentialAttribute in currentCredential )
					{
						//already saved about token info
						//tokenInfo = tokenInfo + credentialAttribute + " : " + currentCredential[credentialAttribute] + "   ";
					}
				}
				
				//get all credentials in the current token
				var allCredentialsInToken = "";
				var numCredentialsInToken = 0; 
				var credentialUidList = currentToken.CredentialUidList; 
			
				//placeholder for token in selection UI window
				var hiddenToken = selectionUIWindow.document.createElement('input');
				hiddenToken.setAttribute('type', 'hidden');
				hiddenToken.setAttribute('id', tokenUID); 
				hiddenToken.setAttribute('value', tokenInfo); 
	
				if(credentialUidList != null)
				{
					if(credentialUidList.CredentialUid.length > 0)
					{
						numCredentialsInToken = credentialUidList.CredentialUid.length;
						
						if(numCredentialsInToken > 1) //credentials are array
						{	
							hiddenToken.setAttribute('numCredentials', numCredentialsInToken); 
												
							for( var k = 0; k < credentialUidList.CredentialUid.length; k++ )
							{
								var credentialForToken = selectionUIWindow.document.createElement('li');
								credentialForToken.setAttribute('type', 'hidden');
								credentialForToken.setAttribute('id', 'credential_'+k+'_in_Token'+i);
								credentialForToken.setAttribute('name', 'credential' );
								credentialForToken.setAttribute('value', credentialUidList.CredentialUid[k]);
								credentialForToken.appendChild(selectionUIWindow.document.createTextNode(credentialUidList.CredentialUid[k]));
				
								hiddenToken.appendChild(credentialForToken);
							}	
						}
						else //there is only one credential
						{
							hiddenToken.setAttribute('numCredentials', 1); //also set number of credential in token
							
							var credentialForToken = selectionUIWindow.document.createElement('li');
							credentialForToken.setAttribute('type', 'hidden');
							credentialForToken.setAttribute('id', 'credential_0_in_'+tokenUID);
							credentialForToken.setAttribute('name', 'credential' );
							credentialForToken.setAttribute('value', credentialUidList.CredentialUid);
							credentialForToken.appendChild(selectionUIWindow.document.createTextNode(credentialUidList.CredentialUid));
			
							hiddenToken.appendChild(credentialForToken);
						}	
					}
				}
				else //if(credentialUidList == null) 
				{
					var credentialForToken = selectionUIWindow.document.createElement('li');
					credentialForToken.setAttribute('type', 'hidden');
					credentialForToken.setAttribute('id', 'credential_null_in'+tokenUID);
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
				if(credentialUidList != null)
				{
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
				tokenInfo = "there is no corresponding token";
				
				//placeholder for token in selection UI window
				var hiddenToken = selectionUIWindow.document.createElement('input');
				hiddenToken.setAttribute('type', 'hidden');
				hiddenToken.setAttribute('id', tokenUID); 
				hiddenToken.setAttribute('value', tokenInfo); 
				hiddenToken.setAttribute('numCredentials', 0); //also set number of credential in token
				
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
					credentialForToken.setAttribute('value', 'no credential for this token');
					credentialForToken.appendChild(selectionUIWindow.document.createTextNode('no credential'));
		
					hiddenToken.appendChild(credentialForToken);
				}
			
				tokenNode.appendChild(hiddenToken);
				
			}
			//-----
		}
		else if(currentToken.CredentialUidList == null)// handle currentToken.CredentialUidList is null
		{
			//------------------------------------
			//  save policyUID for current token
			//------------------------------------
			var policyUIDforToken = selectionUIWindow.document.createElement('input');
			policyUIDforToken.setAttribute('type', 'hidden');
			policyUIDforToken.setAttribute('id', i+'th_token');
			policyUIDforToken.setAttribute('name', 'token');
			policyUIDforToken.setAttribute('value', tokenUID);
			policyUIDforToken.setAttribute('policyUID', policyUID );
			policyUIDforToken.setAttribute('totalNumToken', sizeOfCandidatePresentationTokenList);
			
			tokenNode.appendChild(policyUIDforToken);
			    
			//-----------------------------------
			// save credential info for token
			//-----------------------------------
			var numCredentialsInToken = getNumCredentialsInToken(currentToken);
			
			if( numCredentialsInToken == 0) //there is no credentials in token
			{
				tokenInfo = "null"; //"there is no corresponding token";
				
				//placeholder for token in selection UI window
				var hiddenToken = selectionUIWindow.document.createElement('input');
				hiddenToken.setAttribute('type', 'hidden');
				hiddenToken.setAttribute('id', tokenUID); 
				hiddenToken.setAttribute('value', tokenInfo); 
				hiddenToken.setAttribute('numCredentials', 0); //also set number of credential in token
				
				//get all credentials in the current token
				var allCredentialsInToken = "";
				var numCredentialsInToken = 0; 
				var credentialUidList = currentToken.CredentialUidList; 
		
				if(credentialUidList == null)
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
		}	
	}
}




/* --------------------------------------------------------------------------------------------------------------------
 * @name :  parseOneCandidatePresentationToken(selectionUIWindow, testJSONinput, sizeOfCandidatePresentationTokenList)
 * @description : parse Candidate Presentation Token in case only one token exist
 * ------------------------------------------------------------------------------------------------------------------*/
function parseOneCandidatePresentationToken(selectionUIWindow, idSelectJSON, sizeOfCandidatePresentationTokenList)
{
	var tokenNode = selectionUIWindow.document.getElementById('candidateTokens');

	var currentToken = idSelectJSON.CandidatePresentationTokenList.CandidatePresentationToken;
	var tokenInfo = "";

	var tokenPolicyUID = currentToken.Token["@PolicyUID"];//.@PolicyUID;
	var tokenUID = currentToken.Token["@TokenUID"];//.@TokenUID;
	var policyUID = currentToken.Token["@PolicyUID"];//.@PolicyUID;
	var credentialUids = currentToken.CredentialUidList.CredentialUid; 
	
	//token information to be shown in the UI
	if(currentToken.FriendlyTokenDescription.length > 1)
	{
		tokenInfo = currentToken.FriendlyTokenDescription[0].$; 
	}
	else if(currentToken.FriendlyTokenDescription.length == null)
	{
		tokenInfo = currentToken.FriendlyTokenDescription.$; 
	}	
	
	//------------------------------------
	//  save policyUID for current token
	//------------------------------------
	var policyUIDforToken = selectionUIWindow.document.createElement('input');
	policyUIDforToken.setAttribute('type', 'hidden');
		
	policyUIDforToken.setAttribute('id', 'only_token');
	policyUIDforToken.setAttribute('value', tokenUID);
	policyUIDforToken.setAttribute('policyUID', policyUID );
	policyUIDforToken.setAttribute('totalNumToken', 1);

	tokenNode.appendChild(policyUIDforToken);
	
	//Get number of credentials in the current token 
	var numCredentialsInToken = getNumCredentialsInToken(currentToken);
	
	if( numCredentialsInToken > 1 ) 
	{
		for( var j = 0; j < numCredentialsInToken; j++ )
		{
			var currentCredential = credentialUids[j];
			
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
	
		var credentialUidList = currentToken.CredentialUidList; 
		if(credentialUidList != null)
		{
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
		tokenInfo = "there is no corresponding token";
		
		//placeholder for token in selection UI window
		var hiddenToken = selectionUIWindow.document.createElement('input');
		hiddenToken.setAttribute('type', 'hidden');
		hiddenToken.setAttribute('id', tokenUID); 
		hiddenToken.setAttribute('value', tokenInfo); 
	
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
		else //credentialUidList == null
		{
			var credentialForToken = selectionUIWindow.document.createElement('li');
			credentialForToken.setAttribute('type', 'hidden');
			credentialForToken.setAttribute('id', 'credential_null_in_'+tokenUID);
			credentialForToken.setAttribute('name', 'credential' );
			credentialForToken.setAttribute('value', 'no credential for this token');
			credentialForToken.appendChild(selectionUIWindow.document.createTextNode('no credential'));

			hiddenToken.appendChild(credentialForToken);
		}
	
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
    
	if( credentialUidList == null )
	{
		numCredentialsInToken = 0; 
	}
	else 
	{	
		if(credentialUidList.CredentialUid == null)
		{
			alert("CredentialUidList.CredentialUid is null");
		}
		else 
		{
			if(credentialUidList.CredentialUid.length > 0)
			{
				if(credentialUidList.CredentialUid.length == 36)
				{
					numCredentialsInToken = 1; 
				}
				else 
				{
					numCredentialsInToken = credentialUidList.CredentialUid.length;
				}	
			}	
		}	
	}	
	
	return numCredentialsInToken; 
}



/* ------------------------------------------------------------------------------------
 * @name : _getNumCredentialsInToken(token) 
 * @description : get the number of credentials belongs to the token
 * ! Deprecate : Because JSON format changed (19. April) 
 * -----------------------------------------------------------------------------------*/
function _getNumCredentialsInToken(currentToken)
{
	var numCredentialsInToken = 0; 
	var credentials = currentToken.Token.Credential; 
	
	if( credentials == null )
	{
		//check for the 'CredentialUidList' is null or not. 
		var credentialUidList = currentToken.CredentialUidList; 
		if(credentialUidList == null)
		{
			//alert("credentialUidList is null");
		}
		
		numCredentialsInToken = 0; 
	}
	else 
	{	
		if( credentials.length > 0 ) //there are more than 1 credential, i.e. array of credentials
		{
			numCredentialsInToken = credentials.length;
		}
		else   //there is only one credential 
		{
			if(credentials.@Alias != null)
			{
				numCredentialsInToken = 1; 
			}	
		}
	}	
	
	return numCredentialsInToken; 
}



/* -----------------------------------------------------------------------------------------------
 * @name : getSizeCandidatePresentationTokenList(testJSONinput) 
 * @description : get the number of candidate presentation tokens 
 * ---------------------------------------------------------------------------------------------*/
function getSizeCandidatePresentationTokenList(idSelectJSON)
{
	var arrayOfCandidateToken = idSelectJSON.CandidatePresentationTokenList.CandidatePresentationToken;
	
	if( arrayOfCandidateToken.length > 0) //more than one presentation token exists
	{
		return arrayOfCandidateToken.length;
	}
	else if(arrayOfCandidateToken.length == null && idSelectJSON.CandidatePresentationTokenList.CandidatePresentationToken != null )
	{
		return 1; 
	}
	else 
		return 0;
}



/* ---------------------------------------------------------------------------------------------
 * @name : addContentPane(numberOfCredentials)
 * @description : Adding contentPane which contains radio Button
 * --------------------------------------------------------------------------------------------*/
function addContentPane(numberOfCredentials, selectionUIWindow, idSelectJSON)
{
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
			var elementID = 'credential'+i;
			var credentialIndex = i; 
			var credentialPane = selectionUIWindow.document.createElement('p'); 
			credentialPane.setAttribute('id', elementID);
			credentialPaneNode.appendChild(credentialPane);
		
			if(numberOfCredentials > 1) //which is array of credentials 
			{	
				var node = addCredentialDescriptionContentPane(elementID, credentialIndex, selectionUIWindow, idSelectJSON);
				var credentialUID = idSelectJSON.credentialDescriptions.entry[credentialIndex].value.CredentialUID;
				addRadioButton(node, credentialIndex, selectionUIWindow, idSelectJSON, credentialUID);
			}
			else if(numberOfCredentials == 1) 
			{
				var node = addOneCredentialDescriptionContentPane(elementID, credentialIndex, selectionUIWindow, idSelectJSON);
				var credentialUID = idSelectJSON.credentialDescriptions.entry.value.CredentialUID;
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
		var credentialDescription_rval = idSelectJSON.credentialDescriptions; //rval means 'right value'. key : value
		if( credentialDescription_rval != null )
		{	
			if(credentialDescription_rval.hasOwnProperty('entry'))
			{
				var entry_rval = credentialDescription_rval.entry;
				if( entry_rval != null )
				{
					for( var x in entry_rval ) //in case entry_rval is an array of object, x is index of array
					{
						if( x == 'key' ) //case 1 : there is only one credentail so x is property of entry_rval 'key'
						{
							numberOfCredentials = 1; 
							return numberOfCredentials;
						}
						else  //case 2 : there are more than one credential which is entry_rval is array, x is index
						{
							if( entry_rval[x].hasOwnProperty('key') )
							{
								numberOfCredentials = numberOfCredentials + 1;
							}
						}	
					}
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



/* --------------------------------------------------------------------
 * @name : getTestJSON1()
 * @description : getting test data 'Hotel_Policy_Not_Student.json'
 * -------------------------------------------------------------------*/
function getTestJSON1()
{
	var idselectionJSON = 
	{
			    "credentialDescriptions": {
			        "entry": [
			            {
			                "key": "704eea8d-c148-42cb-bfb6-3dd1328d87d9",
			                "value": {
			                    "CredentialUID": "704eea8d-c148-42cb-bfb6-3dd1328d87d9",
			                    "CredentialSpecificationUID": "http://amex.com/amexcard/specification",
			                    "IssuerParametersUID": "http://www.amex.com/abc/isskey",
			                    "Attribute": [
			                        {
			                            "AttributeUID": "lkadsfijg8itnf10",
			                            "AttributeDescription": {
			                                "@Type": "http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
			                                "@DataType": "xs:anyURI",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "http://visa.com/creditcard/revocation/parameters"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf11",
			                            "AttributeDescription": {
			                                "@Type": "CardType",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Gold"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf12",
			                            "AttributeDescription": {
			                                "@Type": "Name",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Alice"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf13",
			                            "AttributeDescription": {
			                                "@Type": "LastName",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Nextdoor"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf14",
			                            "AttributeDescription": {
			                                "@Type": "CardNumber",
			                                "@DataType": "xs:integer",
			                                "@Encoding": "int"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:int",
			                                "$": "42"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf15",
			                            "AttributeDescription": {
			                                "@Type": "ExpirationDate",
			                                "@DataType": "xs:date",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "2014-02-01+01:00"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf16",
			                            "AttributeDescription": {
			                                "@Type": "SecurityCode",
			                                "@DataType": "xs:integer",
			                                "@Encoding": "int"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:int",
			                                "$": "42"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf17",
			                            "AttributeDescription": {
			                                "@Type": "Status",
			                                "@DataType": "xs:anyURI",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "status"
			                            }
			                        }
			                    ]
			                }
			            },
			            {
			                "key": "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
			                "value": {
			                    "CredentialUID": "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
			                    "CredentialSpecificationUID": "http://www.admin.ch/passport/specification",
			                    "IssuerParametersUID": "http://www.admin.ch/passport/issuancekey_v1.0",
			                    "Attribute": [
			                        {
			                            "AttributeUID": "lkadsfijg8itnf10",
			                            "AttributeDescription": {
			                                "@Type": "http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
			                                "@DataType": "xs:anyURI",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "http://admin.ch/passport/revocation/parameters"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf11",
			                            "AttributeDescription": {
			                                "@Type": "Name",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Alice"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf12",
			                            "AttributeDescription": {
			                                "@Type": "LastName",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Nextdoor"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf13",
			                            "AttributeDescription": {
			                                "@Type": "PassportNumber",
			                                "@DataType": "xs:integer",
			                                "@Encoding": "int"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:int",
			                                "$": "42"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf14",
			                            "AttributeDescription": {
			                                "@Type": "Issued",
			                                "@DataType": "xs:date",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "2011-02-01+01:00"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf15",
			                            "AttributeDescription": {
			                                "@Type": "Expires",
			                                "@DataType": "xs:date",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "2014-02-01+01:00"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf16",
			                            "AttributeDescription": {
			                                "@Type": "IssuedBy",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "service_issuer_integration_test"
			                            }
			                        }
			                    ]
			                }
			            },
			            {
			                "key": "24d4e9eb-9fbf-4e03-8044-37c3810eaa35",
			                "value": {
			                    "CredentialUID": "24d4e9eb-9fbf-4e03-8044-37c3810eaa35",
			                    "CredentialSpecificationUID": "http://visa.com/creditcard/specification",
			                    "IssuerParametersUID": "http://thebestbank.com/cc/issuancekey_v1.0",
			                    "Attribute": [
			                        {
			                            "AttributeUID": "lkadsfijg8itnf10",
			                            "AttributeDescription": {
			                                "@Type": "http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
			                                "@DataType": "xs:anyURI",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "http://visa.com/creditcard/revocation/parameters"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf11",
			                            "AttributeDescription": {
			                                "@Type": "CardType",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Gold"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf12",
			                            "AttributeDescription": {
			                                "@Type": "Name",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Alice"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf13",
			                            "AttributeDescription": {
			                                "@Type": "LastName",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Nextdoor"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf14",
			                            "AttributeDescription": {
			                                "@Type": "CardNumber",
			                                "@DataType": "xs:integer",
			                                "@Encoding": "int"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:int",
			                                "$": "42"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf15",
			                            "AttributeDescription": {
			                                "@Type": "ExpirationDate",
			                                "@DataType": "xs:date",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "2014-02-01+01:00"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf16",
			                            "AttributeDescription": {
			                                "@Type": "SecurityCode",
			                                "@DataType": "xs:integer",
			                                "@Encoding": "int"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:int",
			                                "$": "42"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf17",
			                            "AttributeDescription": {
			                                "@Type": "Status",
			                                "@DataType": "xs:anyURI",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "status"
			                            }
			                        }
			                    ]
			                }
			            }
			        ]
			    },
			    "pseudonyms": {
			        "entry": {
			            "key": "pseudonym/57hj3bet9rsmx5gz",
			            "value": {
			                "Pseudonym": {
			                    "@Exclusive": "false",
			                    "@Scope": "http://www.sweetdreamsuites.com",
			                    "@PseudonymUID": "pseudonym/57hj3bet9rsmx5gz"
			                }
			            }
			        }
			    },
			    "CandidatePresentationTokenList": {
			        "CandidatePresentationToken": [
			            {
			                "token": {
			                    "@PolicyUID": "http://www.sweetdreamsuites.com/policies/booking/standard",
			                    "@TokenUID": "abc4trust.eu/token-uid/4m4xbuf6lf4mmbol",
			                    "Message": {
			                        "Nonce": "SGVsbG9Xb3JsZAo=",
			                        "ApplicationData": "RoomType : Double\nBedsize : King\nArrivalDate : 2012-04-01\nNrOfNights :  2\nReservationCode : HCJ095\nI agree to the terms of service and cancellation policy."
			                    },
			                    "Pseudonym": {
			                        "@Exclusive": "false",
			                        "@Scope": "http://www.sweetdreamsuites.com",
			                        "@Alias": "#mainPseudonym",
			                        "PseudonymValue": "Kis="
			                    },
			                    "Credential": [
			                        {
			                            "@Alias": "#passport",
			                            "CredentialSpecUID": "http://www.admin.ch/passport/specification",
			                            "IssuerParametersUID": "http://www.admin.ch/passport/issuancekey_v1.0",
			                            "RevocationInformationUID": "http://admin.ch/passport/revocation/parameters"
			                        },
			                        {
			                            "@Alias": "#creditcard",
			                            "CredentialSpecUID": "http://visa.com/creditcard/specification",
			                            "IssuerParametersUID": "http://thebestbank.com/cc/issuancekey_v1.0",
			                            "DisclosedAttribute": {
			                                "@AttributeType": "CardNumber",
			                                "@DataHandlingPolicy": "http://www.sweetdreamsuites.com/policies/creditcards",
			                                "InspectorPublicKeyUID": "http://admin.ch/inspector/pub_key_v1",
			                                "InspectionGrounds": "In case of no free cancellation and no show the credit card number should be Disclosed to the hotel."
			                            }
			                        }
			                    ],
			                    "AttributePredicate": {
			                        "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal",
			                        "Attribute": {
			                            "@CredentialAlias": "#creditcard",
			                            "@AttributeType": "ExpirationDate"
			                        },
			                        "ConstantValue": "2012-01-06Z"
			                    }
			                },
			                "CredentialUidList": {
			                    "CredentialUid": [
			                        "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
			                        "24d4e9eb-9fbf-4e03-8044-37c3810eaa35"
			                    ]
			                },
			                "PseudonymChoiceList": {
			                    "PseudonymChoice": null
			                },
			                "InspectorChoiceList": {
			                    "InspectorChoice": null
			                }
			            },
			            {
			                "token": {
			                    "@PolicyUID": "http://www.sweetdreamsuites.com/policies/booking/standard",
			                    "@TokenUID": "abc4trust.eu/token-uid/2i4e8u4lxiccbkbb",
			                    "Message": {
			                        "Nonce": "SGVsbG9Xb3JsZAo=",
			                        "ApplicationData": "RoomType : Double\nBedsize : King\nArrivalDate : 2012-04-01\nNrOfNights :  2\nReservationCode : HCJ095\nI agree to the terms of service and cancellation policy."
			                    },
			                    "Pseudonym": {
			                        "@Exclusive": "false",
			                        "@Scope": "http://www.sweetdreamsuites.com",
			                        "@Alias": "#mainPseudonym"
			                    },
			                    "Credential": [
			                        {
			                            "@Alias": "#passport",
			                            "CredentialSpecUID": "http://www.admin.ch/passport/specification",
			                            "IssuerParametersUID": "http://www.admin.ch/passport/issuancekey_v1.0",
			                            "RevocationInformationUID": "http://admin.ch/passport/revocation/parameters"
			                        },
			                        {
			                            "@Alias": "#creditcard",
			                            "CredentialSpecUID": "http://amex.com/amexcard/specification",
			                            "IssuerParametersUID": "http://www.amex.com/abc/isskey",
			                            "DisclosedAttribute": {
			                                "@AttributeType": "CardNumber",
			                                "@DataHandlingPolicy": "http://www.sweetdreamsuites.com/policies/creditcards",
			                                "InspectorPublicKeyUID": "",
			                                "InspectionGrounds": "In case of no free cancellation and no show the credit card number should be Disclosed to the hotel."
			                            }
			                        }
			                    ],
			                    "AttributePredicate": {
			                        "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal",
			                        "Attribute": {
			                            "@CredentialAlias": "#creditcard",
			                            "@AttributeType": "ExpirationDate"
			                        },
			                        "ConstantValue": "2012-01-06Z"
			                    }
			                },
			                "CredentialUidList": {
			                    "CredentialUid": [
			                        "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
			                        "704eea8d-c148-42cb-bfb6-3dd1328d87d9"
			                    ]
			                },
			                "PseudonymChoiceList": {
			                    "PseudonymChoice": null
			                },
			                "InspectorChoiceList": {
			                    "InspectorChoice": null
			                }
			            }
			        ]
			    }
	};

	return idselectionJSON;
}






/* --------------------------------------------------------------------
 * @name : getTestJSON2()
 * @description : getting test data 'Hotel_Policy_Student.json'
 * -------------------------------------------------------------------*/
function getTestJSON2()
{
	//alert("now in getTestJSON");
	
	var idselectionJSON = 
	{
		    "credentialDescriptions": {
		        "entry": [
		            {
		                "key": "a677febd-8f28-4688-9eb6-0133cc2be072",
		                "value": {
		                    "CredentialUID": "a677febd-8f28-4688-9eb6-0133cc2be072",
		                    "CredentialSpecificationUID": "http://www.ethz.ch/studentid/specification",
		                    "IssuerParametersUID": "http://www.ethz.ch/studentid/issuancekey_v1.0",
		                    "Attribute": [
		                        {
		                            "AttributeUID": "lkadsfijg8itnf10",
		                            "AttributeDescription": {
		                                "@Type": "http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
		                                "@DataType": "xs:anyURI",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "http://www.ethz.ch/studentid/revocation/parameters"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf11",
		                            "AttributeDescription": {
		                                "@Type": "Name",
		                                "@DataType": "xs:string",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Stewart"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf12",
		                            "AttributeDescription": {
		                                "@Type": "LastName",
		                                "@DataType": "xs:string",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Dent"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf13",
		                            "AttributeDescription": {
		                                "@Type": "StudentNumber",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "int"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:int",
		                                "$": "1"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf14",
		                            "AttributeDescription": {
		                                "@Type": "Issued",
		                                "@DataType": "xs:date",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "2012-02-01+01:00"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf15",
		                            "AttributeDescription": {
		                                "@Type": "Expires",
		                                "@DataType": "xs:date",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "2015-02-01+01:00"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf16",
		                            "AttributeDescription": {
		                                "@Type": "IssuedBy",
		                                "@DataType": "xs:string",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "IssuedBy"
		                            }
		                        }
		                    ]
		                }
		            },
		            {
		                "key": "704eea8d-c148-42cb-bfb6-3dd1328d87d9",
		                "value": {
		                    "CredentialUID": "704eea8d-c148-42cb-bfb6-3dd1328d87d9",
		                    "CredentialSpecificationUID": "http://amex.com/amexcard/specification",
		                    "IssuerParametersUID": "http://www.amex.com/abc/isskey",
		                    "Attribute": [
		                        {
		                            "AttributeUID": "lkadsfijg8itnf10",
		                            "AttributeDescription": {
		                                "@Type": "http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
		                                "@DataType": "xs:anyURI",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "http://amex.com/amexcard/revocation/parameters"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf11",
		                            "AttributeDescription": {
		                                "@Type": "CardType",
		                                "@DataType": "xs:string",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Normal"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf12",
		                            "AttributeDescription": {
		                                "@Type": "Name",
		                                "@DataType": "xs:string",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Stewart"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf13",
		                            "AttributeDescription": {
		                                "@Type": "LastName",
		                                "@DataType": "xs:string",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Dent"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf14",
		                            "AttributeDescription": {
		                                "@Type": "CardNumber",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "int"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:int",
		                                "$": "1"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf15",
		                            "AttributeDescription": {
		                                "@Type": "ExpirationDate",
		                                "@DataType": "xs:date",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "2015-02-01+01:00"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf16",
		                            "AttributeDescription": {
		                                "@Type": "SecurityCode",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "int"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:int",
		                                "$": "1"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf17",
		                            "AttributeDescription": {
		                                "@Type": "Status",
		                                "@DataType": "xs:anyURI",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "status"
		                            }
		                        }
		                    ]
		                }
		            },
		            {
		                "key": "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
		                "value": {
		                    "CredentialUID": "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
		                    "CredentialSpecificationUID": "http://www.admin.ch/passport/specification",
		                    "IssuerParametersUID": "http://www.admin.ch/passport/issuancekey_v1.0",
		                    "Attribute": [
		                        {
		                            "AttributeUID": "lkadsfijg8itnf10",
		                            "AttributeDescription": {
		                                "@Type": "http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
		                                "@DataType": "xs:anyURI",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "http://admin.ch/passport/revocation/parameters"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf11",
		                            "AttributeDescription": {
		                                "@Type": "Name",
		                                "@DataType": "xs:string",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Stewart"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf12",
		                            "AttributeDescription": {
		                                "@Type": "LastName",
		                                "@DataType": "xs:string",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Dent"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf13",
		                            "AttributeDescription": {
		                                "@Type": "PassportNumber",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "int"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:int",
		                                "$": "1"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf14",
		                            "AttributeDescription": {
		                                "@Type": "Issued",
		                                "@DataType": "xs:date",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "2012-02-01+01:00"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf15",
		                            "AttributeDescription": {
		                                "@Type": "Expires",
		                                "@DataType": "xs:date",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "2015-02-01+01:00"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf16",
		                            "AttributeDescription": {
		                                "@Type": "IssuedBy",
		                                "@DataType": "xs:string",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "service_issuer_integration_test"
		                            }
		                        }
		                    ]
		                }
		            },
		            {
		                "key": "24d4e9eb-9fbf-4e03-8044-37c3810eaa35",
		                "value": {
		                    "CredentialUID": "24d4e9eb-9fbf-4e03-8044-37c3810eaa35",
		                    "CredentialSpecificationUID": "http://visa.com/creditcard/specification",
		                    "IssuerParametersUID": "http://thebestbank.com/cc/issuancekey_v1.0",
		                    "Attribute": [
		                        {
		                            "AttributeUID": "lkadsfijg8itnf10",
		                            "AttributeDescription": {
		                                "@Type": "http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
		                                "@DataType": "xs:anyURI",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "http://amex.com/amexcard/revocation/parameters"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf11",
		                            "AttributeDescription": {
		                                "@Type": "CardType",
		                                "@DataType": "xs:string",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Normal"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf12",
		                            "AttributeDescription": {
		                                "@Type": "Name",
		                                "@DataType": "xs:string",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Stewart"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf13",
		                            "AttributeDescription": {
		                                "@Type": "LastName",
		                                "@DataType": "xs:string",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Dent"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf14",
		                            "AttributeDescription": {
		                                "@Type": "CardNumber",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "int"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:int",
		                                "$": "1"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf15",
		                            "AttributeDescription": {
		                                "@Type": "ExpirationDate",
		                                "@DataType": "xs:date",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "2015-02-01+01:00"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf16",
		                            "AttributeDescription": {
		                                "@Type": "SecurityCode",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "int"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:int",
		                                "$": "1"
		                            }
		                        },
		                        {
		                            "AttributeUID": "lkadsfijg8itnf17",
		                            "AttributeDescription": {
		                                "@Type": "Status",
		                                "@DataType": "xs:anyURI",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "status"
		                            }
		                        }
		                    ]
		                }
		            }
		        ]
		    },
		    "pseudonyms": {
		        "entry": [
		            {
		                "key": "pseudonym/5fctpxdv2a6srtt6",
		                "value": {
		                    "Pseudonym": {
		                        "@Exclusive": "false",
		                        "@Scope": "http://www.sweetdreamsuites.com",
		                        "@PseudonymUID": "pseudonym/5fctpxdv2a6srtt6"
		                    }
		                }
		            },
		            {
		                "key": "pseudonym/3mwiyl5fuaq9qp7f",
		                "value": {
		                    "Pseudonym": {
		                        "@Exclusive": "false",
		                        "@Scope": "http://www.sweetdreamsuites.com",
		                        "@PseudonymUID": "pseudonym/3mwiyl5fuaq9qp7f"
		                    }
		                }
		            }
		        ]
		    },
		    "CandidatePresentationTokenList": {
		        "CandidatePresentationToken": [
		            {
		                "token": {
		                    "@PolicyUID": "http://www.sweetdreamsuites.com/policies/booking/standard",
		                    "@TokenUID": "abc4trust.eu/token-uid/2zfisjaq4a2dranx",
		                    "Message": {
		                        "Nonce": "SGVsbG9Xb3JsZAo=",
		                        "ApplicationData": "RoomType : Double\nBedsize : King\nArrivalDate : 2012-04-01\nNrOfNights :  2\nReservationCode : HCJ095\nI agree to the terms of service and cancellation policy."
		                    },
		                    "Pseudonym": {
		                        "@Exclusive": "false",
		                        "@Scope": "http://www.sweetdreamsuites.com",
		                        "@Alias": "#mainPseudonym",
		                        "PseudonymValue": "Kis="
		                    },
		                    "Credential": [
		                        {
		                            "@Alias": "#passport",
		                            "CredentialSpecUID": "http://www.admin.ch/passport/specification",
		                            "IssuerParametersUID": "http://www.admin.ch/passport/issuancekey_v1.0",
		                            "RevocationInformationUID": "http://admin.ch/passport/revocation/parameters"
		                        },
		                        {
		                            "@Alias": "#creditcard",
		                            "CredentialSpecUID": "http://visa.com/creditcard/specification",
		                            "IssuerParametersUID": "http://thebestbank.com/cc/issuancekey_v1.0",
		                            "DisclosedAttribute": {
		                                "@AttributeType": "CardNumber",
		                                "@DataHandlingPolicy": "http://www.sweetdreamsuites.com/policies/creditcards",
		                                "InspectorPublicKeyUID": "http://admin.ch/inspector/pub_key_v1",
		                                "InspectionGrounds": "In case of no free cancellation and no show the credit card number should be Disclosed to the hotel."
		                            }
		                        }
		                    ],
		                    "AttributePredicate": {
		                        "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal",
		                        "Attribute": {
		                            "@CredentialAlias": "#creditcard",
		                            "@AttributeType": "ExpirationDate"
		                        },
		                        "ConstantValue": "2012-01-06Z"
		                    }
		                },
		                "CredentialUidList": {
		                    "CredentialUid": [
		                        "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
		                        "24d4e9eb-9fbf-4e03-8044-37c3810eaa35"
		                    ]
		                },
		                "PseudonymChoiceList": {
		                    "PseudonymChoice": null
		                },
		                "InspectorChoiceList": {
		                    "InspectorChoice": null
		                }
		            },
		            {
		                "token": {
		                    "@PolicyUID": "http://www.sweetdreamsuites.com/policies/booking/standard",
		                    "@TokenUID": "abc4trust.eu/token-uid/106tt0e2ojwpt2yj",
		                    "Message": {
		                        "Nonce": "SGVsbG9Xb3JsZAo=",
		                        "ApplicationData": "RoomType : Double\nBedsize : King\nArrivalDate : 2012-04-01\nNrOfNights :  2\nReservationCode : HCJ095\nI agree to the terms of service and cancellation policy."
		                    },
		                    "Pseudonym": {
		                        "@Exclusive": "false",
		                        "@Scope": "http://www.sweetdreamsuites.com",
		                        "@Alias": "#mainPseudonym"
		                    },
		                    "Credential": [
		                        {
		                            "@Alias": "#passport",
		                            "CredentialSpecUID": "http://www.admin.ch/passport/specification",
		                            "IssuerParametersUID": "http://www.admin.ch/passport/issuancekey_v1.0",
		                            "RevocationInformationUID": "http://admin.ch/passport/revocation/parameters"
		                        },
		                        {
		                            "@Alias": "#creditcard",
		                            "CredentialSpecUID": "http://amex.com/amexcard/specification",
		                            "IssuerParametersUID": "http://www.amex.com/abc/isskey",
		                            "DisclosedAttribute": {
		                                "@AttributeType": "CardNumber",
		                                "@DataHandlingPolicy": "http://www.sweetdreamsuites.com/policies/creditcards",
		                                "InspectorPublicKeyUID": "",
		                                "InspectionGrounds": "In case of no free cancellation and no show the credit card number should be Disclosed to the hotel."
		                            }
		                        }
		                    ],
		                    "AttributePredicate": {
		                        "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal",
		                        "Attribute": {
		                            "@CredentialAlias": "#creditcard",
		                            "@AttributeType": "ExpirationDate"
		                        },
		                        "ConstantValue": "2012-01-06Z"
		                    }
		                },
		                "CredentialUidList": {
		                    "CredentialUid": [
		                        "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
		                        "704eea8d-c148-42cb-bfb6-3dd1328d87d9"
		                    ]
		                },
		                "PseudonymChoiceList": {
		                    "PseudonymChoice": null
		                },
		                "InspectorChoiceList": {
		                    "InspectorChoice": null
		                }
		            },
		            {
		                "token": {
		                    "@PolicyUID": "http://www.sweetdreamsuites.com/policies/booking/studentcardoffer",
		                    "@TokenUID": "abc4trust.eu/token-uid/3tl3k9nwfwjsyg1v",
		                    "Message": {
		                        "Nonce": "SGVsbG9Xb3JsZAo=",
		                        "ApplicationData": "RoomType : Double\nBedsize : King\nArrivalDate : 2012-04-01\nNrOfNights :  2\nReservationCode : HCJ095\nI agree to the terms of service and cancellation policy."
		                    },
		                    "Pseudonym": {
		                        "@Exclusive": "false",
		                        "@Scope": "http://www.sweetdreamsuites.com",
		                        "@Alias": "#mainPseudonym"
		                    },
		                    "Credential": [
		                        {
		                            "@Alias": "#passport",
		                            "CredentialSpecUID": "http://www.admin.ch/passport/specification",
		                            "IssuerParametersUID": "http://www.admin.ch/passport/issuancekey_v1.0",
		                            "RevocationInformationUID": "http://www.admin.ch/passport/revocation/parameters"
		                        },
		                        {
		                            "@Alias": "#creditcard",
		                            "CredentialSpecUID": "http://visa.com/creditcard/specification",
		                            "IssuerParametersUID": "http://thebestbank.com/cc/issuancekey_v1.0",
		                            "DisclosedAttribute": {
		                                "@AttributeType": "CardNumber",
		                                "@DataHandlingPolicy": "http://www.sweetdreamsuites.com/policies/creditcards",
		                                "InspectorPublicKeyUID": "",
		                                "InspectionGrounds": "In case of no free cancellation and no show, the credit card number should be Disclosed to the hotel."
		                            }
		                        },
		                        {
		                            "@Alias": "#studentid",
		                            "CredentialSpecUID": "http://www.ethz.ch/studentid/specification",
		                            "IssuerParametersUID": "http://www.ethz.ch/studentid/issuancekey_v1.0",
		                            "RevocationInformationUID": "http://www.ethz.ch/studentid/revocation/information"
		                        }
		                    ],
		                    "AttributePredicate": {
		                        "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal",
		                        "Attribute": {
		                            "@CredentialAlias": "#creditcard",
		                            "@AttributeType": "ExpirationDate"
		                        },
		                        "ConstantValue": "2012-01-06Z"
		                    },
		                    "VerifierDrivenRevocation": {
		                        "RevocationInformationUID": "no:revocation:information:yet",
		                        "Attribute": {
		                            "@AttributeType": "PassportNumber",
		                            "@CredentialAlias": "#passport"
		                        }
		                    }
		                },
		                "CredentialUidList": {
		                    "CredentialUid": [
		                        "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
		                        "24d4e9eb-9fbf-4e03-8044-37c3810eaa35",
		                        "a677febd-8f28-4688-9eb6-0133cc2be072"
		                    ]
		                },
		                "PseudonymChoiceList": {
		                    "PseudonymChoice": null
		                },
		                "InspectorChoiceList": {
		                    "InspectorChoice": null
		                }
		            }
		        ]
		    }
		};	
		
	return idselectionJSON;	
}



/* ------------------------------------------------------------------------
 * @name : getTestJSON3()
 * @description : getting test data 'idselection_json_hotelbooking.txt'
 * -----------------------------------------------------------------------*/
function getTestJSON3()
{	
	var idselectionJSON = 
	{
			"credentialDescriptions": {
			        "entry": [
			            {
			                "key": "704eea8d-c148-42cb-bfb6-3dd1328d87d9",
			                "value": {
			                    "CredentialUID": "704eea8d-c148-42cb-bfb6-3dd1328d87d9",
			                    "CredentialSpecificationUID": "http://amex.com/amexcard/specification",
			                    "IssuerParametersUID": "http://www.amex.com/abc/isskey",
			                    "Attribute": [
			                        {
			                            "AttributeUID": "lkadsfijg8itnf10",
			                            "AttributeDescription": {
			                                "@Type": "http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
			                                "@DataType": "xs:anyURI",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "http://visa.com/creditcard/revocation/parameters"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf11",
			                            "AttributeDescription": {
			                                "@Type": "CardType",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Gold"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf12",
			                            "AttributeDescription": {
			                                "@Type": "Name",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Alice"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf13",
			                            "AttributeDescription": {
			                                "@Type": "LastName",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Nextdoor"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf14",
			                            "AttributeDescription": {
			                                "@Type": "CardNumber",
			                                "@DataType": "xs:integer",
			                                "@Encoding": "int"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:int",
			                                "$": "42"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf15",
			                            "AttributeDescription": {
			                                "@Type": "ExpirationDate",
			                                "@DataType": "xs:date",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "2014-02-01+01:00"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf16",
			                            "AttributeDescription": {
			                                "@Type": "SecurityCode",
			                                "@DataType": "xs:integer",
			                                "@Encoding": "int"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:int",
			                                "$": "42"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf17",
			                            "AttributeDescription": {
			                                "@Type": "Status",
			                                "@DataType": "xs:anyURI",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "status"
			                            }
			                        }
			                    ]
			                }
			            },
			            {
			                "key": "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
			                "value": {
			                    "CredentialUID": "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
			                    "CredentialSpecificationUID": "http://www.admin.ch/passport/specification",
			                    "IssuerParametersUID": "http://www.admin.ch/passport/issuancekey_v1.0",
			                    "Attribute": [
			                        {
			                            "AttributeUID": "lkadsfijg8itnf10",
			                            "AttributeDescription": {
			                                "@Type": "http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
			                                "@DataType": "xs:anyURI",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "http://admin.ch/passport/revocation/parameters"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf11",
			                            "AttributeDescription": {
			                                "@Type": "Name",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Alice"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf12",
			                            "AttributeDescription": {
			                                "@Type": "LastName",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Nextdoor"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf13",
			                            "AttributeDescription": {
			                                "@Type": "PassportNumber",
			                                "@DataType": "xs:integer",
			                                "@Encoding": "int"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:int",
			                                "$": "42"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf14",
			                            "AttributeDescription": {
			                                "@Type": "Issued",
			                                "@DataType": "xs:date",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "2011-02-01+01:00"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf15",
			                            "AttributeDescription": {
			                                "@Type": "Expires",
			                                "@DataType": "xs:date",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "2014-02-01+01:00"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf16",
			                            "AttributeDescription": {
			                                "@Type": "IssuedBy",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "service_issuer_integration_test"
			                            }
			                        }
			                    ]
			                }
			            },
			            {
			                "key": "24d4e9eb-9fbf-4e03-8044-37c3810eaa35",
			                "value": {
			                    "CredentialUID": "24d4e9eb-9fbf-4e03-8044-37c3810eaa35",
			                    "CredentialSpecificationUID": "http://visa.com/creditcard/specification",
			                    "IssuerParametersUID": "http://thebestbank.com/cc/issuancekey_v1.0",
			                    "Attribute": [
			                        {
			                            "AttributeUID": "lkadsfijg8itnf10",
			                            "AttributeDescription": {
			                                "@Type": "http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
			                                "@DataType": "xs:anyURI",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "http://visa.com/creditcard/revocation/parameters"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf11",
			                            "AttributeDescription": {
			                                "@Type": "CardType",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Gold"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf12",
			                            "AttributeDescription": {
			                                "@Type": "Name",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Alice"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf13",
			                            "AttributeDescription": {
			                                "@Type": "LastName",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Nextdoor"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf14",
			                            "AttributeDescription": {
			                                "@Type": "CardNumber",
			                                "@DataType": "xs:integer",
			                                "@Encoding": "int"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:int",
			                                "$": "42"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf15",
			                            "AttributeDescription": {
			                                "@Type": "ExpirationDate",
			                                "@DataType": "xs:date",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "2014-02-01+01:00"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf16",
			                            "AttributeDescription": {
			                                "@Type": "SecurityCode",
			                                "@DataType": "xs:integer",
			                                "@Encoding": "int"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:int",
			                                "$": "42"
			                            }
			                        },
			                        {
			                            "AttributeUID": "lkadsfijg8itnf17",
			                            "AttributeDescription": {
			                                "@Type": "Status",
			                                "@DataType": "xs:anyURI",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "status"
			                            }
			                        }
			                    ]
			                }
			            }
			        ]
			    },
			    "pseudonyms": {
			        "entry": {
			            "key": "pseudonym/57hj3bet9rsmx5gz",
			            "value": {
			                "Pseudonym": {
			                    "@Exclusive": "false",
			                    "@Scope": "http://www.sweetdreamsuites.com",
			                    "@PseudonymUID": "pseudonym/57hj3bet9rsmx5gz"
			                }
			            }
			        }
			    },
			    "CandidatePresentationTokenList": {
			        "CandidatePresentationToken": [
			            {
			                "token": {
			                    "@PolicyUID": "http://www.sweetdreamsuites.com/policies/booking/standard",
			                    "@TokenUID": "abc4trust.eu/token-uid/4m4xbuf6lf4mmbol",
			                    "Message": {
			                        "Nonce": "SGVsbG9Xb3JsZAo=",
			                        "ApplicationData": "RoomType : Double\nBedsize : King\nArrivalDate : 2012-04-01\nNrOfNights :  2\nReservationCode : HCJ095\nI agree to the terms of service and cancellation policy."
			                    },
			                    "Pseudonym": {
			                        "@Exclusive": "false",
			                        "@Scope": "http://www.sweetdreamsuites.com",
			                        "@Alias": "#mainPseudonym",
			                        "PseudonymValue": "Kis="
			                    },
			                    "Credential": [
			                        {
			                            "@Alias": "#passport",
			                            "CredentialSpecUID": "http://www.admin.ch/passport/specification",
			                            "IssuerParametersUID": "http://www.admin.ch/passport/issuancekey_v1.0",
			                            "RevocationInformationUID": "http://admin.ch/passport/revocation/parameters"
			                        },
			                        {
			                            "@Alias": "#creditcard",
			                            "CredentialSpecUID": "http://visa.com/creditcard/specification",
			                            "IssuerParametersUID": "http://thebestbank.com/cc/issuancekey_v1.0",
			                            "DisclosedAttribute": {
			                                "@AttributeType": "CardNumber",
			                                "@DataHandlingPolicy": "http://www.sweetdreamsuites.com/policies/creditcards",
			                                "InspectorPublicKeyUID": "http://admin.ch/inspector/pub_key_v1",
			                                "InspectionGrounds": "In case of no free cancellation and no show the credit card number should be Disclosed to the hotel."
			                            }
			                        }
			                    ],
			                    "AttributePredicate": {
			                        "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal",
			                        "Attribute": {
			                            "@CredentialAlias": "#creditcard",
			                            "@AttributeType": "ExpirationDate"
			                        },
			                        "ConstantValue": "2012-01-06Z"
			                    }
			                },
			                "CredentialUidList": {
			                    "CredentialUid": [
			                        "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
			                        "24d4e9eb-9fbf-4e03-8044-37c3810eaa35"
			                    ]
			                },
			                "PseudonymChoiceList": {
			                    "PseudonymChoice": null
			                },
			                "InspectorChoiceList": {
			                    "InspectorChoice": null
			                }
			            },
			            {
			                "token": {
			                    "@PolicyUID": "http://www.sweetdreamsuites.com/policies/booking/standard",
			                    "@TokenUID": "abc4trust.eu/token-uid/2i4e8u4lxiccbkbb",
			                    "Message": {
			                        "Nonce": "SGVsbG9Xb3JsZAo=",
			                        "ApplicationData": "RoomType : Double\nBedsize : King\nArrivalDate : 2012-04-01\nNrOfNights :  2\nReservationCode : HCJ095\nI agree to the terms of service and cancellation policy."
			                    },
			                    "Pseudonym": {
			                        "@Exclusive": "false",
			                        "@Scope": "http://www.sweetdreamsuites.com",
			                        "@Alias": "#mainPseudonym"
			                    },
			                    "Credential": [
			                        {
			                            "@Alias": "#passport",
			                            "CredentialSpecUID": "http://www.admin.ch/passport/specification",
			                            "IssuerParametersUID": "http://www.admin.ch/passport/issuancekey_v1.0",
			                            "RevocationInformationUID": "http://admin.ch/passport/revocation/parameters"
			                        },
			                        {
			                            "@Alias": "#creditcard",
			                            "CredentialSpecUID": "http://amex.com/amexcard/specification",
			                            "IssuerParametersUID": "http://www.amex.com/abc/isskey",
			                            "DisclosedAttribute": {
			                                "@AttributeType": "CardNumber",
			                                "@DataHandlingPolicy": "http://www.sweetdreamsuites.com/policies/creditcards",
			                                "InspectorPublicKeyUID": "",
			                                "InspectionGrounds": "In case of no free cancellation and no show the credit card number should be Disclosed to the hotel."
			                            }
			                        }
			                    ],
			                    "AttributePredicate": {
			                        "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal",
			                        "Attribute": {
			                            "@CredentialAlias": "#creditcard",
			                            "@AttributeType": "ExpirationDate"
			                        },
			                        "ConstantValue": "2012-01-06Z"
			                    }
			                },
			                "CredentialUidList": {
			                    "CredentialUid": [
			                        "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
			                        "704eea8d-c148-42cb-bfb6-3dd1328d87d9"
			                    ]
			                },
			                "PseudonymChoiceList": {
			                    "PseudonymChoice": null
			                },
			                "InspectorChoiceList": {
			                    "InspectorChoice": null
			                }
			            }
			        ]
			    }
	};
	
	return idselectionJSON;	
}




/* ---------------------------------------------------------------------------------
 * @name : getTestJSON4()
 * @description : getting test data 'Youth Login' - IDCard credential, no pseudonym
 * --------------------------------------------------------------------------------*/
function getTestJSON4()
{	
	var idselectionJSON = 
	{
		    "credentialDescriptions": {
		        "entry": {
		            "key": "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		            "value": {
		                "CredentialUID": "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		                "CredentialSpecificationUID": "http://my.country/identitycard/specification",
		                "IssuerParametersUID": "http://my.country/identitycard/issuancekey_v1.0",
		                "Attribute": [
		                    {
		                        "AttributeUID": "-6973841425144228697",
		                        "AttributeDescription": {
		                            "@Type": "Firstname",
		                            "@DataType": "xs:string",
		                            "@Encoding": "sha256"
		                        },
		                        "AttributeValue": {
		                            "@type": "xs:string",
		                            "$": "Stewart"
		                        }
		                    },
		                    {
		                        "AttributeUID": "-7820189279587932466",
		                        "AttributeDescription": {
		                            "@Type": "Lastname",
		                            "@DataType": "xs:string",
		                            "@Encoding": "sha256"
		                        },
		                        "AttributeValue": {
		                            "@type": "xs:string",
		                            "$": "Dent"
		                        }
		                    },
		                    {
		                        "AttributeUID": "-5729939185712751854",
		                        "AttributeDescription": {
		                            "@Type": "Birthday",
		                            "@DataType": "xs:date",
		                            "@Encoding": "sha256"
		                        },
		                        "AttributeValue": {
		                            "@type": "xs:string",
		                            "$": "1995-02-01+01:00"
		                        }
		                    }
		                ]
		            }
		        }
		    },
		    "pseudonyms": {
		        "entry": {
		            "key": "pseudonym/3voy8onl9subs358",
		            "value": {
		                "Pseudonym": {
		                    "@Exclusive": "true",
		                    "@Scope": "http://www.services.com",
		                    "@PseudonymUID": "pseudonym/3voy8onl9subs358"
		                },
		                "PseudonymMetadata": null
		            }
		        }
		    },
		    "CandidatePresentationTokenList": {
		        "CandidatePresentationToken": {
		            "token": {
		                "@PolicyUID": "http://www.services.com/policies/new/customer/young",
		                "@TokenUID": "abc4trust.eu/token-uid/4mb0hcatmjqai8ud",
		                "Message": {
		                    "ApplicationData": "\n        Authenticate as a new customer under the age of 18.\n      "
		                },
		                "Pseudonym": {
		                    "@Exclusive": "true",
		                    "@Scope": "http://www.services.com",
		                    "@Alias": "#accessPseudonym",
		                    "PseudonymValue": "Kis="
		                },
		                "Credential": {
		                    "@Alias": "#identitycard",
		                    "CredentialSpecUID": "http://my.country/identitycard/specification",
		                    "IssuerParametersUID": "http://my.country/identitycard/issuancekey_v1.0"
		                },
		                "AttributePredicate": {
		                    "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal",
		                    "Attribute": {
		                        "@CredentialAlias": "#identitycard",
		                        "@AttributeType": "Birthday"
		                    },
		                    "ConstantValue": "1994-01-06Z"
		                }
		            },
		            "CredentialUidList": {
		                "CredentialUid": "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce"
		            },
		            "PseudonymChoiceList": {
		                "PseudonymChoice": null
		            },
		            "InspectorChoiceList": null
		        }
		    }
		};
	return idselectionJSON;		
}



/* ---------------------------------------------------------------------------------
 * @name : getTestJSON5()
 * @description : getting test data 'Youth Login' - IDCard credential, with pseudonym
 * --------------------------------------------------------------------------------*/
function getTestJSON5()
{	
	var idselectionJSON = 
	{
		    "credentialDescriptions": {
		        "entry": {
		            "key": "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		            "value": {
		                "CredentialUID": "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		                "CredentialSpecificationUID": "http://my.country/identitycard/specification",
		                "IssuerParametersUID": "http://my.country/identitycard/issuancekey_v1.0",
		                "Attribute": [
		                    {
		                        "AttributeUID": "-6973841425144228697",
		                        "AttributeDescription": {
		                            "@Type": "Firstname",
		                            "@DataType": "xs:string",
		                            "@Encoding": "sha256"
		                        },
		                        "AttributeValue": {
		                            "@type": "xs:string",
		                            "$": "Stewart"
		                        }
		                    },
		                    {
		                        "AttributeUID": "-7820189279587932466",
		                        "AttributeDescription": {
		                            "@Type": "Lastname",
		                            "@DataType": "xs:string",
		                            "@Encoding": "sha256"
		                        },
		                        "AttributeValue": {
		                            "@type": "xs:string",
		                            "$": "Dent"
		                        }
		                    },
		                    {
		                        "AttributeUID": "-5729939185712751854",
		                        "AttributeDescription": {
		                            "@Type": "Birthday",
		                            "@DataType": "xs:date",
		                            "@Encoding": "sha256"
		                        },
		                        "AttributeValue": {
		                            "@type": "xs:string",
		                            "$": "1995-02-01+01:00"
		                        }
		                    }
		                ]
		            }
		        }
		    },
		    "pseudonyms": {
		        "entry": {
		            "key": "pseudonym/3voy8onl9subs358",
		            "value": {
		                "Pseudonym": {
		                    "@Exclusive": "true",
		                    "@Scope": "http://www.services.com",
		                    "@PseudonymUID": "pseudonym/3voy8onl9subs358"
		                },
		                "PseudonymMetadata": null
		            }
		        }
		    },
		    "CandidatePresentationTokenList": {
		        "CandidatePresentationToken": [
		            {
		                "token": {
		                    "@PolicyUID": "http://www.services.com/policies/existing/customer",
		                    "@TokenUID": "abc4trust.eu/token-uid/5cnvwvnhtnj4sy8p",
		                    "Message": {
		                        "ApplicationData": "\n        Authenticate as a returning customer.\n      "
		                    },
		                    "Pseudonym": {
		                        "@Exclusive": "true",
		                        "@Scope": "http://www.services.com",
		                        "@Alias": "#accessPseudonym",
		                        "PseudonymValue": "Kis="
		                    }
		                },
		                "CredentialUidList": null,
		                "PseudonymChoiceList": {
		                    "PseudonymChoice": null
		                },
		                "InspectorChoiceList": null
		            },
		            {
		                "token": {
		                    "@PolicyUID": "http://www.services.com/policies/new/customer/young",
		                    "@TokenUID": "abc4trust.eu/token-uid/4egrqmz7kji7oq27",
		                    "Message": {
		                        "ApplicationData": "\n        Authenticate as a new customer under the age of 18.\n      "
		                    },
		                    "Pseudonym": {
		                        "@Exclusive": "true",
		                        "@Scope": "http://www.services.com",
		                        "@Alias": "#accessPseudonym"
		                    },
		                    "Credential": {
		                        "@Alias": "#identitycard",
		                        "CredentialSpecUID": "http://my.country/identitycard/specification",
		                        "IssuerParametersUID": "http://my.country/identitycard/issuancekey_v1.0"
		                    },
		                    "AttributePredicate": {
		                        "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal",
		                        "Attribute": {
		                            "@CredentialAlias": "#identitycard",
		                            "@AttributeType": "Birthday"
		                        },
		                        "ConstantValue": "1994-01-06Z"
		                    }
		                },
		                "CredentialUidList": {
		                    "CredentialUid": "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce"
		                },
		                "PseudonymChoiceList": {
		                    "PseudonymChoice": null
		                },
		                "InspectorChoiceList": null
		            }
		        ]
		    }
		};

	return idselectionJSON;	
}



/* ---------------------------------------------------------------------------------------------
 * @name : getTestJSON6()
 * @description : getting test data 'Adult Login' - IDCard credential, Visa, Amex, No pseudonym
 * -------------------------------------------------------------------------------------------*/
function getTestJSON6()
{	
	var idselectionJSON = 
	{
		    "credentialDescriptions": {
		        "entry": [
		            {
		                "key": "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		                "value": {
		                    "CredentialUID": "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		                    "CredentialSpecificationUID": "http://my.country/identitycard/specification",
		                    "IssuerParametersUID": "http://my.country/identitycard/issuancekey_v1.0",
		                    "Attribute": [
		                        {
		                            "AttributeUID": "-6973841425144228697",
		                            "AttributeDescription": {
		                                "@Type": "Firstname",
		                                "@DataType": "xs:string",
		                                "@Encoding": "sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Alice"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-7820189279587932466",
		                            "AttributeDescription": {
		                                "@Type": "Lastname",
		                                "@DataType": "xs:string",
		                                "@Encoding": "sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Nexdoor"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-5729939185712751854",
		                            "AttributeDescription": {
		                                "@Type": "Birthday",
		                                "@DataType": "xs:date",
		                                "@Encoding": "sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "1970-02-01+01:00"
		                            }
		                        }
		                    ]
		                }
		            },
		            {
		                "key": "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
		                "value": {
		                    "CredentialUID": "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
		                    "CredentialSpecificationUID": "http://visa.com/creditcard/specification",
		                    "IssuerParametersUID": "http://thebestbank.com/cc/issuancekey_v1.0",
		                    "Attribute": [
		                        {
		                            "AttributeUID": "-7650899656004103383",
		                            "AttributeDescription": {
		                                "@Type": "http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
		                                "@DataType": "xs:anyURI",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "http://visa.com/creditcard/revocation/parameters"
		                            }
		                        },
		                        {
		                            "AttributeUID": "2654003278634790403",
		                            "AttributeDescription": {
		                                "@Type": "CardType",
		                                "@DataType": "xs:string",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Gold"
		                            }
		                        },
		                        {
		                            "AttributeUID": "4630887629667084853",
		                            "AttributeDescription": {
		                                "@Type": "Name",
		                                "@DataType": "xs:string",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Alice"
		                            }
		                        },
		                        {
		                            "AttributeUID": "8092663474985403083",
		                            "AttributeDescription": {
		                                "@Type": "LastName",
		                                "@DataType": "xs:string",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Nextdoor"
		                            }
		                        },
		                        {
		                            "AttributeUID": "4590924838856394713",
		                            "AttributeDescription": {
		                                "@Type": "CardNumber",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "int"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:int",
		                                "$": "42"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-6451407851080853880",
		                            "AttributeDescription": {
		                                "@Type": "ExpirationDate",
		                                "@DataType": "xs:date",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "2014-02-01+01:00"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-2398728429547822990",
		                            "AttributeDescription": {
		                                "@Type": "SecurityCode",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "int"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:int",
		                                "$": "42"
		                            }
		                        },
		                        {
		                            "AttributeUID": "316117992922331269",
		                            "AttributeDescription": {
		                                "@Type": "Status",
		                                "@DataType": "xs:anyURI",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "status"
		                            }
		                        }
		                    ]
		                }
		            },
		            {
		                "key": "24d4e9eb-9fbf-4e03-8044-37c3810eaa35",
		                "value": {
		                    "CredentialUID": "24d4e9eb-9fbf-4e03-8044-37c3810eaa35",
		                    "CredentialSpecificationUID": "http://amex.com/amexcard/specification",
		                    "IssuerParametersUID": "http://www.amex.com/abc/isskey",
		                    "Attribute": [
		                        {
		                            "AttributeUID": "3514479926766758866",
		                            "AttributeDescription": {
		                                "@Type": "http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
		                                "@DataType": "xs:anyURI",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "http://amex.com/amexcard/revocation/parameters"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-7382356228725844255",
		                            "AttributeDescription": {
		                                "@Type": "CardType",
		                                "@DataType": "xs:string",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Gold"
		                            }
		                        },
		                        {
		                            "AttributeUID": "2453477503650610314",
		                            "AttributeDescription": {
		                                "@Type": "Name",
		                                "@DataType": "xs:string",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Alice"
		                            }
		                        },
		                        {
		                            "AttributeUID": "5467729166072017262",
		                            "AttributeDescription": {
		                                "@Type": "LastName",
		                                "@DataType": "xs:string",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Nextdoor"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-1362219025931516052",
		                            "AttributeDescription": {
		                                "@Type": "CardNumber",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "int"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:int",
		                                "$": "1042"
		                            }
		                        },
		                        {
		                            "AttributeUID": "530400857177495258",
		                            "AttributeDescription": {
		                                "@Type": "ExpirationDate",
		                                "@DataType": "xs:date",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "2014-02-01+01:00"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-7692823228049137845",
		                            "AttributeDescription": {
		                                "@Type": "SecurityCode",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "int"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:int",
		                                "$": "1042"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-4727942888021356524",
		                            "AttributeDescription": {
		                                "@Type": "Status",
		                                "@DataType": "xs:anyURI",
		                                "@Encoding": "xenc:sha256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "status"
		                            }
		                        }
		                    ]
		                }
		            }
		        ]
		    },
		    "pseudonyms": {
		        "entry": {
		            "key": "pseudonym/1yqt350r812vwnwp",
		            "value": {
		                "Pseudonym": {
		                    "@Exclusive": "true",
		                    "@Scope": "http://www.services.com",
		                    "@PseudonymUID": "pseudonym/1yqt350r812vwnwp"
		                },
		                "PseudonymMetadata": null
		            }
		        }
		    },
		    "CandidatePresentationTokenList": {
		        "CandidatePresentationToken": [
		            {
		                "token": {
		                    "@PolicyUID": "http://www.services.com/policies/new/customer/adult",
		                    "@TokenUID": "abc4trust.eu/token-uid/4dqw2mox899arq2j",
		                    "Message": {
		                        "ApplicationData": "\n        Authenticate as a new customer over the age of 18.\n      "
		                    },
		                    "Pseudonym": {
		                        "@Exclusive": "true",
		                        "@Scope": "http://www.services.com",
		                        "@Alias": "#accessPseudonym",
		                        "PseudonymValue": "Kis="
		                    },
		                    "Credential": [
		                        {
		                            "@Alias": "#identitycard",
		                            "CredentialSpecUID": "http://my.country/identitycard/specification",
		                            "IssuerParametersUID": "http://my.country/identitycard/issuancekey_v1.0"
		                        },
		                        {
		                            "@Alias": "#creditcard",
		                            "CredentialSpecUID": "http://visa.com/creditcard/specification",
		                            "IssuerParametersUID": "http://thebestbank.com/cc/issuancekey_v1.0",
		                            "DisclosedAttribute": {
		                                "@AttributeType": "CardNumber",
		                                "@DataHandlingPolicy": "http://www.sweetdreamsuites.com/policies/creditcards",
		                                "InspectorPublicKeyUID": "http://admin.ch/inspector/pub_key_v1",
		                                "InspectionGrounds": "In case of no free cancellation and no show the credit card number should be Disclosed to the hotel."
		                            }
		                        }
		                    ],
		                    "AttributePredicate": [
		                        {
		                            "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal",
		                            "Attribute": {
		                                "@CredentialAlias": "#creditcard",
		                                "@AttributeType": "ExpirationDate"
		                            },
		                            "ConstantValue": "1974-12-30Z"
		                        },
		                        {
		                            "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal",
		                            "Attribute": {
		                                "@CredentialAlias": "#identitycard",
		                                "@AttributeType": "Birthday"
		                            },
		                            "ConstantValue": "1994-01-06Z"
		                        }
		                    ]
		                },
		                "CredentialUidList": {
		                    "CredentialUid": [
		                        "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		                        "b07b2aab-4b00-4b12-95d2-8b4c8fb79329"
		                    ]
		                },
		                "PseudonymChoiceList": {
		                    "PseudonymChoice": null
		                },
		                "InspectorChoiceList": {
		                    "InspectorChoice": null
		                }
		            },
		            {
		                "token": {
		                    "@PolicyUID": "http://www.services.com/policies/new/customer/adult",
		                    "@TokenUID": "abc4trust.eu/token-uid/598bkrgqcsle6tfp",
		                    "Message": {
		                        "ApplicationData": "\n        Authenticate as a new customer over the age of 18.\n      "
		                    },
		                    "Pseudonym": {
		                        "@Exclusive": "true",
		                        "@Scope": "http://www.services.com",
		                        "@Alias": "#accessPseudonym"
		                    },
		                    "Credential": [
		                        {
		                            "@Alias": "#identitycard",
		                            "CredentialSpecUID": "http://my.country/identitycard/specification",
		                            "IssuerParametersUID": "http://my.country/identitycard/issuancekey_v1.0"
		                        },
		                        {
		                            "@Alias": "#creditcard",
		                            "CredentialSpecUID": "http://amex.com/amexcard/specification",
		                            "IssuerParametersUID": "http://www.amex.com/abc/isskey",
		                            "DisclosedAttribute": {
		                                "@AttributeType": "CardNumber",
		                                "@DataHandlingPolicy": "http://www.sweetdreamsuites.com/policies/creditcards",
		                                "InspectorPublicKeyUID": "",
		                                "InspectionGrounds": "In case of no free cancellation and no show the credit card number should be Disclosed to the hotel."
		                            }
		                        }
		                    ],
		                    "AttributePredicate": [
		                        {
		                            "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal",
		                            "Attribute": {
		                                "@CredentialAlias": "#creditcard",
		                                "@AttributeType": "ExpirationDate"
		                            },
		                            "ConstantValue": "1974-12-30Z"
		                        },
		                        {
		                            "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal",
		                            "Attribute": {
		                                "@CredentialAlias": "#identitycard",
		                                "@AttributeType": "Birthday"
		                            },
		                            "ConstantValue": "1994-01-06Z"
		                        }
		                    ]
		                },
		                "CredentialUidList": {
		                    "CredentialUid": [
		                        "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		                        "24d4e9eb-9fbf-4e03-8044-37c3810eaa35"
		                    ]
		                },
		                "PseudonymChoiceList": {
		                    "PseudonymChoice": null
		                },
		                "InspectorChoiceList": {
		                    "InspectorChoice": null
		                }
		            }
		        ]
		    }
		};

	return idselectionJSON;	
}		



/* ---------------------------------------------------------------------------------------------
 * @name : getTestJSON7()
 * @description : getting test data 'Adult Login' - IDCard credential, Visa, Amex, with pseudonym
 * -------------------------------------------------------------------------------------------*/
function getTestJSON7()
{	
	var idselectionJSON = 
	{
			"credentialDescriptions": {
			        "entry": [
			            {
			                "key": "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
			                "value": {
			                    "CredentialUID": "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
			                    "CredentialSpecificationUID": "http://my.country/identitycard/specification",
			                    "IssuerParametersUID": "http://my.country/identitycard/issuancekey_v1.0",
			                    "Attribute": [
			                        {
			                            "AttributeUID": "-6973841425144228697",
			                            "AttributeDescription": {
			                                "@Type": "Firstname",
			                                "@DataType": "xs:string",
			                                "@Encoding": "sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Alice"
			                            }
			                        },
			                        {
			                            "AttributeUID": "-7820189279587932466",
			                            "AttributeDescription": {
			                                "@Type": "Lastname",
			                                "@DataType": "xs:string",
			                                "@Encoding": "sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Nexdoor"
			                            }
			                        },
			                        {
			                            "AttributeUID": "-5729939185712751854",
			                            "AttributeDescription": {
			                                "@Type": "Birthday",
			                                "@DataType": "xs:date",
			                                "@Encoding": "sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "1970-02-01+01:00"
			                            }
			                        }
			                    ]
			                }
			            },
			            {
			                "key": "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
			                "value": {
			                    "CredentialUID": "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
			                    "CredentialSpecificationUID": "http://visa.com/creditcard/specification",
			                    "IssuerParametersUID": "http://thebestbank.com/cc/issuancekey_v1.0",
			                    "Attribute": [
			                        {
			                            "AttributeUID": "-7650899656004103383",
			                            "AttributeDescription": {
			                                "@Type": "http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
			                                "@DataType": "xs:anyURI",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "http://visa.com/creditcard/revocation/parameters"
			                            }
			                        },
			                        {
			                            "AttributeUID": "2654003278634790403",
			                            "AttributeDescription": {
			                                "@Type": "CardType",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Gold"
			                            }
			                        },
			                        {
			                            "AttributeUID": "4630887629667084853",
			                            "AttributeDescription": {
			                                "@Type": "Name",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Alice"
			                            }
			                        },
			                        {
			                            "AttributeUID": "8092663474985403083",
			                            "AttributeDescription": {
			                                "@Type": "LastName",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Nextdoor"
			                            }
			                        },
			                        {
			                            "AttributeUID": "4590924838856394713",
			                            "AttributeDescription": {
			                                "@Type": "CardNumber",
			                                "@DataType": "xs:integer",
			                                "@Encoding": "int"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:int",
			                                "$": "42"
			                            }
			                        },
			                        {
			                            "AttributeUID": "-6451407851080853880",
			                            "AttributeDescription": {
			                                "@Type": "ExpirationDate",
			                                "@DataType": "xs:date",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "2014-02-01+01:00"
			                            }
			                        },
			                        {
			                            "AttributeUID": "-2398728429547822990",
			                            "AttributeDescription": {
			                                "@Type": "SecurityCode",
			                                "@DataType": "xs:integer",
			                                "@Encoding": "int"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:int",
			                                "$": "42"
			                            }
			                        },
			                        {
			                            "AttributeUID": "316117992922331269",
			                            "AttributeDescription": {
			                                "@Type": "Status",
			                                "@DataType": "xs:anyURI",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "status"
			                            }
			                        }
			                    ]
			                }
			            },
			            {
			                "key": "24d4e9eb-9fbf-4e03-8044-37c3810eaa35",
			                "value": {
			                    "CredentialUID": "24d4e9eb-9fbf-4e03-8044-37c3810eaa35",
			                    "CredentialSpecificationUID": "http://amex.com/amexcard/specification",
			                    "IssuerParametersUID": "http://www.amex.com/abc/isskey",
			                    "Attribute": [
			                        {
			                            "AttributeUID": "3514479926766758866",
			                            "AttributeDescription": {
			                                "@Type": "http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle",
			                                "@DataType": "xs:anyURI",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "http://amex.com/amexcard/revocation/parameters"
			                            }
			                        },
			                        {
			                            "AttributeUID": "-7382356228725844255",
			                            "AttributeDescription": {
			                                "@Type": "CardType",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Gold"
			                            }
			                        },
			                        {
			                            "AttributeUID": "2453477503650610314",
			                            "AttributeDescription": {
			                                "@Type": "Name",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Alice"
			                            }
			                        },
			                        {
			                            "AttributeUID": "5467729166072017262",
			                            "AttributeDescription": {
			                                "@Type": "LastName",
			                                "@DataType": "xs:string",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "Nextdoor"
			                            }
			                        },
			                        {
			                            "AttributeUID": "-1362219025931516052",
			                            "AttributeDescription": {
			                                "@Type": "CardNumber",
			                                "@DataType": "xs:integer",
			                                "@Encoding": "int"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:int",
			                                "$": "1042"
			                            }
			                        },
			                        {
			                            "AttributeUID": "530400857177495258",
			                            "AttributeDescription": {
			                                "@Type": "ExpirationDate",
			                                "@DataType": "xs:date",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "2014-02-01+01:00"
			                            }
			                        },
			                        {
			                            "AttributeUID": "-7692823228049137845",
			                            "AttributeDescription": {
			                                "@Type": "SecurityCode",
			                                "@DataType": "xs:integer",
			                                "@Encoding": "int"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:int",
			                                "$": "1042"
			                            }
			                        },
			                        {
			                            "AttributeUID": "-4727942888021356524",
			                            "AttributeDescription": {
			                                "@Type": "Status",
			                                "@DataType": "xs:anyURI",
			                                "@Encoding": "xenc:sha256"
			                            },
			                            "AttributeValue": {
			                                "@type": "xs:string",
			                                "$": "status"
			                            }
			                        }
			                    ]
			                }
			            }
			        ]
			    },
			    "pseudonyms": {
			        "entry": {
			            "key": "pseudonym/1yqt350r812vwnwp",
			            "value": {
			                "Pseudonym": {
			                    "@Exclusive": "true",
			                    "@Scope": "http://www.services.com",
			                    "@PseudonymUID": "pseudonym/1yqt350r812vwnwp"
			                },
			                "PseudonymMetadata": null
			            }
			        }
			    },
			    "CandidatePresentationTokenList": {
			        "CandidatePresentationToken": [
			            {
			                "token": {
			                    "@PolicyUID": "http://www.services.com/policies/existing/customer",
			                    "@TokenUID": "abc4trust.eu/token-uid/2ek2qbqkpd3plq7z",
			                    "Message": {
			                        "ApplicationData": "\n        Authenticate as a returning customer.\n      "
			                    },
			                    "Pseudonym": {
			                        "@Exclusive": "true",
			                        "@Scope": "http://www.services.com",
			                        "@Alias": "#accessPseudonym",
			                        "PseudonymValue": "Kis="
			                    }
			                },
			                "CredentialUidList": null,
			                "PseudonymChoiceList": {
			                    "PseudonymChoice": null
			                },
			                "InspectorChoiceList": null
			            },
			            {
			                "token": {
			                    "@PolicyUID": "http://www.services.com/policies/new/customer/adult",
			                    "@TokenUID": "abc4trust.eu/token-uid/436sk6hxyy254l17",
			                    "Message": {
			                        "ApplicationData": "\n        Authenticate as a new customer over the age of 18.\n      "
			                    },
			                    "Pseudonym": {
			                        "@Exclusive": "true",
			                        "@Scope": "http://www.services.com",
			                        "@Alias": "#accessPseudonym"
			                    },
			                    "Credential": [
			                        {
			                            "@Alias": "#identitycard",
			                            "CredentialSpecUID": "http://my.country/identitycard/specification",
			                            "IssuerParametersUID": "http://my.country/identitycard/issuancekey_v1.0"
			                        },
			                        {
			                            "@Alias": "#creditcard",
			                            "CredentialSpecUID": "http://visa.com/creditcard/specification",
			                            "IssuerParametersUID": "http://thebestbank.com/cc/issuancekey_v1.0",
			                            "DisclosedAttribute": {
			                                "@AttributeType": "CardNumber",
			                                "@DataHandlingPolicy": "http://www.sweetdreamsuites.com/policies/creditcards",
			                                "InspectorPublicKeyUID": "",
			                                "InspectionGrounds": "In case of no free cancellation and no show the credit card number should be Disclosed to the hotel."
			                            }
			                        }
			                    ],
			                    "AttributePredicate": [
			                        {
			                            "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal",
			                            "Attribute": {
			                                "@CredentialAlias": "#creditcard",
			                                "@AttributeType": "ExpirationDate"
			                            },
			                            "ConstantValue": "1974-12-30Z"
			                        },
			                        {
			                            "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal",
			                            "Attribute": {
			                                "@CredentialAlias": "#identitycard",
			                                "@AttributeType": "Birthday"
			                            },
			                            "ConstantValue": "1994-01-06Z"
			                        }
			                    ]
			                },
			                "CredentialUidList": {
			                    "CredentialUid": [
			                        "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
			                        "b07b2aab-4b00-4b12-95d2-8b4c8fb79329"
			                    ]
			                },
			                "PseudonymChoiceList": {
			                    "PseudonymChoice": null
			                },
			                "InspectorChoiceList": {
			                    "InspectorChoice": null
			                }
			            },
			            {
			                "token": {
			                    "@PolicyUID": "http://www.services.com/policies/new/customer/adult",
			                    "@TokenUID": "abc4trust.eu/token-uid/4mtdd0jw4h97vwrx",
			                    "Message": {
			                        "ApplicationData": "\n        Authenticate as a new customer over the age of 18.\n      "
			                    },
			                    "Pseudonym": {
			                        "@Exclusive": "true",
			                        "@Scope": "http://www.services.com",
			                        "@Alias": "#accessPseudonym"
			                    },
			                    "Credential": [
			                        {
			                            "@Alias": "#identitycard",
			                            "CredentialSpecUID": "http://my.country/identitycard/specification",
			                            "IssuerParametersUID": "http://my.country/identitycard/issuancekey_v1.0"
			                        },
			                        {
			                            "@Alias": "#creditcard",
			                            "CredentialSpecUID": "http://amex.com/amexcard/specification",
			                            "IssuerParametersUID": "http://www.amex.com/abc/isskey",
			                            "DisclosedAttribute": {
			                                "@AttributeType": "CardNumber",
			                                "@DataHandlingPolicy": "http://www.sweetdreamsuites.com/policies/creditcards",
			                                "InspectorPublicKeyUID": "",
			                                "InspectionGrounds": "In case of no free cancellation and no show the credit card number should be Disclosed to the hotel."
			                            }
			                        }
			                    ],
			                    "AttributePredicate": [
			                        {
			                            "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal",
			                            "Attribute": {
			                                "@CredentialAlias": "#creditcard",
			                                "@AttributeType": "ExpirationDate"
			                            },
			                            "ConstantValue": "1974-12-30Z"
			                        },
			                        {
			                            "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal",
			                            "Attribute": {
			                                "@CredentialAlias": "#identitycard",
			                                "@AttributeType": "Birthday"
			                            },
			                            "ConstantValue": "1994-01-06Z"
			                        }
			                    ]
			                },
			                "CredentialUidList": {
			                    "CredentialUid": [
			                        "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
			                        "24d4e9eb-9fbf-4e03-8044-37c3810eaa35"
			                    ]
			                },
			                "PseudonymChoiceList": {
			                    "PseudonymChoice": null
			                },
			                "InspectorChoiceList": {
			                    "InspectorChoice": null
			                }
			            }
			        ]
			    }
	};
	
	return idselectionJSON;
}	



/* ---------------------------------------------------------------------------------------------
 * @name : getTestJSON8()
 * @description : getting test data First Login, IdCard, Over 18, Visa or Amex
 * -------------------------------------------------------------------------------------------*/
function getTestJSON8()
{	
	var idselectionJSON = 
	{
		    "credentialDescriptions": {
		        "entry": [
		            {
		                "key": "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		                "value": {
		                    "CredentialUID": "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		                    "CredentialSpecificationUID": "http://my.country/identitycard/specification",
		                    "IssuerParametersUID": "http://my.country/identitycard/issuancekey_v1.0",
		                    "Attribute": [
		                        {
		                            "AttributeUID": "-6973841425144228697",
		                            "AttributeDescription": {
		                                "@Type": "Birthday",
		                                "@DataType": "xs:date",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:date:unix:signed"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "1970-02-01Z"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-7820189279587932466",
		                            "AttributeDescription": {
		                                "@Type": "Lastname",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Nexdoor"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-5729939185712751854",
		                            "AttributeDescription": {
		                                "@Type": "Firstname",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Alice"
		                            }
		                        }
		                    ]
		                }
		            },
		            {
		                "key": "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
		                "value": {
		                    "CredentialUID": "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
		                    "CredentialSpecificationUID": "http://visa.com/creditcard/specification",
		                    "IssuerParametersUID": "http://thebestbank.com/cc/issuancekey_v1.0",
		                    "Attribute": [
		                        {
		                            "AttributeUID": "-7650899656004103383",
		                            "AttributeDescription": {
		                                "@Type": "CardNumber",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:integer:signed"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:integer",
		                                "$": "42"
		                            }
		                        },
		                        {
		                            "AttributeUID": "2654003278634790403",
		                            "AttributeDescription": {
		                                "@Type": "Name",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Alice"
		                            }
		                        },
		                        {
		                            "AttributeUID": "4630887629667084853",
		                            "AttributeDescription": {
		                                "@Type": "Status",
		                                "@DataType": "xs:anyURI",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:anyUri:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "status"
		                            }
		                        },
		                        {
		                            "AttributeUID": "8092663474985403083",
		                            "AttributeDescription": {
		                                "@Type": "SecurityCode",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:integer:signed"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:int",
		                                "$": "1"
		                            }
		                        },
		                        {
		                            "AttributeUID": "4590924838856394713",
		                            "AttributeDescription": {
		                                "@Type": "CardType",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Gold"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-6451407851080853880",
		                            "AttributeDescription": {
		                                "@Type": "LastName",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Nextdoor"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-2398728429547822990",
		                            "AttributeDescription": {
		                                "@Type": "ExpirationDate",
		                                "@DataType": "xs:dateTime",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:dateTime:unix:signed"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "2014-02-01T00:00:00.254+01:00"
		                            }
		                        }
		                    ]
		                }
		            },
		            {
		                "key": "24d4e9eb-9fbf-4e03-8044-37c3810eaa35",
		                "value": {
		                    "CredentialUID": "24d4e9eb-9fbf-4e03-8044-37c3810eaa35",
		                    "CredentialSpecificationUID": "http://amex.com/amexcard/specification",
		                    "IssuerParametersUID": "http://www.amex.com/abc/isskey",
		                    "Attribute": [
		                        {
		                            "AttributeUID": "316117992922331269",
		                            "AttributeDescription": {
		                                "@Type": "CardNumber",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:integer:signed"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:integer",
		                                "$": "1042"
		                            }
		                        },
		                        {
		                            "AttributeUID": "3514479926766758866",
		                            "AttributeDescription": {
		                                "@Type": "Name",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Alice"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-7382356228725844255",
		                            "AttributeDescription": {
		                                "@Type": "Status",
		                                "@DataType": "xs:anyURI",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:anyUri:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "status"
		                            }
		                        },
		                        {
		                            "AttributeUID": "2453477503650610314",
		                            "AttributeDescription": {
		                                "@Type": "SecurityCode",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:integer:signed"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:int",
		                                "$": "1"
		                            }
		                        },
		                        {
		                            "AttributeUID": "5467729166072017262",
		                            "AttributeDescription": {
		                                "@Type": "CardType",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Gold"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-1362219025931516052",
		                            "AttributeDescription": {
		                                "@Type": "LastName",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Nextdoor"
		                            }
		                        },
		                        {
		                            "AttributeUID": "530400857177495258",
		                            "AttributeDescription": {
		                                "@Type": "ExpirationDate",
		                                "@DataType": "xs:dateTime",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:dateTime:unix:signed"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "2014-02-01T00:00:00.390+01:00"
		                            }
		                        }
		                    ]
		                }
		            }
		        ]
		    },
		    "pseudonyms": {
		        "entry": {
		            "key": "pseudonym/bzz3y2nej003mls",
		            "value": {
		                "Pseudonym": {
		                    "@Exclusive": "true",
		                    "@Scope": "http://www.services.com",
		                    "@PseudonymUID": "pseudonym/bzz3y2nej003mls"
		                },
		                "PseudonymMetadata": {
		                    "HumanReadableData": "MyHumanReadableData"
		                }
		            }
		        }
		    },
		    "CandidatePresentationTokenList": {
		        "CandidatePresentationToken": [
		            {
		                "Token": {
		                    "@PolicyUID": "http://www.services.com/policies/new/customer/adult",
		                    "@TokenUID": "abc4trust.eu/token-uid/1624afpy5ygnwgcv",
		                    "Message": {
		                        "ApplicationData": "\n        Authenticate as a new customer over the age of 18.\n      "
		                    },
		                    "Pseudonym": {
		                        "@Exclusive": "true",
		                        "@Scope": "http://www.services.com",
		                        "@Alias": "#accessPseudonym",
		                        "PseudonymValue": "Kis="
		                    },
		                    "Credential": [
		                        {
		                            "@Alias": "#identitycard",
		                            "CredentialSpecUID": "http://my.country/identitycard/specification",
		                            "IssuerParametersUID": "http://my.country/identitycard/issuancekey_v1.0"
		                        },
		                        {
		                            "@Alias": "#creditcard",
		                            "CredentialSpecUID": "http://visa.com/creditcard/specification",
		                            "IssuerParametersUID": "http://thebestbank.com/cc/issuancekey_v1.0",
		                            "DisclosedAttribute": {
		                                "@AttributeType": "CardNumber",
		                                "@DataHandlingPolicy": "http://www.sweetdreamsuites.com/policies/creditcards",
		                                "InspectorPublicKeyUID": "http://admin.ch/inspector/pub_key_v1",
		                                "InspectionGrounds": "In case of no free cancellation and no show the credit card number should be Disclosed to the hotel."
		                            }
		                        }
		                    ],
		                    "AttributePredicate": [
		                        {
		                            "@Function": "urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal",
		                            "Attribute": {
		                                "@CredentialAlias": "#creditcard",
		                                "@AttributeType": "ExpirationDate"
		                            },
		                            "ConstantValue": "1974-12-30T10:42:42Z"
		                        },
		                        {
		                            "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal",
		                            "Attribute": {
		                                "@CredentialAlias": "#identitycard",
		                                "@AttributeType": "Birthday"
		                            },
		                            "ConstantValue": "1994-01-06Z"
		                        }
		                    ]
		                },
		                "CredentialUidList": {
		                    "CredentialUid": [
		                        "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		                        "b07b2aab-4b00-4b12-95d2-8b4c8fb79329"
		                    ]
		                },
		                "PseudonymChoiceList": {
		                    "URISet": {
		                        "URI": "pseudonym/bzz3y2nej003mls"
		                    }
		                },
		                "InspectorChoiceList": {
		                    "URISet": {
		                        "URI": [
		                            "http://admin.ch/inspector/pub_key_v1",
		                            "http://thebestbank.com/inspector/pub_key_v1"
		                        ]
		                    }
		                }
		            },
		            {
		                "Token": {
		                    "@PolicyUID": "http://www.services.com/policies/new/customer/adult",
		                    "@TokenUID": "abc4trust.eu/token-uid/48bz7t5pyxzm3ln3",
		                    "Message": {
		                        "ApplicationData": "\n        Authenticate as a new customer over the age of 18.\n      "
		                    },
		                    "Pseudonym": {
		                        "@Exclusive": "true",
		                        "@Scope": "http://www.services.com",
		                        "@Alias": "#accessPseudonym"
		                    },
		                    "Credential": [
		                        {
		                            "@Alias": "#identitycard",
		                            "CredentialSpecUID": "http://my.country/identitycard/specification",
		                            "IssuerParametersUID": "http://my.country/identitycard/issuancekey_v1.0"
		                        },
		                        {
		                            "@Alias": "#creditcard",
		                            "CredentialSpecUID": "http://amex.com/amexcard/specification",
		                            "IssuerParametersUID": "http://www.amex.com/abc/isskey",
		                            "DisclosedAttribute": {
		                                "@AttributeType": "CardNumber",
		                                "@DataHandlingPolicy": "http://www.sweetdreamsuites.com/policies/creditcards",
		                                "InspectorPublicKeyUID": "",
		                                "InspectionGrounds": "In case of no free cancellation and no show the credit card number should be Disclosed to the hotel."
		                            }
		                        }
		                    ],
		                    "AttributePredicate": [
		                        {
		                            "@Function": "urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal",
		                            "Attribute": {
		                                "@CredentialAlias": "#creditcard",
		                                "@AttributeType": "ExpirationDate"
		                            },
		                            "ConstantValue": "1974-12-30T10:42:42Z"
		                        },
		                        {
		                            "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal",
		                            "Attribute": {
		                                "@CredentialAlias": "#identitycard",
		                                "@AttributeType": "Birthday"
		                            },
		                            "ConstantValue": "1994-01-06Z"
		                        }
		                    ]
		                },
		                "CredentialUidList": {
		                    "CredentialUid": [
		                        "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		                        "24d4e9eb-9fbf-4e03-8044-37c3810eaa35"
		                    ]
		                },
		                "PseudonymChoiceList": {
		                    "URISet": {
		                        "URI": "pseudonym/bzz3y2nej003mls"
		                    }
		                },
		                "InspectorChoiceList": {
		                    "URISet": {
		                        "URI": [
		                            "http://admin.ch/inspector/pub_key_v1",
		                            "http://thebestbank.com/inspector/pub_key_v1"
		                        ]
		                    }
		                }
		            }
		        ]
		    }
		};

	return idselectionJSON;
}



/* ---------------------------------------------------------------------------------------------
 * @name : getTestJSON9()
 * @description : getting test data Second Login, IdCard, Over 18, Visa or Amex, Or Pseudonym
 * -------------------------------------------------------------------------------------------*/
function getTestJSON9()
{	
	var idselectionJSON = 
	{
		    "credentialDescriptions": {
		        "entry": [
		            {
		                "key": "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		                "value": {
		                    "CredentialUID": "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		                    "CredentialSpecificationUID": "http://my.country/identitycard/specification",
		                    "IssuerParametersUID": "http://my.country/identitycard/issuancekey_v1.0",
		                    "Attribute": [
		                        {
		                            "AttributeUID": "-6973841425144228697",
		                            "AttributeDescription": {
		                                "@Type": "Birthday",
		                                "@DataType": "xs:date",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:date:unix:signed"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "1970-02-01Z"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-7820189279587932466",
		                            "AttributeDescription": {
		                                "@Type": "Lastname",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Nexdoor"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-5729939185712751854",
		                            "AttributeDescription": {
		                                "@Type": "Firstname",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Alice"
		                            }
		                        }
		                    ]
		                }
		            },
		            {
		                "key": "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
		                "value": {
		                    "CredentialUID": "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
		                    "CredentialSpecificationUID": "http://visa.com/creditcard/specification",
		                    "IssuerParametersUID": "http://thebestbank.com/cc/issuancekey_v1.0",
		                    "Attribute": [
		                        {
		                            "AttributeUID": "-7650899656004103383",
		                            "AttributeDescription": {
		                                "@Type": "CardNumber",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:integer:signed"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:integer",
		                                "$": "42"
		                            }
		                        },
		                        {
		                            "AttributeUID": "2654003278634790403",
		                            "AttributeDescription": {
		                                "@Type": "Name",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Alice"
		                            }
		                        },
		                        {
		                            "AttributeUID": "4630887629667084853",
		                            "AttributeDescription": {
		                                "@Type": "Status",
		                                "@DataType": "xs:anyURI",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:anyUri:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "status"
		                            }
		                        },
		                        {
		                            "AttributeUID": "8092663474985403083",
		                            "AttributeDescription": {
		                                "@Type": "SecurityCode",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:integer:signed"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:int",
		                                "$": "1"
		                            }
		                        },
		                        {
		                            "AttributeUID": "4590924838856394713",
		                            "AttributeDescription": {
		                                "@Type": "CardType",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Gold"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-6451407851080853880",
		                            "AttributeDescription": {
		                                "@Type": "LastName",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Nextdoor"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-2398728429547822990",
		                            "AttributeDescription": {
		                                "@Type": "ExpirationDate",
		                                "@DataType": "xs:dateTime",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:dateTime:unix:signed"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "2014-02-01T00:00:00.254+01:00"
		                            }
		                        }
		                    ]
		                }
		            },
		            {
		                "key": "24d4e9eb-9fbf-4e03-8044-37c3810eaa35",
		                "value": {
		                    "CredentialUID": "24d4e9eb-9fbf-4e03-8044-37c3810eaa35",
		                    "CredentialSpecificationUID": "http://amex.com/amexcard/specification",
		                    "IssuerParametersUID": "http://www.amex.com/abc/isskey",
		                    "Attribute": [
		                        {
		                            "AttributeUID": "316117992922331269",
		                            "AttributeDescription": {
		                                "@Type": "CardNumber",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:integer:signed"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:integer",
		                                "$": "1042"
		                            }
		                        },
		                        {
		                            "AttributeUID": "3514479926766758866",
		                            "AttributeDescription": {
		                                "@Type": "Name",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Alice"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-7382356228725844255",
		                            "AttributeDescription": {
		                                "@Type": "Status",
		                                "@DataType": "xs:anyURI",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:anyUri:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "status"
		                            }
		                        },
		                        {
		                            "AttributeUID": "2453477503650610314",
		                            "AttributeDescription": {
		                                "@Type": "SecurityCode",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:integer:signed"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:int",
		                                "$": "1"
		                            }
		                        },
		                        {
		                            "AttributeUID": "5467729166072017262",
		                            "AttributeDescription": {
		                                "@Type": "CardType",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Gold"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-1362219025931516052",
		                            "AttributeDescription": {
		                                "@Type": "LastName",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Nextdoor"
		                            }
		                        },
		                        {
		                            "AttributeUID": "530400857177495258",
		                            "AttributeDescription": {
		                                "@Type": "ExpirationDate",
		                                "@DataType": "xs:dateTime",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:dateTime:unix:signed"
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "2014-02-01T00:00:00.390+01:00"
		                            }
		                        }
		                    ]
		                }
		            }
		        ]
		    },
		    "pseudonyms": {
		        "entry": {
		            "key": "pseudonym/bzz3y2nej003mls",
		            "value": {
		                "Pseudonym": {
		                    "@Exclusive": "true",
		                    "@Scope": "http://www.services.com",
		                    "@PseudonymUID": "pseudonym/bzz3y2nej003mls"
		                },
		                "PseudonymMetadata": {
		                    "HumanReadableData": "MyHumanReadableData"
		                }
		            }
		        }
		    },
		    "CandidatePresentationTokenList": {
		        "CandidatePresentationToken": [
		            {
		                "token": {
		                    "@PolicyUID": "http://www.services.com/policies/existing/customer",
		                    "@TokenUID": "abc4trust.eu/token-uid/u4lsmj8hu4gqyh7",
		                    "Message": {
		                        "ApplicationData": "\n        Authenticate as a returning customer.\n      "
		                    },
		                    "Pseudonym": {
		                        "@Exclusive": "true",
		                        "@Scope": "http://www.services.com",
		                        "@Alias": "#accessPseudonym",
		                        "PseudonymValue": "Kis="
		                    }
		                },
		                "CredentialUidList": null,
		                "PseudonymChoiceList": {
		                    "URISet": {
		                        "URI": "pseudonym/bzz3y2nej003mls"
		                    }
		                },
		                "InspectorChoiceList": null
		            },
		            {
		                "token": {
		                    "@PolicyUID": "http://www.services.com/policies/new/customer/adult",
		                    "@TokenUID": "abc4trust.eu/token-uid/2thum79jbe3l5h1q",
		                    "Message": {
		                        "ApplicationData": "\n        Authenticate as a new customer over the age of 18.\n      "
		                    },
		                    "Pseudonym": {
		                        "@Exclusive": "true",
		                        "@Scope": "http://www.services.com",
		                        "@Alias": "#accessPseudonym"
		                    },
		                    "Credential": [
		                        {
		                            "@Alias": "#identitycard",
		                            "CredentialSpecUID": "http://my.country/identitycard/specification",
		                            "IssuerParametersUID": "http://my.country/identitycard/issuancekey_v1.0"
		                        },
		                        {
		                            "@Alias": "#creditcard",
		                            "CredentialSpecUID": "http://visa.com/creditcard/specification",
		                            "IssuerParametersUID": "http://thebestbank.com/cc/issuancekey_v1.0",
		                            "DisclosedAttribute": {
		                                "@AttributeType": "CardNumber",
		                                "@DataHandlingPolicy": "http://www.sweetdreamsuites.com/policies/creditcards",
		                                "InspectorPublicKeyUID": "",
		                                "InspectionGrounds": "In case of no free cancellation and no show the credit card number should be Disclosed to the hotel."
		                            }
		                        }
		                    ],
		                    "AttributePredicate": [
		                        {
		                            "@Function": "urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal",
		                            "Attribute": {
		                                "@CredentialAlias": "#creditcard",
		                                "@AttributeType": "ExpirationDate"
		                            },
		                            "ConstantValue": "1974-12-30T10:42:42Z"
		                        },
		                        {
		                            "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal",
		                            "Attribute": {
		                                "@CredentialAlias": "#identitycard",
		                                "@AttributeType": "Birthday"
		                            },
		                            "ConstantValue": "1994-01-06Z"
		                        }
		                    ]
		                },
		                "CredentialUidList": {
		                    "CredentialUid": [
		                        "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		                        "b07b2aab-4b00-4b12-95d2-8b4c8fb79329"
		                    ]
		                },
		                "PseudonymChoiceList": {
		                    "URISet": {
		                        "URI": "pseudonym/bzz3y2nej003mls"
		                    }
		                },
		                "InspectorChoiceList": {
		                    "URISet": {
		                        "URI": [
		                            "http://admin.ch/inspector/pub_key_v1",
		                            "http://thebestbank.com/inspector/pub_key_v1"
		                        ]
		                    }
		                }
		            },
		            {
		                "token": {
		                    "@PolicyUID": "http://www.services.com/policies/new/customer/adult",
		                    "@TokenUID": "abc4trust.eu/token-uid/wpue4pejgeiayyi",
		                    "Message": {
		                        "ApplicationData": "\n        Authenticate as a new customer over the age of 18.\n      "
		                    },
		                    "Pseudonym": {
		                        "@Exclusive": "true",
		                        "@Scope": "http://www.services.com",
		                        "@Alias": "#accessPseudonym"
		                    },
		                    "Credential": [
		                        {
		                            "@Alias": "#identitycard",
		                            "CredentialSpecUID": "http://my.country/identitycard/specification",
		                            "IssuerParametersUID": "http://my.country/identitycard/issuancekey_v1.0"
		                        },
		                        {
		                            "@Alias": "#creditcard",
		                            "CredentialSpecUID": "http://amex.com/amexcard/specification",
		                            "IssuerParametersUID": "http://www.amex.com/abc/isskey",
		                            "DisclosedAttribute": {
		                                "@AttributeType": "CardNumber",
		                                "@DataHandlingPolicy": "http://www.sweetdreamsuites.com/policies/creditcards",
		                                "InspectorPublicKeyUID": "",
		                                "InspectionGrounds": "In case of no free cancellation and no show the credit card number should be Disclosed to the hotel."
		                            }
		                        }
		                    ],
		                    "AttributePredicate": [
		                        {
		                            "@Function": "urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal",
		                            "Attribute": {
		                                "@CredentialAlias": "#creditcard",
		                                "@AttributeType": "ExpirationDate"
		                            },
		                            "ConstantValue": "1974-12-30T10:42:42Z"
		                        },
		                        {
		                            "@Function": "urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal",
		                            "Attribute": {
		                                "@CredentialAlias": "#identitycard",
		                                "@AttributeType": "Birthday"
		                            },
		                            "ConstantValue": "1994-01-06Z"
		                        }
		                    ]
		                },
		                "CredentialUidList": {
		                    "CredentialUid": [
		                        "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		                        "24d4e9eb-9fbf-4e03-8044-37c3810eaa35"
		                    ]
		                },
		                "PseudonymChoiceList": {
		                    "URISet": {
		                        "URI": "pseudonym/bzz3y2nej003mls"
		                    }
		                },
		                "InspectorChoiceList": {
		                    "URISet": {
		                        "URI": [
		                            "http://admin.ch/inspector/pub_key_v1",
		                            "http://thebestbank.com/inspector/pub_key_v1"
		                        ]
		                    }
		                }
		            }
		        ]
		    }
		};

	return idselectionJSON;
}



/* ---------------------------------------------------------------------------------------------
 * @name : getTestJSON10()
 * @description : JSON (April 12) handcrafted JSON input which Hans said contains all
 * -------------------------------------------------------------------------------------------*/
function getTestJSON10()
{	
	var idselectionJSON = 
	{
		    "PolicyDescriptions": {
		        "entry": [
		            {
		                "key": "http://www.services.com/policies/new/customer/adult",
		                "value": {
		                    "PolicyUID": "http://www.services.com/policies/new/customer/adult",
		                    "Message": {
		                        "Nonce": "4545",
		                        "FriendlyPolicyName": {
		                            "@lang": "en",
		                            "$": "Adult policy"
		                        },
		                        "FriendlyPolicyDescription": {
		                            "@lang": "en",
		                            "$": "id card + visa or amex"
		                        },
		                        "ApplicationData": null
		                    }
		                }
		            },
		            {
		                "key": "http://www.services.com/policies/new/customer/young",
		                "value": {
		                    "PolicyUID": "http://www.services.com/policies/new/customer/young",
		                    "Message": {
		                        "Nonce": "",
		                        "FriendlyPolicyName": {
		                            "@lang": "en",
		                            "$": "Customer policy"
		                        },
		                        "FriendlyPolicyDescription": {
		                            "@lang": "en",
		                            "$": "nym exclusive + id card"
		                        },
		                        "ApplicationData": null
		                    }
		                }
		            }
		        ]
		    },
		    "CredentialDescriptions": {
		        "entry": [
		            {
		                "key": "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		                "value": {
		                    "CredentialUID": "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		                    "FriendlyCredentialName": {
		                        "@lang": "en",
		                        "$": "ID card MyCountry"
		                    },
		                    "ImageReference": "file:///C:/Users/soh/ABC4Trust_SourceCode/java/ri/trunk/firefoxplugin/abc4trust/extension/content/testJSON/master.jpg",
		                    "CredentialSpecificationUID": "http://my.country/identitycard/specification",
		                    "IssuerParametersUID": "http://my.country/identitycard/issuancekey_v1.0",
		                    "Attribute": [
		                        {
		                            "AttributeUID": "-6973841425144228697",
		                            "AttributeDescription": {
		                                "@Type": "Birthday",
		                                "@DataType": "xs:date",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:date:unix:signed",
		                                "FriendlyAttributeName": {
		                                    "@lang": "en",
		                                    "$": "Date of birth"
		                                }
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "1970-02-01Z"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-7820189279587932466",
		                            "AttributeDescription": {
		                                "@Type": "Lastname",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256",
		                                "FriendlyAttributeName": {
		                                    "@lang": "en",
		                                    "$": "Last Name"
		                                }
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Nexdoor"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-5729939185712751854",
		                            "AttributeDescription": {
		                                "@Type": "Firstname",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256",
		                                "FriendlyAttributeName": {
		                                    "@lang": "en",
		                                    "$": "First Name"
		                                }
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Alice"
		                            }
		                        }
		                    ]
		                }
		            },
		            {
		                "key": "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
		                "value": {
		                    "CredentialUID": "b07b2aab-4b00-4b12-95d2-8b4c8fb79329",
		                    "FriendlyCredentialName": {
		                        "@lang": "en",
		                        "$": "ID card MyCountry"
		                    },
		                    "ImageReference": "file:///C:/Users/soh/ABC4Trust_SourceCode/java/ri/trunk/firefoxplugin/abc4trust/extension/content/testJSON/visa.jpg",
		                    "CredentialSpecificationUID": "http://visa.com/creditcard/specification",
		                    "IssuerParametersUID": "http://thebestbank.com/cc/issuancekey_v1.0",
		                    "Attribute": [
		                        {
		                            "AttributeUID": "-7650899656004103383",
		                            "AttributeDescription": {
		                                "@Type": "CardNumber",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:integer:signed",
		                                "FriendlyAttributeName": {
		                                    "@lang": "en",
		                                    "$": "Last Name"
		                                }
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:integer",
		                                "$": "42"
		                            }
		                        },
		                        {
		                            "AttributeUID": "2654003278634790403",
		                            "AttributeDescription": {
		                                "@Type": "Name",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256",
		                                "FriendlyAttributeName": {
		                                    "@lang": "en",
		                                    "$": "Name"
		                                }
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Alice"
		                            }
		                        },
		                        {
		                            "AttributeUID": "4630887629667084853",
		                            "AttributeDescription": {
		                                "@Type": "Status",
		                                "@DataType": "xs:anyURI",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:anyUri:sha-256",
		                                "FriendlyAttributeName": {
		                                    "@lang": "en",
		                                    "$": "Status"
		                                }
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "status"
		                            }
		                        },
		                        {
		                            "AttributeUID": "8092663474985403083",
		                            "AttributeDescription": {
		                                "@Type": "SecurityCode",
		                                "@DataType": "xs:integer",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:integer:signed",
		                                "FriendlyAttributeName": {
		                                    "@lang": "en",
		                                    "$": "Code"
		                                }
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:int",
		                                "$": "1"
		                            }
		                        },
		                        {
		                            "AttributeUID": "4590924838856394713",
		                            "AttributeDescription": {
		                                "@Type": "CardType",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256",
		                                "FriendlyAttributeName": {
		                                    "@lang": "en",
		                                    "$": "Type of the card"
		                                }
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Gold"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-6451407851080853880",
		                            "AttributeDescription": {
		                                "@Type": "LastName",
		                                "@DataType": "xs:string",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:string:sha-256",
		                                "FriendlyAttributeName": {
		                                    "@lang": "en",
		                                    "$": "Last Name"
		                                }
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "Nextdoor"
		                            }
		                        },
		                        {
		                            "AttributeUID": "-2398728429547822990",
		                            "AttributeDescription": {
		                                "@Type": "ExpirationDate",
		                                "@DataType": "xs:dateTime",
		                                "@Encoding": "urn:abc4trust:1.0:encoding:dateTime:unix:signed",
		                                "FriendlyAttributeName": {
		                                    "@lang": "en",
		                                    "$": "Expiration date"
		                                }
		                            },
		                            "AttributeValue": {
		                                "@type": "xs:string",
		                                "$": "2014-02-01T00:00:00.196+01:00"
		                            }
		                        }
		                    ]
		                }
		            }
		        ]
		    },
		    "PseudonymDescriptions": {
		        "entry": [
		            {
		                "key": "pseudonym/25w5vtt40a93vqi5",
		                "value": {
		                    "PseudonymDescription": {
		                        "@Exclusive": "false",
		                        "@Scope": "http://www.services.com",
		                        "@PseudonymUID": "pseudonym/25w5vtt40a93vqi5",
		                        "PseudonymMetadata": {
		                            "HumanReadableData": "",
		                            "FriendlyPseudonymDescription": {
		                                "@lang": "en",
		                                "$": ""
		                            },
		                            "Metadata": null
		                        }
		                    }
		                }
		            },
		            {
		                "key": "pseudonym/25w5564564564566",
		                "value": {
		                    "PseudonymDescription": {
		                        "@Exclusive": "false",
		                        "@Scope": "http://www.services.com",
		                        "@PseudonymUID": "pseudonym/25w5vtt40a93vqi5",
		                        "PseudonymMetadata": {
		                            "HumanReadableData": "",
		                            "FriendlyPseudonymDescription": {
		                                "@lang": "en",
		                                "$": ""
		                            },
		                            "Metadata": null
		                        }
		                    }
		                }
		            },
		            {
		                "key": "pseudonym/25w5vtt456434534",
		                "value": {
		                    "PseudonymDescription": {
		                        "@Exclusive": "false",
		                        "@Scope": "http://www.services.com",
		                        "@PseudonymUID": "pseudonym/25w5vtt40a93vqi5",
		                        "PseudonymMetadata": {
		                            "HumanReadableData": "",
		                            "FriendlyPseudonymDescription": {
		                                "@lang": "en",
		                                "$": ""
		                            },
		                            "Metadata": null
		                        }
		                    }
		                }
		            },
		            {
		                "key": "pseudonym/25w5vtt40a93vqi5",
		                "value": {
		                    "PseudonymDescription": {
		                        "@Exclusive": "false",
		                        "@Scope": "http://www.services.com",
		                        "@PseudonymUID": "pseudonym/25w5vtt40a93vqi5",
		                        "PseudonymMetadata": {
		                            "HumanReadableData": "",
		                            "FriendlyPseudonymDescription": {
		                                "@lang": "en",
		                                "$": ""
		                            },
		                            "Metadata": null
		                        }
		                    }
		                }
		            },
		            {
		                "key": "pseudonym/25w5vt3534534534",
		                "value": {
		                    "PseudonymDescription": {
		                        "@Exclusive": "true",
		                        "@Scope": "http://www.services.com",
		                        "@PseudonymUID": "pseudonym/25w5vtt40a93vqi5",
		                        "PseudonymMetadata": {
		                            "HumanReadableData": "",
		                            "FriendlyPseudonymDescription": {
		                                "@lang": "en",
		                                "$": ""
		                            },
		                            "Metadata": null
		                        }
		                    }
		                }
		            }
		        ]
		    },
		    "InspectorDescriptions": {
		        "entry": [
		            {
		                "key": "http://admin.ch/inspector/pub_key_v1",
		                "value": {
		                    "InspectorUID": "http://admin.ch/inspector/pub_key_v1",
		                    "FriendlyInspectorDescription": {
		                        "@lang": "en",
		                        "$": "blabla"
		                    }
		                }
		            },
		            {
		                "key": "http://thebestbank.com/inspector/pub_key_v1",
		                "value": {
		                    "InspectorUID": "http://thebestbank.com/inspector/pub_key_v1",
		                    "FriendlyInspectorDescription": {
		                        "@lang": "en",
		                        "$": "The Best Bank Inspector. The best in the world."
		                    }
		                }
		            }
		        ]
		    },
		    "CandidatePresentationTokenList": {
		        "CandidatePresentationToken": [
		            {
		                "Token": {
		                    "@PolicyUID": "http://www.services.com/policies/new/customer/adult",
		                    "@TokenUID": "abc4trust.eu/token-uid/4t86kfddj5hdb06n"
		                },
		                "FriendlyTokenDescription": {
		                    "@lang": "en",
		                    "$": "this token reveals this and that"
		                },
		                "CredentialUidList": {
		                    "CredentialUid": [
		                        "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
		                        "b07b2aab-4b00-4b12-95d2-8b4c8fb79329"
		                    ]
		                },
		                "PseudonymChoiceList": {
		                    "URISet": [
		                        {
		                            "URI": [
		                                "pseudonym/25w5vtt40a93vqi5",
		                                "pseudonym/25w5564564564566"
		                            ]
		                        },
		                        {
		                            "URI": [
		                                "pseudonym/25w5vtt456434534",
		                                "pseudonym/25w5vtt40a93vqi5"
		                            ]
		                        }
		                    ]
		                },
		                "InspectorChoiceList": {
		                    "URISet": [
		                        {
		                            "URI": [
		                                "http://admin.ch/inspector/pub_key_v1",
		                                "http://thebestbank.com/inspector/pub_key_v1"
		                            ]
		                        },
		                        {
		                            "URI": "http://admin.ch/inspector/pub_key_v1"
		                        }
		                    ]
		                }
		            },
		            {
		                "Token": {
		                    "@PolicyUID": "http://www.services.com/policies/new/customer/young",
		                    "@TokenUID": "abc4trust.eu/token-uid/3a00hf8tfyzhhhmi"
		                },
		                "FriendlyTokenDescription": {
		                    "@lang": "en",
		                    "$": "this token reveals bla bla bla bla"
		                },
		                "CredentialUidList": {
		                    "CredentialUid": "9f37f02c-3e5f-44a7-9379-1b4889d7e6ce"
		                },
		                "PseudonymChoiceList": {
		                    "URISet": {
		                        "URI": "pseudonym/25w5vt3534534534"
		                    }
		                },
		                "InspectorChoiceList": null
		            } 
		        ]
		    }
		};

	return idselectionJSON;
}
		
		
