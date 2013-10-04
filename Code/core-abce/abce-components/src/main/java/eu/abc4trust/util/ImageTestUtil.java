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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class ImageTestUtil {

    public static boolean compareImages(File file1, File file2)
            throws IOException {
        FileInputStream fis1;
        fis1 = new FileInputStream(file1);
        FileInputStream fis2 = new FileInputStream(file2);
        int size = 261120;
        byte[] buffer1 = new byte[size];
        int bytesRead1 = 0;
        byte[] buffer2 = new byte[size];
        int bytesRead2 = 0;
        boolean done = false;
        while (!done) {
            bytesRead1 = fis1.read(buffer1);
            bytesRead2 = fis2.read(buffer2);
            if ((bytesRead1 != bytesRead2) || !Arrays.equals(buffer1, buffer2)) {
            	fis1.close();
            	fis2.close();
                return false;
            }
            if (((bytesRead1 == 0) && (bytesRead2 != 0))
                    || ((bytesRead1 != 0) && (bytesRead2 == 0))) {
            	fis1.close();
            	fis2.close();
                return false;
            }
            if ((bytesRead1 <= 0) && (bytesRead2 <= 0)) {
            	fis1.close();
            	fis2.close();
                done = true;
            }
        }
        fis1.close();
        fis2.close();
        return true;
    }

}
