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

package eu.abc4trust.abce.testharness;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

public final class ImagePathBuilder {
	private ImagePathBuilder() {
		throw new AssertionError();
	}

	public static final String TEST_IMAGE_PATH = setupTestImagePath();
	public static final URI TEST_IMAGE_JPG = createJpgTestURI();
	public static final String TEST_IMAGE_JPG_STRING = setupJpgTestImage();
	public static final URI TEST_IMAGE_PNG = createPngTestURI();
	public static final String TEST_IMAGE_PNG_STRING = setupPngTestImage();
	public static final String DEFAULT_IMAGE = setupDefaultImage();

	private static String setupTestImagePath() {
		StringBuilder builder = new StringBuilder();
		String osSep = System.getProperty("file.separator");
		String currentDir = System.getProperty("user.dir");
		builder.append(currentDir);	
		builder.append(osSep);
		ArrayList<String> path = new ArrayList<String>(
				Arrays.asList("src", "test", "resources", "eu", "abc4trust","sampleImages"));
		for (String p : path) {
			builder.append(p);
			builder.append(osSep);
		}
		String res = builder.toString();        
		return res;

	}

    private static String setupJpgTestImage() {
            String filePath = TEST_IMAGE_PATH + "vi sa.jpg";
            return filePath;
    }

	private static URI createJpgTestURI() {
		try {
			return new URI("file", setupJpgTestImage(), null);
		} catch (URISyntaxException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
	}

    private static String setupDefaultImage() {
        return TEST_IMAGE_PATH + "default.jpg";
    }
    
    private static String setupPngTestImage() {
        String filePath = TEST_IMAGE_PATH + "visa.png";
        return filePath;
    }
 
    private static URI createPngTestURI() {
        try {
            return new URI("file", setupPngTestImage(), null);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
	
}
