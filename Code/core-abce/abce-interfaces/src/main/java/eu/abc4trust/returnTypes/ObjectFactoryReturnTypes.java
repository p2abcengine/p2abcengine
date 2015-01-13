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

package eu.abc4trust.returnTypes;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactoryReturnTypes {
    @XmlElementDecl(namespace = "http://abc4trust.eu/wp2/abcschemav1.0", name = "SitdReturn")
    public static JAXBElement<SitdReturn> wrap(SitdReturn sitd) {
        return new JAXBElement<SitdReturn>(new QName("http://abc4trust.eu/wp2/abcschemav1.0", "SitdReturn"), SitdReturn.class, null, sitd);
    }
    @XmlElementDecl(namespace = "http://abc4trust.eu/wp2/abcschemav1.0", name = "SptdReturn")
    public static JAXBElement<SptdReturn> wrap(SptdReturn sptd) {
        return new JAXBElement<SptdReturn>(new QName("http://abc4trust.eu/wp2/abcschemav1.0", "SptdReturn"), SptdReturn.class, null, sptd);
    }
    @XmlElementDecl(namespace = "http://abc4trust.eu/wp2/abcschemav1.0", name = "SptdArguments")
    public static JAXBElement<SptdArguments> wrap(SptdArguments sptd) {
        return new JAXBElement<SptdArguments>(new QName("http://abc4trust.eu/wp2/abcschemav1.0", "SptdArguments"), SptdArguments.class, null, sptd);
    }
    @XmlElementDecl(namespace = "http://abc4trust.eu/wp2/abcschemav1.0", name = "SitdArguments")
    public static JAXBElement<SitdArguments> wrap(SitdArguments sptd) {
        return new JAXBElement<SitdArguments>(new QName("http://abc4trust.eu/wp2/abcschemav1.0", "SitdArguments"), SitdArguments.class, null, sptd);
    }

    @XmlElementDecl(namespace = "http://abc4trust.eu/wp2/abcschemav1.0", name = "UiPresentationReturn")
    public static JAXBElement<UiPresentationReturn> wrap(UiPresentationReturn r) {
        return new JAXBElement<UiPresentationReturn>(new QName("http://abc4trust.eu/wp2/abcschemav1.0", "UiPresentationReturn"), UiPresentationReturn.class, null, r);
    }
    @XmlElementDecl(namespace = "http://abc4trust.eu/wp2/abcschemav1.0", name = "UiIssuanceReturn")
    public static JAXBElement<UiIssuanceReturn> wrap(UiIssuanceReturn r) {
        return new JAXBElement<UiIssuanceReturn>(new QName("http://abc4trust.eu/wp2/abcschemav1.0", "UiIssuanceReturn"), UiIssuanceReturn.class, null, r);
    }

    @XmlElementDecl(namespace = "http://abc4trust.eu/wp2/abcschemav1.0", name = "UiPresentationArguments")
    public static JAXBElement<UiPresentationArguments> wrap(UiPresentationArguments a) {
        return new JAXBElement<UiPresentationArguments>(new QName("http://abc4trust.eu/wp2/abcschemav1.0", "UiPresentationArguments"), UiPresentationArguments.class, null, a);
    }
    @XmlElementDecl(namespace = "http://abc4trust.eu/wp2/abcschemav1.0", name = "UiIssuanceArguments")
    public static JAXBElement<UiIssuanceArguments> wrap(UiIssuanceArguments a) {
        return new JAXBElement<UiIssuanceArguments>(new QName("http://abc4trust.eu/wp2/abcschemav1.0", "UiIssuanceArguments"), UiIssuanceArguments.class, null, a);
    }

    @XmlElementDecl(namespace = "http://abc4trust.eu/wp2/abcschemav1.0", name = "UiManageCredentialData")
    public static JAXBElement<UiManageCredentialData> wrap(UiManageCredentialData r) {
        return new JAXBElement<UiManageCredentialData>(new QName("http://abc4trust.eu/wp2/abcschemav1.0", "UiManageCredentialData"), UiManageCredentialData.class, null, r);
    }

    @XmlElementDecl(namespace = "http://abc4trust.eu/wp2/abcschemav1.0", name = "IssuanceReturn")
    public static JAXBElement<IssuanceReturn> wrap(IssuanceReturn r) {
        return new JAXBElement<IssuanceReturn>(new QName(
                "http://abc4trust.eu/wp2/abcschemav1.0", "IssuanceReturn"),
                IssuanceReturn.class, null, r);
    }

}
