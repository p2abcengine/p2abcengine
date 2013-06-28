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
