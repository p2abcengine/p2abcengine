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

package eu.abc4trust.cryptoEngine.uprove.user;

import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfint;
import com.microsoft.schemas._2003._10.serialization.arrays.ArrayOfstring;

public class UProveDisclosedAttributes {

    private ArrayOfstring arrayOfStringAttributesParam;
    private ArrayOfint arrayOfIntDisclosedParam;

    public UProveDisclosedAttributes(
            ArrayOfstring arrayOfStringAttributesParam,
            ArrayOfint arrayOfIntDisclosedParam) {
        this.arrayOfStringAttributesParam = arrayOfStringAttributesParam;
        this.arrayOfIntDisclosedParam = arrayOfIntDisclosedParam;
    }

    public ArrayOfstring getArrayOfStringAttributesParam() {
        return this.arrayOfStringAttributesParam;
    }

    public void setArrayOfStringAttributesParam(
            ArrayOfstring arrayOfStringAttributesParam) {
        this.arrayOfStringAttributesParam = arrayOfStringAttributesParam;
    }

    public ArrayOfint getArrayOfIntDisclosedParam() {
        return this.arrayOfIntDisclosedParam;
    }

    public void setArrayOfIntDisclosedParam(ArrayOfint arrayOfIntDisclosedParam) {
        this.arrayOfIntDisclosedParam = arrayOfIntDisclosedParam;
    }

}
