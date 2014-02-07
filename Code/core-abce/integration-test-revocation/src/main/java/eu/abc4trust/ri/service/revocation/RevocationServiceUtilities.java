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

package eu.abc4trust.ri.service.revocation;

import javax.xml.bind.JAXBElement;

import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.ObjectFactory;

public class RevocationServiceUtilities {
    
    //helper routine - for tests
    //wraps a boolean as a AttributeValue (inside a attribute)
    //The /any/ list inside the RevocationMessage requires an xml schema to serialize/deserialize 
    //properly, this is the intention that 3d party software vendor should provide this.
    //For our internal tests we reuse the existing schema.
    public static JAXBElement<Attribute> getWrapper(String val) {
        ObjectFactory of = new ObjectFactory();
        Attribute s = new Attribute();
        s.setAttributeValue(val);
        return of.createAttribute(s);
    }
        
    //helper routine for extracting a string of an JAXBElement<Attribute>
    //warning suppressed, since we are validating the cast and the cast is unavoidable (with generics)
    @SuppressWarnings("unchecked")
    private static String getWrapperValue(Object o) throws Exception    {
        JAXBElement<Attribute> tmp = null;
        try {
            tmp = (JAXBElement<Attribute>) o;
        } catch (ClassCastException e)
        {
            throw new Exception("Expected an Attribute. Received something different:" + o.getClass());
        }
        
        Attribute s = tmp.getValue();
        return (String) s.getAttributeValue();
    }

    //helper routine - used for extracting an Integer from an JAXBElement<Attribute>
    public static Integer getInteger(Object o) throws Exception    {
        return new Integer(getWrapperValue(o));
    }

    //helper routine - used for extracting an Integer from an JAXBElement<Attribute>
    public static String getString(Object o) throws Exception    {
        return getWrapperValue(o);
    }

}
