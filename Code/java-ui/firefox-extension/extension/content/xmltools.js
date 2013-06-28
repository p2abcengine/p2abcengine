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

function appendNodeContents(cnode){
	var xmlstring = "";
	if(cnode.nodeType == 3) {
		xmlstring = cnode.nodeValue;
	}else{
		xmlstring = "<"+cnode.nodeName;
		if(cnode.attributes != null) {
			for(i = 0; i<cnode.attributes.length; i++){
				xmlstring += " "+cnode.attributes[i].nodeName+"=\""+cnode.attributes[i].nodeValue+"\"";
			}
		}
		xmlstring += ">";

		for(var i = 0; i<cnode.childNodes.length; i++){
				xmlstring += appendNodeContents(cnode.childNodes[i]);
		}
		xmlstring += "</"+cnode.nodeName+">";
	}
	return xmlstring
}
