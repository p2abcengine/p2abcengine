/* --------------------------------------------------------------------------------------------
 * @name : about_idselect_testdata() 
 * @description : inform the function from idselect_testdata.js is called 
 * -------------------------------------------------------------------------------------------*/
function about_idselect_testdata()
{
	alert("[DEBUG INFO] idselect_testdata.js is loaded");
	var retIDselect = about_idselect();
	
	return retIDselect;
}


/* --------------------------------------------------------------------------------------------
 * @name : getTestJSON() 
 * @description : get test JSON object
 * --------------------------------------------------------------------------------------------*/
/*
function getTestJSON()
{
	alert("now in getTestJSON");
	
	var idselectionJSON = {
			  	"credentialDescriptions":{
				    "entry":{
				      "key":"9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
				      "value":{
				        "CredentialUID":"9f37f02c-3e5f-44a7-9379-1b4889d7e6ce",
				        "CredentialSpecificationUID":"http://my.country/identitycard/specification",
				        "IssuerParametersUID":"http://my.country/identitycard/issuancekey_v1.0",
				        "Attribute":[
				          {
				            "AttributeUID":"lkadsfijg8itnf0",
				            "AttributeDescription":{"@Type":"Firstname","@DataType":"xs:string","@Encoding":"sha256"},
				            "AttributeValue":{"@type":"xs:string","$":"Alice"}
				          },{
				            "AttributeUID":"lkadsfijg8itnf0",
				            "AttributeDescription":{"@Type":"Lastname","@DataType":"xs:string","@Encoding":"sha256"},
				            "AttributeValue":{"@type":"xs:string","$":"Nexdoor"}
				          },{
				            "AttributeUID":"lkadsfijg8itnf0",
				            "AttributeDescription":{"@Type":"Birthday","@DataType":"xs:date","@Encoding":"sha256"},
				            "AttributeValue":{"@type":"xs:string","$":"1970-02-01+01:00"}
				          }]
				      }
				    }
				},
				"pseudonyms":null,
				"CandidatePresentationTokenList":{
					"CandidatePresentationToken":{
				      "token":{
				        "@PolicyUID":"http://www.sweetdreamsuites.com/policies/booking/standard",
				        "@TokenUID":"abc4trust.eu/token-uid/mvjly3gj1mag4wz",
				        "Message":{"ApplicationData":"\n        Todo: Write something interesting...\n      "},
				        "Credential":{
				          "@Alias":"#identitycard",
				          "CredentialSpecUID":"http://my.country/identitycard/specification",
				          "IssuerParametersUID":"http://my.country/identitycard/issuancekey_v1.0"
				        },
				        "AttributePredicate":{
				          "@Function":"urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal",
				          "Attribute":{
				            "@CredentialAlias":"#identitycard",
				            "@AttributeType":"Birthday"
				          },
				          "ConstantValue":"1994-01-06Z"
				        }
				      },
				      "CredentialUidList":{
				        "CredentialUid":"9f37f02c-3e5f-44a7-9379-1b4889d7e6ce"
				      },
				      "PseudonymChoiceList":null,
				      "InspectorChoiceList":null
				    }
				  }
				};
	
	return idselectionJSON;
}
*/


/* ------------------------------------------------------------------------------------
 * @name : parseJSON(inputJSON)
 * @param : inputJSON is the given JSON object
 * @description : parse the input JSON object for identity selection function 
 * -----------------------------------------------------------------------------------*/
/*
function parseJSON(inputJSON)
{
	alert("inputJSON received");
	
	//var obj = JSON.parse(inputJSON); //JSON.parse() is not supported by FireFox!!
    //alert(obj.count); 

	//1. parse credentialDescription 
	parseCredentialDescriptions(inputJSON); 
	
	//2. parse pseudonym 
	parsePseudonym(inputJSON); 
	
	//3. parse candidatePresentationTokenList
	parseCandidatePresentationTokenList(inputJSON);
	
}
*/


