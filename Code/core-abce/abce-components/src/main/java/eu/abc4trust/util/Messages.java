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

package eu.abc4trust.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class Messages {
    private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$

    //constants:
    public static final String NO_FRIENDLY_DESCRIPTION="NoFriendlyDescription";
    public static final String FROM="From";
    public static final String THE_VALUE_OF="TheValueOf";
    public static final String EQUAL="EqualTo";
    public static final String NOTEQUAL="NotEqualTo";
    public static final String GREATER="GreaterThan";
    public static final String GREATEREQ="GreaterOrEqualTo";
    public static final String LESS="LessThan";
    public static final String LESSEQ="LessOrEqualTo";
    public static final String EQUALONEOF="EqualOneOf";
    public static final String GREATER_DATE = "DateGreaterThan";
    public static final String GREATEREQ_DATE = "DateGreaterOrEqualTo";
    public static final String LESS_DATE = "DateLessThan";
    public static final String LESSEQ_DATE = "DateLessOrEqualTo";


    private Messages() {
    }

    public static String getString(String key, String lang) {
        try {
            Locale locale = new Locale(lang);
            return ResourceBundle.getBundle(BUNDLE_NAME,locale).getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}

