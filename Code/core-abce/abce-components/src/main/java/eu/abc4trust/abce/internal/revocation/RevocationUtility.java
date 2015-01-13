//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
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

package eu.abc4trust.abce.internal.revocation;

import java.net.URI;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import eu.abc4trust.revocationProxy.RevocationMessageType;

public class RevocationUtility {



    public static Element serializeRevocationMessageType(RevocationMessageType rmt) {
        return createW3DomElement("RevocationMessageType", rmt.toString());
    }
    public static Element serializeRevocationInfoUid(URI revInfoUid) {
        return createW3DomElement("RevocationInfoUid", revInfoUid.toString());
    }
    public static Element serializeEpoch(Integer epoch) {
        return createW3DomElement("Epoch", epoch.toString());
    }
    public static RevocationMessageType unserializeRevocationMessageType(Element element) {
        RevocationMessageType rmt = RevocationMessageType.valueOf(element.getTextContent());
        return rmt;
    }
    public static URI unserializeRevocationInfoUid(Element element) {
        URI revInfoUid = URI.create(element.getTextContent());
        return revInfoUid;
    }
    public static Integer unserializeEpoch(Element element) {
        Integer epoch = new Integer(element.getTextContent());
        return epoch;
    }

    private static Element createW3DomElement(String elementName, String value) {
        Element element;
        try {
            element = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement(elementName);
        } catch (DOMException e) {
            throw new IllegalStateException("This should always work!",e);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("This should always work!",e);
        }
        element.setTextContent(value);
        return element;
    }

}
