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

package eu.abc4trust.ri.ui.user.utils;

import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.rwt.SessionSingletonBase;

public class UIProperties {
    
    private static boolean allowDeletingNonRevokedCredential               = true;
    private static boolean allowEditingScopeExclusivePseudonymAlias        = true;
    private static Locale uiFallbackLocale = Locale.ENGLISH;

    // prevent instantiation from outside
    private UIProperties() {
    }

    private static boolean deploymentSpecificPropertiesInitialized = false;
    private static void readDeploymentSpecificProperties() {
    	if(deploymentSpecificPropertiesInitialized) {
    		return;
    	}
		try {
			InputStream is = UIProperties.class.getResourceAsStream("/deploymentspecific.properties");
			if(is==null) {
				// try from system classloader... no prepended
				is = ClassLoader.getSystemClassLoader().getResourceAsStream("deploymentspecific.properties");
			}
			if(is != null) {
				Properties props = new Properties();
				props.load(is);
				is.close();
				System.out.println("Found deployment specific properties : " + props);
				allowDeletingNonRevokedCredential = Boolean.parseBoolean(props.getProperty("allowDeletingNonRevokedCredential", "true"));
				allowEditingScopeExclusivePseudonymAlias = Boolean.parseBoolean(props.getProperty("allowEditingScopeExclusivePseudonymAlias", "true"));

				System.out.println("- allowDeletingNonRevokedCredential : " + allowDeletingNonRevokedCredential);
				System.out.println("- allowEditingScopeExclusivePseudonymAlias : " + allowEditingScopeExclusivePseudonymAlias);

				String uiFallbackLocaleValue = props.getProperty("uiFallbackLocale", null);
				if(uiFallbackLocaleValue!=null) {
					try {
						uiFallbackLocale = new Locale(uiFallbackLocaleValue);
						System.out.println("- uiFallbackLocale : " + uiFallbackLocale + " " + uiFallbackLocaleValue);
					} catch (Exception e) {
						System.out.println("- setting uiFallbackLocale failed for : " + uiFallbackLocaleValue + " - using default : " + uiFallbackLocale );
					}
				} else {
					System.out.println("- use default uiFallbackLocale : " + uiFallbackLocale );
				}
			} else {
				System.out.println("No deployment specific properties.");
			}
		} catch(Exception e) {
			System.err.println("Failed to read DeploymentSpecificProperties in UIProperties..");
			e.printStackTrace();
		} finally {
			deploymentSpecificPropertiesInitialized = true;
		}
    }

    public static UIProperties getSessionSingletonInstance() {
    	if(!deploymentSpecificPropertiesInitialized) {
        	readDeploymentSpecificProperties();
    	}
        return SessionSingletonBase.getInstance(UIProperties.class);
    }
       
    public boolean allowDeletingNonRevokedCredential() {
        return allowDeletingNonRevokedCredential;
    }
    
    public boolean allowEditingScopeExclusivePseudonymAlias() {
        return allowEditingScopeExclusivePseudonymAlias;
    }

	public Locale uiFallbackLocale() {
		return uiFallbackLocale;
	}
}
