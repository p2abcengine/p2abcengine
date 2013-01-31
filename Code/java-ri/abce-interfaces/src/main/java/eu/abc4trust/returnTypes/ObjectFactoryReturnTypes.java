//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
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
}
