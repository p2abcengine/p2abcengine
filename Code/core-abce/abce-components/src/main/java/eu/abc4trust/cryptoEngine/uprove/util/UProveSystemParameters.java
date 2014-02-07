//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*                                                                   *
//* This file is licensed under the Apache License, Version 2.0 (the  *
//* "License"); you may not use this file except in compliance with   *
//* the License. You may obtain a copy of the License at:             *
//*   http://www.apache.org/licenses/LICENSE-2.0                      *
//* Unless required by applicable law or agreed to in writing,        *
//* software distributed under the License is distributed on an       *
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY            *
//* KIND, either express or implied.  See the License for the         *
//* specific language governing permissions and limitations           *
//* under the License.                                                *
//*/**/****************************************************************

package eu.abc4trust.cryptoEngine.uprove.util;

import java.util.List;

import org.w3c.dom.Element;

import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.util.XmlUtils;


/**
 * Handles reading and parsing UProve specific SystemParamters For now they can be represented as
 * XML. Handles mixing parameters with Idemix Parameters inside same ABC4Trust SystemParameter
 * XML/JaxB
 */
public class UProveSystemParameters {

  private final SystemParameters systemParameters;
  private int keyLength = -1;
  private String groupOID = null;
  private int numberOfTokens = -1;
  
  
  public UProveSystemParameters(SystemParameters syspars) {
    this.systemParameters = syspars;
  }
  
  public int getKeyLength() {
    if (keyLength > -1) {
      return keyLength;
    }
    List<Object> list = this.systemParameters.getAny();
    // assume uprove is last
    for (int i = list.size(); i > 0; i--) {
      Object o = list.get(i - 1);
      if (!(o instanceof Element)) {
        continue;
      }
      Element element = (Element) o;
      String localName = element.getNodeName();
      if ("uprove:KeyLength".equals(localName)) {
        String value = element.getTextContent();
        keyLength = Integer.parseInt(value);
        return keyLength;
      }
    }
    try {
      throw new IllegalStateException("UProveSystemParameters not found "
          + XmlUtils.toNormalizedXML(new ObjectFactory().createSystemParameters(systemParameters)));
    } catch (Exception e) {
    	e.printStackTrace();
    	System.err.println("System parameters is null: " + (systemParameters == null));
      throw new IllegalStateException("Failed to print debug of (UProve) SystemParameters", e);
    }
  }

  public String getGroupOID() {
    if (groupOID != null) {
      return groupOID;
    }
    List<Object> list = this.systemParameters.getAny();
    // assume uprove is last
    for (int i = list.size(); i > 0; i--) {
      Object o = list.get(i - 1);
      if (!(o instanceof Element)) {
        continue;
      }
      Element element = (Element) o;
      String localName = element.getNodeName();
      if ("GroupParameters".equals(localName)) {
        groupOID = element.getTextContent();
        return groupOID;
      }
    }
    return groupOID;
  }
  public int getNumberOfTokens() {
    if (numberOfTokens > -1) {
      return numberOfTokens;
    }
    List<Object> list = this.systemParameters.getAny();
    // assume uprove is last
    for (int i = list.size(); i > 0; i--) {
      Object o = list.get(i - 1);
      if (!(o instanceof Element)) {
        continue;
      }
      Element element = (Element) o;
      String localName = element.getNodeName();
      if ("uprove:NumberOfTokens".equals(localName)) {
        String value = element.getTextContent();
        numberOfTokens = Integer.parseInt(value);
        return numberOfTokens;
      }
    }
    numberOfTokens = 50;
    return numberOfTokens;
  }
}
