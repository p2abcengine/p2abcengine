//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.util;

import java.net.URI;




public class MyAttributeReference{

  private String value;
  private String credAlias;
  private String attrType;
  
  
  public MyAttributeReference(URI theCredAlias, URI theAttrType) {
    value = new String(theCredAlias.toString() + theAttrType.toString());
    credAlias = theCredAlias.toString();
    attrType = new String(theAttrType.toString());
  }
   
  @Override
  public boolean equals(Object arg0) {
		if (arg0 instanceof MyAttributeReference)
			return this.getAttributeReference().equals(((MyAttributeReference) arg0).getAttributeReference());
		else
			return false;
	}
	
  @Override
  public int hashCode() {
	return this.getAttributeReference().hashCode();
  }
  
  public boolean isConstant() {
	    return (value.equals(Constants.CONSTANT));   
	}

	/**
     *
	 * @return the deterministic string representation of the given attribute reference.
	 */
  public String getAttributeReference() {
    return value;
  }

  public String getCredentialAlias() {
	return credAlias;
  }

  public String getAttributeType() {
	return attrType;
  }
  
  public void replaceRefValueToConstant(){
	  value = Constants.CONSTANT;
	  credAlias = "";
	  attrType = Constants.CONSTANT;
  }

}
