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

package eu.abc4trust.smartcard;

import java.io.Serializable;
import java.net.URI;

public class TrustedIssuerParameters implements Serializable {

	// If updated - change serialVersionUID
	private static final long serialVersionUID = 1L;

	//
	public final URI parametersUri;
	public final GroupParameters groupParams;
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
	public TrustedIssuerParameters(int courseID, URI parametersUri, GroupParameters groupParams,
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
	public TrustedIssuerParameters(URI parametersUri, GroupParameters groupParams) {
		this.parametersUri = parametersUri;
		this.groupParams = groupParams;
		this.enforceAttendanceCheck = false;
		this.course = null;
	}

}
