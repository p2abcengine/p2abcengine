//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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
