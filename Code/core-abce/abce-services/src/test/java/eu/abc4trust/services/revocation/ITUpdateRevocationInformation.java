//* Licensed Materials - Property of                                  *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
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

package eu.abc4trust.services.revocation;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import eu.abc4trust.services.ITAbstract;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.RevocationInformation;

public class ITUpdateRevocationInformation extends ITAbstract {

    static ObjectFactory of = new ObjectFactory();

    @Test
    public void updateRevocationInformation() throws Exception {
    	RevocationServiceFactory revocationServiceFactory = new RevocationServiceFactory();

        String revocationAuthorityParametersUid = "urn:revocationauthorityparameters:test:foobar";
        
        RevocationInformation revocationInformation = revocationServiceFactory
        		.updateRevocationInformation(revocationAuthorityParametersUid);
        
        assertNotNull(revocationInformation);
    }
}