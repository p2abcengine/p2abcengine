//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.14                                           *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
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

package eu.abc4trust.ri.ui.user;



public class PresentationContainer {

	private String resource = "";
	private String policyXml = "";
	private String stringToken = "";
	private String successUrl = "";
	private String failureUrl = "";
	
	public PresentationContainer(final String resource, final String policyXml, final String successUrl, final String failureUrl) {
		this.resource = resource;
		this.policyXml = policyXml;
		this.successUrl = successUrl;
		this.failureUrl = failureUrl;
	}
	
	public void setToken(final String stringToken) {
		this.stringToken = stringToken;
	}
	
	public String getResource() {
		return resource;
	}
	
	public String getPolicyXml() {
		return policyXml;
	}
	
	public String getSuccessUrl() {
		return successUrl;
	}
	
	public String getFailureUrl() {
		return failureUrl;
	}
	
	public String getStringToken() {
		return stringToken;
	}
}
