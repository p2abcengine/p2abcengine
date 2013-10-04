//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

package eu.abc4trust.xml.util;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class AbcNamespace extends NamespacePrefixMapper {

    @Override
    public String getPreferredPrefix(String base, String other, boolean arg2) {
        if (base.startsWith("http://abc4trust.eu/wp2")) {
            return "abc";
        } else if(base.equals("http://www.w3.org/2001/XMLSchema")) {
            return "xs";
        } else if(base.equals("http://www.w3.org/2001/XMLSchema-instance")) {
            return "xsi";
        } else {
            return other;
        }
    }

}
