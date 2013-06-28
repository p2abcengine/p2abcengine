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

package eu.abce4trust.ri.test.tools;

import java.net.ConnectException;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.WebResource.Builder;

public class ServiceProxy {

    protected <T> T getMessage(String urlString, Builder resource,
            Class<T> clazz) {
        T r = null;
        try {
            r = resource.get(clazz);
        } catch (ClientHandlerException ex) {
            if (ex.getCause() instanceof ConnectException) {
                this.exit("Could not access service: " + urlString);
            }
        }
        return r;
    }

    protected <T> T postMessage(String urlStr, Builder resource, Class<T> clazz) {
        T r = null;
        try {
            r = resource.post(clazz);
        } catch (ClientHandlerException ex) {
            if (ex.getCause() instanceof ConnectException) {
                this.exit("Could not access service: " + urlStr);
            }
        }
        return r;
    }

    protected <T> T postMessage(String urlStr, Builder resource,
            Class<T> clazz,
            Object requestEntity) {
        T r = null;
        try {
            r = resource.post(clazz, requestEntity);
        } catch (ClientHandlerException ex) {
            if (ex.getCause() instanceof ConnectException) {
                this.exit("Could not access service: " + urlStr);
            }
        }
        return r;
    }

    protected void postMessage(String urlString, Builder resource,
            Object requestEntity) {
        try {
            resource.post(requestEntity);
        } catch (ClientHandlerException ex) {
            if (ex.getCause() instanceof ConnectException) {
                this.exit("Could not access service: " + urlString);
            }
        }
    }

    protected void exit(String string) {
        System.err.println("Terminating due to error: ");
        System.err.println("  " + string);
        System.exit(-1);
    }
}
