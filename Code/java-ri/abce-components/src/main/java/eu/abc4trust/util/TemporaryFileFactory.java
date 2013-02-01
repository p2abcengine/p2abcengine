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

package eu.abc4trust.util;

import java.io.File;
import java.io.IOException;

public class TemporaryFileFactory {

    public static File createTemporaryFile() {
        File temp = null;
        try {
            temp = File.createTempFile("tmp", ".tmp");
            temp.deleteOnExit();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return temp;
    }

    public static File createTemporaryDir() {
        File temp = createTemporaryFile();
        return new File(temp.getParent());
    }
}
