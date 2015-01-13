//* Licensed Materials - Property of                                  *
//* IBM                                                               *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
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

package eu.abc4trust.smartcard;

import java.io.Serializable;
import java.net.URI;

public class TrustedIssuerParameters implements Serializable {

	// If updated - change serialVersionUID
	private static final long serialVersionUID = 1L;

	//
	public final URI parametersUri;
	public final SmartcardParameters groupParams;
	public final boolean enforceAttendanceCheck;
	// Only used if enforceAttendanceCheck=true
	public final Course course;

	/**
	 * With attendance check
	 * @param parametersUri
	 * @param groupParams
	 * @param minimumAttendance
	 * @param courseKey
	 */
	public TrustedIssuerParameters(int courseID, URI parametersUri, SmartcardParameters groupParams,
			int minimumAttendance, int keyID) {
		this.parametersUri = parametersUri;
		this.groupParams = groupParams;
		this.enforceAttendanceCheck = true;
		this.course = new Course(courseID, parametersUri, minimumAttendance, keyID);
	}

	/**
	 * Without attendance check
	 * @param parametersUri
	 * @param credBases
	 */
	public TrustedIssuerParameters(URI parametersUri, SmartcardParameters groupParams) {
		this.parametersUri = parametersUri;
		this.groupParams = groupParams;
		this.enforceAttendanceCheck = false;
		this.course = null;
	}

}