/* -------------------------------------------------------------------------------------
 * @name : parseCredentialDescription(inputJSON)
 * @param : inputJSON is the given JSON object
 * @description : 
 * ------------------------------------------------------------------------------------*/

/*
function parseCredentialDescriptions(inputJSON)
{
	alert("now parsing CredentialDescriptions... " + inputJSON);
	
	//"credentialDescriptions" : { } 
	var credentialDescriptions = inputJSON.credentialDescriptions;
	if(credentialDescriptions && typedef credentialDescriptions == object)
	{
		alert("credentialDescriptions : " + credentialDescriptions);
		
		//"entry" : { }
		var credentialDescription_entry = credentialDescriptions.entry;
		if(credentialDescription_entry && typedef credentialDescription_entry == object)
		{
			alert("credentialDescriptions - entry :" + credentialDescription_entry ); 
			
			var credentialDescription_entry_key = credentialDescription_entry.key;
			if(credentialDescription_entry_key && typedef credentialDescription_entry_key == object)
			{
				alert("credentialDescription_entry_key : " + credentialDescription_entry_key);
			}
		}
		else 
		{
			alert("there is no value object of credentialDescription - entry");
		}
	}
	else
		alert("there is no value object of credentialDescription");
}
*/


function getTestJSON2()
{
	alert("now in getTestJSON");
	
	var idselectionJSON = {"credentialDescriptions":{"entry":[{"key":"a677febd-8f28-4688-9eb6-0133cc2be072","value":{"CredentialUID":"a677febd-8f28-4688-9eb6-0133cc2be072","CredentialSpecificationUID":"http://www.ethz.ch/studentid/specification","IssuerParametersUID":"http://www.ethz.ch/studentid/issuancekey_v1.0","Attribute":[{"AttributeUID":"lkadsfijg8itnf10","AttributeDescription":{"@Type":"http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle","@DataType":"xs:anyURI","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"http://www.ethz.ch/studentid/revocation/parameters"}},{"AttributeUID":"lkadsfijg8itnf11","AttributeDescription":{"@Type":"Name","@DataType":"xs:string","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"Stewart"}},{"AttributeUID":"lkadsfijg8itnf12","AttributeDescription":{"@Type":"LastName","@DataType":"xs:string","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"Dent"}},{"AttributeUID":"lkadsfijg8itnf13","AttributeDescription":{"@Type":"StudentNumber","@DataType":"xs:integer","@Encoding":"int"},"AttributeValue":{"@type":"xs:int","$":"1"}},{"AttributeUID":"lkadsfijg8itnf14","AttributeDescription":{"@Type":"Issued","@DataType":"xs:date","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"2012-02-01+01:00"}},{"AttributeUID":"lkadsfijg8itnf15","AttributeDescription":{"@Type":"Expires","@DataType":"xs:date","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"2015-02-01+01:00"}},{"AttributeUID":"lkadsfijg8itnf16","AttributeDescription":{"@Type":"IssuedBy","@DataType":"xs:string","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"IssuedBy"}}]}},{"key":"704eea8d-c148-42cb-bfb6-3dd1328d87d9","value":{"CredentialUID":"704eea8d-c148-42cb-bfb6-3dd1328d87d9","CredentialSpecificationUID":"http://amex.com/amexcard/specification","IssuerParametersUID":"http://www.amex.com/abc/isskey","Attribute":[{"AttributeUID":"lkadsfijg8itnf10","AttributeDescription":{"@Type":"http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle","@DataType":"xs:anyURI","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"http://amex.com/amexcard/revocation/parameters"}},{"AttributeUID":"lkadsfijg8itnf11","AttributeDescription":{"@Type":"CardType","@DataType":"xs:string","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"Normal"}},{"AttributeUID":"lkadsfijg8itnf12","AttributeDescription":{"@Type":"Name","@DataType":"xs:string","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"Stewart"}},{"AttributeUID":"lkadsfijg8itnf13","AttributeDescription":{"@Type":"LastName","@DataType":"xs:string","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"Dent"}},{"AttributeUID":"lkadsfijg8itnf14","AttributeDescription":{"@Type":"CardNumber","@DataType":"xs:integer","@Encoding":"int"},"AttributeValue":{"@type":"xs:int","$":"1"}},{"AttributeUID":"lkadsfijg8itnf15","AttributeDescription":{"@Type":"ExpirationDate","@DataType":"xs:date","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"2015-02-01+01:00"}},{"AttributeUID":"lkadsfijg8itnf16","AttributeDescription":{"@Type":"SecurityCode","@DataType":"xs:integer","@Encoding":"int"},"AttributeValue":{"@type":"xs:int","$":"1"}},{"AttributeUID":"lkadsfijg8itnf17","AttributeDescription":{"@Type":"Status","@DataType":"xs:anyURI","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"status"}}]}},{"key":"b07b2aab-4b00-4b12-95d2-8b4c8fb79329","value":{"CredentialUID":"b07b2aab-4b00-4b12-95d2-8b4c8fb79329","CredentialSpecificationUID":"http://www.admin.ch/passport/specification","IssuerParametersUID":"http://www.admin.ch/passport/issuancekey_v1.0","Attribute":[{"AttributeUID":"lkadsfijg8itnf10","AttributeDescription":{"@Type":"http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle","@DataType":"xs:anyURI","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"http://admin.ch/passport/revocation/parameters"}},{"AttributeUID":"lkadsfijg8itnf11","AttributeDescription":{"@Type":"Name","@DataType":"xs:string","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"Stewart"}},{"AttributeUID":"lkadsfijg8itnf12","AttributeDescription":{"@Type":"LastName","@DataType":"xs:string","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"Dent"}},{"AttributeUID":"lkadsfijg8itnf13","AttributeDescription":{"@Type":"PassportNumber","@DataType":"xs:integer","@Encoding":"int"},"AttributeValue":{"@type":"xs:int","$":"1"}},{"AttributeUID":"lkadsfijg8itnf14","AttributeDescription":{"@Type":"Issued","@DataType":"xs:date","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"2012-02-01+01:00"}},{"AttributeUID":"lkadsfijg8itnf15","AttributeDescription":{"@Type":"Expires","@DataType":"xs:date","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"2015-02-01+01:00"}},{"AttributeUID":"lkadsfijg8itnf16","AttributeDescription":{"@Type":"IssuedBy","@DataType":"xs:string","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"service_issuer_integration_test"}}]}},{"key":"24d4e9eb-9fbf-4e03-8044-37c3810eaa35","value":{"CredentialUID":"24d4e9eb-9fbf-4e03-8044-37c3810eaa35","CredentialSpecificationUID":"http://visa.com/creditcard/specification","IssuerParametersUID":"http://thebestbank.com/cc/issuancekey_v1.0","Attribute":[{"AttributeUID":"lkadsfijg8itnf10","AttributeDescription":{"@Type":"http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle","@DataType":"xs:anyURI","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"http://amex.com/amexcard/revocation/parameters"}},{"AttributeUID":"lkadsfijg8itnf11","AttributeDescription":{"@Type":"CardType","@DataType":"xs:string","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"Normal"}},{"AttributeUID":"lkadsfijg8itnf12","AttributeDescription":{"@Type":"Name","@DataType":"xs:string","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"Stewart"}},{"AttributeUID":"lkadsfijg8itnf13","AttributeDescription":{"@Type":"LastName","@DataType":"xs:string","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"Dent"}},{"AttributeUID":"lkadsfijg8itnf14","AttributeDescription":{"@Type":"CardNumber","@DataType":"xs:integer","@Encoding":"int"},"AttributeValue":{"@type":"xs:int","$":"1"}},{"AttributeUID":"lkadsfijg8itnf15","AttributeDescription":{"@Type":"ExpirationDate","@DataType":"xs:date","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"2015-02-01+01:00"}},{"AttributeUID":"lkadsfijg8itnf16","AttributeDescription":{"@Type":"SecurityCode","@DataType":"xs:integer","@Encoding":"int"},"AttributeValue":{"@type":"xs:int","$":"1"}},{"AttributeUID":"lkadsfijg8itnf17","AttributeDescription":{"@Type":"Status","@DataType":"xs:anyURI","@Encoding":"xenc:sha256"},"AttributeValue":{"@type":"xs:string","$":"status"}}]}}]},"pseudonyms":{"entry":[{"key":"pseudonym/5fctpxdv2a6srtt6","value":{"Pseudonym":{"@Exclusive":"false","@Scope":"http://www.sweetdreamsuites.com","@PseudonymUID":"pseudonym/5fctpxdv2a6srtt6"}}},{"key":"pseudonym/3mwiyl5fuaq9qp7f","value":{"Pseudonym":{"@Exclusive":"false","@Scope":"http://www.sweetdreamsuites.com","@PseudonymUID":"pseudonym/3mwiyl5fuaq9qp7f"}}}]},"CandidatePresentationTokenList":{"CandidatePresentationToken":[{"token":{"@PolicyUID":"http://www.sweetdreamsuites.com/policies/booking/standard","@TokenUID":"abc4trust.eu/token-uid/2zfisjaq4a2dranx","Message":{"Nonce":"SGVsbG9Xb3JsZAo=","ApplicationData":"RoomType : Double\nBedsize : King\nArrivalDate : 2012-04-01\nNrOfNights :  2\nReservationCode : HCJ095\nI agree to the terms of service and cancellation policy."},"Pseudonym":{"@Exclusive":"false","@Scope":"http://www.sweetdreamsuites.com","@Alias":"#mainPseudonym","PseudonymValue":"Kis="},"Credential":[{"@Alias":"#passport","CredentialSpecUID":"http://www.admin.ch/passport/specification","IssuerParametersUID":"http://www.admin.ch/passport/issuancekey_v1.0","RevocationInformationUID":"http://admin.ch/passport/revocation/parameters"},{"@Alias":"#creditcard","CredentialSpecUID":"http://visa.com/creditcard/specification","IssuerParametersUID":"http://thebestbank.com/cc/issuancekey_v1.0","DisclosedAttribute":{"@AttributeType":"CardNumber","@DataHandlingPolicy":"http://www.sweetdreamsuites.com/policies/creditcards","InspectorPublicKeyUID":"http://admin.ch/inspector/pub_key_v1","InspectionGrounds":"In case of no free cancellation and no show the credit card number should be Disclosed to the hotel."}}],"AttributePredicate":{"@Function":"urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal","Attribute":{"@CredentialAlias":"#creditcard","@AttributeType":"ExpirationDate"},"ConstantValue":"2012-01-06Z"}},"CredentialUidList":{"CredentialUid":["b07b2aab-4b00-4b12-95d2-8b4c8fb79329","24d4e9eb-9fbf-4e03-8044-37c3810eaa35"]},"PseudonymChoiceList":{"PseudonymChoice":null},"InspectorChoiceList":{"InspectorChoice":null}},{"token":{"@PolicyUID":"http://www.sweetdreamsuites.com/policies/booking/standard","@TokenUID":"abc4trust.eu/token-uid/106tt0e2ojwpt2yj","Message":{"Nonce":"SGVsbG9Xb3JsZAo=","ApplicationData":"RoomType : Double\nBedsize : King\nArrivalDate : 2012-04-01\nNrOfNights :  2\nReservationCode : HCJ095\nI agree to the terms of service and cancellation policy."},"Pseudonym":{"@Exclusive":"false","@Scope":"http://www.sweetdreamsuites.com","@Alias":"#mainPseudonym"},"Credential":[{"@Alias":"#passport","CredentialSpecUID":"http://www.admin.ch/passport/specification","IssuerParametersUID":"http://www.admin.ch/passport/issuancekey_v1.0","RevocationInformationUID":"http://admin.ch/passport/revocation/parameters"},{"@Alias":"#creditcard","CredentialSpecUID":"http://amex.com/amexcard/specification","IssuerParametersUID":"http://www.amex.com/abc/isskey","DisclosedAttribute":{"@AttributeType":"CardNumber","@DataHandlingPolicy":"http://www.sweetdreamsuites.com/policies/creditcards","InspectorPublicKeyUID":"","InspectionGrounds":"In case of no free cancellation and no show the credit card number should be Disclosed to the hotel."}}],"AttributePredicate":{"@Function":"urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal","Attribute":{"@CredentialAlias":"#creditcard","@AttributeType":"ExpirationDate"},"ConstantValue":"2012-01-06Z"}},"CredentialUidList":{"CredentialUid":["b07b2aab-4b00-4b12-95d2-8b4c8fb79329","704eea8d-c148-42cb-bfb6-3dd1328d87d9"]},"PseudonymChoiceList":{"PseudonymChoice":null},"InspectorChoiceList":{"InspectorChoice":null}},{"token":{"@PolicyUID":"http://www.sweetdreamsuites.com/policies/booking/studentcardoffer","@TokenUID":"abc4trust.eu/token-uid/3tl3k9nwfwjsyg1v","Message":{"Nonce":"SGVsbG9Xb3JsZAo=","ApplicationData":"RoomType : Double\nBedsize : King\nArrivalDate : 2012-04-01\nNrOfNights :  2\nReservationCode : HCJ095\nI agree to the terms of service and cancellation policy."},"Pseudonym":{"@Exclusive":"false","@Scope":"http://www.sweetdreamsuites.com","@Alias":"#mainPseudonym"},"Credential":[{"@Alias":"#passport","CredentialSpecUID":"http://www.admin.ch/passport/specification","IssuerParametersUID":"http://www.admin.ch/passport/issuancekey_v1.0","RevocationInformationUID":"http://www.admin.ch/passport/revocation/parameters"},{"@Alias":"#creditcard","CredentialSpecUID":"http://visa.com/creditcard/specification","IssuerParametersUID":"http://thebestbank.com/cc/issuancekey_v1.0","DisclosedAttribute":{"@AttributeType":"CardNumber","@DataHandlingPolicy":"http://www.sweetdreamsuites.com/policies/creditcards","InspectorPublicKeyUID":"","InspectionGrounds":"In case of no free cancellation and no show, the credit card number should be Disclosed to the hotel."}},{"@Alias":"#studentid","CredentialSpecUID":"http://www.ethz.ch/studentid/specification","IssuerParametersUID":"http://www.ethz.ch/studentid/issuancekey_v1.0","RevocationInformationUID":"http://www.ethz.ch/studentid/revocation/information"}],"AttributePredicate":{"@Function":"urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal","Attribute":{"@CredentialAlias":"#creditcard","@AttributeType":"ExpirationDate"},"ConstantValue":"2012-01-06Z"},"VerifierDrivenRevocation":{"RevocationInformationUID":"no:revocation:information:yet","Attribute":{"@AttributeType":"PassportNumber","@CredentialAlias":"#passport"}}},"CredentialUidList":{"CredentialUid":["b07b2aab-4b00-4b12-95d2-8b4c8fb79329","24d4e9eb-9fbf-4e03-8044-37c3810eaa35","a677febd-8f28-4688-9eb6-0133cc2be072"]},"PseudonymChoiceList":{"PseudonymChoice":null},"InspectorChoiceList":{"InspectorChoice":null}}]}};
	return idselectionJSON;
}
