//* Licensed Materials - Property of                                  *
//* Miracle A/S                                                       *
//* Alexandra Instituttet A/S                                         *
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
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
package eu.abc4trust.util;

import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.util.XmlUtils;

public class ParameterUtil {

	private static final String ISSUER_PARAMETERS_ID_CARD_IDEMIX = "/eu/abc4trust/sampleXml/issuance/parameters/IdCardIssuerParametersIdemix.xml";
	private static final String ISSUER_SECRET_KEY_ID_CARD_IDEMIX = "/eu/abc4trust/sampleXml/issuance/parameters/IdCardIssuerSecretKeyIdemix.xml";

	private static final String ISSUER_PARAMETERS_ID_CARD_UPROVE = "/eu/abc4trust/sampleXml/issuance/parameters/IdCardIssuerParametersUProve.xml";
	private static final String ISSUER_SECRET_KEY_ID_CARD_UPROVE = "/eu/abc4trust/sampleXml/issuance/parameters/IdCardIssuerSecretKeyUProve.xml";
	
	private static final String ISSUER_PARAMETERS_STUDENT_CARD_IDEMIX = "/eu/abc4trust/sampleXml/issuance/parameters/studentCardIssuerParametersIdemix.xml";
	private static final String ISSUER_SECRET_KEY_STUDENT_CARD_IDEMIX = "/eu/abc4trust/sampleXml/issuance/parameters/studentCardIssuerSecretKeyIdemix.xml";

	private static final String ISSUER_PARAMETERS_STUDENT_CARD_UPROVE = "/eu/abc4trust/sampleXml/issuance/parameters/studentCardIssuerParametersUProve.xml";
    private static final String ISSUER_SECRET_KEY_STUDENT_CARD_UPROVE = "/eu/abc4trust/sampleXml/issuance/parameters/studentCardIssuerSecretKeyUProve.xml";
	
	/**
	 * Returns pregenerated issuer parameters for an id-card credential
	 * using revocationUID: revocationUID1 
	 * @return
	 */
	public static IssuerParameters getIdCardIssuerParametersIdemix(){
		try {
			return (IssuerParameters) XmlUtils.getObjectFromXML(
			    ParameterUtil.class.getClass().getResourceAsStream(
			        ISSUER_PARAMETERS_ID_CARD_IDEMIX), true);
		} catch (UnsupportedEncodingException e) {
		} catch (JAXBException e) {
		} catch (SAXException e) {
		}
		return null;
	}

	/**
	 * Returns pregenerated secret key for a id- card credential 
	 * @return
	 */
	public static SecretKey getIdCardIssuerSecretKeyIdemix(){
		try {
			return (SecretKey) XmlUtils.getObjectFromXML(
			    ParameterUtil.class.getClass().getResourceAsStream(
			        ISSUER_SECRET_KEY_ID_CARD_IDEMIX), true);
		} catch (UnsupportedEncodingException e) {
		} catch (JAXBException e) {
		} catch (SAXException e) {
		}
		return null;
	}
	
	/**
     * Returns pregenerated issuer parameters for an id-card credential
     * using revocationUID: revocationUID1 
     * @return
     */
    public static IssuerParameters getIdCardIssuerParametersUProve(){
        try {
            return (IssuerParameters) XmlUtils.getObjectFromXML(
                ParameterUtil.class.getClass().getResourceAsStream(
                    ISSUER_PARAMETERS_ID_CARD_UPROVE), true);
        } catch (UnsupportedEncodingException e) {
        } catch (JAXBException e) {
        } catch (SAXException e) {
        }
        return null;
    }

    /**
     * Returns pregenerated secret key for a id- card credential 
     * @return
     */
    public static SecretKey getIdCardIssuerSecretKeyUProve(){
        try {
            return (SecretKey) XmlUtils.getObjectFromXML(
                ParameterUtil.class.getClass().getResourceAsStream(
                    ISSUER_SECRET_KEY_ID_CARD_UPROVE), true);
        } catch (UnsupportedEncodingException e) {
        } catch (JAXBException e) {
        } catch (SAXException e) {
        }
        return null;
    }
	
	/**
	 * Returns pregenerated secret key for a student-card credential 
	 * @return
	 */
	public static SecretKey getStudentCardIssuerSecretKeyIdemix(){
		try {
			return (SecretKey) XmlUtils.getObjectFromXML(
			    ParameterUtil.class.getClass().getResourceAsStream(
			        ISSUER_SECRET_KEY_STUDENT_CARD_IDEMIX), true);
		} catch (UnsupportedEncodingException e) {
		} catch (JAXBException e) {
		} catch (SAXException e) {
		}
		return null;
	}
	
	/**
	 * Returns pregenerated issuer parameters for an student-card credential
	 * using revocationUID: revocationUID2 
	 * @return
	 */
	public static IssuerParameters getStudentCardIssuerParametersIdemix(){
		try {
			return (IssuerParameters) XmlUtils.getObjectFromXML(
			    ParameterUtil.class.getClass().getResourceAsStream(
			        ISSUER_PARAMETERS_STUDENT_CARD_IDEMIX), true);
		} catch (UnsupportedEncodingException e) {
		} catch (JAXBException e) {
		} catch (SAXException e) {
		}
		return null;
	}

	/**
     * Returns pregenerated secret key for a student-card credential 
     * @return
     */
    public static SecretKey getStudentCardIssuerSecretKeyUProve(){
        try {
            return (SecretKey) XmlUtils.getObjectFromXML(
                ParameterUtil.class.getClass().getResourceAsStream(
                    ISSUER_SECRET_KEY_STUDENT_CARD_UPROVE), true);
        } catch (UnsupportedEncodingException e) {
        } catch (JAXBException e) {
        } catch (SAXException e) {
        }
        return null;
    }
    
    /**
     * Returns pregenerated issuer parameters for an student-card credential
     * using revocationUID: revocationUID2 
     * @return
     */
    public static IssuerParameters getStudentCardIssuerParametersUProve(){
        try {
            return (IssuerParameters) XmlUtils.getObjectFromXML(
                ParameterUtil.class.getClass().getResourceAsStream(
                    ISSUER_PARAMETERS_STUDENT_CARD_UPROVE), true);
        } catch (UnsupportedEncodingException e) {
        } catch (JAXBException e) {
        } catch (SAXException e) {
        }
        return null;
    }
	
	
}
